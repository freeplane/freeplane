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
package org.freeplane.features.filter;

/**
 * @author Dimitry Polivaev
 */
public class FilterInfo {
	public static final int FILTER_INITIAL_VALUE = 1;
	public static final int FILTER_SHOW_ANCESTOR = 4;
	public static final int FILTER_SHOW_DESCENDANT = 8;
	public static final int FILTER_SHOW_ECLIPSED = 16;
	public static final int FILTER_SHOW_HIDDEN = 32;
	public static final int FILTER_SHOW_MATCHED = 2;
	private int info = FilterInfo.FILTER_INITIAL_VALUE;

	/**
	 *
	 */
	public FilterInfo() {
		super();
	}

	void add(final int flag) {
		if ((flag & (FilterInfo.FILTER_SHOW_MATCHED | FilterInfo.FILTER_SHOW_HIDDEN)) != 0) {
			info &= ~FilterInfo.FILTER_INITIAL_VALUE;
		}
		info |= flag;
	}

	private int get() {
		return info;
	}

	/**
	 */
	public boolean isAncestor() {
		return (info & FilterInfo.FILTER_SHOW_ANCESTOR) != 0;
	}

	/**
	 */
	public boolean isMatched() {
		return (info & FilterInfo.FILTER_SHOW_MATCHED) != 0;
	}

	public void reset() {
		info = FilterInfo.FILTER_INITIAL_VALUE;
	}

	public void setAncestor() {
		add(FilterInfo.FILTER_SHOW_ANCESTOR);
	}

	public void setDescendant() {
		add(FilterInfo.FILTER_SHOW_DESCENDANT);
	}

	public void setMatched() {
		add(FilterInfo.FILTER_SHOW_MATCHED);
	}

	public boolean isUnset() {
		return info == FilterInfo.FILTER_INITIAL_VALUE;
	}

	boolean isVisible(final int filterOptions) {
		return ((filterOptions & FilterInfo.FILTER_SHOW_ANCESTOR) != 0 || (filterOptions & FilterInfo.FILTER_SHOW_ECLIPSED) >= (info & FilterInfo.FILTER_SHOW_ECLIPSED))
		        && ((filterOptions & info & ~FilterInfo.FILTER_SHOW_ECLIPSED) != 0);
	}
}
