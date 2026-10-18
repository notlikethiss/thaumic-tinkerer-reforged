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
    if not ctx.profile.modern:
        from nbt import Int, TagList, TAG_INT, TAG_COMPOUND, to_nbt
        entries = [{"pos": TagList([Int(x), Int(0), Int(z)], TAG_INT), "state": Int(0)} for x in range(7) for z in range(7)]
        template = {
            "DataVersion": Int(ctx.profile.data_version),
            "size": TagList([Int(7), Int(7), Int(7)], TAG_INT),
            "palette": TagList([{"Name": "minecraft:stone"}], TAG_COMPOUND),
            "blocks": TagList(entries, TAG_COMPOUND),
            "entities": TagList([], TAG_COMPOUND),
        }
        target = ctx.DATA / ctx.NS / ctx.profile.structure_folder / "test" / "platform.nbt"
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(to_nbt(template))
