/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2018 dimitry
 *
 *  This file author is dimitry
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.plugin.collaboration.client.event.children;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;

import org.freeplane.collaboration.event.batch.ModifiableUpdateHeader;
import org.freeplane.collaboration.event.batch.UpdateBlockCompleted;
import org.freeplane.collaboration.event.children.NodeInserted;
import org.freeplane.collaboration.event.children.NodeMoved;
import org.freeplane.collaboration.event.children.NodeRemoved;
import org.freeplane.collaboration.event.children.RootNodeIdUpdated;
import org.freeplane.collaboration.event.children.SpecialNodeTypeSet;
import org.freeplane.collaboration.event.children.SpecialNodeTypeSet.SpecialNodeType;
import org.freeplane.features.map.FirstGroupNodeFlag;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.SummaryNodeFlag;
import org.freeplane.plugin.collaboration.client.event.TestObjects;
import org.freeplane.plugin.collaboration.client.event.UpdatesEventCaptor;
import org.freeplane.plugin.collaboration.client.event.batch.UpdateBlockGeneratorFactory;
import org.freeplane.plugin.collaboration.client.event.batch.Updates;
import org.freeplane.plugin.collaboration.client.event.content.ContentUpdateGenerators;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Dimitry Polivaev
 * Jan 6, 2018
 */
@RunWith(MockitoJUnitRunner.class)
public class MapStructureEventGeneratorSpec {
	private static final int DELAY_MILLIS = 10;
	private static final String USER_ID = "userId";
	@Mock
	private MapStructureEventFactory structuralEventFactory;
	@Mock
	private ContentUpdateGenerators contentUpdateGenerators;
	@Mock
	private UpdateBlockGeneratorFactory updateBlockGeneratorFactory;
	private ModifiableUpdateHeader header = ModifiableUpdateHeader.create().setMapId("mapId")
	    .setMapRevision(0);
	private UpdatesEventCaptor consumer;
	private MapStructureEventGenerator uut;
	final private TestObjects testObjects = new TestObjects();
	final private MapModel map = testObjects.map;
	final private NodeModel parent = testObjects.parent;
	final private NodeModel child = testObjects.child;
	final private NodeModel child2 = testObjects.child2;
	@Mock
	private NodeInserted childInserted;
	@Mock
	private NodeInserted child2Inserted;
	@Mock
	private NodeMoved childMoved;
	@Mock
	private NodeRemoved childRemoved;
	@Mock
	private SpecialNodeTypeSet specialTypeSet;
	@Mock
	private RootNodeIdUpdated rootNodeSet;

	@BeforeClass
	static public void setupClass() throws InterruptedException, InvocationTargetException {
		AwtThreadStarter.await();
	}

	@Before
	public void setup() {
		createTestedInstance(1);
	}

	private void createTestedInstance(final int expectedEventCount) {
		consumer = new UpdatesEventCaptor(expectedEventCount);
		Updates updates = new Updates(USER_ID, consumer, DELAY_MILLIS, header);
		when(updateBlockGeneratorFactory.of(map)).thenReturn(updates);
		when(updateBlockGeneratorFactory.of(Mockito.any(NodeModel.class))).thenReturn(updates);
		uut = new MapStructureEventGenerator(updateBlockGeneratorFactory, contentUpdateGenerators,
		    structuralEventFactory);
	}

	@Test
	public void generatesEventOnNodeInsertion() throws Exception {
		when(structuralEventFactory.createNodeInsertedEvent(child)).thenReturn(childInserted);
		uut.onNodeInserted(child);
		final UpdateBlockCompleted event = consumer.getEvent();
		UpdateBlockCompleted expected = UpdateBlockCompleted.builder()
			.userId(USER_ID)
		    .mapId(header.mapId()).mapRevision(1)
		    .addUpdateBlock(childInserted).build();
		assertThat(event).isEqualTo(expected);
	}

	@Test
	public void generatesSpecialNodeTypeEventOnNodeInsertion() throws Exception {
		child.addExtension(SummaryNodeFlag.SUMMARY);
		when(structuralEventFactory.createNodeInsertedEvent(child)).thenReturn(childInserted);
		when(structuralEventFactory.createSpecialNodeTypeSetEvent(child, SpecialNodeType.SUMMARY_END))
		    .thenReturn(specialTypeSet);
		uut.onNodeInserted(child);
		final UpdateBlockCompleted event = consumer.getEvent();
		UpdateBlockCompleted expected = UpdateBlockCompleted.builder()
			.userId(USER_ID)
		    .mapId(header.mapId()).mapRevision(1)
		    .addUpdateBlock(childInserted, specialTypeSet).build();
		assertThat(event).isEqualTo(expected);
	}

	@Test
	public void generatesEventsForInsertedChildOnNodeInsertion() throws Exception {
		parent.insert(child);
		child.insert(child2);
		when(structuralEventFactory.createNodeInsertedEvent(child)).thenReturn(childInserted);
		when(structuralEventFactory.createNodeInsertedEvent(child2)).thenReturn(child2Inserted);
		uut.onNodeInserted(child);
		final UpdateBlockCompleted event = consumer.getEvent();
		UpdateBlockCompleted expected = UpdateBlockCompleted.builder()
			.userId(USER_ID)
		    .mapId(header.mapId()).mapRevision(1)
		    .addUpdateBlock(childInserted,
		        child2Inserted)
		    .build();
		assertThat(event).isEqualTo(expected);
	}

	@Test
	public void generatesContentEventsForInsertedChildOnNodeInsertion() throws Exception {
		parent.insert(child);
		child.insert(child2);
		when(structuralEventFactory.createNodeInsertedEvent(child)).thenReturn(childInserted);
		when(structuralEventFactory.createNodeInsertedEvent(child2)).thenReturn(child2Inserted);
		parent.insert(child);
		uut.onNodeInserted(child);
		consumer.getEvent();
		verify(contentUpdateGenerators).onNewNode(child);
		verify(contentUpdateGenerators).onNewNode(child2);
	}

	@Test
	public void generatesSummaryBeginEventsForInsertedChildOnNodeInsertion() throws Exception {
		parent.insert(child);
		child.insert(child2);
		child2.addExtension(FirstGroupNodeFlag.FIRST_GROUP);
		when(structuralEventFactory.createNodeInsertedEvent(child)).thenReturn(childInserted);
		when(structuralEventFactory.createNodeInsertedEvent(child2)).thenReturn(child2Inserted);
		when(structuralEventFactory.createSpecialNodeTypeSetEvent(child2, SpecialNodeType.SUMMARY_BEGIN))
		    .thenReturn(specialTypeSet);
		uut.onNodeInserted(child);
		final UpdateBlockCompleted event = consumer.getEvent();
		UpdateBlockCompleted expected = UpdateBlockCompleted.builder()
			.userId(USER_ID)
		    .mapId(header.mapId()).mapRevision(1)
		    .addUpdateBlock(childInserted, child2Inserted, specialTypeSet).build();
		assertThat(event).isEqualTo(expected);
	}

	@Test
	public void generatesSummaryEndEventsForInsertedChildOnNodeInsertion() throws Exception {
		parent.insert(child);
		child.insert(child2);
		child2.addExtension(SummaryNodeFlag.SUMMARY);
		when(structuralEventFactory.createNodeInsertedEvent(child)).thenReturn(childInserted);
		when(structuralEventFactory.createNodeInsertedEvent(child2)).thenReturn(child2Inserted);
		when(structuralEventFactory.createSpecialNodeTypeSetEvent(child2, SpecialNodeType.SUMMARY_END))
		    .thenReturn(specialTypeSet);
		uut.onNodeInserted(child);
		final UpdateBlockCompleted event = consumer.getEvent();
		UpdateBlockCompleted expected = UpdateBlockCompleted.builder()
			.userId(USER_ID)
		    .mapId(header.mapId()).mapRevision(1)
		    .addUpdateBlock(childInserted, child2Inserted, specialTypeSet).build();
		assertThat(event).isEqualTo(expected);
	}

	@Test
	public void generatesSummaryBeginEndEndEventsForInsertedChildOnNodeInsertion() throws Exception {
		parent.insert(child);
		child.insert(child2);
		child2.addExtension(FirstGroupNodeFlag.FIRST_GROUP);
		child2.addExtension(SummaryNodeFlag.SUMMARY);
		when(structuralEventFactory.createNodeInsertedEvent(child)).thenReturn(childInserted);
		when(structuralEventFactory.createNodeInsertedEvent(child2)).thenReturn(child2Inserted);
		when(structuralEventFactory.createSpecialNodeTypeSetEvent(child2, SpecialNodeType.SUMMARY_BEGIN_END))
		    .thenReturn(specialTypeSet);
		uut.onNodeInserted(child);
		final UpdateBlockCompleted event = consumer.getEvent();
		UpdateBlockCompleted expected = UpdateBlockCompleted.builder()
			.userId(USER_ID)
		    .mapId(header.mapId()).mapRevision(1)
		    .addUpdateBlock(childInserted, child2Inserted, specialTypeSet).build();
		assertThat(event).isEqualTo(expected);
	}

	@Test
	public void generatesEventOnNewMap() throws Exception {
		parent.insert(child);
		when(map.getRootNode()).thenReturn(parent);
		when(structuralEventFactory.createNodeInsertedEvent(child)).thenReturn(childInserted);
		when(structuralEventFactory.createRootNodeIdUpdatedEvent(map)).thenReturn(rootNodeSet);
		uut.onNewMap(map);
		final UpdateBlockCompleted event = consumer.getEvent();
		UpdateBlockCompleted expected = UpdateBlockCompleted.builder()
			.userId(USER_ID)
		    .mapId(header.mapId()).mapRevision(1)
		    .addUpdateBlock(rootNodeSet, childInserted).build();
		assertThat(event).isEqualTo(expected);
	}

	@Test
	public void generatesContentEventsOnNewMap() throws Exception {
		parent.insert(child);
		when(map.getRootNode()).thenReturn(parent);
		when(structuralEventFactory.createNodeInsertedEvent(child)).thenReturn(childInserted);
		when(structuralEventFactory.createRootNodeIdUpdatedEvent(map)).thenReturn(rootNodeSet);
		uut.onNewMap(map);
		consumer.getEvent();
		verify(contentUpdateGenerators).onNewMap(map);
		verify(contentUpdateGenerators).onNewNode(parent);
		verify(contentUpdateGenerators).onNewNode(child);
	}

	@Test
	public void generatesEventOnNodeMovement() throws Exception {
		when(structuralEventFactory.createNodeMovedEvent(child)).thenReturn(childMoved);
		uut.onNodeMoved(child);
		final UpdateBlockCompleted event = consumer.getEvent();
		UpdateBlockCompleted expected = UpdateBlockCompleted.builder()
			.userId(USER_ID)
		    .mapId(header.mapId()).mapRevision(1)
		    .addUpdateBlock(childMoved).build();
		assertThat(event).isEqualTo(expected);
	}

	@Test
	public void generatesEventOnNodeRemoval() throws Exception {
		when(structuralEventFactory.createNodeRemovedEvent(child)).thenReturn(childRemoved);
		uut.onNodeRemoved(child);
		final UpdateBlockCompleted event = consumer.getEvent();
		UpdateBlockCompleted expected = UpdateBlockCompleted.builder()
			.userId(USER_ID)
		    .mapId(header.mapId()).mapRevision(1)
		    .addUpdateBlock(childRemoved).build();
		assertThat(event).isEqualTo(expected);
	}
}
