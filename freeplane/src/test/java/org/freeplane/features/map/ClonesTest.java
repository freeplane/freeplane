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

import static org.freeplane.features.map.NodeModel.CloneType.TREE;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.hamcrest.collection.IsEmptyIterable;
import org.junit.Test;

/**
 * @author Dimitry Polivaev
 * 16.02.2014
 */
public class ClonesTest {
	MapModel map = null;

	private NodeModel root() {
	    final NodeModel parent = new NodeModel("parent", map);
		parent.setClones(new SingleNodeList(parent, TREE));
	    return parent;
    }

	@Test
	public void clonesContainsNothingBeforeAddingToTree() {
		final NodeModel node = new NodeModel("node", map);
		assertThat(node.subtreeClones(), IsEmptyIterable.<NodeModel>emptyIterable());

	}

	@Test
	public void clonesContainsNodeNothingAfterAddingToDetachedParent() {
		final NodeModel parent = new NodeModel("parent", map);
		final NodeModel node = new NodeModel("node", map);
		parent.insert(node);
		assertThat(node.subtreeClones(), IsEmptyIterable.<NodeModel>emptyIterable());
	}

	@Test
	public void clonesContainsNodeItselfAfterAddingToAttachedParent() {
		final NodeModel parent = root();
		final NodeModel node = new NodeModel("node", map);
		parent.insert(node);
		assertThat(node.subtreeClones(), contains(node));
	}

	@Test
	public void clonesOfChildContainsNodeItselfAfterAddingToAttachedParent() {
		final NodeModel parent = root();
		final NodeModel node = new NodeModel("node", map);
		final NodeModel child = new NodeModel("child", map);
		node.insert(child);
		parent.insert(node);
		assertThat(child.subtreeClones(), contains(child));
	}

	@Test
	public void clonesContainsNodeNothingAfterRemovingFromParent() {
		final NodeModel parent = root();
		final NodeModel node = new NodeModel("node", map);
		parent.insert(node);
		parent.remove(parent.getIndex(node));
		assertThat(node.subtreeClones(), IsEmptyIterable.<NodeModel>emptyIterable());
	}

	@Test
	public void clonesOfChildContainsNothingAfterRemovingFromParent() {
		final NodeModel parent = root();
		final NodeModel node = new NodeModel("node", map);
		parent.insert(node);
		final NodeModel child = new NodeModel("child", map);
		node.insert(child);
		parent.remove(parent.getIndex(node));
		assertThat(child.subtreeClones(), IsEmptyIterable.<NodeModel>emptyIterable());
	}

	@Test
	public void multipleClonesAfterAddingToAttachedParent() {
		final NodeModel parent = root();
		final NodeModel node = new NodeModel("node", map);
		parent.insert(node);
		final NodeModel clone = node.cloneTree();
		parent.insert(clone);
		assertThat(node.subtreeClones(), contains(node, clone));
		assertThat(node.allClones(), contains(node, clone));
	}

	@Test
	public void removedClone() {
		final NodeModel parent = root();
		final NodeModel node = new NodeModel("node", map);
		parent.insert(node);
		final NodeModel clone = node.cloneTree();
		parent.insert(clone);
		parent.remove(parent.getIndex(clone));
		assertThat(node.subtreeClones(), contains(node));
		assertThat(node.allClones(), not(contains(clone)));
	}
	@Test
	public void subtreeContainsClone() {
		final NodeModel parent = root();
		final NodeModel node = new NodeModel("node", map);
		parent.insert(node);
		final NodeModel clone = node.cloneTree();
		parent.insert(clone);
		final NodeModel child = new NodeModel("child", map);
		node.insert(child);
		clone.insert(child.cloneTree());
		assertThat(clone.subtreeContainsCloneOf(child), is(true));
	}

}
