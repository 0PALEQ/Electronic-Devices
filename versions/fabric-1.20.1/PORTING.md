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

The video quad uses `POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL` with Minecraft's emissive entity program. Every vertex supplies opaque white color, the default overlay, full-bright light, and a transformed front normal. Do not simplify this back to `POSITION_TEXTURE`: Iris and other shader-pack pipelines can substitute a world shader that expects the complete entity format, and missing attributes can be read as invalid color data (observed as a strong yellow tint).

## Target branches

Create one source branch or Gradle subproject per Minecraft API generation:

| Target | Loaders | WATERMeDIA policy |
| --- | --- | --- |
| Minecraft 1.20.1 | Fabric | WATERMeDIA `3.0.0.23+` and Binaries `3.0.0.6+` |
| Minecraft 1.21.11 | Fabric and NeoForge | Latest explicitly tested v3 and matching Binaries |
| Minecraft 26.1.2 | Fabric and NeoForge | Latest explicitly tested v3 and matching Binaries |
| Minecraft 26.2 | Fabric and NeoForge | Latest explicitly tested v3 and matching Binaries |

Do not use an unbounded dynamic Gradle dependency for release builds. Record the tested versions in each branch and publish them in that branch's metadata. Runtime reflection remains useful for patch-level API tolerance, but the release should still state which versions were tested.

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
