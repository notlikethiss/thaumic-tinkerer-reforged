import shutil

ACTIVE_COMPONENT = "thaumictinkerer:gear_active"


def toggled_item(ctx, name, base_texture, active_texture, parent="minecraft:item/generated"):
    ctx.item_model(name, {"parent": parent, "textures": {"layer0": ctx.ref(base_texture, "item")}})
    ctx.item_model(f"{name}_active", {"parent": parent, "textures": {"layer0": ctx.ref(active_texture, "item")}})
    ctx.write(ctx.ASSETS / "items" / f"{name}.json", {
        "model": {
            "type": "minecraft:condition",
            "property": "minecraft:has_component",
            "component": ACTIVE_COMPONENT,
            "on_true": {"type": "minecraft:model", "model": ctx.ref(f"{name}_active", "item")},
            "on_false": {"type": "minecraft:model", "model": ctx.ref(name, "item")},
        },
    })


def custom_shaped(ctx, name, recipe_type, result, pattern, key):
    content = {"type": recipe_type}
    ctx.profile.recipe_category(content, "misc")
    content["key"] = {symbol: ctx.profile.recipe_ingredient(ingredient) for symbol, ingredient in key.items()}
    content["pattern"] = pattern
    content["result"] = ctx.profile.recipe_result(result, 1, None)
    ctx.recipe(name, content, [result, *key.values()])


def generate(ctx):
    toggled_item(ctx, "cleansing_talisman", "cleansingtalisman0", "cleansingtalisman1")
    toggled_item(ctx, "xp_talisman", "xptalisman0", "xptalisman1")
    toggled_item(ctx, "blood_sword", "bloodsword0", "bloodsword1", "minecraft:item/handheld")

    ctx.generated_item("revealing_helm", "revealinghelm")
    ctx.generated_item("share_tome", "sharebook")

    ctx.item_model("infused_inkwell", {"parent": "minecraft:item/generated", "textures": {"layer0": "thaumcraft:item/inkwell"}})
    ctx.item_definition("infused_inkwell", ctx.ref("infused_inkwell", "item"))

    target = ctx.ASSETS / "textures" / "entity" / "equipment" / "humanoid" / "revealing_helm.png"
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(ctx.SOURCE_TEXTURES / "model" / "revealing.png", target)
    ctx.write(ctx.ASSETS / "equipment" / "revealing_helm.json", {
        "layers": {"humanoid": [{"texture": f"{ctx.NS}:revealing_helm"}]},
    })

    custom_shaped(
        ctx, "share_tome", f"{ctx.NS}:share_tome", "thaumictinkerer:share_tome",
        [" S ", "PTP", " P "],
        {"S": "thaumcraft:scribing_tools", "T": "thaumcraft:thaumonomicon", "P": "minecraft:paper"},
    )
    ctx.shaped(
        "infused_inkwell_refill", "thaumictinkerer:infused_inkwell", 1,
        ["QQQ", "QCQ", "QQQ"],
        {"Q": "minecraft:ink_sac", "C": "thaumictinkerer:infused_inkwell"},
    )
