package com.cookiecraftmods.edm.init;

import com.cookiecraftmods.edm.ElectronicDeviceMod;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import com.cookiecraftmods.edm.item.TvRemoteBlockItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ElectronicDeviceMod.MOD_ID);

	public static final RegistryObject<Item> LAPTOP_1 = registerBlock("laptop_1", ModBlocks.LAPTOP_1);
	public static final RegistryObject<Item> MONITOR = registerBlock("monitor", ModBlocks.MONITOR);
	public static final RegistryObject<Item> PC = registerBlock("pc", ModBlocks.PC);
	public static final RegistryObject<Item> MOUSEN_KEYBOARD = registerBlock("mousen_keyboard", ModBlocks.MOUSEN_KEYBOARD);
	public static final RegistryObject<Item> MONITOR_WITH_MOUSEN_KEYBOARD = registerBlock("monitor_with_mousen_keyboard", ModBlocks.MONITOR_WITH_MOUSEN_KEYBOARD);
	public static final RegistryObject<Item> TV = registerBlock("tv", ModBlocks.TV);
	public static final RegistryObject<Item> CONSOLE = registerBlock("console", ModBlocks.CONSOLE);
	public static final RegistryObject<Item> GAMES = registerBlock("games", ModBlocks.GAMES);
	public static final RegistryObject<Item> PHONE_BLUE = registerBlock("phone_blue", ModBlocks.PHONE_BLUE);
	public static final RegistryObject<Item> GAMING_PAD = registerBlock("gaming_pad", ModBlocks.GAMING_PAD);
	public static final RegistryObject<Item> TV_REMOTE = ITEMS.register("tv_remote", () -> new TvRemoteBlockItem(ModBlocks.TV_REMOTE.get(), new Item.Settings()));
	public static final RegistryObject<Item> COFFEE_MACHINE = registerBlock("coffee_machine", ModBlocks.COFFEE_MACHINE);
	public static final RegistryObject<Item> BLENDER = registerBlock("blender", ModBlocks.BLENDER);
	public static final RegistryObject<Item> MICROWAVE_OVEN = registerBlock("microwave_oven", ModBlocks.MICROWAVE_OVEN);
	public static final RegistryObject<Item> MOUNTED_TV = registerBlock("mounted_tv", ModBlocks.MOUNTED_TV);
	public static final RegistryObject<Item> TOASTER = registerBlock("toaster", ModBlocks.TOASTER);
	public static final RegistryObject<Item> WHITE_TOASTER = registerBlock("white_toaster", ModBlocks.WHITE_TOASTER);
	public static final RegistryObject<Item> WHITE_ROUTER = registerBlock("white_router", ModBlocks.WHITE_ROUTER);
	public static final RegistryObject<Item> BLACK_ROUTER = registerBlock("black_router", ModBlocks.BLACK_ROUTER);
	public static final RegistryObject<Item> BLACK_GAMING_PC = registerBlock("black_gaming_pc", ModBlocks.BLACK_GAMING_PC);
	public static final RegistryObject<Item> PHONE_GRAPHITE = registerBlock("phone_graphite", ModBlocks.PHONE_GRAPHITE);
	public static final RegistryObject<Item> PHONE_WHITE = registerBlock("phone_white", ModBlocks.PHONE_WHITE);
	public static final RegistryObject<Item> PHONE_RED = registerBlock("phone_red", ModBlocks.PHONE_RED);
	public static final RegistryObject<Item> PHONE_PINK = registerBlock("phone_pink", ModBlocks.PHONE_PINK);
	public static final RegistryObject<Item> PHONE_DESERT = registerBlock("phone_desert", ModBlocks.PHONE_DESERT);
	public static final RegistryObject<Item> PHONE_CYANAND_LIME = registerBlock("phone_cyanand_lime", ModBlocks.PHONE_CYANAND_LIME);
	public static final RegistryObject<Item> DRONE = registerBlock("drone", ModBlocks.DRONE);
	public static final RegistryObject<Item> PROJECTOR = registerBlock("projector", ModBlocks.PROJECTOR);
	public static final RegistryObject<Item> HEADPHONES = registerBlock("headphones", ModBlocks.HEADPHONES);
	public static final RegistryObject<Item> SMARTWATCH = registerBlock("smartwatch", ModBlocks.SMARTWATCH);
	public static final RegistryObject<Item> TURN_TABLE = registerBlock("turn_table", ModBlocks.TURN_TABLE);
	public static final RegistryObject<Item> BLACK_PC = registerBlock("black_pc", ModBlocks.BLACK_PC);
	public static final RegistryObject<Item> KEYBOARD = register("keyboard");
	public static final RegistryObject<Item> SCREEN = register("screen");
	public static final RegistryObject<Item> MOUSE = register("mouse");
	public static final RegistryObject<Item> PLASTIC = register("plastic");
	public static final RegistryObject<Item> BLACK_PLASTICK = register("black_plastick");
	public static final RegistryObject<Item> ELECTRONIC_PARTS = register("electronic_parts");
	public static final RegistryObject<Item> SILVER_INGOT = register("silver_ingot");
	public static final RegistryObject<Item> SILVER_ORE = registerBlock("silver_ore", ModBlocks.SILVER_ORE);
	public static final RegistryObject<Item> SILVER_BLOCK = registerBlock("silver_block", ModBlocks.SILVER_BLOCK);
	public static final RegistryObject<Item> SILVER_NUGGET = register("silver_nugget");
	public static final RegistryObject<Item> BATTERY = register("battery");
	public static final RegistryObject<Item> COFFEE_VENDING_MACHINE = registerBlock("coffee_vending_machine", ModBlocks.COFFEE_VENDING_MACHINE);
	public static final RegistryObject<Item> BLACK_DOOR_BELL = registerBlock("black_door_bell", ModBlocks.BLACK_DOOR_BELL);
	public static final RegistryObject<Item> WHITE_DOOR_BELL = registerBlock("white_door_bell", ModBlocks.WHITE_DOOR_BELL);
	public static final RegistryObject<Item> HAIR_DRYER = registerBlock("hair_dryer", ModBlocks.HAIR_DRYER);
	public static final RegistryObject<Item> SPEAKER = registerBlock("speaker", ModBlocks.SPEAKER);
	public static final RegistryObject<Item> VENDING_MACHINE = registerBlock("vending_machine", ModBlocks.VENDING_MACHINE);
	public static final RegistryObject<Item> WASHING_MACHINE = registerBlock("washing_machine", ModBlocks.WASHING_MACHINE);
	public static final RegistryObject<Item> MONITOR_2_BLACK = registerBlock("monitor_2_black", ModBlocks.MONITOR_2_BLACK);
	public static final RegistryObject<Item> MONITOR_2_WHITE = registerBlock("monitor_2_white", ModBlocks.MONITOR_2_WHITE);
	public static final RegistryObject<Item> DOUBLE_MONITOR_2_BLACK = registerBlock("double_monitor_2_black", ModBlocks.DOUBLE_MONITOR_2_BLACK);
	public static final RegistryObject<Item> DOUBLE_MONITOR_2_WHITE = registerBlock("double_monitor_2_white", ModBlocks.DOUBLE_MONITOR_2_WHITE);
	public static final RegistryObject<Item> TV_2 = registerBlock("tv_2", ModBlocks.TV_2);
	public static final RegistryObject<Item> TV_2_STANDING = registerBlock("tv_2_standing", ModBlocks.TV_2_STANDING);
	public static final RegistryObject<Item> PRINTER = registerBlock("printer", ModBlocks.PRINTER);
	public static final RegistryObject<Item> VACUUM_ROBOT = registerBlock("vacuum_robot", ModBlocks.VACUUM_ROBOT);

	private ModItems() {
	}

	public static void register(IEventBus eventBus) {
		ITEMS.register(eventBus);
	}

	private static RegistryObject<Item> register(String name) {
		return ITEMS.register(name, () -> new Item(new Item.Settings()));
	}

	private static RegistryObject<Item> registerBlock(String name, RegistryObject<? extends Block> block) {
		return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Settings()));
	}
}
