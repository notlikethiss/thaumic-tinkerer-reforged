VANILLA = [
    "aqua_affinity", "bane_of_arthropods", "binding_curse", "blast_protection", "breach", "channeling", "density",
    "depth_strider", "efficiency", "feather_falling", "fire_aspect", "fire_protection", "flame", "fortune",
    "frost_walker", "impaling", "infinity", "knockback", "looting", "loyalty", "luck_of_the_sea", "lunge", "lure",
    "mending", "multishot", "piercing", "power", "projectile_protection", "protection", "punch", "quick_charge",
    "respiration", "riptide", "sharpness", "silk_touch", "smite", "soul_speed", "sweeping_edge", "swift_sneak",
    "thorns", "unbreaking", "vanishing_curse", "wind_burst",
]
VANILLA_IDS = [f"minecraft:{name}" for name in VANILLA]
THAUMCRAFT_IDS = ["thaumcraft:haste", "thaumcraft:repair"]

DURABILITY = {"minecraft:unbreaking", "minecraft:mending", "thaumcraft:repair"}

ENCHANTMENTS = {
    "ascent_boost": {"max": 4, "slots": ["legs"], "items": "legs"},
    "slow_fall": {"max": 3, "slots": ["feet"], "items": "feet", "deny": {"minecraft:feather_falling"}},
    "auto_smelt": {"max": 1, "slots": ["mainhand"], "items": "digger", "allow": DURABILITY},
    "desintegrate": {"max": 1, "slots": ["mainhand"], "items": "digger", "allow": DURABILITY},
    "quick_draw": {"max": 2, "slots": ["mainhand"], "items": "bow", "deny": {"minecraft:punch"}},
    "vampirism": {"max": 2, "slots": ["mainhand"], "items": "sword", "deny": {"minecraft:fire_aspect", "minecraft:knockback"}},
    "dispersed_strike": {"max": 5, "slots": ["mainhand"], "items": "sword", "allow": DURABILITY | {"minecraft:knockback", "minecraft:looting"}},
    "shatter": {"max": 5, "slots": ["mainhand"], "items": "digger", "deny": {"minecraft:efficiency", "thaumictinkerer:desintegrate"}},
    "final_strike": {"max": 5, "slots": ["mainhand"], "items": "sword", "allow": DURABILITY | {"minecraft:sharpness", "minecraft:smite"}},
    "focused_strike": {"max": 5, "slots": ["mainhand"], "items": "sword", "allow": DURABILITY | {"minecraft:knockback", "minecraft:looting"}},
    "pounce": {"max": 5, "slots": ["legs"], "items": "legs"},
    "shockwave": {"max": 5, "slots": ["feet"], "items": "feet", "deny": {"minecraft:feather_falling", "thaumictinkerer:slow_fall"}},
    "tunnel": {"max": 5, "slots": ["mainhand"], "items": "digger", "deny": {"thaumictinkerer:shatter", "minecraft:efficiency"}},
    "valiance": {"max": 5, "slots": ["mainhand"], "items": "sword", "allow": DURABILITY | {"minecraft:sharpness", "minecraft:smite"}},
}

WOOD_BLOCKS = [
    "#minecraft:logs", "#minecraft:planks", "#minecraft:wooden_doors", "#minecraft:wooden_fences", "#minecraft:fence_gates",
    "#minecraft:wooden_stairs", "#minecraft:wooden_slabs", "#minecraft:wooden_trapdoors", "#minecraft:wooden_pressure_plates",
    "#minecraft:all_signs", "minecraft:bookshelf", "minecraft:chest", "minecraft:trapped_chest", "minecraft:crafting_table",
    "minecraft:jukebox", "minecraft:note_block", "minecraft:daylight_detector",
]


def exclusive_set(name, spec):
    universe = VANILLA_IDS + THAUMCRAFT_IDS + [f"thaumictinkerer:{other}" for other in ENCHANTMENTS]
    own = f"thaumictinkerer:{name}"
    if "allow" in spec:
        return [other for other in universe if other != own and other not in spec["allow"]]
    return [other for other in universe if other != own and other in spec.get("deny", set())]


def enchantments(ctx):
    for name, spec in ENCHANTMENTS.items():
        content = {
            "anvil_cost": 4,
            "description": {"translate": f"enchantment.{ctx.NS}.{name}"},
            "effects": {},
            "max_cost": {"base": 61, "per_level_above_first": 10},
            "max_level": spec["max"],
            "min_cost": {"base": 20, "per_level_above_first": 10},
            "slots": spec["slots"],
            "supported_items": f"#{ctx.NS}:enchantable/{spec['items']}",
            "weight": 1,
        }
        denied = exclusive_set(name, spec)
        if not ctx.profile.modern:
            denied = [value for value in denied if value != "minecraft:lunge"]
        if denied:
            content["exclusive_set"] = denied
        ctx.write(ctx.DATA / ctx.NS / "enchantment" / f"{name}.json", content)


def tags(ctx):
    ns = ctx.NS
    ctx.item_tags[f"{ns}:enchantable/digger"].update(["#minecraft:pickaxes", "#minecraft:axes", "#minecraft:shovels"])
    ctx.item_tags[f"{ns}:enchantable/sword"].add("#minecraft:swords")
    ctx.item_tags[f"{ns}:enchantable/bow"].add("#minecraft:enchantable/bow")
    ctx.item_tags[f"{ns}:enchantable/legs"].add("#minecraft:enchantable/leg_armor")
    ctx.item_tags[f"{ns}:enchantable/feet"].add("#minecraft:enchantable/foot_armor")
    ctx.item_tags[f"{ns}:enchanter_excluded/slow_fall"].add(f"{ns}:boots_of_the_horizontal_shield")
    ctx.item_tags[f"{ns}:enchanter_excluded/shockwave"].add(f"{ns}:boots_of_the_horizontal_shield")
    ctx.item_tags[f"{ns}:enchanter_excluded/auto_smelt"].add("thaumcraft:elemental_axe")
    ctx.item_tags[f"{ns}:no_remove_enchant"]
    ctx.block_tags[f"{ns}:flaming_touch_breakable"].update(WOOD_BLOCKS)


def generate(ctx):
    enchantments(ctx)
    tags(ctx)

    ns = ctx.NS
    ctx.block_model("osmotic_enchanter", {
        "parent": "minecraft:block/block",
        "textures": {
            "particle": ctx.ref("enchanter2"),
            "bottom": ctx.ref("enchanter0"),
            "top": ctx.ref("enchanter1"),
            "side": ctx.ref("enchanter2"),
        },
        "elements": [{
            "from": [0, 0, 0],
            "to": [16, 12, 16],
            "faces": {
                "down": {"uv": [0, 0, 16, 16], "texture": "#bottom", "cullface": "down"},
                "up": {"uv": [0, 0, 16, 16], "texture": "#top"},
                "north": {"uv": [0, 4, 16, 16], "texture": "#side", "cullface": "north"},
                "south": {"uv": [0, 4, 16, 16], "texture": "#side", "cullface": "south"},
                "west": {"uv": [0, 4, 16, 16], "texture": "#side", "cullface": "west"},
                "east": {"uv": [0, 4, 16, 16], "texture": "#side", "cullface": "east"},
            },
        }],
    })
    ctx.simple_state("osmotic_enchanter")
    ctx.block_item("osmotic_enchanter")
    ctx.self_drop("osmotic_enchanter")
    ctx.mineable("pickaxe", "osmotic_enchanter")

    ctx.generated_item("spellbinding_cloth", "spellcloth", tints=[{"type": f"{ns}:spell_cloth"}])

    ctx.write(ctx.DATA / ns / ctx.profile.recipe_folder / "spell_cloth.json", {"type": f"{ns}:spell_cloth"})
