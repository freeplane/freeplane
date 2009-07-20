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
package org.freeplane.features.mindmapmode.file;

import java.awt.EventQueue;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.TimerTask;

import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.resources.FpStringUtils;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.url.UrlManager;
import org.freeplane.core.util.LogTool;
import org.freeplane.features.mindmapmode.MMapModel;

public class DoAutomaticSave extends TimerTask {
	/**
	 * This value is compared with the result of
	 * getNumberOfChangesSinceLastSave(). If the values coincide, no further
	 * automatic saving is performed until the value changes again.
	 */
	private int changeState;
	final private boolean filesShouldBeDeletedAfterShutdown;
	final private ModeController modeController;
	final private MapModel model;
	final private int numberOfFiles;

	public DoAutomaticSave(final ModeController modeController, final MapModel model, final int numberOfTempFiles,
	                       final boolean filesShouldBeDeletedAfterShutdown) {
		this.modeController = modeController;
		this.model = model;
		numberOfFiles = ((numberOfTempFiles > 0) ? numberOfTempFiles : 1);
		this.filesShouldBeDeletedAfterShutdown = filesShouldBeDeletedAfterShutdown;
		changeState = model.getNumberOfChangesSinceLastSave();
	}

	@Override
	public void run() {
		/* Map is dirty enough? */
		if (model.getNumberOfChangesSinceLastSave() == changeState) {
			return;
		}
		changeState = model.getNumberOfChangesSinceLastSave();
		if (changeState == 0) {
			/* map was recently saved. */
			return;
		}
		try {
			cancel();
			EventQueue.invokeAndWait(new Runnable() {
				public void run() {
					/* Now, it is dirty, we save it. */
					try {
						final String name;
						final File pathToStore;
						final URL url = model.getURL();
						if (url == null) {
							name = model.getTitle() + UrlManager.FREEPLANE_FILE_EXTENSION;
							final String freeplaneUserDirectory = ResourceController.getResourceController()
							    .getFreeplaneUserDirectory();
							pathToStore = new File(freeplaneUserDirectory, ".backup");
						}
						else {
							final File file = new File(url.getFile());
							name = file.getName();
							pathToStore = new File(file.getParent(), ".backup");
						}
						pathToStore.mkdirs();
						final File tempFile = MFileManager.renameBackupFiles(pathToStore, name, numberOfFiles,
						    "autosave");
						if (tempFile == null) {
							return;
						}
						if (filesShouldBeDeletedAfterShutdown) {
							tempFile.deleteOnExit();
						}
						((MFileManager) UrlManager.getController(modeController)).saveInternal((MMapModel) model,
						    tempFile, true /*=internal call*/);
						modeController.getController().getViewController().out(
						    FpStringUtils.format("automatically_save_message", new Object[] { tempFile.toString() }));
					}
					catch (final Exception e) {
						LogTool.severe("Error in automatic MapModel.save(): ", e);
					}
				}
			});
		}
		catch (final InterruptedException e) {
			LogTool.severe(e);
		}
		catch (final InvocationTargetException e) {
			LogTool.severe(e);
		}
	}
}
