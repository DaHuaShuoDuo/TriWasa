# Easy Place Registry Plan Locator

The canonical plan is:

`D:\mcgaoshou\Frostors-Carpet-Addition\EASY_PLACE_REGISTRY_PLAN.md`

This TriWasa file is only a locator. Read the canonical plan before changing
TriWasa Easy Place code. Do not create or maintain a second independent plan
here.

Fixed TriWasa constraints from the canonical plan:

- the client uses its own registry, not FCA's server registry;
- `EasyPlaceStateCodec.encode(...)` and `apply(...)` remain facades during the
  migration;
- wire values and protocol version must remain compatible;
- state adapters do not send packets or decide server placement success;
- printer-specific hooks are limited to target/click transforms that cannot be
  expressed through the common Litematica protocol entry points.

Current TriWasa baseline: branch `codex/easy-place-registry`; registry migration
commit `2ac269a`, family test split commit `f3dc45f`. The canonical plan records
the completed family split and dual-version runtime verification.
