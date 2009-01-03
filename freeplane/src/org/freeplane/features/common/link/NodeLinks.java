/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.features.common.link;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.map.MapModel;
import org.freeplane.core.map.NodeModel;

/**
 * @author Dimitry Polivaev
 */
public class NodeLinks implements IExtension {
	/**
	 * @param source2
	 * @return
	 */
	public static NodeLinks createLinkExtension(final NodeModel node) {
		NodeLinks linkExtension = NodeLinks.getLinkExtension(node);
		if (linkExtension != null) {
			return linkExtension;
		}
		linkExtension = new NodeLinks();
		node.addExtension(linkExtension);
		return linkExtension;
	}

	public static String getLink(final NodeModel node) {
		final NodeLinks links = NodeLinks.getModel(node);
		return links != null ? links.getLink() : null;
	}

	/**
	 * @param node
	 * @return
	 */
	public static NodeLinks getLinkExtension(final NodeModel node) {
		return (NodeLinks) node.getExtension(NodeLinks.class);
	}

	public static Collection<LinkModel> getLinks(final NodeModel node) {
		final NodeLinks links = NodeLinks.getLinkExtension(node);
		return links != null ? links.getLinks() : Collections.EMPTY_LIST;
	}

	public static NodeLinks getModel(final NodeModel node) {
		final NodeLinks links = (NodeLinks) node.getExtension(NodeLinks.class);
		return links;
	}

	private String link;
	final private LinkedList<LinkModel> links;

	public NodeLinks() {
		links = new LinkedList<LinkModel>();
	}

	public void addArrowlink(final ArrowLinkModel newLink) {
		final String targetID = newLink.getTargetID();
		final Iterator<LinkModel> iterator = links.iterator();
		while (iterator.hasNext()) {
			final LinkModel link = iterator.next();
			if (link instanceof ArrowLinkModel && link.getTargetID().equals(targetID)) {
				return;
			}
		}
		links.add(newLink);
		final MapModel map = newLink.getSource().getMap();
		MapLinks mapLinks = (MapLinks) map.getExtension(MapLinks.class);
		if (mapLinks == null) {
			mapLinks = new MapLinks();
			map.addExtension(mapLinks);
		}
		mapLinks.add(newLink);
	}

	/**
	 * @return
	 */
	public String getLink() {
		return link;
	}

	public Collection<LinkModel> getLinks() {
		return Collections.unmodifiableCollection(links);
	}

	public void removeArrowlink(final ArrowLinkModel link) {
		final NodeModel node = link.getSource();
		final Iterator<LinkModel> iterator = NodeLinks.getLinkExtension(node).links.iterator();
		while (iterator.hasNext()) {
			final LinkModel i = iterator.next();
			if (i == link) {
				iterator.remove();
			}
		}
		final MapModel map = link.getSource().getMap();
		final MapLinks mapLinks = (MapLinks) map.getExtension(MapLinks.class);
		mapLinks.remove(link);
	}

	public String removeLocalHyperLink() {
		final Iterator<LinkModel> iterator = links.iterator();
		while (iterator.hasNext()) {
			final LinkModel link = iterator.next();
			if (link instanceof HyperTextLinkModel) {
				iterator.remove();
				return link.getTargetID();
			}
		}
		return null;
	}

	public void setLink(final String link) {
		this.link = link;
	}

	public void setLocalHyperlink(final String targetID) {
		removeLocalHyperLink();
		if (targetID != null) {
			links.add(new HyperTextLinkModel(targetID));
		}
	}
}
