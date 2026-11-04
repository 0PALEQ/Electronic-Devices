package com.cookiecraftmods.edm.media;

import com.cookiecraftmods.edm.block.TvDevice;
import com.cookiecraftmods.edm.block.entity.TvBlockEntity;
import com.cookiecraftmods.edm.network.ModNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public final class RemoteControl {
	private RemoteControl() {
	}

	public static boolean advance(Player player, BoundTv binding) {
		ResolvedTv target = resolve(player, binding);
		if (target == null) return false;

		TvDevice.AdvanceResult result = TvDevice.advance(target.world(), target.pos(), target.state());
		Component message = switch (result) {
			case ADVANCED -> Component.translatable("message.electronic_device_mod.remote_next_video");
			case POWERED_ON -> Component.translatable("message.electronic_device_mod.tv_powered_on");
			case ONLY_VIDEO -> Component.translatable("message.electronic_device_mod.tv_only_video");
			case NO_MEDIA -> Component.translatable("message.electronic_device_mod.tv_no_videos");
			case TV_MISSING -> Component.translatable("message.electronic_device_mod.remote_tv_missing");
		};
		player.sendOverlayMessage(message);
		return result != TvDevice.AdvanceResult.TV_MISSING;
	}

	public static boolean openMenu(Player player, BoundTv binding) {
		ResolvedTv target = resolve(player, binding);
		if (target == null || !(player instanceof ServerPlayer serverPlayer)) return false;
		if (!ModNetworking.openMediaScreenRemote(serverPlayer, target.world(), target.pos())) {
			player.sendOverlayMessage(Component.translatable("message.electronic_device_mod.remote_tv_missing"));
			return false;
		}
		return true;
	}

	private static ResolvedTv resolve(Player player, BoundTv binding) {
		if (!(player instanceof ServerPlayer serverPlayer)) return null;
		if (binding == null) {
			player.sendOverlayMessage(Component.translatable("message.electronic_device_mod.remote_unbound"));
			return null;
		}
		MinecraftServer server = serverPlayer.level().getServer();
		ServerLevel targetWorld = server == null ? null : server.getLevel(binding.dimension());
		if (targetWorld == null) {
			player.sendOverlayMessage(Component.translatable("message.electronic_device_mod.remote_dimension_missing"));
			return null;
		}
		MediaConfig config = MediaConfig.get();
		if (!config.allowCrossDimensionRemotes && targetWorld != player.level()) {
			player.sendOverlayMessage(Component.translatable("message.electronic_device_mod.remote_wrong_dimension"));
			return null;
		}
		BlockPos pos = binding.pos();
		if (config.remoteRange >= 0 && targetWorld == player.level()
			&& player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > (double) config.remoteRange * config.remoteRange) {
			player.sendOverlayMessage(Component.translatable("message.electronic_device_mod.remote_out_of_range"));
			return null;
		}
		if (!targetWorld.hasChunkAt(pos)) {
			player.sendOverlayMessage(Component.translatable("message.electronic_device_mod.remote_chunk_unloaded"));
			return null;
		}
		BlockState state = targetWorld.getBlockState(pos);
		if (!(state.getBlock() instanceof TvDevice)
			|| !(targetWorld.getBlockEntity(pos) instanceof TvBlockEntity)) {
			player.sendOverlayMessage(Component.translatable("message.electronic_device_mod.remote_tv_missing"));
			return null;
		}
		return new ResolvedTv(targetWorld, pos, state);
	}

	private record ResolvedTv(ServerLevel world, BlockPos pos, BlockState state) {
	}
}
