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

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.styles.MapStyleModel;
import org.freeplane.features.mindmapmode.file.MFileManager;

/**
 * @author Dimitry Polivaev
 * Nov 1, 2010
 */
public class UserPropertiesUpdater {
	private File defaultTemplateFile() {
		MFileManager fm = (MFileManager) MFileManager.getController();
		final File userTemplates = fm.defaultUserTemplateDir();
		final File userDefault = new File(userTemplates, fm.getStandardTemplateName());
		if(userDefault.exists()){
			return userDefault;
		}
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
//    <combo name="standardnodeshape">
//        <choice value="fork"/>
//        <choice value="bubble"/>
//        <choice value="as_parent"/>
//        <choice value="combined"/>
//    </combo>
//    <combo name="standardrootnodeshape">
//        <choice value="fork"/>
//        <choice value="bubble"/>
//        <choice value="combined"/>
//    </combo>
//    <color name="standardnodetextcolor"/>
//    <color name="standardedgecolor"/>
//    <color name="standardlinkcolor"/>
//    <color name="standardbackgroundcolor"/>
//    <color name="standardcloudcolor"/>
//    <font name="defaultfont"/>
//    <number name="defaultfontsize" min="4" max="272"/>
//    <number name="defaultfontstyle" min="0" max="3"/>
//    <combo name="standardedgestyle">
//        <choice value="bezier"/>
//        <choice value="linear"/>
//        <choice value="sharp_bezier"/>
//        <choice value="sharp_linear"/>
//        <choice value="horizontal"/>
//        <choice value="hide_edge"/>
//    </combo>
		return userDefault;
	}
	private NodeModel findDefaultStyleNode(MapModel defaultStyleMap) {
		NodeModel styleNode = MapStyleModel.getExtension(defaultStyleMap).getStyleNode(MapStyleModel.DEFAULT_STYLE);
        return styleNode;
	}

}
