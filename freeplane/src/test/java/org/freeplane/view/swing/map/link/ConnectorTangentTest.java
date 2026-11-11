package org.freeplane.view.swing.map.link;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import java.awt.Point;
import java.awt.Rectangle;

import org.junit.Test;

public class ConnectorTangentTest {
	private static final Rectangle CONTENT = new Rectangle(100, 100, 100, 40);
	private static final Point CURRENT = new Point(30, 0);

	@Test
	public void measuresTangentFromNodeBorderOnRightSide() {
		Point inclination = new ConnectorTangent(CONTENT, 1f, false)
				.inclinationWithHandleAt(new Point(280, 120), CURRENT);

		assertThat(inclination).isEqualTo(new Point(80, 0));
	}

	@Test
	public void mirrorsHorizontalComponentForLeftNodes() {
		Point inclination = new ConnectorTangent(CONTENT, 1f, true)
				.inclinationWithHandleAt(new Point(20, 120), CURRENT);

		assertThat(inclination).isEqualTo(new Point(80, 0));
	}

	@Test
	public void measuresVerticalTangentFromTopOrBottomBorder() {
		Point inclination = new ConnectorTangent(CONTENT, 1f, false)
				.inclinationWithHandleAt(new Point(150, 200), CURRENT);

		assertThat(inclination).isEqualTo(new Point(0, 60));
	}

	@Test
	public void unzoomsLength() {
		Point inclination = new ConnectorTangent(CONTENT, 2f, false)
				.inclinationWithHandleAt(new Point(280, 120), CURRENT);

		assertThat(inclination).isEqualTo(new Point(40, 0));
	}

	@Test
	public void keepsOrientationTowardsHandleOnDiagonal() {
		Point inclination = new ConnectorTangent(CONTENT, 1f, false)
				.inclinationWithHandleAt(new Point(250, 220), CURRENT);

		assertThat(Math.atan2(inclination.y, inclination.x)).isCloseTo(Math.atan2(100, 100), offset(0.02));
		assertThat(inclination.distance(0, 0)).isCloseTo(Math.hypot(100, 100) - Math.hypot(20, 20), offset(1.5));
	}

	@Test
	public void neverGetsShorterThanMinimalLength() {
		Point inclination = new ConnectorTangent(CONTENT, 1f, false)
				.inclinationWithHandleAt(new Point(190, 120), CURRENT);

		assertThat(inclination).isEqualTo(new Point(ConnectorTangent.MINIMAL_LENGTH, 0));
	}

	@Test
	public void keepsCurrentInclinationWhenHandleIsOnNodeCenter() {
		Point inclination = new ConnectorTangent(CONTENT, 1f, false)
				.inclinationWithHandleAt(new Point(150, 120), CURRENT);

		assertThat(inclination).isEqualTo(CURRENT);
	}

	@Test
	public void snapsAngleToMultiplesOfFifteenDegrees() {
		Point inclination = new ConnectorTangent(CONTENT, 1f, false).snappingAngle(true)
				.inclinationWithHandleAt(new Point(280, 130), CURRENT);

		assertThat(inclination.y).isZero();
		assertThat(inclination.x).isPositive();
	}

	@Test
	public void keepsLengthAndChangesOnlyOrientation() {
		Point inclination = new ConnectorTangent(CONTENT, 1f, false).keepingLengthOf(new Point(30, 40))
				.inclinationWithHandleAt(new Point(150, 400), CURRENT);

		assertThat(inclination).isEqualTo(new Point(0, 50));
	}
}
