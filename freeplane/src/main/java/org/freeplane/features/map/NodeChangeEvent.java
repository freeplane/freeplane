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
package org.freeplane.features.map;

import java.awt.AWTEvent;

/**
 * @author Dimitry Polivaev 27.11.2008
 */
public class NodeChangeEvent extends AWTEvent {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	final private Object newValue;
	final private Object oldValue;
	final private Object property;
	final private boolean persistent;
	final private boolean setsDirtyFlag;
	final private boolean updatesModificationTime;

	public boolean isPersistent() {
		return persistent;
	}

	public NodeChangeEvent(final NodeModel node, final Object property,
	                       final Object oldValue, final Object newValue,
	                       boolean persistent, boolean setsDirtyFlag, boolean updatesModificationTime) {
		super(node, 0);
		this.oldValue = oldValue;
		this.newValue = newValue;
		this.property = property;
		this.persistent = persistent;
		this.setsDirtyFlag = setsDirtyFlag;
		this.updatesModificationTime = updatesModificationTime;
	}

	public Object getNewValue() {
		return newValue;
	}

	public NodeModel getNode() {
		return (NodeModel) getSource();
	}

	public Object getOldValue() {
		return oldValue;
	}

	public Object getProperty() {
		return property;
	}

	public NodeChangeEvent forNode(NodeModel node) {
		return new NodeChangeEvent(node, getProperty(), getOldValue(), getNewValue(), persistent, setsDirtyFlag, updatesModificationTime);
    }

	public boolean setsDirtyFlag() {
		return setsDirtyFlag;
	}

	public boolean updatesModificationTime() {
		return updatesModificationTime;
	}
}
