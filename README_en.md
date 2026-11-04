# TriWasa

English | [简体中文](https://github.com/DaHuaShuoDuo/TriWasa/blob/main/README.md)

TriWasa is a Masa client add-on for Minecraft Java. It currently provides FCA
V2-Extra easy-place support, a big ender chest terminal, Quick Shulker hand
restocking, a client pickup filter, and several optional client compatibility
fixes.

> **AI-assisted development disclosure:** Some code, tests, and documentation
> were produced with generative AI assistance and reviewed by the maintainer.
> Feature design, release decisions, and maintenance remain the maintainer's
> responsibility.

The mod ID is `triwasa`. TriWasa is a client-only mod and should not be installed
on a dedicated server.

## Supported Versions

| Minecraft | Fabric Loader | Fabric API | MaLiLib | Litematica |
| --- | --- | --- | --- | --- |
| 26.2 | >= 0.19.3 | >= 0.152.1 | >= 0.29.5 | >= 0.28.6 < 0.29.0 |
| 26.3 | >= 0.19.5 | >= 0.160.5 | >= 0.30.2 | >= 0.29.1 < 0.30.0 |

TriWasa requires Java 25. Install the build matching your Minecraft version.

## Installation and Dependencies

Required:

- Java 25
- Fabric API
- MaLiLib
- Litematica

Optional:

- Frostor's Carpet Addition: reuse its public protocol API when loaded in the
  same process
- Roughly Enough Items: enables REI interface fixes
- Quick Shulker: enables hand-item restocking

When FCA is loaded in the same process, TriWasa reuses its payloads and codecs.
Without FCA, TriWasa continues to use its wire-compatible implementation.
TriWasa and EarlyCompat cannot be installed together.

Enable the required rules on the FCA server when needed:

```text
/carpet easyPlaceProtocolExtra true
/carpet bigEnderChest true
```

## Feature Overview

### V2-Extra Easy Place

TriWasa encodes FCA V2-Extra block states at Litematica's actual placement call
sites. It supports normal easy place and `litematica-printer`, including rail
shape preservation, crafter slot locks, and Litematica V2/V3 hit-position
handling. FCA remains authoritative for final block state and placement
validation.

The master switch is `easyPlaceV2ProtocolExtra`.

### Big Ender Chest Terminal

After FCA accepts a compatible handshake, TriWasa replaces the vanilla 6×9
aggregate menu with its custom terminal. The terminal provides:

- Complete index snapshots with CRC32 validation, continuous deltas, and
  automatic resynchronization.
- Local search, sorting, scrolling, and item-list paging.
- Stack/half-stack extraction, quick move to inventory, batch insertion, and
  dropping.
- Large slot amounts use compact K/M/G units while tooltips retain the exact total.
- Capability hot-switching with fallback to the vanilla 6×9 menu.
- Visible aggregate entries are exposed as client-only read-only slots so
  shulker box, bundle, and other container preview integrations can inspect them.

FCA publishes the live `bigEnderChest` rule state through Carpet rule events. A
client may complete the protocol handshake while the rule is disabled; enabling
it later requires no reconnect or world reload, and the first chest opened after
the change uses the terminal.

The master switch is `bigEnderChestTerminalEnabled`. Terminal size, sorting, and
search-persistence options are available in the MaLiLib configuration screen.

### Quick Shulker Hand Restocking

`quickShulkerHandRestock` can restock the selected hand item from Quick Shulker
boxes in the player inventory. It tracks only the active hotbar item whose count
decreases and does not read schematic material lists. Quick Shulker must be
installed on both the client and server.

### Client Pickup Filter

After the client receives a pickup result, `itemPickupFilterEnabled` can discard
matching items by stack using a blacklist or whitelist. It cannot replace server
permissions, region protection, or entity interception.

### Other Mod Fixes

TriWasa automatically fixes REI submenu clipping and terminal interaction
bounds, and prevents Inventory Profiles Next from injecting sort buttons into
the big ender chest terminal. These fixes do not add required dependencies.

## Configuration

TriWasa stores configuration in `config/triwasa.json` through MaLiLib:

- **Porting**: V2-Extra easy place and pickup filtering.
- **Utilities**: Quick Shulker and the big ender chest terminal.
- **Other Mod Fixes**: applied automatically when the matching mod is present.

The `[+]` / `[-]` control on a parent only expands or collapses child options;
it is not a feature switch. Missing configuration, search filtering, and a
disabled parent do not permanently discard recoverable child settings.

## Compatibility

- TriWasa is client-only.
- TriWasa and EarlyCompat both take over easy-place handling and cannot be used
  together.
- FCA owns the big ender chest, protocol, and final placement validation; TriWasa
  does not store authoritative server state.
- REI, Quick Shulker, and Inventory Profiles Next are optional compatibility
  targets.

## Building

Use the project Wrapper with a Java 25 JVM. `JAVA_HOME` may point to any JDK 25
installation.

```bash
export JAVA_HOME=/path/to/jdk-25
./gradlew :26.2:build :26.3:build
```

Windows PowerShell:

```powershell
$env:JAVA_HOME = '<JDK 25 installation>'
.\gradlew.bat :26.2:build :26.3:build
```

Prepare the matching FCA API described in
[`libs/README.md`](https://github.com/DaHuaShuoDuo/TriWasa/blob/main/libs/README.md).
Shared source, resource, or Mixin changes must be built for both 26.2 and 26.3.

Every PR runs change classification and `CI Gate`; the full two-version build
runs only when build-related files change.

## License and Third-Party Sources

TriWasa uses the
[MIT License](https://github.com/DaHuaShuoDuo/TriWasa/blob/main/LICENSE).

The easy-place protocol and some state adapters reference EarlyCompat
(CC0-1.0); the collapsible parent/child configuration interaction references
HalfMasa (MIT). See
[THIRD_PARTY_NOTICES.md](https://github.com/DaHuaShuoDuo/TriWasa/blob/main/THIRD_PARTY_NOTICES.md)
for additional notices.

TriWasa does not redistribute MaLiLib, Litematica, FCA, or Quick Shulker source
code or binaries.
