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
package org.freeplane.features.mindmapnode.pattern;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MindIcon;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.MultipleNodeAction;
import org.freeplane.features.common.edge.EdgeController;
import org.freeplane.features.common.edge.EdgeModel;
import org.freeplane.features.common.icon.IconController;
import org.freeplane.features.common.nodestyle.NodeStyleController;
import org.freeplane.features.common.text.TextController;
import org.freeplane.features.mindmapmode.edge.MEdgeController;
import org.freeplane.features.mindmapmode.icon.MIconController;
import org.freeplane.features.mindmapmode.nodestyle.MNodeStyleController;
import org.freeplane.features.mindmapmode.text.MTextController;

class ApplyPatternAction extends MultipleNodeAction {
	/**
	 */
	public static String edgeWidthIntToString(final int value) {
		if (value == EdgeModel.WIDTH_PARENT) {
			return null;
		}
		if (value == EdgeModel.WIDTH_THIN) {
			return EdgeModel.EDGE_WIDTH_THIN_STRING;
		}
		return Integer.toString(value);
	}

	/**
	 */
	public static int edgeWidthStringToInt(final String value) {
		if (value == null) {
			return EdgeModel.WIDTH_PARENT;
		}
		if (value.equals(EdgeModel.EDGE_WIDTH_THIN_STRING)) {
			return EdgeModel.WIDTH_THIN;
		}
		return Integer.valueOf(value).intValue();
	}

	final private Pattern mpattern;

	public ApplyPatternAction(final ModeController controller, final Pattern pattern) {
		super(controller.getController());
		mpattern = pattern;
		MenuBuilder.setLabelAndMnemonic(this, pattern.getName());
	}

	@Override
	protected void actionPerformed(final ActionEvent e, final NodeModel node) {
		applyPattern(node, mpattern);
	}

	public void applyPattern(final NodeModel node, final Pattern pattern) {
		final Controller controller = getController();
		if (pattern.getPatternNodeText() != null) {
			if (pattern.getPatternNodeText().getValue() != null) {
				((MTextController) TextController.getController(controller.getModeController())).setNodeText(node,
				    pattern.getPatternNodeText().getValue());
			}
			else {
				((MTextController) TextController.getController(controller.getModeController())).setNodeText(node, "");
			}
		}
		if (pattern.getPatternNodeColor() != null) {
			((MNodeStyleController) NodeStyleController.getController(controller.getModeController())).setColor(node,
			    TreeXmlReader.xmlToColor(pattern.getPatternNodeColor().getValue()));
		}
		if (pattern.getPatternNodeBackgroundColor() != null) {
			((MNodeStyleController) NodeStyleController.getController(controller.getModeController()))
			    .setBackgroundColor(node, TreeXmlReader.xmlToColor(pattern.getPatternNodeBackgroundColor().getValue()));
		}
		if (pattern.getPatternNodeStyle() != null) {
			((MNodeStyleController) NodeStyleController.getController(controller.getModeController())).setShape(node,
			    pattern.getPatternNodeStyle().getValue());
		}
		if (pattern.getPatternIcon() != null) {
			final String iconName = pattern.getPatternIcon().getValue();
			if (iconName == null) {
				while (((MIconController) IconController.getController(getModeController())).removeIcon(node,
				    MindIcon.LAST) > 0) {
				}
			}
			else {
				final List icons = node.getIcons();
				boolean found = false;
				for (final Iterator iterator = icons.iterator(); iterator.hasNext();) {
					final MindIcon icon = (MindIcon) iterator.next();
					if (icon.getName() != null && icon.getName().equals(iconName)) {
						found = true;
						break;
					}
				}
				if (!found) {
					((MIconController) IconController.getController(controller.getModeController())).addIcon(node,
					    MindIcon.factory(iconName), MindIcon.LAST);
				}
			}
		}
		if (pattern.getPatternNodeFontName() != null) {
			String nodeFontFamily = pattern.getPatternNodeFontName().getValue();
			if (nodeFontFamily == null) {
				nodeFontFamily = Controller.getResourceController().getDefaultFontFamilyName();
			}
			((MNodeStyleController) NodeStyleController.getController(controller.getModeController())).setFontFamily(
			    node, nodeFontFamily);
		}
		if (pattern.getPatternNodeFontSize() != null) {
			String nodeFontSize = pattern.getPatternNodeFontSize().getValue();
			if (nodeFontSize == null) {
				nodeFontSize = "" + Controller.getResourceController().getDefaultFontSize();
			}
			((MNodeStyleController) NodeStyleController.getController(controller.getModeController())).setFontSize(
			    node, Integer.parseInt(nodeFontSize));
		}
		if (pattern.getPatternNodeFontItalic() != null) {
			((MNodeStyleController) NodeStyleController.getController(controller.getModeController())).setItalic(node,
			    "true".equals(pattern.getPatternNodeFontItalic().getValue()));
		}
		if (pattern.getPatternNodeFontBold() != null) {
			((MNodeStyleController) NodeStyleController.getController(controller.getModeController())).setBold(node,
			    "true".equals(pattern.getPatternNodeFontBold().getValue()));
		}
		if (pattern.getPatternEdgeColor() != null) {
			((MEdgeController) EdgeController.getController(getModeController())).setColor(node, TreeXmlReader
			    .xmlToColor(pattern.getPatternEdgeColor().getValue()));
		}
		if (pattern.getPatternEdgeStyle() != null) {
			((MEdgeController) EdgeController.getController(getModeController())).setStyle(node, pattern
			    .getPatternEdgeStyle().getValue());
		}
		final PatternProperty patternEdgeWidth = pattern.getPatternEdgeWidth();
		if (patternEdgeWidth != null) {
			int width;
			if (patternEdgeWidth.getValue() != null) {
				width = ApplyPatternAction.edgeWidthStringToInt(patternEdgeWidth.getValue());
			}
			else {
				width = EdgeModel.WIDTH_PARENT;
			}
			((MEdgeController) EdgeController.getController(getModeController())).setWidth(node, width);
		}
		if (pattern.getPatternChild() != null && pattern.getPatternChild().getValue() != null) {
			final String searchedPatternName = pattern.getPatternChild().getValue();
			final ApplyPatternAction[] patterns = MPatternController.getController(controller.getModeController()).patterns;
			for (int i = 0; i < patterns.length; i++) {
				final ApplyPatternAction action = patterns[i];
				if (action.getPattern().getName().equals(searchedPatternName)) {
					for (final ListIterator j = node.getModeController().getMapController().childrenUnfolded(node); j
					    .hasNext();) {
						final NodeModel child = (NodeModel) j.next();
						applyPattern(child, action.getPattern());
					}
					break;
				}
			}
		}
		final IExternalPatternAction action = (IExternalPatternAction) getModeController().getExtension(
		    IExternalPatternAction.class);
		if (action != null) {
			final IExternalPatternAction externalAction = action;
			externalAction.act(node, pattern);
		}
	}

	/**
	 * @return Returns the pattern.
	 */
	public Pattern getPattern() {
		return mpattern;
	}
}
