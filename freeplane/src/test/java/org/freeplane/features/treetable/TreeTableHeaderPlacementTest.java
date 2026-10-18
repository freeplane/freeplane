package org.freeplane.features.treetable;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class TreeTableHeaderPlacementTest {

	@Test
	public void placesHeaderSubtreeGapAfterTreeColumn() {
		assertThat(TreeTableHeaderPlacement.headerX(200, 6, 10)).isEqualTo(196);
	}

	@Test
	public void extendsTreeColumnToWidestRowSubtree() {
		assertThat(TreeTableHeaderPlacement.treeColumnRight(80, new int[] { 20, 30 }, new int[] { 100, 200 }, 10))
			.isEqualTo(220);
	}

	@Test
	public void keepsTreeColumnAtLeastAsWideAsTableRoot() {
		assertThat(TreeTableHeaderPlacement.treeColumnRight(300, new int[] { 20 }, new int[] { 100 }, 10))
			.isEqualTo(300);
	}
}
