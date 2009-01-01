/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
package org.freeplane.core.filter;

import org.freeplane.core.map.NodeModel;

/**
 * @author Dimitry Polivaev
 */
public interface IFilter {
	public static final int FILTER_INITIAL_VALUE = 1;
	static final int FILTER_SHOW_ANCESTOR = 4;
	static final int FILTER_SHOW_DESCENDANT = 8;
	static final int FILTER_SHOW_ECLIPSED = 16;
	static final int FILTER_SHOW_HIDDEN = 32;
	static final int FILTER_SHOW_MATCHED = 2;

	void applyFilter();

	boolean areAncestorsShown();

	boolean areDescendantsShown();

	boolean areEclipsedShown();

	boolean areHiddenShown();

	boolean areMatchedShown();

	Object getCondition();

	boolean isVisible(NodeModel node);
}
