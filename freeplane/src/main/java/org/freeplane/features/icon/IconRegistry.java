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

import java.util.Map;
import java.util.Optional;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.core.util.collection.SortedComboBoxModel;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.styles.MapStyle;

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
public class IconRegistry implements IExtension, IMapChangeListener{
    final private SortedComboBoxModel<NamedIcon> mapIcons;
    final private SortedComboBoxModel<Tag> mapTags;
    public static final String TAG_COLOR_PROPERTY_PREFIX = "tag.color.";

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

    @Override
    public void mapChanged(MapChangeEvent event) {
        String property = (String)event.getProperty();
        if(event.getSource().getClass().equals(MapStyle.class)
                && event.getProperty() instanceof String
                && property.startsWith(TAG_COLOR_PROPERTY_PREFIX)) {
            String tagContent = property.substring(TAG_COLOR_PROPERTY_PREFIX.length());
            String value = (String)event.getNewValue();
            setTagColorByProperty(tagContent, value);
        }
    }

    public void setTagColorByProperty(String tagContent, String value) {
        createTag(tagContent).setColor(Optional.ofNullable(value).map(ColorUtils::stringToColor));
    }


}
