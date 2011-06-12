/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry Polivaev
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

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;

/**
 * @author Dimitry Polivaev
 * Mar 30, 2009
 */
public class FilterHistory {
// // 	private final Controller controller;
	private ListIterator<Filter> filters;

	FilterHistory() {
//		this.controller = controller;
		init();
	}

	void add(final Filter filter) {
		final Filter currentFilter = getCurrentFilter();
		if (isConditionStronger(currentFilter, filter)) {
			filters.previous();
			filters.remove();
		}
		while (filters.hasNext()) {
			filters.next();
			filters.remove();
		}
		filters.add(filter);
	}

	void clear() {
		init();
	}

	Filter getCurrentFilter() {
		filters.previous();
		return filters.next();
	}

	private void init() {
		final List<Filter> list = new LinkedList<Filter>();
		filters = list.listIterator();
		filters.add(Filter.createTransparentFilter());
	}

	private boolean isConditionStronger(final Filter oldFilter, final Filter newFilter) {
		return newFilter.isConditionStronger(oldFilter);
	}

	void redo() {
		if (!filters.hasNext()) {
			return;
		}
		Controller controller = Controller.getCurrentController();
		final MapModel map = controller.getMap();
		final Filter next = filters.next();
		next.applyFilter(this, map, true);
	}

	void undo() {
		Controller controller = Controller.getCurrentController();
		final MapModel map = controller.getMap();
		final Filter previous = filters.previous();
		undoImpl(map);
		while (previous != filters.next()) {
			;
		}
		if (filters.nextIndex() > 1) {
			filters.previous();
		}
	}

	private void undoImpl( final MapModel map) {
		if (!filters.hasPrevious()) {
			return;
		}
		final Filter previous = filters.previous();
		if (previous.appliesToVisibleNodesOnly()) {
			undoImpl(map);
		}
		previous.applyFilter(this, map, true);
	}
}
