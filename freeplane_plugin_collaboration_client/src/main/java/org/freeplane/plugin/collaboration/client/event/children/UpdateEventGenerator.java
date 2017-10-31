package org.freeplane.plugin.collaboration.client.event.children;

import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeDeletionEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeMoveEvent;
import org.freeplane.plugin.collaboration.client.event.batch.MapUpdateTimer;
import org.freeplane.plugin.collaboration.client.event.batch.MapUpdateTimerFactory;

public class UpdateEventGenerator implements IMapChangeListener, INodeChangeListener{
	private MapUpdateTimerFactory timerFactory;
	private ChildrenUpdateGeneratorFactory generatorFactory;
	
	public UpdateEventGenerator(MapUpdateTimerFactory timerFactory, ChildrenUpdateGeneratorFactory generatorFactory) {
		super();
		this.timerFactory = timerFactory;
		this.generatorFactory = generatorFactory;
	}

	private ChildrenUpdateGenerator getGenerator(MapModel map) {
		ChildrenUpdateGenerator generator = map.getExtension(ChildrenUpdateGenerator.class);
		if(generator == null) {
			final MapUpdateTimer timer = timerFactory.createTimer(map);
			generator = generatorFactory.create(map, timer);
			map.addExtension(generator);
		}
		return generator;
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
		// TODO Auto-generated method stub
	}

	@Override
	public void mapChanged(MapChangeEvent event) {
		// TODO Auto-generated method stub
	}
}
