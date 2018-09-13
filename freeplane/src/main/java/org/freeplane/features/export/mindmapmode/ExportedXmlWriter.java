package org.freeplane.features.export.mindmapmode;

import org.freeplane.features.map.MapWriter;

import java.io.Writer;

public interface ExportedXmlWriter {
	void writeXml(Writer writer, MapWriter.Mode mode);
}
