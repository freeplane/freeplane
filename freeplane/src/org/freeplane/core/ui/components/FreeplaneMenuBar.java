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
package org.freeplane.core.ui.components;

import java.awt.event.KeyEvent;

import javax.swing.JMenuBar;
import javax.swing.KeyStroke;

/**
 * This is the menu bar for Freeplane. Actions are defined in MenuListener.
 * Moreover, the StructuredMenuHolder of all menus are hold here.
 */
public class FreeplaneMenuBar extends JMenuBar {
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
	public static final String EDIT_MENU = FreeplaneMenuBar.MENU_BAR_PREFIX + "/edit";
	public static final String EXTRAS_MENU = FreeplaneMenuBar.MENU_BAR_PREFIX + "/extras";
	public static final String FILE_MENU = FreeplaneMenuBar.MENU_BAR_PREFIX + "/file";
	public static final String FORMAT_MENU = FreeplaneMenuBar.MENU_BAR_PREFIX + "/format";
	public static final String HELP_MENU = FreeplaneMenuBar.MENU_BAR_PREFIX + "/help";
	public static final String INSERT_MENU = FreeplaneMenuBar.MENU_BAR_PREFIX + "/insert";
	public static final String MAP_POPUP_MENU = "/map_popup";
	public static final String MENU_BAR_PREFIX = "/menu_bar";
	public static final String MINDMAP_MENU = FreeplaneMenuBar.MENU_BAR_PREFIX + "/mindmaps";
	public static final String MODES_MENU = FreeplaneMenuBar.MINDMAP_MENU + "/modes";
	public static final String NAVIGATE_MENU = FreeplaneMenuBar.MENU_BAR_PREFIX + "/navigate";

	public static final String VIEW_MENU = FreeplaneMenuBar.MENU_BAR_PREFIX + "/view";

	public FreeplaneMenuBar() {
	}

	@Override
	public boolean processKeyBinding(final KeyStroke ks, final KeyEvent e, final int condition, final boolean pressed) {
		return super.processKeyBinding(ks, e, condition, pressed);
	}
}
