# X11 Popup Calibration Acceptance Evidence

## Status

The canonical four-scenario pointer acceptance **passes on isolated Xvfb/Openbox**. It compiled with `javac --release 8`, exited `0`, and recorded `failure-class=NONE` four times, pointer selection four times, native placement error `(0,0)`, popup persistence above 800 ms before the pointer, and `frame-correction-setBounds-delta=0` in every scenario. All three required negative controls exited `1` with `failure-class=PRODUCT` four times.

The host session is Wayland with XWayland available on `DISPLAY=:1` and `:2`; the isolated acceptance display is Xvfb. **XWayland-only runtime acceptance and native X11 KWin runtime acceptance remain UNVERIFIED.**

## Commands and artifacts

The four required runs executed sequentially on the same private display:

```bash
JAVA_HOME=/home/henry/.sdkman/candidates/java/21.0.8-zulu \
PATH="/home/henry/.sdkman/candidates/java/21.0.8-zulu/bin:$PATH" \
python3 ai-specs/tasks/freeplane-frame-resynchronizer-x11-evidence/epoch-2/run-acceptance.py \
  --mutant-offset 240,180 --negative-controls
```

The runner itself exited `0`, meaning the canonical pass and expected mutant failures were validated. The individual Java probe exits were `0, 1, 1, 1`; no probe timed out. The complete runner summary is in [`logs/runner.stdout`](logs/runner.stdout), with its exit in [`logs/runner.result.txt`](logs/runner.result.txt).

| Run | Complete stdout | Exit record | Probe exit | Classification |
| --- | --- | --- | ---: | --- |
| Canonical | [`probe.stdout`](logs/probe.stdout) | [`probe.result.txt`](logs/probe.result.txt) | 0 | `NONE` x4 |
| `(240,180)` displacement | [`falsifiability-shift-240-180.stdout`](logs/falsifiability-shift-240-180.stdout) | [`result`](logs/falsifiability-shift-240-180.result.txt) | 1 | `PRODUCT` x4 |
| Eleven frame writes per popup | [`falsifiability-frame-writes-11.stdout`](logs/falsifiability-frame-writes-11.stdout) | [`result`](logs/falsifiability-frame-writes-11.result.txt) | 1 | `PRODUCT` x4 |
| Consuming item | [`falsifiability-consuming-item.stdout`](logs/falsifiability-consuming-item.stdout) | [`result`](logs/falsifiability-consuming-item.result.txt) | 1 | `PRODUCT` x4 |

Exact compile and Java commands are in [`logs/commands.txt`](logs/commands.txt). Each run has separate compiler and probe stdout/stderr files. All four compilations exited `0`; compiler stderr contains only the three Java 8 obsolete-option warnings, and probe stderr files are empty. Source hashes, Java identity, host display inventory, and the Git HEAD at execution are in [`logs/runtime-identity.txt`](logs/runtime-identity.txt).

The committed probe modes are selected with `-Dfreeplane.acceptance.mode=frame-writes-11` and `-Dfreeplane.acceptance.mode=consuming-item`; the default is `canonical`. The displacement control still recompiles a disposable probe copy with only its scenario `menu.show(...)` offset changed. Production classes are shared unchanged by all variants.

### Retry and failure diagnosis

The runner records one JSON object per probe attempt and diagnosis in [`logs/attempts.jsonl`](logs/attempts.jsonl), including the attempt number, phase, exit code, timeout flag, classification presence, outcome, reason, display, and the last eight stdout/stderr lines for failures. A probe or display-acquisition attempt is retried at most three times only while no scenario classification has been produced. The backoff is linear: one second after attempt 1 and two seconds after attempt 2. Any `failure-class=PRODUCT`, `failure-class=ENVIRONMENT`, or `failure-class=NONE` record ends retrying immediately; PRODUCT controls therefore cannot be masked by a retry. Each retry closes the runner-owned Xvfb/Openbox pair and acquires a fresh private display. The deterministic retry-boundary check is recorded in [`logs/retry-policy-check.result.txt`](logs/retry-policy-check.result.txt): a PRODUCT-classified failure made one probe call with zero display closes, while a pre-classification failure waited one second, closed the display once, and succeeded on attempt 2.
### Reproducibility runs

Three fresh canonical invocations completed with a classification record on every run. Each completed on attempt 1, so no retry was needed in this sample. The per-run stdout, stderr, and result records are [`logs/repro-run-1.*`](logs/repro-run-1.stdout), [`logs/repro-run-2.*`](logs/repro-run-2.stdout), and [`logs/repro-run-3.*`](logs/repro-run-3.stdout); the combined attempt ledger is [`logs/attempts.jsonl`](logs/attempts.jsonl).

| Invocation | Runner exit | Canonical probe exit | Classification record | Attempts |
| --- | ---: | ---: | --- | ---: |
| Repro run 1 | 0 | 0 | yes (`NONE` x4) | 1 |
| Repro run 2 | 0 | 0 | yes (`NONE` x4) | 1 |
| Repro run 3 | 0 | 0 | yes (`NONE` x4) | 1 |

The one required frame-write negative-control invocation reused the isolated runner with `--negative-controls`. Its runner exit was `0` after validating the expected controls; the frame-write probe itself exited `1`, produced `PRODUCT` classification records for all four scenarios, and had one terminal classified attempt. The result is in [`logs/runner.result.txt`](logs/runner.result.txt), with runner output in [`logs/repro-negative-controls.stdout`](logs/repro-negative-controls.stdout) and the frame-write probe output in [`logs/falsifiability-frame-writes-11.stdout`](logs/falsifiability-frame-writes-11.stdout). The diagnosis ledger records the frame-write and consuming-item PRODUCT failures with `retry disabled`.

## Display isolation and cleanup

The successful runs used:

```text
DISPLAY=:77
XDG_SESSION_TYPE=wayland
selected display kind=private Xvfb with Openbox window manager
screen=2240x1400x24
screen-bounds=(0,0,2240,1400)
window-manager=Openbox
```

The runner selects a free display in `:77` through `:99`, starts its own Xvfb and Openbox process groups, and terminates those groups in cleanup. Openbox supplies activation, maximize, and restore support. The shared XWayland displays are inspected for identity only. Every scenario frame activated on attempt 1 in all four required runs.

Both the main runner and the supplemental budget runner cleaned up their private display processes. The pre-existing `Xvfb :91` process, PID `4168168`, was preserved. Cleanup verification is recorded in [`logs/process-cleanup.txt`](logs/process-cleanup.txt).

## Enforcement rules

The probe emits exactly one authoritative `failure-class=<PRODUCT|ENVIRONMENT|NONE> reason=<short> scenario=<name>` line per scenario. Classification never supplies the exit gate by itself: failed invariants are also passed to `require(...)`, which records a failure and makes `main` exit `1`.

1. **Zero popup-triggered frame requests.** `CountingFrame.setBounds(int,int,int,int)` counts both Java `setBounds` overloads exactly once, including requests that repeat the current rectangle without visibly moving the frame. The scenario delta spans activation and popup creation through persistence, pointer interaction, diagnostic fallback, and popup cleanup. Any nonzero delta fails acceptance and has classification precedence: `failure-class=PRODUCT reason=popup-triggered frame correction`.
2. **At most ten requests for an unchanged maximized target.** Counting starts before each maximize/restore transition. While maximized, each requested rectangle is compared with the previous requested target; an unchanged rectangle increments the target count and a changed rectangle starts a new count. The maximum count and its target are retained through the scenario, so a subsequent target change cannot hide an earlier overrun. A new explicit state transition resets the measurement. A maximum above 10 fails acceptance and classifies `PRODUCT`, including writes during state settling before popup measurement starts.
3. **Placement and click containment remain product gates.** A click outside the measured item bounds classifies `PRODUCT: popup misplaced; click landed outside item bounds`. Native placement error above the unchanged one-pixel tolerance classifies `PRODUCT: popup misplaced; placement error exceeds tolerance`. The pointer target remains derived from the expected calibrated popup origin, so it cannot follow a displaced popup.
4. **Missing selection defaults to PRODUCT.** Delivered item events are recorded independently of the action listener. With no selection, recorded receipt produces `PRODUCT: pointer selection missing after delivered click`; absence of receipt evidence produces `PRODUCT: pointer selection missing; event delivery unproven`. Missing action, in-bounds geometry, and an active frame cannot establish an environment failure.
5. **ENVIRONMENT requires positive independent evidence.** The remaining environment branch requires a frame that could not be activated after five bounded attempts and zero recorded item press/release events. It still exits nonzero. Measured product violations above take precedence. A placement-caused outside click remains `PRODUCT`.

The enforcement and classification are in [`PopupCoordinateAcceptanceProbe.java`](PopupCoordinateAcceptanceProbe.java): `runScenario` contains both `require(...)` gates, `logScenarioFailureClass` assigns the authoritative reason, and `CountingFrame` implements request accounting. The item subclass in `runScenario` records delivered events and implements the consuming control. [`run-acceptance.py`](run-acceptance.py) checks scenario identity, classification cardinality, exit status, persistence, geometry, request deltas, and delivered press/release witnesses for the new controls.

All prior persistence, placement, selection, activation, native-evidence, toolkit, factory/delegate, correction, and popup-class parity gates remain enforced. The diagnostic `JMenuItem.doClick()` occurs after pointer evidence is captured and cannot satisfy pointer selection. Robot still supplies the actual button press/release; XTest synchronizes pointer motion.

## Canonical run

The probe exited `0`. Each popup persisted above 800 ms before the pointer. The menu was visible immediately before the click and hidden after pointer selection, before the fallback. All clicks were inside the item bounds, and the action listener received `selected-<scenario>` during the pointer phase.

| Scenario | Persistence | Pointer selection | Placement error | Popup frame-write delta | Maximum unchanged maximized-target requests |
| --- | ---: | --- | --- | ---: | ---: |
| `startup-normal` | 911 ms | true | `(0,0)` | 0 | 0 |
| `maximize` | 903 ms | true | `(0,0)` | 0 | 10 |
| `restore` | 903 ms | true | `(0,0)` | 0 | 0 |
| `remaximize` | 903 ms | true | `(0,0)` | 0 | 10 |

```text
probe-exit-code=0
failure-class=NONE reason=all scenario gates passed scenario=startup-normal
failure-class=NONE reason=all scenario gates passed scenario=maximize
failure-class=NONE reason=all scenario gates passed scenario=restore
failure-class=NONE reason=all scenario gates passed scenario=remaximize
```

The adapter and FlatLaf delegate both produced `javax.swing.Popup$HeavyWeightWindow` for heavyweight menus, lightweight-enabled menus, and tooltips. The split-cache correction assertion returned `(-240,-180)` and the matched-geometry assertion returned `(0,0)`.

## Negative controls

### `(240,180)` displacement: placement and intended-click gates

Only the disposable probe call changes to `menu.show(owner, evidence.popupLocal.x + 240, evidence.popupLocal.y + 180)`. All four scenarios measured placement error `(240,180)`, an intended click outside the actual item, no pointer selection, and dismissal by the outside pointer click. Persistence was `909, 903, 903, 902` ms; frame-write deltas stayed zero.

```text
probe-exit-code=1
failure-class=PRODUCT reason=popup misplaced; click landed outside item bounds scenario=startup-normal
failure-class=PRODUCT reason=popup misplaced; click landed outside item bounds scenario=maximize
failure-class=PRODUCT reason=popup misplaced; click landed outside item bounds scenario=restore
failure-class=PRODUCT reason=popup misplaced; click landed outside item bounds scenario=remaximize
```

This control proves the placement/containment failure still exits nonzero and cannot be relabelled as an environment dispatch failure.

### Eleven frame writes per popup: zero-write gate

The committed `frame-writes-11` mode issues eleven `frame.setBounds(...)` requests to one unchanged rectangle immediately after each scenario popup is shown. It alternates the rectangle and integer overloads to expose incomplete accounting. Every scenario records `frame-correction-setBounds-delta=11`, with successful pointer selection, placement error `(0,0)`, and persistence of `909, 903, 902, 903` ms. Thus selection or placement failure cannot explain rejection.

```text
probe-exit-code=1
failure-class=PRODUCT reason=popup-triggered frame correction scenario=startup-normal
failure-class=PRODUCT reason=popup-triggered frame correction scenario=maximize
failure-class=PRODUCT reason=popup-triggered frame correction scenario=restore
failure-class=PRODUCT reason=popup-triggered frame correction scenario=remaximize
```

The log also contains four `assertion-failure=popup-triggered frame correction ... setBounds-delta=11` records. Both maximized scenarios separately report `requests=11 limit=10`, `within-budget=false`, and a maximized-target budget assertion. The zero-write gate has classification precedence, so its exact required reason is preserved in all four scenarios.

### Consuming item: delivered-click selection gate

The committed `consuming-item` mode logs and consumes item `MOUSE_PRESSED` and `MOUSE_RELEASED` events before Swing can invoke its normal mouse action. All four scenarios record one delivered press and one delivered release with `consumed=true`, an active frame, an in-bounds intended click, placement error `(0,0)`, and zero popup frame writes. Pointer selection remains false even though the later diagnostic fallback succeeds. Persistence was `907, 901, 901, 903` ms.

```text
probe-exit-code=1
failure-class=PRODUCT reason=pointer selection missing after delivered click scenario=startup-normal
failure-class=PRODUCT reason=pointer selection missing after delivered click scenario=maximize
failure-class=PRODUCT reason=pointer selection missing after delivered click scenario=restore
failure-class=PRODUCT reason=maximized frame target budget exceeded scenario=remaximize
```

The remaximize transition also recorded 15 requests for the unchanged `(0,0,2240,1400)` target before popup measurement. That independent product violation has classification precedence and is retained in the evidence. The delivered click and missing-selection assertions are present in all four scenarios; none is classified `ENVIRONMENT`. No production change was made to conceal or repair this additional diagnostic.

### Independent maximized-budget control

To prove the target budget is enforced independently of the zero-popup-write gate, a supplemental disposable copy adds eleven unchanged-target requests after each maximized transition settles, before `runScenario` takes its popup-write baseline. The exact addition is preserved in [`logs/falsifiability-maximized-target-budget-11.patch`](logs/falsifiability-maximized-target-budget-11.patch); it removes or disables no gate.

Reproduce after the main runner with:

```bash
JAVA_HOME=/home/henry/.sdkman/candidates/java/21.0.8-zulu \
python3 ai-specs/tasks/freeplane-frame-resynchronizer-x11-evidence/epoch-2/run-maximized-budget-control.py
```

The supplemental probe exited `1`, with zero popup frame writes, successful pointer selection, and placement error `(0,0)` in all scenarios. Its only two assertion failures are maximized-target budget overruns. The observed maximum counts were 15 for maximize and 11 for remaximize; the maximum records include the transition's production requests as well as the injected requests.

```text
probe-exit-code=1
failure-class=NONE reason=all scenario gates passed scenario=startup-normal
failure-class=PRODUCT reason=maximized frame target budget exceeded scenario=maximize
failure-class=NONE reason=all scenario gates passed scenario=restore
failure-class=PRODUCT reason=maximized frame target budget exceeded scenario=remaximize
independent-budget-validation=True
```

Full stdout, stderr, compiler diagnostics, commands, runtime identity, and exit status use the unambiguous `logs/falsifiability-maximized-target-budget-11.*` prefix. The helper exited `0` after validating the expected negative result. This establishes that exceeding ten requests is itself sufficient to fail acceptance.

### Reproduction before enforcement

The committed modes were run before the new gates were added. The frame-write mode exited `0` and classified all four scenarios `NONE`; the old rectangle-only counter saw six of the eleven alternating-overload requests. The consuming-item mode exited `1` but classified all four scenarios `ENVIRONMENT` despite logged delivered press/release events. The stricter runner rejected both controls and exited `1`.

These original failing checks are retained in `logs/pre-enforcement-frame-writes-11.*`, `logs/pre-enforcement-consuming-item.*`, and `logs/pre-enforcement-runner.*`, with their original commands and runtime identity under the same prefix. They show that the controls expose the demonstrated defects rather than merely producing expected failures after implementation.

## Regression and source boundaries

All modifications for the F2 harness fix are confined to `ai-specs/tasks/freeplane-frame-resynchronizer-x11-evidence/epoch-2/`. The F1 production seam and focused tests are the only changes outside that directory. Java probe compilation, the canonical GUI runs, the frame-write negative control, source/hash checks, log consistency checks, and process cleanup were verified for this update.

The required full regression command was run with `gradle :freeplane:test --rerun-tasks` and exited `0`. Aggregating `freeplane/build/test-results/test/*.xml` produced `742 tests, 0 failures, 0 errors, 2 skipped`. The focused `FrameResynchronizerPopupFactoryTest` suite also passed after the mutation was restored. Current production source hashes are recorded in `logs/runtime-identity.txt` and checked against the source tree during evidence verification.

## Limitations

The host has `kwin_wayland` with XWayland displays `:1` and `:2`; `kwin_x11=NONE`. The canonical pass was obtained on isolated Xvfb with Openbox. Therefore:

- XWayland-only runtime acceptance: **UNVERIFIED**.
- Native X11 KWin runtime acceptance: **UNVERIFIED**.
- Isolated Xvfb/Openbox canonical acceptance: **PASS**, as documented above.

The shared XWayland activation limitation remains unresolved by these isolated-display results. No native X11 KWin or shared XWayland pointer acceptance claim is made.
