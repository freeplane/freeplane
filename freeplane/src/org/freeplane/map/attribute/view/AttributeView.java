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
package org.freeplane.map.attribute.view;

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

import org.freeplane.map.attribute.AttributeRegistry;
import org.freeplane.map.attribute.AttributeTableLayoutModel;
import org.freeplane.map.attribute.IAttributeTableModel;
import org.freeplane.map.attribute.NodeAttributeTableModel;
import org.freeplane.map.attribute.mindmapnode.MAttributeController;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.map.tree.view.MapView;
import org.freeplane.map.tree.view.NodeView;
import org.freeplane.modes.ModeController;

/**
 * This class represents a single Node of a MindMap (in analogy to
 * TreeCellRenderer).
 */
public class AttributeView implements ChangeListener, TableModelListener {
	private static final Color HEADER_BACKGROUND = UIManager
	    .getColor("TableHeader.background");
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
		reducedAttributeTableModel = new ReducedAttributeTableModelDecorator(
		    this);
		currentAttributeTableModel = reducedAttributeTableModel;
		setViewType(getAttributeRegistry().getAttributeViewType());
		addListeners();
	}

	private void addListeners() {
		if (getNodeView().getModel().getMap().isReadOnly()) {
			return;
		}
		getAttributeRegistry().addChangeListener(this);
		addTableModelListeners();
	}

	private void addTableModelListeners() {
		if (getNodeView().getModel().getMap().isReadOnly()) {
			return;
		}
		final ModeController modeController = getModeController();
		if (attributeTable != null) {
			if (AttributeView.tablePopupMenu == null) {
				AttributeView.tablePopupMenu = ((MAttributeController) modeController
				    .getAttributeController()).getAttributeTablePopupMenu();
			}
			getAttributes().getLayout().addColumnWidthChangeListener(
			    attributeTable);
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
		return getNode().getMap().getRegistry().getAttributes();
	}

	public NodeAttributeTableModel getAttributes() {
		return getNode().getAttributes();
	}

	public IAttributeTableModel getCurrentAttributeTableModel() {
		return currentAttributeTableModel;
	}

	/**
	 * @return Returns the extendedAttributeTableModel.
	 */
	private ExtendedAttributeTableModelDecorator getExtendedAttributeTableModel() {
		if (extendedAttributeTableModel == null) {
			extendedAttributeTableModel = new ExtendedAttributeTableModelDecorator(
			    this);
		}
		return extendedAttributeTableModel;
	}

	/**
	 */
	public MapView getMapView() {
		return getNodeView().getMap();
	}

	private ModeController getModeController() {
		return nodeView.getModel().getMap().getModeController();
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
		return currentAttributeTableModel == reducedAttributeTableModel ? getAttributeRegistry()
		    .getAttributeViewType()
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
			attributeViewScrollPane = new AttributeViewScrollPane(
			    attributeTable);
			getNodeView().getContentPane().add(attributeViewScrollPane);
			getAttributes().removeTableModelListener(this);
			setViewType(getAttributeRegistry().getAttributeViewType());
		}
	}

	private void removeListeners() {
		if (getNodeView().getModel().getMap().isReadOnly()) {
			return;
		}
		getAttributeRegistry().removeChangeListener(this);
		if (attributeTable != null) {
			getAttributes().getLayout().removeColumnWidthChangeListener(
			    attributeTable);
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
			attributeViewScrollPane
			    .setColumnHeaderView(currentColumnHeaderView);
			attributeViewScrollPane.invalidate();
		}
	}

	public void startEditing() {
		if (getNode().getMap().isReadOnly()) {
			return;
		}
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
		final String registryAttributeViewType = getAttributeRegistry()
		    .getAttributeViewType();
		if (registryAttributeViewType != getViewType()) {
			setViewType(registryAttributeViewType);
		}
		getNodeView().requestFocus();
	}

	public void syncronizeAttributeView() {
		if (attributeTable == null
		        && currentAttributeTableModel.areAttributesVisible()) {
			provideAttributeTable();
		}
	}

	public void tableChanged(final TableModelEvent e) {
		final MapView map = getNodeView().getMap();
		map.getModel().nodeChanged(getNode());
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
