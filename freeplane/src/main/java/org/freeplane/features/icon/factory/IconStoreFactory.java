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
import org.freeplane.features.icon.IconNotFound;
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
    private static final String SEPARATOR = ";";
	private static final ResourceController RESOURCE_CONTROLLER = ResourceController.getResourceController();
    private static final String GROUP_NAMES_KEY = "icons.groups";
    private static final String STATE_ICON_NAMES_KEY = "icons.state";
	private static final String GROUP_KEY = "icons.group.%s";
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
		for (final String groupName : groupNames) {
            if (USER_ICON_GROUP_NAME.equals(groupName)) {
			    iconStore.addGroup(getUserIcons());
			}
			else {
			    iconStore.addGroup(getBuiltinIcons(groupName));
			}
		}
    }

	private IconGroup getBuiltinIcons(final String groupName) {
        final String groupIconName = RESOURCE_CONTROLLER.getProperty(String.format(GROUP_ICON_KEY, groupName));
		final String[] iconNames = RESOURCE_CONTROLLER.getProperty(String.format(GROUP_KEY, groupName))
		    .split(SEPARATOR);
		final List<MindIcon> icons = new ArrayList<MindIcon>(iconNames.length);
		MindIcon groupIcon = null;
		for (final String iconName : iconNames) {
			final MindIcon icon = createMindIcon(iconName);
			icons.add(icon);
			if(iconName.equals(groupIconName))
			    groupIcon = icon;
		}
        if (groupIcon == null) {
            groupIcon = icons.size() > 0 ? icons.get(0) : new IconNotFound("?");
        }
        final String description = TextUtils.getText(String.format(GROUP_DESC_KEY, groupName), StringUtils.capitalize(groupName));
		IconGroup iconGroup = new IconGroup(groupName, groupIcon, description);
		iconGroup.addIcons(icons);
        return iconGroup;
	}

	private IconGroup getUserIcons() {
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
		MindIcon groupIcon = createMindIcon(USER_GROUP_ICON);
		final String description = TextUtils.getText(String.format(GROUP_DESC_KEY, USER_ICON_GROUP_NAME));
		return new IconGroup(USER_ICON_GROUP_NAME, groupIcon, description, icons);
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
			if (iconName.equals("")) {
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
