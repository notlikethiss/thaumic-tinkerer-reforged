# Backport to Minecraft 1.21.1

Source addon baseline: `e498a76` on `main`. Minecraft 26.3 remains on `main`; this branch is `mc/1.21.1`.

Required Thaumcraft base: branch `mc/1.21.1`, commit `1d10115` or a descendant containing its addon contracts. Base baseline before these changes: `8348611`. The base changes are committed separately and do not import its general configuration phase.

CI resource validation also requires the base's portable Java selection fix `9d274e3`. Update the base branch before rerunning the addon workflow; the validator no longer requires a Homebrew JDK path.

Targets: Minecraft 1.21.1, NeoForge 21.1.256, ModDevGradle 2.0.148, Java 21 toolchain, Gradle on Java 25, JEI 19.57.0.451, TerraBlender 1.21.1-4.1.0.1, Curios 9.5.1+1.21.1. The base retains its Parchment 1.21.1 / 2024.11.17 mapping profile; the addon uses the same mappings.

## Maintenance

1. Review changes since the source baseline on `main`.
2. Apply shared behavior changes without dropping existing features or changing IDs. `tools/backport/rewrite.py --target 1.21.1` handles mechanical Java mappings; lifecycle and rendering changes require review.
3. Adapt version differences in `platform/` and `client/platform/`. The base's value IO, registration, transfer and baubles adapters remain the integration boundary.
4. Regenerate assets with `convert_assets.py`, then resources with `gen_resources.py`. Keep both resource profiles working; never edit generated JSON or textures directly.
5. Run the commands in README, runtime recipe comparison, client/server loading and relevant original-behavior scenarios. Record the new source baseline only after verification.

## Differences

- Configuration uses NeoForge SERVER configs, preserving all eight keys/defaults and server authority.
- Attachments use explicit payloads, including lifecycle resynchronization. Inventory transfer proxies resolve legacy item/fluid capabilities on each access and preserve the requested side.
- Block removal explicitly invokes the base cleanup bridge. Neighbor notifications pass a position instead of the newer nullable orientation.
- Armor uses 1.21.1 ArmorItem materials/layers. Item modes use integer custom model data. Namespace-qualified textures remain in `thaumictinkerer`.
- GUI, block rendering, player layers, custom items, predicates, tinting and camouflage use 1.21.1 APIs. Version-independent geometry stays shared.
- Tests keep the suite interface and names through a generator; the namespace-filtered structure has the same platform layout as the base. A helper offsets relative Y by one.
- `tools/validate_backport.py` reuses the matching base's Minecraft identifier validator. Deterministic generation covers JSON, textures and the test structure.

## Original behavior audit

The tables of 74 research entries and 129 original recipe records were re-extracted without changes. Runtime comparison has zero hard mismatches. Its ten informational differences are documented in the validation report; excluded recipes remain excluded.

The enchanter's 40 entries are independently extracted and compared with runtime IDs, textures, research gates and aspect costs at every supported level. Eight higher enchantments retain the original Vampirism research gate; the inherited port had incorrectly required their individual research entries. A regression test now checks the original access rules.

The original tree tool increments gathered drops' entity tick counter by 20 and preserves their pickup delay. This branch restores that behavior instead of clearing the pickup delay. Additional tests cover tree pickup timing and the existing collection of mass-harvest drops toward the player. The original iteration helper ignores its block-type argument; column mode therefore continues to harvest all matching shovel materials, including across intervening stone.

See [validation results and remaining manual acceptance](docs/BACKPORT_VALIDATION.md) and [feature coverage](docs/BACKPORT_AUDIT.md). Visual comparison, a two-client playthrough and load/FPS measurements remain manual; successful automated checks do not establish those results.
