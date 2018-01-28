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
package org.freeplane.features.map;

import java.util.Date;

/**
 * Here, the creation and modification times of objects (by now, only for nodes)
 * are stored. The storage as longs is preferred as they are normally inlined by
 * the Java compiler.
 *
 * @author foltin
 */
public class HistoryInformationModel {
	long createdAt = 0l;
	long lastModifiedAt = 0l;

	/**
	 * Initializes to today.
	 */
	public HistoryInformationModel() {
		final long now = System.currentTimeMillis();
		createdAt = now;
		lastModifiedAt = now;
	}

	public HistoryInformationModel(final Date createdAt, final Date lastModifiedAt) {
		this.createdAt = createdAt.getTime();
		this.lastModifiedAt = lastModifiedAt.getTime();
	}

	public Date getCreatedAt() {
		return new Date(createdAt);
	}

	public Date getLastModifiedAt() {
		return new Date(lastModifiedAt);
	}

	public void setCreatedAt(final Date createdAt) {
		this.createdAt = createdAt.getTime();
	}

	public void setLastModifiedAt(final Date lastModifiedAt) {
		this.lastModifiedAt = lastModifiedAt.getTime();
	}

	public boolean isSet() {
		return createdAt != 0 || lastModifiedAt != 0;
	}
}
