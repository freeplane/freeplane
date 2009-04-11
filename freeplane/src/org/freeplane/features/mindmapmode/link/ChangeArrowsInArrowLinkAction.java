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

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.undo.IActor;
import org.freeplane.features.common.link.ArrowLinkModel;

class ChangeArrowsInArrowLinkAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ArrowLinkModel arrowLink;
	boolean hasEndArrow;
	boolean hasStartArrow;

	public ChangeArrowsInArrowLinkAction(final MLinkController linkController, final String text,
	                                     final String iconPath, final ArrowLinkModel arrowLink,
	                                     final boolean hasStartArrow, final boolean hasEndArrow) {
		super("ChangeArrowsInArrowLinkAction", linkController.getModeController().getController());
		this.arrowLink = arrowLink;
		this.hasStartArrow = hasStartArrow;
		this.hasEndArrow = hasEndArrow;
	}

	public void actionPerformed(final ActionEvent e) {
		changeArrowsOfArrowLink(arrowLink, hasStartArrow, hasEndArrow);
	}

	public void changeArrowsOfArrowLink(final ArrowLinkModel link, final boolean hasStartArrow,
	                                    final boolean hasEndArrow) {
		final IActor actor = new IActor() {
			final private String oldEndArrow = link.getEndArrow();
			final private String oldStartArrow = link.getStartArrow();

			public void act() {
				link.setStartArrow(hasStartArrow ? "Default" : "None");
				link.setEndArrow(hasEndArrow ? "Default" : "None");
				getModeController().getMapController().nodeChanged(link.getSource());
			}

			public String getDescription() {
				return "changeArrowsOfArrowLink";
			}

			public void undo() {
				link.setStartArrow(oldStartArrow);
				link.setEndArrow(oldEndArrow);
				getModeController().getMapController().nodeChanged(link.getSource());
			}
		};
		getModeController().execute(actor);
	}
}
