package org.freeplane.core.ui.components;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Frame;
import java.awt.Rectangle;

import org.junit.Test;

public class FrameResynchronizerTest {
    private static final Rectangle PRIMARY_SCREEN = new Rectangle(0, 0, 2560, 1440);
    private static final Rectangle LEFT_SCREEN = new Rectangle(0, 0, 1920, 1080);
    private static final Rectangle RIGHT_SCREEN = new Rectangle(5120, 0, 1920, 1080);

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
}
