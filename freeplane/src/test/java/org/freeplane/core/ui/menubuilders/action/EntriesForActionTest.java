package org.freeplane.core.ui.menubuilders.action;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Collection;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.junit.Test;

public class EntriesForActionTest {
	@Test
	public void returnsEmptyListIfNoActionWasRegistered() throws Exception {
		final EntriesForAction entriesForAction = new EntriesForAction();
		AFreeplaneAction action = mock(AFreeplaneAction.class);
		Collection<Entry> entries = entriesForAction.entries(action);
		assertThat(entries.isEmpty(), equalTo(true));
	}

	@Test
	public void returnsListWithRegisteredAction() throws Exception {
		final EntriesForAction entriesForAction = new EntriesForAction();
		AFreeplaneAction action = mock(AFreeplaneAction.class);
		final Entry actionEntry = new Entry();
		entriesForAction.registerEntry(action, actionEntry);
		Collection<Entry> entries = entriesForAction.entries(action);
		assertThat(entries, equalTo((Collection<Entry>) asList(actionEntry)));
	}

	@Test
	public void returnsListWithTwoRegisteredActions() throws Exception {
		final EntriesForAction entriesForAction = new EntriesForAction();
		AFreeplaneAction action = mock(AFreeplaneAction.class);
		final Entry actionEntry1 = new Entry();
		final Entry actionEntry2 = new Entry();
		entriesForAction.registerEntry(action, actionEntry1);
		entriesForAction.registerEntry(action, actionEntry2);
		Collection<Entry> entries = entriesForAction.entries(action);
		assertThat(entries, equalTo((Collection<Entry>) asList(actionEntry1, actionEntry2)));
	}

	@Test
	public void returnsEmptyListIfNoActionWasUnregistered() throws Exception {
		final EntriesForAction entriesForAction = new EntriesForAction();
		AFreeplaneAction action = mock(AFreeplaneAction.class);
		final Entry actionEntry = new Entry();
		entriesForAction.registerEntry(action, actionEntry);
		entriesForAction.unregisterEntry(action, actionEntry);
		Collection<Entry> entries = entriesForAction.entries(action);
		assertThat(entries.isEmpty(), equalTo(true));
	}
}
