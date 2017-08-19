package org.freeplane.plugin.collaboration.client.event.batch;

import java.util.LinkedHashSet;

import javax.swing.Timer;

import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeDeletionEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeMoveEvent;
import org.freeplane.plugin.collaboration.client.event.ImmutableUpdatesFinished;
import org.freeplane.plugin.collaboration.client.event.children.ChildrenUpdated;
import org.freeplane.plugin.collaboration.client.event.children.UpdateEventFactory;

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
	public void onNodeDeleted(NodeDeletionEvent nodeDeletionEvent) {
		onChangedStructure(nodeDeletionEvent.parent);	
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
		final ImmutableUpdatesFinished.Builder builder = builder();
		for(NodeModel parent : changedParents) {
			final ChildrenUpdated childrenUpdated = eventFactory.createChildrenUpdatedEvent(parent);
			builder.addUpdateEvents(childrenUpdated);
		}
		changedParents.clear();
		UpdatesFinished event = builder.build();
		consumer.onUpdates(event);
	}
	
	protected ImmutableUpdatesFinished.Builder builder() {
		return UpdatesFinished.builder()
				.mapId("")
				.mapRevision(0L);
	}

	@Override
	public void onNodeMoved(NodeMoveEvent nodeMoveEvent) {
		onChangedStructure(nodeMoveEvent.oldParent);		
		onChangedStructure(nodeMoveEvent.newParent);
	}

	@Override
	public void onPreNodeMoved(NodeMoveEvent nodeMoveEvent) {
	}

	@Override
	public void onPreNodeDelete(NodeDeletionEvent nodeDeletionEvent) {
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
