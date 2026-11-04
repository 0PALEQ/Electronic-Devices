package com.cookiecraftmods.edm;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cookiecraftmods.edm.init.ModBlockEntities;
import com.cookiecraftmods.edm.init.ModBlocks;
import com.cookiecraftmods.edm.init.ModItemGroups;
import com.cookiecraftmods.edm.init.ModItems;
import com.cookiecraftmods.edm.init.ModSounds;
import com.cookiecraftmods.edm.world.ModWorldGen;
import com.cookiecraftmods.edm.media.MediaConfig;
import com.cookiecraftmods.edm.network.ModNetworking;

public class ElectronicDeviceMod implements ModInitializer {
	public static final String MOD_ID = "electronic_device_mod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		MediaConfig.get();
		ModBlocks.register();
		ModItems.register();
		ModSounds.register();
		ModBlockEntities.register();
		ModItemGroups.register();
		ModWorldGen.register();
		ModNetworking.register();
	}
}
