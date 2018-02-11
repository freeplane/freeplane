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
package org.freeplane.plugin.collaboration.client.event.content.links;

import java.net.URI;
import java.util.Collection;
import java.util.Optional;

import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.NodeLinkModel;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.collaboration.client.event.batch.UpdatesAccessor;
import org.freeplane.plugin.collaboration.client.event.content.NodeUpdateGenerator;

/**
 * @author Dimitry Polivaev
 * Jan 2, 2018
 */
public class LinkUpdateGenerator implements NodeUpdateGenerator {
	public LinkUpdateGenerator(UpdatesAccessor updates, LinkController linkController) {
		this(updates, linkController, new LinkUpdateEventFactory());
	}
	
	LinkUpdateGenerator(UpdatesAccessor updates, LinkController linkController, LinkUpdateEventFactory eventFactory) {
		super();
		this.linkController = linkController;
		this.updates = updates;
		this.eventFactory = eventFactory;
	}
	final private LinkUpdateEventFactory eventFactory;
	final private UpdatesAccessor updates;
	final private LinkController linkController;
	
	public boolean handles(NodeChangeEvent event) {
		final Object property = event.getProperty();
		return NodeLinks.CONNECTOR.equals(property) || NodeLinks.HYPERLINK_CHANGED.equals(property);
	}
	
	public void onNodeChange(NodeChangeEvent event) {
		final Object property = event.getProperty();
		if(NodeLinks.CONNECTOR.equals(property)) {
			onConnectorChange(event);
		}
		else if(NodeLinks.HYPERLINK_CHANGED.equals(property)) {
			onHyperlinkChange(event.getNode(), (URI) event.getNewValue());
		}
	}

	private void onHyperlinkChange(NodeModel node, URI uri) {
		updates.of(node.getMap())
		.addUpdateEvent(node.createID(), () -> eventFactory.createHyperlinkChangedEvent(node, Optional.ofNullable(uri)));

	}

	private void onConnectorChange(NodeChangeEvent event) {
		final NodeModel node = event.getNode();
		ConnectorModel before = (ConnectorModel) event.getOldValue();
		ConnectorModel after = (ConnectorModel) event.getNewValue();
		onConnectorChange(node, before, after);
	}

	private void onConnectorChange(final NodeModel node, ConnectorModel before, ConnectorModel after) {
		if(before == null && after != null) {
			createConnectorAddedEvent(node, after);
			createConnectorUpdatedEvent(node, after);
		} else if(before != null && after != null)
			createConnectorUpdatedEvent(node, after);
		else if(before != null && after == null)
			createConnectorRemovedEvent(node, before);
	}

	private void createConnectorRemovedEvent(final NodeModel node, ConnectorModel connector) {
		updates.of(node.getMap())
		.addUpdateEvent(node.createID(), () -> eventFactory.createConnectorRemovedEvent(node, connector));
	}

	private void createConnectorUpdatedEvent(final NodeModel node, ConnectorModel connector) {
		updates.of(node.getMap())
			.addUpdateEvent(node.createID(), () -> eventFactory.createConnectorUpdatedEvent(node, connector));
	}

	private void createConnectorAddedEvent(final NodeModel node, ConnectorModel connector) {
		updates.of(node.getMap())
			.addUpdateEvent(node.createID(), () -> eventFactory.createConnectorAddedEvent(node, connector));
	}

	@Override
	public void onNewNode(NodeModel node) {
		final Collection<NodeLinkModel> links = linkController.getLinksFrom(node);
		for(NodeLinkModel link : links) {
			if(link instanceof ConnectorModel) {
				createConnectorAddedEvent(node, (ConnectorModel)link);
				createConnectorUpdatedEvent(node, (ConnectorModel)link);
			}
		}
		final URI link = linkController.getLink(node);
		if(link != null)
			onHyperlinkChange(node, link);
	}
}