package com.cookiecraftmods.edm.init;

import com.cookiecraftmods.edm.ElectronicDeviceMod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class ModSounds {
	public static final SoundEvent DOORBELL = Registry.register(
		Registries.SOUND_EVENT,
		Identifier.of(ElectronicDeviceMod.MOD_ID, "doorbell"),
		SoundEvent.of(Identifier.of(ElectronicDeviceMod.MOD_ID, "doorbell"))
	);

	private ModSounds() {
	}

	public static void register() {
	}
}
