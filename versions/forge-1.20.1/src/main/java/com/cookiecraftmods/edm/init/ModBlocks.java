package com.cookiecraftmods.edm.init;

import java.util.function.Supplier;
import net.minecraft.block.Block;
import com.cookiecraftmods.edm.ElectronicDeviceMod;
import com.cookiecraftmods.edm.block.BlackDoorBellBlock;
import com.cookiecraftmods.edm.block.BlackGamingPCBlock;
import com.cookiecraftmods.edm.block.BlackPCBlock;
import com.cookiecraftmods.edm.block.BlackRouterBlock;
import com.cookiecraftmods.edm.block.BlenderBlock;
import com.cookiecraftmods.edm.block.CoffeeMachineBlock;
import com.cookiecraftmods.edm.block.CoffeeVendingMachineBlock;
import com.cookiecraftmods.edm.block.ConsoleBlock;
import com.cookiecraftmods.edm.block.DoubleMonitor2BlackBlock;
import com.cookiecraftmods.edm.block.DoubleMonitor2WhiteBlock;
import com.cookiecraftmods.edm.block.DroneBlock;
import com.cookiecraftmods.edm.block.GamesBlock;
import com.cookiecraftmods.edm.block.GamingPadBlock;
import com.cookiecraftmods.edm.block.HairDryerBlock;
import com.cookiecraftmods.edm.block.HeadphonesBlock;
import com.cookiecraftmods.edm.block.Laptop1Block;
import com.cookiecraftmods.edm.block.MicrowaveOvenBlock;
import com.cookiecraftmods.edm.block.Monitor2BlackBlock;
import com.cookiecraftmods.edm.block.Monitor2WhiteBlock;
import com.cookiecraftmods.edm.block.MonitorBlock;
import com.cookiecraftmods.edm.block.MonitorWithMousenKeyboardBlock;
import com.cookiecraftmods.edm.block.MountedTVBlock;
import com.cookiecraftmods.edm.block.MousenKeyboardBlock;
import com.cookiecraftmods.edm.block.PCBlock;
import com.cookiecraftmods.edm.block.PhoneBlueBlock;
import com.cookiecraftmods.edm.block.PhoneCyanandLimeBlock;
import com.cookiecraftmods.edm.block.PhoneDesertBlock;
import com.cookiecraftmods.edm.block.PhoneGraphiteBlock;
import com.cookiecraftmods.edm.block.PhonePinkBlock;
import com.cookiecraftmods.edm.block.PhoneRedBlock;
import com.cookiecraftmods.edm.block.PhoneWhiteBlock;
import com.cookiecraftmods.edm.block.PrinterBlock;
import com.cookiecraftmods.edm.block.ProjectorBlock;
import com.cookiecraftmods.edm.block.SilverBlockBlock;
import com.cookiecraftmods.edm.block.SilverOreBlock;
import com.cookiecraftmods.edm.block.SmartwatchBlock;
import com.cookiecraftmods.edm.block.SpeakerBlock;
import com.cookiecraftmods.edm.block.ToasterBlock;
import com.cookiecraftmods.edm.block.TurnTableBlock;
import com.cookiecraftmods.edm.block.TV2Block;
import com.cookiecraftmods.edm.block.TV2StandingBlock;
import com.cookiecraftmods.edm.block.TVBlock;
import com.cookiecraftmods.edm.block.TVRemoteBlock;
import com.cookiecraftmods.edm.block.VacuumRobotBlock;
import com.cookiecraftmods.edm.block.VendingMachineBlock;
import com.cookiecraftmods.edm.block.WashingMachineBlock;
import com.cookiecraftmods.edm.block.WhiteDoorBellBlock;
import com.cookiecraftmods.edm.block.WhiteRouterBlock;
import com.cookiecraftmods.edm.block.WhiteToasterBlock;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
	private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ElectronicDeviceMod.MOD_ID);

	public static final RegistryObject<Block> LAPTOP_1 = register("laptop_1", Laptop1Block::new);
	public static final RegistryObject<Block> MONITOR = register("monitor", MonitorBlock::new);
	public static final RegistryObject<Block> PC = register("pc", PCBlock::new);
	public static final RegistryObject<Block> MOUSEN_KEYBOARD = register("mousen_keyboard", MousenKeyboardBlock::new);
	public static final RegistryObject<Block> MONITOR_WITH_MOUSEN_KEYBOARD = register("monitor_with_mousen_keyboard", MonitorWithMousenKeyboardBlock::new);
	public static final RegistryObject<Block> TV = register("tv", TVBlock::new);
	public static final RegistryObject<Block> CONSOLE = register("console", ConsoleBlock::new);
	public static final RegistryObject<Block> GAMES = register("games", GamesBlock::new);
	public static final RegistryObject<Block> PHONE_BLUE = register("phone_blue", PhoneBlueBlock::new);
	public static final RegistryObject<Block> GAMING_PAD = register("gaming_pad", GamingPadBlock::new);
	public static final RegistryObject<Block> TV_REMOTE = register("tv_remote", TVRemoteBlock::new);
	public static final RegistryObject<Block> COFFEE_MACHINE = register("coffee_machine", CoffeeMachineBlock::new);
	public static final RegistryObject<Block> BLENDER = register("blender", BlenderBlock::new);
	public static final RegistryObject<Block> MICROWAVE_OVEN = register("microwave_oven", MicrowaveOvenBlock::new);
	public static final RegistryObject<Block> MOUNTED_TV = register("mounted_tv", MountedTVBlock::new);
	public static final RegistryObject<Block> TOASTER = register("toaster", ToasterBlock::new);
	public static final RegistryObject<Block> WHITE_TOASTER = register("white_toaster", WhiteToasterBlock::new);
	public static final RegistryObject<Block> WHITE_ROUTER = register("white_router", WhiteRouterBlock::new);
	public static final RegistryObject<Block> BLACK_ROUTER = register("black_router", BlackRouterBlock::new);
	public static final RegistryObject<Block> BLACK_GAMING_PC = register("black_gaming_pc", BlackGamingPCBlock::new);
	public static final RegistryObject<Block> PHONE_GRAPHITE = register("phone_graphite", PhoneGraphiteBlock::new);
	public static final RegistryObject<Block> PHONE_WHITE = register("phone_white", PhoneWhiteBlock::new);
	public static final RegistryObject<Block> PHONE_RED = register("phone_red", PhoneRedBlock::new);
	public static final RegistryObject<Block> PHONE_PINK = register("phone_pink", PhonePinkBlock::new);
	public static final RegistryObject<Block> PHONE_DESERT = register("phone_desert", PhoneDesertBlock::new);
	public static final RegistryObject<Block> PHONE_CYANAND_LIME = register("phone_cyanand_lime", PhoneCyanandLimeBlock::new);
	public static final RegistryObject<Block> DRONE = register("drone", DroneBlock::new);
	public static final RegistryObject<Block> PROJECTOR = register("projector", ProjectorBlock::new);
	public static final RegistryObject<Block> HEADPHONES = register("headphones", HeadphonesBlock::new);
	public static final RegistryObject<Block> SMARTWATCH = register("smartwatch", SmartwatchBlock::new);
	public static final RegistryObject<Block> TURN_TABLE = register("turn_table", TurnTableBlock::new);
	public static final RegistryObject<Block> BLACK_PC = register("black_pc", BlackPCBlock::new);
	public static final RegistryObject<Block> SILVER_ORE = register("silver_ore", SilverOreBlock::new);
	public static final RegistryObject<Block> SILVER_BLOCK = register("silver_block", SilverBlockBlock::new);
	public static final RegistryObject<Block> COFFEE_VENDING_MACHINE = register("coffee_vending_machine", CoffeeVendingMachineBlock::new);
	public static final RegistryObject<Block> BLACK_DOOR_BELL = register("black_door_bell", BlackDoorBellBlock::new);
	public static final RegistryObject<Block> WHITE_DOOR_BELL = register("white_door_bell", WhiteDoorBellBlock::new);
	public static final RegistryObject<Block> HAIR_DRYER = register("hair_dryer", HairDryerBlock::new);
	public static final RegistryObject<Block> SPEAKER = register("speaker", SpeakerBlock::new);
	public static final RegistryObject<Block> VENDING_MACHINE = register("vending_machine", VendingMachineBlock::new);
	public static final RegistryObject<Block> WASHING_MACHINE = register("washing_machine", WashingMachineBlock::new);
	public static final RegistryObject<Block> MONITOR_2_BLACK = register("monitor_2_black", Monitor2BlackBlock::new);
	public static final RegistryObject<Block> MONITOR_2_WHITE = register("monitor_2_white", Monitor2WhiteBlock::new);
	public static final RegistryObject<Block> DOUBLE_MONITOR_2_BLACK = register("double_monitor_2_black", DoubleMonitor2BlackBlock::new);
	public static final RegistryObject<Block> DOUBLE_MONITOR_2_WHITE = register("double_monitor_2_white", DoubleMonitor2WhiteBlock::new);
	public static final RegistryObject<Block> TV_2 = register("tv_2", TV2Block::new);
	public static final RegistryObject<Block> TV_2_STANDING = register("tv_2_standing", TV2StandingBlock::new);
	public static final RegistryObject<Block> PRINTER = register("printer", PrinterBlock::new);
	public static final RegistryObject<Block> VACUUM_ROBOT = register("vacuum_robot", VacuumRobotBlock::new);

	private ModBlocks() {
	}

	public static void register(IEventBus eventBus) {
		BLOCKS.register(eventBus);
	}

	private static RegistryObject<Block> register(String name, Supplier<? extends Block> supplier) {
		return BLOCKS.register(name, supplier);
	}
}
