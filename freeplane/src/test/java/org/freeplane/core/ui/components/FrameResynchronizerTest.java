package org.freeplane.core.ui.components;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.lang.reflect.Field;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.junit.Test;

public class FrameResynchronizerTest {
    private static final Rectangle PRIMARY_SCREEN = new Rectangle(0, 0, 2560, 1440);
    private static final Rectangle LEFT_SCREEN = new Rectangle(0, 0, 1920, 1080);
    private static final Rectangle RIGHT_SCREEN = new Rectangle(5120, 0, 1920, 1080);
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
    public void getWorkAreaBounds_withMissingBoundsOrInsets_returnsNull() {
        assertThat(FrameResynchronizer.getWorkAreaBounds(null, new Insets(0, 0, 0, 0))).isNull();
        assertThat(FrameResynchronizer.getWorkAreaBounds(FULL_SCREEN, null)).isNull();
    }

    @Test
    public void getWorkAreaBounds_withNonPositiveDerivedDimensions_returnsNull() {
        assertThat(FrameResynchronizer.getWorkAreaBounds(FULL_SCREEN, new Insets(0, 1920, 0, 0))).isNull();
        assertThat(FrameResynchronizer.getWorkAreaBounds(FULL_SCREEN, new Insets(1080, 0, 0, 0))).isNull();
        assertThat(FrameResynchronizer.getWorkAreaBounds(FULL_SCREEN, new Insets(0, 1921, 0, 0))).isNull();
        assertThat(FrameResynchronizer.getWorkAreaBounds(FULL_SCREEN, new Insets(1081, 0, 0, 0))).isNull();
    }

    @Test
    public void getScreenWorkAreas_withTopInset_usesShrunkConfigurationBounds() {
        final GraphicsConfiguration graphicsConfiguration = mock(GraphicsConfiguration.class);
        final FrameResynchronizer.FrameGeometry geometry = mock(FrameResynchronizer.FrameGeometry.class);
        when(graphicsConfiguration.getBounds()).thenReturn(FULL_SCREEN);
        when(geometry.getScreenInsets(graphicsConfiguration)).thenReturn(new Insets(32, 0, 0, 0));

        assertThat(FrameResynchronizer.getScreenWorkAreas(
            new GraphicsConfiguration[] { graphicsConfiguration }, geometry))
            .containsExactly(TOP_PANEL_WORK_AREA);
    }

    @Test
    public void getScreenWorkAreas_withFailedInsetLookup_marksAreaUnavailable() {
        final GraphicsConfiguration graphicsConfiguration = mock(GraphicsConfiguration.class);
        final FrameResynchronizer.FrameGeometry geometry = mock(FrameResynchronizer.FrameGeometry.class);
        when(graphicsConfiguration.getBounds()).thenReturn(FULL_SCREEN);
        when(geometry.getScreenInsets(graphicsConfiguration)).thenThrow(new IllegalStateException("insets unavailable"));

        assertThat(FrameResynchronizer.getScreenWorkAreas(
            new GraphicsConfiguration[] { graphicsConfiguration }, geometry))
            .containsExactly((Rectangle) null);
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

    @Test
    public void findTargetScreenBounds_withNativeGeometry_matchesContainingScreen() {
        // Native geometry reports window on LEFT_SCREEN (0, 0, 1920x1080)
        // even if stale normalBounds was on RIGHT_SCREEN (5120, 0, 1920x1080)
        final Rectangle nativeBounds = new Rectangle(0, 32, 1920, 1048);

        assertThat(FrameResynchronizer.findTargetScreenBounds(nativeBounds,
            new Rectangle[] { LEFT_SCREEN, PRIMARY_SCREEN, RIGHT_SCREEN })).isEqualTo(LEFT_SCREEN);
    }

    @Test
    public void needsNormalWindowResynchronization_withMismatchedPositions_returnsTrue() {
        // KWin placed the normal window on LEFT_SCREEN while AWT still believes it is on RIGHT_SCREEN.
        final Rectangle frameBounds = new Rectangle(2240, 180, 1920, 1080);
        final Rectangle nativeBounds = new Rectangle(5120, 0, 1920, 1080);

        assertThat(FrameResynchronizer.needsNormalWindowResynchronization(
            true, Frame.NORMAL, frameBounds, nativeBounds)).isTrue();
    }

    @Test
    public void needsNormalWindowResynchronization_withMatchingBounds_returnsFalse() {
        final Rectangle frameBounds = new Rectangle(5120, 0, 1920, 1080);

        assertThat(FrameResynchronizer.needsNormalWindowResynchronization(
            true, Frame.NORMAL, frameBounds, new Rectangle(frameBounds))).isFalse();
    }

    @Test
    public void needsNormalWindowResynchronization_withSizeMismatch_returnsFalse() {
        // A size difference means the window manager is still placing the window.
        // Acting on transient geometry must be avoided.
        final Rectangle frameBounds = new Rectangle(5120, 0, 1920, 1080);

        assertThat(FrameResynchronizer.needsNormalWindowResynchronization(
            true, Frame.NORMAL, frameBounds, new Rectangle(5120, 0, 1912, 1040))).isFalse();
    }

    @Test
    public void needsNormalWindowResynchronization_whenMaximized_returnsFalse() {
        final Rectangle frameBounds = new Rectangle(2240, 180, 1920, 1080);

        assertThat(FrameResynchronizer.needsNormalWindowResynchronization(
            true, Frame.MAXIMIZED_BOTH, frameBounds, new Rectangle(5120, 0, 1920, 1080))).isFalse();
    }

    @Test
    public void needsNormalWindowResynchronization_whenIconified_returnsFalse() {
        final Rectangle frameBounds = new Rectangle(2240, 180, 1920, 1080);

        assertThat(FrameResynchronizer.needsNormalWindowResynchronization(
            true, Frame.NORMAL | Frame.ICONIFIED, frameBounds, new Rectangle(5120, 0, 1920, 1080))).isFalse();
    }

    @Test
    public void needsNormalWindowResynchronization_withNonX11Toolkit_returnsFalse() {
        final Rectangle frameBounds = new Rectangle(2240, 180, 1920, 1080);

        assertThat(FrameResynchronizer.needsNormalWindowResynchronization(
            false, Frame.NORMAL, frameBounds, new Rectangle(5120, 0, 1920, 1080))).isFalse();
    }

    @Test
    public void needsNormalWindowResynchronization_withMissingBounds_returnsFalse() {
        assertThat(FrameResynchronizer.needsNormalWindowResynchronization(
            true, Frame.NORMAL, null, new Rectangle(5120, 0, 1920, 1080))).isFalse();
        assertThat(FrameResynchronizer.needsNormalWindowResynchronization(
            true, Frame.NORMAL, new Rectangle(2240, 180, 1920, 1080), null)).isFalse();
    }

    @Test
    public void needsNormalWindowResynchronization_withSmallCoordinateDifference_returnsFalse() {
        final Rectangle frameBounds = new Rectangle(5119, 1, 1920, 1080);

        assertThat(FrameResynchronizer.needsNormalWindowResynchronization(
            true, Frame.NORMAL, frameBounds, new Rectangle(5120, 0, 1920, 1080))).isFalse();
    }

    @Test
    public void findTargetScreenBounds_returnsScreenWithLargestFrameIntersection() {
        final Rectangle normalBounds = new Rectangle(5270, 84, 1608, 940);

        assertThat(FrameResynchronizer.findTargetScreenBounds(normalBounds,
            new Rectangle[] { LEFT_SCREEN, PRIMARY_SCREEN, RIGHT_SCREEN })).isEqualTo(RIGHT_SCREEN);
    }

    @Test
    public void findTargetScreenBounds_prefersScreenContainingMostOfCrossingFrame() {
        final Rectangle normalBounds = new Rectangle(1800, 100, 1600, 900);

        assertThat(FrameResynchronizer.findTargetScreenBounds(normalBounds,
            new Rectangle[] { LEFT_SCREEN, PRIMARY_SCREEN, RIGHT_SCREEN })).isEqualTo(PRIMARY_SCREEN);
    }

    @Test
    public void findTargetScreenBounds_withMissingOrDisjointBounds_returnsNull() {
        assertThat(FrameResynchronizer.findTargetScreenBounds(null,
            new Rectangle[] { LEFT_SCREEN })).isNull();
        assertThat(FrameResynchronizer.findTargetScreenBounds(new Rectangle(3000, 100, 100, 100),
            new Rectangle[] { LEFT_SCREEN, PRIMARY_SCREEN, RIGHT_SCREEN })).isNull();
    }

    @Test
    public void findTargetScreenBounds_ignoresMissingScreenBounds() {
        final Rectangle normalBounds = new Rectangle(5270, 84, 1608, 940);

        assertThat(FrameResynchronizer.findTargetScreenBounds(normalBounds,
            new Rectangle[] { null, RIGHT_SCREEN })).isEqualTo(RIGHT_SCREEN);
    }

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
    public void needsFrameResynchronization_withStaleNormalX11FrameLocation_returnsTrue() {
        final Rectangle restoredBounds = new Rectangle(2616, 363, 1912, 1008);

        assertThat(FrameResynchronizer.needsFrameResynchronization(
            true, Frame.NORMAL, new Rectangle(1920, 0, 1912, 1008), restoredBounds)).isTrue();
    }

    @Test
    public void needsFrameResynchronization_withCurrentNormalFrameLocation_returnsFalse() {
        final Rectangle restoredBounds = new Rectangle(2616, 363, 1912, 1008);

        assertThat(FrameResynchronizer.needsFrameResynchronization(
            true, Frame.NORMAL, restoredBounds, restoredBounds)).isFalse();
    }

    @Test
    public void needsFrameResynchronization_withNonX11Toolkit_returnsFalse() {
        final Rectangle restoredBounds = new Rectangle(2616, 363, 1912, 1008);

        assertThat(FrameResynchronizer.needsFrameResynchronization(
            false, Frame.NORMAL, new Rectangle(1920, 0, 1912, 1008), restoredBounds)).isFalse();
    }

    @Test
    public void needsFrameResynchronization_whenIconified_returnsFalse() {
        final Rectangle restoredBounds = new Rectangle(2616, 363, 1912, 1008);

        assertThat(FrameResynchronizer.needsFrameResynchronization(
            true, Frame.NORMAL | Frame.ICONIFIED, new Rectangle(1920, 0, 1912, 1008), restoredBounds)).isFalse();
    }

    @Test
    public void needsFrameResynchronization_withMissingBounds_returnsFalse() {
        final Rectangle restoredBounds = new Rectangle(2616, 363, 1912, 1008);

        assertThat(FrameResynchronizer.needsFrameResynchronization(
            true, Frame.NORMAL, null, restoredBounds)).isFalse();
        assertThat(FrameResynchronizer.needsFrameResynchronization(
            true, Frame.NORMAL, new Rectangle(1920, 0, 1912, 1008), null)).isFalse();
    }

    @Test
    public void needsNormalFrameResynchronization_whenLeavingMaximizedWithStaleLocation_returnsTrue() {
        final Rectangle normalBounds = new Rectangle(2616, 363, 1912, 1008);

        assertThat(FrameResynchronizer.needsNormalFrameResynchronization(
            true, Frame.MAXIMIZED_BOTH, Frame.NORMAL, new Rectangle(1920, 0, 1912, 1008), normalBounds)).isTrue();
    }

    @Test
    public void needsNormalFrameResynchronization_whenStillMaximized_returnsFalse() {
        final Rectangle normalBounds = new Rectangle(2616, 363, 1912, 1008);

        assertThat(FrameResynchronizer.needsNormalFrameResynchronization(
            true, Frame.MAXIMIZED_BOTH, Frame.MAXIMIZED_BOTH,
            new Rectangle(1920, 0, 1912, 1008), normalBounds)).isFalse();
    }

    @Test
    public void needsNormalFrameResynchronization_whenNotLeavingMaximized_returnsFalse() {
        final Rectangle normalBounds = new Rectangle(2616, 363, 1912, 1008);

        assertThat(FrameResynchronizer.needsNormalFrameResynchronization(
            true, Frame.NORMAL, Frame.NORMAL, new Rectangle(1920, 0, 1912, 1008), normalBounds)).isFalse();
    }

    @Test
    public void needsNormalFrameResynchronization_whenIconified_returnsFalse() {
        final Rectangle normalBounds = new Rectangle(2616, 363, 1912, 1008);

        assertThat(FrameResynchronizer.needsNormalFrameResynchronization(
            true, Frame.MAXIMIZED_BOTH, Frame.ICONIFIED,
            new Rectangle(1920, 0, 1912, 1008), normalBounds)).isFalse();
    }

    @Test
    public void needsNormalFrameResynchronization_withNonX11Toolkit_returnsFalse() {
        final Rectangle normalBounds = new Rectangle(2616, 363, 1912, 1008);

        assertThat(FrameResynchronizer.needsNormalFrameResynchronization(
            false, Frame.MAXIMIZED_BOTH, Frame.NORMAL,
            new Rectangle(1920, 0, 1912, 1008), normalBounds)).isFalse();
    }

    @Test
    public void needsMaximizedFrameResynchronization_withStaleX11FrameLocation_returnsTrue() {
        assertThat(FrameResynchronizer.needsMaximizedFrameResynchronization(
            true, Frame.MAXIMIZED_BOTH, new Rectangle(150, 100, 2560, 1440), PRIMARY_SCREEN)).isTrue();
    }

    @Test
    public void needsMaximizedFrameResynchronization_withNonX11Toolkit_returnsFalse() {
        assertThat(FrameResynchronizer.needsMaximizedFrameResynchronization(
            false, Frame.MAXIMIZED_BOTH, new Rectangle(150, 100, 2560, 1440), PRIMARY_SCREEN)).isFalse();
    }

    @Test
    public void needsMaximizedFrameResynchronization_withoutBothMaximizeStateBits_returnsFalse() {
        assertThat(FrameResynchronizer.needsMaximizedFrameResynchronization(
            true, Frame.MAXIMIZED_HORIZ, new Rectangle(150, 100, 2560, 1440), PRIMARY_SCREEN)).isFalse();
    }

    @Test
    public void needsMaximizedFrameResynchronization_whenIconified_returnsFalse() {
        assertThat(FrameResynchronizer.needsMaximizedFrameResynchronization(
            true, Frame.MAXIMIZED_BOTH | Frame.ICONIFIED, new Rectangle(150, 100, 2560, 1440), PRIMARY_SCREEN)).isFalse();
    }

    @Test
    public void needsMaximizedFrameResynchronization_withCurrentSecondaryScreenOrigin_returnsFalse() {
        final Rectangle secondaryScreen = new Rectangle(2560, 0, 1920, 1080);

        assertThat(FrameResynchronizer.needsMaximizedFrameResynchronization(
            true, Frame.MAXIMIZED_BOTH, new Rectangle(2560, 0, 1920, 1080), secondaryScreen)).isFalse();
    }

    @Test
    public void needsMaximizedFrameResynchronization_withStaleSecondaryScreenLocation_returnsTrue() {
        final Rectangle secondaryScreen = new Rectangle(2560, 0, 1920, 1080);

        assertThat(FrameResynchronizer.needsMaximizedFrameResynchronization(
            true, Frame.MAXIMIZED_BOTH, new Rectangle(150, 100, 1920, 1080), secondaryScreen)).isTrue();
    }

    @Test
    public void needsMaximizedFrameResynchronization_withMissingBounds_returnsFalse() {
        assertThat(FrameResynchronizer.needsMaximizedFrameResynchronization(
            true, Frame.MAXIMIZED_BOTH, null, PRIMARY_SCREEN)).isFalse();
        assertThat(FrameResynchronizer.needsMaximizedFrameResynchronization(
            true, Frame.MAXIMIZED_BOTH, PRIMARY_SCREEN, null)).isFalse();
    }

    @Test
    public void needsMaximizedFrameResynchronization_withCurrentScreenOrigin_returnsFalse() {
        assertThat(FrameResynchronizer.needsMaximizedFrameResynchronization(
            true, Frame.MAXIMIZED_BOTH, new Rectangle(0, 0, 2560, 1440), PRIMARY_SCREEN)).isFalse();
    }

    @Test
    public void needsMaximizedFrameResynchronization_whenWindowManagerAlreadyMaximized_returnsFalse() {
        // Measured on KWin/XWayland: the WM maximizes the CLIENT area to the work
        // area (0,0,1920,1080) while AWT keeps reporting the decorated outer frame
        // (-4,-34,1928,1120). The window is already correct, so no bounds request is
        // needed; deciding from AWT bounds retried 69 times for one maximize.
        final Rectangle awtOuter = new Rectangle(-4, -34, 1928, 1120);
        final Rectangle nativeClient = new Rectangle(0, 0, 1920, 1080);
        final Rectangle workArea = new Rectangle(0, 0, 1920, 1080);
        assertThat(FrameResynchronizer.needsMaximizedFrameResynchronization(
            true, Frame.MAXIMIZED_BOTH, awtOuter, workArea)).isTrue();
        assertThat(FrameResynchronizer.needsMaximizedFrameResynchronization(
            true, Frame.MAXIMIZED_BOTH, nativeClient, workArea)).isFalse();
    }

    @Test
    public void needsMaximizedFrameResynchronization_whenNativeStillOffTarget_returnsTrue() {
        assertThat(FrameResynchronizer.needsMaximizedFrameResynchronization(
            true, Frame.MAXIMIZED_BOTH, new Rectangle(204, 184, 1920, 1080),
            new Rectangle(0, 0, 1920, 1080))).isTrue();
    }

    @Test
    public void maximizedComponentEvent_whenWindowManagerAlreadyMaximized_requestsNoBounds() throws Exception {
        final Rectangle workArea = new Rectangle(0, 0, 1920, 1080);
        final Rectangle awtOuter = new Rectangle(-4, -34, 1928, 1120);
        final JFrame frame = resynchronizerFrame(awtOuter, Frame.MAXIMIZED_BOTH);
        // Native geometry already covers the work area; only AWT bounds disagree.
        final FrameResynchronizer resynchronizer = maximizedResynchronizer(frame, workArea, workArea);

        SwingUtilities.invokeAndWait(() -> {
            try {
                resynchronizer.componentMoved(mock(ComponentEvent.class));

                verify(frame, never()).setBounds(any(Rectangle.class));
                verify(frame, never()).validate();
                assertThat(deferredResynchronizationTimer(resynchronizer)).isNull();
            }
            finally {
                stopDeferredResynchronizationTimer(resynchronizer);
            }
        });
    }

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
        for (int attempt = 1; attempt <= 10; attempt++) {
            assertThat(guard.shouldRequest(current, secondTarget)).isTrue();
        }
        assertThat(guard.shouldRequest(current, secondTarget)).isFalse();
        for (int attempt = 1; attempt <= 10; attempt++) {
            assertThat(guard.shouldRequest(current, firstTarget)).isTrue();
        }
        assertThat(guard.shouldRequest(current, firstTarget)).isFalse();
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

    @Test
    public void maximizedListener_stopsAfterTenSameTargetCorrections() {
        final Rectangle current = new Rectangle(0, 0, 1920, 1080);
        final JFrame frame = resynchronizerFrame(current, Frame.MAXIMIZED_BOTH);
        final FrameResynchronizer resynchronizer = maximizedResynchronizer(frame, current, TOP_PANEL_WORK_AREA);

        for (int attempt = 1; attempt <= 11; attempt++) {
            resynchronizer.windowStateChanged(windowStateChangedEvent(Frame.NORMAL, Frame.MAXIMIZED_BOTH));
        }

        verify(frame, times(10)).setBounds(TOP_PANEL_WORK_AREA);
    }

    @Test
    public void maximizedListener_leavingAndReenteringMaximizedResetsGuard() {
        final Rectangle current = new Rectangle(0, 0, 1920, 1080);
        final JFrame frame = resynchronizerFrame(current, Frame.MAXIMIZED_BOTH);
        final FrameResynchronizer resynchronizer = maximizedResynchronizer(frame, current, TOP_PANEL_WORK_AREA);

        for (int attempt = 1; attempt <= 10; attempt++) {
            resynchronizer.windowStateChanged(windowStateChangedEvent(Frame.NORMAL, Frame.MAXIMIZED_BOTH));
        }
        when(frame.getExtendedState()).thenReturn(Frame.NORMAL);
        resynchronizer.windowStateChanged(windowStateChangedEvent(Frame.MAXIMIZED_BOTH, Frame.NORMAL));
        when(frame.getExtendedState()).thenReturn(Frame.MAXIMIZED_BOTH);
        resynchronizer.windowStateChanged(windowStateChangedEvent(Frame.NORMAL, Frame.MAXIMIZED_BOTH));

        verify(frame, times(11)).setBounds(TOP_PANEL_WORK_AREA);
    }

    @Test
    public void maximizedComponentEvents_andDeferredRetries_shareTenRequestBudget() throws Exception {
        final JFrame frame = resynchronizerFrame(FULL_SCREEN, Frame.MAXIMIZED_BOTH);
        final FrameResynchronizer resynchronizer = maximizedResynchronizer(frame, FULL_SCREEN, TOP_PANEL_WORK_AREA);
        final ComponentEvent event = mock(ComponentEvent.class);

        SwingUtilities.invokeAndWait(() -> {
            try {
                // Keep reporting the same stale bounds, as if the WM ignored each request.
                for (int cycle = 1; cycle <= 5; cycle++) {
                    resynchronizer.componentMoved(event);
                    verify(frame, times(2 * cycle - 1)).setBounds(TOP_PANEL_WORK_AREA);
                    fireDeferredResynchronizationTimer(resynchronizer);
                    verify(frame, times(2 * cycle)).setBounds(TOP_PANEL_WORK_AREA);
                }

                final Timer pendingTimer = deferredResynchronizationTimer(resynchronizer);
                assertThat(pendingTimer).isNotNull();
                for (int eventCount = 0; eventCount < 3; eventCount++) {
                    resynchronizer.componentMoved(event);
                    verify(frame, times(10)).setBounds(any(Rectangle.class));
                    assertThat(deferredResynchronizationTimer(resynchronizer)).isSameAs(pendingTimer);
                }
                fireDeferredResynchronizationTimer(resynchronizer);
                verify(frame, times(10)).setBounds(any(Rectangle.class));
                verify(frame, times(10)).validate();
                assertThat(deferredResynchronizationTimer(resynchronizer)).isNull();
            }
            finally {
                stopDeferredResynchronizationTimer(resynchronizer);
            }
        });
    }

    @Test
    public void maximizedComponentEvents_withCachedAwtAtTarget_retryNativeMismatchOnlyTenTimes() throws Exception {
        final JFrame frame = resynchronizerFrame(TOP_PANEL_WORK_AREA, Frame.MAXIMIZED_BOTH);
        final Rectangle nativeBounds = new Rectangle(240, 212, 1920, 1048);
        final FrameResynchronizer resynchronizer = maximizedResynchronizer(frame, nativeBounds, TOP_PANEL_WORK_AREA);
        final ComponentEvent event = mock(ComponentEvent.class);

        SwingUtilities.invokeAndWait(() -> {
            try {
                resynchronizer.componentMoved(event);
                verify(frame).setBounds(TOP_PANEL_WORK_AREA);
                verify(frame).validate();
                for (int attempt = 2; attempt <= 10; attempt++) {
                    fireDeferredResynchronizationTimer(resynchronizer);
                    verify(frame, times(attempt)).setBounds(TOP_PANEL_WORK_AREA);
                }

                // AWT stays at target while native geometry keeps ignoring the requests.
                fireDeferredResynchronizationTimer(resynchronizer);
                assertThat(deferredResynchronizationTimer(resynchronizer)).isNull();
                for (int eventCount = 0; eventCount < 3; eventCount++) {
                    resynchronizer.componentMoved(event);
                    resynchronizer.componentResized(event);
                    verify(frame, times(10)).setBounds(any(Rectangle.class));
                    assertThat(deferredResynchronizationTimer(resynchronizer)).isNull();
                }
                verify(frame, times(10)).validate();
            }
            finally {
                stopDeferredResynchronizationTimer(resynchronizer);
            }
        });
    }

    @Test
    public void maximizedComponentEvents_afterNativeConvergence_resetExhaustedSameTargetBudget() throws Exception {
        final Rectangle awtOuter = new Rectangle(-4, -2, 1928, 1086);
        final JFrame frame = resynchronizerFrame(awtOuter, Frame.MAXIMIZED_BOTH);
        final Rectangle displacedNativeBounds = new Rectangle(240, 212, 1920, 1048);
        final FrameResynchronizer.FrameGeometry geometry = mock(FrameResynchronizer.FrameGeometry.class);
        when(geometry.isX11Toolkit()).thenReturn(true);
        when(geometry.getNativeWindowBounds(frame)).thenReturn(displacedNativeBounds);
        when(geometry.getScreenWorkAreas()).thenReturn(new Rectangle[] { TOP_PANEL_WORK_AREA });
        final FrameResynchronizer resynchronizer = new FrameResynchronizer(frame, geometry);
        final ComponentEvent event = mock(ComponentEvent.class);

        SwingUtilities.invokeAndWait(() -> {
            try {
                resynchronizer.componentMoved(event);
                for (int attempt = 2; attempt <= 10; attempt++) {
                    fireDeferredResynchronizationTimer(resynchronizer);
                }
                verify(frame, times(10)).setBounds(TOP_PANEL_WORK_AREA);
                fireDeferredResynchronizationTimer(resynchronizer);
                assertThat(deferredResynchronizationTimer(resynchronizer)).isNull();
                for (int eventCount = 0; eventCount < 3; eventCount++) {
                    resynchronizer.componentMoved(event);
                    verify(frame, times(10)).setBounds(any(Rectangle.class));
                    assertThat(deferredResynchronizationTimer(resynchronizer)).isNull();
                }

                // Missing native geometry is stable, but cannot replenish an exhausted budget.
                when(geometry.getNativeWindowBounds(frame)).thenReturn(null);
                resynchronizer.componentMoved(event);
                verify(frame, times(10)).setBounds(any(Rectangle.class));
                assertThat(deferredResynchronizationTimer(resynchronizer)).isNull();
                when(geometry.getNativeWindowBounds(frame)).thenReturn(displacedNativeBounds);
                resynchronizer.componentMoved(event);
                verify(frame, times(10)).setBounds(any(Rectangle.class));
                assertThat(deferredResynchronizationTimer(resynchronizer)).isNull();

                // Only native geometry converges; decorated AWT bounds remain different.
                when(geometry.getNativeWindowBounds(frame)).thenReturn(TOP_PANEL_WORK_AREA);
                resynchronizer.componentMoved(event);
                verify(frame, times(10)).setBounds(any(Rectangle.class));
                assertThat(deferredResynchronizationTimer(resynchronizer)).isNull();

                when(geometry.getNativeWindowBounds(frame)).thenReturn(displacedNativeBounds);
                resynchronizer.componentResized(event);
                verify(frame, times(11)).setBounds(TOP_PANEL_WORK_AREA);
                for (int attempt = 12; attempt <= 20; attempt++) {
                    fireDeferredResynchronizationTimer(resynchronizer);
                    verify(frame, times(attempt)).setBounds(TOP_PANEL_WORK_AREA);
                }
                fireDeferredResynchronizationTimer(resynchronizer);
                resynchronizer.componentMoved(event);
                verify(frame, times(20)).setBounds(any(Rectangle.class));
                verify(frame, times(20)).validate();
                assertThat(deferredResynchronizationTimer(resynchronizer)).isNull();
            }
            finally {
                stopDeferredResynchronizationTimer(resynchronizer);
            }
        });
    }

    @Test
    public void maximizedComponentEvents_afterDeferredExhaustion_doNotRestartTimer() throws Exception {
        final JFrame frame = resynchronizerFrame(FULL_SCREEN, Frame.MAXIMIZED_BOTH);
        final FrameResynchronizer resynchronizer = maximizedResynchronizer(frame, FULL_SCREEN, TOP_PANEL_WORK_AREA);
        final ComponentEvent event = mock(ComponentEvent.class);

        SwingUtilities.invokeAndWait(() -> {
            try {
                resynchronizer.componentMoved(event);
                for (int attempt = 2; attempt <= 10; attempt++) {
                    fireDeferredResynchronizationTimer(resynchronizer);
                }
                verify(frame, times(10)).setBounds(TOP_PANEL_WORK_AREA);

                // The next deferred check must stop without making an eleventh request.
                fireDeferredResynchronizationTimer(resynchronizer);
                verify(frame, times(10)).setBounds(any(Rectangle.class));
                assertThat(deferredResynchronizationTimer(resynchronizer)).isNull();
                for (int eventCount = 0; eventCount < 3; eventCount++) {
                    resynchronizer.componentMoved(event);
                    verify(frame, times(10)).setBounds(any(Rectangle.class));
                    assertThat(deferredResynchronizationTimer(resynchronizer)).isNull();
                }
                verify(frame, times(10)).validate();
            }
            finally {
                stopDeferredResynchronizationTimer(resynchronizer);
            }
        });
    }

    @Test
    public void maximizedComponentEvent_doesNotScheduleTimerWhenStable() throws Exception {
        final JFrame frame = resynchronizerFrame(TOP_PANEL_WORK_AREA, Frame.MAXIMIZED_BOTH);
        final FrameResynchronizer resynchronizer = maximizedResynchronizer(frame, TOP_PANEL_WORK_AREA,
            TOP_PANEL_WORK_AREA);

        SwingUtilities.invokeAndWait(() -> {
            try {
                resynchronizer.componentMoved(mock(ComponentEvent.class));

                verify(frame, never()).setBounds(any(Rectangle.class));
                verify(frame, never()).validate();
                assertThat(deferredResynchronizationTimer(resynchronizer)).isNull();
            }
            finally {
                stopDeferredResynchronizationTimer(resynchronizer);
            }
        });
    }

    @Test
    public void maximizedWindowOpened_doesNotScheduleTimerWhenStable() throws Exception {
        final FrameResynchronizer resynchronizer = maximizedResynchronizer(TOP_PANEL_WORK_AREA, TOP_PANEL_WORK_AREA);

        resynchronizer.windowOpened(mock(WindowEvent.class));

        final Timer timer = deferredResynchronizationTimer(resynchronizer);
        if (timer != null) {
            timer.stop();
        }
        assertThat(timer).isNull();
    }

    @Test
    public void maximizedComponentEvent_withUnavailableWorkAreasAndInsets_doesNotCorrectOrRetry() throws Exception {
        final Rectangle disjointBounds = new Rectangle(4000, 0, 100, 100);
        final JFrame frame = resynchronizerFrame(disjointBounds, Frame.MAXIMIZED_BOTH);
        final GraphicsConfiguration graphicsConfiguration = mock(GraphicsConfiguration.class);
        when(graphicsConfiguration.getBounds()).thenReturn(FULL_SCREEN);
        when(frame.getGraphicsConfiguration()).thenReturn(graphicsConfiguration);
        final FrameResynchronizer.FrameGeometry geometry = mock(FrameResynchronizer.FrameGeometry.class);
        when(geometry.isX11Toolkit()).thenReturn(true);
        when(geometry.getNativeWindowBounds(frame)).thenReturn(disjointBounds);
        when(geometry.getScreenWorkAreas()).thenReturn(new Rectangle[] { null, null });
        when(geometry.getScreenInsets(graphicsConfiguration)).thenReturn(null);
        final FrameResynchronizer resynchronizer = new FrameResynchronizer(frame, geometry);

        SwingUtilities.invokeAndWait(() -> {
            try {
                resynchronizer.componentMoved(mock(ComponentEvent.class));

                verify(frame, never()).setBounds(any(Rectangle.class));
                verify(frame, never()).validate();
                assertThat(deferredResynchronizationTimer(resynchronizer)).isNull();
            }
            finally {
                stopDeferredResynchronizationTimer(resynchronizer);
            }
        });
    }

    @Test
    public void maximizedListener_usesInsetShrunkFrameConfigurationFallback() {
        final Rectangle disjointBounds = new Rectangle(4000, 0, 100, 100);
        final JFrame frame = resynchronizerFrame(disjointBounds, Frame.MAXIMIZED_BOTH);
        final GraphicsConfiguration graphicsConfiguration = mock(GraphicsConfiguration.class);
        when(graphicsConfiguration.getBounds()).thenReturn(FULL_SCREEN);
        when(frame.getGraphicsConfiguration()).thenReturn(graphicsConfiguration);
        final FrameResynchronizer.FrameGeometry geometry = mock(FrameResynchronizer.FrameGeometry.class);
        when(geometry.isX11Toolkit()).thenReturn(true);
        when(geometry.getNativeWindowBounds(frame)).thenReturn(disjointBounds);
        when(geometry.getScreenWorkAreas()).thenReturn(new Rectangle[] { null });
        when(geometry.getScreenInsets(graphicsConfiguration)).thenReturn(new Insets(32, 0, 0, 0));
        final FrameResynchronizer resynchronizer = new FrameResynchronizer(frame, geometry);

        resynchronizer.windowStateChanged(windowStateChangedEvent(Frame.NORMAL, Frame.MAXIMIZED_BOTH));

        verify(frame).setBounds(TOP_PANEL_WORK_AREA);
        verify(frame).validate();
    }

    @Test
    public void maximizedComponentEvent_correctsSizeOnlyMismatchThroughActivePath() throws Exception {
        final Rectangle current = new Rectangle(0, 32, 1920, 1047);
        final JFrame frame = resynchronizerFrame(current, Frame.MAXIMIZED_BOTH);
        final FrameResynchronizer resynchronizer = maximizedResynchronizer(frame, current, TOP_PANEL_WORK_AREA);

        SwingUtilities.invokeAndWait(() -> {
            try {
                resynchronizer.componentResized(mock(ComponentEvent.class));

                verify(frame).setBounds(TOP_PANEL_WORK_AREA);
                verify(frame).validate();
            }
            finally {
                stopDeferredResynchronizationTimer(resynchronizer);
            }
        });
    }

    @Test
    public void maximizedComponentEvent_withNonX11Toolkit_appliesNothing() throws Exception {
        final Rectangle current = new Rectangle(0, 32, 1920, 1047);
        final JFrame frame = resynchronizerFrame(current, Frame.MAXIMIZED_BOTH);
        final FrameResynchronizer.FrameGeometry geometry = mock(FrameResynchronizer.FrameGeometry.class);
        when(geometry.isX11Toolkit()).thenReturn(false);
        when(geometry.getNativeWindowBounds(frame)).thenReturn(current);
        when(geometry.getScreenWorkAreas()).thenReturn(new Rectangle[] { TOP_PANEL_WORK_AREA });
        final FrameResynchronizer resynchronizer = new FrameResynchronizer(frame, geometry);

        SwingUtilities.invokeAndWait(() -> {
            try {
                resynchronizer.componentMoved(mock(ComponentEvent.class));

                verify(frame, never()).setBounds(any(Rectangle.class));
                verify(frame, never()).validate();
                assertThat(deferredResynchronizationTimer(resynchronizer)).isNull();
            }
            finally {
                stopDeferredResynchronizationTimer(resynchronizer);
            }
        });
    }

    private static FrameResynchronizer maximizedResynchronizer(final Rectangle current,
            final Rectangle target) {
        return maximizedResynchronizer(resynchronizerFrame(current, Frame.MAXIMIZED_BOTH), current, target);
    }

    private static FrameResynchronizer maximizedResynchronizer(final JFrame frame, final Rectangle nativeBounds,
            final Rectangle target) {
        final FrameResynchronizer.FrameGeometry geometry = mock(FrameResynchronizer.FrameGeometry.class);
        when(geometry.isX11Toolkit()).thenReturn(true);
        when(geometry.getNativeWindowBounds(frame)).thenReturn(nativeBounds);
        when(geometry.getScreenWorkAreas()).thenReturn(new Rectangle[] { target });
        return new FrameResynchronizer(frame, geometry);
    }

    private static JFrame resynchronizerFrame(final Rectangle bounds, final int windowState) {
        final JFrame frame = mock(JFrame.class);
        when(frame.getBounds()).thenReturn(bounds);
        when(frame.getExtendedState()).thenReturn(windowState);
        return frame;
    }

    private static WindowEvent windowStateChangedEvent(final int oldState, final int newState) {
        final WindowEvent event = mock(WindowEvent.class);
        when(event.getOldState()).thenReturn(oldState);
        when(event.getNewState()).thenReturn(newState);
        return event;
    }

    private static Timer deferredResynchronizationTimer(final FrameResynchronizer resynchronizer) {
        try {
            final Field timerField = FrameResynchronizer.class.getDeclaredField("deferredResynchronizationTimer");
            timerField.setAccessible(true);
            return (Timer) timerField.get(resynchronizer);
        }
        catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }

    private static void fireDeferredResynchronizationTimer(final FrameResynchronizer resynchronizer) {
        final Timer timer = deferredResynchronizationTimer(resynchronizer);
        assertThat(timer).isNotNull();
        timer.stop();
        for (final ActionListener listener : timer.getActionListeners()) {
            listener.actionPerformed(new ActionEvent(timer, ActionEvent.ACTION_PERFORMED, "test"));
        }
    }

    private static void stopDeferredResynchronizationTimer(final FrameResynchronizer resynchronizer) {
        final Timer timer = deferredResynchronizationTimer(resynchronizer);
        if (timer != null) {
            timer.stop();
        }
    }
}
