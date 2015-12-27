/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2014 Dimitry
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
package org.freeplane.features.map;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.freeplane.features.map.NodeModel.CloneType;

/**
 * @author Dimitry Polivaev
 * 09.02.2014
 */
public class MultipleNodeList implements Clones {
	private final CloneType cloneType;
	public MultipleNodeList(CloneType cloneType) {
		this.cloneType = cloneType;
	}

	LinkedList<NodeModel> nodes = new LinkedList<NodeModel>();
	public Clones add(NodeModel nodeModel) {
		nodes.add(nodeModel);
		return this;
	}

	public Clones remove(NodeModel nodeModel) {
		nodes.remove(nodeModel);
		if(nodes.size() == 1)
			return new SingleNodeList(head(), cloneType);
		else
			return this;

	}

	public Iterable<NodeModel> all() {
		return nodes;
	}

	public Iterator<NodeModel> iterator() {
	    return nodes.iterator();
    }
	public int size() {
	    return nodes.size();
    }

	public void attach() {
		throw new IllegalStateException();

    }

	public void detach(NodeModel nodeModel) {
		final Clones reducedClones = remove(nodeModel);
		final NodeModel head = head();
		nodeModel.setClones(new DetachedNodeList(nodeModel, head, cloneType));
		head.setClones(reducedClones);
    }

	public Collection<NodeModel> toCollection() {
	    return nodes;
    }

	public boolean contains(NodeModel node) {
	    return node != null && head().equals(node.clones(cloneType).head());
    }

	public NodeModel head() {
	    return nodes.get(0);
    }

	public CloneType getCloneType() {
		return cloneType;
	}
}
