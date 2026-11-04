package com.cookiecraftmods.edm.block;

import com.cookiecraftmods.edm.block.entity.TvBlockEntity;
import com.cookiecraftmods.edm.network.ModNetworking;
import com.cookiecraftmods.edm.media.MediaPermissions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public interface TvDevice {
	IntegerProperty DISPLAY_STATE = IntegerProperty.create("blockstate", 0, 1);

	enum AdvanceResult {
		ADVANCED,
		POWERED_ON,
		ONLY_VIDEO,
		NO_MEDIA,
		TV_MISSING,
		SOURCE_DENIED
	}

	static InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		if (player.isShiftKeyDown()) {
			if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer) {
				ModNetworking.openMediaScreen(serverPlayer, pos);
			}
			return world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
		}
		if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer) {
			AdvanceResult result = advance(world, pos, state, serverPlayer);
			Component message = switch (result) {
				case ADVANCED -> Component.translatable("message.electronic_device_mod.tv_next_video");
				case POWERED_ON -> Component.translatable("message.electronic_device_mod.tv_powered_on");
				case ONLY_VIDEO -> Component.translatable("message.electronic_device_mod.tv_only_video");
				case NO_MEDIA -> Component.translatable("message.electronic_device_mod.tv_no_videos");
				case TV_MISSING, SOURCE_DENIED -> null;
			};
			if (message != null) serverPlayer.sendOverlayMessage(message);
		}
		return world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
	}

	static boolean toggle(Level world, BlockPos pos, BlockState state) {
		if (!(state.getBlock() instanceof TvDevice) || !state.hasProperty(DISPLAY_STATE)) return false;
		return setPowered(world, pos, state, state.getValue(DISPLAY_STATE) == 0);
	}

	static boolean setPowered(Level world, BlockPos pos, BlockState state, boolean powered) {
		if (!(state.getBlock() instanceof TvDevice) || !state.hasProperty(DISPLAY_STATE)) return false;
		int next = powered ? 1 : 0;
		if (state.getValue(DISPLAY_STATE) == next) return true;
		if (world.getBlockEntity(pos) instanceof TvBlockEntity tv) {
			tv.beforePowerChange(powered);
		}
		world.setBlock(pos, state.setValue(DISPLAY_STATE, next), Block.UPDATE_ALL);
		return true;
	}

	static AdvanceResult advance(Level world, BlockPos pos, BlockState state, ServerPlayer player) {
		if (!(state.getBlock() instanceof TvDevice) || !state.hasProperty(DISPLAY_STATE)
			|| !(world.getBlockEntity(pos) instanceof TvBlockEntity tv)) {
			return AdvanceResult.TV_MISSING;
		}
		if (!tv.hasMedia()) return AdvanceResult.NO_MEDIA;
		int nextIndex = state.getValue(DISPLAY_STATE) == 0 ? tv.getSelectedMediaIndex()
			: (tv.getSelectedMediaIndex() + 1) % tv.getSavedVideoCount();
		try {
			MediaPermissions.checkSource(player, tv.getMediaUrls().get(nextIndex));
		} catch (IllegalArgumentException exception) {
			player.sendOverlayMessage(Component.literal(exception.getMessage()));
			return AdvanceResult.SOURCE_DENIED;
		}

		if (state.getValue(DISPLAY_STATE) == 0) {
			setPowered(world, pos, state, true);
			return AdvanceResult.POWERED_ON;
		}
		return tv.selectNextMedia() ? AdvanceResult.ADVANCED : AdvanceResult.ONLY_VIDEO;
	}

	static void onPlaced(Level world, BlockPos pos, @Nullable LivingEntity placer, ItemStack stack) {
		if (!world.isClientSide() && world.getBlockEntity(pos) instanceof TvBlockEntity tv && placer != null) {
			tv.setOwner(placer.getUUID());
		}
	}
}
