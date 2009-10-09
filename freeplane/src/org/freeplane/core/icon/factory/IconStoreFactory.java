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
package org.freeplane.core.icon.factory;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.freeplane.core.icon.IconGroup;
import org.freeplane.core.icon.IconNotFound;
import org.freeplane.core.icon.IconStore;
import org.freeplane.core.icon.MindIcon;
import org.freeplane.core.icon.UIIcon;
import org.freeplane.core.icon.UserIcon;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;

/**
 * 
 * Factory for IconStore objects.
 * 
 * @author Tamas Eppel
 *
 */
public class IconStoreFactory {

	private static final String SEPARATOR = ";";

	private static final ResourceController RESOURCE_CONTROLLER = ResourceController.getResourceController();
	
	private static final String GROUP_NAMES_KEY = "icons.groups";
	
	private static final String GROUP_KEY = "icons.group.%s";
	
	private static final String GROUP_ICON_KEY = "IconGroupPopupAction.%s.icon";
	
	private static final String GROUP_DESC_KEY = "IconGroupPopupAction.%s.text";
	
	private static IconStore groups;
		
	/**
	 * 
	 * Creates an IconStore from the property file. If one was already
	 * constructed it will be returned without creating a new one.
	 * 
	 * @return
	 */
	public static IconStore create() {
		if(groups != null) {
			return groups;
		}
		
		IconStore iconStore = new IconStore();
		
		setIconGroups(iconStore);
		
		return iconStore;
	}
	
	private static void setIconGroups(IconStore iconStore) {
		String[] groupNames = RESOURCE_CONTROLLER.getProperty(GROUP_NAMES_KEY).split(SEPARATOR);
		for(String groupName : groupNames) {
			String description = ResourceBundles.getText(String.format(GROUP_DESC_KEY, groupName));
			
			List<MindIcon> icons;
			UIIcon groupIcon = null;
			if("user".equals(groupName)) {
				icons = getUserIcons();
				groupIcon = MindIconFactory.create("user_icon");
			}
			else {
				String groupIconName = RESOURCE_CONTROLLER.getProperty(String.format(GROUP_ICON_KEY, groupName));
				Map<String, MindIcon> iconMap = getIcons(groupName);
				groupIcon = iconMap.get(groupIconName);
				icons = new ArrayList<MindIcon>(iconMap.values());
			}

			if(groupIcon == null) {
				groupIcon = icons.size() > 0 ? icons.get(0) : IconNotFound.instance();
			}
			iconStore.addGroup(new IconGroup(groupName, groupIcon, description, icons));
			
		}
	}
	
	private static Map<String, MindIcon> getIcons(String groupName) {
		String[]       iconNames = RESOURCE_CONTROLLER.getProperty(String.format(GROUP_KEY, groupName)).split(SEPARATOR);
		Map<String, MindIcon> icons = new LinkedHashMap<String, MindIcon>(iconNames.length);
		
		for(String iconName : iconNames) {
			MindIcon icon = MindIconFactory.create(iconName);
			icons.put(iconName, icon);
		}
		
		return icons;
	}
	
	private static List<MindIcon> getUserIcons() {
		List<MindIcon> icons = Collections.emptyList();

		final ResourceController resourceController = ResourceController.getResourceController();
		if (resourceController.isApplet()){
			return icons;
		}
		final File iconDir = new File(resourceController.getFreeplaneUserDirectory(), "icons");
		if (! iconDir.exists()) {
			return icons;
		}
		final String[] userIconArray = iconDir.list(new FilenameFilter() {
			public boolean accept(final File dir, final String name) {
				return name.matches(".*\\.png");
			}
		});
		if (userIconArray != null) {
			icons = new ArrayList<MindIcon>(userIconArray.length);
			for (String fileName : userIconArray) {
				String iconName = fileName.substring(0, fileName.length() - 4);
				if (iconName.equals("")) {
					continue;
				}
				UserIcon icon = new UserIcon(iconName, fileName, iconName);
				icons.add(icon);
			}
		}
		return icons;		
	}
}
