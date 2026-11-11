package org.freeplane.view.swing.ui.mindmapmode;

import java.awt.event.KeyEvent;

enum SelectedConnectorKey {
	DESELECT, REMOVE, DESELECT_AND_PASS, IGNORE;

	static SelectedConnectorKey of(int keyCode, int modifiers) {
		switch (keyCode) {
		case KeyEvent.VK_SHIFT:
		case KeyEvent.VK_CONTROL:
		case KeyEvent.VK_ALT:
		case KeyEvent.VK_ALT_GRAPH:
		case KeyEvent.VK_META:
		case KeyEvent.VK_UNDEFINED:
			return IGNORE;
		case KeyEvent.VK_ESCAPE:
			return modifiers == 0 ? DESELECT : DESELECT_AND_PASS;
		case KeyEvent.VK_DELETE:
			return modifiers == 0 ? REMOVE : DESELECT_AND_PASS;
		default:
			return DESELECT_AND_PASS;
		}
	}
}
