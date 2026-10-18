package org.freeplane.view.swing.map;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.BasicStroke;
import java.awt.Color;

import org.freeplane.api.Dash;
import org.junit.Test;

public class TreeTableLineStyleTest {

	@Test
	public void scalesWidthWithZoom() {
		final TreeTableLineStyle style = new TreeTableLineStyle(Color.RED, 2f, Dash.SOLID);

		assertThat(style.paintedWidth(1.5f)).isEqualTo(3f);
	}

	@Test
	public void neverPaintsLineThinnerThanOnePixel() {
		final TreeTableLineStyle style = new TreeTableLineStyle(Color.RED, 1f, Dash.SOLID);

		assertThat(style.paintedWidth(0.25f)).isEqualTo(TreeTableLineStyle.MINIMAL_PAINTED_WIDTH);
	}

	@Test
	public void keepsColor() {
		assertThat(new TreeTableLineStyle(Color.BLUE, 1f, Dash.SOLID).getColor()).isEqualTo(Color.BLUE);
	}

	@Test
	public void createsSolidStrokeWithoutDashPattern() {
		final BasicStroke stroke = (BasicStroke) new TreeTableLineStyle(Color.RED, 2f, Dash.SOLID).stroke(1f);

		assertThat(stroke.getDashArray()).isNull();
	}

	@Test
	public void createsDashedStrokeFromDash() {
		final BasicStroke stroke = (BasicStroke) new TreeTableLineStyle(Color.RED, 2f, Dash.DASHES).stroke(1f);

		assertThat(stroke.getDashArray()).isNotNull();
	}
}
