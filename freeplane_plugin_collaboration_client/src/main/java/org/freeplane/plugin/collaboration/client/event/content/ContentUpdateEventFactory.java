package org.freeplane.plugin.collaboration.client.event.content;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.TranslatedObject;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TypeReference;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.MapWriter;
import org.freeplane.features.map.MapWriter.Mode;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.MapExtensions;
import org.freeplane.features.styles.StyleString;
import org.freeplane.plugin.collaboration.client.event.MapUpdated;

public class ContentUpdateEventFactory {
	final MapWriter mapWriter;
	public ContentUpdateEventFactory(MapWriter mapWriter) {
		super();
		this.mapWriter = mapWriter;
	}
	
	public MapUpdated createNodeContentUpdatedEvent(final NodeModel node) {
		Writer writer = new StringWriter();
		final Map<Class<? extends IExtension>, ? extends IExtension> exclusions = node.removeAll(ContentUpdateGenerator.getNodeContentExclusions());
		try {
			mapWriter.writeNodeAsXml(writer, node, Mode.ADDITIONAL_CONTENT, true, false, false);
		}
		catch (IOException e) {
			LogUtils.severe(e);
		}
		finally {
			node.addAll(exclusions);
		}
		return NodeContentUpdated.builder().nodeId(node.createID()).content(writer.toString()).build();
	}

	public MapUpdated createMapContentUpdatedEvent(MapModel map) {
		Writer writer = new StringWriter();
		NodeModel node = map.getRootNode();
		final Map<Class<? extends IExtension>, ? extends IExtension> exclusions = node.retainAll(MapExtensions.getAll());
		try {
			writer.append("<map>");
			mapWriter.writeNodeAsXml(writer, node, Mode.ADDITIONAL_CONTENT, true, false, false);
			writer.append("</map>");
		}
		catch (IOException e) {
			LogUtils.severe(e);
		}
		finally {
			node.addAll(exclusions);
		}
		return MapContentUpdated.builder().content(writer.toString()).build();
	}
	
	public MapUpdated createCoreUpdatedEvent(NodeModel node) {
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
