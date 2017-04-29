/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2010 dimitry
 *
 *  This file author is dimitry
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
package org.freeplane.main.application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.freeplane.core.util.Compat;

/**
 * @author Dimitry Polivaev
 * Nov 1, 2010
 */
public class UserPropertiesUpdater {
	private static final String ORG_FREEPLANE_OLD_USERFPDIR = "org.freeplane.old_userfpdir";

	void importOldProperties(){
		final File userPreferencesFile = ApplicationResourceController.getUserPreferencesFile();
		if(userPreferencesFile.exists()){
			return;
		}
		copyUserFilesFromPreviousVersionTo(userPreferencesFile.getParentFile());
		if(userPreferencesFile.exists()){
			removeVersionSpecificProperties(userPreferencesFile);
			return;
		}
	}

	private void copyUserFilesFromPreviousVersionTo(File targetDirectory) {
		final File parentDirectory = targetDirectory.getParentFile();
		final String previousDirName = Compat.PREVIOUS_VERSION_DIR_NAME;
		final File sourceDirectory;
		String old_userfpdir = System.getProperty(ORG_FREEPLANE_OLD_USERFPDIR);
		if (isDefined(old_userfpdir))
			sourceDirectory = new File(old_userfpdir, previousDirName);
		else
			sourceDirectory = new File(parentDirectory, previousDirName);
		if (sourceDirectory.exists() && !sourceDirectory.getAbsolutePath().equals(targetDirectory.getAbsolutePath())) {
			try {
				parentDirectory.mkdirs();
				org.apache.commons.io.FileUtils.copyDirectory(sourceDirectory, targetDirectory);
				final File templateDirectory = new File(targetDirectory, "templates");
				org.apache.commons.io.FileUtils.deleteDirectory(templateDirectory);
				templateDirectory.mkdir();
			}
			catch (IOException e) {
			}
			return;
		}
    }

	private boolean isDefined(String old_userfpdir) {
	    return old_userfpdir != null;
    }

	private void removeVersionSpecificProperties(File userPreferencesFile) {
		try {
			Properties userProp = loadProperties(userPreferencesFile);
			for(String name : new String[]{
					"lastOpened_1.0.20",
					"openedNow_1.0.20",
					"openedNow_1.3.04",
					"browse_url_storage",
					"single_backup_directory_path",
			"standard_template"})
				userProp.remove(name);

			saveProperties(userProp, userPreferencesFile);
        }
        catch (IOException e) {
        }
    }

	Properties loadProperties(File userPreferencesFile) throws IOException {
	    FileInputStream inputStream = null;
	    Properties userProp = new Properties();
	    try{
	    inputStream = new FileInputStream(userPreferencesFile);
	    userProp.load(inputStream);
	    }
	    finally {
	    	org.freeplane.core.util.FileUtils.silentlyClose(inputStream);
	    }
	    return userProp;
    }

	void saveProperties(Properties userProp, File userPreferencesFile) throws IOException {
	    FileOutputStream outputStream = null;
	    try{
	    	outputStream = new FileOutputStream(userPreferencesFile);
	    	userProp.store(outputStream, null);
	    }
	    finally {
	    	org.freeplane.core.util.FileUtils.silentlyClose(outputStream);
	    }
    }
}

