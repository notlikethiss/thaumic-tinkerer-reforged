import argparse
import json
import re
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

import ttjava
from extract_tt_research import Evaluator

ROOT = Path(__file__).resolve().parent.parent
COMMON = ROOT / "reference" / "tt-src" / "src" / "main" / "java" / "thaumic" / "tinkerer" / "common"
OUTPUT = ROOT / "tools" / "tt_recipes.json"
REFS = json.loads((ROOT / "tools" / "tc4_refs.json").read_text(encoding="utf-8"))
ID_MAP = json.loads((ROOT / "tools" / "id_map.json").read_text(encoding="utf-8"))
RECIPE_CLASSES = {
    "ThaumicTinkererArcaneRecipe": "arcane",
    "ThaumicTinkererCraftingBenchRecipe": "bench",
    "ThaumicTinkererCrucibleRecipe": "crucible",
    "ThaumicTinkererInfusionRecipe": "infusion",
}
MOB_ASPECTS = ["WATER", "MAN", "AIR", "FLIGHT", "FIRE", "MAGIC", "UNDEAD", "FLESH", "BEAST", "POISON", "EARTH", "ELDRITCH", "TRAVEL", "METAL", "SLIME", "CLOTH", "CROP"]
WILDCARD = 32767


def load_names():
    items = ttjava.load_constants(COMMON / "lib" / "LibItemNames.java")
    blocks = ttjava.load_constants(COMMON / "lib" / "LibBlockNames.java")
    classes = {}
    for path in COMMON.rglob("*.java"):
        text = ttjava.strip_comments(path.read_text(encoding="utf-8", errors="replace"))
        match = re.search(r"(?:getItemName|getBlockName)\(\)\s*\{\s*return\s+(\w+)\.(\w+)\s*;", text)
        if not match:
            continue
        table = items if match.group(1) == "LibItemNames" else blocks
        if match.group(2) in table:
            classes[path.stem] = table[match.group(2)]
    return classes


CLASS_NAMES = load_names()


def mapped_ids(name, meta):
    result = []
    for kind in ("items", "blocks"):
        rows = [row for row in ID_MAP[kind] if row["old"] == name]
        if not rows:
            continue
        exact = [row for row in rows if row["meta"] == meta]
        chosen = exact or [row for row in rows if row["meta"] is None]
        if meta == WILDCARD:
            chosen = rows
        for row in chosen:
            if row["skip"] is None and row["id"]:
                result.append("thaumictinkerer:" + row["id"])
    return result


def table_ids(table, reference, meta):
    entry = table.get(reference)
    if entry is None:
        return None
    if meta != WILDCARD and str(meta) in entry:
        value = entry[str(meta)]
    elif "*" in entry:
        value = entry["*"]
    else:
        return None
    values = value if isinstance(value, list) else [value]
    return [re.sub(r"\[.*\]$", "", item) for item in values]


CLASS_OVERRIDES = {"BlockTravelSlab": "travelSlab", "BlockWardSlab": "wardSlab", "BlockDarkQuartzSlab": "darkQuartzSlab"}
KEY_OVERRIDES = {
    "GASEOUS_LIGHT": "gaseousLightItem",
    "GASEOUS_SHADOW": "gaseousShadowItem",
    "ICHORCLOTH_HELM": "ichorclothHelm",
    "ICHORCLOTH_CHEST": "ichorclothChest",
    "ICHORCLOTH_LEGS": "ichorclothLegs",
    "ICHORCLOTH_BOOTS": "ichorclothBoots",
}
ITEM_NAME_CONSTANTS = ttjava.load_constants(COMMON / "lib" / "LibItemNames.java")


class Context:
    def __init__(self, owner):
        self.owner = owner
        self.key = ""


def class_name(cls):
    return CLASS_OVERRIDES.get(cls) or CLASS_NAMES.get(cls)


def item_options(node, context):
    kind = node["t"]
    if kind == "str":
        mapped = REFS["oredict"].get(node["v"])
        return [mapped] if mapped else ["?oredict " + node["v"]], 1, 0
    if kind == "field":
        return reference_options(node["raw"], 0)
    if kind == "call" and node["name"] in ("getFirstItemFromClass", "getFirstBlockFromClass"):
        cls = node["args"][0]["target"]["v"]
        name = class_name(cls)
        ids = mapped_ids(name, 0) if name else []
        return (ids or ["?class " + cls]), 1, 0
    if kind == "call" and node["name"] == "oreDictOrStack":
        first, _, meta = item_options(node["args"][0], context)
        ore = REFS["oredict"].get(node["args"][1]["v"])
        return first + ([ore] if ore else []), 1, meta
    if kind == "call" and node["name"] == "getStackFromAspect":
        return ["thaumictinkerer:infused_seeds"], 1, 0
    if kind == "new" and node["cls"] == "ItemStack" and node["args"][0]["t"] == "call" and node["args"][0]["name"] == "getItemFromClassAndName":
        constant = node["args"][0]["args"][1]["name"]
        ids = mapped_ids(ITEM_NAME_CONSTANTS[constant], 0)
        return (ids or ["?" + node["raw"]]), 1, 0
    if kind == "new" and node["cls"] == "ItemStack" and node["args"][0]["t"] == "call" and node["args"][0]["name"] == "get":
        inner = node["args"][0]
        index = inner["args"][0]["v"]
        return ["thaumictinkerer:" + ("gaseous_tenebrae", "gaseous_illuminae")[index]], 1, 0
    if kind == "new" and node["cls"] == "ItemStack":
        args = node["args"]
        count = 1
        meta = 0
        if len(args) > 1:
            count = args[1]["v"] if args[1]["t"] == "num" else None
        if len(args) > 2:
            meta = args[2]["v"] if args[2]["t"] == "num" else None
        if count is None or meta is None:
            return ["?" + node["raw"]], 1, 0
        first = args[0]
        if first["t"] == "name" and first["v"] == "this":
            name = KEY_OVERRIDES.get(context.key) or class_name(context.owner)
            ids = mapped_ids(name, meta) if name else []
            return (ids or ["?this " + context.owner]), count, meta
        if first["t"] == "call" and first["name"] in ("getFirstItemFromClass", "getFirstBlockFromClass"):
            cls = first["args"][0]["target"]["v"]
            name = class_name(cls)
            ids = mapped_ids(name, meta) if name else []
            return (ids or ["?class " + cls]), count, meta
        if first["t"] == "call" and first["name"] == "getFirstItemFromClass":
            pass
        if first["t"] == "field":
            options, _, _ = reference_options(first["raw"], meta)
            return options, count, meta
        return ["?" + node["raw"]], count, meta
    return ["?" + node["raw"]], 1, 0


def reference_options(raw, meta):
    reference = raw.replace(" ", "")
    if reference.startswith(("ConfigItems.", "ConfigBlocks.")):
        ids = table_ids(REFS["thaumcraft"], reference, meta)
    else:
        ids = table_ids(REFS["vanilla"], reference, meta)
    return (ids or ["?" + reference + " " + str(meta)]), 1, meta


def format_stack(options, count, meta):
    text = "|".join(options)
    return {"ids": options, "count": count, "meta": meta, "text": text}


def build_ingredient(node, context):
    options, count, meta = item_options(node, context)
    return format_stack(options, count, meta)


def aspects_dict(evaluator, node):
    result = {}
    for name, amount in evaluator.aspects(node):
        result[name] = result.get(name, 0) + amount
    return dict(sorted(result.items()))


def extract_arcane(args, evaluator, context):
    key = evaluator.string(args[0])
    context.key = key
    research = evaluator.string(args[1])
    result = build_ingredient(args[2], context)
    aspects = aspects_dict(evaluator, args[3])
    stuff = args[4:]
    rows = []
    index = 0
    while index < len(stuff) and stuff[index]["t"] == "str":
        rows.append(stuff[index]["v"])
        index += 1
    mapping = {}
    while index + 1 < len(stuff):
        mapping[stuff[index]["v"]] = build_ingredient(stuff[index + 1], context)
        index += 2
    width = max(len(row) for row in rows)
    grid = []
    for row in rows:
        for column in range(width):
            symbol = row[column] if column < len(row) else " "
            grid.append(None if symbol == " " else mapping[symbol])
    return {"key": key, "type": "arcane", "research": research, "result": result, "aspects": aspects, "width": width, "height": len(rows), "grid": grid}


def extract_bench(args, evaluator, context):
    key = evaluator.string(args[0])
    context.key = key
    result = build_ingredient(args[1], context)
    stuff = args[2:]
    rows = []
    index = 0
    while index < len(stuff) and stuff[index]["t"] == "str":
        rows.append(stuff[index]["v"])
        index += 1
    mapping = {}
    while index + 1 < len(stuff):
        mapping[stuff[index]["v"]] = build_ingredient(stuff[index + 1], context)
        index += 2
    return {"key": key, "type": "bench", "result": result, "rows": rows, "mapping": mapping}


def extract_crucible(args, evaluator, context):
    context.key = evaluator.string(args[0])
    return {
        "key": evaluator.string(args[0]),
        "type": "crucible",
        "research": evaluator.string(args[0]),
        "result": build_ingredient(args[1], context),
        "catalyst": build_ingredient(args[2], context),
        "aspects": aspects_dict(evaluator, args[3]),
    }


def flatten_stuff(nodes, context):
    result = []
    for node in nodes:
        if node["t"] == "array":
            result.extend(build_ingredient(item, context) for item in node["items"])
        else:
            result.append(build_ingredient(node, context))
    return result


def extract_infusion(args, evaluator, context):
    key = evaluator.string(args[0])
    context.key = key
    offset = 1
    research = key
    try:
        research = evaluator.string(args[1])
        offset = 2
    except ValueError:
        pass
    result = build_ingredient(args[offset], context)
    instability = evaluator.integer(args[offset + 1])
    aspects = aspects_dict(evaluator, args[offset + 2])
    central = build_ingredient(args[offset + 3], context)
    components = flatten_stuff(args[offset + 4:], context)
    return {"key": key, "type": "infusion", "research": research, "result": result, "instability": instability, "aspects": aspects, "central": central, "components": components}


def mob_recipes():
    recipes = []
    for index, aspect in enumerate(MOB_ASPECTS):
        base = format_stack(mapped_ids("mobAspect", index), 1, index)
        condensed = format_stack(mapped_ids("mobAspect", index + 21), 1, index + 21)
        infused = format_stack(mapped_ids("mobAspect", index + 42), 1, index + 42)
        recipes.append({"key": "SUMMON1", "variant": aspect, "type": "bench", "result": condensed, "rows": ["XXX", "XXX", "XXX"], "mapping": {"X": base}})
        recipes.append({
            "key": "SUMMON",
            "variant": aspect,
            "type": "infusion",
            "research": "SUMMON",
            "result": infused,
            "instability": 4,
            "aspects": {aspect: 10},
            "central": condensed,
            "components": [condensed] * 8,
        })
    return recipes


def collect():
    constants = ttjava.load_constants(COMMON / "lib" / "LibResearch.java")
    evaluator = Evaluator(constants)
    recipes = []
    for path in sorted(COMMON.rglob("*.java")):
        if path.suffix != ".java" or path.parent.name == "registry" and path.name.startswith("ThaumicTinkerer"):
            continue
        text = ttjava.strip_comments(path.read_text(encoding="utf-8", errors="replace"))
        if "Recipe(" not in text and "addShapelessRecipe" not in text:
            continue
        context = Context(path.stem)
        for node in ttjava.find_new(text, set(RECIPE_CLASSES)):
            if node["t"] == "error":
                raise ValueError(str(path) + ": " + node["error"])
            if path.stem == "ItemMobAspect":
                continue
            kind = RECIPE_CLASSES[node["cls"]]
            builder = {"arcane": extract_arcane, "bench": extract_bench, "crucible": extract_crucible, "infusion": extract_infusion}[kind]
            recipe = builder(node["args"], evaluator, context)
            recipe["source"] = str(path.relative_to(COMMON))
            recipes.append(recipe)
        if path.stem == "ItemKamiResource":
            recipes.append({
                "key": "",
                "type": "bench",
                "result": format_stack(mapped_ids("kamiResource", 3), 9, 3),
                "rows": [],
                "mapping": {},
                "shapeless": [format_stack(mapped_ids("kamiResource", 2), 1, 2)],
                "source": "item/kami/ItemKamiResource.java",
            })
    recipes.extend(mob_recipes())
    return recipes


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--compare", help="runtime recipe dump to compare with")
    arguments = parser.parse_args()
    recipes = collect()
    OUTPUT.write_text(json.dumps(recipes, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    print("recipes:", len(recipes))
    unresolved = [recipe["key"] for recipe in recipes if "?" in json.dumps(recipe)]
    print("unresolved:", unresolved)
    if arguments.compare:
        import compare_recipes
        result = compare_recipes.run(recipes, Path(arguments.compare))
        if result["problems"]:
            raise SystemExit(1)


if __name__ == "__main__":
    main()
