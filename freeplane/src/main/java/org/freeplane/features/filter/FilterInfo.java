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
    static final int MATCHES =  1 << 1;
    static final int HAS_MATCHED_DESCENDANT =  1 << 2;
    static final int HAS_MATCHED_ANCESTOR =  1 << 3;
    static final int NO_MATCH =  1 << 4;
    static final int HAS_HIDDEN_DESCENDANT =  1 << 5;
    static final int HAS_HIDDEN_ANCESTOR =  1 << 6;

    static final int SHOW_AS_INITIAL_VALUE = 1 << 0;
	static final int SHOW_AS_MATCHED =  MATCHES;
	static final int SHOW_AS_MATCHED_ANCESTOR =  HAS_MATCHED_DESCENDANT;
	static final int SHOW_AS_MATCHED_DESCENDANT =  HAS_MATCHED_ANCESTOR;
	static final int SHOW_AS_HIDDEN =  NO_MATCH;
    static final int SHOW_AS_HIDDEN_ANCESTOR =  HAS_HIDDEN_DESCENDANT;
    static final int SHOW_AS_HIDDEN_DESCENDANT =  HAS_HIDDEN_ANCESTOR;


    static public final FilterInfo TRANSPARENT = new FilterInfo(SHOW_AS_MATCHED);

	private int info;

    public FilterInfo() {
        this(SHOW_AS_INITIAL_VALUE);
    }

    private FilterInfo(int info) {
        this.info = info;
    }

    boolean set(final int newInfo) {
        if(info != newInfo) {
            info = newInfo;
            return true;
        }
        return false;
    }

    boolean add(final int flag) {
        int oldInfo = info;
        if ((flag & (SHOW_AS_MATCHED | SHOW_AS_HIDDEN)) != 0) {
            info &= ~SHOW_AS_INITIAL_VALUE;
        }
        info |= flag;
        return oldInfo != info;
    }

	/**
	 */
	public boolean canBeAncestor() {
		return (info & (SHOW_AS_MATCHED_ANCESTOR|SHOW_AS_INITIAL_VALUE)) != 0;
	}

	/**
	 */
	public boolean isMatched() {
		return (info & SHOW_AS_MATCHED) != 0;
	}

	public void reset() {
		info = SHOW_AS_INITIAL_VALUE;
	}

    boolean matches(final int filterOptions) {
        return get(filterOptions) != 0;
    }

    int get(final int filterOptions) {
        return filterOptions & info;
    }

    boolean isNotChecked() {
        return matches(SHOW_AS_INITIAL_VALUE);
    }
}
