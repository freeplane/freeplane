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
package org.freeplane.features.attribute.mindmapmode;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;

import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.core.util.collection.SortedComboBoxModel;
import org.freeplane.features.attribute.AttributeRegistry;
import org.freeplane.features.attribute.AttributeRegistryElement;
import org.freeplane.features.icon.IconStore;
import org.freeplane.features.icon.factory.IconStoreFactory;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewManager;

class ImportAttributesDialog extends JDialog implements TreeSelectionListener {
	private static final IconStore STORE = IconStoreFactory.ICON_STORE;

	static private class AttributeTreeNodeInfo extends TreeNodeInfo {
		final private boolean restricted;

		public AttributeTreeNodeInfo(final String info, final boolean restricted) {
			super(info);
			this.restricted = restricted;
		}

		boolean isRestricted() {
			return restricted;
		}
	}

	static private class MyRenderer extends DefaultTreeCellRenderer {
		static final Icon iconFull = STORE.getUIIcon("ok_button.svg").getIcon();
		static final Icon iconNotSelected = STORE.getUIIcon("cancel_button.svg").getIcon();
		static final Icon iconPartial = STORE.getUIIcon("forward.svg").getIcon();
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		public MyRenderer() {
		}

		@Override
		public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean sel,
		                                              final boolean expanded, final boolean leaf, final int row,
		                                              final boolean hasFocus) {
			super.getTreeCellRendererComponent(tree, value, false, expanded, leaf, row, false);
			final DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			final TreeNodeInfo info = (TreeNodeInfo) node.getUserObject();
			switch (info.getSelected()) {
				case TreeNodeInfo.FULL_SELECTED:
					setIcon(MyRenderer.iconFull);
					break;
				case TreeNodeInfo.PARTIAL_SELECTED:
					setIcon(MyRenderer.iconPartial);
					break;
				case TreeNodeInfo.NOT_SELECTED:
					setIcon(MyRenderer.iconNotSelected);
					break;
			}
			return this;
		}
	}

	static private class TreeNodeInfo {
		static final private int FULL_SELECTED = 0;
		static final private int NOT_SELECTED = 2;
		static final private int PARTIAL_SELECTED = 1;
		final private String info;
		private int selected;

		public TreeNodeInfo(final String info) {
			this.info = info;
			selected = TreeNodeInfo.NOT_SELECTED;
		}

		String getInfo() {
			return info;
		}

		int getSelected() {
			return selected;
		}

		void setSelected(final int selected) {
			this.selected = selected;
		}

		@Override
		public String toString() {
			return info;
		}
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
// // 	final private Controller controller;
	private AttributeRegistry currentAttributes;
	final private Component parentComponent;
	MyRenderer renderer = null;
	final private JScrollPane scrollPane;
	final private DefaultMutableTreeNode topNode;
	final private JTree tree;
	final private DefaultTreeModel treeModel;

	public ImportAttributesDialog( final Component parentComponent) {
		super(UITools.getCurrentFrame(), TextUtils
		    .getText("attributes_import"), true);
//		this.controller = controller;
		this.parentComponent = parentComponent;
		final TreeNodeInfo nodeInfo = new TreeNodeInfo(TextUtils.getText("attribute_top"));
		topNode = new DefaultMutableTreeNode(nodeInfo);
		treeModel = new DefaultTreeModel(topNode);
		tree = new JTree(treeModel);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(this);
		scrollPane = new JScrollPane();
		scrollPane.setViewportView(tree);
		scrollPane.setPreferredSize(new Dimension(600, 300));
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		final Box buttons = Box.createHorizontalBox();
		buttons.setBorder(new EmptyBorder(5, 5, 5, 5));
		final JButton okBtn = new JButton();
		LabelAndMnemonicSetter.setLabelAndMnemonic(okBtn, TextUtils.getRawText("ok"));
		okBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				performImport(topNode);
				dispose();
			}
		});
		final JButton cancelBtn = new JButton();
		LabelAndMnemonicSetter.setLabelAndMnemonic(cancelBtn, TextUtils.getRawText("cancel"));
		cancelBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				dispose();
			}
		});
		buttons.add(Box.createHorizontalGlue());
		buttons.add(okBtn);
		buttons.add(Box.createHorizontalGlue());
		buttons.add(cancelBtn);
		buttons.add(Box.createHorizontalGlue());
		getContentPane().add(buttons, BorderLayout.SOUTH);
		UITools.addEscapeActionToDialog(this);
	}

	private void createAttributeSubTrees(final DefaultMutableTreeNode mapInfo, final AttributeRegistry attributes) {
		if (attributes == null) {
			return;
		}
		for (int i = 0; i < attributes.size(); i++) {
			final AttributeRegistryElement element = attributes.getElement(i);
			final TreeNodeInfo treeNodeInfo = new AttributeTreeNodeInfo(element.getKey().toString(), element
			    .isRestricted());
			final DefaultMutableTreeNode attributeInfo = new DefaultMutableTreeNode(treeNodeInfo);
			createValueSubTrees(attributeInfo, element, currentAttributes);
			if (attributeInfo.getChildCount() != 0) {
				mapInfo.add(attributeInfo);
			}
		}
	}

	private void createMapSubTrees(final DefaultMutableTreeNode top) {
		top.removeAllChildren();
		final TreeNodeInfo topInfo = (TreeNodeInfo) top.getUserObject();
		topInfo.setSelected(TreeNodeInfo.NOT_SELECTED);
		Controller controller = Controller.getCurrentController();
		final IMapViewManager mapViewManager = controller.getMapViewManager();
		final MapModel currentMap = controller.getMap();
		currentAttributes = AttributeRegistry.getRegistry(currentMap);
		final Iterator<Entry<String, MapModel>> iterator = mapViewManager.getMaps().entrySet().iterator();
		while (iterator.hasNext()) {
			final Entry<String, MapModel> entry = iterator.next();
			final String nextmapName = entry.getKey();
			final MapModel nextMap = entry.getValue();
			if (nextMap == currentMap) {
				continue;
			}
			final TreeNodeInfo treeNodeInfo = new TreeNodeInfo(nextmapName);
			final DefaultMutableTreeNode mapInfo = new DefaultMutableTreeNode(treeNodeInfo);
			createAttributeSubTrees(mapInfo, AttributeRegistry.getRegistry(nextMap));
			if (mapInfo.getChildCount() != 0) {
				top.add(mapInfo);
			}
		}
	}

	private void createValueSubTrees(final DefaultMutableTreeNode attributeInfo,
	                                 final AttributeRegistryElement element, final AttributeRegistry currentAttributes) {
		final String attributeName = element.getKey().toString();
		final SortedComboBoxModel values = element.getValues();
		for (int i = 0; i < values.getSize(); i++) {
			final Object nextElement = values.getElementAt(i);
			if (!currentAttributes.exist(attributeName, nextElement)) {
				final TreeNodeInfo treeNodeInfo = new TreeNodeInfo(nextElement.toString());
				final DefaultMutableTreeNode valueInfo = new DefaultMutableTreeNode(treeNodeInfo);
				attributeInfo.add(valueInfo);
			}
		}
	}

	private void performImport(final DefaultMutableTreeNode node) {
		final TreeNodeInfo info = (TreeNodeInfo) node.getUserObject();
		if (info.getSelected() == TreeNodeInfo.NOT_SELECTED) {
			return;
		}
		final String name = info.getInfo();
		boolean attributeNameRegistered = false;
		for (int i = 0; i < node.getChildCount(); i++) {
			final DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
			if (childNode.isLeaf()) {
				if (attributeNameRegistered == false) {
					attributeNameRegistered = true;
					if (-1 == currentAttributes.indexOf(name)) {
						currentAttributes.performRegistryAttribute(name);
						final int index = currentAttributes.indexOf(name);
						currentAttributes.performSetRestriction(index,
						    ((AttributeTreeNodeInfo) info).isRestricted());
					}
				}
				final TreeNodeInfo childInfo = (TreeNodeInfo) childNode.getUserObject();
				if (childInfo.getSelected() == TreeNodeInfo.FULL_SELECTED) {
					final String value = childInfo.getInfo();
					currentAttributes.performRegistryAttributeValue(name, value, true);
				}
			}
			else {
				performImport(childNode);
			}
		}
	}

	private void setParentSelectionType(final DefaultMutableTreeNode selectedNode, final int newSelectionType) {
		final TreeNode parentNode = selectedNode.getParent();
		if (parentNode == null) {
			return;
		}
		final DefaultMutableTreeNode defaultMutableParentNode = (DefaultMutableTreeNode) parentNode;
		final TreeNodeInfo info = (TreeNodeInfo) (defaultMutableParentNode).getUserObject();
		if (newSelectionType == TreeNodeInfo.PARTIAL_SELECTED) {
			if (info.getSelected() != TreeNodeInfo.PARTIAL_SELECTED) {
				info.setSelected(TreeNodeInfo.PARTIAL_SELECTED);
				treeModel.nodeChanged(defaultMutableParentNode);
			}
			setParentSelectionType(defaultMutableParentNode, TreeNodeInfo.PARTIAL_SELECTED);
			return;
		}
		for (int i = 0; i < defaultMutableParentNode.getChildCount(); i++) {
			final TreeNodeInfo childInfo = (TreeNodeInfo) ((DefaultMutableTreeNode) defaultMutableParentNode
			    .getChildAt(i)).getUserObject();
			if (childInfo.getSelected() != newSelectionType) {
				if (info.getSelected() != TreeNodeInfo.PARTIAL_SELECTED) {
					info.setSelected(TreeNodeInfo.PARTIAL_SELECTED);
					treeModel.nodeChanged(defaultMutableParentNode);
				}
				setParentSelectionType(defaultMutableParentNode, TreeNodeInfo.PARTIAL_SELECTED);
				return;
			}
		}
		if (info.getSelected() != newSelectionType) {
			info.setSelected(newSelectionType);
			treeModel.nodeChanged(defaultMutableParentNode);
		}
		setParentSelectionType(defaultMutableParentNode, newSelectionType);
	}

	private void setSelectionType(final TreeNode selectedNode, final int newSelectionType) {
		final TreeNodeInfo info = (TreeNodeInfo) ((DefaultMutableTreeNode) selectedNode).getUserObject();
		if (info.getSelected() != newSelectionType) {
			info.setSelected(newSelectionType);
			treeModel.nodeChanged(selectedNode);
		}
		for (int i = 0; i < selectedNode.getChildCount(); i++) {
			setSelectionType(selectedNode.getChildAt(i), newSelectionType);
		}
	}

	@Override
	public void show() {
		createMapSubTrees(topNode);
		if (topNode.getChildCount() == 0) {
			JOptionPane.showMessageDialog(parentComponent, TextUtils.getText("attributes_no_import_candidates_found"),
			    getTitle(), JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		treeModel.reload();
		if (renderer == null) {
			renderer = new MyRenderer();
		}
		tree.setCellRenderer(renderer);
		setLocationRelativeTo(parentComponent);
		pack();
		super.show();
	}

	@Override
	public void valueChanged(final TreeSelectionEvent e) {
		final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		if (selectedNode == null) {
			return;
		}
		final TreeNodeInfo info = (TreeNodeInfo) selectedNode.getUserObject();
		int newSelectionType;
		switch (info.getSelected()) {
			case TreeNodeInfo.FULL_SELECTED:
				newSelectionType = TreeNodeInfo.NOT_SELECTED;
				break;
			default:
				newSelectionType = TreeNodeInfo.FULL_SELECTED;
				break;
		}
		setSelectionType(selectedNode, newSelectionType);
		setParentSelectionType(selectedNode, newSelectionType);
		tree.clearSelection();
	}
}
