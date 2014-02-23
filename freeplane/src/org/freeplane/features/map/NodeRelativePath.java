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


/**
 * @author Dimitry Polivaev
 * 23.02.2014
 */
public class NodeRelativePath {

	private final NodeModel commonAncestor;
	private final int[] beginPath;
	private final int[] endPath;

	public NodeRelativePath(NodeModel begin, NodeModel end) {
		final NodeAbsolutePath absoluteBeginPath = new NodeAbsolutePath(begin);
		final NodeAbsolutePath absoluteEndPath = new NodeAbsolutePath(end);
		NodeModel commonAncestor = null;
		NodeModel nodeOnBeginPath = null;
		while(absoluteBeginPath.hasNext() && absoluteEndPath.hasNext() && (nodeOnBeginPath = absoluteBeginPath.next()).equals(absoluteEndPath.next())){
			commonAncestor = nodeOnBeginPath;
		}
		int commonAncestorIndex = absoluteBeginPath.lastIndex();
		this.commonAncestor = commonAncestor;
		beginPath = path(absoluteBeginPath, commonAncestorIndex);
		endPath = path(absoluteEndPath, commonAncestorIndex);
    }

	private int [] path(final NodeAbsolutePath absolutePath, int startingIndex) {
	    int [] path = new int[absolutePath.size() - startingIndex - 1];
	    if(path.length > 0){
	    	for(int i = 0; i < path.length; i++){
	    		NodeModel nodeOnPath = absolutePath.next();
	    		path[i] = nodeOnPath.getParentNode().getIndex(nodeOnPath);
	    	}
	    }
		return path;
    }

	public NodeModel commonAncestor() {
		return commonAncestor;
    }

	public NodeModel pathBegin(NodeModel commonAncestor) {
		return relativeNode(commonAncestor, beginPath);
    }

	public NodeModel pathEnd(NodeModel commonAncestor) {
		return relativeNode(commonAncestor, endPath);
    }

	private NodeModel relativeNode(NodeModel commonAncestor, final int[] path) {
	    NodeModel relativeNode = commonAncestor;
		for(int childNumber : path)
	    	relativeNode = relativeNode.getChildAt(childNumber);
	    return relativeNode;
    }
}
