def gas_block(ctx, name):
    ctx.block_model(name, {"parent": "minecraft:block/block", "textures": {"particle": "thaumcraft:block/blank"}})
    ctx.simple_state(name)
    ctx.block_item(name)


def generate(ctx):
    gas_block(ctx, "gaseous_light")
    gas_block(ctx, "gaseous_shadow")
    gas_block(ctx, "nitor_gas")

    ctx.generated_item("gaseous_illuminae", "gaseouslightitem")
    ctx.generated_item("gaseous_tenebrae", "gaseousshadowitem")
    ctx.generated_item("fume_dissipator", "gasremover")
    ctx.generated_item("bright_nitor", "brightnitor")
