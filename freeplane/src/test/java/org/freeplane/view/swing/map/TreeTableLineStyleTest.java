package org.freeplane.view.swing.map;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.BasicStroke;
import java.awt.Color;

import org.freeplane.api.Dash;
import org.junit.Test;

public class TreeTableLineStyleTest {

	@Test
	public void paintsWithNodeBorderWidth() {
		final BasicStroke stroke = (BasicStroke) new TreeTableLineStyle(Color.RED, 3, Dash.SOLID).stroke();

		assertThat(stroke.getLineWidth()).isEqualTo(3f);
	}

	@Test
	public void keepsColor() {
		assertThat(new TreeTableLineStyle(Color.BLUE, 1, Dash.SOLID).getColor()).isEqualTo(Color.BLUE);
	}

	@Test
	public void createsSolidStrokeWithoutDashPattern() {
		final BasicStroke stroke = (BasicStroke) new TreeTableLineStyle(Color.RED, 2, Dash.SOLID).stroke();

		assertThat(stroke.getDashArray()).isNull();
	}

	@Test
	public void createsDashedStrokeFromDash() {
		final BasicStroke stroke = (BasicStroke) new TreeTableLineStyle(Color.RED, 2, Dash.DASHES).stroke();

		assertThat(stroke.getDashArray()).isNotNull();
	}
}
