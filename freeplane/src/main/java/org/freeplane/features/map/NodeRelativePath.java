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
import java.util.Comparator;
import java.util.HashMap;


/**
 * @author Dimitry Polivaev
 * 23.02.2014
 */
public class NodeRelativePath {

	private final NodeModel commonAncestor;
	private final int[] beginPath;
	private final int[] endPath;
	
	public final static Comparator<NodeModel> comparator() {
		return new Comparator<NodeModel>() {
			private final HashMap<NodeModel, NodeAbsolutePath> paths = new HashMap<>(); 

			@Override
			public int compare(NodeModel o1, NodeModel o2) {
				if(o1 == o2)
					return 0;
				if(o1 == null)
					return -1;
				final NodeAbsolutePath absoluteBeginPath = getPath(o1);
				final NodeAbsolutePath absoluteEndPath = getPath(o2);
				return new NodeRelativePath(absoluteBeginPath, absoluteEndPath).compareNodePositions();
			}

			public NodeAbsolutePath getPath(NodeModel node) {
				NodeAbsolutePath path = paths.get(node);
				if(path == null) {
					path = new NodeAbsolutePath(node);
					paths.put(node, path);
				}
				return path;
			}
		};
	}

	public NodeRelativePath(NodeModel begin, NodeModel end) {
		this(new NodeAbsolutePath(begin), new NodeAbsolutePath(end));
	}
		
	public NodeRelativePath(NodeAbsolutePath absoluteBeginPath, NodeAbsolutePath absoluteEndPath) {
		NodeModel commonAncestor = null;
		while(absoluteBeginPath.hasNext() && absoluteEndPath.hasNext()){
			 NodeModel nextNodeOnBeginPath = absoluteBeginPath.next();
			 if(nextNodeOnBeginPath.equals(absoluteEndPath.next())){
				 commonAncestor = nextNodeOnBeginPath;
			 }
			 else{
				 absoluteBeginPath.previous();
				 absoluteEndPath.previous();
				 break;
			 }
		}
		this.commonAncestor = commonAncestor;
		int commonAncestorIndex = absoluteBeginPath.lastIndex();
		beginPath = path(absoluteBeginPath, commonAncestorIndex);
		endPath = path(absoluteEndPath, commonAncestorIndex);
    }

	private int [] path(final NodeAbsolutePath absolutePath, int startingIndex) {
	    int [] path = new int[absolutePath.size() - startingIndex - 1];
	    if(path.length > 0){
	    	for(int i = 0; i < path.length; i++){
	    		NodeModel nodeOnPath = absolutePath.next();
	    		final NodeModel parentNode = nodeOnPath.getParentNode();
				path[i] = parentNode.getIndex(nodeOnPath);
	    	}
	    }
		return path;
    }

	public NodeModel commonAncestor() {
		return commonAncestor;
    }

	public NodeModel pathBegin(NodeModel commonAncestor) {
		return relativeNode(commonAncestor, beginPath, beginPath.length);
    }

	public NodeModel pathEnd(NodeModel commonAncestor) {
		return relativeNode(commonAncestor, endPath, endPath.length);
    }

	private NodeModel relativeNode(NodeModel commonAncestor, final int[] path, int level) {
	    NodeModel relativeNode = commonAncestor;
		for(int position = 0; position < level; position++){
	    	relativeNode = relativeNode.getChildAt(path[position]);
		}
	    return relativeNode;
    }

	public NodeModel ancestorForBegin(NodeModel begin) {
		final int backLevelNumber = beginPath.length;
		return ancestor(begin, backLevelNumber);
	    
    }

	private NodeModel ancestor(NodeModel source, final int backLevelNumber) {
	    NodeModel ancestor = source;
		for(int i = 0; i < backLevelNumber; i++){
			if(ancestor == null)
				return ancestor;
	    	ancestor = ancestor.getParentNode();
		}
	    return ancestor;
    }

	public boolean equalPathsTo(NodeRelativePath nodeRelativePath2) {
	    return Arrays.equals(beginPath, nodeRelativePath2.beginPath) && Arrays.equals(endPath, nodeRelativePath2.endPath);
    }

	public NodeModel beginPathElement(int level) {
		return relativeNode(commonAncestor, beginPath, level);
	}

	public NodeModel endPathElement(int level) {
		return relativeNode(commonAncestor, endPath, level);
	}

	public int getPathLength() {
		return beginPath.length + endPath.length;
	}

	public int compareNodePositions() {
		if(beginPath.length == 0 && endPath.length == 0)
			return 0;
		if(beginPath.length == 0) 
			return -1;
		if(endPath.length == 0) 
			return 1;
		return Integer.compare(beginPath[0], endPath[0]);
	}
	
}
