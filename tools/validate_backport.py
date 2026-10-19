import argparse
import importlib.util
import os
import re
import shutil
import subprocess
from pathlib import Path

import mcformat

ROOT = Path(__file__).resolve().parent.parent
JAVA_VERSIONS = {"26.3": 25, "1.21.1": 21, "1.19.2": 17}


def java_executable(version):
    required = JAVA_VERSIONS[version]
    executable = "java.exe" if os.name == "nt" else "java"
    homes = [os.environ.get(name) for name in [f"JAVA_HOME_{required}_X64", f"JAVA_HOME_{required}_ARM64", "JAVA_HOME"]]
    candidates = [str(Path(home) / "bin" / executable) for home in homes if home]
    if java := shutil.which("java"):
        candidates.append(java)
    for candidate in dict.fromkeys(candidates):
        try:
            result = subprocess.run([candidate, "-version"], capture_output=True, text=True, timeout=10)
        except (OSError, subprocess.TimeoutExpired):
            continue
        match = re.search(r'\bversion\s+"(?:1\.)?(\d+)', result.stdout + result.stderr)
        if result.returncode == 0 and match and int(match.group(1)) >= required:
            return candidate
    raise SystemExit(f"Minecraft {version} reports need Java {required} or newer. Set JAVA_HOME or add Java to PATH.")


def vanilla_inputs(validator, version, cache=None):
    directory = validator.fetch(version).resolve()
    output = ((cache or ROOT / "tools/cache/vanilla-reports") / version).resolve()
    reports = output / "reports"
    if not (reports / "registries.json").is_file():
        output.mkdir(parents=True, exist_ok=True)
        command = [java_executable(version), "-DbundlerMainClass=net.minecraft.data.Main", "-jar", "server.jar", "--reports", "--output", str(output)]
        result = subprocess.run(command, cwd=directory, capture_output=True, text=True)
        if result.returncode != 0 or not (reports / "registries.json").is_file():
            raise SystemExit("Vanilla data generator failed:\n" + result.stdout[-2000:] + result.stderr[-2000:])
    return reports, directory / "server.jar", directory / "client.jar"


def validate(validator, version, out, cache=None):
    reports, server, client = vanilla_inputs(validator, version, cache)
    return validator.Validator(version, out, reports_directory=reports, server_jar=server, client_jar=client).run(None)


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
    problems = validate(validator, profile.name, mcformat.out_root)
    for source, value, expected in problems:
        print(f"{source}: {value} (expected {expected})")
    print(f"{profile.name}: {len(problems)} resource problem(s)")
    raise SystemExit(1 if problems else 0)


if __name__ == "__main__":
    main()
