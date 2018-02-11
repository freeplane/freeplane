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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.collaboration.client.event.TestObjects;
import org.freeplane.plugin.collaboration.client.event.content.other.ContentUpdateGenerator;
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
public class ContentUpdateGeneratorsSpec {
	private TestObjects testObjects = new TestObjects();
	private MapModel map = testObjects.map;
	private NodeModel node = testObjects.parent;
	@Mock
	private ContentUpdateGenerator contentUpdateGenerator1;
	@Mock
	private ContentUpdateGenerator contentUpdateGenerator2;
	
	private ContentUpdateGenerators uut;
	
	@Before
	public void setup() {
		uut = new ContentUpdateGenerators(Arrays.asList(contentUpdateGenerator1, contentUpdateGenerator2), 
			Arrays.asList(contentUpdateGenerator1, contentUpdateGenerator2));
	}

	@Test
	public void onNodeContentUpdateHandledByGenerator1() throws Exception {
		NodeChangeEvent event = new NodeChangeEvent(node, NodeModel.UNKNOWN_PROPERTY, null, null, true);
		when(contentUpdateGenerator1.handles(event)).thenReturn(true);
		uut.onNodeContentUpdate(event);
		verify(contentUpdateGenerator1).handles(event);
		verify(contentUpdateGenerator1).onNodeChange(event);
		verifyNoMoreInteractions(contentUpdateGenerator1, contentUpdateGenerator2);
		
	}
	
	@Test
	public void onNodeContentUpdateHandledByGenerator2() throws Exception {
		NodeChangeEvent event = new NodeChangeEvent(node, NodeModel.UNKNOWN_PROPERTY, null, null, true);
		when(contentUpdateGenerator2.handles(event)).thenReturn(true);
		uut.onNodeContentUpdate(event);
		verify(contentUpdateGenerator1).handles(event);
		verify(contentUpdateGenerator2).handles(event);
		verify(contentUpdateGenerator2).onNodeChange(event);
		verifyNoMoreInteractions(contentUpdateGenerator1, contentUpdateGenerator2);
		
	}
	
	@Test
	public void onNewNodeContentUpdateHandledByAllGenerators() throws Exception {
		uut.onNewNode(node);
		verify(contentUpdateGenerator1).onNewNode(node);
		verify(contentUpdateGenerator2).onNewNode(node);
		verifyNoMoreInteractions(contentUpdateGenerator1, contentUpdateGenerator2);
		
	}

	@Test
	public void onMapContentUpdateHandledByGenerator1() throws Exception {
		MapChangeEvent event = new MapChangeEvent(this, map, NodeModel.UNKNOWN_PROPERTY, null, null);
		when(contentUpdateGenerator1.handles(event)).thenReturn(true);
		uut.onMapContentUpdate(event);
		verify(contentUpdateGenerator1).handles(event);
		verify(contentUpdateGenerator1).onMapChange(map);
		verifyNoMoreInteractions(contentUpdateGenerator1, contentUpdateGenerator2);
		
	}
	
	@Test
	public void onMapContentUpdateHandledByGenerator2() throws Exception {
		MapChangeEvent event = new MapChangeEvent(this, map, NodeModel.UNKNOWN_PROPERTY, null, null);
		when(contentUpdateGenerator2.handles(event)).thenReturn(true);
		uut.onMapContentUpdate(event);
		verify(contentUpdateGenerator1).handles(event);
		verify(contentUpdateGenerator2).handles(event);
		verify(contentUpdateGenerator2).onMapChange(map);
		verifyNoMoreInteractions(contentUpdateGenerator1, contentUpdateGenerator2);
		
	}

	@Test
	public void onNewMapHandledByAllGenerators() throws Exception {
		uut.onNewMap(map);
		verify(contentUpdateGenerator1).onNewMap((map));
		verify(contentUpdateGenerator2).onNewMap((map));
		verifyNoMoreInteractions(contentUpdateGenerator1, contentUpdateGenerator2);
		
	}
}
