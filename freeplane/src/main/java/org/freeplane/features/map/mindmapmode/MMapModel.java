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
package org.freeplane.features.map.mindmapmode;

import java.awt.EventQueue;
import java.io.File;
import java.net.URL;
import java.util.Timer;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.undo.IUndoHandler;
import org.freeplane.core.undo.UndoHandler;
import org.freeplane.core.util.SysUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.url.UrlManager;
import org.freeplane.features.url.mindmapmode.DoAutomaticSave;
import org.freeplane.features.url.mindmapmode.DummyLockManager;
import org.freeplane.features.url.mindmapmode.LockManager;
import org.freeplane.features.url.mindmapmode.MFileManager;

public class MMapModel extends MapModel {
	private static int unnamedMapsNumber = 1;
	private LockManager lockManager;
	private Timer timerForAutomaticSaving;
	private int titleNumber = 0;
	private boolean autosaveEnabled;

	/**
	 * The current version and all other version that don't need XML update for
	 * sure.
	 */
	public MMapModel() {
		super();
		this.autosaveEnabled = false;
		addExtension(IUndoHandler.class, new UndoHandler(this));
		this.lockManager = ResourceController.getResourceController().getBooleanProperty(
		"experimental_file_locking_on") ? new LockManager() : new DummyLockManager();
	}

	public void enableAutosave() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				if(! autosaveEnabled) {
					autosaveEnabled = true;
					scheduleTimerForAutomaticSaving();
				}
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
		getLockManager().release();
		/* cancel the timer, if map is closed. */
		if (getTimerForAutomaticSaving() != null) {
			getTimerForAutomaticSaving().cancel();
		}
		autosaveEnabled = false;
		super.destroy();
	}

	public LockManager getLockManager() {
		return lockManager;
	}

	public Timer getTimerForAutomaticSaving() {
		return timerForAutomaticSaving;
	}

	@Override
	public String getTitle() {
		final URL url = getURL();
		if (url != null) {
			final File file = getFile();
			if(file != null) {
	            final String fileName = file.getName();
	            if(fileName.endsWith(UrlManager.FREEPLANE_FILE_EXTENSION))
	            	return fileName.substring(0, fileName.length() - UrlManager.FREEPLANE_FILE_EXTENSION.length());
	            return fileName;
            }
            else
				return url.toString();
		}
		if (titleNumber == 0) {
			titleNumber = MMapModel.unnamedMapsNumber++;
		}
		return TextUtils.getText("mindmap") + titleNumber;
	}

	public void scheduleTimerForAutomaticSaving() {
		if (!(UrlManager.getController() instanceof MFileManager)
				|| Controller.getCurrentController().getMapViewManager().isHeadless()
				|| ! autosaveEnabled) {
			return;
		}
		final int numberOfTempFiles = Integer.parseInt(ResourceController.getResourceController().getProperty(
		    "number_of_different_files_for_automatic_save"));
		if (numberOfTempFiles == 0) {
			return;
		}
		final boolean filesShouldBeDeletedAfterShutdown = ResourceController.getResourceController()
		    .getBooleanProperty("delete_automatic_saves_at_exit");
		final int delay = ResourceController.getResourceController().getTimeProperty("time_for_automatic_save");
		if (delay == 0) {
			return;
		}
		final boolean useSingleBackupDirectory = ResourceController.getResourceController().getBooleanProperty(
		    "single_backup_directory");
		final String singleBackupDirectory = ResourceController.getResourceController()
		    .getProperty("single_backup_directory_path");
		final Timer timer = SysUtils.createTimer("TimerForAutomaticSaving");
		timer.schedule(new DoAutomaticSave(this, numberOfTempFiles, filesShouldBeDeletedAfterShutdown,
		    useSingleBackupDirectory, singleBackupDirectory), delay, delay);
		this.timerForAutomaticSaving = timer;
	}

	@Override
	public boolean close() {
		final Controller controller = Controller.getCurrentController();
		final ModeController modeController = controller.getModeController(MModeController.MODENAME);
		final MMapController mapController = (MMapController) modeController.getMapController();
		return mapController.close(this);
	}
}
