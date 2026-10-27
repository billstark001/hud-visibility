#!/usr/bin/env python3
"""Validate and package already verified JARs; never upload or publish anything."""

import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess


VERSION = re.compile(r"\d+\.\d+\.\d+(?:-[0-9A-Za-z]+(?:[.-][0-9A-Za-z]+)*)?")


def changelog_section(text, version):
    headings = list(re.finditer(r"^## (?:\[([^\]]+)\]|(\S+))[^\n]*$", text, re.M))
    released = [h for h in headings if (h[1] or h[2]) != "Unreleased"]
    if not released or (released[0][1] or released[0][2]) != version:
        raise ValueError("The first released changelog section must match mod_version")
    heading = released[0]
    end = next((h.start() for h in headings if h.start() > heading.start()), len(text))
    body = text[heading.end():end].strip()
    if not body:
        raise ValueError("The release changelog is empty")
    return body


def prepare(root, tag=None, source_commit=""):
    root = Path(root).resolve()
    properties = {}
    for line in (root / "gradle.properties").read_text(encoding="utf-8").splitlines():
        if "=" in line and not line.lstrip().startswith("#"):
            key, value = line.split("=", 1)
            if key in properties:
                raise ValueError(f"Duplicate Gradle property: {key}")
            properties[key] = value.strip()
    version = properties["mod_version"]
    tag = tag or f"v{version}"
    if not VERSION.fullmatch(version) or tag != f"v{version}":
        raise ValueError("Release tag must be v<mod_version>, using a semantic version")
    name = properties["mod_name"]
    base = properties["archives_base_name"]
    if not re.fullmatch(r"[a-z0-9][a-z0-9_-]*", base):
        raise ValueError("Invalid archive base name")
    manifest = json.loads((root / "build/target-manifest.json").read_text(encoding="utf-8"))
    if manifest["version"] != version or manifest["modId"] != properties["mod_id"]:
        raise ValueError("Stale target manifest; run writeTargetManifest again")
    targets = manifest["targets"]
    if not targets or len(targets) != len(set(targets)):
        raise ValueError("The target matrix is empty or contains duplicates")
    entries = []
    for minecraft in targets:
        if not re.fullmatch(r"\d+(?:\.\d+){1,2}(?:-[a-z0-9-]+)?", minecraft):
            raise ValueError(f"Invalid Minecraft target: {minecraft}")
        for loader in ("fabric", "neoforge"):
            entries.append({
                "minecraft": minecraft,
                "loader": loader,
                "file": f"{base}-{version}+{minecraft}-{loader}.jar",
                "dependencies": "cloth-config(required)" + (
                    "\nfabric-api(required)\nmodmenu(optional)" if loader == "fabric" else ""),
            })
    distribution = root / "build/distributions"
    actual = {p.name for p in distribution.glob("*.jar")}
    expected = {entry["file"] for entry in entries}
    if actual != expected:
        raise ValueError(f"Distribution matrix mismatch: missing={expected-actual}, extra={actual-expected}")
    notes = changelog_section((root / "CHANGELOG.md").read_text(encoding="utf-8"), version)
    notes += "\n\n## Installation\n\nChoose the JAR matching your exact Minecraft version and loader. "
    notes += "Install [Cloth Config](https://modrinth.com/mod/cloth-config) for both loaders, "
    notes += "and [Fabric API](https://modrinth.com/mod/fabric-api) on Fabric. "
    notes += "Configure through **Options → HUD Visibility**, Mod Menu, or NeoForge's Mods screen.\n\n"
    notes += "Supported Minecraft versions: " + ", ".join(targets) + ". Fabric and NeoForge; client side only.\n"
    notes += "English, 简体中文, 繁體中文, 日本語. Shader-pack compatibility has not been tested.\n"

    output = root / "build/release"
    if output.is_symlink() or output.resolve().parent != (root / "build").resolve():
        raise ValueError("Refusing a redirected release directory")
    output.mkdir(parents=True, exist_ok=True)
    for previous in output.iterdir():
        if previous.is_symlink() or not previous.is_file() or not (
            previous.name in {"release-notes.md", "manifest.json", "SHA256SUMS"}
            or previous.name.startswith(base + "-") and previous.suffix == ".jar"
        ):
            raise ValueError(f"Unexpected file in the generated release directory: {previous.name}")
    for previous in output.iterdir():
        previous.unlink()
    for entry in entries:
        source = distribution / entry["file"]
        shutil.copyfile(source, output / entry["file"])
        entry["sha256"] = hashlib.sha256(source.read_bytes()).hexdigest()
    suffix = version.partition("-")[2].lower()
    channel = "release" if not suffix else "alpha" if "alpha" in suffix else "beta"
    metadata = {"version": version, "tag": tag, "release_name": f"{name} {tag}",
                "channel": channel, "prerelease": bool(suffix), "source_commit": source_commit,
                "artifacts": entries}
    (output / "release-notes.md").write_text(notes, encoding="utf-8", newline="\n")
    (output / "manifest.json").write_text(json.dumps(metadata, indent=2) + "\n", encoding="utf-8", newline="\n")
    (output / "SHA256SUMS").write_text("".join(
        f"{entry['sha256']}  {entry['file']}\n" for entry in sorted(entries, key=lambda e: e["file"])
    ), encoding="utf-8", newline="\n")
    return metadata


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--tag", help="Must exactly match v<mod_version>")
    args = parser.parse_args()
    root = Path(__file__).resolve().parent.parent
    commit = subprocess.check_output(["git", "rev-parse", "HEAD"], cwd=root, text=True).strip()
    try:
        metadata = prepare(root, args.tag, commit)
    except (ValueError, KeyError, OSError) as error:
        parser.exit(1, f"Release validation failed: {error}\n")
    if os.environ.get("GITHUB_OUTPUT"):
        with open(os.environ["GITHUB_OUTPUT"], "a", encoding="utf-8", newline="\n") as output:
            for key in ("version", "tag", "release_name", "channel"):
                output.write(f"{key}={metadata[key]}\n")
            output.write("prerelease=" + str(metadata["prerelease"]).lower() + "\n")
            matrix = {"include": [{k: e[k] for k in ("minecraft", "loader", "file", "dependencies")}
                                  for e in metadata["artifacts"]]}
            output.write("matrix=" + json.dumps(matrix, separators=(",", ":")) + "\n")
    print(f"Prepared {metadata['tag']}: {len(metadata['artifacts'])} verified JARs in build/release")


if __name__ == "__main__":
    main()
