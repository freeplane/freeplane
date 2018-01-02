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
package org.freeplane.plugin.collaboration.client.event.content;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.collaboration.client.event.TestObjects;
import org.freeplane.plugin.collaboration.client.event.UpdatesEventCaptor;
import org.freeplane.plugin.collaboration.client.event.batch.Updates;
import org.freeplane.plugin.collaboration.client.event.batch.ModifiableUpdateHeaderExtension;
import org.freeplane.plugin.collaboration.client.event.batch.UpdateBlockCompleted;
import org.junit.Before;
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
	
	
	private static final int TIMEOUT = 100;

	private static final int DELAY_MILLIS = 10;

	private ModifiableUpdateHeaderExtension header = ModifiableUpdateHeaderExtension.create().setMapId("mapId").setMapRevision(0);
	
	@Mock
	private ContentUpdateEventFactory eventFactory;
	
	final private TestObjects testObjects = new TestObjects();
	final private NodeModel node = testObjects.parent;
	private UpdatesEventCaptor consumer;

	
	@Before
	public void createTestedInstance() {
		consumer = new UpdatesEventCaptor(1);
		Updates updates = new Updates(consumer, DELAY_MILLIS, header);
		uut = new CoreUpdateGenerator(updates);
	}


	@Test
	public void testName() throws Exception {
		node.setID("id");
		node.setText("content");

		uut.onCoreUpdate(node);

		CoreUpdated coreUpdated= CoreUpdated.builder() //
				.nodeId("id").mediaType(CoreMediaType.PLAIN_TEXT).content("content").build();
		final UpdateBlockCompleted event = consumer.getEvent(TIMEOUT, TimeUnit.MILLISECONDS);
		UpdateBlockCompleted expected = UpdateBlockCompleted.builder()
				.mapId(header.mapId()).mapRevision(1)
				.addUpdateBlock(coreUpdated).build();
		
		assertThat(event).isEqualTo(expected);
		assertThat(header.mapRevision()).isEqualTo(1);
}
}
