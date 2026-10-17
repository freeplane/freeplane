# Design: Convergent Maximized-Frame Resynchronization

- **Date:** 2026-10-17
- **Status:** Approved for specification
- **Scope:** `FrameResynchronizer` maximized-window geometry on X11 and XWayland
- **Related change:** PR #2983, `1b55f1c21d83cf243505c9a48c581a88ed3c713f`

## Problem

PR #2983 repairs stale AWT frame coordinates so FlatLaf heavyweight popups stay attached to their invoker after an X11 window-manager move, maximize, or restore. Its normal-window correction is valid and must remain.

The maximized branch currently compares the Java frame origin with the complete `GraphicsConfiguration` screen bounds. A window manager may correctly maximize into a work area that excludes a panel or dock. In the observed KWin/XWayland session, the screen bounds are `(0, 0, 1920, 1080)` while the maximized frame is `(0, 32, 1920, 1048)`. The resynchronizer repeatedly requests the former, receives the latter, and processes another `COMPONENT_MOVED` event. Because deferred attempts are reset by each event, the loop has no effective upper bound and consumes the EDT.

Both native X11 and XWayland use `sun.awt.X11.XToolkit`, so the correction must be expressed in window geometry terms rather than by disabling the feature for a session type.

## Goals

- Make maximized-frame resynchronization converge when a window manager reserves screen insets.
- Preserve the normal-window native-coordinate correction that fixes the original X11 popup-placement defect.
- Preserve correct full-screen maximization behavior when no insets exist.
- Apply the same behavior to native X11 and XWayland.
- Bound repeated correction attempts as a defense against an unexpected non-converging window manager response.
- Make target resolution and termination behavior observable in unit tests without requiring a particular desktop session.

## Non-Goals

- Replacing FlatLaf's `FlatPopupFactory` or changing popup-factory selection.
- Changing Windows, macOS, or non-X11 toolkit behavior.
- Reworking native X11 querying, monitor selection, or normal-window size gating.
- Hiding the problem with a Wayland-only environment check.

## Design

### Work-area target geometry

The monitor target used by the maximized path will represent the usable work area, not the complete monitor rectangle. For each `GraphicsConfiguration`, derive:

```text
workArea.x      = screen.x + insets.left
workArea.y      = screen.y + insets.top
workArea.width  = screen.width  - insets.left - insets.right
workArea.height = screen.height - insets.top  - insets.bottom
```

The production resolver will enumerate the existing graphics-device/configuration set and create work-area rectangles from each configuration's bounds and `Toolkit.getScreenInsets(configuration)`. A valid zero-inset result is a valid work area and therefore preserves the existing full-screen behavior. A thrown inset lookup, a missing configuration, or non-positive derived dimensions produces no work-area rectangle; it is not converted to the complete screen rectangle.

The existing largest-intersection monitor selection remains in place, but it will select among these work-area rectangles. Native bounds are tried first; if they are absent or do not intersect a work area, the saved normal bounds are tried. If neither resolves to a work area, the frame graphics configuration is used as the fallback source for work-area resolution. If that configuration is unavailable or its work area is invalid, maximized resynchronization is a stable no-op for that event. An unavailable work area still means no maximized correction; it is never converted to the complete screen rectangle.

No normal-window code will consume work-area rectangles. Normal windows continue to compare their AWT bounds with native X11 frame bounds only after the existing size-agreement check.

### Maximized-only repeated-correction guard

The maximized path will use an instance-local guard separate from the normal-window path and from the existing deferred-timer counter. Its contract is:

- The key is the exact `(x, y, width, height)` of the current maximized work-area target.
- `MAX_MAXIMIZED_RESYNCHRONIZATION_ATTEMPTS` is `10`.
- The first `setBounds` request for a target counts as attempt `1`.
- Before every maximized `setBounds` request, immediate or deferred, the maximized predicate and guard compare the current frame and target with exact `Rectangle.equals` semantics, including width and height.
- The maximized predicate does not broaden the shared normal-window `needsFrameResynchronization` behavior; the full-rectangle comparison is confined to `needsMaximizedFrameResynchronization`.
- If the frame already equals the target, the guard resets and reports stable.
- If the target differs from the previous target, the guard replaces the stored target and resets its count to zero before evaluating the request.
- If the unchanged target has fewer than ten requests, the guard increments the count and permits one request.
- At ten requests for the unchanged target, the guard reports exhausted/stable, permits no further `setBounds`, and prevents another deferred timer from being scheduled for that event.
- Leaving maximized state explicitly resets the guard. Re-entering maximized state therefore starts a fresh sequence.

The maximized resynchronization method will return an explicit stable/unstable result. Component-event handling will schedule a deferred retry only when the maximized result is unstable. Reaching the target, having no target, and exhausting the guard are all stable results. Thus same-target `COMPONENT_MOVED`/`COMPONENT_RESIZED` events cannot restart an unbounded timer cycle after exhaustion. The existing normal-window scheduling and native-coordinate correction remain on their current path.

The guard is a last-resort safety boundary; the primary convergence mechanism is matching the WM's work-area geometry.

### Toolkit boundaries

The existing `isX11Toolkit()` gate remains authoritative. Native X11 and XWayland both participate; Windows and other toolkits remain excluded. No `XDG_SESSION_TYPE` check is added.

## Error Handling

- If a graphics configuration or screen-inset lookup is unavailable, the maximized path returns a stable no-op for that event and does not call `setBounds` or schedule a deferred maximized retry.
- Invalid or non-positive work-area dimensions are omitted from target selection; they never fall back to the complete screen rectangle.
- Native geometry failures continue to return `null` as they do today.
- Exhausting the repeated-correction budget stops correction for the unchanged target rather than spinning on the EDT.
- Normal-window failures continue through the existing behavior and are not changed by the maximized work-area resolver.

## Test Strategy

Add focused tests to `FrameResynchronizerTest` before production changes:

- Work-area derivation returns `(0, 32, 1920, 1048)` from `(0, 0, 1920, 1080)` plus a top inset of `32`.
- Zero insets produce the original full-screen rectangle.
- Target resolution selects the inset work area for intersecting native bounds.
- Null or disjoint native bounds fall back to normal bounds and then to the frame-graphics work area; no available work area produces no target.
- A maximized frame matching a non-zero-inset work area is stable, while a frame at the full-screen origin is stale against that target.
- A guard-driven event/timer sequence permits exactly ten requests for one unchanged target, rejects the eleventh, and reports no deferred retry after exhaustion.
- Reaching the target and leaving/re-entering maximized state reset the guard for a later target.
- Existing normal-window size-gating and maximize/restore behavior remain covered.
- Existing non-X11 toolkit tests continue to prove that the resynchronizer is a no-op outside X11.

The implementation phase will run the focused component test first, then the relevant `freeplane` test suite, compile checks, and the available X11/WM-backed acceptance checks. A real native-X11 WM is required to make a native-X11 runtime claim; the existing Wayland/XWayland reproduction remains the primary live regression check.

## Acceptance

The change is acceptable only if:

1. The maximized work-area regression tests pass.
2. The target-resolution and guard state-machine tests pass, including the exact ten-request termination boundary.
3. The existing popup-coordinate and maximize/restore tests pass.
4. The normal-window native-coordinate path is unchanged in behavior.
5. No event-driven maximized correction path can issue unbounded repeated `setBounds` calls or restart timers for one unchanged target.
6. Native X11 and XWayland use the same geometry contract, while non-X11 toolkits remain unaffected.
7. The absence of a real native-X11 WM is reported as a residual verification limitation rather than inferred away.
