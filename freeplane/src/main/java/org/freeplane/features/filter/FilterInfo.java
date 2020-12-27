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
	static final int FILTER_SHOW_AS_INITIAL_VALUE = 1;
	static final int FILTER_SHOW_AS_MATCHED = 2;
	static final int FILTER_SHOW_AS_ANCESTOR = 4;
	static final int FILTER_SHOW_AS_DESCENDANT = 8;
	static final int FILTER_SHOW_AS_HIDDEN = 16;
	
	static public final FilterInfo TRANSPARENT = new FilterInfo(FILTER_SHOW_AS_MATCHED);
	
	private int info;

    public FilterInfo() {
        this(FILTER_SHOW_AS_INITIAL_VALUE);
    }
    
    private FilterInfo(int info) {
        this.info = info;
    }

	void add(final int flag) {
		if ((flag & (FILTER_SHOW_AS_MATCHED | FILTER_SHOW_AS_HIDDEN)) != 0) {
			info &= ~FILTER_SHOW_AS_INITIAL_VALUE;
		}
		info |= flag;
	}

	/**
	 */
	public boolean canBeAncestor() {
		return (info & (FILTER_SHOW_AS_ANCESTOR|FILTER_SHOW_AS_INITIAL_VALUE)) != 0;
	}

	/**
	 */
	public boolean isMatched() {
		return (info & FILTER_SHOW_AS_MATCHED) != 0;
	}

	public void reset() {
		info = FILTER_SHOW_AS_INITIAL_VALUE;
	}

	boolean matches(final int filterOptions) {
		return (filterOptions & info) != 0;
	}

    boolean isNotChecked() {
        return matches(FILTER_SHOW_AS_INITIAL_VALUE);
    }
}
