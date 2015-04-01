package org.freeplane.core.ui.menubuilders.action;

import static org.mockito.Mockito.mock;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.junit.Test;
import org.mockito.Mockito;

public class AcceleratorDestroyerTest {
	@Test
	public void unregistersEntryWithAction() {
		Entry actionEntry = new Entry();
		final AFreeplaneAction action = mock(AFreeplaneAction.class);
		new EntryAccessor().setAction(actionEntry, action);
		IEntriesForAction entries = mock(IEntriesForAction.class);
		final AcceleratorDestroyer acceleratorDestroyer = new AcceleratorDestroyer(entries);
		acceleratorDestroyer.visit(actionEntry);
		Mockito.verify(entries).unregisterEntry(action, actionEntry);
	}
}
