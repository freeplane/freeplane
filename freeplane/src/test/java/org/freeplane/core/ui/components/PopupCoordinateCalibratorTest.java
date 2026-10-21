package org.freeplane.core.ui.components;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JFrame;

import org.junit.Test;

public class PopupCoordinateCalibratorTest {
    @Test
    public void calculateCorrection_withSplitCacheDelta_returnsNegativePlacementError() {
        final JFrame frame = mock(JFrame.class);
        final Component owner = mock(Component.class);
        final PopupCoordinateCalibrator.GeometryProvider geometry = mock(
            PopupCoordinateCalibrator.GeometryProvider.class);
        when(geometry.getNativeClientOrigin(frame)).thenReturn(new Point(0, 0));
        when(geometry.getOwnerLocationWithinFrame(owner, frame)).thenReturn(new Point(1035, 5));
        when(geometry.getOwnerLocationOnScreen(owner)).thenReturn(new Point(1275, 185));

        assertThat(PopupCoordinateCalibrator.calculateCorrection(owner, frame, geometry))
            .isEqualTo(new Point(-240, -180));
    }

    @Test
    public void calculateCorrection_whenOwnerAlreadyAgrees_returnsZero() {
        final JFrame frame = mock(JFrame.class);
        final Component owner = mock(Component.class);
        final PopupCoordinateCalibrator.GeometryProvider geometry = mock(
            PopupCoordinateCalibrator.GeometryProvider.class);
        when(geometry.getNativeClientOrigin(frame)).thenReturn(new Point(240, 180));
        when(geometry.getOwnerLocationWithinFrame(owner, frame)).thenReturn(new Point(10, 20));
        when(geometry.getOwnerLocationOnScreen(owner)).thenReturn(new Point(250, 200));

        assertThat(PopupCoordinateCalibrator.calculateCorrection(owner, frame, geometry))
            .isEqualTo(new Point(0, 0));
    }

    @Test
    public void calculateCorrection_withPositiveDelta_returnsPositiveCorrection() {
        final JFrame frame = mock(JFrame.class);
        final Component owner = mock(Component.class);
        final PopupCoordinateCalibrator.GeometryProvider geometry = mock(
            PopupCoordinateCalibrator.GeometryProvider.class);
        when(geometry.getNativeClientOrigin(frame)).thenReturn(new Point(240, 180));
        when(geometry.getOwnerLocationWithinFrame(owner, frame)).thenReturn(new Point(10, 20));
        when(geometry.getOwnerLocationOnScreen(owner)).thenReturn(new Point(200, 150));

        assertThat(PopupCoordinateCalibrator.calculateCorrection(owner, frame, geometry))
            .isEqualTo(new Point(50, 50));
    }

    @Test
    public void calculateCorrection_withNegativeFrameAndOwnerOrigin_returnsZero() {
        final JFrame frame = mock(JFrame.class);
        final Component owner = mock(Component.class);
        final PopupCoordinateCalibrator.GeometryProvider geometry = mock(
            PopupCoordinateCalibrator.GeometryProvider.class);
        when(geometry.getNativeClientOrigin(frame)).thenReturn(new Point(-1200, -40));
        when(geometry.getOwnerLocationWithinFrame(owner, frame)).thenReturn(new Point(4, 30));
        when(geometry.getOwnerLocationOnScreen(owner)).thenReturn(new Point(-1196, -10));

        assertThat(PopupCoordinateCalibrator.calculateCorrection(owner, frame, geometry))
            .isEqualTo(new Point(0, 0));
    }

    @Test
    public void calculateCorrection_withoutNativeFrameBounds_returnsNull() {
        final JFrame frame = mock(JFrame.class);
        final Component owner = mock(Component.class);
        final PopupCoordinateCalibrator.GeometryProvider geometry = mock(
            PopupCoordinateCalibrator.GeometryProvider.class);
        when(geometry.getNativeClientOrigin(frame)).thenReturn(null);
        when(geometry.getOwnerLocationWithinFrame(owner, frame)).thenReturn(new Point(10, 20));
        when(geometry.getOwnerLocationOnScreen(owner)).thenReturn(new Point(250, 200));

        assertThat(PopupCoordinateCalibrator.calculateCorrection(owner, frame, geometry)).isNull();
    }

    @Test
    public void calculateCorrection_withoutOwnerLocationWithinFrame_returnsNull() {
        final JFrame frame = mock(JFrame.class);
        final Component owner = mock(Component.class);
        final PopupCoordinateCalibrator.GeometryProvider geometry = mock(
            PopupCoordinateCalibrator.GeometryProvider.class);
        when(geometry.getNativeClientOrigin(frame)).thenReturn(new Point(240, 180));
        when(geometry.getOwnerLocationWithinFrame(owner, frame)).thenReturn(null);
        when(geometry.getOwnerLocationOnScreen(owner)).thenReturn(new Point(250, 200));

        assertThat(PopupCoordinateCalibrator.calculateCorrection(owner, frame, geometry)).isNull();
    }

    @Test
    public void calculateCorrection_withoutOwnerScreenLocation_returnsNull() {
        final JFrame frame = mock(JFrame.class);
        final Component owner = mock(Component.class);
        final PopupCoordinateCalibrator.GeometryProvider geometry = mock(
            PopupCoordinateCalibrator.GeometryProvider.class);
        when(geometry.getNativeClientOrigin(frame)).thenReturn(new Point(240, 180));
        when(geometry.getOwnerLocationWithinFrame(owner, frame)).thenReturn(new Point(10, 20));
        when(geometry.getOwnerLocationOnScreen(owner)).thenReturn(null);

        assertThat(PopupCoordinateCalibrator.calculateCorrection(owner, frame, geometry)).isNull();
    }

    @Test
    public void calculateCorrection_maximizedDecoratedFrame_returnsZeroEvenWhenFrameOriginIsStale() {
        // Regression pin for the shipped defect: the maximized frame reports a stale
        // AWT origin (-4,-2) with 4/34 decoration insets, while the native client
        // area starts at (0,32). Anchoring to the native client origin and to a
        // root-pane-relative owner position must not emit a correction at all.
        final JFrame frame = mock(JFrame.class);
        final Component owner = mock(Component.class);
        final PopupCoordinateCalibrator.GeometryProvider geometry = mock(
            PopupCoordinateCalibrator.GeometryProvider.class);
        when(geometry.getNativeClientOrigin(frame)).thenReturn(new Point(0, 32));
        when(geometry.getOwnerLocationWithinFrame(owner, frame)).thenReturn(new Point(120, 80));
        when(geometry.getOwnerLocationOnScreen(owner)).thenReturn(new Point(120, 112));

        assertThat(PopupCoordinateCalibrator.calculateCorrection(owner, frame, geometry))
            .isEqualTo(new Point(0, 0));
    }

    @Test
    public void calculateCorrection_unmaximizedFrameWithStaleAwtOrigin_returnsZero() {
        // Normal window: owner screen position already correct, so no correction may
        // be emitted even though the frame's own AWT origin disagrees with native.
        final JFrame frame = mock(JFrame.class);
        final Component owner = mock(Component.class);
        final PopupCoordinateCalibrator.GeometryProvider geometry = mock(
            PopupCoordinateCalibrator.GeometryProvider.class);
        when(geometry.getNativeClientOrigin(frame)).thenReturn(new Point(244, 214));
        when(geometry.getOwnerLocationWithinFrame(owner, frame)).thenReturn(new Point(120, 80));
        when(geometry.getOwnerLocationOnScreen(owner)).thenReturn(new Point(364, 294));

        assertThat(PopupCoordinateCalibrator.calculateCorrection(owner, frame, geometry))
            .isEqualTo(new Point(0, 0));
    }

    @Test
    public void calculateCorrection_withMissingNativeClientOrigin_returnsNull() {
        final JFrame frame = mock(JFrame.class);
        final Component owner = mock(Component.class);
        final PopupCoordinateCalibrator.GeometryProvider geometry = mock(
            PopupCoordinateCalibrator.GeometryProvider.class);
        when(geometry.getOwnerLocationWithinFrame(owner, frame)).thenReturn(new Point(10, 20));
        when(geometry.getOwnerLocationOnScreen(owner)).thenReturn(new Point(250, 200));
        when(geometry.getNativeClientOrigin(frame)).thenReturn(null);

        assertThat(PopupCoordinateCalibrator.calculateCorrection(owner, frame, geometry)).isNull();
    }

    @Test
    public void calculateCorrection_whenProviderThrows_returnsNull() {
        final JFrame frame = mock(JFrame.class);
        final Component owner = mock(Component.class);
        final PopupCoordinateCalibrator.GeometryProvider geometry = mock(
            PopupCoordinateCalibrator.GeometryProvider.class);
        when(geometry.getNativeClientOrigin(frame)).thenThrow(new IllegalStateException("native bounds unavailable"));

        assertThat(PopupCoordinateCalibrator.calculateCorrection(owner, frame, geometry)).isNull();
    }

    @Test
    public void calculateCorrection_whenOwnerScreenLocationThrows_returnsNull() {
        final JFrame frame = mock(JFrame.class);
        final Component owner = mock(Component.class);
        final PopupCoordinateCalibrator.GeometryProvider geometry = mock(
            PopupCoordinateCalibrator.GeometryProvider.class);
        when(geometry.getNativeClientOrigin(frame)).thenReturn(new Point(240, 180));
        when(geometry.getOwnerLocationWithinFrame(owner, frame)).thenReturn(new Point(10, 20));
        when(geometry.getOwnerLocationOnScreen(owner)).thenThrow(new IllegalStateException("owner not showing"));

        assertThat(PopupCoordinateCalibrator.calculateCorrection(owner, frame, geometry)).isNull();
    }

    @Test
    public void calculateCorrection_withoutOwnerFrameOrProvider_returnsNull() {
        final JFrame frame = mock(JFrame.class);
        final Component owner = mock(Component.class);
        final PopupCoordinateCalibrator.GeometryProvider geometry = mock(
            PopupCoordinateCalibrator.GeometryProvider.class);

        assertThat(PopupCoordinateCalibrator.calculateCorrection(null, frame, geometry)).isNull();
        assertThat(PopupCoordinateCalibrator.calculateCorrection(owner, null, geometry)).isNull();
        assertThat(PopupCoordinateCalibrator.calculateCorrection(owner, frame, null)).isNull();
        verifyNoInteractions(frame, owner);
        verify(geometry, never()).getNativeClientOrigin(frame);
    }

    @Test
    public void calculateCorrection_doesNotMutateOwnerOrFrame() {
        final JFrame frame = mock(JFrame.class);
        final Component owner = mock(Component.class);
        final PopupCoordinateCalibrator.GeometryProvider geometry = mock(
            PopupCoordinateCalibrator.GeometryProvider.class);
        when(geometry.getNativeClientOrigin(frame)).thenReturn(new Point(0, 0));
        when(geometry.getOwnerLocationWithinFrame(owner, frame)).thenReturn(new Point(1035, 5));
        when(geometry.getOwnerLocationOnScreen(owner)).thenReturn(new Point(1275, 185));

        assertThat(PopupCoordinateCalibrator.calculateCorrection(owner, frame, geometry))
            .isEqualTo(new Point(-240, -180));

        verifyNoInteractions(frame, owner);
    }
}
