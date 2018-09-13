package org.freeplane.features.export.mindmapmode;

import java.io.File;

import org.freeplane.features.map.MapModel;

public interface IExportEngine {
	public void export(MapModel map, ExportedXmlWriter xmlWriter, File toFile);
}
