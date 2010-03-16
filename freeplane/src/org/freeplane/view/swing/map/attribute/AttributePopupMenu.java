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

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;

import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.features.common.attribute.AttributeTableLayoutModel;
import org.freeplane.features.common.attribute.IAttributeTableModel;

/**
 * @author Dimitry Polivaev
 */
class AttributePopupMenu extends JPopupMenu implements MouseListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JMenuItem delete = null;
	private JMenuItem down = null;
	private JMenuItem insert = null;
	private boolean oldTable;
	private JMenuItem optimalWidth = null;
	private int row;
	private AttributeTable table;
	private JMenuItem up = null;

	@Override
	protected void firePopupMenuWillBecomeInvisible() {
		if (row != -1) {
			table.removeRowSelectionInterval(row, row);
		}
		oldTable = true;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				if (!oldTable) {
					return;
				}
				final KeyboardFocusManager focusManager = java.awt.KeyboardFocusManager
				    .getCurrentKeyboardFocusManager();
				final Component focusOwner = SwingUtilities.getAncestorOfClass(AttributeTable.class, focusManager
				    .getFocusOwner());
				if (table != focusOwner && focusOwner instanceof JComponent) {
					table.requestFocus(true);
					((JComponent) focusOwner).requestFocus();
				}
				table = null;
			}
		});
	}

	@Override
	protected void firePopupMenuWillBecomeVisible() {
		super.firePopupMenuWillBecomeVisible();
		if (row != -1) {
			table.addRowSelectionInterval(row, row);
		}
	}

	/**
	 * @return Returns the delete.
	 */
	private JMenuItem getDelete() {
		if (delete == null) {
			delete = new JMenuItem(ResourceBundles.getText("attributes_popup_delete"));
			delete.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					table.removeRow(row);
				}
			});
		}
		return delete;
	}

	/**
	 * @return Returns the down.
	 */
	private JMenuItem getDown() {
		if (down == null) {
			down = new JMenuItem(ResourceBundles.getText("attributes_popup_down"));
			down.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					table.moveRowDown(row);
				}
			});
		}
		return down;
	}

	/**
	 * @return Returns the insert.
	 */
	private JMenuItem getInsert() {
		if (insert == null) {
			insert = new JMenuItem(ResourceBundles.getText("attributes_popup_new"));
			insert.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					table.insertRow(row + 1);
				}
			});
		}
		return insert;
	}

	/**
	 * @return Returns the optimalWidth.
	 */
	private JMenuItem getOptimalWidth() {
		if (optimalWidth == null) {
			optimalWidth = new JMenuItem(ResourceBundles.getText("attributes_popup_optimal_width"));
			optimalWidth.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					table.setOptimalColumnWidths();
				}
			});
		}
		return optimalWidth;
	}

	public AttributeTable getTable() {
		return table;
	}

	/**
	 * @return Returns the up.
	 */
	private JMenuItem getUp() {
		if (up == null) {
			up = new JMenuItem(ResourceBundles.getText("attributes_popup_up"));
			up.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					table.moveRowUp(row);
				}
			});
		}
		return up;
	}

	/**
	 *
	 */
	private void make() {
		final String attributeViewType = table.getAttributeView().getViewType();
		final IAttributeTableModel model = table.getAttributeTableModel();
		final int rowCount = model.getRowCount();
		if (attributeViewType.equals(AttributeTableLayoutModel.SHOW_ALL)) {
			if (rowCount != 0) {
				add(getOptimalWidth());
			}
			add(getInsert());
			if (row != -1) {
				add(getDelete());
				if (row != 0) {
					add(getUp());
				}
				if (row != rowCount - 1) {
					add(getDown());
				}
			}
		}
		else {
			if (rowCount != 0) {
				add(getOptimalWidth());
			}
		}
	}

	private void maybeShowPopup(final MouseEvent e) {
		if (e.isPopupTrigger()) {
			selectTable(e.getComponent(), e.getPoint());
			if(table.isEditing()){
				return;
			}
			table.requestFocus();
			make();
			show(e.getComponent(), e.getX(), e.getY());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(final MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(final MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(final MouseEvent e) {
	}

	public void mousePressed(final MouseEvent e) {
		maybeShowPopup(e);
	}

	public void mouseReleased(final MouseEvent e) {
		maybeShowPopup(e);
	}

	private void selectTable(final Component component, final Point point) throws AssertionError {
		final int componentCount = getComponentCount();
		for (int i = componentCount; i > 0;) {
			remove(--i);
		}
		if (component instanceof AttributeTable) {
			table = (AttributeTable) component;
			if(table.isEditing()){
				return;
			}
			oldTable = false;
			row = table.rowAtPoint(point);
			if (table.getValueAt(row, 0).equals("")) {
				row--;
			}
			if(row >= 0){
				table.changeSelection(row, table.columnAtPoint(point), false, false);
			}
			return;
		}
		if (component instanceof JTableHeader) {
			final JTableHeader header = (JTableHeader) component;
			table = (AttributeTable) header.getTable();
			if(table.isEditing()){
				return;
			}
			oldTable = false;
			row = -1;
			return;
		}
		throw new AssertionError();
	}
}
