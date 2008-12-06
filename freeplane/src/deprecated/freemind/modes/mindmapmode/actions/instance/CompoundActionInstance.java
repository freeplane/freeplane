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

public class CompoundActionInstance implements ActionInstance {
	final private ArrayList choiceList = new ArrayList();

	public void addAtChoice(final int position, final Object choice) {
		choiceList.add(position, choice);
	}

	public void addChoice(final Object choice) {
		choiceList.add(choice);
	}

	public void clearChoiceList() {
		choiceList.clear();
	}

	public Object getChoice(final int index) {
		return choiceList.get(index);
	}

	public java.util.List getListChoiceList() {
		return java.util.Collections.unmodifiableList(choiceList);
	}

	public int sizeChoiceList() {
		return choiceList.size();
	}
}
