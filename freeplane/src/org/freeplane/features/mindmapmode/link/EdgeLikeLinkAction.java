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

import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.undo.IActor;
import org.freeplane.features.common.link.ConnectorModel;

class EdgeLikeLinkAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ConnectorModel arrowLink;

	public EdgeLikeLinkAction(final MLinkController linkController, final ConnectorModel arrowLink) {
		super("EdgeLikeLinkAction", linkController.getModeController().getController());
		this.arrowLink = arrowLink;
	}

	public void actionPerformed(final ActionEvent e) {
		setEdgeLike(! arrowLink.isEdgeLike());
	}

	public void setEdgeLike(final boolean edgeLike) {
		final boolean alreadyEdgeLike = arrowLink.isEdgeLike();
		if (alreadyEdgeLike == edgeLike) {
			return;
		}
		final IActor actor = new IActor() {
			public void act() {
				arrowLink.setEdgeLike(edgeLike);
				final NodeModel node = arrowLink.getSource();
				getModeController().getMapController().nodeChanged(node);
			}

			public String getDescription() {
				return "setEdgeLike";
			}

			public void undo() {
				arrowLink.setEdgeLike(alreadyEdgeLike);
				final NodeModel node = arrowLink.getSource();
				getModeController().getMapController().nodeChanged(node);
			}
		};
		getModeController().execute(actor, arrowLink.getSource().getMap());
	}
}
