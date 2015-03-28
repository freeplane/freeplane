/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.view.swing.map.cloud;

import java.awt.Point;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Vector;

class ConvexHull {
	protected class thetaComparator implements Comparator<Object> {
		Point p0;

		public thetaComparator(final Point p0) {
			this.p0 = new Point(p0);
		}

		/* the < relation. */
		public int compare(final Object p1, final Object p2) {
			final double comp = theta(p0, (Point) p1) - theta(p0, (Point) p2);
			if (((Point) p1).equals(p2)) {
				return 0;
			}
			if (comp > 0) {
				return 1;
			}
			if (comp < 0) {
				return -1;
			}
			int dx1, dx2, dy1, dy2;
			dx1 = ((Point) p1).x - (p0).x;
			dy1 = ((Point) p1).y - (p0).y;
			dx2 = ((Point) p2).x - (p0).x;
			dy2 = ((Point) p2).y - (p0).y;
			final int comp2 = (dx1 * dx1 + dy1 * dy1) - (dx2 * dx2 + dy2 * dy2);
			if (comp2 > 0) {
				return -1;
			}
			if (comp2 < 0) {
				return 1;
			}
			return 0;
		}

		double theta(final Point p1, final Point p2) {
			int dx, dy, ax, ay;
			double t;
			dx = p2.x - p1.x;
			ax = Math.abs(dx);
			dy = p2.y - p1.y;
			ay = Math.abs(dy);
			if ((dx == 0) && (dy == 0)) {
				t = 0;
			}
			else {
				t = ((double) dy) / ((double) (ax + ay));
			}
			if (dx < 0) {
				t = 2f - t;
			}
			else {
				if (dy < 0) {
					t = 4f + t;
				}
			}
			return t * 90f;
		}
	}

	public Vector<Point>/* <newPoint> */calculateHull(final LinkedList<Point> coordinates) {
		// use a copy of coordinates since it will get modified in doGraham()
		return doGraham(new Vector<Point>(coordinates));
	}

	protected int ccw(final Point p0, final Point p1, final Point p2) {
		int dx1, dx2, dy1, dy2;
		dx1 = p1.x - p0.x;
		dy1 = p1.y - p0.y;
		dx2 = p2.x - p0.x;
		dy2 = p2.y - p0.y;
		final int comp = dx1 * dy2 - dy1 * dx2;
		if (comp > 0) {
			return 1;
		}
		if (comp < 0) {
			return -1;
		}
		if ((dx1 * dx2 < 0) || (dy1 * dy2 < 0)) {
			return -1;
		}
		if (dx1 * dx1 + dy1 * dy1 >= dx2 * dx2 + dy2 * dy2) {
			return 0;
		}
		return 1;
	}

	Vector<Point> doGraham(final Vector<Point> p) {
		int i;
		int min, m;
		Point t;
		min = 0;
		for (i = 1; i < p.size(); ++i) {
			if (((Point) p.get(i)).y < ((Point) p.get(min)).y) {
				min = i;
			}
		}
		for (i = 0; i < p.size(); ++i) {
			if ((((Point) p.get(i)).y == ((Point) p.get(min)).y) && (((Point) p.get(i)).x > ((Point) p.get(min)).x)) {
				min = i;
			}
		}
		t = p.get(0);
		p.set(0, p.get(min));
		p.set(min, t);
		final thetaComparator comp = new thetaComparator((Point) p.get(0));
		Collections.sort(p, comp);
		p.add(0, new Point((Point) p.get(p.size() - 1)));
		m = 3;
		for (i = 4; i < p.size(); ++i) {
			while (m > 0 && ccw((Point) p.get(m), (Point) p.get(m - 1), (Point) p.get(i)) >= 0) {
				m--;
			}
			m++;
			t = (Point) p.get(m);
			p.set(m, p.get(i));
			p.set(i, t);
		}
		p.remove(0);
		p.setSize(m);
		return p;
	}
}
