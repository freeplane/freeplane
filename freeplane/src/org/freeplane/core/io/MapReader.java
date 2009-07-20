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
package org.freeplane.core.io;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;

import org.freeplane.core.io.MapWriter.Hint;
import org.freeplane.core.io.MapWriter.Mode;
import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.n3.nanoxml.XMLException;

/**
 * @author Dimitry Polivaev
 * 20.12.2008
 */
public class MapReader implements IElementDOMHandler, IHintProvider {
	public class NodeTreeCreator {
		public NodeTreeCreator() {
			super();
			hints = new HashMap<Object, Object>();
		}

		public NodeModel create(final Reader pReader) throws XMLException {
			final TreeXmlReader reader = new TreeXmlReader(readManager);
			try {
				reader.load(pReader);
				final NodeModel node = nodeBuilder.getMapChild();
				return node;
			}
			finally {
				nodeBuilder.reset();
			}
		}

		public NodeModel createNodeTreeFromXml(final MapModel map, final Reader pReader) throws IOException,
		        XMLException {
			start(map);
			final NodeModel node = create(pReader);
			finish(node);
			return node;
		}

		public void finish(final NodeModel node) {
			final HashMap<String, String> newIds = nodeBuilder.getNewIds();
			readManager.readingCompleted(node, newIds);
			newIds.clear();
			createdMap = null;
		}

		void start(final MapModel map) {
			createdMap = map;
		}
	}

	private MapModel createdMap;
	private HashMap<Object, Object> hints;
	private boolean mapLoadingInProcess;
	private final NodeBuilder nodeBuilder;
	final private ReadManager readManager;

	public MapReader(final ReadManager readManager) {
		this.readManager = readManager;
		nodeBuilder = new NodeBuilder(this);
		nodeBuilder.registerBy(readManager);
	}

	public Object createElement(final Object parent, final String tag, final XMLElement attributes) {
		return getCreatedMap();
	}

	public NodeModel createNodeTreeFromXml(final MapModel map, final Reader pReader, final Mode mode)
	        throws IOException, XMLException {
		try {
			mapLoadingInProcess = true;
			final NodeTreeCreator nodeTreeCreator = new NodeTreeCreator();
			setHint(Hint.MODE, mode);
			final NodeModel topNode = nodeTreeCreator.createNodeTreeFromXml(map, pReader);
			mapLoadingInProcess = false;
			return topNode;
		}
		finally {
			mapLoadingInProcess = false;
		}
	}

	public void endElement(final Object parent, final String tag, final Object element, final XMLElement dom) {
		final MapModel map = (MapModel) element;
		if (dom.getAttributeCount() != 0 || dom.hasChildren()) {
			map.addExtension(new UnknownElements(dom));
		}
	}

	public MapModel getCreatedMap() {
		return createdMap;
	}

	public Object getHint(final Object key) {
		return hints.get(key);
	}

	public boolean isMapLoadingInProcess() {
		return mapLoadingInProcess;
	}

	public NodeTreeCreator nodeTreeCreator(final MapModel map) {
		final NodeTreeCreator nodeTreeCreator = new NodeTreeCreator();
		nodeTreeCreator.start(map);
		return nodeTreeCreator;
	}

	public void setHint(final Object key, final Object value) {
		hints.put(key, value);
	}
}
