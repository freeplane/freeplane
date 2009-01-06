package org.freeplane.features.ui;

import java.awt.Cursor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.InputEvent;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.features.common.clipboard.ClipboardController;
import org.freeplane.features.common.clipboard.MindMapNodesSelection;
import org.freeplane.view.swing.map.MainView;

/**
 * The NodeDragListener which belongs to every NodeView
 */
class DefaultNodeDragListener implements DragGestureListener {
	public DefaultNodeDragListener() {
	}

	public void dragGestureRecognized(final DragGestureEvent e) {
		if (!Controller.getResourceController().getBoolProperty("draganddrop")) {
			return;
		}
		final NodeModel node = ((MainView) e.getComponent()).getNodeView().getModel();
		if (node.isRoot()) {
			return;
		}
		String dragAction = "MOVE";
		Cursor cursor = getCursorByAction(e.getDragAction());
		if ((e.getTriggerEvent().getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) != 0) {
			cursor = DragSource.DefaultLinkDrop;
			dragAction = "LINK";
		}
		if ((e.getTriggerEvent().getModifiersEx() & InputEvent.BUTTON2_DOWN_MASK) != 0) {
			cursor = DragSource.DefaultCopyDrop;
			dragAction = "COPY";
		}
		final ModeController modeController = Controller.getModeController();
		final Transferable t = ClipboardController.getController(modeController).copy(
		    Controller.getController().getSelection());
		((MindMapNodesSelection) t).setDropAction(dragAction);
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
