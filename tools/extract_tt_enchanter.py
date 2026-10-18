import argparse
import json
import re
from pathlib import Path

import ttjava
from compare_recipes import runtime_aspects

ROOT = Path(__file__).resolve().parent.parent
TT = ROOT / "reference/tt-src/src/main/java/thaumic/tinkerer"
SOURCE = TT / "common/enchantment/core/EnchantmentManager.java"
OUTPUT = ROOT / "tools/tt_enchanter.json"


def collect():
    research = ttjava.load_constants(TT / "common/lib/LibResearch.java")
    textures = ttjava.load_constants(TT / "client/lib/LibResources.java")
    rows = {}
    pattern = r"registerExponentialCostData\((\w+)\.(\w+), LibResources\.(\w+), (true|false), (new AspectList\(\)(?:\.add\(Aspect\.\w+, \d+\))+)(?:, LibResearch\.(\w+))?\);"
    calls = re.findall(pattern, ttjava.strip_comments(SOURCE.read_text()))
    assert len(calls) == 41, "Unexpected original enchanter registration shape"
    for owner, name, texture, vanilla, aspects, gate in calls:
        namespace = {"Enchantment": "minecraft", "Config": "thaumcraft", "ModEnchantments": "thaumictinkerer"}[owner]
        name = {"field_151369_A": "lure", "field_151370_z": "luck_of_the_sea", "dispersedStrikes": "dispersedStrike"}.get(name, name)
        if owner == "Config":
            name = name.removeprefix("ench")
        name = re.sub(r"(?<!^)(?=[A-Z])", "_", name).lower()
        key = namespace + ":" + name
        rows[key] = {
            "id": key,
            "texture": textures[texture].lower().replace("ttinkerer:", "thaumictinkerer:"),
            "vanilla": vanilla == "true",
            "research": research[gate] if gate else "",
            "aspects": {aspect: int(amount) for aspect, amount in re.findall(r"\.add\(Aspect\.(\w+), (\d+)\)", aspects)},
        }
    assert len(rows) == 40
    return list(rows.values())


def compare(expected, actual):
    actual = {row["id"]: row for row in actual}
    problems = []
    if set(actual) != {row["id"] for row in expected}:
        problems.append("Enchanter enchantment IDs differ")
    for row in expected:
        key = row["id"]
        value = actual.get(key)
        if value is None:
            problems.append(key + ": missing")
            continue
        for field in ["texture", "vanilla", "research"]:
            if row[field] != value[field]:
                problems.append(f"{key}: {field} TT {row[field]} vs runtime {value[field]}")
        if row["aspects"] != runtime_aspects(value["aspects"]):
            problems.append(key + ": base aspect cost differs")
        if not value["levels"]:
            problems.append(key + ": no applicable levels")
        for level, cost in value["levels"].items():
            factor = int(level) * (1 + int(level) * 0.2)
            wanted = {aspect: int(amount * factor) for aspect, amount in row["aspects"].items()}
            if wanted != runtime_aspects(cost):
                problems.append(f"{key}: level {level} cost differs")
    return problems


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--compare", type=Path)
    args = parser.parse_args()
    rows = collect()
    OUTPUT.write_text(json.dumps(rows, indent=2, sort_keys=True) + "\n")
    print("TT enchanter entries:", len(rows))
    if args.compare:
        problems = compare(rows, json.loads(args.compare.read_text()))
        print("PROBLEMS", len(problems))
        for problem in problems:
            print(problem)
        if problems:
            raise SystemExit(1)


if __name__ == "__main__":
    main()
