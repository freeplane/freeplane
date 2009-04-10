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
import java.util.List;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.FreeplaneResourceBundle;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.undo.IActor;
import org.freeplane.features.common.link.ArrowLinkModel;
import org.freeplane.features.common.link.NodeLinks;

/**
 * @author foltin
 */
class AddArrowLinkAction extends AFreeplaneAction {
	/**
     * 
     */
    private static final long serialVersionUID = 1L;

	private static String getIconPath() {
		return "/images/designer.png";
	}

	private static String getTitle() {
		return "add_link";
	}

	/**
	 */
	public AddArrowLinkAction(final Controller controller) {
		super("AddArrowLinkAction", controller);
	}

	public void actionPerformed(final ActionEvent e) {
		final List selecteds = getModeController().getMapController().getSelectedNodes();
		if (selecteds.size() < 2) {
			final Controller controller = getController();
			controller.errorMessage(FreeplaneResourceBundle.getText("less_than_two_selected_nodes"));
			return;
		}
		for (int i = 1; i < selecteds.size(); i++) {
			addLink((NodeModel) selecteds.get(i), (NodeModel) selecteds.get(0));
		}
	}

	public void addLink(final NodeModel source, final NodeModel target) {
		final String targetID = target.createID();
		final IActor actor = new IActor() {
			private ArrowLinkModel arrowLink;

			public void act() {
				NodeLinks nodeLinks = (NodeLinks) source.getExtension(NodeLinks.class);
				if (nodeLinks == null) {
					nodeLinks = new NodeLinks();
					source.addExtension(nodeLinks);
				}
				arrowLink = new ArrowLinkModel(source, targetID);
				nodeLinks.addArrowlink(arrowLink);
				getModeController().getMapController().nodeChanged(source);
			}

			public String getDescription() {
				return "addLink";
			}

			public void undo() {
				final NodeLinks nodeLinks = (NodeLinks) source.getExtension(NodeLinks.class);
				nodeLinks.removeArrowlink(arrowLink);
				getModeController().getMapController().nodeChanged(source);
			}
		};
		getModeController().execute(actor);
	}

}
