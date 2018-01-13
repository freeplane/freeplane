/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2018 dimitry
 *
 *  This file author is dimitry
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
package org.freeplane.plugin.collaboration.client.event.content.other;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.icon.HierarchicalIcons;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.FirstGroupNodeFlag;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.MapWriter;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.SummaryNodeFlag;
import org.freeplane.features.mode.MapExtensions;
import org.freeplane.plugin.collaboration.client.event.batch.UpdateBlockGeneratorFactory;
import org.freeplane.plugin.collaboration.client.event.batch.Updates;
import org.freeplane.plugin.collaboration.client.event.content.MapUpdateGenerator;
import org.freeplane.plugin.collaboration.client.event.content.NodeUpdateGenerator;

/**
 * @author Dimitry Polivaev
 * Jan 2, 2018
 */
public class ContentUpdateGenerator implements NodeUpdateGenerator, MapUpdateGenerator {
	public ContentUpdateGenerator(UpdateBlockGeneratorFactory updateBlockGeneratorFactory, MapWriter writer) {
		this(updateBlockGeneratorFactory, new ContentUpdateEventFactory(writer));
	}

	ContentUpdateGenerator(UpdateBlockGeneratorFactory updateBlockGeneratorFactory,
	                       ContentUpdateEventFactory eventFactory) {
		super();
		this.updateBlockGeneratorFactory = updateBlockGeneratorFactory;
		this.eventFactory = eventFactory;
	}

	final private ContentUpdateEventFactory eventFactory;
	final private UpdateBlockGeneratorFactory updateBlockGeneratorFactory;

	@Override
	public boolean handles(NodeChangeEvent event) {
		return true;
	}

	@Override
	public boolean handles(MapChangeEvent event) {
		return true;
	}

	@Override
	public void onNodeChange(NodeChangeEvent event) {
		final NodeModel node = event.getNode();
		onNodeChange(node);
	}

	private void onNodeChange(final NodeModel node) {
		getUpdates(node.getMap()).addUpdateEvent(node.createID(),
		    () -> eventFactory.createNodeContentUpdatedEvent(node));
	}

	@Override
	public void onMapChange(MapModel map) {
		getUpdates(map).addUpdateEvent("map", () -> eventFactory.createMapContentUpdatedEvent(map));
	}

	private static Collection<Class<? extends IExtension>> NODE_CONTENT_EXCLUSIONS = null;
	private static Collection<Class<? extends IExtension>> MAP_CONTENT = null;

	public static Collection<Class<? extends IExtension>> getNodeContentExclusions() {
		if (NODE_CONTENT_EXCLUSIONS == null) {
			NODE_CONTENT_EXCLUSIONS = new ArrayList<>(MapExtensions.getAll());
			Collections.addAll(NODE_CONTENT_EXCLUSIONS, HierarchicalIcons.ACCUMULATED_ICONS_EXTENSION_CLASS,
			    SummaryNodeFlag.class, FirstGroupNodeFlag.class, NodeLinks.class);
		}
		return NODE_CONTENT_EXCLUSIONS;
	}

	public static Collection<Class<? extends IExtension>> getMapContentExtensions() {
		if (MAP_CONTENT == null) {
			MAP_CONTENT = new ArrayList<>(MapExtensions.getAll());
		}
		return MAP_CONTENT;
	}

	private Updates getUpdates(MapModel map) {
		return updateBlockGeneratorFactory.of(map);
	}

	@Override
	public void onNewMap(MapModel map) {
		onMapChange(map);
	}

	@Override
	public void onNewNode(NodeModel node) {
		onNodeChange(node);
	}
}
