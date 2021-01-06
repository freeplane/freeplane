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
package org.freeplane.features.icon.factory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.menubuilders.XmlEntryStructureBuilder;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.ui.menubuilders.generic.RecursiveMenuStructureProcessor;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.icon.EmojiIcon;
import org.freeplane.features.icon.IconGroup;
import org.freeplane.features.icon.IconStore;
import org.freeplane.features.icon.MindIcon;
import org.freeplane.features.icon.UIIcon;
import org.freeplane.features.icon.UserIcon;

/**
 * 
 * Factory for IconStore objects.
 * 
 * @author Tamas Eppel
 *
 */
public class IconStoreFactory {
	class EmojiGroupBuilder implements EntryVisitor {
        @Override
        public void visit(Entry entry) {
            String emoji = (String) entry.getAttribute("emoji");
            String file = (String) entry.getAttribute("file");
            String entity = entry.isLeaf() ? (String) entry.getAttribute("entity") : "";
            String description = (String) entry.getAttribute("description");
            EmojiIcon emojiIcon = new EmojiIcon(emoji, entity, file, description, order++);
            String name = entry.getName();
            IconGroup entryGroup = name.isEmpty() ? new IconGroup(emojiIcon) : new IconGroup(name, emojiIcon);
            entry.setAttribute(IconGroup.class, entryGroup);
            Entry parent = entry.getParent();
            if(parent != null) {
                IconGroup parentGroup = parent.getAttribute(IconGroup.class);
                parentGroup.addGroup(entryGroup);
            }
        }

        @Override
        public boolean shouldSkipChildren(Entry entry) {
            return false;
        }
    }

    private static final String USER_GROUP_ICON = "user_icon";
    private static final String USER_ICON_GROUP_NAME = "user";
    public static final String SEPARATOR = ";";
	private static final ResourceController RESOURCE_CONTROLLER = ResourceController.getResourceController();
    private static final String GROUP_NAMES_KEY = "icons.groups";
    private static final String STATE_ICON_NAMES_KEY = "icons.state";
    private static final String GROUP_KEY = "icons.group.%s";
    private static final String GROUPS_KEY = "icons.groups.%s";
	private static final String GROUP_ICON_KEY = "IconGroupPopupAction.%s.icon";
	private static final String GROUP_DESC_KEY = "IconGroupPopupAction.%s.text";
	private static final Pattern iconFileNamePattern = Pattern.compile(".*\\.(svg|png)$", Pattern.CASE_INSENSITIVE);
	private static final String EMOJI_ENTRIES_RESOURCE = "/images/emoji/xml/emojientries.xml";
	public static IconStore ICON_STORE = new IconStoreFactory().createIcons();
	
	private int order = 0;
    private final IconStore iconStore;
	
	private IconStoreFactory() {
	    iconStore = new IconStore();
	}
	
    private MindIcon createMindIcon(final String name) {
        final String translationKeyLabel = name.indexOf('/') > 0 ? "" : ("icon_" + name);
        return new MindIcon(name, name + ".svg", translationKeyLabel, order++);
    }
    
	private IconStore createIcons() {
		createClassicIcons();
		createEmojiIcons();
		createStateIcons();
		return iconStore;
	}

    private void createEmojiIcons() {
        final RecursiveMenuStructureProcessor actionBuilder = new RecursiveMenuStructureProcessor();
        EntryVisitor defaultBuilder = new EmojiGroupBuilder();
        actionBuilder.setDefaultBuilder(defaultBuilder);
        try {
            InputStream resource = ResourceController.getResourceController().getResourceStream(EMOJI_ENTRIES_RESOURCE);
            final Reader reader = new InputStreamReader(resource, StandardCharsets.UTF_8);
            Entry emojiGroupEntry = XmlEntryStructureBuilder.buildMenuStructure(reader);
            emojiGroupEntry.setName(IconStore.EMOJI_GROUP);
            emojiGroupEntry.setAttribute("emoji", "📙");
            emojiGroupEntry.setAttribute("file", "1f4d9.svg");
            emojiGroupEntry.setAttribute("description", TextUtils.getText("emoji_collection"));
            actionBuilder.build(emojiGroupEntry);
            iconStore.addGroup(emojiGroupEntry.getAttribute(IconGroup.class));
        } catch (IOException e) {
           throw new RuntimeException(e);
        }

    }

    private void createStateIcons() {
        final String[] stateIconNames = RESOURCE_CONTROLLER.getProperty(STATE_ICON_NAMES_KEY).split(SEPARATOR);
		for(String name : stateIconNames) {
		    UIIcon icon = new UIIcon(name, name, order++);
		    iconStore.addUIIcon(icon);
		}
    }

    private void createClassicIcons() {
        final String[] groupNames = RESOURCE_CONTROLLER.getProperty(GROUP_NAMES_KEY).split(SEPARATOR);
        IconGroup userIcons = getUserIcons();
		for (final String groupName : groupNames) {
            if (USER_ICON_GROUP_NAME.equals(groupName)) {
                iconStore.addGroup(userIcons);
			}
			else {
			    iconStore.addGroup(getBuiltinIconGroups(groupName));
			}
		}
		userIcons.addGroups(loadUserIcons());
    }

	private IconGroup getBuiltinIconGroups(final String groupName) {
        final List<MindIcon> icons = getBuiltinIcons(groupName);
        final List<IconGroup> groups = getBuiltinGroups(groupName);
        final String description = createDescription(groupName);
        MindIcon groupIcon = findGroupIcon(icons, groups, groupName);
		IconGroup iconGroup = new IconGroup(groupName, groupIcon, description);
		iconGroup.addIcons(icons);
		iconGroup.addGroups(groups);
        return iconGroup;
	}

    private String createDescription(final String groupName) {
        String translatedDescription = TextUtils.getText(String.format(GROUP_DESC_KEY, groupName), "");
        return translatedDescription.isEmpty() ? StringUtils.capitalize(groupName.replaceFirst(".*\\.", "")) : translatedDescription;
    }

    private MindIcon findGroupIcon(final List<MindIcon> icons, final List<IconGroup> groups,
            final String groupName) {
        String key = String.format(GROUP_ICON_KEY, groupName);
        final String groupIconName = RESOURCE_CONTROLLER.getProperty(key);
        MindIcon groupIcon = null;
        if(groupIconName != null) {
            groupIcon = icons.stream().filter(icon -> groupIconName.equals(icon.getName())).findAny().orElseGet(
            () -> groups.stream().filter(group -> groupIconName.equals(group.getName())).map(IconGroup::getGroupIcon).findAny().orElse(null));
        }
        return groupIcon;
    }

    private List<IconGroup> getBuiltinGroups(final String groupName) {
        String key = String.format(GROUPS_KEY, groupName);
        final String[] groupNames = RESOURCE_CONTROLLER.getArrayProperty(key, IconStoreFactory.SEPARATOR);
        final List<IconGroup> groups = new ArrayList<>(groupNames.length);
        for (final String subGroupName : groupNames) {
            final IconGroup subGroup = getBuiltinIconGroups(subGroupName);
            groups.add(subGroup);
        }
        return groups;
    }

    private List<MindIcon> getBuiltinIcons(final String groupName) {
        String key = String.format(GROUP_KEY, groupName);
        final String[] iconNames = RESOURCE_CONTROLLER.getArrayProperty(key, IconStoreFactory.SEPARATOR);
		final List<MindIcon> icons = new ArrayList<>(iconNames.length);
        for (final String iconName : iconNames) {
            final MindIcon icon = createMindIcon(iconName);
            icons.add(icon);
        }
        return icons;
    }

	private IconGroup getUserIcons() {
		MindIcon groupIcon = createMindIcon(USER_GROUP_ICON);
		final String description = TextUtils.getText(String.format(GROUP_DESC_KEY, USER_ICON_GROUP_NAME));
		return new IconGroup(USER_ICON_GROUP_NAME, groupIcon, description);
	}

    private List<IconGroup> loadUserIcons() {
        final List<IconGroup> icons;
		if (RESOURCE_CONTROLLER.isApplet()) {
		    icons = Collections.emptyList();
		}
		else {
		    final File iconDir = new File(RESOURCE_CONTROLLER.getFreeplaneUserDirectory(), "icons");
		    if (!iconDir.exists()) {
		        LogUtils.info("creating user icons directory " + iconDir);
		        iconDir.mkdirs();
		        icons = Collections.emptyList();
		    }
		    else {
		        icons = getUserIconsFromDirectory(iconDir, "");
		    }
		}
        return icons;
    }

	private List<IconGroup> getUserIconsFromDirectory(final File iconDir, final String dir) {
		final String[] userIconArray = iconDir.list(new FilenameFilter() {
			public boolean accept(final File dir, final String name) {
				return hasValidIconFileExtension(name) || new File(dir, name).isDirectory();
			}
		});
		Arrays.sort(userIconArray, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return getIconFileNameWithoutExtension(o1).compareTo(getIconFileNameWithoutExtension(o2));
			}
		});

		if (userIconArray == null) {
			return Collections.emptyList();
		}
		final List<IconGroup> icons = new ArrayList<IconGroup>(userIconArray.length);
		for (final String fileName : userIconArray) {
			final File childDir = new File(iconDir, fileName);
			if (childDir.isDirectory()) {
				final String fullName = dir + fileName;
				final List<IconGroup> childUserIcons = getUserIconsFromDirectory(childDir, fullName + '/');
				if(! childUserIcons.isEmpty()) {
				    IconGroup firstGroupIcon = childUserIcons.get(0);
				    icons.add(new IconGroup(fileName, firstGroupIcon.getGroupIcon(), fileName, childUserIcons));
				}
			}
		}
		for (final String fileName : userIconArray) {
			final File childDir = new File(iconDir, fileName);
			final String fullName = dir + fileName;
			if (childDir.isDirectory()) {
				continue;
			}
			final String iconName = fullName.substring(0, fullName.length() - 4);
			final String iconDescription = fileName.substring(0, fileName.length() - 4);
			if (iconName.equals("") || iconStore.containsMindIcon(iconName)) {
				continue;
			}
			final UserIcon icon = new UserIcon(iconName, fullName, iconDescription, order++);
			icons.add(new IconGroup(icon));
		}
		return icons;
	}

	private boolean hasValidIconFileExtension(final String name) {
		return iconFileNamePattern.matcher(name).matches();
	}

	private String getIconFileNameWithoutExtension(final String fileNameWithExtension) {
		return hasValidIconFileExtension(fileNameWithExtension) ?
				fileNameWithExtension.substring(0, fileNameWithExtension.length() - 4) : fileNameWithExtension;
	}
}
