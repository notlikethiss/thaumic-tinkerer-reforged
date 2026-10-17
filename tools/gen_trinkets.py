ACTIVE_COMPONENT = "thaumictinkerer:ring_active"


def generate(ctx):
    ctx.item_model("block_talisman", {"parent": "minecraft:item/generated", "textures": {"layer0": ctx.ref("blocktalisman0", "item")}})
    ctx.item_model("block_talisman_active", {"parent": "minecraft:item/generated", "textures": {"layer0": ctx.ref("blocktalisman1", "item")}})
    ctx.write(ctx.ASSETS / "items" / "block_talisman.json", {
        "model": {
            "type": "minecraft:condition",
            "property": "minecraft:has_component",
            "component": ACTIVE_COMPONENT,
            "on_true": {"type": "minecraft:model", "model": ctx.ref("block_talisman_active", "item")},
            "on_false": {"type": "minecraft:model", "model": ctx.ref("block_talisman", "item")},
        },
    })

    ctx.generated_item("placement_mirror", ["placementmirror1", "placementmirror0"])
    ctx.generated_item("protoclay", "protoclay")
    ctx.generated_item("ichor_pouch", "ichorpouch")
    ctx.generated_item("cat_amulet", "catamulet")
