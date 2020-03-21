package org.freeplane.features.export.mindmapmode;

import org.freeplane.features.map.MapWriter;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

class BranchXmlWriter{
	private final List<NodeModel> branches;

	 BranchXmlWriter(List<NodeModel> branches) {
		this.branches = branches;
	}

	void writeXml(Writer writer, MapWriter.Mode mode) {
		try {
		    writer.append("<!DOCTYPE mindmap [\n" + 
		            "    <!ENTITY nbsp \"&#160;\"> \n" + 
		            "]>\n");
			if(branches.size() == 1 && branches.get(0).isRoot()) {
				Controller.getCurrentModeController().getMapController().getFilteredXml(branches.get(0).getMap(), writer, mode, MapWriter.Mode.EXPORT.equals(mode));
			}
			else {
				writer.append("<map>\n");
				Controller.getCurrentModeController().getMapController().getFilteredXml(branches, writer, mode, MapWriter.Mode.EXPORT.equals(mode));
				writer.append("</map>\n");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
