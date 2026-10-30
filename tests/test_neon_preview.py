"""The standalone test APK must not replace Home or gain remote/boot hooks."""
import importlib.util
from pathlib import Path
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
SPEC = importlib.util.spec_from_file_location("check_neon_preview", ROOT / "tools/check-neon-preview.py")
CHECK = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(CHECK)
A = CHECK.ANDROID


def manifest():
    text = (ROOT / "launcher/app/src/neonPreview/AndroidManifest.xml").read_text()
    text = text.replace("${applicationId}", CHECK.PACKAGE)
    root = ET.fromstring(text)
    root.set("package", CHECK.PACKAGE)
    return root


class NeonPreviewTest(unittest.TestCase):
    def test_standalone_manifest_can_be_installed_beside_the_launcher(self):
        CHECK.validate(manifest())

    def test_existing_launcher_identity_is_refused(self):
        root = manifest()
        root.set("package", "com.gathin.tvhome")
        with self.assertRaisesRegex(ValueError, "own application ID"):
            CHECK.validate(root)

    def test_home_and_vendor_home_filters_are_refused(self):
        for category in ("HOME", "COMMON_HOME"):
            with self.subTest(category=category):
                root = manifest()
                intent = root.find("application/activity/intent-filter")
                ET.SubElement(intent, "category", {A + "name": "android.intent.category." + category})
                with self.assertRaisesRegex(ValueError, "register as Home"):
                    CHECK.validate(root)

    def test_remote_and_boot_components_are_refused(self):
        for component in ("service", "receiver", "activity-alias"):
            with self.subTest(component=component):
                root = manifest()
                ET.SubElement(root.find("application"), component, {A + "name": "unexpected"})
                with self.assertRaisesRegex(ValueError, "services, receivers"):
                    CHECK.validate(root)

    def test_secure_settings_permission_is_refused(self):
        root = manifest()
        ET.SubElement(root, "uses-permission", {A + "name": "android.permission.WRITE_SECURE_SETTINGS"})
        with self.assertRaisesRegex(ValueError, "Unexpected permission"):
            CHECK.validate(root)

    def test_original_theme_provider_authority_is_refused(self):
        root = manifest()
        root.find("application/provider").set(A + "authorities", "com.gathin.tvhome.theme")
        with self.assertRaisesRegex(ValueError, "isolated"):
            CHECK.validate(root)

    def test_missing_tv_app_entry_is_refused(self):
        root = manifest()
        intent = root.find("application/activity/intent-filter")
        for category in list(intent.findall("category")):
            intent.remove(category)
        with self.assertRaisesRegex(ValueError, "TV app list"):
            CHECK.validate(root)
