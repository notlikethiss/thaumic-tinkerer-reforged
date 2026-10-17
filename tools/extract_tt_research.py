import json
import re
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

import ttjava

ROOT = Path(__file__).resolve().parent.parent
COMMON = ROOT / "reference" / "tt-src" / "src" / "main" / "java" / "thaumic" / "tinkerer" / "common"
OUTPUT = ROOT / "tools" / "tt_research.json"
EXCLUDED = {"PERIPHERALS", "ASPECT_ANALYZER", "GOLEM_CONNECTOR", "REMOTE_PLACER", "MOB_DISPLAY"}
DEFAULT_PAGES = {
    "SHARE_TOME": [{"type": "text", "text": "0"}, {"type": "crafting", "recipes": ["SHARE_TOME"]}],
    "FOCUS_ENDER_CHEST": [{"type": "text", "text": "0"}, {"type": "arcane", "recipes": ["FOCUS_ENDER_CHEST"]}],
}
PAGE_HELPERS = {
    "recipePage": "crafting",
    "arcaneRecipePage": "arcane",
    "infusionPage": "infusion",
    "enchantPage": "enchant",
    "crucibleRecipePage": "crucible",
}
FLAG_METHODS = {
    "setSecondary": "secondary",
    "setRound": "round",
    "setStub": "stub",
    "setSpecial": "special",
    "setConcealed": "concealed",
    "setAutoUnlock": "autoUnlock",
    "setVirtual": "virtual",
    "setHidden": "hidden",
    "setLost": "lost",
}
LIST_METHODS = {"setParents": "parents", "setParentsHidden": "parentsHidden", "setSiblings": "siblings"}


class Evaluator:
    def __init__(self, constants):
        self.constants = constants

    def string(self, node):
        kind = node["t"]
        if kind == "str":
            return node["v"]
        if kind == "field" and node["target"]["t"] == "name" and node["target"]["v"] == "LibResearch":
            return self.constants[node["name"]]
        if kind == "bin" and node["op"] == "+":
            return self.string(node["l"]) + self.string(node["r"])
        if kind == "num":
            return str(node["v"])
        raise ValueError("cannot evaluate string " + node["raw"])

    def integer(self, node):
        if node["t"] == "num":
            return int(node["v"])
        if node["t"] == "neg":
            return -self.integer(node["v"])
        raise ValueError("cannot evaluate int " + node["raw"])

    def aspects(self, node):
        entries = []
        chain = []
        current = node
        while current["t"] == "call":
            chain.append(current)
            current = current["target"]
        if current["t"] != "new" or current["cls"] != "AspectList":
            raise ValueError("cannot evaluate aspects " + node["raw"])
        for call in reversed(chain):
            if call["name"] != "add":
                raise ValueError("unexpected aspect call " + call["raw"])
            aspect = call["args"][0]
            if aspect["t"] != "field" or aspect["target"]["v"] != "Aspect":
                raise ValueError("unexpected aspect " + aspect["raw"])
            entries.append([aspect["name"], self.integer(call["args"][1])])
        return entries

    def page(self, node):
        if node["t"] == "new" and node["cls"] == "ResearchPage":
            if len(node["args"]) == 1 and node["args"][0]["t"] == "str":
                return {"type": "text", "text": node["args"][0]["v"]}
            raise ValueError("unknown page " + node["raw"])
        if node["t"] == "call" and node["name"] in PAGE_HELPERS:
            kind = PAGE_HELPERS[node["name"]]
            name = self.string(node["args"][0])
            if node["name"] == "infusionPage" and len(node["args"]) == 2:
                count = self.integer(node["args"][1])
                return {"type": "infusion", "recipes": [name + str(index) for index in range(count)]}
            return {"type": kind, "recipes": [name]}
        raise ValueError("unknown page " + node["raw"])


def chain_of(node):
    calls = []
    current = node
    while current["t"] == "call":
        calls.append(current)
        current = current["target"]
    calls.reverse()
    return current, calls


def apply_pages(entry, evaluator, nodes):
    entry["pages"] = [evaluator.page(item) for item in nodes]
    has_infusion = any(page["type"] == "infusion" for page in entry["pages"])
    if has_infusion and not entry["kami"]:
        entry["parentsHidden"] = ["INFUSION"] + entry["parentsHidden"]


def build(node, followups, evaluator, source_file):
    root, calls = chain_of(node)
    if root["t"] != "new":
        raise ValueError("bad chain root " + root["raw"])
    for followup in followups:
        calls = calls + chain_of(followup)[1]
    args = root["args"]
    entry = {
        "key": evaluator.string(args[0]),
        "category": "TT_CATEGORY",
        "kami": root["cls"] == "KamiResearchItem",
        "aspects": evaluator.aspects(args[1]),
        "column": evaluator.integer(args[2]),
        "row": evaluator.integer(args[3]),
        "complexity": evaluator.integer(args[4]),
        "icon": args[5]["raw"],
        "parents": [],
        "parentsHidden": [],
        "siblings": [],
        "warp": 0,
        "pages": [],
        "source": source_file,
    }
    for flag in FLAG_METHODS.values():
        entry[flag] = False
    if entry["kami"]:
        entry["concealed"] = True
    if len(args) > 6:
        apply_pages(entry, evaluator, args[6:])
    for call in calls:
        name = call["name"]
        if name in FLAG_METHODS:
            entry[FLAG_METHODS[name]] = True
        elif name in LIST_METHODS:
            entry[LIST_METHODS[name]] = [evaluator.string(item) for item in call["args"]]
        elif name == "setWarp":
            entry["warp"] = evaluator.integer(call["args"][0])
        elif name == "setPages":
            apply_pages(entry, evaluator, call["args"])
        elif name == "registerResearchItem":
            pass
        else:
            raise ValueError("unknown method " + name + " in " + entry["key"])
    if not entry["pages"] and entry["key"] in DEFAULT_PAGES:
        entry["pages"] = DEFAULT_PAGES[entry["key"]]
    entry["parentsHidden"] = list(dict.fromkeys(entry["parentsHidden"]))
    entry["complexityClamped"] = max(1, min(3, entry["complexity"]))
    return entry


def collect():
    constants = ttjava.load_constants(COMMON / "lib" / "LibResearch.java")
    evaluator = Evaluator(constants)
    entries = {}
    order = []
    for path in sorted(COMMON.rglob("*.java")):
        text = ttjava.strip_comments(path.read_text(encoding="utf-8", errors="replace"))
        if path.name == "ResearchHelper.java":
            text = re.sub(r"\)\s*;\s*research\.setPages", ").setPages", text)
        elif "new TTResearchItem" not in text and "new KamiResearchItem" not in text:
            continue
        if path.name in ("TTResearchItem.java", "KamiResearchItem.java"):
            continue
        for node, followups in ttjava.find_statements(text, {"TTResearchItem", "KamiResearchItem"}):
            if node["t"] == "error":
                raise ValueError(str(path) + ": " + node["error"])
            if node["t"] not in ("call", "new"):
                raise ValueError(str(path) + ": unexpected node " + node["raw"][:80])
            if node["t"] == "new" and not node["args"]:
                continue
            if node["t"] == "new" and len(node["args"]) < 6:
                continue
            entry = build(node, followups, evaluator, str(path.relative_to(COMMON)))
            key = entry["key"]
            if key in entries:
                entry["variantCount"] = entries[key].get("variantCount", 0) + 1
                entries[key] = entry
                continue
            entries[key] = entry
            order.append(key)
    result = []
    for key in order:
        if key in EXCLUDED:
            continue
        result.append(entries[key])
    return result


def main():
    entries = collect()
    OUTPUT.write_text(json.dumps(entries, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    print("entries:", len(entries))


if __name__ == "__main__":
    main()
