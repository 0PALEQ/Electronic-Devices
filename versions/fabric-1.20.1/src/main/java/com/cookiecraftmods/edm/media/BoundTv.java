package com.cookiecraftmods.edm.media;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public record BoundTv(RegistryKey<World> dimension, BlockPos pos) {
	private static final String DIMENSION_KEY = "EdmTvDimension";
	private static final String POSITION_KEY = "EdmTvPosition";

	public void write(NbtCompound nbt) {
		nbt.putString(DIMENSION_KEY, dimension.getValue().toString());
		nbt.putLong(POSITION_KEY, pos.asLong());
	}

	public static BoundTv read(NbtCompound nbt) {
		if (nbt == null || !nbt.contains(DIMENSION_KEY) || !nbt.contains(POSITION_KEY)) return null;
		Identifier identifier = Identifier.tryParse(nbt.getString(DIMENSION_KEY));
		if (identifier == null) return null;
		return new BoundTv(RegistryKey.of(RegistryKeys.WORLD, identifier), BlockPos.fromLong(nbt.getLong(POSITION_KEY)));
	}
}
