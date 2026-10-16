import argparse
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
BASE = ROOT.parent / "thaumcraft-4-reforged"
CONFIG = BASE / "reference" / "decompiled" / "thaumcraft" / "common" / "config"
ID_MAP = BASE / "tools" / "id_map.json"
OUTPUT = ROOT / "tools" / "tc4_refs.json"
WILDCARD = 32767

sys.path.insert(0, str(BASE / "tools"))
import convert_tags as ct

ORE_DICT = dict(ct.ORE_DICT)
ORE_DICT.update({
    "ingotIron": "#c:ingots/iron",
    "ingotThaumium": "#c:ingots/thaumium",
    "ingotVoid": "#c:ingots/void_metal",
    "nuggetThaumium": "#c:nuggets/thaumium",
    "nuggetVoid": "#c:nuggets/void_metal",
    "oreCinnabar": "#c:ores/cinnabar",
    "gemEmerald": "#c:gems/emerald",
})
ITEM_REGISTRATION = re.compile(r"GameRegistry\.registerItem\((\w+),\s*\"(\w+)\"")
BLOCK_REGISTRATION = re.compile(r"GameRegistry\.registerBlock\((\w+),(?:\s*[\w.]+\.class,)?\s*\"(\w+)\"")
BLOCK_NAME = re.compile(r"^\s*(\w+) = new [\w.]+\(.*\)\.setUnlocalizedName\(\"(\w+)\"\)", re.MULTILINE)
ITEM_NAME = re.compile(r"^\s*(\w+) = new [\w.]+\(.*\)\.setUnlocalizedName\(\"(\w+)\"\)", re.MULTILINE)
REFERENCE = re.compile(r"^(?:(ConfigItems|ConfigBlocks)\.)?(\w+)$")


def load_fields():
    items_text = (CONFIG / "ConfigItems.java").read_text(encoding="utf-8")
    blocks_text = (CONFIG / "ConfigBlocks.java").read_text(encoding="utf-8")
    items = dict(ITEM_NAME.findall(items_text))
    items.update(dict(ITEM_REGISTRATION.findall(items_text)))
    blocks = dict(BLOCK_NAME.findall(blocks_text))
    blocks.update(dict(BLOCK_REGISTRATION.findall(blocks_text)))
    return items, blocks


ITEM_FIELDS, BLOCK_FIELDS = load_fields()
ROWS = json.loads(ID_MAP.read_text(encoding="utf-8"))


def describe(kind, row):
    identifier = f"thaumcraft:{row['id']}"
    state = row.get("state") if kind == "blocks" else None
    if state:
        identifier += "[" + ",".join(f"{key}={value}" for key, value in sorted(state.items())) + "]"
    return identifier


def select(rows, meta):
    if meta == WILDCARD:
        return rows
    exact = [row for row in rows if row["meta"] == meta]
    return exact or [row for row in rows if row["meta"] is None]


def thaumcraft_ref(kind, field, meta):
    table = ITEM_FIELDS if kind == "items" else BLOCK_FIELDS
    old = table.get(field)
    if old is None:
        return None
    rows = [row for row in ROWS[kind] if row["old"] == old]
    results = []
    for row in select(rows, meta):
        if row["skip"] is not None:
            results.append(f"skipped ({row['skip']}): {old}")
        elif row["id"]:
            results.append(describe(kind, row))
    return results or None


def vanilla_ref(reference, meta):
    return ct.vanilla_ids(reference, meta)


def oredict_ref(name):
    value = ORE_DICT.get(name)
    return [value] if value else None


def resolve(text, meta_text=None):
    parts = text.replace(":", " ").split()
    name = parts[0]
    meta = WILDCARD
    if meta_text is None and len(parts) > 1:
        meta_text = parts[1]
    if meta_text is not None:
        meta = WILDCARD if meta_text in ("*", "wildcard") else int(meta_text)
    elif not name.startswith(("ConfigItems.", "ConfigBlocks.", "Items.", "Blocks.")):
        return oredict_ref(name)
    elif not name.startswith(("ConfigItems.", "ConfigBlocks.")):
        meta = 0
    else:
        meta = 0
    if name.startswith("ConfigItems."):
        return thaumcraft_ref("items", name.split(".", 1)[1], meta)
    if name.startswith("ConfigBlocks."):
        return thaumcraft_ref("blocks", name.split(".", 1)[1], meta)
    if name.startswith(("Items.", "Blocks.")):
        return vanilla_ref(name, meta)
    return oredict_ref(name)


def build_all():
    result = {"thaumcraft": {}, "vanilla": {}, "oredict": dict(sorted(ORE_DICT.items()))}
    for prefix, kind, table in (("ConfigItems", "items", ITEM_FIELDS), ("ConfigBlocks", "blocks", BLOCK_FIELDS)):
        for field, old in sorted(table.items()):
            rows = [row for row in ROWS[kind] if row["old"] == old]
            if not rows:
                continue
            entry = {}
            for row in rows:
                key = "*" if row["meta"] is None else str(row["meta"])
                entry[key] = describe(kind, row) if row["skip"] is None else f"skipped: {row['skip']}"
            result["thaumcraft"][f"{prefix}.{field}"] = entry
    for reference in sorted(ct.VANILLA["mcp_fields"]):
        kind = "blocks" if reference.startswith("Blocks.") else "items"
        base = ct.VANILLA["mcp_fields"][reference]
        metas = sorted({int(key.rsplit(":", 1)[1]) for key in ct.VANILLA["flatten"][kind] if key.rsplit(":", 1)[0] == base})
        entry = {}
        default = ct.vanilla_ids(reference, 0)
        if default:
            entry["*"] = default
        for meta in metas:
            ids = ct.vanilla_ids(reference, meta)
            if ids and ids != default:
                entry[str(meta)] = ids
        if entry:
            result["vanilla"][reference] = entry
    return result


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("reference", nargs="?", help="ConfigItems.itemResource, ConfigBlocks.blockCosmeticSolid, Items.coal, Blocks.stone or an ore dictionary name")
    parser.add_argument("meta", nargs="?", help="meta value, or * for every meta")
    parser.add_argument("--all", action="store_true", help="write tools/tc4_refs.json")
    arguments = parser.parse_args()
    if arguments.all:
        data = build_all()
        OUTPUT.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
        print(f"wrote {OUTPUT.relative_to(ROOT)}: {len(data['thaumcraft'])} thaumcraft, {len(data['vanilla'])} vanilla, {len(data['oredict'])} oredict")
        return 0
    if not arguments.reference:
        parser.error("reference or --all is required")
    found = resolve(arguments.reference, arguments.meta)
    if not found:
        print(f"no mapping for {arguments.reference} {arguments.meta or ''}".strip())
        return 1
    print("\n".join(found))
    return 0


if __name__ == "__main__":
    sys.exit(main())
