package org.freeplane.view.swing.ui;

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
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.InputEvent;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.common.clipboard.ClipboardController;
import org.freeplane.features.common.clipboard.MindMapNodesSelection;
import org.freeplane.view.swing.map.MainView;

/**
 * The NodeDragListener which belongs to every NodeView
 */
class DefaultNodeDragListener implements DragGestureListener {
	final private Controller controller;

	public DefaultNodeDragListener(final Controller controller) {
		this.controller = controller;
	}

	public void dragGestureRecognized(final DragGestureEvent e) {
		if (!ResourceController.getResourceController().getBooleanProperty("draganddrop")) {
			return;
		}
		final int dragActionType = e.getDragAction();
		if (dragActionType == DnDConstants.ACTION_MOVE) {
			final NodeModel node = ((MainView) e.getComponent()).getNodeView().getModel();
			if (node.isRoot()) {
				return;
			}
		}
		final String dragActionName;
		Cursor cursor = getCursorByAction(dragActionType);
		if ((e.getTriggerEvent().getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) != 0) {
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
		final ModeController modeController = controller.getModeController();
		final Transferable t = ClipboardController.getController(modeController).copy(controller.getSelection());
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
