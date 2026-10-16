import argparse
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DEFAULT_OUT = ROOT / "src" / "generated" / "resources"
NS = "thaumictinkerer"


class Profile:
    name = "26.3"
    modern = True
    explicit_render_types = False
    data_version = 5023
    recipe_folder = "recipe"
    loot_folder = "loot_table"
    block_tag_folder = "tags/block"
    item_tag_folder = "tags/item"
    structure_folder = "structure"
    common_namespace = "c"
    biome_modifier_namespace = "neoforge"
    loot_modifier_namespace = "neoforge"
    feature_folder = "feature"
    pack_mcmeta = None
    vanilla_renames = {}
    dropped_vanilla_ids = set()
    dropped_tags = set()
    tag_expansions = {}
    data_maps = True
    enchantment_json = True

    def rename_vanilla(self, value):
        prefix = "#" if value.startswith("#") else ""
        bare = value[len(prefix):]
        return prefix + self.vanilla_renames.get(bare, bare)

    def aspect_target(self, target):
        if isinstance(target, list):
            result = []
            for entry in target:
                converted = self.aspect_target(entry)
                if isinstance(converted, list):
                    result.extend(converted)
                elif converted is not None:
                    result.append(converted)
            return result or None
        if target in self.dropped_vanilla_ids:
            return None
        if target in self.tag_expansions:
            return list(self.tag_expansions[target])
        return self.vanilla_renames.get(target, target)

    def tag_name(self, tag):
        namespace, path = tag.split(":")
        if namespace == "c":
            namespace = self.common_namespace
        return namespace, path

    def recipe_ingredient(self, value):
        return value

    def recipe_conditions(self, items):
        conditions = [{"type": "neoforge:registered", "value": item} for item in items]
        return {"neoforge:conditions": conditions} if conditions else {}

    def recipe_result(self, item, count, components):
        result = {"id": item}
        if components:
            result["components"] = components
        if count > 1:
            result["count"] = count
        return result

    def smelting_result(self, item, count):
        return self.recipe_result(item, count, None)

    def recipe_category(self, content, category):
        content["category"] = category

    def loot_table(self, table):
        return table

    def loot_modifier(self, name, target, table):
        return {
            "type": "neoforge:add_table",
            "condition": {"type": "neoforge:loot_table_id", "loot_table_id": target},
            "table": table,
        }

    def spawn_modifier(self, biomes, entity, weight, min_count=1, max_count=1):
        count = min_count if min_count == max_count else {
            "type": "minecraft:uniform",
            "min_inclusive": min_count,
            "max_inclusive": max_count,
        }
        return {
            "type": "neoforge:add_spawns",
            "biomes": biomes,
            "spawners": {"type": entity, "count": count, "weight": weight},
        }

    def feature_modifier(self, biomes, feature, step):
        return {"type": "neoforge:add_features", "biomes": biomes, "features": feature, "step": step}

    def configured_feature(self, content):
        return content

    def model_element(self, element, name, notes):
        return element

    def item_model_display(self):
        return None


PROFILES = {"26.3": Profile()}

profile = PROFILES["26.3"]
out_root = DEFAULT_OUT
out_overridden = False


def gradle_version():
    for line in (ROOT / "gradle.properties").read_text(encoding="utf-8").splitlines():
        match = re.match(r"minecraft_version=(.+)", line)
        if match:
            return match.group(1).strip()
    return "26.3"


def add_arguments(parser):
    parser.add_argument("--mc", help="minecraft version profile (default: gradle.properties)")
    parser.add_argument("--out", help="output root (default: src/generated/resources)")


def init(argv=None, parser=None):
    global profile, out_root, out_overridden
    parser = parser or argparse.ArgumentParser()
    add_arguments(parser)
    args, _ = parser.parse_known_args(argv)
    version = args.mc or gradle_version()
    if version not in PROFILES:
        raise SystemExit(f"unknown minecraft version {version}, expected one of {', '.join(PROFILES)}")
    profile = PROFILES[version]
    if args.out:
        out_root = Path(args.out).resolve()
        out_overridden = True
    return profile
