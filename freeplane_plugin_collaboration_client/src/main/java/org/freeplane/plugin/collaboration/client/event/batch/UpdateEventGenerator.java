package org.freeplane.plugin.collaboration.client.event.batch;

import java.util.WeakHashMap;

import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeDeletionEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeMoveEvent;
import org.freeplane.plugin.collaboration.client.event.children.UpdateEventFactory;

public class UpdateEventGenerator implements IMapChangeListener, INodeChangeListener{
	WeakHashMap<MapModel, MapUpdateTimer> timers = new WeakHashMap<>();
	private MapUpdateTimerFactory timerFactory;
	
	public UpdateEventGenerator(MapUpdateTimerFactory timerFactory) {
		super();
		this.timerFactory = timerFactory;
	}

	private MapUpdateTimer getTimer(MapModel map) {
		if(timers.containsKey(map))
			return timers.get(map);
		final MapUpdateTimer timer = this.timerFactory.create(map);
		timers.put(map, timer);
		return timer;
	}
	
	public UpdateEventGenerator(UpdatesProcessor consumer, UpdateEventFactory eventFactory, int delay) {
		this(new MapUpdateTimerFactory(consumer, eventFactory, delay));
	}

	@Override
	public void onNodeDeleted(NodeDeletionEvent nodeDeletionEvent) {
		onChangedStructure(nodeDeletionEvent.parent);	
	}

	@Override
	public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {
		final MapUpdateTimer timer = getTimer(parent.getMap());
		timer.onNodeInserted(parent, child);
	}

	private void onChangedStructure(NodeModel parent) {
		final MapUpdateTimer timer = getTimer(parent.getMap());
		timer.onChangedStructure(parent);
	}

	@Override
	public void onNodeMoved(NodeMoveEvent nodeMoveEvent) {
		final MapUpdateTimer oldMapTimer = getTimer(nodeMoveEvent.oldParent.getMap());
		oldMapTimer.onChangedStructure(nodeMoveEvent.oldParent);		
		final MapUpdateTimer newMapTimer = getTimer(nodeMoveEvent.newParent.getMap());
		newMapTimer.onChangedStructure(nodeMoveEvent.newParent);
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
