# Development dependencies

JAR files in this directory are ignored by Git and are not distributed with TriWasa.
Place the required files here before running a build.

MaLiLib and Litematica:

- `malilib-fabric-26.3-rc-1-0.29.99-dev.20260910.223448.jar`
- `litematica-fabric-26.3-rc-1-0.28.99-dev.20260916.183551.jar`

These are Minecraft 26.3 development builds from the Masa/Sakura-Ryoko distribution.
MaLiLib and Litematica are required at runtime but are not bundled into the TriWasa artifact.

Optional FCA protocol API compilation:

- `frostors-carpet-addition-fabric-26.2-0.1.0.jar`
- `frostors-carpet-addition-fabric-26.3-0.1.0.jar`

These are compile-only inputs for TriWasa's FCA payload adapters. They must match
`fca_version` in `gradle.properties`; FCA remains optional at runtime and is not bundled.

Optional REI Mixin compilation:

- `RoughlyEnoughItems-fabric-26.2.820.jar`
- `RoughlyEnoughItems-fabric-26.3.824.jar`
- `cloth-config-fabric-26.2.155.jar`
- `cloth-config-fabric-26.3.158.jar`
- `basic-math-0.6.1.jar`

Download these versions from their official Modrinth, CurseForge or Maven releases.
REI remains optional at runtime and is not bundled.

The JSON/Java build files define exact filenames and versions. Do not commit local JARs.
