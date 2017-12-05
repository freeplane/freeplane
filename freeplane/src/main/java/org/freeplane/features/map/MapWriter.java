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
package org.freeplane.features.map;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IAttributeWriter;
import org.freeplane.core.io.IElementWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.io.xml.TreeXmlWriter;
import org.freeplane.core.util.FreeplaneVersion;
import org.freeplane.features.link.LinkBuilder;
import org.freeplane.features.link.LinkController;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * 07.12.2008
 */
public class MapWriter implements IElementWriter, IAttributeWriter {

	public enum WriterHint {
		FORCE_FORMATTING, ALREADY_WRITTEN
	}
	private static final String USAGE_COMMENT = "<!--To view this file,"
	        + " download free mind mapping software Freeplane from http://freeplane.sourceforge.net -->"
	        + System.getProperty("line.separator");

	public enum Hint {
		MODE
	};

	public enum Mode {
		CLIPBOARD, FILE, EXPORT, STYLE, ADDITIONAL_CONTENT
	};

	private NodeWriter currentNodeWriter;
	final private MapController mapController;
	private boolean saveInvisible;
	final WriteManager writeManager;

	public MapWriter(final MapController mapController) {
		this.mapController = mapController;
		writeManager = mapController.getWriteManager();
	}

	public boolean isSaveInvisible() {
		return saveInvisible;
	}

	public void setSaveInvisible(final boolean saveInvisible) {
		this.saveInvisible = saveInvisible;
	}

	public void writeAttributes(final ITreeWriter writer, final Object userObject, final String tag) {
		final MapModel map = (MapModel) userObject;
		writer.addAttribute("version", FreeplaneVersion.XML_VERSION);
		writer.addExtensionAttributes(map, Arrays.asList(map.getExtensions().values().toArray(new IExtension[] {})));
	}

	public void writeContent(final ITreeWriter writer, final Object node, final String tag) throws IOException {
		writer.addElementContent(USAGE_COMMENT);
		final MapModel map = (MapModel) node;
		writer.addExtensionNodes(map, Arrays.asList(map.getExtensions().values().toArray(new IExtension[] {})));
		final NodeModel rootNode = map.getRootNode();
		writeNode(writer, rootNode, saveInvisible, true);
	}

	public void writeMapAsXml(final MapModel map, final Writer fileout, final Mode mode, final boolean saveInvisible,
	                          final boolean forceFormat) throws IOException {
		final TreeXmlWriter xmlWriter = new TreeXmlWriter(writeManager, fileout);
		xmlWriter.setHint(Hint.MODE, mode);
		if (forceFormat) {
			xmlWriter.setHint(WriterHint.FORCE_FORMATTING);
		}
		final XMLElement xmlMap = new XMLElement("map");
		setSaveInvisible(saveInvisible);
		xmlWriter.addElement(map, xmlMap);
		xmlWriter.flush();
		fileout.close();
	}

	private void writeNode(final ITreeWriter xmlWriter, final NodeModel node, final boolean writeInvisible,
	                       final boolean writeChildren) throws IOException {
		final NodeWriter oldNodeWriter = currentNodeWriter;
		final Object mode = xmlWriter.getHint(Hint.MODE);
		final String nodeTag;
		if (Mode.STYLE.equals(mode)) {
			nodeTag = NodeBuilder.XML_STYLENODE;
		}
		else {
			nodeTag = NodeBuilder.XML_NODE;
		}
		if (oldNodeWriter != null)
			oldNodeWriter.unregisterFrom(writeManager);
		LinkBuilder currentLinkBuilder = new LinkBuilder(mapController.getModeController().getExtension(LinkController.class));
		currentNodeWriter = new NodeWriter(mapController, currentLinkBuilder, nodeTag, writeChildren, writeInvisible);
		try {
			currentNodeWriter.registerBy(writeManager);
			xmlWriter.addElement(node, nodeTag);
		}
		finally {
			
			currentNodeWriter.unregisterFrom(writeManager);
				if (oldNodeWriter != null)
				oldNodeWriter.registerBy(writeManager);
			currentNodeWriter = oldNodeWriter;
		}
	}

	public void writeNodeAsXml(final Writer writer, final NodeModel node, final Mode mode,
	                           final boolean writeInvisible, final boolean writeChildren, boolean forceFormat) throws IOException {
		final TreeXmlWriter xmlWriter = new TreeXmlWriter(writeManager, writer);
		xmlWriter.setHint(Hint.MODE, mode);
		if (forceFormat) {
			xmlWriter.setHint(WriterHint.FORCE_FORMATTING);
		}
		writeNode(xmlWriter, node, writeInvisible, writeChildren);
		xmlWriter.flush();
	}
}

