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
package org.freeplane.map.pattern.mindmapnode;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.io.xml.TreeXmlWriter;
import org.freeplane.core.map.NodeModel;
import org.freeplane.core.util.Tools;
import org.freeplane.map.edge.EdgeModel;
import org.freeplane.map.icon.MindIcon;
import org.freeplane.map.nodestyle.NodeStyleModel;

/**
 * This class constructs patterns from files or from nodes and saves them back.
 */
public class StylePatternFactory {
	public static final String FALSE_VALUE = "false";
	private static final String PATTERN_DUMMY = "<pattern name='dummy'/>";
	private static final String PATTERNS_DUMMY = "<patterns/>";
	public static final String TRUE_VALUE = "true";

	private static String addSeparatorIfNecessary(String result) {
		if (result.length() > 0) {
			result += ", ";
		}
		return result;
	}

	private static String addSubPatternToString(String result, final PatternProperty patternType,
	                                            final String patternString) {
		if (patternType != null) {
			result = StylePatternFactory.addSeparatorIfNecessary(result);
			if (patternType.getValue() == null) {
				result += "-" + Controller.getText(patternString);
			}
			else {
				result += "+" + Controller.getText(patternString) + " " + patternType.getValue();
			}
		}
		return result;
	}

	public static Pattern createPatternFromNode(final NodeModel node) {
		final Pattern pattern = new Pattern();
		if (node.getColor() != null) {
			final PatternProperty subPattern = new PatternProperty();
			subPattern.setValue(TreeXmlWriter.colorToXml(node.getColor()));
			pattern.setPatternNodeColor(subPattern);
		}
		if (node.getBackgroundColor() != null) {
			final PatternProperty subPattern = new PatternProperty();
			subPattern.setValue(TreeXmlWriter.colorToXml(node.getBackgroundColor()));
			pattern.setPatternNodeBackgroundColor(subPattern);
		}
		if (node.getShape() != null) {
			final PatternProperty subPattern = new PatternProperty();
			subPattern.setValue(node.getShape());
			pattern.setPatternNodeStyle(subPattern);
		}
		final NodeStyleModel font = node.getNodeStyleModel();
		if (font != null) {
			final PatternProperty nodeFontBold = new PatternProperty();
			nodeFontBold.setValue(font.isBold() ? StylePatternFactory.TRUE_VALUE
			        : StylePatternFactory.FALSE_VALUE);
			pattern.setPatternNodeFontBold(nodeFontBold);
			final PatternProperty nodeFontItalic = new PatternProperty();
			nodeFontItalic.setValue(font.isItalic() ? StylePatternFactory.TRUE_VALUE
			        : StylePatternFactory.FALSE_VALUE);
			pattern.setPatternNodeFontItalic(nodeFontItalic);
			final PatternProperty nodeFontSize = new PatternProperty();
			nodeFontSize.setValue("" + font.getFontSize());
			pattern.setPatternNodeFontSize(nodeFontSize);
			final PatternProperty subPattern = new PatternProperty();
			subPattern.setValue(font.getFontFamilyName());
			pattern.setPatternNodeFontName(subPattern);
		}
		if (node.getIcons().size() == 1) {
			final PatternProperty iconPattern = new PatternProperty();
			iconPattern.setValue(((MindIcon) node.getIcons().get(0)).getName());
			pattern.setPatternIcon(iconPattern);
		}
		final EdgeModel edge = node.getEdge();
		if (edge != null) {
			final Color edgeColor = edge.getColor();
			if (edgeColor != null) {
				final PatternProperty colorPattern = new PatternProperty();
				colorPattern.setValue(TreeXmlWriter.colorToXml(edgeColor));
				pattern.setPatternEdgeColor(colorPattern);
			}
			final String edgeStyle = edge.getStyle();
			if (edgeStyle != null) {
				final PatternProperty stylePattern = new PatternProperty();
				stylePattern.setValue(edgeStyle);
				pattern.setPatternEdgeStyle(stylePattern);
			}
			final int edgeWidth = edge.getWidth();
			if (edgeWidth != EdgeModel.WIDTH_PARENT) {
				final PatternProperty edgeWidthPattern = new PatternProperty();
				edgeWidthPattern.setValue("" + edgeWidth);
				pattern.setPatternEdgeWidth(edgeWidthPattern);
			}
		}
		return pattern;
	}

	public static Pattern createPatternFromSelected(final NodeModel focussed, final List selected) {
		Pattern nodePattern = StylePatternFactory.createPatternFromNode(focussed);
		for (final Iterator iter = selected.iterator(); iter.hasNext();) {
			final NodeModel node = (NodeModel) iter.next();
			final Pattern tempNodePattern = StylePatternFactory.createPatternFromNode(node);
			nodePattern = StylePatternFactory.intersectPattern(nodePattern, tempNodePattern);
		}
		return nodePattern;
	}

	public static Pattern getPatternFromString(final String pattern) {
		String patternString = pattern;
		if (patternString == null) {
			patternString = StylePatternFactory.PATTERN_DUMMY;
		}
		final Pattern pat = Pattern.unMarshall(patternString);
		return pat;
	}

	public static Patterns getPatternsFromString(final String patterns) {
		String patternsString = patterns;
		if (patternsString == null) {
			patternsString = StylePatternFactory.PATTERNS_DUMMY;
		}
		final Patterns pat = Patterns.unMarshall(patternsString);
		return pat;
	}

	/**
	 * Build the intersection of two patterns. Only, if the property is the
	 * same, or both properties are to be removed, it is kept, otherwise it is
	 * set to 'don't touch'.
	 */
	public static Pattern intersectPattern(final Pattern p1, final Pattern p2) {
		final Pattern result = new Pattern();
		result.setPatternEdgeColor(StylePatternFactory.processPatternProperties(p1
		    .getPatternEdgeColor(), p2.getPatternEdgeColor(), new PatternProperty()));
		result.setPatternEdgeStyle(StylePatternFactory.processPatternProperties(p1
		    .getPatternEdgeStyle(), p2.getPatternEdgeStyle(), new PatternProperty()));
		result.setPatternEdgeWidth(StylePatternFactory.processPatternProperties(p1
		    .getPatternEdgeWidth(), p2.getPatternEdgeWidth(), new PatternProperty()));
		result.setPatternIcon(StylePatternFactory.processPatternProperties(p1.getPatternIcon(), p2
		    .getPatternIcon(), new PatternProperty()));
		result.setPatternNodeBackgroundColor(StylePatternFactory.processPatternProperties(p1
		    .getPatternNodeBackgroundColor(), p2.getPatternNodeBackgroundColor(),
		    new PatternProperty()));
		result.setPatternNodeColor(StylePatternFactory.processPatternProperties(p1
		    .getPatternNodeColor(), p2.getPatternNodeColor(), new PatternProperty()));
		result.setPatternNodeFontBold(StylePatternFactory.processPatternProperties(p1
		    .getPatternNodeFontBold(), p2.getPatternNodeFontBold(), new PatternProperty()));
		result.setPatternNodeFontItalic(StylePatternFactory.processPatternProperties(p1
		    .getPatternNodeFontItalic(), p2.getPatternNodeFontItalic(), new PatternProperty()));
		result.setPatternNodeFontName(StylePatternFactory.processPatternProperties(p1
		    .getPatternNodeFontName(), p2.getPatternNodeFontName(), new PatternProperty()));
		result.setPatternNodeFontSize(StylePatternFactory.processPatternProperties(p1
		    .getPatternNodeFontSize(), p2.getPatternNodeFontSize(), new PatternProperty()));
		result.setPatternNodeStyle(StylePatternFactory.processPatternProperties(p1
		    .getPatternNodeStyle(), p2.getPatternNodeStyle(), new PatternProperty()));
		return result;
	}

	public static List loadPatterns(final File file) throws Exception {
		return StylePatternFactory.loadPatterns(new BufferedReader(new FileReader(file)));
	}

	/**
	 * @return a List of Pattern elements.
	 * @throws Exception
	 */
	public static List loadPatterns(final Reader reader) throws Exception {
		final Patterns patterns = Patterns.unMarshall(reader);
		return patterns.getListChoiceList();
	}

	private static PatternProperty processPatternProperties(final PatternProperty prop1,
	                                                        final PatternProperty prop2,
	                                                        final PatternProperty destination) {
		if (prop1 == null || prop2 == null) {
			return null;
		}
		if (Tools.safeEquals(prop1.getValue(), prop2.getValue())) {
			destination.setValue(prop1.getValue());
			return destination;
		}
		return null;
	}

	/**
	 * the result is written to, and it is closed afterwards List of Pattern
	 * elements.
	 *
	 * @throws Exception
	 */
	public static void savePatterns(final Writer writer, final List listOfPatterns)
	        throws Exception {
		final Patterns patterns = new Patterns();
		for (final Iterator iter = listOfPatterns.iterator(); iter.hasNext();) {
			final Pattern pattern = (Pattern) iter.next();
			patterns.addChoice(pattern);
		}
		final String marshalledResult = patterns.marshall();
		writer.write(marshalledResult);
		writer.close();
	}

	public static String toString(final Pattern pPattern) {
		String result = "";
		if (pPattern.getPatternNodeColor() != null) {
			result = StylePatternFactory.addSeparatorIfNecessary(result);
			if (pPattern.getPatternNodeColor().getValue() == null) {
				result += "-" + Controller.getText("PatternToString.color");
			}
			else {
				result += "+" + Controller.getText("PatternToString.color");
			}
		}
		if (pPattern.getPatternNodeBackgroundColor() != null) {
			result = StylePatternFactory.addSeparatorIfNecessary(result);
			if (pPattern.getPatternNodeBackgroundColor().getValue() == null) {
				result += "-" + Controller.getText("PatternToString.backgroundColor");
			}
			else {
				result += "+" + Controller.getText("PatternToString.backgroundColor");
			}
		}
		result = StylePatternFactory.addSubPatternToString(result, pPattern
		    .getPatternNodeFontSize(), "PatternToString.NodeFontSize");
		result = StylePatternFactory.addSubPatternToString(result, pPattern
		    .getPatternNodeFontName(), "PatternToString.FontName");
		result = StylePatternFactory.addSubPatternToString(result, pPattern
		    .getPatternNodeFontBold(), "PatternToString.FontBold");
		result = StylePatternFactory.addSubPatternToString(result, pPattern
		    .getPatternNodeFontItalic(), "PatternToString.FontItalic");
		result = StylePatternFactory.addSubPatternToString(result, pPattern.getPatternEdgeStyle(),
		    "PatternToString.EdgeStyle");
		result = StylePatternFactory.addSubPatternToString(result, pPattern.getPatternEdgeColor(),
		    "PatternToString.EdgeColor");
		result = StylePatternFactory.addSubPatternToString(result, pPattern.getPatternEdgeWidth(),
		    "PatternToString.EdgeWidth");
		result = StylePatternFactory.addSubPatternToString(result, pPattern.getPatternIcon(),
		    "PatternToString.Icon");
		result = StylePatternFactory.addSubPatternToString(result, pPattern.getPatternChild(),
		    "PatternToString.Child");
		return result;
	}
}
