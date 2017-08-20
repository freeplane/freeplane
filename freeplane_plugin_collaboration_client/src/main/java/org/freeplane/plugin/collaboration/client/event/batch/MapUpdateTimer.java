package org.freeplane.plugin.collaboration.client.event.batch;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

import javax.swing.Timer;
import javax.swing.event.EventListenerList;

import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.collaboration.client.event.children.ChildrenUpdated;
import org.freeplane.plugin.collaboration.client.event.children.SpecialNodeTypeSet;
import org.freeplane.plugin.collaboration.client.event.children.UpdateEventFactory;
import org.freeplane.plugin.collaboration.client.event.children.SpecialNodeTypeSet.SpecialNodeType;

@SuppressWarnings("serial") 
class MapUpdateTimer extends Timer {
	final private ModifiableUpdateHeaderExtension header;
	final private UpdatesProcessor consumer;
	final private UpdateEventFactory eventFactory;
	final private LinkedHashSet<NodeModel> changedParents;
	final private LinkedHashMap<NodeModel, SpecialNodeType> specialNodes;
	private ImmutableUpdatesFinished.Builder builder;

	MapUpdateTimer(UpdatesProcessor consumer, UpdateEventFactory eventFactory, int delay, ModifiableUpdateHeaderExtension header) {
		super(delay, null);
		setRepeats(false);
		this.consumer = consumer;
		this.eventFactory = eventFactory;
		this.header = header;
		changedParents = new LinkedHashSet<>();
		specialNodes = new LinkedHashMap<>();
	}

	@Override
	protected void fireActionPerformed(ActionEvent e) {		
		builder = createBuilder();
		notifyListeners(e);
		listenerList = new EventListenerList();
		UpdatesFinished event = builder.build();
		builder = null;
		header.setMapRevision(header.mapRevision() + 1);
		consumer.onUpdates(event);
	}
	
	private ImmutableUpdatesFinished.Builder createBuilder() {
		return UpdatesFinished.builder()
				.mapId(header.mapId())
				.mapRevision(header.mapRevision() + 1);
	}


	private void notifyListeners(ActionEvent e) {
        Object[] listeners = listenerList.getListenerList();
        for (int i=0; i<=listeners.length-2; i+=2) {
            if (listeners[i]==ActionListener.class) {
                ((ActionListener)listeners[i+1]).actionPerformed(e);
            }
        }
	}
	
	void onNodeInserted(NodeModel parent, NodeModel child) {
		onChangedStructure(parent);
		if(specialNodes.isEmpty())
			addActionListener(e -> generateSpecialNodeTypeSetEvent());
		SpecialNodeTypeSet.SpecialNodeType.of(child).ifPresent(t -> specialNodes.put(child, t));
	}

	void onChangedStructure(NodeModel parent) {
		if(changedParents.isEmpty())
			addActionListener(e -> generateStructureChangedEvent());
		changedParents.add(parent);
		restart();
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
	

}