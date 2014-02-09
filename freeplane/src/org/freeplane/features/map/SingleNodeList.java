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

/**
 * @author Dimitry Polivaev
 * 09.02.2014
 */
public class SingleNodeList implements NodeList{
	private final NodeModel nodeModel;

	SingleNodeList(NodeModel nodeModel) {
		this.nodeModel = nodeModel;
	}

	public NodeList add(NodeModel nodeModel) {
		final MultipleNodeList multipleNodeList = new MultipleNodeList();
		multipleNodeList.add(this.nodeModel);
		multipleNodeList.add(nodeModel);
		return multipleNodeList;
	}

	public NodeList remove(NodeModel nodeModel) {
		return null;
	}

	public Iterable<NodeModel> all() {
		return Arrays.asList(nodeModel);
	}

	public int size() {
	    return 1;
    }
}
