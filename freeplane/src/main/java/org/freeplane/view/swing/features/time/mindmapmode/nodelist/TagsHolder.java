/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2013 Dimitry
 *
 *  This file author is Dimitry
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
package org.freeplane.view.swing.features.time.mindmapmode.nodelist;

import java.util.List;
import java.util.stream.Collectors;

import org.freeplane.core.util.collection.ListComparator;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.Tag;
import org.freeplane.features.map.NodeModel;
/**
 * @author Dimitry Polivaev
 * 03.10.2013
 */
class TagsHolder extends TextHolder {
	private List<Tag> tags = null;
	private final NodeModel node;
	private boolean showsTagCategories;

	public TagsHolder(final NodeModel node, boolean showsTagCategories) {
		this.node = node;
		this.showsTagCategories = showsTagCategories;
		setTextAccessor(new TextAccessor() {

            @Override
            public void setText(String newText) {
            }

            @Override
            public String getText() {
                return getTags().stream().map(Tag::getContent).collect(Collectors.joining("\n"));
            }

            @Override
            public NodeModel getNode() {
                return node;
            }
        });
	}

	private void initialize() {
		if(tags != null)
			return;
		final IconController iconController = IconController.getController();
        tags = (showsTagCategories
				? iconController.getTagsWithCategories(node)
				: iconController.getTags(node))
                .stream()
                .filter(tag -> ! tag.isEmpty())
                .collect(Collectors.toList());

	}

	@Override
    public int compareTo(final TextHolder compareToObject) {
		return ListComparator.compareLists(tags, ((TagsHolder)compareToObject).tags);
	}

	public List<Tag> getTags() {
		initialize();
		return tags;
	}

}