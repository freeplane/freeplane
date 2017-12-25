package org.freeplane.plugin.collaboration.client.event.content;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapWriter;
import org.freeplane.features.map.MapWriter.Mode;
import org.freeplane.features.map.NodeModel;

public class ContentUpdateEventFactory {
	final MapWriter mapWriter;
	public ContentUpdateEventFactory(MapWriter mapWriter) {
		super();
		this.mapWriter = mapWriter;
	}
	
	public ContentUpdated createContentUpdatedEvent(final NodeModel node) {
		Writer writer = new StringWriter();
		final Map<Class<? extends IExtension>, ? extends IExtension> exclusions = node.removeAll(ContentUpdated.EXCLUSIONS);
		try {
			mapWriter.writeNodeAsXml(writer, node, Mode.ADDITIONAL_CONTENT, true, false, false);
		}
		catch (IOException e) {
			LogUtils.severe(e);
		}
		finally {
			node.addAll(exclusions);
		}
		return ContentUpdated.builder().nodeId(node.createID()).content(writer.toString()).build();
	}
}
