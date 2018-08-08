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

import javax.swing.event.ChangeListener;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.mindmapmode.MMapModel;

public interface IUndoHandler extends IExtension {

	boolean controlsCurrentMap();

	void setChangeEventSource(IUndoHandler source);

	void addActor(IActor actor);

	boolean canRedo();

	boolean canUndo();

	void addChangeListener(ChangeListener listener);

	void removeChangeListener(ChangeListener listener);

	void commit();

	void fireStateChanged();

	String getLastDescription();

	boolean isUndoActionRunning();

	void redo();

	public void resetRedo();

	void rollback();

	void startTransaction();

	void forceNewTransaction();

	void undo();

	void deactivate();

	public void delayedCommit();

	public void delayedRollback();

	public int getTransactionLevel();

}
