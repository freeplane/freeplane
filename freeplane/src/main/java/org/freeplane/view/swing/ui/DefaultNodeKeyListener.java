package org.freeplane.view.swing.ui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.ActionAcceleratorManager;
import org.freeplane.core.ui.ControllerPopupMenuListener;
import org.freeplane.core.ui.IEditHandler;
import org.freeplane.core.ui.IEditHandler.FirstAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

/**
 * The KeyListener which belongs to the node and cares for Events like C-D
 * (Delete Node). It forwards the requests to NodeController.
 */
public class DefaultNodeKeyListener implements KeyListener {
	final private IEditHandler editHandler;

	public DefaultNodeKeyListener(final IEditHandler editHandler) {
		this.editHandler = editHandler;
	}

	public void keyPressed(final KeyEvent e) {
		final boolean checkForScrollMap = e.isShiftDown() && e.isControlDown()&& e.isAltDown();
		final MapView mapView = (MapView) Controller.getCurrentController().getMapViewManager().getMapViewComponent();
		if (mapView == null || SwingUtilities.isDescendingFrom(mapView, e.getComponent()))
			return;
		final ActionAcceleratorManager acceleratorManager = ResourceController.getResourceController().getAcceleratorManager();
		if(acceleratorManager.canProcessKeyEvent(e))
			return;
		if(checkForScrollMap){
			switch (e.getKeyCode()) {
			case KeyEvent.VK_UP:
					mapView.scrollBy(0, -10);
					e.consume();
				return;
			case KeyEvent.VK_DOWN:
					mapView.scrollBy(0, 10);
					e.consume();
				return;
			case KeyEvent.VK_LEFT:
					mapView.scrollBy(-10, 0);
					e.consume();
				return;
			case KeyEvent.VK_RIGHT:
					mapView.scrollBy(10, 0);
					e.consume();
			}
			return;
		}
		if ((e.isAltDown() || e.isControlDown() || e.isMetaDown())) {
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
		}
		final boolean continious = e.isShiftDown();
		switch (e.getKeyCode()) {
			case KeyEvent.VK_UP:
				if (mapView.selectUp(continious))
					e.consume();
				return;
			case KeyEvent.VK_DOWN:
				if (mapView.selectDown(continious))
					e.consume();
				return;
			case KeyEvent.VK_LEFT:
				if (mapView.selectLeft(continious))
					e.consume();
				return;
			case KeyEvent.VK_RIGHT:
				if (mapView.selectRight(continious))
					e.consume();
				return;
			case KeyEvent.VK_PAGE_UP:
				if (mapView.selectPageUp(continious))
					e.consume();
				return;
			case KeyEvent.VK_PAGE_DOWN:
				if (mapView.selectPageDown(continious))
					e.consume();
				return;
			case KeyEvent.VK_HOME:
			case KeyEvent.VK_END:
			case KeyEvent.VK_BACK_SPACE:
				if (editHandler != null) {
					editHandler.edit(e, FirstAction.EDIT_CURRENT, false);
				}
				return;
			case KeyEvent.VK_CONTEXT_MENU:
				final ModeController modeController = Controller.getCurrentModeController();
				final NodeModel node = Controller.getCurrentModeController().getMapController().getSelectedNode();
				final NodeView nodeView = mapView.getNodeView(node);
				final JPopupMenu popupmenu = modeController.getUserInputListenerFactory().getNodePopupMenu();
				if (popupmenu != null) {
					popupmenu.addHierarchyListener(new ControllerPopupMenuListener());
					final MainView mainView = nodeView.getMainView();
					popupmenu.show(mainView, mainView.getX(), mainView.getY());
				}
		}
	}

	public void keyReleased(final KeyEvent e) {
	}

	public void keyTyped(final KeyEvent e) {
		if ((e.isAltDown() || e.isControlDown() || e.isMetaDown()) || AltCodeChecker.isAltCode(e.getKeyChar())) {
			return;
		}
		final String keyTypeActionString = ResourceController.getResourceController().getProperty("key_type_action",
		    FirstAction.EDIT_CURRENT.toString());
		final FirstAction keyTypeAction = FirstAction.valueOf(keyTypeActionString);
		if (!FirstAction.IGNORE.equals(keyTypeAction)) {
			if (! isActionEvent(e)) {
				if (editHandler != null) {
					editHandler.edit(e, keyTypeAction, false);
				}
				return;
			}
		}
	}

	private boolean isActionEvent(final KeyEvent e) {
	    return e.isActionKey() || isControlCharacter(e.getKeyChar());
    }

	private boolean isControlCharacter(char keyChar) {
	    return keyChar == KeyEvent.CHAR_UNDEFINED || keyChar <= KeyEvent.VK_SPACE|| keyChar == KeyEvent.VK_DELETE;
    }
}
