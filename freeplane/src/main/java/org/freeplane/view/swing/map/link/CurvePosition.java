package org.freeplane.view.swing.map.link;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

public class CurvePosition {
	private static final double FLATNESS = 1;

	public static double fractionAlong(Shape curve, Point point) {
		final PathIterator iterator = curve.getPathIterator(null, FLATNESS);
		final double[] coordinates = new double[6];
		double lastX = 0, lastY = 0, length = 0, bestDistance = Double.POSITIVE_INFINITY, bestLength = 0;
		boolean started = false;
		for (; !iterator.isDone(); iterator.next()) {
			final int segment = iterator.currentSegment(coordinates);
			final double x = coordinates[0], y = coordinates[1];
			if (segment == PathIterator.SEG_MOVETO || !started) {
				if (!started && point.distance(x, y) < bestDistance) {
					bestDistance = point.distance(x, y);
					bestLength = 0;
				}
				started = true;
			}
			else if (segment == PathIterator.SEG_LINETO) {
				final double segmentLength = Point2D.distance(lastX, lastY, x, y);
				final double distance = Line2D.ptSegDist(lastX, lastY, x, y, point.x, point.y);
				if (distance < bestDistance) {
					bestDistance = distance;
					bestLength = length + projectedLength(lastX, lastY, x, y, point, segmentLength);
				}
				length += segmentLength;
			}
			lastX = x;
			lastY = y;
		}
		return length == 0 ? 0 : bestLength / length;
	}

	public static ConnectorEnd endToReveal(double fraction) {
		return fraction < 0.5 ? ConnectorEnd.TARGET : ConnectorEnd.SOURCE;
	}

	private static double projectedLength(double x1, double y1, double x2, double y2, Point point, double segmentLength) {
		if (segmentLength == 0)
			return 0;
		final double projection = ((point.x - x1) * (x2 - x1) + (point.y - y1) * (y2 - y1)) / segmentLength;
		return Math.max(0, Math.min(segmentLength, projection));
	}
}
