package org.freeplane.plugin.collaboration.client.event;

import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeDeletionEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeMoveEvent;
import org.freeplane.plugin.collaboration.client.event.batch.Updates;
import org.freeplane.plugin.collaboration.client.event.children.MapStructureEventGenerator;
import org.freeplane.plugin.collaboration.client.event.content.ContentUpdateGenerators;

public class UpdateEventGenerator implements IMapChangeListener, INodeChangeListener{
	final private MapStructureEventGenerator structureGenerator;
	final private ContentUpdateGenerators contentGenerators;
	

	public UpdateEventGenerator(MapStructureEventGenerator mapStructureEventGenerator,
	                            ContentUpdateGenerators contentGenerators) {
		super();
		this.structureGenerator = mapStructureEventGenerator;
		this.contentGenerators = contentGenerators;
	}

	@Override
	public void onNodeDeleted(NodeDeletionEvent nodeDeletionEvent) {
		final MapModel map = nodeDeletionEvent.node.getMap();
		if(map.containsExtension(Updates.class))
			structureGenerator.onNodeRemoved(nodeDeletionEvent.node);	
	}

	@Override
	public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {
		final MapModel map = parent.getMap();
		if(map.containsExtension(Updates.class))
			structureGenerator.onNodeInserted(child);
	}


	@Override
	public void onNodeMoved(NodeMoveEvent nodeMoveEvent) {
		final MapModel map = nodeMoveEvent.child.getMap();
		if(map.containsExtension(Updates.class))
			structureGenerator.onNodeMoved(nodeMoveEvent.child);
	}
	
	public void onNewMap(MapModel map) {
		if(map.containsExtension(Updates.class))
			structureGenerator.onNewMap(map);
	}

	@Override
	public void onPreNodeMoved(NodeMoveEvent nodeMoveEvent) {
		// continue
	}

	@Override
	public void onPreNodeDelete(NodeDeletionEvent nodeDeletionEvent) {
		// continue
	}

	@Override
	public void nodeChanged(NodeChangeEvent event) {
		final MapModel map = event.getNode().getMap();
		if(event.isPersistent() && map.containsExtension(Updates.class))
			contentGenerators.onNodeContentUpdate(event);
	}

	@Override
	public void mapChanged(MapChangeEvent event) {
		final MapModel map = event.getMap();
		if(map.containsExtension(Updates.class))
		contentGenerators.onMapContentUpdate(event);
	}
}
