/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry Polivaev
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

import java.awt.event.ActionEvent;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;

/**
 * @author Dimitry Polivaev
 * Mar 28, 2009
 */
class EditFilterAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	private final FilterController filterController;
	private AFilterComposerDialog filterDialog = null;

	EditFilterAction(final FilterController filterController) {
		super("EditFilterAction");
		this.filterController = filterController;
	}

	public void actionPerformed(final ActionEvent arg0) {
		final Object selectedItem = filterController.getFilterConditions().getSelectedItem();
		if (selectedItem != null) {
			getFilterDialog().setSelectedItem(selectedItem);
		}
		getFilterDialog().show();
	}
	
	private class FilterComposerDialog extends AFilterComposerDialog{
	    private static final long serialVersionUID = 1L;

		public FilterComposerDialog() {
	        super(TextUtils.getText("filter_dialog"), false);
        }

		protected DefaultComboBoxModel createModel() {
			DefaultComboBoxModel model = new DefaultComboBoxModel();
			ComboBoxModel externalConditionsModel = filterController.getFilterConditions();
			for (int i = FilterController.USER_DEFINED_CONDITION_START_INDEX; i < externalConditionsModel.getSize(); i++) {
				final Object element = externalConditionsModel.getElementAt(i);
				model.addElement(element);
			}
			Object selectedItem = externalConditionsModel.getSelectedItem(); 
			if(model.getIndexOf(selectedItem) != -1){
				model.setSelectedItem(selectedItem);
			}
			else{
				model.setSelectedItem(null);
			}
			return model;
	    }
		
		protected void applyModel(DefaultComboBoxModel model, int[] selectedIndices) {
		    filterController.setFilterConditions(model);
	    }

        @Override
        protected boolean isSelectionValid(int[] selectedIndices) {
            return true;
        }
		
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
	 * )
	 */
	private AFilterComposerDialog getFilterDialog() {
		if (filterDialog == null) {
			filterDialog = new FilterComposerDialog();
			getFilterDialog().setLocationRelativeTo(filterController.getFilterToolbar());
			Controller.getCurrentController().getMapViewManager().addMapViewChangeListener(filterDialog);
		}
		return filterDialog;
	}
}
