package com.cookiecraftmods.edm.world;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;
import com.cookiecraftmods.edm.ElectronicDeviceMod;

public final class ModWorldGen {
	private static final RegistryKey<PlacedFeature> SILVER_ORE = RegistryKey.of(
		RegistryKeys.PLACED_FEATURE,
		Identifier.of(ElectronicDeviceMod.MOD_ID, "silver_ore")
	);

	private ModWorldGen() {
	}

	public static void register() {
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, SILVER_ORE);
	}
}
