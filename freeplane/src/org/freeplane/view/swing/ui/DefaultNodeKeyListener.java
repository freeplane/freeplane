package org.freeplane.view.swing.ui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.IEditHandler;
import org.freeplane.view.swing.map.MapView;

/**
 * The KeyListener which belongs to the node and cares for Events like C-D
 * (Delete Node). It forwards the requests to NodeController.
 */
public class DefaultNodeKeyListener implements KeyListener {
	final private Controller controller;
	private boolean disabledKeyType = true;
	final private IEditHandler editHandler;
	private boolean keyTypeAddsNew = false;

	public DefaultNodeKeyListener(final Controller controller, final IEditHandler editHandler) {
		this.controller = controller;
		this.editHandler = editHandler;
		disabledKeyType = ResourceController.getResourceController().getBooleanProperty("disable_key_type");
		keyTypeAddsNew = ResourceController.getResourceController().getBooleanProperty("key_type_adds_new");
	}

	public void keyPressed(final KeyEvent e) {
		if (e.isAltDown() || e.isControlDown() || e.isMetaDown()) {
			return;
		}
		switch (e.getKeyCode()) {
			case KeyEvent.VK_ENTER:
			case KeyEvent.VK_ESCAPE:
			case KeyEvent.VK_SHIFT:
			case KeyEvent.VK_DELETE:
			case KeyEvent.VK_SPACE:
			case KeyEvent.VK_INSERT:
			case KeyEvent.VK_TAB:
			case KeyEvent.VK_BACK_SPACE:
				return;
			case KeyEvent.VK_UP:
			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_LEFT:
			case KeyEvent.VK_RIGHT:
			case KeyEvent.VK_PAGE_UP:
			case KeyEvent.VK_PAGE_DOWN:
				((MapView) controller.getViewController().getMapView()).move(e);
				return;
			case KeyEvent.VK_HOME:
			case KeyEvent.VK_END:
				if (editHandler != null) {
					editHandler.edit(e, false, false);
				}
				return;
		}
		if (!disabledKeyType) {
			if (!e.isActionKey() && e.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
				if (editHandler != null) {
					editHandler.edit(e, keyTypeAddsNew, false);
				}
				return;
			}
		}
	}

	public void keyReleased(final KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
			((MapView) controller.getViewController().getMapView()).resetShiftSelectionOrigin();
		}
	}

	public void keyTyped(final KeyEvent e) {
	}
}
