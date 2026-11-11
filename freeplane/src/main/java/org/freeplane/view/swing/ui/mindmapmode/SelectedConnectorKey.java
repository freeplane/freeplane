package org.freeplane.view.swing.ui.mindmapmode;

import java.awt.event.KeyEvent;

enum SelectedConnectorKey {
	DESELECT, IGNORE;

	static SelectedConnectorKey of(int keyCode, int modifiers) {
		if (keyCode == KeyEvent.VK_ESCAPE && modifiers == 0)
			return DESELECT;
		return IGNORE;
	}
}
