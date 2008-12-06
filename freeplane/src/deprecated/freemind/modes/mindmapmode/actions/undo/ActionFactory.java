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
package deprecated.freemind.modes.mindmapmode.actions.undo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import deprecated.freemind.modes.mindmapmode.actions.instance.ActionInstance;

/**
 * @author foltin
 * @deprecated
 */
@Deprecated
public class ActionFactory {
	/** HashMap of Action class -> actor instance. */
	final private HashMap registeredActors;
	/**
	 * This Vector denotes all handler of the action to be called for each
	 * action.
	 */
	final private Vector registeredHandler;
	private UndoActionHandler undoActionHandler;

	/**
	 *
	 */
	public ActionFactory() {
		super();
		registeredHandler = new Vector();
		registeredActors = new HashMap();
	}

	/**
	 */
	public void deregisterActor(final Class action) {
		registeredActors.remove(action);
	}

	public void deregisterHandler(final IActionHandler newHandler) {
		registeredHandler.remove(newHandler);
	}

	public void endTransaction(final String name) {
		for (final Iterator i = registeredHandler.iterator(); i.hasNext();) {
			final IActionHandler handler = (IActionHandler) i.next();
			handler.endTransaction(name);
		}
	}

	/**
	 * @return the success of the action. If an exception arises, the method
	 *         returns false.
	 */
	public void executeAction(final ActionPair pair) {
		if (pair == null) {
			return;
		}
		if (undoActionHandler != null) {
			try {
				undoActionHandler.executeAction(pair);
			}
			catch (final Exception e) {
				org.freeplane.main.Tools.logException(e);
			}
		}
		try {
			final ActionInstance action = pair.getDoAction();
			final IActor actor = getActor(action);
			actor.act(action);
			final Object[] aArray = registeredHandler.toArray();
			for (int i = 0; i < aArray.length; i++) {
				final IActionHandler handler = (IActionHandler) aArray[i];
				handler.executeAction(action);
			}
		}
		catch (final Exception e) {
			org.freeplane.main.Tools.logException(e);
		}
	}

	public IActor getActor(final ActionInstance action) {
		for (final Iterator i = registeredActors.keySet().iterator(); i
		    .hasNext();) {
			final Class actorClass = (Class) i.next();
			if (actorClass.isInstance(action)) {
				return (IActor) registeredActors.get(actorClass);
			}
		}
		throw new IllegalArgumentException("No actor present for action "
		        + action.getClass());
	}

	/**
	 */
	public void registerActor(final IActor actor, final Class action) {
		registeredActors.put(action, actor);
	}

	/**
	 * The handler is put in front. Thus it is called before others are called.
	 */
	public void registerHandler(final IActionHandler newHandler) {
		if (!registeredHandler.contains(newHandler)) {
			registeredHandler.remove(newHandler);
		}
		registeredHandler.add(0, newHandler);
	}

	public void registerUndoHandler(final UndoActionHandler undoActionHandler) {
		this.undoActionHandler = undoActionHandler;
	}

	public void startTransaction(final String name) {
		for (final Iterator i = registeredHandler.iterator(); i.hasNext();) {
			final IActionHandler handler = (IActionHandler) i.next();
			handler.startTransaction(name);
		}
	}
}
