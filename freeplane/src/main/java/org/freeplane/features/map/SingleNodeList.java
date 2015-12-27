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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.freeplane.features.map.NodeModel.CloneType;

/**
 * @author Dimitry Polivaev
 * 09.02.2014
 */
public class SingleNodeList implements Clones{
	private final NodeModel nodeModel;
	private final CloneType cloneType;

	SingleNodeList(NodeModel nodeModel, CloneType cloneType) {
		this.nodeModel = nodeModel;
		this.cloneType = cloneType;
	}

	public Clones add(NodeModel nodeModel) {
		final MultipleNodeList multipleNodeList = new MultipleNodeList(cloneType);
		multipleNodeList.add(this.nodeModel);
		multipleNodeList.add(nodeModel);
		return multipleNodeList;
	}

	public Clones remove(NodeModel nodeModel) {
		return null;
	}

	public int size() {
	    return 1;
    }

	public Iterator<NodeModel> iterator() {
	    return Arrays.asList(nodeModel).iterator();
    }

	public void attach() {
		throw new IllegalStateException();
    }

	public void detach(NodeModel nodeModel) {
	    nodeModel.setClones(new DetachedNodeList(nodeModel, cloneType));
    }

	public Collection<NodeModel> toCollection() {
	    return Arrays.asList(nodeModel);
    }

	public boolean contains(NodeModel node) {
	    return nodeModel.equals(node);
    }

	public NodeModel head() {
	    return nodeModel;
    }

	public CloneType getCloneType() {
		return cloneType;
	}
}
