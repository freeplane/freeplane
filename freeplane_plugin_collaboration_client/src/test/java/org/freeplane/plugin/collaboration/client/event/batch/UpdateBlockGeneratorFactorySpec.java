package org.freeplane.plugin.collaboration.client.event.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.freeplane.collaboration.event.batch.ModifiableUpdateHeader;
import org.freeplane.features.map.MapModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateBlockGeneratorFactorySpec {
	@Mock
	private MapModel map;

	@Test
	public void setsTimerHeaderToMapExtension() throws Exception {
		UpdateBlockGeneratorFactory uut = new UpdateBlockGeneratorFactory(null, 0);
		final ModifiableUpdateHeaderWrapper headerExtension = new ModifiableUpdateHeaderWrapper(
		    ModifiableUpdateHeader.create()
		        .setMapId("mapId").setMapRevision(1L));
		when(map.getExtension(ModifiableUpdateHeaderWrapper.class)).thenReturn(headerExtension);
		Updates updates = uut.of(map);
		assertThat(updates).hasFieldOrPropertyWithValue("header", headerExtension.header);
	}

	@Test
	public void createsOnlyOneTimerForAGivenMap() throws Exception {
		UpdateBlockGeneratorFactory uut = new UpdateBlockGeneratorFactory(null, 0);
		final ModifiableUpdateHeaderWrapper headerExtension = new ModifiableUpdateHeaderWrapper(
		    ModifiableUpdateHeader.create()
		        .setMapId("mapId").setMapRevision(1L));
		when(map.getExtension(ModifiableUpdateHeaderWrapper.class)).thenReturn(headerExtension);
		Updates timer1 = uut.of(map);
		Updates timer2 = uut.of(map);
		assertThat(timer1).isSameAs(timer2);
	}
}
