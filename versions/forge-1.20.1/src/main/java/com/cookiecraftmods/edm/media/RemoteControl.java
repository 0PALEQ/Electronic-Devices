package com.cookiecraftmods.edm.media;

import com.cookiecraftmods.edm.block.TvDevice;
import com.cookiecraftmods.edm.block.entity.TvBlockEntity;
import com.cookiecraftmods.edm.network.ModNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public final class RemoteControl {
	private RemoteControl() {
	}

	public static boolean advance(PlayerEntity player, BoundTv binding) {
		ResolvedTv target = resolve(player, binding);
		if (target == null) return false;

		TvDevice.AdvanceResult result = TvDevice.advance(target.world(), target.pos(), target.state());
		Text message = switch (result) {
			case ADVANCED -> Text.translatable("message.electronic_device_mod.remote_next_video");
			case POWERED_ON -> Text.translatable("message.electronic_device_mod.tv_powered_on");
			case ONLY_VIDEO -> Text.translatable("message.electronic_device_mod.tv_only_video");
			case NO_MEDIA -> Text.translatable("message.electronic_device_mod.tv_no_videos");
			case TV_MISSING -> Text.translatable("message.electronic_device_mod.remote_tv_missing");
		};
		player.sendMessage(message, true);
		return result != TvDevice.AdvanceResult.TV_MISSING;
	}

	public static boolean openMenu(PlayerEntity player, BoundTv binding) {
		ResolvedTv target = resolve(player, binding);
		if (target == null || !(player instanceof ServerPlayerEntity serverPlayer)) return false;
		if (!ModNetworking.openMediaScreenRemote(serverPlayer, target.world(), target.pos())) {
			player.sendMessage(Text.translatable("message.electronic_device_mod.remote_tv_missing"), true);
			return false;
		}
		return true;
	}

	private static ResolvedTv resolve(PlayerEntity player, BoundTv binding) {
		if (!(player instanceof ServerPlayerEntity serverPlayer)) return null;
		if (binding == null) {
			player.sendMessage(Text.translatable("message.electronic_device_mod.remote_unbound"), true);
			return null;
		}
		MinecraftServer server = serverPlayer.getServer();
		ServerWorld targetWorld = server == null ? null : server.getWorld(binding.dimension());
		if (targetWorld == null) {
			player.sendMessage(Text.translatable("message.electronic_device_mod.remote_dimension_missing"), true);
			return null;
		}
		MediaConfig config = MediaConfig.get();
		if (!config.allowCrossDimensionRemotes && targetWorld != player.getWorld()) {
			player.sendMessage(Text.translatable("message.electronic_device_mod.remote_wrong_dimension"), true);
			return null;
		}
		BlockPos pos = binding.pos();
		if (config.remoteRange >= 0 && targetWorld == player.getWorld()
			&& player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > (double) config.remoteRange * config.remoteRange) {
			player.sendMessage(Text.translatable("message.electronic_device_mod.remote_out_of_range"), true);
			return null;
		}
		if (!targetWorld.isChunkLoaded(pos)) {
			player.sendMessage(Text.translatable("message.electronic_device_mod.remote_chunk_unloaded"), true);
			return null;
		}
		BlockState state = targetWorld.getBlockState(pos);
		if (!(state.getBlock() instanceof TvDevice)
			|| !(targetWorld.getBlockEntity(pos) instanceof TvBlockEntity)) {
			player.sendMessage(Text.translatable("message.electronic_device_mod.remote_tv_missing"), true);
			return null;
		}
		return new ResolvedTv(targetWorld, pos, state);
	}

	private record ResolvedTv(ServerWorld world, BlockPos pos, BlockState state) {
	}
}
