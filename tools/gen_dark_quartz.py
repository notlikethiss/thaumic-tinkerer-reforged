def generate(ctx):
    ctx.cube_bottom_top("dark_quartz_block", "darkquartz1", "darkquartz1", "darkquartz0")
    ctx.simple_state("dark_quartz_block")
    ctx.block_item("dark_quartz_block")
    ctx.self_drop("dark_quartz_block")

    ctx.cube_column("chiseled_dark_quartz_block", "chiseleddarkquartz1", "chiseleddarkquartz0")
    ctx.simple_state("chiseled_dark_quartz_block")
    ctx.block_item("chiseled_dark_quartz_block")
    ctx.self_drop("chiseled_dark_quartz_block")

    ctx.cube_column("dark_quartz_pillar", "pillardarkquartz1", "pillardarkquartz0")
    ctx.blockstate("dark_quartz_pillar", {"variants": {
        "axis=y": {"model": ctx.ref("dark_quartz_pillar")},
        "axis=z": {"model": ctx.ref("dark_quartz_pillar"), "x": 90},
        "axis=x": {"model": ctx.ref("dark_quartz_pillar"), "x": 90, "y": 90},
    }})
    ctx.block_item("dark_quartz_pillar")
    ctx.self_drop("dark_quartz_pillar")

    ctx.slab("dark_quartz_slab", "darkquartz1", "darkquartz0", "dark_quartz_block")
    ctx.stairs("dark_quartz_stairs", "darkquartz1", "darkquartz0")

    ctx.mineable("pickaxe", "dark_quartz_block", "chiseled_dark_quartz_block", "dark_quartz_pillar", "dark_quartz_slab", "dark_quartz_stairs")
    ctx.tag_both("minecraft:slabs", "dark_quartz_slab")
    ctx.tag_both("minecraft:stairs", "dark_quartz_stairs")

    ctx.generated_item("dark_quartz", "darkquartzitem")

    ctx.shaped(
        "dark_quartz_from_coal", "thaumictinkerer:dark_quartz", 4,
        ["Q Q", " C ", "Q Q"],
        {"Q": "minecraft:quartz", "C": "minecraft:coal"},
    )
    ctx.shaped(
        "dark_quartz_from_charcoal", "thaumictinkerer:dark_quartz", 4,
        ["Q Q", " C ", "Q Q"],
        {"Q": "minecraft:quartz", "C": "minecraft:charcoal"},
    )
    ctx.shaped(
        "dark_quartz_block", "thaumictinkerer:dark_quartz_block", 1,
        ["QQ", "QQ"],
        {"Q": "thaumictinkerer:dark_quartz"},
        "building",
    )
    ctx.shaped(
        "dark_quartz_slab", "thaumictinkerer:dark_quartz_slab", 6,
        ["QQQ"],
        {"Q": "thaumictinkerer:dark_quartz_block"},
        "building",
    )
    ctx.shaped(
        "dark_quartz_pillar", "thaumictinkerer:dark_quartz_pillar", 2,
        ["Q", "Q"],
        {"Q": "thaumictinkerer:dark_quartz_block"},
        "building",
    )
    ctx.shaped(
        "chiseled_dark_quartz_block", "thaumictinkerer:chiseled_dark_quartz_block", 1,
        ["Q", "Q"],
        {"Q": "thaumictinkerer:dark_quartz_slab"},
        "building",
    )
    ctx.shaped(
        "dark_quartz_stairs", "thaumictinkerer:dark_quartz_stairs", 4,
        ["  Q", " QQ", "QQQ"],
        {"Q": "thaumictinkerer:dark_quartz_block"},
        "building",
    )
