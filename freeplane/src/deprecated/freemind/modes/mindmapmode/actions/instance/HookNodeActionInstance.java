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
package deprecated.freemind.modes.mindmapmode.actions.instance;

import java.util.ArrayList;

public class HookNodeActionInstance extends NodeActionInstance {
	private String hookName;
	final private ArrayList nodeListMemberList = new ArrayList();

	public void addAtNodeListMember(
	                                final int position,
	                                final NodeListMemberActionInstance nodeListMember) {
		nodeListMemberList.add(position, nodeListMember);
	}

	public void addNodeListMember(
	                              final NodeListMemberActionInstance nodeListMember) {
		nodeListMemberList.add(nodeListMember);
	}

	public void clearNodeListMemberList() {
		nodeListMemberList.clear();
	}

	public String getHookName() {
		return hookName;
	}

	public java.util.List getListNodeListMemberList() {
		return java.util.Collections.unmodifiableList(nodeListMemberList);
	}

	public NodeListMemberActionInstance getNodeListMember(final int index) {
		return (NodeListMemberActionInstance) nodeListMemberList.get(index);
	}

	public void setHookName(final String hookName) {
		this.hookName = hookName;
	}

	public int sizeNodeListMemberList() {
		return nodeListMemberList.size();
	}
}
