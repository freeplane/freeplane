package org.freeplane.view.swing.map.link;

import java.awt.Point;
import java.awt.Rectangle;

public class ConnectorTangent {
	static final int MINIMAL_LENGTH = 10;
	static final double SNAP_ANGLE = Math.toRadians(15);

	private final Rectangle content;
	private final float zoom;
	private final boolean topOrLeft;
	private boolean snapsAngle;
	private int keptLength;

	public ConnectorTangent(Rectangle content, float zoom, boolean topOrLeft) {
		this.content = content;
		this.zoom = zoom;
		this.topOrLeft = topOrLeft;
	}

	public ConnectorTangent snappingAngle(boolean snapsAngle) {
		this.snapsAngle = snapsAngle;
		return this;
	}

	public ConnectorTangent keepingLengthOf(Point inclination) {
		keptLength = inclination == null ? 0 : (int) Math.round(inclination.distance(0, 0));
		return this;
	}

	public Point inclinationWithHandleAt(Point handle, Point currentInclination) {
		final double dx = handle.x - content.getCenterX();
		final double dy = handle.y - content.getCenterY();
		if (dx == 0 && dy == 0)
			return new Point(currentInclination);
		double angle = Math.atan2(dy, dx);
		if (snapsAngle)
			angle = Math.round(angle / SNAP_ANGLE) * SNAP_ANGLE;
		final double ux = Math.cos(angle);
		final double uy = Math.sin(angle);
		final double length;
		if (keptLength > 0)
			length = keptLength;
		else
			length = Math.max(MINIMAL_LENGTH, (dx * ux + dy * uy - distanceToBorder(ux, uy)) / zoom);
		final int x = (int) Math.round(ux * length);
		final int y = (int) Math.round(uy * length);
		return new Point(topOrLeft ? -x : x, y);
	}

	private double distanceToBorder(double ux, double uy) {
		final double halfWidth = content.getWidth() / 2;
		final double halfHeight = content.getHeight() / 2;
		final double horizontal = Math.abs(ux) < 1e-9 ? Double.POSITIVE_INFINITY : halfWidth / Math.abs(ux);
		final double vertical = Math.abs(uy) < 1e-9 ? Double.POSITIVE_INFINITY : halfHeight / Math.abs(uy);
		return Math.min(horizontal, vertical);
	}
}
