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

import org.freeplane.core.controller.Controller;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.common.edge.EdgeModel;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.nodestyle.NodeStyleController;
import org.freeplane.features.common.nodestyle.NodeStyleModel;
import org.freeplane.features.common.styles.MapStyleModel;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.features.mindmapmode.file.MFileManager;

/**
 * @author Dimitry Polivaev
 * Nov 1, 2010
 */
public class UserPropertiesUpdater {
	void importOldProperties(){
		final File userPreferencesFile = ApplicationResourceController.getUserPreferencesFile();
		if(userPreferencesFile.exists()){
			return;
		}
		final File oldUserPreferencesFile =new File(System.getProperty("user.home"), ".freeplane/auto.properties");
		if(! oldUserPreferencesFile.exists()){
			return;
		}
		Properties userProp = new Properties();
		try {
	        userProp.load(new FileInputStream(oldUserPreferencesFile));
	        userProp.remove("lastOpened_1.0.20");
	        userProp.remove("openedNow_1.0.20");
	        userProp.remove("browse_url_storage");
	        userProp.store(new FileOutputStream(userPreferencesFile), null);
        }
        catch (IOException e) {
        }
	}
	void importOldDefaultStyle() {
		final ModeController modeController = Controller.getCurrentController().getModeController(MModeController.MODENAME);
		MFileManager fm = (MFileManager) MFileManager.getController(modeController);
		final File userTemplates = fm.defaultUserTemplateDir();
		final File userDefault = new File(userTemplates, fm.getStandardTemplateName());
		if(userDefault.exists()){
			return;
		}
		userDefault.getParentFile().mkdirs();
		MapModel defaultStyleMap = new MapModel(null);
		final File allUserTemplates = fm.defaultStandardTemplateDir();
		final File standardTemplate = new File(allUserTemplates, fm.getStandardTemplateName());
		try {
			fm.loadImpl(standardTemplate.toURL(), defaultStyleMap);
		}
		catch (Exception e) {
			LogUtils.warn(e);
			try {
				fm.loadImpl(ResourceController.getResourceController().getResource("/styles/viewer_standard.mm"), defaultStyleMap);
			}
			catch (Exception e2) {
				LogUtils.severe(e);
			}
		}
		NodeModel styleNode = findDefaultStyleNode(defaultStyleMap);
		styleNode.removeExtension(NodeStyleModel.class);
		styleNode.removeExtension(EdgeModel.class);

		final NodeStyleController nodeStyleController = NodeStyleController.getController(modeController);
		final NodeStyleModel nodeStyleModel = new NodeStyleModel();

		nodeStyleModel.setBackgroundColor(nodeStyleController.getBackgroundColor(styleNode));
		nodeStyleModel.setBold(nodeStyleController.isBold(styleNode));
		nodeStyleModel.setColor(nodeStyleController.getColor(styleNode));
		nodeStyleModel.setFontFamilyName(nodeStyleController.getFontFamilyName(styleNode));
		nodeStyleModel.setFontSize(nodeStyleController.getFontSize(styleNode));
		nodeStyleModel.setItalic(nodeStyleController.isItalic(styleNode));
		nodeStyleModel.setShape(nodeStyleController.getShape(styleNode));

		styleNode.addExtension(nodeStyleModel);

		final EdgeModel standardEdgeModel = EdgeModel.getModel(styleNode);
		if(standardEdgeModel != null){
			final EdgeModel edgeModel = new EdgeModel();
			edgeModel.setColor(standardEdgeModel.getColor());
			edgeModel.setStyle(standardEdgeModel.getStyle());
			edgeModel.setWidth(standardEdgeModel.getWidth());
			styleNode.addExtension(edgeModel);
		}

        try {
	        fm.writeToFile(defaultStyleMap, userDefault);
        }
        catch (IOException e) {
        }
	}
	private NodeModel findDefaultStyleNode(MapModel defaultStyleMap) {
		NodeModel styleNode = MapStyleModel.getExtension(defaultStyleMap).getStyleNode(MapStyleModel.DEFAULT_STYLE);
        return styleNode;
	}

}
