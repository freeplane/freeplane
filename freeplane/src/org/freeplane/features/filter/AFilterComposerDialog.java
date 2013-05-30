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
package org.freeplane.features.filter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.ConditionNotSatisfiedDecorator;
import org.freeplane.features.filter.condition.ConjunctConditions;
import org.freeplane.features.filter.condition.DisjunctConditions;
import org.freeplane.features.filter.condition.ICombinedCondition;
import org.freeplane.features.map.IMapSelectionListener;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.UrlManager;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 */
public abstract class AFilterComposerDialog extends JDialog implements IMapSelectionListener {
	/**
	 * @author Dimitry Polivaev
	 */
	private class AddElementaryConditionAction extends AFreeplaneAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		AddElementaryConditionAction() {
			super("AddElementaryConditionAction");
		}

		public void actionPerformed(final ActionEvent e) {
			ASelectableCondition newCond;
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
			final boolean success;
			if (source == btnOK || source == btnApply) {
				success = applyChanges();
			}
			else {
				success = true;
			}
			if (!success) {
				return;
			}
			internalConditionsModel = null;
			if (source == btnOK) {
				dispose(true);
			}
			else if (source == btnCancel) {
				dispose(false);
			}
			else {
				initInternalConditionModel();
			}
		}

	}

	private boolean success;

	public boolean isSuccess() {
    	return success;
    }

	private void dispose(boolean b) {
        this.success = b;
        dispose();
    }
	private class ConditionListMouseListener extends MouseAdapter {
		@Override
		public void mouseClicked(final MouseEvent e) {
			if (e.getClickCount() == 2) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						if (selectCondition()) {
							dispose(true);
						}
					}
				});
			}
		}
	}

	private class ConditionListSelectionListener implements ListSelectionListener {
		public void valueChanged(final ListSelectionEvent e) {
			if (elementaryConditionList.getMinSelectionIndex() == -1) {
				btnNot.setEnabled(false);
				btnSplit.setEnabled(false);
				btnAnd.setEnabled(false);
				btnOr.setEnabled(false);
				btnDelete.setEnabled(false);
				btnName.setEnabled(false);
				return;
			}
			else if (elementaryConditionList.getMinSelectionIndex() == elementaryConditionList.getMaxSelectionIndex()) {
				btnNot.setEnabled(true);
				btnSplit.setEnabled(elementaryConditionList.getSelectedValue() instanceof ICombinedCondition);
				btnAnd.setEnabled(false);
				btnOr.setEnabled(false);
				btnDelete.setEnabled(true);
				btnName.setEnabled(true);
				return;
			}
			else {
				btnNot.setEnabled(false);
				btnSplit.setEnabled(false);
				btnAnd.setEnabled(true);
				btnOr.setEnabled(true);
				btnDelete.setEnabled(true);
				btnName.setEnabled(false);
			}
		}
	}

	private class CreateConjunctConditionAction extends AFreeplaneAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		CreateConjunctConditionAction() {
			super("CreateConjunctConditionAction");
		}

		public void actionPerformed(final ActionEvent e) {
			final ASelectableCondition[] selectedValues = toConditionsArray(elementaryConditionList.getSelectedValues());
			if (selectedValues.length < 2) {
				return;
			}
			final ASelectableCondition newCond = new ConjunctConditions(selectedValues);
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
			super("CreateDisjunctConditionAction");
		}

		public void actionPerformed(final ActionEvent e) {
			final ASelectableCondition[] selectedValues = toConditionsArray(elementaryConditionList.getSelectedValues());
			if (selectedValues.length < 2) {
				return;
			}
			final ASelectableCondition newCond = new DisjunctConditions(selectedValues);
			final DefaultComboBoxModel model = (DefaultComboBoxModel) elementaryConditionList.getModel();
			model.addElement(newCond);
			validate();
		}
	}

	private ASelectableCondition[] toConditionsArray(final Object[] objects) {
		final ASelectableCondition[] conditions = new ASelectableCondition[objects.length];
		for (int i = 0; i < objects.length; i++) {
			conditions[i] = (ASelectableCondition) objects[i];
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
			super("CreateNotSatisfiedConditionAction");
		}

		public void actionPerformed(final ActionEvent e) {
			final int min = elementaryConditionList.getMinSelectionIndex();
			if (min >= 0) {
				final int max = elementaryConditionList.getMinSelectionIndex();
				if (min == max) {
					final ASelectableCondition oldCond = (ASelectableCondition) elementaryConditionList
					    .getSelectedValue();
					final ASelectableCondition newCond = new ConditionNotSatisfiedDecorator(oldCond);
					final DefaultComboBoxModel model = (DefaultComboBoxModel) elementaryConditionList.getModel();
					model.addElement(newCond);
					validate();
				}
			}
		}
	}

	private class SplitConditionAction extends AFreeplaneAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * 
		 */
		SplitConditionAction() {
			super("SplitConditionAction");
		}

		public void actionPerformed(final ActionEvent e) {
			final int min = elementaryConditionList.getMinSelectionIndex();
			if (min >= 0) {
				final int max = elementaryConditionList.getMinSelectionIndex();
				if (min == max) {
					final ASelectableCondition oldCond = (ASelectableCondition) elementaryConditionList
					    .getSelectedValue();
					if (!(oldCond instanceof ICombinedCondition)) {
						return;
					}
					final Collection<ASelectableCondition> newConditions = ((ICombinedCondition) oldCond).split();
					final DefaultComboBoxModel model = (DefaultComboBoxModel) elementaryConditionList.getModel();
					for (ASelectableCondition newCond : newConditions) {
						final int index = model.getIndexOf(newCond);
						if (-1 == index) {
							model.addElement(newCond);
							final int newIndex = model.getSize() - 1;
							elementaryConditionList.addSelectionInterval(newIndex, newIndex);
						}
						else {
							elementaryConditionList.addSelectionInterval(index, index);
						}
					}
					elementaryConditionList.removeSelectionInterval(min, min);
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
			super("DeleteConditionAction");
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

	private class NameConditionAction extends AFreeplaneAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		NameConditionAction() {
			super("NameConditionAction");
		}

		public void actionPerformed(final ActionEvent e) {
			final DefaultComboBoxModel model = (DefaultComboBoxModel) elementaryConditionList.getModel();
			final int minSelectionIndex = elementaryConditionList.getMinSelectionIndex();
			if (minSelectionIndex == -1) {
				return;
			}
			final ASelectableCondition condition = (ASelectableCondition) model.getElementAt(minSelectionIndex);
			final String userName = condition.getUserName();
			final String newUserName = JOptionPane.showInputDialog(AFilterComposerDialog.this,
			    TextUtils.getText("enter_condition_name"), userName == null ? "" : userName);
			if(newUserName == null)
				return;
			XMLElement xmlCondition = new XMLElement();
			condition.toXml(xmlCondition);
			ASelectableCondition newCondition = filterController.getConditionFactory().loadCondition(xmlCondition.getChildAtIndex(0));
			if(newCondition== null)
				return;
			if (newUserName.equals("")) {
				if(userName == null)
					return;
				newCondition.setUserName(null);
			}
			else {
				if(newUserName.equals(userName))
					return;
				newCondition.setUserName(newUserName);
			}
			model.removeElementAt(minSelectionIndex);
			model.insertElementAt(newCondition, minSelectionIndex);
		}
	}

	private class LoadAction implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			final JFileChooser chooser = getFileChooser();
			final int returnVal = chooser.showOpenDialog(AFilterComposerDialog.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					final File theFile = chooser.getSelectedFile();
					internalConditionsModel.removeAllElements();
					filterController.loadConditions(internalConditionsModel, theFile.getCanonicalPath());
				}
				catch (final Exception ex) {
					LogUtils.severe(ex);
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
			final String extension = FileUtils.getExtension(f.getName());
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
			return TextUtils.getText("mindmaps_filter_desc");
		}
	}

	private class SaveAction implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			final JFileChooser chooser = getFileChooser();
			chooser.setDialogTitle(TextUtils.getText("SaveAsAction.text"));
			final int returnVal = chooser.showSaveDialog(AFilterComposerDialog.this);
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
				LogUtils.severe(ex);
			}
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final private JButton btnAdd;
	final private JButton btnAnd;
	final private JButton btnApply;
	final private JButton btnCancel;
	final private JButton btnDelete;
	final private JButton btnName;
	private JButton btnLoad;
	final private JButton btnNot;
	final private JButton btnSplit;
	final private JButton btnOK;
	final private JButton btnOr;
	private JButton btnSave;
	final private ConditionListSelectionListener conditionListListener;
	// // 	final private Controller controller;
	final private FilterConditionEditor editor;
	final private JList elementaryConditionList;
	final private FilterController filterController;
	private DefaultComboBoxModel internalConditionsModel;

	public AFilterComposerDialog(String title, boolean modal) {
		super(Controller.getCurrentController().getViewController().getFrame(), title, modal);
		filterController = FilterController.getCurrentFilterController();
		editor = new FilterConditionEditor(filterController);
		editor.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
		    BorderFactory.createEmptyBorder(5, 0, 5, 0)));
		//		this.controller = controller;
		getContentPane().add(editor, BorderLayout.NORTH);
		final Box conditionButtonBox = Box.createVerticalBox();
		conditionButtonBox.setBorder(new EmptyBorder(0, 10, 0, 10));
		getContentPane().add(conditionButtonBox, BorderLayout.EAST);
		btnAdd = new JButton(new AddElementaryConditionAction());
		btnAdd.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
		conditionButtonBox.add(Box.createVerticalGlue());
		conditionButtonBox.add(btnAdd);
		btnNot = new JButton(new CreateNotSatisfiedConditionAction());
		conditionButtonBox.add(Box.createVerticalGlue());
		btnNot.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
		conditionButtonBox.add(btnNot);
		btnNot.setEnabled(false);
		btnAnd = new JButton(new CreateConjunctConditionAction());
		conditionButtonBox.add(Box.createVerticalGlue());
		btnAnd.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
		conditionButtonBox.add(btnAnd);
		btnAnd.setEnabled(false);
		btnOr = new JButton(new CreateDisjunctConditionAction());
		conditionButtonBox.add(Box.createVerticalGlue());
		btnOr.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
		conditionButtonBox.add(btnOr);
		btnOr.setEnabled(false);
		btnSplit = new JButton(new SplitConditionAction());
		conditionButtonBox.add(Box.createVerticalGlue());
		btnSplit.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
		conditionButtonBox.add(btnSplit);
		btnSplit.setEnabled(false);
		btnDelete = new JButton(new DeleteConditionAction());
		btnDelete.setEnabled(false);
		conditionButtonBox.add(Box.createVerticalGlue());
		btnDelete.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
		conditionButtonBox.add(btnDelete);
		btnName = new JButton(new NameConditionAction());
		btnName.setEnabled(false);
		conditionButtonBox.add(Box.createVerticalGlue());
		btnName.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
		conditionButtonBox.add(btnName);
		conditionButtonBox.add(Box.createVerticalGlue());
		final Box controllerBox = Box.createHorizontalBox();
		controllerBox.setBorder(new EmptyBorder(5, 0, 5, 0));
		getContentPane().add(controllerBox, BorderLayout.SOUTH);
		final CloseAction closeAction = new CloseAction();
		btnOK = new JButton();
		MenuBuilder.setLabelAndMnemonic(btnOK, TextUtils.getRawText("ok"));
		btnOK.addActionListener(closeAction);
		btnOK.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
		controllerBox.add(Box.createHorizontalGlue());
		controllerBox.add(btnOK);
		if (!isModal()) {
			btnApply = new JButton();
			MenuBuilder.setLabelAndMnemonic(btnApply, TextUtils.getRawText("apply"));
			btnApply.addActionListener(closeAction);
			btnApply.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
			controllerBox.add(Box.createHorizontalGlue());
			controllerBox.add(btnApply);
		}
		else {
			btnApply = null;
		}
		btnCancel = new JButton();
		MenuBuilder.setLabelAndMnemonic(btnCancel, TextUtils.getRawText("cancel"));
		btnCancel.addActionListener(closeAction);
		btnCancel.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
		controllerBox.add(Box.createHorizontalGlue());
		controllerBox.add(btnCancel);
		controllerBox.add(Box.createHorizontalGlue());
		Controller controller = Controller.getCurrentController();
		if (!controller.getViewController().isApplet()) {
			final ActionListener saveAction = new SaveAction();
			btnSave = new JButton();
			MenuBuilder.setLabelAndMnemonic(btnSave, TextUtils.getRawText("FilterComposerDialog.save"));
			btnSave.addActionListener(saveAction);
			btnSave.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
			final ActionListener loadAction = new LoadAction();
			btnLoad = new JButton();
			MenuBuilder.setLabelAndMnemonic(btnLoad, TextUtils.getRawText("load"));
			btnLoad.addActionListener(loadAction);
			btnLoad.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
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
		final JLabel conditionColumnHeader = new JLabel(TextUtils.getText("filter_conditions"));
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

	private boolean applyChanges() {
		internalConditionsModel.setSelectedItem(elementaryConditionList.getSelectedValue());
		final int[] selectedIndices = elementaryConditionList.getSelectedIndices();
		if (applyModel(internalConditionsModel, selectedIndices)) {
			internalConditionsModel = null;
			return true;
		}
		else {
			return false;
		}
	}

	abstract protected boolean applyModel(DefaultComboBoxModel model, int[] selectedIndices);

	public void beforeMapChange(final MapModel oldMap, final MapModel newMap) {
	}

	protected JFileChooser getFileChooser() {
		final JFileChooser chooser = UrlManager.getController().getFileChooser(MindMapFilterFileFilter.filter, false);
		return chooser;
	}

	private void initInternalConditionModel() {
		internalConditionsModel = createModel();
		elementaryConditionList.setModel(internalConditionsModel);
		Object selectedItem = internalConditionsModel.getSelectedItem();
		if (selectedItem != null) {
			int selectedIndex = internalConditionsModel.getIndexOf(selectedItem);
			if (selectedIndex >= 0) {
				elementaryConditionList.setSelectedIndex(selectedIndex);
				return;
			}
		}
	}

	abstract protected DefaultComboBoxModel createModel();

	private boolean selectCondition() {
		final int min = elementaryConditionList.getMinSelectionIndex();
		if (min >= 0) {
			final int max = elementaryConditionList.getMinSelectionIndex();
			if (min == max) {
				return applyChanges();
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
		success = false;
		super.show();
	}
}
