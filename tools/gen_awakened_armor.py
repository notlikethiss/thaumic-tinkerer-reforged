import shutil

ARMOR = {
    "cowl_of_the_abyssal_depths": "ichorclothhelmgem",
    "robes_of_the_stratosphere": "ichorclothchestgem",
    "leggings_of_the_burning_mantle": "ichorclothlegsgem",
    "boots_of_the_horizontal_shield": "ichorclothbootsgem",
}


def generate(ctx):
    for name, texture in ARMOR.items():
        ctx.generated_item(name, texture)
    for name, tag in zip(ARMOR, ("head_armor", "chest_armor", "leg_armor", "foot_armor"), strict=True):
        ctx.item_tags[f"minecraft:{tag}"].add(f"{ctx.NS}:{name}")

    for layer, source in (("humanoid", "ichorgem1"), ("humanoid_leggings", "ichorgem2")):
        target = ctx.ASSETS / "textures" / "entity" / "equipment" / layer / "ichorcloth_gem.png"
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(ctx.SOURCE_TEXTURES / "model" / f"{source}.png", target)
    ctx.write(ctx.ASSETS / "equipment" / "ichorcloth_gem.json", {
        "layers": {
            "humanoid": [{"texture": f"{ctx.NS}:ichorcloth_gem"}],
            "humanoid_leggings": [{"texture": f"{ctx.NS}:ichorcloth_gem"}],
        },
    })
