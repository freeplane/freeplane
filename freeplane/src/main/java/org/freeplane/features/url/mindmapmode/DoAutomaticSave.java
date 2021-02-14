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
import java.net.URL;

import javax.swing.Timer;

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
    static final String AUTOSAVE_EXTENSION = "autosave";
    /**
     * This value is compared with the result of
     * getNumberOfChangesSinceLastSave(). If the values coincide, no further
     * automatic saving is performed until the value changes again.
     */
    private int changeState;
    final private boolean filesShouldBeDeletedAfterShutdown;
    final private MapModel model;
    final private int numberOfFiles;
    private final File singleBackupDirectory;
    static final String BACKUP_DIR = ".backup";

    public DoAutomaticSave(final MapModel model, final int numberOfTempFiles,
            final boolean filesShouldBeDeletedAfterShutdown, boolean useSingleBackupDirectory,
            final String singleBackupDirectory) {
        this.model = model;
        numberOfFiles = ((numberOfTempFiles > 0) ? numberOfTempFiles : 1);
        this.filesShouldBeDeletedAfterShutdown = filesShouldBeDeletedAfterShutdown;
        this.singleBackupDirectory = useSingleBackupDirectory ? new File(singleBackupDirectory) : null;
        changeState = model.getNumberOfChangesSinceLastSave();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        /* Map is dirty enough? */
        if (model.getNumberOfChangesSinceLastSave() == changeState) {
            return;
        }
        changeState = model.getNumberOfChangesSinceLastSave();
        if (changeState == 0) {
            /* map was recently saved. */
            return;
        }
        Timer timer =  (Timer) e.getSource();
        timer.stop();
        try {
            final ModeController currentModeController = Controller.getCurrentModeController();
            if(!(currentModeController instanceof MModeController))
                return;
            MModeController modeController = ((MModeController) currentModeController);
            final File pathToStore;
            final URL url = model.getURL();
            final File file = new File(url != null ? url.getFile() //
                    : model.getTitle() + UrlManager.FREEPLANE_FILE_EXTENSION);
            if (url == null) {
                pathToStore = new File(ResourceController.getResourceController()
                        .getFreeplaneUserDirectory(), BACKUP_DIR);
            }
            else if (singleBackupDirectory != null) {
                pathToStore = singleBackupDirectory;
            }
            else {
                pathToStore = new File(file.getParent(), BACKUP_DIR);
            }
            pathToStore.mkdirs();
            final File tempFile = MFileManager.renameBackupFiles(pathToStore, file, numberOfFiles,
                    AUTOSAVE_EXTENSION);
            if (tempFile == null) {
                return;
            }
            if (filesShouldBeDeletedAfterShutdown) {
                tempFile.deleteOnExit();
            }
            if(file.canWrite()) {
                ((MFileManager) UrlManager.getController())
                .saveInternal((MMapModel) model, tempFile, true /*=internal call*/);
                modeController.getController().getViewController()
                .out(TextUtils.format("automatically_save_message", tempFile));
            }
        }
        catch (final Exception ex) {
            LogUtils.severe("Error in automatic MapModel.save(): ", ex);
        }
    }

}
