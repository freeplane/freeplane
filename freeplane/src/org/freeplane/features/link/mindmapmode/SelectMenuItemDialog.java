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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
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

import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.MenuUtils;
import org.freeplane.core.util.MenuUtils.MenuEntry;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.icon.MindIcon;
import org.freeplane.features.icon.factory.MindIconFactory;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;

/**
 * Presents the menu bar as a tree. Only allows the selection of leaf nodes.
 *
 * @author vboerchers
 */
public class SelectMenuItemDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	// append "/extras/first/scripting/scripts" for scripts
	private static final String SELECTION_ROOT_KEY = FreeplaneMenuBar.MENU_BAR_PREFIX;
	private static final Dimension DIALOG_DIMENSION = new Dimension(350, 350);
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
				if (selectedNode != null && selectedNode.isLeaf()) {
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
		private static final String DEFAULT_ICON = "button";
		private static final long serialVersionUID = 1L;

		public MenuIconRenderer() {
			setOpenIcon(null);
			setClosedIcon(null);
			// set default
			setLeafIcon(MindIconFactory.create(DEFAULT_ICON).getIcon());
		}

		@Override
		public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean sel,
		                                              final boolean expanded, final boolean leaf, final int row,
		                                              final boolean hasFocus) {
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			if (leaf) {
				final DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
				final MenuEntry menuEntry = (MenuEntry) node.getUserObject();
				if (menuEntry.getIconKey() != null) {
					final MindIcon mindIcon = menuEntry.createMindIcon();
					if (mindIcon != null)
						setIcon(mindIcon.getIcon());
				}
			}
			return this;
		}
	}

	public SelectMenuItemDialog(final NodeModel node) {
		super(UITools.getFrame(), TextUtils.getText("select_menu_item_dialog"), true);
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
		MenuBuilder.setLabelAndMnemonic(button, TextUtils.getRawText(key));
		button.addActionListener(closeAction);
		button.setMaximumSize(new Dimension(1000, 1000));
		return button;
	}

	private JTree createTree() {
		final MModeController modeController = (MModeController) Controller.getCurrentModeController();
		final MenuBuilder menuBuilder = modeController.getUserInputListenerFactory().getMenuBuilder(MenuBuilder.class);
		final DefaultMutableTreeNode treeRoot = MenuUtils.createMenuEntryTree(SELECTION_ROOT_KEY, menuBuilder);
		if (treeRoot.getUserObject() == null)
			treeRoot.setUserObject(new MenuEntry(null, TextUtils.getText("select_menu_item_root_node")));
		JTree jTree = new JTree(treeRoot);
		jTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		// replace the standard icons
		jTree.setCellRenderer(new MenuIconRenderer());
		jTree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(final TreeSelectionEvent e) {
				final DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
				btnOK.setEnabled(node != null && node.isLeaf());
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
}
