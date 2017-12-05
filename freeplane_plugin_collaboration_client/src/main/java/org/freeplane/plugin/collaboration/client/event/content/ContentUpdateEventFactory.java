package org.freeplane.plugin.collaboration.client.event.content;

import org.freeplane.features.map.NodeModel;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapWriter.Mode;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

public class ContentUpdateEventFactory {
	public ContentUpdated createContentUpdatedEvent(final NodeModel node) {
		ModeController modeController = Controller.getCurrentModeController();
		Writer writer = new StringWriter();
		try {
			modeController.getMapController().getMapWriter()
			    .writeNodeAsXml(writer, node, Mode.ADDITIONAL_CONTENT, true, false, false);
		}
		catch (IOException e) {
			LogUtils.severe(e);
		}

		return ContentUpdated.builder().content(writer.toString()).build();
	}
}
