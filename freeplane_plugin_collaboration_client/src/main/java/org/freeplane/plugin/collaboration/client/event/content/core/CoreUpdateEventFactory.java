package org.freeplane.plugin.collaboration.client.event.content.core;

import org.freeplane.core.resources.TranslatedObject;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TypeReference;
import org.freeplane.features.map.MapWriter;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.styles.StyleString;
import org.freeplane.plugin.collaboration.client.event.MapUpdated;

class CoreUpdateEventFactory {
	final MapWriter mapWriter;
	CoreUpdateEventFactory(MapWriter mapWriter) {
		super();
		this.mapWriter = mapWriter;
	}
	
	MapUpdated createCoreUpdatedEvent(NodeModel node) {
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
