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
package org.freeplane.features.clipboard;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.MindIcon;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.note.NoteModel;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.text.DetailTextModel;
import org.freeplane.features.text.TextController;
import org.freeplane.features.url.UrlManager;

class MindMapHTMLWriter {
	private static String el = System.getProperty("line.separator");

	private static String convertSpecialChar(final char c) {
		String cvt;
		switch ((int) c) {
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
				cvt = "&#" + Integer.toString((int) c) + ";";
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
			intValue = (int) text.charAt(i);
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
		if(color != null && (defaultColor == null || ! color.equals(defaultColor)))
			fontStyle.append("color: ").append(ColorUtils.colorToString(color)).append( "; ");
		if(font != null){
			final int fontSize = font.getSize();
			if (defaultFont == null || fontSize != defaultFont.getSize())
				fontStyle.append("font-size: ").append(fontSize).append( "pt; ");
			final String fontFamily = font.getFamily();
			if (defaultFont == null || ! fontFamily.equals(defaultFont.getFamily()))
				fontStyle.append("font-family: ").append(fontFamily).append( ", sans-serif; ");
			if ((defaultFont == null || ! defaultFont.isItalic()) && font.isItalic()) {
				fontStyle.append("font-style: italic; ");
			}
			if ((defaultFont == null || ! defaultFont.isBold()) && font.isBold()) {
				fontStyle.append("font-weight: bold; ");
			}
		}
		return fontStyle.toString();
	}

	private String getProperty(final String key) {
		return ResourceController.getResourceController().getProperty(key);
	}

	private void writeBodyWithFolding(final NodeModel rootNodeOfBranch) throws IOException {
		writeJavaScript();
		fileout.write("<SPAN class=\"foldspecial\" onclick=\"fold_document()\">All +</SPAN>" + MindMapHTMLWriter.el);
		fileout.write("<SPAN class=\"foldspecial\" onclick=\"unfold_document()\">All -</SPAN>" + MindMapHTMLWriter.el);
		writeHTML(rootNodeOfBranch, "1", 0, /* isRoot */true, true, /* depth */
		1);
		fileout.write("<SCRIPT type=\"text/javascript\">" + MindMapHTMLWriter.el);
		fileout.write("fold_document();" + MindMapHTMLWriter.el);
		fileout.write("</SCRIPT>" + MindMapHTMLWriter.el);
	}

	private void writeFoldingButtons(final String localParentID) throws IOException {
		fileout.write("<span id=\"show" + localParentID + "\" class=\"foldclosed\" onClick=\"show_folder('"
		        + localParentID + "')\" style=\"POSITION: absolute\">+</span> " + "<span id=\"hide" + localParentID
		        + "\" class=\"foldopened\" onClick=\"hide_folder('" + localParentID + "')\">-</span>");
		fileout.write("\n");
	}

	void writeHTML(final Collection<NodeModel> selectedNodes) throws IOException {
		fileout.write("<html>" + MindMapHTMLWriter.el + "<head>" + MindMapHTMLWriter.el);
		if(! selectedNodes.isEmpty()){
			final MapModel map = selectedNodes.iterator().next().getMap();
			setDefaultsFrom(map);
			writeStyle();
		}
		fileout.write(MindMapHTMLWriter.el + "</head>" + MindMapHTMLWriter.el + "<body>" + MindMapHTMLWriter.el);
		for (NodeModel node : selectedNodes) {
			writeHTML(node, "1", 0, /* isRoot */true, true, /* depth */1);
		}
		fileout.write("</body>" + MindMapHTMLWriter.el);
		fileout.write("</html>" + MindMapHTMLWriter.el);
		fileout.close();
		resetDefaults();
	}

	private void resetDefaults() {
		defaultFont = null;
		defaultColor = null;
	}

	private void setDefaultsFrom(MapModel map) {
	    final MapStyleModel model = MapStyleModel.getExtension(map);
        final NodeModel styleNode = model.getStyleNodeSafe(MapStyleModel.DEFAULT_STYLE);
        defaultFont = nodeStyleController.getFont(styleNode);
        defaultColor =  nodeStyleController.getColor(styleNode);
    }

	void writeHTML(final NodeModel rootNodeOfBranch) throws IOException {
		setDefaultsFrom(rootNodeOfBranch.getMap());
		final String htmlExportFoldingOption = getProperty("html_export_folding");
		writeFoldingCode = (htmlExportFoldingOption.equals("html_export_fold_currently_folded") && mapController
		    .hasFoldedStrictDescendant(rootNodeOfBranch))
		        || htmlExportFoldingOption.equals("html_export_fold_all");
		ResourceController.getResourceController().getBooleanProperty("export_icons_in_html");
		fileout
		    .write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">"
		            + MindMapHTMLWriter.el + "<html>" + MindMapHTMLWriter.el + "<head>" + MindMapHTMLWriter.el);
		fileout.write("<title>"
		        + MindMapHTMLWriter.writeHTML_escapeUnicodeAndSpecialCharacters(TextController.getController().getPlainTextContent(rootNodeOfBranch)
		            .replace('\n', ' ')) + "</title>" + MindMapHTMLWriter.el);
		writeStyle();
		fileout.write(MindMapHTMLWriter.el + "</head>" + MindMapHTMLWriter.el + "<body");
		final MapStyleModel style = MapStyleModel.getExtension(rootNodeOfBranch.getMap());
		final Color background = style != null ? style.getBackgroundColor() : null;
		if (background != null) {
			fileout.write(" bgcolor=" + ColorUtils.colorToString(background));
		}
		fileout.write(">" + MindMapHTMLWriter.el);
		if (writeFoldingCode) {
			writeBodyWithFolding(rootNodeOfBranch);
		}
		else {
			writeHTML(rootNodeOfBranch, "1", 0, /* isRoot */true, true, /* depth */
			1);
		}
		fileout.write("</body>" + MindMapHTMLWriter.el);
		fileout.write("</html>" + MindMapHTMLWriter.el);
		fileout.close();
		resetDefaults();
	}

	private int writeHTML(final NodeModel model, final String parentID, int lastChildNumber, final boolean isRoot,
	                      final boolean treatAsParagraph, final int depth) throws IOException {
		boolean createFolding = false;
		if(writeFoldingCode){
			createFolding = mapController.isFolded(model);
			if (getProperty("html_export_folding").equals("html_export_fold_all")) {
				createFolding = mapController.hasChildren(model);
			}
			if (getProperty("html_export_folding").equals("html_export_no_folding") || basedOnHeadings || isRoot) {
				createFolding = false;
			}
		}
		final TextController textController = TextController.getController();
		final Object userObject = model.getUserObject();
		final String text = textController.getTransformedTextNoThrow(userObject, model, userObject);
		final boolean hasHtml = text.startsWith("<html>");
		final boolean heading = basedOnHeadings && !hasHtml && mapController.hasChildren(model) && depth <= 6;
		if (!treatAsParagraph && !basedOnHeadings) {
			fileout.write("<li>");
		}
		else {
			if (heading) {
				fileout.write("<h" + depth + ">");
			}
			else if (!hasHtml) {
				fileout.write("<p>");
			}
		}
		String localParentID = parentID;
		if (createFolding) {
			lastChildNumber++;
			localParentID = parentID + "_" + lastChildNumber;
			writeFoldingButtons(localParentID);
		}
		String link = NodeLinks.getLinkAsString(model);
		if (link != null) {
			if (link.endsWith(UrlManager.FREEPLANE_FILE_EXTENSION)) {
				link += ".html";
			}
			fileout.write("<a href=\"" + link + "\" target=\"_blank\"><span class=l>~</span>&nbsp;");
		}
		final String fontStyle = fontStyle(nodeStyleController.getColor(model), nodeStyleController.getFont(model));
		if (!fontStyle.equals("")) {
			fileout.write("<span style=\"" + fontStyle + "\">");
		}
		if (ResourceController.getResourceController().getBooleanProperty("export_icons_in_html")) {
			writeIcons(model);
		}
		writeModelContent(text);
        final String detailText = DetailTextModel.getDetailTextText(model);
        if(detailText != null){
        	writeModelContent(detailText);
        }
		if (fontStyle != "") {
			fileout.write("</span>");
		}
        final String noteContent = NoteModel.getNoteText(model);
        if(noteContent != null){
        	writeModelContent(noteContent);
        }
		fileout.write(MindMapHTMLWriter.el);
		if (link != null) {
			fileout.write("</a>" + MindMapHTMLWriter.el);
		}
		if (heading) {
			fileout.write("</h" + depth + ">" + MindMapHTMLWriter.el);
		}
		boolean treatChildrenAsParagraph = false;
		for (final NodeModel child : mapController.childrenUnfolded(model)) {
			if (child.toString().length() > 100) {
				treatChildrenAsParagraph = true;
				break;
			}
		}
		if (getProperty("html_export_folding").equals("html_export_based_on_headings")) {
			for (final NodeModel child : mapController.childrenUnfolded(model)) {
				lastChildNumber = writeHTML(child, parentID, lastChildNumber,/*isRoot=*/false,
				    treatChildrenAsParagraph, depth + 1);
			}
			return lastChildNumber;
		}
		if (mapController.hasChildren(model)) {
			if (getProperty("html_export_folding").equals("html_export_based_on_headings")) {
				for (final NodeModel child : mapController.childrenUnfolded(model)) {
					lastChildNumber = writeHTML(child, parentID, lastChildNumber,
					/*isRoot=*/false, treatChildrenAsParagraph, depth + 1);
				}
			}
			else if (createFolding) {
				fileout.write("<ul id=\"fold" + localParentID
				        + "\" style=\"POSITION: relative; VISIBILITY: visible;\">");
				if (treatChildrenAsParagraph) {
					fileout.write("<li>");
				}
				int localLastChildNumber = 0;
				for (final NodeModel child : mapController.childrenUnfolded(model)) {
					localLastChildNumber = writeHTML(child, localParentID, localLastChildNumber,
					/* isRoot=*/false, treatChildrenAsParagraph, depth + 1);
				}
			}
			else {
				fileout.write("<ul>");
				if (treatChildrenAsParagraph) {
					fileout.write("<li>");
				}
				for (final NodeModel child : mapController.childrenUnfolded(model)) {
					lastChildNumber = writeHTML(child, parentID, lastChildNumber,
					/* isRoot= */false, treatChildrenAsParagraph, depth + 1);
				}
			}
			if (treatChildrenAsParagraph) {
				fileout.write("</li>");
			}
			fileout.write(MindMapHTMLWriter.el);
			fileout.write("</ul>");
		}
		if (!treatAsParagraph) {
			fileout.write(MindMapHTMLWriter.el + "</li>" + MindMapHTMLWriter.el);
		}
		return lastChildNumber;
	}

	private void writeIcons(final NodeModel model) throws IOException {
		final Collection<MindIcon> icons = IconController.getController().getIcons(model);
		for (MindIcon icon : icons) {
			final String iconFileName = icon.getFileName();
			fileout.write("<img src=\"" + iconFileName + "\" alt=\"" + icon.getDescription() + "\">");
		}
	}

	private void writeJavaScript() throws IOException {
		fileout.write("" + MindMapHTMLWriter.el + "<script type=\"text/javascript\">" + MindMapHTMLWriter.el
		        + "   // Here we implement folding. It works fine with MSIE5.5, MSIE6.0 and" + MindMapHTMLWriter.el
		        + "   // Mozilla 0.9.6." + MindMapHTMLWriter.el + "" + MindMapHTMLWriter.el
		        + "   if (document.layers) {" + MindMapHTMLWriter.el + "      //Netscape 4 specific code"
		        + MindMapHTMLWriter.el + "      pre = 'document.';" + MindMapHTMLWriter.el + "      post = ''; }"
		        + MindMapHTMLWriter.el + "   if (document.getElementById) {" + MindMapHTMLWriter.el
		        + "      //Netscape 6 specific code" + MindMapHTMLWriter.el
		        + "      pre = 'document.getElementById(\"';" + MindMapHTMLWriter.el + "      post = '\").style'; }"
		        + MindMapHTMLWriter.el + "   if (document.all) {" + MindMapHTMLWriter.el + "      //IE4+ specific code"
		        + MindMapHTMLWriter.el + "      pre = 'document.all.';" + MindMapHTMLWriter.el
		        + "      post = '.style'; }" + MindMapHTMLWriter.el + "" + MindMapHTMLWriter.el
		        + "function layer_exists(layer) {" + MindMapHTMLWriter.el + "   try {" + MindMapHTMLWriter.el
		        + "      eval(pre + layer + post);" + MindMapHTMLWriter.el + "      return true; }"
		        + MindMapHTMLWriter.el + "   catch (error) {" + MindMapHTMLWriter.el + "      return false; }}"
		        + MindMapHTMLWriter.el + "" + MindMapHTMLWriter.el + "function show_layer(layer) {"
		        + MindMapHTMLWriter.el + "   eval(pre + layer + post).position = 'relative'; " + MindMapHTMLWriter.el
		        + "   eval(pre + layer + post).visibility = 'visible'; }" + MindMapHTMLWriter.el + ""
		        + MindMapHTMLWriter.el + "function hide_layer(layer) {" + MindMapHTMLWriter.el
		        + "   eval(pre + layer + post).visibility = 'hidden';" + MindMapHTMLWriter.el
		        + "   eval(pre + layer + post).position = 'absolute'; }" + MindMapHTMLWriter.el + ""
		        + MindMapHTMLWriter.el + "function hide_folder(folder) {" + MindMapHTMLWriter.el
		        + "    hide_folding_layer(folder)" + MindMapHTMLWriter.el + "    show_layer('show'+folder);"
		        + MindMapHTMLWriter.el + "" + MindMapHTMLWriter.el
		        + "    scrollBy(0,0); // This is a work around to make it work in Browsers (Explorer, Mozilla)"
		        + MindMapHTMLWriter.el + "}" + MindMapHTMLWriter.el + "" + MindMapHTMLWriter.el
		        + "function show_folder(folder) {" + MindMapHTMLWriter.el
		        + "    // Precondition: all subfolders are folded" + MindMapHTMLWriter.el + "" + MindMapHTMLWriter.el
		        + "    show_layer('hide'+folder);" + MindMapHTMLWriter.el + "    hide_layer('show'+folder);"
		        + MindMapHTMLWriter.el + "    show_layer('fold'+folder);" + MindMapHTMLWriter.el + ""
		        + MindMapHTMLWriter.el
		        + "    scrollBy(0,0); // This is a work around to make it work in Browsers (Explorer, Mozilla)"
		        + MindMapHTMLWriter.el + "" + MindMapHTMLWriter.el + "    var i;" + MindMapHTMLWriter.el
		        + "    for (i=1; layer_exists('fold'+folder+'_'+i); ++i) {" + MindMapHTMLWriter.el
		        + "       show_layer('show'+folder+'_'+i); }" + MindMapHTMLWriter.el + "}" + MindMapHTMLWriter.el + ""
		        + "function show_folder_completely(folder) {" + MindMapHTMLWriter.el
		        + "    // Precondition: all subfolders are folded" + MindMapHTMLWriter.el + "" + MindMapHTMLWriter.el
		        + "    show_layer('hide'+folder);" + MindMapHTMLWriter.el + "    hide_layer('show'+folder);"
		        + MindMapHTMLWriter.el + "    show_layer('fold'+folder);" + MindMapHTMLWriter.el + ""
		        + MindMapHTMLWriter.el
		        + "    scrollBy(0,0); // This is a work around to make it work in Browsers (Explorer, Mozilla)"
		        + MindMapHTMLWriter.el + "" + MindMapHTMLWriter.el + "    var i;" + MindMapHTMLWriter.el
		        + "    for (i=1; layer_exists('fold'+folder+'_'+i); ++i) {" + MindMapHTMLWriter.el
		        + "       show_folder_completely(folder+'_'+i); }" + MindMapHTMLWriter.el + "}" + MindMapHTMLWriter.el
		        + "" + MindMapHTMLWriter.el + "" + MindMapHTMLWriter.el + "" + MindMapHTMLWriter.el
		        + "function hide_folding_layer(folder) {" + MindMapHTMLWriter.el + "   var i;" + MindMapHTMLWriter.el
		        + "   for (i=1; layer_exists('fold'+folder+'_'+i); ++i) {" + MindMapHTMLWriter.el
		        + "       hide_folding_layer(folder+'_'+i); }" + MindMapHTMLWriter.el + "" + MindMapHTMLWriter.el
		        + "   hide_layer('hide'+folder);" + MindMapHTMLWriter.el + "   hide_layer('show'+folder);"
		        + MindMapHTMLWriter.el + "   hide_layer('fold'+folder);" + MindMapHTMLWriter.el + ""
		        + MindMapHTMLWriter.el
		        + "   scrollBy(0,0); // This is a work around to make it work in Browsers (Explorer, Mozilla)"
		        + MindMapHTMLWriter.el + "}" + MindMapHTMLWriter.el + "" + MindMapHTMLWriter.el
		        + "function fold_document() {" + MindMapHTMLWriter.el + "   var i;" + MindMapHTMLWriter.el
		        + "   var folder = '1';" + MindMapHTMLWriter.el
		        + "   for (i=1; layer_exists('fold'+folder+'_'+i); ++i) {" + MindMapHTMLWriter.el
		        + "       hide_folder(folder+'_'+i); }" + MindMapHTMLWriter.el + "}" + MindMapHTMLWriter.el + ""
		        + MindMapHTMLWriter.el + "function unfold_document() {" + MindMapHTMLWriter.el + "   var i;"
		        + MindMapHTMLWriter.el + "   var folder = '1';" + MindMapHTMLWriter.el
		        + "   for (i=1; layer_exists('fold'+folder+'_'+i); ++i) {" + MindMapHTMLWriter.el
		        + "       show_folder_completely(folder+'_'+i); }" + MindMapHTMLWriter.el + "}" + MindMapHTMLWriter.el
		        + "" + MindMapHTMLWriter.el + "</script>" + MindMapHTMLWriter.el);
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
		fileout.write("<style type=\"text/css\">" + MindMapHTMLWriter.el);
		fileout.write("    body {");
		writeDefaultFontStyle();
		fileout.write("}" + MindMapHTMLWriter.el);
		fileout.write("    li { list-style: none;  margin: 0; }" + MindMapHTMLWriter.el);
		fileout.write("    p { margin: 0; }" + MindMapHTMLWriter.el);
		if (writeFoldingCode) {
			fileout
			    .write("    span.foldopened { color: white; font-size: xx-small;"
			            + MindMapHTMLWriter.el
			            + "    border-width: 1; font-family: monospace; padding: 0em 0.25em 0em 0.25em; background: #e0e0e0;"
			            + MindMapHTMLWriter.el
			            + "    VISIBILITY: visible;"
			            + MindMapHTMLWriter.el
			            + "    cursor:pointer; }"
			            + MindMapHTMLWriter.el
			            + ""
			            + MindMapHTMLWriter.el
			            + ""
			            + MindMapHTMLWriter.el
			            + "    span.foldclosed { color: #666666; font-size: xx-small;"
			            + MindMapHTMLWriter.el
			            + "    border-width: 1; font-family: monospace; padding: 0em 0.25em 0em 0.25em; background: #e0e0e0;"
			            + MindMapHTMLWriter.el
			            + "    VISIBILITY: hidden;"
			            + MindMapHTMLWriter.el
			            + "    cursor:pointer; }"
			            + MindMapHTMLWriter.el
			            + ""
			            + MindMapHTMLWriter.el
			            + "    span.foldspecial { color: #666666; font-size: xx-small; border-style: none solid solid none;"
			            + MindMapHTMLWriter.el
			            + "    border-color: #CCCCCC; border-width: 1; font-family: sans-serif; padding: 0em 0.1em 0em 0.1em; background: #e0e0e0;"
			            + MindMapHTMLWriter.el + "    cursor:pointer; }" + MindMapHTMLWriter.el);
		}
		fileout.write(MindMapHTMLWriter.el + "    span.l { color: red; font-weight: bold; }" + MindMapHTMLWriter.el
		        + "" + MindMapHTMLWriter.el + "    a.mapnode:link {text-decoration: none; color: black; }"
		        + MindMapHTMLWriter.el + "    a.mapnode:visited {text-decoration: none; color: black; }"
		        + MindMapHTMLWriter.el + "    a.mapnode:active {text-decoration: none; color: black; }"
		        + MindMapHTMLWriter.el
		        + "    a.mapnode:hover {text-decoration: none; color: black; background: #eeeee0; }"
		        + MindMapHTMLWriter.el + "" + MindMapHTMLWriter.el + "</style>" + MindMapHTMLWriter.el
		        + "<!-- ^ Position is not set to relative / absolute here because of Mozilla -->");
	}

	private void writeDefaultFontStyle() throws IOException {
	    Font font = defaultFont;
		defaultFont = null;
		Color color = defaultColor;
		defaultColor = null;
		fontStyle(color, font);
		defaultFont = font;
		defaultColor = color;
    }
}
