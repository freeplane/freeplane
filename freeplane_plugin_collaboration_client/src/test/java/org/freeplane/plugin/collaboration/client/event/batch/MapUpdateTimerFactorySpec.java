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
		MapUpdateTimerFactory uut = new MapUpdateTimerFactory(null, null, 0);
		final ModifiableUpdateHeaderExtension header = ModifiableUpdateHeaderExtension.create()
				.setMapId("mapId").setMapRevision(1L);
		when(map.getExtension(ModifiableUpdateHeaderExtension.class)).thenReturn(header);

		final MapUpdateTimer timer = uut.create(map);
		
		assertThat(timer).hasFieldOrPropertyWithValue("header", header);
	}
}
