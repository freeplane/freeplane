/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2010 dimitry
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
package org.freeplane.features.nodestyle;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.NodeHookDescriptor;
import org.freeplane.features.mode.PersistentNodeHook;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * Nov 28, 2010
 */

@NodeHookDescriptor(hookName = "NodeCss", onceForMap = false)
public class NodeCssHook extends PersistentNodeHook implements IExtension{
	private ModeController modeController;

	public NodeCssHook() {
	    super();
		modeController = Controller.getCurrentModeController();
		modeController.addExtension(NodeCssHook.class, this);
    }

	@Override
	protected void registerActions() {
	}



	@Override
    protected Class<? extends IExtension> getExtensionClass() {
	    return NodeCss.class;
    }

	@Override
	protected IExtension createExtension(final NodeModel node, final XMLElement element) {
		String content = element.getContent();
		if(content == null || content.trim().isEmpty())
			return NodeCss.EMPTY;
		else
			return new NodeCss(content);
	}

	@Override
	protected void saveExtension(IExtension extension, XMLElement element) {
		super.saveExtension(extension, element);
		final NodeCss nodeCss = (NodeCss)extension;
		element.setContent(nodeCss.css);
	}

}

