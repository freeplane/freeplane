/**
 * author: Marcel Genzmehr
 * 14.12.2011
 */
package org.docear.plugin.features;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IElementWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.WriteManager;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.MapWriter;
import org.freeplane.features.map.NodeModel;

/**
 * 
 */
public class DocearMapWriter extends MapWriter {
	protected static final String USAGE_COMMENT = "<!--To view this file, download Docear - The Academic Literature Suite from http://www.docear.org -->"
			+ System.getProperty("line.separator");

	private final WriteManager writeManager;
	private List<IElementWriter> mapWriter = new ArrayList<IElementWriter>();
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	/**
	 * @param mapController
	 */

	public DocearMapWriter(MapController mapController) {
		super(mapController);
		writeManager = mapController.getWriteManager();
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	public void setMapWriteHandler() {		
		Iterator<IElementWriter> writerHandle = writeManager.getElementWriters().iterator("map");
		mapWriter.clear();
		while(writerHandle.hasNext()) {
			mapWriter.add(writerHandle.next());
		}
		for(IElementWriter writer : mapWriter) {
			writeManager.removeElementWriter("map", writer);
		}
		writeManager.addElementWriter("map", this);
	}
	
	
	public void resetMapWriteHandler() {
		writeManager.removeElementWriter("map", this);
		for(IElementWriter writer : mapWriter) {
			writeManager.addElementWriter("map", writer);
		}
		mapWriter.clear();
	}
	
	public void writeContent(final ITreeWriter writer, final Object node, final String tag) throws IOException {
		writer.addElementContent(USAGE_COMMENT);
		final MapModel map = (MapModel) node;
		writer.addExtensionNodes(map, Arrays.asList(map.getExtensions().values().toArray(new IExtension[] {})));
		final NodeModel rootNode = map.getRootNode();
		writeNode(writer, rootNode, isSaveInvisible(), true);
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
