package org.freeplane.plugin.collaboration.client.event.batch;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

import javax.swing.Timer;
import javax.swing.event.EventListenerList;

import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeDeletionEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeMoveEvent;
import org.freeplane.plugin.collaboration.client.event.children.ChildrenUpdated;
import org.freeplane.plugin.collaboration.client.event.children.SpecialNodeTypeSet;
import org.freeplane.plugin.collaboration.client.event.children.SpecialNodeTypeSet.SpecialNodeType;
import org.freeplane.plugin.collaboration.client.event.children.UpdateEventFactory;

public class UpdateEventGenerator implements IMapChangeListener, INodeChangeListener{
	@SuppressWarnings("serial") 
	class OneTimeTimer extends Timer {

		OneTimeTimer(int delay) {
			super(delay, null);
			setRepeats(false);
		}

		@Override
		protected void fireActionPerformed(ActionEvent e) {		
			builder = createBuilder();
			notifyListeners(e);
			listenerList = new EventListenerList();
			UpdatesFinished event = builder.build();
			builder = null;
			consumer.onUpdates(event);
		}
		
		private ImmutableUpdatesFinished.Builder createBuilder() {
			return UpdatesFinished.builder()
					.mapId("")
					.mapRevision(0L);
		}


		private void notifyListeners(ActionEvent e) {
	        Object[] listeners = listenerList.getListenerList();

	        for (int i=0; i<=listeners.length-2; i+=2) {
	            if (listeners[i]==ActionListener.class) {
	                ((ActionListener)listeners[i+1]).actionPerformed(e);
	            }
	        }

		}
	}
	
	final private UpdatesProcessor consumer;
	final private UpdateEventFactory eventFactory;
	final private LinkedHashSet<NodeModel> changedParents;
	final private LinkedHashMap<NodeModel, SpecialNodeType> specialNodes;
	private ImmutableUpdatesFinished.Builder builder;

	final private Timer timer;
	
	public UpdateEventGenerator(UpdatesProcessor consumer, UpdateEventFactory eventFactory, int delay) {
		super();
		this.consumer = consumer;
		this.eventFactory = eventFactory;
		timer = new OneTimeTimer(delay);
		changedParents = new LinkedHashSet<>();
		specialNodes = new LinkedHashMap<>();
	}

	@Override
	public void onNodeDeleted(NodeDeletionEvent nodeDeletionEvent) {
		onChangedStructure(nodeDeletionEvent.parent);	
	}

	@Override
	public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {
		onChangedStructure(parent);
		if(specialNodes.isEmpty())
			timer.addActionListener(e -> generateSpecialNodeTypeSetEvent());
		SpecialNodeTypeSet.SpecialNodeType.of(child).ifPresent(t -> specialNodes.put(child, t));
	}

	private void onChangedStructure(NodeModel parent) {
		if(changedParents.isEmpty())
			timer.addActionListener(e -> generateStructureChangedEvent());
		changedParents.add(parent);
		timer.restart();
	}

	private void generateSpecialNodeTypeSetEvent() {
		for( Entry<NodeModel, SpecialNodeType> e : specialNodes.entrySet()) {
			builder.addUpdateEvents(SpecialNodeTypeSet.builder()
					.nodeId(e.getKey().createID())
					.content(e.getValue()).build());
		}
		specialNodes.clear();
	}
	
	private void generateStructureChangedEvent() {
		for(NodeModel parent : changedParents) {
			final ChildrenUpdated childrenUpdated = eventFactory.createChildrenUpdatedEvent(parent);
			builder.addUpdateEvents(childrenUpdated);
		}
		changedParents.clear();
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
