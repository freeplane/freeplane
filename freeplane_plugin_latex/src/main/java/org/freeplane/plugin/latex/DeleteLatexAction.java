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

import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

/**
 * 
 * @author Stefan Ott
 * 
 * This class is called when a (legacy!) LaTeX formula is deleted
 * @see http://freeplane.sourceforge.net/wiki/index.php/LaTeX_in_Freeplane
 */
@EnabledAction(checkOnNodeChange = true)
public class DeleteLatexAction extends AMultipleNodeAction {
	private static final long serialVersionUID = 1L;
	private final LatexNodeHook nodeHook;

	public DeleteLatexAction(final LatexNodeHook nodeHook) {
		super("LatexDeleteLatexAction");
		this.nodeHook = nodeHook;
	}

	@Override
	protected void actionPerformed(final ActionEvent e, final NodeModel node) {
		final LatexExtension latexExtension = (LatexExtension) node.getExtension(LatexExtension.class);
		if (latexExtension != null) {
			nodeHook.undoableDeactivateHook(node);
			Controller.getCurrentModeController().getMapController()
			    .nodeChanged(node, NodeModel.UNKNOWN_PROPERTY, null, null);
			return;
		}
	}

	@Override
	public void setEnabled() {
		final NodeModel node = Controller.getCurrentModeController().getMapController().getSelectedNode();
		setEnabled(node != null && (LatexExtension) node.getExtension(LatexExtension.class) != null);
	}
}
