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
package org.freeplane.features.map.clipboard;

import static org.freeplane.features.nodestyle.FontUtils.isStrikedThrough;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.Collection;
import java.util.List;

import javax.swing.Icon;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.MindIcon;
import org.freeplane.features.icon.NamedIcon;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.note.NoteModel;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.text.DetailModel;
import org.freeplane.features.text.TextController;
import org.freeplane.features.url.UrlManager;

class MindMapHTMLWriter {
	private static String lf = System.getProperty("line.separator");

	private static String convertSpecialChar(final char c) {
		String cvt;
		switch (c) {
			case 0xe4:
				cvt = "&auml;";
				break;
			case 0xf6:
				cvt = "&ouml;";
				break;
			case 0xfc:
				cvt = "&uuml;";
				break;
			case 0xc4:
				cvt = "&Auml;";
				break;
			case 0xd6:
				cvt = "&Ouml;";
				break;
			case 0xdc:
				cvt = "&Uuml;";
				break;
			case 0xdf:
				cvt = "&szlig;";
				break;
			default:
				cvt = "&#" + Integer.toString(c) + ";";
				break;
		}
		return cvt;
	}

	private static String writeHTML_escapeUnicodeAndSpecialCharacters(final String text) {
		final int len = text.length();
		final StringBuilder result = new StringBuilder(len);
		int intValue;
		char myChar;
		boolean previousSpace = false;
		boolean spaceOccured = false;
		for (int i = 0; i < len; ++i) {
			myChar = text.charAt(i);
			intValue = text.charAt(i);
			if (intValue >= 128) {
				result.append(MindMapHTMLWriter.convertSpecialChar(myChar));
			}
			else {
				spaceOccured = false;
				switch (myChar) {
					case '&':
						result.append("&amp;");
						break;
					case '<':
						result.append("&lt;");
						break;
					case '>':
						result.append("&gt;");
						break;
					case ' ':
						spaceOccured = true;
						if (previousSpace) {
							result.append("&nbsp;");
						}
						else {
							result.append(" ");
						}
						break;
					case '\n':
						result.append("\n<br>\n");
						break;
					default:
						result.append(myChar);
				}
				previousSpace = spaceOccured;
			}
		}
		return result.toString();
	}

	final private boolean basedOnHeadings;
	final private Writer fileout;
	final private MapController mapController;
	private boolean writeFoldingCode;
	private final NodeStyleController nodeStyleController;
	private Font defaultFont;
	private Color defaultColor;

	MindMapHTMLWriter(final MapController mapController, final Writer fileout) {
		this.mapController = mapController;
		nodeStyleController = NodeStyleController.getController();
		this.fileout = fileout;
		writeFoldingCode = false;
		basedOnHeadings = (getProperty("html_export_folding").equals("html_export_based_on_headings"));
	}

	private String fontStyle(Color color, Font font) throws IOException {
		StringBuilder fontStyle = new StringBuilder();
		if (color != null && (defaultColor == null || !color.equals(defaultColor)))
			fontStyle.append("color: ").append(ColorUtils.colorToString(color)).append("; ");
		if (font != null) {
			final int fontSize = font.getSize();
			if (defaultFont == null || fontSize != defaultFont.getSize())
				fontStyle.append("font-size: ").append(fontSize).append("pt; ");
			final String fontFamily = font.getFamily();
			if (defaultFont == null || !fontFamily.equals(defaultFont.getFamily()))
				fontStyle.append("font-family: \"").append(fontFamily).append("\", sans-serif; ");
			if ((defaultFont == null || !defaultFont.isItalic()) && font.isItalic()) {
				fontStyle.append("font-style: italic; ");
			}
			if ((defaultFont == null || !defaultFont.isBold()) && font.isBold()) {
				fontStyle.append("font-weight: bold; ");
			}
			if ((defaultFont == null || !isStrikedThrough(defaultFont)) && isStrikedThrough(font)) {
				fontStyle.append("text-decoration: line-through; ");
			}
		}
		return fontStyle.toString();
	}

	private String getProperty(final String key) {
		return ResourceController.getResourceController().getProperty(key);
	}

	private void writeBodyWithFolding(final NodeModel rootNodeOfBranch) throws IOException {
		writeJavaScript();
		fileout.write("<SPAN class=\"foldspecial\" onclick=\"unfold_document()\">All +</SPAN>" + lf);
		fileout.write("<SPAN class=\"foldspecial\" onclick=\"fold_document()\">All -</SPAN>" + lf);
		writeHTML(rootNodeOfBranch, "1", 0, /* isRoot */true, true, /* depth */
		    1);
		fileout.write("<SCRIPT type=\"text/javascript\">" + lf);
		fileout.write("fold_document();" + lf);
		fileout.write("</SCRIPT>" + lf);
	}

	private void writeFoldingButtons(final String localParentID) throws IOException {
		fileout.write("<span id=\"show" + localParentID + "\" class=\"foldclosed\" onClick=\"show_folder('"
		        + localParentID + "')\" style=\"POSITION: absolute\">+</span> " + "<span id=\"hide" + localParentID
		        + "\" class=\"foldopened\" onClick=\"hide_folder('" + localParentID + "')\">-</span>");
		fileout.write("\n");
	}

	void writeHTML(final Collection<NodeModel> selectedNodes) throws IOException {
		fileout.write("<html>" + lf + "<head>" + lf);
		if (!selectedNodes.isEmpty()) {
			final MapModel map = selectedNodes.iterator().next().getMap();
			setDefaultsFrom(map);
			writeStyle();
		}
		fileout.write(lf + "</head>" + lf + "<body>" + lf);
		for (NodeModel node : selectedNodes) {
			writeHTML(node, "1", 0, /* isRoot */true, true, /* depth */1);
		}
		fileout.write("</body>" + lf);
		fileout.write("</html>" + lf);
		fileout.close();
		resetDefaults();
	}

	private void resetDefaults() {
		defaultFont = null;
		defaultColor = null;
	}

	private void setDefaultsFrom(MapModel map) {
		final MapStyleModel model = MapStyleModel.getExtension(map);
		final NodeModel styleNode = model.getDefaultStyleNode();
		defaultFont = nodeStyleController.getFont(styleNode);
		defaultColor = nodeStyleController.getColor(styleNode);
	}

	void writeHTML(final List<NodeModel> branchRootNodes) throws IOException {
		if(branchRootNodes.isEmpty())
			return;
		NodeModel firstNode = branchRootNodes.get(0);
		MapModel map = firstNode.getMap();
		setDefaultsFrom(map);
		final String htmlExportFoldingOption = getProperty("html_export_folding");
		writeFoldingCode = htmlExportFoldingOption.equals("html_export_fold_all")
				|| (htmlExportFoldingOption.equals("html_export_fold_currently_folded")
				&& hasFoldedStrictDescendant(branchRootNodes));
		ResourceController.getResourceController().getBooleanProperty("export_icons_in_html");
		fileout
		    .write(
		        "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">"
		                + lf + "<html>" + lf + "<head>" + lf);
		fileout.write("<title>"
		        + MindMapHTMLWriter.writeHTML_escapeUnicodeAndSpecialCharacters(
		            TextController.getController().getPlainTransformedTextWithoutNodeNumber(branchRootNodes.size() == 1 ?  firstNode : map.getRootNode())
		                .replace('\n', ' '))
		        + "</title>" + lf);
		writeStyle();
		fileout.write(lf + "</head>" + lf + "<body");
		final MapStyleModel style = MapStyleModel.getExtension(map);
		final Color background = style != null ? style.getBackgroundColor() : null;
		if (background != null) {
			fileout.write(" bgcolor=" + ColorUtils.colorToString(background));
		}
		fileout.write(">" + lf);
		for(NodeModel node : branchRootNodes) {
			if (writeFoldingCode) {
				writeBodyWithFolding(node);
			} else {
				writeHTML(node, "1", 0, /* isRoot */true, true, /* depth */ 1);
			}
		}
		fileout.write("</body>" + lf);
		fileout.write("</html>" + lf);
		fileout.close();
		resetDefaults();
	}

	private boolean hasFoldedStrictDescendant(List<NodeModel> branchRootNodes) {
		for(NodeModel node : branchRootNodes) {
			if (mapController.hasFoldedStrictDescendant(node))
				return true;
		}
		return  false;
	}

	private int writeHTML(final NodeModel node, final String parentID, int lastChildNumber, final boolean isRoot,
	                      final boolean treatAsParagraph, final int depth)
	        throws IOException {
		if (!node.hasVisibleContent(FilterController.getFilter(node.getMap()))) {
			for (final NodeModel child : node.getChildren()) {
				lastChildNumber = writeHTML(child, parentID, lastChildNumber, false, false, depth);
			}
			return lastChildNumber;
		}
		boolean createFolding = false;
		if (writeFoldingCode) {
			createFolding = mapController.isFolded(node);
			if (getProperty("html_export_folding").equals("html_export_fold_all")) {
				createFolding = node.hasChildren();
			}
			if (getProperty("html_export_folding").equals("html_export_no_folding") || basedOnHeadings || isRoot) {
				createFolding = false;
			}
		}
		final TextController textController = TextController.getController();
		final Object userObject = node.getUserObject();
		Object transformed = textController.getTransformedObjectNoFormattingNoThrow(node, node, userObject);
		boolean containsIcon = transformed instanceof Icon;
		final String text =  containsIcon ? HtmlUtils.iconToHtml((Icon) transformed) :  transformed.toString();
		final boolean containsHtml = ! containsIcon && text.startsWith("<html>");
		final boolean heading = basedOnHeadings && !containsHtml && node.hasChildren() && depth <= 6;
		if (!treatAsParagraph && !basedOnHeadings) {
			fileout.write("<li>");
		}
		else {
			if (heading) {
				fileout.write(lf + "<h" + depth + ">");
			}
			else if (!containsHtml) {
				fileout.write("<p>");
			}
		}
		String localParentID = parentID;
		if (createFolding) {
			lastChildNumber++;
			localParentID = parentID + "_" + lastChildNumber;
			writeFoldingButtons(localParentID);
		}
		final String fontStyle = fontStyle(nodeStyleController.getColor(node), nodeStyleController.getFont(node));
		boolean shouldOutputFontStyle = !fontStyle.equals("");
		if (shouldOutputFontStyle) {
			fileout.write("<span style=\"" + fontStyle + "\">");
		}
		String link = NodeLinks.getLinkAsString(node);
		if (link != null && ! text.contains(link)) {
			if (link.endsWith(UrlManager.FREEPLANE_FILE_EXTENSION)) {
				link += ".html";
			}
			fileout.write("<a href=\"" + link + "\" target=\"_blank\">");
		}
		if (ResourceController.getResourceController().getBooleanProperty("export_icons_in_html")) {
			writeIcons(node);
		}
		if(containsIcon)
			fileout.write(text);
		else
			writeModelContent(text);
		if (link != null) {
			fileout.write("</a>" + lf);
		}
		if (shouldOutputFontStyle) {
			fileout.write("</span>");
		}
		final String detailText = DetailModel.getDetailText(node);
		if (detailText != null) {
			writeModelContent(node, DetailModel.getDetail(node), detailText);
		}
		final String noteContent = NoteModel.getNoteText(node);
		if (noteContent != null) {
			writeModelContent(node, NoteModel.getNote(node), noteContent);
		}
		if (heading) {
			fileout.write("</h" + depth + ">" + lf);
		}
		if (getProperty("html_export_folding").equals("html_export_based_on_headings")) {
			for (final NodeModel child : node.getChildren()) {
				lastChildNumber = writeHTML(child, parentID, lastChildNumber, /*isRoot=*/false,
				    false, depth + 1);
			}
			return lastChildNumber;
		}
		if (node.hasChildren()) {
			if (getProperty("html_export_folding").equals("html_export_based_on_headings")) {
				for (final NodeModel child : node.getChildren()) {
					lastChildNumber = writeHTML(child, parentID, lastChildNumber,
					    /*isRoot=*/false, false, depth + 1);
				}
			}
			else if (createFolding) {
				fileout.write(lf + "<ul id=\"fold" + localParentID
				        + "\" style=\"POSITION: relative; VISIBILITY: visible;\">" + lf);
				int localLastChildNumber = 0;
				for (final NodeModel child : node.getChildren()) {
					localLastChildNumber = writeHTML(child, localParentID, localLastChildNumber,
					    /* isRoot=*/false, false, depth + 1);
				}
			}
			else {
				fileout.write(lf + "<ul>" + lf);
				for (final NodeModel child : node.getChildren()) {
					lastChildNumber = writeHTML(child, parentID, lastChildNumber,
					    /* isRoot= */false, false, depth + 1);
				}
			}
			fileout.write("</ul>" + lf);
		}
		if (!treatAsParagraph) {
			fileout.write("</li>" + lf);
		}
		return lastChildNumber;
	}

	private void writeModelContent(NodeModel node, Object nodeProperty, String data)  throws IOException{
		final Object transformed = TextController.getController().getTransformedObjectNoFormattingNoThrow(node, nodeProperty, data);
		String text = HtmlUtils.objectToHtml(transformed);
		writeModelContent(text);
	}

	private void writeIcons(final NodeModel model) throws IOException {
		final IconController iconController = IconController.getController();
		final Collection<NamedIcon> icons = iconController.getIcons(model);
		for (NamedIcon icon : icons) {
			if(icon instanceof MindIcon) {
				MindIcon mindIcon = (MindIcon) icon;
				try {
                    final String iconFile =  new URI(null, mindIcon.getFile(), null).toString();
                    fileout.write("<img src=\"icons/" + iconFile + "\" alt=\"" + mindIcon.getTranslatedDescription() + "\"");
                    final Quantity<LengthUnit> iconSize = iconController.getIconSize(model);
                    fileout.write(" height = \"" + iconSize.toBaseUnitsRounded() + "\"");
                    fileout.write(">");
                } catch (Exception e) {
                    LogUtils.severe(e);
                }
			}
		}
	}

	private void writeJavaScript() throws IOException {
		fileout.write("<script type=\"text/javascript\">" + lf);
		fileout.write(FileUtils.slurpResource("/html/folding.js"));
		fileout.write(lf + "</script>" + lf);
	}

	private void writeModelContent(final String string) throws IOException {
		if (string.matches(" +")) {
			fileout.write("&nbsp;");
		}
		else if (string.startsWith("<html")) {
			String output = string.substring(6);
			int start = output.indexOf("<body");
			if (start == -1) {
				start = output.indexOf('>') + 1;
			}
			else {
				start = output.indexOf('>', start + 5) + 1;
			}
			int end = output.indexOf("</body>");
			if (end == -1) {
				end = output.indexOf("</html>");
			}
			if (end == -1) {
				end = output.length();
			}
			output = output.substring(start, end);
			fileout.write(output);
		}
		else {
			fileout.write(HtmlUtils.unicodeToHTMLUnicodeEntity(string));
		}
	}

	private void writeStyle() throws IOException {
		fileout.write("<style type=\"text/css\">" + lf);
		fileout.write("    body {");
		writeDefaultFontStyle();
		fileout.write("}" + lf);
		fileout.write(FileUtils.slurpResource("/html/freeplane.css"));
		if (writeFoldingCode)
			fileout.write(FileUtils.slurpResource("/html/folding.css"));
		fileout.write(lf + "</style>");
	}

	private void writeDefaultFontStyle() throws IOException {
		Font font = defaultFont;
		defaultFont = null;
		Color color = defaultColor;
		defaultColor = null;
		fileout.write(fontStyle(color, font));
		defaultFont = font;
		defaultColor = color;
	}
}
