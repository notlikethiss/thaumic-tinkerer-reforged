SIDES = ("north", "south", "east", "west")


def generate(ctx):
    faces = {side: {"uv": [0, 14, 16, 16], "texture": "#side"} for side in SIDES}
    faces["down"] = {"texture": "#bottom", "cullface": "down"}
    faces["up"] = {"texture": "#top"}
    ctx.block_model("dynamism_tablet", {
        "parent": "minecraft:block/block",
        "textures": {
            "particle": ctx.ref("animationtablet2"),
            "bottom": ctx.ref("animationtablet0"),
            "top": ctx.ref("animationtablet1"),
            "side": ctx.ref("animationtablet2"),
        },
        "elements": [{"from": [0, 0, 0], "to": [16, 2, 16], "faces": faces}],
    })
    variants = {}
    for facing in SIDES:
        for powered in ("false", "true"):
            variants[f"facing={facing},powered={powered}"] = {"model": ctx.ref("dynamism_tablet")}
    ctx.blockstate("dynamism_tablet", {"variants": variants})
    ctx.block_item("dynamism_tablet")
    ctx.self_drop("dynamism_tablet")
    ctx.mineable("pickaxe", "dynamism_tablet")
