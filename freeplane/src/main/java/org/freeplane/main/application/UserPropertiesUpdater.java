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

import static java.util.Arrays.asList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Stream;

import org.freeplane.core.resources.ResourceController;
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
		File previousPropertyDirectory = copyUserFilesFromPreviousVersionTo(userPreferencesFile.getParentFile());
		if(userPreferencesFile.exists()) {
            try {
                Properties userProp = loadProperties(userPreferencesFile);
                if(! FreeplaneVersion.getVersion().isFinal()) {
                    for(String name : asList("lastOpened_1.0.20", "openedNow_1.3.04"))
                        userProp.remove(name);
                }
                String value = userProp.getProperty("single_backup_directory_path");
                String previousPropertyDirectoryPath = previousPropertyDirectory.getPath();
                if (value.startsWith(previousPropertyDirectoryPath)) {
                    value = "{freeplaneuserdir}" + value.substring(previousPropertyDirectoryPath.length());
                    userProp.setProperty("single_backup_directory_path", value);
                }
            
            	saveProperties(userProp, userPreferencesFile);
            }
            catch (IOException e) {
            }
		}
	}

	private File copyUserFilesFromPreviousVersionTo(File targetDirectory) {
	    try {
	        File canonicalTargetDirectory = targetDirectory.getCanonicalFile();
	        final File parentDirectory = canonicalTargetDirectory.getParentFile();
	        final String previousDirName = Compat.PREVIOUS_VERSION_DIR_NAME;
	        String old_userfpdir = System.getProperty(ORG_FREEPLANE_OLD_USERFPDIR);
	        File previousPropertyDirectory;
            if (isDefined(old_userfpdir))
	            previousPropertyDirectory = new File(old_userfpdir, previousDirName).getCanonicalFile();
	        else
	            previousPropertyDirectory = new File(parentDirectory, previousDirName).getCanonicalFile();
	        if (previousPropertyDirectory.exists() && !previousPropertyDirectory.equals(canonicalTargetDirectory)) {
	            parentDirectory.mkdirs();
	            org.apache.commons.io.FileUtils.copyDirectory(previousPropertyDirectory,
	                    canonicalTargetDirectory,
	                    file -> ! Stream.of("logs", "templates", ".backup", "compiledscripts")
	                    .map(name -> new File(previousPropertyDirectory, name))
	                    .anyMatch(file::equals),
	                    true);
	            new File(canonicalTargetDirectory, "templates").mkdir();
	        }
	        return previousPropertyDirectory;
	    }
	    catch (IOException e) {
	        return null;
	    }
	}

	private boolean isDefined(String old_userfpdir) {
	    return old_userfpdir != null;
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

