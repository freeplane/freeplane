package org.freeplane.main.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Frame;
import java.awt.Rectangle;

import org.junit.Test;

public class ApplicationViewControllerTest {
    private static final Rectangle PRIMARY_SCREEN = new Rectangle(0, 0, 2560, 1440);

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
