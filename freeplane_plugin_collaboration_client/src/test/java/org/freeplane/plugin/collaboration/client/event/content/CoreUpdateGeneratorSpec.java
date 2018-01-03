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

import java.lang.reflect.InvocationTargetException;

import org.freeplane.core.resources.TranslatedObject;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.styles.StyleTranslatedObject;
import org.freeplane.plugin.collaboration.client.event.TestObjects;
import org.freeplane.plugin.collaboration.client.event.UpdatesEventCaptor;
import org.freeplane.plugin.collaboration.client.event.batch.Updates;
import org.freeplane.plugin.collaboration.client.event.children.AwtThreadStarter;
import org.freeplane.plugin.collaboration.client.event.batch.ModifiableUpdateHeaderExtension;
import org.freeplane.plugin.collaboration.client.event.batch.UpdateBlockCompleted;
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

	private ModifiableUpdateHeaderExtension header = ModifiableUpdateHeaderExtension.create().setMapId("mapId").setMapRevision(0);
	
	@Mock
	private ContentUpdateEventFactory eventFactory;
	
	final private TestObjects testObjects = new TestObjects();
	final private NodeModel node = testObjects.parent;
	private UpdatesEventCaptor consumer;

	
	private UpdateBlockCompleted updateBlock(CoreMediaType mediaType, String content) {
		CoreUpdated coreUpdated= CoreUpdated.builder() //
				.nodeId(node.getID()).mediaType(mediaType).content(content).build();
		UpdateBlockCompleted expected = UpdateBlockCompleted.builder()
				.mapId(header.mapId()).mapRevision(1)
				.addUpdateBlock(coreUpdated).build();
		return expected;
	}


	@BeforeClass
	static public void setupClass() throws InterruptedException, InvocationTargetException {
		AwtThreadStarter.await();
	}
	
	@Before
	public void createTestedInstance() {
		consumer = new UpdatesEventCaptor(1);
		Updates updates = new Updates(consumer, DELAY_MILLIS, header);
		uut = new CoreUpdateGenerator(updates);
	}


	@Test
	public void plainText() throws Exception {
		node.setUserObject("content");

		uut.onCoreUpdate(node);
		final UpdateBlockCompleted event = consumer.getEvent();

		UpdateBlockCompleted expected = updateBlock(CoreMediaType.PLAIN_TEXT, "content");

		assertThat(event).isEqualTo(expected);
		assertThat(header.mapRevision()).isEqualTo(1);
	}

	@Test
	public void translatedObject() throws Exception {
		node.setUserObject(new TranslatedObject("key", "value"));

		uut.onCoreUpdate(node);
		final UpdateBlockCompleted event = consumer.getEvent();
		
		UpdateBlockCompleted expected = updateBlock(CoreMediaType.LOCALIZED_TEXT, "key");

		assertThat(event).isEqualTo(expected);
		assertThat(header.mapRevision()).isEqualTo(1);
	}

	@Test
	public void html() throws Exception {
		node.setUserObject("<html>content</html>");

		uut.onCoreUpdate(node);
		final UpdateBlockCompleted event = consumer.getEvent();

		UpdateBlockCompleted expected = updateBlock(CoreMediaType.HTML, "<html>content</html>");

		assertThat(event).isEqualTo(expected);
		assertThat(header.mapRevision()).isEqualTo(1);
	}
	

	@Test
	public void object() throws Exception {
		node.setUserObject(Integer.valueOf(3));

		uut.onCoreUpdate(node);
		final UpdateBlockCompleted event = consumer.getEvent();

		UpdateBlockCompleted expected = updateBlock(CoreMediaType.OBJECT, "java.lang.Integer|3");

		assertThat(event).isEqualTo(expected);
		assertThat(header.mapRevision()).isEqualTo(1);
	}


}
