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
package org.freeplane.features.url.mindmapmode;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

import javax.swing.Timer;

import org.apache.commons.io.FileUtils;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.mindmapmode.MMapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.url.UrlManager;

public class DoAutomaticSave implements ActionListener {
    /**
     * This value is compared with the result of
     * getNumberOfChangesSinceLastSave(). If the values coincide, no further
     * automatic saving is performed until the value changes again.
     */
    private int changeState;
    final private boolean filesShouldBeDeletedAfterShutdown;
    final private MapModel model;
    final private int numberOfFiles;

    public DoAutomaticSave(final MapModel model, final int numberOfTempFiles,
            final boolean filesShouldBeDeletedAfterShutdown) {
        this.model = model;
        numberOfFiles = ((numberOfTempFiles > 0) ? numberOfTempFiles : 1);
        this.filesShouldBeDeletedAfterShutdown = filesShouldBeDeletedAfterShutdown;
        changeState = model.getNumberOfChangesSinceLastSave();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final URL url = model.getURL();
        if (url == null) {
            return;
        }
        final int currentChangeState = model.getNumberOfChangesSinceLastSave();
        if (model.isReadOnly() || currentChangeState == changeState) {
            return;
        }
        if (currentChangeState == 0) {
            changeState = 0;
            /* map was recently saved. */
            return;
        }
        final ModeController currentModeController = Controller.getCurrentModeController();
        if (!(currentModeController instanceof MModeController)) {
            return;
        }
        final UrlManager fileManager = UrlManager.getController();
        if (!(fileManager instanceof MFileManager)) {
            return;
        }
        final MModeController modeController = (MModeController) currentModeController;
        final File file = new File(url.getFile());
        final File pathToStore = MFileManager.backupDir(file);
        final Timer timer = (Timer) e.getSource();
        timer.stop();
        try {
            FileUtils.forceMkdir(pathToStore);
            final File tempFile = MFileManager.renameAutosaveFiles(pathToStore, file, numberOfFiles, filesShouldBeDeletedAfterShutdown);
            if (tempFile == null) {
                throw new IOException("Can't rotate automatic backups for " + file + " in " + pathToStore);
            }
            if (filesShouldBeDeletedAfterShutdown) {
                tempFile.deleteOnExit();
            }
            final boolean saved;
            final File savedFile;
            if (ResourceController.getResourceController().getBooleanProperty("automatically_overwrite_mindmap_files")) {
                if (tempFile.isFile() && tempFile.canWrite()) {
                    Files.delete(tempFile.toPath());
                }
                FileUtils.moveFile(file, tempFile);
                saved = ((MFileManager) fileManager).save(model);
                savedFile = model.getFile();
            }
            else if (tempFile.isFile() && tempFile.canWrite()
                    || !tempFile.exists() && tempFile.getParentFile().canWrite()) {
                saved = ((MFileManager) fileManager).saveInternal((MMapModel) model, tempFile, true);
                savedFile = tempFile;
            }
            else {
                throw new IOException("Can't write automatic backup " + tempFile);
            }
            if (saved) {
                changeState = currentChangeState;
                outputStatusMessageAfterAutomaticSave(modeController, savedFile);
            }
        }
        catch (final Exception ex) {
            LogUtils.severe("Error automatically saving " + file + " with backups in " + pathToStore, ex);
        }
        finally {
            ((MMapModel) model).resumeTimerForAutomaticSaving(timer);
        }
    }

	private void outputStatusMessageAfterAutomaticSave(MModeController modeController, final File savedFile) {
		if (ResourceController.getResourceController().getBooleanProperty("outputs_status_message_after_automatic_save", true))
			modeController.getController().getViewController()
			.out(TextUtils.format("automatically_save_message", savedFile));
	}

}
