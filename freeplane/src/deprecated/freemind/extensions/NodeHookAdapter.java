/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is to be deleted.
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
/*
 * FreeMind - A Program for creating and viewing MindmapsCopyright (C) 2000-2001
 * Joerg Mueller <joergmueller@bigfoot.com>See COPYING for DetailsThis program
 * is free software; you can redistribute it and/ormodify it under the terms of
 * the GNU General Public Licenseas published by the Free Software Foundation;
 * either version 2of the License, or (at your option) any later version.This
 * program is distributed in the hope that it will be useful,but WITHOUT ANY
 * WARRANTY; without even the implied warranty ofMERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See theGNU General Public License for more details.You
 * should have received a copy of the GNU General Public Licensealong with this
 * program; if not, write to the Free SoftwareFoundation, Inc., 59 Temple Place
 * - Suite 330, Boston, MA 02111-1307, USA.
 */
/* $Id: NodeHookAdapter.java,v 1.1.4.4.2.2 2007/04/21 15:11:20 dpolivaev Exp $ */
package deprecated.freemind.extensions;

import org.freeplane.map.tree.MapModel;
import org.freeplane.map.tree.NodeModel;

/**
 * @author christianfoltin
 */
public abstract class NodeHookAdapter extends HookAdapter implements INodeHook {
	private NodeModel node;

	/**
	 *
	 */
	public NodeHookAdapter() {
		super();
	}

	/**
	 */
	protected MapModel getMap() {
		return node.getMap();
	}

	/**
	 */
	protected NodeModel getNode() {
		return node;
	}

	/*
	 * (non-Javadoc)
	 * @see freemind.modes.NodeHook#invoke()
	 */
	public void invoke(final NodeModel node) {
	}

	/**
	 */
	protected void nodeChanged(final NodeModel node) {
		getController().getMapController().nodeChanged(node);
	}

	/**
	 */
	public void setNode(final NodeModel node) {
		this.node = node;
	}
}
