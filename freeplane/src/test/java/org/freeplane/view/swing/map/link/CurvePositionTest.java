package org.freeplane.view.swing.map.link;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import java.awt.Point;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;

import org.junit.Test;

public class CurvePositionTest {
	@Test
	public void measuresFractionAlongStraightLine() {
		Line2D line = new Line2D.Double(0, 0, 300, 0);

		assertThat(CurvePosition.fractionAlong(line, new Point(60, 5))).isCloseTo(0.2, offset(0.001));
		assertThat(CurvePosition.fractionAlong(line, new Point(-20, 0))).isZero();
		assertThat(CurvePosition.fractionAlong(line, new Point(400, 0))).isEqualTo(1.0);
	}

	@Test
	public void measuresFractionAlongPathLengthNotDistanceFromStart() {
		GeneralPath path = new GeneralPath();
		path.moveTo(0, 0);
		path.lineTo(100, 0);
		path.lineTo(100, 300);

		assertThat(CurvePosition.fractionAlong(path, new Point(102, 200))).isCloseTo(0.75, offset(0.001));
	}

	@Test
	public void findsPositionOnLoopingCurve() {
		CubicCurve2D curve = new CubicCurve2D.Double(0, 0, 300, 0, 300, 100, 0, 100);

		assertThat(CurvePosition.fractionAlong(curve, new Point(10, 1))).isLessThan(0.1);
		assertThat(CurvePosition.fractionAlong(curve, new Point(10, 99))).isGreaterThan(0.9);
	}

	@Test
	public void revealsEndOfTheOtherHalf() {
		assertThat(CurvePosition.endToReveal(0.1)).isEqualTo(ConnectorEnd.TARGET);
		assertThat(CurvePosition.endToReveal(0.49)).isEqualTo(ConnectorEnd.TARGET);
		assertThat(CurvePosition.endToReveal(0.5)).isEqualTo(ConnectorEnd.SOURCE);
		assertThat(CurvePosition.endToReveal(0.9)).isEqualTo(ConnectorEnd.SOURCE);
	}
}
