package org.freeplane.features.mindmapnode.pattern;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.freeplane.core.util.LogTool;
import org.freeplane.n3.nanoxml.IXMLParser;
import org.freeplane.n3.nanoxml.IXMLReader;
import org.freeplane.n3.nanoxml.StdXMLReader;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.n3.nanoxml.XMLException;
import org.freeplane.n3.nanoxml.XMLParserFactory;
import org.freeplane.n3.nanoxml.XMLWriter;

public class Pattern implements Cloneable {
	public static Pattern unMarshall(final String patternString) {
		try {
			final IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
			final IXMLReader xmlReader = new StdXMLReader(new StringReader(patternString));
			parser.setReader(xmlReader);
			final XMLElement xml = (XMLElement) parser.parse();
			return Pattern.unMarshall(xml);
		}
		catch (final XMLException e) {
			LogTool.severe(e);
			return null;
		}
	}

	public static Pattern unMarshall(final XMLElement xmlPattern) {
		final Pattern pattern = new Pattern();
		pattern.unMarshallImpl(xmlPattern);
		return pattern;
	}

	private String name;
	private PatternProperty patternChild;
	private PatternProperty patternCloud;
	private PatternProperty patternCloudColor;
	private PatternProperty patternEdgeColor;
	private PatternProperty patternEdgeStyle;
	private PatternProperty patternEdgeWidth;
	private PatternProperty patternIcon;
	private PatternProperty patternNodeBackgroundColor;
	private PatternProperty patternNodeColor;
	private PatternProperty patternNodeFontBold;
	private PatternProperty patternNodeFontItalic;
	private PatternProperty patternNodeFontName;
	private PatternProperty patternNodeFontSize;
	private PatternProperty patternNodeStyle;
	private PatternProperty patternNodeText;
	private PatternProperty patternScript;

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public String getName() {
		return name;
	}

	public PatternProperty getPatternChild() {
		return patternChild;
	}

	public PatternProperty getPatternCloud() {
		return patternCloud;
	}

	public PatternProperty getPatternCloudColor() {
		return patternCloudColor;
	}

	public PatternProperty getPatternEdgeColor() {
		return patternEdgeColor;
	}

	public PatternProperty getPatternEdgeStyle() {
		return patternEdgeStyle;
	}

	public PatternProperty getPatternEdgeWidth() {
		return patternEdgeWidth;
	}

	public PatternProperty getPatternIcon() {
		return patternIcon;
	}

	public PatternProperty getPatternNodeBackgroundColor() {
		return patternNodeBackgroundColor;
	}

	public PatternProperty getPatternNodeColor() {
		return patternNodeColor;
	}

	public PatternProperty getPatternNodeFontBold() {
		return patternNodeFontBold;
	}

	public PatternProperty getPatternNodeFontItalic() {
		return patternNodeFontItalic;
	}

	public PatternProperty getPatternNodeFontName() {
		return patternNodeFontName;
	}

	public PatternProperty getPatternNodeFontSize() {
		return patternNodeFontSize;
	}

	public PatternProperty getPatternNodeStyle() {
		return patternNodeStyle;
	}

	public PatternProperty getPatternNodeText() {
		return patternNodeText;
	}

	public PatternProperty getPatternScript() {
		return patternScript;
	}

	private void marschall(final XMLElement xml, final String string, final PatternProperty pattern) {
		if (pattern == null) {
			return;
		}
		final XMLElement property = new XMLElement(string);
		final String value = pattern.getValue();
		if (value != null) {
			property.setAttribute("value", value);
		}
		xml.addChild(property);
	}

	public String marshall() {
		final XMLElement xml = new XMLElement("pattern");
		xml.setAttribute("name", name);
		marschall(xml, "pattern_node_background_color", patternNodeBackgroundColor);
		marschall(xml, "pattern_node_color", patternNodeColor);
		marschall(xml, "pattern_node_style", patternNodeStyle);
		marschall(xml, "pattern_node_text", patternNodeText);
		marschall(xml, "pattern_node_font_name", patternNodeFontName);
		marschall(xml, "pattern_node_font_bold", patternNodeFontBold);
		marschall(xml, "pattern_node_font_italic", patternNodeFontItalic);
		marschall(xml, "pattern_node_font_size", patternNodeFontSize);
		marschall(xml, "pattern_icon", patternIcon);
		marschall(xml, "pattern_cloud", patternCloud);
		marschall(xml, "pattern_cloud_color", patternCloudColor);
		marschall(xml, "pattern_edge_color", patternEdgeColor);
		marschall(xml, "pattern_edge_style", patternEdgeStyle);
		marschall(xml, "pattern_edge_width", patternEdgeWidth);
		marschall(xml, "pattern_child", patternChild);
		marschall(xml, "pattern_script", patternScript);
		final StringWriter string = new StringWriter();
		final XMLWriter writer = new XMLWriter(string);
		try {
			writer.write(xml, true);
			return string.toString();
		}
		catch (final IOException e) {
			LogTool.severe(e);
			return null;
		}
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setPatternChild(final PatternProperty patternChild) {
		this.patternChild = patternChild;
	}

	public void setPatternCloud(final PatternProperty patternCloud) {
		this.patternCloud = patternCloud;
	}

	public void setPatternCloudColor(final PatternProperty patternEdgeColor) {
		patternCloudColor = patternEdgeColor;
	}

	public void setPatternEdgeColor(final PatternProperty patternEdgeColor) {
		this.patternEdgeColor = patternEdgeColor;
	}

	public void setPatternEdgeStyle(final PatternProperty patternEdgeStyle) {
		this.patternEdgeStyle = patternEdgeStyle;
	}

	public void setPatternEdgeWidth(final PatternProperty patternEdgeWidth) {
		this.patternEdgeWidth = patternEdgeWidth;
	}

	public void setPatternIcon(final PatternProperty patternIcon) {
		this.patternIcon = patternIcon;
	}

	public void setPatternNodeBackgroundColor(final PatternProperty patternNodeBackgroundColor) {
		this.patternNodeBackgroundColor = patternNodeBackgroundColor;
	}

	public void setPatternNodeColor(final PatternProperty patternNodeColor) {
		this.patternNodeColor = patternNodeColor;
	}

	public void setPatternNodeFontBold(final PatternProperty patternNodeFontBold) {
		this.patternNodeFontBold = patternNodeFontBold;
	}

	public void setPatternNodeFontItalic(final PatternProperty patternNodeFontItalic) {
		this.patternNodeFontItalic = patternNodeFontItalic;
	}

	public void setPatternNodeFontName(final PatternProperty patternNodeFontName) {
		this.patternNodeFontName = patternNodeFontName;
	}

	public void setPatternNodeFontSize(final PatternProperty patternNodeFontSize) {
		this.patternNodeFontSize = patternNodeFontSize;
	}

	public void setPatternNodeStyle(final PatternProperty patternNodeStyle) {
		this.patternNodeStyle = patternNodeStyle;
	}

	public void setPatternNodeText(final PatternProperty patternNodeText) {
		this.patternNodeText = patternNodeText;
	}

	public void setPatternScript(final PatternProperty patternScript) {
		this.patternScript = patternScript;
	}

	private void unMarshallImpl(final XMLElement xmlPattern) {
		name = xmlPattern.getAttribute("name", null);
		{
			final XMLElement xmlProperty = xmlPattern.getFirstChildNamed("pattern_node_background_color");
			if (xmlProperty != null) {
				patternNodeBackgroundColor = new PatternProperty();
				patternNodeBackgroundColor.value = xmlProperty.getAttribute("value", null);
			}
		}
		{
			final XMLElement xmlProperty = xmlPattern.getFirstChildNamed("pattern_node_color");
			if (xmlProperty != null) {
				patternNodeColor = new PatternProperty();
				patternNodeColor.value = xmlProperty.getAttribute("value", null);
			}
		}
		{
			final XMLElement xmlProperty = xmlPattern.getFirstChildNamed("pattern_node_style");
			if (xmlProperty != null) {
				patternNodeStyle = new PatternProperty();
				patternNodeStyle.value = xmlProperty.getAttribute("value", null);
			}
		}
		{
			final XMLElement xmlProperty = xmlPattern.getFirstChildNamed("pattern_node_text");
			if (xmlProperty != null) {
				patternNodeText = new PatternProperty();
				patternNodeText.value = xmlProperty.getAttribute("value", null);
			}
		}
		{
			final XMLElement xmlProperty = xmlPattern.getFirstChildNamed("pattern_node_font_name");
			if (xmlProperty != null) {
				patternNodeFontName = new PatternProperty();
				patternNodeFontName.value = xmlProperty.getAttribute("value", null);
			}
		}
		{
			final XMLElement xmlProperty = xmlPattern.getFirstChildNamed("pattern_node_font_bold");
			if (xmlProperty != null) {
				patternNodeFontBold = new PatternProperty();
				patternNodeFontBold.value = xmlProperty.getAttribute("value", null);
			}
		}
		{
			final XMLElement xmlProperty = xmlPattern.getFirstChildNamed("pattern_node_font_italic");
			if (xmlProperty != null) {
				patternNodeFontItalic = new PatternProperty();
				patternNodeFontItalic.value = xmlProperty.getAttribute("value", null);
			}
		}
		{
			final XMLElement xmlProperty = xmlPattern.getFirstChildNamed("pattern_node_font_size");
			if (xmlProperty != null) {
				patternNodeFontSize = new PatternProperty();
				patternNodeFontSize.value = xmlProperty.getAttribute("value", null);
			}
		}
		{
			final XMLElement xmlProperty = xmlPattern.getFirstChildNamed("pattern_icon");
			if (xmlProperty != null) {
				patternIcon = new PatternProperty();
				patternIcon.value = xmlProperty.getAttribute("value", null);
			}
		}
		{
			final XMLElement xmlProperty = xmlPattern.getFirstChildNamed("pattern_cloud");
			if (xmlProperty != null) {
				patternCloud = new PatternProperty();
				patternCloud.value = xmlProperty.getAttribute("value", null);
			}
		}
		{
			final XMLElement xmlProperty = xmlPattern.getFirstChildNamed("pattern_cloud_color");
			if (xmlProperty != null) {
				patternCloudColor = new PatternProperty();
				patternCloudColor.value = xmlProperty.getAttribute("value", null);
			}
		}
		{
			final XMLElement xmlProperty = xmlPattern.getFirstChildNamed("pattern_edge_color");
			if (xmlProperty != null) {
				patternEdgeColor = new PatternProperty();
				patternEdgeColor.value = xmlProperty.getAttribute("value", null);
			}
		}
		{
			final XMLElement xmlProperty = xmlPattern.getFirstChildNamed("pattern_edge_style");
			if (xmlProperty != null) {
				patternEdgeStyle = new PatternProperty();
				patternEdgeStyle.value = xmlProperty.getAttribute("value", null);
			}
		}
		{
			final XMLElement xmlProperty = xmlPattern.getFirstChildNamed("pattern_edge_width");
			if (xmlProperty != null) {
				patternEdgeWidth = new PatternProperty();
				patternEdgeWidth.value = xmlProperty.getAttribute("value", null);
			}
		}
		{
			final XMLElement xmlProperty = xmlPattern.getFirstChildNamed("pattern_child");
			if (xmlProperty != null) {
				patternChild = new PatternProperty();
				patternChild.value = xmlProperty.getAttribute("value", null);
			}
		}
		{
			final XMLElement xmlProperty = xmlPattern.getFirstChildNamed("pattern_script");
			if (xmlProperty != null) {
				patternScript = new PatternProperty();
				patternScript.value = xmlProperty.getAttribute("value", null);
			}
		}
	}
}
