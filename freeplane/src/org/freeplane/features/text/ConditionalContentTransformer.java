/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Felix Natter
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
package org.freeplane.features.text;

import javax.swing.Icon;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.map.NodeModel;

/**
 * Decorator for IContentTransformer implementations that enables to switch
 * on/off depending on prefs option at transformation time
 * (we do this at transformation in order to not require a restart).
 * 
 * @author Felix Natter
 *
 */
public class ConditionalContentTransformer implements IContentTransformer {

	private final IContentTransformer target;
	private final String prefsConditionKey;
	
	public ConditionalContentTransformer(IContentTransformer target, final String prefsConditionKey)
	{
		this.target = target;
		this.prefsConditionKey = prefsConditionKey;
	}
	
	public int compareTo(IContentTransformer o) {
		return target.compareTo(o);
	}

	public Object transformContent(TextController textController,
			Object content, NodeModel node, Object transformedExtension)
			throws TransformationException {
		if (ResourceController.getResourceController().getBooleanProperty(prefsConditionKey))
			return target.transformContent(textController, content, node, transformedExtension);
		else
			return content;
	}

	public Icon getIcon(TextController textController, Object content,
			NodeModel node, Object transformedExtension) {
		if (ResourceController.getResourceController().getBooleanProperty(prefsConditionKey))
			return target.getIcon(textController, content, node, transformedExtension);
		else
			return null;
	}

	public int getPriority() {
		return target.getPriority();
	}

	public boolean markTransformation() {
		return target.markTransformation();
	}

}
