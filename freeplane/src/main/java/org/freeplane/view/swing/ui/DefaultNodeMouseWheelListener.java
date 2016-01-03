package org.freeplane.view.swing.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.SwingUtilities;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.view.swing.map.MapView;

public class DefaultNodeMouseWheelListener  implements MouseWheelListener{
	private final MouseWheelListener mapMouseWheelListener;
	public DefaultNodeMouseWheelListener(MouseWheelListener mapMouseWheelListener) {
		super();
		this.mapMouseWheelListener = mapMouseWheelListener;
	}
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		final Component source = e.getComponent();
		final Point location = new Point(e.getX(), e.getY());
		final Container map = SwingUtilities.getAncestorOfClass(MapView.class, source);
		UITools.convertPointToAncestor(source, location, map);
		final MouseWheelEvent mapWheelEvent = new MouseWheelEvent(map, e.getID(), e.getWhen(), e.getModifiers() | e.getModifiersEx(), 
				location.x, location.y, e.getXOnScreen(), e.getYOnScreen(), 
				e.getClickCount(), e.isPopupTrigger(), e.getScrollType(),
				e.getScrollAmount(), e.getWheelRotation());
		mapMouseWheelListener.mouseWheelMoved(mapWheelEvent);
	}

}
