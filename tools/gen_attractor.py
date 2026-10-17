def attractor(ctx, name, corporeal):
    ctx.block_model(name, {"parent": "minecraft:block/block", "textures": {"particle": "minecraft:block/oak_log"}})
    ctx.blockstate(name, {"variants": {
        "pull=true": {"model": ctx.ref(name)},
        "pull=false": {"model": ctx.ref(name)},
    }})
    ctx.item_model(name, {"parent": "minecraft:block/block", "textures": {"particle": "minecraft:block/oak_log"}})
    ctx.write(ctx.ASSETS / "items" / f"{name}.json", {
        "model": {
            "type": "minecraft:special",
            "base": ctx.ref(name, "item"),
            "model": {"type": f"{ctx.NS}:attractor", "corporeal": corporeal},
        },
    })
    ctx.self_drop(name)


def generate(ctx):
    attractor(ctx, "kinetic_attractor", False)
    attractor(ctx, "corporeal_attractor", True)
    ctx.mineable("pickaxe", "kinetic_attractor", "corporeal_attractor")
    ctx.generated_item("soul_mould", "soulmould")
