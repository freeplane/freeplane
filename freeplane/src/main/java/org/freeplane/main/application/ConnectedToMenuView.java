/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2013 Dimitry
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
package org.freeplane.main.application;

import java.awt.Component;
import java.awt.event.KeyEvent;

import javax.swing.Icon;
import javax.swing.KeyStroke;

import net.infonode.docking.View;

/**
 * @author Dimitry Polivaev
 * 27.04.2013
 */
@SuppressWarnings("serial")
final class ConnectedToMenuView extends View {
	ConnectedToMenuView(String title, Icon icon, Component component) {
	    super(title, icon, component);
    }
    @Override
    protected boolean processKeyBinding(final KeyStroke ks, final KeyEvent e, final int condition, final boolean pressed) {
    	return super.processKeyBinding(ks, e, condition, pressed) || MenuKeyProcessor.INSTANCE.processKeyBinding(ks, e, condition, pressed);
    }
}