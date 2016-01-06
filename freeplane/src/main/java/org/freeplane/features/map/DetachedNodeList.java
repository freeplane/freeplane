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
import java.util.Collections;
import java.util.Iterator;

import org.freeplane.features.map.NodeModel.CloneType;

/**
 * @author Dimitry Polivaev
 * 16.02.2014
 */
public class DetachedNodeList implements Clones {
	private final NodeModel clonedNode;
	private final NodeModel clone;
	private final CloneType cloneType;

	public DetachedNodeList(NodeModel clonedNode, CloneType cloneType) {
		this(clonedNode, clonedNode, cloneType);
    }
	
	public DetachedNodeList forClone(NodeModel node){
		return new DetachedNodeList(node, clone == clonedNode ? node : clonedNode, cloneType);
	}

	public DetachedNodeList(NodeModel clone, NodeModel clonedNode, CloneType cloneType) {
		this.clone = clone;
		this.clonedNode = clonedNode;
		this.cloneType = cloneType;
    }

	public Iterator<NodeModel> iterator() {
		return Collections.<NodeModel>emptyList().iterator();
	}

	public int size() {
		return 0;
	}

	public void attach() {
		if(clonedNode == clone)
			clone.setClones(new SingleNodeList(clone, cloneType));
		else if (! clonedNode.isAttached()){
			clone.setClones(new SingleNodeList(clone, cloneType));
			clonedNode.setClones(new DetachedNodeList(clonedNode, clone, cloneType));
		}
        else {
	        final Clones clonesWithNewClone = clonedNode.clones(cloneType).add(clone);
	        clonedNode.setClones(clonesWithNewClone);
	        clone.setClones(clonesWithNewClone);
        }
    }

	public void detach(NodeModel nodeModel) {
		throw new IllegalStateException();
    }

	public Clones add(NodeModel clone) {
		throw new IllegalStateException();
    }

	public Collection<NodeModel> toCollection() {
	    return Collections.<NodeModel>emptyList();
    }

	public boolean contains(NodeModel node) {
	    return false;
    }

	public NodeModel otherThan(NodeModel node) {
		throw new IllegalStateException();
    }

	public NodeModel head() {
		throw new IllegalStateException();
    }

	public CloneType getCloneType() {
		return cloneType;
	}
}
