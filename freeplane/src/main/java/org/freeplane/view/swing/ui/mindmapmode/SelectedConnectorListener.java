package org.freeplane.view.swing.ui.mindmapmode;

import java.awt.AWTEvent;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.ref.WeakReference;

import javax.swing.SwingUtilities;

import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.mode.Controller;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.link.ConnectorView;

class SelectedConnectorListener implements KeyEventDispatcher, AWTEventListener {
	private static SelectedConnectorListener installed;
	private final WeakReference<MapView> mapView;

	static void install(MapView mapView) {
		uninstall();
		installed = new SelectedConnectorListener(mapView);
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(installed);
		Toolkit.getDefaultToolkit().addAWTEventListener(installed, AWTEvent.MOUSE_EVENT_MASK);
	}

	private static void uninstall() {
		if (installed != null) {
			KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(installed);
			Toolkit.getDefaultToolkit().removeAWTEventListener(installed);
		}
		installed = null;
	}

	private SelectedConnectorListener(MapView mapView) {
		this.mapView = new WeakReference<>(mapView);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		final MapView map = selectingMap();
		if (map == null || e.getID() != KeyEvent.KEY_PRESSED || !isInWindowOf(map, e))
			return false;
		switch (SelectedConnectorKey.of(e.getKeyCode(), e.getModifiersEx())) {
		case DESELECT:
			deselect(map);
			e.consume();
			return true;
		case REMOVE:
			final ConnectorModel connector = ConnectorView.selectedConnector(map);
			deselect(map);
			linkController().removeArrowLink(connector);
			e.consume();
			return true;
		case DESELECT_AND_PASS:
			deselect(map);
			return false;
		default:
			return false;
		}
	}

	@Override
	public void eventDispatched(AWTEvent event) {
		if (event.getID() != MouseEvent.MOUSE_PRESSED)
			return;
		final MapView map = selectingMap();
		if (map != null && event.getSource() != map)
			deselect(map);
	}

	private MapView selectingMap() {
		final MapView map = mapView.get();
		if (map == null || ConnectorView.selectedConnector(map) == null) {
			uninstall();
			return null;
		}
		return map;
	}

	private boolean isInWindowOf(MapView map, KeyEvent e) {
		final Window window = SwingUtilities.getWindowAncestor(map);
		return window != null && window == SwingUtilities.getWindowAncestor(e.getComponent());
	}

	private void deselect(MapView map) {
		ConnectorView.disableControlPoints(map);
		map.repaintVisible();
		uninstall();
	}

	private MLinkController linkController() {
		return (MLinkController) LinkController.getController(Controller.getCurrentController().getModeController());
	}
}
