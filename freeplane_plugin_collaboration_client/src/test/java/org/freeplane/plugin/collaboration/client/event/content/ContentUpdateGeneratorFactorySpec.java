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
import static org.mockito.Mockito.when;

import org.freeplane.features.map.MapModel;
import org.freeplane.plugin.collaboration.client.event.batch.MapUpdateTimer;
import org.freeplane.plugin.collaboration.client.event.batch.MapUpdateTimerFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Dimitry Polivaev
 * Jan 1, 2018
 */
@RunWith(MockitoJUnitRunner.class)
public class ContentUpdateGeneratorFactorySpec {
	@Mock
	private ContentUpdateEventFactory eventFactory;
	@Mock
	private MapUpdateTimerFactory timerFactory;
	
	@InjectMocks
	private ContentUpdateGeneratorFactory uut;
	
	@Mock
	private MapUpdateTimer timer;

	@Test
	public void createsContentUpdateGenerator() throws Exception {
		MapModel map = new MapModel(null, null);
		when(timerFactory.createTimer(map)).thenReturn(timer);
		ContentUpdateGenerator expected = new ContentUpdateGenerator(timer, eventFactory);
		assertThat(uut.generatorOf(map)).isEqualToComparingFieldByField(expected);
	}
}
