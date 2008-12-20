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
package deprecated.freemind.common;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.Vector;

public abstract class PropertyBean {
	final private Vector mPropertyChangeListeners = new Vector();

	public void addPropertyChangeListener(final PropertyChangeListener listener) {
		mPropertyChangeListeners.add(listener);
	}

	protected void firePropertyChangeEvent() {
		final PropertyChangeEvent evt = new PropertyChangeEvent(this, getLabel(), null, getValue());
		for (final Iterator i = mPropertyChangeListeners.iterator(); i.hasNext();) {
			final PropertyChangeListener listener = (PropertyChangeListener) i.next();
			listener.propertyChange(evt);
		}
	}

	/** The key of the property. */
	public abstract String getLabel();

	public abstract String getValue();

	public void removePropertyChangeListener(final PropertyChangeListener listener) {
		mPropertyChangeListeners.remove(listener);
	}

	public abstract void setValue(String value);
}
