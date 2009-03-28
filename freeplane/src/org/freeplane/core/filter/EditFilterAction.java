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
package org.freeplane.core.filter;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;

/**
 * @author Dimitry Polivaev
 * Mar 28, 2009
 */
class EditFilterAction extends AFreeplaneAction {
	/**
     * 
     */
    private final FilterController filterController;
	private static final long serialVersionUID = 6280668187345048213L;

	EditFilterAction(FilterController filterController) {
		super(filterController.getController(), "filter_edit_description", "/images/Btn_edit.gif");
		this.filterController = filterController;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
	 * )
	 */
	private FilterComposerDialog getFilterDialog() {
		if (filterDialog == null) {
			filterDialog = new FilterComposerDialog(getController());
			getFilterDialog().setLocationRelativeTo(this.filterController.getFilterToolbar());
			getController().getMapViewManager().addMapSelectionListener(filterDialog);
		}
		return filterDialog;
	}
	private FilterComposerDialog filterDialog = null;
	public void actionPerformed(final ActionEvent arg0) {
		final Object selectedItem = this.filterController.getFilterConditions().getSelectedItem();
		filterController.showFilterToolbar(true);
		if (selectedItem != null) {
			getFilterDialog().setSelectedItem(selectedItem);
		}
		getFilterDialog().show();
	}

	public String getName() {
		return getClass().getSimpleName();
	}
}