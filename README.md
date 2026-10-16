# Thaumic Tinkerer: Reforged

Unofficial port of **Thaumic Tinkerer 2.5** by Vazkii, pixlepix and nekosune from Minecraft 1.7.10 to **Minecraft 26.3** on **NeoForge 26.3**. It is an addon to [Thaumcraft 4: Reforged](../thaumcraft-4-reforged) and needs it to run.

The goal is to recreate the original gameplay as closely as possible, including the KAMI content. The Bedrock dimension, ComputerCraft peripherals and integrations with other mods (except JEI) are not ported. The technical side is rebuilt on modern APIs.

The project is a scaffold for now: registries, platform seams, config, game test harness and the Python tooling are in place, gameplay content is ported feature by feature.

> **Personal use only.** The original Thaumic Tinkerer is licensed under CC BY-NC-SA 3.0. This port is not intended for redistribution.

## Requirements to build

- Java 25
- Python 3
- A checkout of Thaumcraft 4: Reforged next to this repository (`../thaumcraft-4-reforged`)

Versions match the base mod:

- Mod `1.0`
- Minecraft `26.3`
- NeoForge `26.3.0.57-beta`
- ModDevGradle `2.0.148`
- TerraBlender `26.3-26.3.0.0.9` (required by the base)
- JEI `31.9.0.58` (optional at runtime; included in development runs)

## Building and running

The base mod is included as a Gradle composite build (`includeBuild("../thaumcraft-4-reforged")`), so Gradle builds it automatically and both mods are loaded in every run.

```sh
export JAVA_HOME=/path/to/openjdk-25

./gradlew build               # build the addon jar into build/libs
./gradlew runClient           # start a development client
./gradlew runServer           # start a development server (game folder: run/)
./gradlew runGameTestServer   # run the addon game tests only
python3 tools/check_seams.py  # check version-specific API boundaries
python3 tools/inventory.py --check
```

## Tools

| Script                    | Purpose                                                                                    |
| ------------------------- | ------------------------------------------------------------------------------------------ |
| `tools/fetch_tt.py`       | Downloads the Thaumic Tinkerer source (branch `1.7-final`) into `reference/tt-src`          |
| `tools/convert_assets.py` | Converts original textures and `.lang` files, merges extra translations from `tools/lang/`  |
| `tools/gen_resources.py`  | Generates blockstates, models, loot tables, recipes and tags from per-feature generators   |
| `tools/check_seams.py`    | Checks version-specific API boundaries                                                     |
| `tools/inventory.py`      | Checks `tools/id_map.json` against registrations, models and lang                         |
| `tools/id_map.json`       | Maps every original block and item (with metadata) to a modern id                          |

## Credits

- **Vazkii, pixlepix and nekosune** for the original Thaumic Tinkerer (CC BY-NC-SA 3.0)
- **Azanor** for Thaumcraft
