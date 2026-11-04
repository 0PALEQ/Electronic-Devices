package com.cookiecraftmods.edm.init;

import com.cookiecraftmods.edm.ElectronicDeviceMod;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
	private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ElectronicDeviceMod.MOD_ID);
	public static final RegistryObject<SoundEvent> DOORBELL = SOUNDS.register(
		"doorbell", () -> SoundEvent.of(Identifier.of(ElectronicDeviceMod.MOD_ID, "doorbell"))
	);

	private ModSounds() {
	}

	public static void register(IEventBus eventBus) {
		SOUNDS.register(eventBus);
	}
}
