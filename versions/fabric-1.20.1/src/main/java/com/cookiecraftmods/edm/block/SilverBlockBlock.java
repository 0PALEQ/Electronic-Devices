
package com.cookiecraftmods.edm.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class SilverBlockBlock extends Block {
	public SilverBlockBlock() {
		super(AbstractBlock.Settings.create().sounds(BlockSoundGroup.METAL).strength(5f, 10f).requiresTool());
	}

	@Override
	public int getOpacity(BlockState state, BlockView worldIn, BlockPos pos) {
		return 15;
	}
}
