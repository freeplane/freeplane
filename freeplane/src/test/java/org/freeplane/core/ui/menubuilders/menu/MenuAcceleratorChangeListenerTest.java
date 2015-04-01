package org.freeplane.core.ui.menubuilders.menu;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.mock;

import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.menubuilders.action.EntriesForAction;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.junit.Assert;
import org.junit.Test;

public class MenuAcceleratorChangeListenerTest {
	@Test
	public void setsKeystroke() throws Exception {
		final EntriesForAction entriesForAction = new EntriesForAction();
		final MenuAcceleratorChangeListener menuAcceleratorChangeListener = new MenuAcceleratorChangeListener(entriesForAction);
		final AFreeplaneAction action = mock(AFreeplaneAction.class);
		Entry actionEntry = new Entry();
		final JMenuItem menu = new JMenuItem();
		new EntryAccessor().setComponent(actionEntry, menu);
		entriesForAction.registerEntry(action, actionEntry);

		final KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0);
		menuAcceleratorChangeListener.acceleratorChanged(action, null, keyStroke);
		Assert.assertThat(menu.getAccelerator(), equalTo(keyStroke));
	}
}
