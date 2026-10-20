# Native X11/KWin acceptance report

This record preserves the native-X11 report supplied by the tester for this
change. It is a report of the tester's run, not a new run on the PR preparation
machine. The tested checkout was
`e3555cf5162aa74f92e8083c8b3fdccf950c4c8a` and included Graph Workspace.

The test scope is one connected display on native Xorg/KWin, with FlatLaf
heavyweight popups. Cases A–G and I passed; multi-monitor case H was not exercised.
The complete measurements and qualifications follow in
[tester-report.md](tester-report.md).

## Environment and scope

| Item | Reported value |
| --- | --- |
| Display server | Native Xorg + `kwin_x11`, `DISPLAY=:0`, no `WAYLAND_DISPLAY` |
| Desktop | KWin 6.7.5-3 / Plasma 6.7.5-1 |
| Application runtime | Arch OpenJDK 21.0.12.1 (`jre21-openjdk`) |
| Toolkit | `sun.awt.X11.XToolkit`; the reported property key was `awt.toolkit` |
| Look and feel | `com.formdev.flatlaf.FlatIntelliJLaf` |
| Display | One connected eDP-1 output, 2240×1400 |
| Work area | `0,32 2240x1368` (32 px top panel) |
| Harness compiler | JDK 27 `javac --release 8`; the application ran on JRE 21 |
| Profile | Disposable byte-copy of the real profile, selected with `-U` |

The headless measurement shell printed an empty `XDG_SESSION_TYPE`. The tester
identified native X11 from Xorg, `kwin_x11`, its session environment, and the
absence of `WAYLAND_DISPLAY`. The implementation gates on the actual toolkit
class, without consulting session environment variables.

## Results

| Case | Surface or transition | Reported result |
| --- | --- | --- |
| A | Unmaximized menus at three positions | PASS: 0 px gap and 0 px horizontal offset |
| B | Maximized File/Edit/View/Navigate menus | PASS: all native popup tops at y=58, with 0 px gap/offset |
| C | Navigate → Presentation submenu | PASS: no background gap; stock Swing submenu placement |
| D | Pointer selection of Navigate → Go to root | PASS: selected root and popup windows unmapped |
| E | Three toolbar combo dropdowns | PASS: tester reports 0 px gap/offset; see original measurements |
| F | Maximize transition and request count | PASS: 0 patched `setBounds` requests, 0 popup-triggered requests |
| G | Graph Workspace normal and maximized | PASS: 0 px gap/offset in the separate graph-enabled test checkout |
| H | Multiple displays | N/A: only one connected display |
| I | Unmaximize and immediate menu opening | PASS: exact normal geometry restored; menus flush |

For case F, the tester reported **0** maximize requests on the patched build and
**2** on base commit `cb8782fb37` with the same harness. There was exactly one
geometry transition in 336 samples over 2.5 seconds. A menu opened about 60 ms
after maximize had the same rectangle as one opened three seconds later.

These native-X11 counts are separate from the earlier XWayland measurements;
they are not pooled into a single before/after count.

## Relationship to the PR source

The test checkout and the original PR integration tip `49d8878b35` have different
whole-tree hashes. The test checkout includes Graph Workspace and other changes
that are outside this PR. During preparation, Git blob IDs and SHA-256 values were
compared for all four production files touched by the fix:

- `org/freeplane/core/ui/components/FrameResynchronizer.java`
- `org/freeplane/core/ui/components/PopupCoordinateCalibrator.java`
- `org/freeplane/core/ui/components/X11PopupFactory.java`
- `org/freeplane/features/ui/FrameController.java`

All four match byte for byte between the tested checkout and the PR source.
[provenance.json](provenance.json) records the exact commits, whole-tree hashes,
per-file blob IDs, and source SHA-256 values. This establishes source equivalence
for the fix; it does not claim whole-application or runtime-environment identity.
Graph Workspace case G is integration coverage from that checkout; the graph
plugin itself is outside this PR.

## Artifact availability

The tester listed ten screenshots under `/tmp/fp-test/`, including normal and
maximized menus, a cascading submenu seam, a combo dropdown, and Graph Workspace.
That directory was not transferred to the PR preparation machine, and none of
those native-X11 images is included in this record. Their original filenames are
preserved in [tester-report.md](tester-report.md) and marked unavailable in
[provenance.json](provenance.json). The native-X11 evidence available here is the
reported environment, measurements, pointer-selection result, and request counts.

The PR also includes actual archived
[XWayland screenshots](../xwayland-20261019/report.md) from the separate developer
run. Those images are identified as XWayland and do not stand in for the missing
native-X11 screenshots.

## Qualifications

FlatLaf is material to this test: the tester verified that Metal used in-frame
lightweight menus and did not exercise this heavyweight popup path. A submenu
owned by a popup window is not calibrated; case C verifies that stock Swing
placement remains intact.

No Gradle suite was run as part of the native-X11 session. The PR's automated
regression and build results are separate evidence from the integration or
delivery-candidate worktree. The original report's note about graph-plugin tests
does not describe the module set of this PR.

For the combo measurements, the original report lists a control border row of
388 and a native popup top row of 391, while reporting zero gap relative to the
control border box. Without the source image, that row relationship cannot be
independently recomputed here; the original numbers and tester's conclusion are
both preserved.

The tester reported terminating a pre-existing Freeplane instance to free the
single-instance slot. This is recorded as a test-isolation side effect; any loss
of unsaved changes cannot be established from the on-disk map. Personal document
identifiers in that note are redacted in the committed copy. The original report
is retained in the external PM archive.
