package org.freeplane.features.treetable;

import static org.assertj.core.api.Assertions.assertThat;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.junit.Test;

public class TreeTableColumnTest {

	@Test
	public void convertsZoomedPixelsToUnzoomedPoints() {
		assertThat(TreeTableColumn.widthFromZoomedPixels(150, 1.5))
			.isEqualTo(new Quantity<LengthUnit>(100, LengthUnit.pt));
	}

	@Test
	public void roundsToWholePoints() {
		assertThat(TreeTableColumn.widthFromZoomedPixels(101, 2))
			.isEqualTo(new Quantity<LengthUnit>(51, LengthUnit.pt));
	}

	@Test
	public void neverGetsNarrowerThanMinimalWidth() {
		assertThat(TreeTableColumn.widthFromZoomedPixels(5, 1)).isEqualTo(TreeTableColumn.MINIMAL_WIDTH);
		assertThat(TreeTableColumn.widthFromZoomedPixels(-30, 1)).isEqualTo(TreeTableColumn.MINIMAL_WIDTH);
	}
}
