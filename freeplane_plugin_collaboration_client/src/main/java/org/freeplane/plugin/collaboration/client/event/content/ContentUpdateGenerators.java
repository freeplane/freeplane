package org.freeplane.plugin.collaboration.client.event.content;

import java.util.List;

import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;

public class ContentUpdateGenerators{

	private final List<MapUpdateGenerator> mapUpdateGenerators;
	private final List<NodeUpdateGenerator> nodeUpdateGenerators;

	public ContentUpdateGenerators(List<MapUpdateGenerator> mapUpdateGenerators, 
	                               List<NodeUpdateGenerator> nodeUpdateGenerators) {
		super();
		this.mapUpdateGenerators = mapUpdateGenerators;
		this.nodeUpdateGenerators = nodeUpdateGenerators;
	}

	public void onNodeContentUpdate(NodeChangeEvent event) {
		for(NodeUpdateGenerator g : nodeUpdateGenerators) {
			if(g.handles(event)) {
				g.onNodeChange(event);
				break;
			}
		}
	}

	public void onNewNode(NodeModel node) {
		for(NodeUpdateGenerator g : nodeUpdateGenerators) {
			g.onNewNode(node);
		}
	}
	
	public void onMapContentUpdate(MapChangeEvent event) {
		for(MapUpdateGenerator g : mapUpdateGenerators) {
			if(g.handles(event)) {
				g.onMapChange(event.getMap());
				break;
			}
		}
	}

	public void onNewMap(MapModel map) {
		for(MapUpdateGenerator g : mapUpdateGenerators) {
			g.onNewMap(map);
		}
	}
}

