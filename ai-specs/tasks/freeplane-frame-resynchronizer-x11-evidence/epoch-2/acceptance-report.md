# X11 Popup Calibration Acceptance Evidence

This update addresses the frontier-audit finding that the former `menu-showing-after-selection` sample was taken after `JMenuItem.doClick()` and could not attribute dismissal to the Robot pointer click. The probe now records visibility before the pointer click, after pointer dispatch and before fallback, and after fallback. The measurement defect is corrected, but the latest canonical run on `DISPLAY=:1` still fails pointer selection in all four scenarios; this run is not an acceptance pass. Native X11 KWin runtime acceptance remains UNVERIFIED; see [LIMITATIONS](#limitations).

## Commands and Run Status

The full regression suite was previously run with the required Java runtime:

```bash
JAVA_HOME=/home/henry/.sdkman/candidates/java/21.0.8-zulu PATH="$JAVA_HOME/bin:$PATH" gradle :freeplane:test --rerun-tasks
```

The canonical evidence runner was rerun for this update:

```bash
JAVA_HOME=/home/henry/.sdkman/candidates/java/21.0.8-zulu PATH="$JAVA_HOME/bin:$PATH" DISPLAY=:1 python3 ai-specs/tasks/freeplane-frame-resynchronizer-x11-evidence/epoch-2/run-acceptance.py
```

It compiled successfully, completed before the 90-second process bound, and returned exit code 1 because pointer-event dispatch timed out and pointer selection was missing in every scenario. The exit summary is preserved in [`logs/probe.result.txt`](logs/probe.result.txt). The exact compile and Java commands are in [`logs/commands.txt`](logs/commands.txt); complete output is in [`logs/probe.stdout`](logs/probe.stdout) and [`logs/probe.stderr`](logs/probe.stderr). Toolkit, factory/delegate, popup-class parity, correction, and canonical placement checks passed in this run. The pointer-selection gate remains active and failed.

The `(240,180)` and `+3/+3` mutants were generated outside the repository from this checkpointed probe source and run sequentially on `DISPLAY=:1`. Both compiled with `javac --release 8`, completed before the process bound, and exited 1 on the intentionally active gates. The mutation and command templates are recorded in [`logs/falsifiability-commands.txt`](logs/falsifiability-commands.txt). Their output and compiler records are in [`logs/falsifiability-shift-240-180.stdout`](logs/falsifiability-shift-240-180.stdout), [`logs/falsifiability-shift-240-180.stderr`](logs/falsifiability-shift-240-180.stderr), [`logs/falsifiability-shift-240-180.javac.stderr`](logs/falsifiability-shift-240-180.javac.stderr), [`logs/falsifiability-shift-240-180.result.txt`](logs/falsifiability-shift-240-180.result.txt), [`logs/falsifiability-shift-3-3.stdout`](logs/falsifiability-shift-3-3.stdout), [`logs/falsifiability-shift-3-3.stderr`](logs/falsifiability-shift-3-3.stderr), [`logs/falsifiability-shift-3-3.javac.stderr`](logs/falsifiability-shift-3-3.javac.stderr), and [`logs/falsifiability-shift-3-3.result.txt`](logs/falsifiability-shift-3-3.result.txt).

## Runtime Identity

The runner captured the complete environment identity in [`logs/runtime-identity.txt`](logs/runtime-identity.txt). This run used `DISPLAY=:1`, `XDG_SESSION_TYPE=wayland`, Java 21.0.8 Zulu, and a 5760x1080 XWayland display. The recorded Git HEAD was `18f82b23f1c4264f03192de5d5feab7424800b14`. Production source hashes are recorded in the identity log and were not changed for this evidence update.

The probe reported `sun.awt.X11.XToolkit`, `org.freeplane.core.ui.components.X11PopupFactory`, and the `com.formdev.flatlaf.ui.FlatPopupFactory` delegate. All three popup-class parity checks passed.

## Checkpoint Semantics

For each scenario, persistence is sampled separately after at least 800 ms and before the pointer click. `menu-showing-before-pointer` is sampled immediately before Robot presses the button. `menu-showing-after-pointer` is sampled after the bounded pointer-dispatch wait and before any fallback. The existing `item.doClick()` runs only after that checkpoint; its output is labeled `diagnostic-only`, and its result cannot satisfy the pointer-selection assertion. `menu-showing-after-fallback` is the separate late sample.

The interaction remains a `java.awt.Robot` pointer press/release. XTest is used only for XWayland pointer-motion synchronization. The placement threshold remains greater than 1 px, and the toolkit, factory/delegate, popup-class parity, and missing-pointer-selection failure gates were not weakened.

## Canonical Run

Placement error was `(0,0)` in all four scenarios, and each popup remained visible at the persistence checkpoint for 903-913 ms. The raw persistence and visibility checkpoint lines are:

```text
popup-persistence scenario=startup-normal duration-ms=913 minimum-ms=800 menu-showing=true
menu-showing-before-pointer scenario=startup-normal value=true
menu-showing-after-pointer scenario=startup-normal value=true
menu-showing-after-fallback scenario=startup-normal value=true
popup-persistence scenario=maximize duration-ms=903 minimum-ms=800 menu-showing=true
menu-showing-before-pointer scenario=maximize value=true
menu-showing-after-pointer scenario=maximize value=true
menu-showing-after-fallback scenario=maximize value=true
popup-persistence scenario=restore duration-ms=903 minimum-ms=800 menu-showing=true
menu-showing-before-pointer scenario=restore value=true
menu-showing-after-pointer scenario=restore value=true
menu-showing-after-fallback scenario=restore value=true
popup-persistence scenario=remaximize duration-ms=903 minimum-ms=800 menu-showing=true
menu-showing-before-pointer scenario=remaximize value=true
menu-showing-after-pointer scenario=remaximize value=true
menu-showing-after-fallback scenario=remaximize value=true
```

The click target was inside the correctly placed item bounds in every scenario, but no `popup-pointer-selected=...` event was recorded. The runner logged `pointer-event-dispatch timeout` and `popup-pointer-selection-missing` for all four. The later diagnostic fallback selected each item, but did not change the already-recorded after-pointer values. Therefore this run establishes persistence and the new phase separation, but does not establish successful canonical pointer selection or pointer-triggered dismissal.

| Scenario | Placement error | Persistence | After pointer | Pointer selection | Diagnostic fallback |
| --- | --- | --- | --- | --- | --- |
| startup-normal | `(0,0)` | `913ms`, showing | showing | missing | selected |
| maximize | `(0,0)` | `903ms`, showing | showing | missing | selected |
| restore | `(0,0)` | `903ms`, showing | showing | missing | selected |
| remaximize | `(0,0)` | `903ms`, showing | showing | missing | selected |

## `(240,180)` Mutant

The raw persistence and visibility checkpoint lines are:

```text
popup-persistence scenario=startup-normal duration-ms=911 minimum-ms=800 menu-showing=true
menu-showing-before-pointer scenario=startup-normal value=true
menu-showing-after-pointer scenario=startup-normal value=false
menu-showing-after-fallback scenario=startup-normal value=false
popup-persistence scenario=maximize duration-ms=903 minimum-ms=800 menu-showing=true
menu-showing-before-pointer scenario=maximize value=true
menu-showing-after-pointer scenario=maximize value=false
menu-showing-after-fallback scenario=maximize value=false
popup-persistence scenario=restore duration-ms=901 minimum-ms=800 menu-showing=true
menu-showing-before-pointer scenario=restore value=true
menu-showing-after-pointer scenario=restore value=true
menu-showing-after-fallback scenario=restore value=true
popup-persistence scenario=remaximize duration-ms=903 minimum-ms=800 menu-showing=true
menu-showing-before-pointer scenario=remaximize value=true
menu-showing-after-pointer scenario=remaximize value=true
menu-showing-after-fallback scenario=remaximize value=true
```

The per-scenario geometry and outcome from the mutant's raw `scenario=` records:

| Scenario | Intended click | Shifted item bounds | Placement error | Pointer selection | After pointer | After fallback |
| --- | --- | --- | --- | --- | --- | --- |
| startup-normal | `(241,214)` | `(385,382,192,24)` | `(240,180)` | no | hidden | hidden |
| maximize | `(104,126)` | `(265,294,158,24)` | `(240,180)` | no | hidden | hidden |
| restore | `(217,214)` | `(385,382,144,24)` | `(240,180)` | no | showing | showing |
| remaximize | `(110,126)` | `(265,294,170,24)` | `(240,180)` | no | showing | showing |

The intended click missed the shifted item in all four scenarios, and the placement gate correctly failed at 240 px. In startup-normal and maximize, the popup was already hidden at the pre-fallback checkpoint, so those runs show pointer-phase dismissal without selection. In restore and remaximize, it remained visible; those runs show a miss without dismissal. The fallback selected each item diagnostically, but it occurred after the checkpoint and did not determine either visibility result.

A second, in-menu control shifts the popup by `(3,3)`. Its raw persistence and visibility checkpoint lines are:

```text
popup-persistence scenario=startup-normal duration-ms=911 minimum-ms=800 menu-showing=true
menu-showing-before-pointer scenario=startup-normal value=true
menu-showing-after-pointer scenario=startup-normal value=true
menu-showing-after-fallback scenario=startup-normal value=true
popup-persistence scenario=maximize duration-ms=903 minimum-ms=800 menu-showing=true
menu-showing-before-pointer scenario=maximize value=true
menu-showing-after-pointer scenario=maximize value=true
menu-showing-after-fallback scenario=maximize value=true
popup-persistence scenario=restore duration-ms=902 minimum-ms=800 menu-showing=true
menu-showing-before-pointer scenario=restore value=true
menu-showing-after-pointer scenario=restore value=true
menu-showing-after-fallback scenario=restore value=true
popup-persistence scenario=remaximize duration-ms=902 minimum-ms=800 menu-showing=true
menu-showing-before-pointer scenario=remaximize value=true
menu-showing-after-pointer scenario=remaximize value=true
menu-showing-after-fallback scenario=remaximize value=true
```

| Scenario | Intended click | Shifted item bounds | Placement error | Pointer selection | After pointer | Other gate failure |
| --- | --- | --- | --- | --- | --- | --- |
| startup-normal | `(241,214)` | `(148,205,192,24)` | `(3,3)` | no | showing | frame-active timeout |
| maximize | `(104,126)` | `(28,117,158,24)` | `(3,3)` | no | showing | frame-active timeout |
| restore | `(97,126)` | `(28,117,144,24)` | `(3,3)` | no | showing | frame-active timeout |
| remaximize | `(230,214)` | `(148,205,170,24)` | `(3,3)` | no | showing | frame-active timeout |

Each intended click coordinate lies within the recorded shifted item bounds, but the probe emitted no pointer selection and every scenario also failed its frame-active gate. The popup stayed visible before and after the diagnostic fallback in all four. This control establishes geometric overlap, not that the pointer event was actually delivered to the item; it cannot prove an in-menu click-and-dismiss outcome in this runtime.

The `(240,180)` mutant proves that the intended click misses the shifted item in all four scenarios and records the actual, scenario-dependent visibility outcome before fallback. In startup-normal and maximize, the popup was already hidden at the pre-fallback checkpoint, so those runs show pointer-phase dismissal without selection. In restore and remaximize, it remained visible; those runs show a miss without dismissal. The fallback selected each item diagnostically, but it occurred after the checkpoint and did not determine either visibility result.

Together the mutants distinguish an outside-menu geometry from an intended point that geometrically overlaps the item, but the fresh runs do not establish whether the `+3/+3` input reached the item, nor do they establish canonical selection: the canonical run also lacked pointer selection. A claim that canonical clicks select while mutant clicks do not is not supported by this capture. The prior audit's confounding measurement is resolved; end-to-end canonical pointer acceptance remains blocked by the observed missing selection.

## Popup-Class Parity and Correction

| Popup case | Adapter class | Delegate class | Equal | Required class |
| --- | --- | --- | --- | --- |
| heavyweight menu | `javax.swing.Popup$HeavyWeightWindow` | `javax.swing.Popup$HeavyWeightWindow` | `true` | `javax.swing.Popup$HeavyWeightWindow` |
| lightweight-enabled menu | `javax.swing.Popup$HeavyWeightWindow` | `javax.swing.Popup$HeavyWeightWindow` | `true` | `javax.swing.Popup$HeavyWeightWindow` |
| `JToolTip` | `javax.swing.Popup$HeavyWeightWindow` | `javax.swing.Popup$HeavyWeightWindow` | `true` | `javax.swing.Popup$HeavyWeightWindow` |

| Correction assertion | Expected | Actual | Result |
| --- | --- | --- | --- |
| Synthetic split-cache stale owner screen location | `(-240,-180)` | `(-240,-180)` | `true` |
| Matched native/AWT geometry | `(0,0)` | `(0,0)` | `true` |

The synthetic split-cache case exercises `PopupCoordinateCalibrator.calculateCorrection(...)` through the same geometry-provider contract used by production. Maximized owner correction was `(4,34)`; the normal geometry correction was `(0,0)`.

## Regression Suite

The previously captured `gradle :freeplane:test --rerun-tasks` result was `BUILD SUCCESSFUL`. Counting 104 XML result files produced:

```text
xml_files 104
{'tests': 741, 'failures': 0, 'errors': 0, 'skipped': 3}
```

This update reran the acceptance runner and mutant, not the full Gradle suite.

## LIMITATIONS

Available X displays are XWayland under `kwin_wayland` (`DISPLAY=:1` and `:2`). `_NET_WORKAREA` on `:1` is `0, 32, 5760, 1048`, including the 32 px top-panel strut. There is no native X11 KWin session in this environment (`kwin_x11=NONE`). Only XWayland was exercised, so native-X11 runtime acceptance remains **UNVERIFIED** and this evidence must not be read as a native-X11 KWin pass.

The current XWayland canonical run also failed the Robot pointer-selection gate. The measurement correction and mutant records are valid for their logged observations, but the requested canonical four-scenario pointer-selection acceptance must be rerun in an environment where the probe can produce the pointer selection events.
