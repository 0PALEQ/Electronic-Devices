## All-platform media playback

TV playlists accept absolute HTTP(S), file and water media URLs. Website hosts
are resolved by WaterMedia rather than a YouTube allowlist. Video, live streams,
audio, pictures and animations share the media field. Live/non-seekable media is
not forced to seek. Local files must exist on each viewing client; remote water
resources require WaterMedia's own service. For multi-source remote playlists or
galleries, the first WaterMedia source is used; Previous/Next selects saved TV URLs.
See the workspace `shared/README.md` for examples and limitations.

## WaterMedia stable 3.0 compatibility (November 4, 2026)

The active targets use WATERMeDIA 3.0.0 (Modrinth 7aLfBD61) and
WATERMeDIA Binaries 3.0.0 (IvgVb1Pv), published November 2, 2026.
Both are optional for EDM; install both on clients for TV playback.
Dedicated servers do not need either mod.

Stable 3.0 replaces the prerelease MediaAPI with api.player.PlayerAPI,
uses PlayerAPI.player(MRL, Supplier, Supplier), and moves native backend
checks to api.codecs.CodecsAPI. Platform handlers implement Platform.
EDM uses MediaPlayer.audioLanguage(String) before starting playback when
preferGameLanguageForYouTube is enabled. The old resolver replacement and
DataSlave split-package shim have been removed. Older 3.0 prereleases are
not supported by this adapter. Version ordering alone cannot distinguish
all old four-component prerelease numbers; use the exact stable files above.

# Electronic Device Mod — Feature Reference

This document describes the features implemented in the source tree for Electronic Device Mod `26.8.5` on Fabric for Minecraft `1.21.11`.

## At a glance

- Mod ID: `electronic_device_mod`
- 49 registered blocks
- 58 registered items: 49 block items and 9 standalone crafting/material items
- 57 recipe files: 49 shaped crafting recipes, 7 shapeless crafting recipes, and 1 smelting recipe
- 49 block loot tables
- 12 right-clickable blocks
- 10 blocks with on/off visual state changes
- 2 doorbells with sound activation
- 1 custom sound event
- Silver ore generation in all Overworld biomes
- A dedicated **Electronic Device Mod** creative tab with the Laptop 1 as its icon

The mod is primarily decorative. Most devices have custom models, custom voxel outlines, transparent/non-full-block rendering, and horizontal placement rotation, but no machine processing or GUI behavior.

## Right-click interactions

### Visual blockstate toggles

The following blocks have an integer `blockstate` property with values `0` and `1`. They are placed in state `0`. Monitor right-clicks toggle this value directly; TV power is controlled by the playlist interactions described below.

| Block | Registry ID | State 0 | State 1 | Sound |
| --- | --- | --- | --- | --- |
| Monitor 2 Black | `monitor_2_black` | Screen off | Active display texture | None |
| Monitor 2 White | `monitor_2_white` | Screen off | Active display texture | None |
| Double Monitor 2 Black | `double_monitor_2_black` | Both screens off | Active horizontal and vertical display textures | None |
| Double Monitor 2 White | `double_monitor_2_white` | Both screens off | Active horizontal and vertical display textures | None |
| TV 2 | `tv_2` | Screen off | Active YouTube video | Video audio when assigned |
| TV 2 Standing | `tv_2_standing` | Screen off | Active YouTube video | Video audio when assigned |
| TV | `tv` | Screen off | Active YouTube video | Video audio when assigned |
| Mounted TV | `mounted_tv` | Screen off | Active YouTube video | Video audio when assigned |

Laptop 1, Monitor, and Monitor with Mouse and Keyboard do not currently change state when clicked.

### TV video and remote control

All four TV blocks (`tv`, `mounted_tv`, `tv_2`, and `tv_2_standing`) share the same persistent YouTube playback and remote-control implementation.

- Normal right-click powers on an off TV at its current selection. While powered, it advances to the next saved video; a one-video playlist is left playing instead of being restarted.
- Sneak-right-click with an empty hand opens the paginated TV YouTube Playlist screen. Sneak-using either a handheld bound remote or a placed bound remote opens the same screen for its bound TV.
- The playlist screen can select any saved video, switch TV power, set the TV's persistent 0–100% volume, add a YouTube URL, and remove individual entries. Adding or selecting a video powers the TV on; removing the final entry powers it off.
- The owner or a media manager can add and remove URLs. Common `watch`, `youtu.be`, Shorts, live, and embed links are accepted and validated by the server. Existing viewers can select entries and use the power control just as they can operate the TV directly.
- Each TV stores its normalized URL list and selected index in block-entity NBT. The previous single `MediaUrl` is migrated automatically into the first playlist entry. Duplicate URLs select the existing entry instead of consuming another slot.
- Local image and video uploads are not supported. The mod does not copy user media to the server, create a world media library, impose upload quotas, or distribute files to clients.
- Each client resolves and plays the stored YouTube URL through WATERMeDIA. Playback seeks to the server clock, so nearby players see approximately the same position.
- WATERMeDIA supplies the video texture and audio. Each TV's saved volume is applied before the Minecraft master and records volume settings, and audio fades out over 32 blocks.
- WATERMeDIA `3.0.0.20+` contains its own YouTube resolver; a separate Platform/YouTube Extension is no longer used. This release targets WATERMeDIA `3.0.0` stable and WATERMeDIA Binaries `3.0.0`.
- WATERMeDIA `3.0.0.22` and `3.0.0.23` accidentally omit the public `DataSlave` record used by their YouTube resolver. Earlier Fabric builds supplied a shim for those prereleases; it has been removed for stable 3.0.0.
- EDM `26.8.2+` ignores the obsolete Platform Extension's incompatible YouTube handler and selects WATERMeDIA 3's built-in resolver. Full failures remain in the client log while the TV screen shows a short wrapped diagnostic.
- EDM `26.8.3+` matches Minecraft's block-model rotation convention on every facing and renders video through the full emissive-entity vertex format. This keeps side-facing screens on their TV frames and supplies shader packs with explicit white color, overlay, full-bright light, and surface-normal data instead of undefined attributes that can produce a yellow tint.
- EDM `26.8.4+` stores a persistent per-TV YouTube playlist, adds the playlist/power screen, and gives TVs and remotes next-video controls.
- EDM `26.8.5+` lets direct and remote interactions open the same target-aware menu and adds persistent per-TV volume control.
- Sneak-right-clicking any of the four TVs while holding a TV Remote binds that item to the TV and saves its dimension and position.
- Right-clicking a bound remote in the air, or normally right-clicking it after placement, powers on/advances the bound TV. Sneak-right-click opens the bound TV's menu, where power, volume, selection, addition, and removal are available. A placed remote retains the item binding.
- Remote range and cross-dimension behavior are server-configurable; remotes never force-load chunks.

`electronic_device_mod.media.manage.others` permits editing another player's TV (OP level 2 fallback). `electronic_device_mod.media.url` permits assigning YouTube videos (available to all players by its default OP-level-0 fallback). `config/electronic_device_mod-media.json` controls the maximum URL length, saved videos per TV (default and hard maximum 128), remote range, and cross-dimension remote behavior.

### Doorbell sound activation

| Block | Registry ID | Right-click behavior |
| --- | --- | --- |
| Black Door Bell | `black_door_bell` | Plays `electronic_device_mod:doorbell` |
| White Door Bell | `white_door_bell` | Plays `electronic_device_mod:doorbell` |

The doorbell sound is played in the `NEUTRAL` sound category at volume `1.0` and pitch `1.0`. It uses the bundled `sounds/doorbell.ogg` resource and does not change the doorbell blockstate.

### Monitor 2 Black storage internals

`monitor_2_black` is also backed by a persistent block entity:

- 9 inventory slots with a maximum stack size of 64
- Accepts and extracts any valid item from every side
- Saves its inventory to NBT
- Drops stored contents when the block is replaced
- Produces comparator output based on inventory fullness
- Declares a generic 9×3 container screen handler factory

The current right-click implementation only toggles the monitor's visual blockstate. It does not call `openHandledScreen`, so this inventory is not exposed as a player-openable GUI through normal right-clicking.

### Non-interactive blocks

All other 37 registered blocks have no custom `onUse` behavior. Right-clicking them does not toggle a model, play a custom sound, open a GUI, or start a machine action.

## Registered blocks

Unless noted otherwise, these are placeable decorative blocks with custom block and item models. Registry IDs below are relative to the `electronic_device_mod` namespace.

### Computers and office devices

| Display name | Registry ID | Behavior |
| --- | --- | --- |
| Laptop 1 | `laptop_1` | Decorative |
| Monitor | `monitor` | Decorative |
| PC | `pc` | Decorative |
| Black PC | `black_pc` | Decorative; no recipe and not added to a creative tab |
| Black Gaming PC | `black_gaming_pc` | Decorative |
| Mouse and Keyboard | `mousen_keyboard` | Decorative |
| Monitor with Mouse and Keyboard | `monitor_with_mousen_keyboard` | Decorative |
| Monitor 2 Black | `monitor_2_black` | Right-click display toggle; storage block entity and comparator output |
| Monitor 2 White | `monitor_2_white` | Right-click display toggle |
| Double Monitor 2 Black | `double_monitor_2_black` | Right-click display toggle |
| Double Monitor 2 White | `double_monitor_2_white` | Right-click display toggle |
| Printer | `printer` | Decorative |
| Projector | `projector` | Decorative |

### Entertainment and media devices

| Display name | Registry ID | Behavior |
| --- | --- | --- |
| TV | `tv` | Persistent YouTube playlist, power/next controls, synchronized video and audio |
| Mounted TV | `mounted_tv` | Persistent YouTube playlist, power/next controls, synchronized video and audio |
| TV 2 | `tv_2` | Persistent YouTube playlist, power/next controls, synchronized video and audio; no recipe |
| TV 2 Standing | `tv_2_standing` | Persistent YouTube playlist, power/next controls, synchronized video and audio |
| Console | `console` | Decorative |
| Games | `games` | Decorative |
| Gaming Pad | `gaming_pad` | Decorative |
| TV Remote | `tv_remote` | Bindable handheld/placeable next-video and TV-menu control |
| Speaker | `speaker` | Decorative |
| Turn Table | `turn_table` | Decorative |
| Headphones | `headphones` | Decorative |

### Phones and personal devices

| Display name | Registry ID | Behavior |
| --- | --- | --- |
| Phone Blue | `phone_blue` | Decorative; included in the `minecraft:smartphones` item tag |
| Phone Graphite | `phone_graphite` | Decorative |
| Phone White | `phone_white` | Decorative |
| Phone Red | `phone_red` | Decorative |
| Phone Pink | `phone_pink` | Decorative |
| Phone Desert | `phone_desert` | Decorative |
| Phone Cyan and Lime | `phone_cyanand_lime` | Decorative |
| Smartwatch | `smartwatch` | Decorative; does not have horizontal rotation |
| Drone | `drone` | Decorative |

### Networking and smart-home devices

| Display name | Registry ID | Behavior |
| --- | --- | --- |
| White Router | `white_router` | Decorative |
| Black Router | `black_router` | Decorative |
| Black Door Bell | `black_door_bell` | Plays the custom doorbell sound on right-click |
| White Door Bell | `white_door_bell` | Plays the custom doorbell sound on right-click |

### Kitchen and household devices

| Display name | Registry ID | Behavior |
| --- | --- | --- |
| Coffee Machine | `coffee_machine` | Decorative |
| Coffee Vending Machine | `coffee_vending_machine` | Decorative |
| Blender | `blender` | Decorative |
| Microwave Oven | `microwave_oven` | Decorative |
| Toaster | `toaster` | Decorative |
| White Toaster | `white_toaster` | Decorative |
| Vending Machine | `vending_machine` | Decorative |
| Washing Machine | `washing_machine` | Decorative |
| Vacuum Robot | `vacuum_robot` | Decorative |
| Hair Dryer | `hair_dryer` | Decorative |

### Silver blocks

| Display name | Registry ID | Behavior |
| --- | --- | --- |
| Silver Ore | `silver_ore` | Naturally generated ore; pickaxe and iron-tier tool required |
| Block of Silver | `silver_block` | Storage/building block; pickaxe and iron-tier tool required |

### Placement and physical behavior

- 46 of the 49 blocks rotate horizontally and face opposite the player when placed.
- Smartwatch, Silver Ore, and Block of Silver are the three non-directional blocks.
- Most device blocks are non-opaque, non-full blocks with model-specific outline shapes and no camera collision.
- Most decorative devices have hardness `1` and blast resistance `10`.
- Silver Ore has hardness `3`, blast resistance `5`, stone sounds, and requires the correct tool.
- Block of Silver has hardness `5`, blast resistance `10`, metal sounds, and requires the correct tool.
- Every registered block has a loot table with an explosion-survival condition.
- 48 blocks drop their own block item. Silver Ore is the exception and drops one Silver Ingot directly; its loot table does not define Silk Touch or Fortune alternatives.

## Standalone items and crafting components

| Display name | Registry ID | Purpose |
| --- | --- | --- |
| Keyboard | `keyboard` | Laptop and mouse/keyboard crafting component |
| Screen | `screen` | Display-device crafting component |
| Mouse | `mouse` | Mouse/keyboard crafting component |
| Plastic | `plastic` | White/light device casing material |
| Black Plastic | `black_plastick` | Black device casing material; the registry ID intentionally uses `plastick` |
| Electronic Parts | `electronic_parts` | Core electronics crafting component |
| Silver Ingot | `silver_ingot` | Silver material and recipe component |
| Silver Nugget | `silver_nugget` | Small silver component |
| Battery | `battery` | Portable-device crafting component |

## Recipe inventory

Ingredient IDs without the `minecraft:` prefix belong to `electronic_device_mod`. Counts describe total ingredients consumed. Every crafting recipe produces one output unless another output count is shown.

### Materials, parts, and silver processing

| Recipe ID | Type | Inputs | Output |
| --- | --- | --- | --- |
| `plastic_recipe` | Shaped | `minecraft:kelp`, `minecraft:charcoal`, `minecraft:sugar_cane` | 2× `plastic` |
| `black_plastick_recipe` | Shaped | `plastic`, `minecraft:black_dye` | `black_plastick` |
| `electronic_parts_recipe` | Shaped | 2× `silver_nugget`, `plastic`, `minecraft:gold_ingot`, 2× `minecraft:gold_nugget` | `electronic_parts` |
| `battery_recipe` | Shaped | `minecraft:copper_ingot`, 6× `silver_nugget`, `minecraft:iron_ingot`, `minecraft:redstone` | `battery` |
| `screen_recipe` | Shaped | 6× `black_plastick`, `#forge:stained_glass_panes`, `silver_nugget`, `electronic_parts` | `screen` |
| `keyboard_recipe` | Shaped | 7× `black_plastick`, `silver_nugget`, `electronic_parts` | `keyboard` |
| `mouse_recipe` | Shaped | 8× `black_plastick`, `electronic_parts` | `mouse` |
| `silver_ore_smelting` | Smelting | `silver_ore`; 200 ticks, 0.7 XP | `silver_ingot` |
| `silver_ore_block_recipe` | Shaped | 9× `silver_ingot` | `silver_block` |
| `silver_block_ore_recipe` | Shapeless | `silver_block` | 9× `silver_ingot` |
| `silver_ingot_recipe` | Shaped | 9× `silver_nugget` | `silver_ingot` |
| `silver_nugget_recipe` | Shapeless | `silver_ingot` | 9× `silver_nugget` |

### Computers, office equipment, and gaming

| Recipe ID | Type | Inputs | Output |
| --- | --- | --- | --- |
| `laptop_recipe` | Shaped | `minecraft:redstone`, `screen`, `electronic_parts`, `keyboard` | `laptop_1` |
| `monitor_recipe` | Shaped | 5× `black_plastick`, `screen`, `minecraft:iron_ingot` | `monitor` |
| `pc_recipe` | Shaped | 2× `minecraft:iron_nugget`, 4× `black_plastick`, `#forge:stained_glass_panes`, `electronic_parts`, `minecraft:redstone` | `pc` |
| `black_gaming_pc_recipe` | Shaped | 2× `minecraft:iron_ingot`, 4× `black_plastick`, `#forge:stained_glass_panes`, `electronic_parts`, `minecraft:redstone` | `black_gaming_pc` |
| `moune_n_keyboard_recipe` | Shaped | `mouse`, `keyboard` | `mousen_keyboard` |
| `mwmnkr` | Shapeless | `monitor`, `mousen_keyboard` | `monitor_with_mousen_keyboard` |
| `monitor_2_black_recipe` | Shapeless | `monitor_with_mousen_keyboard`, `black_plastick` | `monitor_2_black` |
| `monitor_2_white_recipe` | Shapeless | `monitor_with_mousen_keyboard`, `plastic` | `monitor_2_white` |
| `double_monitor_2_black_recipe` | Shapeless | `monitor_2_black`, `screen`, `black_plastick` | `double_monitor_2_black` |
| `double_monitor_2_white_recipe` | Shapeless | `monitor_2_white`, `screen`, `plastic` | `double_monitor_2_white` |
| `console_recipe` | Shaped | 7× `black_plastick`, `electronic_parts`, `minecraft:redstone` | `console` |
| `games_recipe` | Shaped | 2× `plastic`, `silver_nugget` | `games` |
| `gaming_pad_recipe` | Shaped | 7× `black_plastick`, `electronic_parts` | `gaming_pad` |
| `printer_recipe` | Shaped | 6× `plastic`, `minecraft:paper`, `electronic_parts`, `minecraft:black_dye` | `printer` |

`black_pc` is registered but has no recipe.

### Phones and wearables

| Recipe ID | Type | Inputs | Output |
| --- | --- | --- | --- |
| `blue_phone_recipe` | Shaped | 5× `black_plastick`, `screen`, `electronic_parts`, `minecraft:blue_dye`, `battery` | `phone_blue` |
| `graphite_phone_recipe` | Shaped | 5× `black_plastick`, `screen`, `electronic_parts`, `minecraft:black_dye`, `battery` | `phone_graphite` |
| `white_phone_recipe` | Shaped | 5× `black_plastick`, `screen`, `electronic_parts`, `minecraft:white_dye`, `battery` | `phone_white` |
| `red_phone_recipe` | Shaped | 5× `black_plastick`, `screen`, `electronic_parts`, `minecraft:red_dye`, `battery` | `phone_red` |
| `pink_phone_recipe` | Shaped | 5× `black_plastick`, `screen`, `electronic_parts`, `minecraft:pink_dye`, `battery` | `phone_pink` |
| `desert_phone_recipe` | Shaped | 5× `black_plastick`, `screen`, `electronic_parts`, `minecraft:yellow_dye`, `battery` | `phone_desert` |
| `cyan_lime_phone_recipe` | Shaped | 4× `black_plastick`, `screen`, `minecraft:lime_dye`, `electronic_parts`, `minecraft:cyan_dye`, `battery` | `phone_cyanand_lime` |
| `smartwatch_recipe` | Shaped | 2× `black_plastick`, `electronic_parts`, `screen`, `battery` | `smartwatch` |

### Audio, video, and gadgets

| Recipe ID | Type | Inputs | Output |
| --- | --- | --- | --- |
| `tv_recipe` | Shaped | 5× `black_plastick`, `screen`, 2× `minecraft:iron_ingot`, `electronic_parts` | `tv` |
| `mounted_tv_recipe` | Shaped | 2× `minecraft:iron_nugget`, `tv` | `mounted_tv` |
| `tv_2_standing_recipe` | Shaped | 2× `minecraft:iron_ingot`, `tv`, `electronic_parts` | `tv_2_standing` |
| `tv_remote_recipe` | Shaped | 5× `black_plastick`, `electronic_parts` | `tv_remote` |
| `projector_recipe` | Shaped | 6× `black_plastick`, `minecraft:glass_pane`, `electronic_parts`, `minecraft:redstone_lamp` | `projector` |
| `headphones_recipe` | Shaped | 3× `black_plastick`, 2× `minecraft:copper_ingot`, `electronic_parts` | `headphones` |
| `speaker_recipe` | Shaped | 8× `minecraft:copper_ingot`, `electronic_parts` | `speaker` |
| `turn_table_recipe` | Shaped | 2× `minecraft:redstone`, `minecraft:iron_nugget`, 5× `plastic`, `minecraft:jukebox` | `turn_table` |
| `drone_recipe` | Shaped | 2× `minecraft:iron_nugget`, 2× `black_plastick`, `electronic_parts`, `battery` | `drone` |

`tv_2` is registered and clickable but has no recipe.

### Appliances, networking, and smart-home devices

| Recipe ID | Type | Inputs | Output |
| --- | --- | --- | --- |
| `white_router_recipe` | Shaped | 2× `minecraft:copper_ingot`, 5× `plastic`, `electronic_parts` | `white_router` |
| `black_router_recipe` | Shaped | 2× `minecraft:copper_ingot`, 5× `black_plastick`, `electronic_parts` | `black_router` |
| `door_bell_1_recipe` | Shaped | `minecraft:stone_button`, `minecraft:redstone`, `minecraft:black_dye` | `black_door_bell` |
| `door_bell_2_recipe` | Shaped | `minecraft:stone_button`, `minecraft:redstone`, `minecraft:white_dye` | `white_door_bell` |
| `coffe_machine_recipe` | Shaped | 7× `black_plastick`, `electronic_parts`, `minecraft:water_bucket` | `coffee_machine` |
| `coffee_vm_recipe` | Shaped | 7× `minecraft:iron_ingot`, `electronic_parts`, `minecraft:cocoa_beans` | `coffee_vending_machine` |
| `blender_recipe` | Shaped | 5× `plastic`, `minecraft:bucket`, `electronic_parts`, `minecraft:iron_nugget` | `blender` |
| `microwave_recipe` | Shaped | 3× `black_plastick`, 3× `minecraft:iron_ingot`, `electronic_parts`, `screen` | `microwave_oven` |
| `toaster_recipe` | Shaped | 4× `minecraft:iron_ingot`, 3× `black_plastick` | `toaster` |
| `white_toaster_recipe` | Shaped | 4× `minecraft:iron_ingot`, 3× `plastic` | `white_toaster` |
| `hair_dryer_recipe` | Shaped | 3× `black_plastick`, `electronic_parts`, `minecraft:iron_nugget` | `hair_dryer` |
| `vm_recipe` | Shaped | 7× `minecraft:iron_ingot`, `#forge:stained_glass_panes`, `electronic_parts` | `vending_machine` |
| `washing_machine_recipe` | Shaped | `electronic_parts`, `plastic`, `screen`, 5× `minecraft:iron_ingot`, `minecraft:water_bucket` | `washing_machine` |
| `vacuum_robot_recipe` | Shaped | 3× `black_plastick`, 2× `minecraft:iron_ingot`, `electronic_parts`, `battery` | `vacuum_robot` |

## Silver ore world generation

Silver ore is added to every Overworld biome during the `UNDERGROUND_ORES` generation step.

- Vein size: 7 blocks
- Placement attempts: 11 per chunk
- Height distribution: uniform from Y=1 through Y=63
- Replaces blocks matching the `stone_ore_replaceables` rule-test tag
- Air-exposure discard chance: 0
- Mining requirement: pickaxe and iron-tier-or-better tool
- Processing: smelts into one Silver Ingot in 200 ticks and awards 0.7 experience

## Creative inventory and tags

- The **Electronic Device Mod** creative tab contains 54 entries and uses `laptop_1` as its icon.
- Silver Ingot is added to the vanilla **Ingredients** tab.
- Silver Ore and Block of Silver are added to the vanilla **Building Blocks** tab.
- Black PC is registered but is not added to any creative tab.
- The `minecraft:smartphones` item tag currently contains only `phone_blue`.
- Silver Ore and Block of Silver are tagged as `mineable/pickaxe` and `needs_iron_tool`.

## Current data-level limitations

- `black_pc` and `tv_2` have no recipe.
- Silver Ore is obtained through world generation rather than a crafting recipe.
- `screen_recipe`, `pc_recipe`, `black_gaming_pc_recipe`, and `vm_recipe` use the tag `#forge:stained_glass_panes`. This mod does not define that tag locally, so those ingredient slots depend on another loaded data pack or mod defining the tag.
- Only Monitor 2 Black has a block entity/inventory implementation; its current right-click handler does not open the declared container screen.
- Speakers, headphones, turn tables, appliances, routers, the printer, projector, drone, and vacuum robot are visual decorations only. They do not process items, consume power, move, or perform appliance functions.

## Runtime requirements

- Minecraft `1.21.11`
- Fabric Loader `0.19.3` or newer
- Fabric API `0.141.4+1.21.11` or newer
- WATERMeDIA `3.0.0` stable on clients that need TV YouTube video and audio playback
- WATERMeDIA Binaries `3.0.0` on those clients
- Java 21 or newer for the game (the mod's own bytecode still targets 17; see `PORTING.md`)

See `PORTING.md` for the WATERMeDIA v2/v3 adapter design and the Fabric/NeoForge plan for Minecraft 26.1.2 and 26.2.
