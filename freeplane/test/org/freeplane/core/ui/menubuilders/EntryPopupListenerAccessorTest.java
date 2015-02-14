package org.freeplane.core.ui.menubuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Test;

public class EntryPopupListenerAccessorTest {


	@Test
	public void forwardsPopupMenuWillBecomeVisibleToRegisteredEntryPopupListener() {
		final Entry entry = new Entry();
		final EntryPopupListenerAccessor entryEntryPopupListenerAccessor = new EntryPopupListenerAccessor(entry);
		EntryPopupListener  popupMenuListener = mock(EntryPopupListener.class);
		entryEntryPopupListenerAccessor.addEntryPopupListener(popupMenuListener);
		
		entryEntryPopupListenerAccessor.popupMenuWillBecomeVisible();
		verify(popupMenuListener).popupWillBecomeVisible(entry);
	}

	@Test
	public void forwardsPopupMenuWillBecomeInvisibleToRegisteredEntryPopupListener() {
		final Entry entry = new Entry();
		final EntryPopupListenerAccessor entryEntryPopupListenerAccessor = new EntryPopupListenerAccessor(entry);
		EntryPopupListener  popupMenuListener = mock(EntryPopupListener.class);
		entryEntryPopupListenerAccessor.addEntryPopupListener(popupMenuListener);
		
		entryEntryPopupListenerAccessor.popupMenuWillBecomeInvisible();
		verify(popupMenuListener).popupWillBecomeInvisible(entry);
	}


	@Test
	public void supportsMoreThanOneListener() {
		final Entry entry = new Entry();
		final EntryPopupListenerAccessor entryEntryPopupListenerAccessor = new EntryPopupListenerAccessor(entry);
		EntryPopupListener  popupMenuListener1 = mock(EntryPopupListener.class);
		entryEntryPopupListenerAccessor.addEntryPopupListener(popupMenuListener1);
		EntryPopupListener  popupMenuListener2 = mock(EntryPopupListener.class);
		entryEntryPopupListenerAccessor.addEntryPopupListener(popupMenuListener2);
		
		entryEntryPopupListenerAccessor.popupMenuWillBecomeVisible();
		verify(popupMenuListener1).popupWillBecomeVisible(entry);
	}


	@Test
	public void sharesEntryPopupListenersForEntry() {
		final Entry entry = new Entry();
		final EntryPopupListenerAccessor entryEntryPopupListenerAccessor = new EntryPopupListenerAccessor(entry);
		EntryPopupListener  popupMenuListener = mock(EntryPopupListener.class);
		entryEntryPopupListenerAccessor.addEntryPopupListener(popupMenuListener);
		
		final EntryPopupListenerAccessor entryEntryPopupListener2 = new EntryPopupListenerAccessor(entry);
		entryEntryPopupListener2.popupMenuWillBecomeVisible();
		verify(popupMenuListener).popupWillBecomeVisible(entry);
	}

	@Test
	public void doNotCallsUnregisteredEntryPopupListener() {
		final Entry entry = new Entry();
		final EntryPopupListenerAccessor entryEntryPopupListenerAccessor = new EntryPopupListenerAccessor(entry);
		EntryPopupListener  popupMenuListener = mock(EntryPopupListener.class);
		entryEntryPopupListenerAccessor.addEntryPopupListener(popupMenuListener);
		entryEntryPopupListenerAccessor.removeEntryPopupListener(popupMenuListener);
		
		entryEntryPopupListenerAccessor.popupMenuWillBecomeVisible();
		verify(popupMenuListener, never()).popupWillBecomeVisible(entry);
	}

}
