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
package org.freeplane.core.filter;

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
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.ComboBoxEditor;
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
import javax.swing.plaf.basic.BasicComboBoxEditor;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.filter.condition.ConditionNotSatisfiedDecorator;
import org.freeplane.core.filter.condition.ConjunctConditions;
import org.freeplane.core.filter.condition.DisjunctConditions;
import org.freeplane.core.filter.condition.ICondition;
import org.freeplane.core.filter.condition.IElementaryConditionController;
import org.freeplane.core.filter.util.ExtendedComboBoxModel;
import org.freeplane.core.frame.IMapSelectionListener;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.resources.FreeplaneResourceBundle;
import org.freeplane.core.resources.NamedObject;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.url.UrlManager;

/**
 * @author Dimitry Polivaev
 */
class FilterComposerDialog extends JDialog implements IMapSelectionListener {
	/**
	 * @author Dimitry Polivaev
	 */
	private class AddElementaryConditionAction extends AFreeplaneAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		AddElementaryConditionAction(final Controller controller) {
			super("AddElementaryConditionAction", controller);
		}

		public void actionPerformed(final ActionEvent e) {
			ICondition newCond;
			final Object value = values.getSelectedItem();
			final NamedObject simpleCond = (NamedObject) elementaryConditions.getSelectedItem();
			final boolean ignoreCase = caseInsensitive.isSelected();
			final Object selectedItem = filteredPropertiesComponent.getSelectedItem();
			newCond = filterController.getConditionFactory().createCondition(selectedItem, simpleCond, value,
			    ignoreCase);
			if (newCond != null) {
				final DefaultComboBoxModel model = (DefaultComboBoxModel) elementaryConditionList.getModel();
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

	private class ConditionListSelectionListener implements ListSelectionListener, ListDataListener {
		public void contentsChanged(final ListDataEvent e) {
		}

		public void intervalAdded(final ListDataEvent e) {
			elementaryConditionList.setSelectedIndex(e.getIndex0());
		}

		public void intervalRemoved(final ListDataEvent e) {
		}

		public void valueChanged(final ListSelectionEvent e) {
			if (elementaryConditionList.getMinSelectionIndex() == -1) {
				btnNot.setEnabled(false);
				btnAnd.setEnabled(false);
				btnOr.setEnabled(false);
				btnDelete.setEnabled(false);
				return;
			}
			else if (elementaryConditionList.getMinSelectionIndex() == elementaryConditionList.getMaxSelectionIndex()) {
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

	private class CreateConjunctConditionAction extends AFreeplaneAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		CreateConjunctConditionAction() {
			super("CreateConjunctConditionAction", controller);
		}

		public void actionPerformed(final ActionEvent e) {
			final Object[] selectedValues = elementaryConditionList.getSelectedValues();
			if (selectedValues.length < 2) {
				return;
			}
			final ICondition newCond = new ConjunctConditions(selectedValues);
			final DefaultComboBoxModel model = (DefaultComboBoxModel) elementaryConditionList.getModel();
			model.addElement(newCond);
			validate();
		}
	}

	private class CreateDisjunctConditionAction extends AFreeplaneAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		CreateDisjunctConditionAction() {
			super("CreateDisjunctConditionAction", controller);
		}

		public void actionPerformed(final ActionEvent e) {
			final Object[] selectedValues = elementaryConditionList.getSelectedValues();
			if (selectedValues.length < 2) {
				return;
			}
			final ICondition newCond = new DisjunctConditions(selectedValues);
			final DefaultComboBoxModel model = (DefaultComboBoxModel) elementaryConditionList.getModel();
			model.addElement(newCond);
			validate();
		}
	}

	private class CreateNotSatisfiedConditionAction extends AFreeplaneAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * 
		 */
		CreateNotSatisfiedConditionAction() {
			super("CreateNotSatisfiedConditionAction", controller);
		}

		public void actionPerformed(final ActionEvent e) {
			final int min = elementaryConditionList.getMinSelectionIndex();
			if (min >= 0) {
				final int max = elementaryConditionList.getMinSelectionIndex();
				if (min == max) {
					final ICondition oldCond = (ICondition) elementaryConditionList.getSelectedValue();
					final ICondition newCond = new ConditionNotSatisfiedDecorator(oldCond);
					final DefaultComboBoxModel model = (DefaultComboBoxModel) elementaryConditionList.getModel();
					model.addElement(newCond);
					validate();
				}
			}
		}
	}

	private class DeleteConditionAction extends AFreeplaneAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		DeleteConditionAction() {
			super("DeleteConditionAction", controller);
		}

		public void actionPerformed(final ActionEvent e) {
			final DefaultComboBoxModel model = (DefaultComboBoxModel) elementaryConditionList.getModel();
			final int minSelectionIndex = elementaryConditionList.getMinSelectionIndex();
			int selectedIndex;
			while (0 <= (selectedIndex = elementaryConditionList.getSelectedIndex())) {
				model.removeElementAt(selectedIndex);
			}
			final int size = elementaryConditionList.getModel().getSize();
			if (size > 0) {
				elementaryConditionList.setSelectedIndex(minSelectionIndex < size ? minSelectionIndex : size - 1);
			}
			validate();
		}
	}

	private class ElementaryConditionChangeListener implements ItemListener {
		public void itemStateChanged(final ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				final Object property = filteredPropertiesModel.getSelectedItem();
				final NamedObject selectedItem = (NamedObject) elementaryConditions.getSelectedItem();
				final IElementaryConditionController conditionController = filterController.getConditionFactory()
				    .getConditionController(property);
				final boolean canSelectValues = conditionController.canSelectValues(property, selectedItem);
				values.setEnabled(canSelectValues);
				caseInsensitive.setEnabled(canSelectValues
				        && conditionController.isCaseDependent(property, selectedItem));
			}
		}
	}

	private class FilteredPropertyChangeListener implements ItemListener {
		public void itemStateChanged(final ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				final Object selectedProperty = filteredPropertiesComponent.getSelectedItem();
				final IElementaryConditionController conditionController = filterController.getConditionFactory()
				    .getConditionController(selectedProperty);
				final ComboBoxModel simpleConditionComboBoxModel = conditionController
				    .getConditionsForProperty(selectedProperty);
				elementaryConditions.setModel(simpleConditionComboBoxModel);
				elementaryConditions.setEnabled(simpleConditionComboBoxModel.getSize() > 0);
				final NamedObject selectedCondition = (NamedObject) simpleConditionComboBoxModel.getSelectedItem();
				values.setEditable(conditionController.canEditValues(selectedProperty, selectedCondition));
				final boolean canSelectValues = conditionController
				    .canSelectValues(selectedProperty, selectedCondition);
				values.setEnabled(canSelectValues);
				values.setModel(conditionController.getValuesForProperty(selectedProperty));
				final ComboBoxEditor valueEditor = conditionController.getValueEditor();
				values.setEditor(valueEditor != null ? valueEditor : new BasicComboBoxEditor());
				if (values.getModel().getSize() > 0) {
					values.setSelectedIndex(0);
				}
				else {
					values.setSelectedIndex(-1);
				}
				caseInsensitive.setEnabled(canSelectValues
				        && conditionController.isCaseDependent(selectedProperty, selectedCondition));
				return;
			}
		}
	}

	private class LoadAction implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			final JFileChooser chooser = getFileChooser();
			final int returnVal = chooser.showOpenDialog(FilterComposerDialog.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					final File theFile = chooser.getSelectedFile();
					filterController.loadConditions(internalConditionsModel, theFile.getCanonicalPath());
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
			final String extension = UrlManager.getExtension(f.getName());
			if (extension != null) {
				if (extension.equals(FilterController.FREEPLANE_FILTER_EXTENSION_WITHOUT_DOT)) {
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
			return FreeplaneResourceBundle.getText("mindmaps_filter_desc");
		}
	}

	private class SaveAction implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			final JFileChooser chooser = getFileChooser();
			chooser.setDialogTitle(FreeplaneResourceBundle.getText("SaveAsAction.text"));
			final int returnVal = chooser.showSaveDialog(FilterComposerDialog.this);
			if (returnVal != JFileChooser.APPROVE_OPTION) {
				return;
			}
			try {
				final File f = chooser.getSelectedFile();
				String canonicalPath = f.getCanonicalPath();
				final String suffix = '.' + FilterController.FREEPLANE_FILTER_EXTENSION_WITHOUT_DOT;
				if (!canonicalPath.endsWith(suffix)) {
					canonicalPath = canonicalPath + suffix;
				}
				filterController.saveConditions(internalConditionsModel, canonicalPath);
			}
			catch (final Exception ex) {
				handleSavingException(ex);
			}
		}

		private void handleSavingException(final Exception ex) {
		}
	}

	private static final Dimension maxButtonDimension = new Dimension(1000, 1000);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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
	final private ConditionListSelectionListener conditionListListener;
	final private Controller controller;
	final private JList elementaryConditionList;
	final private JComboBox elementaryConditions;
	private ComboBoxModel externalConditionsModel;
	final private FilterController filterController;
	final private JComboBox filteredPropertiesComponent;
	final private ExtendedComboBoxModel filteredPropertiesModel;
	private DefaultComboBoxModel internalConditionsModel;
	//	private static final String FILTER_ICON = "filter_icon";
	//	private static final int ICON_POSITION = 1;
	//	final private ExtendedComboBoxModel icons;
	//	private static final String FILTER_NODE = "filter_node";
	//	final private ExtendedComboBoxModel nodes;
	//	private static final int NODE_POSITION = 0;
	//	private AttributeRegistry registeredAttributes;
	//	final private DefaultComboBoxModel simpleAttributeConditionComboBoxModel;
	//	final private DefaultComboBoxModel simpleIconConditionComboBoxModel;
	//	final private DefaultComboBoxModel simpleNodeConditionComboBoxModel;
	final private JComboBox values;

	public FilterComposerDialog(final Controller controller) {
		super(controller.getViewController().getFrame(), FreeplaneResourceBundle.getText("filter_dialog"));
		filterController = FilterController.getController(controller);
		this.controller = controller;
		final Box simpleConditionBox = Box.createHorizontalBox();
		simpleConditionBox.setBorder(new EmptyBorder(5, 0, 5, 0));
		getContentPane().add(simpleConditionBox, BorderLayout.NORTH);
		filteredPropertiesComponent = new JComboBox();
		filteredPropertiesModel = new ExtendedComboBoxModel();
		filteredPropertiesComponent.setModel(filteredPropertiesModel);
		filteredPropertiesComponent.addItemListener(new FilteredPropertyChangeListener());
		simpleConditionBox.add(Box.createHorizontalGlue());
		simpleConditionBox.add(filteredPropertiesComponent);
		filteredPropertiesComponent.setRenderer(filterController.getConditionRenderer());
		elementaryConditions = new JComboBox();
		elementaryConditions.addItemListener(new ElementaryConditionChangeListener());
		simpleConditionBox.add(Box.createHorizontalGlue());
		simpleConditionBox.add(elementaryConditions);
		elementaryConditions.setRenderer(filterController.getConditionRenderer());
		values = new JComboBox();
		simpleConditionBox.add(Box.createHorizontalGlue());
		simpleConditionBox.add(values);
		values.setRenderer(filterController.getConditionRenderer());
		values.setEditable(true);
		caseInsensitive = new JCheckBox();
		simpleConditionBox.add(Box.createHorizontalGlue());
		simpleConditionBox.add(caseInsensitive);
		caseInsensitive.setText(FreeplaneResourceBundle.getText("filter_ignore_case"));
		final Box conditionButtonBox = Box.createVerticalBox();
		conditionButtonBox.setBorder(new EmptyBorder(0, 10, 0, 10));
		getContentPane().add(conditionButtonBox, BorderLayout.EAST);
		btnAdd = new JButton(new AddElementaryConditionAction(controller));
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
		MenuBuilder.setLabelAndMnemonic(btnOK, FreeplaneResourceBundle.getText("ok"));
		btnOK.addActionListener(closeAction);
		btnOK.setMaximumSize(FilterComposerDialog.maxButtonDimension);
		btnApply = new JButton();
		MenuBuilder.setLabelAndMnemonic(btnApply, FreeplaneResourceBundle.getText("apply"));
		btnApply.addActionListener(closeAction);
		btnApply.setMaximumSize(FilterComposerDialog.maxButtonDimension);
		btnCancel = new JButton();
		MenuBuilder.setLabelAndMnemonic(btnCancel, FreeplaneResourceBundle.getText("cancel"));
		btnCancel.addActionListener(closeAction);
		btnCancel.setMaximumSize(FilterComposerDialog.maxButtonDimension);
		controllerBox.add(Box.createHorizontalGlue());
		controllerBox.add(btnOK);
		controllerBox.add(Box.createHorizontalGlue());
		controllerBox.add(btnApply);
		controllerBox.add(Box.createHorizontalGlue());
		controllerBox.add(btnCancel);
		controllerBox.add(Box.createHorizontalGlue());
		if (!controller.getViewController().isApplet()) {
			final ActionListener saveAction = new SaveAction();
			btnSave = new JButton();
			MenuBuilder.setLabelAndMnemonic(btnSave, FreeplaneResourceBundle.getText("SaveAction.text"));
			btnSave.addActionListener(saveAction);
			btnSave.setMaximumSize(FilterComposerDialog.maxButtonDimension);
			final ActionListener loadAction = new LoadAction();
			btnLoad = new JButton();
			MenuBuilder.setLabelAndMnemonic(btnLoad, FreeplaneResourceBundle.getText("load"));
			btnLoad.addActionListener(loadAction);
			btnLoad.setMaximumSize(FilterComposerDialog.maxButtonDimension);
			controllerBox.add(btnSave);
			controllerBox.add(Box.createHorizontalGlue());
			controllerBox.add(btnLoad);
			controllerBox.add(Box.createHorizontalGlue());
		}
		elementaryConditionList = new JList();
		elementaryConditionList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		elementaryConditionList.setCellRenderer(filterController.getConditionRenderer());
		elementaryConditionList.setLayoutOrientation(JList.VERTICAL);
		elementaryConditionList.setAlignmentX(Component.LEFT_ALIGNMENT);
		conditionListListener = new ConditionListSelectionListener();
		elementaryConditionList.addListSelectionListener(conditionListListener);
		elementaryConditionList.addMouseListener(new ConditionListMouseListener());
		final JScrollPane conditionScrollPane = new JScrollPane(elementaryConditionList);
		final JLabel conditionColumnHeader = new JLabel(FreeplaneResourceBundle.getText("filter_conditions"));
		conditionColumnHeader.setHorizontalAlignment(SwingConstants.CENTER);
		conditionScrollPane.setColumnHeaderView(conditionColumnHeader);
		conditionScrollPane.setPreferredSize(new Dimension(500, 200));
		getContentPane().add(conditionScrollPane, BorderLayout.CENTER);
		UITools.addEscapeActionToDialog(this);
		mapChanged(controller.getMap());
		pack();
	}

	public void afterMapChange(final MapModel oldMap, final MapModel newMap) {
		mapChanged(newMap);
	}

	public void afterMapClose(final MapModel oldMap) {
	}

	private void applyChanges() {
		internalConditionsModel.setSelectedItem(elementaryConditionList.getSelectedValue());
		internalConditionsModel.removeListDataListener(conditionListListener);
		filterController.setFilterConditions(internalConditionsModel);
		internalConditionsModel = null;
	}

	public void beforeMapChange(final MapModel oldMap, final MapModel newMap) {
	}

	protected JFileChooser getFileChooser() {
		final ModeController modeController = controller.getModeController();
		final JFileChooser chooser = UrlManager.getController(modeController).getFileChooser(
		    MindMapFilterFileFilter.filter);
		return chooser;
	}

	private void initInternalConditionModel() {
		externalConditionsModel = filterController.getFilterConditions();
		if (internalConditionsModel == null) {
			internalConditionsModel = new DefaultComboBoxModel();
			internalConditionsModel.addListDataListener(conditionListListener);
			elementaryConditionList.setModel(internalConditionsModel);
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
			elementaryConditionList.setSelectedIndex(index);
		}
		else {
			elementaryConditionList.clearSelection();
		}
	}

	public boolean isMapChangeAllowed(final MapModel oldMap, final MapModel newMap) {
		return true;
	}

	/**
	 */
	void mapChanged(final MapModel newMap) {
		if (newMap != null) {
			filteredPropertiesModel.removeAllElements();
			final Iterator<IElementaryConditionController> conditionIterator = filterController.getConditionFactory()
			    .conditionIterator();
			while (conditionIterator.hasNext()) {
				final IElementaryConditionController next = conditionIterator.next();
				filteredPropertiesModel.addExtensionList(next.getFilteredProperties());
				filteredPropertiesModel.setSelectedItem(filteredPropertiesModel.getElementAt(0));
			}
		}
		else {
			values.setSelectedIndex(-1);
			filteredPropertiesComponent.setSelectedIndex(0);
			filteredPropertiesModel.setExtensionList(null);
		}
	}

	private boolean selectCondition() {
		final int min = elementaryConditionList.getMinSelectionIndex();
		if (min >= 0) {
			final int max = elementaryConditionList.getMinSelectionIndex();
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
		elementaryConditionList.setSelectedValue(selectedItem, true);
	}

	@Override
	public void show() {
		initInternalConditionModel();
		super.show();
	}
}
