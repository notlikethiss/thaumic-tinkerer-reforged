import shutil
import subprocess
import sys
import tarfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
URL = "https://codeload.github.com/Thaumic-Tinkerer/ThaumicTinkerer/tar.gz/refs/heads/1.7-final"
CACHE = ROOT / "tools" / "cache"
ARCHIVE = CACHE / "tt-1.7-final.tar.gz"
TARGET = ROOT / "reference" / "tt-src"


def download():
    CACHE.mkdir(parents=True, exist_ok=True)
    if ARCHIVE.exists():
        return
    subprocess.run(["curl", "-fsSL", "-o", str(ARCHIVE), URL], check=True)


def extract():
    if TARGET.exists():
        shutil.rmtree(TARGET)
    TARGET.mkdir(parents=True)
    root = TARGET.resolve()
    count = 0
    with tarfile.open(ARCHIVE) as archive:
        for member in archive.getmembers():
            parts = Path(member.name).parts
            if len(parts) < 2 or not (member.isfile() or member.isdir()):
                continue
            destination = (TARGET / Path(*parts[1:])).resolve()
            if root not in destination.parents and destination != root:
                continue
            if member.isdir():
                destination.mkdir(parents=True, exist_ok=True)
                continue
            destination.parent.mkdir(parents=True, exist_ok=True)
            with archive.extractfile(member) as source, destination.open("wb") as output:
                shutil.copyfileobj(source, output)
            count += 1
    return count


def main():
    download()
    print(f"extracted {extract()} files into {TARGET}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
