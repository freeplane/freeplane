/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
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
package org.freeplane.core.resources.components;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Vector;

public abstract class PropertyBean extends PropertyAdapter implements IPropertyControl {
	
	private class MouseClickListener extends MouseAdapter{
		@Override
        public void mousePressed(MouseEvent e) {
			firePropertyChangeEvent();
        }
	}
	
	final private List<PropertyChangeListener> mPropertyChangeListeners = new Vector<PropertyChangeListener>();
	private MouseClickListener mouseCliskListener;

	public PropertyBean(final String name) {
		super(name);
	}

	public PropertyBean(final String name, final String label, final String description) {
		super(name, label, description);
	}

	public void addPropertyChangeListener(final PropertyChangeListener listener) {
		mPropertyChangeListeners.add(listener);
	}
	
	public void fireOnMouseClick(){
		if(mouseCliskListener != null){
			return;
		}
		mouseCliskListener = new MouseClickListener();
		final Component[] components = getComponents();
		for(Component c:components){
			c.addMouseListener(mouseCliskListener);
		}
	}

	protected void firePropertyChangeEvent() {
		final PropertyChangeEvent evt = new PropertyChangeEvent(this, getName(), null, getValue());
		for (final PropertyChangeListener l : mPropertyChangeListeners) {
			l.propertyChange(evt);
		}
	}

	public abstract String getValue();

	public void removePropertyChangeListener(final PropertyChangeListener listener) {
		mPropertyChangeListeners.remove(listener);
	}

	public abstract void setValue(String value);

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + getName() + "->" + getValue() + ")";
	}
	
	protected abstract Component[] getComponents();
}
