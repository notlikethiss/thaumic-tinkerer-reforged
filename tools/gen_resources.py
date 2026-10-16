import importlib
import json
import shutil
import sys
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(Path(__file__).resolve().parent))
import mcformat

profile = mcformat.init()
OUT = mcformat.out_root
ASSETS = OUT / "assets" / "thaumictinkerer"
DATA = OUT / "data"
NS = "thaumictinkerer"
SOURCE_TEXTURES = ROOT / "src" / "main" / "resources" / "assets" / NS / "textures"

FEATURES = ["core"]

ASPECT_TINT = {"type": "thaumcraft:aspect"}

block_tags = defaultdict(set)
item_tags = defaultdict(set)
BLOCK_MODELS = {}
TEXTURE_REFERENCES = set()


def write(path, content):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(content, indent=2) + "\n", encoding="utf-8")


def ref(name, kind="block"):
    return f"{NS}:{kind}/{name}"


def tint(color):
    value = color | 0xFF000000
    if value >= 0x80000000:
        value -= 0x100000000
    return {"type": "minecraft:constant", "value": value}


def block_model(name, content):
    BLOCK_MODELS[name] = content
    write(ASSETS / "models" / "block" / f"{name}.json", content)


def item_model(name, content):
    write(ASSETS / "models" / "item" / f"{name}.json", content)


def item_definition(name, model, tints=None):
    definition = {"type": "minecraft:model", "model": model}
    if tints:
        definition["tints"] = tints
    write(ASSETS / "items" / f"{name}.json", {"model": definition})


def blockstate(name, content):
    write(ASSETS / "blockstates" / f"{name}.json", content)


def simple_state(name, model=None):
    blockstate(name, {"variants": {"": {"model": model or ref(name)}}})


def cube_all(name, texture):
    block_model(name, {"parent": "minecraft:block/cube_all", "textures": {"all": ref(texture)}})


def cube_column(name, end, side):
    block_model(name, {"parent": "minecraft:block/cube_column", "textures": {"end": ref(end), "side": ref(side)}})


def generated_item(name, layers, tints=None, texture_kind="item"):
    if isinstance(layers, str):
        layers = [layers]
    item_model(name, {
        "parent": "minecraft:item/generated",
        "textures": {f"layer{index}": ref(layer, texture_kind) for index, layer in enumerate(layers)},
    })
    item_definition(name, ref(name, "item"), tints)


def block_item(name, tints=None, model=None):
    item_definition(name, model or ref(name), tints)


def write_loot(name, pools):
    write(DATA / NS / profile.loot_folder / "blocks" / f"{name}.json", profile.loot_table({
        "type": "minecraft:block",
        "pools": pools,
        "random_sequence": f"{NS}:blocks/{name}",
    }))


def self_drop(name):
    write_loot(name, [{
        "rolls": 1,
        "entries": [{"type": "minecraft:item", "name": f"{NS}:{name}"}],
        "condition": {"type": "minecraft:survives_explosion"},
    }])


def slab_drop(name):
    write_loot(name, [{
        "rolls": 1,
        "entries": [{
            "type": "minecraft:item",
            "name": f"{NS}:{name}",
            "modifier": [
                {
                    "type": "minecraft:set_count",
                    "count": 2,
                    "add": False,
                    "condition": {"type": "minecraft:match_block", "blocks": f"{NS}:{name}", "state": {"type": "double"}},
                },
                {"type": "minecraft:explosion_decay"},
            ],
        }],
    }])


def bonus_drop(block, item, base_max=1):
    modifiers = []
    if base_max > 1:
        modifiers.append({"type": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": base_max}})
    modifiers.append({
        "type": "minecraft:apply_bonus",
        "enchantment": "minecraft:fortune",
        "formula": "minecraft:uniform_bonus_count",
        "parameters": {"bonusMultiplier": 1},
    })
    modifiers.append({"type": "minecraft:explosion_decay"})
    write_loot(block, [{
        "rolls": 1,
        "entries": [{
            "type": "minecraft:alternatives",
            "children": [
                {"type": "minecraft:item", "condition": "minecraft:tool/can_silk_touch", "name": f"{NS}:{block}"},
                {"type": "minecraft:item", "name": item, "modifier": modifiers},
            ],
        }],
    }])


def shear_only(name):
    write_loot(name, [{
        "rolls": 1,
        "entries": [{
            "type": "minecraft:item",
            "condition": {"type": "minecraft:any_of", "terms": ["minecraft:tool/can_shear", "minecraft:tool/can_silk_touch"]},
            "name": f"{NS}:{name}",
        }],
    }])


def mineable(tool, *blocks):
    for name in blocks:
        block_tags[f"minecraft:mineable/{tool}"].add(f"{NS}:{name}")


def tag_both(tag, *names):
    for name in names:
        block_tags[tag].add(f"{NS}:{name}")
        item_tags[tag].add(f"{NS}:{name}")


def stairs_state(name):
    variants = {}
    table = {
        "straight": ("stairs", {"east": 0, "west": 180, "south": 90, "north": 270}, {"east": 0, "west": 180, "south": 90, "north": 270}),
        "outer_right": ("stairs_outer", {"east": 0, "west": 180, "south": 90, "north": 270}, {"east": 90, "west": 270, "south": 180, "north": 0}),
        "outer_left": ("stairs_outer", {"east": 270, "west": 90, "south": 0, "north": 180}, {"east": 0, "west": 180, "south": 90, "north": 270}),
        "inner_right": ("stairs_inner", {"east": 0, "west": 180, "south": 90, "north": 270}, {"east": 90, "west": 270, "south": 180, "north": 0}),
        "inner_left": ("stairs_inner", {"east": 270, "west": 90, "south": 0, "north": 180}, {"east": 0, "west": 180, "south": 90, "north": 270}),
    }
    suffix = {"stairs": "", "stairs_outer": "_outer", "stairs_inner": "_inner"}
    for shape, (kind, bottom, top) in table.items():
        for facing in ["east", "west", "south", "north"]:
            for half, rotations in (("bottom", bottom), ("top", top)):
                variant = {"model": ref(name + suffix[kind]), "y": rotations[facing]}
                if half == "top":
                    variant["x"] = 180
                if variant["y"] or half == "top":
                    variant["uvlock"] = True
                if not variant["y"]:
                    del variant["y"]
                variants[f"facing={facing},half={half},shape={shape}"] = variant
    blockstate(name, {"variants": variants})


def stairs(name, texture):
    textures = {"bottom": ref(texture), "top": ref(texture), "side": ref(texture)}
    for suffix, parent in (("", "stairs"), ("_inner", "inner_stairs"), ("_outer", "outer_stairs")):
        block_model(name + suffix, {"parent": f"minecraft:block/{parent}", "textures": textures})
    stairs_state(name)
    block_item(name)
    self_drop(name)


def slab(name, texture, double_texture=None):
    textures = {"bottom": ref(texture), "top": ref(texture), "side": ref(texture)}
    block_model(name, {"parent": "minecraft:block/slab", "textures": textures})
    block_model(f"{name}_top", {"parent": "minecraft:block/slab_top", "textures": textures})
    block_model(f"{name}_double", {"parent": "minecraft:block/cube_all", "textures": {"all": ref(texture)}})
    blockstate(name, {"variants": {
        "type=bottom": {"model": ref(name)},
        "type=top": {"model": ref(f"{name}_top")},
        "type=double": {"model": ref(f"{name}_double")},
    }})
    block_item(name)
    slab_drop(name)


def recipe(name, content, referenced, extra_conditions=None):
    conditions = profile.recipe_conditions(sorted(value for value in referenced if isinstance(value, str) and value.startswith(f"{NS}:")))
    if extra_conditions:
        existing = conditions.get("neoforge:conditions", [])
        conditions = {"neoforge:conditions": existing + extra_conditions}
    if conditions:
        content = {**conditions, **content}
    write(DATA / NS / profile.recipe_folder / f"{name}.json", content)


def shaped(name, result, count, pattern, key, category="misc", conditions=None, components=None):
    content = {"type": "minecraft:crafting_shaped"}
    profile.recipe_category(content, category)
    content["key"] = {symbol: profile.recipe_ingredient(ingredient) for symbol, ingredient in key.items()}
    content["pattern"] = pattern
    content["result"] = profile.recipe_result(result, count, components)
    recipe(name, content, [result, *key.values()], conditions)


def shapeless(name, result, count, ingredients, category="misc", conditions=None):
    content = {"type": "minecraft:crafting_shapeless"}
    profile.recipe_category(content, category)
    content["ingredients"] = [profile.recipe_ingredient(ingredient) for ingredient in ingredients]
    content["result"] = profile.recipe_result(result, count, None)
    recipe(name, content, [result, *ingredients], conditions)


def smelting(name, ingredient, result, count, experience):
    content = {
        "type": "minecraft:smelting",
        "cookingtime": 200,
        "experience": experience,
        "ingredient": profile.recipe_ingredient(ingredient),
        "result": profile.smelting_result(result, count),
    }
    recipe(name, content, [ingredient, result])


def not_tag_empty(tag):
    return [{"type": "neoforge:not", "value": {"type": "neoforge:tag_empty", "tag": tag}}]


def tag_entries(values):
    return [{"id": value, "required": False} if value.startswith(f"{NS}:") else value for value in sorted(values)]


def write_tags():
    for tag, values in block_tags.items():
        namespace, path = profile.tag_name(tag)
        write(DATA / namespace / profile.block_tag_folder / f"{path}.json", {"replace": False, "values": tag_entries(values)})
    for tag, values in item_tags.items():
        namespace, path = profile.tag_name(tag)
        write(DATA / namespace / profile.item_tag_folder / f"{path}.json", {"replace": False, "values": tag_entries(values)})


def collect_textures(value):
    if isinstance(value, dict):
        for key, entry in value.items():
            if key == "textures" and isinstance(entry, dict):
                TEXTURE_REFERENCES.update(texture for texture in entry.values() if isinstance(texture, str) and not texture.startswith("#"))
            else:
                collect_textures(entry)
    elif isinstance(value, list):
        for entry in value:
            collect_textures(entry)


def validate_textures():
    for path in (ASSETS / "models").rglob("*.json"):
        collect_textures(json.loads(path.read_text(encoding="utf-8")))
    missing = []
    for texture in sorted(TEXTURE_REFERENCES):
        namespace, _, location = texture.partition(":")
        if namespace != NS:
            continue
        if not (SOURCE_TEXTURES / f"{location}.png").exists() and not (ASSETS / "textures" / f"{location}.png").exists():
            missing.append(texture)
    if missing:
        raise SystemExit("missing textures:\n  " + "\n  ".join(missing))


def item_definition_models(definition):
    if definition["type"] == "minecraft:empty":
        return
    if definition["type"] == "minecraft:select":
        for case in definition["cases"]:
            yield from item_definition_models(case["model"])
        if "fallback" in definition:
            yield from item_definition_models(definition["fallback"])
    elif definition["type"] in ("minecraft:special", f"{NS}:special_owner"):
        yield definition["base"]
    elif definition["type"] == "minecraft:condition":
        yield from item_definition_models(definition["on_true"])
        yield from item_definition_models(definition["on_false"])
    elif definition["type"] == "minecraft:range_dispatch":
        for entry in definition["entries"]:
            yield from item_definition_models(entry["model"])
        if "fallback" in definition:
            yield from item_definition_models(definition["fallback"])
    elif definition["type"] == "minecraft:composite":
        for model in definition["models"]:
            yield from item_definition_models(model)
    else:
        yield definition["model"]


def blockstate_models(value):
    if isinstance(value, dict):
        model = value.get("model")
        if isinstance(model, str):
            yield model
        for key, entry in value.items():
            if key != "model":
                yield from blockstate_models(entry)
    elif isinstance(value, list):
        for entry in value:
            yield from blockstate_models(entry)


def validate_models():
    missing = []
    for path in (ASSETS / "items").glob("*.json"):
        for model in item_definition_models(json.loads(path.read_text(encoding="utf-8"))["model"]):
            namespace, _, location = model.partition(":")
            if namespace == NS and not (ASSETS / "models" / f"{location}.json").exists():
                missing.append(f"{path.name}: {model}")
    for path in (ASSETS / "blockstates").glob("*.json"):
        for model in blockstate_models(json.loads(path.read_text(encoding="utf-8"))):
            namespace, _, location = model.partition(":")
            if namespace == NS and not (ASSETS / "models" / f"{location}.json").exists():
                missing.append(f"{path.name}: {model}")
    if missing:
        raise SystemExit("missing models:\n  " + "\n  ".join(missing))


GALACTIC_LETTERS = {
    "А": "A", "Б": "B", "В": "V", "Г": "G", "Д": "D", "Е": "E", "Ё": "E", "Ж": "X", "З": "Z", "И": "I", "Й": "J",
    "К": "K", "Л": "L", "М": "M", "Н": "N", "О": "O", "П": "P", "Р": "R", "С": "S", "Т": "T", "У": "U", "Ф": "F",
    "Х": "H", "Ц": "C", "Ч": "C", "Ш": "W", "Щ": "W", "Ъ": "Q", "Ы": "Y", "Ь": "Q", "Э": "E", "Ю": "U", "Я": "Y",
}


def main():
    if OUT.exists():
        if mcformat.out_overridden and OUT == ROOT:
            raise SystemExit("refusing to clear the repository root")
        shutil.rmtree(OUT)
    OUT.mkdir(parents=True, exist_ok=True)
    ctx = sys.modules[__name__]
    for feature in FEATURES:
        importlib.import_module(f"gen_{feature}").generate(ctx)
    write_tags()
    validate_textures()
    validate_models()
    count = sum(1 for _ in OUT.rglob("*.json"))
    print(f"generated {count} files")


if __name__ == "__main__":
    main()
