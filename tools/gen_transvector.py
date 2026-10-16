DIRECTIONS = ("down", "up", "north", "south", "west", "east")
OPPOSITE = {"down": "up", "up": "down", "north": "south", "south": "north", "west": "east", "east": "west"}


def camo_variant(ctx, model):
    return {"type": f"{ctx.NS}:camo", "fallback": {"model": model}}


def camo_state(ctx, name):
    ctx.blockstate(name, {"variants": {"": camo_variant(ctx, ctx.ref(name))}})


def dislocator(ctx):
    for direction in DIRECTIONS:
        textures = {face: ctx.ref("dislocator1" if face == direction else "dislocator0") for face in DIRECTIONS}
        textures["particle"] = ctx.ref("dislocator0")
        ctx.block_model(f"transvector_dislocator_{direction}", {"parent": "minecraft:block/cube", "textures": textures})
    ctx.cube_all("transvector_dislocator_item", "dislocator0")
    variants = {}
    for direction in DIRECTIONS:
        for powered in ("false", "true"):
            variants[f"facing={direction},powered={powered}"] = camo_variant(ctx, ctx.ref(f"transvector_dislocator_{direction}"))
    ctx.blockstate("transvector_dislocator", {"variants": variants})
    ctx.block_item("transvector_dislocator", model=ctx.ref("transvector_dislocator_item"))
    ctx.self_drop("transvector_dislocator")


def generate(ctx):
    ctx.cube_all("transvector_interface", "interface")
    camo_state(ctx, "transvector_interface")
    ctx.block_item("transvector_interface")
    ctx.self_drop("transvector_interface")

    dislocator(ctx)

    ctx.cube_all("ethereal_platform", "platform")
    camo_state(ctx, "ethereal_platform")
    ctx.block_item("ethereal_platform")
    ctx.self_drop("ethereal_platform")

    ctx.mineable("pickaxe", "transvector_interface", "transvector_dislocator")
    ctx.mineable("axe", "ethereal_platform")

    ctx.item_model("transvector_binder", {
        "parent": "minecraft:item/handheld",
        "textures": {"layer0": ctx.ref("connector", "item")},
    })
    ctx.item_definition("transvector_binder", ctx.ref("transvector_binder", "item"))
