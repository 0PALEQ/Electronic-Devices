package com.cookiecraftmods.edm.world;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import com.cookiecraftmods.edm.ElectronicDeviceMod;

public final class ModWorldGen {
	private static final ResourceKey<PlacedFeature> SILVER_ORE = ResourceKey.create(
		Registries.PLACED_FEATURE,
		Identifier.fromNamespaceAndPath(ElectronicDeviceMod.MOD_ID, "silver_ore")
	);

	private ModWorldGen() {
	}

	public static void register() {
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Decoration.UNDERGROUND_ORES, SILVER_ORE);
	}
}
