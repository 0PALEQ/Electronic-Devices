# Electronic Device Mod workspace

This workspace keeps every active Minecraft/loader target under `versions/` and
stores common resources and loader-independent compatibility code under
`shared/`.

## Layout

```text
edm/
├── shared/
│   └── src/main/resources/       Common assets and data used by every target
└── versions/
    ├── fabric-1.20.1/
    ├── fabric-1.21.1/
    ├── fabric-1.21.11/
    ├── fabric-26.1.2/
    ├── fabric-26.2/
    ├── forge-1.20.1/
    ├── neoforge-1.21.1/
    ├── neoforge-1.21.11/
    └── neoforge-26.1.2/
```

Each version remains an independent Gradle project with its own wrapper,
dependencies, Java sources, metadata, and version-specific resources. Its
`build.gradle` adds the shared Java and resource trees to the main source set.

## Resource ownership

- Put a resource in `shared/src/main/resources` only when the same file works
  unchanged for every active target.
- Keep loader metadata and Minecraft-version-specific assets/data in the
  corresponding `versions/<loader>-<minecraft>/src/main/resources` tree.
- Run a version's wrapper from that version directory, for example:
  `cd versions/fabric-1.20.1` followed by `./gradlew build`.

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
## YouTube language preference

Client config `config/electronic_device_mod-media.json` includes
`preferGameLanguageForYouTube` (default false). When enabled, EDM passes
the base Minecraft language (for example `en` for `en_us`) to WaterMedia's
public player API to select a matching audio track when available.
Verify the reflective API against a downloaded stable WaterMedia jar with:
`powershell -ExecutionPolicy Bypass -File scripts/verify-watermedia.ps1 -WaterMediaJar <jar> -JavaHome <jdk>`.
This checks binary signatures for all adapters without initializing native codecs.

TV playback also accepts all WaterMedia platform hosts and direct media URLs,
including HTTP(S), `file://` and `water://` resources. See [shared/README.md](shared/README.md)
for examples, client-local file behavior and remote playlist limitations.
