FIRES = (
    ("aer", "fireair", "aerfire"),
    ("perditio", "firechaos", "perditiofire"),
    ("terra", "fireearth", "terrafire"),
    ("ignis", "firefire", "ignisfire"),
    ("ordo", "fireorder", "ordofire"),
    ("aqua", "firewater", "aquafire"),
)

POTIONS = (
    ("aer", "potion_aer"),
    ("ignis", "potion_ignis"),
    ("terra", "potion_terra"),
    ("aqua", "potion_aqua"),
)

NONE_ARE_FALSE = {"east": "false", "north": "false", "south": "false", "up": "false", "west": "false"}


def variants(prefix, parts, rotation=None):
    entries = []
    for part in parts:
        entry = {"model": prefix + part}
        if rotation:
            entry["y"] = rotation
        entries.append(entry)
    return entries


def fire_state(model):
    side_parts = ("side0", "side1", "side_alt0", "side_alt1")
    up_parts = ("up0", "up1", "up_alt0", "up_alt1")
    prefix = f"thaumictinkerer:block/{model}_"
    multipart = [{"apply": variants(prefix, ("floor0", "floor1")), "when": NONE_ARE_FALSE}]
    for side, rotation in (("north", None), ("east", 90), ("south", 180), ("west", 270)):
        multipart.append({
            "apply": variants(prefix, side_parts, rotation),
            "when": {"OR": [{side: "true"}, NONE_ARE_FALSE]},
        })
    multipart.append({"apply": variants(prefix, up_parts), "when": {"up": "true"}})
    return {"multipart": multipart}


def fire_models(ctx, model, layer):
    for part, parent in (
        ("floor", "template_fire_floor"),
        ("side", "template_fire_side"),
        ("side_alt", "template_fire_side_alt"),
        ("up", "template_fire_up"),
        ("up_alt", "template_fire_up_alt"),
    ):
        for index in (0, 1):
            ctx.block_model(f"{model}_{part}{index}", {
                "parent": f"minecraft:block/{parent}",
                "textures": {"fire": ctx.ref(f"{layer}_layer_{index}")},
            })


def generate(ctx):
    for element, layer, icon in FIRES:
        name = f"{element}_imbued_fire"
        fire_models(ctx, name, layer)
        ctx.blockstate(name, fire_state(name))
        ctx.generated_item(name, icon, texture_kind="block")
        ctx.block_tags["minecraft:fire"].add(f"{ctx.NS}:{name}")

    ctx.block_model("forcefield", {"parent": "minecraft:block/block", "textures": {"particle": "thaumcraft:block/blank"}})
    ctx.simple_state("forcefield")

    for element, texture in POTIONS:
        ctx.generated_item(f"{element}_infused_potion", texture)
