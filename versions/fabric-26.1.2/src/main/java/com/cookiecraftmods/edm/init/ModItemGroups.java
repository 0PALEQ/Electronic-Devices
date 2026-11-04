package com.cookiecraftmods.edm.init;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import com.cookiecraftmods.edm.ElectronicDeviceMod;

public final class ModItemGroups {
	public static final CreativeModeTab ELECTRONIC_DEVICE_MOD = Registry.register(
		BuiltInRegistries.CREATIVE_MODE_TAB,
		Identifier.fromNamespaceAndPath(ElectronicDeviceMod.MOD_ID, "electronic_device_mod"),
		FabricCreativeModeTab.builder()
			.title(Component.translatable("item_group.electronic_device_mod.electronic_device_mod"))
			.icon(() -> new ItemStack(ModBlocks.LAPTOP_1))
			.displayItems((parameters, output) -> {
				output.accept(ModItems.LAPTOP_1);
				output.accept(ModItems.MONITOR);
				output.accept(ModItems.PC);
				output.accept(ModItems.MOUSEN_KEYBOARD);
				output.accept(ModItems.MONITOR_WITH_MOUSEN_KEYBOARD);
				output.accept(ModItems.TV);
				output.accept(ModItems.CONSOLE);
				output.accept(ModItems.GAMES);
				output.accept(ModItems.PHONE_BLUE);
				output.accept(ModItems.GAMING_PAD);
				output.accept(ModItems.TV_REMOTE);
				output.accept(ModItems.COFFEE_MACHINE);
				output.accept(ModItems.BLENDER);
				output.accept(ModItems.MICROWAVE_OVEN);
				output.accept(ModItems.MOUNTED_TV);
				output.accept(ModItems.TOASTER);
				output.accept(ModItems.WHITE_TOASTER);
				output.accept(ModItems.WHITE_ROUTER);
				output.accept(ModItems.BLACK_ROUTER);
				output.accept(ModItems.BLACK_GAMING_PC);
				output.accept(ModItems.PHONE_GRAPHITE);
				output.accept(ModItems.PHONE_WHITE);
				output.accept(ModItems.PHONE_RED);
				output.accept(ModItems.PHONE_PINK);
				output.accept(ModItems.PHONE_DESERT);
				output.accept(ModItems.PHONE_CYANAND_LIME);
				output.accept(ModItems.DRONE);
				output.accept(ModItems.PROJECTOR);
				output.accept(ModItems.HEADPHONES);
				output.accept(ModItems.SMARTWATCH);
				output.accept(ModItems.TURN_TABLE);
				output.accept(ModItems.KEYBOARD);
				output.accept(ModItems.SCREEN);
				output.accept(ModItems.MOUSE);
				output.accept(ModItems.PLASTIC);
				output.accept(ModItems.BLACK_PLASTICK);
				output.accept(ModItems.ELECTRONIC_PARTS);
				output.accept(ModItems.SILVER_NUGGET);
				output.accept(ModItems.BATTERY);
				output.accept(ModItems.COFFEE_VENDING_MACHINE);
				output.accept(ModItems.BLACK_DOOR_BELL);
				output.accept(ModItems.WHITE_DOOR_BELL);
				output.accept(ModItems.HAIR_DRYER);
				output.accept(ModItems.SPEAKER);
				output.accept(ModItems.VENDING_MACHINE);
				output.accept(ModItems.WASHING_MACHINE);
				output.accept(ModItems.MONITOR_2_BLACK);
				output.accept(ModItems.MONITOR_2_WHITE);
				output.accept(ModItems.DOUBLE_MONITOR_2_BLACK);
				output.accept(ModItems.DOUBLE_MONITOR_2_WHITE);
				output.accept(ModItems.TV_2);
				output.accept(ModItems.TV_2_STANDING);
				output.accept(ModItems.PRINTER);
				output.accept(ModItems.VACUUM_ROBOT);
			})
			.build()
	);

	private ModItemGroups() {
	}

	public static void register() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(entries -> entries.accept(ModItems.SILVER_INGOT));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS).register(entries -> {
			entries.accept(ModItems.SILVER_ORE);
			entries.accept(ModItems.SILVER_BLOCK);
		});
	}
}
