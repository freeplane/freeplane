/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
package org.freeplane.features.common.addins.mapstyle;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.StringWriter;

import org.freeplane.core.addins.NodeHookDescriptor;
import org.freeplane.core.addins.PersistentNodeHook;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IElementContentHandler;
import org.freeplane.core.io.IElementHandler;
import org.freeplane.core.io.IExtensionElementWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.MapWriter;
import org.freeplane.core.io.MapWriter.Mode;
import org.freeplane.core.modecontroller.IMapLifeCycleListener;
import org.freeplane.core.modecontroller.MapChangeEvent;
import org.freeplane.core.modecontroller.MapController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.features.common.nodestyle.NodeStyleModel;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * Mar 9, 2009
 */
@NodeHookDescriptor(hookName = "MapStyle")
public class MapStyle extends PersistentNodeHook implements IExtension, IMapLifeCycleListener {
	public static final String RESOURCES_BACKGROUND_COLOR = "standardbackgroundcolor";
	public static final String MAP_STYLES = "MAP_STYLES";

	public MapStyle(final ModeController modeController) {
		super(modeController);
		if (modeController.getModeName().equals("MindMap")) {
			registerAction(new MapBackgroundColorAction(this));
		}
		final MapController mapController = modeController.getMapController();
		mapController.addMapLifeCycleListener(this);
	}

	protected class XmlWriter implements IExtensionElementWriter {
		public void writeContent(final ITreeWriter writer, final Object object, final IExtension extension)
		        throws IOException {
			final MapStyleModel mapStyleModel = (MapStyleModel) extension;
			final MapModel styleMap = mapStyleModel.getStyleMap();
			if(styleMap == null){
				return;
			}
			final MapWriter mapWriter = getModeController().getMapController().getMapWriter();
			StringWriter sw = new StringWriter();
			final NodeModel rootNode = styleMap.getRootNode();
			try {
		        mapWriter.writeNodeAsXml(sw, rootNode, Mode.STYLE, true, true);
	        }
	        catch (IOException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
	        }
			final XMLElement element = new XMLElement("hook");
			saveExtension(extension, element);
			writer.addElement(sw.toString(), element);
		}
	}
	protected XmlWriter createXmlWriter() {
		return new XmlWriter();
	}

	protected class MyXmlReader extends XmlReader implements IElementContentHandler {

		public void endElement(Object parent, String tag, Object userObject, XMLElement attributes, String content) {
			super.endElement(parent, tag, userObject, attributes);
			final NodeModel node = (NodeModel) userObject;
			MapStyleModel.getExtension(node).createStyleMap(getModeController(), content);
        }
	}

	protected IElementHandler createXmlReader() {
		return new MyXmlReader();
	}

	@Override
	protected IExtension createExtension(final NodeModel node, final XMLElement element) {
		final String colorString = element.getAttribute("background", null);
		final Color bgColor;
		if (colorString != null) {
			bgColor = ColorUtils.stringToColor(colorString);
		}
		else {
			bgColor = null;
		}
		final MapStyleModel model = new MapStyleModel();
		model.setBackgroundColor(bgColor);
		return model;
	}

	public Color getBackground(final MapModel map) {
		final IExtension extension = map.getRootNode().getExtension(MapStyleModel.class);
		final Color backgroundColor = extension != null ? ((MapStyleModel) extension).getBackgroundColor() : null;
		if (backgroundColor != null) {
			return backgroundColor;
		}
		final String stdcolor = ResourceController.getResourceController().getProperty(
		    MapStyle.RESOURCES_BACKGROUND_COLOR);
		final Color standardMapBackgroundColor = ColorUtils.stringToColor(stdcolor);
		return standardMapBackgroundColor;
	}

	@Override
	protected Class getExtensionClass() {
		return MapStyleModel.class;
	}

	public void onCreate(final MapModel map) {
		final NodeModel rootNode = map.getRootNode();
		if (rootNode.containsExtension(MapStyleModel.class)) {
			return;
		}
		final MapStyleModel extension = new MapStyleModel();
		rootNode.addExtension(extension);
		extension.createStyleMap(getModeController(), null);
	}

	public void onRemove(final MapModel map) {
	}

	@Override
	protected void saveExtension(final IExtension extension, final XMLElement element) {
		final MapStyleModel mapStyleModel = (MapStyleModel) extension;
		super.saveExtension(extension, element);
		final Color backgroundColor = mapStyleModel.getBackgroundColor();
		if (backgroundColor != null) {
			element.setAttribute("background", ColorUtils.colorToString(backgroundColor));
		}
	}

	public void setBackgroundColor(final MapStyleModel model, final Color actionColor) {
		final Color oldColor = model.getBackgroundColor();
		if (actionColor == oldColor || actionColor != null && actionColor.equals(oldColor)) {
			return;
		}
		final IActor actor = new IActor() {
			public void act() {
				model.setBackgroundColor(actionColor);
				getModeController().getMapController().fireMapChanged(
				    new MapChangeEvent(MapStyle.this, getController().getMap(), MapStyle.RESOURCES_BACKGROUND_COLOR,
				        oldColor, actionColor));
			}

			public String getDescription() {
				return "MapStyle.setBackgroundColor";
			}

			public void undo() {
				model.setBackgroundColor(oldColor);
				getModeController().getMapController().fireMapChanged(
				    new MapChangeEvent(MapStyle.this, getController().getMap(), MapStyle.RESOURCES_BACKGROUND_COLOR,
				        actionColor, oldColor));
			}
		};
		getModeController().execute(actor, getController().getMap());
	}

}
