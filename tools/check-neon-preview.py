#!/usr/bin/env python3
"""Validate the actual merged APK manifest printed by Android's apkanalyzer."""
import sys
import xml.etree.ElementTree as ET

ANDROID = "{http://schemas.android.com/apk/res/android}"
PACKAGE = "com.crottedepiaf.cleanyourtv.neontest"


def validate(root):
    if root.get("package") != PACKAGE:
        raise ValueError("Neon Test must use its own application ID")
    app = root.find("application")
    if app is None or app.get(ANDROID + "label") != "CleanYourTV Neon Test":
        raise ValueError("Neon Test must have a distinct application label")
    if app.findall("service") or app.findall("receiver") or app.findall("activity-alias"):
        raise ValueError("Neon Test must not declare services, receivers or activity aliases")
    for category in root.iter("category"):
        if category.get(ANDROID + "name") in {
            "android.intent.category.HOME", "android.intent.category.COMMON_HOME"
        }:
            raise ValueError("Neon Test must not register as Home")
    permissions = {p.get(ANDROID + "name") for p in root.findall("uses-permission")}
    if permissions - {"android.permission.ACCESS_NETWORK_STATE", "android.permission.ACCESS_WIFI_STATE"}:
        raise ValueError("Unexpected permission in the standalone test app")
    providers = app.findall("provider")
    if len(providers) != 1 or providers[0].get(ANDROID + "authorities") != PACKAGE + ".theme":
        raise ValueError("Theme provider must be isolated from the installed launcher")
    main = next((a for a in app.findall("activity")
                 if a.get(ANDROID + "name") == "com.gathin.tvhome.MainActivity"), None)
    if main is None or main.get(ANDROID + "exported") != "true":
        raise ValueError("Missing manually launchable MainActivity")
    categories = {c.get(ANDROID + "name") for c in main.iter("category")}
    if "android.intent.category.LEANBACK_LAUNCHER" not in categories:
        raise ValueError("Neon Test must appear in the TV app list")


if __name__ == "__main__":
    try:
        validate(ET.parse(sys.argv[1]).getroot())
    except (ValueError, ET.ParseError, IndexError) as error:
        sys.exit(str(error))
    print("Neon Test: separate package, TV app entry, no HOME or remote/boot services")
