# X11 Popup Calibration Acceptance Evidence

## Status

The canonical four-scenario pointer acceptance **passes on the isolated Xvfb/Openbox display described below**. The probe compiled with `javac --release 8`, exited `0`, and recorded `failure-class=NONE`, active-frame, persistence, in-bounds pointer-click, pointer-selection, dismissal, correction, and placement checkpoints for every scenario. The `(240,180)` mutant exited `1` and recorded `failure-class=PRODUCT` for all four scenarios.

This is not a native KWin runtime claim. The host session is Wayland with XWayland available on `DISPLAY=:1` and `:2`; the isolated acceptance display is Xvfb. **XWayland-only runtime acceptance and native X11 KWin runtime acceptance remain UNVERIFIED.**

## Command and Artifacts

The canonical run and the `(240,180)` falsifiability mutant were run sequentially by the same runner invocation:

```bash
JAVA_HOME=/home/henry/.sdkman/candidates/java/21.0.8-zulu \
PATH="/home/henry/.sdkman/candidates/java/21.0.8-zulu/bin:$PATH" \
python3 ai-specs/tasks/freeplane-frame-resynchronizer-x11-evidence/epoch-2/run-acceptance.py \
  --mutant-offset 240,180
```

The runner reported:

```text
probe-exit-code=0
probe-timeout=False
canonical-classification-validation=True
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

The probe emits exactly one authoritative `failure-class=<PRODUCT|ENVIRONMENT|NONE> reason=<short> scenario=<name>` line per scenario. It classifies by the physical cause, in this order:

- If the intended click falls outside the actual `popup-item-screen-bounds`, the classification is `PRODUCT`, with reason `popup misplaced; click landed outside item bounds`. The containment check compares the logged `pointer-click-screen-point` with the item's actual screen rectangle; it does not move the target to follow a displaced popup.
- If the native placement error exceeds the unchanged one-pixel tolerance on either axis, the classification is `PRODUCT`, with reason `popup misplaced; placement error exceeds tolerance`, even if the click fits inside a large item.
- If the frame could not be activated and there is no measured misplacement, the classification is `ENVIRONMENT`, with reason `frame could not be activated`.
- A missing pointer selection is classified as `ENVIRONMENT: pointer dispatch failed although the click landed inside the item` only when the frame was active, the click was inside the actual item bounds, native placement evidence was available and within tolerance, and the menu was showing at both the persistence checkpoint and immediately before the click.
- Other failed scenario gates remain failures and are conservatively classified as `PRODUCT`; a scenario whose gates all passed is `NONE`.

The earlier mutant log labelled pointer dispatch/selection failures `ENVIRONMENT` solely because selection was missing. That label was wrong: the logged `(240,180)` placement error put the actual item away from the intended click, so the Robot clicked outside the popup and dismissed it without selecting an entry. The product misplacement caused the miss. Those downstream selection diagnostics therefore cannot establish an environment problem.

Assertion and final failure records retain the individual gate diagnostics without competing `PRODUCT:` or `ENVIRONMENT:` labels; the single `failure-class` line supplies the classification. The runner also requires exactly four classification lines, one for each expected scenario: all `NONE` for canonical and all `PRODUCT` for the mutant.

No exit gate was weakened. Misplacement, an out-of-bounds click, missing selection, an inactive frame, unavailable placement evidence, insufficient persistence, wrong toolkit or popup factory/delegate, failed correction assertions, and popup-class parity mismatches still make the probe exit nonzero. The actual button press and release remain `java.awt.Robot` events. XTest synchronizes pointer motion before the Robot click, and the later diagnostic `JMenuItem.doClick()` fallback cannot satisfy the pointer-selection gate.

## Canonical Run

Every scenario became active on the first activation attempt. Each popup stayed visible for more than the required 800 ms before the pointer phase. The click was inside the item bounds, the item action listener recorded pointer selection, the popup was dismissed by the pointer click, and the native placement error was exactly `(0,0)`.

| Scenario | Frame active | Persistence | Click inside item bounds | Pointer selection | Placement error | Before pointer | After pointer | After fallback |
| --- | --- | ---: | --- | --- | --- | --- | --- |
| `startup-normal` | true, attempt 1 | 912 ms | true | `selected-startup-normal` | `(0,0)` | showing | hidden | hidden |
| `maximize` | true, attempt 1 | 904 ms | true | `selected-maximize` | `(0,0)` | showing | hidden | hidden |
| `restore` | true, attempt 1 | 903 ms | true | `selected-restore` | `(0,0)` | showing | hidden | hidden |
| `remaximize` | true, attempt 1 | 902 ms | true | `selected-remaximize` | `(0,0)` | showing | hidden | hidden |

The canonical probe exited `0` and emitted these authoritative classifications:

```text
failure-class=NONE reason=all scenario gates passed scenario=startup-normal
failure-class=NONE reason=all scenario gates passed scenario=maximize
failure-class=NONE reason=all scenario gates passed scenario=restore
failure-class=NONE reason=all scenario gates passed scenario=remaximize
```

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
| `startup-normal` | 910 ms | `(238,202)` | `(382,370,192,24)` | false | missing | `(240,180)` | showing | hidden | hidden |
| `maximize` | 902 ms | `(101,82)` | `(262,250,158,24)` | false | missing | `(240,180)` | showing | hidden | hidden |
| `restore` | 903 ms | `(214,202)` | `(382,370,144,24)` | false | missing | `(240,180)` | showing | hidden | hidden |
| `remaximize` | 903 ms | `(107,82)` | `(262,250,170,24)` | false | missing | `(240,180)` | showing | hidden | hidden |

The mutant probe exited `1` and emitted these authoritative classifications:

```text
failure-class=PRODUCT reason=popup misplaced; click landed outside item bounds scenario=startup-normal
failure-class=PRODUCT reason=popup misplaced; click landed outside item bounds scenario=maximize
failure-class=PRODUCT reason=popup misplaced; click landed outside item bounds scenario=restore
failure-class=PRODUCT reason=popup misplaced; click landed outside item bounds scenario=remaximize
```

The actual geometry checkpoints show the miss directly (item bounds are `x,y,width,height`):

```text
popup-pointer-target=startup-normal clicked-screen-point=(238,202) menu-screen-bounds=(381,366,194,32) item-screen-bounds=(382,370,192,24)
popup-pointer-target=maximize clicked-screen-point=(101,82) menu-screen-bounds=(261,246,160,32) item-screen-bounds=(262,250,158,24)
popup-pointer-target=restore clicked-screen-point=(214,202) menu-screen-bounds=(381,366,146,32) item-screen-bounds=(382,370,144,24)
popup-pointer-target=remaximize clicked-screen-point=(107,82) menu-screen-bounds=(261,246,172,32) item-screen-bounds=(262,250,170,24)
```

For every mutant scenario the raw log also records `pointer-click-inside-item-bounds ... value=false`, `placement-error=(240,180)`, `menu-showing-before-pointer ... value=true`, `menu-showing-after-pointer ... value=false`, and `pointer-selection=false`. There are no `ENVIRONMENT` classifications in this mutant run.

The mutant proves that the popup was misplaced, so the intended click missed and no entry could be selected by that click. The outside Robot click dismissed the menu in all four scenarios, matching the reported symptom that the popup disappears without a selectable entry at the expected position. The dismissal is established by the explicit post-pointer checkpoints, before the diagnostic fallback. It also proves that the placement and pointer-selection gates reject the defect and that the click target does not follow the displaced popup.

The mutant does not prove anything about selection or persistence of a correctly placed popup. It does not test clicking the displaced item's actual position, and it does not establish XWayland or native X11 KWin behavior. Positive correctly placed popup evidence comes from the separate canonical Xvfb/Openbox run.

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
