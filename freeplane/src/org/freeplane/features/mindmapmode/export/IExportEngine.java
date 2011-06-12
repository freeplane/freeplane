package org.freeplane.features.mindmapmode.export;

import java.io.File;

import org.freeplane.features.map.MapModel;

public interface IExportEngine {
	public void export(MapModel map, File toFile);
}
