package org.freeplane.plugin.collaboration.client;

import java.util.LinkedHashSet;

import javax.swing.Timer;

import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeDeletionEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeMoveEvent;
import org.freeplane.plugin.collaboration.client.ImmutableUpdatesCompleted.Builder;

public class UpdateEventGenerator implements IMapChangeListener, INodeChangeListener{
	
	final private UpdatesProcessor consumer;
	final private UpdateEventFactory eventFactory;
	final private LinkedHashSet<NodeModel> changedParents;

	final private Timer timer;
	
	public UpdateEventGenerator(UpdatesProcessor consumer, UpdateEventFactory eventFactory, int delay) {
		super();
		this.consumer = consumer;
		this.eventFactory = eventFactory;
		timer = new OneTimeTimer(delay);
		changedParents = new LinkedHashSet<>();
	}

	@Override
	public void nodeChanged(NodeChangeEvent event) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Method not implemented");
	}

	@Override
	public void mapChanged(MapChangeEvent event) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Method not implemented");
	}

	@Override
	public void onNodeDeleted(NodeDeletionEvent nodeDeletionEvent) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Method not implemented");
	}

	@Override
	public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {
		onChangedStructure(parent);
	}

	private void onChangedStructure(NodeModel parent) {
		if(changedParents.isEmpty())
			timer.addActionListener(e -> generateStructureChangedEvent());
		changedParents.add(parent);
		timer.restart();
	}

	private void generateStructureChangedEvent() {
		final Builder builder = builder();
		for(NodeModel parent : changedParents) {
			final ChildrenUpdated childrenUpdated = eventFactory.createChildrenUpdatedEvent(parent);
			builder.addUpdateEvents(childrenUpdated);
		}
		changedParents.clear();
		UpdatesCompleted event = builder.build();
		consumer.onUpdatesCompleted(event);
	}
	
	protected Builder builder() {
		return UpdatesCompleted.builder()
				.mapId("")
				.mapRevision(0L);
	}

	@Override
	public void onNodeMoved(NodeMoveEvent nodeMoveEvent) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Method not implemented");
	}

	@Override
	public void onPreNodeMoved(NodeMoveEvent nodeMoveEvent) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Method not implemented");
	}

	@Override
	public void onPreNodeDelete(NodeDeletionEvent nodeDeletionEvent) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Method not implemented");
	}

}
