package com.cookiecraftmods.edm.network;

import com.cookiecraftmods.edm.ElectronicDeviceMod;
import com.cookiecraftmods.edm.block.TvDevice;
import com.cookiecraftmods.edm.block.entity.TvBlockEntity;
import com.cookiecraftmods.edm.client.ElectronicDeviceModClient;
import com.cookiecraftmods.edm.media.MediaConfig;
import com.cookiecraftmods.edm.media.MediaPermissions;
import com.cookiecraftmods.edm.media.MediaUrl;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import static com.cookiecraftmods.edm.ElectronicDeviceMod.LOGGER;

public final class ModNetworking {
	public static final int MAX_URL_PACKET_LENGTH = 8192;
	private static final String NETWORK_PROTOCOL_VERSION = "1";
	private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
		id("main"), () -> NETWORK_PROTOCOL_VERSION,
		NETWORK_PROTOCOL_VERSION::equals, NETWORK_PROTOCOL_VERSION::equals
	);

	private static final long MENU_SESSION_LIFETIME_MS = 5L * 60L * 1000L;
	private static final Map<UUID, MenuSession> MENU_SESSIONS = new ConcurrentHashMap<>();
	private static boolean registered;
	private static int packetId;

	private ModNetworking() {
	}

	public static synchronized void register() {
		if (registered) return;
		registered = true;

		CHANNEL.messageBuilder(AddMediaUrlMessage.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
			.encoder(AddMediaUrlMessage::encode)
			.decoder(AddMediaUrlMessage::decode)
			.consumerMainThread(ModNetworking::handleAddMediaUrl)
			.add();
		CHANNEL.messageBuilder(SelectMediaMessage.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
			.encoder(SelectMediaMessage::encode)
			.decoder(SelectMediaMessage::decode)
			.consumerMainThread(ModNetworking::handleSelectMedia)
			.add();
		CHANNEL.messageBuilder(RemoveMediaMessage.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
			.encoder(RemoveMediaMessage::encode)
			.decoder(RemoveMediaMessage::decode)
			.consumerMainThread(ModNetworking::handleRemoveMedia)
			.add();
		CHANNEL.messageBuilder(ToggleMediaPowerMessage.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
			.encoder(ToggleMediaPowerMessage::encode)
			.decoder(ToggleMediaPowerMessage::decode)
			.consumerMainThread(ModNetworking::handleTogglePower)
			.add();
		CHANNEL.messageBuilder(SetMediaVolumeMessage.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
			.encoder(SetMediaVolumeMessage::encode)
			.decoder(SetMediaVolumeMessage::decode)
			.consumerMainThread(ModNetworking::handleSetVolume)
			.add();
		CHANNEL.messageBuilder(OpenMediaScreenMessage.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
			.encoder(OpenMediaScreenMessage::encode)
			.decoder(OpenMediaScreenMessage::decode)
			.consumerMainThread(ModNetworking::handleOpenMediaScreen)
			.add();
		CHANNEL.messageBuilder(MediaActionResultMessage.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
			.encoder(MediaActionResultMessage::encode)
			.decoder(MediaActionResultMessage::decode)
			.consumerMainThread(ModNetworking::handleMediaActionResult)
			.add();
	}

	public static void requestAddMediaUrl(long menuToken, String url) {
		CHANNEL.sendToServer(new AddMediaUrlMessage(menuToken, url));
	}

	public static void requestSelectMedia(long menuToken, int index) {
		CHANNEL.sendToServer(new SelectMediaMessage(menuToken, index));
	}

	public static void requestRemoveMedia(long menuToken, int index) {
		CHANNEL.sendToServer(new RemoveMediaMessage(menuToken, index));
	}

	public static void requestTogglePower(long menuToken) {
		CHANNEL.sendToServer(new ToggleMediaPowerMessage(menuToken));
	}

	public static void requestSetVolume(long menuToken, int volume) {
		CHANNEL.sendToServer(new SetMediaVolumeMessage(menuToken, volume));
	}

	private static void handleAddMediaUrl(AddMediaUrlMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
		ServerPlayerEntity player = contextSupplier.get().getSender();
		if (player != null) addMediaUrl(player, message.menuToken(), message.url());
	}

	private static void handleSelectMedia(SelectMediaMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
		ServerPlayerEntity player = contextSupplier.get().getSender();
		if (player != null) selectMedia(player, message.menuToken(), message.index());
	}

	private static void handleRemoveMedia(RemoveMediaMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
		ServerPlayerEntity player = contextSupplier.get().getSender();
		if (player != null) removeMedia(player, message.menuToken(), message.index());
	}

	private static void handleTogglePower(ToggleMediaPowerMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
		ServerPlayerEntity player = contextSupplier.get().getSender();
		if (player != null) togglePower(player, message.menuToken());
	}

	private static void handleSetVolume(SetMediaVolumeMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
		ServerPlayerEntity player = contextSupplier.get().getSender();
		if (player != null) setVolume(player, message.menuToken(), message.volume());
	}

	private static void handleOpenMediaScreen(OpenMediaScreenMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
		ElectronicDeviceModClient.openMediaScreen(
			message.dimension(), message.pos(), message.menuToken(), message.mediaUrls(),
			message.selectedMediaIndex(), message.playbackPosition(), message.maxUrlLength(),
			message.maxSavedVideos(), message.volume(), message.powered(), message.canEdit()
		);
	}

	private static void handleMediaActionResult(MediaActionResultMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
		ElectronicDeviceModClient.handleMediaActionResult(message.menuToken(), message.success(), message.message());
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

		OpenMediaScreenMessage message = new OpenMediaScreenMessage(
			session.dimension().getValue(), session.pos(), session.token(), mediaUrls,
			tv.getSelectedMediaIndex(), tv.getPlaybackPositionMs(), config.maxMediaUrlLength,
			config.maxSavedVideosPerTv, tv.getVolume(), powered, canEdit(player, tv)
		);
		CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
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
		CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new MediaActionResultMessage(token, success, message));
	}

	private static Identifier id(String path) {
		return Identifier.of(ElectronicDeviceMod.MOD_ID, path);
	}

	private record AddMediaUrlMessage(long menuToken, String url) {
		private void encode(PacketByteBuf buffer) {
			buffer.writeLong(menuToken);
			buffer.writeString(url, MAX_URL_PACKET_LENGTH);
		}

		private static AddMediaUrlMessage decode(PacketByteBuf buffer) {
			return new AddMediaUrlMessage(buffer.readLong(), buffer.readString(MAX_URL_PACKET_LENGTH));
		}
	}

	private record SelectMediaMessage(long menuToken, int index) {
		private void encode(PacketByteBuf buffer) {
			buffer.writeLong(menuToken);
			buffer.writeVarInt(index);
		}

		private static SelectMediaMessage decode(PacketByteBuf buffer) {
			return new SelectMediaMessage(buffer.readLong(), buffer.readVarInt());
		}
	}

	private record RemoveMediaMessage(long menuToken, int index) {
		private void encode(PacketByteBuf buffer) {
			buffer.writeLong(menuToken);
			buffer.writeVarInt(index);
		}

		private static RemoveMediaMessage decode(PacketByteBuf buffer) {
			return new RemoveMediaMessage(buffer.readLong(), buffer.readVarInt());
		}
	}

	private record ToggleMediaPowerMessage(long menuToken) {
		private void encode(PacketByteBuf buffer) {
			buffer.writeLong(menuToken);
		}

		private static ToggleMediaPowerMessage decode(PacketByteBuf buffer) {
			return new ToggleMediaPowerMessage(buffer.readLong());
		}
	}

	private record SetMediaVolumeMessage(long menuToken, int volume) {
		private void encode(PacketByteBuf buffer) {
			buffer.writeLong(menuToken);
			buffer.writeVarInt(volume);
		}

		private static SetMediaVolumeMessage decode(PacketByteBuf buffer) {
			return new SetMediaVolumeMessage(buffer.readLong(), buffer.readVarInt());
		}
	}

	private record OpenMediaScreenMessage(
		Identifier dimension, BlockPos pos, long menuToken, List<String> mediaUrls,
		int selectedMediaIndex, long playbackPosition, int maxUrlLength, int maxSavedVideos,
		int volume, boolean powered, boolean canEdit
	) {
		private OpenMediaScreenMessage {
			pos = pos.toImmutable();
			mediaUrls = List.copyOf(mediaUrls);
		}

		private void encode(PacketByteBuf buffer) {
			buffer.writeIdentifier(dimension);
			buffer.writeBlockPos(pos);
			buffer.writeLong(menuToken);
			buffer.writeVarInt(mediaUrls.size());
			for (String url : mediaUrls) buffer.writeString(url, MAX_URL_PACKET_LENGTH);
			buffer.writeVarInt(selectedMediaIndex);
			buffer.writeLong(playbackPosition);
			buffer.writeVarInt(maxUrlLength);
			buffer.writeVarInt(maxSavedVideos);
			buffer.writeVarInt(volume);
			buffer.writeBoolean(powered);
			buffer.writeBoolean(canEdit);
		}

		private static OpenMediaScreenMessage decode(PacketByteBuf buffer) {
			Identifier dimension = buffer.readIdentifier();
			BlockPos pos = buffer.readBlockPos();
			long menuToken = buffer.readLong();
			int mediaCount = buffer.readVarInt();
			if (mediaCount < 0 || mediaCount > TvBlockEntity.MAX_SAVED_VIDEOS) {
				throw new IllegalStateException("Invalid TV playlist size: " + mediaCount);
			}
			List<String> mediaUrls = new ArrayList<>(mediaCount);
			for (int index = 0; index < mediaCount; index++) {
				mediaUrls.add(buffer.readString(MAX_URL_PACKET_LENGTH));
			}
			return new OpenMediaScreenMessage(
				dimension, pos, menuToken, mediaUrls, buffer.readVarInt(), buffer.readLong(),
				buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(),
				buffer.readBoolean(), buffer.readBoolean()
			);
		}
	}

	private record MediaActionResultMessage(long menuToken, boolean success, String message) {
		private void encode(PacketByteBuf buffer) {
			buffer.writeLong(menuToken);
			buffer.writeBoolean(success);
			buffer.writeString(message, 512);
		}

		private static MediaActionResultMessage decode(PacketByteBuf buffer) {
			return new MediaActionResultMessage(buffer.readLong(), buffer.readBoolean(), buffer.readString(512));
		}
	}

	private record MenuSession(
		RegistryKey<World> dimension, BlockPos pos, long token, boolean remote, long expiresAtMs
	) {
	}

	private record MenuTarget(MenuSession session, ServerWorld world, BlockPos pos, TvBlockEntity tv) {
	}
}
