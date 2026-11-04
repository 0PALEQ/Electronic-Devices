package com.cookiecraftmods.edm.network;

import com.cookiecraftmods.edm.ElectronicDeviceMod;
import com.cookiecraftmods.edm.block.TvDevice;
import com.cookiecraftmods.edm.block.entity.TvBlockEntity;
import com.cookiecraftmods.edm.media.MediaConfig;
import com.cookiecraftmods.edm.media.MediaPermissions;
import com.cookiecraftmods.edm.media.MediaUrl;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static com.cookiecraftmods.edm.ElectronicDeviceMod.LOGGER;

public final class ModNetworking {
	public static final int MAX_URL_PACKET_LENGTH = 8192;
	public static final Identifier OPEN_MEDIA_SCREEN = id("open_media_screen");
	public static final Identifier SET_YOUTUBE_URL = id("set_youtube_url");
	public static final Identifier SELECT_MEDIA = id("select_media");
	public static final Identifier REMOVE_MEDIA = id("remove_media");
	public static final Identifier TOGGLE_MEDIA_POWER = id("toggle_media_power");
	public static final Identifier SET_MEDIA_VOLUME = id("set_media_volume");
	public static final Identifier MEDIA_ACTION_RESULT = id("media_action_result");

	private static final long MENU_SESSION_LIFETIME_MS = 5L * 60L * 1000L;
	private static final Map<UUID, MenuSession> MENU_SESSIONS = new ConcurrentHashMap<>();

	private ModNetworking() {
	}

	public static void register() {
		ServerPlayNetworking.registerGlobalReceiver(SET_YOUTUBE_URL, (server, player, handler, buffer, responseSender) -> {
			long token = buffer.readLong();
			String url = buffer.readString(MAX_URL_PACKET_LENGTH);
			server.execute(() -> addMediaUrl(player, token, url));
		});
		ServerPlayNetworking.registerGlobalReceiver(SELECT_MEDIA, (server, player, handler, buffer, responseSender) -> {
			long token = buffer.readLong();
			int index = buffer.readVarInt();
			server.execute(() -> selectMedia(player, token, index));
		});
		ServerPlayNetworking.registerGlobalReceiver(REMOVE_MEDIA, (server, player, handler, buffer, responseSender) -> {
			long token = buffer.readLong();
			int index = buffer.readVarInt();
			server.execute(() -> removeMedia(player, token, index));
		});
		ServerPlayNetworking.registerGlobalReceiver(TOGGLE_MEDIA_POWER, (server, player, handler, buffer, responseSender) -> {
			long token = buffer.readLong();
			server.execute(() -> togglePower(player, token));
		});
		ServerPlayNetworking.registerGlobalReceiver(SET_MEDIA_VOLUME, (server, player, handler, buffer, responseSender) -> {
			long token = buffer.readLong();
			int volume = buffer.readVarInt();
			server.execute(() -> setVolume(player, token, volume));
		});
	}

	public static void openMediaScreen(ServerPlayerEntity player, BlockPos pos) {
		ServerWorld world = player.getServerWorld();
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
		MenuSession session = new MenuSession(
			world.getRegistryKey(), pos.toImmutable(), ThreadLocalRandom.current().nextLong(), remote,
			System.currentTimeMillis() + MENU_SESSION_LIFETIME_MS
		);
		MENU_SESSIONS.put(player.getUuid(), session);
		sendMediaScreen(player, session, world, tv);
	}

	private static void sendMediaScreen(
		ServerPlayerEntity player, MenuSession session, ServerWorld world, TvBlockEntity tv
	) {
		MediaConfig config = MediaConfig.get();
		List<String> mediaUrls = tv.getMediaUrls();
		BlockState state = world.getBlockState(session.pos());
		boolean powered = state.contains(TvDevice.DISPLAY_STATE) && state.get(TvDevice.DISPLAY_STATE) == 1;

		PacketByteBuf buffer = PacketByteBufs.create();
		buffer.writeIdentifier(session.dimension().getValue());
		buffer.writeBlockPos(session.pos());
		buffer.writeLong(session.token());
		buffer.writeVarInt(mediaUrls.size());
		for (String url : mediaUrls) buffer.writeString(url, MAX_URL_PACKET_LENGTH);
		buffer.writeVarInt(tv.getSelectedMediaIndex());
		buffer.writeLong(tv.getPlaybackPositionMs());
		buffer.writeVarInt(config.maxMediaUrlLength);
		buffer.writeVarInt(config.maxSavedVideosPerTv);
		buffer.writeVarInt(tv.getVolume());
		buffer.writeBoolean(powered);
		buffer.writeBoolean(canEdit(player, tv));
		ServerPlayNetworking.send(player, OPEN_MEDIA_SCREEN, buffer);
	}

	private static void addMediaUrl(ServerPlayerEntity player, long token, String rawUrl) {
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
			return null;
		}

		MinecraftServer server = player.getServer();
		ServerWorld world = server == null ? null : server.getWorld(session.dimension());
		if (world == null || !world.isChunkLoaded(session.pos())) return null;
		if (session.remote()) {
			MediaConfig config = MediaConfig.get();
			if (!config.allowCrossDimensionRemotes && world != player.getWorld()) return null;
			if (config.remoteRange >= 0 && world == player.getWorld()
				&& player.squaredDistanceTo(
					session.pos().getX() + 0.5, session.pos().getY() + 0.5, session.pos().getZ() + 0.5
				) > (double) config.remoteRange * config.remoteRange) {
				return null;
			}
		} else if (world != player.getWorld() || player.squaredDistanceTo(
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
		return tv.isOwner(player.getUuid()) || MediaPermissions.canManageOthers(player);
	}

	private static void refreshAndResult(ServerPlayerEntity player, MenuTarget target, String message) {
		sendMediaScreen(player, target.session(), target.world(), target.tv());
		sendActionResult(player, target.session().token(), true, message);
	}

	private static void sendActionResult(ServerPlayerEntity player, long token, boolean success, String message) {
		PacketByteBuf buffer = PacketByteBufs.create();
		buffer.writeLong(token);
		buffer.writeBoolean(success);
		buffer.writeString(message, 512);
		ServerPlayNetworking.send(player, MEDIA_ACTION_RESULT, buffer);
	}

	private static Identifier id(String path) {
		return new Identifier(ElectronicDeviceMod.MOD_ID, path);
	}

	private record MenuSession(
		RegistryKey<World> dimension, BlockPos pos, long token, boolean remote, long expiresAtMs
	) {
	}

	private record MenuTarget(MenuSession session, ServerWorld world, BlockPos pos, TvBlockEntity tv) {
	}
}
