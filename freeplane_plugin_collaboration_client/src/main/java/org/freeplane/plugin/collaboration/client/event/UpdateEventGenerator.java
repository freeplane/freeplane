package org.freeplane.plugin.collaboration.client.event;

import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeDeletionEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeMoveEvent;
import org.freeplane.plugin.collaboration.client.event.children.ChildrenUpdateGenerator;
import org.freeplane.plugin.collaboration.client.event.children.ChildrenUpdateGenerators;
import org.freeplane.plugin.collaboration.client.event.content.ContentUpdateGenerator;
import org.freeplane.plugin.collaboration.client.event.content.ContentUpdateGenerators;

public class UpdateEventGenerator implements IMapChangeListener, INodeChangeListener{
	final private ChildrenUpdateGenerators structureGenerators;
	final private ContentUpdateGenerators contentGenerators;
	

	public UpdateEventGenerator(ChildrenUpdateGenerators structureGenerators,
	                            ContentUpdateGenerators contentGenerators) {
		super();
		this.structureGenerators = structureGenerators;
		this.contentGenerators = contentGenerators;
	}



	private ChildrenUpdateGenerator getGenerator(MapModel map) {
			return structureGenerators.of(map);
	}
	
	
	
	@Override
	public void onNodeDeleted(NodeDeletionEvent nodeDeletionEvent) {
		onChangedStructure(nodeDeletionEvent.parent);	
	}

	@Override
	public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {
		final ChildrenUpdateGenerator generator = getGenerator(parent.getMap());
		generator.onNodeInserted(parent, child);
	}

	private void onChangedStructure(NodeModel parent) {
		final ChildrenUpdateGenerator generator = getGenerator(parent.getMap());
		generator.onChangedStructure(parent);
	}

	@Override
	public void onNodeMoved(NodeMoveEvent nodeMoveEvent) {
		final ChildrenUpdateGenerator oldMapGenerator = getGenerator(nodeMoveEvent.oldParent.getMap());
		oldMapGenerator.onChangedStructure(nodeMoveEvent.oldParent);		
		final ChildrenUpdateGenerator newMapGenerator = getGenerator(nodeMoveEvent.newParent.getMap());
		newMapGenerator.onChangedStructure(nodeMoveEvent.newParent);
	}
	
	public void onNewMap(MapModel map) {
		getGenerator(map).onNewMap(map);
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
		NodeModel node = event.getNode();
		final ContentUpdateGenerator generator = contentGenerators.of(node.getMap());
		generator.onNodeContentUpdate(node);
	}

	@Override
	public void mapChanged(MapChangeEvent event) {
		MapModel map = event.getMap();
		final ContentUpdateGenerator generator = contentGenerators.of(map);
		generator.onNodeContentUpdate(map.getRootNode());
	}
}
