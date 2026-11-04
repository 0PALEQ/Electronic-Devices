package com.cookiecraftmods.edm.block;

import com.cookiecraftmods.edm.block.entity.TvBlockEntity;
import com.cookiecraftmods.edm.network.ModNetworking;
import com.cookiecraftmods.edm.media.MediaPermissions;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public interface TvDevice {
	IntProperty DISPLAY_STATE = IntProperty.of("blockstate", 0, 1);

	enum AdvanceResult {
		ADVANCED,
		POWERED_ON,
		ONLY_VIDEO,
		NO_MEDIA,
		TV_MISSING,
		SOURCE_DENIED
	}

	static ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (player.isSneaking()) {
			if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer) {
				ModNetworking.openMediaScreen(serverPlayer, pos);
			}
			return ActionResult.success(world.isClient);
		}
		if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer) {
			AdvanceResult result = advance(world, pos, state, serverPlayer);
			Text message = switch (result) {
				case ADVANCED -> Text.translatable("message.electronic_device_mod.tv_next_video");
				case POWERED_ON -> Text.translatable("message.electronic_device_mod.tv_powered_on");
				case ONLY_VIDEO -> Text.translatable("message.electronic_device_mod.tv_only_video");
				case NO_MEDIA -> Text.translatable("message.electronic_device_mod.tv_no_videos");
				case TV_MISSING, SOURCE_DENIED -> null;
			};
			if (message != null) serverPlayer.sendMessage(message, true);
		}
		return ActionResult.success(world.isClient);
	}

	static boolean toggle(World world, BlockPos pos, BlockState state) {
		if (!(state.getBlock() instanceof TvDevice) || !state.contains(DISPLAY_STATE)) return false;
		return setPowered(world, pos, state, state.get(DISPLAY_STATE) == 0);
	}

	static boolean setPowered(World world, BlockPos pos, BlockState state, boolean powered) {
		if (!(state.getBlock() instanceof TvDevice) || !state.contains(DISPLAY_STATE)) return false;
		int next = powered ? 1 : 0;
		if (state.get(DISPLAY_STATE) == next) return true;
		if (world.getBlockEntity(pos) instanceof TvBlockEntity tv) {
			tv.beforePowerChange(powered);
		}
		world.setBlockState(pos, state.with(DISPLAY_STATE, next), Block.NOTIFY_ALL);
		return true;
	}

	static AdvanceResult advance(World world, BlockPos pos, BlockState state, ServerPlayerEntity player) {
		if (!(state.getBlock() instanceof TvDevice) || !state.contains(DISPLAY_STATE)
			|| !(world.getBlockEntity(pos) instanceof TvBlockEntity tv)) {
			return AdvanceResult.TV_MISSING;
		}
		if (!tv.hasMedia()) return AdvanceResult.NO_MEDIA;
		int nextIndex = state.get(DISPLAY_STATE) == 0 ? tv.getSelectedMediaIndex()
			: (tv.getSelectedMediaIndex() + 1) % tv.getSavedVideoCount();
		try {
			MediaPermissions.checkSource(player, tv.getMediaUrls().get(nextIndex));
		} catch (IllegalArgumentException exception) {
			player.sendMessage(Text.literal(exception.getMessage()), true);
			return AdvanceResult.SOURCE_DENIED;
		}

		if (state.get(DISPLAY_STATE) == 0) {
			setPowered(world, pos, state, true);
			return AdvanceResult.POWERED_ON;
		}
		return tv.selectNextMedia() ? AdvanceResult.ADVANCED : AdvanceResult.ONLY_VIDEO;
	}

	static void onPlaced(World world, BlockPos pos, @Nullable LivingEntity placer, ItemStack stack) {
		if (!world.isClient && world.getBlockEntity(pos) instanceof TvBlockEntity tv && placer != null) {
			tv.setOwner(placer.getUuid());
		}
	}
}
