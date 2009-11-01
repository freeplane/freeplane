/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Dimitry Polivaev in 2008.
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
package org.freeplane.features.mindmapmode.addins.styles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JOptionPane;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.icon.IconController;
import org.freeplane.core.icon.IconStore;
import org.freeplane.core.icon.factory.IconStoreFactory;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.url.UrlManager;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.core.util.LogTool;
import org.freeplane.features.common.cloud.CloudController;
import org.freeplane.features.common.edge.EdgeController;
import org.freeplane.features.common.edge.EdgeModel;
import org.freeplane.features.common.edge.EdgeStyle;
import org.freeplane.features.common.nodestyle.NodeStyleController;
import org.freeplane.features.common.text.TextController;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.features.mindmapmode.cloud.MCloudController;
import org.freeplane.features.mindmapmode.edge.MEdgeController;
import org.freeplane.features.mindmapmode.icon.MIconController;
import org.freeplane.features.mindmapmode.nodestyle.MNodeStyleController;
import org.freeplane.features.mindmapmode.text.MTextController;

/**
 * @author Dimitry Polivaev
 */
public class MPatternController implements IExtension {
	public static MPatternController getController(final ModeController modeController) {
		return (MPatternController) modeController.getExtension(MPatternController.class);
	}

	private static final IconStore STORE = IconStoreFactory.create();
	private static final String EDGE_WIDTH_THIN_STRING = "thin";
	public static void install(final ModeController modeController, final MPatternController patternController) {
		modeController.addExtension(MPatternController.class, patternController);
	}
	private MModeController modeController;
	public MPatternController(final ModeController modeController) {
		super();
		this.modeController = (MModeController) modeController;
	}

	public void applyPattern(final NodeModel node, final Pattern pattern) {
			if (pattern.getPatternNodeText() != null) {
				if (pattern.getPatternNodeText().getValue() != null) {
					((MTextController) TextController.getController(modeController)).setNodeText(node,
					    pattern.getPatternNodeText().getValue());
				}
				else {
					((MTextController) TextController.getController(modeController)).setNodeText(node, "");
				}
			}
			if (pattern.getPatternNodeColor() != null) {
				((MNodeStyleController) NodeStyleController.getController(modeController)).setColor(node,
				    ColorUtils.stringToColor(pattern.getPatternNodeColor().getValue()));
			}
			if (pattern.getPatternNodeBackgroundColor() != null) {
				((MNodeStyleController) NodeStyleController.getController(modeController))
				    .setBackgroundColor(node, ColorUtils.stringToColor(pattern.getPatternNodeBackgroundColor().getValue()));
			}
			if (pattern.getPatternNodeStyle() != null) {
				((MNodeStyleController) NodeStyleController.getController(modeController)).setShape(node,
				    pattern.getPatternNodeStyle().getValue());
			}
			if (pattern.getPatternIcon() != null) {
				final String iconName = pattern.getPatternIcon().getValue();
				while (((MIconController) IconController.getController(modeController))
				    .removeIcon(node) > 0) {
				}
				if (iconName != null) {
					((MIconController) IconController.getController(modeController)).addIcon(node, STORE.getMindIcon(iconName));
				}
			}
			if (pattern.getPatternNodeFontName() != null) {
				String nodeFontFamily = pattern.getPatternNodeFontName().getValue();
				((MNodeStyleController) NodeStyleController.getController(modeController)).setFontFamily(
				    node, nodeFontFamily);
			}
			if (pattern.getPatternNodeFontSize() != null) {
				String nodeFontSize = pattern.getPatternNodeFontSize().getValue();
				((MNodeStyleController) NodeStyleController.getController(modeController)).setFontSize(
				    node, nodeFontSize == null ? null : Integer.valueOf(nodeFontSize));
			}
			if (pattern.getPatternNodeFontItalic() != null) {
				((MNodeStyleController) NodeStyleController.getController(modeController)).setItalic(node,
				    "true".equals(pattern.getPatternNodeFontItalic().getValue()));
			}
			if (pattern.getPatternNodeFontBold() != null) {
				((MNodeStyleController) NodeStyleController.getController(modeController)).setBold(node,
				    "true".equals(pattern.getPatternNodeFontBold().getValue()));
			}
			if (pattern.getPatternCloud() != null) {
				((MCloudController) CloudController.getController(modeController)).setCloud(node, "true"
				    .equals(pattern.getPatternCloud().getValue()));
			}
			if (pattern.getPatternCloudColor() != null) {
				((MCloudController) CloudController.getController(modeController)).setColor(node, ColorUtils
				    .stringToColor(pattern.getPatternCloudColor().getValue()));
			}
			if (pattern.getPatternEdgeColor() != null) {
				((MEdgeController) EdgeController.getController(modeController)).setColor(node, ColorUtils
				    .stringToColor(pattern.getPatternEdgeColor().getValue()));
			}
			if (pattern.getPatternEdgeStyle() != null) {
				final String value = pattern
				    .getPatternEdgeStyle().getValue();
				((MEdgeController) EdgeController.getController(modeController)).setStyle(node, EdgeStyle.getStyle(value));
			}
			final PatternProperty patternEdgeWidth = pattern.getPatternEdgeWidth();
			if (patternEdgeWidth != null) {
				int width;
				if (patternEdgeWidth.getValue() != null) {
					width = edgeWidthStringToInt(patternEdgeWidth.getValue());
				}
				else {
					width = EdgeModel.WIDTH_PARENT;
				}
				((MEdgeController) EdgeController.getController(modeController)).setWidth(node, width);
			}
	}

	/**
	 */
	public static String edgeWidthIntToString(final int value) {
		if (value == EdgeModel.WIDTH_PARENT) {
			return null;
		}
		if (value == EdgeModel.WIDTH_THIN) {
			return EDGE_WIDTH_THIN_STRING;
		}
		return Integer.toString(value);
	}

	/**
	 */
	public static int edgeWidthStringToInt(final String value) {
		if (value == null) {
			return EdgeModel.WIDTH_PARENT;
		}
		if (value.equals(EDGE_WIDTH_THIN_STRING)) {
			return EdgeModel.WIDTH_THIN;
		}
		return Integer.valueOf(value).intValue();
	}


}
