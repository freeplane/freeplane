# Maximized-Frame Resynchronization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use the deterministic
> subagent-driven-development controller to implement this plan task-by-task.
> Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make Freeplane's X11/XWayland maximized-frame resynchronization converge on WM work-area bounds without changing PR #2983's normal-window popup correction.

**Architecture:** Keep the existing X11 toolkit gate and normal-window native-geometry path. Add a work-area resolver that derives usable monitor rectangles from `GraphicsConfiguration` bounds and screen insets, then use a maximized-only per-target guard and conditional retry policy so repeated WM feedback cannot loop indefinitely.

**Tech Stack:** Java 8 language target, Swing/AWT, JUnit 4, AssertJ, Gradle.

## Global Constraints

- Use `gradle`, not `gradlew` or Maven.
- Run Gradle with Java from `~/.sdkman/candidates/java/21.0.8-zulu`.
- Preserve the existing `isX11Toolkit()` gate and do not add `XDG_SESSION_TYPE` branching.
- Preserve `FlatPopupFactory` selection and the normal-window native-coordinate correction from PR #2983.
- Do not broaden the shared `needsFrameResynchronization` predicate; the full-rectangle comparison belongs only to maximized-frame comparison.
- Valid zero screen insets mean the complete screen rectangle; unavailable or invalid work-area data means no maximized correction, never a full-screen fallback.
- A real native-X11 window manager is unavailable in this environment; report that runtime verification limitation explicitly.
- Every task must leave the integration worktree with focused tests and a commit.

## Task 1: Add Work-Area Target Resolution

**Implementer tier:** Advanced

**Files:**

- Modify: `freeplane/src/main/java/org/freeplane/core/ui/components/FrameResynchronizer.java:46-139,283-299`
- Test: `freeplane/src/test/java/org/freeplane/core/ui/components/FrameResynchronizerTest.java:1-263`

**Interfaces:**

- Consumes: existing `findTargetScreenBounds(Rectangle frameBounds, Rectangle[] screenBounds)` and `needsFrameResynchronization(boolean isX11Toolkit, int windowState, Rectangle frameBounds, Rectangle targetBounds)`.
- Produces: package-visible `getWorkAreaBounds(Rectangle screenBounds, Insets insets)`, returning a copied work-area rectangle or `null` for missing inputs or non-positive derived dimensions.
- Produces: package-visible `findTargetMaximizedBounds(Rectangle nativeBounds, Rectangle normalBounds, Rectangle[] workAreaBounds, Rectangle fallbackWorkArea)`, selecting native intersection first, normal-bounds intersection second, and a copied fallback work area last.
- Produces: a maximized predicate that requires X11, `Frame.MAXIMIZED_BOTH`, non-null bounds, and full `Rectangle.equals` inequality; the shared `needsFrameResynchronization` remains origin-only.

- [ ] **Step 1: Write the failing work-area and target-resolution tests**

Add `java.awt.Insets` to `FrameResynchronizerTest` and add tests with these exact fixtures and expectations:

```java
private static final Rectangle FULL_SCREEN = new Rectangle(0, 0, 1920, 1080);
private static final Rectangle TOP_PANEL_WORK_AREA = new Rectangle(0, 32, 1920, 1048);

@Test
public void getWorkAreaBounds_withTopInset_returnsUsableArea() {
    assertThat(FrameResynchronizer.getWorkAreaBounds(FULL_SCREEN, new Insets(32, 0, 0, 0)))
        .isEqualTo(TOP_PANEL_WORK_AREA);
}

@Test
public void getWorkAreaBounds_withZeroInsets_preservesFullScreen() {
    assertThat(FrameResynchronizer.getWorkAreaBounds(FULL_SCREEN, new Insets(0, 0, 0, 0)))
        .isEqualTo(FULL_SCREEN);
}

@Test
public void findTargetMaximizedBounds_withNativeGeometry_selectsInsetWorkArea() {
    assertThat(FrameResynchronizer.findTargetMaximizedBounds(
        new Rectangle(0, 32, 1920, 1048), null,
        new Rectangle[] { TOP_PANEL_WORK_AREA }, null))
        .isEqualTo(TOP_PANEL_WORK_AREA);
}

@Test
public void findTargetMaximizedBounds_withDisjointNativeGeometry_usesNormalBoundsThenFallback() {
    final Rectangle normalBounds = new Rectangle(0, 32, 1920, 1048);
    final Rectangle fallback = new Rectangle(1920, 0, 1920, 1080);

    assertThat(FrameResynchronizer.findTargetMaximizedBounds(
        new Rectangle(4000, 0, 100, 100), normalBounds,
        new Rectangle[] { TOP_PANEL_WORK_AREA }, fallback))
        .isEqualTo(TOP_PANEL_WORK_AREA);
    assertThat(FrameResynchronizer.findTargetMaximizedBounds(
        new Rectangle(4000, 0, 100, 100), null,
        new Rectangle[] { TOP_PANEL_WORK_AREA }, fallback))
        .isEqualTo(fallback);
}

@Test
public void findTargetMaximizedBounds_withoutAnyWorkArea_returnsNull() {
    assertThat(FrameResynchronizer.findTargetMaximizedBounds(
        null, null, new Rectangle[] { null }, null)).isNull();
}

@Test
public void needsMaximizedFrameResynchronization_withTopInsetFrame_isStable() {
    assertThat(FrameResynchronizer.needsMaximizedFrameResynchronization(
        true, Frame.MAXIMIZED_BOTH, TOP_PANEL_WORK_AREA, TOP_PANEL_WORK_AREA)).isFalse();
    assertThat(FrameResynchronizer.needsMaximizedFrameResynchronization(
        true, Frame.MAXIMIZED_BOTH, FULL_SCREEN, TOP_PANEL_WORK_AREA)).isTrue();
}

@Test
public void needsMaximizedFrameResynchronization_withSizeOnlyDifference_returnsTrue() {
    assertThat(FrameResynchronizer.needsMaximizedFrameResynchronization(
        true, Frame.MAXIMIZED_BOTH,
        new Rectangle(0, 32, 1920, 1047), TOP_PANEL_WORK_AREA)).isTrue();
}
```

Keep the existing zero-origin, non-X11, iconified, maximize-state, normal-window, and largest-intersection tests unchanged.

- [ ] **Step 2: Run the focused test and confirm the new contract fails**

Run:

```bash
cd /data/home/henry-arch/.local/state/pi/project-manager/worktrees/0233ff88d3cc54bbd61550c61fa917d6e2a5717f8a03be4e9aa63dcd47dd1cfa/pm-run-20261017-122451-1d0829ed/integration
JAVA_HOME="$HOME/.sdkman/candidates/java/21.0.8-zulu" PATH="$JAVA_HOME/bin:$PATH" gradle :freeplane:test --tests "org.freeplane.core.ui.components.FrameResynchronizerTest"
```

Expected: FAIL at compilation because the new work-area and target-resolution methods do not exist, and the existing maximized predicate does not reject a size-only mismatch.

- [ ] **Step 3: Implement the minimal work-area resolver and maximized comparison**

Import `java.awt.Insets`. Add this package-visible helper before the native-window methods:

```java
static Rectangle getWorkAreaBounds(final Rectangle screenBounds, final Insets insets) {
    if (screenBounds == null || insets == null) {
        return null;
    }
    final int width = screenBounds.width - insets.left - insets.right;
    final int height = screenBounds.height - insets.top - insets.bottom;
    if (width <= 0 || height <= 0) {
        return null;
    }
    return new Rectangle(screenBounds.x + insets.left, screenBounds.y + insets.top, width, height);
}
```

Add the target resolver using `findTargetScreenBounds` twice, then copy the fallback rectangle so callers cannot mutate a stored graphics rectangle:

```java
static Rectangle findTargetMaximizedBounds(final Rectangle nativeBounds, final Rectangle normalBounds,
        final Rectangle[] workAreaBounds, final Rectangle fallbackWorkArea) {
    Rectangle targetBounds = findTargetScreenBounds(nativeBounds, workAreaBounds);
    if (targetBounds == null) {
        targetBounds = findTargetScreenBounds(normalBounds, workAreaBounds);
    }
    if (targetBounds != null) {
        return targetBounds;
    }
    return fallbackWorkArea == null ? null : new Rectangle(fallbackWorkArea);
}
```

Change only `needsMaximizedFrameResynchronization` so it keeps its X11, maximize, iconified, and null checks while using `!frameBounds.equals(targetBounds)`. Leave `needsFrameResynchronization` and all normal-window predicates unchanged.

Replace the full-screen `getScreenBounds()` result with a work-area array: enumerate the existing `GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()`, obtain each default `GraphicsConfiguration`, call `Toolkit.getDefaultToolkit().getScreenInsets(configuration)`, pass the configuration bounds and insets through `getWorkAreaBounds`, and retain `null` for missing/failed/invalid entries. Catch a runtime inset-lookup failure and leave that entry unavailable.

In `resynchronizeMaximizedFrame`, resolve the target from the work-area array using native bounds, normal bounds, and then the frame graphics configuration's own derived work area. If no target exists, return stable without calling `setBounds`. Do not use `graphicsConfiguration.getBounds()` directly as a fallback. Keep the normal-window path untouched.

- [ ] **Step 4: Run the focused tests and confirm they pass**

Run the focused Gradle command from Step 2. Expected: PASS for the new work-area, target-resolution, full-rectangle maximized comparison, and all existing `FrameResynchronizerTest` tests.

- [ ] **Step 5: Inspect the scoped diff and commit**

Run:

```bash
git diff --check
git diff -- freeplane/src/main/java/org/freeplane/core/ui/components/FrameResynchronizer.java freeplane/src/test/java/org/freeplane/core/ui/components/FrameResynchronizerTest.java
git status --short
```

Confirm that `needsFrameResynchronization`, `needsNormalWindowResynchronization`, `FlatPopupFactory` code, and non-X11 gates were not broadened or removed. Commit:

```bash
git add freeplane/src/main/java/org/freeplane/core/ui/components/FrameResynchronizer.java freeplane/src/test/java/org/freeplane/core/ui/components/FrameResynchronizerTest.java
git commit -m "Use monitor work areas for maximized frame sync"
```

## Task 2: Bound Maximized Feedback and Preserve Lifecycle Behavior

**Implementer tier:** Capable

**Files:**

- Modify: `freeplane/src/main/java/org/freeplane/core/ui/components/FrameResynchronizer.java:109-139,301-425`
- Test: `freeplane/src/test/java/org/freeplane/core/ui/components/FrameResynchronizerTest.java:1-263`

**Interfaces:**

- Consumes: Task 1's `findTargetMaximizedBounds(Rectangle, Rectangle, Rectangle[], Rectangle)` and full-rectangle `needsMaximizedFrameResynchronization(boolean, int, Rectangle, Rectangle)`.
- Produces: package-visible `MaximizedResynchronizationGuard` with `shouldRequest(Rectangle currentBounds, Rectangle targetBounds)`, `isExhausted()`, and `reset()`; first request is count `1`, and no more than ten requests are permitted for one unchanged target.
- Produces: package-visible `shouldScheduleDeferredResynchronization(boolean maximized, boolean stable)`, returning `false` only for a stable maximized result and `true` for normal/non-maximized paths.
- Preserves: `needsNormalWindowResynchronization`, normal-window deferred scheduling, `resynchronizeFrame` usage during maximize-to-normal restoration, and the existing `isX11Toolkit()` behavior.

- [ ] **Step 1: Write the failing guard and retry-policy tests**

Add tests for the package-visible guard and scheduling policy:

```java
@Test
public void maximizedGuard_allowsExactlyTenRequestsForOneTarget() {
    final FrameResynchronizer.MaximizedResynchronizationGuard guard =
        new FrameResynchronizer.MaximizedResynchronizationGuard();
    final Rectangle current = new Rectangle(0, 0, 1920, 1080);
    final Rectangle target = new Rectangle(0, 32, 1920, 1048);

    for (int attempt = 1; attempt <= 10; attempt++) {
        assertThat(guard.shouldRequest(current, target)).isTrue();
    }
    assertThat(guard.isExhausted()).isTrue();
    assertThat(guard.shouldRequest(current, target)).isFalse();
}

@Test
public void maximizedGuard_reachingTarget_resetsSameTargetBudget() {
    final FrameResynchronizer.MaximizedResynchronizationGuard guard =
        new FrameResynchronizer.MaximizedResynchronizationGuard();
    final Rectangle current = new Rectangle(0, 0, 1920, 1080);
    final Rectangle target = new Rectangle(0, 32, 1920, 1048);

    assertThat(guard.shouldRequest(current, target)).isTrue();
    assertThat(guard.shouldRequest(target, target)).isFalse();
    assertThat(guard.shouldRequest(current, target)).isTrue();
}

@Test
public void maximizedGuard_reset_allowsARequestAfterLeavingAndReenteringMaximized() {
    final FrameResynchronizer.MaximizedResynchronizationGuard guard =
        new FrameResynchronizer.MaximizedResynchronizationGuard();
    final Rectangle current = new Rectangle(0, 0, 1920, 1080);
    final Rectangle target = new Rectangle(0, 32, 1920, 1048);

    for (int attempt = 1; attempt <= 10; attempt++) {
        guard.shouldRequest(current, target);
    }
    guard.reset();
    assertThat(guard.shouldRequest(current, target)).isTrue();
}

@Test
public void shouldScheduleDeferredResynchronization_stopsOnlyStableMaximizedRetries() {
    assertThat(FrameResynchronizer.shouldScheduleDeferredResynchronization(true, false)).isTrue();
    assertThat(FrameResynchronizer.shouldScheduleDeferredResynchronization(true, true)).isFalse();
    assertThat(FrameResynchronizer.shouldScheduleDeferredResynchronization(false, true)).isTrue();
}
```

The first guard test is the falsifiable event/timer boundary: each permitted request represents one immediate or deferred `setBounds` cycle, and the eleventh same-target event must be stable with no retry request.

- [ ] **Step 2: Run the focused test and confirm the guard API fails**

Run the same focused Gradle command used in Task 1. Expected: FAIL at compilation because the guard and retry-policy seam do not yet exist.

- [ ] **Step 3: Implement the maximized-only guard and conditional scheduling**

Add an instance-local guard with this behavior:

```java
static final class MaximizedResynchronizationGuard {
    private static final int MAX_ATTEMPTS = 10;
    private Rectangle targetBounds;
    private int attempts;

    boolean shouldRequest(final Rectangle currentBounds, final Rectangle targetBounds) {
        if (currentBounds == null || targetBounds == null || currentBounds.equals(targetBounds)) {
            reset();
            return false;
        }
        if (!targetBounds.equals(this.targetBounds)) {
            this.targetBounds = new Rectangle(targetBounds);
            attempts = 0;
        }
        if (attempts >= MAX_ATTEMPTS) {
            return false;
        }
        attempts++;
        return true;
    }

    boolean isExhausted() {
        return attempts >= MAX_ATTEMPTS;
    }

    void reset() {
        targetBounds = null;
        attempts = 0;
    }
}
```

Add `maximizedResynchronizationGuard` as an instance field. Make `resynchronizeMaximizedFrame` instance-scoped and use Task 1's target resolver. For a null target, a full-rectangle match, or an exhausted guard, return `true` without setting bounds. For an allowed request, call `frame.setBounds(targetBounds)` and `frame.validate()` directly so a size-only mismatch is corrected; do not route maximized requests through the origin-only `resynchronizeFrame` helper. Return `false` only after an allowed correction request that may produce a later WM event.

Add:

```java
static boolean shouldScheduleDeferredResynchronization(final boolean maximized, final boolean stable) {
    return !maximized || !stable;
}
```

In `handleComponentGeometryChanged` and `windowOpened`, retain the current normal/non-maximized scheduling behavior, but for maximized windows call `scheduleDeferredResynchronization()` only when `shouldScheduleDeferredResynchronization(true, stable)` is true. This makes an exhausted or no-target maximized result stable and prevents a same-target component event from restarting the timer. Do not reset the maximized guard from `scheduleDeferredResynchronization`; its state is independent of the existing deferred-timer attempt counter.

Reset the maximized guard on a maximize-to-normal transition before the existing normal-bounds restoration path. Re-entering maximized mode then starts a fresh per-target sequence. Leave `resynchronizeFrame` and `needsNormalFrameResynchronization` unchanged.

- [ ] **Step 4: Run the focused tests and the module test suite**

Run:

```bash
cd /data/home/henry-arch/.local/state/pi/project-manager/worktrees/0233ff88d3cc54bbd61550c61fa917d6e2a5717f8a03be4e9aa63dcd47dd1cfa/pm-run-20261017-122451-1d0829ed/integration
JAVA_HOME="$HOME/.sdkman/candidates/java/21.0.8-zulu" PATH="$JAVA_HOME/bin:$PATH" gradle :freeplane:test --tests "org.freeplane.core.ui.components.FrameResynchronizerTest"
JAVA_HOME="$HOME/.sdkman/candidates/java/21.0.8-zulu" PATH="$JAVA_HOME/bin:$PATH" gradle :freeplane:test
```

Expected: both commands pass. The focused test must include the exact ten-request boundary, no-target behavior, size-only maximized correction, maximize-to-normal reset coverage, normal-window size gating, and non-X11 no-op coverage.

- [ ] **Step 5: Perform available runtime and diff verification**

Run the existing Freeplane development build under the available Wayland/XWayland session and exercise a maximized frame with the observed top-panel geometry. Confirm that the EDT no longer enters the repeated `componentMoved -> resynchronizeMaximizedFrame -> setBounds` cycle, that a normal window moved between monitors still aligns its popup, and that maximize-to-normal restores the saved normal bounds.

Record that this environment has no native X11 window manager (`kwin_x11` or equivalent), so native-X11 runtime behavior remains unverified. The shared `sun.awt.X11.XToolkit` code path and unit tests are evidence for the common geometry contract, not proof of native-X11 WM-specific behavior.

Run:

```bash
git diff --check
git status --short
git log --oneline --decorate -3
```

Confirm only the two scoped Java files changed after the design commits. Commit:

```bash
git add freeplane/src/main/java/org/freeplane/core/ui/components/FrameResynchronizer.java freeplane/src/test/java/org/freeplane/core/ui/components/FrameResynchronizerTest.java
git commit -m "Bound maximized frame resynchronization"
```
