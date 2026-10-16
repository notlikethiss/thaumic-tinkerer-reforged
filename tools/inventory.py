import argparse
import json
import re
import sys
from collections import OrderedDict
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
NS = "thaumictinkerer"
REGISTRY = ROOT / "src" / "main" / "java" / NS
LANG_SOURCE = ROOT / "reference" / "tt-src" / "src" / "main" / "resources" / "assets" / "ttinkerer" / "lang" / "en_US.lang"
ASSETS = ROOT / "src" / "generated" / "resources" / "assets" / NS
LANG_OUT = ROOT / "src" / "main" / "resources" / "assets" / NS / "lang" / "en_us.json"
ID_MAP_PATH = ROOT / "tools" / "id_map.json"
ID_PATTERN = re.compile(r"^[a-z0-9_]+$")
NAME_KEY = re.compile(r"^(tile|item)\..+\.name$")
LIST_LIMIT = 15


def read_source_lang():
    if not LANG_SOURCE.exists():
        return None
    entries = OrderedDict()
    for line in LANG_SOURCE.read_bytes().decode("utf-8-sig").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        entries[key.strip()] = value
    return entries


def state_key(row):
    return json.dumps(row.get("state") or {}, sort_keys=True)


def validate_schema(data, lang):
    errors = []
    covered = set()
    for kind in ("blocks", "items"):
        seen = {}
        for row in data.get(kind, []):
            label = f"{kind}:{row.get('old')}:{row.get('meta')}"
            missing = {"old", "meta", "lang", "id", "skip"} - set(row)
            if kind == "blocks" and "state" not in row:
                missing.add("state")
            if missing:
                errors.append(f"{label}: missing fields {sorted(missing)}")
                continue
            if row["meta"] is not None and not isinstance(row["meta"], int):
                errors.append(f"{label}: meta must be int or null")
            if row["lang"] is not None:
                covered.add(row["lang"])
                if lang is not None and row["lang"] not in lang:
                    errors.append(f"{label}: lang key {row['lang']} is not in en_US.lang")
            if row["skip"] is not None:
                if not isinstance(row["skip"], str) or not row["skip"].strip():
                    errors.append(f"{label}: skip must be a non-empty string or null")
                continue
            if not isinstance(row["id"], str) or not ID_PATTERN.match(row["id"]):
                errors.append(f"{label}: invalid id {row['id']!r}")
                continue
            key = (row["id"], state_key(row) if kind == "blocks" else "")
            if key in seen:
                errors.append(f"{label}: duplicate id {row['id']} (already used by {seen[key]})")
            seen[key] = label
    if lang is not None:
        for key in lang:
            if NAME_KEY.match(key) and key not in covered:
                errors.append(f"lang key not covered: {key}")
    return errors


def active_ids(data, kind):
    return OrderedDict((row["id"], row) for row in data.get(kind, []) if row["skip"] is None)


def registry_text():
    return "\n".join(path.read_text(encoding="utf-8") for path in sorted((REGISTRY / "registry").rglob("*.java")))


def load_lang():
    if not LANG_OUT.exists():
        return {}
    return json.loads(LANG_OUT.read_text(encoding="utf-8"))


def report(title, missing, total, show_all):
    print(f"{title}: missing {len(missing)} of {total}")
    shown = missing if show_all else missing[:LIST_LIMIT]
    for name in shown:
        print(f"  {name}")
    if len(missing) > len(shown):
        print(f"  ... {len(missing) - len(shown)} more (use --all)")


def check(strict, show_all):
    if not ID_MAP_PATH.exists():
        print("tools/id_map.json is missing")
        return 1
    data = json.loads(ID_MAP_PATH.read_text(encoding="utf-8"))
    lang_source = read_source_lang()
    if lang_source is None:
        print("reference/tt-src is missing, lang coverage of the source is not checked")
    errors = validate_schema(data, lang_source)
    for kind in ("blocks", "items"):
        rows = data.get(kind, [])
        skipped = sum(1 for row in rows if row["skip"] is not None)
        print(f"{kind}: entries {len(rows)}, mapped {len(rows) - skipped}, skipped {skipped}, unique ids {len(active_ids(data, kind))}")
    for error in errors:
        print("ERROR:", error)

    blocks = active_ids(data, "blocks")
    items = active_ids(data, "items")
    text = registry_text()
    lang = load_lang()
    registered = lambda name: f'"{name}"' in text
    missing_registered = [f"{kind}:{name}" for kind, ids in (("block", blocks), ("item", items)) for name in ids if not registered(name)]
    missing_models = []
    for name in blocks:
        if not (ASSETS / "blockstates" / f"{name}.json").exists():
            missing_models.append(f"block:{name}")
        if not (ASSETS / "items" / f"{name}.json").exists():
            missing_models.append(f"block item:{name}")
    for name in items:
        if name not in blocks and not (ASSETS / "items" / f"{name}.json").exists():
            missing_models.append(f"item:{name}")
    missing_lang = [f"block:{name}" for name in blocks if f"block.{NS}.{name}" not in lang]
    missing_lang += [f"item:{name}" for name in items if name not in blocks and f"item.{NS}.{name}" not in lang]
    total = len(blocks) + len(items)
    report("registered", missing_registered, total, show_all)
    report("models", missing_models, len(blocks) * 2 + len([name for name in items if name not in blocks]), show_all)
    report("lang", missing_lang, total, show_all)
    incomplete = bool(missing_registered or missing_models or missing_lang)
    print("check:", "schema errors" if errors else ("ok" if not incomplete else "content incomplete"))
    if errors:
        return 1
    if strict and incomplete:
        return 1
    return 0


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    parser.add_argument("--strict", action="store_true")
    parser.add_argument("--all", action="store_true")
    arguments = parser.parse_args()
    return check(arguments.strict, arguments.all)


if __name__ == "__main__":
    sys.exit(main())
