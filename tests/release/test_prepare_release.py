import hashlib
import importlib.util
import json
from pathlib import Path
import tempfile
import unittest

spec = importlib.util.spec_from_file_location(
    "prepare_release", Path(__file__).resolve().parents[2] / "scripts/prepare-release.py")
release = importlib.util.module_from_spec(spec)
spec.loader.exec_module(release)


class ReleaseBundleTest(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary.cleanup)
        self.root = Path(self.temporary.name)
        self.targets = ["1.21.1", "1.21.11", "26.2", "26.3"]
        self.fixture("0.1.0")

    def fixture(self, version):
        (self.root / "gradle.properties").write_text(
            f"mod_version={version}\nmod_name=HUD Visibility\n"
            "archives_base_name=hud-visibility\nmod_id=hud_visibility\n", encoding="utf-8")
        (self.root / "CHANGELOG.md").write_text(
            f"# Changelog\n\n## {version}\n\n- Independent visibility controls.\n", encoding="utf-8")
        distribution = self.root / "build/distributions"
        distribution.mkdir(parents=True, exist_ok=True)
        for previous in distribution.iterdir():
            previous.unlink()
        for minecraft in self.targets:
            for loader in ("fabric", "neoforge"):
                (distribution / f"hud-visibility-{version}+{minecraft}-{loader}.jar").write_bytes(
                    f"verified {minecraft} {loader}".encode())
        (self.root / "build/target-manifest.json").write_text(json.dumps({
            "modId": "hud_visibility", "version": version, "targets": self.targets}), encoding="utf-8")

    def test_packages_every_target_with_exact_dependencies_and_checksums(self):
        result = release.prepare(self.root, "v0.1.0", "source-commit")
        self.assertEqual(8, len(result["artifacts"]))
        self.assertEqual("source-commit", result["source_commit"])
        self.assertFalse(result["prerelease"])
        output = self.root / "build/release"
        self.assertEqual(8, len(list(output.glob("*.jar"))))
        for entry in result["artifacts"]:
            self.assertEqual(entry["sha256"], hashlib.sha256((output / entry["file"]).read_bytes()).hexdigest())
            self.assertIn("cloth-config(required)", entry["dependencies"])
            self.assertEqual(entry["loader"] == "fabric", "fabric-api(required)" in entry["dependencies"])
        self.assertEqual(8, len((output / "SHA256SUMS").read_text().splitlines()))
        self.assertIn("Cloth Config", (output / "release-notes.md").read_text(encoding="utf-8"))

    def test_rejects_mismatched_tag_without_touching_an_existing_bundle(self):
        release.prepare(self.root)
        original = (self.root / "build/release/manifest.json").read_bytes()
        with self.assertRaisesRegex(ValueError, "Release tag"):
            release.prepare(self.root, "v0.2.0")
        self.assertEqual(original, (self.root / "build/release/manifest.json").read_bytes())

    def test_rejects_missing_and_unexpected_jars(self):
        distribution = self.root / "build/distributions"
        jar = next(distribution.glob("*.jar"))
        data = jar.read_bytes()
        jar.unlink()
        with self.assertRaisesRegex(ValueError, "matrix mismatch"):
            release.prepare(self.root)
        jar.write_bytes(data)
        (distribution / "stale-sources.jar").write_bytes(b"stale")
        with self.assertRaisesRegex(ValueError, "matrix mismatch"):
            release.prepare(self.root)

    def test_rejects_a_stale_generated_target_manifest(self):
        path = self.root / "build/target-manifest.json"
        manifest = json.loads(path.read_text())
        manifest["version"] = "0.0.9"
        path.write_text(json.dumps(manifest))
        with self.assertRaisesRegex(ValueError, "Stale target manifest"):
            release.prepare(self.root)

    def test_extracts_only_the_current_nonempty_changelog_section(self):
        text = "# Changelog\n\n## [Unreleased]\n\n- Next.\n\n## [0.1.0] - 2026-10-28\n\n- Current.\n\n## [0.0.9]\n\n- Old.\n"
        self.assertEqual("- Current.", release.changelog_section(text, "0.1.0"))
        with self.assertRaisesRegex(ValueError, "first released"):
            release.changelog_section(text, "0.0.9")
        with self.assertRaisesRegex(ValueError, "empty"):
            release.changelog_section("## 0.1.0\n\n## 0.0.9\n- Old", "0.1.0")

    def test_reruns_replace_only_generated_files_and_classify_prereleases(self):
        for version, channel in [("0.1.0-alpha.1", "alpha"), ("0.1.0-rc.1", "beta")]:
            self.fixture(version)
            first = release.prepare(self.root, f"v{version}")
            second = release.prepare(self.root, f"v{version}")
            self.assertEqual(channel, second["channel"])
            self.assertTrue(second["prerelease"])
            self.assertEqual(first, second)
            self.assertEqual(8, len(list((self.root / "build/release").glob("*.jar"))))


if __name__ == "__main__":
    unittest.main()
