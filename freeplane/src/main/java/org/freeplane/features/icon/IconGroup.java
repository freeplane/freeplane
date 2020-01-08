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
import java.util.Collections;
import java.util.List;

/**
 * 
 * Stores all kinds of icons used in Freeplane.
 * 
 * @author Tamas Eppel
 *
 */
public class IconGroup {
	private final String name;
	private final List<IconGroup> groups ;
	private final MindIcon groupIcon;
	private final String description;
	
	private IconGroup(final MindIcon groupIcon) {
	    this(groupIcon.getName(), groupIcon, groupIcon.getTranslatedDescription());
	}

    public IconGroup(final String name, final MindIcon groupIcon, final String description) {
        this.name = name;
        this.groupIcon = groupIcon;
        this.description = description;
        this.groups = new ArrayList<>();
    }
    public IconGroup(final String name, final MindIcon groupIcon, final String description, final List<MindIcon> icons) {
        this(name, groupIcon, description);
        icons.stream().map(IconGroup::new).forEach(this.groups::add);
    }

	public String getName() {
		return name;
	}
	public List<IconGroup> getGroups() {
		return Collections.unmodifiableList(groups);
	}

	public List<MindIcon> getIcons() {
	    if(isLeaf())
	        return Collections.singletonList(getGroupIcon());
	    List<MindIcon> icons = new ArrayList<>(); 	                    
	    groups.stream().map(IconGroup::getIcons).forEach(icons::addAll);
	    return icons;
	}


	public MindIcon getGroupIcon() {
		return groupIcon;
	}

	public String getDescription() {
		return description;
	}

    public void addGroup(final IconGroup group) {
        groups.add(group);
    }

	@Override
	public int hashCode() {
	    throw new RuntimeException();
	}

	@Override
	public boolean equals(final Object obj) {
        throw new RuntimeException();
	}

	@Override
	public String toString() {
		return String.format("icon group [%s]", name);
	}
	
	public boolean isLeaf() {
	    return groups.isEmpty();
	}
}
