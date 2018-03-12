package org.freeplane.plugin.collaboration.client.session;

import javax.swing.event.ChangeListener;

import org.freeplane.core.undo.IActor;
import org.freeplane.core.undo.IUndoHandler;
import org.freeplane.features.map.MapModel;
import org.freeplane.plugin.collaboration.client.event.batch.Updates;

public class SessionUndoHandler implements IUndoHandler{

	enum State {
		SESSION_TRANSACTION_COMPLETED, SESSION_UNDO_COMPLETED, SESSION_REDO_COMPLETED, SESSION_TRANSACTION_STARTED, INNER_TRANSACTION_STARTED;

		public boolean canRedo() {
			 return canUndo();
			 }

		public boolean canUndo() {
			 return this != SESSION_TRANSACTION_STARTED
					  && this != SESSION_REDO_COMPLETED
					  && this != SESSION_UNDO_COMPLETED;
			 }
	}

	@Override
	public void setChangeEventSource(IUndoHandler source) {
		delegate.setChangeEventSource(source);
	}

	private IUndoHandler delegate;
	private final MapModel map;
	private State state = State.SESSION_TRANSACTION_COMPLETED;

	public SessionUndoHandler(MapModel map) {
		this.map = map;
		this.delegate = map.removeExtension(IUndoHandler.class);
		delegate.setChangeEventSource(this);
		map.addExtension(IUndoHandler.class, this);
	}

	public void removeFromMap() {
		if(map == null)
			throw new IllegalStateException();
		final IUndoHandler currentHandler = map.removeExtension(IUndoHandler.class);
		if (this == currentHandler) {
			delegate.setChangeEventSource(delegate);
			map.addExtension(IUndoHandler.class, delegate);
		}
		else {
			if(currentHandler != null)
				map.addExtension(currentHandler);
			throw new IllegalStateException();
		}
	}



	@Override
	public boolean controlsCurrentMap() {
		return delegate.controlsCurrentMap();
	}

	@Override
	public void addActor(IActor actor) {
		if(getTransactionLevel() == 0)
			startTransaction();
		delegate.addActor(actor);
	}

	@Override
	public boolean canRedo() {
		return state.canRedo() && delegate.canRedo();
	}

	@Override
	public boolean canUndo() {
		return state.canUndo() && delegate.canUndo();
	}

	@Override
	public void addChangeListener(ChangeListener listener) {
		delegate.addChangeListener(listener);
	}

	@Override
	public void removeChangeListener(ChangeListener listener) {
		delegate.removeChangeListener(listener);
	}

	@Override
	public void commit() {
		if(state == State.SESSION_UNDO_COMPLETED || state == State.SESSION_REDO_COMPLETED) {
			state = State.SESSION_TRANSACTION_COMPLETED;
			delegate.fireStateChanged();
		}
		else {
			if(state == State.SESSION_TRANSACTION_STARTED)
				state = State.SESSION_TRANSACTION_COMPLETED;
			delegate.commit();
		}
	}

	@Override
	public String getLastDescription() {
		return delegate.getLastDescription();
	}

	@Override
	public boolean isUndoActionRunning() {
		return delegate.isUndoActionRunning();
	}

	@Override
	public void redo() {
		if(state == State.INNER_TRANSACTION_STARTED)
			delegate.redo();
		else {
			flush();
			state = State.SESSION_REDO_COMPLETED;
			delegate.redo();
			flush();
		}
	}

	@Override
	public void resetRedo() {
		delegate.resetRedo();
	}

	@Override
	public void rollback() {
		if(state == State.SESSION_UNDO_COMPLETED)
			redo();
		else if (state == State.SESSION_REDO_COMPLETED)
			undo();
		else {
			state = State.SESSION_TRANSACTION_COMPLETED;
			delegate.rollback();
		}
	}

	@Override
	public void startTransaction() {
		delegate.startTransaction();
		state = getTransactionLevel() == 1 ? State.SESSION_TRANSACTION_STARTED : State.INNER_TRANSACTION_STARTED;
	}

	@Override
	public void forceNewTransaction() {
		delegate.forceNewTransaction();
	}

	@Override
	public void undo() {
		if(state == State.INNER_TRANSACTION_STARTED)
			delegate.undo();
		else {
			flush();
			state = State.SESSION_UNDO_COMPLETED;
			delegate.undo();
			flush();
		}
	}

	private void flush() {
		if(map == null || getTransactionLevel() != 1)
			return;
		Updates updates = map.getExtension(Updates.class);
		if(updates == null)
			return;
		updates.flush();
	}

	@Override
	public void deactivate() {
		delegate.deactivate();
	}

	@Override
	public void delayedCommit() {
		delegate.delayedCommit();
	}

	@Override
	public void delayedRollback() {
		delegate.delayedRollback();
	}

	@Override
	public int getTransactionLevel() {
		return delegate.getTransactionLevel();
	}

	@Override
	public void fireStateChanged() {
		delegate.fireStateChanged();
	}


}
