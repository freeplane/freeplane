package org.freeplane.core.ui.components;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import javax.swing.JFrame;
import javax.swing.Popup;
import javax.swing.PopupFactory;

import org.junit.Test;

public class FrameResynchronizerPopupFactoryTest {
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
    public void ensurePopupFactory_outsideX11ReturnsTheActiveFactoryUnchanged() {
        final PopupFactory delegate = mock(PopupFactory.class);
        final Set<JFrame> frames = Collections.newSetFromMap(new WeakHashMap<JFrame, Boolean>());
        frames.add(mock(JFrame.class));

        assertThat(FrameResynchronizer.ensurePopupFactory(false, delegate, frames,
            mock(PopupCoordinateCalibrator.GeometryProvider.class))).isSameAs(delegate);
    }

    @Test
    public void ensurePopupFactory_withEmptyRegistryReturnsTheActiveFactoryUnchanged() {
        final PopupFactory delegate = mock(PopupFactory.class);
        final Set<JFrame> frames = Collections.newSetFromMap(new WeakHashMap<JFrame, Boolean>());

        assertThat(FrameResynchronizer.ensurePopupFactory(true, delegate, frames,
            mock(PopupCoordinateCalibrator.GeometryProvider.class))).isSameAs(delegate);
    }

    @Test
    public void ensurePopupFactory_doesNotWrapAnExistingAdapterAgain() {
        final Set<JFrame> frames = Collections.newSetFromMap(new WeakHashMap<JFrame, Boolean>());
        frames.add(mock(JFrame.class));
        final X11PopupFactory adapter = new X11PopupFactory(mock(PopupFactory.class),
            owner -> mock(JFrame.class), mock(PopupCoordinateCalibrator.GeometryProvider.class));

        assertThat(FrameResynchronizer.ensurePopupFactory(true, adapter, frames,
            mock(PopupCoordinateCalibrator.GeometryProvider.class))).isSameAs(adapter);
    }

    @Test
    public void ensurePopupFactory_afterDelegateReplacementWrapsTheReplacement() {
        final Set<JFrame> frames = Collections.newSetFromMap(new WeakHashMap<JFrame, Boolean>());
        frames.add(mock(JFrame.class));
        final PopupFactory firstDelegate = mock(PopupFactory.class);
        final PopupFactory firstAdapter = FrameResynchronizer.ensurePopupFactory(true, firstDelegate, frames,
            mock(PopupCoordinateCalibrator.GeometryProvider.class));
        final PopupFactory replacement = mock(PopupFactory.class);

        final PopupFactory replacementAdapter = FrameResynchronizer.ensurePopupFactory(true, replacement, frames,
            mock(PopupCoordinateCalibrator.GeometryProvider.class));

        assertThat(replacementAdapter).isInstanceOf(X11PopupFactory.class);
        assertThat(replacementAdapter).isNotSameAs(firstAdapter);
        assertThat(((X11PopupFactory) replacementAdapter).getDelegate()).isSameAs(replacement);
    }

    @Test
    public void ensurePopupFactory_usesThePassedRegistryForRegisteredOwnerCalibration() {
        final Set<JFrame> frames = Collections.newSetFromMap(new WeakHashMap<JFrame, Boolean>());
        final JFrame registeredFrame = mock(JFrame.class);
        frames.add(registeredFrame);
        final PopupFactory delegate = mock(PopupFactory.class);
        final Popup popup = mock(Popup.class);
        final PopupCoordinateCalibrator.GeometryProvider geometryProvider =
            mock(PopupCoordinateCalibrator.GeometryProvider.class);
        when(geometryProvider.getNativeFrameBounds(registeredFrame)).thenReturn(new Rectangle(100, 200, 400, 300));
        when(geometryProvider.getOwnerLocationWithinFrame(registeredFrame, registeredFrame))
            .thenReturn(new Point(10, 20));
        when(geometryProvider.getOwnerLocationOnScreen(registeredFrame)).thenReturn(new Point(90, 190));
        when(delegate.getPopup(registeredFrame, registeredFrame, 40, 60)).thenReturn(popup);

        final PopupFactory result = FrameResynchronizer.ensurePopupFactory(true, delegate, frames, geometryProvider);

        assertThat(result.getPopup(registeredFrame, registeredFrame, 20, 30)).isSameAs(popup);
        verify(delegate).getPopup(registeredFrame, registeredFrame, 40, 60);
    }

    @Test
    public void ensurePopupFactory_withUnanchoredOwnerDelegatesOriginalCoordinates() {
        final Set<JFrame> frames = Collections.newSetFromMap(new WeakHashMap<JFrame, Boolean>());
        frames.add(mock(JFrame.class));
        final PopupFactory delegate = mock(PopupFactory.class);
        final Popup popup = mock(Popup.class);
        final Component owner = mock(Component.class);
        final Component contents = mock(Component.class);
        when(delegate.getPopup(owner, contents, 40, 50)).thenReturn(popup);

        final PopupFactory result = FrameResynchronizer.ensurePopupFactory(true, delegate, frames,
            mock(PopupCoordinateCalibrator.GeometryProvider.class));

        assertThat(result.getPopup(owner, contents, 40, 50)).isSameAs(popup);
        verify(delegate).getPopup(owner, contents, 40, 50);
    }
}
