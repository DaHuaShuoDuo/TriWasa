# Third-party notices

## HalfMasa

The collapsible parent/child configuration hierarchy and the `[+]` / `[-]`
expansion interaction are adapted from
[HalfMasa](https://github.com/halfkite/halfmasa) by half_masa.

HalfMasa is licensed under the MIT License, copyright (c) 2026 half_masa. The
original license text is included at
`third_party/halfmasa-LICENSE.txt`.

## EarlyCompat

The V2-Extra protocol layout and high-fidelity block state adaptation are based
on [EarlyCompat](https://github.com/chenjunfu2/EarlyCompat) by chenjunfu2.
EarlyCompat is released under CC0-1.0:

https://github.com/chenjunfu2/EarlyCompat

TriWasa contains a client-side adaptation for Minecraft 26.2/26.3 and
integrates with the server-side implementation in Frostor's Carpet Addition.
EarlyCompat's Fanetlib-based network implementation and unrelated bug fixes are
not copied.

## Item Pickup Filter

The client-side pickup filtering behavior is adapted from
[Item Pickup Filter](https://github.com/dev-jcwd/Item-Pickup-Filter) by jcwd.

Item Pickup Filter is licensed under the MIT License, copyright (c) 2026 jcwd.
The original license text is included at
`third_party/item-pickup-filter-LICENSE.txt`.
TriWasa reimplements the behavior for Minecraft 26.2/26.3, moves all options to
MaLiLib's configuration screen, and replaces the global inventory scan with a
pickup-triggered pending-drop queue.

## Frostor's Carpet Addition

TriWasa implements a compatible client for FCA's public big ender chest protocol
v2. Protocol constants, NBT field names and wire records are mirrored so the
client can connect to a remote server without loading FCA Java classes.

Frostor's Carpet Addition is licensed under the MIT License, copyright (c)
2026 Frostor. TriWasa does not bundle FCA, its assets, or unrelated server code.
The terminal implementation, layout renderer and interaction controller are
TriWasa-specific and do not contain AE2 client code or textures.

## Quick Shulker

The optional hand-restock integration uses Quick Shulker's public client helper
to request that a carried shulker box be opened:

https://github.com/MoRanpcy/quickshulker

Quick Shulker is licensed under the MIT License. TriWasa does not copy or bundle
Quick Shulker code, and owns its own menu confirmation, hotbar swap, timeout and
recovery state machine.
