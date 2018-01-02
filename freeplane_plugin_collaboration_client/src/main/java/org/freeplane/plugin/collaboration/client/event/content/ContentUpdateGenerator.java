/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2017 dimitry
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
package org.freeplane.plugin.collaboration.client.event.content;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.icon.HierarchicalIcons;
import org.freeplane.features.map.FirstGroupNodeFlag;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.SummaryNodeFlag;
import org.freeplane.features.mode.MapExtensions;
import org.freeplane.plugin.collaboration.client.event.batch.Updates;

/**
 * @author Dimitry Polivaev
 * Dec 4, 2017
 */
public class ContentUpdateGenerator {

	private Updates updates;
	private ContentUpdateEventFactory eventFactory;
	private static Collection<Class<? extends IExtension>> NODE_CONTENT_EXCLUSIONS =  null;
	private static Collection<Class<? extends IExtension>> MAP_CONTENT =  null;

	public ContentUpdateGenerator(Updates updates, ContentUpdateEventFactory eventFactory) {
		this.updates = updates;
		this.eventFactory = eventFactory;
	}

	public void onNodeContentUpdate(NodeModel node) {
		updates.addUpdateEvent(() -> eventFactory.createNodeContentUpdatedEvent(node));

	}

	public void onMapContentUpdate(MapModel map) {
			updates.addUpdateEvent(() -> eventFactory.createMapContentUpdatedEvent(map));

	}

	public void onNodeCoreContentUpdate(NodeModel node) {
			updates.addUpdateEvent(() -> eventFactory.createCoreContentUpdatedEvent(node));

	}
	public static Collection<Class<? extends IExtension>> getNodeContentExclusions() {
		if(NODE_CONTENT_EXCLUSIONS == null) {
			NODE_CONTENT_EXCLUSIONS =  new ArrayList<>(MapExtensions.getAll());
			Collections.addAll(NODE_CONTENT_EXCLUSIONS, 
				HierarchicalIcons.ACCUMULATED_ICONS_EXTENSION_CLASS, 
				SummaryNodeFlag.class, 
				FirstGroupNodeFlag.class
				);
		}
		return NODE_CONTENT_EXCLUSIONS;
	}

	public static Collection<Class<? extends IExtension>> getMapContentExtensions() {
		if(MAP_CONTENT == null) {
			MAP_CONTENT =  new ArrayList<>(MapExtensions.getAll());
		}
		return MAP_CONTENT;
	}
}
