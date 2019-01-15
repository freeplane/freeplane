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
package org.freeplane.core.undo;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Contains a list of actors and applies act() and undo() in a batch operation.
 *
 * Implements composite design pattern.
 *
 * @author Robert Ladstaetter
 */
public class CompoundActor implements IActor {
	final private LinkedList<IActor> actors;

	public CompoundActor() {
		this(new LinkedList<IActor>());
	}

	@SuppressWarnings("unchecked")
	public CompoundActor(final LinkedList<? extends IActor> actors) {
		this.actors = (LinkedList<IActor>) actors;
	}

	public void act() {
		for (final IActor a : actors) {
			a.act();
		}
	}

	public void add(final IActor firstActor) {
		actors.add(firstActor);
	}

	public String getDescription() {
		if (actors.size() == 0) {
			return "";
		}
		final String firstDescription = actors.getFirst().getDescription();
		if (actors.size() == 1) {
			return firstDescription;
		}
		final String lastDescription = actors.getLast().getDescription();
		if (actors.size() == 2 && !firstDescription.equals("") && !lastDescription.equals("")) {
			return firstDescription + ", " + lastDescription;
		}
		return firstDescription + "... " + lastDescription;
	}

	public void undo() {
		final ListIterator<IActor> iterator = actors.listIterator(actors.size());
		while (iterator.hasPrevious()) {
			iterator.previous().undo();
		}
	}

	public boolean isEmpty() {
		return actors.size() == 0;
	}
}
