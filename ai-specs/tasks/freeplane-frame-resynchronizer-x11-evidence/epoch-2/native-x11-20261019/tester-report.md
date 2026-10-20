The test result on native X11 is ready:

All cases measured. Here is the report.

```
Environment
  Display server:      X11  (native Xorg + kwin_x11; DISPLAY=:0, XDG_SESSION_TYPE=x11,
                             no WAYLAND_DISPLAY — this is the X11 Plasma session, not XWayland)
  Desktop / version:   KWin 6.7.5-3 / Plasma 6.7.5-1
  Java:                OpenJDK 21.0.12.1 (jre21-openjdk, Arch build) for the app;
                       awt.toolkit = sun.awt.X11.XToolkit
  Resolution:          2240x1400, 1 monitor (eDP-1; HDMI-1 + DP-1..4 disconnected)
                       work area = 0,32 2240x1368 (32 px top panel)
  awt.toolkit.name:    (property unset; the printed key is `awt.toolkit`)
                       awt.toolkit = sun.awt.X11.XToolkit
  Build under test:    e3555cf5162aa74f92e8083c8b3fdccf950c4c8a, freeplaneviewer.jar
                       contains FrameResynchronizer, PopupCoordinateCalibrator,
                       X11PopupFactory  ->  patch is in the tested binary

Results
  A  unmaximized menu       PASS
  B  maximized menu         PASS
  C  cascading submenu      PASS
  D  popup selection        PASS
  E  combo dropdown         PASS
  F  maximize transition    PASS
  G  graph workspace        PASS
  H  multi-monitor          N/A (single connected output)
  I  unmaximize transition  PASS
```

## Measurements (one success case, pixel- and X11-verified)

Window moved away from the corner, unmaximized; Edit menu opened:

```
$ xdotool getwindowgeometry --shell "$(xdotool getactivewindow)"
WINDOW=71303175
X=604
Y=284
WIDTH=1100
HEIGHT=780

$ xprop -id "$(xdotool getactivewindow)" _NET_FRAME_EXTENTS
_NET_FRAME_EXTENTS(CARDINAL) = 4, 4, 34, 6

$ echo "XDG_SESSION_TYPE=$XDG_SESSION_TYPE WAYLAND_DISPLAY=$WAYLAND_DISPLAY DISPLAY=$DISPLAY"
XDG_SESSION_TYPE= WAYLAND_DISPLAY= DISPLAY=:0      <- this shell is headless;
                                                      kwin_x11's own env says x11
```

| quantity | value |
|---|---|
| menu-bar `Edit` item box (hover highlight) | x 643..684, y 284..309 |
| popup native X11 window | `x=643 y=310 w=267 h=573` |
| popup visible panel top border row | 310 (= item bottom + 1) → **gap 0 px** |
| popup visible panel left border column | 643 (= item left) → **dx 0 px** |
| popup visible panel right border | 909 (= 643+267−1, matches native width) |

Maximized (`_NET_FRAME_EXTENTS = 0,0,0,0`, work area 0,32 2240x1368) — four menus:

| menu | item box | popup native window | gap | dx |
|---|---|---|---|---|
| File | x 0..38, y 32..57 | `0,58 328x591` | 0 | 0 |
| Edit | x 39..80 | `39,58 267x573` | 0 | 0 |
| View | x 137..183 | `137,58 418x381` | 0 | 0 |
| Navigate | x 247..322 | `247,58 424x712` | 0 | 0 |

Hovering while a menu is open (File→Edit→Insert→View→Navigate) re-positions the
popup to exactly each title's left edge at `y = item.bottom+1` every time, and
Escape unmaps it.

## Details per case

- **A — PASS.** Three different unmaximized positions tested (`604,284 1100x780`,
  `904,334 1000x700`, `254,214 1300x850`, each moved away from the corner).
  Every menu opened flush: gap 0 px, dx 0 px, popup width matches the native
  window width. No decoration-inset error (no `+34`/`+4` shift anywhere).
- **B — PASS.** File/Edit/View/Navigate flush, 0/0.
- **C — PASS.** Navigate → hover `Presentation`: parent popup native
  `x 247..670`, submenu native `x 666..972`, `y 249..490` aligned to the
  Presentation row. Pixel scan of that row found **zero** map-background
  columns between the panels — no gap. The windows overlap by 5 px and the
  submenu's 1 px border lands on the parent's border (parent's right border
  column 670, submenu visible panel from 666/667), i.e. one shared border line,
  which is FlatLaf's popup shadow/border inset, not a coordinate error. Note the
  submenu's owner is the parent popup window (not a registered `JFrame`), so the
  calibrator returns no correction for submenus — this geometry is stock Swing.
- **D — PASS.** `Navigate > Go to root`: selection moved from node A3 to the
  root, and both popup windows went `map=0` (trial-by-pointer worked, popup
  closed normally, no dismiss-on-release symptom).
- **E — PASS.** Three toolbar combos (`83%`, `Default` style, `Core, details or
  note`): control bottom border row 388, dropdown native top row 391, dropdown
  visible first row 391 → gap 0 from the control's border box; style combo
  dropdown left 1439 = control's left border 1439 → dx 0.
- **F — PASS, churn fix confirmed.** Instrumented `JFrame` (same technique as
  the branch's probe) on this KWin display:
  - patched build (`e3555cf`): `maximize-setBounds-requests = 0`,
    `popup-triggered-setBounds = 0`
  - base commit `cb8782fb37` (same harness): `maximize-setBounds-requests = 2`
    and AWT still reporting `y=0` while native client is `y=32`
  - 336 geometry samples over 2.5 s: exactly **one** transition
    (`328,420 1904x968` → `0,32 2240x1368` at t≈36 ms), nothing after
  - a File menu opened within ~60 ms of maximizing produced rect
    `(0,58)–(327,648)`, byte-identical to the same menu opened 3 s later
  - 30 fps x11grab of the transition: one maximize transition, no repeated
    resizing/flicker; no misplaced menu opened quickly after maximizing.
- **G — PASS.** Graph Workspace (`Ctrl+Alt+G` path: View → Open Graph Workspace).
  Unmaximized `1272x760+484+350` and maximized `2240x1368+0+32`: File/Edit/View/
  Maps all gap 0, dx 0 (e.g. maximized File item 0..38 → popup `0,58 180x147`).
- **H — N/A.** One connected output only.
- **I — PASS.** `904,334 1000x700` → maximize `0,32 2240x1368` → unmaximize
  returns exactly `904,334 1000x700` (also verified `254,214 1300x850`). Menu
  opened right after unmaximizing is flush (File `904,360`, Edit `943,360`,
  Navigate `1151,360` = item bottom+1, item left).

## Screenshots

```
/tmp/fp-test/FINAL-A-unmaximized-File-menu.png
/tmp/fp-test/FINAL-A-unmaximized-Edit-menu.png
/tmp/fp-test/FINAL-A-unmaximized-File-menu-crop.png
/tmp/fp-test/FINAL-B-maximized-File-menu.png
/tmp/fp-test/FINAL-B-crop.png
/tmp/fp-test/FINAL-C-cascading-submenu.png
/tmp/fp-test/FINAL-C-parent-menu.png
/tmp/fp-test/FINAL-C-seam-4x.png          (4x zoom of the parent/submenu seam)
/tmp/fp-test/FINAL-E-combo-dropdown.png
/tmp/fp-test/FINAL-G-graph-workspace-maximized-Edit-menu.png
```
Captured with `magick import -window root` (worked fine here; `spectacle` not needed).

**Failures: none.** No case produced a gap or a wrong-region overlap.

## Notes

1. **LAF matters for this test.** The shipped default is
   `lookandfeel=com.formdev.flatlaf.FlatIntelliJLaf` (heavyweight popups → real
   X11 popup windows). The profile on this machine had `lookandfeel=default`,
   which resolves to **Metal** here — Metal uses *lightweight* popups drawn
   inside the frame, so it cannot exercise the patched path at all (I verified
   this: with Metal the menus are in-frame pixel diffs, no `win1` window). All
   results above are from FlatLaf, i.e. the path the patch fixes.
2. **Test isolation.** A byte-copy of the real profile was used
   (`~/.config/freeplane/1.12.x` → `/tmp/fptest-config/freeplane`, launched with
   `-U`), so realistic settings without touching the real profile. The real
   profile still reads `lookandfeel=default` and has no lock file left.
3. **Side effect to flag:** launching required a free single-instance slot, and
   the pre-existing Freeplane instance (started 13:45 from this worktree on
   `[personal mind-map path redacted]`) was terminated in the process. Any unsaved edits
   in that window were lost — worth checking. My disposable instance is now
   closed; no Freeplane processes remain.
4. The brief's exact check `java -XshowSettings:properties -version | grep -i
   awt.toolkit.name` matches **nothing** — JDK prints the key as `awt.toolkit`.
   Its value is `sun.awt.X11.XToolkit`, so the patch's install condition is met
   (independently confirmed: a JVM on this display with the shipped jar reports
   `PopupFactory.getSharedInstance() = org.freeplane.core.ui.components.X11PopupFactory`).
5. Only a JRE 21 is installed (no `javac`), so the instrumentation harness was
   compiled with JDK 27 `javac --release 8`; the app itself ran on JRE 21.
6. I did not run `gradle`, so the two known-failing `AccessibleGraphCanvasShould`
   tests were not exercised and are irrelevant to this result.
7. One measurement pitfall worth noting for future runs: a whole-screen pixel
   diff between "hover" and "open" also catches mind-map canvas repaints
   (selection-focus ring), which can produce spurious left edges. The X11 popup
   window origin plus a targeted row/column scan is the reliable signal; both
   agreed to the pixel once the diff region was constrained.