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
import java.util.stream.Stream;

import org.freeplane.core.util.Compat;
import org.freeplane.core.util.FreeplaneVersion;

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
		if(userPreferencesFile.exists() && ! FreeplaneVersion.getVersion().isFinal()) {
			removeProperties(userPreferencesFile, "lastOpened_1.0.20", "openedNow_1.3.04");
		}
	}

	private void copyUserFilesFromPreviousVersionTo(File targetDirectory) {
		try {
		File canonicalTargetDirectory = targetDirectory.getCanonicalFile();
		final File parentDirectory = canonicalTargetDirectory.getParentFile();
		final String previousDirName = Compat.PREVIOUS_VERSION_DIR_NAME;
		final File sourceDirectory;
		String old_userfpdir = System.getProperty(ORG_FREEPLANE_OLD_USERFPDIR);
			if (isDefined(old_userfpdir))
				sourceDirectory = new File(old_userfpdir, previousDirName).getCanonicalFile();
			else
				sourceDirectory = new File(parentDirectory, previousDirName).getCanonicalFile();
			if (sourceDirectory.exists() && !sourceDirectory.equals(canonicalTargetDirectory)) {
				parentDirectory.mkdirs();
				org.apache.commons.io.FileUtils.copyDirectory(sourceDirectory,
					canonicalTargetDirectory,
					file -> ! Stream.of("logs", "templates", ".backup", "compiledscripts")
					.map(name -> new File(sourceDirectory, name))
					.anyMatch(file::equals),
				true);
				new File(canonicalTargetDirectory, "templates").mkdir();
			}
		}
		catch (IOException e) {
		}
	}

	private boolean isDefined(String old_userfpdir) {
	    return old_userfpdir != null;
    }

	private void removeProperties(File userPreferencesFile, String... propertyNames) {
		try {
			Properties userProp = loadProperties(userPreferencesFile);
			for(String name : propertyNames)
				userProp.remove(name);

			saveProperties(userProp, userPreferencesFile);
        }
        catch (IOException e) {
        }
    }

	Properties loadProperties(File userPreferencesFile) throws IOException {
	    Properties userProp = new Properties();
	    try (FileInputStream inputStream = new FileInputStream(userPreferencesFile)){
	        userProp.load(inputStream);
	    }
	    return userProp;
    }

	void saveProperties(Properties userProp, File userPreferencesFile) throws IOException {
	    try(FileOutputStream outputStream = new FileOutputStream(userPreferencesFile)){
	    	userProp.store(outputStream, null);
	    }
    }
}

