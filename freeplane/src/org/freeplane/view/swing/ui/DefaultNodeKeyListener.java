package org.freeplane.view.swing.ui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.KeyStroke;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.IEditHandler;
import org.freeplane.view.swing.map.MapView;

/**
 * The KeyListener which belongs to the node and cares for Events like C-D
 * (Delete Node). It forwards the requests to NodeController.
 */
public class DefaultNodeKeyListener implements KeyListener {
	private boolean disabledKeyType = true;
	final private IEditHandler editHandler;
	final private KeyStroke keyStrokeDown;
	final private KeyStroke keyStrokeLeft;
	final private KeyStroke keyStrokeRight;
	final private KeyStroke keyStrokeUp;
	private boolean keyTypeAddsNew = false;
	final private String up, down, left, right;

	public DefaultNodeKeyListener(final IEditHandler editHandler) {
		this.editHandler = editHandler;
		up = Controller.getResourceController().getAdjustableProperty("keystroke_move_up");
		down = Controller.getResourceController().getAdjustableProperty("keystroke_move_down");
		left = Controller.getResourceController().getAdjustableProperty("keystroke_move_left");
		right = Controller.getResourceController().getAdjustableProperty("keystroke_move_right");
		disabledKeyType = Controller.getResourceController().getBoolProperty("disable_key_type");
		keyTypeAddsNew = Controller.getResourceController().getBoolProperty("key_type_adds_new");
		keyStrokeUp = KeyStroke.getKeyStroke(up);
		keyStrokeDown = KeyStroke.getKeyStroke(down);
		keyStrokeLeft = KeyStroke.getKeyStroke(left);
		keyStrokeRight = KeyStroke.getKeyStroke(right);
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
				return;
			case KeyEvent.VK_UP:
			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_LEFT:
			case KeyEvent.VK_RIGHT:
			case KeyEvent.VK_PAGE_UP:
			case KeyEvent.VK_PAGE_DOWN:
				((MapView) Controller.getController().getViewController().getMapView()).move(e);
				return;
			case KeyEvent.VK_HOME:
			case KeyEvent.VK_END:
			case KeyEvent.VK_BACK_SPACE:
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
		boolean doMove = false;
		if (keyStrokeUp != null && e.getKeyCode() == keyStrokeUp.getKeyCode()) {
			e.setKeyCode(KeyEvent.VK_UP);
			doMove = true;
		}
		else if (keyStrokeDown != null && e.getKeyCode() == keyStrokeDown.getKeyCode()) {
			e.setKeyCode(KeyEvent.VK_DOWN);
			doMove = true;
		}
		else if (keyStrokeLeft != null && e.getKeyCode() == keyStrokeLeft.getKeyCode()) {
			e.setKeyCode(KeyEvent.VK_LEFT);
			doMove = true;
		}
		else if (keyStrokeRight != null && e.getKeyCode() == keyStrokeRight.getKeyCode()) {
			e.setKeyCode(KeyEvent.VK_RIGHT);
			doMove = true;
		}
		if (doMove) {
			((MapView) Controller.getController().getViewController().getMapView()).move(e);
			e.consume();
			return;
		}
	}

	public void keyReleased(final KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
			((MapView) Controller.getController().getViewController().getMapView())
			    .resetShiftSelectionOrigin();
		}
	}

	public void keyTyped(final KeyEvent e) {
	}
}
