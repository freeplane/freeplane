package org.freeplane.plugin.script;

import static org.assertj.core.api.Assertions.assertThat;

import org.freeplane.api.Side;
import org.freeplane.features.map.NodeModel;
import org.junit.Test;

public class ViewSideEnumTest {
	@Test
	public void checkSameValuesInApiAndCore() {
		for(Side viewSide : Side.values()) {
			assertThat(viewSide.name()).isEqualTo(NodeModel.Side.values()[viewSide.ordinal()].name());
		}
	}
}
