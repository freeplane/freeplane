# X11 Popup Calibration Acceptance Evidence

This evidence was captured from the integration worktree at `DISPLAY=:1`. The probe result is a pass for the exercised XWayland runtime. Native X11 KWin runtime acceptance is explicitly unverified; see [LIMITATIONS](#limitations).

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

The acceptance runner was invoked with:

```bash
JAVA_HOME=/home/henry/.sdkman/candidates/java/21.0.8-zulu PATH="$JAVA_HOME/bin:$PATH" DISPLAY=:1 python3 ai-specs/tasks/freeplane-frame-resynchronizer-x11-evidence/epoch-2/run-acceptance.py
```

The runner located its dependencies with these searches:

```bash
find ~/.gradle/caches -name 'flatlaf-3.7.2.jar'
find BIN/core BIN ~/.gradle/caches -type f \( -name 'jna-*.jar' -o -name 'jna-platform-*.jar' \)
```

The runner compiled the probe with `javac --release 8` using the built Freeplane classes and the located FlatLaf/JNA jars. The exact resolved compile and run commands are preserved in [`logs/commands.txt`](logs/commands.txt). The Java run used:

```bash
/home/henry/.sdkman/candidates/java/21.0.8-zulu/bin/java \
  --add-opens=java.desktop/sun.awt=ALL-UNNAMED \
  --add-opens=java.desktop/sun.awt.X11=ALL-UNNAMED \
  -cp <temporary-probe-classes>:freeplane/build/classes/java/main:<flatlaf-3.7.2.jar>:<jna.jar>:<jna-platform.jar> \
  org.freeplane.core.ui.components.PopupCoordinateAcceptanceProbe
```

The runner used a 90-second process bound, a new process group, and process-group termination on timeout. The final run exited before the timeout and left no background probe process.

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
java-version=OpenJDK Runtime Environment Zulu21.44+17 (build 21.0.8+9-LTS)
java-version=OpenJDK 64-Bit Server VM Zulu21.44+17-CA (build 21.0.8+9-LTS, mixed mode, sharing)
git-head=9380a85c212a76531e3e951d22801d7eda158620
source-sha256=freeplane/src/main/java/org/freeplane/core/ui/components/FrameResynchronizer.java 8e3cfbc2620dadd11c97f56f3f980640c938ba61929c7bc20ae760bfe0b63b7c
source-sha256=freeplane/src/main/java/org/freeplane/core/ui/components/PopupCoordinateCalibrator.java 793f0b2569e3b44387a3c338c82fc3d6bbc386292ecd76cf21bb57c6ac37080c
source-sha256=freeplane/src/main/java/org/freeplane/core/ui/components/X11PopupFactory.java 3907eba98e2f384e8a80e1afc9bf9f6063435770bd6a0337e359132b383c8419
```

The probe itself recorded `toolkit-class=sun.awt.X11.XToolkit`, the Java default screen device as `(1920,0,1920,1080)`, and zero Java screen insets. The X display identity above reports the merged XWayland screen as `5760x1080` with the `32px` top-panel work-area strut.

## Probe result

The final probe exit code was **0**. Its complete stdout is preserved in [`logs/probe.stdout`](logs/probe.stdout); the full stdout is reproduced here:

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
popup-item-selected=selected-startup-normal
scenario=startup-normal owner-awt-screen=(144,174) owner-location-within-frame=(24,54) native-outer-frame-bounds=(120,120,900,600) expected-native-owner-origin=(144,174) correction=(0,0) requested-popup-coordinates=(144,198) popup-local-offset=(0,24) expected-popup-native-origin=(144,198) popup-window-class=javax.swing.Popup$HeavyWeightWindow popup-awt-bounds=(144,198,194,32) popup-native-bounds=(144,198,194,32) placement-error=(0,0) placement-error-max=0 persistence-ms=913 menu-showing-before-selection=true menu-showing-after-selection=true selection=true selected-text=selected-startup-normal frame-correction-setBounds-delta=0
popup-item-selected=selected-maximize
scenario=maximize owner-awt-screen=(20,52) owner-location-within-frame=(24,54) native-outer-frame-bounds=(0,32,1920,1048) expected-native-owner-origin=(24,86) correction=(4,34) requested-popup-coordinates=(20,76) popup-local-offset=(0,24) expected-popup-native-origin=(24,110) popup-window-class=javax.swing.Popup$HeavyWeightWindow popup-awt-bounds=(24,110,160,32) popup-native-bounds=(24,110,160,32) placement-error=(0,0) placement-error-max=0 persistence-ms=901 menu-showing-before-selection=true menu-showing-after-selection=true selection=true selected-text=selected-maximize frame-correction-setBounds-delta=0
popup-item-selected=selected-restore
scenario=restore owner-awt-screen=(144,174) owner-location-within-frame=(24,54) native-outer-frame-bounds=(120,120,900,600) expected-native-owner-origin=(144,174) correction=(0,0) requested-popup-coordinates=(144,198) popup-local-offset=(0,24) expected-popup-native-origin=(144,198) popup-window-class=javax.swing.Popup$HeavyWeightWindow popup-awt-bounds=(144,198,146,32) popup-native-bounds=(144,198,146,32) placement-error=(0,0) placement-error-max=0 persistence-ms=901 menu-showing-before-selection=true menu-showing-after-selection=true selection=true selected-text=selected-restore frame-correction-setBounds-delta=0
popup-item-selected=selected-remaximize
scenario=remaximize owner-awt-screen=(20,52) owner-location-within-frame=(24,54) native-outer-frame-bounds=(0,32,1920,1048) expected-native-owner-origin=(24,86) correction=(4,34) requested-popup-coordinates=(20,76) popup-local-offset=(0,24) expected-popup-native-origin=(24,110) popup-window-class=javax.swing.Popup$HeavyWeightWindow popup-awt-bounds=(24,110,172,32) popup-native-bounds=(24,110,172,32) placement-error=(0,0) placement-error-max=0 persistence-ms=903 menu-showing-before-selection=true menu-showing-after-selection=true selection=true selected-text=selected-remaximize frame-correction-setBounds-delta=0
probe-result=PASS
```

## Scenario evidence

| Scenario | Native frame bounds | Computed correction | Popup AWT bounds | Popup native bounds | Placement error | Persistence | Selection | Frame correction delta |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| startup-normal | `(120,120,900,600)` | `(0,0)` | `(144,198,194,32)` | `(144,198,194,32)` | `(0,0)`, max `0px` | `913ms`, showing before selection | `selected-startup-normal`; showing after selection | `0` |
| maximize | `(0,32,1920,1048)` | `(4,34)` | `(24,110,160,32)` | `(24,110,160,32)` | `(0,0)`, max `0px` | `901ms`, showing before selection | `selected-maximize`; showing after selection | `0` |
| restore | `(120,120,900,600)` | `(0,0)` | `(144,198,146,32)` | `(144,198,146,32)` | `(0,0)`, max `0px` | `901ms`, showing before selection | `selected-restore`; showing after selection | `0` |
| remaximize | `(0,32,1920,1048)` | `(4,34)` | `(24,110,172,32)` | `(24,110,172,32)` | `(0,0)`, max `0px` | `903ms`, showing before selection | `selected-remaximize`; showing after selection | `0` |

The owner-local correction is `(0,0)` for normal geometry and `(4,34)` after maximization. The maximized owner AWT screen location was `(20,52)`, while the native frame plus owner-within-frame origin was `(24,86)`, producing the measured `(4,34)` correction. Each popup's native origin matched the corrected expected origin exactly.

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
{'tests': 741, 'failures': 0, 'errors': 0, 'skipped': 3}
```

## LIMITATIONS

Available X displays are XWayland under kwin_wayland (DISPLAY=:1 and :2). `_NET_WORKAREA` on :1 is `0, 32, 5760, 1048`, i.e. a real 32px top-panel strut on a 5760x1080 screen.

There is NO native X11 KWin session in this environment (pgrep shows kwin_wayland only). This is an explicit verification limitation. Only XWayland was exercised, so native-X11 runtime acceptance remains UNVERIFIED; this evidence must not be read as a native-X11 KWin pass.

XWayland uses `sun.awt.X11.XToolkit`, so the same production gate and the same calibration code path are exercised. The desktop runtime evidence therefore verifies the shared X11 toolkit/calibration path under XWayland, while the native X11 KWin environment remains unavailable for runtime acceptance.
