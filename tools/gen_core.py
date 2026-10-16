import json
from pathlib import Path

ID_MAP = Path(__file__).resolve().parent / "id_map.json"


def ids():
    data = json.loads(ID_MAP.read_text(encoding="utf-8"))
    return {
        kind: {row["id"] for row in data[kind] if row["skip"] is None}
        for kind in ("blocks", "items")
    }


def known(name):
    registered = ids()
    return name in registered["blocks"] or name in registered["items"]


def generate(ctx):
    ctx.known_id = known
