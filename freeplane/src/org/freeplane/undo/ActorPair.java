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

public class ActorPair implements IUndoableActor {
	final private String description;
	final private IActor performingActor;
	final private IActor undoingActor;

	public ActorPair(final IActor performingActor, final IActor undoingActor,
	                 final String description) {
		super();
		this.performingActor = performingActor;
		this.undoingActor = undoingActor;
		this.description = description;
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.base.undo.Actor#act()
	 */
	public void act() {
		performingActor.act();
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.base.undo.UndoableActor#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.base.undo.UndoableActor#undo()
	 */
	public void undo() {
		undoingActor.act();
	}
}
