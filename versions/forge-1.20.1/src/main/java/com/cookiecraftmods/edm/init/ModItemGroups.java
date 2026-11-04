package com.cookiecraftmods.edm.init;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import com.cookiecraftmods.edm.ElectronicDeviceMod;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModItemGroups {
	private static final DeferredRegister<ItemGroup> ITEM_GROUPS = DeferredRegister.create(
		RegistryKeys.ITEM_GROUP, ElectronicDeviceMod.MOD_ID
	);
	public static final RegistryObject<ItemGroup> ELECTRONIC_DEVICE_MOD = ITEM_GROUPS.register(
		"electronic_device_mod",
		() -> ItemGroup.builder()
			.displayName(Text.translatable("item_group.electronic_device_mod.electronic_device_mod"))
			.icon(() -> new ItemStack(ModItems.LAPTOP_1.get()))
			.entries((parameters, output) -> {
				output.add(ModItems.LAPTOP_1.get());
				output.add(ModItems.MONITOR.get());
				output.add(ModItems.PC.get());
				output.add(ModItems.MOUSEN_KEYBOARD.get());
				output.add(ModItems.MONITOR_WITH_MOUSEN_KEYBOARD.get());
				output.add(ModItems.TV.get());
				output.add(ModItems.CONSOLE.get());
				output.add(ModItems.GAMES.get());
				output.add(ModItems.PHONE_BLUE.get());
				output.add(ModItems.GAMING_PAD.get());
				output.add(ModItems.TV_REMOTE.get());
				output.add(ModItems.COFFEE_MACHINE.get());
				output.add(ModItems.BLENDER.get());
				output.add(ModItems.MICROWAVE_OVEN.get());
				output.add(ModItems.MOUNTED_TV.get());
				output.add(ModItems.TOASTER.get());
				output.add(ModItems.WHITE_TOASTER.get());
				output.add(ModItems.WHITE_ROUTER.get());
				output.add(ModItems.BLACK_ROUTER.get());
				output.add(ModItems.BLACK_GAMING_PC.get());
				output.add(ModItems.PHONE_GRAPHITE.get());
				output.add(ModItems.PHONE_WHITE.get());
				output.add(ModItems.PHONE_RED.get());
				output.add(ModItems.PHONE_PINK.get());
				output.add(ModItems.PHONE_DESERT.get());
				output.add(ModItems.PHONE_CYANAND_LIME.get());
				output.add(ModItems.DRONE.get());
				output.add(ModItems.PROJECTOR.get());
				output.add(ModItems.HEADPHONES.get());
				output.add(ModItems.SMARTWATCH.get());
				output.add(ModItems.TURN_TABLE.get());
				output.add(ModItems.KEYBOARD.get());
				output.add(ModItems.SCREEN.get());
				output.add(ModItems.MOUSE.get());
				output.add(ModItems.PLASTIC.get());
				output.add(ModItems.BLACK_PLASTICK.get());
				output.add(ModItems.ELECTRONIC_PARTS.get());
				output.add(ModItems.SILVER_NUGGET.get());
				output.add(ModItems.BATTERY.get());
				output.add(ModItems.COFFEE_VENDING_MACHINE.get());
				output.add(ModItems.BLACK_DOOR_BELL.get());
				output.add(ModItems.WHITE_DOOR_BELL.get());
				output.add(ModItems.HAIR_DRYER.get());
				output.add(ModItems.SPEAKER.get());
				output.add(ModItems.VENDING_MACHINE.get());
				output.add(ModItems.WASHING_MACHINE.get());
				output.add(ModItems.MONITOR_2_BLACK.get());
				output.add(ModItems.MONITOR_2_WHITE.get());
				output.add(ModItems.DOUBLE_MONITOR_2_BLACK.get());
				output.add(ModItems.DOUBLE_MONITOR_2_WHITE.get());
				output.add(ModItems.TV_2.get());
				output.add(ModItems.TV_2_STANDING.get());
				output.add(ModItems.PRINTER.get());
				output.add(ModItems.VACUUM_ROBOT.get());
			})
			.build()
	);

	private ModItemGroups() {
	}

	public static void register(IEventBus eventBus) {
		ITEM_GROUPS.register(eventBus);
		eventBus.addListener(ModItemGroups::addVanillaTabEntries);
	}

	private static void addVanillaTabEntries(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == ItemGroups.INGREDIENTS) {
			event.accept(ModItems.SILVER_INGOT);
		} else if (event.getTabKey() == ItemGroups.BUILDING_BLOCKS) {
			event.accept(ModItems.SILVER_ORE);
			event.accept(ModItems.SILVER_BLOCK);
		}
	}
}
