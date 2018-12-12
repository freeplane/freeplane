package org.freeplane.view.swing.map.attribute;

import java.awt.Component;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import org.freeplane.features.attribute.Attribute;
import org.freeplane.features.attribute.AttributeRegistry;
import org.freeplane.features.attribute.AttributeSelection;
import org.freeplane.features.attribute.NodeAttribute;
import org.freeplane.features.map.NodeModel;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

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
	public List<SelectedAttribute> getSelectedAttributes() {
		if(selectedTable == null)
			return AttributeSelection.EMPTY.getSelectedAttributes();
		final ListSelectionModel selectionModel = selectedTable.getSelectionModel();
		int minSelectedRow = selectionModel.getMinSelectionIndex();
		if(minSelectedRow == -1)
			return AttributeSelection.EMPTY.getSelectedAttributes();
		AttributeTableModel attributeTableModel = selectedTable.getAttributeTableModel();
		NodeModel node = attributeTableModel.getNode();
		int maxSelectedRow = selectionModel.getMaxSelectionIndex();
		final ArrayList<SelectedAttribute> selectedAttributes = new ArrayList<>(maxSelectedRow - minSelectedRow + 1);
		for(int rowIndex = minSelectedRow; rowIndex <= maxSelectedRow; rowIndex++) {
			if(selectionModel.isSelectedIndex(rowIndex)) {
				final SelectedAttribute.SelectedPart selectedPart;
				selectedPart = selectedTable.isCellSelected(rowIndex, 0) ?
						selectedTable.isCellSelected(rowIndex, 1) ?
								SelectedAttribute.SelectedPart.BOTH : SelectedAttribute.SelectedPart.NAME : SelectedAttribute.SelectedPart.VALUE;
				Attribute attribute = attributeTableModel.getAttribute(rowIndex);
				if(attribute  != null) {
					final NodeAttribute nodeAttrribute = new NodeAttribute(node, attribute);
					selectedAttributes.add(new SelectedAttribute(nodeAttrribute, selectedPart)) ;
				}
			}
		}
		return selectedAttributes;
	}
}
