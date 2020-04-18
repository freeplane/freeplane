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
package org.freeplane.view.swing.features.time.mindmapmode.nodelist;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.filter.Filter;
import org.freeplane.features.mode.Controller;

/**
 * @author Dimitry Polivaev
 * 01.09.2009
 */
public class AllMapsNodeListAction extends AFreeplaneAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private final NodeList nodeList;

	public AllMapsNodeListAction() {
		super("AllMapsNodeListAction");
		nodeList = new NodeListWithReplacement(NodeList.REMINDER_TEXT_WINDOW_TITLE_ALL_NODES,
			true,
			"allmapsnodelistwindow.configuration");
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
	    Filter filter = Filter.createTransparentFilter();
		nodeList.startup((node, reminder) -> node.hasVisibleContent(filter));
	}
}
