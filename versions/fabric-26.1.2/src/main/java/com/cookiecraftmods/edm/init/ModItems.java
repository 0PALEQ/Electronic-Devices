package com.cookiecraftmods.edm.init;

import com.cookiecraftmods.edm.ElectronicDeviceMod;
import com.cookiecraftmods.edm.item.TvRemoteBlockItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public final class ModItems {
	public static final Item LAPTOP_1 = registerBlock("laptop_1", ModBlocks.LAPTOP_1);
	public static final Item MONITOR = registerBlock("monitor", ModBlocks.MONITOR);
	public static final Item PC = registerBlock("pc", ModBlocks.PC);
	public static final Item MOUSEN_KEYBOARD = registerBlock("mousen_keyboard", ModBlocks.MOUSEN_KEYBOARD);
	public static final Item MONITOR_WITH_MOUSEN_KEYBOARD = registerBlock("monitor_with_mousen_keyboard", ModBlocks.MONITOR_WITH_MOUSEN_KEYBOARD);
	public static final Item TV = registerBlock("tv", ModBlocks.TV);
	public static final Item CONSOLE = registerBlock("console", ModBlocks.CONSOLE);
	public static final Item GAMES = registerBlock("games", ModBlocks.GAMES);
	public static final Item PHONE_BLUE = registerBlock("phone_blue", ModBlocks.PHONE_BLUE);
	public static final Item GAMING_PAD = registerBlock("gaming_pad", ModBlocks.GAMING_PAD);
	public static final Item TV_REMOTE = registerItem("tv_remote", key -> new TvRemoteBlockItem(ModBlocks.TV_REMOTE, new Item.Properties().setId(key)));
	public static final Item COFFEE_MACHINE = registerBlock("coffee_machine", ModBlocks.COFFEE_MACHINE);
	public static final Item BLENDER = registerBlock("blender", ModBlocks.BLENDER);
	public static final Item MICROWAVE_OVEN = registerBlock("microwave_oven", ModBlocks.MICROWAVE_OVEN);
	public static final Item MOUNTED_TV = registerBlock("mounted_tv", ModBlocks.MOUNTED_TV);
	public static final Item TOASTER = registerBlock("toaster", ModBlocks.TOASTER);
	public static final Item WHITE_TOASTER = registerBlock("white_toaster", ModBlocks.WHITE_TOASTER);
	public static final Item WHITE_ROUTER = registerBlock("white_router", ModBlocks.WHITE_ROUTER);
	public static final Item BLACK_ROUTER = registerBlock("black_router", ModBlocks.BLACK_ROUTER);
	public static final Item BLACK_GAMING_PC = registerBlock("black_gaming_pc", ModBlocks.BLACK_GAMING_PC);
	public static final Item PHONE_GRAPHITE = registerBlock("phone_graphite", ModBlocks.PHONE_GRAPHITE);
	public static final Item PHONE_WHITE = registerBlock("phone_white", ModBlocks.PHONE_WHITE);
	public static final Item PHONE_RED = registerBlock("phone_red", ModBlocks.PHONE_RED);
	public static final Item PHONE_PINK = registerBlock("phone_pink", ModBlocks.PHONE_PINK);
	public static final Item PHONE_DESERT = registerBlock("phone_desert", ModBlocks.PHONE_DESERT);
	public static final Item PHONE_CYANAND_LIME = registerBlock("phone_cyanand_lime", ModBlocks.PHONE_CYANAND_LIME);
	public static final Item DRONE = registerBlock("drone", ModBlocks.DRONE);
	public static final Item PROJECTOR = registerBlock("projector", ModBlocks.PROJECTOR);
	public static final Item HEADPHONES = registerBlock("headphones", ModBlocks.HEADPHONES);
	public static final Item SMARTWATCH = registerBlock("smartwatch", ModBlocks.SMARTWATCH);
	public static final Item TURN_TABLE = registerBlock("turn_table", ModBlocks.TURN_TABLE);
	public static final Item BLACK_PC = registerBlock("black_pc", ModBlocks.BLACK_PC);
	public static final Item KEYBOARD = register("keyboard");
	public static final Item SCREEN = register("screen");
	public static final Item MOUSE = register("mouse");
	public static final Item PLASTIC = register("plastic");
	public static final Item BLACK_PLASTICK = register("black_plastick");
	public static final Item ELECTRONIC_PARTS = register("electronic_parts");
	public static final Item SILVER_INGOT = register("silver_ingot");
	public static final Item SILVER_ORE = registerBlock("silver_ore", ModBlocks.SILVER_ORE);
	public static final Item SILVER_BLOCK = registerBlock("silver_block", ModBlocks.SILVER_BLOCK);
	public static final Item SILVER_NUGGET = register("silver_nugget");
	public static final Item BATTERY = register("battery");
	public static final Item COFFEE_VENDING_MACHINE = registerBlock("coffee_vending_machine", ModBlocks.COFFEE_VENDING_MACHINE);
	public static final Item BLACK_DOOR_BELL = registerBlock("black_door_bell", ModBlocks.BLACK_DOOR_BELL);
	public static final Item WHITE_DOOR_BELL = registerBlock("white_door_bell", ModBlocks.WHITE_DOOR_BELL);
	public static final Item HAIR_DRYER = registerBlock("hair_dryer", ModBlocks.HAIR_DRYER);
	public static final Item SPEAKER = registerBlock("speaker", ModBlocks.SPEAKER);
	public static final Item VENDING_MACHINE = registerBlock("vending_machine", ModBlocks.VENDING_MACHINE);
	public static final Item WASHING_MACHINE = registerBlock("washing_machine", ModBlocks.WASHING_MACHINE);
	public static final Item MONITOR_2_BLACK = registerBlock("monitor_2_black", ModBlocks.MONITOR_2_BLACK);
	public static final Item MONITOR_2_WHITE = registerBlock("monitor_2_white", ModBlocks.MONITOR_2_WHITE);
	public static final Item DOUBLE_MONITOR_2_BLACK = registerBlock("double_monitor_2_black", ModBlocks.DOUBLE_MONITOR_2_BLACK);
	public static final Item DOUBLE_MONITOR_2_WHITE = registerBlock("double_monitor_2_white", ModBlocks.DOUBLE_MONITOR_2_WHITE);
	public static final Item TV_2 = registerBlock("tv_2", ModBlocks.TV_2);
	public static final Item TV_2_STANDING = registerBlock("tv_2_standing", ModBlocks.TV_2_STANDING);
	public static final Item PRINTER = registerBlock("printer", ModBlocks.PRINTER);
	public static final Item VACUUM_ROBOT = registerBlock("vacuum_robot", ModBlocks.VACUUM_ROBOT);

	private ModItems() {
	}

	public static void register() {
	}

	private static Item register(String name) {
		return registerItem(name, key -> new Item(new Item.Properties().setId(key)));
	}

	private static Item registerBlock(String name, net.minecraft.world.level.block.Block block) {
		return registerItem(name, key -> new BlockItem(block, new Item.Properties().setId(key)));
	}

	private static Item registerItem(String name, java.util.function.Function<ResourceKey<Item>, Item> factory) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ElectronicDeviceMod.MOD_ID, name));
		return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(key));
	}
}
