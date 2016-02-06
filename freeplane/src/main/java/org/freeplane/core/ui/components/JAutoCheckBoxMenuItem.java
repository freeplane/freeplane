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
package org.freeplane.core.ui.components;

import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.KeyStroke;

import org.freeplane.core.ui.IFreeplaneAction;


public class JAutoCheckBoxMenuItem extends JCheckBoxMenuItem implements IKeyBindingManager {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public JAutoCheckBoxMenuItem(final IFreeplaneAction a) {
		super();
		setModel(new ActionToggleButtonModel(a));
		setAction(a);
	}
	
	

	@Override
    protected void configurePropertiesFromAction(Action a) {
	    super.configurePropertiesFromAction(a);
	    if(a != null)
	    	setSelected(((IFreeplaneAction)a).isSelected());
    }

	@Override
	protected PropertyChangeListener createActionPropertyChangeListener(final Action a) {
		return new PropertyChangeListenerWithSelectionSupport(this, super.createActionPropertyChangeListener(a));
	};

	private boolean isKeyBindingProcessed = false;

	@Override
	protected boolean processKeyBinding(final KeyStroke ks, final KeyEvent e, final int condition, final boolean pressed) {
		try {
			isKeyBindingProcessed = true;
			return super.processKeyBinding(ks, e, condition, pressed);
		}
		finally {
			isKeyBindingProcessed = false;
		}
	}

	public boolean isKeyBindingProcessed() {
		return isKeyBindingProcessed;
	}
}
