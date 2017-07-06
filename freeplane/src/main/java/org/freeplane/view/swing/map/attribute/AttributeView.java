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

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.event.AncestorEvent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

import org.freeplane.features.attribute.AttributeRegistry;
import org.freeplane.features.attribute.AttributeTableLayoutModel;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.map.NodeModel;
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
	private ListSelectionListener tableSelectionListener;

	public AttributeView(final NodeView nodeView, final boolean addToNodeView) {
		super();
		this.nodeView = nodeView;
		if(addToNodeView){
			reducedAttributeTableModel = new ReducedAttributeTableModelDecorator(this);
			currentAttributeTableModel = reducedAttributeTableModel;
		}
		else{
			reducedAttributeTableModel = null;
			currentAttributeTableModel = extendedAttributeTableModel = new ExtendedAttributeTableModelDecorator(this);
			
		}
		setViewType(getAttributeRegistry().getAttributeViewType());
		addListeners();
	}

	private void addListeners() {
		getAttributeRegistry().addChangeListener(this);
		addTableListeners();
	}

	private void addTableListeners() {
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
			if (tableSelectionListener != null) {
				attributeTable.getSelectionModel().addListSelectionListener(tableSelectionListener);
				attributeTable.getColumnModel().getSelectionModel().addListSelectionListener(tableSelectionListener);
			}
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
	
	static private int VIEWER_POSITION = 3; 

	private void provideAttributeTable() {
		if (attributeTable == null) {
			getAttributes().removeTableModelListener(this);
			attributeTable = new AttributeTable(this);
			tableHeader = attributeTable.getTableHeader();
			tableHeader.setBackground(AttributeView.HEADER_BACKGROUND);
			addTableListeners();
			attributeViewScrollPane = new AttributeViewScrollPane(attributeTable);
			attributeViewScrollPane.setAlignmentX(AttributeViewScrollPane.LEFT_ALIGNMENT);
			if(isReduced()){
				getNodeView().addContent(attributeViewScrollPane, VIEWER_POSITION);
			}
			setViewType(getAttributeRegistry().getAttributeViewType());
		}
	}

	boolean isReduced() {
	    return reducedAttributeTableModel != null;
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
		if (viewType == AttributeTableLayoutModel.SHOW_ALL || ! isReduced()) {
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
		attributeTable.requestFocusInWindow();
		if (currentAttributeTableModel.getRowCount() == 0) {
			attributeTable.insertRow(0);
		}
		else {
			attributeTable.changeSelection(0, 0, false, false);
		}
	}

	public void stateChanged(final ChangeEvent event) {
		setViewType(getAttributeRegistry().getAttributeViewType());
		if(isReduced()){
			reducedAttributeTableModel.stateChanged(null);
		}
		if(attributeTable != null)
			attributeTable.revalidate();
	}

	public void stopEditing() {
		if (attributeTable.isEditing()) {
			attributeTable.getCellEditor().stopCellEditing();
		}
		final String registryAttributeViewType = getAttributeRegistry().getAttributeViewType();
		if (registryAttributeViewType != getViewType()) {
			setViewType(registryAttributeViewType);
		}
		getNodeView().requestFocusInWindow();
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
		if (isReduced()) {
			reducedAttributeTableModel.viewRemoved(nodeView);
		}
		if (extendedAttributeTableModel != null) {
			extendedAttributeTableModel.viewRemoved(nodeView);
		}
		if (attributeTable != null) {
			attributeTable.viewRemoved(nodeView);
			attributeTable = null;
		}
	}

	JComponent getContainer() {
		if(attributeViewScrollPane == null){
			provideAttributeTable();
		}
	    return attributeViewScrollPane;
    }

	public void addRow() {
		attributeTable.insertRow(attributeTable.getRowCount());
    }
	
	public void setOptimalColumnWidths() {
		syncronizeAttributeView();
		if(attributeTable != null)
			attributeTable.setOptimalColumnWidths();
	}

	public AttributeTable getAttributeTable() {
	    return attributeTable;
    }

	public void addTableSelectionListener(ListSelectionListener listSelectionListener) {
		// we have to cache the listener to enable lazy construction of the AttributeTable
		tableSelectionListener = listSelectionListener;
    }
}
