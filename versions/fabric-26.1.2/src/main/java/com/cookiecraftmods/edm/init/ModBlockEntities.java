package com.cookiecraftmods.edm.init;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;
import com.cookiecraftmods.edm.ElectronicDeviceMod;
import com.cookiecraftmods.edm.block.entity.Monitor2BlackBlockEntity;
import com.cookiecraftmods.edm.block.entity.TvBlockEntity;
import com.cookiecraftmods.edm.block.entity.TvRemoteBlockEntity;

public final class ModBlockEntities {
	public static final BlockEntityType<Monitor2BlackBlockEntity> MONITOR_2_BLACK = Registry.register(
		BuiltInRegistries.BLOCK_ENTITY_TYPE,
		Identifier.fromNamespaceAndPath(ElectronicDeviceMod.MOD_ID, "monitor_2_black"),
		FabricBlockEntityTypeBuilder.create(Monitor2BlackBlockEntity::new, ModBlocks.MONITOR_2_BLACK).build()
	);
	public static final BlockEntityType<TvBlockEntity> TV = Registry.register(
		BuiltInRegistries.BLOCK_ENTITY_TYPE,
		Identifier.fromNamespaceAndPath(ElectronicDeviceMod.MOD_ID, "tv"),
		FabricBlockEntityTypeBuilder.create(
			TvBlockEntity::new,
			ModBlocks.TV, ModBlocks.MOUNTED_TV, ModBlocks.TV_2, ModBlocks.TV_2_STANDING
		).build()
	);
	public static final BlockEntityType<TvRemoteBlockEntity> TV_REMOTE = Registry.register(
		BuiltInRegistries.BLOCK_ENTITY_TYPE,
		Identifier.fromNamespaceAndPath(ElectronicDeviceMod.MOD_ID, "tv_remote"),
		FabricBlockEntityTypeBuilder.create(TvRemoteBlockEntity::new, ModBlocks.TV_REMOTE).build()
	);

	private ModBlockEntities() {
	}

	public static void register() {
	}
}
