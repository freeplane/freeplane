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
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import org.freeplane.controller.Freeplane;
import org.freeplane.main.Tools;
import org.freeplane.map.edge.EdgeModel;
import org.freeplane.map.icon.MindIcon;
import org.freeplane.map.tree.NodeModel;

import deprecated.freemind.common.ITextTranslator;
import deprecated.freemind.common.XmlBindingTools;
import freemind.controller.actions.generated.instance.Pattern;
import freemind.controller.actions.generated.instance.PatternEdgeColor;
import freemind.controller.actions.generated.instance.PatternEdgeStyle;
import freemind.controller.actions.generated.instance.PatternEdgeWidth;
import freemind.controller.actions.generated.instance.PatternIcon;
import freemind.controller.actions.generated.instance.PatternNodeBackgroundColor;
import freemind.controller.actions.generated.instance.PatternNodeColor;
import freemind.controller.actions.generated.instance.PatternNodeFontBold;
import freemind.controller.actions.generated.instance.PatternNodeFontItalic;
import freemind.controller.actions.generated.instance.PatternNodeFontName;
import freemind.controller.actions.generated.instance.PatternNodeFontSize;
import freemind.controller.actions.generated.instance.PatternNodeStyle;
import freemind.controller.actions.generated.instance.PatternPropertyBase;
import freemind.controller.actions.generated.instance.Patterns;

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

	private static String addSubPatternToString(
	                                            String result,
	                                            final PatternPropertyBase patternType,
	                                            final String patternString) {
		if (patternType != null) {
			result = StylePatternFactory.addSeparatorIfNecessary(result);
			if (patternType.getValue() == null) {
				result += "-"
				        + Freeplane.getController().getResourceController()
				            .getResourceString(patternString);
			}
			else {
				result += "+"
				        + Freeplane.getController().getResourceController()
				            .getResourceString(patternString) + " "
				        + patternType.getValue();
			}
		}
		return result;
	}

	public static Pattern createPatternFromNode(final NodeModel node) {
		final Pattern pattern = new Pattern();
		if (node.getColor() != null) {
			final PatternNodeColor subPattern = new PatternNodeColor();
			subPattern.setValue(Tools.colorToXml(node.getColor()));
			pattern.setPatternNodeColor(subPattern);
		}
		if (node.getBackgroundColor() != null) {
			final PatternNodeBackgroundColor subPattern = new PatternNodeBackgroundColor();
			subPattern.setValue(Tools.colorToXml(node.getBackgroundColor()));
			pattern.setPatternNodeBackgroundColor(subPattern);
		}
		if (node.getShape() != null) {
			final PatternNodeStyle subPattern = new PatternNodeStyle();
			subPattern.setValue(node.getShape());
			pattern.setPatternNodeStyle(subPattern);
		}
		final Font font = node.getFont();
		if (font != null) {
			final PatternNodeFontBold nodeFontBold = new PatternNodeFontBold();
			nodeFontBold
			    .setValue(font.isBold() ? StylePatternFactory.TRUE_VALUE
			            : StylePatternFactory.FALSE_VALUE);
			pattern.setPatternNodeFontBold(nodeFontBold);
			final PatternNodeFontItalic nodeFontItalic = new PatternNodeFontItalic();
			nodeFontItalic
			    .setValue(font.isItalic() ? StylePatternFactory.TRUE_VALUE
			            : StylePatternFactory.FALSE_VALUE);
			pattern.setPatternNodeFontItalic(nodeFontItalic);
			final PatternNodeFontSize nodeFontSize = new PatternNodeFontSize();
			nodeFontSize.setValue("" + font.getSize());
			pattern.setPatternNodeFontSize(nodeFontSize);
			final PatternNodeFontName subPattern = new PatternNodeFontName();
			subPattern.setValue(font.getFontName());
			pattern.setPatternNodeFontName(subPattern);
		}
		if (node.getIcons().size() == 1) {
			final PatternIcon iconPattern = new PatternIcon();
			iconPattern.setValue(((MindIcon) node.getIcons().get(0)).getName());
			pattern.setPatternIcon(iconPattern);
		}
		final EdgeModel edge = node.getEdge();
		if (edge != null) {
			final Color edgeColor = edge.getColor();
			if (edgeColor != null) {
				final PatternEdgeColor colorPattern = new PatternEdgeColor();
				colorPattern.setValue(Tools.colorToXml(edgeColor));
				pattern.setPatternEdgeColor(colorPattern);
			}
			final String edgeStyle = edge.getStyle();
			if (edgeStyle != null) {
				final PatternEdgeStyle stylePattern = new PatternEdgeStyle();
				stylePattern.setValue(edgeStyle);
				pattern.setPatternEdgeStyle(stylePattern);
			}
			final int edgeWidth = edge.getWidth();
			if (edgeWidth != EdgeModel.WIDTH_PARENT) {
				final PatternEdgeWidth edgeWidthPattern = new PatternEdgeWidth();
				edgeWidthPattern.setValue("" + edgeWidth);
				pattern.setPatternEdgeWidth(edgeWidthPattern);
			}
		}
		return pattern;
	}

	public static Pattern createPatternFromSelected(final NodeModel focussed,
	                                                final List selected) {
		Pattern nodePattern = StylePatternFactory
		    .createPatternFromNode(focussed);
		for (final Iterator iter = selected.iterator(); iter.hasNext();) {
			final NodeModel node = (NodeModel) iter.next();
			final Pattern tempNodePattern = StylePatternFactory
			    .createPatternFromNode(node);
			nodePattern = StylePatternFactory.intersectPattern(nodePattern,
			    tempNodePattern);
		}
		return nodePattern;
	}

	public static Pattern getPatternFromString(final String pattern) {
		String patternString = pattern;
		if (patternString == null) {
			patternString = StylePatternFactory.PATTERN_DUMMY;
		}
		final Pattern pat = (Pattern) XmlBindingTools.getInstance().unMarshall(
		    patternString);
		return pat;
	}

	public static Patterns getPatternsFromString(final String patterns) {
		String patternsString = patterns;
		if (patternsString == null) {
			patternsString = StylePatternFactory.PATTERNS_DUMMY;
		}
		final Patterns pat = (Patterns) XmlBindingTools.getInstance()
		    .unMarshall(patternsString);
		return pat;
	}

	/**
	 * Build the intersection of two patterns. Only, if the property is the
	 * same, or both properties are to be removed, it is kept, otherwise it is
	 * set to 'don't touch'.
	 */
	public static Pattern intersectPattern(final Pattern p1, final Pattern p2) {
		final Pattern result = new Pattern();
		result.setPatternEdgeColor((PatternEdgeColor) StylePatternFactory
		    .processPatternProperties(p1.getPatternEdgeColor(), p2
		        .getPatternEdgeColor(), new PatternEdgeColor()));
		result.setPatternEdgeStyle((PatternEdgeStyle) StylePatternFactory
		    .processPatternProperties(p1.getPatternEdgeStyle(), p2
		        .getPatternEdgeStyle(), new PatternEdgeStyle()));
		result.setPatternEdgeWidth((PatternEdgeWidth) StylePatternFactory
		    .processPatternProperties(p1.getPatternEdgeWidth(), p2
		        .getPatternEdgeWidth(), new PatternEdgeWidth()));
		result.setPatternIcon((PatternIcon) StylePatternFactory
		    .processPatternProperties(p1.getPatternIcon(), p2.getPatternIcon(),
		        new PatternIcon()));
		result
		    .setPatternNodeBackgroundColor((PatternNodeBackgroundColor) StylePatternFactory
		        .processPatternProperties(p1.getPatternNodeBackgroundColor(),
		            p2.getPatternNodeBackgroundColor(),
		            new PatternNodeBackgroundColor()));
		result.setPatternNodeColor((PatternNodeColor) StylePatternFactory
		    .processPatternProperties(p1.getPatternNodeColor(), p2
		        .getPatternNodeColor(), new PatternNodeColor()));
		result.setPatternNodeFontBold((PatternNodeFontBold) StylePatternFactory
		    .processPatternProperties(p1.getPatternNodeFontBold(), p2
		        .getPatternNodeFontBold(), new PatternNodeFontBold()));
		result
		    .setPatternNodeFontItalic((PatternNodeFontItalic) StylePatternFactory
		        .processPatternProperties(p1.getPatternNodeFontItalic(), p2
		            .getPatternNodeFontItalic(), new PatternNodeFontItalic()));
		result.setPatternNodeFontName((PatternNodeFontName) StylePatternFactory
		    .processPatternProperties(p1.getPatternNodeFontName(), p2
		        .getPatternNodeFontName(), new PatternNodeFontName()));
		result.setPatternNodeFontSize((PatternNodeFontSize) StylePatternFactory
		    .processPatternProperties(p1.getPatternNodeFontSize(), p2
		        .getPatternNodeFontSize(), new PatternNodeFontSize()));
		result.setPatternNodeStyle((PatternNodeStyle) StylePatternFactory
		    .processPatternProperties(p1.getPatternNodeStyle(), p2
		        .getPatternNodeStyle(), new PatternNodeStyle()));
		return result;
	}

	public static List loadPatterns(final File file) throws Exception {
		return StylePatternFactory.loadPatterns(new BufferedReader(
		    new FileReader(file)));
	}

	/**
	 * @return a List of Pattern elements.
	 * @throws Exception
	 */
	public static List loadPatterns(final Reader reader) throws Exception {
		final Patterns patterns = (Patterns) XmlBindingTools.getInstance()
		    .unMarshall(reader);
		return patterns.getListChoiceList();
	}

	private static PatternPropertyBase processPatternProperties(
	                                                            final PatternPropertyBase prop1,
	                                                            final PatternPropertyBase prop2,
	                                                            final PatternPropertyBase destination) {
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
	public static void savePatterns(final Writer writer,
	                                final List listOfPatterns) throws Exception {
		final Patterns patterns = new Patterns();
		for (final Iterator iter = listOfPatterns.iterator(); iter.hasNext();) {
			final Pattern pattern = (Pattern) iter.next();
			patterns.addChoice(pattern);
		}
		final String marshalledResult = XmlBindingTools.getInstance().marshall(
		    patterns);
		writer.write(marshalledResult);
		writer.close();
	}

	public static String toString(final Pattern pPattern,
	                              final ITextTranslator translator) {
		String result = "";
		if (pPattern.getPatternNodeColor() != null) {
			result = StylePatternFactory.addSeparatorIfNecessary(result);
			if (pPattern.getPatternNodeColor().getValue() == null) {
				result += "-"
				        + Freeplane.getController().getResourceController()
				            .getResourceString("PatternToString.color");
			}
			else {
				result += "+"
				        + Freeplane.getController().getResourceController()
				            .getResourceString("PatternToString.color");
			}
		}
		if (pPattern.getPatternNodeBackgroundColor() != null) {
			result = StylePatternFactory.addSeparatorIfNecessary(result);
			if (pPattern.getPatternNodeBackgroundColor().getValue() == null) {
				result += "-"
				        + Freeplane.getController().getResourceController()
				            .getResourceString(
				                "PatternToString.backgroundColor");
			}
			else {
				result += "+"
				        + Freeplane.getController().getResourceController()
				            .getResourceString(
				                "PatternToString.backgroundColor");
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
		result = StylePatternFactory.addSubPatternToString(result, pPattern
		    .getPatternEdgeStyle(), "PatternToString.EdgeStyle");
		result = StylePatternFactory.addSubPatternToString(result, pPattern
		    .getPatternEdgeColor(), "PatternToString.EdgeColor");
		result = StylePatternFactory.addSubPatternToString(result, pPattern
		    .getPatternEdgeWidth(), "PatternToString.EdgeWidth");
		result = StylePatternFactory.addSubPatternToString(result, pPattern
		    .getPatternIcon(), "PatternToString.Icon");
		result = StylePatternFactory.addSubPatternToString(result, pPattern
		    .getPatternChild(), "PatternToString.Child");
		return result;
	}
}
