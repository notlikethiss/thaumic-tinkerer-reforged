TRAVEL = "thaumcraft:block/paving_stone_travel"
WARDING = "thaumcraft:block/paving_stone_warding"


def slab(ctx, name, texture):
    textures = {"bottom": texture, "top": texture, "side": texture}
    ctx.block_model(name, {"parent": "minecraft:block/slab", "textures": textures})
    ctx.block_model(f"{name}_top", {"parent": "minecraft:block/slab_top", "textures": textures})
    ctx.block_model(f"{name}_double", {"parent": "minecraft:block/cube_all", "textures": {"all": texture}})
    ctx.blockstate(name, {"variants": {
        "type=bottom": {"model": ctx.ref(name)},
        "type=top": {"model": ctx.ref(f"{name}_top")},
        "type=double": {"model": ctx.ref(f"{name}_double")},
    }})
    ctx.block_item(name)
    ctx.slab_drop(name)


def stairs(ctx, name, texture):
    textures = {"bottom": texture, "top": texture, "side": texture}
    for suffix, parent in (("", "stairs"), ("_inner", "inner_stairs"), ("_outer", "outer_stairs")):
        ctx.block_model(name + suffix, {"parent": f"minecraft:block/{parent}", "textures": textures})
    ctx.stairs_state(name)
    ctx.block_item(name)
    ctx.self_drop(name)


def generate(ctx):
    slab(ctx, "travel_slab", TRAVEL)
    stairs(ctx, "travel_stairs", TRAVEL)
    slab(ctx, "ward_slab", WARDING)

    ctx.mineable("pickaxe", "travel_slab", "travel_stairs", "ward_slab")
    ctx.tag_both("minecraft:slabs", "travel_slab", "ward_slab")
    ctx.tag_both("minecraft:stairs", "travel_stairs")

    ctx.shaped(
        "travel_slab", "thaumictinkerer:travel_slab", 6,
        ["QQQ"],
        {"Q": "thaumcraft:travel_paving_stone"},
        "building",
    )
    ctx.shaped(
        "travel_stairs", "thaumictinkerer:travel_stairs", 4,
        ["  Q", " QQ", "QQQ"],
        {"Q": "thaumcraft:travel_paving_stone"},
        "building",
    )
    ctx.shaped(
        "ward_slab", "thaumictinkerer:ward_slab", 6,
        ["QQQ"],
        {"Q": "thaumcraft:obsidian_totem"},
        "building",
    )
