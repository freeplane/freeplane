/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2010 Volker Boerchers
 *
 *  This file author is Volker Boerchers
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
 *  along with this program.  If not, see &lt;http://www.gnu.org/licenses/&gt;.
 */
package org.freeplane.features.link.mindmapmode;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.MenuUtils;
import org.freeplane.core.util.MenuUtils.MenuEntry;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

/**
 * Presents the menu bar as a tree.
 * 
 * Only allows the selection of leaf nodes unless {@link #enableNonLeafNodes()} is invoked.
 *
 * @author vboerchers
 */
public class SelectMenuItemDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	// append "/extras/first/scripting/scripts" for scripts
	private static final String SELECTION_ROOT_KEY = "main_menu";
	private static final Dimension DIALOG_DIMENSION = new Dimension(350, 350);
	private boolean enableNonLeafNodes = false;
	private JButton btnOK;
	private final JTree tree;
	private MenuEntry menuItem;

	private class CloseAction implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			final Object source = e.getSource();
			if (source == btnOK) {
				final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree
				    .getLastSelectedPathComponent();
				// this condition actually has to be true due to the TreeSelectionListener
				if (selectedNode != null && nodeIsSelectable(selectedNode)) {
					menuItem = (MenuEntry) selectedNode.getUserObject();
					dispose();
				}
			}
			else {
				dispose();
			}
		}
	}

	private class MenuIconRenderer extends DefaultTreeCellRenderer {
		private static final String DEFAULT_ICON = "menuitem_icon";
		private static final long serialVersionUID = 1L;

		public MenuIconRenderer() {
			setOpenIcon(null);
			setClosedIcon(null);
			// set default
			setLeafIcon(ResourceController.getResourceController().getIcon(DEFAULT_ICON));
		}

		@Override
		public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean sel,
		                                              final boolean expanded, final boolean leaf, final int row,
		                                              final boolean hasFocus) {
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			final DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			Object userObject = node.getUserObject();
			if(userObject instanceof MenuEntry) {
			    final MenuEntry menuEntry = (MenuEntry) userObject;
			    Icon icon = menuEntry.getIcon();
			    if (icon != null) {
			        setIcon(icon);
			    }
			}
			return this;
		}
	}

	public SelectMenuItemDialog(final NodeModel node, boolean enableNonLeafNodes) {
		super((Frame) UITools.getMenuComponent(), TextUtils.getText("select_menu_item_dialog"), true);
		this.enableNonLeafNodes = enableNonLeafNodes;
		Controller.getCurrentController().getMapViewManager().scrollNodeToVisible(node);
		UITools.setDialogLocationRelativeTo(this, node);
		setSize(DIALOG_DIMENSION);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		UITools.addEscapeActionToDialog(this);
		tree = createTree();
		getContentPane().add(new JScrollPane(tree));
		getContentPane().add(createButtonBar(), BorderLayout.SOUTH);
		getRootPane().setDefaultButton(btnOK);
		setVisible(true);
	}
	
	/** Opens a dialog with only leaf nodes are selectable. */
	public SelectMenuItemDialog(final NodeModel node) {
		this(node, false);
	}

	public MenuEntry getMenuItem() {
		return menuItem;
	}

	private Box createButtonBar() {
		final Box controllerBox = Box.createHorizontalBox();
		controllerBox.setBorder(new EmptyBorder(5, 0, 5, 0));
		final CloseAction closeAction = new CloseAction();
		btnOK = createButton("ok", closeAction);
		final JButton btnCancel = createButton("cancel", closeAction);
		controllerBox.add(Box.createHorizontalGlue());
		controllerBox.add(btnOK);
		controllerBox.add(Box.createHorizontalGlue());
		controllerBox.add(btnCancel);
		controllerBox.add(Box.createHorizontalGlue());
		return controllerBox;
	}

	private JButton createButton(final String key, final CloseAction closeAction) {
		final JButton button = new JButton();
		LabelAndMnemonicSetter.setLabelAndMnemonic(button, TextUtils.getRawText(key));
		button.addActionListener(closeAction);
		button.setMaximumSize(new Dimension(1000, 1000));
		return button;
	}

	private JTree createTree() {
		final DefaultMutableTreeNode treeRoot = MenuUtils.createMenuEntryTree(SELECTION_ROOT_KEY);
		if (treeRoot.getUserObject() == null)
			treeRoot.setUserObject(new MenuEntry(TextUtils.getText("select_menu_item_root_node"), null));
		JTree jTree = new JTree(treeRoot);
		jTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		// replace the standard icons
		jTree.setCellRenderer(new MenuIconRenderer());
		jTree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(final TreeSelectionEvent e) {
				final DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
				btnOK.setEnabled(node != null && nodeIsSelectable(node));
			}
		});
		jTree.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >= 2) {
					if (btnOK.isEnabled())
						btnOK.doClick();
				}
			}
		});

		return jTree;
	}

	private boolean nodeIsSelectable(final DefaultMutableTreeNode selectedNode) {
		return enableNonLeafNodes || selectedNode.isLeaf();
	}
}
