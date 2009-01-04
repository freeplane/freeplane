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
package org.freeplane.features.common.text;

import java.awt.event.ActionEvent;
import java.util.Collection;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.ui.FreeplaneAction;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.util.HtmlTools;

class FindNextAction extends FreeplaneAction {
	final private FindAction find;

	public FindNextAction(final ModeController controller, final FindAction find) {
		super("find_next");
		MenuBuilder.setLabelAndMnemonic(this, controller.getText("find_next"));
		this.find = find;
	}

	public void actionPerformed(final ActionEvent e) {
		final Collection subterms = find.getSubterms();
		if (subterms == null) {
			Controller.getController().informationMessage(
			    getModeController().getText("no_previous_find"),
			    Controller.getController().getMapView().getSelected());
			return;
		}
		final boolean found = find.findNext();
		Controller.getController().getMapView().repaint();
		if (!found) {
			final String messageText = getModeController().getText("no_more_found_from");
			final String searchTerm = messageText.startsWith("<html>") ? HtmlTools
			    .toXMLEscapedText(find.getSearchTerm()) : find.getSearchTerm();
			Controller.getController().informationMessage(
			    messageText.replaceAll("\\$1", searchTerm).replaceAll("\\$2",
			        find.getFindFromText()), Controller.getController().getMapView().getSelected());
		}
	}
}
