package org.freeplane.core.ui.menubuilders.action;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.menubuilders.generic.Entry;

public interface IEntriesForAction {
	void registerEntry(AFreeplaneAction action, Entry actionEntry);

	void unregisterEntry(AFreeplaneAction action, Entry actionEntry);
}
