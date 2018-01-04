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
package org.freeplane.plugin.collaboration.client.event.content;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.when;

import java.io.Writer;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.TranslatedObject;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.MapWriter;
import org.freeplane.features.map.MapWriter.Mode;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.SummaryNodeFlag;
import org.freeplane.features.mode.MapExtensions;
import org.freeplane.plugin.collaboration.client.event.MapUpdated;
import org.freeplane.plugin.collaboration.client.event.TestObjects;
import org.freeplane.plugin.collaboration.client.event.content.core.CoreMediaType;
import org.freeplane.plugin.collaboration.client.event.content.core.CoreUpdated;
import org.freeplane.plugin.collaboration.client.event.content.other.MapContentUpdated;
import org.freeplane.plugin.collaboration.client.event.content.other.NodeContentUpdated;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

/**
 * @author Dimitry Polivaev
 * Dec 4, 2017
 */
@RunWith(MockitoJUnitRunner.class)
public class ContentUpdateEventFactorySpec {
	@Mock
	private MapWriter mapWriter;
	@InjectMocks
	private ContentUpdateEventFactory uut;

	TestObjects testObjects = new TestObjects();
	MapModel map = testObjects.map;
	NodeModel node = testObjects.parent;

	@Test
	public void createsNodeContentUpdatedEventUsingContentFromWriterCall() throws Exception {
		Mockito.doAnswer(new Answer<Void>() {

			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				invocation.getArgumentAt(0, Writer.class).append("content");
				return null;
			}
		}).when(mapWriter).writeNodeAsXml(any(), same(node), same(Mode.ADDITIONAL_CONTENT), eq(true), eq(false), eq(false));
		final MapUpdated event = uut.createNodeContentUpdatedEvent(node);
		assertThat(event).isEqualTo(NodeContentUpdated.builder().nodeId(node.getID()).content("content").build());
	}
	

	@Test
	public void excludesSpecificNodeExtensionsDuringTheWriterCall() throws Exception {
		node.addExtension(SummaryNodeFlag.SUMMARY);
		Mockito.doAnswer(new Answer<Void>() {

			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				final NodeModel node = invocation.getArgumentAt(1, NodeModel.class);
				assertThat(node.containsExtension(SummaryNodeFlag.class)).isFalse();
				invocation.getArgumentAt(0, Writer.class).append("content");
				return null;
			}
		}).when(mapWriter).writeNodeAsXml(any(), same(node), same(Mode.ADDITIONAL_CONTENT), eq(true), eq(false), eq(false));
		final MapUpdated event = uut.createNodeContentUpdatedEvent(node);
		assertThat(event).isEqualTo(NodeContentUpdated.builder().nodeId(node.getID()).content("content").build());
		assertThat(node.containsExtension(SummaryNodeFlag.class)).isTrue();
	}

	@Test
	public void createsMapContentUpdatedEventUsingContentFromWriterCall() throws Exception {
		when(map.getRootNode()).thenReturn(node);
		IExtension mapExtension = new IExtension() {};
		node.addExtension(mapExtension);
		MapExtensions.registerMapExtension(mapExtension.getClass());
		Mockito.doAnswer(new Answer<Void>() {

			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				assertThat(node.containsExtension(mapExtension.getClass())).isTrue();
				invocation.getArgumentAt(0, Writer.class).append("content");
				return null;
			}
		}).when(mapWriter).writeNodeAsXml(any(), same(node), same(Mode.ADDITIONAL_CONTENT), eq(true), eq(false), eq(false));
		final MapUpdated event = uut.createMapContentUpdatedEvent(map);
		assertThat(event).isEqualTo(MapContentUpdated.builder().content("<map>content</map>").build());
	}
	
	@Test
	public void excludesNodeExtensionsDuringTheWriterCall() throws Exception {
		when(map.getRootNode()).thenReturn(node);
		IExtension nodeExtension = new IExtension() {};
		node.addExtension(nodeExtension);
		Mockito.doAnswer(new Answer<Void>() {

			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				final NodeModel node = invocation.getArgumentAt(1, NodeModel.class);
				assertThat(node.containsExtension(nodeExtension.getClass())).isFalse();
				invocation.getArgumentAt(0, Writer.class).append("content");
				return null;
			}
		}).when(mapWriter).writeNodeAsXml(any(), same(node), same(Mode.ADDITIONAL_CONTENT), eq(true), eq(false), eq(false));
		
		final MapUpdated event = uut.createMapContentUpdatedEvent(map);
		
		assertThat(event).isEqualTo(MapContentUpdated.builder().content("<map>content</map>").build());
		assertThat(node.containsExtension(nodeExtension.getClass())).isTrue();
	}
	
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
