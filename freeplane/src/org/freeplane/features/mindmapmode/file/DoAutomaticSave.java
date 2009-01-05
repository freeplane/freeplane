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
import java.util.TimerTask;
import java.util.Vector;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.url.UrlManager;
import org.freeplane.features.mindmapmode.MMapModel;

public class DoAutomaticSave extends TimerTask {
	/**
	 * This value is compared with the result of
	 * getNumberOfChangesSinceLastSave(). If the values coincide, no further
	 * automatic saving is performed until the value changes again.
	 */
	private int changeState;
	final private boolean filesShouldBeDeletedAfterShutdown;
	final private MapModel model;
	final private int numberOfFiles;
	final private File pathToStore;
	final private Vector tempFileStack;

	public DoAutomaticSave(final MapModel model, final int numberOfTempFiles,
	                       final boolean filesShouldBeDeletedAfterShutdown, final File pathToStore) {
		this.model = model;
		tempFileStack = new Vector();
		numberOfFiles = ((numberOfTempFiles > 0) ? numberOfTempFiles : 1);
		this.filesShouldBeDeletedAfterShutdown = filesShouldBeDeletedAfterShutdown;
		this.pathToStore = pathToStore;
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
					File tempFile;
					if (tempFileStack.size() >= numberOfFiles) {
						tempFile = (File) tempFileStack.remove(0);
					}
					else {
						try {
							tempFile = File
							    .createTempFile(
							        "FM_"
							                + ((model.toString() == null) ? "unnamed" : model
							                    .toString()),
							        org.freeplane.features.mindmapmode.file.MFileManager.FREEPLANE_FILE_EXTENSION,
							        pathToStore);
							if (filesShouldBeDeletedAfterShutdown) {
								tempFile.deleteOnExit();
							}
						}
						catch (final Exception e) {
							System.err.println("Error in automatic MapModel.save(): "
							        + e.getMessage());
							org.freeplane.core.util.Tools.logException(e);
							return;
						}
					}
					try {
						((MFileManager) UrlManager.getController(model.getModeController()))
						    .saveInternal((MMapModel) model, tempFile, true /*=internal call*/);
						Controller.getController().getViewController().out(
						    Controller.getResourceController().format("automatically_save_message",
						        new Object[] { tempFile.toString() }));
					}
					catch (final Exception e) {
						System.err.println("Error in automatic MapModel.save(): " + e.getMessage());
						org.freeplane.core.util.Tools.logException(e);
					}
					tempFileStack.add(tempFile);
				}
			});
		}
		catch (final InterruptedException e) {
			org.freeplane.core.util.Tools.logException(e);
		}
		catch (final InvocationTargetException e) {
			org.freeplane.core.util.Tools.logException(e);
		}
	}
}
