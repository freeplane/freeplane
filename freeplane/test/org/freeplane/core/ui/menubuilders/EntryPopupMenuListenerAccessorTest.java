package org.freeplane.core.ui.menubuilders;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.junit.Test;
import org.mockito.ArgumentMatcher;

public class EntryPopupMenuListenerAccessorTest {


	@Test
	public void forwardsPopupMenuWillBecomeVisibleToRegisteredPopupMenuListener() {
		final Entry entry = new Entry();
		final EntryPopupMenuListenerAccessor entryPopupMenuListener = new EntryPopupMenuListenerAccessor(entry);
		PopupMenuListener  popupMenuListener = mock(PopupMenuListener.class);
		entryPopupMenuListener.addPopupMenuListener(popupMenuListener);
		
		entryPopupMenuListener.popupMenuWillBecomeVisible();
		verify(popupMenuListener).popupMenuWillBecomeVisible(argThat(new ArgumentMatcher<PopupMenuEvent>() {

			@Override
			public boolean matches(Object argument) {
				return ((PopupMenuEvent)argument).getSource().equals(entry);
			}
		}));
	}

	@Test
	public void forwardsPopupMenuWillBecomeInvisibleToRegisteredPopupMenuListener() {
		final Entry entry = new Entry();
		final EntryPopupMenuListenerAccessor entryPopupMenuListener = new EntryPopupMenuListenerAccessor(entry);
		PopupMenuListener  popupMenuListener = mock(PopupMenuListener.class);
		entryPopupMenuListener.addPopupMenuListener(popupMenuListener);
		
		entryPopupMenuListener.popupMenuWillBecomeInvisible();
		verify(popupMenuListener).popupMenuWillBecomeInvisible(argThat(new ArgumentMatcher<PopupMenuEvent>() {

			@Override
			public boolean matches(Object argument) {
				return ((PopupMenuEvent)argument).getSource().equals(entry);
			}
		}));
	}



	@Test
	public void forwardsPopupMenuCanceledToRegisteredPopupMenuListener() {
		final Entry entry = new Entry();
		final EntryPopupMenuListenerAccessor entryPopupMenuListener = new EntryPopupMenuListenerAccessor(entry);
		PopupMenuListener  popupMenuListener = mock(PopupMenuListener.class);
		entryPopupMenuListener.addPopupMenuListener(popupMenuListener);
		
		entryPopupMenuListener.popupMenuCanceled();
		verify(popupMenuListener).popupMenuCanceled(argThat(new ArgumentMatcher<PopupMenuEvent>() {

			@Override
			public boolean matches(Object argument) {
				return ((PopupMenuEvent)argument).getSource().equals(entry);
			}
		}));
	}


	@Test
	public void supportsMoreThanOneListener() {
		final Entry entry = new Entry();
		final EntryPopupMenuListenerAccessor entryPopupMenuListener = new EntryPopupMenuListenerAccessor(entry);
		PopupMenuListener  popupMenuListener1 = mock(PopupMenuListener.class);
		entryPopupMenuListener.addPopupMenuListener(popupMenuListener1);
		PopupMenuListener  popupMenuListener2 = mock(PopupMenuListener.class);
		entryPopupMenuListener.addPopupMenuListener(popupMenuListener2);
		
		entryPopupMenuListener.popupMenuWillBecomeVisible();
		verify(popupMenuListener1).popupMenuWillBecomeVisible(argThat(new ArgumentMatcher<PopupMenuEvent>() {

			@Override
			public boolean matches(Object argument) {
				return ((PopupMenuEvent)argument).getSource().equals(entry);
			}
		}));
	}


	@Test
	public void sharesPopupMenuListenersForEntry() {
		final Entry entry = new Entry();
		final EntryPopupMenuListenerAccessor entryPopupMenuListener = new EntryPopupMenuListenerAccessor(entry);
		PopupMenuListener  popupMenuListener = mock(PopupMenuListener.class);
		entryPopupMenuListener.addPopupMenuListener(popupMenuListener);
		
		final EntryPopupMenuListenerAccessor entryPopupMenuListener2 = new EntryPopupMenuListenerAccessor(entry);
		entryPopupMenuListener2.popupMenuWillBecomeVisible();
		verify(popupMenuListener).popupMenuWillBecomeVisible(argThat(new ArgumentMatcher<PopupMenuEvent>() {

			@Override
			public boolean matches(Object argument) {
				return ((PopupMenuEvent)argument).getSource().equals(entry);
			}
		}));
	}

	@Test
	public void doNotCallsUnregisteredPopupMenuListener() {
		final Entry entry = new Entry();
		final EntryPopupMenuListenerAccessor entryPopupMenuListener = new EntryPopupMenuListenerAccessor(entry);
		PopupMenuListener  popupMenuListener = mock(PopupMenuListener.class);
		entryPopupMenuListener.addPopupMenuListener(popupMenuListener);
		entryPopupMenuListener.removePopupMenuListener(popupMenuListener);
		
		entryPopupMenuListener.popupMenuWillBecomeVisible();
		verify(popupMenuListener, never()).popupMenuWillBecomeVisible(argThat(new ArgumentMatcher<PopupMenuEvent>() {

			@Override
			public boolean matches(Object argument) {
				return ((PopupMenuEvent)argument).getSource().equals(entry);
			}
		}));
	}

}
