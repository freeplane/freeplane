package org.freeplane.features.export.mindmapmode;

import java.io.File;
import java.util.List;

import org.freeplane.features.map.NodeModel;

public interface IExportEngine {
	public void export(List<NodeModel> nodes, File toFile);
}
