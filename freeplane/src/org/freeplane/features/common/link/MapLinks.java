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
package org.freeplane.features.common.link;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.model.MapModel;

/**
 * @author Dimitry Polivaev
 */
public class MapLinks implements IExtension {
	final private HashMap<String, Set<LinkModel>> links = new HashMap<String, Set<LinkModel>>();

	public boolean add(final LinkModel link) {
		final String targetID = link.getTargetID();
		Set<LinkModel> set = links.get(targetID);
		if (set == null) {
			set = new HashSet<LinkModel>();
			set.add(link);
			links.put(targetID, set);
			return true;
		}
		if (set.contains(link)) {
			return false;
		}
		set.add(link);
		return true;
	}

	public boolean containsTarget(final String targetID) {
		return links.containsKey(targetID);
	}

	public Set<LinkModel> get(final String targetID) {
		if(targetID == null) {
			return null;
		}
		final Set<LinkModel> set = links.get(targetID);
		return set == null ? null : Collections.unmodifiableSet(set);
	}

	public boolean remove(final LinkModel link) {
		final String targetID = link.getTargetID();
		final Set<LinkModel> set = links.get(targetID);
		if (set == null) {
			return false;
		}
		if (set.remove(link)) {
			if (set.isEmpty()) {
				links.remove(targetID);
			}
			return true;
		}
		return false;
	}

	public void set(final String targetID, final Set<LinkModel> set) {
		links.put(targetID, set);
	}

	public static MapLinks getLinks(final MapModel map) {
        return (MapLinks) map.getExtension(MapLinks.class);
    }
}
