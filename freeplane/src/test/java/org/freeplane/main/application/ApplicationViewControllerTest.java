package org.freeplane.main.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Frame;
import java.awt.Rectangle;

import org.junit.Test;

public class ApplicationViewControllerTest {
    private static final Rectangle PRIMARY_SCREEN = new Rectangle(0, 0, 2560, 1440);

    @Test
    public void needsFrameResynchronization_withStaleNormalX11FrameLocation_returnsTrue() {
        final Rectangle restoredBounds = new Rectangle(2616, 363, 1912, 1008);

        assertThat(ApplicationViewController.needsFrameResynchronization(
            true, Frame.NORMAL, new Rectangle(1920, 0, 1912, 1008), restoredBounds)).isTrue();
    }

    @Test
    public void needsFrameResynchronization_withCurrentNormalFrameLocation_returnsFalse() {
        final Rectangle restoredBounds = new Rectangle(2616, 363, 1912, 1008);

        assertThat(ApplicationViewController.needsFrameResynchronization(
            true, Frame.NORMAL, restoredBounds, restoredBounds)).isFalse();
    }

    @Test
    public void needsFrameResynchronization_withNonX11Toolkit_returnsFalse() {
        final Rectangle restoredBounds = new Rectangle(2616, 363, 1912, 1008);

        assertThat(ApplicationViewController.needsFrameResynchronization(
            false, Frame.NORMAL, new Rectangle(1920, 0, 1912, 1008), restoredBounds)).isFalse();
    }

    @Test
    public void needsFrameResynchronization_whenIconified_returnsFalse() {
        final Rectangle restoredBounds = new Rectangle(2616, 363, 1912, 1008);

        assertThat(ApplicationViewController.needsFrameResynchronization(
            true, Frame.NORMAL | Frame.ICONIFIED, new Rectangle(1920, 0, 1912, 1008), restoredBounds)).isFalse();
    }

    @Test
    public void needsFrameResynchronization_withMissingBounds_returnsFalse() {
        final Rectangle restoredBounds = new Rectangle(2616, 363, 1912, 1008);

        assertThat(ApplicationViewController.needsFrameResynchronization(
            true, Frame.NORMAL, null, restoredBounds)).isFalse();
        assertThat(ApplicationViewController.needsFrameResynchronization(
            true, Frame.NORMAL, new Rectangle(1920, 0, 1912, 1008), null)).isFalse();
    }

    @Test
    public void needsNormalFrameResynchronization_whenLeavingMaximizedWithStaleLocation_returnsTrue() {
        final Rectangle normalBounds = new Rectangle(2616, 363, 1912, 1008);

        assertThat(ApplicationViewController.needsNormalFrameResynchronization(
            true, Frame.MAXIMIZED_BOTH, Frame.NORMAL, new Rectangle(1920, 0, 1912, 1008), normalBounds)).isTrue();
    }

    @Test
    public void needsNormalFrameResynchronization_whenStillMaximized_returnsFalse() {
        final Rectangle normalBounds = new Rectangle(2616, 363, 1912, 1008);

        assertThat(ApplicationViewController.needsNormalFrameResynchronization(
            true, Frame.MAXIMIZED_BOTH, Frame.MAXIMIZED_BOTH,
            new Rectangle(1920, 0, 1912, 1008), normalBounds)).isFalse();
    }

    @Test
    public void needsNormalFrameResynchronization_whenNotLeavingMaximized_returnsFalse() {
        final Rectangle normalBounds = new Rectangle(2616, 363, 1912, 1008);

        assertThat(ApplicationViewController.needsNormalFrameResynchronization(
            true, Frame.NORMAL, Frame.NORMAL, new Rectangle(1920, 0, 1912, 1008), normalBounds)).isFalse();
    }

    @Test
    public void needsNormalFrameResynchronization_whenIconified_returnsFalse() {
        final Rectangle normalBounds = new Rectangle(2616, 363, 1912, 1008);

        assertThat(ApplicationViewController.needsNormalFrameResynchronization(
            true, Frame.MAXIMIZED_BOTH, Frame.ICONIFIED,
            new Rectangle(1920, 0, 1912, 1008), normalBounds)).isFalse();
    }

    @Test
    public void needsNormalFrameResynchronization_withNonX11Toolkit_returnsFalse() {
        final Rectangle normalBounds = new Rectangle(2616, 363, 1912, 1008);

        assertThat(ApplicationViewController.needsNormalFrameResynchronization(
            false, Frame.MAXIMIZED_BOTH, Frame.NORMAL,
            new Rectangle(1920, 0, 1912, 1008), normalBounds)).isFalse();
    }

    @Test
    public void needsMaximizedFrameResynchronization_withStaleX11FrameLocation_returnsTrue() {
        assertThat(ApplicationViewController.needsMaximizedFrameResynchronization(
            true, Frame.MAXIMIZED_BOTH, new Rectangle(150, 100, 2560, 1440), PRIMARY_SCREEN)).isTrue();
    }

    @Test
    public void needsMaximizedFrameResynchronization_withNonX11Toolkit_returnsFalse() {
        assertThat(ApplicationViewController.needsMaximizedFrameResynchronization(
            false, Frame.MAXIMIZED_BOTH, new Rectangle(150, 100, 2560, 1440), PRIMARY_SCREEN)).isFalse();
    }

    @Test
    public void needsMaximizedFrameResynchronization_withoutBothMaximizeStateBits_returnsFalse() {
        assertThat(ApplicationViewController.needsMaximizedFrameResynchronization(
            true, Frame.MAXIMIZED_HORIZ, new Rectangle(150, 100, 2560, 1440), PRIMARY_SCREEN)).isFalse();
    }

    @Test
    public void needsMaximizedFrameResynchronization_whenIconified_returnsFalse() {
        assertThat(ApplicationViewController.needsMaximizedFrameResynchronization(
            true, Frame.MAXIMIZED_BOTH | Frame.ICONIFIED, new Rectangle(150, 100, 2560, 1440), PRIMARY_SCREEN)).isFalse();
    }

    @Test
    public void needsMaximizedFrameResynchronization_withCurrentSecondaryScreenOrigin_returnsFalse() {
        final Rectangle secondaryScreen = new Rectangle(2560, 0, 1920, 1080);

        assertThat(ApplicationViewController.needsMaximizedFrameResynchronization(
            true, Frame.MAXIMIZED_BOTH, new Rectangle(2560, 0, 1920, 1080), secondaryScreen)).isFalse();
    }

    @Test
    public void needsMaximizedFrameResynchronization_withStaleSecondaryScreenLocation_returnsTrue() {
        final Rectangle secondaryScreen = new Rectangle(2560, 0, 1920, 1080);

        assertThat(ApplicationViewController.needsMaximizedFrameResynchronization(
            true, Frame.MAXIMIZED_BOTH, new Rectangle(150, 100, 1920, 1080), secondaryScreen)).isTrue();
    }

    @Test
    public void needsMaximizedFrameResynchronization_withMissingBounds_returnsFalse() {
        assertThat(ApplicationViewController.needsMaximizedFrameResynchronization(
            true, Frame.MAXIMIZED_BOTH, null, PRIMARY_SCREEN)).isFalse();
        assertThat(ApplicationViewController.needsMaximizedFrameResynchronization(
            true, Frame.MAXIMIZED_BOTH, PRIMARY_SCREEN, null)).isFalse();
    }

    @Test
    public void needsMaximizedFrameResynchronization_withCurrentScreenOrigin_returnsFalse() {
        assertThat(ApplicationViewController.needsMaximizedFrameResynchronization(
            true, Frame.MAXIMIZED_BOTH, new Rectangle(0, 0, 2560, 1440), PRIMARY_SCREEN)).isFalse();
    }
}
