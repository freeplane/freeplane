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
package org.freeplane.features.mindmapmode.link;

import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.frame.ColorTracker;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.undo.IUndoableActor;
import org.freeplane.features.common.link.ArrowLinkModel;
import org.freeplane.features.common.link.LinkController;

class ColorArrowLinkAction extends AFreeplaneAction {
	ArrowLinkModel arrowLink;

	public ColorArrowLinkAction(final MLinkController modeController, final ArrowLinkModel arrowLink) {
		super(modeController.getModeController().getController(), "arrow_link_color", "/images/Colors24.gif");
		this.arrowLink = arrowLink;
	}

	public void actionPerformed(final ActionEvent e) {
		final Controller controller = getController();
		final ModeController modeController = getModeController();
		final Color selectedColor = LinkController.getController(modeController).getColor(arrowLink);
		final Color color = ColorTracker.showCommonJColorChooserDialog(controller, controller.getSelection().getSelected(),
		    (String) this.getValue(Action.NAME), selectedColor);
		if (color == null) {
			return;
		}
		setArrowLinkColor(arrowLink, color);
	}

	public void setArrowLinkColor(final ArrowLinkModel arrowLink, final Color color) {
		final IUndoableActor actor = new IUndoableActor() {
			private Color oldColor;

			public void act() {
				oldColor = arrowLink.getColor();
				arrowLink.setColor(color);
				final NodeModel node = arrowLink.getSource();
				getModeController().getMapController().nodeChanged(node);
			}

			public String getDescription() {
				return "setArrowLinkColor";
			}

			public void undo() {
				arrowLink.setColor(oldColor);
				final NodeModel node = arrowLink.getSource();
				getModeController().getMapController().nodeChanged(node);
			}
		};
		getModeController().execute(actor);
	}
}
