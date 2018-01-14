/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2017 dimitry
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

import org.freeplane.collaboration.event.MapUpdated;
import org.freeplane.collaboration.event.content.core.CoreMediaType;
import org.freeplane.collaboration.event.content.core.CoreUpdated;
import org.freeplane.core.resources.TranslatedObject;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.MapWriter;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.collaboration.client.event.TestObjects;
import org.freeplane.plugin.collaboration.client.event.content.core.CoreUpdateEventFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Dimitry Polivaev
 * Dec 4, 2017
 */
@RunWith(MockitoJUnitRunner.class)
public class CoreUpdateEventFactorySpec {
	@Mock
	private MapWriter mapWriter;
	@InjectMocks
	private CoreUpdateEventFactory uut;

	TestObjects testObjects = new TestObjects();
	MapModel map = testObjects.map;
	NodeModel node = testObjects.parent;

	
	private CoreUpdated event(CoreMediaType mediaType, String content) {
		CoreUpdated coreUpdated= CoreUpdated.builder() //
				.nodeId(node.getID()).mediaType(mediaType).content(content).build();
		return coreUpdated;
	}


	@Test
	public void plainText() throws Exception {
		node.setUserObject("content");
		
		final MapUpdated event = uut.createCoreUpdatedEvent(node);

		MapUpdated expected = event(CoreMediaType.PLAIN_TEXT, "content");

		assertThat(event).isEqualTo(expected);
	}

	@Test
	public void translatedObject() throws Exception {
		node.setUserObject(new TranslatedObject("key", "value"));

		final MapUpdated event = uut.createCoreUpdatedEvent(node);
		
		MapUpdated expected = event(CoreMediaType.LOCALIZED_TEXT, "key");

		assertThat(event).isEqualTo(expected);
	}

	@Test
	public void html() throws Exception {
		node.setUserObject("<html>content</html>");

		final MapUpdated event = uut.createCoreUpdatedEvent(node);
		MapUpdated expected = event(CoreMediaType.HTML, "<html>content</html>");
		assertThat(event).isEqualTo(expected);
	}
	

	@Test
	public void object() throws Exception {
		node.setUserObject(Integer.valueOf(3));

		final MapUpdated event = uut.createCoreUpdatedEvent(node);

		MapUpdated expected = event(CoreMediaType.OBJECT, "java.lang.Integer|3");

		assertThat(event).isEqualTo(expected);
	}

}
