package org.freeplane.features.export.mindmapmode;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.MapWriter;
import org.freeplane.features.mode.Controller;

import java.io.IOException;
import java.io.Writer;

public class MapXmlWriter implements ExportedXmlWriter {
	private final MapModel map;

	public MapXmlWriter(MapModel map) {
		this.map = map;
	}

	@Override
	public void writeXml(Writer writer, MapWriter.Mode mode) {
		try {
			Controller.getCurrentModeController().getMapController().getFilteredXml(map, writer, mode, MapWriter.Mode.EXPORT.equals(mode));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
