package com.cookiecraftmods.edm.network;

import com.cookiecraftmods.edm.ElectronicDeviceMod;
import com.cookiecraftmods.edm.block.TvDevice;
import com.cookiecraftmods.edm.block.entity.TvBlockEntity;
import com.cookiecraftmods.edm.media.MediaConfig;
import com.cookiecraftmods.edm.media.MediaPermissions;
import com.cookiecraftmods.edm.media.MediaUrl;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
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

	private ModNetworking() {
	}

	public static void register() {
		PayloadTypeRegistry.clientboundPlay().register(OpenMediaScreenPayload.ID, OpenMediaScreenPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(MediaActionResultPayload.ID, MediaActionResultPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(SetYoutubeUrlPayload.ID, SetYoutubeUrlPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(SelectMediaPayload.ID, SelectMediaPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(RemoveMediaPayload.ID, RemoveMediaPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(ToggleMediaPowerPayload.ID, ToggleMediaPowerPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(SetMediaVolumePayload.ID, SetMediaVolumePayload.CODEC);

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

	public static void openMediaScreen(ServerPlayer player, BlockPos pos) {
		ServerLevel world = player.level();
		TvBlockEntity tv = getTv(world, pos);
		if (tv == null) return;
		openMediaScreen(player, world, pos, tv, false);
	}

	public static boolean openMediaScreenRemote(ServerPlayer player, ServerLevel world, BlockPos pos) {
		TvBlockEntity tv = getTv(world, pos);
		if (tv == null) return false;
		openMediaScreen(player, world, pos, tv, true);
		return true;
	}

	private static void openMediaScreen(
		ServerPlayer player, ServerLevel world, BlockPos pos, TvBlockEntity tv, boolean remote
	) {
		MenuSession session = new MenuSession(
			world.dimension(), pos.immutable(), ThreadLocalRandom.current().nextLong(), remote,
			System.currentTimeMillis() + MENU_SESSION_LIFETIME_MS
		);
		MENU_SESSIONS.put(player.getUUID(), session);
		sendMediaScreen(player, session, world, tv);
	}

	private static void sendMediaScreen(
		ServerPlayer player, MenuSession session, ServerLevel world, TvBlockEntity tv
	) {
		MediaConfig config = MediaConfig.get();
		List<String> mediaUrls = tv.getMediaUrls();
		BlockState state = world.getBlockState(session.pos());
		boolean powered = state.hasProperty(TvDevice.DISPLAY_STATE) && state.getValue(TvDevice.DISPLAY_STATE) == 1;

		ServerPlayNetworking.send(player, new OpenMediaScreenPayload(
			session.dimension().identifier(), session.pos(), session.token(), mediaUrls, tv.getSelectedMediaIndex(),
			tv.getPlaybackPositionMs(), config.maxMediaUrlLength, config.maxSavedVideosPerTv, tv.getVolume(),
			powered, canEdit(player, tv)
		));
	}

	private static void addMediaUrl(ServerPlayer player, long token, String rawUrl) {
		MenuTarget target = getMenuTarget(player, token);
		if (target == null) {
			sendActionResult(player, token, false, "The TV menu expired or the target TV is unavailable.");
			return;
		}
		TvBlockEntity tv = target.tv();
		if (!canEdit(player, tv)) {
			sendActionResult(player, token, false, "You do not own this TV.");
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

		boolean added = tv.addMediaUrl(url, player.getUUID());
		BlockState state = target.world().getBlockState(target.pos());
		TvDevice.setPowered(target.world(), target.pos(), state, true);
		LOGGER.info("Player {} {} a media item {} TV playlist at {} in {}",
			player.getName().getString(), added ? "added" : "selected", added ? "to" : "in",
			target.pos(), target.world().dimension().identifier());
		refreshAndResult(player, target, added ? "Media item saved and selected." : "Saved media item selected.");
	}

	private static boolean checkSource(ServerPlayer player, long token, String url) {
		try {
			MediaPermissions.checkSource(player, url);
			return true;
		} catch (IllegalArgumentException exception) {
			sendActionResult(player, token, false, exception.getMessage());
			return false;
		}
	}

	private static void selectMedia(ServerPlayer player, long token, int index) {
		MenuTarget target = getMenuTarget(player, token);
		if (target == null) {
			sendActionResult(player, token, false, "The TV menu expired or the target TV is unavailable.");
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

	private static void removeMedia(ServerPlayer player, long token, int index) {
		MenuTarget target = getMenuTarget(player, token);
		if (target == null) {
			sendActionResult(player, token, false, "The TV menu expired or the target TV is unavailable.");
			return;
		}
		TvBlockEntity tv = target.tv();
		if (!canEdit(player, tv)) {
			sendActionResult(player, token, false, "You do not own this TV.");
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
			player.getName().getString(), target.pos(), target.world().dimension().identifier());
		refreshAndResult(player, target, "Saved video removed.");
	}

	private static void togglePower(ServerPlayer player, long token) {
		MenuTarget target = getMenuTarget(player, token);
		if (target == null) {
			sendActionResult(player, token, false, "The TV menu expired or the target TV is unavailable.");
			return;
		}
		BlockState state = target.world().getBlockState(target.pos());
		boolean powered = state.hasProperty(TvDevice.DISPLAY_STATE) && state.getValue(TvDevice.DISPLAY_STATE) == 1;
		if (!powered && !target.tv().hasMedia()) {
			sendActionResult(player, token, false, "Save a media item before turning the TV on.");
			return;
		}
		if (!powered && !checkSource(player, token, target.tv().getMediaUrl())) return;
		TvDevice.toggle(target.world(), target.pos(), state);
		refreshAndResult(player, target, powered ? "TV turned off." : "TV turned on.");
	}

	private static void setVolume(ServerPlayer player, long token, int volume) {
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

	private static MenuTarget getMenuTarget(ServerPlayer player, long token) {
		MenuSession session = MENU_SESSIONS.get(player.getUUID());
		if (session == null || session.token() != token) return null;
		if (session.expiresAtMs() < System.currentTimeMillis()) {
			MENU_SESSIONS.remove(player.getUUID(), session);
			return null;
		}

		MinecraftServer server = player.level().getServer();
		ServerLevel world = server == null ? null : server.getLevel(session.dimension());
		if (world == null || !world.hasChunkAt(session.pos())) return null;
		if (session.remote()) {
			MediaConfig config = MediaConfig.get();
			if (!config.allowCrossDimensionRemotes && world != player.level()) return null;
			if (config.remoteRange >= 0 && world == player.level()
				&& player.distanceToSqr(
					session.pos().getX() + 0.5, session.pos().getY() + 0.5, session.pos().getZ() + 0.5
				) > (double) config.remoteRange * config.remoteRange) {
				return null;
			}
		} else if (world != player.level() || player.distanceToSqr(
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
		MENU_SESSIONS.put(player.getUUID(), refreshed);
		return new MenuTarget(refreshed, world, session.pos(), tv);
	}

	private static TvBlockEntity getTv(ServerLevel world, BlockPos pos) {
		return world.getBlockState(pos).getBlock() instanceof TvDevice
			&& world.getBlockEntity(pos) instanceof TvBlockEntity tv ? tv : null;
	}

	private static boolean canEdit(ServerPlayer player, TvBlockEntity tv) {
		return tv.isOwner(player.getUUID()) || MediaPermissions.canManageOthers(player);
	}

	private static void refreshAndResult(ServerPlayer player, MenuTarget target, String message) {
		sendMediaScreen(player, target.session(), target.world(), target.tv());
		sendActionResult(player, target.session().token(), true, message);
	}

	private static void sendActionResult(ServerPlayer player, long token, boolean success, String message) {
		ServerPlayNetworking.send(player, new MediaActionResultPayload(token, success, message));
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(ElectronicDeviceMod.MOD_ID, path);
	}

	private record MenuSession(
		ResourceKey<Level> dimension, BlockPos pos, long token, boolean remote, long expiresAtMs
	) {
	}

	private record MenuTarget(MenuSession session, ServerLevel world, BlockPos pos, TvBlockEntity tv) {
	}

	public record OpenMediaScreenPayload(
		Identifier dimension, BlockPos pos, long token, List<String> mediaUrls, int selectedMediaIndex,
		long playbackPositionMs, int maxUrlLength, int maxSavedVideos, int volume, boolean powered, boolean canEdit
	) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<OpenMediaScreenPayload> ID = new CustomPacketPayload.Type<>(id("open_media_screen"));
		public static final StreamCodec<RegistryFriendlyByteBuf, OpenMediaScreenPayload> CODEC =
			StreamCodec.ofMember(OpenMediaScreenPayload::write, OpenMediaScreenPayload::read);

		private static OpenMediaScreenPayload read(RegistryFriendlyByteBuf buffer) {
			Identifier dimension = buffer.readIdentifier();
			BlockPos pos = buffer.readBlockPos();
			long token = buffer.readLong();
			int mediaCount = buffer.readVarInt();
			if (mediaCount < 0 || mediaCount > TvBlockEntity.MAX_SAVED_VIDEOS) {
				throw new IllegalStateException("Invalid TV playlist size: " + mediaCount);
			}
			List<String> mediaUrls = new ArrayList<>(mediaCount);
			for (int index = 0; index < mediaCount; index++) {
				mediaUrls.add(buffer.readUtf(MAX_URL_PACKET_LENGTH));
			}
			int selectedMediaIndex = buffer.readVarInt();
			long playbackPositionMs = buffer.readLong();
			int maxUrlLength = buffer.readVarInt();
			int maxSavedVideos = buffer.readVarInt();
			int volume = buffer.readVarInt();
			boolean powered = buffer.readBoolean();
			boolean canEdit = buffer.readBoolean();
			return new OpenMediaScreenPayload(
				dimension, pos, token, mediaUrls, selectedMediaIndex, playbackPositionMs,
				maxUrlLength, maxSavedVideos, volume, powered, canEdit
			);
		}

		private void write(RegistryFriendlyByteBuf buffer) {
			buffer.writeIdentifier(dimension);
			buffer.writeBlockPos(pos);
			buffer.writeLong(token);
			buffer.writeVarInt(mediaUrls.size());
			for (String url : mediaUrls) buffer.writeUtf(url, MAX_URL_PACKET_LENGTH);
			buffer.writeVarInt(selectedMediaIndex);
			buffer.writeLong(playbackPositionMs);
			buffer.writeVarInt(maxUrlLength);
			buffer.writeVarInt(maxSavedVideos);
			buffer.writeVarInt(volume);
			buffer.writeBoolean(powered);
			buffer.writeBoolean(canEdit);
		}

		@Override
		public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
			return ID;
		}
	}

	public record MediaActionResultPayload(long token, boolean success, String message) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<MediaActionResultPayload> ID = new CustomPacketPayload.Type<>(id("media_action_result"));
		public static final StreamCodec<RegistryFriendlyByteBuf, MediaActionResultPayload> CODEC =
			StreamCodec.ofMember(MediaActionResultPayload::write, MediaActionResultPayload::read);

		private static MediaActionResultPayload read(RegistryFriendlyByteBuf buffer) {
			return new MediaActionResultPayload(buffer.readLong(), buffer.readBoolean(), buffer.readUtf(512));
		}

		private void write(RegistryFriendlyByteBuf buffer) {
			buffer.writeLong(token);
			buffer.writeBoolean(success);
			buffer.writeUtf(message, 512);
		}

		@Override
		public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
			return ID;
		}
	}

	public record SetYoutubeUrlPayload(long token, String url) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<SetYoutubeUrlPayload> ID = new CustomPacketPayload.Type<>(id("set_youtube_url"));
		public static final StreamCodec<RegistryFriendlyByteBuf, SetYoutubeUrlPayload> CODEC =
			StreamCodec.ofMember(SetYoutubeUrlPayload::write, SetYoutubeUrlPayload::read);

		private static SetYoutubeUrlPayload read(RegistryFriendlyByteBuf buffer) {
			return new SetYoutubeUrlPayload(buffer.readLong(), buffer.readUtf(MAX_URL_PACKET_LENGTH));
		}

		private void write(RegistryFriendlyByteBuf buffer) {
			buffer.writeLong(token);
			buffer.writeUtf(url, MAX_URL_PACKET_LENGTH);
		}

		@Override
		public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
			return ID;
		}
	}

	public record SelectMediaPayload(long token, int index) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<SelectMediaPayload> ID = new CustomPacketPayload.Type<>(id("select_media"));
		public static final StreamCodec<RegistryFriendlyByteBuf, SelectMediaPayload> CODEC =
			StreamCodec.ofMember(SelectMediaPayload::write, SelectMediaPayload::read);

		private static SelectMediaPayload read(RegistryFriendlyByteBuf buffer) {
			return new SelectMediaPayload(buffer.readLong(), buffer.readVarInt());
		}

		private void write(RegistryFriendlyByteBuf buffer) {
			buffer.writeLong(token);
			buffer.writeVarInt(index);
		}

		@Override
		public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
			return ID;
		}
	}

	public record RemoveMediaPayload(long token, int index) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<RemoveMediaPayload> ID = new CustomPacketPayload.Type<>(id("remove_media"));
		public static final StreamCodec<RegistryFriendlyByteBuf, RemoveMediaPayload> CODEC =
			StreamCodec.ofMember(RemoveMediaPayload::write, RemoveMediaPayload::read);

		private static RemoveMediaPayload read(RegistryFriendlyByteBuf buffer) {
			return new RemoveMediaPayload(buffer.readLong(), buffer.readVarInt());
		}

		private void write(RegistryFriendlyByteBuf buffer) {
			buffer.writeLong(token);
			buffer.writeVarInt(index);
		}

		@Override
		public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
			return ID;
		}
	}

	public record ToggleMediaPowerPayload(long token) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<ToggleMediaPowerPayload> ID = new CustomPacketPayload.Type<>(id("toggle_media_power"));
		public static final StreamCodec<RegistryFriendlyByteBuf, ToggleMediaPowerPayload> CODEC =
			StreamCodec.ofMember(ToggleMediaPowerPayload::write, ToggleMediaPowerPayload::read);

		private static ToggleMediaPowerPayload read(RegistryFriendlyByteBuf buffer) {
			return new ToggleMediaPowerPayload(buffer.readLong());
		}

		private void write(RegistryFriendlyByteBuf buffer) {
			buffer.writeLong(token);
		}

		@Override
		public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
			return ID;
		}
	}

	public record SetMediaVolumePayload(long token, int volume) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<SetMediaVolumePayload> ID = new CustomPacketPayload.Type<>(id("set_media_volume"));
		public static final StreamCodec<RegistryFriendlyByteBuf, SetMediaVolumePayload> CODEC =
			StreamCodec.ofMember(SetMediaVolumePayload::write, SetMediaVolumePayload::read);

		private static SetMediaVolumePayload read(RegistryFriendlyByteBuf buffer) {
			return new SetMediaVolumePayload(buffer.readLong(), buffer.readVarInt());
		}

		private void write(RegistryFriendlyByteBuf buffer) {
			buffer.writeLong(token);
			buffer.writeVarInt(volume);
		}

		@Override
		public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
			return ID;
		}
	}
}
