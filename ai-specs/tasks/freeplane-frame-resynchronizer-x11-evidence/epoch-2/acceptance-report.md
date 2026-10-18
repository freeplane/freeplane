# X11 Popup Calibration Acceptance Evidence

This evidence was captured from the integration worktree at `DISPLAY=:1`. The canonical probe passes for the exercised XWayland runtime. Native X11 KWin runtime acceptance is explicitly unverified; see [LIMITATIONS](#limitations).

## Commands

The full regression suite was run with the required Java runtime:

```bash
JAVA_HOME=/home/henry/.sdkman/candidates/java/21.0.8-zulu PATH="$JAVA_HOME/bin:$PATH" gradle :freeplane:test --rerun-tasks
```

The result XML files were counted with:

```bash
python3 - <<'PY'
from pathlib import Path
import xml.etree.ElementTree as ET
root = Path('freeplane/build/test-results/test')
totals = {key: 0 for key in ('tests', 'failures', 'errors', 'skipped')}
files = sorted(root.glob('TEST-*.xml'))
for path in files:
    node = ET.parse(path).getroot()
    for key in totals:
        totals[key] += int(node.attrib.get(key, '0'))
print('xml_files', len(files))
print(totals)
PY
```

The canonical acceptance runner was invoked with:

```bash
JAVA_HOME=/home/henry/.sdkman/candidates/java/21.0.8-zulu PATH="$JAVA_HOME/bin:$PATH" DISPLAY=:1 python3 ai-specs/tasks/freeplane-frame-resynchronizer-x11-evidence/epoch-2/run-acceptance.py
```

The runner compiled the probe with `javac --release 8` against the built Freeplane classes, FlatLaf 3.7.2, and JNA. The exact resolved canonical compile and run commands are preserved in [`logs/commands.txt`](logs/commands.txt). The Java run used:

```bash
/home/henry/.sdkman/candidates/java/21.0.8-zulu/bin/java \
  --add-opens=java.desktop/sun.awt=ALL-UNNAMED \
  --add-opens=java.desktop/sun.awt.X11=ALL-UNNAMED \
  -cp <temporary-probe-classes>:freeplane/build/classes/java/main:<flatlaf-3.7.2.jar>:<jna.jar>:<jna-platform.jar> \
  org.freeplane.core.ui.components.PopupCoordinateAcceptanceProbe
```

The runner used a 90-second process bound, a new process group, and process-group termination on timeout. The final canonical run exited before the timeout and left no probe process behind.

The two falsifiability mutants were generated and run outside the repository in `/tmp`, sequentially on the shared `DISPLAY=:1`. The only mutation was adding the stated offset to the scenario `menu.show(...)` coordinates. The exact mutation, compile template, and run template are preserved in [`logs/falsifiability-commands.txt`](logs/falsifiability-commands.txt). Complete mutant stdout, stderr, and exit records are preserved in:

- [`logs/falsifiability-shift-240-180.stdout`](logs/falsifiability-shift-240-180.stdout), [`logs/falsifiability-shift-240-180.stderr`](logs/falsifiability-shift-240-180.stderr), [`logs/falsifiability-shift-240-180.result.txt`](logs/falsifiability-shift-240-180.result.txt)
- [`logs/falsifiability-shift-3-3.stdout`](logs/falsifiability-shift-3-3.stdout), [`logs/falsifiability-shift-3-3.stderr`](logs/falsifiability-shift-3-3.stderr), [`logs/falsifiability-shift-3-3.result.txt`](logs/falsifiability-shift-3-3.result.txt)

## Runtime identity

The runner recorded the following identity block in [`logs/runtime-identity.txt`](logs/runtime-identity.txt):

```text
DISPLAY=:1
XDG_SESSION_TYPE=wayland
display=:1 dimensions=5760x1080
display=:1 _NET_WORKAREA=0, 32, 5760, 1048
display=:1 _NET_WORKAREA-raw=_NET_WORKAREA(CARDINAL) = 0, 32, 5760, 1048
display=:2 dimensions=5760x1080
display=:2 _NET_WORKAREA=0, 32, 5760, 1048
display=:2 _NET_WORKAREA-raw=_NET_WORKAREA(CARDINAL) = 0, 32, 5760, 1048
kwin_wayland=93597 /usr/bin/kwin_wayland_wrapper --xwayland
93604 /usr/bin/kwin_wayland --wayland-fd 7 --socket wayland-1 --xwayland-fd 8 --xwayland-fd 9 --xwayland-display :1 --xwayland-initfd 11 --xwayland-init-display :2 --xwayland-xauthority /run/user/1000/xauth_QkGXlp --xwayland
kwin_x11=NONE
java-version-command=/home/henry/.sdkman/candidates/java/21.0.8-zulu/bin/java --version
java-version=openjdk 21.0.8 2025-07-15 LTS
java-version=OpenJDK Runtime Environment Zulu21.44+17-CA (build 21.0.8+9-LTS)
java-version=OpenJDK 64-Bit Server VM Zulu21.44+17-CA (build 21.0.8+9-LTS, mixed mode, sharing)
git-head=39f55bbbd1b28ac2a47990f528b3e1a2554caaba
source-sha256=freeplane/src/main/java/org/freeplane/core/ui/components/FrameResynchronizer.java 8e3cfbc2620dadd11c97f56f3f980640c938ba61929c7bc20ae760bfe0b63b7c
source-sha256=freeplane/src/main/java/org/freeplane/core/ui/components/PopupCoordinateCalibrator.java 793f0b2569e3b44387a3c338c82fc3d6bbc386292ecd76cf21bb57c6ac37080c
source-sha256=freeplane/src/main/java/org/freeplane/core/ui/components/X11PopupFactory.java 3907eba98e2f384e8a80e1afc9bf9f6063435770bd6a0337e359132b383c8419
```

The probe recorded `toolkit-class=sun.awt.X11.XToolkit`, the Java default screen device as `(1920,0,1920,1080)`, and zero Java screen insets. The X display identity above reports the merged XWayland screen as `5760x1080` with the `32px` top-panel work-area strut.

## Pointer-selection path

For every scenario, the probe shows a `JPopupMenu`, waits at least 800 ms, reads the first `JMenuItem`'s actual screen bounds with `getLocationOnScreen()`, and records the menu's screen bounds. It derives the click target from the expected calibrated popup origin plus the first item's local offset. With correct geometry, that target is the center of the recorded item. This target choice is deliberate: clicking the item's actual location after a popup has been displaced would make a misplacement mutant self-fulfilling and would not test whether the intended screen coordinate hits the menu.

The probe moves the pointer with `java.awt.Robot`, uses an XTest motion only to synchronize Java's X11 warp with the compositor pointer on this XWayland runtime, then presses and releases with `Robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)` and `Robot.mouseRelease(...)`. A bounded `invokeLater` drain and EDT wait follow the click. The `popup-pointer-selected=<text>` record is emitted only inside the item's `ActionListener` while the pointer phase is active.

The existing `item.doClick()` call remains after the pointer assertion as a separate diagnostic fallback. Its listener record is `popup-doClick-fallback-selected=<text>`, and its state is stored separately. It cannot satisfy the pointer assertion.

## Probe result

The final canonical probe exit code was **0**. Its complete stdout is preserved in [`logs/probe.stdout`](logs/probe.stdout); the full stdout is reproduced here:

```text
probe=PopupCoordinateAcceptanceProbe
display=:1
java-version=21.0.8
toolkit-class=sun.awt.X11.XToolkit
screen-bounds=(1920,0,1920,1080)
screen-insets=(0,0,0,0)
screen-size=1920x1080
popup-factory-class=org.freeplane.core.ui.components.X11PopupFactory
popup-factory-delegate-class=com.formdev.flatlaf.ui.FlatPopupFactory
popup-factory-delegate-is-flatpopupfactory=true
correction-assertion=split-cache expected=(-240,-180) actual=(-240,-180) result=true
correction-assertion=matched-geometry expected=(0,0) actual=(0,0) result=true
popup-class-parity=heavyweight-menu adapter-class=javax.swing.Popup$HeavyWeightWindow delegate-class=javax.swing.Popup$HeavyWeightWindow equal=true expected=javax.swing.Popup$HeavyWeightWindow
popup-class-parity=lightweight-enabled-menu adapter-class=javax.swing.Popup$HeavyWeightWindow delegate-class=javax.swing.Popup$HeavyWeightWindow equal=true expected=javax.swing.Popup$HeavyWeightWindow
popup-class-parity=tooltip adapter-class=javax.swing.Popup$HeavyWeightWindow delegate-class=javax.swing.Popup$HeavyWeightWindow equal=true expected=javax.swing.Popup$HeavyWeightWindow
popup-pointer-target=startup-normal clicked-screen-point=(241,214) menu-screen-bounds=(144,198,194,32) item-screen-bounds=(145,202,192,24)
popup-pointer-selected=selected-startup-normal
popup-doClick-fallback-selected=selected-startup-normal
scenario=startup-normal owner-awt-screen=(144,174) owner-location-within-frame=(24,54) native-outer-frame-bounds=(120,120,900,600) expected-native-owner-origin=(144,174) correction=(0,0) requested-popup-coordinates=(144,198) popup-local-offset=(0,24) expected-popup-native-origin=(144,198) popup-window-class=javax.swing.Popup$HeavyWeightWindow popup-awt-bounds=(144,198,194,32) popup-native-bounds=(144,198,194,32) popup-menu-screen-bounds=(144,198,194,32) popup-item-screen-bounds=(145,202,192,24) pointer-click-screen-point=(241,214) placement-error=(0,0) placement-error-max=0 persistence-ms=911 menu-showing-before-selection=true menu-showing-after-selection=false pointer-selection=true pointer-selected-text=selected-startup-normal doClick-fallback-selection=true doClick-fallback-text=selected-startup-normal frame-correction-setBounds-delta=0
popup-pointer-target=maximize clicked-screen-point=(104,126) menu-screen-bounds=(24,110,160,32) item-screen-bounds=(25,114,158,24)
popup-pointer-selected=selected-maximize
popup-doClick-fallback-selected=selected-maximize
scenario=maximize owner-awt-screen=(20,52) owner-location-within-frame=(24,54) native-outer-frame-bounds=(0,32,1920,1048) expected-native-owner-origin=(24,86) correction=(4,34) requested-popup-coordinates=(20,76) popup-local-offset=(0,24) expected-popup-native-origin=(24,110) popup-window-class=javax.swing.Popup$HeavyWeightWindow popup-awt-bounds=(24,110,160,32) popup-native-bounds=(24,110,160,32) popup-menu-screen-bounds=(24,110,160,32) popup-item-screen-bounds=(25,114,158,24) pointer-click-screen-point=(104,126) placement-error=(0,0) placement-error-max=0 persistence-ms=905 menu-showing-before-selection=true menu-showing-after-selection=false pointer-selection=true pointer-selected-text=selected-maximize doClick-fallback-selection=true doClick-fallback-text=selected-maximize frame-correction-setBounds-delta=0
popup-pointer-target=restore clicked-screen-point=(217,214) menu-screen-bounds=(144,198,146,32) item-screen-bounds=(145,202,144,24)
popup-pointer-selected=selected-restore
popup-doClick-fallback-selected=selected-restore
scenario=restore owner-awt-screen=(144,174) owner-location-within-frame=(24,54) native-outer-frame-bounds=(120,120,900,600) expected-native-owner-origin=(144,174) correction=(0,0) requested-popup-coordinates=(144,198) popup-local-offset=(0,24) expected-popup-native-origin=(144,198) popup-window-class=javax.swing.Popup$HeavyWeightWindow popup-awt-bounds=(144,198,146,32) popup-native-bounds=(144,198,146,32) popup-menu-screen-bounds=(144,198,146,32) popup-item-screen-bounds=(145,202,144,24) pointer-click-screen-point=(217,214) placement-error=(0,0) placement-error-max=0 persistence-ms=903 menu-showing-before-selection=true menu-showing-after-selection=false pointer-selection=true pointer-selected-text=selected-restore doClick-fallback-selection=true doClick-fallback-text=selected-restore frame-correction-setBounds-delta=0
popup-pointer-target=remaximize clicked-screen-point=(110,126) menu-screen-bounds=(24,110,172,32) item-screen-bounds=(25,114,170,24)
popup-pointer-selected=selected-remaximize
popup-doClick-fallback-selected=selected-remaximize
scenario=remaximize owner-awt-screen=(20,52) owner-location-within-frame=(24,54) native-outer-frame-bounds=(0,32,1920,1048) expected-native-owner-origin=(24,86) correction=(4,34) requested-popup-coordinates=(20,76) popup-local-offset=(0,24) expected-popup-native-origin=(24,110) popup-window-class=javax.swing.Popup$HeavyWeightWindow popup-awt-bounds=(24,110,172,32) popup-native-bounds=(24,110,172,32) popup-menu-screen-bounds=(24,110,172,32) popup-item-screen-bounds=(25,114,170,24) pointer-click-screen-point=(110,126) placement-error=(0,0) placement-error-max=0 persistence-ms=903 menu-showing-before-selection=true menu-showing-after-selection=false pointer-selection=true pointer-selected-text=selected-remaximize doClick-fallback-selection=true doClick-fallback-text=selected-remaximize frame-correction-setBounds-delta=0
probe-result=PASS
```

## Scenario evidence

| Scenario | Clicked screen point | Menu screen bounds | First item screen bounds | Popup native bounds | Placement error | Persistence before click | Pointer result | doClick fallback |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| startup-normal | `(241,214)` | `(144,198,194,32)` | `(145,202,192,24)` | `(144,198,194,32)` | `(0,0)`, max `0px` | `911ms`, showing | `selected-startup-normal` | `selected-startup-normal` |
| maximize | `(104,126)` | `(24,110,160,32)` | `(25,114,158,24)` | `(24,110,160,32)` | `(0,0)`, max `0px` | `905ms`, showing | `selected-maximize` | `selected-maximize` |
| restore | `(217,214)` | `(144,198,146,32)` | `(145,202,144,24)` | `(144,198,146,32)` | `(0,0)`, max `0px` | `903ms`, showing | `selected-restore` | `selected-restore` |
| remaximize | `(110,126)` | `(24,110,172,32)` | `(25,114,170,24)` | `(24,110,172,32)` | `(0,0)`, max `0px` | `903ms`, showing | `selected-remaximize` | `selected-remaximize` |

The menu was still showing after the 800 ms persistence interval in all four scenarios. The pointer click then selected the item and dismissed the popup, which is why `menu-showing-after-selection=false` is recorded. Each pointer result was asserted before the independent fallback call.

The owner-local correction was `(0,0)` for normal geometry and `(4,34)` after maximization. The maximized owner AWT screen location was `(20,52)`, while the native frame plus owner-within-frame origin was `(24,86)`, producing the measured `(4,34)` correction. Each popup's native origin matched the corrected expected origin exactly.

## Falsifiability evidence

The previous `doClick()` path was insufficient evidence because `JMenuItem.doClick()` dispatches an `ActionEvent` directly and bypasses pointer hit-testing. A popup rendered at the wrong screen position can remain visible while the pointer lands outside it; `doClick()` would still invoke the listener and falsely report selection. The new pointer assertion is evaluated separately and remains failed when the fallback succeeds.

The exact three requested results are:

```text
correct geometry => selected
+240/+180 => not selected
+3/+3 => observed result: pointer selected, but the probe exited nonzero because placement error was 3px (>1px)
```

| Geometry | Pointer-selection lines | Representative evidence | Probe exit |
| --- | ---: | --- | ---: |
| correct geometry | `4` | All four scenarios have `popup-pointer-selected=...`, placement error max `0`, and persistence `903–911ms`. | `0` |
| `+240/+180` mutant | `0` | No `popup-pointer-selected` line. Startup popup bounds were `(384,378,194,32)`, clicked point remained `(241,214)`, `pointer-selection=false`, and placement error max was `240px`. | `1` |
| `+3/+3` mutant | `4` | All four scenarios emitted `popup-pointer-selected=...`; popup origins were displaced by `(3,3)`, pointer selection remained true, and each placement error max was `3px`. | `1` because the probe's placement gate is `1px` |

The `(240,180)` mutant therefore proves that the pointer path distinguishes a correctly positioned popup from a visibly present but unclickable popup. The `(3,3)` result measures the hit-test tolerance separately from the stricter placement assertion: the item still contains the expected click center at a 3 px displacement, but the probe rejects the geometry.

## Popup-class parity

| Popup case | Adapter-produced class | Delegate-produced class | Equal | Required class |
| --- | --- | --- | --- | --- |
| heavyweight menu | `javax.swing.Popup$HeavyWeightWindow` | `javax.swing.Popup$HeavyWeightWindow` | `true` | `javax.swing.Popup$HeavyWeightWindow` |
| menu with `setLightWeightPopupEnabled(true)` | `javax.swing.Popup$HeavyWeightWindow` | `javax.swing.Popup$HeavyWeightWindow` | `true` | `javax.swing.Popup$HeavyWeightWindow` |
| `JToolTip` | `javax.swing.Popup$HeavyWeightWindow` | `javax.swing.Popup$HeavyWeightWindow` | `true` | `javax.swing.Popup$HeavyWeightWindow` |

The shared factory was `org.freeplane.core.ui.components.X11PopupFactory`, and its delegate was `com.formdev.flatlaf.ui.FlatPopupFactory`.

## Correction assertions

| Assertion | Expected | Actual | Result |
| --- | --- | --- | --- |
| Synthetic split-cache stale owner screen location | `(-240,-180)` | `(-240,-180)` | `true` |
| Matched native/AWT geometry | `(0,0)` | `(0,0)` | `true` |

The synthetic split-cache case used a stale owner screen location offset by `(240,180)` and exercised `PopupCoordinateCalibrator.calculateCorrection(...)` directly through the same geometry-provider contract used by production.

## Full-suite result

`gradle :freeplane:test --rerun-tasks` completed with `BUILD SUCCESSFUL`. Counting the 104 XML result files produced:

```text
xml_files 104
{'tests': 741, 'failures': 0, 'errors': 0, 'skipped': 3}
```

## LIMITATIONS

Available X displays are XWayland under kwin_wayland (DISPLAY=:1 and :2). `_NET_WORKAREA` on :1 is `0, 32, 5760, 1048`, a real 32px top-panel strut on a 5760x1080 screen.

There is NO native X11 KWin session in this environment (`pgrep` shows `kwin_wayland` only). This is an explicit verification limitation. Only XWayland was exercised, so native-X11 runtime acceptance remains UNVERIFIED; this evidence must not be read as a native-X11 KWin pass.

XWayland uses `sun.awt.X11.XToolkit`, so the same production gate and the same calibration code path are exercised. The desktop runtime evidence verifies the shared X11 toolkit/calibration path under XWayland, while the native X11 KWin environment remains unavailable for runtime acceptance.
