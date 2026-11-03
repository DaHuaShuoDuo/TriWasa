# Development dependencies

`malilib-fabric-26.3-rc-1-0.29.99-dev.20260910.223448.jar` is a local development
dependency because Masa has not published this Minecraft 26.3 build to a public
Maven repository yet.

`litematica-fabric-26.3-rc-1-0.28.99-dev.20260916.183551.jar` is included for
the same reason.

MaLiLib and Litematica are required at runtime and are not bundled into the
TriWasa artifact.

`RoughlyEnoughItems-fabric-*.jar`, `cloth-config-fabric-*.jar`, and
`basic-math-0.6.1.jar` are compile-only dependencies used by the optional REI
Mixins. None of these dependencies are bundled.

`fabric-carpet-26.*.jar` and
`frostors-carpet-addition-fabric-26.*-0.1.0.jar` are development-only runtime
dependencies for the headed Fabric Client GameTests. They are not TriWasa
runtime requirements and are never bundled into the mod artifact.
