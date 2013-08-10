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

import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JLabel;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.edge.EdgeModel;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.nodestyle.NodeSizeModel;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.NodeStyleModel;
import org.freeplane.features.note.mindmapmode.MNoteController;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.url.mindmapmode.MFileManager;

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
			removeOpenedMaps(userPreferencesFile);
			return;
		}
		final File oldUserPreferencesFile =new File(System.getProperty("user.home"), ".freeplane/auto.properties");
		if(! oldUserPreferencesFile.exists()){
			return;
		}
		importOldPreferences(userPreferencesFile, oldUserPreferencesFile);
		importOldIcons();
	}

	private void copyUserFilesFromPreviousVersionTo(File targetDirectory) {
		final File parentDirectory = targetDirectory.getParentFile();
		final String previousDirName = "1.2.x";
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
			}
			catch (IOException e) {
			}
			return;
		}
    }

	private boolean isDefined(String old_userfpdir) {
	    return old_userfpdir != null;
    }

	private void importOldPreferences(final File userPreferencesFile,
			final File oldUserPreferencesFile) {
		try {
			Properties userProp = loadProperties(userPreferencesFile);
	        userProp.remove("lastOpened_1.0.20");
	        userProp.remove("openedNow_1.0.20");
	        userProp.remove("browse_url_storage");
	        fixFontSize(userProp, "defaultfontsize");
	        fixFontSize(userProp, "label_font_size");
	        saveProperties(userProp, userPreferencesFile);
        }
        catch (IOException e) {
        }
	}

	private void removeOpenedMaps(File userPreferencesFile) {
		try {
			Properties userProp = loadProperties(userPreferencesFile);
	        userProp.remove("lastOpened_1.0.20");
	        userProp.remove("openedNow_1.0.20");
	        userProp.remove("browse_url_storage");
	        userProp.remove("single_backup_directory_path");
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

	private void fixFontSize(Properties userProp, String name) {
	    final Object defaultFontSizeObj = userProp.remove(name);
	    if(defaultFontSizeObj == null)
	    	return;
	    try {
	        int oldDefaultFontSize = Integer.parseInt(defaultFontSizeObj.toString());
	        int newDefaultFontSize = Math.round(oldDefaultFontSize / UITools.FONT_SCALE_FACTOR);
	        userProp.put(name, Integer.toString(newDefaultFontSize));
        }
        catch (NumberFormatException e) {
        }
    }

	void importOldDefaultStyle() {
		final ModeController modeController = Controller.getCurrentController().getModeController(MModeController.MODENAME);
		MFileManager fm = MFileManager.getController(modeController);
		final String standardTemplateName = fm.getStandardTemplateName();
		final File userDefault;
		final File absolute = new File(standardTemplateName);
		if(absolute.isAbsolute())
			userDefault = absolute;
		else{
			final File userTemplates = fm.defaultUserTemplateDir();
			userDefault= new File(userTemplates, standardTemplateName);
		}
		if(userDefault.exists()){
			return;
		}
		userDefault.getParentFile().mkdirs();
		if(! userDefault.getParentFile().exists()){
			return;
		}
		MapModel defaultStyleMap = new MapModel();
		final File allUserTemplates = fm.defaultStandardTemplateDir();
		final File standardTemplate = new File(allUserTemplates, "standard.mm");
		try {
			fm.loadCatchExceptions(standardTemplate.toURL(), defaultStyleMap);
		}
		catch (Exception e) {
			LogUtils.warn(e);
			try {
				fm.loadCatchExceptions(ResourceController.getResourceController().getResource("/styles/viewer_standard.mm"), defaultStyleMap);
			}
			catch (Exception e2) {
				defaultStyleMap.createNewRoot();
				LogUtils.severe(e);
			}
		}
        final NodeStyleController nodeStyleController = NodeStyleController.getController(modeController);
        updateDefaultStyle(nodeStyleController, defaultStyleMap);
        updateNoteStyle(nodeStyleController, defaultStyleMap);

        try {
	        fm.writeToFile(defaultStyleMap, userDefault);
        }
        catch (IOException e) {
        }


	}
   private void updateDefaultStyle(final NodeStyleController nodeStyleController, MapModel defaultStyleMap) {
        NodeModel styleNode1 = MapStyleModel.getExtension(defaultStyleMap).getStyleNode(MapStyleModel.DEFAULT_STYLE);
		NodeModel styleNode = styleNode1;
		styleNode.removeExtension(NodeStyleModel.class);
		styleNode.removeExtension(EdgeModel.class);

		final NodeStyleModel nodeStyleModel = new NodeStyleModel();

		nodeStyleModel.setBackgroundColor(nodeStyleController.getBackgroundColor(styleNode));
		nodeStyleModel.setBold(nodeStyleController.isBold(styleNode));
		nodeStyleModel.setColor(nodeStyleController.getColor(styleNode));
		nodeStyleModel.setFontFamilyName(nodeStyleController.getFontFamilyName(styleNode));
		nodeStyleModel.setFontSize(nodeStyleController.getFontSize(styleNode));
		nodeStyleModel.setItalic(nodeStyleController.isItalic(styleNode));
		nodeStyleModel.setShape(nodeStyleController.getShape(styleNode));

		styleNode.addExtension(nodeStyleModel);

		final NodeSizeModel nodeSizeModel = new NodeSizeModel();
		nodeSizeModel.setMaxNodeWidth(nodeStyleController.getMaxWidth(styleNode));
		nodeSizeModel.setMinNodeWidth(nodeStyleController.getMinWidth(styleNode));

		final EdgeModel standardEdgeModel = EdgeModel.getModel(styleNode);
		if(standardEdgeModel != null){
			final EdgeModel edgeModel = new EdgeModel();
			edgeModel.setColor(standardEdgeModel.getColor());
			edgeModel.setStyle(standardEdgeModel.getStyle());
			edgeModel.setWidth(standardEdgeModel.getWidth());
			styleNode.addExtension(edgeModel);
		}
    }

   private void updateNoteStyle(final NodeStyleController nodeStyleController, MapModel defaultStyleMap) {
       if (ResourceController.getResourceController().getBooleanProperty((MNoteController.RESOURCES_USE_DEFAULT_FONT_FOR_NOTES_TOO)))
           return;
       final NodeModel styleNode = MapStyleModel.getExtension(defaultStyleMap).getStyleNode(MapStyleModel.NOTE_STYLE);
       if(styleNode == null)
           return;
       styleNode.removeExtension(NodeStyleModel.class);
       final Font defaultFont = new JLabel().getFont();
       final NodeStyleModel nodeStyleModel = new NodeStyleModel();
       nodeStyleModel.setFontFamilyName(defaultFont.getFamily());
       nodeStyleModel.setFontSize(defaultFont.getSize());
       styleNode.addExtension(nodeStyleModel);
   }

   private void importOldIcons() {
		final File oldUserPreferencesFile =new File(System.getProperty("user.home"), ".freeplane/auto.properties");
		if(! oldUserPreferencesFile.exists()){
			return;
		}
	   final File userPreferencesFile = ApplicationResourceController.getUserPreferencesFile();
		final File iconDir = new File(userPreferencesFile.getParentFile(), "icons");
		if (iconDir.exists()) {
			return;
		}
		LogUtils.info("creating user icons directory " + iconDir);
		iconDir.mkdirs();
		final File oldIconDir = new File(oldUserPreferencesFile.getParentFile(), "icons");
		if(oldIconDir.exists()){
			try {
				org.apache.commons.io.FileUtils.copyDirectory(oldIconDir, iconDir);
			} catch (Exception e) {
			}
		}
   }
}

