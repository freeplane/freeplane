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

import org.freeplane.features.map.NodeModel;

/**
 * @author Dimitry Polivaev
 */
public class HyperTextLinkModel extends NodeLinkModel {
	/**
	 * @param source
	 * @param target
	 */
	public HyperTextLinkModel(final NodeModel source, final String targetID) {
		super(source, targetID);
	}
	
	public NodeLinkModel cloneForSource(NodeModel sourceClone, String targetId) {
	    return new HyperTextLinkModel(sourceClone, targetId);
    }
	
	@Override
    public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + getSource().hashCode();
	    final String targetID = getTargetID();
	    if(targetID == null)
	    	return result;
		result = prime * result + targetID.hashCode();
		return result;
    }

	@Override
    public boolean equals(Object obj) {
	    if (this == obj)
		    return true;
	    if (obj == null)
		    return false;
	    if (getClass() != obj.getClass())
		    return false;
	    NodeLinkModel other = (NodeLinkModel) obj;
	    if (!getSource().equals(other.getSource()))
	        return false;
	    final String targetID = getTargetID();
	    if(targetID == null)
	    	return other.getTargetID() == null;
	    else
	    	return targetID.equals(other.getTargetID());
    }

	public NodeLinkModel cloneForSource(NodeModel sourceClone) {
		final NodeModel source = getSource();
		if(sourceClone == source)
			return this;
		final NodeModel target = getTarget();
		if(target != null){
	        return cloneForSource(sourceClone, target.getID());
		}
		return null;
	}
}
