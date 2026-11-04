package com.cookiecraftmods.edm.media;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public record BoundTv(ResourceKey<Level> dimension, BlockPos pos) {
	private static final String DIMENSION_KEY = "EdmTvDimension";
	private static final String POSITION_KEY = "EdmTvPosition";

	public void write(CompoundTag nbt) {
		nbt.putString(DIMENSION_KEY, dimension.identifier().toString());
		nbt.putLong(POSITION_KEY, pos.asLong());
	}

	public static BoundTv read(CompoundTag nbt) {
		if (nbt == null || !nbt.contains(DIMENSION_KEY) || !nbt.contains(POSITION_KEY)) return null;
		Identifier identifier = Identifier.tryParse(nbt.getStringOr(DIMENSION_KEY, ""));
		if (identifier == null) return null;
		return new BoundTv(ResourceKey.create(Registries.DIMENSION, identifier), BlockPos.of(nbt.getLongOr(POSITION_KEY, 0L)));
	}

	public void write(ValueOutput view) {
		view.putString(DIMENSION_KEY, dimension.identifier().toString());
		view.putLong(POSITION_KEY, pos.asLong());
	}

	public static BoundTv read(ValueInput view) {
		Optional<String> dimensionValue = view.getString(DIMENSION_KEY);
		Optional<Long> posValue = view.getLong(POSITION_KEY);
		if (dimensionValue.isEmpty() || posValue.isEmpty()) return null;
		Identifier identifier = Identifier.tryParse(dimensionValue.get());
		if (identifier == null) return null;
		return new BoundTv(ResourceKey.create(Registries.DIMENSION, identifier), BlockPos.of(posValue.get()));
	}
}
