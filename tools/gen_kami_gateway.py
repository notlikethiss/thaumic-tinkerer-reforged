import struct
import zlib

SIDES = ("north", "south", "east", "west")


def png_chunk(kind, data):
    body = kind + data
    return struct.pack(">I", len(data)) + body + struct.pack(">I", zlib.crc32(body) & 0xFFFFFFFF)


def white_png():
    header = struct.pack(">IIBBBBB", 1, 1, 8, 6, 0, 0, 0)
    pixels = zlib.compress(b"\x00\xff\xff\xff\xff")
    return b"\x89PNG\r\n\x1a\n" + png_chunk(b"IHDR", header) + png_chunk(b"IDAT", pixels) + png_chunk(b"IEND", b"")


def generate(ctx):
    target = ctx.ASSETS / "textures" / "misc" / "white.png"
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_bytes(white_png())

    ctx.generated_item("sky_pearl", "skypearl")

    faces = {side: {"texture": "#side"} for side in SIDES}
    faces["down"] = {"texture": "#side", "cullface": "down"}
    faces["up"] = {"texture": "#top"}
    ctx.block_model("warp_gate", {
        "textures": {
            "particle": ctx.ref("warpgate1"),
            "top": ctx.ref("warpgate0"),
            "side": ctx.ref("warpgate1"),
            "overlay": ctx.ref("warpgate2"),
        },
        "elements": [
            {"from": [0, 0, 0], "to": [16, 16, 16], "faces": faces},
            {"from": [0, 16.01, 0], "to": [16, 16.01, 16], "light_emission": 15, "faces": {"up": {"texture": "#overlay"}}},
        ],
    })
    ctx.simple_state("warp_gate")
    ctx.block_item("warp_gate")
    ctx.self_drop("warp_gate")
    ctx.mineable("pickaxe", "warp_gate")
