KINDS = ["aer", "ignis", "aqua", "terra", "ordo", "perditio", "generic"]
PRIMAL_KINDS = KINDS[:-1]
STAGES = [0, 0, 1, 1, 2, 2, 2, 3]
GRAINS = {
    "aer_infused_grain": "fruit_aer",
    "ignis_infused_grain": "fruit_ignis",
    "terra_infused_grain": "fruit_terra",
    "aqua_infused_grain": "fruit_aqua",
}


def crop_model(ctx, kind, stage):
    face = {"uv": [0, 0, 16, 16], "texture": "#crop"}
    if kind == "generic":
        face["tintindex"] = 0

    def plane(start, end, sides):
        return {"from": start, "to": end, "faces": {side: dict(face) for side in sides}}

    ctx.block_model(f"infused_grain_crop_{kind}_{stage}", {
        "ambientocclusion": False,
        "textures": {"crop": ctx.ref(f"crop_{kind}_{stage}"), "particle": ctx.ref(f"crop_{kind}_{stage}")},
        "elements": [
            plane([4, -1, 0], [4, 15, 16], ["east", "west"]),
            plane([12, -1, 0], [12, 15, 16], ["east", "west"]),
            plane([0, -1, 4], [16, 15, 4], ["north", "south"]),
            plane([0, -1, 12], [16, 15, 12], ["north", "south"]),
        ],
    })


def crops(ctx):
    variants = {}
    for kind in KINDS:
        for stage in range(4):
            crop_model(ctx, kind, stage)
        for age, stage in enumerate(STAGES):
            variants[f"age={age},kind={kind}"] = {"model": ctx.ref(f"infused_grain_crop_{kind}_{stage}")}
    ctx.blockstate("infused_grain_crop", {"variants": variants})
    ctx.block_item("infused_grain_crop", model=ctx.ref("infused_grain_crop_aer_3"))


def farmland(ctx):
    ctx.block_model("infused_farmland", {
        "parent": "minecraft:block/cube_bottom_top",
        "textures": {
            "top": ctx.ref("infusedfarmland"),
            "bottom": "minecraft:block/dirt",
            "side": "minecraft:block/dirt",
        },
    })
    ctx.blockstate("infused_farmland", {"variants": {f"moisture={moisture}": {"model": ctx.ref("infused_farmland")} for moisture in range(8)}})
    ctx.write_loot("infused_farmland", [{
        "rolls": 1,
        "entries": [{"type": "minecraft:item", "name": "minecraft:dirt"}],
        "condition": {"type": "minecraft:survives_explosion"},
    }])
    ctx.mineable("shovel", "infused_farmland")
    ctx.block_item("infused_farmland")


def seeds(ctx):
    cases = []
    for kind in PRIMAL_KINDS:
        ctx.item_model(f"infused_seeds_{kind}", {
            "parent": "minecraft:item/generated",
            "textures": {"layer0": ctx.ref(f"seed_{kind}", "item")},
        })
        cases.append({"when": kind, "model": {"type": "minecraft:model", "model": ctx.ref(f"infused_seeds_{kind}", "item")}})
    ctx.item_model("infused_seeds_generic", {
        "parent": "minecraft:item/generated",
        "textures": {"layer0": ctx.ref("seed_generic", "item")},
    })
    ctx.write(ctx.ASSETS / "items" / "infused_seeds.json", {"model": {
        "type": "minecraft:select",
        "property": "minecraft:component",
        "component": f"{ctx.NS}:seed_aspect",
        "cases": cases,
        "fallback": {
            "type": "minecraft:model",
            "model": ctx.ref("infused_seeds_generic", "item"),
            "tints": [{"type": f"{ctx.NS}:seed_aspect"}],
        },
    }})


def generate(ctx):
    crops(ctx)
    farmland(ctx)
    seeds(ctx)
    for name, texture in GRAINS.items():
        ctx.generated_item(name, texture)
