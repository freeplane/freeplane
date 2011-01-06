package org.freeplane.view.swing.map.attribute;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.SwingUtilities;

import org.freeplane.features.common.attribute.AttributeRegistry;
import org.freeplane.view.swing.map.NodeView;

class AttributeTableFocusListener implements FocusListener {
	private AttributeTable focusedTable;
	static AttributeTableFocusListener focusListener = new AttributeTableFocusListener();

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
	 */
	public void focusGained(final FocusEvent event) {
		final Component source = (Component) event.getSource();
		event.getOppositeComponent();
		if (source instanceof AttributeTable) {
			focusedTable = (AttributeTable) source;
		}
		else {
			focusedTable = (AttributeTable) SwingUtilities.getAncestorOfClass(AttributeTable.class, source);
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				if (focusedTable != null) {
					final Component newNodeViewInFocus = SwingUtilities.getAncestorOfClass(NodeView.class,
					    focusedTable);
					if (newNodeViewInFocus != null) {
						final NodeView viewer = (NodeView) newNodeViewInFocus;
						if (viewer != viewer.getMap().getSelected()) {
							viewer.getMap().selectAsTheOnlyOneSelected(viewer, false);
						}
					}
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
	 */
	public void focusLost(final FocusEvent event) {
		if (event.isTemporary()) {
			return;
		}
		final Component oppositeComponent = event.getOppositeComponent();
		final Component newTable;
		if (oppositeComponent instanceof AttributeTable) {
			newTable = oppositeComponent;
		}
		else {
			newTable = SwingUtilities.getAncestorOfClass(AttributeTable.class, oppositeComponent);
		}
		if (focusedTable == null) {
			return;
		}
		if (focusedTable != newTable) {
			if (focusedTable.isEditing()) {
				focusedTable.getCellEditor().stopCellEditing();
			}
			if (!focusedTable.isPopupShown()) {
				NodeView nodeView = ((NodeView)SwingUtilities.getAncestorOfClass(NodeView.class, focusedTable));
				if(nodeView != null){
					final AttributeView attributeView = nodeView.getAttributeView();
					final String currentAttributeViewType = AttributeRegistry.getRegistry(
							attributeView.getNode().getMap()).getAttributeViewType();
					if (focusedTable.getViewType() != currentAttributeViewType) {
						attributeView.stateChanged(null);
					}
				}
			}
			focusedTable = null;
			return;
		}
	}
}