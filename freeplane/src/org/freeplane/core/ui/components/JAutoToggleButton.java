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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JToggleButton;

import org.freeplane.core.ui.SelectableAction;

public class JAutoToggleButton extends JToggleButton implements PropertyChangeListener {
	private PropertyChangeListener defaultPropertyChanegListener;

	public JAutoToggleButton(final Action a) {
		super(a);
	}

	@Override
	protected PropertyChangeListener createActionPropertyChangeListener(final Action a) {
		defaultPropertyChanegListener = super.createActionPropertyChangeListener(a);
		return this;
	};

	public void propertyChange(final PropertyChangeEvent e) {
		if (e.getPropertyName().equals(SelectableAction.SELECTION_PROPERTY)) {
			final Boolean isSelected = (Boolean) e.getNewValue();
			setSelected(isSelected.booleanValue());
		}
		else {
			defaultPropertyChanegListener.propertyChange(e);
		}
	}
}
