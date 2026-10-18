import argparse
import copy
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


CHEST_DISPLAY = {
    "gui": {"rotation": [30, 45, 0], "translation": [0, 0, 0], "scale": [0.625, 0.625, 0.625]},
    "ground": {"rotation": [0, 0, 0], "translation": [0, 3, 0], "scale": [0.25, 0.25, 0.25]},
    "head": {"rotation": [0, 180, 0], "translation": [0, 0, 0], "scale": [1, 1, 1]},
    "fixed": {"rotation": [0, 180, 0], "translation": [0, 0, 0], "scale": [0.5, 0.5, 0.5]},
    "thirdperson_righthand": {"rotation": [75, 315, 0], "translation": [0, 2.5, 0], "scale": [0.375, 0.375, 0.375]},
    "firstperson_righthand": {"rotation": [0, 315, 0], "translation": [0, 0, 0], "scale": [0.4, 0.4, 0.4]},
}

ALLOWED_ELEMENT_ANGLES = (0, 22.5, 45)


class Legacy(Profile):
    modern = False
    block_state_keys = ("Name", "Properties")
    explicit_render_types = True
    feature_folder = "configured_feature"

    def recipe_ingredient(self, value):
        if value.startswith("#"):
            return {"tag": value[1:]}
        return {"item": value}

    def recipe_conditions(self, items):
        conditions = [{"type": "neoforge:item_exists", "item": item} for item in items]
        return {"neoforge:conditions": conditions} if conditions else {}

    def recipe_category(self, content, category):
        content["category"] = category

    def configured_feature(self, content):
        return {**content, "config": {}}

    def _entry(self, entry):
        result = {}
        for key, value in entry.items():
            if key == "condition":
                result["conditions"] = self._condition_list(value)
            elif key == "modifier":
                result["functions"] = self._function_list(value)
            elif key == "children":
                result[key] = [self._entry(child) for child in value]
            elif key == "value" and entry.get("type") == "minecraft:loot_table":
                result[self.loot_table_reference_key] = value
            else:
                result[key] = value
        return result

    def _condition_list(self, value):
        values = value if isinstance(value, list) else [value]
        result = []
        for condition in values:
            result.extend(self._condition(condition))
        return result

    def _function_list(self, value):
        values = value if isinstance(value, list) else [value]
        return [self._function(function) for function in values]

    def _pool(self, pool):
        result = {}
        for key, value in pool.items():
            if key == "condition":
                result["conditions"] = self._condition_list(value)
            elif key == "modifier":
                result["functions"] = self._function_list(value)
            elif key == "entries":
                result[key] = [self._entry(entry) for entry in value]
            else:
                result[key] = value
        return result

    def loot_table(self, table):
        result = {}
        for key, value in table.items():
            if key == "pools":
                result[key] = [self._pool(pool) for pool in value]
            elif key == "random_sequence":
                if self.random_sequence:
                    result[key] = value
            else:
                result[key] = value
        return result

    def _common_condition(self, condition):
        kind = condition["type"]
        rest = {key: value for key, value in condition.items() if key != "type"}
        if kind == "minecraft:match_block":
            return [{"condition": "minecraft:block_state_property", "block": rest["blocks"], "properties": rest["state"]}]
        if kind.startswith("neoforge:"):
            return [{"condition": self.loot_condition_namespace + kind[len("neoforge:"):], **rest}]
        return None

    def loot_modifier(self, name, target, table):
        return {
            "type": self.loot_modifier_type,
            "conditions": [{"condition": self.loot_condition_namespace + "loot_table_id", "loot_table_id": target}],
            "table": table,
        }

    def item_model_display(self):
        return CHEST_DISPLAY

    def model_element(self, element, name, notes):
        element = copy.deepcopy(element)
        if "rotation" in element:
            angle = element["rotation"]["angle"]
            if abs(angle) not in ALLOWED_ELEMENT_ANGLES:
                snapped = min(ALLOWED_ELEMENT_ANGLES[1:], key=lambda allowed: abs(allowed - abs(angle)))
                element["rotation"]["angle"] = snapped if angle > 0 else -snapped
                notes.append({"model": name, "note": f"element rotation {angle} is not allowed by {self.name} (only 0, 22.5, 45), snapped to {element['rotation']['angle']}"})
        return element


class Profile1211(Legacy):
    name = "1.21.1"
    data_version = 3955
    random_sequence = True
    pack_mcmeta = {"pack": {"description": "Thaumic Tinkerer Reforged resources", "pack_format": 34}}
    vanilla_features = {"minecraft:grass": "minecraft:single_piece_of_grass"}
    loot_table_reference_key = "value"
    loot_condition_namespace = "neoforge:"
    loot_modifier_type = "neoforge:add_table"
    dropped_vanilla_ids = {"minecraft:copper_nugget"}

    def model_element(self, element, name, notes):
        element = super().model_element(element, name, notes)
        if "light_emission" in element:
            light = element.pop("light_emission")
            element["neoforge_data"] = {"block_light": light, "sky_light": light}
        return element

    def spawn_modifier(self, biomes, entity, weight, min_count=1, max_count=1):
        return {
            "type": "neoforge:add_spawns",
            "biomes": biomes,
            "spawners": {"type": entity, "weight": weight, "minCount": min_count, "maxCount": max_count},
        }

    def _condition(self, condition):
        if isinstance(condition, str):
            if condition == "minecraft:tool/can_shear":
                return [{"condition": "minecraft:match_tool", "predicate": {"items": "minecraft:shears"}}]
            if condition == "minecraft:tool/can_silk_touch":
                return [{"condition": "minecraft:match_tool", "predicate": {"predicates": {"minecraft:enchantments": [{"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}]
            raise ValueError(condition)
        common = self._common_condition(condition)
        if common is not None:
            return common
        kind = condition["type"]
        rest = {key: value for key, value in condition.items() if key != "type"}
        if kind in ("minecraft:any_of", "minecraft:all_of"):
            terms = []
            for term in rest["terms"]:
                terms.extend(self._condition(term))
            return [{"condition": kind, "terms": terms}]
        return [{"condition": kind, **rest}]

    def _with_conditions(self, rest):
        return {("conditions" if key == "condition" else key): (self._condition_list(value) if key == "condition" else value) for key, value in rest.items()}

    def _function(self, function):
        return {"function": function["type"], **self._with_conditions({key: value for key, value in function.items() if key != "type"})}



PROFILES = {"26.3": Profile(), "1.21.1": Profile1211()}

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
