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
package org.freeplane.addins.mindmapmode;

import java.awt.Color;

import org.freeplane.addins.NodeHookDescriptor;
import org.freeplane.addins.PersistentNodeHook;
import org.freeplane.core.controller.ActionDescriptor;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.map.NodeModel;
import org.freeplane.core.mode.INodeChangeListener;
import org.freeplane.core.mode.NodeChangeEvent;
import org.freeplane.map.nodestyle.mindmapmode.MNodeStyleController;
import org.freeplane.modes.mindmapmode.MModeController;

/**
 * @author foltin
 */
@NodeHookDescriptor(hookName = "accessories/plugins/RevisionPlugin.properties")
@ActionDescriptor(name = "accessories/plugins/RevisionPlugin.properties_name")
public class RevisionPlugin extends PersistentNodeHook implements INodeChangeListener {
	public RevisionPlugin(final MModeController modeController) {
		super(modeController);
	}

	@Override
	protected void add(final NodeModel node, final IExtension extension) {
		super.add(node, extension);
		getModeController().addNodeChangeListener(this);
	}

	public void nodeChanged(final NodeChangeEvent event) {
		if (event.getProperty().equals(NodeModel.NODE_TEXT)
		        && !((MModeController) getModeController()).isUndoAction()) {
			final MNodeStyleController nodeStyleController = (MNodeStyleController) getModeController()
			    .getNodeStyleController();
			nodeStyleController.setBackgroundColor(event.getNode(), Color.YELLOW);
		}
	}

	@Override
	protected void remove(final NodeModel node, final IExtension extension) {
		getModeController().removeNodeChangeListener(this);
		super.remove(node, extension);
	}
}
