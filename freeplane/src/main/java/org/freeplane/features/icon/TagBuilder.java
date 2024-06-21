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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IAttributeHandler;
import org.freeplane.core.io.IExtensionAttributeWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.features.map.NodeBuilder;
import org.freeplane.features.map.NodeModel;

class TagBuilder implements IExtensionAttributeWriter {
	private void registerAttributeHandlers(final ReadManager reader) {
		final IAttributeHandler tagsHandler = new IAttributeHandler() {
            @Override
            public void setAttribute(final Object userObject, final String value) {
                final NodeModel node = (NodeModel) userObject;
                TagCategories tagCategories = node.getMap().getIconRegistry().getTagCategories();
                Tags.setTagReferences(node, Stream.of(value.split("\n"))
                        .map(tagCategories::createTagReference).collect(Collectors.toList()));
            }
        };
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "TAGS", tagsHandler);
		reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, "TAGS", tagsHandler);
	}

    void registerBy(final ReadManager readManager, final WriteManager writeManager) {
		registerAttributeHandlers(readManager);
		writeManager.addExtensionAttributeWriter(Tags.class, this);
	}

	@Override
    public void writeAttributes(final ITreeWriter writer, final Object userObject, final IExtension extension) {
		final Tags tagsExtension = (Tags) extension;
        List<Tag> tags = tagsExtension.getTags();
        if (! tags.isEmpty()) {
            writer.addAttribute("TAGS", tags.stream().map(Tag::getContent).collect(Collectors.joining("\n")));
        }
 	}
}
