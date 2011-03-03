/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2011 dimitry
 *
 *  This file author is dimitry
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
package org.freeplane.features.common.link;

import java.net.URI;

import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.text.AbstractTextTransformer;
import org.freeplane.features.common.text.TextController;

/**
 * @author Dimitry Polivaev
 * Mar 3, 2011
 */
public class LinkTransformer extends AbstractTextTransformer {
	private ModeController modeController;

	public LinkTransformer(ModeController modeController, int priority) {
	    super(priority);
	    this.modeController = modeController;
		final NodeUpdateChangeListener listener = new NodeUpdateChangeListener();
		modeController.getMapController().addNodeChangeListener(listener);
		modeController.getMapController().addMapChangeListener(listener);
    }

	public Object transformContent(Object content, NodeModel node, Object transformedExtension) {
		if(! (content instanceof URI))
			return content;
		final String string = content.toString();
		if(! string.startsWith("#"))
			return content;
		final String nodeID=string.substring(1);
		final NodeModel target = node.getMap().getNodeForID(nodeID);
		return TextController.getController(modeController).getShortText(target);
	}
}
