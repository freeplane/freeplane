/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Tamas Eppel
 *
 *  This file author is Tamas Eppel
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
package org.freeplane.features.icon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * Stores all kinds of icons used in Freeplane.
 * 
 * @author Tamas Eppel
 *
 */
public class IconStore {
	private final Map<String, IconGroup> groups;
	private final Map<String, MindIcon> mindIcons;
	private final Map<String, UIIcon> uiIcons;

	public IconStore() {
		groups = new LinkedHashMap<String, IconGroup>();
		mindIcons = new LinkedHashMap<>();
		uiIcons = new HashMap<String, UIIcon>();
	}

	public void addGroup(final IconGroup group) {
		groups.put(group.getName(), group);
		addIcons(group);
	}

    private void addIcons(final IconGroup group) {
        for (final IconGroup subgroup : group.getGroups()) {
		    if (subgroup.isLeaf())
		        mindIcons.put(subgroup.getName(), subgroup.getGroupIcon());
		    else
		        addIcons(subgroup);
		}
    }

    public void addEmojiIcon(final EmojiIcon icon) {
        mindIcons.put(icon.getName(), icon);
    }

	public void addUIIcon(final UIIcon uiIcon) {
		uiIcons.put(uiIcon.getFile(), uiIcon);
	}

	/**
	 * @return all groups in the store
	 */
	public Collection<IconGroup> getGroups() {
		return groups.values();
	}

	/**
	 * @return all MindIcons from all groups in the store, including user icons
	 */
	public Collection<MindIcon> getMindIcons() {
		return mindIcons.values();
	}

	/**
	 * @return all user icons in the store
	 */
	public List<MindIcon> getUserIcons() {
	    return groups.get("user").getIcons();
	}

	/**
	 * @param name of MindIcon to return
	 * @return MindIcon with given name
	 */
	public MindIcon getMindIcon(final String name) {
		if(name == null){
			return new IconNotFound(name);
		}
		if (mindIcons.containsKey(name)) {
			return mindIcons.get(name);
		}
		return new IconNotFound(name);
	}

	/**
	 * Returns a UIIcon with a given name.
	 * 
	 * @param name of UIIcon to return
	 * @return UIIcon with given name
	 */
	public UIIcon getUIIcon(final String name) {
		if (mindIcons.containsKey(name)) {
		    return mindIcons.get(name);
		}
		else if (uiIcons.containsKey(name)) {
		    return uiIcons.get(name);
		}
		else {
		    return new IconNotFound(name);
		}
	}
}
