package org.freeplane.view.swing.map;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Rectangle;

import org.junit.Test;

public class TreeTableHeaderButtonTest {
	private static final Rectangle HEADER = new Rectangle(100, 20, 60, 30);

	@Test
	public void placesButtonAtRightEdgeOfHeader() {
		final Rectangle button = TreeTableHeaderButton.boundsIn(HEADER, 16);

		assertThat(button.x + button.width).isEqualTo(160);
		assertThat(button.width).isEqualTo(16);
	}

	@Test
	public void centersButtonVerticallyInHeader() {
		final Rectangle button = TreeTableHeaderButton.boundsIn(HEADER, 16);

		assertThat(button.y).isEqualTo(27);
		assertThat(button.height).isEqualTo(16);
	}

	@Test
	public void leavesRemainingWidthToHeaderText() {
		final Rectangle text = TreeTableHeaderButton.textBoundsIn(HEADER, 16);

		assertThat(text).isEqualTo(new Rectangle(100, 20, 44, 30));
	}

	@Test
	public void shrinksButtonToSmallHeader() {
		final Rectangle button = TreeTableHeaderButton.boundsIn(new Rectangle(0, 0, 10, 12), 16);

		assertThat(button).isEqualTo(new Rectangle(0, 0, 10, 12));
	}

	@Test
	public void leavesNoTextWidthWhenButtonCoversHeader() {
		final Rectangle text = TreeTableHeaderButton.textBoundsIn(new Rectangle(0, 0, 10, 12), 16);

		assertThat(text.width).isZero();
	}

	@Test
	public void doesNotOverlapButtonAndText() {
		final Rectangle button = TreeTableHeaderButton.boundsIn(HEADER, 16);
		final Rectangle text = TreeTableHeaderButton.textBoundsIn(HEADER, 16);

		assertThat(text.x + text.width).isEqualTo(button.x);
	}
}
