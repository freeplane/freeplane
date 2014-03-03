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
package org.freeplane.view.swing.features.filepreview;

import java.awt.Dimension;

public interface ScalableComponent {
	/**
	 * Returns not scaled size of the displayed component
	 */
	Dimension getOriginalSize();

	/**
	 * Adjusts size of the given viewer component after the mouse button is
	 * released
	 */
	void setFinalViewerSize(Dimension size);
	void setFinalViewerSize(float zoom);

	/**
	 * Adjusts size of the given viewer component inside resize operation by
	 * mouse drag
	 */
	void setDraftViewerSize(Dimension size);

	void setMaximumComponentSize(Dimension size);

	void setCenter(boolean center);
}
