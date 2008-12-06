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

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.ListIterator;

public class UndoHandler implements IUndoHandler {
	private class RedoAction implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			redo();
		}
	}

	private class UndoAction implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			undo();
		}
	}

	/**
	 *
	 */
	private static final int MAX_ENTRIES = 100;
	private static final long TIME_TO_BEGIN_NEW_ACTION = 100;
	private boolean actionFrameStarted;
	private ListIterator actorIterator;
	final private LinkedList<IUndoableActor> actorList;
	private boolean isUndoActionRunning = false;
	final private ActionListener redoAction;
	private long timeOfLastAdd;
	final private ActionListener undoAction;

	public UndoHandler() {
		actionFrameStarted = false;
		actorList = new LinkedList();
		actorIterator = actorList.listIterator();
		redoAction = new RedoAction();
		timeOfLastAdd = 0;
		undoAction = new UndoAction();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.base.undo.UndoHandler#addActor(freeplane.base.undo.UndoableActor
	 * )
	 */
	public void addActor(final IUndoableActor actor) {
		resetRedo();
		final long currentTime = System.currentTimeMillis();
		if ((actorList.size() > 0)
		        && (actionFrameStarted || currentTime - timeOfLastAdd < UndoHandler.TIME_TO_BEGIN_NEW_ACTION)) {
			final IUndoableActor lastActor = (IUndoableActor) actorIterator
			    .previous();
			CompoundActor compoundActor;
			if (!(lastActor instanceof CompoundActor)) {
				compoundActor = new CompoundActor();
				compoundActor.addActor(lastActor);
				actorIterator.set(compoundActor);
			}
			else {
				compoundActor = (CompoundActor) lastActor;
			}
			compoundActor.addActor(actor);
			actorIterator.next();
		}
		else {
			actorIterator.add(actor);
			final int maxEntries = UndoHandler.MAX_ENTRIES;
			while (actorList.size() > maxEntries) {
				actorList.removeFirst();
				actorIterator = actorList.listIterator(actorList.size());
			}
		}
		startActionFrame();
		timeOfLastAdd = currentTime;
	}

	public boolean canRedo() {
		return actorIterator.hasNext();
	}

	public boolean canUndo() {
		return actorIterator.hasPrevious();
	}

	public String getLastDescription() {
		final String description;
		if (canUndo()) {
			description = actorList.getLast().getDescription();
		}
		else {
			description = null;
		}
		return description;
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.base.undo.UndoHandler#getRedoAction()
	 */
	public ActionListener getRedoAction() {
		return redoAction;
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.base.undo.UndoHandler#getUndoAction()
	 */
	public ActionListener getUndoAction() {
		return undoAction;
	}

	public boolean isUndoActionRunning() {
		return isUndoActionRunning;
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.base.undo.UndoHandler#redo()
	 */
	public void redo() {
		if (canRedo()) {
			final IUndoableActor redoActor = (IUndoableActor) actorIterator
			    .next();
			isUndoActionRunning = true;
			redoActor.act();
			isUndoActionRunning = false;
		}
	}

	public void resetRedo() {
		while (canRedo()) {
			actorIterator.next();
			actorIterator.remove();
		}
	}

	private void startActionFrame() {
		if (actionFrameStarted == false && EventQueue.isDispatchThread()) {
			actionFrameStarted = true;
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					actionFrameStarted = false;
				}
			});
		}
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.base.undo.UndoHandler#undo()
	 */
	public void undo() {
		if (canUndo()) {
			final IUndoableActor actor = (IUndoableActor) actorIterator
			    .previous();
			isUndoActionRunning = true;
			actor.undo();
			isUndoActionRunning = false;
		}
	}
}
