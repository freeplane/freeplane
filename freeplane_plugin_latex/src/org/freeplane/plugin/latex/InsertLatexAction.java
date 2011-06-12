/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2010 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Stefan Ott in 2011.
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
package org.freeplane.plugin.latex;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

/**
 * 
 * @author Stefan Ott
 *
 *This class is called when a LaTeX formula is inserted into
 * (added to) a node
 */
@EnabledAction(checkOnNodeChange = true)
public class InsertLatexAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;
	private final LatexNodeHook nodeHook;

	public InsertLatexAction(final LatexNodeHook nodeHook) {
		super("LatexInsertLatexAction");
		this.nodeHook = nodeHook;
	}

	public void actionPerformed(final ActionEvent arg0) {
		final NodeModel node = Controller.getCurrentModeController().getMapController().getSelectedNode();
		final LatexExtension latexExtension = (LatexExtension) node.getExtension(LatexExtension.class);
		if (latexExtension == null) {
			nodeHook.editLatexInEditor(node);
			Controller.getCurrentModeController().getMapController()
			    .nodeChanged(node, NodeModel.UNKNOWN_PROPERTY, null, null);
			return;
		}
	}

	@Override
	public void setEnabled() {
		final NodeModel node = Controller.getCurrentModeController().getMapController().getSelectedNode();
		setEnabled(node != null && (LatexExtension) node.getExtension(LatexExtension.class) == null);
	}
}
