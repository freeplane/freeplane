package org.freeplane.plugin.collaboration.client.event.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.freeplane.features.map.MapModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MapUpdateTimerFactorySpec {
	@Mock
	private MapModel map;

	@Test
	public void setsTimerHeaderToMapExtension() throws Exception {
		MapUpdateTimerFactory uut = new MapUpdateTimerFactory(null, 0);
		final ModifiableUpdateHeaderExtension header = ModifiableUpdateHeaderExtension.create()
				.setMapId("mapId").setMapRevision(1L);
		when(map.getExtension(ModifiableUpdateHeaderExtension.class)).thenReturn(header);

		MapUpdateTimer timer = uut.createTimer(map);
		
		assertThat(timer).hasFieldOrPropertyWithValue("header", header);
	}

	@Test
	public void createsOnlyOneTimerForAGivenMap() throws Exception {
		MapUpdateTimerFactory uut = new MapUpdateTimerFactory(null, 0);
		MapUpdateTimer timer1 = uut.createTimer(map);
		MapUpdateTimer timer2 = uut.createTimer(map);
		
		assertThat(timer1).isSameAs(timer2);
	}
}
