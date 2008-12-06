/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
package org.freeplane.undo;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

public class CompoundActor implements IUndoableActor {
	final private LinkedList actorList = new LinkedList();

	/*
	 * (non-Javadoc)
	 * @see freeplane.base.undo.UndoableActor#act()
	 */
	public void act() {
		final Iterator iterator = actorList.iterator();
		while (iterator.hasNext()) {
			((IUndoableActor) iterator.next()).act();
		}
	}

	/**
	 */
	public void addActor(final IUndoableActor firstActor) {
		actorList.add(firstActor);
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.base.undo.UndoableActor#getDescription()
	 */
	public String getDescription() {
		if (actorList.size() == 0) {
			return "";
		}
		final String firstDescription = ((IUndoableActor) actorList.getFirst())
		    .getDescription();
		if (actorList.size() == 1) {
			return firstDescription;
		}
		final String lastDescription = ((IUndoableActor) actorList.getLast())
		    .getDescription();
		if (actorList.size() == 2 && !firstDescription.equals("")
		        && !lastDescription.equals("")) {
			return firstDescription + ", " + lastDescription;
		}
		return firstDescription + "... " + lastDescription;
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.base.undo.UndoableActor#undo()
	 */
	public void undo() {
		final ListIterator iterator = actorList.listIterator(actorList.size());
		while (iterator.hasPrevious()) {
			((IUndoableActor) iterator.previous()).undo();
		}
	}
}
