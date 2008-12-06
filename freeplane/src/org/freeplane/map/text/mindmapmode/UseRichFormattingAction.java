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
package org.freeplane.map.text.mindmapmode;

import org.freeplane.main.HtmlTools;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.map.tree.mindmapmode.MindMapMapModel;
import org.freeplane.modes.mindmapmode.MModeController;

import deprecated.freemind.modes.mindmapmode.actions.undo.ISingleNodeOperation;
import deprecated.freemind.modes.mindmapmode.actions.undo.NodeGeneralAction;

class UseRichFormattingAction extends NodeGeneralAction {
	public UseRichFormattingAction(final MModeController modeController) {
		super(modeController, "use_rich_formatting", null,
		    new ISingleNodeOperation() {
			    public void apply(final MindMapMapModel map,
			                      final NodeModel selected) {
				    final String nodeText = selected.getText();
				    if (!HtmlTools.isHtmlNode(nodeText)) {
					    ((MTextController) modeController.getTextController())
					        .setNodeText(selected, HtmlTools
					            .plainToHTML(nodeText));
				    }
			    }
		    });
	}
}
