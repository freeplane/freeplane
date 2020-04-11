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
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListDataListener;

import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.ui.components.JComboBoxWithBorder;
import org.freeplane.core.ui.components.TypedListCellRenderer;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.attribute.Attribute;
import org.freeplane.features.attribute.AttributeController;
import org.freeplane.features.attribute.AttributeRegistry;
import org.freeplane.features.attribute.AttributeRegistryElement;
import org.freeplane.features.attribute.IAttributesListener;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.filter.Filter;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.IMapSelectionListener;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

class AssignAttributeDialog extends JDialog implements IAttributesListener, IMapSelectionListener {
	private class AddAction extends IteratingAction {
		private String name;
		private String value;

		@Override
		public void actionPerformed(final ActionEvent e) {
			if (attributeNames.getSelectedItem() == null) {
				UITools.showAttributeEmptyStringErrorMessage();
				return;
			}
			name = attributeNames.getSelectedItem().toString();
			if (name.equals("")) {
				UITools.showAttributeEmptyStringErrorMessage();
				return;
			}
			final Object valueSelectedItem = attributeValues.getSelectedItem();
			value = valueSelectedItem != null ? valueSelectedItem.toString() : "";
			super.actionPerformed(e);
			if (valueSelectedItem == null) {
				selectedAttributeChanged(name, attributeValues);
			}
		}

		@Override
		protected void performAction(final NodeModel model) {
			attributeController.createAttributeTableModel(model);
			final NodeAttributeTableModel attributes = NodeAttributeTableModel.getModel(model);
			attributeController.performInsertRow(model, attributes, attributes.getRowCount(), name, value);
		}
	}

	protected static class ClonedComboBoxModel extends AbstractListModel implements ComboBoxModel {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private Object selectedItem;
		final private AbstractListModel sharedListModel;

		public ClonedComboBoxModel(final ComboBoxModel sharedListModel) {
			super();
			this.sharedListModel = (AbstractListModel) sharedListModel;
		}

		@Override
		public void addListDataListener(final ListDataListener l) {
			super.addListDataListener(l);
			sharedListModel.addListDataListener(l);
		}

		@Override
		public Object getElementAt(final int index) {
			return sharedListModel.getElementAt(index);
		}

		@Override
		public Object getSelectedItem() {
			return selectedItem;
		}

		@Override
		public int getSize() {
			return sharedListModel.getSize();
		}

		@Override
		public void removeListDataListener(final ListDataListener l) {
			super.removeListDataListener(l);
			sharedListModel.removeListDataListener(l);
		}

		@Override
		public void setSelectedItem(final Object anItem) {
			selectedItem = anItem;
			fireContentsChanged(this, -1, -1);
		}
	}

	private class DeleteAttributeAction extends IteratingAction {
		private String name;

		@Override
		public void actionPerformed(final ActionEvent e) {
			final Object selectedItem = attributeNames.getSelectedItem();
			if (selectedItem == null) {
				UITools.showAttributeEmptyStringErrorMessage();
				return;
			}
			name = selectedItem.toString();
			if (name.equals("")) {
				UITools.showAttributeEmptyStringErrorMessage();
				return;
			}
			super.actionPerformed(e);
		}

		@Override
		protected void performAction(final NodeModel model) {
			final NodeAttributeTableModel attributes = NodeAttributeTableModel.getModel(model);
			for (int i = attributes.getRowCount() - 1; i >= 0; i--) {
				if (attributes.getAttribute(i).getName().equals(name)) {
					attributeController.performRemoveRow(model, attributes, i);
				}
			}
		}
	}

	private class DeleteValueAction extends IteratingAction {
		private String name;
		private String value;

		@Override
		public void actionPerformed(final ActionEvent e) {
			if (attributeNames.getSelectedItem() == null) {
				UITools.showAttributeEmptyStringErrorMessage();
				return;
			}
			name = attributeNames.getSelectedItem().toString();
			if (name.equals("")) {
				UITools.showAttributeEmptyStringErrorMessage();
				return;
			}
			final Object valueSelectedItem = attributeValues.getSelectedItem();
			value = valueSelectedItem != null ? valueSelectedItem.toString() : "";
			super.actionPerformed(e);
		}

		@Override
		protected void performAction(final NodeModel model) {
			final NodeAttributeTableModel attributes = NodeAttributeTableModel.getModel(model);
			for (int i = attributes.getRowCount() - 1; i >= 0; i--) {
				final Attribute attribute = attributes.getAttribute(i);
				if (attribute.getName().equals(name) && attribute.getValue().equals(value)) {
					attributeController.performRemoveRow(model, attributes, i);
				}
			}
		}
	}

	private abstract class IteratingAction implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			try {
				if (selectedBtn.getModel().isSelected()) {
					final Collection<NodeModel> selecteds = mapSelection.getSelection();
					final Iterator<NodeModel> iterator = selecteds.iterator();
					while (iterator.hasNext()) {
						final NodeModel selectedNodeView = iterator.next();
						performAction(selectedNodeView);
					}
					return;
				}
				final NodeModel nodeView = Controller.getCurrentController().getMap().getRootNode();
				iterate(nodeView);
			}
			catch (final NullPointerException ex) {
			}
		}

		private void iterate(final NodeModel node) {
		    Filter filter = Controller.getCurrentController().getSelection().getFilter();
		    if (node.hasVisibleContent(filter)) {
		        if (!node.isRoot() || !skipRootBtn.isSelected()) {
		            performAction(node);
		        }
		    }
		    if (node.isFolded()) {
		        return;
			}
			final Iterator<NodeModel> iterator = node.getChildren().iterator();
			while (iterator.hasNext()) {
				iterate(iterator.next());
			}
		}

		abstract protected void performAction(NodeModel model);

	}

	private class ReplaceValueAction extends IteratingAction {
		private String name;
		private String replacingName;
		private String replacingValue;
		private String value;

		@Override
		public void actionPerformed(final ActionEvent e) {
			if (attributeNames.getSelectedItem() == null) {
				UITools.showAttributeEmptyStringErrorMessage();
				return;
			}
			if (replacingAttributeNames.getSelectedItem() == null) {
				UITools.showAttributeEmptyStringErrorMessage();
				return;
			}
			name = attributeNames.getSelectedItem().toString();
			if (name.equals("")) {
				UITools.showAttributeEmptyStringErrorMessage();
				return;
			}
			replacingName = replacingAttributeNames.getSelectedItem().toString();
			if (replacingName.equals("")) {
				UITools.showAttributeEmptyStringErrorMessage();
				return;
			}
			final Object valueSelectedItem = attributeValues.getSelectedItem();
			value = valueSelectedItem != null ? valueSelectedItem.toString() : "";
			final Object replacingValueSelectedItem = replacingAttributeValues.getSelectedItem();
			replacingValue = replacingValueSelectedItem != null ? replacingValueSelectedItem.toString() : "";
			super.actionPerformed(e);
		}

		@Override
		protected void performAction(final NodeModel model) {
			final NodeAttributeTableModel attributes = NodeAttributeTableModel.getModel(model);
			for (int i = attributes.getRowCount() - 1; i >= 0; i--) {
				final Attribute attribute = attributes.getAttribute(i);
				if (attribute.getName().equals(name) && attribute.getValue().equals(value)) {
					attributeController.performRemoveRow(model, attributes, i);
					attributeController.performInsertRow(model, attributes, i, replacingName, replacingValue);
				}
			}
		}
	}

	private static final Dimension maxButtonDimension = new Dimension(1000, 1000);
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	final private AttributeController attributeController;
	final private JComboBox attributeNames;
	final private JComboBox attributeValues;
	private IMapSelection mapSelection;
	final private JComboBox replacingAttributeNames;
	final private JComboBox replacingAttributeValues;
	final private JRadioButton selectedBtn;
	final private JCheckBox skipRootBtn;
	final private JRadioButton visibleBtn;

	public AssignAttributeDialog(final AttributeController attributeController, final Frame frame) {
		super(frame, TextUtils.getText("attributes_assign_dialog"), false);
		this.attributeController = attributeController;
		mapSelection = Controller.getCurrentController().getSelection();
		final Border actionBorder = new CompoundBorder(new EmptyBorder(5, 10, 0, 10), new CompoundBorder(
		    new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
		final Border emptyBorder = new EmptyBorder(5, 5, 5, 5);
		final Border btnBorder = new EmptyBorder(2, 2, 2, 2);
		selectedBtn = new JRadioButton();
		LabelAndMnemonicSetter.setLabelAndMnemonic(selectedBtn, TextUtils.getRawText("attributes_for_selected"));
		selectedBtn.setSelected(true);
		visibleBtn = new JRadioButton();
		LabelAndMnemonicSetter.setLabelAndMnemonic(visibleBtn, TextUtils.getRawText("attributes_for_visible"));
		final ButtonGroup group = new ButtonGroup();
		group.add(selectedBtn);
		group.add(visibleBtn);
		skipRootBtn = new JCheckBox();
		LabelAndMnemonicSetter.setLabelAndMnemonic(skipRootBtn, TextUtils.getRawText("attributes_skip_root"));
		skipRootBtn.setSelected(true);
		final Box selectionBox = Box.createHorizontalBox();
		selectionBox.setBorder(emptyBorder);
		selectionBox.add(Box.createHorizontalGlue());
		selectionBox.add(selectedBtn);
		selectionBox.add(Box.createHorizontalGlue());
		selectionBox.add(visibleBtn);
		selectionBox.add(Box.createHorizontalGlue());
		selectionBox.add(skipRootBtn);
		selectionBox.add(Box.createHorizontalGlue());
		getContentPane().add(selectionBox, BorderLayout.NORTH);
		final JButton addBtn = new JButton();
		LabelAndMnemonicSetter.setLabelAndMnemonic(addBtn, TextUtils.getRawText("filter_add"));
		addBtn.addActionListener(new AddAction());
		addBtn.setMaximumSize(AssignAttributeDialog.maxButtonDimension);
		final JButton deleteAttributeBtn = new JButton();
		LabelAndMnemonicSetter.setLabelAndMnemonic(deleteAttributeBtn, TextUtils.getRawText("attribute_delete"));
		deleteAttributeBtn.addActionListener(new DeleteAttributeAction());
		deleteAttributeBtn.setMaximumSize(AssignAttributeDialog.maxButtonDimension);
		final JButton deleteAttributeValueBtn = new JButton();
		LabelAndMnemonicSetter.setLabelAndMnemonic(deleteAttributeValueBtn, TextUtils.getRawText("attribute_delete_value"));
		deleteAttributeValueBtn.addActionListener(new DeleteValueAction());
		deleteAttributeValueBtn.setMaximumSize(AssignAttributeDialog.maxButtonDimension);
		final JButton replaceBtn = new JButton();
		LabelAndMnemonicSetter.setLabelAndMnemonic(replaceBtn, TextUtils.getRawText("attribute_replace"));
		replaceBtn.addActionListener(new ReplaceValueAction());
		replaceBtn.setMaximumSize(AssignAttributeDialog.maxButtonDimension);
		UITools.addEscapeActionToDialog(this);
		// Size of JComboBoxes (30 chars)
		final String pattern = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
		final JLabel patternLabel = new JLabel(pattern);
		final Dimension comboBoxMaximumSize = patternLabel.getPreferredSize();
		comboBoxMaximumSize.width += 4;
		comboBoxMaximumSize.height += 10;
		attributeNames = new JComboBoxWithBorder();
		attributeNames.setMaximumSize(comboBoxMaximumSize);
		attributeNames.setPreferredSize(comboBoxMaximumSize);
		attributeNames.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent e) {
				selectedAttributeChanged(e.getItem(), attributeValues);
			}
		});
		attributeValues = new JComboBoxWithBorder();
		attributeValues.setRenderer(new TypedListCellRenderer());
		attributeValues.setMaximumSize(comboBoxMaximumSize);
		attributeValues.setPreferredSize(comboBoxMaximumSize);
		replacingAttributeNames = new JComboBoxWithBorder();
		replacingAttributeNames.setMaximumSize(comboBoxMaximumSize);
		replacingAttributeNames.setPreferredSize(comboBoxMaximumSize);
		replacingAttributeNames.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent e) {
				selectedAttributeChanged(e.getItem(), replacingAttributeValues);
			}
		});
		replacingAttributeValues = new JComboBoxWithBorder();
		replacingAttributeValues.setRenderer(new TypedListCellRenderer());
		replacingAttributeValues.setMaximumSize(comboBoxMaximumSize);
		replacingAttributeValues.setPreferredSize(comboBoxMaximumSize);
		final Box addDeleteBtnBox = Box.createVerticalBox();
		addDeleteBtnBox.setBorder(btnBorder);
		addDeleteBtnBox.add(Box.createVerticalGlue());
		addDeleteBtnBox.add(addBtn);
		addDeleteBtnBox.add(deleteAttributeBtn);
		addDeleteBtnBox.add(deleteAttributeValueBtn);
		addDeleteBtnBox.add(Box.createVerticalGlue());
		final Box addDeleteBox = Box.createHorizontalBox();
		addDeleteBox.setBorder(actionBorder);
		addDeleteBox.add(Box.createHorizontalGlue());
		addDeleteBox.add(addDeleteBtnBox);
		addDeleteBox.add(Box.createHorizontalStrut(5));
		addDeleteBox.add(attributeNames);
		addDeleteBox.add(Box.createHorizontalStrut(5));
		addDeleteBox.add(attributeValues);
		addDeleteBox.add(Box.createHorizontalStrut(5));
		final Box outerReplaceBox = Box.createVerticalBox();
		outerReplaceBox.setBorder(actionBorder);
		final Box replaceBox = Box.createHorizontalBox();
		replaceBox.setBorder(btnBorder);
		replaceBox.add(Box.createHorizontalGlue());
		replaceBox.add(replaceBtn);
		replaceBox.add(Box.createHorizontalStrut(5));
		replaceBox.add(replacingAttributeNames);
		replaceBox.add(Box.createHorizontalStrut(5));
		replaceBox.add(replacingAttributeValues);
		replaceBox.add(Box.createHorizontalStrut(5));
		outerReplaceBox.add(Box.createVerticalGlue());
		outerReplaceBox.add(replaceBox);
		outerReplaceBox.add(Box.createVerticalGlue());
		final Box actionBox = Box.createVerticalBox();
		actionBox.add(Box.createVerticalGlue());
		actionBox.add(addDeleteBox);
		actionBox.add(Box.createVerticalStrut(5));
		actionBox.add(outerReplaceBox);
		actionBox.add(Box.createVerticalGlue());
		getContentPane().add(actionBox, BorderLayout.CENTER);
		final JButton closeBtn = new JButton();
		LabelAndMnemonicSetter.setLabelAndMnemonic(closeBtn, TextUtils.getRawText("simplyhtml.closeBtnName"));
		closeBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				dispose();
			}
		});
		final Box bottomBox = Box.createHorizontalBox();
		bottomBox.setBorder(emptyBorder);
		bottomBox.add(Box.createHorizontalGlue());
		bottomBox.add(closeBtn);
		bottomBox.add(Box.createHorizontalGlue());
		getContentPane().add(bottomBox, BorderLayout.SOUTH);
		pack();
		comboBoxMaximumSize.width = 1000;
		attributeNames.setMaximumSize(comboBoxMaximumSize);
		attributeValues.setMaximumSize(comboBoxMaximumSize);
		replacingAttributeNames.setMaximumSize(comboBoxMaximumSize);
		replacingAttributeValues.setMaximumSize(comboBoxMaximumSize);
		afterMapChange(null, Controller.getCurrentController().getMap());
		Controller.getCurrentController().getMapViewManager().addMapSelectionListener(this);
	}

	@Override
	public void afterMapChange(final MapModel oldMap, final MapModel newMap) {
		if (oldMap != null) {
			final AttributeRegistry attributes = AttributeRegistry.getRegistry(oldMap);
			if (attributes != null) {
				attributes.removeAttributesListener(this);
			}
		}
		if (newMap == null) {
			setVisible(false);
			return;
		}
		mapSelection = Controller.getCurrentController().getSelection();
		final AttributeRegistry attributes = AttributeRegistry.getRegistry(newMap);
		if (attributes == null) {
			setVisible(false);
			return;
		}
		attributes.addAttributesListener(this);
		attributesChanged();
	}

	private void attributesChanged() {
		final MapModel map = Controller.getCurrentController().getMap();
		final AttributeRegistry attributes = AttributeRegistry.getRegistry(map);
		final ComboBoxModel names = attributes.getComboBoxModel();
		attributeNames.setModel(new ClonedComboBoxModel(names));
		attributeNames.setEditable(!attributes.isRestricted());
		replacingAttributeNames.setModel(new ClonedComboBoxModel(names));
		replacingAttributeNames.setEditable(!attributes.isRestricted());
		if (attributes.size() > 0) {
			final Object first = names.getElementAt(0);
			attributeNames.setSelectedItem(first);
			replacingAttributeNames.setSelectedItem(first);
			selectedAttributeChanged(attributeNames.getSelectedItem(), attributeValues);
			selectedAttributeChanged(replacingAttributeNames.getSelectedItem(), replacingAttributeValues);
		}
		else {
			attributeValues.setModel(new DefaultComboBoxModel());
			attributeValues.setEditable(false);
			replacingAttributeValues.setModel(new DefaultComboBoxModel());
			replacingAttributeValues.setEditable(false);
		}
	}

	@Override
	public void attributesChanged(final ChangeEvent e) {
		attributesChanged();
	}

	private void selectedAttributeChanged(final Object selectedAttributeName, final JComboBox values) {
		final MapModel map = Controller.getCurrentController().getMap();
		final AttributeRegistry attributes = AttributeRegistry.getRegistry(map);
		try {
			final AttributeRegistryElement element = attributes.getElement(selectedAttributeName.toString());
			final ComboBoxModel selectedValues = element.getValues();
			values.setModel(new ClonedComboBoxModel(selectedValues));
			try {
				final Object firstValue = selectedValues.getElementAt(0);
				values.setSelectedItem(firstValue);
			}
			catch (final ArrayIndexOutOfBoundsException ex) {
			}
			values.setEditable(!element.isRestricted());
		}
		catch (final NoSuchElementException ex) {
			values.setEditable(!selectedAttributeName.toString().equals(""));
		}
	}
}
