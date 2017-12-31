package org.freeplane.features.map.mindmapmode;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.undo.IActor;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.MapWriter.Mode;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.features.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLException;

public class NodeContentManipulator {
	private final MMapController mapController;
	
	public NodeContentManipulator(MMapController mapController) {
		super();
		this.mapController = mapController;
	}
	
	public void updateNodeContent(NodeModel node, String newContent, Collection<Class<? extends IExtension>> exclusions) {
		try {
			final NodeModel newNode =  mapController.getMapReader().createNodeTreeFromXml(node.getMap(), 
					new StringReader(newContent), Mode.ADDITIONAL_CONTENT);
			final IActor actor = new IActor() {

				@Override
				public void undo() {
					act();
				}

				@Override
				public String getDescription() {
					return "updateContent";
				}

				@Override
				public void act() {
					NodeModel.swapUserObjects(node, newNode);				
					NodeModel.swapIcons(node, newNode);
					NodeModel.swapExtensionsExcluding(node, newNode, exclusions);
					mapController.nodeRefresh(node);
				}
			};
			mapController.getMModeController().execute(actor, node.getMap());

		} catch (IOException | XMLException e) {
			e.printStackTrace();
		}
	}

	public void updateMapContent(MapModel map, String newContent, Collection<Class<? extends IExtension>> extensions) {
		try {
			final NodeModel newNode =  mapController.getMapReader().createNodeTreeFromXml(map, 
					new StringReader(newContent), Mode.ADDITIONAL_CONTENT);
			final IActor actor = new IActor() {

				@Override
				public void undo() {
					act();
				}

				@Override
				public String getDescription() {
					return "updateContent";
				}

				@Override
				public void act() {
					NodeModel.swapExtensions(map.getRootNode(), newNode, extensions);
					mapController.fireMapChanged(
					    new MapChangeEvent(this, map, MapStyle.MAP_STYLES, null, null));
				}
			};
			mapController.getMModeController().execute(actor, map);

		} catch (IOException | XMLException e) {
			e.printStackTrace();
		}
	}
}
