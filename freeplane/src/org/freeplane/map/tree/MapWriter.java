/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.map.tree;

import java.io.IOException;
import java.io.Writer;

import org.freeplane.io.INodeWriter;
import org.freeplane.io.ITreeWriter;
import org.freeplane.io.WriteManager;
import org.freeplane.io.xml.TreeXmlWriter;

/**
 * @author Dimitry Polivaev
 * 07.12.2008
 */
public class MapWriter implements INodeWriter<String>{

	public MapWriter(WriteManager writeManager) {
	    this.writeManager = writeManager;
    }

	private NodeWriter currentNodeWriter;
	public boolean isSaveInvisible() {
    	return saveInvisible;
    }

	public void setSaveInvisible(boolean saveInvisible) {
    	this.saveInvisible = saveInvisible;
    }

	private boolean saveInvisible;
	final private WriteManager writeManager;

	public void writeContent(ITreeWriter writer, Object node, String tag)
            throws IOException {
		writer.addNodeContent("<!--To view this file, download free mind mapping software FreeMind from http://freemind.sourceforge.net -->\n");
		MapModel map = (MapModel) node;
		map.getRegistry().write(writer);
		final NodeModel rootNode = map.getRootNode();
		writeNode(writer, rootNode, saveInvisible, true);
    }
	public void writeNodeAsXml(final Writer writer, final NodeModel node,
	                           final boolean writeInvisible,
	                           final boolean writeChildren) throws IOException {
		final TreeXmlWriter xmlWriter = new TreeXmlWriter(writeManager,
		    writer);
		writeNode(xmlWriter, node, writeInvisible, writeChildren);
	}

	private void writeNode(final ITreeWriter xmlWriter, final NodeModel node,
                           final boolean writeInvisible,
                           final boolean writeChildren) throws IOException {
	    final NodeWriter oldNodeWriter = currentNodeWriter;
		if (oldNodeWriter != null) {
			writeManager.removeNodeWriter(NodeBuilder.XML_NODE, oldNodeWriter);
			writeManager.removeAttributeWriter(NodeBuilder.XML_NODE,
			    oldNodeWriter);
		}
		currentNodeWriter = new NodeWriter(node.getModeController().getMapController(), writeChildren,
		    writeInvisible, MapController.isSSaveOnlyIntrinsicallyNeededIds());
		try {
			writeManager.addNodeWriter(NodeBuilder.XML_NODE, currentNodeWriter);
			writeManager.addAttributeWriter(NodeBuilder.XML_NODE,
			    currentNodeWriter);
			xmlWriter.addNode(node, NodeBuilder.XML_NODE);
		}
		finally {
			writeManager.removeNodeWriter(NodeBuilder.XML_NODE,
			    currentNodeWriter);
			writeManager.removeAttributeWriter(NodeBuilder.XML_NODE,
			    currentNodeWriter);
			if (oldNodeWriter != null) {
				writeManager.addNodeWriter(NodeBuilder.XML_NODE, oldNodeWriter);
				writeManager.addAttributeWriter(NodeBuilder.XML_NODE,
				    oldNodeWriter);
			}
		}
    }
}
