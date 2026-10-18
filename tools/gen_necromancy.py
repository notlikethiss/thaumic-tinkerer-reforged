SIDES = ("north", "south", "east", "west")

ASPECTS = (
    "aqua", "humanus", "aer", "volatus", "ignis", "praecantatio", "exanimis", "corpus", "bestia",
    "venenum", "terra", "alienis", "iter", "metallum", "limus", "pannus", "messis",
)


def generate(ctx):
    for aspect in ASPECTS:
        ctx.generated_item(f"{aspect}_soul_aspect", aspect)
        ctx.generated_item(f"condensed_{aspect}_soul_aspect", f"{aspect}_condensed")
        ctx.generated_item(f"infused_{aspect}_soul_aspect", aspect)
        ctx.shaped(
            f"condensed_{aspect}_soul_aspect", f"thaumictinkerer:condensed_{aspect}_soul_aspect", 1,
            ["XXX", "XXX", "XXX"],
            {"X": f"thaumictinkerer:{aspect}_soul_aspect"},
        )

    faces = {side: {"uv": [0, 14, 16, 16], "texture": "#side"} for side in SIDES}
    faces["down"] = {"texture": "#bottom", "cullface": "down"}
    faces["up"] = {"texture": "#top"}
    ctx.block_model("necromancy_tablet", {
        "parent": "minecraft:block/block",
        "textures": {
            "particle": ctx.ref("spawner2"),
            "bottom": "minecraft:block/obsidian",
            "top": ctx.ref("spawner1"),
            "side": ctx.ref("spawner2"),
        },
        "elements": [{"from": [0, 0, 0], "to": [16, 2, 16], "faces": faces}],
    })
    ctx.simple_state("necromancy_tablet")
    ctx.block_item("necromancy_tablet")
    ctx.self_drop("necromancy_tablet")
    ctx.mineable("pickaxe", "necromancy_tablet")
