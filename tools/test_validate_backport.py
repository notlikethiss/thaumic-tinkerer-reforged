import json
import os
import subprocess
import tempfile
import unittest
from pathlib import Path
from types import SimpleNamespace
from unittest.mock import Mock, patch

from validate_backport import java_executable, validate, vanilla_inputs


class JavaSelectionTests(unittest.TestCase):
    def version(self, major):
        return subprocess.CompletedProcess([], 0, "", f'openjdk version "{major}.0.1"')

    @patch("validate_backport.shutil.which", return_value=None)
    def test_actions_jdk_selected_before_gradle_jdk(self, which):
        with patch.dict(os.environ, {"JAVA_HOME_21_X64": "/opt/hostedtoolcache/jdk/21/x64", "JAVA_HOME": "/opt/hostedtoolcache/jdk/25/x64"}, clear=True):
            with patch("validate_backport.subprocess.run", return_value=self.version(21)) as run:
                java = java_executable("1.21.1")
        self.assertEqual(str(Path("/opt/hostedtoolcache/jdk/21/x64/bin") / ("java.exe" if os.name == "nt" else "java")), java)
        self.assertEqual([java, "-version"], run.call_args.args[0])

    @patch("validate_backport.shutil.which", return_value=None)
    def test_java_home_with_spaces(self, which):
        with patch.dict(os.environ, {"JAVA_HOME": "/test/JDK with spaces"}, clear=True):
            with patch("validate_backport.subprocess.run", return_value=self.version(25)):
                self.assertEqual(str(Path("/test/JDK with spaces/bin") / ("java.exe" if os.name == "nt" else "java")), java_executable("1.21.1"))

    @patch("validate_backport.shutil.which", return_value="/usr/bin/java")
    def test_unavailable_home_falls_back_to_path(self, which):
        with patch.dict(os.environ, {"JAVA_HOME": "/missing/jdk"}, clear=True):
            with patch("validate_backport.subprocess.run", side_effect=[FileNotFoundError(), self.version(21)]):
                self.assertEqual("/usr/bin/java", java_executable("1.21.1"))

    @patch("validate_backport.shutil.which", return_value="/usr/bin/java")
    def test_old_java_reports_requirement(self, which):
        with patch.dict(os.environ, {}, clear=True):
            with patch("validate_backport.subprocess.run", return_value=self.version(17)):
                with self.assertRaisesRegex(SystemExit, "need Java 21 or newer.*JAVA_HOME"):
                    java_executable("1.21.1")


class VanillaReportTests(unittest.TestCase):
    def test_uncached_reports_passed_explicitly_to_legacy_validator(self):
        with tempfile.TemporaryDirectory(prefix="tt validation ") as temporary:
            root = Path(temporary).resolve()
            directory = root / "minecraft"
            directory.mkdir()
            base = SimpleNamespace(fetch=Mock(return_value=directory), Validator=Mock())
            base.Validator.return_value.run.return_value = []
            cache = root / "empty cache"

            def generate(command, **kwargs):
                self.assertEqual("/linux/jdk/bin/java", command[0])
                self.assertEqual(directory, kwargs["cwd"])
                self.assertIn("-DbundlerMainClass=net.minecraft.data.Main", command)
                output = Path(command[command.index("--output") + 1])
                self.assertTrue(output.is_absolute())
                reports = output / "reports"
                reports.mkdir()
                (reports / "registries.json").write_text(json.dumps({"minecraft:item": {"entries": {}}}))
                return subprocess.CompletedProcess(command, 0, "", "")

            with patch("validate_backport.java_executable", return_value="/linux/jdk/bin/java"):
                with patch("validate_backport.subprocess.run", side_effect=generate):
                    self.assertEqual([], validate(base, "1.21.1", root / "resources", cache))
            args, kwargs = base.Validator.call_args
            self.assertEqual(("1.21.1", root / "resources"), args)
            self.assertTrue((kwargs["reports_directory"] / "registries.json").is_file())
            self.assertEqual(directory / "server.jar", kwargs["server_jar"])
            self.assertEqual(directory / "client.jar", kwargs["client_jar"])
            base.Validator.return_value.run.assert_called_once_with(None)

    def test_cached_reports_need_no_java(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary).resolve()
            reports = root / "cache/1.21.1/reports"
            reports.mkdir(parents=True)
            (reports / "registries.json").write_text("{}")
            base = SimpleNamespace(fetch=Mock(return_value=root))
            with patch("validate_backport.java_executable", side_effect=AssertionError("cached reports should not launch Java")):
                self.assertEqual((reports, root / "server.jar", root / "client.jar"), vanilla_inputs(base, "1.21.1", root / "cache"))

    def test_failed_or_missing_reports_are_rejected(self):
        for status in [0, 1]:
            with self.subTest(status=status), tempfile.TemporaryDirectory() as temporary:
                root = Path(temporary).resolve()
                base = SimpleNamespace(fetch=Mock(return_value=root), Validator=Mock())
                with patch("validate_backport.java_executable", return_value="java"):
                    with patch("validate_backport.subprocess.run", return_value=subprocess.CompletedProcess([], status, "", "datagen failed")):
                        with self.assertRaisesRegex(SystemExit, "Vanilla data generator failed"):
                            validate(base, "1.21.1", root / "resources", root / "cache")
                base.Validator.assert_not_called()


if __name__ == "__main__":
    unittest.main()
