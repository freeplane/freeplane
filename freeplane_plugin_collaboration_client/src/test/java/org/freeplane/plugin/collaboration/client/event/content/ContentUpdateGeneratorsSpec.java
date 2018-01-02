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
import static org.mockito.Mockito.when;

import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.collaboration.client.event.TestObjects;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
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
	private ContentUpdateGeneratorFactory generatorFactory;
	@InjectMocks
	private ContentUpdateGenerators uut;
	
	@Mock
	private ContentUpdateGenerator contentUpdateGenerator;
	
	
	@Mock
	private CoreUpdateGenerator coreUpdateGenerator;

	@Test
	public void onNodeContentUpdate() throws Exception {
		when(generatorFactory.contentUpdateGeneratorOf(map)).thenReturn(contentUpdateGenerator);
		NodeChangeEvent event = new NodeChangeEvent(node, NodeModel.UNKNOWN_PROPERTY, null, null);
		uut.onNodeContentUpdate(event);
		
		verify(contentUpdateGenerator).onNodeContentUpdate(node);
		
	}


	@Test
	public void onMapContentUpdate() throws Exception {
		when(generatorFactory.contentUpdateGeneratorOf(map)).thenReturn(contentUpdateGenerator);
		MapChangeEvent event = new MapChangeEvent(this, map, NodeModel.UNKNOWN_PROPERTY, null, null);
		uut.onMapContentUpdate(event);
		
		verify(contentUpdateGenerator).onMapContentUpdate(map);
		
	}


	@Test
	public void onCoreContentUpdate() throws Exception {
		when(generatorFactory.coreUpdateGeneratorOf(map)).thenReturn(coreUpdateGenerator);
		NodeChangeEvent event = new NodeChangeEvent(node, NodeModel.NODE_TEXT, "1", "2");
		uut.onNodeContentUpdate(event);
		
		verify(coreUpdateGenerator).onCoreUpdate(node);
		
	}
}
