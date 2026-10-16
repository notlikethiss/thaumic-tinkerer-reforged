FOCI = {
    "focus_flight": "focusflight",
    "focus_dislocation": "focusdislocation",
    "focus_telekinesis": "focustelekinesis",
    "focus_smelt": "focussmelt",
    "focus_heal": "focusheal",
    "focus_ender_chest": "focusenderchest",
    "focus_deflect": "focusdeflect",
}


def generate(ctx):
    for name, texture in FOCI.items():
        ctx.generated_item(name, texture)
