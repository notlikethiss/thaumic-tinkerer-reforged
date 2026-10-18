import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
AUDIT = ROOT / "tools/backport/audit.json"
JAVA = ROOT / "src/main/java/thaumictinkerer"


def update():
    rows = json.loads(AUDIT.read_text())
    research = json.loads((ROOT / "tools/tt_research.json").read_text())
    recipes = json.loads((ROOT / "tools/tt_recipes.json").read_text())
    extra_sources = {
        "gas": ["item/ItemGas.java", "item/ItemGasRemover.java"],
        "dark_quartz": ["item/quartz/ItemDarkQuartz.java"],
        "transvector": ["item/ItemConnector.java"],
    }
    for row in rows:
        row["original_sources"] = sorted(set(row["original_sources"] + extra_sources.get(row["feature"], [])))
        stem = Path(row["test"]).stem.replace("GameTests", "")
        implementations = list(JAVA.rglob(stem + "Feature.java"))
        if row["feature"] == "core":
            implementations = [JAVA / "ThaumicTinkerer.java", JAVA / "Features.java", JAVA / "Config.java"]
        assert implementations, row["feature"]
        row["implementation"] = [str(path.relative_to(ROOT)) for path in sorted(implementations)]
        if row["feature"] == "enchanting":
            row["implementation"].append("src/main/java/thaumictinkerer/enchanting/EnchantmentManager.java")
            row["enchanter"] = json.loads((ROOT / "tools/tt_enchanter.json").read_text())
        row["cases"] = re.findall(r'\.add\("([^"]+)"', (ROOT / row["test"]).read_text())
        row["research"] = [{"key": item["key"], "source": item["source"]} for item in research if item["source"] in row["original_sources"]]
        row["recipes"] = []
        for item in recipes:
            source = item.get("source", "item/ItemMobAspect.java")
            if source in row["original_sources"]:
                record = {key: item[key] for key in ["key", "type", "research", "variant", "result"] if key in item}
                record["source"] = source
                row["recipes"].append(record)
        for path in row["implementation"] + [row["test"], row["generator"]]:
            assert (ROOT / path).is_file(), path
        for path in row["original_sources"]:
            assert (ROOT / "reference/tt-src/src/main/java/thaumic/tinkerer/common" / path).is_file(), path
    covered_research = {item["key"] for row in rows for item in row["research"]}
    assert covered_research == {item["key"] for item in research}
    excluded = {"ASPECT_ANALYZER", "GOLEM_CONNECTOR", "REMOTE_PLACER"}
    covered_recipes = {(item["key"], item.get("variant")) for row in rows for item in row["recipes"]}
    assert all((item["key"], item.get("variant")) in covered_recipes for item in recipes if item["key"] not in excluded)
    AUDIT.write_text(json.dumps(rows, ensure_ascii=False, indent=2) + "\n")
    print(f"Audit: {len(rows)} modules, {len(covered_research)} research keys; all included original recipe records mapped")


if __name__ == "__main__":
    update()
