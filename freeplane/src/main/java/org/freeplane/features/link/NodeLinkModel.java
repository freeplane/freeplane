/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry
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
package org.freeplane.features.link;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.freeplane.features.map.Clones;
import org.freeplane.features.map.NodeModel;

/**
 * @author Dimitry Polivaev
 * 08.08.2009
 */
public abstract class NodeLinkModel{

	private String targetID;
	final private NodeModel source;

	public NodeLinkModel(final NodeModel source, final String targetID) {
		this.targetID = targetID;
		this.source = source;
	}

	public NodeModel getSource() {
		return source;
	}

	public NodeModel getTarget() {
		return getSource().getMap().getNodeForID(getTargetID());
	}

	public String getTargetID() {
		return targetID;
	}

	void setTargetID(final String targetID) {
		this.targetID = targetID;
	}

	public boolean isSelfLink() {
		return getSource().createID().equals(getTargetID());
	}

    public Collection<NodeLinkModel> clones() {
	    final Clones sourceNodeClones = getSource().subtreeClones();
	    if(sourceNodeClones.size() == 1)
	    	return Arrays.<NodeLinkModel>asList(this);
	    ArrayList<NodeLinkModel> clones = new ArrayList<NodeLinkModel>(sourceNodeClones.size());
	    for(NodeModel sourceClone : sourceNodeClones) {
			final NodeLinkModel cloneForSource = cloneForSource(sourceClone);
			if(cloneForSource != null)
				clones.add(cloneForSource);
		}
	    return clones;
    }

    public abstract  NodeLinkModel cloneForSource(NodeModel sourceClone);
	public abstract NodeLinkModel cloneForSource(NodeModel sourceClone, String targetId);
}
