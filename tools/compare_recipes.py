import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
RECIPE_DIR = ROOT / "src" / "generated" / "resources" / "data" / "thaumictinkerer" / "recipe"
MOB_TAGS = {}


def mob_tags():
    rows = json.loads((ROOT / "tools" / "id_map.json").read_text(encoding="utf-8"))["items"]
    for row in rows:
        if row["old"] == "mobAspect" and row["meta"] is not None and row["meta"] < 17:
            MOB_TAGS[row["meta"]] = row["id"].replace("_soul_aspect", "")


RESEARCH_FIXES = {
    "INFUSED_POTIONSPOT0": "INFUSED_POTIONS",
    "INFUSED_POTIONSPOT1": "INFUSED_POTIONS",
    "INFUSED_POTIONSPOT2": "INFUSED_POTIONS",
    "INFUSED_POTIONSPOT3": "INFUSED_POTIONS",
}


def split_top(text):
    parts = []
    depth = 0
    current = ""
    for char in text:
        if char in "[(":
            depth += 1
        elif char in "])":
            depth -= 1
        if char == "," and depth == 0:
            parts.append(current.strip())
            current = ""
        else:
            current += char
    if current.strip():
        parts.append(current.strip())
    return parts


def parse_runtime(text):
    if text == "-":
        return [], "", 0
    if text.startswith("#"):
        return [text], "", 1
    if text.startswith("any(["):
        options = []
        for part in split_top(text[5:-2]):
            options.extend(parse_runtime(part)[0])
        return options, "", 1
    match = re.match(r"^(\S+) x(\d+)(.*)$", text)
    return [match.group(1)], match.group(3).strip(), int(match.group(2))


def same(truth, runtime_text, soft, label, problems):
    options, extra, count = parse_runtime(runtime_text)
    wanted = truth["ids"] if truth else []
    if not truth:
        if options:
            problems.append(f"{label}: expected empty, got {runtime_text}")
        return
    if truth.get("count", 0) > 0 and count != truth["count"]:
        problems.append(f"{label}: count TT {truth['count']} vs runtime {count}")
    if truth["ids"] == ["minecraft:potion"] and truth.get("meta") in (8198, 8195):
        potion = {8198: "night_vision", 8195: "fire_resistance"}[truth["meta"]]
        if f"tt_potion=minecraft:{potion}" not in extra:
            problems.append(f"{label}: potion data should be {potion}, got {extra}")
    if set(options) == set(wanted):
        return
    if options and set(options) <= set(wanted):
        soft.append(f"{label}: runtime {runtime_text} is a subset of TT {wanted}")
        return
    problems.append(f"{label}: TT {wanted} vs runtime {runtime_text}")


def infusion_component(truth, runtime_text, soft, label, problems):
    if truth.get("count", 1) != 1:
        soft.append(f"{label}: TT declares x{truth['count']}; TC4 InfusionRecipe ignores ingredient stack size")
    same({**truth, "count": 1}, runtime_text, soft, label, problems)


def check_result(truth, runtime_text, label, problems, soft):
    options, extra, count = parse_runtime(runtime_text)
    if truth["count"] != count and truth["count"] != 0:
        problems.append(f"{label}: result count TT {truth['count']} vs runtime {count}")
    same(truth, runtime_text, soft, label + " result", problems)
    if extra:
        soft.append(f"{label}: runtime result extra {extra}")


def aspects_text(mapping):
    return str(dict(sorted(mapping.items())))


def runtime_aspects(text):
    return {key: int(value) for key, value in re.findall(r"(\w+)=(\d+)", text)}


def load_json_recipe(path_id):
    name = path_id.split(":", 1)[1]
    return json.loads((RECIPE_DIR / (name + ".json")).read_text(encoding="utf-8"))


def ingredient_id(value):
    if isinstance(value, dict):
        return value.get("item") or ("#" + value["tag"] if "tag" in value else str(value))
    if isinstance(value, list):
        return "|".join(ingredient_id(item) for item in value)
    return value


def compare_bench(truth, runtime_entries, problems, soft, json_index):
    label = "bench " + truth["key"] + " -> " + truth["result"]["text"]
    found = None
    first = None
    for entry in runtime_entries:
        if entry["type"] == "CraftingRecipeRef" and parse_runtime(entry["result"])[0] == truth["result"]["ids"]:
            loaded = load_json_recipe(entry["recipe"])
            first = first or loaded
            if candidate_matches(truth, loaded):
                found = loaded
                label += " [" + entry["key"] + "]"
                break
    if found is None and first is not None:
        found = first
    if found is None:
        for candidate in json_index.get(truth["result"]["ids"][0], []):
            if candidate_matches(truth, candidate):
                found = candidate
                break
    if found is None:
        problems.append(label + ": no addon recipe found")
        return
    if not candidate_matches(truth, found):
        problems.append(label + ": addon recipe differs: " + json.dumps(found.get("pattern") or found.get("ingredients")) + " " + json.dumps(found.get("key")))


def candidate_matches(truth, found):
    result = found.get("result", {})
    if result.get("id") != truth["result"]["ids"][0] or result.get("count", 1) != truth["result"]["count"]:
        return False
    if "pattern" in found:
        if "shapeless" in truth:
            return False
        mirrored_rows = [row[::-1] for row in truth["rows"]]
        def normalize(rows, mapping):
            return [[mapping[char]["text"] if char != " " else None for char in row] for row in rows]
        wanted = normalize(truth["rows"], truth["mapping"])
        wanted_mirror = normalize(mirrored_rows, truth["mapping"])
        key = {symbol: {"text": ingredient_id(value)} for symbol, value in found["key"].items()}
        actual = normalize(found["pattern"], key)
        def flat(rows):
            return [[cell if cell is None else cell.split("|")[0] for cell in row] for row in rows]
        def loose(rows):
            return [[cell if cell is None else cell.replace("#c:", "").split("|")[0] for cell in row] for row in rows]
        return loose(actual) in (loose(wanted), loose(wanted_mirror)) or related(actual, wanted)
    if "ingredients" in found:
        wanted = [item["text"].split("|")[0] for item in truth.get("shapeless", [])]
        if not wanted:
            wanted = []
            for row in truth["rows"]:
                for char in row:
                    if char != " ":
                        wanted.append(truth["mapping"][char]["text"].split("|")[0])
        actual = [ingredient_id(item).split("|")[0] for item in found["ingredients"]]
        return sorted(wanted) == sorted(actual)
    return False


def related(actual, wanted):
    flat_actual = [cell for row in actual for cell in row]
    flat_wanted = [cell for row in wanted for cell in row]
    if len(flat_actual) != len(flat_wanted):
        return False
    for left, right in zip(flat_actual, flat_wanted):
        if left is None or right is None:
            if left != right:
                return False
            continue
        if left.split("|")[0] not in right.split("|") and right.split("|")[0] not in left.split("|"):
            return False
    return True


def run(recipes, dump_path):
    mob_tags()
    dump = json.loads(dump_path.read_text(encoding="utf-8"))
    by_key = {}
    for entry in dump:
        by_key.setdefault(entry["key"], []).append(entry)
    json_index = {}
    for path in RECIPE_DIR.glob("*.json"):
        data = json.loads(path.read_text(encoding="utf-8"))
        result = data.get("result", {}).get("id") if isinstance(data.get("result"), dict) else None
        if result:
            json_index.setdefault(result, []).append(data)
    problems = []
    soft = []
    used = set()
    skipped = {"ASPECT_ANALYZER", "GOLEM_CONNECTOR", "REMOTE_PLACER"}
    for truth in recipes:
        key = truth["key"]
        if key in skipped:
            continue
        if truth["type"] == "bench":
            candidates = [entry for name, entries in by_key.items() if name == key or name.startswith(key + "_") for entry in entries] if key else []
            if key == "SUMMON1":
                tag = MOB_TAGS[next(i for i, v in enumerate(["WATER", "MAN", "AIR", "FLIGHT", "FIRE", "MAGIC", "UNDEAD", "FLESH", "BEAST", "POISON", "EARTH", "ELDRITCH", "TRAVEL", "METAL", "SLIME", "CLOTH", "CROP"]) if v == truth["variant"])]
                candidates = by_key.get("SUMMON1_" + tag.upper(), [])
            for entry in candidates:
                used.add(entry["key"])
            compare_bench(truth, candidates, problems, soft, json_index)
            continue
        if truth["type"] == "infusion" and key == "SUMMON":
            tag = MOB_TAGS[["WATER", "MAN", "AIR", "FLIGHT", "FIRE", "MAGIC", "UNDEAD", "FLESH", "BEAST", "POISON", "EARTH", "ELDRITCH", "TRAVEL", "METAL", "SLIME", "CLOTH", "CROP"].index(truth["variant"])]
            key = "SUMMON_INFUSED_" + tag.upper()
        entries = by_key.get(key)
        if not entries:
            problems.append(f"{truth['type']} {key}: missing in addon")
            continue
        entry = entries[-1]
        used.add(key)
        label = f"{truth['type']} {key}"
        if truth["type"] == "arcane":
            if entry["type"] not in ("ShapedArcaneRecipe", "ShapelessArcaneRecipe"):
                problems.append(f"{label}: addon type {entry['type']}")
                continue
            if entry["research"] != truth["research"]:
                problems.append(f"{label}: research TT {truth['research']} vs {entry['research']}")
            check_result(truth["result"], entry["result"], label, problems, soft)
            if key in {f"INFUSED_POTIONS{index}" for index in range(6)}:
                aspect = ["aer", "ignis", "terra", "aqua", "ordo", "perditio"][int(key[-1])]
                if f"seed_aspect={aspect}" not in entry["result"]:
                    problems.append(f"{label}: wrong seed aspect, expected {aspect}")
            if runtime_aspects(entry["aspects"]) != truth["aspects"]:
                problems.append(f"{label}: aspects TT {truth['aspects']} vs {entry['aspects']}")
            if entry.get("width") != truth["width"] or entry.get("height") != truth["height"]:
                problems.append(f"{label}: size TT {truth['width']}x{truth['height']} vs {entry.get('width')}x{entry.get('height')}")
            for index, (cell, text) in enumerate(zip(truth["grid"], entry["grid"])):
                same(cell, text, soft, f"{label} cell {index}", problems)
            if len(truth["grid"]) != len(entry["grid"]):
                problems.append(f"{label}: grid size differs")
        elif truth["type"] == "infusion":
            if entry["type"] != "InfusionRecipe":
                problems.append(f"{label}: addon type {entry['type']}")
                continue
            if entry["research"] != truth["research"]:
                problems.append(f"{label}: research TT {truth['research']} vs {entry['research']}")
            check_result(truth["result"], entry["result"], label, problems, soft)
            if key in {f"INFUSED_POTIONS{index}" for index in range(6)}:
                aspect = ["aer", "ignis", "terra", "aqua", "ordo", "perditio"][int(key[-1])]
                if f"seed_aspect={aspect}" not in entry["result"]:
                    problems.append(f"{label}: wrong seed aspect, expected {aspect}")
            if runtime_aspects(entry["aspects"]) != truth["aspects"]:
                problems.append(f"{label}: aspects TT {truth['aspects']} vs {entry['aspects']}")
            if entry["instability"] != truth["instability"]:
                problems.append(f"{label}: instability TT {truth['instability']} vs {entry['instability']}")
            infusion_component(truth["central"], entry["central"], soft, label + " central", problems)
            if len(truth["components"]) != len(entry["components"]):
                problems.append(f"{label}: component count TT {len(truth['components'])} vs {len(entry['components'])}")
            else:
                for index, (component, text) in enumerate(zip(truth["components"], entry["components"])):
                    infusion_component(component, text, soft, f"{label} component {index}", problems)
        elif truth["type"] == "crucible":
            if entry["type"] != "CrucibleRecipe":
                problems.append(f"{label}: addon type {entry['type']}")
                continue
            if entry["research"] != RESEARCH_FIXES.get(key, truth["research"]):
                problems.append(f"{label}: research TT {truth['research']} vs {entry['research']}")
            check_result(truth["result"], entry["result"], label, problems, soft)
            if key in {f"INFUSED_POTIONS{index}" for index in range(6)}:
                aspect = ["aer", "ignis", "terra", "aqua", "ordo", "perditio"][int(key[-1])]
                if f"seed_aspect={aspect}" not in entry["result"]:
                    problems.append(f"{label}: wrong seed aspect, expected {aspect}")
            if runtime_aspects(entry["aspects"]) != truth["aspects"]:
                problems.append(f"{label}: aspects TT {truth['aspects']} vs {entry['aspects']}")
            same(truth["catalyst"], entry["catalyst"], soft, label + " catalyst", problems)
    known = {recipe["key"] for recipe in recipes}
    first = next(index for index, entry in enumerate(dump) if entry["key"] in known)
    extras = [entry["key"] for entry in dump[first:] if entry["key"] not in used and entry["key"] not in known]
    print("PROBLEMS", len(problems))
    for line in problems:
        print("  " + line)
    print("SOFT", len(soft))
    for line in soft:
        print("  " + line)
    print("ADDON ONLY", extras)
    return {"problems": problems, "soft": soft, "extras": extras}
