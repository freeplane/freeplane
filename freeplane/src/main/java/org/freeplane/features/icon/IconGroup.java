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

import org.freeplane.core.util.TextUtils;

/**
 * 
 * Stores all kinds of icons used in Freeplane.
 * 
 * @author Tamas Eppel
 *
 */
public class IconGroup {
	private String name;
	private List<MindIcon> icons;
	private UIIcon groupIcon;
	private String description;

	public IconGroup(final String name, final UIIcon groupIcon) {
		this.name = name;
		this.groupIcon = groupIcon;
	}

	public IconGroup(final String name, final UIIcon groupIcon, final String description) {
		this.name = name;
		this.groupIcon = groupIcon;
		this.description = description;
	}

	public IconGroup(final String name, final UIIcon groupIcon, final String description, final List<MindIcon> icons) {
		this.name = name;
		this.groupIcon = groupIcon;
		this.description = description;
		this.icons = new ArrayList<MindIcon>(icons);
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public List<MindIcon> getIcons() {
		return Collections.unmodifiableList(icons);
	}

	public void setIcons(final List<MindIcon> icons) {
		this.icons = new ArrayList<MindIcon>(icons);
	}

	public UIIcon getGroupIcon() {
		return groupIcon;
	}

	public void setGroupIcon(final UIIcon groupIcon) {
		this.groupIcon = groupIcon;
	}

	public String getDescription() {
		if (description == null) {
			description = TextUtils.getText(getDescriptionKey());
		}
		return description;
	}

	public String getDescriptionKey() {
		return "IconGroupPopupAction." + name.toLowerCase() + ".text";
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public void addIcon(final MindIcon icon) {
		icons.add(icon);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final IconGroup other = (IconGroup) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		}
		else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format("icon group [%s]", name);
	}
}
