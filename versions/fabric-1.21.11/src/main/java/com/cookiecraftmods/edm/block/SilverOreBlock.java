
package com.cookiecraftmods.edm.block;

import com.cookiecraftmods.edm.ElectronicDeviceMod;
import net.minecraft.block.AbstractBlock;
import net.minecraft.util.Identifier;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class SilverOreBlock extends Block {
	public SilverOreBlock() {
		super(AbstractBlock.Settings.create().registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(ElectronicDeviceMod.MOD_ID, "silver_ore"))).instrument(NoteBlockInstrument.BASEDRUM).sounds(BlockSoundGroup.STONE).strength(3f, 5f).requiresTool());
	}

	@Override
	public int getOpacity(BlockState state) {
		return 15;
	}
}
