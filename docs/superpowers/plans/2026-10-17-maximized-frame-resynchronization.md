# X11 Popup Coordinate Calibration and Convergent Frame Resynchronization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use the deterministic
> subagent-driven-development controller to implement this plan task-by-task.
> Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make Freeplane heavyweight popup menus persist at the correct screen position and remain selectable on Linux native X11 and XWayland when a panel or dock reserves screen space, while retaining convergent maximized-frame synchronization and unchanged non-X11 behavior.

**Architecture:** Retain the approved work-area target resolver and bounded maximized-frame guard already present in the integration branch. Add a package-private owner-local calibration module and a delegating `PopupFactory` adapter that adjusts only the coordinates passed to the active shared factory; `FlatPopupFactory` remains the delegate and continues to own popup type selection, tooltip handling, reuse, borders, and platform behavior. Register Freeplane frames through `FrameResynchronizer.install(JFrame)`, keep the registry weak, and refresh the adapter after successful look-and-feel replacement so the current shared factory is always wrapped once.

**Tech Stack:** Java 8 language target, Swing/AWT, JUnit 4, AssertJ, Mockito, FlatLaf 3.7.2, JNA X11 geometry access, and Gradle.

## Global Constraints

- Use `gradle`, not `gradlew` or Maven.
- Run Gradle with Java from `~/.sdkman/candidates/java/21.0.8-zulu`.
- Preserve the existing exact `sun.awt.X11.XToolkit` check in `isX11Toolkit()` and do not add `XDG_SESSION_TYPE` branching.
- Runtime corrective behavior is Linux-only behind the exact X11 toolkit gate; native X11 and XWayland use the same path, while macOS, Windows, and other toolkits retain pass-through behavior.
- Preserve `FlatPopupFactory` as the active shared factory delegate. Do not subclass it, replace its implementation, reproduce its heavyweight-selection logic, or change popup-factory selection for non-X11 toolkits.
- Preserve the PR #2983 normal-window native-coordinate correction and do not broaden the shared origin-only `needsFrameResynchronization` predicate.
- Maximized targets use monitor work areas derived as `screen.x + insets.left`, `screen.y + insets.top`, `screen.width - insets.left - insets.right`, and `screen.height - insets.top - insets.bottom`; zero insets preserve the complete screen, and missing or invalid work-area data produces no target rather than a full-screen fallback.
- Maximized resynchronization permits at most ten `setBounds` requests for one unchanged target, counts before requesting, resets on convergence, target change, or leaving maximized state, and does not restart a deferred timer after exhaustion or stable convergence.
- Popup calibration computes one owner-local translation from native outer-frame bounds, owner location within the frame, and owner screen location. It never calls `setBounds`, schedules a timer, mutates the owner or frame, or retries after delegation.
- Registered frames are weak keys. A current `X11PopupFactory` adapter is reused instead of wrapped again, and a look-and-feel change wraps the newly active shared factory exactly once.
- Calibration failures, unavailable native geometry, unavailable owner geometry, non-registered owners, and non-X11 execution delegate with the original popup coordinates; delegate exceptions propagate unchanged.
- Unit tests must be desktop-free for integer geometry and use injected native/AWT geometry providers. Native runtime acceptance is a separate evidence task.
- The epoch-1 frame-resynchronization branches and worktrees remain quarantined historical artifacts; this plan uses the retained frame behavior already on the integration branch and does not resume, delete, merge, or push those artifacts.
- Do not merge to the delivery target or push. Delivery remains a separate explicit user choice after implementation and verification.
- Each task ends with focused verification, `git diff --check`, and a commit in the integration worktree.

## Task 1: Lock the Retained Work-Area and Maximized-Guard Contract

**Implementer tier:** Standard

**Files:**

- Modify: `freeplane/src/test/java/org/freeplane/core/ui/components/FrameResynchronizerTest.java:390-420`

**Interfaces:**

- Consumes: `FrameResynchronizer.findTargetMaximizedBounds(Rectangle nativeBounds, Rectangle normalBounds, Rectangle[] workAreaBounds, Rectangle fallbackWorkArea)`, which returns the native-intersection target first, the normal-bounds target second, and a copied fallback last.
- Consumes: `FrameResynchronizer.MaximizedResynchronizationGuard.shouldRequest(Rectangle currentBounds, Rectangle targetBounds)`, `isExhausted()`, and `reset()`.
- Produces: regression assertions for copied fallback rectangles, target-change budget reset, the exact ten-request boundary, stable unavailable-target behavior, and the existing top-panel, zero-inset, normal-window, and non-X11 contracts.

- [ ] **Step 1: Add the missing boundary assertions without changing the retained frame implementation**

Add these tests beside the existing target-resolution and guard tests. They make the epoch-2 foundation explicit without reviving the quarantined epoch-1 branch:

```java
@Test
public void findTargetMaximizedBounds_withFallback_returnsIndependentRectangle() {
    final Rectangle fallback = new Rectangle(1920, 0, 1920, 1080);

    final Rectangle target = FrameResynchronizer.findTargetMaximizedBounds(
        new Rectangle(4000, 0, 100, 100), null,
        new Rectangle[] { null }, fallback);

    assertThat(target).isEqualTo(fallback).isNotSameAs(fallback);
    target.x = 0;
    assertThat(fallback).isEqualTo(new Rectangle(1920, 0, 1920, 1080));
}

@Test
public void maximizedGuard_targetChange_resetsTheTenRequestBudget() {
    final FrameResynchronizer.MaximizedResynchronizationGuard guard =
        new FrameResynchronizer.MaximizedResynchronizationGuard();
    final Rectangle current = new Rectangle(0, 0, 1920, 1080);
    final Rectangle firstTarget = new Rectangle(0, 32, 1920, 1048);
    final Rectangle secondTarget = new Rectangle(1920, 32, 1920, 1048);

    for (int attempt = 1; attempt <= 10; attempt++) {
        assertThat(guard.shouldRequest(current, firstTarget)).isTrue();
    }
    assertThat(guard.shouldRequest(current, firstTarget)).isFalse();
    assertThat(guard.shouldRequest(current, secondTarget)).isTrue();
    assertThat(guard.shouldRequest(current, firstTarget)).isTrue();
}
```

Keep the existing assertions that a top panel produces `(0,32,1920,1048)`, zero insets preserve `(0,0,1920,1080)`, an unavailable target performs no correction or retry, a size-only maximized mismatch is corrected, the shared normal predicate remains origin-only, and the listener stops at ten requests.

- [ ] **Step 2: Run the focused frame test against the retained foundation**

Run:

```bash
cd /home/henry-arch/.local/state/pi/project-manager/worktrees/0233ff88d3cc54bbd61550c61fa917d6e2a5717f8a03be4e9aa63dcd47dd1cfa/pm-run-20261017-122451-1d0829ed/integration
JAVA_HOME="$HOME/.sdkman/candidates/java/21.0.8-zulu" PATH="$JAVA_HOME/bin:$PATH" gradle :freeplane:test --tests "org.freeplane.core.ui.components.FrameResynchronizerTest"
```

Expected: the complete `FrameResynchronizerTest` class passes. If a new assertion fails, make the smallest source correction that preserves the work-area formula, fallback order, exact maximized comparison, normal-window correction, and ten-request guard, then rerun the same command.

- [ ] **Step 3: Inspect the frame synchronization diff and verify the protected seams**

Run:

```bash
git diff --check
git diff -- freeplane/src/main/java/org/freeplane/core/ui/components/FrameResynchronizer.java freeplane/src/test/java/org/freeplane/core/ui/components/FrameResynchronizerTest.java
git status --short
```

Confirm that the shared `needsFrameResynchronization` predicate remains origin-only, the normal-window native-coordinate path is intact, the exact X11 gate remains in force, and no popup-factory code is introduced in this task.

- [ ] **Step 4: Commit the contract pinning tests**

```bash
git add freeplane/src/test/java/org/freeplane/core/ui/components/FrameResynchronizerTest.java
git commit -m "Pin frame resynchronization target and guard boundaries"
```

## Task 2: Implement Owner-Local Popup Calibration and the Delegating Adapter

**Implementer tier:** Advanced

**Files:**

- Create: `freeplane/src/main/java/org/freeplane/core/ui/components/PopupCoordinateCalibrator.java`
- Create: `freeplane/src/main/java/org/freeplane/core/ui/components/X11PopupFactory.java`
- Create: `freeplane/src/test/java/org/freeplane/core/ui/components/PopupCoordinateCalibratorTest.java`
- Create: `freeplane/src/test/java/org/freeplane/core/ui/components/X11PopupFactoryTest.java`

**Interfaces:**

- Consumes: `java.awt.Component`, `java.awt.Point`, `java.awt.Rectangle`, `javax.swing.JFrame`, `javax.swing.Popup`, and `javax.swing.PopupFactory`.
- Produces: package-private `PopupCoordinateCalibrator.GeometryProvider` with `getNativeFrameBounds(JFrame frame)`, `getOwnerLocationWithinFrame(Component owner, JFrame frame)`, and `getOwnerLocationOnScreen(Component owner)`.
- Produces: package-private `PopupCoordinateCalibrator.calculateCorrection(Component owner, JFrame frame, GeometryProvider geometryProvider)`, returning a `Point` translation or `null` when calibration is unavailable.
- Produces: package-private `PopupOwnerResolver` with `findFrame(Component owner)`, allowing the factory tests to resolve a registered frame without a displayable Swing hierarchy.
- Produces: package-private `X11PopupFactory` extending `javax.swing.PopupFactory`, with an injected `PopupOwnerResolver` and `GeometryProvider`; its public `getPopup(Component owner, Component contents, int x, int y)` delegates exactly once.

- [ ] **Step 1: Write the owner-local calibration tests**

Create tests using mocked `JFrame` and `Component` objects plus an injected `GeometryProvider`; do not construct or show a native desktop frame. Pin these cases:

```java
@Test
public void calculateCorrection_withSplitCacheDelta_returnsNegativePlacementError() {
    final JFrame frame = mock(JFrame.class);
    final Component owner = mock(Component.class);
    final PopupCoordinateCalibrator.GeometryProvider geometry = mock(PopupCoordinateCalibrator.GeometryProvider.class);
    when(geometry.getNativeFrameBounds(frame)).thenReturn(new Rectangle(0, 0, 2240, 1400));
    when(geometry.getOwnerLocationWithinFrame(owner, frame)).thenReturn(new Point(1035, 5));
    when(geometry.getOwnerLocationOnScreen(owner)).thenReturn(new Point(1275, 185));

    assertThat(PopupCoordinateCalibrator.calculateCorrection(owner, frame, geometry))
        .isEqualTo(new Point(-240, -180));
}

@Test
public void calculateCorrection_whenOwnerAlreadyAgrees_returnsZero() {
    final JFrame frame = mock(JFrame.class);
    final Component owner = mock(Component.class);
    final PopupCoordinateCalibrator.GeometryProvider geometry = mock(PopupCoordinateCalibrator.GeometryProvider.class);
    when(geometry.getNativeFrameBounds(frame)).thenReturn(new Rectangle(240, 180, 960, 600));
    when(geometry.getOwnerLocationWithinFrame(owner, frame)).thenReturn(new Point(10, 20));
    when(geometry.getOwnerLocationOnScreen(owner)).thenReturn(new Point(250, 200));

    assertThat(PopupCoordinateCalibrator.calculateCorrection(owner, frame, geometry))
        .isEqualTo(new Point(0, 0));
}
```

Add a negative-delta case, a `null` result case for each unavailable geometry value, a non-positive native width or height case, and a provider-exception case. Verify that a failure returns `null` instead of escaping to popup creation.

- [ ] **Step 2: Run the new tests and confirm the calibration classes are absent**

Run:

```bash
JAVA_HOME="$HOME/.sdkman/candidates/java/21.0.8-zulu" PATH="$JAVA_HOME/bin:$PATH" gradle :freeplane:test --tests "org.freeplane.core.ui.components.PopupCoordinateCalibratorTest"
```

Expected: compilation fails because the two new production classes do not yet exist.

- [ ] **Step 3: Implement the single owner-local correction calculation**

Implement `PopupCoordinateCalibrator` as a final package-private class. The calculation must follow this exact sequence:

```java
static Point calculateCorrection(final Component owner, final JFrame frame,
        final GeometryProvider geometryProvider) {
    if (owner == null || frame == null || geometryProvider == null) {
        return null;
    }
    try {
        final Rectangle nativeFrameBounds = geometryProvider.getNativeFrameBounds(frame);
        final Point ownerLocationWithinFrame = geometryProvider.getOwnerLocationWithinFrame(owner, frame);
        final Point ownerLocationOnScreen = geometryProvider.getOwnerLocationOnScreen(owner);
        if (nativeFrameBounds == null || nativeFrameBounds.width <= 0 || nativeFrameBounds.height <= 0
                || ownerLocationWithinFrame == null || ownerLocationOnScreen == null) {
            return null;
        }
        final int expectedX = nativeFrameBounds.x + ownerLocationWithinFrame.x;
        final int expectedY = nativeFrameBounds.y + ownerLocationWithinFrame.y;
        return new Point(expectedX - ownerLocationOnScreen.x, expectedY - ownerLocationOnScreen.y);
    }
    catch (RuntimeException unavailableGeometry) {
        return null;
    }
}
```

Do not call `setBounds`, `validate`, `Timer.start`, `Timer.stop`, `invokeLater`, or any other retry mechanism from this class.

- [ ] **Step 4: Write the delegating factory tests**

Construct `X11PopupFactory` with a mocked delegate, an injected resolver that returns a registered frame, and the geometry provider from the preceding tests. Pin these behaviors:

- A correction of `(-240,-180)` changes a request `(1000,400)` to `(760,220)`.
- The delegate receives the original owner and contents, the corrected coordinates, and exactly one `getPopup` call.
- A zero correction passes the original coordinates unchanged.
- A resolver that returns no registered frame passes the original coordinates and does not query geometry.
- A geometry provider that returns `null` or throws passes the original coordinates and still returns the delegate popup.
- No call is made to frame mutation methods, no timer is created or scheduled, and no second delegate call occurs.
- An exception thrown by the delegate propagates to the caller.

Use a `PopupOwnerResolver` functional seam so these tests do not depend on `SwingUtilities.getWindowAncestor` finding a real displayable hierarchy.

- [ ] **Step 5: Implement the delegating adapter without reproducing FlatLaf behavior**

Implement `X11PopupFactory` as a direct subclass of `PopupFactory`, with a captured `PopupFactory delegate`. Its `getPopup` method may calculate a correction and change only the two integer arguments before making this one call:

```java
return delegate.getPopup(owner, contents, correctedX, correctedY);
```

Catch only runtime failures from owner lookup or calibration, restore the original coordinates for those failures, and keep the delegate call outside that failure handler so delegate exceptions are preserved. Do not reference `FlatPopupFactory` from this class and do not override any other `PopupFactory` method.

- [ ] **Step 6: Run the popup calibration and adapter tests**

Run:

```bash
JAVA_HOME="$HOME/.sdkman/candidates/java/21.0.8-zulu" PATH="$JAVA_HOME/bin:$PATH" gradle :freeplane:test --tests "org.freeplane.core.ui.components.PopupCoordinateCalibratorTest" --tests "org.freeplane.core.ui.components.X11PopupFactoryTest"
```

Expected: all geometry and delegation tests pass, including the split-cache correction `(-240,-180)` and the unchanged-coordinate failure paths.

- [ ] **Step 7: Inspect and commit the popup seam**

Run:

```bash
git diff --check
git diff -- freeplane/src/main/java/org/freeplane/core/ui/components/PopupCoordinateCalibrator.java freeplane/src/main/java/org/freeplane/core/ui/components/X11PopupFactory.java freeplane/src/test/java/org/freeplane/core/ui/components/PopupCoordinateCalibratorTest.java freeplane/src/test/java/org/freeplane/core/ui/components/X11PopupFactoryTest.java
git status --short
```

Confirm that the adapter has no frame mutation, timer, retry, FlatLaf subclass, or popup-type-selection logic. Commit:

```bash
git add freeplane/src/main/java/org/freeplane/core/ui/components/PopupCoordinateCalibrator.java freeplane/src/main/java/org/freeplane/core/ui/components/X11PopupFactory.java freeplane/src/test/java/org/freeplane/core/ui/components/PopupCoordinateCalibratorTest.java freeplane/src/test/java/org/freeplane/core/ui/components/X11PopupFactoryTest.java
git commit -m "Calibrate X11 popup coordinates at creation"
```

## Task 3: Register Frames and Refresh the Adapter Across Look-and-Feel Changes

**Implementer tier:** Capable

**Files:**

- Modify: `freeplane/src/main/java/org/freeplane/core/ui/components/FrameResynchronizer.java:1-55,130-180`
- Modify: `freeplane/src/main/java/org/freeplane/features/ui/FrameController.java:630-757`
- Create: `freeplane/src/test/java/org/freeplane/core/ui/components/FrameResynchronizerPopupFactoryTest.java`

**Interfaces:**

- Consumes: `X11PopupFactory(PopupFactory delegate, PopupOwnerResolver ownerResolver, PopupCoordinateCalibrator.GeometryProvider geometryProvider)` and its one-call `getPopup` delegation from Task 2.
- Consumes: the existing `FrameResynchronizer.install(JFrame)` listener registration and the existing `FrameResynchronizer.getNativeWindowBounds(Component)` native query.
- Produces: `FrameResynchronizer.install(JFrame)` registration of the Freeplane frame in a `WeakHashMap`-backed frame set before the adapter refresh.
- Produces: public `FrameResynchronizer.refreshPopupFactory()`, which is a no-op outside the exact X11 toolkit gate and wraps the current shared factory only when a registered frame exists.
- Produces: package-visible `FrameResynchronizer.ensurePopupFactory(boolean isX11Toolkit, PopupFactory currentFactory, Set<JFrame> registeredFrames, PopupCoordinateCalibrator.GeometryProvider geometryProvider)` for desktop-free lifecycle tests.
- Produces: package-visible `X11PopupFactory.getDelegate()` for identity tests; it returns the captured active delegate without exposing or mutating the registry.
- Produces: `FrameController.setLookAndFeel(String)` refresh calls after every successful `UIManager.setLookAndFeel(...)` path, including direct class-name, instantiated look-and-feel, fallback, and helper-method success paths.

- [ ] **Step 1: Add tests for registration-independent factory refresh decisions**

Create `FrameResynchronizerPopupFactoryTest` with a `WeakHashMap`-backed `Set<JFrame>` and mocked factories/providers. Pin this decision table:

```java
@Test
public void ensurePopupFactory_onX11WrapsCurrentFactoryWhenAFrameIsRegistered() {
    final Set<JFrame> frames = Collections.newSetFromMap(new WeakHashMap<JFrame, Boolean>());
    frames.add(mock(JFrame.class));
    final PopupFactory delegate = mock(PopupFactory.class);

    final PopupFactory result = FrameResynchronizer.ensurePopupFactory(
        true, delegate, frames, mock(PopupCoordinateCalibrator.GeometryProvider.class));

    assertThat(result).isInstanceOf(X11PopupFactory.class);
    assertThat(((X11PopupFactory) result).getDelegate()).isSameAs(delegate);
}

@Test
public void ensurePopupFactory_doesNotWrapAnExistingAdapterAgain() {
    final Set<JFrame> frames = Collections.newSetFromMap(new WeakHashMap<JFrame, Boolean>());
    frames.add(mock(JFrame.class));
    final PopupFactory adapter = new X11PopupFactory(mock(PopupFactory.class),
        owner -> mock(JFrame.class), mock(PopupCoordinateCalibrator.GeometryProvider.class));

    assertThat(FrameResynchronizer.ensurePopupFactory(true, adapter, frames,
        mock(PopupCoordinateCalibrator.GeometryProvider.class))).isSameAs(adapter);
}

@Test
public void ensurePopupFactory_outsideX11ReturnsTheActiveFactoryUnchanged() {
    final PopupFactory delegate = mock(PopupFactory.class);
    final Set<JFrame> frames = Collections.newSetFromMap(new WeakHashMap<JFrame, Boolean>());

    assertThat(FrameResynchronizer.ensurePopupFactory(false, delegate, frames,
        mock(PopupCoordinateCalibrator.GeometryProvider.class))).isSameAs(delegate);
}
```

Add a test that replacing the active delegate creates a new adapter whose `getDelegate()` is the replacement, while the registered-frame set remains the same weak-key set. Add a test that an empty registry leaves the active factory unchanged.

- [ ] **Step 2: Run the lifecycle tests and confirm the production seam is absent**

Run:

```bash
JAVA_HOME="$HOME/.sdkman/candidates/java/21.0.8-zulu" PATH="$JAVA_HOME/bin:$PATH" gradle :freeplane:test --tests "org.freeplane.core.ui.components.FrameResynchronizerPopupFactoryTest"
```

Expected: compilation fails until the package-visible factory decision seam and adapter delegate accessor exist.

- [ ] **Step 3: Add the weak registry and production geometry provider**

In `FrameResynchronizer`, add a static registry whose values do not retain frames:

```java
private static final Set<JFrame> REGISTERED_FRAMES =
    Collections.newSetFromMap(new WeakHashMap<JFrame, Boolean>());
```

Create one default `GeometryProvider` whose methods call `getNativeWindowBounds(frame)`, `SwingUtilities.convertPoint(owner, 0, 0, frame)`, and `owner.getLocationOnScreen()`. The provider must return the same logical screen coordinate space used by `getNativeWindowBounds` and let the calibrator convert runtime failures into a no-op.

Implement `ensurePopupFactory` with this order:

1. Return `currentFactory` when `isX11Toolkit` is false, the factory is unavailable, the registry is empty, or the current factory is already `X11PopupFactory`.
2. Otherwise create one `X11PopupFactory` around the current factory, using a resolver that finds the `JFrame` owner itself or the nearest window ancestor and accepts it only when it is present in `REGISTERED_FRAMES`.
3. Preserve the registry as weak keys and pass the current active factory as the captured delegate.

Add a package-visible `getDelegate()` only for tests; it must return the captured delegate and must not expose or mutate the registry.

- [ ] **Step 4: Wire `install(JFrame)` and the public refresh method**

Keep the existing window, window-state, and component listener registration. Add the frame to `REGISTERED_FRAMES` and call `refreshPopupFactory()` from the same `install(JFrame)` entry point. Do not add another installation call to `ApplicationViewController`; its existing call after stored bounds is the lifecycle point.

Implement:

```java
public static void refreshPopupFactory() {
    if (!isX11Toolkit()) {
        return;
    }
    final PopupFactory currentFactory = PopupFactory.getSharedInstance();
    final PopupFactory refreshedFactory = ensurePopupFactory(true, currentFactory,
        REGISTERED_FRAMES, DEFAULT_POPUP_GEOMETRY_PROVIDER);
    if (refreshedFactory != currentFactory) {
        PopupFactory.setSharedInstance(refreshedFactory);
    }
}
```

The method must leave non-X11 shared-factory state untouched, must not wrap a wrapper, and must do nothing when no registered Freeplane frame exists.

- [ ] **Step 5: Refresh after every successful look-and-feel replacement**

In `FrameController.setLookAndFeel` and `tryToSetLookAndFeel`, call `FrameResynchronizer.refreshPopupFactory()` immediately after each successful `UIManager.setLookAndFeel(...)` return:

- after the default system look-and-feel call;
- after the installed look-and-feel helper succeeds;
- after the user-library look-and-feel instance succeeds;
- after the fallback system look-and-feel call in the recovery branch; and
- inside both successful branches of `tryToSetLookAndFeel`.

Do not call refresh after a failed attempt, do not alter `OSKeyBindingManager`, and do not change the existing `FlatLaf` setup or the current `FrameResynchronizer.install(frame)` call in `ApplicationViewController`.

- [ ] **Step 6: Run focused lifecycle tests and compile the application sources**

Run:

```bash
JAVA_HOME="$HOME/.sdkman/candidates/java/21.0.8-zulu" PATH="$JAVA_HOME/bin:$PATH" gradle :freeplane:test --tests "org.freeplane.core.ui.components.FrameResynchronizerPopupFactoryTest" --tests "org.freeplane.core.ui.components.PopupCoordinateCalibratorTest" --tests "org.freeplane.core.ui.components.X11PopupFactoryTest"
JAVA_HOME="$HOME/.sdkman/candidates/java/21.0.8-zulu" PATH="$JAVA_HOME/bin:$PATH" gradle :freeplane:compileJava
```

Expected: all popup/lifecycle tests pass and the application compiles with the Java 8 source target. The compile verifies that `FrameController` can call the refresh method without changing behavior on other platforms.

- [ ] **Step 7: Inspect and commit the lifecycle wiring**

Run:

```bash
git diff --check
git diff -- freeplane/src/main/java/org/freeplane/core/ui/components/FrameResynchronizer.java freeplane/src/main/java/org/freeplane/features/ui/FrameController.java freeplane/src/test/java/org/freeplane/core/ui/components/FrameResynchronizerPopupFactoryTest.java
git status --short
```

Confirm that the only popup-factory change is the delegating X11 adapter, the registry has weak keys, `FlatPopupFactory` remains the captured delegate, and `install(JFrame)` remains the sole frame-registration call site. Commit:

```bash
git add freeplane/src/main/java/org/freeplane/core/ui/components/FrameResynchronizer.java freeplane/src/main/java/org/freeplane/features/ui/FrameController.java freeplane/src/test/java/org/freeplane/core/ui/components/FrameResynchronizerPopupFactoryTest.java
git commit -m "Refresh X11 popup calibration with frame lifecycle"
```

## Task 4: Run Full Regression Checks and Capture Native-X11 Acceptance Evidence

**Implementer tier:** Frontier

**Files:**

- Create: `ai-specs/tasks/freeplane-frame-resynchronizer-x11-evidence/epoch-2/PopupCoordinateAcceptanceProbe.java`
- Create: `ai-specs/tasks/freeplane-frame-resynchronizer-x11-evidence/epoch-2/run-acceptance.py`
- Create: `ai-specs/tasks/freeplane-frame-resynchronizer-x11-evidence/epoch-2/acceptance-report.md`

**Interfaces:**

- Consumes: the compiled `FrameResynchronizer`, `PopupCoordinateCalibrator`, and `X11PopupFactory` classes from Tasks 2 and 3; the existing JNA X11 dependency; and the existing disposable popup-probe conventions under `ai-specs/tasks/freeplane-frame-resynchronizer-x11-evidence/`.
- Consumes: the baseline source identity `0734fb23db6873f4ec64c3e3e29fc8c3e17edc7a` and the patched integration build identity recorded when this task runs.
- Produces: bounded baseline and patched logs for saved-maximized, normal-then-maximize, external-maximize where `wmctrl` is available, restore, and remaximize sequences.
- Produces: an acceptance report containing toolkit class, active popup factory and delegate class, owner-local calibration values, popup placement error, menu persistence, successful menu-item selection, frame correction count, and any explicit runtime limitation.

- [ ] **Step 1: Add a disposable probe that records the approved evidence fields**

Base the probe on the existing popup-coordinate and popup-selection probes, but compile it against the current integration classes without changing production source. The probe must:

- log `Toolkit.getDefaultToolkit().getClass().getName()` and require the exact `sun.awt.X11.XToolkit` value for native acceptance;
- log `PopupFactory.getSharedInstance()` and, when it is `X11PopupFactory`, its delegate class, proving that the active delegate remains FlatLaf's factory;
- create a heavyweight `JPopupMenu` with selectable items from a button or menu owner inside a registered `JFrame`;
- record owner AWT screen location, owner location within the frame, native outer-frame bounds, expected owner screen location, calculated correction, popup request coordinates, popup AWT/native bounds, and integer placement error;
- use a real pointer click or equivalent input path that exercises menu-item selection and log `popup-item-selected` only from the item action listener;
- run maximize, restore, and remaximize transitions and record every frame correction request, including whether opening each popup adds a new correction loop; and
- exit with a nonzero status when the popup disappears before selection, selection is absent, the native-X11 toolkit identity is wrong for a native acceptance run, or placement error exceeds the agreed integer-pixel tolerance.

The probe must not modify the production `FrameResynchronizer` source, must not use panel auto-hide, and must not treat a missing native X11 display as a passing result.

- [ ] **Step 2: Add a bounded runner for baseline and patched executions**

Implement `run-acceptance.py` to compile the probe with `javac --release 8`, run each scenario with a bounded timeout, retain stdout and stderr in separate baseline/patched log directories, and record the source SHA-256 and runtime identity. Use the existing evidence convention of preserving a prior log before replacing it. The runner must stop after a failed compilation, nonzero probe exit, or timeout and must not leave a background Freeplane process running.

Run the Gradle build before the probe with:

```bash
cd /home/henry-arch/.local/state/pi/project-manager/worktrees/0233ff88d3cc54bbd61550c61fa917d6e2a5717f8a03be4e9aa63dcd47dd1cfa/pm-run-20261017-122451-1d0829ed/integration
JAVA_HOME="$HOME/.sdkman/candidates/java/21.0.8-zulu" PATH="$JAVA_HOME/bin:$PATH" gradle :freeplane:test :freeplane:compileJava
```

The runner may use the existing native X11 display and `XAUTHORITY`; it must record `DISPLAY`, toolkit identity, output geometry, screen insets, and panel-strut work area. It must not branch on `XDG_SESSION_TYPE`.

- [ ] **Step 3: Execute the native-X11 baseline and patched measurements**

Run the baseline against the recorded baseline source identity and the patched probe against the current integration build. On the native X11 session with the Plasma panel strut, require these patched observations:

- screen bounds `(0,0,2240,1400)` and work area `(0,32,2240,1368)` or the equivalent values reported by the active display;
- the exact `sun.awt.X11.XToolkit` class;
- an active `X11PopupFactory` whose delegate is the current FlatLaf shared factory;
- an owner-local correction matching the injected split-cache witness, `(-240,-180)`, when that fault scenario is enabled;
- popup native placement error equal to zero within integer-pixel tolerance;
- a heavyweight popup that remains visible long enough for a real pointer selection and a `popup-item-selected` record;
- frame correction requests bounded at ten for one unchanged maximized target, with no new frame correction loop caused by opening a popup; and
- successful restore and remaximize popup selection with the same owner-local calibration contract.

Retain the baseline evidence showing the prior split-cache placement error `(240,180)` where available. If the native-X11 or XWayland display is unavailable, write that exact verification limitation into the report, include the completed desktop-free test results, and leave the acceptance status explicitly unverified rather than converting the limitation into a pass.

- [ ] **Step 4: Run non-X11 and full automated regression checks**

Run the complete focused vector and the application compile again:

```bash
JAVA_HOME="$HOME/.sdkman/candidates/java/21.0.8-zulu" PATH="$JAVA_HOME/bin:$PATH" gradle :freeplane:test --tests "org.freeplane.core.ui.components.FrameResynchronizerTest" --tests "org.freeplane.core.ui.components.PopupCoordinateCalibratorTest" --tests "org.freeplane.core.ui.components.X11PopupFactoryTest" --tests "org.freeplane.core.ui.components.FrameResynchronizerPopupFactoryTest"
JAVA_HOME="$HOME/.sdkman/candidates/java/21.0.8-zulu" PATH="$JAVA_HOME/bin:$PATH" gradle :freeplane:compileJava
```

Use the injected `false` toolkit tests to verify unchanged non-X11 behavior. Record that macOS and Windows require pass-through source behavior; do not claim native runtime results for platforms that were not exercised.

- [ ] **Step 5: Write the acceptance report and inspect the final diff**

Write `acceptance-report.md` with the actual commit identities, commands, test counts, runtime/toolkit identity, work-area values, correction values, popup persistence and selection records, frame correction counts, baseline comparison, and any unavailable-runtime limitation. Do not omit a failed or unavailable acceptance run.

Run:

```bash
git diff --check
git status --short
git log -6 --oneline --decorate
```

Confirm that no generated class files, auxiliary logs outside the evidence directory, or delivery-target changes are staged. Commit the probe, bounded runner, and report:

```bash
git add ai-specs/tasks/freeplane-frame-resynchronizer-x11-evidence/epoch-2
git commit -m "Record X11 popup calibration acceptance evidence"
```
