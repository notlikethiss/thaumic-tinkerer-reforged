def generate(ctx):
    side = {"uv": [0, 14, 16, 16], "texture": "#side"}
    ctx.block_model("essentia_funnel", {
        "textures": {"top": ctx.ref("funnel0"), "side": ctx.ref("funnel1"), "particle": ctx.ref("funnel1")},
        "elements": [{
            "from": [0, 0, 0],
            "to": [16, 2, 16],
            "faces": {
                "down": {"texture": "#top"},
                "up": {"texture": "#top"},
                "north": side,
                "south": side,
                "west": side,
                "east": side,
            },
        }],
    })
    ctx.simple_state("essentia_funnel")
    ctx.block_item("essentia_funnel")
    ctx.self_drop("essentia_funnel")
    ctx.mineable("pickaxe", "essentia_funnel")
