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
package org.freeplane.features.link;

import java.net.URI;

import javax.swing.Icon;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.ObjectAndIcon;
import org.freeplane.features.explorer.MapExplorerController;
import org.freeplane.features.format.PatternFormat;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.text.AbstractContentTransformer;
import org.freeplane.features.text.TextController;

/**
 * @author Dimitry Polivaev
 * Mar 3, 2011
 */
public class LinkTransformer extends AbstractContentTransformer {

	private static Icon localLinkIcon = ResourceController.getResourceController().getIcon("link_local_icon");
	private ModeController modeController;

	public LinkTransformer(ModeController modeController, int priority) {
	    super(priority);
	    this.modeController = modeController;
    }

	@Override
	public Object transformContent(NodeModel node, Object nodeProperty, Object content, TextController textController) {
		if(PatternFormat.IDENTITY_PATTERN.equals(textController.getNodeFormat(node)))
			return content;
		if(! (content instanceof URI))
			return content;
		final String string = content.toString();
		if(! string.startsWith("#"))
			return content;
		final String reference=string.substring(1);
		final NodeModel target = modeController.getExtension(MapExplorerController.class).getNodeAt(node, reference);
		if(target != null){
			final String shortText = TextController.getController(modeController).getShortPlainText(target);
			return new ObjectAndIcon(shortText, localLinkIcon);
		}
		else
			return content;
    }
}
