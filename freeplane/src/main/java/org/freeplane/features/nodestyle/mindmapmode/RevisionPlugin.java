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
package org.freeplane.features.nodestyle.mindmapmode;

import java.awt.Color;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.NodeHookDescriptor;
import org.freeplane.features.mode.PersistentNodeHook;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author foltin
 */
@NodeHookDescriptor(hookName = "accessories/plugins/RevisionPlugin.properties")
public class RevisionPlugin extends PersistentNodeHook implements INodeChangeListener, IExtension {
	public RevisionPlugin() {
		super();
		Controller.getCurrentModeController().getMapController().addUINodeChangeListener(this);
	}

	@Override
	protected void add(final NodeModel node, final IExtension extension) {
		super.add(node, extension);
	}

	@Override
	protected IExtension createExtension(final NodeModel node, final XMLElement element) {
		return this;
	}

	@Override
	public void nodeChanged(final NodeChangeEvent event) {
		final NodeModel node = event.getNode();
		if (!isActive(node)) {
			return;
		}
		if (event.getProperty().equals(NodeModel.NODE_TEXT)
				&& !((MModeController) Controller.getCurrentModeController()).isUndoAction(node.getMap())) {
			final MNodeStyleController nodeStyleController = (MNodeStyleController) NodeStyleController
			    .getController();
			final String colorProperty = ResourceController.getResourceController().getProperty("revision_color");
			final Color color = ColorUtils.stringToColor(colorProperty);
			nodeStyleController.setBackgroundColor(event.getNode(), color);
		}
	}

	@Override
	protected void remove(final NodeModel node, final IExtension extension) {
		super.remove(node, extension);
	}
}
