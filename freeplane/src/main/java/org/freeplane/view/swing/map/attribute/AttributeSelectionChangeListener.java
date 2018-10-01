package org.freeplane.view.swing.map.attribute;

import org.freeplane.features.attribute.*;
import org.freeplane.features.map.NodeModel;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

class AttributeSelectionChangeListener implements PropertyChangeListener, AttributeSelection {
	AttributeTable selectedTable;


	private void focusGained(final Component source) {
		final AttributeTable newTable;
		if (source instanceof AttributeTable) {
			newTable = (AttributeTable) source;
		}
		else {
			newTable = (AttributeTable) SwingUtilities.getAncestorOfClass(AttributeTable.class, source);
		}
		if(newTable != null) {
			selectedTable = newTable;
			selectedTable.setSelectedCellTypeInfo();
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (selectedTable != null) {
						final Component newNodeViewInFocus = SwingUtilities.getAncestorOfClass(NodeView.class,
								selectedTable);
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
	}

	private void focusLost(final Component oppositeComponent) {
		if (selectedTable == null || null == SwingUtilities.getAncestorOfClass(MapView.class, oppositeComponent)
				&& ! (oppositeComponent instanceof AttributeTable)) {
			return;
		}
		final Component newTable;
		if (oppositeComponent instanceof AttributeTable) {
			newTable = oppositeComponent;
		}
		else {
			newTable = SwingUtilities.getAncestorOfClass(AttributeTable.class, oppositeComponent);
		}
		if (selectedTable != newTable) {
			selectedTable.clearSelection();
			if (selectedTable.isEditing()) {
				selectedTable.getCellEditor().stopCellEditing();
			}
			if (!selectedTable.getAttributeView().isPopupShown()) {
				final AttributeView attributeView = selectedTable.getAttributeView();
				final String currentAttributeViewType = AttributeRegistry.getRegistry(
					attributeView.getNode().getMap()).getAttributeViewType();
				if (attributeView.getViewType() != currentAttributeViewType) {
					attributeView.stateChanged(null);
				}
			}
			selectedTable = null;
			return;
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Component newFocusOwner = (Component) evt.getNewValue();
		if(selectedTable != null) {
			focusLost(newFocusOwner);
		}
		focusGained(newFocusOwner);
	}

	@Override
	public NodeAttribute getSelectedAttribute() {
		if(selectedTable == null)
			return null;
		int selectedRow = selectedTable.getSelectedRow();
		if(selectedRow == -1)
			return null;
		AttributeTableModel attributeTableModel = selectedTable.getAttributeTableModel();
		NodeAttributeTableModel attributes = attributeTableModel.getNodeAttributeModel();
		NodeModel node = attributeTableModel.getNode();
		Attribute attribute = attributes.getAttribute(selectedRow);
		return new NodeAttribute(node, attribute);
	}
}
