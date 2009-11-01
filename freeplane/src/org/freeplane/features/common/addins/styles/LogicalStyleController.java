/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry
 *
 *  This file author is Dimitry
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
package org.freeplane.features.common.addins.styles;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IAttributeHandler;
import org.freeplane.core.io.IAttributeWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.MapReader;
import org.freeplane.core.io.MapWriter;
import org.freeplane.core.io.NodeBuilder;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.modecontroller.MapController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.NamedObject;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.features.mindmapmode.addins.styles.MLogicalStyleController;

/**
 * @author Dimitry Polivaev
 * 28.09.2009
 */
public class LogicalStyleController implements IExtension{
	public LogicalStyleController(ModeController modeController){
		final MapController mapController = modeController.getMapController();
		final ReadManager readManager = mapController.getReadManager();
		readManager.addAttributeHandler(NodeBuilder.XML_NODE, "STYLE_REF", new IAttributeHandler() {
			public void setAttribute(Object node, String value) {
				final LogicalStyleModel extension = LogicalStyleModel.createExtension((NodeModel) node);
				extension.setStyle(value);
			}
		});
		readManager.addAttributeHandler(NodeBuilder.XML_NODE, "LOCALIZED_STYLE_REF", new IAttributeHandler() {
			public void setAttribute(Object node, String value) {
				final LogicalStyleModel extension = LogicalStyleModel.createExtension((NodeModel) node);
				extension.setStyle(NamedObject.formatText(value));
			}
		});
		final WriteManager writeManager = mapController.getWriteManager();
		writeManager.addAttributeWriter(NodeBuilder.XML_NODE, new IAttributeWriter() {
			public void writeAttributes(ITreeWriter writer, Object node, String tag) {
				final LogicalStyleModel extension = LogicalStyleModel.getExtension((NodeModel) node);
				if(extension == null){
					return;
				}
				final Object style = extension.getStyle();
				if(style == null || style.equals(MapStyleModel.DEFAULT_STYLE)){
					return;
				}
				String value = NamedObject.toKeyString(style);
				if(style instanceof NamedObject){
					writer.addAttribute("LOCALIZED_STYLE_REF", value);
				}
				else{
					writer.addAttribute("STYLE_REF", value);
				}
			}
		});
	}

	public static void install(MModeController modeController, LogicalStyleController logicalStyleController) {
		modeController.addExtension(LogicalStyleController.class, logicalStyleController);
	    
    }
}
