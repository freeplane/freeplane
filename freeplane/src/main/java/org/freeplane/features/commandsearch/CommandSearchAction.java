/*
 *  Freeplane - mind map editor
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;

public class CommandSearchAction extends AFreeplaneAction {
    private static final long serialVersionUID = 1L;
    static final String KEY = "CommandSearchAction";
    private CommandSearchDialog commandSearchDialog;

    public CommandSearchAction()
    {
        super(KEY);
    }

    @Override
    public void actionPerformed(final ActionEvent e)
    {
        if(commandSearchDialog == null)
            createDialog();
        else
            commandSearchDialog.toFront();
    }

    private void createDialog() {
        commandSearchDialog = new CommandSearchDialog(UITools.getCurrentFrame());
        commandSearchDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                commandSearchDialog = null;
            }
        });

    }
}
