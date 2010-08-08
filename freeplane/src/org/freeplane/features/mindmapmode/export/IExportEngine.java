package org.freeplane.features.mindmapmode.export;

import java.io.File;

import org.freeplane.features.common.map.MapModel;

public interface IExportEngine {
	public void export(MapModel map, File toFile);
}
