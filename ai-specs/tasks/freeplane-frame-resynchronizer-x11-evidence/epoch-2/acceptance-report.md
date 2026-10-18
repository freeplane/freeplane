# X11 Popup Calibration Acceptance Evidence

## Status

The canonical four-scenario pointer acceptance **passes on the isolated Xvfb/Openbox display described below**. The probe compiled with `javac --release 8`, exited `0`, and recorded active-frame, persistence, in-bounds pointer-click, pointer-selection, dismissal, correction, and placement checkpoints for every scenario.

This is not a native KWin runtime claim. The host session is Wayland with XWayland available on `DISPLAY=:1` and `:2`; the isolated acceptance display is Xvfb. **XWayland-only runtime acceptance and native X11 KWin runtime acceptance remain UNVERIFIED.**

## Command and Artifacts

The canonical run and the `(240,180)` falsifiability mutant were run sequentially by the same runner invocation:

```bash
JAVA_HOME=/home/henry/.sdkman/candidates/java/21.0.8-zulu \
PATH="$JAVA_HOME/bin:$PATH" \
python3 ai-specs/tasks/freeplane-frame-resynchronizer-x11-evidence/epoch-2/run-acceptance.py \
  --mutant-offset 240,180
```

The runner reported:

```text
probe-exit-code=0
probe-timeout=False
mutant-offset=240,180
mutant-exit-code=1
mutant-timeout=False
mutant-validation=True
```

The complete canonical output is in [`logs/probe.stdout`](logs/probe.stdout), with the result summary in [`logs/probe.result.txt`](logs/probe.result.txt). The complete mutant output is in [`logs/falsifiability-shift-240-180.stdout`](logs/falsifiability-shift-240-180.stdout), with its result summary in [`logs/falsifiability-shift-240-180.result.txt`](logs/falsifiability-shift-240-180.result.txt). Exact compile and probe commands are in [`logs/commands.txt`](logs/commands.txt); compiler diagnostics are in [`logs/javac.stderr`](logs/javac.stderr) and [`logs/falsifiability-shift-240-180.javac.stderr`](logs/falsifiability-shift-240-180.javac.stderr).

## Display Choice and Rationale

The first attempt used the shared `DISPLAY=:1`, which is XWayland under `kwin_wayland`. That environment did not provide an active Java frame to the probe: the earlier run reported `frame-active timeout-ms=5000`, `xdotool getactivewindow` returned no active window, and Robot pointer events did not reach the popup. This was an **environmental** failure, not evidence of popup misplacement. The earlier shared-display run therefore remained unverified for pointer acceptance.

Plain Xvfb was also tested. It allowed pointer selection, but it had no window manager, so the `maximize` and `remaximize` scenarios could not be exercised. The runner therefore now selects the first free display in `:77` through `:99`, starts Xvfb and Openbox together, and terminates both process groups in `finally` cleanup.

The successful run used:

```text
DISPLAY=:77
XDG_SESSION_TYPE=wayland
selected display kind=private Xvfb with Openbox window manager
screen=2240x1400x24
screen-bounds=(0,0,2240,1400)
window-manager=Openbox
```

This isolated display provides a deterministic active frame and a window manager capable of the maximize and restore transitions while keeping the pointer test independent of unrelated windows on the shared XWayland display. The selected display identity, host display inventory, Java identity, Git HEAD, and production source hashes are recorded in [`logs/runtime-identity.txt`](logs/runtime-identity.txt). The runner-owned `Xvfb :77` and Openbox processes were cleaned up after the run.

## Probe Gates and Failure Classification

The probe now:

- retries frame activation for a bounded five-second interval and logs `frame-active` for the initial frame and every scenario;
- reports `ENVIRONMENT: frame could not be activated ...` when the frame cannot become active;
- requires the computed Robot click to be inside the recorded item screen bounds and logs `pointer-click-inside-item-bounds`;
- keeps pointer selection separate from the later diagnostic `JMenuItem.doClick()` fallback;
- reports pointer dispatch or selection failures as `ENVIRONMENT:` failures; and
- reports placement-gate failures as `PRODUCT: popup misplaced ... placement-error=(X,Y)`.

The actual button press and release remain `java.awt.Robot` events. XTest is used only to synchronize pointer motion before the Robot click, so the test does not replace pointer selection with `doClick()`.

## Canonical Run

Every scenario became active on the first activation attempt. Each popup stayed visible for more than the required 800 ms before the pointer phase. The click was inside the item bounds, the item action listener recorded pointer selection, the popup was dismissed by the pointer click, and the native placement error was exactly `(0,0)`.

| Scenario | Frame active | Persistence | Click inside item bounds | Pointer selection | Placement error | Before pointer | After pointer | After fallback |
| --- | --- | ---: | --- | --- | --- | --- | --- |
| `startup-normal` | true, attempt 1 | 908 ms | true | `selected-startup-normal` | `(0,0)` | showing | hidden | hidden |
| `maximize` | true, attempt 1 | 903 ms | true | `selected-maximize` | `(0,0)` | showing | hidden | hidden |
| `restore` | true, attempt 1 | 903 ms | true | `selected-restore` | `(0,0)` | showing | hidden | hidden |
| `remaximize` | true, attempt 1 | 904 ms | true | `selected-remaximize` | `(0,0)` | showing | hidden | hidden |

Representative raw checkpoints, including all four active-frame and pointer-bound assertions, are preserved in the canonical log:

```text
frame-active scenario=initial value=true attempt=1
frame-active scenario=startup-normal value=true attempt=1
pointer-click-inside-item-bounds scenario=startup-normal value=true
frame-active scenario=maximize value=true attempt=1
pointer-click-inside-item-bounds scenario=maximize value=true
frame-active scenario=restore value=true attempt=1
pointer-click-inside-item-bounds scenario=restore value=true
frame-active scenario=remaximize value=true attempt=1
pointer-click-inside-item-bounds scenario=remaximize value=true
```

The per-scenario records also contain `pointer-selection=true`, `pointer-selected-text=selected-<scenario>`, `menu-showing-before-pointer=true`, `menu-showing-after-pointer=false`, `persistence-ms` at or above 800, and `placement-error=(0,0)`. The diagnostic fallback ran only after those pointer checkpoints and was not needed to satisfy pointer selection.

Popup factory and correction gates also passed. The adapter and delegate both produced `javax.swing.Popup$HeavyWeightWindow` for heavyweight menus, lightweight-enabled menus, and tooltips. The split-cache correction assertion was `(-240,-180)` as expected, and the matched-geometry correction assertion was `(0,0)`.

## `(240,180)` Mutant

The mutant changed the single production-style call from the correct local popup coordinates to:

```java
menu.show(owner, evidence.popupLocal.x + 240, evidence.popupLocal.y + 180);
```

It was compiled and run on the same isolated display, with no production source or test changes. Frame activation still passed in all four scenarios and persistence remained above 800 ms, but the popup was displaced by exactly `(240,180)`. The intended click was therefore outside the actual item bounds in every scenario.

| Scenario | Persistence | Intended click | Actual item bounds | Click inside item | Pointer selection | Placement error | Before pointer | After pointer | After fallback |
| --- | ---: | --- | --- | --- | --- | --- | --- | --- | --- |
| `startup-normal` | 905 ms | `(238,202)` | `(382,370,192,24)` | false | missing | `(240,180)` | showing | hidden | hidden |
| `maximize` | 902 ms | `(101,82)` | `(262,250,158,24)` | false | missing | `(240,180)` | showing | hidden | hidden |
| `restore` | 903 ms | `(214,202)` | `(382,370,144,24)` | false | missing | `(240,180)` | showing | hidden | hidden |
| `remaximize` | 903 ms | `(107,82)` | `(262,250,170,24)` | false | missing | `(240,180)` | showing | hidden | hidden |

The raw mutant log records, for every scenario:

```text
pointer-click-inside-item-bounds ... value=false
assertion-failure=PRODUCT: popup misplaced ... placement-error=(240,180)
menu-showing-before-pointer ... value=true
menu-showing-after-pointer ... value=false
menu-showing-after-fallback ... value=false
```

Thus the misplaced popup was **dismissed by the outside Robot click in all four mutant scenarios**, and this statement is based on the explicit `menu-showing-after-pointer ... value=false` checkpoints. It is not inferred from the later fallback. The mutant also records pointer-selection-missing and pointer-dispatch/selection environment-labelled failures, which are expected downstream consequences of deliberately clicking the correct-location point while the popup has been shifted away. Frame activation itself passed, so those mutant `ENVIRONMENT:` lines do not indicate the earlier shared-display focus problem.

The mutant proves that a large coordinate displacement is detected by the product placement gate and cannot silently pass through the acceptance runner. It also proves that the click target is checked against the actual item bounds rather than being adjusted to follow the displaced popup. It does not prove pointer selection on a misplaced item, because the intended point is intentionally outside that item, and it does not extend the result to XWayland or native X11 KWin behavior.

## Regression and Source Boundaries

The previously captured full regression run completed successfully:

```text
BUILD SUCCESSFUL
XML results: 741 tests, 0 failures, 0 errors, 3 skipped
```

This update reran the acceptance probe and mutant; it did not rerun the full Gradle suite. Production source hashes are recorded in `logs/runtime-identity.txt`, and no production source or production test file was modified by this evidence update.

## Limitations

The host has `kwin_wayland` with XWayland displays `:1` and `:2`; `kwin_x11=NONE`. The canonical pass was obtained on isolated Xvfb with Openbox, not on the shared XWayland compositor and not on a native X11 KWin session. Therefore:

- XWayland-only runtime acceptance: **UNVERIFIED**.
- Native X11 KWin runtime acceptance: **UNVERIFIED**.
- Isolated Xvfb/Openbox canonical acceptance: **PASS**, as documented above.

The earlier `frame-active` timeout is resolved for the isolated acceptance environment by display isolation, window-manager support, bounded activation retries, and explicit environment/product failure labels. It remains a limitation of the shared XWayland environment rather than a passing product result there.
