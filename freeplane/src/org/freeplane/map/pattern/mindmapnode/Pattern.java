package org.freeplane.map.pattern.mindmapnode;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.freeplane.io.xml.n3.nanoxml.IXMLElement;
import org.freeplane.io.xml.n3.nanoxml.IXMLParser;
import org.freeplane.io.xml.n3.nanoxml.IXMLReader;
import org.freeplane.io.xml.n3.nanoxml.StdXMLParser;
import org.freeplane.io.xml.n3.nanoxml.StdXMLReader;
import org.freeplane.io.xml.n3.nanoxml.XMLElement;
import org.freeplane.io.xml.n3.nanoxml.XMLException;
import org.freeplane.io.xml.n3.nanoxml.XMLParserFactory;
import org.freeplane.io.xml.n3.nanoxml.XMLWriter;

public class Pattern implements Cloneable {
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	private PatternProperty patternNodeBackgroundColor;
	private PatternProperty patternNodeColor;
	private PatternProperty patternNodeStyle;
	private PatternProperty patternNodeText;
	private PatternProperty patternNodeFontName;
	private PatternProperty patternNodeFontBold;
	private PatternProperty patternNodeFontItalic;
	private PatternProperty patternNodeFontSize;
	private PatternProperty patternIcon;
	private PatternProperty patternEdgeColor;
	private PatternProperty patternEdgeStyle;
	private PatternProperty patternEdgeWidth;
	private PatternProperty patternChild;
	private PatternProperty patternScript;
	private String name;

	public PatternProperty getPatternNodeBackgroundColor() {
		return this.patternNodeBackgroundColor;
	}

	public void setPatternNodeBackgroundColor(PatternProperty patternNodeBackgroundColor) {
		this.patternNodeBackgroundColor = patternNodeBackgroundColor;
	}

	public PatternProperty getPatternNodeColor() {
		return this.patternNodeColor;
	}

	public void setPatternNodeColor(PatternProperty patternNodeColor) {
		this.patternNodeColor = patternNodeColor;
	}

	public PatternProperty getPatternNodeStyle() {
		return this.patternNodeStyle;
	}

	public void setPatternNodeStyle(PatternProperty patternNodeStyle) {
		this.patternNodeStyle = patternNodeStyle;
	}

	public PatternProperty getPatternNodeText() {
		return this.patternNodeText;
	}

	public void setPatternNodeText(PatternProperty patternNodeText) {
		this.patternNodeText = patternNodeText;
	}

	public PatternProperty getPatternNodeFontName() {
		return this.patternNodeFontName;
	}

	public void setPatternNodeFontName(PatternProperty patternNodeFontName) {
		this.patternNodeFontName = patternNodeFontName;
	}

	public PatternProperty getPatternNodeFontBold() {
		return this.patternNodeFontBold;
	}

	public void setPatternNodeFontBold(PatternProperty patternNodeFontBold) {
		this.patternNodeFontBold = patternNodeFontBold;
	}

	public PatternProperty getPatternNodeFontItalic() {
		return this.patternNodeFontItalic;
	}

	public void setPatternNodeFontItalic(PatternProperty patternNodeFontItalic) {
		this.patternNodeFontItalic = patternNodeFontItalic;
	}

	public PatternProperty getPatternNodeFontSize() {
		return this.patternNodeFontSize;
	}

	public void setPatternNodeFontSize(PatternProperty patternNodeFontSize) {
		this.patternNodeFontSize = patternNodeFontSize;
	}

	public PatternProperty getPatternIcon() {
		return this.patternIcon;
	}

	public void setPatternIcon(PatternProperty patternIcon) {
		this.patternIcon = patternIcon;
	}

	public PatternProperty getPatternEdgeColor() {
		return this.patternEdgeColor;
	}

	public void setPatternEdgeColor(PatternProperty patternEdgeColor) {
		this.patternEdgeColor = patternEdgeColor;
	}

	public PatternProperty getPatternEdgeStyle() {
		return this.patternEdgeStyle;
	}

	public void setPatternEdgeStyle(PatternProperty patternEdgeStyle) {
		this.patternEdgeStyle = patternEdgeStyle;
	}

	public PatternProperty getPatternEdgeWidth() {
		return this.patternEdgeWidth;
	}

	public void setPatternEdgeWidth(PatternProperty patternEdgeWidth) {
		this.patternEdgeWidth = patternEdgeWidth;
	}

	public PatternProperty getPatternChild() {
		return this.patternChild;
	}

	public void setPatternChild(PatternProperty patternChild) {
		this.patternChild = patternChild;
	}

	public PatternProperty getPatternScript() {
		return this.patternScript;
	}

	public void setPatternScript(PatternProperty patternScript) {
		this.patternScript = patternScript;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String marshall() {
		IXMLElement xml= new XMLElement("pattern");
		xml.setAttribute("name", name);
		marschall(xml, "pattern_node_background_color", patternNodeBackgroundColor);
		marschall(xml, "pattern_node_color", patternNodeColor);
		marschall(xml, "pattern_node_style", patternNodeStyle);
		marschall(xml, "pattern_node_text", patternNodeText);
		marschall(xml, "pattern_node_font_name", patternNodeFontName);
		marschall(xml, "pattern_node_font_bold", patternNodeFontBold);
		marschall(xml, "pattern_node_font_italic", patternNodeFontItalic);
		marschall(xml, "pattern_node_font_size", patternNodeFontSize);
		marschall(xml, "pattern_node_icon", patternIcon);
		marschall(xml, "pattern_node_edge_color", patternEdgeColor);
		marschall(xml, "pattern_node_edge_style", patternEdgeStyle);
		marschall(xml, "pattern_node_wdge_width", patternEdgeWidth);
		marschall(xml, "pattern_node_child", patternChild);
		marschall(xml, "pattern_node_script", patternScript);
		final StringWriter string = new StringWriter();
		XMLWriter writer = new XMLWriter(string); 
		try {
	        writer.write(xml);
	        return string.toString();
        }
        catch (IOException e) {
	        e.printStackTrace();
	        return null;
        }
	}

	private void marschall(IXMLElement xml, String string,
                           PatternProperty patternNodeBackgroundColor2) {
	    // TODO Auto-generated method stub
	    
    }

	public static Pattern unMarshall(String patternString) {
		try {
			IXMLParser parser =  XMLParserFactory.createDefaultXMLParser();
			IXMLReader xmlReader = new StdXMLReader(new StringReader(patternString));
			parser.setReader(xmlReader);
			IXMLElement xml = (IXMLElement) parser.parse();
			return unMarshall(xml);
		}
		catch (XMLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Pattern unMarshall(IXMLElement xmlPattern) {
		Pattern pattern = new Pattern();
		pattern.unMarshallImpl(xmlPattern);
		return pattern;
	}

	private void unMarshallImpl(IXMLElement xmlPattern) {
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
