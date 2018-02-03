package org.freeplane.plugin.collaboration.client.event;

import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeDeletionEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeMoveEvent;
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
		structureGenerator.onNodeRemoved(nodeDeletionEvent.node);	
	}

	@Override
	public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {
		structureGenerator.onNodeInserted(child);
	}


	@Override
	public void onNodeMoved(NodeMoveEvent nodeMoveEvent) {
		structureGenerator.onNodeMoved(nodeMoveEvent.child);
	}
	
	public void onNewMap(MapModel map) {
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
		if(event.isPersistent())
			contentGenerators.onNodeContentUpdate(event);
	}

	@Override
	public void mapChanged(MapChangeEvent event) {
		contentGenerators.onMapContentUpdate(event);
	}
}
