package org.freeplane.features.export.mindmapmode;

import org.freeplane.features.map.MapWriter;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

class BranchXmlWriter implements ExportedXmlWriter {
	private final List<NodeModel> branches;

	public BranchXmlWriter(List<NodeModel> branches) {
		this.branches = branches;
	}

	@Override
	public void writeXml(Writer writer, MapWriter.Mode mode) {
		try {
			writer.append("<map>\n");
			Controller.getCurrentModeController().getMapController().getFilteredXml(branches, writer, mode, MapWriter.Mode.EXPORT.equals(mode));
			writer.append("</map>\n");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
