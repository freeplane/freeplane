package org.freeplane.view.swing.ui.mindmapmode;

import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.InputEvent;

import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.clipboard.MapClipboardController;
import org.freeplane.features.map.clipboard.MindMapNodesSelection;
import org.freeplane.features.mode.Controller;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

/**
 * The NodeDragListener which belongs to every NodeView
 */
public class MNodeDragListener implements DragGestureListener {
	public void dragGestureRecognized(final DragGestureEvent e) {
		final MainView mainView = (MainView) e.getComponent();
		final NodeView nodeView = mainView.getNodeView();
		final MapView mapView = nodeView.getMap();
		mapView.select();
		if(! nodeView.isSelected()){
			nodeView.getMap().getModeController().getController().getSelection().selectAsTheOnlyOneSelected(nodeView.getModel());
		}
		Rectangle bounds = new Rectangle(0, 0, mainView.getWidth(), mainView.getHeight());
		if(!bounds.contains(e.getDragOrigin()))
			return;
		final int dragActionType = e.getDragAction();
		if (dragActionType == DnDConstants.ACTION_MOVE) {
			final NodeModel node = nodeView.getModel();
			if (node.isRoot()) {
				if(! isLinkDragEvent(e))
					return;
			}
		}
		final String dragActionName;
		Cursor cursor = getCursorByAction(dragActionType);
		if (isLinkDragEvent(e)) {
			cursor = DragSource.DefaultLinkDrop;
			dragActionName = "LINK";
		}
		else if ((e.getTriggerEvent().getModifiersEx() & InputEvent.BUTTON2_DOWN_MASK) != 0) {
			cursor = DragSource.DefaultCopyDrop;
			dragActionName = "COPY";
		}
		else {
			dragActionName = "MOVE";
		}
		final Transferable t = MapClipboardController.getController().copy(Controller.getCurrentController().getSelection());
		((MindMapNodesSelection) t).setDropAction(dragActionName);
		try {
			e.startDrag(cursor, t, new DragSourceListener() {
				public void dragDropEnd(final DragSourceDropEvent dsde) {
				}

				public void dragEnter(final DragSourceDragEvent e) {
				}

				public void dragExit(final DragSourceEvent dse) {
				}

				public void dragOver(final DragSourceDragEvent dsde) {
				}

				public void dropActionChanged(final DragSourceDragEvent dsde) {
					dsde.getDragSourceContext().setCursor(getCursorByAction(dsde.getUserAction()));
				}
			});
		}
		catch (final InvalidDnDOperationException ex) {
		}
	}

	private boolean isLinkDragEvent(final DragGestureEvent e) {
	    return (e.getTriggerEvent().getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) != 0;
    }

	public Cursor getCursorByAction(final int dragAction) {
		switch (dragAction) {
			case DnDConstants.ACTION_COPY:
				return DragSource.DefaultCopyDrop;
			case DnDConstants.ACTION_LINK:
				return DragSource.DefaultLinkDrop;
			default:
				return DragSource.DefaultMoveDrop;
		}
	}
}
