
package com.cookiecraftmods.edm.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.Instrument;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class SilverOreBlock extends Block {
	public SilverOreBlock() {
		super(AbstractBlock.Settings.create().instrument(Instrument.BASEDRUM).sounds(BlockSoundGroup.STONE).strength(3f, 5f).requiresTool());
	}

	@Override
	public int getOpacity(BlockState state, BlockView worldIn, BlockPos pos) {
		return 15;
	}
}
