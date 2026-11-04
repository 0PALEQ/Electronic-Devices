package com.cookiecraftmods.edm.init;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import com.cookiecraftmods.edm.ElectronicDeviceMod;

public final class ModItemGroups {
	public static final ItemGroup ELECTRONIC_DEVICE_MOD = Registry.register(
		Registries.ITEM_GROUP,
		new Identifier(ElectronicDeviceMod.MOD_ID, "electronic_device_mod"),
		FabricItemGroup.builder()
			.displayName(Text.translatable("item_group.electronic_device_mod.electronic_device_mod"))
			.icon(() -> new ItemStack(ModBlocks.LAPTOP_1))
			.entries((parameters, output) -> {
				output.add(ModItems.LAPTOP_1);
				output.add(ModItems.MONITOR);
				output.add(ModItems.PC);
				output.add(ModItems.MOUSEN_KEYBOARD);
				output.add(ModItems.MONITOR_WITH_MOUSEN_KEYBOARD);
				output.add(ModItems.TV);
				output.add(ModItems.CONSOLE);
				output.add(ModItems.GAMES);
				output.add(ModItems.PHONE_BLUE);
				output.add(ModItems.GAMING_PAD);
				output.add(ModItems.TV_REMOTE);
				output.add(ModItems.COFFEE_MACHINE);
				output.add(ModItems.BLENDER);
				output.add(ModItems.MICROWAVE_OVEN);
				output.add(ModItems.MOUNTED_TV);
				output.add(ModItems.TOASTER);
				output.add(ModItems.WHITE_TOASTER);
				output.add(ModItems.WHITE_ROUTER);
				output.add(ModItems.BLACK_ROUTER);
				output.add(ModItems.BLACK_GAMING_PC);
				output.add(ModItems.PHONE_GRAPHITE);
				output.add(ModItems.PHONE_WHITE);
				output.add(ModItems.PHONE_RED);
				output.add(ModItems.PHONE_PINK);
				output.add(ModItems.PHONE_DESERT);
				output.add(ModItems.PHONE_CYANAND_LIME);
				output.add(ModItems.DRONE);
				output.add(ModItems.PROJECTOR);
				output.add(ModItems.HEADPHONES);
				output.add(ModItems.SMARTWATCH);
				output.add(ModItems.TURN_TABLE);
				output.add(ModItems.KEYBOARD);
				output.add(ModItems.SCREEN);
				output.add(ModItems.MOUSE);
				output.add(ModItems.PLASTIC);
				output.add(ModItems.BLACK_PLASTICK);
				output.add(ModItems.ELECTRONIC_PARTS);
				output.add(ModItems.SILVER_NUGGET);
				output.add(ModItems.BATTERY);
				output.add(ModItems.COFFEE_VENDING_MACHINE);
				output.add(ModItems.BLACK_DOOR_BELL);
				output.add(ModItems.WHITE_DOOR_BELL);
				output.add(ModItems.HAIR_DRYER);
				output.add(ModItems.SPEAKER);
				output.add(ModItems.VENDING_MACHINE);
				output.add(ModItems.WASHING_MACHINE);
				output.add(ModItems.MONITOR_2_BLACK);
				output.add(ModItems.MONITOR_2_WHITE);
				output.add(ModItems.DOUBLE_MONITOR_2_BLACK);
				output.add(ModItems.DOUBLE_MONITOR_2_WHITE);
				output.add(ModItems.TV_2);
				output.add(ModItems.TV_2_STANDING);
				output.add(ModItems.PRINTER);
				output.add(ModItems.VACUUM_ROBOT);
			})
			.build()
	);

	private ModItemGroups() {
	}

	public static void register() {
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> entries.add(ModItems.SILVER_INGOT));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(entries -> {
			entries.add(ModItems.SILVER_ORE);
			entries.add(ModItems.SILVER_BLOCK);
		});
	}
}
