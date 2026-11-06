# Development dependencies

JAR files in this directory are ignored by Git and are not distributed with TriWasa.
Place the required files here before running a build.

MaLiLib and Litematica are resolved from the public Masa/Sakura-Ryoko Maven repository
for both supported Minecraft versions. They are required at runtime but are not bundled
into the TriWasa artifact.

FCA protocol API compilation:

- `frostors-carpet-addition-fabric-26.2-0.2.0-beta.3.jar`
- `frostors-carpet-addition-fabric-26.3-0.2.0-beta.3.jar`

These are compile-only inputs for TriWasa's FCA payload adapters. They must match
`fca_version` in `gradle.properties`; FCA remains optional at runtime and is not bundled.

REI, Cloth Config and basic-math are resolved from the public Shedaniel Maven repository.
FCA and these optional APIs remain compile-only and are not bundled.

The JSON/Java build files define exact filenames and versions. Do not commit local JARs.
