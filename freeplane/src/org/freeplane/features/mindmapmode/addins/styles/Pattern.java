package org.freeplane.features.mindmapmode.addins.styles;

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

class Pattern implements Cloneable {
	private String name;
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

	public void setName(final String name) {
		this.name = name;
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
}
