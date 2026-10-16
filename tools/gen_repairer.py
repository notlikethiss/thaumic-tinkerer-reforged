FACINGS = ("north", "south", "west", "east")


def generate(ctx):
    name = "thaumic_restorer"
    particle = {"parent": "minecraft:block/block", "textures": {"particle": "thaumcraft:block/thaumiumblock"}}
    ctx.block_model(name, particle)
    ctx.blockstate(name, {"variants": {f"facing={facing}": {"model": ctx.ref(name)} for facing in FACINGS}})
    ctx.item_model(name, particle)
    ctx.write(ctx.ASSETS / "items" / f"{name}.json", {
        "model": {"type": "minecraft:special", "base": ctx.ref(name, "item"), "model": {"type": f"{ctx.NS}:thaumic_restorer"}},
    })
    ctx.self_drop(name)
    ctx.mineable("pickaxe", name)
