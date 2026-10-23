package org.freeplane.features.treetable;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class TreeTableCellLayoutTest {

	@Test
	public void wrapsTextInsideColumnNodeWidth() {
		assertThat(TreeTableCellLayout.wrapWidth(80, 4)).isEqualTo(76);
	}

	@Test
	public void keepsPositiveWrapWidth() {
		assertThat(TreeTableCellLayout.wrapWidth(2, 4)).isEqualTo(1);
	}

	@Test
	public void fitsRowToHighestCellText() {
		assertThat(TreeTableCellLayout.requiredRowHeight(new int[] { 15, 45, 30 }, 4)).isEqualTo(53);
	}

	@Test
	public void requiresNoHeightWithoutCellText() {
		assertThat(TreeTableCellLayout.requiredRowHeight(new int[] {}, 4)).isZero();
	}

	@Test
	public void keepsPlainValue() {
		assertThat(TreeTableCellLayout.simplifiedValue("Rapide")).isEqualTo("Rapide");
	}

	@Test
	public void turnsUnformattedHtmlIntoPlainText() {
		assertThat(TreeTableCellLayout.simplifiedValue("<html><head></head><body><p>Rapide</p></body></html>"))
			.isEqualTo("Rapide");
	}

	@Test
	public void keepsFormattedHtml() {
		final String html = "<html><body><p><b>Rapide</b></p></body></html>";

		assertThat(TreeTableCellLayout.simplifiedValue(html)).isEqualTo(html);
	}
}
