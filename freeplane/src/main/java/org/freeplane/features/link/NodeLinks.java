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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

/**
 * @author Dimitry Polivaev
 */
public class NodeLinks implements IExtension {
	public static final Object CONNECTOR = "connector";

	/**
	 * @param source2
	 * @return
	 */
	public static NodeLinks createLinkExtension(final NodeModel node) {
		NodeLinks nodeLinks = NodeLinks.getLinkExtension(node);
		if (nodeLinks != null) {
			return nodeLinks;
		}
		nodeLinks = new NodeLinks();
		node.addExtension(nodeLinks);
		return nodeLinks;
	}

	public static URI getLink(final NodeModel node) {
		final NodeLinks links = NodeLinks.getLinkExtension(node);
		return links != null ? links.getHyperLink(node) : null;
	}

	public static Boolean formatNodeAsHyperlink(final NodeModel node) {
		final NodeLinks links = NodeLinks.getLinkExtension(node);
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
		return node.getExtension(NodeLinks.class);
	}

	public static Collection<NodeLinkModel> getLinks(final NodeModel node) {
		final NodeLinks links = NodeLinks.getLinkExtension(node);
		if (links != null) {
	        final Collection<NodeLinkModel> sharedLinks = links.getLinks();
	        final ArrayList<NodeLinkModel> clones = new ArrayList<NodeLinkModel>(sharedLinks.size());
	        for(NodeLinkModel sharedLink : sharedLinks){
				final NodeLinkModel cloneForSource = sharedLink.cloneForSource(node);
				if(cloneForSource != null)
					clones.add(cloneForSource);
	        }
	        return clones;
        }
        else
			return Collections.<NodeLinkModel> emptyList();
	}

	private URI nonLocalHyperlink;
	private Boolean formatNodeAsHyperlink;
	final private LinkedList<NodeLinkModel> links;
	//DOCEAR - fixed: new property type for node link changes
	static public final Object HYPERLINK_CHANGED = "hyperlink_changed";

	public NodeLinks() {
		links = new LinkedList<NodeLinkModel>();
	}

	public void addArrowlink(final NodeLinkModel newLink) {
		links.add(newLink);
		final MapModel map = newLink.getSource().getMap();
		addLinkToMap(map, newLink);
	}

	private void addLinkToMap(final MapModel map, final NodeLinkModel newLink) {
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
	public URI getHyperLink(NodeModel clone) {
		if(nonLocalHyperlink != null)
			return nonLocalHyperlink;
		final Iterator<NodeLinkModel> iterator = links.iterator();
		while (iterator.hasNext()) {
			final NodeLinkModel link = iterator.next();
			if (link instanceof HyperTextLinkModel) {
				try {
	                return new URI("#" + link.getTargetID());
                }
                catch (URISyntaxException e) {
	                LogUtils.severe(e);
                }
			}
		}
		return null;
	}

	public List<NodeLinkModel> getLinks() {
		return Collections.unmodifiableList(links);
	}

	public void removeArrowlink(final NodeLinkModel link) {
		final NodeModel node = link.getSource();
		for (final NodeLinkModel i : NodeLinks.getLinkExtension(node).links) {
			if (link.equals(i.cloneForSource(link.getSource()))) {
				links.remove(i);
				final MapModel map = link.getSource().getMap();
				removeLinkFromMap(map, i);
				return;
			}
		}
	}

	private void removeLinkFromMap(final MapModel map, final NodeLinkModel link) {
		final MapLinks mapLinks = MapLinks.getLinks(map);
		mapLinks.remove(link);
	}

	public String removeLocalHyperLink(final NodeModel node) {
		final Iterator<NodeLinkModel> iterator = links.iterator();
		while (iterator.hasNext()) {
			final NodeLinkModel link = iterator.next();
			if (link instanceof HyperTextLinkModel) {
				iterator.remove();
				removeLinkFromMap(node.getMap(), link);
				return link.getTargetID();
			}
		}
		return null;
	}

	public void setHyperLink(final URI hyperlink) {
		this.nonLocalHyperlink = hyperlink;
	}

	public void setLocalHyperlink(final NodeModel node, final String targetID) {
		this.nonLocalHyperlink = null;
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
		if (linkString.startsWith("#ID")) {
			final String id = linkString.substring(1);
			if (model.getMap().getNodeForID_(id) == null) {
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

	public void replaceMapLinksForDeletedSourceNode(MapLinks mapLinks, final NodeModel deletionRoot, NodeModel node) {
		final ListIterator<NodeLinkModel> linkIterator = links.listIterator();
		LINKS: while (linkIterator.hasNext()) {
			NodeLinkModel link = linkIterator.next();
			final NodeModel linkSource = link.getSource();
			if(linkSource.equals(node)) {
				mapLinks.remove(link);
				for(NodeModel newSource : node.subtreeClones()){
					if(node != newSource && ! newSource.isDescendantOf(deletionRoot)) {
						final NodeLinkModel cloneForSource = link.cloneForSource(newSource);
						if(cloneForSource != null){
							linkIterator.remove();
							linkIterator.add(cloneForSource);
							mapLinks.add(cloneForSource);
							continue LINKS;
						}
					}
				}
			}
		}
	}
}
