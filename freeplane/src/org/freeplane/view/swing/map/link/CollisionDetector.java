/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry
 *
 *  This file author is Dimitry
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.view.swing.map.link;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * @author Dimitry Polivaev
 * 09.08.2009
 */
public class CollisionDetector {
	/** MAXIMAL_RECTANGLE_SIZE_FOR_COLLISION_DETECTION describes itself. */
	static final private int MAXIMAL_RECTANGLE_SIZE_FOR_COLLISION_DETECTION = 16;

	public boolean detectCollision(final Point p, final Shape shape) {
		final Rectangle2D rec = getControlRectangle(p);
		final PathIterator pathIterator = shape.getPathIterator(new AffineTransform(),
		    MAXIMAL_RECTANGLE_SIZE_FOR_COLLISION_DETECTION / 4);
		double lastCoords[] = new double[6];
		pathIterator.currentSegment(lastCoords);
		for (;;) {
			pathIterator.next();
			final double nextCoords[] = new double[6];
			if (pathIterator.isDone() || PathIterator.SEG_CLOSE == pathIterator.currentSegment(nextCoords)) {
				break;
			}
			final double x = Math.min(lastCoords[0], nextCoords[0]) - 1;
			final double y = Math.min(lastCoords[1], nextCoords[1]) - 1;
			final double w = Math.abs(lastCoords[0] - nextCoords[0]) + 2;
			final double h = Math.abs(lastCoords[1] - nextCoords[1]) + 2;
			if (rec.intersects(x, y, w, h)) {
				return true;
			}
			lastCoords = nextCoords;
		}
		return false;
	}

	private Rectangle2D getControlRectangle(final Point2D p) {
		final int side = MAXIMAL_RECTANGLE_SIZE_FOR_COLLISION_DETECTION;
		return new Rectangle2D.Double(p.getX() - side / 2, p.getY() - side / 2, side, side);
	}
}
