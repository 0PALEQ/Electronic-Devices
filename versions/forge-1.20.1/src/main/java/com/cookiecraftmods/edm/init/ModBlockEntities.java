package com.cookiecraftmods.edm.init;

import net.minecraft.block.entity.BlockEntityType;
import com.cookiecraftmods.edm.ElectronicDeviceMod;
import com.cookiecraftmods.edm.block.entity.Monitor2BlackBlockEntity;
import com.cookiecraftmods.edm.block.entity.TvBlockEntity;
import com.cookiecraftmods.edm.block.entity.TvRemoteBlockEntity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
	private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
		ForgeRegistries.BLOCK_ENTITY_TYPES, ElectronicDeviceMod.MOD_ID
	);
	public static final RegistryObject<BlockEntityType<Monitor2BlackBlockEntity>> MONITOR_2_BLACK = BLOCK_ENTITIES.register(
		"monitor_2_black",
		() -> BlockEntityType.Builder.create(Monitor2BlackBlockEntity::new, ModBlocks.MONITOR_2_BLACK.get()).build(null)
	);
	public static final RegistryObject<BlockEntityType<TvBlockEntity>> TV = BLOCK_ENTITIES.register(
		"tv",
		() -> BlockEntityType.Builder.create(
			TvBlockEntity::new,
			ModBlocks.TV.get(), ModBlocks.MOUNTED_TV.get(), ModBlocks.TV_2.get(), ModBlocks.TV_2_STANDING.get()
		).build(null)
	);
	public static final RegistryObject<BlockEntityType<TvRemoteBlockEntity>> TV_REMOTE = BLOCK_ENTITIES.register(
		"tv_remote",
		() -> BlockEntityType.Builder.create(TvRemoteBlockEntity::new, ModBlocks.TV_REMOTE.get()).build(null)
	);

	private ModBlockEntities() {
	}

	public static void register(IEventBus eventBus) {
		BLOCK_ENTITIES.register(eventBus);
	}
}
