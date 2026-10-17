TOOLS = {
    "awakened_ichorium_pickaxe": "ichorpickgem",
    "awakened_ichorium_shovel": "ichorshovelgem",
    "awakened_ichorium_axe": "ichoraxegem",
    "awakened_ichorium_sword": "ichorswordgem",
}


def generate(ctx):
    for name, texture in TOOLS.items():
        for mode in range(3):
            ctx.item_model(f"{name}_{mode}", {"parent": "minecraft:item/handheld", "textures": {"layer0": ctx.ref(f"{texture}{mode}", "item")}})
        ctx.write(ctx.ASSETS / "items" / f"{name}.json", {
            "model": {
                "type": "minecraft:range_dispatch",
                "property": "minecraft:custom_model_data",
                "index": 0,
                "entries": [
                    {"threshold": float(mode), "model": {"type": "minecraft:model", "model": ctx.ref(f"{name}_{mode}", "item")}}
                    for mode in range(1, 3)
                ],
                "fallback": {"type": "minecraft:model", "model": ctx.ref(f"{name}_0", "item")},
            },
        })
