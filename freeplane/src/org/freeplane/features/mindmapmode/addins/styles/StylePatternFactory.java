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
package org.freeplane.features.mindmapmode.addins.styles;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

import javax.swing.text.StyledEditorKit.BoldAction;

import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.features.common.cloud.CloudModel;
import org.freeplane.features.common.edge.EdgeModel;
import org.freeplane.features.common.edge.EdgeStyle;
import org.freeplane.features.common.nodestyle.NodeStyleModel;

/**
 * This class constructs patterns from files or from nodes and saves them back.
 */
public class StylePatternFactory {

	public static Pattern createPatternFromNode(final NodeModel node) {
		final Pattern pattern = new Pattern();
		{
			final PatternProperty subPattern = new PatternProperty();
			subPattern.setValue(ColorUtils.colorToString(NodeStyleModel.getColor(node)));
			pattern.setPatternNodeColor(subPattern);
		}
		{
			final PatternProperty subPattern = new PatternProperty();
			subPattern.setValue(ColorUtils.colorToString(NodeStyleModel.getBackgroundColor(node)));
			pattern.setPatternNodeBackgroundColor(subPattern);
		}
		{
			final PatternProperty subPattern = new PatternProperty();
			subPattern.setValue(NodeStyleModel.getShape(node));
			pattern.setPatternNodeStyle(subPattern);
		}
		final NodeStyleModel font = NodeStyleModel.getModel(node);
		final PatternProperty nodeFontBold = new PatternProperty();
		final PatternProperty nodeFontItalic = new PatternProperty();
		final PatternProperty nodeFontSize = new PatternProperty();
		final PatternProperty nodeFontName = new PatternProperty();
		if (font != null) {
			if (font.isBold() != null) {
				nodeFontBold.setValue(font.isBold().toString());
			}
			if (font.isItalic() != null) {
				nodeFontItalic.setValue(font.isItalic().toString());
			}
			if (font.getFontSize() != null) {
				nodeFontSize.setValue("" + font.getFontSize());
			}
			if (font.getFontFamilyName() != null) {
				nodeFontName.setValue(font.getFontFamilyName());
			}
		}
		pattern.setPatternNodeFontBold(nodeFontBold);
		pattern.setPatternNodeFontItalic(nodeFontItalic);
		pattern.setPatternNodeFontSize(nodeFontSize);
		pattern.setPatternNodeFontName(nodeFontName);
		
		final PatternProperty iconPattern = new PatternProperty();
		if (node.getIcons().size() == 1) {
			iconPattern.setValue((node.getIcons().get(0)).getName());
			pattern.setPatternIcon(iconPattern);
		}
		else if (node.getIcons().size() == 0) {
			pattern.setPatternIcon(iconPattern);
		}
		final EdgeModel edge = EdgeModel.getModel(node);
		final PatternProperty edgeColorPattern = new PatternProperty();
		final PatternProperty edgeStylePattern = new PatternProperty();
		final PatternProperty edgeWidthPattern = new PatternProperty();
		if (edge != null) {
			final Color edgeColor = edge.getColor();
			if (edgeColor != null) {
				edgeColorPattern.setValue(ColorUtils.colorToString(edgeColor));
			}
			final String edgeStyle = EdgeStyle.toString(edge.getStyle());
			if (edgeStyle != null) {
				edgeStylePattern.setValue(edgeStyle);
			}
			final int edgeWidth = edge.getWidth();
			if (edgeWidth != EdgeModel.WIDTH_PARENT) {
				edgeWidthPattern.setValue("" + edgeWidth);
			}
		}
		pattern.setPatternEdgeColor(edgeColorPattern);
		pattern.setPatternEdgeStyle(edgeStylePattern);
		pattern.setPatternEdgeWidth(edgeWidthPattern);
		
		final CloudModel cloud = CloudModel.getModel(node);
		final PatternProperty cloudColorPattern = new PatternProperty();
		final PatternProperty cloudPattern = new PatternProperty();
		if (cloud != null) {
			final Color cloudColor = cloud.getColor();
			if (cloudColor != null) {
				cloudColorPattern.setValue(ColorUtils.colorToString(cloudColor));
			}
			else {
				cloudPattern.setValue(Boolean.TRUE.toString());
			}
		}
		else{
			pattern.setPatternCloud(cloudPattern);
		}
		return pattern;
	}

	public static Pattern createPatternFromSelected(final NodeModel focussed, final List<NodeModel> selected) {
		Pattern nodePattern = StylePatternFactory.createPatternFromNode(focussed);
		for (final NodeModel node : selected) {
			final Pattern tempNodePattern = StylePatternFactory.createPatternFromNode(node);
			nodePattern = StylePatternFactory.intersectPattern(nodePattern, tempNodePattern);
		}
		return nodePattern;
	}

	/**
	 * Build the intersection of two patterns. Only, if the property is the
	 * same, or both properties are to be removed, it is kept, otherwise it is
	 * set to 'don't touch'.
	 */
	public static Pattern intersectPattern(final Pattern p1, final Pattern p2) {
		final Pattern result = new Pattern();
		result.setPatternEdgeColor(StylePatternFactory.processPatternProperties(p1.getPatternEdgeColor(), p2
		    .getPatternEdgeColor(), new PatternProperty()));
		result.setPatternCloud(StylePatternFactory.processPatternProperties(p1.getPatternCloud(), p2.getPatternCloud(),
		    new PatternProperty()));
		result.setPatternCloudColor(StylePatternFactory.processPatternProperties(p1.getPatternCloudColor(), p2
		    .getPatternCloudColor(), new PatternProperty()));
		result.setPatternEdgeStyle(StylePatternFactory.processPatternProperties(p1.getPatternEdgeStyle(), p2
		    .getPatternEdgeStyle(), new PatternProperty()));
		result.setPatternEdgeWidth(StylePatternFactory.processPatternProperties(p1.getPatternEdgeWidth(), p2
		    .getPatternEdgeWidth(), new PatternProperty()));
		result.setPatternIcon(StylePatternFactory.processPatternProperties(p1.getPatternIcon(), p2.getPatternIcon(),
		    new PatternProperty()));
		result.setPatternNodeBackgroundColor(StylePatternFactory.processPatternProperties(p1
		    .getPatternNodeBackgroundColor(), p2.getPatternNodeBackgroundColor(), new PatternProperty()));
		result.setPatternNodeColor(StylePatternFactory.processPatternProperties(p1.getPatternNodeColor(), p2
		    .getPatternNodeColor(), new PatternProperty()));
		result.setPatternNodeFontBold(StylePatternFactory.processPatternProperties(p1.getPatternNodeFontBold(), p2
		    .getPatternNodeFontBold(), new PatternProperty()));
		result.setPatternNodeFontItalic(StylePatternFactory.processPatternProperties(p1.getPatternNodeFontItalic(), p2
		    .getPatternNodeFontItalic(), new PatternProperty()));
		result.setPatternNodeFontName(StylePatternFactory.processPatternProperties(p1.getPatternNodeFontName(), p2
		    .getPatternNodeFontName(), new PatternProperty()));
		result.setPatternNodeFontSize(StylePatternFactory.processPatternProperties(p1.getPatternNodeFontSize(), p2
		    .getPatternNodeFontSize(), new PatternProperty()));
		result.setPatternNodeStyle(StylePatternFactory.processPatternProperties(p1.getPatternNodeStyle(), p2
		    .getPatternNodeStyle(), new PatternProperty()));
		return result;
	}

	private static PatternProperty processPatternProperties(final PatternProperty prop1, final PatternProperty prop2,
	                                                        final PatternProperty destination) {
		if (prop1 == null || prop2 == null) {
			return null;
		}
		if (StylePatternFactory.safeEquals(prop1.getValue(), prop2.getValue())) {
			destination.setValue(prop1.getValue());
			return destination;
		}
		return null;
	}

	private static boolean safeEquals(final String string1, final String string2) {
		return (string1 != null && string2 != null && string1.equals(string2)) || (string1 == null && string2 == null);
	}

}
