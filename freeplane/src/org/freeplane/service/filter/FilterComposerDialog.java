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
package org.freeplane.service.filter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.Box;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import org.freeplane.controller.Controller;
import org.freeplane.controller.FreeMindAction;
import org.freeplane.controller.resources.NamedObject;
import org.freeplane.main.Tools;
import org.freeplane.map.attribute.AttributeRegistry;
import org.freeplane.map.icon.MindIcon;
import org.freeplane.map.tree.MapModel;
import org.freeplane.map.tree.MapRegistry;
import org.freeplane.modes.ModeController;
import org.freeplane.modes.mindmapmode.MModeController;
import org.freeplane.service.filter.condition.ConditionNotSatisfiedDecorator;
import org.freeplane.service.filter.condition.ConjunctConditions;
import org.freeplane.service.filter.condition.DisjunctConditions;
import org.freeplane.service.filter.condition.ICondition;
import org.freeplane.service.filter.util.ExtendedComboBoxModel;
import org.freeplane.ui.MenuBuilder;

/**
 * @author Dimitry Polivaev
 */
public class FilterComposerDialog extends JDialog {
	/**
	 * @author Dimitry Polivaev
	 */
	private class AddConditionAction extends FreeMindAction {
		/*
		 * (non-Javadoc)
		 * @see
		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
		 * )
		 */
		AddConditionAction() {
			super("filter_add");
		}

		public void actionPerformed(final ActionEvent e) {
			ICondition newCond;
			String value;
			try {
				value = getAttributeValue();
			}
			catch (final NullPointerException ex) {
				return;
			}
			final NamedObject simpleCond = (NamedObject) simpleCondition
			    .getSelectedItem();
			final boolean ignoreCase = caseInsensitive.isSelected();
			final Object selectedItem = attributes.getSelectedItem();
			if (selectedItem instanceof NamedObject) {
				final NamedObject attribute = (NamedObject) selectedItem;
				newCond = FilterController.getConditionFactory()
				    .createCondition(attribute, simpleCond, value, ignoreCase);
			}
			else {
				final String attribute = selectedItem.toString();
				newCond = FilterController.getConditionFactory()
				    .createAttributeCondition(attribute, simpleCond, value,
				        ignoreCase);
			}
			final DefaultComboBoxModel model = (DefaultComboBoxModel) conditionList
			    .getModel();
			if (newCond != null) {
				model.addElement(newCond);
			}
			if (values.isEditable()) {
				final Object item = values.getSelectedItem();
				if (item != null && !item.equals("")) {
					values.removeItem(item);
					values.insertItemAt(item, 0);
					values.setSelectedIndex(0);
					if (values.getItemCount() >= 10) {
						values.removeItemAt(9);
					}
				}
			}
			validate();
		}
	}

	private class CloseAction implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			final Object source = e.getSource();
			if (source == btnOK || source == btnApply) {
				applyChanges();
			}
			if (source == btnOK || source == btnCancel) {
				dispose();
			}
			else {
				initInternalConditionModel();
			}
		}
	}

	private class ConditionListMouseListener extends MouseAdapter {
		@Override
		public void mouseClicked(final MouseEvent e) {
			if (e.getClickCount() == 2) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						if (selectCondition()) {
							dispose();
						}
					}
				});
			}
		}
	}

	private class ConditionListSelectionListener implements
	        ListSelectionListener, ListDataListener {
		public void contentsChanged(final ListDataEvent e) {
		}

		public void intervalAdded(final ListDataEvent e) {
			conditionList.setSelectedIndex(e.getIndex0());
		}

		public void intervalRemoved(final ListDataEvent e) {
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * javax.swing.event.ListSelectionListener#valueChanged(javax.swing.
		 * event.ListSelectionEvent)
		 */
		public void valueChanged(final ListSelectionEvent e) {
			if (conditionList.getMinSelectionIndex() == -1) {
				btnNot.setEnabled(false);
				btnAnd.setEnabled(false);
				btnOr.setEnabled(false);
				btnDelete.setEnabled(false);
				return;
			}
			else if (conditionList.getMinSelectionIndex() == conditionList
			    .getMaxSelectionIndex()) {
				btnNot.setEnabled(true);
				btnAnd.setEnabled(false);
				btnOr.setEnabled(false);
				btnDelete.setEnabled(true);
				return;
			}
			else {
				btnNot.setEnabled(false);
				btnAnd.setEnabled(true);
				btnOr.setEnabled(true);
				btnDelete.setEnabled(true);
			}
		}
	}

	private class CreateConjunctConditionAction extends FreeMindAction {
		/*
		 * (non-Javadoc)
		 * @see
		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
		 * )
		 */
		CreateConjunctConditionAction() {
			super("filter_and");
		}

		public void actionPerformed(final ActionEvent e) {
			final Object[] selectedValues = conditionList.getSelectedValues();
			if (selectedValues.length < 2) {
				return;
			}
			final ICondition newCond = new ConjunctConditions(selectedValues);
			final DefaultComboBoxModel model = (DefaultComboBoxModel) conditionList
			    .getModel();
			model.addElement(newCond);
			validate();
		}
	}

	private class CreateDisjunctConditionAction extends FreeMindAction {
		/*
		 * (non-Javadoc)
		 * @see
		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
		 * )
		 */
		CreateDisjunctConditionAction() {
			super("filter_or");
		}

		public void actionPerformed(final ActionEvent e) {
			final Object[] selectedValues = conditionList.getSelectedValues();
			if (selectedValues.length < 2) {
				return;
			}
			final ICondition newCond = new DisjunctConditions(selectedValues);
			final DefaultComboBoxModel model = (DefaultComboBoxModel) conditionList
			    .getModel();
			model.addElement(newCond);
			validate();
		}
	}

	private class CreateNotSatisfiedConditionAction extends FreeMindAction {
		/*
		 * (non-Javadoc)
		 * @see
		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
		 * )
		 */
		CreateNotSatisfiedConditionAction() {
			super("filter_not");
		}

		public void actionPerformed(final ActionEvent e) {
			final int min = conditionList.getMinSelectionIndex();
			if (min >= 0) {
				final int max = conditionList.getMinSelectionIndex();
				if (min == max) {
					final ICondition oldCond = (ICondition) conditionList
					    .getSelectedValue();
					final ICondition newCond = new ConditionNotSatisfiedDecorator(
					    oldCond);
					final DefaultComboBoxModel model = (DefaultComboBoxModel) conditionList
					    .getModel();
					model.addElement(newCond);
					validate();
				}
			}
		}
	}

	private class DeleteConditionAction extends FreeMindAction {
		/*
		 * (non-Javadoc)
		 * @see
		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
		 * )
		 */
		DeleteConditionAction() {
			super("filter_delete");
		}

		public void actionPerformed(final ActionEvent e) {
			final DefaultComboBoxModel model = (DefaultComboBoxModel) conditionList
			    .getModel();
			final int minSelectionIndex = conditionList.getMinSelectionIndex();
			int selectedIndex;
			while (0 <= (selectedIndex = conditionList.getSelectedIndex())) {
				model.removeElementAt(selectedIndex);
			}
			final int size = conditionList.getModel().getSize();
			if (size > 0) {
				conditionList
				    .setSelectedIndex(minSelectionIndex < size ? minSelectionIndex
				            : size - 1);
			}
			validate();
		}
	}

	private class LoadAction implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			final JFileChooser chooser = getFileChooser();
			final int returnVal = chooser
			    .showOpenDialog(FilterComposerDialog.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					final File theFile = chooser.getSelectedFile();
					fc.loadConditions(internalConditionsModel, theFile
					    .getCanonicalPath());
				}
				catch (final Exception ex) {
					handleLoadingException(ex);
				}
				{
				}
			}
		}

		private void handleLoadingException(final Exception ex) {
		}
	}

	static private class MindMapFilterFileFilter extends FileFilter {
		static FileFilter filter = new MindMapFilterFileFilter();

		@Override
		public boolean accept(final File f) {
			if (f.isDirectory()) {
				return true;
			}
			final String extension = Tools.getExtension(f.getName());
			if (extension != null) {
				if (extension
				    .equals(FilterController.FREEMIND_FILTER_EXTENSION_WITHOUT_DOT)) {
					return true;
				}
				else {
					return false;
				}
			}
			return false;
		}

		@Override
		public String getDescription() {
			return Controller.getText("mindmaps_filter_desc");
		}
	}

	private class SaveAction implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			final JFileChooser chooser = getFileChooser();
			chooser.setDialogTitle(Controller.getText("save_as"));
			final int returnVal = chooser
			    .showSaveDialog(FilterComposerDialog.this);
			if (returnVal != JFileChooser.APPROVE_OPTION) {
				return;
			}
			try {
				final File f = chooser.getSelectedFile();
				String canonicalPath = f.getCanonicalPath();
				final String suffix = '.' + FilterController.FREEMIND_FILTER_EXTENSION_WITHOUT_DOT;
				if (!canonicalPath.endsWith(suffix)) {
					canonicalPath = canonicalPath + suffix;
				}
				fc.saveConditions(internalConditionsModel, canonicalPath);
			}
			catch (final Exception ex) {
				handleSavingException(ex);
			}
		}

		private void handleSavingException(final Exception ex) {
		}
	}

	private class SelectedAttributeChangeListener implements ItemListener {
		/*
		 * (non-Javadoc)
		 * @see
		 * javax.swing.event.ListSelectionListener#valueChanged(javax.swing.
		 * event.ListSelectionEvent)
		 */
		public void itemStateChanged(final ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				if (attributes.getSelectedIndex() == FilterComposerDialog.NODE_POSITION) {
					simpleCondition.setModel(simpleNodeConditionComboBoxModel);
					simpleCondition.setEnabled(true);
					values.setEditable(true);
					values.setEnabled(true);
					nodes.setExtensionList(null);
					values.setModel(nodes);
					caseInsensitive.setEnabled(true);
					return;
				}
				if (attributes.getSelectedIndex() == FilterComposerDialog.ICON_POSITION) {
					simpleCondition.setModel(simpleIconConditionComboBoxModel);
					simpleCondition.setSelectedIndex(0);
					simpleCondition.setEnabled(false);
					values.setEditable(false);
					values.setEnabled(true);
					values.setModel(icons);
					if (icons.getSize() >= 1) {
						values.setSelectedIndex(0);
					}
					caseInsensitive.setEnabled(false);
					return;
				}
				if (attributes.getSelectedIndex() > FilterComposerDialog.NODE_POSITION) {
					final String attributeName = attributes.getSelectedItem()
					    .toString();
					nodes.setExtensionList(registeredAttributes.getElement(
					    attributeName).getValues());
					values.setModel(nodes);
					if (values.getSelectedItem() != null) {
						if (nodes.getSize() >= 1) {
							values.setSelectedIndex(0);
						}
						else {
							values.setSelectedItem(null);
						}
					}
					if (simpleCondition.getModel() != simpleAttributeConditionComboBoxModel) {
						simpleCondition
						    .setModel(simpleAttributeConditionComboBoxModel);
						simpleCondition.setSelectedIndex(0);
					}
					if (simpleCondition.getSelectedIndex() == 0) {
						caseInsensitive.setEnabled(false);
						values.setEnabled(false);
					}
					values.setEditable(true);
					simpleCondition.setEnabled(true);
					return;
				}
			}
		}
	}

	private class SimpleConditionChangeListener implements ItemListener {
		public void itemStateChanged(final ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				final boolean considerValue = !simpleCondition
				    .getSelectedItem().equals("filter_exist")
				        && !simpleCondition.getSelectedItem().equals(
				            "filter_does_not_exist");
				caseInsensitive.setEnabled(considerValue);
				values.setEnabled(considerValue);
			}
		}
	}

	private static final int ICON_POSITION = 1;
	private static final Dimension maxButtonDimension = new Dimension(1000,
	    1000);
	private static final int NODE_POSITION = 0;
	final private JComboBox attributes;
	final private JButton btnAdd;
	final private JButton btnAnd;
	final private JButton btnApply;
	final private JButton btnCancel;
	final private JButton btnDelete;
	private JButton btnLoad;
	final private JButton btnNot;
	final private JButton btnOK;
	final private JButton btnOr;
	private JButton btnSave;
	final private JCheckBox caseInsensitive;
	final private JList conditionList;
	final private ConditionListSelectionListener conditionListListener;
	private ComboBoxModel externalConditionsModel;
	final private FilterController fc;
	final private ExtendedComboBoxModel filteredAttributeComboBoxModel;
	final private ExtendedComboBoxModel icons;
	private DefaultComboBoxModel internalConditionsModel;
	final private ExtendedComboBoxModel nodes;
	private AttributeRegistry registeredAttributes;
	final private DefaultComboBoxModel simpleAttributeConditionComboBoxModel;
	final private JComboBox simpleCondition;
	final private DefaultComboBoxModel simpleIconConditionComboBoxModel;
	final private DefaultComboBoxModel simpleNodeConditionComboBoxModel;
	final private JComboBox values;

	public FilterComposerDialog(final FilterToolbar ft) {
		super(Controller.getController().getViewController().getJFrame(),
		    Controller.getText("filter_dialog"));
		fc = Controller.getController().getFilterController();
		final Box simpleConditionBox = Box.createHorizontalBox();
		simpleConditionBox.setBorder(new EmptyBorder(5, 0, 5, 0));
		getContentPane().add(simpleConditionBox, BorderLayout.NORTH);
		attributes = new JComboBox();
		filteredAttributeComboBoxModel = new ExtendedComboBoxModel(
		    new NamedObject[] {
		            Controller.getResourceController().createTranslatedString(
		                "filter_node"),
		            Controller.getResourceController().createTranslatedString(
		                "filter_icon") });
		final MapRegistry registry = Controller.getController().getModel()
		    .getRegistry();
		registeredAttributes = registry.getAttributes();
		filteredAttributeComboBoxModel.setExtensionList(registeredAttributes
		    .getListBoxModel());
		attributes.setModel(filteredAttributeComboBoxModel);
		attributes.addItemListener(new SelectedAttributeChangeListener());
		simpleConditionBox.add(Box.createHorizontalGlue());
		simpleConditionBox.add(attributes);
		attributes.setRenderer(fc.getConditionRenderer());
		simpleNodeConditionComboBoxModel = new DefaultComboBoxModel(
		    FilterController.getConditionFactory().getNodeConditionNames());
		simpleIconConditionComboBoxModel = new DefaultComboBoxModel(
		    FilterController.getConditionFactory().getIconConditionNames());
		simpleCondition = new JComboBox();
		simpleCondition.setModel(simpleNodeConditionComboBoxModel);
		simpleCondition.addItemListener(new SimpleConditionChangeListener());
		simpleConditionBox.add(Box.createHorizontalGlue());
		simpleConditionBox.add(simpleCondition);
		simpleCondition.setRenderer(fc.getConditionRenderer());
		simpleAttributeConditionComboBoxModel = new DefaultComboBoxModel(
		    FilterController.getConditionFactory().getAttributeConditionNames());
		values = new JComboBox();
		nodes = new ExtendedComboBoxModel();
		values.setModel(nodes);
		simpleConditionBox.add(Box.createHorizontalGlue());
		simpleConditionBox.add(values);
		values.setRenderer(fc.getConditionRenderer());
		values.setEditable(true);
		icons = new ExtendedComboBoxModel();
		icons.setExtensionList(registry.getIcons());
		caseInsensitive = new JCheckBox();
		simpleConditionBox.add(Box.createHorizontalGlue());
		simpleConditionBox.add(caseInsensitive);
		caseInsensitive.setText(Controller.getText("filter_ignore_case"));
		final Box conditionButtonBox = Box.createVerticalBox();
		conditionButtonBox.setBorder(new EmptyBorder(0, 10, 0, 10));
		getContentPane().add(conditionButtonBox, BorderLayout.EAST);
		btnAdd = new JButton(new AddConditionAction());
		btnAdd.setMaximumSize(FilterComposerDialog.maxButtonDimension);
		conditionButtonBox.add(Box.createVerticalGlue());
		conditionButtonBox.add(btnAdd);
		btnNot = new JButton(new CreateNotSatisfiedConditionAction());
		conditionButtonBox.add(Box.createVerticalGlue());
		btnNot.setMaximumSize(FilterComposerDialog.maxButtonDimension);
		conditionButtonBox.add(btnNot);
		btnNot.setEnabled(false);
		btnAnd = new JButton(new CreateConjunctConditionAction());
		conditionButtonBox.add(Box.createVerticalGlue());
		btnAnd.setMaximumSize(FilterComposerDialog.maxButtonDimension);
		conditionButtonBox.add(btnAnd);
		btnAnd.setEnabled(false);
		btnOr = new JButton(new CreateDisjunctConditionAction());
		conditionButtonBox.add(Box.createVerticalGlue());
		btnOr.setMaximumSize(FilterComposerDialog.maxButtonDimension);
		conditionButtonBox.add(btnOr);
		btnOr.setEnabled(false);
		btnDelete = new JButton(new DeleteConditionAction());
		btnDelete.setEnabled(false);
		conditionButtonBox.add(Box.createVerticalGlue());
		btnDelete.setMaximumSize(FilterComposerDialog.maxButtonDimension);
		conditionButtonBox.add(btnDelete);
		conditionButtonBox.add(Box.createVerticalGlue());
		final Box controllerBox = Box.createHorizontalBox();
		controllerBox.setBorder(new EmptyBorder(5, 0, 5, 0));
		getContentPane().add(controllerBox, BorderLayout.SOUTH);
		final CloseAction closeAction = new CloseAction();
		btnOK = new JButton();
		MenuBuilder.setLabelAndMnemonic(btnOK, Controller.getText("ok"));
		btnOK.addActionListener(closeAction);
		btnOK.setMaximumSize(FilterComposerDialog.maxButtonDimension);
		btnApply = new JButton();
		MenuBuilder.setLabelAndMnemonic(btnApply, Controller.getText("apply"));
		btnApply.addActionListener(closeAction);
		btnApply.setMaximumSize(FilterComposerDialog.maxButtonDimension);
		btnCancel = new JButton();
		MenuBuilder
		    .setLabelAndMnemonic(btnCancel, Controller.getText("cancel"));
		btnCancel.addActionListener(closeAction);
		btnCancel.setMaximumSize(FilterComposerDialog.maxButtonDimension);
		controllerBox.add(Box.createHorizontalGlue());
		controllerBox.add(btnOK);
		controllerBox.add(Box.createHorizontalGlue());
		controllerBox.add(btnApply);
		controllerBox.add(Box.createHorizontalGlue());
		controllerBox.add(btnCancel);
		controllerBox.add(Box.createHorizontalGlue());
		if (!Controller.getController().getViewController().isApplet()) {
			final ActionListener saveAction = new SaveAction();
			btnSave = new JButton();
			MenuBuilder
			    .setLabelAndMnemonic(btnSave, Controller.getText("save"));
			btnSave.addActionListener(saveAction);
			btnSave.setMaximumSize(FilterComposerDialog.maxButtonDimension);
			final ActionListener loadAction = new LoadAction();
			btnLoad = new JButton();
			MenuBuilder
			    .setLabelAndMnemonic(btnLoad, Controller.getText("load"));
			btnLoad.addActionListener(loadAction);
			btnLoad.setMaximumSize(FilterComposerDialog.maxButtonDimension);
			controllerBox.add(btnSave);
			controllerBox.add(Box.createHorizontalGlue());
			controllerBox.add(btnLoad);
			controllerBox.add(Box.createHorizontalGlue());
		}
		conditionList = new JList();
		conditionList
		    .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		conditionList.setCellRenderer(fc.getConditionRenderer());
		conditionList.setLayoutOrientation(JList.VERTICAL);
		conditionList.setAlignmentX(Component.LEFT_ALIGNMENT);
		conditionListListener = new ConditionListSelectionListener();
		conditionList.addListSelectionListener(conditionListListener);
		conditionList.addMouseListener(new ConditionListMouseListener());
		final JScrollPane conditionScrollPane = new JScrollPane(conditionList);
		final JLabel conditionColumnHeader = new JLabel(Controller
		    .getText("filter_conditions"));
		conditionColumnHeader.setHorizontalAlignment(SwingConstants.CENTER);
		conditionScrollPane.setColumnHeaderView(conditionColumnHeader);
		conditionScrollPane.setPreferredSize(new Dimension(500, 200));
		getContentPane().add(conditionScrollPane, BorderLayout.CENTER);
		Tools.addEscapeActionToDialog(this);
		pack();
	}

	private void applyChanges() {
		internalConditionsModel.setSelectedItem(conditionList
		    .getSelectedValue());
		internalConditionsModel.removeListDataListener(conditionListListener);
		fc.setFilterConditionModel(internalConditionsModel);
		internalConditionsModel = null;
	}

	private String getAttributeValue() {
		if (attributes.getSelectedIndex() == FilterComposerDialog.ICON_POSITION) {
			final MindIcon mi = (MindIcon) values.getSelectedItem();
			return mi.getName();
		}
		final Object item = values.getSelectedItem();
		return item != null ? item.toString() : "";
	}

	protected JFileChooser getFileChooser() {
		final ModeController modeController = fc.getMap().getModeController();
		final JFileChooser chooser = ((MModeController) modeController)
		    .getUrlManager().getFileChooser(MindMapFilterFileFilter.filter);
		return chooser;
	}

	private void initInternalConditionModel() {
		externalConditionsModel = fc.getFilterConditionModel();
		if (internalConditionsModel == null) {
			internalConditionsModel = new DefaultComboBoxModel();
			internalConditionsModel.addListDataListener(conditionListListener);
			conditionList.setModel(internalConditionsModel);
		}
		else {
			internalConditionsModel.removeAllElements();
		}
		int index = -1;
		for (int i = 2; i < externalConditionsModel.getSize(); i++) {
			final Object element = externalConditionsModel.getElementAt(i);
			internalConditionsModel.addElement(element);
			if (element == externalConditionsModel.getSelectedItem()) {
				index = i - 2;
			}
		}
		if (index >= 0) {
			conditionList.setSelectedIndex(index);
		}
		else {
			conditionList.clearSelection();
		}
	}

	/**
	 */
	void mapChanged(final MapModel newMap) {
		if (newMap != null) {
			icons.setExtensionList(newMap.getRegistry().getIcons());
			if (icons.getSize() >= 1 && values.getModel() == icons) {
				values.setSelectedIndex(0);
			}
			else {
				values.setSelectedIndex(-1);
				if (values.getModel() == icons) {
					values.setSelectedItem(null);
				}
			}
			if (attributes.getSelectedIndex() > 1) {
				attributes.setSelectedIndex(0);
			}
			registeredAttributes = newMap.getRegistry().getAttributes();
			filteredAttributeComboBoxModel
			    .setExtensionList(registeredAttributes.getListBoxModel());
		}
		else {
			icons.setExtensionList(null);
			values.setSelectedIndex(-1);
			attributes.setSelectedIndex(0);
			filteredAttributeComboBoxModel.setExtensionList(null);
		}
	}

	private boolean selectCondition() {
		final int min = conditionList.getMinSelectionIndex();
		if (min >= 0) {
			final int max = conditionList.getMinSelectionIndex();
			if (min == max) {
				applyChanges();
				return true;
			}
		}
		return false;
	}

	/**
	 */
	public void setSelectedItem(final Object selectedItem) {
		conditionList.setSelectedValue(selectedItem, true);
	}

	@Override
	public void show() {
		initInternalConditionModel();
		super.show();
	}
}
