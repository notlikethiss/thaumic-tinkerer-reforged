import argparse
import importlib.util
from pathlib import Path

import mcformat

ROOT = Path(__file__).resolve().parent.parent


def matching_base(explicit):
    if explicit:
        return Path(explicit).resolve()
    for directory in ROOT.parents:
        base = directory / "thaumcraft-4-reforged"
        for candidate in (base, base / ".worktrees" / f"mc-{mcformat.gradle_version()}", directory / f"thaumcraft-4-reforged-{mcformat.gradle_version()}"):
            properties = candidate / "gradle.properties"
            if properties.exists() and f"minecraft_version={mcformat.gradle_version()}" in properties.read_text().splitlines():
                return candidate
    raise SystemExit("No matching base checkout; pass --base /path/to/thaumcraft")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--base")
    args = parser.parse_args()
    profile = mcformat.init([])
    base = matching_base(args.base)
    source = base / "tools" / "validate_ids.py"
    spec = importlib.util.spec_from_file_location("tc_validate_ids", source)
    validator = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(validator)
    problems = validator.Validator(profile.name, mcformat.out_root).run(None)
    for source, value, expected in problems:
        print(f"{source}: {value} (expected {expected})")
    print(f"{profile.name}: {len(problems)} resource problem(s)")
    raise SystemExit(1 if problems else 0)


if __name__ == "__main__":
    main()
