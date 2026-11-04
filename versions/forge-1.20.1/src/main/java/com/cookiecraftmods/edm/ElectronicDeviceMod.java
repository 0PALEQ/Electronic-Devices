package com.cookiecraftmods.edm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cookiecraftmods.edm.init.ModBlockEntities;
import com.cookiecraftmods.edm.init.ModBlocks;
import com.cookiecraftmods.edm.init.ModItemGroups;
import com.cookiecraftmods.edm.init.ModItems;
import com.cookiecraftmods.edm.init.ModSounds;
import com.cookiecraftmods.edm.media.MediaConfig;
import com.cookiecraftmods.edm.media.MediaPermissions;
import com.cookiecraftmods.edm.network.ModNetworking;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ElectronicDeviceMod.MOD_ID)
public class ElectronicDeviceMod {
	public static final String MOD_ID = "electronic_device_mod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public ElectronicDeviceMod(FMLJavaModLoadingContext loadingContext) {
		IEventBus modBus = loadingContext.getModEventBus();
		ModBlocks.register(modBus);
		ModItems.register(modBus);
		ModSounds.register(modBus);
		ModBlockEntities.register(modBus);
		ModItemGroups.register(modBus);

		MinecraftForge.EVENT_BUS.addListener(MediaPermissions::registerNodes);
		MediaConfig.get();
		ModNetworking.register();
	}
}
