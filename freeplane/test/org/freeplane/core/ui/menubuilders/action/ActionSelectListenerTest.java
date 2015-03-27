package org.freeplane.core.ui.menubuilders.action;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.menubuilders.action.ActionSelectListener;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryPopupListenerCollection;
import org.freeplane.features.mode.FreeplaneActions;
import org.junit.Test;
import org.mockito.Mockito;


public class ActionSelectListenerTest {

	@Test
	public void activatesSelectOnPopup_forCheckSelectionOnPopup() {
		Entry entry = new Entry();
		final AFreeplaneAction someAction = Mockito.mock(AFreeplaneAction.class);
		when(someAction.checkSelectionOnPopup()).thenReturn(true);
		when(someAction.isEnabled()).thenReturn(true);
		entry.setAction(someAction);
		
		final EntryPopupListenerCollection entryPopupListenerCollection = new EntryPopupListenerCollection();
		final ActionSelectListener actionSelectListener = new ActionSelectListener();
		entryPopupListenerCollection.addEntryPopupListener(actionSelectListener);
		entryPopupListenerCollection.childEntriesWillBecomeVisible(entry);
		
		verify(someAction).setSelected();
	}

	@Test
	public void dontActivateSelectOnPopup_forNotCheckSelectionOnPopup() {
		Entry entry = new Entry();
		final AFreeplaneAction someAction = Mockito.mock(AFreeplaneAction.class);
		when(someAction.checkSelectionOnPopup()).thenReturn(false);
		when(someAction.isEnabled()).thenReturn(true);
		entry.setAction(someAction);
		
		final EntryPopupListenerCollection entryPopupListenerCollection = new EntryPopupListenerCollection();
		final ActionSelectListener actionSelectListener = new ActionSelectListener();
		entryPopupListenerCollection.addEntryPopupListener(actionSelectListener);
		entryPopupListenerCollection.childEntriesWillBecomeVisible(entry);
		
		verify(someAction, never()).setSelected();
	}


}
