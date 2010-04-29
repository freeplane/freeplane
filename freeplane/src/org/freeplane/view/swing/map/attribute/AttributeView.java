/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.view.swing.map.attribute;

import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.event.AncestorEvent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

import org.freeplane.core.model.NodeModel;
import org.freeplane.features.common.attribute.AttributeRegistry;
import org.freeplane.features.common.attribute.AttributeTableLayoutModel;
import org.freeplane.features.common.attribute.NodeAttributeTableModel;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

/**
 * This class represents a single Node of a MindMap (in analogy to
 * TreeCellRenderer).
 */
public class AttributeView implements ChangeListener, TableModelListener {
	private static final Color HEADER_BACKGROUND = UIManager.getColor("TableHeader.background");
	static private AttributePopupMenu tablePopupMenu;
	private AttributeTable attributeTable;
	private JScrollPane attributeViewScrollPane;
	private AttributeTableModelDecoratorAdapter currentAttributeTableModel;
	private ExtendedAttributeTableModelDecorator extendedAttributeTableModel = null;
	final private NodeView nodeView;
	final private ReducedAttributeTableModelDecorator reducedAttributeTableModel;
	private JTableHeader tableHeader;

	public AttributeView(final NodeView nodeView) {
		super();
		this.nodeView = nodeView;
		reducedAttributeTableModel = new ReducedAttributeTableModelDecorator(this);
		currentAttributeTableModel = reducedAttributeTableModel;
		setViewType(getAttributeRegistry().getAttributeViewType());
		addListeners();
	}

	private void addListeners() {
		getAttributeRegistry().addChangeListener(this);
		addTableModelListeners();
	}

	private void addTableModelListeners() {
		if (!getMapView().getModeController().canEdit()) {
			return;
		}
		if (attributeTable != null) {
			if (AttributeView.tablePopupMenu == null) {
				AttributeView.tablePopupMenu = new AttributePopupMenu();
			}
			getAttributes().getLayout().addColumnWidthChangeListener(attributeTable);
			attributeTable.addMouseListener(AttributeView.tablePopupMenu);
			tableHeader.addMouseListener(AttributeView.tablePopupMenu);
		}
		else {
			getAttributes().addTableModelListener(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * @seejavax.swing.event.AncestorListener#ancestorMoved(javax.swing.event.
	 * AncestorEvent)
	 */
	public void ancestorMoved(final AncestorEvent event) {
	}

	/**
	 */
	public boolean areAttributesVisible() {
		final String viewType = getViewType();
		return viewType != AttributeTableLayoutModel.HIDE_ALL
		        && (currentAttributeTableModel.areAttributesVisible() || viewType != getAttributeRegistry()
		            .getAttributeViewType());
	}

	AttributeRegistry getAttributeRegistry() {
		return AttributeRegistry.getRegistry(getNode().getMap());
	}

	public NodeAttributeTableModel getAttributes() {
		return NodeAttributeTableModel.getModel(getNode());
	}

	public TableModel getCurrentAttributeTableModel() {
		return currentAttributeTableModel;
	}

	/**
	 * @return Returns the extendedAttributeTableModel.
	 */
	private ExtendedAttributeTableModelDecorator getExtendedAttributeTableModel() {
		if (extendedAttributeTableModel == null) {
			extendedAttributeTableModel = new ExtendedAttributeTableModelDecorator(this);
		}
		return extendedAttributeTableModel;
	}

	/**
	 */
	public MapView getMapView() {
		return getNodeView().getMap();
	}

	/**
	 */
	NodeModel getNode() {
		return getNodeView().getModel();
	}

	/**
	 */
	public NodeView getNodeView() {
		return nodeView;
	}

	public String getViewType() {
		return currentAttributeTableModel == reducedAttributeTableModel ? getAttributeRegistry().getAttributeViewType()
		        : AttributeTableLayoutModel.SHOW_ALL;
	}

	boolean isPopupShown() {
		return attributeTable != null && AttributeView.tablePopupMenu != null
		        && (AttributeView.tablePopupMenu.getTable() == attributeTable);
	}

	private void provideAttributeTable() {
		if (attributeTable == null) {
			attributeTable = new AttributeTable(this);
			tableHeader = attributeTable.getTableHeader();
			tableHeader.setBackground(AttributeView.HEADER_BACKGROUND);
			addTableModelListeners();
			attributeViewScrollPane = new AttributeViewScrollPane(attributeTable);
			getNodeView().getContentPane().add(attributeViewScrollPane);
			getAttributes().removeTableModelListener(this);
			setViewType(getAttributeRegistry().getAttributeViewType());
		}
	}

	private void removeListeners() {
		getAttributeRegistry().removeChangeListener(this);
		if (!getMapView().getModeController().canEdit()) {
			return;
		}
		if (attributeTable != null) {
			getAttributes().getLayout().removeColumnWidthChangeListener(attributeTable);
			attributeTable.getParent().remove(attributeTable);
			attributeTable.getModel().removeTableModelListener(attributeTable);
			attributeTable.removeMouseListener(AttributeView.tablePopupMenu);
			tableHeader.removeMouseListener(AttributeView.tablePopupMenu);
			AttributeView.tablePopupMenu = null;
		}
		else {
			getAttributes().removeTableModelListener(this);
		}
	}

	private void setViewType(final String viewType) {
		JTableHeader currentColumnHeaderView = null;
		if (viewType == AttributeTableLayoutModel.SHOW_ALL) {
			currentAttributeTableModel = getExtendedAttributeTableModel();
			currentColumnHeaderView = tableHeader;
		}
		else {
			currentAttributeTableModel = reducedAttributeTableModel;
		}
		if (attributeTable != null) {
			attributeTable.setModel(currentAttributeTableModel);
			attributeTable.setTableHeader(currentColumnHeaderView);
			attributeViewScrollPane.setColumnHeaderView(currentColumnHeaderView);
			attributeViewScrollPane.invalidate();
		}
	}

	public void startEditing() {
		provideAttributeTable();
		if (currentAttributeTableModel == reducedAttributeTableModel) {
			getExtendedAttributeTableModel();
			setViewType(AttributeTableLayoutModel.SHOW_ALL);
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				startEditingTable();
			}
		});
	}

	private void startEditingTable() {
		attributeTable.requestFocus();
		if (currentAttributeTableModel.getRowCount() == 0) {
			attributeTable.insertRow(0);
		}
		else {
			attributeTable.changeSelection(0, 0, false, false);
		}
	}

	public void stateChanged(final ChangeEvent event) {
		setViewType(getAttributeRegistry().getAttributeViewType());
		reducedAttributeTableModel.stateChanged(null);
		getNodeView().revalidate();
	}

	public void stopEditing() {
		if (attributeTable.isEditing()) {
			attributeTable.getCellEditor().stopCellEditing();
		}
		final String registryAttributeViewType = getAttributeRegistry().getAttributeViewType();
		if (registryAttributeViewType != getViewType()) {
			setViewType(registryAttributeViewType);
		}
		getNodeView().requestFocus();
	}

	public void syncronizeAttributeView() {
		if (attributeTable == null && currentAttributeTableModel.areAttributesVisible()) {
			provideAttributeTable();
		}
	}

	public void tableChanged(final TableModelEvent e) {
		final NodeModel node = getNode();
		getMapView().getModeController().getMapController().nodeChanged(node);
	}

	/**
	 */
	public void update() {
		if (attributeTable != null && attributeTable.isVisible()) {
			attributeTable.updateAttributeTable();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.event.AncestorListener#ancestorRemoved(javax.swing.event.
	 * AncestorEvent)
	 */
	public void viewRemoved() {
		removeListeners();
		if (reducedAttributeTableModel != null) {
			reducedAttributeTableModel.viewRemoved();
		}
		if (extendedAttributeTableModel != null) {
			extendedAttributeTableModel.viewRemoved();
		}
		if (attributeTable != null) {
			attributeTable.viewRemoved();
		}
	}
}
