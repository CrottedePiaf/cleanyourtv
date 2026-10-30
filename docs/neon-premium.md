# Neon Premium — CrottedePiaf fork

Neon Premium is an opt-in native skin: electric blue and violet, glass surfaces, a gradient focus rim,
cached background lights and short transitions. No new dependency, permission, accessibility service,
network access, video or runtime blur is added.

## Activation

Build this fork with `glass_style` set to `neon` in
[`config.xml`](../launcher/app/src/main/res/values/config.xml), or merge `"glass_style": "neon"` into the
TV's **existing** theme JSON. Do not replace a device's configuration with a generic theme: it carries
the household's source and remote mappings. The default remains `glass` for other installations.

The skin reuses the existing dock, grid, app library and quick panel. Existing options can refine the
composition: `cinematic_home`, `dock_glass`, `always_show_names`, `columns`, `rows_below_dock`,
`focus_overshoot` and `ambient_glow`. These composition options remain independent choices; enabling Neon
does not change the user's app order, columns, wallpaper selection or key filtering. Neon takes visual
priority over seasonal decorations while enabled; leaving Neon restores the configured seasonal skin.

Entering, updating and leaving Neon preserve the saved layout, motion, wallpaper, dock opacity and
clock format choices. The normal explicit reset in Launcher settings is still available. Ordinary
non-Neon themes keep their existing reset policy.

## Eight implemented surfaces

1. **Home:** existing clock, shelf and dock over a cached navy/blue/violet backdrop. A chosen wallpaper
   still takes priority; existing cinematic artwork can also cover the backdrop.
2. **Focused selection:** existing hardware-cached GlassView, blue halo and blue-to-violet rim. Existing
   zoom and spring settings stay in control.
3. **App opening:** reuse the 200 ms native scale/fade transition for Neon when motion is enabled.
   External applications and firmware retain control of their own rendering and launch timing.
4. **Navigation:** existing D-pad rules and focus restoration, with matching header and menu selectors.
5. **Dynamic background:** blue/violet ambient lights respond to focused tiles using the existing
   small texture and transition machinery. Local wallpaper rotation and time-of-day backgrounds remain
   available. No continuously running background animation is introduced.
6. **Quick menu:** existing QuickSettingsActivity and Home popups get the glass palette. Their actions,
   task behaviour and dismissal rules remain intact.
7. **Startup:** a short 280 ms staggered tile appearance reuses the existing initial-layout mechanism.
   It is not a replacement for Android's boot animation and adds no blocking splash or launch delay.
   Normal HOME returns retain TileRefresh's existing policy.
8. **Application grid:** existing RecyclerView, adaptive sizing, banners, stable IDs, reorder and library.
   Artwork keeps its original colours; the skin supplies the surrounding surface and focus.

## Motion and performance

Launcher settings' **Animations: Off** disables tile zoom, floating focus movement, panel entrances,
caption fades and the custom app-opening transition. `reduced_motion` keeps brief, bounded movement
and removes staggered entrances. Android's disabled animator setting is also respected on Config load.
System/vendor screens and third-party apps control their own animations.

The backdrop is one 480×270 texture generated once per Home instance. Glass is rendered into its
existing small hardware layer. Transitions use alpha, scale and translation; there is no per-frame
layout animation, full-screen blur or video decoder. Wallpaper loading and memory-pressure behaviour
are retained. Home does not add any background polling.

## Philips TPM191E / Android TV 12 acceptance

The implementation has not yet been checked on a physical TV or compared with the approved mockup.
Use the [device notes](../examples/philips-tpm191e/README.md) as context, then verify on the actual unit:

- Short and held HOME from Home, streaming, Live TV and HDMI; focus returns to the expected first tile.
- Physical Sources, Settings and mapped app buttons; held Volume Up/Down with the current helper setup.
- Cold boot, standby/wake, opening and closing the quick panel, and return from launcher settings.
- All D-pad edges, incomplete final rows, reorder, library, rapid movement and empty app lists.
- Animations on/off, reduced motion, Android animator scale zero, and visible focus in every mode.
- Upgrade, theme activation/update/rollback with pre-existing saved preferences and app order.
- Compare startup, HOME return, frame times and memory against the baseline with `tools/bench.py`.
- Capture each of the eight surfaces on the TV, with identifying information removed, before publishing
  this as a verified theme. No synthetic screenshot is registered in the theme catalogue.

Keep `HomeKeyService`, `InputKeyService`, `key_filter`, `home_long_press_home`, source mappings and
power-on behaviour as configured on the unit. Key filtering can suppress held-volume repeat on this
Philips platform; the skin does not change that trade-off. Injected keys do not validate the physical
remote's accessibility path.

## Local checks

Use JDK 17, Android SDK 34 and build-tools 34.0.0 as described in the repository instructions:

```sh
cd launcher
./gradlew --no-daemon testReleaseUnitTest lintRelease assembleRelease
```

Then run the Python repository checks and confirm the release APK stays below 589,824 bytes. The
new motion-duration and preference-policy tests supplement the existing navigation, HOME, wallpaper
and order tests; they do not establish physical-TV compatibility or visual fidelity.
