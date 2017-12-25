package org.freeplane.features.map.mindmapmode;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;

import org.freeplane.features.icon.AccumulatedIcons;
import org.freeplane.features.map.MapWriter.Mode;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.undo.IActor;
import org.freeplane.features.map.FirstGroupNodeFlag;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.SummaryNodeFlag;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.n3.nanoxml.XMLException;

public class NodeContentManipulator {
	private final MMapController mapController;
	
	public NodeContentManipulator(MMapController mapController) {
		super();
		this.mapController = mapController;
	}
	
	public void updateContent(NodeModel node, String newContent, Collection<Class<? extends IExtension>> exclusions) {
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
					NodeModel.swapExtensions(node, newNode, exclusions);
					mapController.nodeRefresh(node);
				}
			};
			mapController.getMModeController().execute(actor, node.getMap());

		} catch (IOException | XMLException e) {
			e.printStackTrace();
		}
	}

}
