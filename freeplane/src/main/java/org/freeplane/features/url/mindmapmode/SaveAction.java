/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.features.url.mindmapmode;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;

@EnabledAction(checkOnNodeChange=true)
class SaveAction extends AFreeplaneAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public SaveAction() {
		super("SaveAction");
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		if (Controller.getCurrentController().getMap().isReadOnly()) {
			JOptionPane.showMessageDialog(Controller.getCurrentController()
				    .getMapViewManager().getMapViewComponent(),
					TextUtils.getText("SaveAction_readonlyMsg"),
					TextUtils.getText("SaveAction_readonlyTitle"),
				    JOptionPane.WARNING_MESSAGE);
			return;
		}
		final boolean success = ((MModeController) Controller.getCurrentModeController()).save();
		final Controller controller = Controller.getCurrentController();
		if (success) {
			controller.getViewController().out(TextUtils.getText("saved"));
		}
		else {
			controller.getViewController().out(TextUtils.getText("saving_canceled"));
		}
	}

	@Override
	public void setEnabled() {
		final Controller controller = Controller.getCurrentController();
		MapModel map = controller.getMap();
		setEnabled(map != null && ! map.isSaved());
	}
}
