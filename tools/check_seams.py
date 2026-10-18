import argparse
import re
import sys
from pathlib import Path

SOURCE_DIR = Path("src") / "main" / "java" / "thaumictinkerer"

PLATFORM = ("platform/", "client/platform/")
CLIENT_PLATFORM = ("client/platform/",)
GEOM = "client/geom/"
CLIENT = "client/"

OUTSIDE_PLATFORM = [
    "net.neoforged.neoforge.network.*",
    "net.neoforged.neoforge.client.network.*",
    "net.neoforged.neoforge.attachment.*",
    "net.neoforged.neoforge.transfer.*",
    "net.neoforged.neoforge.capabilities.*",
    "net.minecraft.core.component.DataComponentType",
    "net.minecraft.world.level.portal.TeleportTransition",
    "net.minecraft.network.protocol.game.ClientboundChunksBiomesPacket",
    "net.minecraft.world.item.ToolMaterial",
    "net.minecraft.world.item.equipment.*",
    "net.minecraft.world.item.component.Consumable",
    "net.minecraft.world.item.component.Consumables",
    "net.minecraft.world.item.consume_effects.*",
]

OUTSIDE_CLIENT_PLATFORM = [
    "com.mojang.renderpearl.*",
    "net.minecraft.client.renderer.RenderPipelines",
    "net.minecraft.client.renderer.rendertype.*",
]

GEOM_ALLOWED = ["java.*", "org.joml.*", "thaumictinkerer.client.geom.*"]

OUTSIDE_CLIENT = [
    "net.minecraft.client.*",
    "thaumictinkerer.client.*",
    "thaumcraft.client.*",
    "com.mojang.blaze3d.*",
    "net.neoforged.neoforge.client.*",
]
OUTSIDE_CLIENT_EXCEPT_FILES = ("ThaumicTinkerer.java",)

IMPORT_PATTERN = re.compile(r"^\s*import\s+(?:static\s+)?([\w.]+(?:\.\*)?)\s*;")


def matches(imported, pattern):
    if pattern.endswith(".*"):
        return imported.startswith(pattern[:-1])
    return imported == pattern or imported.startswith(pattern + ".")


def matches_any(imported, patterns):
    return any(matches(imported, pattern) for pattern in patterns)


def violated_rule(relative, imported):
    if relative in ("platform/TtCamoBlockEntity.java", "platform/TtCropBlockEntity.java") and imported in ("net.neoforged.neoforge.client.model.data.ModelData", "net.neoforged.neoforge.client.model.data.ModelProperty"):
        return None
    in_platform = relative.startswith(PLATFORM)
    in_client_platform = relative.startswith(CLIENT_PLATFORM)
    in_client = relative.startswith(CLIENT)
    if not in_platform and matches_any(imported, OUTSIDE_PLATFORM):
        return "outside platform/ and client/platform/"
    if not in_client_platform and matches_any(imported, OUTSIDE_CLIENT_PLATFORM):
        return "outside client/platform/"
    if relative.startswith(GEOM) and not matches_any(imported, GEOM_ALLOWED):
        return "client/geom/ allows only java.*, org.joml.*, thaumictinkerer.client.geom.*"
    if not in_client and relative not in OUTSIDE_CLIENT_EXCEPT_FILES and matches_any(imported, OUTSIDE_CLIENT):
        return "outside client/"
    return None


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    arguments = parser.parse_args()
    source = Path(arguments.root) / SOURCE_DIR
    violations = []
    files = 0
    for path in sorted(source.rglob("*.java")):
        files += 1
        relative = path.relative_to(source).as_posix()
        for number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
            found = IMPORT_PATTERN.match(line)
            if not found:
                continue
            imported = found.group(1)
            rule = violated_rule(relative, imported)
            if rule:
                violations.append(f"{path}:{number}: {imported} ({rule})")
    for violation in violations:
        print(violation)
    if violations:
        print(f"{len(violations)} seam violations in {files} files")
        return 1
    print(f"seams ok: {files} files checked")
    return 0


if __name__ == "__main__":
    sys.exit(main())
