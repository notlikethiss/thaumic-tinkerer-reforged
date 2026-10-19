# Thaumic Tinkerer: Reforged

[![Thaumic Tinkerer: Reforged](poster.png)](poster.png)

Unofficial port of **Thaumic Tinkerer 2.5** by Vazkii, pixlepix and nekosune from Minecraft 1.7.10 to **Minecraft 1.21.1** on **NeoForge 21.1**. It is an addon to [Thaumcraft 4: Reforged](https://github.com/notlikethiss/thaumcraft-4-reforged) and needs it to run.

The goal is to recreate the original gameplay as closely as possible, including the KAMI content. The Bedrock dimension, ComputerCraft peripherals and integrations with other mods (except JEI) are not ported. The technical side is rebuilt on modern APIs.

This branch includes every existing addon feature module, including KAMI. Visual acceptance and two-client gameplay checks remain manual.

> **Personal use only.** The original Thaumic Tinkerer is licensed under CC BY-NC-SA 3.0. This port is not intended for redistribution.

## Requirements to build

- Java 21 toolchain; Java 25 to run the bundled Gradle wrapper
- Python 3
- A checkout of Thaumcraft 4: Reforged next to this repository (`../thaumcraft-4-reforged`), on `mc/1.21.1` with the addon API from commit `1d10115` or a compatible descendant

Versions match the base mod:

- [Thaumcraft 4: Reforged](https://github.com/notlikethiss/thaumcraft-4-reforged) `1.0`
- Minecraft `1.21.1`
- NeoForge `21.1.256`
- ModDevGradle `2.0.148`
- TerraBlender `1.21.1-4.1.0.1` (required by the base)
- Curios `9.5.1+1.21.1` (required by the base)
- JEI `19.57.0.451` (optional at runtime; included in development runs)

## Building and running

The base mod is included as a Gradle composite build. Discovery selects a nearby checkout targeting the same Minecraft version, including `.worktrees/mc-1.21.1`. Override it with `-PthaumcraftDir=/absolute/path/to/base`. Gradle builds the base automatically and loads both mods in development runs.

```sh
export JAVA_HOME=/path/to/openjdk-25

./gradlew build               # build the addon jar into build/libs
./gradlew runClient           # client: run/client
./gradlew runClient -PwithoutJei # optional JEI must not be required
./gradlew runServer           # start a development server (game folder: run/server)
./gradlew runGameTestServer   # run the addon game tests only
python3 tools/check_seams.py  # check version-specific API boundaries
python3 tools/inventory.py --check --strict
python3 tools/validate_backport.py
```

## Automatic releases

GitHub Actions builds pushes to `main` and `mc/**` and can also be started manually with **Run workflow**. Pushes that only change Markdown files are skipped.

The workflow checks out Thaumcraft 4: Reforged alongside the addon, using the matching `mc/**` branch for Minecraft branches and `main` otherwise. Java, Minecraft and mod versions are read from `gradle.properties`.

After resource validation, seam checks, the Gradle build, GameTests and recipe comparison pass, the workflow replaces the release and tag named after the Minecraft version (currently `1.21.1`), attaches the addon JAR and uses the latest commit as release notes. Releases from `main` are marked as latest. This follows the release process used by Thaumcraft 4: Reforged.

## Tools

| Script                    | Purpose                                                                                    |
| ------------------------- | ------------------------------------------------------------------------------------------ |
| `tools/fetch_tt.py`       | Downloads the Thaumic Tinkerer source (branch `1.7-final`) into `reference/tt-src`         |
| `tools/convert_assets.py` | Converts original textures and `.lang` files, merges extra translations from `tools/lang/` |
| `tools/gen_resources.py`  | Generates blockstates, models, loot tables, recipes and tags from per-feature generators   |
| `tools/check_seams.py`    | Checks version-specific API boundaries                                                     |
| `tools/inventory.py`      | Checks `tools/id_map.json` against registrations, models and lang                          |
| `tools/id_map.json`       | Maps every original block and item (with metadata) to a modern id                          |

## Credits

- **Vazkii, pixlepix and nekosune** for the original Thaumic Tinkerer (CC BY-NC-SA 3.0)
- **overseas** for the port
- **Azanor** for Thaumcraft
