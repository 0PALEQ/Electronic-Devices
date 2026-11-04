package com.cookiecraftmods.edm.util;

import net.minecraft.block.BlockState;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class BlockStateUtil {
	private BlockStateUtil() {
	}

	public static void cycleBinaryProperty(World world, BlockPos pos, BlockState state, IntProperty property) {
		int nextValue = state.get(property) == 0 ? 1 : 0;
		world.setBlockState(pos, state.with(property, nextValue), 3);
	}
}
