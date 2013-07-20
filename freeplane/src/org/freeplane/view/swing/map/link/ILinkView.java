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

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import org.freeplane.features.link.ConnectorModel;

/**
 * @author Dimitry Polivaev
 * 09.08.2009
 */
public interface ILinkView {
	/**
	 * Determines, whether or not a given point p is in an epsilon-neighbourhood
	 * for the cubic curve.
	 * @param b 
	 */
	public abstract boolean detectCollision(final Point p, boolean selectedOnly);

	/**
	 * fc: This getter is public, because the view gets the model by click on
	 * the curve.
	 */
	public abstract ConnectorModel getModel();

	/**
	 * \param iterativeLevel describes the n-th nested arrowLink that is to be
	 * painted.
	 */
	public abstract void paint(final Graphics graphics);

	public abstract void increaseBounds(final Rectangle innerBounds);
}
