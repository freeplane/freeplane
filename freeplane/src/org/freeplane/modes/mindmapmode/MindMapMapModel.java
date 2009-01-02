/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.modes.mindmapmode;

import java.awt.EventQueue;
import java.io.File;
import java.util.Timer;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.map.MapModel;
import org.freeplane.core.map.ModeController;
import org.freeplane.core.map.NodeModel;
import org.freeplane.core.undo.IUndoHandler;
import org.freeplane.core.undo.UndoHandler;
import org.freeplane.core.util.Tools;
import org.freeplane.modes.mindmapmode.url.DoAutomaticSave;

public class MindMapMapModel extends MapModel {
	private static int unnamedMapsNumber = 1;
	private LockManager lockManager;
	private Timer timerForAutomaticSaving;
	private int titleNumber = 0;
	final private IUndoHandler undoHandler;

	/**
	 * The current version and all other version that don't need XML update for
	 * sure.
	 */
	public MindMapMapModel(final NodeModel root, final ModeController modeController) {
		super(modeController, root);
		setReadOnly(false);
		this.setLockManager(Controller.getResourceController().getBoolProperty(
		    "experimental_file_locking_on") ? new LockManager() : new DummyLockManager());
		undoHandler = new UndoHandler();
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				scheduleTimerForAutomaticSaving();
			}
		});
	}

	/**
	 * When a map is closed, this method is called.
	 *
	 * @param mindMapMapModel
	 */
	@Override
	public void destroy() {
		getLockManager().releaseLock();
		getLockManager().releaseTimer();
		/* cancel the timer, if map is closed. */
		if (getTimerForAutomaticSaving() != null) {
			getTimerForAutomaticSaving().cancel();
		}
		super.destroy();
	}

	LockManager getLockManager() {
		return lockManager;
	}

	public Timer getTimerForAutomaticSaving() {
		return timerForAutomaticSaving;
	}

	@Override
	public String getTitle() {
		if (getURL() != null) {
			return getFile().getName();
		}
		if (titleNumber == 0) {
			titleNumber = MindMapMapModel.unnamedMapsNumber++;
		}
		return Controller.getText("mindmap") + titleNumber;
	}

	public IUndoHandler getUndoHandler() {
		return undoHandler;
	}

	public void scheduleTimerForAutomaticSaving() {
		final int numberOfTempFiles = Integer.parseInt(Controller.getResourceController()
		    .getProperty("number_of_different_files_for_automatic_save"));
		if (numberOfTempFiles == 0) {
			return;
		}
		final boolean filesShouldBeDeletedAfterShutdown = Controller.getResourceController()
		    .getBoolProperty("delete_automatic_saves_at_exit");
		String path = Controller.getResourceController().getProperty("path_to_automatic_saves");
		/* two standard values: */
		if (Tools.safeEquals(path, "default")) {
			path = null;
		}
		if (Tools.safeEquals(path, "freemind_home")) {
			path = Controller.getResourceController().getFreemindUserDirectory();
		}
		int delay = Integer.parseInt(Controller.getResourceController().getProperty(
		    "time_for_automatic_save"));
		if (delay == 0) {
			return;
		}
		File dirToStore = null;
		if (path != null) {
			dirToStore = new File(path);
			/* existence? */
			if (!dirToStore.isDirectory()) {
				dirToStore = null;
				System.err.println("Temporary directory " + path
				        + " not found. Disabling automatic store.");
				delay = Integer.MAX_VALUE;
				return;
			}
		}
		final Timer timer = new Timer();
		timer.schedule(new DoAutomaticSave(this, numberOfTempFiles,
		    filesShouldBeDeletedAfterShutdown, dirToStore), delay, delay);
		this.setTimerForAutomaticSaving(timer);
	}

	void setLockManager(final LockManager lockManager) {
		this.lockManager = lockManager;
	}

	void setTimerForAutomaticSaving(final Timer timerForAutomaticSaving) {
		this.timerForAutomaticSaving = timerForAutomaticSaving;
	}
}
