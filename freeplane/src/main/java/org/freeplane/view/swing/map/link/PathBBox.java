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
package org.freeplane.view.swing.map.link;

import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;

class PathBBox {
	private static void accum(final double[] bounds, final double x, final double y) {
		bounds[0] = Math.min(bounds[0], x);
		bounds[1] = Math.min(bounds[1], y);
		bounds[2] = Math.max(bounds[2], x);
		bounds[3] = Math.max(bounds[3], y);
	}

	private static void accumCubic(final double bounds[], final double t, final double curx, final double cury,
	                               final double cpx0, final double cpy0, final double cpx1, final double cpy1,
	                               final double endx, final double endy) {
		final double u = (1 - t);
		final double x = curx * u * u * u + 3.0 * cpx0 * t * u * u + 3.0 * cpx1 * t * t * u + endx * t * t * t;
		final double y = cury * u * u * u + 3.0 * cpy0 * t * u * u + 3.0 * cpy1 * t * t * u + endy * t * t * t;
		PathBBox.accum(bounds, x, y);
	}

	private static void accumQuad(final double bounds[], final double t, final double curx, final double cury,
	                              final double cpx0, final double cpy0, final double endx, final double endy) {
		final double u = (1 - t);
		final double x = curx * u * u + 2.0 * cpx0 * t * u + endx * t * t;
		final double y = cury * u * u + 2.0 * cpy0 * t * u + endy * t * t;
		PathBBox.accum(bounds, x, y);
	}

	private static int findCubicZeros(final double zeros[], final double cur, final double cp0, final double cp1,
	                                  final double end) {
		zeros[0] = (cp0 - cur) * 3.0;
		zeros[1] = (cp1 - cp0 - cp0 + cur) * 6.0;
		zeros[2] = (end + (cp0 - cp1) * 3.0 - cur) * 3.0;
		final int num = QuadCurve2D.solveQuadratic(zeros);
		int ret = 0;
		for (int i = 0; i < num; i++) {
			final double t = zeros[i];
			if (t > 0 && t < 1) {
				zeros[ret] = t;
				ret++;
			}
		}
		return ret;
	}

	private static double findQuadZero(final double cur, final double cp, final double end) {
		return -(cp + cp - cur - cur) / (2.0 * (cur - cp - cp + end));
	}

	public static Rectangle2D getBBox(final Shape s) {
		boolean first = true;
		final double bounds[] = new double[4];
		final double coords[] = new double[6];
		double curx = 0;
		double cury = 0;
		double movx = 0;
		double movy = 0;
		double cpx0, cpy0, cpx1, cpy1, endx, endy;
		for (final PathIterator pi = s.getPathIterator(null); !pi.isDone(); pi.next()) {
			pi.currentSegment(coords);
			switch (pi.currentSegment(coords)) {
				case PathIterator.SEG_MOVETO:
					movx = curx = coords[0];
					movy = cury = coords[1];
					if (first) {
						bounds[0] = bounds[2] = curx;
						bounds[1] = bounds[3] = cury;
						first = false;
					}
					else {
						PathBBox.accum(bounds, curx, cury);
					}
					break;
				case PathIterator.SEG_LINETO:
					curx = coords[0];
					cury = coords[1];
					PathBBox.accum(bounds, curx, cury);
					break;
				case PathIterator.SEG_QUADTO:
					cpx0 = coords[0];
					cpy0 = coords[1];
					endx = coords[2];
					endy = coords[3];
					double t = PathBBox.findQuadZero(curx, cpx0, endx);
					if (t > 0 && t < 1) {
						PathBBox.accumQuad(bounds, t, curx, cury, cpx0, cpy0, endx, endy);
					}
					t = PathBBox.findQuadZero(cury, cpy0, endy);
					if (t > 0 && t < 1) {
						PathBBox.accumQuad(bounds, t, curx, cury, cpx0, cpy0, endx, endy);
					}
					curx = endx;
					cury = endy;
					PathBBox.accum(bounds, curx, cury);
					break;
				case PathIterator.SEG_CUBICTO:
					cpx0 = coords[0];
					cpy0 = coords[1];
					cpx1 = coords[2];
					cpy1 = coords[3];
					endx = coords[4];
					endy = coords[5];
					int num = PathBBox.findCubicZeros(coords, curx, cpx0, cpx1, endx);
					for (int i = 0; i < num; i++) {
						PathBBox.accumCubic(bounds, coords[i], curx, cury, cpx0, cpy0, cpx1, cpy1, endx, endy);
					}
					num = PathBBox.findCubicZeros(coords, cury, cpy0, cpy1, endy);
					for (int i = 0; i < num; i++) {
						PathBBox.accumCubic(bounds, coords[i], curx, cury, cpx0, cpy0, cpx1, cpy1, endx, endy);
					}
					curx = endx;
					cury = endy;
					PathBBox.accum(bounds, curx, cury);
					break;
				case PathIterator.SEG_CLOSE:
					curx = movx;
					cury = movy;
					break;
			}
		}
		return new Rectangle2D.Double(bounds[0], bounds[1], bounds[2] - bounds[0], bounds[3] - bounds[1]);
	}
}
