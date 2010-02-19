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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.Box;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
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

import org.freeplane.core.controller.Controller;
import org.freeplane.core.filter.condition.ConditionNotSatisfiedDecorator;
import org.freeplane.core.filter.condition.ConjunctConditions;
import org.freeplane.core.filter.condition.DisjunctConditions;
import org.freeplane.core.filter.condition.ISelectableCondition;
import org.freeplane.core.frame.IMapSelectionListener;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.url.UrlManager;
import org.freeplane.core.util.LogTool;

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
			ISelectableCondition newCond;
			newCond = editor.getCondition();
			if (newCond != null) {
				final DefaultComboBoxModel model = (DefaultComboBoxModel) elementaryConditionList.getModel();
				model.addElement(newCond);
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
			final ISelectableCondition[] selectedValues = toConditionsArray(elementaryConditionList.getSelectedValues());
			if (selectedValues.length < 2) {
				return;
			}
			final ISelectableCondition newCond = new ConjunctConditions(selectedValues);
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
			final ISelectableCondition[] selectedValues = toConditionsArray(elementaryConditionList.getSelectedValues());
			if (selectedValues.length < 2) {
				return;
			}
			final ISelectableCondition newCond = new DisjunctConditions(selectedValues);
			final DefaultComboBoxModel model = (DefaultComboBoxModel) elementaryConditionList.getModel();
			model.addElement(newCond);
			validate();
		}
	}
	
	private ISelectableCondition[] toConditionsArray(final Object[] objects) {
		final ISelectableCondition[] conditions = new ISelectableCondition[objects.length];
		for (int i = 0; i < objects.length; i++) {
			conditions[i] = (ISelectableCondition)objects[i];
		}
		return conditions;
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
					final ISelectableCondition oldCond = (ISelectableCondition) elementaryConditionList.getSelectedValue();
					final ISelectableCondition newCond = new ConditionNotSatisfiedDecorator(oldCond);
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
					LogTool.severe(ex);
				}
			}
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
			return ResourceBundles.getText("mindmaps_filter_desc");
		}
	}

	private class SaveAction implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			final JFileChooser chooser = getFileChooser();
			chooser.setDialogTitle(ResourceBundles.getText("SaveAsAction.text"));
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
				LogTool.severe(ex);
			}
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
	final private ConditionListSelectionListener conditionListListener;
	final private Controller controller;
	final private FilterConditionEditor editor;
	final private JList elementaryConditionList;
	private ComboBoxModel externalConditionsModel;
	final private FilterController filterController;
	private DefaultComboBoxModel internalConditionsModel;

	public FilterComposerDialog(final Controller controller) {
		super(controller.getViewController().getFrame(), ResourceBundles.getText("filter_dialog"));
		filterController = FilterController.getController(controller);
		editor = new FilterConditionEditor(filterController);
		this.controller = controller;
		getContentPane().add(editor, BorderLayout.NORTH);
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
		MenuBuilder.setLabelAndMnemonic(btnOK, ResourceBundles.getText("ok"));
		btnOK.addActionListener(closeAction);
		btnOK.setMaximumSize(FilterComposerDialog.maxButtonDimension);
		btnApply = new JButton();
		MenuBuilder.setLabelAndMnemonic(btnApply, ResourceBundles.getText("apply"));
		btnApply.addActionListener(closeAction);
		btnApply.setMaximumSize(FilterComposerDialog.maxButtonDimension);
		btnCancel = new JButton();
		MenuBuilder.setLabelAndMnemonic(btnCancel, ResourceBundles.getText("cancel"));
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
			MenuBuilder.setLabelAndMnemonic(btnSave, ResourceBundles.getText("SaveAction.text"));
			btnSave.addActionListener(saveAction);
			btnSave.setMaximumSize(FilterComposerDialog.maxButtonDimension);
			final ActionListener loadAction = new LoadAction();
			btnLoad = new JButton();
			MenuBuilder.setLabelAndMnemonic(btnLoad, ResourceBundles.getText("load"));
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
		UITools.setScrollbarIncrement(conditionScrollPane);
		UITools.addScrollbarIncrementPropertyListener(conditionScrollPane);
		final JLabel conditionColumnHeader = new JLabel(ResourceBundles.getText("filter_conditions"));
		conditionColumnHeader.setHorizontalAlignment(SwingConstants.CENTER);
		conditionScrollPane.setColumnHeaderView(conditionColumnHeader);
		conditionScrollPane.setPreferredSize(new Dimension(500, 200));
		getContentPane().add(conditionScrollPane, BorderLayout.CENTER);
		UITools.addEscapeActionToDialog(this);
		pack();
	}

	public void afterMapChange(final MapModel oldMap, final MapModel newMap) {
		editor.mapChanged(newMap);
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
