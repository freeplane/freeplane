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
package org.freeplane.plugin.collaboration.client.event.content.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;

import org.freeplane.collaboration.event.MapUpdated;
import org.freeplane.collaboration.event.batch.ImmutableUpdateBlockCompleted;
import org.freeplane.collaboration.event.batch.ModifiableUpdateHeader;
import org.freeplane.collaboration.event.batch.UpdateBlockCompleted;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.collaboration.client.event.TestObjects;
import org.freeplane.plugin.collaboration.client.event.UpdatesEventCaptor;
import org.freeplane.plugin.collaboration.client.event.batch.UpdateBlockGeneratorFactory;
import org.freeplane.plugin.collaboration.client.event.batch.Updates;
import org.freeplane.plugin.collaboration.client.event.children.AwtThreadStarter;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Dimitry Polivaev
 * Jan 2, 2018
 */
@RunWith(MockitoJUnitRunner.class)
public class CoreUpdateGeneratorSpec {
	private CoreUpdateGenerator uut;
	private static final int DELAY_MILLIS = 10;
	private static final String USER_ID="userID";
	private ModifiableUpdateHeader header = ModifiableUpdateHeader.create().setMapId("mapId")
	    .setMapRevision(0);
	@Mock
	private CoreUpdateEventFactory eventFactory;
	@Mock
	private UpdateBlockGeneratorFactory updateBlockGeneratorFactory;
	final private TestObjects testObjects = new TestObjects();
	private MapModel map = testObjects.map;
	final private NodeModel node = testObjects.parent;
	private UpdatesEventCaptor consumer;

	private ImmutableUpdateBlockCompleted updateBlock(final MapUpdated event) {
		return UpdateBlockCompleted.builder()
			.userId(USER_ID)
		    .mapId(header.mapId()).mapRevision(1)
		    .addUpdateBlock(event).build();
	}

	@BeforeClass
	static public void setupClass() throws InterruptedException, InvocationTargetException {
		AwtThreadStarter.await();
	}

	@Before
	public void createTestedInstance() {
		consumer = new UpdatesEventCaptor(1);
		Updates updates = new Updates(USER_ID, consumer, DELAY_MILLIS, header);
		when(updateBlockGeneratorFactory.of(map)).thenReturn(updates);
		uut = new CoreUpdateGenerator(updateBlockGeneratorFactory, eventFactory);
	}

	@Test
	public void createsUpdateBlockOnNodeChange() throws Exception {
		final MapUpdated event = mock(MapUpdated.class);
		when(eventFactory.createCoreUpdatedEvent(node)).thenReturn(event);
		uut.onNodeChange(new NodeChangeEvent(node, NodeModel.UNKNOWN_PROPERTY, null, null, true));
		UpdateBlockCompleted expected = updateBlock(event);
		assertThat(consumer.getEvent()).isEqualTo(expected);
		assertThat(header.mapRevision()).isEqualTo(1);
	}

	@Test
	public void createsUpdateBlockOnNewNode() throws Exception {
		final MapUpdated event = mock(MapUpdated.class);
		when(eventFactory.createCoreUpdatedEvent(node)).thenReturn(event);
		uut.onNewNode(node);
		UpdateBlockCompleted expected = updateBlock(event);
		assertThat(consumer.getEvent()).isEqualTo(expected);
		assertThat(header.mapRevision()).isEqualTo(1);
	}
}
