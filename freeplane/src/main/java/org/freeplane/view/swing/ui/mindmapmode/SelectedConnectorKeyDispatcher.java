package org.freeplane.view.swing.ui.mindmapmode;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.lang.ref.WeakReference;

import javax.swing.SwingUtilities;

import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.link.ConnectorView;

class SelectedConnectorKeyDispatcher implements KeyEventDispatcher {
	private static SelectedConnectorKeyDispatcher installed;
	private final WeakReference<MapView> mapView;

	static void install(MapView mapView) {
		uninstall();
		installed = new SelectedConnectorKeyDispatcher(mapView);
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(installed);
	}

	private static void uninstall() {
		if (installed != null)
			KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(installed);
		installed = null;
	}

	private SelectedConnectorKeyDispatcher(MapView mapView) {
		this.mapView = new WeakReference<>(mapView);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		final MapView map = mapView.get();
		if (map == null || !ConnectorView.showsControlPoints(map)) {
			uninstall();
			return false;
		}
		if (e.getID() != KeyEvent.KEY_PRESSED || !isInWindowOf(map, e))
			return false;
		switch (SelectedConnectorKey.of(e.getKeyCode(), e.getModifiersEx())) {
		case DESELECT:
			deselect(map);
			e.consume();
			return true;
		default:
			return false;
		}
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
}
