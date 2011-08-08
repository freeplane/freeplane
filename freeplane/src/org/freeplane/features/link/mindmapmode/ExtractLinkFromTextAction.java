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
package org.freeplane.features.link.mindmapmode;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.map.NodeModel;

/**
 * @author Dimitry Polivaev
 * Mar 20, 2009
 */
public class ExtractLinkFromTextAction extends AMultipleNodeAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public ExtractLinkFromTextAction() {
		super("ExtractLinkFromTextAction");
	}

	@Override
	protected void actionPerformed(final ActionEvent e, final NodeModel node) {
		final MLinkController controller = (MLinkController) LinkController.getController();
		final String link = LinkController.findLink(node.getText());
		if (link != null) {
			controller.setLink(node, link, LinkController.LINK_ABSOLUTE);
		}
	}
}
