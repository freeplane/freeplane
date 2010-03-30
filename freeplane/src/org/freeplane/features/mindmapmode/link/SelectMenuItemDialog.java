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
package org.freeplane.features.mindmapmode.link;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.IndexedTree.Node;
import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtil;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.mindmapmode.MModeController;

/**
 * Presents the menu bar as a tree. Only allows the selection of leaf nodes.
 *
 * @author vboerchers
 */
class SelectMenuItemDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	// append "/extras/first/scripting/scripts" for scripts
	private static final String SELECTION_ROOT_KEY = FreeplaneMenuBar.MENU_BAR_PREFIX;
	private static final Dimension DIALOG_DIMENSION = new Dimension(350, 350);
	private JButton btnOK;
	private JTree tree;
	private String menuItemKey;

	private class CloseAction implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			final Object source = e.getSource();
			if (source == btnOK) {
				DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
				// this condition actually has to be true due to the TreeSelectionListener
				if (selectedNode != null && selectedNode.isLeaf()) {
					menuItemKey = ((SelectMenuItemDialog.MenuEntry) selectedNode.getUserObject()).getKey();
					dispose();
				}
			}
			else {
				dispose();
			}
		}
	}

	private class MenuIconRenderer extends DefaultTreeCellRenderer {
		private static final long serialVersionUID = 1L;

		public MenuIconRenderer() {
			setOpenIcon(null);
			setClosedIcon(null);
			// set default
			setLeafIcon(new ImageIcon(ResourceController.getResourceController()
			    .getResource("/images/icons/button.png")));
		}

		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
		                                              boolean leaf, int row, boolean hasFocus) {
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			if (leaf) {
				final DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
				final SelectMenuItemDialog.MenuEntry menuEntry = (SelectMenuItemDialog.MenuEntry) node.getUserObject();
				if (menuEntry.getIcon() != null)
					setIcon(menuEntry.getIcon());
			}
			return this;
		}
	}

	private static class MenuEntry {
		private final String key;
		private final String label;
		private final Icon icon;

		public MenuEntry(String key, String label, Icon icon) {
			this.key = key;
			this.label = label;
			this.icon = icon;
		}

		public Icon getIcon() {
			return icon;
		}

		public String getKey() {
			return key;
		}

		@Override
		public String toString() {
			return label;
		}
	}

	public SelectMenuItemDialog(Controller controller, NodeModel node) {
		super(UITools.getFrame(), TextUtil.getText("select_menu_item_dialog"), true);
		controller.getViewController().scrollNodeToVisible(node);
		UITools.setDialogLocationRelativeTo(this, controller, node);
		setSize(DIALOG_DIMENSION);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		UITools.addEscapeActionToDialog(this);
		tree = createTree(controller);
		getContentPane().add(new JScrollPane(tree));
		getContentPane().add(createButtonBar(), BorderLayout.SOUTH);
		getRootPane().setDefaultButton(btnOK);
		setVisible(true);
	}

	private Box createButtonBar() {
		final Box controllerBox = Box.createHorizontalBox();
		controllerBox.setBorder(new EmptyBorder(5, 0, 5, 0));
		final CloseAction closeAction = new CloseAction();
		btnOK = createButton("ok", closeAction);
		JButton btnCancel = createButton("cancel", closeAction);
		controllerBox.add(Box.createHorizontalGlue());
		controllerBox.add(btnOK);
		controllerBox.add(Box.createHorizontalGlue());
		controllerBox.add(btnCancel);
		controllerBox.add(Box.createHorizontalGlue());
		return controllerBox;
	}

	private JButton createButton(String key, final CloseAction closeAction) {
		JButton button = new JButton();
		MenuBuilder.setLabelAndMnemonic(button, TextUtil.getText(key));
		button.addActionListener(closeAction);
		button.setMaximumSize(new Dimension(1000, 1000));
		return button;
	}

	private JTree createTree(Controller controller) {
		MModeController modeController = (MModeController) controller.getModeController();
		final MenuBuilder menuBuilder = modeController.getUserInputListenerFactory().getMenuBuilder();
		final DefaultMutableTreeNode menuRoot = menuBuilder.get(SELECTION_ROOT_KEY);
		final Object rootLabel = (menuRoot.getUserObject() instanceof JMenuItem) ? convert(menuRoot) : TextUtil
		    .getText("select_menu_item_root_node");
		DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode(rootLabel);
		addChildrenRecursive(treeRoot, menuRoot.children());
		JTree jTree = new JTree(treeRoot);
		jTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		// replace the standard icons
		jTree.setCellRenderer(new MenuIconRenderer());
		jTree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
				btnOK.setEnabled(node != null && node.isLeaf());
			}
		});
		return jTree;
	}

	@SuppressWarnings("unchecked")
	private static void addChildrenRecursive(DefaultMutableTreeNode treeNode, Enumeration menuChildren) {
		while (menuChildren.hasMoreElements()) {
			DefaultMutableTreeNode childMenu = (DefaultMutableTreeNode) menuChildren.nextElement();
			final DefaultMutableTreeNode treeChild = convert(childMenu);
			if (treeChild != null) {
				treeNode.add(treeChild);
				addChildrenRecursive(treeChild, childMenu.children());
			}
			else {
				addChildrenRecursive(treeNode, childMenu.children());
			}
		}
	}

	// in: node for JMenu, out: node for MenuEntry
	private static DefaultMutableTreeNode convert(DefaultMutableTreeNode menuNode) {
		IndexedTree.Node node = (Node) menuNode;
		final Object userObject = menuNode.getUserObject();
		if (userObject instanceof JMenuItem) {
			JMenuItem jMenuItem = (JMenuItem) userObject;
			return new DefaultMutableTreeNode(new MenuEntry(String.valueOf(node.getKey()), jMenuItem.getText(),
			    jMenuItem.getIcon()));
		}
		// the other expected types are String and javax.swing.JPopupMenu.Separator
		// - just omit them
		return null;
	}

	public String getMenuItemKey() {
		return menuItemKey;
	}
}
