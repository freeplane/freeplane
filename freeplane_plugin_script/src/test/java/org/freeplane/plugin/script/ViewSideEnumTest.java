package org.freeplane.plugin.script;

import static org.assertj.core.api.Assertions.assertThat;

import org.freeplane.api.ViewSide;
import org.freeplane.features.map.NodeModel.Side;
import org.junit.Test;

public class ViewSideEnumTest {
	@Test
	public void checkSameValuesInApiAndCore() {
		for(ViewSide viewSide : ViewSide.values()) {
			assertThat(viewSide.name()).isEqualTo(Side.values()[viewSide.ordinal()].name());
		}
	}
}
