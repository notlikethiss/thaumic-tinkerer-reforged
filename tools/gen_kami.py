import shutil

RESOURCES = {
    "ichor": "ichor",
    "ichor_cloth": "ichorcloth",
    "ichorium_ingot": "ichorium",
    "ichorium_nugget": "ichornugget",
    "ichorium_cap": "ichorcap",
    "ichorcloth_rod": "ichorclothrod",
    "nether_shard": "nethershard",
    "ender_shard": "endershard",
}

ARMOR = {
    "ichorcloth_cowl": "ichorclothhelm",
    "ichorcloth_robe": "ichorclothchest",
    "ichorcloth_leggings": "ichorclothlegs",
    "ichorcloth_boots": "ichorclothboots",
}

TOOLS = {
    "ichorium_pickaxe": "ichorpick",
    "ichorium_shovel": "ichorshovel",
    "ichorium_axe": "ichoraxe",
    "ichorium_sword": "ichorsword",
}


def generate(ctx):
    for name, texture in RESOURCES.items():
        ctx.generated_item(name, texture)
    for name, texture in ARMOR.items():
        ctx.generated_item(name, texture)
    for name, texture in TOOLS.items():
        ctx.item_model(name, {"parent": "minecraft:item/handheld", "textures": {"layer0": ctx.ref(texture, "item")}})
        ctx.item_definition(name, ctx.ref(name, "item"))

    for layer, source in (("humanoid", "ichor1"), ("humanoid_leggings", "ichor2")):
        target = ctx.ASSETS / "textures" / "entity" / "equipment" / layer / "ichorcloth.png"
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(ctx.SOURCE_TEXTURES / "model" / f"{source}.png", target)
    ctx.write(ctx.ASSETS / "equipment" / "ichorcloth.json", {
        "layers": {
            "humanoid": [{"texture": f"{ctx.NS}:ichorcloth"}],
            "humanoid_leggings": [{"texture": f"{ctx.NS}:ichorcloth"}],
        },
    })

    ctx.shapeless("ichorium_nugget", "thaumictinkerer:ichorium_nugget", 9, ["thaumictinkerer:ichorium_ingot"])
