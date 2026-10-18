package org.freeplane.core.ui.components;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.Popup;
import javax.swing.PopupFactory;

import org.junit.Test;

public class X11PopupFactoryTest {
    @Test
    public void getPopup_appliesOwnerLocalCorrectionToRequestedCoordinates() {
        final PopupFixture fixture = new PopupFixture();
        fixture.geometryReturns(new Rectangle(0, 0, 2240, 1400), new Point(1035, 5), new Point(1275, 185));

        final Popup popup = fixture.factory.getPopup(fixture.owner, fixture.contents, 1000, 400);

        assertThat(popup).isSameAs(fixture.delegatePopup);
        verify(fixture.delegate, times(1)).getPopup(fixture.owner, fixture.contents, 760, 220);
    }

    @Test
    public void getPopup_forwardsTheOriginalOwnerAndContentsToTheDelegate() {
        final PopupFixture fixture = new PopupFixture();
        fixture.geometryReturns(new Rectangle(0, 0, 2240, 1400), new Point(1035, 5), new Point(1275, 185));

        fixture.factory.getPopup(fixture.owner, fixture.contents, 1000, 400);

        verify(fixture.delegate, times(1)).getPopup(
            same(fixture.owner), same(fixture.contents), anyInt(), anyInt());
    }

    @Test
    public void getPopup_withPositiveCorrection_shiftsRequestedCoordinatesForward() {
        final PopupFixture fixture = new PopupFixture();
        fixture.geometryReturns(new Rectangle(240, 180, 960, 600), new Point(10, 20), new Point(200, 150));

        fixture.factory.getPopup(fixture.owner, fixture.contents, 1000, 400);

        verify(fixture.delegate, times(1)).getPopup(fixture.owner, fixture.contents, 1050, 450);
    }

    @Test
    public void getPopup_withZeroCorrection_passesRequestedCoordinatesUnchanged() {
        final PopupFixture fixture = new PopupFixture();
        fixture.geometryReturns(new Rectangle(240, 180, 960, 600), new Point(10, 20), new Point(250, 200));

        final Popup popup = fixture.factory.getPopup(fixture.owner, fixture.contents, 300, 240);

        assertThat(popup).isSameAs(fixture.delegatePopup);
        verify(fixture.delegate, times(1)).getPopup(fixture.owner, fixture.contents, 300, 240);
    }

    @Test
    public void getPopup_withoutRegisteredFrame_passesRequestedCoordinatesAndSkipsGeometry() {
        final PopupFixture fixture = new PopupFixture();
        fixture.resolver = unregisteredResolver();

        final Popup popup = fixture.factory.getPopup(fixture.owner, fixture.contents, 1000, 400);

        assertThat(popup).isSameAs(fixture.delegatePopup);
        verify(fixture.delegate, times(1)).getPopup(fixture.owner, fixture.contents, 1000, 400);
        verifyNoInteractions(fixture.geometry);
    }

    @Test
    public void getPopup_withoutNativeBounds_passesRequestedCoordinatesAndDelegates() {
        final PopupFixture fixture = new PopupFixture();
        fixture.geometryReturns(null, new Point(1035, 5), new Point(1275, 185));

        final Popup popup = fixture.factory.getPopup(fixture.owner, fixture.contents, 1000, 400);

        assertThat(popup).isSameAs(fixture.delegatePopup);
        verify(fixture.delegate, times(1)).getPopup(fixture.owner, fixture.contents, 1000, 400);
    }

    @Test
    public void getPopup_withoutOwnerScreenLocation_passesRequestedCoordinatesAndDelegates() {
        final PopupFixture fixture = new PopupFixture();
        fixture.geometryReturns(new Rectangle(0, 0, 2240, 1400), new Point(1035, 5), null);

        final Popup popup = fixture.factory.getPopup(fixture.owner, fixture.contents, 1000, 400);

        assertThat(popup).isSameAs(fixture.delegatePopup);
        verify(fixture.delegate, times(1)).getPopup(fixture.owner, fixture.contents, 1000, 400);
    }

    @Test
    public void getPopup_withNonPositiveNativeWidth_delegatesOriginalCoordinates() {
        final PopupFixture fixture = new PopupFixture();
        fixture.geometryReturns(new Rectangle(240, 180, 0, 600), new Point(10, 20), new Point(1000, 400));

        final Popup popup = fixture.factory.getPopup(fixture.owner, fixture.contents, 1000, 400);

        assertThat(popup).isSameAs(fixture.delegatePopup);
        verify(fixture.delegate, times(1)).getPopup(fixture.owner, fixture.contents, 1000, 400);
        verifyNoMoreInteractions(fixture.delegate);
    }

    @Test
    public void getPopup_withMissingOwnerLocationWithinFrame_delegatesOriginalCoordinates() {
        final PopupFixture fixture = new PopupFixture();
        fixture.geometryReturns(new Rectangle(0, 0, 2240, 1400), null, new Point(1035, 5));

        final Popup popup = fixture.factory.getPopup(fixture.owner, fixture.contents, 1000, 400);

        assertThat(popup).isSameAs(fixture.delegatePopup);
        verify(fixture.delegate, times(1)).getPopup(fixture.owner, fixture.contents, 1000, 400);
        verifyNoMoreInteractions(fixture.delegate);
    }

    @Test
    public void getPopup_whenGeometryProviderThrows_passesRequestedCoordinatesAndDelegates() {
        final PopupFixture fixture = new PopupFixture();
        when(fixture.geometry.getNativeFrameBounds(fixture.frame))
            .thenThrow(new IllegalStateException("native bounds unavailable"));

        final Popup popup = fixture.factory.getPopup(fixture.owner, fixture.contents, 1000, 400);

        assertThat(popup).isSameAs(fixture.delegatePopup);
        verify(fixture.delegate, times(1)).getPopup(fixture.owner, fixture.contents, 1000, 400);
    }

    @Test
    public void getPopup_whenOwnerLookupThrows_passesRequestedCoordinatesAndDelegates() {
        final PopupFixture fixture = new PopupFixture();
        fixture.resolver = new X11PopupFactory.PopupOwnerResolver() {
            @Override
            public JFrame findFrame(final Component owner) {
                throw new IllegalStateException("owner hierarchy unavailable");
            }
        };

        final Popup popup = fixture.factory.getPopup(fixture.owner, fixture.contents, 1000, 400);

        assertThat(popup).isSameAs(fixture.delegatePopup);
        verify(fixture.delegate, times(1)).getPopup(fixture.owner, fixture.contents, 1000, 400);
        verifyNoInteractions(fixture.geometry);
    }

    @Test
    public void getPopup_callsTheDelegateExactlyOnce() {
        final PopupFixture fixture = new PopupFixture();
        fixture.geometryReturns(new Rectangle(0, 0, 2240, 1400), new Point(1035, 5), new Point(1275, 185));

        fixture.factory.getPopup(fixture.owner, fixture.contents, 1000, 400);

        verify(fixture.delegate, times(1)).getPopup(any(Component.class), any(Component.class), anyInt(), anyInt());
        verifyNoMoreInteractions(fixture.delegate);
    }

    @Test
    public void getPopup_doesNotMutateTheOwnerOrFrame() {
        final PopupFixture fixture = new PopupFixture();
        fixture.geometryReturns(new Rectangle(0, 0, 2240, 1400), new Point(1035, 5), new Point(1275, 185));

        fixture.factory.getPopup(fixture.owner, fixture.contents, 1000, 400);

        verifyNoInteractions(fixture.frame);
        verify(fixture.owner, never()).setBounds(any(Rectangle.class));
        verify(fixture.owner, never()).setLocation(anyInt(), anyInt());
        verify(fixture.owner, never()).validate();
        verify(fixture.owner, never()).revalidate();
        verify(fixture.owner, never()).invalidate();
    }

    @Test
    public void getPopup_whenDelegateThrows_propagatesTheSameExceptionInstance() {
        final PopupFixture fixture = new PopupFixture();
        fixture.geometryReturns(new Rectangle(0, 0, 2240, 1400), new Point(1035, 5), new Point(1275, 185));
        final IllegalStateException delegateFailure = new IllegalStateException("delegate failed");
        when(fixture.delegate.getPopup(fixture.owner, fixture.contents, 760, 220)).thenThrow(delegateFailure);

        final Throwable thrown = catchThrowable(new org.assertj.core.api.ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() {
                fixture.factory.getPopup(fixture.owner, fixture.contents, 1000, 400);
            }
        });

        assertThat(thrown).isSameAs(delegateFailure);
        verify(fixture.delegate, times(1)).getPopup(fixture.owner, fixture.contents, 760, 220);
    }

    @Test
    public void getPopup_whenDelegateRejectsUncorrectedCoordinates_propagatesTheIllegalArgumentException() {
        final PopupFixture fixture = new PopupFixture();
        fixture.resolver = unregisteredResolver();
        final IllegalArgumentException delegateFailure = new IllegalArgumentException("popup not allowed");
        when(fixture.delegate.getPopup(fixture.owner, fixture.contents, 1000, 400)).thenThrow(delegateFailure);

        final Throwable thrown = catchThrowable(new org.assertj.core.api.ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() {
                fixture.factory.getPopup(fixture.owner, fixture.contents, 1000, 400);
            }
        });

        assertThat(thrown).isSameAs(delegateFailure);
    }

    @Test
    public void getDelegate_returnsTheCapturedActiveFactory() {
        final PopupFixture fixture = new PopupFixture();

        assertThat(fixture.factory.getDelegate()).isSameAs(fixture.delegate);
    }

    @Test
    public void getDelegate_doesNotExposeTheAdapterItself() {
        final PopupFixture fixture = new PopupFixture();

        assertThat(fixture.factory.getDelegate()).isNotSameAs(fixture.factory);
        assertThat(fixture.factory.getDelegate()).isSameAs(fixture.delegate);
    }

    private static X11PopupFactory.PopupOwnerResolver registeredResolver(final JFrame frame) {
        return new X11PopupFactory.PopupOwnerResolver() {
            @Override
            public JFrame findFrame(final Component owner) {
                return frame;
            }
        };
    }

    private static X11PopupFactory.PopupOwnerResolver unregisteredResolver() {
        return new X11PopupFactory.PopupOwnerResolver() {
            @Override
            public JFrame findFrame(final Component owner) {
                return null;
            }
        };
    }

    private static final class PopupFixture {
        private final PopupFactory delegate = mock(PopupFactory.class);
        private final PopupCoordinateCalibrator.GeometryProvider geometry = mock(
            PopupCoordinateCalibrator.GeometryProvider.class);
        private final JFrame frame = mock(JFrame.class);
        private final Component owner = mock(Component.class);
        private final Component contents = mock(Component.class);
        private final Popup delegatePopup = mock(Popup.class);
        private final X11PopupFactory factory;
        private X11PopupFactory.PopupOwnerResolver resolver;

        private PopupFixture() {
            resolver = registeredResolver(frame);
            when(delegate.getPopup(any(Component.class), any(Component.class), anyInt(), anyInt()))
                .thenReturn(delegatePopup);
            factory = new X11PopupFactory(delegate, new X11PopupFactory.PopupOwnerResolver() {
                @Override
                public JFrame findFrame(final Component candidate) {
                    return resolver.findFrame(candidate);
                }
            }, geometry);
        }

        private void geometryReturns(final Rectangle nativeFrameBounds, final Point ownerLocationWithinFrame,
                final Point ownerLocationOnScreen) {
            when(geometry.getNativeFrameBounds(frame)).thenReturn(nativeFrameBounds);
            when(geometry.getOwnerLocationWithinFrame(owner, frame)).thenReturn(ownerLocationWithinFrame);
            when(geometry.getOwnerLocationOnScreen(owner)).thenReturn(ownerLocationOnScreen);
        }
    }
}
