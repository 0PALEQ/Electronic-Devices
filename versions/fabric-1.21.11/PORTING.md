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

# Multi-version and loader porting notes

The TV system is intentionally split into two responsibilities:

1. The dedicated server owns URL validation, permissions, each TV's saved URL playlist and active index, power state, volume, and playback clock.
2. Each client sends only the server-selected YouTube URL to a client-only WATERMeDIA adapter for resolving, decoding, rendering, and audio.

The server never contacts YouTube and does not need WATERMeDIA or its native binaries. Local media upload, server-side media storage, file distribution, and client file caches are deliberately outside the design.

## WATERMeDIA compatibility

The reflection adapter resolves URLs through `PlayerAPI.mrl(URI)`, waits for
MRL status `LOADED`, then calls `PlayerAPI.player(MRL, Supplier, Supplier)`.
The suppliers create the GL and OpenAL engines. `MediaPlayer` exposes the
texture, volume, seek, playback clock and controls. `CodecsAPI` reports native
FFmpeg readiness. The stable API and exact supported files are recorded above.
The v2 fallback remains for legacy clients but is not the supported release target.

Use `scripts/verify-watermedia.ps1` from the workspace root to check all reflected
stable API signatures against the published jar. A full build does not exercise
reflection, so run this check when updating WaterMedia. In-game playback still
requires a client smoke test with both WaterMedia and Binaries installed.

## TV render compatibility

Minecraft blockstate-model Y rotations and `MatrixStack` positive-Y rotations use opposite signs. The block-entity video quad therefore maps east to `-90` degrees and west to `+90` degrees so multi-block models such as `tv_2` remain aligned with their screen frames.

The video quad is submitted through `RenderLayers.entityTranslucentEmissive(Identifier)` (an emissive, translucent entity-style pipeline). Every vertex supplies opaque white color, the default overlay, full-bright light, and a transformed front normal. Do not drop these attributes: Iris and other shader-pack pipelines can substitute a world shader that expects the complete entity vertex format, and missing attributes can be read as invalid color data (observed as a strong yellow tint).

As of Minecraft 1.21.11, block-entity rendering goes through a render-state/command-queue design rather than direct immediate-mode drawing — see "Notes from the 1.21.1 → 1.21.11 port" below for the full shape. `TvBlockEntityRenderer` resolves the WATERMeDIA texture Identifier in `updateRenderState` and submits the quad via `OrderedRenderCommandQueue#submitCustom` in `render`.

WATERMeDIA still only exposes a raw GL texture id (`MediaPlayer#texture()`, now `long`-typed but numerically a `GLuint`) — there is no Identifier-based texture handed to the mod. `client/render/ExternalGlTexture` and `ExternalGlTextureView` wrap that id as `GpuTexture`/`GpuTextureView` subclasses with `close()` overridden to a no-op (WATERMeDIA owns the underlying GL texture's lifecycle, not Minecraft's `TextureManager`; letting the base class's `close()` run would delete WATERMeDIA's live texture, e.g. on a resource-pack reload). `client/render/WaterMediaVideoTexture` (an `AbstractTexture`) wraps those and is registered once per TV under a stable synthetic Identifier (`electronic_device_mod:dynamic/tv/<dimension>/<x>_<y>_<z>`, derived in `MediaPlaybackManager.textureIdFor`) — re-registering under the same id on a new session just replaces the old (harmlessly no-op-closed) entry, so there is no accumulation. `RenderSetup.Builder#texture(String, Identifier)` re-resolves that Identifier through `TextureManager` on every draw, so mutating the registered texture's wrapped GL id in place (only recreating the `GlTexture`/`GlTextureView` when WATERMeDIA's id actually changes) is enough to keep the picture live without re-registering every frame.

## Target branches

Create one source branch or Gradle subproject per Minecraft API generation:

| Target | Loaders | WATERMeDIA policy |
| --- | --- | --- |
| Minecraft 1.20.1 | Fabric | WATERMeDIA `3.0.0.23+` and Binaries `3.0.0.6+` |
| Minecraft 1.21.1 | Fabric | WATERMeDIA `3.0.0.23+` and Binaries `3.0.0.6+` |
| Minecraft 1.21.11 | Fabric | WATERMeDIA `3.0.0.23+` and Binaries `3.0.0.6+` (implemented in this branch) |
| Minecraft 26.1.2 | Fabric and NeoForge | Latest explicitly tested v3 and matching Binaries |
| Minecraft 26.2 | Fabric and NeoForge | Latest explicitly tested v3 and matching Binaries |

Do not use an unbounded dynamic Gradle dependency for release builds. Record the tested versions in each branch and publish them in that branch's metadata. Runtime reflection remains useful for patch-level API tolerance, but the release should still state which versions were tested.

## Notes from the 1.20.1 → 1.21.1 port

WATERMeDIA needed no version bump — `3.0.0.23`/Binaries `3.0.0.6` already support 1.21.1, and `WaterMediaBridge`'s reflection targets (`org.watermedia.api.media.MediaAPI`/`MRL`/`MediaPlayer`) were unchanged. The Minecraft-side breaking changes that did require code changes:

- **Networking**: Fabric API removed the raw-`PacketByteBuf` `ServerPlayNetworking`/`ClientPlayNetworking` receiver overloads. Every packet is now a `record ... implements CustomPayload` with a `CustomPayload.Id` and a `PacketCodec` (built via `PacketCodec.of(Payload::write, Payload::read)` for payloads with more than the 6 fields `PacketCodec.tuple(...)` supports), registered through `PayloadTypeRegistry.playC2S()`/`playS2C()`. See `ModNetworking`.
- **`Identifier`**: the constructor is now protected; use `Identifier.of(namespace, path)`.
- **`Block#onUse`** dropped its `Hand` parameter (item-specific interaction moved to a new `onUseWithItem`); every block's `onUse` override lost that parameter.
- **`BlockEntity#readNbt`/`writeNbt`/`toInitialChunkDataNbt`/`createNbtWithIdentifyingData`** all gained a `RegistryWrapper.WrapperLookup` parameter. `LootableContainerBlockEntity` also dropped `deserializeLootTable`/`serializeLootTable` (loot-table NBT moved to the data-components `readComponents`/`addComponents` path) — `Monitor2BlackBlockEntity` never assigns a loot table in practice, so it now always reads/writes its stacks directly via `Inventories.readNbt`/`writeNbt` (also now `WrapperLookup`-aware).
- **`ItemStack` NBT accessors** (`getNbt()`, `getOrCreateNbt()`) were removed in the Data Components migration; custom per-stack NBT (the TV remote's `BoundTv` binding) now goes through `NbtComponent`/`DataComponentTypes.CUSTOM_DATA` (`stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt()` to read, `NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, consumer)` to read-modify-write).
- **`Item#appendTooltip`** signature changed to `(ItemStack, Item.TooltipContext, List<Text>, TooltipType)` — the old `net.minecraft.client.item.TooltipContext` "advanced tooltip" flag is now `net.minecraft.item.tooltip.TooltipType`, and `Item.TooltipContext` is a new nested type replacing the old `World` parameter.
- **`net.minecraft.block.enums.Instrument`** was renamed to `NoteBlockInstrument` (same constants, e.g. `BASEDRUM`).
- **Recipe JSON**: the `result` object's `item` key was renamed to `id` for shaped/shapeless recipes (`{"item": "..."} → {"id": "..."}`); smelting's bare-string `result` was unaffected. Data pack format bumped 15 → 48 in `pack.mcmeta`.
- **Toolchain**: Fabric Loom `1.17.x` (needed for 1.21.1) requires JVM 21+ to *run* Gradle itself, separate from the Java 17 the mod's bytecode still targets (`it.options.release = 17` in `build.gradle`). Point `JAVA_HOME` at a JDK 21+ install when building; the Gradle wrapper was bumped to `9.5.0` to match. `me.lucko:fabric-permissions-api` stayed pinned at `0.3.1` — newer releases are compiled for a Java bytecode version too new for this toolchain to even read.

## Notes from the 1.21.1 → 1.21.11 port

WATERMeDIA again needed no version bump — `3.0.0.23`/Binaries `3.0.0.6` are published for 1.21.11 too, and every method `WaterMediaBridge` calls via reflection (`MediaAPI.mrl`/`createPlayer`/`glEngine`/`alEngine`, `MediaPlayer#texture`/`canPlay`/`seek`/etc., `MRL#status`/`exception`) kept its name and signature; verified against the WATERMeDIA sources jar published alongside `3.0.0.23`. `MediaPlayer#texture()` now returns `long` instead of `int` (Vulkan handles need the width; GL ids still fit the lower 32 bits) — the existing `instanceof Number` handling in `V3Playback.texture` already tolerates this with no code change. The Minecraft-side breaking changes:

- **Block/item registration order**: `AbstractBlock.Settings`/`Item.Settings` now require `.registryKey(RegistryKey<Block|Item>)` to be set *before* construction — the default loot-table and translation-key suppliers read it eagerly from inside `AbstractBlock`'s/`Item`'s own constructor (`Objects.requireNonNull(this.registryKey, "Block id not set")`), so a block or item built before `Registry.register` assigns its key now throws immediately at class-init time. Every block constructor chains `.registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "<name>")))` onto `AbstractBlock.Settings.create()`; `ModItems`'s `register`/`registerBlock` helpers build the key first and pass it into an `Item.Settings().registryKey(key)` before constructing the `Item`/`BlockItem`.
- **Rendering engine rewrite**: Minecraft replaced the old immediate-mode `Tessellator`/`BufferBuilder`/`BufferRenderer`/raw-shader path with a backend-agnostic GPU abstraction (`com.mojang.blaze3d.pipeline.RenderPipeline`, `com.mojang.blaze3d.textures.GpuTexture`/`GpuTextureView`, `com.mojang.blaze3d.systems.RenderPass`). `BlockEntityRenderer<T extends BlockEntity, S extends BlockEntityRenderState>` is now generic over a render-state type: `createRenderState()` builds it, `updateRenderState(T, S, float tickProgress, Vec3d cameraPos, CrumblingOverlayCommand)` copies whatever the draw call needs off the block entity (this is the right place for anything that touches other game state, since it runs separately from drawing), and `render(S, MatrixStack, OrderedRenderCommandQueue, CameraRenderState)` only emits geometry — via `queue.submitCustom(matrices, renderLayer, (entry, vertexConsumer) -> ...)` for a raw quad, exactly as `AbstractEndPortalBlockEntityRenderer` does in vanilla. See the "TV render compatibility" section above for how `TvBlockEntityRenderer` and the new `client/render/ExternalGlTexture`/`ExternalGlTextureView`/`WaterMediaVideoTexture` classes adapt WATERMeDIA's raw GL texture id to this pipeline. `VertexConsumer#vertex(Matrix4fc, float, float, float)` and `#normal(MatrixStack.Entry, float, float, float)` default methods are unchanged, so the actual per-vertex calls didn't need to change.
- **`state.property.DirectionProperty`** was removed; `HorizontalFacingBlock.FACING` is now typed `EnumProperty<Direction>` (`Properties.HORIZONTAL_FACING`). Every block's `FACING` field changed type to match.
- **`AbstractBlock#isTransparent`/`#getOpacity`** dropped their `BlockView`/`BlockPos` parameters — both are now single-argument, `(BlockState state)`.
- **`AbstractBlock#getComparatorOutput`** gained a trailing `Direction` parameter: `(BlockState, World, BlockPos, Direction)`.
- **`AbstractBlock#onStateReplaced`** dropped its `BlockState newState` parameter and narrowed `World` to `ServerWorld`: `(BlockState state, ServerWorld world, BlockPos pos, boolean moved)`. It's now only invoked when the block actually changed, so the old `state.getBlock() != newState.getBlock()` guard is gone too — see vanilla `ChestBlock`/`ShulkerBoxBlock` for the pattern.
- **`World#isClient`** (and the same field on `Entity`) is private now; use the `isClient()` accessor everywhere a raw field read used to work.
- **`Item#use`** returns `ActionResult` directly instead of `TypedActionResult<ItemStack>` (no more stack-swap payload on the return).
- **`ActionResult`** is a sealed interface with fixed instances (`SUCCESS`, `SUCCESS_SERVER`, `CONSUME`, `FAIL`, `PASS`, `PASS_TO_DEFAULT_BLOCK_ACTION`) instead of an enum with a `success(boolean)` factory. The old `ActionResult.success(world.isClient)` convention (client-side swing vs. silent server-side success) becomes `world.isClient() ? ActionResult.SUCCESS : ActionResult.SUCCESS_SERVER`.
- **`BlockEntity#readNbt`/`writeNbt`** are gone; override `readData(ReadView)`/`writeData(WriteView)` instead (`toUpdatePacket`/`toInitialChunkDataNbt` kept their old signatures and did not need changes). `ReadView`/`WriteView` are Codec-based: `view.read(key, Codec<T>)` returns `Optional<T>`, `view.put(key, Codec<T>, value)`/`putNullable` write, and there are typed convenience accessors (`getInt`/`getLong`/`getBoolean`/`getString`, each with a `(key, fallback)` overload plus an `getOptionalX(key)` presence-checking one) and list helpers (`getOptionalTypedListView(key, Codec<T>)` to read, `getListAppender(key, Codec<T>)` to write). UUIDs go through `Uuids.CODEC` (`view.read("Owner", Uuids.CODEC)` / `view.putNullable("Owner", Uuids.CODEC, owner)`) — there's no more `containsUuid`/`getUuid`/`putUuid` on the raw type. `Inventories.readNbt`/`writeNbt` became `Inventories.readData(ReadView, DefaultedList)`/`writeData(WriteView, DefaultedList, boolean)` (no more `WrapperLookup` parameter), and `LootableContainerBlockEntity`'s `readLootTable`/`writeLootTable` now take `ReadView`/`WriteView` too. `BoundTv` (the TV remote's binding record) keeps its original `NbtCompound`-based `read`/`write` for the `ItemStack` `NbtComponent` custom-data path (unaffected by this — `DataComponentTypes.CUSTOM_DATA` still stores a plain `NbtCompound`) and gained `ReadView`/`WriteView` overloads for `TvRemoteBlockEntity`'s persistence. `NbtCompound`'s own raw accessors changed too, independent of the View split: `getString`/`getInt`/`getLong` etc. return `Optional<T>` (or use the `(key, fallback)` overload), and `contains(key)` is single-argument (no more type-check overload).
- **`Item#appendTooltip`** signature changed again, from `(ItemStack, TooltipContext, List<Text>, TooltipType)` to `(ItemStack, TooltipContext, TooltipDisplayComponent, Consumer<Text>, TooltipType)` — append via `textConsumer.accept(...)` instead of `tooltip.add(...)`.
- **`Entity#getWorld`** was renamed to `getEntityWorld()` (covariantly typed `ServerWorld` on `ServerPlayerEntity`). `ServerPlayerEntity#getServer()`/`#getServerWorld()` convenience methods are gone — use `player.getEntityWorld().getServer()` and `player.getEntityWorld()` respectively.
- **`pack.mcmeta` is optional and was removed.** Fabric mods don't need one — Fabric API's own 1.21.11 jars ship without any `pack.mcmeta` at all, and Fabric Loader supplies safe defaults for a mod's resource/data packs when it's absent. The file matters if you actually try to hand-maintain it: Minecraft split client resource-pack and data-pack format numbers further apart (this build: `resource_major` 75, `data_major` 94, per the merged jar's `version.json`), and a legacy single `"pack_format"` integer can satisfy vanilla's dual validation (`PackVersion.Format#validate`) only up to format 64 for resources / 81 for data before it demands explicit `min_format`/`max_format` (and, right at those exact boundary numbers, `supported_formats` too) — the two thresholds don't move together, so a single shared pack.mcmeta can't cleanly express "new" for one resource type and "old" for the other at arbitrary format numbers. Simplest fix: don't ship the file.
- **Recipe ingredient JSON**: the `{"item": "namespace:path"}` and `{"tag": "namespace:path"}` object shorthands are gone from `Ingredient`'s codec; use a bare string instead (`"namespace:path"` for an item, `"#namespace:path"` for a tag). The `result` object's `id`/`count` shape (established in the 1.20.1 → 1.21.1 port) is unchanged.
- **`me.lucko:fabric-permissions-api`**: bump from `0.3.1` to `0.6.1` (the `0.6.0` release is explicitly "Update to 1.21.11"; the API package/surface is unchanged, `Permissions.check(...)` compiles as-is). Do **not** jump to `0.7.0` — it targets 26.1's newer required Java version and its class files (major version 69, i.e. Java 25) can't be read by this project's JDK 23 toolchain (`class file has wrong version 69.0, should be 67.0`). Older pins fail differently and more dangerously: `0.3.1` compiles fine against 1.21.11 but throws `NoSuchMethodError` on the very first permission check at runtime, because the intermediary method it was built against no longer maps onto the same Entity method — a reminder that a clean compile against a mapped dependency doesn't prove runtime compatibility; only running the mod does.
- **Toolchain**: no build-tool version changes were needed beyond the version pins in `gradle.properties` (`minecraft_version`, `yarn_mappings`, `fabric_api_version`) — JDK 21+/Gradle `9.5.0`/Loom `1.17.18` from the 1.21.1 branch carried over unchanged. `it.options.release = 17` also carried over unchanged; nothing about this port required raising the mod's own compiled bytecode target, only the JDK used to *run* the build (JDK 23 here, since it must be able to read fabric-permissions-api's class files at whatever version they were compiled for).

## Loader boundary

Keep these concepts loader-neutral:

- YouTube URL validation and normalization
- TV owner, URL-list, selected-index, and single-URL migration NBT fields
- per-TV volume persistence and clamping
- the server playback clock
- `WaterMediaBridge.Playback`
- remote bindings and range rules

Keep these behind loader-specific implementations:

| Concern | Fabric | NeoForge |
| --- | --- | --- |
| Initialization | Fabric entrypoints | Mod constructor/event bus |
| Packets | Fabric networking API | NeoForge payload registration |
| Permissions | Fabric Permissions API | NeoForge permission service or LuckPerms integration |
| Renderer registration | Fabric client registration | NeoForge client setup event |
| Mod presence | Fabric Loader | NeoForge `ModList` |

`WaterMediaBridge` already detects Fabric, NeoForge, and legacy Forge mod lists without linking directly to a loader class. The remaining Fabric imports should be moved behind platform services when the first NeoForge workspace is created.

## Required regression tests for every port

- A dedicated server starts without WATERMeDIA installed.
- Clients without WATERMeDIA can join safely; their TVs simply do not render video or play audio.
- WATERMeDIA `3.0.0.23+` plus matching Binaries resolves and plays `watch`, `youtu.be`, Shorts, live, and embed links.
- The video is neither horizontally mirrored nor shader-tinted on all four TV models and all four horizontal facings.
- A late-joining player seeks to approximately the server playback position.
- Turning a TV off pauses audio; turning it on resumes from the saved server position.
- Existing single-URL TV NBT migrates to a one-entry playlist without changing the active video.
- Add, duplicate-select, explicit-select, removal, final-entry removal/power-off, pagination, and the configured playlist cap behave safely.
- Normal TV/remote activation powers on or advances; sneak-using a TV or bound remote opens the target TV's playlist, power, and volume screen.
- Direct and remote-opened menus change the intended TV in its saved dimension, and the 0–100% volume survives save/reload and scales WATERMeDIA audio.
- Invalid schemes, non-YouTube hosts, user-info URLs, custom ports, and non-video YouTube pages are rejected server-side.
- `tv`, `mounted_tv`, `tv_2`, and `tv_2_standing` all open the playlist screen, persist URL lists, render the selected video, and bind to a remote.
- Cross-dimension, range-limited, missing-TV, and unloaded-chunk remote cases remain safe.
