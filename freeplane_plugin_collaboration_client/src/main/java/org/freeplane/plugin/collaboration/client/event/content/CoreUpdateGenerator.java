/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2018 dimitry
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
package org.freeplane.plugin.collaboration.client.event.content;

import org.freeplane.core.resources.TranslatedObject;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TypeReference;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.styles.StyleString;
import org.freeplane.features.styles.StyleTranslatedObject;
import org.freeplane.plugin.collaboration.client.event.MapUpdated;
import org.freeplane.plugin.collaboration.client.event.batch.Updates;

/**
 * @author Dimitry Polivaev
 * Jan 2, 2018
 */
public class CoreUpdateGenerator {

	private Updates updates;

	public CoreUpdateGenerator(Updates updates) {
		this.updates = updates;
	}

	public void onCoreUpdate(NodeModel node) {
			updates.addUpdateEvent(node.createID(), () -> createCoreUpdatedEvent(node));
	}

	private MapUpdated createCoreUpdatedEvent(NodeModel node) {
		final Object data = node.getUserObject();
		final CoreMediaType mediaType;
		final String content;
		final Class<? extends Object> dataClass = data.getClass();
		if (dataClass.equals(TranslatedObject.class)) {
			mediaType = CoreMediaType.LOCALIZED_TEXT;
			content = ((TranslatedObject) data).getObject().toString();
		}
		else if(! (data instanceof String || data instanceof StyleString)){
			mediaType = CoreMediaType.OBJECT;
			content = TypeReference.toSpec(data);
		}
		else {
			content = node.getText();
			mediaType = HtmlUtils.isHtmlNode(content) ? CoreMediaType.HTML : CoreMediaType.PLAIN_TEXT;
		}
		return CoreUpdated.builder() //
				.nodeId(node.getID()).mediaType(mediaType).content(content).build();
	}
	
	
}
