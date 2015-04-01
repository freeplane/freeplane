package org.freeplane.core.ui.menubuilders.menu;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IAcceleratorChangeListener;
import org.freeplane.core.ui.menubuilders.action.EntriesForAction;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;

public class MenuAcceleratorChangeListener implements IAcceleratorChangeListener {
	final private EntriesForAction entries;

	public MenuAcceleratorChangeListener(EntriesForAction entries) {
		this.entries = entries;
	}

	@Override
	public void acceleratorChanged(AFreeplaneAction action, KeyStroke oldStroke, KeyStroke newStroke) {
		for (Entry entry : entries.entries(action)) {
			final Object component = new EntryAccessor().getComponent(entry);
			if (component instanceof JMenuItem)
				((JMenuItem) component).setAccelerator(newStroke);
		}
	}
}
