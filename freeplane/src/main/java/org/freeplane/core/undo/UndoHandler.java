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

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.ViewController;

class UndoHandler implements IUndoHandler {
	final private List<ChangeListener> listeners;

	public static final int COMMIT_DELAY = 2;

	private static class ActorList extends LinkedList<CompoundActor> {
		private static final long serialVersionUID = 1L;
		int commitDelay = COMMIT_DELAY;
	}

	/**
	 *
	 */
	private static final int MAX_ENTRIES = 100;
	private static final long TIME_TO_BEGIN_NEW_ACTION = 100;
	private boolean actionFrameStarted;
	private ListIterator<CompoundActor> actorIterator;
	private ActorList actorList;
	private boolean isUndoActionRunning = false;
	private long timeOfLastAdd;
	final private LinkedList<ActorList> transactionList;
	final private LinkedList<ListIterator<CompoundActor>> transactionIteratorList;
	private boolean deactivated;
	private ChangeEvent event;
	
	@Override
	public void setChangeEventSource(IUndoHandler source) {
		event = new ChangeEvent(source);
	}

	final private MapModel map;

	UndoHandler(MapModel map) {
		this.map = map;
		actionFrameStarted = false;
		deactivated = false;
		listeners = new LinkedList<ChangeListener>();
		actorList = new ActorList();
		transactionList = new LinkedList<ActorList>();
		transactionIteratorList = new LinkedList<ListIterator<CompoundActor>>();
		actorIterator = actorList.listIterator();
		timeOfLastAdd = 0;
		event = new ChangeEvent(this);
	}

	public void deactivate() {
		deactivated = true;
		fireStateChanged();
		startActionFrame();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.base.undo.UndoHandler#addActor(freeplane.base.undo.UndoableActor
	 * )
	 */
	public void addActor(final IActor actor) {
		resetRedo();
		actorList.commitDelay = COMMIT_DELAY;
		final long currentTime = System.currentTimeMillis();
		if (deactivated) {
			if (!actionFrameStarted && currentTime - timeOfLastAdd > UndoHandler.TIME_TO_BEGIN_NEW_ACTION) {
				deactivated = false;
			}
			else {
				if (actorList.size() > 0) {
					actorList.clear();
					actorIterator = actorList.listIterator();
				}
				return;
			}
		}
		if ((actorList.size() > 0)
		        && (actionFrameStarted || currentTime - timeOfLastAdd < UndoHandler.TIME_TO_BEGIN_NEW_ACTION)) {
			CompoundActor compoundActor = (CompoundActor) actorIterator.previous();
			compoundActor.add(actor);
			actorIterator.next();
		}
		else {
			CompoundActor compoundActor;
			if(actor instanceof CompoundActor) {
				compoundActor =(CompoundActor) actor;
			}
			else {
				compoundActor = new CompoundActor();
				if(controlsCurrentMap()){
					final IMapSelection selection = Controller.getCurrentController().getSelection();
					final SelectionActor selectionActor = SelectionActor.create(selection);
					compoundActor.add(selectionActor);
				}
				compoundActor.add(actor);
			}
			actorIterator.add(compoundActor);
			final int maxEntries = UndoHandler.MAX_ENTRIES;
			while (actorList.size() > maxEntries) {
				actorList.removeFirst();
				actorIterator = actorList.listIterator(actorList.size());
			}
		}
		startActionFrame();
		timeOfLastAdd = currentTime;
		fireStateChanged();
	}

	public boolean controlsCurrentMap() {
		return map == Controller.getCurrentController().getMap();
	}

	private void fireStateChanged() {
		for (final ChangeListener listener : listeners) {
			listener.stateChanged(event);
		}
	}

	public boolean canRedo() {
		return actorIterator.hasNext();
	}

	public boolean canUndo() {
		return actorIterator.hasPrevious();
	}

	public void commit() {
		resetRedo();
		final CompoundActor compoundActor = new CompoundActor(actorList);
		actionFrameStarted = false;
		timeOfLastAdd = 0;
		if (transactionList.isEmpty()) {
			// FIXME: this happens when new Maps are closed via the scripting API. Fix the basic error instead.
			LogUtils.warn("transactionList is empty on UndoHandler.commit()");
			return;
		}
		actorList = transactionList.removeLast();
		actorIterator = transactionIteratorList.removeLast();
		if (!compoundActor.isEmpty()) {
			addActor(compoundActor);
			actionFrameStarted = false;
			timeOfLastAdd = 0;
		}
		else {
			fireStateChanged();
		}
	}

	public void delayedCommit() {
		if (actorList.commitDelay == 0) {
			commit();
			return;
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				actorList.commitDelay--;
				delayedCommit();
			}
		});
	}

	public void delayedRollback() {
		if (actorList.commitDelay == 0) {
			rollback();
			return;
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				actorList.commitDelay--;
				delayedRollback();
			}
		});
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


	public boolean isUndoActionRunning() {
		return isUndoActionRunning;
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.base.undo.UndoHandler#redo()
	 */
	public void redo() {
		if (canRedo()) {
			final IActor redoActor = actorIterator.next();
			isUndoActionRunning = true;
			redoActor.act();
			isUndoActionRunning = false;
			fireStateChanged();
		}
	}

	public void resetRedo() {
		while (canRedo()) {
			actorIterator.next();
			actorIterator.remove();
		}
		fireStateChanged();
	}

	public void rollback() {
		try {
			isUndoActionRunning = true;
			while (actorIterator.hasPrevious()) {
				final IActor actor = actorIterator.previous();
				actor.undo();
			}
		}
		finally {
			isUndoActionRunning = false;
		}
		if (transactionList.isEmpty()) {
			// FIXME: got here if exceptions occur after opening a map via the scripting API. Fix the basic error instead.
			LogUtils.warn("transactionList is empty on UndoHandler.rollback()");
			return;
		}
		actorList = transactionList.removeLast();
		actorIterator = transactionIteratorList.removeLast();
		fireStateChanged();
	}

	private void startActionFrame() {
		if (actionFrameStarted == false) {
	        final ViewController viewController = Controller.getCurrentController().getViewController();
			if (viewController.isDispatchThread()) {
	        	actionFrameStarted = true;
	        	viewController.invokeLater(new Runnable() {
	        		public void run() {
	        			actionFrameStarted = false;
	        		}
	        	});
	        }
        }
	}

	public void forceNewTransaction() {
		timeOfLastAdd = 0;
		actionFrameStarted = false;
    }
	
	public void startTransaction() {
		transactionList.addLast(actorList);
		transactionIteratorList.addLast(actorIterator);
		final ActorList newActorList = new ActorList();
		actorList = newActorList;
		actorIterator = newActorList.listIterator();
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.base.undo.UndoHandler#undo()
	 */
	public void undo() {
		if (canUndo()) {
			final IActor actor = actorIterator.previous();
			try {
				isUndoActionRunning = true;
				actor.undo();
			}
			finally {
				isUndoActionRunning = false;
				fireStateChanged();
			}
		}
	}

	public void addChangeListener(final ChangeListener listener) {
		listeners.add(listener);
	}

	public void removeChangeListener(final ChangeListener listener) {
		listeners.remove(listener);
	}

    public int getTransactionLevel() {
        return transactionList.size();
    }
}
