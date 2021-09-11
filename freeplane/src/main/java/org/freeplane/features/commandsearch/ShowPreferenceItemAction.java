/*
 *  Freeplane - mind map editor
 *
 *  Copyright (C) 2020 Felix Natter
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
package org.freeplane.features.commandsearch;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import org.freeplane.core.resources.components.OptionPanelBuilder;
import org.freeplane.core.resources.components.OptionPanelConstants;
import org.freeplane.core.resources.components.ShowPreferencesAction;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;

class ShowPreferenceItemAction extends AbstractAction {
    private PreferencesItem preferencesItem;

    ShowPreferenceItemAction(PreferencesItem preferencesItem) {
        super("Show");
        this.preferencesItem = preferencesItem;
    }

    public void actionPerformed(ActionEvent actionEvent) {
        //System.out.format("Showing preferences item: %s\n", preferencesItem);
        showPrefsDialog();
    }

    private void showPrefsDialog()
    {
    	final Controller controller = Controller.getCurrentController();
		MModeController modeController = (MModeController) controller.getModeController(MModeController.MODENAME);
		OptionPanelBuilder optionPanelBuilder = modeController.getOptionPanelBuilder();
		ShowPreferencesAction showPreferencesAction = MModeController.createShowPreferencesAction(optionPanelBuilder, true, this.preferencesItem.getKey());
        int uniqueId = new Long(System.currentTimeMillis()).intValue();
        showPreferencesAction.actionPerformed(
                new ActionEvent(this, uniqueId, OptionPanelConstants.OPTION_PANEL_RESOURCE_PREFIX + preferencesItem.getTab()));
    }

}
