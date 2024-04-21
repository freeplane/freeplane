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
package org.freeplane.features.icon;

import java.awt.Color;
import java.util.Optional;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.core.util.collection.SortedComboBoxModel;
import org.freeplane.features.map.MapModel;

/**
 * @author Dimitry Polivaev
 *
 * maintains a set of icons which currently are or have been
 * used on the map during the last editing session. This information is
 * used in IconConditionController calling IconRegistry.getIcons() to
 * prepare values available in Filter Editor Dialog / find dialog when
 * filter on icons is selected
 *
 * 03.01.2009
 */
public class IconRegistry implements IExtension {
    final private SortedComboBoxModel<NamedIcon> mapIcons;
    final private SortedComboBoxModel<Tag> mapTags;

	public IconRegistry() {
		super();
		mapIcons = new SortedComboBoxModel<>();
		mapTags = new SortedComboBoxModel<>(Tag.class);
	}

    public void addIcon(final NamedIcon icon) {
        if(icon != null)
            mapIcons.add(icon);
    }


    public Tag createTag(String string) {
        Tag tag = new Tag(string);
        return registryTag(tag);
    }

    public Tag registryTag(Tag tag) {
        Tag registeredTag = mapTags.addIfNotExists(tag);
        return registeredTag;
    }

    private void addTag(final Tag tag) {
        if(tag != null && ! tag.isEmpty())
            mapTags.add(tag);
    }

    public SortedComboBoxModel<NamedIcon> getIconsAsListModel() {
        return mapIcons;
    }

    public SortedComboBoxModel<Tag> getTagsAsListModel() {
        return mapTags;
    }

	public void registryMapContent(final MapModel map) {
		final IconRegistry newRegistry = map.getIconRegistry();
		final SortedComboBoxModel<NamedIcon> newMapIcons = newRegistry.mapIcons;
		for (final NamedIcon uiIcon : newMapIcons) {
			mapIcons.add(uiIcon);
		}
	}

    public Tag setTagColor(String tagContent, String value) {
        return setTagColor(tagContent, Optional.ofNullable(value).map(ColorUtils::stringToColor));
    }

    public Tag setTagColor(String tagContent, Optional<Color> value) {
        Tag tag = createTag(tagContent);
        tag.setColor(value);
        return tag;
    }


    public Optional<Tag>getTag(Tag required) {
        return mapTags.getElement(required);
    }

    public Optional<Color>getTagColor(Tag required) {
        return getTag(required).flatMap(Tag::getColor);
    }

}
