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
package org.freeplane.features.link;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

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

	public static URI getLink(final NodeModel node) {
		final NodeLinks links = NodeLinks.getModel(node);
		return links != null ? links.getHyperLink() : null;
	}

	public static Boolean formatNodeAsHyperlink(final NodeModel node) {
		final NodeLinks links = NodeLinks.getModel(node);
		return links != null ? links.formatNodeAsHyperlink() : null;
	}

	public static String getLinkAsString(final NodeModel selectedNode) {
		final URI link = NodeLinks.getValidLink(selectedNode);
		return link != null ? link.toString() : null;
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
		return links != null ? links.getLinks() : Collections.<LinkModel> emptyList();
	}

	public static NodeLinks getModel(final NodeModel node) {
		final NodeLinks links = (NodeLinks) node.getExtension(NodeLinks.class);
		return links;
	}

	private URI hyperlink;
	private Boolean formatNodeAsHyperlink;
	final private LinkedList<LinkModel> links;

	public NodeLinks() {
		links = new LinkedList<LinkModel>();
	}

	public void addArrowlink(final NodeLinkModel newLink) {
		links.add(newLink);
		final MapModel map = newLink.getSource().getMap();
		addLinkToMap(map, newLink);
	}

	private void addLinkToMap(final MapModel map, final LinkModel newLink) {
		MapLinks mapLinks = MapLinks.getLinks(map);
		if (mapLinks == null) {
			mapLinks = new MapLinks();
			map.addExtension(mapLinks);
		}
		mapLinks.add(newLink);
	}

	/**
	 * @return
	 */
	public URI getHyperLink() {
		return hyperlink;
	}

	public Collection<LinkModel> getLinks() {
		return Collections.unmodifiableCollection(links);
	}

	public void removeArrowlink(final NodeLinkModel link) {
		final NodeModel node = link.getSource();
		final Iterator<LinkModel> iterator = NodeLinks.getLinkExtension(node).links.iterator();
		while (iterator.hasNext()) {
			final LinkModel i = iterator.next();
			if (i == link) {
				iterator.remove();
			}
		}
		final MapModel map = link.getSource().getMap();
		removeLinkFromMap(map, link);
	}

	private void removeLinkFromMap(final MapModel map, final LinkModel link) {
		final MapLinks mapLinks = MapLinks.getLinks(map);
		mapLinks.remove(link);
	}

	public String removeLocalHyperLink(final NodeModel node) {
		final Iterator<LinkModel> iterator = links.iterator();
		while (iterator.hasNext()) {
			final LinkModel link = iterator.next();
			if (link instanceof HyperTextLinkModel) {
				iterator.remove();
				removeLinkFromMap(node.getMap(), link);
				return link.getTargetID();
			}
		}
		return null;
	}

	public void setHyperLink(final URI hyperlink) {
		this.hyperlink = hyperlink;
	}

	public void setLocalHyperlink(final NodeModel node, final String targetID) {
		removeLocalHyperLink(node);
		if (targetID != null) {
			final HyperTextLinkModel link = new HyperTextLinkModel(node, targetID);
			links.add(link);
			addLinkToMap(node.getMap(), link);
		}
	}

	public static URI getValidLink(final NodeModel model) {
		final URI link = NodeLinks.getLink(model);
		if (link == null) {
			return null;
		}
		final String linkString = link.toString();
		if (linkString.startsWith("#")) {
			final String id = linkString.substring(1);
			if (model.getMap().getNodeForID(id) == null) {
				return null;
			}
		}
		return link;
	}
	
	public Boolean formatNodeAsHyperlink() {
    	return formatNodeAsHyperlink;
    }

	public void setFormatNodeAsHyperlink(Boolean formatNodeAsHyperlink) {
    	this.formatNodeAsHyperlink = formatNodeAsHyperlink;
    }

}
