package org.freeplane.core.ui.menubuilders.action;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.UserRole;
import org.junit.Test;
import org.mockito.Mockito;


public class ActionStatusUpdaterTest {

	@Test
	public void activatesSelectOnPopup_forCheckSelectionOnPopup() {
		Entry menuEntry = new Entry();
		Entry actionEntry = new Entry();
		menuEntry.addChild(actionEntry);
		final AFreeplaneAction someAction = Mockito.mock(AFreeplaneAction.class);
		when(someAction.checkSelectionOnPopup()).thenReturn(true);
		when(someAction.isEnabled()).thenReturn(true);
		new EntryAccessor().setAction(actionEntry, someAction);

		final ActionStatusUpdater actionSelectListener = new ActionStatusUpdater();
		actionSelectListener.childEntriesWillBecomeVisible(menuEntry, UserRole.EDITOR);
		verify(someAction).setSelected();
	}

	@Test
	public void dontActivateSelectOnPopup_forNotCheckSelectionOnPopup() {
		Entry menuEntry = new Entry();
		Entry actionEntry = new Entry();
		menuEntry.addChild(actionEntry);
		final AFreeplaneAction someAction = Mockito.mock(AFreeplaneAction.class);
		when(someAction.checkSelectionOnPopup()).thenReturn(false);
		when(someAction.isEnabled()).thenReturn(true);
		new EntryAccessor().setAction(actionEntry, someAction);

		final ActionStatusUpdater actionSelectListener = new ActionStatusUpdater();
		actionSelectListener.childEntriesWillBecomeVisible(menuEntry, UserRole.EDITOR);

		verify(someAction, never()).setSelected();
	}


}
