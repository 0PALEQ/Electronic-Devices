package com.cookiecraftmods.edm.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public final class BlockStateUtil {
	private BlockStateUtil() {
	}

	public static void cycleBinaryProperty(Level world, BlockPos pos, BlockState state, IntegerProperty property) {
		int nextValue = state.getValue(property) == 0 ? 1 : 0;
		world.setBlock(pos, state.setValue(property, nextValue), 3);
	}
}
