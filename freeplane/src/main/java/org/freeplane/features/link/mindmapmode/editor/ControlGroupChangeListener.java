/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2016 jberry
 *
 *  This file author is jberry
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
package org.freeplane.features.link.mindmapmode.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collection;

import org.freeplane.core.resources.components.BooleanProperty;
import org.freeplane.core.resources.components.IPropertyControl;

abstract class ControlGroupChangeListener implements PropertyChangeListener {
	final private Collection<IPropertyControl> properties;
	final private BooleanProperty mSet;
	protected boolean internalChange;

	public ControlGroupChangeListener(final BooleanProperty mSet, final IPropertyControl... properties) {
		super();
		this.mSet = mSet;
		this.properties = Arrays.asList(properties);
	}

	void update() {
        if (internalChange) {
            return;
        }
        internalChange = true;
        updateValue();
        internalChange = false;
	    
	}
	abstract void updateValue();
	abstract void applyValue(final boolean enabled, PropertyChangeEvent evt);

	public void propertyChange(final PropertyChangeEvent evt) {
		if (internalChange) {
			return;
		}
		final boolean enabled;
		if (evt.getSource().equals(mSet)) {
			enabled = mSet.getBooleanValue();
		}
		else {
			assert properties.contains(evt.getSource());
			enabled = true;
		}
		if (enabled )
			internalChange = true;
		applyValue(enabled, evt);
		if (enabled  && ! mSet.getBooleanValue())
			mSet.setValue(true);
		internalChange = false;
		update();
	}
}