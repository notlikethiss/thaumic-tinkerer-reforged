FOCI = {
    "focus_shadowbeam": "focusshadowbeam",
    "focus_xp_drain": "focusxpdrain",
    "focus_recall": "focusrecall",
}


def generate(ctx):
    for name, texture in FOCI.items():
        ctx.generated_item(name, texture)
