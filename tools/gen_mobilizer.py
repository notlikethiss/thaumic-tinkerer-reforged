def generate(ctx):
    for name in ("levitational_locomotive", "levitational_locomotive_relay"):
        ctx.cube_bottom_top(name, f"{name}1", f"{name}0", f"{name}2")
        ctx.simple_state(name)
        ctx.block_item(name)
        ctx.self_drop(name)
        ctx.mineable("pickaxe", name)
