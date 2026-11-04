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
including HTTP(S), `file://` and `water://` resources.
## Server media source permissions

On the server, edit `config/electronic_device_mod-media.json` after its first
startup, then restart the server. Existing configs automatically receive the new
`mediaSources` section. Client settings cannot override the server's rules.
The same settings apply to the integrated server in singleplayer.

Each entry under `mediaSources.sources` has two settings:

- `enabled`: allow this source (default `true`). Setting it to `false` blocks
  everyone, including administrators.
- `requireAdmin`: require server operator level 2 or higher (default `false`).
  On Fabric, the permission `electronic_device_mod.media.admin` can also be
  assigned through a permissions provider.

Example section inside the existing config (keep the other generated entries):

```json
"mediaSources": {
  "sources": {
    "youtube": { "enabled": true, "requireAdmin": false },
    "twitch": { "enabled": true, "requireAdmin": true },
    "adult_content": { "enabled": false, "requireAdmin": false },
    "direct": { "enabled": true, "requireAdmin": true },
    "file": { "enabled": false, "requireAdmin": false },
    "water": { "enabled": false, "requireAdmin": false }
  }
}
```

Available source keys: `youtube`, `twitch`, `kick`, `bilibili`, `odysee`,
`dtube`, `vidlii`, `twitter` (also X), `bluesky`, `tiktok`, `facebook`,
`instagram`, `soundcloud`, `newgrounds`, `imgur`, `lightshot`, `medal`,
`streamable`, `sendvid`, `googleDrive`, `dropbox`, `mediafire`, `adult_content`,
`direct`, `file`, and `water`.

`direct` covers all HTTP(S) hosts not recognized as one of the named sources,
including custom streams and future platforms. Platform matching includes
subdomains and common short-link aliases. It classifies the submitted URL;
it does not inspect redirects, remote playlists, or the eventual content of
third-party URLs. To restrict arbitrary external links, restrict `direct` too.
`file` and `water` are separate because they refer to resources on each client.

Rules are checked on the server when adding URLs, selecting saved entries,
turning a TV on, and cycling media through the TV or remote control. Removing
the selected entry turns the TV off so removal cannot start a restricted entry.
Administrator restrictions control who can start media; other nearby players
can still watch playback started by an administrator. Existing ownership and
URL assignment permissions continue to apply.

Disabled sources are also suppressed in TV state sent by the server, including
TVs that were already playing when the world was saved. Their playlist entries
remain available if the source is enabled again later.

### Adult content category

`adult_content` applies one rule to the built-in adult-site domain list and all
its subdomains. To include additional sites, add bare domains under
`mediaSources.additionalAdultDomains`, alongside `sources`, for example:

```json
"additionalAdultDomains": ["adult-site.example", "another-adult-site.example"]
```

This category matches domains, not the content of individual videos. No domain
list can identify every adult site or adult content on general-purpose platforms.
Unknown hosts use `direct` unless added to `additionalAdultDomains`. Redirects
and remote playlist contents are not inspected. Restrict `direct` as well if
unknown hosts must not be available. Previous platform-specific settings migrate
automatically to `adult_content`, preserving the more restrictive options.
