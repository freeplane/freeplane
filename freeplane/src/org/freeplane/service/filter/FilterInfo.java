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
package org.freeplane.service.filter;

/**
 * @author Dimitry Polivaev
 */
public class FilterInfo {
	private int info = IFilter.FILTER_INITIAL_VALUE;

	/**
	 *
	 */
	public FilterInfo() {
		super();
	}

	void add(final int flag) {
		if ((flag & (IFilter.FILTER_SHOW_MATCHED | IFilter.FILTER_SHOW_HIDDEN)) != 0) {
			info &= ~IFilter.FILTER_INITIAL_VALUE;
		}
		info |= flag;
	}

	int get() {
		return info;
	}

	/**
	 */
	public boolean isAncestor() {
		return (info & IFilter.FILTER_SHOW_ANCESTOR) != 0;
	}

	/**
	 */
	public boolean isMatched() {
		return (info & IFilter.FILTER_SHOW_MATCHED) != 0;
	}

	public void reset() {
		info = IFilter.FILTER_INITIAL_VALUE;
	}

	public void setAncestor() {
		add(IFilter.FILTER_SHOW_ANCESTOR);
	}

	public void setDescendant() {
		add(IFilter.FILTER_SHOW_DESCENDANT);
	}

	public void setMatched() {
		add(IFilter.FILTER_SHOW_MATCHED);
	}
}
