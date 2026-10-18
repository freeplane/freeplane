# Design: X11 Popup Coordinate Calibration and Convergent Frame Resynchronization

- **Date:** 2026-10-17
- **Status:** Approved for specification
- **Epoch:** 2 replacement design
- **Scope:** Freeplane heavyweight popup placement and frame geometry on native X11 and XWayland
- **Related change:** PR #2983, `1b55f1c21d83cf243505c9a48c581a88ed3c713f`

## Problem

PR #2983 repairs stale AWT frame coordinates so FlatLaf heavyweight popups stay attached to their invoker after an X11 window-manager move, maximize, or restore. Its normal-window correction remains valid and must be preserved.

The epoch-1 maximized branch compared the Java frame origin with the complete `GraphicsConfiguration` screen bounds. A window manager may correctly maximize into a work area that excludes a panel or dock. In the observed KWin/X11 session, the screen bounds were `(0, 0, 2240, 1400)` while the usable work area was `(0, 32, 2240, 1368)`. Repeated correction requests then consumed the EDT without converging.

The epoch-1 work-area and retry changes bound that feedback loop, but native-X11 regression evidence exposed a second cache split. A frame write updates AWT component bounds, while popup placement can still read a stale X11 peer origin. With a fault-injected stale peer origin `(240, 180)`, the popup placement error was exactly `(240, 180)`. A native peer refresh corrected that cache only partially. The merged frame resynchronizer also reached `corrections=10 originMatches=false` in the external-maximize scenario.

The repair therefore needs two coordinated paths:

1. Keep the frame resynchronizer convergent when the window manager reserves screen insets.
2. Correct the popup request at creation time using the native frame origin and the popup owner's local position, without repeatedly writing frame geometry.

## Goals

- Make maximized-frame resynchronization converge on the monitor work area, including top-panel and dock insets.
- Preserve the normal-window native-coordinate correction from PR #2983.
- Preserve correct full-screen maximization when all screen insets are zero.
- Make heavyweight FlatLaf menus and other popups persist and remain selectable on native X11 with a panel strut present.
- Apply the same popup and frame behavior to native X11 and XWayland, which both use `sun.awt.X11.XToolkit`.
- Keep macOS, Windows, and other non-X11 runtime behavior unchanged.
- Bound repeated frame correction attempts and make termination observable in unit tests.
- Make popup target resolution and fallback behavior testable without a desktop session.
- Preserve the current FlatLaf popup implementation as the active delegate.

## Non-Goals

- Replacing or subclassing FlatLaf's `FlatPopupFactory`.
- Changing popup selection for macOS, Windows, or any non-X11 toolkit.
- Reworking native X11 querying, monitor selection, or normal-window size gating beyond the seams required for popup calibration.
- Adding `XDG_SESSION_TYPE` branching.
- Moving popup-specific corrections into individual menu, combo-box, tooltip, or popup classes.
- Hiding the problem with a panel auto-hide workaround.

The epoch-1 non-goal that prohibited all `PopupFactory` seam changes is explicitly superseded by this epoch-2 design. The shared factory is wrapped only to delegate to the existing active factory; FlatLaf's selection and behavior remain intact.

## Design

### Work-area target geometry

The maximized target represents the usable work area rather than the complete monitor rectangle. For each `GraphicsConfiguration`, derive:

```text
workArea.x      = screen.x + insets.left
workArea.y      = screen.y + insets.top
workArea.width  = screen.width  - insets.left - insets.right
workArea.height = screen.height - insets.top  - insets.bottom
```

A valid zero-inset result is a valid work area and preserves full-screen maximization. A missing configuration, failed inset lookup, or non-positive derived dimension produces no work-area rectangle; it never becomes a full-screen fallback.

Target resolution tries, in order:

1. the work area intersecting the native frame bounds;
2. the work area intersecting the saved normal bounds;
3. the frame graphics configuration's own derived work area.

If no work area is available, maximized resynchronization is a stable no-op for that event. The existing largest-intersection monitor selection remains in place. Normal windows continue to use native frame bounds and the existing size-agreement check; they do not consume work-area rectangles.

### Maximized correction guard

The maximized path uses an instance-local guard keyed by the exact target rectangle. It permits at most ten `setBounds` requests for one unchanged target. A request is counted before calling `setBounds`. The guard resets when the current frame equals the target, when the target changes, and when the frame leaves maximized state. Exhaustion is a stable result and cannot schedule another deferred retry for that unchanged target.

The full-rectangle comparison is confined to the maximized predicate. The shared origin-only `needsFrameResynchronization` predicate and the normal-window native-coordinate correction remain unchanged.

### Popup factory seam

`FrameResynchronizer.install(JFrame)` registers the Freeplane frame for popup calibration and ensures that the current shared `PopupFactory` is wrapped by a package-private delegating adapter. The adapter overrides the public `getPopup(Component owner, Component contents, int x, int y)` entry point, corrects only the requested screen coordinates, and delegates to the captured active factory.

The captured delegate is normally FlatLaf's `FlatPopupFactory`. The adapter does not reproduce or bypass FlatLaf's heavyweight selection, tooltip adjustment, popup reuse, border handling, or platform behavior. It passes the corrected coordinates into the same public method that Freeplane would otherwise call.

The adapter must not wrap another instance of itself. If a look-and-feel change replaces the shared factory, the refresh operation wraps the new active factory. If the current shared instance is already the adapter, frame registration is updated without adding another layer.

Only registered Freeplane top-level frames participate. A popup owned by a component in an unrelated window is delegated unchanged. A popup owned by a child window created from an already-created popup is also delegated unchanged unless that child is independently registered; its position was produced by the already-calibrated parent popup.

### Owner-local native calibration

The correction is computed at popup creation time. It does not use the frame's `getLocationOnScreen()` as the sole source of truth because the external-maximize evidence showed that the frame's cached screen origin can remain stale while a child component's screen location is already native-correct.

For an owner component inside a registered frame:

1. Query the native outer frame bounds through the existing `FrameResynchronizer.getNativeWindowBounds(frame)` method. Its origin is in logical screen coordinates after the existing graphics-transform conversion.
2. Convert `(0, 0)` from the owner to the frame with `SwingUtilities.convertPoint(owner, 0, 0, frame)`.
3. Derive the expected native owner location:

   ```text
   expectedOwnerScreen = nativeFrameBounds.origin + ownerLocationWithinFrame
   ```

   The frame decoration offset cancels because the native query returns the outer frame origin and the owner point is expressed in the frame's outer coordinate system. This is equivalent to the probe's native-client-plus-insets calculation.
4. Read `owner.getLocationOnScreen()` as the AWT location used by the popup request.
5. Apply:

   ```text
   correction = expectedOwnerScreen - actualOwnerScreen
   correctedX = requestedX + correction.x
   correctedY = requestedY + correction.y
   ```

When the AWT owner location already agrees with the native calculation, the correction is `(0, 0)`. In the split-cache fault, the correction is the negative of the injected peer-cache offset, removing the measured popup placement error.

The calculation is a single per-request translation. It does not call `setBounds`, mutate the owner or frame, reschedule the frame timer, or retry after delegation.

### Installation and look-and-feel lifecycle

The initial installation happens from `FrameResynchronizer.install(frame)`, after FlatLaf has initialized and before the frame is shown. The existing early system look-and-feel call in `OSKeyBindingManager` occurs before the frame is registered and is therefore covered by this later installation.

`FrameController.setLookAndFeel(...)` refreshes the adapter after each successful `UIManager.setLookAndFeel(...)` operation. This ensures a newly installed FlatLaf factory is wrapped while preserving the factory selected by the current look and feel. The refresh operation is a no-op outside the X11 toolkit gate.

The registered frame collection uses weak keys. Disposed frames can therefore be collected without a new teardown protocol. The global adapter retains only its delegate and the weak registry; it does not retain disposed application windows strongly.

### Toolkit boundaries

The existing exact toolkit predicate remains authoritative:

```text
sun.awt.X11.XToolkit
```

Native X11 and XWayland both participate. On macOS the toolkit is a macOS AWT toolkit, and on Windows it is a Windows AWT toolkit; neither installs the adapter or performs native calibration. The existing frame resynchronizer and the new popup path therefore pass through unchanged on those systems. No `XDG_SESSION_TYPE` check is added.

The Java source and unit tests compile on all supported platforms, but the native popup correction is runtime-disabled outside X11.

## Error Handling

- A non-X11 toolkit never installs the calibration adapter.
- An owner outside a registered frame is delegated with its original coordinates.
- A missing native frame, missing peer shell, failed X11 translation, unavailable display, or implausible native geometry returns the original popup coordinates.
- An owner that is not showing, cannot be converted into frame coordinates, or throws while reporting its screen location is delegated unchanged.
- Calibration failures do not prevent the delegate factory from creating a popup.
- Exceptions from the delegate factory itself are not converted into calibration failures; existing delegate behavior is preserved.
- A missing or invalid maximized work area is a stable no-op.
- Exhausting the maximized correction budget stops correction for the unchanged target and prevents an EDT feedback loop.

## Test Strategy

### Desktop-free unit tests

Add focused tests around the existing `FrameResynchronizerTest` and the popup calibration module:

- work-area derivation with top, bottom, left, and zero insets;
- native, normal, and fallback target resolution;
- unavailable target as a stable no-op;
- exact full-rectangle maximized comparison;
- exactly ten maximized requests for one unchanged target, with the eleventh rejected;
- guard reset after convergence, target change, and maximize-to-normal transition;
- owner-local correction with zero, positive, and negative deltas;
- unchanged coordinates when the toolkit, owner, native bounds, or owner location is unavailable;
- delegation with corrected coordinates and exactly one delegate call;
- no frame mutation or retry scheduling from popup calibration;
- no double wrapping;
- refresh after the shared look-and-feel factory changes;
- unchanged behavior for non-X11 toolkits;
- existing normal-window native-coordinate correction, work-area selection, and FlatLaf selection tests.

Tests use injected native and AWT geometry providers for the pure calibration calculation. They do not create a native desktop window merely to verify integer coordinate arithmetic.

### Native-X11 acceptance measurement

Run a real Freeplane build on native X11 with the Plasma panel strut present and record both baseline and patched runs. The log must contain:

- toolkit class and active popup factory delegate;
- owner AWT screen location;
- owner location within the frame;
- native frame bounds;
- expected native owner location;
- computed correction;
- popup request coordinates;
- popup AWT and native bounds;
- popup placement error;
- menu persistence and a successful menu-item selection event;
- frame correction count and evidence that opening the popup adds no frame correction loop.

The existing split-cache evidence supplies the baseline error `(240,180)`. The patched run must show the corresponding owner-local correction and a native popup placement error of zero, subject to the display's integer-pixel tolerance. The same acceptance sequence covers maximization, remaximization, and restore.

Run the equivalent XWayland acceptance path where available. Run ordinary popup smoke checks on macOS and Windows to confirm the non-X11 pass-through path, but do not claim native calibration there.

## Acceptance

The epoch-2 replacement is acceptable only if:

1. Maximized work-area and guard tests pass, including the exact ten-request termination boundary.
2. Popup calibration tests prove owner-local correction, no-op fallback, delegate preservation, and no frame mutation.
3. The active FlatLaf factory remains the delegate; no FlatLaf subclass or replacement implementation is introduced.
4. Native X11 with a top-panel strut shows persistent, selectable heavyweight menus with no material popup placement error.
5. The native-X11 log shows the expected correction and a bounded frame correction count; opening a popup does not restart frame resynchronization.
6. Native X11 and XWayland use the same X11 geometry contract.
7. macOS, Windows, and other non-X11 toolkits do not install the adapter and retain their existing popup behavior.
8. Existing normal-window native-coordinate correction and zero-inset full-screen maximization remain covered and passing.
9. No event-driven maximized correction path can issue unbounded repeated `setBounds` calls or restart timers for one unchanged target.
10. Any unavailable native-X11 runtime environment is reported as a verification limitation rather than inferred away.
