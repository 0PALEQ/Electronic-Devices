package com.cookiecraftmods.edm.network;

import com.cookiecraftmods.edm.ElectronicDeviceMod;
import com.cookiecraftmods.edm.block.TvDevice;
import com.cookiecraftmods.edm.block.entity.TvBlockEntity;
import com.cookiecraftmods.edm.media.MediaConfig;
import com.cookiecraftmods.edm.media.MediaPermissions;
import com.cookiecraftmods.edm.media.MediaUrl;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static com.cookiecraftmods.edm.ElectronicDeviceMod.LOGGER;

public final class ModNetworking {
	public static final int MAX_URL_PACKET_LENGTH = 8192;

	private static final long MENU_SESSION_LIFETIME_MS = 5L * 60L * 1000L;
	private static final Map<UUID, MenuSession> MENU_SESSIONS = new ConcurrentHashMap<>();
	private static final Map<UUID, List<String>> MENU_LINKS = new ConcurrentHashMap<>();

	private ModNetworking() {
	}

	public static void register() {
		PayloadTypeRegistry.playS2C().register(OpenMediaScreenPayload.ID, OpenMediaScreenPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(MediaActionResultPayload.ID, MediaActionResultPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SetYoutubeUrlPayload.ID, SetYoutubeUrlPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SelectMediaPayload.ID, SelectMediaPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(RemoveMediaPayload.ID, RemoveMediaPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ToggleMediaPowerPayload.ID, ToggleMediaPowerPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SetMediaVolumePayload.ID, SetMediaVolumePayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(SetYoutubeUrlPayload.ID, (payload, context) ->
			context.server().execute(() -> addMediaUrl(context.player(), payload.token(), payload.url())));
		ServerPlayNetworking.registerGlobalReceiver(SelectMediaPayload.ID, (payload, context) ->
			context.server().execute(() -> selectMedia(context.player(), payload.token(), payload.index())));
		ServerPlayNetworking.registerGlobalReceiver(RemoveMediaPayload.ID, (payload, context) ->
			context.server().execute(() -> removeMedia(context.player(), payload.token(), payload.index())));
		ServerPlayNetworking.registerGlobalReceiver(ToggleMediaPowerPayload.ID, (payload, context) ->
			context.server().execute(() -> togglePower(context.player(), payload.token())));
		ServerPlayNetworking.registerGlobalReceiver(SetMediaVolumePayload.ID, (payload, context) ->
			context.server().execute(() -> setVolume(context.player(), payload.token(), payload.volume())));
	}

	public static void openMediaScreen(ServerPlayerEntity player, BlockPos pos) {
		ServerWorld world = player.getEntityWorld();
		TvBlockEntity tv = getTv(world, pos);
		if (tv == null) return;
		openMediaScreen(player, world, pos, tv, false);
	}

	public static boolean openMediaScreenRemote(ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
		TvBlockEntity tv = getTv(world, pos);
		if (tv == null) return false;
		openMediaScreen(player, world, pos, tv, true);
		return true;
	}

	private static void openMediaScreen(
		ServerPlayerEntity player, ServerWorld world, BlockPos pos, TvBlockEntity tv, boolean remote
	) {
		openMediaScreen(player, world, pos, tv, remote, false);
	}

	private static void openMediaScreen(
		ServerPlayerEntity player, ServerWorld world, BlockPos pos, TvBlockEntity tv, boolean remote, boolean refreshOnly
	) {
		MenuSession session = new MenuSession(
			world.getRegistryKey(), pos.toImmutable(), ThreadLocalRandom.current().nextLong(), remote,
			System.currentTimeMillis() + MENU_SESSION_LIFETIME_MS
		);
		MENU_SESSIONS.put(player.getUuid(), session);
		sendMediaScreen(player, session, world, tv, refreshOnly);
	}

	private static void sendMediaScreen(
		ServerPlayerEntity player, MenuSession session, ServerWorld world, TvBlockEntity tv, boolean refreshOnly
	) {
		MediaConfig config = MediaConfig.get();
		List<String> mediaUrls = config.mediaSources.linkWhitelistMode
			? List.copyOf(config.mediaSources.approvedLinks) : tv.getMediaUrls();
		MENU_LINKS.put(player.getUuid(), List.copyOf(mediaUrls));
		BlockState state = world.getBlockState(session.pos());
		boolean powered = state.contains(TvDevice.DISPLAY_STATE) && state.get(TvDevice.DISPLAY_STATE) == 1;

		ServerPlayNetworking.send(player, new OpenMediaScreenPayload(
			session.dimension().getValue(), session.pos(), session.token(), mediaUrls, (config.mediaSources.linkWhitelistMode ? mediaUrls.indexOf(tv.getMediaUrl()) : tv.getSelectedMediaIndex()),
			tv.getPlaybackPositionMs(), config.maxMediaUrlLength, (config.mediaSources.linkWhitelistMode ? 128 : config.maxSavedVideosPerTv), tv.getVolume(),
			powered, canEdit(player, tv), refreshOnly
		));
        if (config.mediaSources.linkWhitelistMode) {
            sendActionResult(player, session.token(), true, canEdit(player, tv)
                ? "Server whitelist: add or remove approved links for every TV."
                : "Server whitelist: choose an approved link. Only administrators can edit this list.");
        }

	}

	private static void addMediaUrl(ServerPlayerEntity player, long token, String rawUrl) {
		MenuTarget target = getMenuTarget(player, token);
		if (target == null) {
			sendActionResult(player, token, false, "The TV menu expired or the target TV is unavailable.");
			return;
		}
		TvBlockEntity tv = target.tv();
		if (!canEdit(player, tv)) {
			sendActionResult(player, token, false, "You do not have permission to edit this media list.");
			return;
		}
		MediaConfig whitelistConfig = MediaConfig.get();
        if (whitelistConfig.mediaSources.linkWhitelistMode) {
            try {
                String approved = MediaUrl.validate(rawUrl, whitelistConfig.maxMediaUrlLength);
                // Validate source rules without temporarily changing the active policy.
                var policy = whitelistConfig.mediaSources;
                if (!policy.approvedLinks.contains(approved)) {
                    if (policy.approvedLinks.size() >= 128) throw new IllegalArgumentException("The server's approved media list is full.");
                    policy.approvedLinks.add(approved);
                    try { MediaPermissions.checkSource(player, approved); }
                    catch (IllegalArgumentException exception) { policy.approvedLinks.remove(approved); throw exception; }
                    whitelistConfig.save();
                }
                refreshAndResult(player, target, "Link added to the server's approved media list.");
            } catch (IllegalArgumentException exception) {
                sendActionResult(player, token, false, exception.getMessage());
            }
            return;
        }
		if (!MediaPermissions.canAssignUrl(player)) {
			sendActionResult(player, token, false, "You do not have permission to assign media.");
			return;
		}

		final String url;
		try {
			url = MediaUrl.validate(rawUrl, MediaConfig.get().maxMediaUrlLength);
			MediaPermissions.checkSource(player, url);
		} catch (IllegalArgumentException exception) {
			sendActionResult(player, token, false, exception.getMessage());
			return;
		}

		MediaConfig config = MediaConfig.get();
		if (tv.indexOfMedia(url) < 0 && tv.getSavedVideoCount() >= config.maxSavedVideosPerTv) {
			sendActionResult(player, token, false, "This TV's saved-media list is full.");
			return;
		}

		boolean added = tv.addMediaUrl(url, player.getUuid());
		BlockState state = target.world().getBlockState(target.pos());
		TvDevice.setPowered(target.world(), target.pos(), state, true);
		LOGGER.info("Player {} {} a media item {} TV playlist at {} in {}",
			player.getName().getString(), added ? "added" : "selected", added ? "to" : "in",
			target.pos(), target.world().getRegistryKey().getValue());
		refreshAndResult(player, target, added ? "Media item saved and selected." : "Saved media item selected.");
	}

	private static boolean checkSource(ServerPlayerEntity player, long token, String url) {
		try {
			MediaPermissions.checkSource(player, url);
			return true;
		} catch (IllegalArgumentException exception) {
			sendActionResult(player, token, false, exception.getMessage());
			return false;
		}
	}

	private static void selectMedia(ServerPlayerEntity player, long token, int index) {
		MenuTarget target = getMenuTarget(player, token);
		if (target == null) {
			sendActionResult(player, token, false, "The TV menu expired or the target TV is unavailable.");
			return;
		}
        if (MediaConfig.get().mediaSources.linkWhitelistMode) {
            List<String> links = MENU_LINKS.getOrDefault(player.getUuid(), List.of());
            if (index < 0 || index >= links.size()) {
                sendActionResult(player, token, false, "That approved link no longer exists.");
                return;
            }
            String url = links.get(index);
            if (!checkSource(player, token, url)) return;
            target.tv().clearMedia();
            target.tv().addMediaUrl(url, player.getUuid());
            TvDevice.setPowered(target.world(), target.pos(), target.world().getBlockState(target.pos()), true);
            refreshAndResult(player, target, "Approved video selected.");
            return;
        }
		if (index < 0 || index >= target.tv().getSavedVideoCount()) {
			sendActionResult(player, token, false, "That saved video no longer exists.");
			return;
		}
		if (!checkSource(player, token, target.tv().getMediaUrls().get(index))) return;
		if (!target.tv().selectMedia(index)) {
			sendActionResult(player, token, false, "That saved video no longer exists.");
			return;
		}
		TvDevice.setPowered(target.world(), target.pos(), target.world().getBlockState(target.pos()), true);
		refreshAndResult(player, target, "Saved video selected.");
	}

	private static void removeMedia(ServerPlayerEntity player, long token, int index) {
		MenuTarget target = getMenuTarget(player, token);
		if (target == null) {
			sendActionResult(player, token, false, "The TV menu expired or the target TV is unavailable.");
			return;
		}
		TvBlockEntity tv = target.tv();
		if (!canEdit(player, tv)) {
			sendActionResult(player, token, false, "You do not have permission to edit this media list.");
			return;
		}
        if (MediaConfig.get().mediaSources.linkWhitelistMode) {
            var config = MediaConfig.get();
            List<String> shown = MENU_LINKS.getOrDefault(player.getUuid(), List.of());
            if (index < 0 || index >= shown.size() || !config.mediaSources.approvedLinks.contains(shown.get(index))) {
                sendActionResult(player, token, false, "That approved link no longer exists.");
                return;
            }
            config.mediaSources.approvedLinks.remove(shown.get(index));
            config.save();
            TvBlockEntity.refreshMediaPolicy(player.getEntityWorld().getServer());
            refreshAndResult(player, target, "Link removed from the server's approved media list.");
            return;
        }
		if (index < 0 || index >= tv.getSavedVideoCount()) {
			sendActionResult(player, token, false, "That saved video no longer exists.");
			return;
		}
		if (tv.getSavedVideoCount() == 1 || index == tv.getSelectedMediaIndex()) {
			TvDevice.setPowered(target.world(), target.pos(), target.world().getBlockState(target.pos()), false);
		}
		tv.removeMedia(index);
		LOGGER.info("Player {} removed a media item from TV playlist at {} in {}",
			player.getName().getString(), target.pos(), target.world().getRegistryKey().getValue());
		refreshAndResult(player, target, "Saved video removed.");
	}

	private static void togglePower(ServerPlayerEntity player, long token) {
		MenuTarget target = getMenuTarget(player, token);
		if (target == null) {
			sendActionResult(player, token, false, "The TV menu expired or the target TV is unavailable.");
			return;
		}
		BlockState state = target.world().getBlockState(target.pos());
		boolean powered = state.contains(TvDevice.DISPLAY_STATE) && state.get(TvDevice.DISPLAY_STATE) == 1;
		if (!powered && !target.tv().hasMedia()) {
			sendActionResult(player, token, false, "Save a media item before turning the TV on.");
			return;
		}
		if (!powered && !checkSource(player, token, target.tv().getMediaUrl())) return;
		TvDevice.toggle(target.world(), target.pos(), state);
		refreshAndResult(player, target, powered ? "TV turned off." : "TV turned on.");
	}

	private static void setVolume(ServerPlayerEntity player, long token, int volume) {
		MenuTarget target = getMenuTarget(player, token);
		if (target == null) {
			sendActionResult(player, token, false, "The TV menu expired or the target TV is unavailable.");
			return;
		}
		if (volume < 0 || volume > 100) {
			sendActionResult(player, token, false, "TV volume must be between 0 and 100.");
			return;
		}
		target.tv().setVolume(volume);
	}

	private static MenuTarget getMenuTarget(ServerPlayerEntity player, long token) {
		MenuSession session = MENU_SESSIONS.get(player.getUuid());
		if (session == null || session.token() != token) return null;
		if (session.expiresAtMs() < System.currentTimeMillis()) {
			MENU_SESSIONS.remove(player.getUuid(), session);
			MENU_LINKS.remove(player.getUuid());
			return null;
		}

		MinecraftServer server = player.getEntityWorld().getServer();
		ServerWorld world = server == null ? null : server.getWorld(session.dimension());
		if (world == null || !world.isChunkLoaded(session.pos())) return null;
		if (session.remote()) {
			MediaConfig config = MediaConfig.get();
			if (!config.allowCrossDimensionRemotes && world != player.getEntityWorld()) return null;
			if (config.remoteRange >= 0 && world == player.getEntityWorld()
				&& player.squaredDistanceTo(
					session.pos().getX() + 0.5, session.pos().getY() + 0.5, session.pos().getZ() + 0.5
				) > (double) config.remoteRange * config.remoteRange) {
				return null;
			}
		} else if (world != player.getEntityWorld() || player.squaredDistanceTo(
			session.pos().getX() + 0.5, session.pos().getY() + 0.5, session.pos().getZ() + 0.5
		) > 64.0) {
			return null;
		}

		TvBlockEntity tv = getTv(world, session.pos());
		if (tv == null) return null;
		MenuSession refreshed = new MenuSession(
			session.dimension(), session.pos(), session.token(), session.remote(),
			System.currentTimeMillis() + MENU_SESSION_LIFETIME_MS
		);
		MENU_SESSIONS.put(player.getUuid(), refreshed);
		return new MenuTarget(refreshed, world, session.pos(), tv);
	}

	private static TvBlockEntity getTv(ServerWorld world, BlockPos pos) {
		return world.getBlockState(pos).getBlock() instanceof TvDevice
			&& world.getBlockEntity(pos) instanceof TvBlockEntity tv ? tv : null;
	}

	private static boolean canEdit(ServerPlayerEntity player, TvBlockEntity tv) {
		return MediaConfig.get().mediaSources.linkWhitelistMode ? MediaPermissions.canManageWhitelist(player)
			: tv.isOwner(player.getUuid()) || MediaPermissions.canManageOthers(player);
	}

	private static void refreshAndResult(ServerPlayerEntity player, MenuTarget target, String message) {
        if (MediaConfig.get().mediaSources.linkWhitelistMode) {
            MinecraftServer server = player.getEntityWorld().getServer();
            if (server != null) for (ServerPlayerEntity viewer : server.getPlayerManager().getPlayerList()) {
                MenuSession session = MENU_SESSIONS.get(viewer.getUuid());
                if (session == null) continue;
                MenuTarget viewing = getMenuTarget(viewer, session.token());
                if (viewing != null) {
                    // New token rejects clicks sent from a list superseded by this update.
                    openMediaScreen(viewer, viewing.world(), viewing.pos(), viewing.tv(), session.remote(), true);
                }
            }
        } else {
		sendMediaScreen(player, target.session(), target.world(), target.tv(), false);
        }
		MenuSession current = MENU_SESSIONS.get(player.getUuid());
		sendActionResult(player, current == null ? target.session().token() : current.token(), true, message);
	}

	private static void sendActionResult(ServerPlayerEntity player, long token, boolean success, String message) {
		ServerPlayNetworking.send(player, new MediaActionResultPayload(token, success, message));
	}

	private static Identifier id(String path) {
		return Identifier.of(ElectronicDeviceMod.MOD_ID, path);
	}

	private record MenuSession(
		RegistryKey<World> dimension, BlockPos pos, long token, boolean remote, long expiresAtMs
	) {
	}

	private record MenuTarget(MenuSession session, ServerWorld world, BlockPos pos, TvBlockEntity tv) {
	}

	public record OpenMediaScreenPayload(
		Identifier dimension, BlockPos pos, long token, List<String> mediaUrls, int selectedMediaIndex,
		long playbackPositionMs, int maxUrlLength, int maxSavedVideos, int volume, boolean powered, boolean canEdit, boolean refreshOnly
	) implements CustomPayload {
		public static final CustomPayload.Id<OpenMediaScreenPayload> ID = new CustomPayload.Id<>(id("open_media_screen"));
		public static final PacketCodec<RegistryByteBuf, OpenMediaScreenPayload> CODEC =
			PacketCodec.of(OpenMediaScreenPayload::write, OpenMediaScreenPayload::read);

		private static OpenMediaScreenPayload read(RegistryByteBuf buffer) {
			Identifier dimension = buffer.readIdentifier();
			BlockPos pos = buffer.readBlockPos();
			long token = buffer.readLong();
			int mediaCount = buffer.readVarInt();
			if (mediaCount < 0 || mediaCount > TvBlockEntity.MAX_SAVED_VIDEOS) {
				throw new IllegalStateException("Invalid TV playlist size: " + mediaCount);
			}
			List<String> mediaUrls = new ArrayList<>(mediaCount);
			for (int index = 0; index < mediaCount; index++) {
				mediaUrls.add(buffer.readString(MAX_URL_PACKET_LENGTH));
			}
			int selectedMediaIndex = buffer.readVarInt();
			long playbackPositionMs = buffer.readLong();
			int maxUrlLength = buffer.readVarInt();
			int maxSavedVideos = buffer.readVarInt();
			int volume = buffer.readVarInt();
			boolean powered = buffer.readBoolean();
			boolean canEdit = buffer.readBoolean();
			boolean refreshOnly = buffer.readBoolean();
			return new OpenMediaScreenPayload(
				dimension, pos, token, mediaUrls, selectedMediaIndex, playbackPositionMs,
				maxUrlLength, maxSavedVideos, volume, powered, canEdit, refreshOnly
			);
		}

		private void write(RegistryByteBuf buffer) {
			buffer.writeIdentifier(dimension);
			buffer.writeBlockPos(pos);
			buffer.writeLong(token);
			buffer.writeVarInt(mediaUrls.size());
			for (String url : mediaUrls) buffer.writeString(url, MAX_URL_PACKET_LENGTH);
			buffer.writeVarInt(selectedMediaIndex);
			buffer.writeLong(playbackPositionMs);
			buffer.writeVarInt(maxUrlLength);
			buffer.writeVarInt(maxSavedVideos);
			buffer.writeVarInt(volume);
			buffer.writeBoolean(powered);
			buffer.writeBoolean(canEdit);
			buffer.writeBoolean(refreshOnly);
		}

		@Override
		public CustomPayload.Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	public record MediaActionResultPayload(long token, boolean success, String message) implements CustomPayload {
		public static final CustomPayload.Id<MediaActionResultPayload> ID = new CustomPayload.Id<>(id("media_action_result"));
		public static final PacketCodec<RegistryByteBuf, MediaActionResultPayload> CODEC =
			PacketCodec.of(MediaActionResultPayload::write, MediaActionResultPayload::read);

		private static MediaActionResultPayload read(RegistryByteBuf buffer) {
			return new MediaActionResultPayload(buffer.readLong(), buffer.readBoolean(), buffer.readString(512));
		}

		private void write(RegistryByteBuf buffer) {
			buffer.writeLong(token);
			buffer.writeBoolean(success);
			buffer.writeString(message, 512);
		}

		@Override
		public CustomPayload.Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	public record SetYoutubeUrlPayload(long token, String url) implements CustomPayload {
		public static final CustomPayload.Id<SetYoutubeUrlPayload> ID = new CustomPayload.Id<>(id("set_youtube_url"));
		public static final PacketCodec<RegistryByteBuf, SetYoutubeUrlPayload> CODEC =
			PacketCodec.of(SetYoutubeUrlPayload::write, SetYoutubeUrlPayload::read);

		private static SetYoutubeUrlPayload read(RegistryByteBuf buffer) {
			return new SetYoutubeUrlPayload(buffer.readLong(), buffer.readString(MAX_URL_PACKET_LENGTH));
		}

		private void write(RegistryByteBuf buffer) {
			buffer.writeLong(token);
			buffer.writeString(url, MAX_URL_PACKET_LENGTH);
		}

		@Override
		public CustomPayload.Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	public record SelectMediaPayload(long token, int index) implements CustomPayload {
		public static final CustomPayload.Id<SelectMediaPayload> ID = new CustomPayload.Id<>(id("select_media"));
		public static final PacketCodec<RegistryByteBuf, SelectMediaPayload> CODEC =
			PacketCodec.of(SelectMediaPayload::write, SelectMediaPayload::read);

		private static SelectMediaPayload read(RegistryByteBuf buffer) {
			return new SelectMediaPayload(buffer.readLong(), buffer.readVarInt());
		}

		private void write(RegistryByteBuf buffer) {
			buffer.writeLong(token);
			buffer.writeVarInt(index);
		}

		@Override
		public CustomPayload.Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	public record RemoveMediaPayload(long token, int index) implements CustomPayload {
		public static final CustomPayload.Id<RemoveMediaPayload> ID = new CustomPayload.Id<>(id("remove_media"));
		public static final PacketCodec<RegistryByteBuf, RemoveMediaPayload> CODEC =
			PacketCodec.of(RemoveMediaPayload::write, RemoveMediaPayload::read);

		private static RemoveMediaPayload read(RegistryByteBuf buffer) {
			return new RemoveMediaPayload(buffer.readLong(), buffer.readVarInt());
		}

		private void write(RegistryByteBuf buffer) {
			buffer.writeLong(token);
			buffer.writeVarInt(index);
		}

		@Override
		public CustomPayload.Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	public record ToggleMediaPowerPayload(long token) implements CustomPayload {
		public static final CustomPayload.Id<ToggleMediaPowerPayload> ID = new CustomPayload.Id<>(id("toggle_media_power"));
		public static final PacketCodec<RegistryByteBuf, ToggleMediaPowerPayload> CODEC =
			PacketCodec.of(ToggleMediaPowerPayload::write, ToggleMediaPowerPayload::read);

		private static ToggleMediaPowerPayload read(RegistryByteBuf buffer) {
			return new ToggleMediaPowerPayload(buffer.readLong());
		}

		private void write(RegistryByteBuf buffer) {
			buffer.writeLong(token);
		}

		@Override
		public CustomPayload.Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	public record SetMediaVolumePayload(long token, int volume) implements CustomPayload {
		public static final CustomPayload.Id<SetMediaVolumePayload> ID = new CustomPayload.Id<>(id("set_media_volume"));
		public static final PacketCodec<RegistryByteBuf, SetMediaVolumePayload> CODEC =
			PacketCodec.of(SetMediaVolumePayload::write, SetMediaVolumePayload::read);

		private static SetMediaVolumePayload read(RegistryByteBuf buffer) {
			return new SetMediaVolumePayload(buffer.readLong(), buffer.readVarInt());
		}

		private void write(RegistryByteBuf buffer) {
			buffer.writeLong(token);
			buffer.writeVarInt(volume);
		}

		@Override
		public CustomPayload.Id<? extends CustomPayload> getId() {
			return ID;
		}
	}
}
