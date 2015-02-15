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
		
		entryEntryPopupListenerAccessor.entryWillBecomeVisible();
		verify(popupMenuListener).entryWillBecomeVisible(entry);
	}

	@Test
	public void forwardsPopupMenuWillBecomeInvisibleToRegisteredEntryPopupListener() {
		final Entry entry = new Entry();
		final EntryPopupListenerAccessor entryEntryPopupListenerAccessor = new EntryPopupListenerAccessor(entry);
		EntryPopupListener  popupMenuListener = mock(EntryPopupListener.class);
		entryEntryPopupListenerAccessor.addEntryPopupListener(popupMenuListener);
		
		entryEntryPopupListenerAccessor.entryWillBecomeInvisible();
		verify(popupMenuListener).entryWillBecomeInvisible(entry);
	}


	@Test
	public void supportsMoreThanOneListener() {
		final Entry entry = new Entry();
		final EntryPopupListenerAccessor entryEntryPopupListenerAccessor = new EntryPopupListenerAccessor(entry);
		EntryPopupListener  popupMenuListener1 = mock(EntryPopupListener.class);
		entryEntryPopupListenerAccessor.addEntryPopupListener(popupMenuListener1);
		EntryPopupListener  popupMenuListener2 = mock(EntryPopupListener.class);
		entryEntryPopupListenerAccessor.addEntryPopupListener(popupMenuListener2);
		
		entryEntryPopupListenerAccessor.entryWillBecomeVisible();
		verify(popupMenuListener1).entryWillBecomeVisible(entry);
	}


	@Test
	public void sharesEntryPopupListenersForEntry() {
		final Entry entry = new Entry();
		final EntryPopupListenerAccessor entryEntryPopupListenerAccessor = new EntryPopupListenerAccessor(entry);
		EntryPopupListener  popupMenuListener = mock(EntryPopupListener.class);
		entryEntryPopupListenerAccessor.addEntryPopupListener(popupMenuListener);
		
		final EntryPopupListenerAccessor entryEntryPopupListener2 = new EntryPopupListenerAccessor(entry);
		entryEntryPopupListener2.entryWillBecomeVisible();
		verify(popupMenuListener).entryWillBecomeVisible(entry);
	}

	@Test
	public void doNotCallsUnregisteredEntryPopupListener() {
		final Entry entry = new Entry();
		final EntryPopupListenerAccessor entryEntryPopupListenerAccessor = new EntryPopupListenerAccessor(entry);
		EntryPopupListener  popupMenuListener = mock(EntryPopupListener.class);
		entryEntryPopupListenerAccessor.addEntryPopupListener(popupMenuListener);
		entryEntryPopupListenerAccessor.removeEntryPopupListener(popupMenuListener);
		
		entryEntryPopupListenerAccessor.entryWillBecomeVisible();
		verify(popupMenuListener, never()).entryWillBecomeVisible(entry);
	}

}
