package com.cookiecraftmods.edm.init;

import com.cookiecraftmods.edm.ElectronicDeviceMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class ModSounds {
	public static final SoundEvent DOORBELL = Registry.register(
		BuiltInRegistries.SOUND_EVENT,
		Identifier.fromNamespaceAndPath(ElectronicDeviceMod.MOD_ID, "doorbell"),
		SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(ElectronicDeviceMod.MOD_ID, "doorbell"))
	);

	private ModSounds() {
	}

	public static void register() {
	}
}
