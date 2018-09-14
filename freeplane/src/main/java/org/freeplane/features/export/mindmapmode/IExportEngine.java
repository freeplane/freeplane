package org.freeplane.features.export.mindmapmode;

import org.freeplane.features.map.NodeModel;

import java.io.File;
import java.util.List;

public interface IExportEngine {
	public void export(List<NodeModel> nodes, File toFile);
}
