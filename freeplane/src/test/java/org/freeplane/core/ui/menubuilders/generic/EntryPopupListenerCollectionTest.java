package org.freeplane.core.ui.menubuilders.generic;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Test;

public class EntryPopupListenerCollectionTest {


	@Test
	public void forwardsPopupMenuWillBecomeVisibleToRegisteredEntryPopupListener() {
		final Entry entry = new Entry();
		final EntryPopupListenerCollection entryEntryPopupListenerAccessor = new EntryPopupListenerCollection();
		EntryPopupListener  popupMenuListener = mock(EntryPopupListener.class);
		entryEntryPopupListenerAccessor.addEntryPopupListener(popupMenuListener);
		
		entryEntryPopupListenerAccessor.childEntriesWillBecomeVisible(entry);
		verify(popupMenuListener).childEntriesWillBecomeVisible(entry);
	}

	@Test
	public void forwardsPopupMenuWillBecomeInvisibleToRegisteredEntryPopupListener() {
		final Entry entry = new Entry();
		final EntryPopupListenerCollection entryEntryPopupListenerAccessor = new EntryPopupListenerCollection();
		EntryPopupListener  popupMenuListener = mock(EntryPopupListener.class);
		entryEntryPopupListenerAccessor.addEntryPopupListener(popupMenuListener);
		
		entryEntryPopupListenerAccessor.childEntriesHidden(entry);
		verify(popupMenuListener).childEntriesHidden(entry);
	}


	@Test
	public void supportsMoreThanOneListener() {
		final Entry entry = new Entry();
		final EntryPopupListenerCollection entryEntryPopupListenerAccessor = new EntryPopupListenerCollection();
		EntryPopupListener  popupMenuListener1 = mock(EntryPopupListener.class);
		entryEntryPopupListenerAccessor.addEntryPopupListener(popupMenuListener1);
		EntryPopupListener  popupMenuListener2 = mock(EntryPopupListener.class);
		entryEntryPopupListenerAccessor.addEntryPopupListener(popupMenuListener2);
		
		entryEntryPopupListenerAccessor.childEntriesWillBecomeVisible(entry);
		verify(popupMenuListener1).childEntriesWillBecomeVisible(entry);
	}

	@Test
	public void doNotCallsUnregisteredEntryPopupListener() {
		final Entry entry = new Entry();
		final EntryPopupListenerCollection entryEntryPopupListenerAccessor = new EntryPopupListenerCollection();
		EntryPopupListener  popupMenuListener = mock(EntryPopupListener.class);
		entryEntryPopupListenerAccessor.addEntryPopupListener(popupMenuListener);
		entryEntryPopupListenerAccessor.removeEntryPopupListener(popupMenuListener);
		
		entryEntryPopupListenerAccessor.childEntriesWillBecomeVisible(entry);
		verify(popupMenuListener, never()).childEntriesWillBecomeVisible(entry);
	}

}
