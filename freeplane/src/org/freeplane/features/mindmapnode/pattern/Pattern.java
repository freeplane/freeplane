package org.freeplane.features.mindmapnode.pattern;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.freeplane.n3.nanoxml.IXMLElement;
import org.freeplane.n3.nanoxml.IXMLParser;
import org.freeplane.n3.nanoxml.IXMLReader;
import org.freeplane.n3.nanoxml.StdXMLReader;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.n3.nanoxml.XMLException;
import org.freeplane.n3.nanoxml.XMLParserFactory;
import org.freeplane.n3.nanoxml.XMLWriter;

// TODO ARCH rladstaetter 18.02.2009 
public class Pattern implements Cloneable {
	public static Pattern unMarshall(final IXMLElement xmlPattern) {
		final Pattern pattern = new Pattern();
		pattern.unMarshallImpl(xmlPattern);
		return pattern;
	}

	public static Pattern unMarshall(final String patternString) {
		try {
			final IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
			final IXMLReader xmlReader = new StdXMLReader(new StringReader(patternString));
			parser.setReader(xmlReader);
			final IXMLElement xml = (IXMLElement) parser.parse();
			return Pattern.unMarshall(xml);
		}
		catch (final XMLException e) {
			e.printStackTrace();
			return null;
		}
	}

	private String name;
	//// TODO rladstaetter 18.02.2009 make following attributes typesafe
	private PatternProperty patternChild;
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

	private void marshall(final IXMLElement xml, final String string, final PatternProperty pattern) {
		// FIXME rladstaetter method not implemented
		//		throw new UnsupportedOperationException();
	}

	public String marshall() {
		final IXMLElement xml = new XMLElement("pattern");
		xml.setAttribute("name", name);
		marshall(xml, "pattern_node_background_color", patternNodeBackgroundColor);
		marshall(xml, "pattern_node_color", patternNodeColor);
		marshall(xml, "pattern_node_style", patternNodeStyle);
		marshall(xml, "pattern_node_text", patternNodeText);
		marshall(xml, "pattern_node_font_name", patternNodeFontName);
		marshall(xml, "pattern_node_font_bold", patternNodeFontBold);
		marshall(xml, "pattern_node_font_italic", patternNodeFontItalic);
		marshall(xml, "pattern_node_font_size", patternNodeFontSize);
		marshall(xml, "pattern_node_icon", patternIcon);
		marshall(xml, "pattern_node_edge_color", patternEdgeColor);
		marshall(xml, "pattern_node_edge_style", patternEdgeStyle);
		marshall(xml, "pattern_node_wdge_width", patternEdgeWidth);
		marshall(xml, "pattern_node_child", patternChild);
		marshall(xml, "pattern_node_script", patternScript);
		final StringWriter string = new StringWriter();
		final XMLWriter writer = new XMLWriter(string);
		try {
			writer.write(xml);
			return string.toString();
		}
		catch (final IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setPatternChild(final PatternProperty patternChild) {
		this.patternChild = patternChild;
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

	private void unMarshallImpl(final IXMLElement xmlPattern) {
		name = xmlPattern.getAttribute("name", null);
		{
			final IXMLElement xmlProperty = xmlPattern.getFirstChildNamed("pattern_node_background_color");
			if (xmlProperty != null) {
				patternNodeBackgroundColor = new PatternProperty();
				patternNodeBackgroundColor.value = xmlProperty.getAttribute("value", null);
			}
		}
		{
			final IXMLElement xmlProperty = xmlPattern.getFirstChildNamed("pattern_node_color");
			if (xmlProperty != null) {
				patternNodeColor = new PatternProperty();
				patternNodeColor.value = xmlProperty.getAttribute("value", null);
			}
		}
		{
			final IXMLElement xmlProperty = xmlPattern.getFirstChildNamed("pattern_node_style");
			if (xmlProperty != null) {
				patternNodeStyle = new PatternProperty();
				patternNodeStyle.value = xmlProperty.getAttribute("value", null);
			}
		}
		{
			final IXMLElement xmlProperty = xmlPattern.getFirstChildNamed("pattern_node_textr");
			if (xmlProperty != null) {
				patternNodeText = new PatternProperty();
				patternNodeText.value = xmlProperty.getAttribute("value", null);
			}
		}
		{
			final IXMLElement xmlProperty = xmlPattern.getFirstChildNamed("pattern_node_font_name");
			if (xmlProperty != null) {
				patternNodeFontName = new PatternProperty();
				patternNodeFontName.value = xmlProperty.getAttribute("value", null);
			}
		}
		{
			final IXMLElement xmlProperty = xmlPattern.getFirstChildNamed("pattern_font_bold");
			if (xmlProperty != null) {
				patternNodeFontBold = new PatternProperty();
				patternNodeFontBold.value = xmlProperty.getAttribute("value", null);
			}
		}
		{
			final IXMLElement xmlProperty = xmlPattern.getFirstChildNamed("pattern_font_italic");
			if (xmlProperty != null) {
				patternNodeFontItalic = new PatternProperty();
				patternNodeFontItalic.value = xmlProperty.getAttribute("value", null);
			}
		}
		{
			final IXMLElement xmlProperty = xmlPattern.getFirstChildNamed("pattern_font_size");
			if (xmlProperty != null) {
				patternNodeFontSize = new PatternProperty();
				patternNodeFontSize.value = xmlProperty.getAttribute("value", null);
			}
		}
		{
			final IXMLElement xmlProperty = xmlPattern.getFirstChildNamed("pattern_icon");
			if (xmlProperty != null) {
				patternIcon = new PatternProperty();
				patternIcon.value = xmlProperty.getAttribute("value", null);
			}
		}
		{
			final IXMLElement xmlProperty = xmlPattern.getFirstChildNamed("pattern_edge_color");
			if (xmlProperty != null) {
				patternEdgeColor = new PatternProperty();
				patternEdgeColor.value = xmlProperty.getAttribute("value", null);
			}
		}
		{
			final IXMLElement xmlProperty = xmlPattern.getFirstChildNamed("pattern_edge_style");
			if (xmlProperty != null) {
				patternEdgeStyle = new PatternProperty();
				patternEdgeStyle.value = xmlProperty.getAttribute("value", null);
			}
		}
		{
			final IXMLElement xmlProperty = xmlPattern.getFirstChildNamed("pattern_edge_width");
			if (xmlProperty != null) {
				patternEdgeWidth = new PatternProperty();
				patternEdgeWidth.value = xmlProperty.getAttribute("value", null);
			}
		}
		{
			final IXMLElement xmlProperty = xmlPattern.getFirstChildNamed("pattern_child");
			if (xmlProperty != null) {
				patternChild = new PatternProperty();
				patternChild.value = xmlProperty.getAttribute("value", null);
			}
		}
		{
			final IXMLElement xmlProperty = xmlPattern.getFirstChildNamed("pattern_script");
			if (xmlProperty != null) {
				patternScript = new PatternProperty();
				patternScript.value = xmlProperty.getAttribute("value", null);
			}
		}
	}

}
