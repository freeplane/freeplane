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
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.attribute.AttributeTableLayoutModel;
import org.freeplane.features.attribute.IAttributeTableModel;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.UrlManager;
import org.freeplane.features.url.mindmapmode.MFileManager;
import org.freeplane.view.swing.ui.mindmapmode.INodeSelector;
import org.freeplane.view.swing.ui.mindmapmode.NodeSelector;

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
	private JMenuItem insertFileLink = null;
	private boolean oldTable;
	private JMenuItem optimalWidth = null;
	private int row;
	private AttributeTable table;
	private JMenuItem up = null;
	private int col;
	private JMenuItem insertLink;
	private JMenuItem insertNodeLink;
	private JMenuItem insertAnchoredLink;

	@Override
	protected void firePopupMenuWillBecomeInvisible() {
		if (row != -1) {
			table.removeRowSelectionInterval(row, row);
		}
		oldTable = true;
		EventQueue.invokeLater(new Runnable() {
			@Override
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
					((JComponent) focusOwner).requestFocusInWindow();
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
			delete = new JMenuItem(TextUtils.getText("attributes_popup_delete"));
			delete.addActionListener(new ActionListener() {
				@Override
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
			down = new JMenuItem(TextUtils.getText("attributes_popup_down"));
			down.addActionListener(new ActionListener() {
				@Override
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
			insert = new JMenuItem(TextUtils.getText("attributes_popup_new"));
			insert.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					table.insertRow(row + 1);
				}
			});
		}
		return insert;
	}
	/**
	 * @return Returns the insert.
	 */
	private JMenuItem getInsertFileLink() {
		if (insertFileLink == null) {
			insertFileLink = new JMenuItem(TextUtils.getText("SetLinkByFileChooserAction.text"));
			insertFileLink.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final AttributeTable table = AttributePopupMenu.this.table;
					final URI relative = ((MFileManager) UrlManager.getController())
				    .getLinkByFileChooser(Controller.getCurrentController().getMap());
					if (relative != null) {
						table.setValueAt(relative, row, col);
					}
				}
			});
		}
		return insertFileLink;
	}
	/**
	 * @return Returns the insert.
	 */
	private JMenuItem getInsertLink() {
		if (insertLink == null) {
			insertLink = new JMenuItem(TextUtils.getText("SetLinkByTextFieldAction.text"));
			insertLink.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final AttributeTable table = AttributePopupMenu.this.table;
					final Object oldValue = table.getValueAt(row, col);
					final String inputValue = JOptionPane.showInputDialog(table, TextUtils.getText("edit_link_manually"), oldValue.toString());
					if (inputValue != null && (oldValue instanceof String || ! oldValue.equals(inputValue))) {
						if (inputValue.toString().equals("")) {
							table.setValueAt("", row, col);
						}
						try {
							final URI link = LinkController.createURI(inputValue.trim());
							if(! oldValue.equals(link))
								table.setValueAt(link, row, col);
						}
						catch (final URISyntaxException e1) {
							LogUtils.warn(e1);
							UITools.errorMessage(TextUtils.format("invalid_uri", inputValue));
							return;
						}
					}
				}

			});
		}
		return insertLink;
	}

	private JMenuItem getInsertNodeLink() {
		if (insertNodeLink == null) {
			insertNodeLink = new JMenuItem(TextUtils.getText("SetNodeLink.text"));
			insertNodeLink.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final AttributeTable table = AttributePopupMenu.this.table;
					final Object oldValue = table.getValueAt(row, col);
					final NodeSelector nodeSelector = new NodeSelector();
					nodeSelector.show(table, new INodeSelector() {
						@Override
						public void nodeSelected(NodeModel node) {
							if(node == null)
								return;
							final String inputValue = "#" + node.getID();
							try {
								final URI link = LinkController.createURI(inputValue);
								if(! oldValue.equals(link))
									table.setValueAt(link, row, col);
							}
							catch (final URISyntaxException e1) {
								LogUtils.severe(e1);
								return;
							}
						}
					});
				}

			});
		}
		return insertNodeLink;
	}

	private JMenuItem getInsertAnchoredLink() {
		if (insertAnchoredLink == null) {
			insertAnchoredLink = new JMenuItem(TextUtils.getText("MakeLinkToAnchorAction.text"));
			insertAnchoredLink.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final AttributeTable table = AttributePopupMenu.this.table;
					final Object oldValue = table.getValueAt(row, col);
					final LinkController linkController = LinkController.getController();
					if(linkController instanceof MLinkController) {
	                    final MLinkController mLinkController = (MLinkController)linkController;
						if (mLinkController.isAnchored()) {
                            try {
                            	final String anchorIDforNode = mLinkController.getAnchorIDforNode(((AttributeTableModel) table.getModel()).getNode());
                            	if(anchorIDforNode != null){
                            		URI link = LinkController.createURI(anchorIDforNode);
                            		if(! oldValue.equals(link))
                            			table.setValueAt(link, row, col);
                            	}
                            }
                            catch (URISyntaxException e1) {
                            }
	                    }
                    }
				}

			});
		}
		return insertAnchoredLink;
	}
	/**
	 * @return Returns the optimalWidth.
	 */
	private JMenuItem getOptimalWidth() {
		if (optimalWidth == null) {
			optimalWidth = new JMenuItem(TextUtils.getText("attributes_popup_optimal_width"));
			optimalWidth.addActionListener(new ActionListener() {
				@Override
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
			up = new JMenuItem(TextUtils.getText("attributes_popup_up"));
			up.addActionListener(new ActionListener() {
				@Override
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
		add(getOptimalWidth());
		if(col == 1){
			add(getInsertLink());
			add(getInsertFileLink());
			add(getInsertNodeLink());
			final LinkController linkController = LinkController.getController();
			if(linkController instanceof MLinkController && ((MLinkController)linkController).isAnchored())
				add(getInsertAnchoredLink());
		}
		if (attributeViewType.equals(AttributeTableLayoutModel.SHOW_ALL)) {
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
	}

	private void maybeShowPopup(final MouseEvent e) {
		if (e.isPopupTrigger()) {
			selectTable(e.getComponent(), e.getPoint());
			if (table.isEditing()) {
				return;
			}
			table.requestFocusInWindow();
			make();
			show(e.getComponent(), e.getX(), e.getY());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(final MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(final MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(final MouseEvent e) {
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		maybeShowPopup(e);
	}

	@Override
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
			if (table.isEditing()) {
				return;
			}
			oldTable = false;
			row = table.rowAtPoint(point);
			col = table.columnAtPoint(point);
			if (row >= 0) {
				if (table.getValueAt(row, 0).equals("")) {
					row--;
				}
			}
			if (row >= 0) {
				table.changeSelection(row, table.columnAtPoint(point), false, false);
			}
			return;
		}
		if (component instanceof JTableHeader) {
			final JTableHeader header = (JTableHeader) component;
			table = (AttributeTable) header.getTable();
			if (table.isEditing()) {
				return;
			}
			oldTable = false;
			row = -1;
			col = -1;
			return;
		}
		throw new AssertionError();
	}

	@Override
    public void setVisible(boolean visible) {
	    super.setVisible(visible);
	    if(visible){
	    	return;
	    }
	    table.requestFocusInWindow();
    }


}
