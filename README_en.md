# TriWasa

English | [简体中文](https://github.com/DaHuaShuoDuo/TriWasa/blob/main/README.md)

TriWasa is a Masa client add-on for Minecraft Java. It currently provides FCA
V2-Extra easy-place support, Better Easy Place fixes, Better Printer fixes, a big ender chest
terminal, Quick Shulker hand restocking, a client pickup filter, and several
optional client compatibility fixes.

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
  same process; the big ender chest terminal requires matching FCA `0.2.1-beta.1`
- Hana's Fourth-Revision Printer (the Hana `litematica-printer` branch): enables Better Printer
- Mod Menu: provides a configuration button in the mod list
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
sites. It supports normal easy place and Hana's Fourth-Revision Printer
(the Hana `litematica-printer` branch), including rail
shape preservation, crafter slot locks, and Litematica V2/V3 hit-position
handling. FCA remains authoritative for final block state and placement
validation.

The master switch is `easyPlaceV2ProtocolExtra`.
Its rail, wall, observer, TNT, and piston protection children all default to
enabled. The rail switch
suppresses rail shape recalculation and PP updates and preserves the initial
`POWERED` state of powered, activator, and detector rails. In 26.x, powered and
activator rails both use `PoweredRailBlock`, while detector rails use the
separate `DetectorRailBlock`. The client compares adjacent actual rails with
the projection and sends a match mask through the Z channel, so only rail type
and shape matches are protected. Neighbor redstone state is not compared, and
ordinary rails have no redstone state.
Disabling the wall switch omits wall V2-Extra state and wall-protection flags,
returning wall placement and subsequent updates to vanilla behavior.
While enabled, the client compares each adjacent wall in the actual world with
Litematica's projected state and sends a per-direction match mask through the Z
channel. `WATERLOGGED` is ignored; other mismatches do not receive neighbor
update protection.
Observers use a dedicated family that synchronizes only `FACING`; projected
`POWERED` is never encoded, so server-side placement always starts inactive.
The peer `easyPlaceObserverProtection` switch defaults to enabled; disabling it
omits observer V2-Extra state and protection flags, restoring vanilla placement
and detection-face updates.
The peer `easyPlaceTntProtection` switch defaults to enabled and prevents
easy-place redstone priming of TNT while enabled. TNT has no persistent active
block state, so no TNT state adapter is added.
The request is emitted from the common `BlockItem` placement path for all block
items, including standing/wall coral-fan items.
The peer `easyPlacePistonProtection` switch compares the placement target, six
face neighbors, and five QC second-hop candidates with the projection while
the projected piston remains unextended. Matching entries are packed into the
12-bit mask in Y bits 8-19 with `Y_PISTON_SCHEMA` at bit 20. Projected
`EXTENDED` is never restored, and mismatched pistons retain vanilla updates.
Disabling the rail switch omits the rail protection flag and match mask, as
well as the initial active-state bit for powered, activator, and detector
rails, returning rail synchronization and updates to the unprotected path.

Client-side state encoding and decoding uses explicit behavior-family registries
that freeze after initialization. The stable
`EasyPlaceStateCodec.encode(...)` / `apply(...)` entry points select the owning
adapter; concrete codecs live under `easyplace/adapter` in directional,
attachment, rail, wall, observer, multiface, quantity, container, sign, redstone, and special
families, with shared pure state operations in `EasyPlaceStateSupport`.
Target-position and click-face transforms for wall bells, hanging signs,
candles, and similar placement cases remain in dedicated rules and client
placement entry points rather than state adapters. Mixins only bridge the
required Litematica call paths. All vanilla wall blocks encode their four side
connections as base-3 raw data. `UP` uses bit 26 as a schema marker and bit 27
as its value while the server continues to accept the legacy bit-zero layout;
`WATERLOGGED` is omitted and remains authoritative from FCA's server-side
placement state. The old monolithic
`LegacyEasyPlaceStateCodec` mapping has been removed. The V2-Extra payload
marker and protocol version remain 1. In the auxiliary Z channel, the low nine
bits remain the crafter slot mask, while bits 9 and 10 request rail and wall
update protection, bits 11-16 mark matching wall neighbors, and bit 17 requests
rail neighbors in `NORTH,SOUTH,WEST,EAST,UP` order through bits 17-21; bit 22
requests observer protection and bit 23 requests TNT protection; bit 24 is the
`AUXILIARY_SCHEMA` marker, bit 25 is free, and bits 26-27 are the wall schema
and `UP` value. Auxiliary Z data has two layouts. The legacy layout,
`z = auxiliary * 2 + 2`, stays anchored at world Z=0. After the server confirms
`easy_place_relative_aux`, only the low 24 bits remain in Z and are anchored at
the packet `blockPos`; the three high bits move to X payload bits 8-10. The
relative offset stays at or below 2^25 and every even value is exact in
binary32. The handshake also advertises `easy_place_y_precision` with
`y_precision_bits=8` and `easy_place_piston_protection` with
`piston_protection_bits=12`. Once confirmed, the client stores an eight-bit
fixed-point hit height in Y bits 0-7, the piston mask in bits 8-19, and the
schema marker at bit 20. X raw and Y precision are encoded relative to the
packet's `BlockHitResult.blockPos`, and FCA decodes them against the same anchor
before target transformations. The HELLO handshake must declare
`easy_place_packet_anchor`; a client without that capability falls back to
vanilla easy place instead of silently decoding the wrong raw state. Multiface wire
indices use the fixed order
`DOWN, UP, NORTH, SOUTH, WEST, EAST`; vine derives `UP, NORTH, SOUTH, WEST,
EAST` by filtering out `DOWN`. Clients or servers built with the historical
face order must be upgraded to a matching release, because mixed versions can
decode multiface and vine faces in the wrong direction. The old `main` snapshot
is preserved by the `legacy` tag in both repositories for historical
compatibility only and is no longer maintained.

### Better Easy Place

`betterEasyPlace` is enabled by default and is the master switch for TriWasa's
shared Litematica placement corrections. It does not depend on the FCA protocol handshake.
It fixes support-block and click-face selection for hanging signs and allows
unsupported wall hanging signs and candles to use a virtual target-position click.
Candle support is bypassed only during placement and does not change later
`updateShape` behavior.
Ceiling signs with `attached=true` still require a vertical chain above and will
not send an invalid placement while that dependency is missing. Easy Place also
suppresses vanilla wall-sign chaining so repeated clicks cannot place extra signs
in adjacent directions. Future generic
hand-placement or vanilla Mixin fixes should remain under
`mixin/bettereasyplace` and be controlled by this switch. Quantity-state blocks can continue incrementing after their first placement until they reach the projected amount. Candles, flower beds, leaf litter, sea pickles, snow layers, and turtle eggs are supported; each interaction adds one level and consumes one item.
Glow lichen, sculk veins, resin clumps, and other multiface blocks increment one face at a time at the same position until the projected face set is complete.
Vines use the same same-position incremental path for missing faces while retaining valid-attachment checks.

`easyPlaceFullBlockSelection` is a peer of `betterPrinter`, is disabled by
default, and is governed by the `betterEasyPlace` master switch. While enabled,
the easy place schematic trace treats torches, buttons,
slabs, stairs, fences, candles, and signs as full 1×1×1 blocks, so aiming at the
empty part of a cell still selects the target. The scope covers only that single
easy place schematic trace: area selection dragging, pick block, verifier
highlights, and printer targets keep the real outline shapes. The click uses the
full block hit position and face, so face-dependent blocks (slab halves, wall
torches, trapdoors) may end up with a facing that differs from the schematic when
FCA is not installed; an FCA server forcing the V2-Extra state is unaffected.

### Better Printer (Child Option)

`betterPrinter` is enabled by default and is a child option of `betterEasyPlace`.
It groups the Guide/Action-level Mixins that directly modify Hana's
Fourth-Revision Printer (the Hana `litematica-printer` branch) and is active only when
that printer is installed and the parent switch is enabled.
It allows unsupported wall hanging signs and candles to use the printer's existing
target-position click path and retains the vertical-chain dependency check for
ceiling signs with `attached=true`. Turtle eggs no longer receive the printer guide's synthetic below-support requirement, matching vanilla placement semantics. Printer-specific corrections are inactive
when this switch is off. Glow lichen, sculk veins, resin clumps, and other
`MultifaceBlock` variants share the same face-by-face printer guidance. Vines only try missing faces that have valid support, preventing repeated attempts against unsupported faces. Quantity blocks including flower beds, leaf litter, sea pickles, snow layers, candles, and turtle eggs increment until their target amount is reached; leaf litter reuses the flower-bed guide while reading `SegmentableBlock.AMOUNT`. The feature does not change later `updateShape`
behavior. The server must still accept the final state; a vanilla server without
FCA may reject unsupported wall-sign or candle placement. Shared client placement-state
bridges are enabled by the Better Easy Place master switch.

Better Printer supports only Hana's Fourth-Revision Printer
(the Hana `litematica-printer` branch), not the upstream original printer. Their Guide,
task-scheduling, and placement-confirmation chains differ, so the corresponding
Mixins are not interchangeable.

### Schematic Verifier Position Picker

Every mismatch-state row in the Litematica schematic verifier has a **Pick Position** button. The child screen lists every block position for that mismatch state and supports searching by coordinate, block ID, state, or display name. Click positions to toggle them, and keep the child screen open while selecting multiple blocks. Selections from different mismatch groups are independent and persist together; the world overlays, targeted outline, and verifier HUD show the selected blocks from every group. Each row has a **Copy** button for copying that block's integer `x y z` coordinates. **Show All** selects every position in the current group. Coarse-selecting a state row marks every position in that state as selected in the child screen; a row with exact selections is also highlighted, and clicking it again clears both the coarse and exact selections for that group.

This feature only narrows verifier highlighting. It does not change mismatch counts, sorting, or the scope of ignoring a mismatch state.
The `verifierPositionPicker` switch (enabled by default) controls the **Pick Position** button on each
mismatch row; when disabled the button is hidden, the verifier falls back to the vanilla per-category
highlighting, and existing exact selections no longer affect overlay merging.

### Big Ender Chest Terminal

After FCA accepts a compatible protocol v3 handshake, the server opens a vanilla `GENERIC_9x6` carrier (54 read-only placeholder slots plus 36 player slots), and TriWasa replaces the vanilla chest screen with its terminal. TriWasa does not register a custom `MenuType`, so vanilla clients on an integrated server or LAN do not need TriWasa installed. The terminal provides:

- Complete index snapshots with CRC32 validation, continuous deltas, and
  automatic resynchronization.
- Local search, sorting, scrolling, and item-list paging.
- Stack/half-stack extraction, quick move to inventory, batch insertion, and
  dropping.
- Large slot amounts use compact K/M/G units; terminal sub-options select small
  (default) or large amount text. Large abbreviates from 1000 with a shorter
  width, while tooltips retain the exact total.
- Open, close, and action messages support silent, action-bar, and chat feedback,
  defaulting to the action bar.
- Capability hot-switching with fallback to the vanilla 6×9 menu.
- Container components are passed through real item tooltip context so shulker
  box, bundle, and other preview integrations can inspect them.

FCA publishes the live `bigEnderChest` rule state through Carpet rule events. A
client may complete the protocol handshake while the rule is disabled; enabling
it later requires no reconnect or world reload, and the first chest opened after
the change uses the terminal.

The master switch is `bigEnderChestTerminalEnabled`. Feedback mode, terminal size,
sorting, and search-persistence options are available in the MaLiLib
configuration screen.

### Configuration Screen

Press `C+T` by default to open the main TriWasa configuration screen. The hotkey
can be rebound on the All tab. With Mod Menu installed, the configuration
button in the mod list opens the same screen.

Every true/false option has a keybind slot to the right of its switch; together
they fill the original row width, and all of them are unbound by default. Once
bound, pressing the key toggles that option exactly like clicking the switch.

### Quick Shulker Hand Restocking

`quickShulkerHandRestock` can restock the selected hand item from Quick Shulker
boxes in the player inventory. It triggers only when a main-hand right-click use
reduces the item or leaves a remainder; moving the stack or depositing it into a
container does not trigger a restock. It handles cases such as an empty bucket
after using a water or powder snow bucket or a bowl after eating stew. Remainders
use available inventory capacity first, then the shulker box, and are dropped
only when neither has room. It does not read schematic material lists. Quick
Shulker must be installed on both the client and server.

### Client Pickup Filter

After the client receives a pickup result, `itemPickupFilterEnabled` can discard
matching items by stack using a blacklist or whitelist. It cannot replace server
permissions, region protection, or entity interception.

### Projection Material Stats Fix

`projectionMaterialStatsFix` fixes Litematica's material list undercounting
multi-item and multiface blocks. It is off by default; when enabled, pink
petals, wildflowers and leaf litter count 1-4 segments, resin clumps count their
attached faces, and vines count up to five faces. It also removes the duplicated
multiplier in the recipe JSON export. Regenerate the material list after
toggling the switch.

### Other Mod Fixes

TriWasa automatically fixes REI submenu clipping and terminal interaction
bounds, and prevents Inventory Profiles Next from injecting sort buttons into
the big ender chest terminal. IPN compatibility is an automatic adapter and is
not shown as a configuration-page fix. None of these compatibility features add
required dependencies.

## Configuration

TriWasa stores configuration in `config/triwasa.json` through MaLiLib:

- **All**: the configuration hotkey, every feature option, and fix information
  in one searchable list.
- **Porting**: V2-Extra easy place and pickup filtering.
- **Auxiliary Features**: Better Easy Place, Better Printer, Quick Shulker, the big ender
  chest terminal, and the projection material stats fix.
- **Other Mod Fixes**: optional fixes that change another mod's own behavior.

The `[+]` / `[-]` control on a parent only expands or collapses child options;
it is not a feature switch. Missing configuration, search filtering, and a
disabled parent do not permanently discard recoverable child settings.

## Compatibility

- TriWasa is client-only.
- TriWasa and EarlyCompat both take over easy-place handling and cannot be used
  together.
- FCA owns the big ender chest, protocol, and final placement validation; TriWasa
  does not store authoritative server state.
- Better Printer supports only Hana's Fourth-Revision Printer
  (the Hana `litematica-printer` branch), not the upstream original `litematica-printer`.
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
`logicTest` runs the protocol and state tests through the stable
`EasyPlaceProtocolTest` entry point, which aggregates suites split across wire,
directional, attachment, rail, multiface, quantity, container, hanging-sign,
special-block, and printer/config behavior families.

Every PR runs change classification and `CI Gate`; the full two-version build
runs only when build-related files change.

### Release Build

Run **Release Build** manually from GitHub Actions. The workflow checks out the FCA
tag matching `fca_version`, builds both 26.2 and 26.3, and uploads a
`triwasa-<version>` Actions artifact containing both JARs, `SHA256SUMS.txt`, and
`BUILD-INFO.txt` for creating a GitHub Release.

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
