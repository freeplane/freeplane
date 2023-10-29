/*
 * Created on 13 May 2023
 *
 * author dimitry
 */
package org.freeplane.features.map.clipboard;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.clipboard.ClipboardController.CopiedNodeSet;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.note.NoteModel;
import org.freeplane.features.text.DetailModel;
import org.freeplane.features.text.TextController;

public class MindMapPlainTextWriter {
    public static final MindMapPlainTextWriter INSTANCE = new MindMapPlainTextWriter();

    private MindMapPlainTextWriter() {/**/}

    public String getAsPlainText(final Collection<NodeModel> selectedNodes, CopiedNodeSet copiedNodeSet) {
        try {
            final StringWriter stringWriter = new StringWriter();
            try (final BufferedWriter fileout = new BufferedWriter(stringWriter)) {
                for (final Iterator<NodeModel> it = selectedNodes.iterator(); it.hasNext();) {
                    writeTXT(it.next(), copiedNodeSet, fileout,/* depth= */0);
                }
            }
            return stringWriter.toString();
        }
        catch (final Exception e) {
            LogUtils.severe(e);
            return null;
        }
    }
    private void writeTXT(final NodeModel mindMapNodeModel, CopiedNodeSet copiedNodeSet, final Writer fileout, final int depth) throws IOException {
        boolean indentTextOutput = ResourceController.getResourceController().getBooleanProperty("indentTextOutput");
        boolean indentationUsesTabsInTextOutput = ResourceController.getResourceController().getBooleanProperty("indentationUsesTabsInTextOutput");
        String indentation = indentTextOutput ? (indentationUsesTabsInTextOutput ? "\t" : "    ") : "";
        writeTXT(mindMapNodeModel, copiedNodeSet, fileout, depth, indentation);
    }

    private void writeTXT(final NodeModel node, CopiedNodeSet copiedNodeSet, final Writer fileout, final int depth, String indentation) throws IOException {
		String core = getTransformedTextForClipboard(node, node, node.getUserObject());
		writeMultilineTXT(fileout, depth, indentation, core);
		if (NodeLinks.getValidLink(node) != null) {
			final String link = NodeLinks.getLinkAsString(node);
			if (! core.contains(link)) {
				writeTXT(fileout, depth, indentation, " <" + link + ">");
			}
		}
		String detailText = DetailModel.getDetailText(node);
		if(detailText != null) {
			String details = getTransformedTextForClipboard(node, DetailModel.getDetail(node), detailText);
			writeMultilineTXT(fileout, depth+1, indentation, details);
		}
		String noteText = NoteModel.getNoteText(node);
		if(noteText != null) {
			String transformedNote = getTransformedTextForClipboard(node, NoteModel.getNote(node), noteText);
			writeMultilineTXT(fileout, depth+1, indentation, transformedNote);
		}
        writeChildrenText(node, copiedNodeSet, fileout, depth, indentation);
    }

    private void writeMultilineTXT(final Writer fileout, final int depth, String indentation,
            String transformedTextForClipboard) throws IOException {
        String[] plainTextContentStrings = transformedTextForClipboard.split("\\n");
        for(String plainTextContent : plainTextContentStrings) {
            if(! plainTextContent.isEmpty())
                writeTXT(fileout, depth, indentation, plainTextContent);
        }
    }

    private void writeTXT(final Writer fileout, final int depth,
            String indentation, String plainTextContent) throws IOException {
        if(! indentation.isEmpty()) {
            for (int i = 0; i < depth; ++i) {
                fileout.write(indentation);
            }
        }
        fileout.write(plainTextContent);
        fileout.write("\n");
    }

    private String getTransformedTextForClipboard(final NodeModel node, Object nodeProperty, Object content) {
        String text = TextController.getController().getTransformedTextForClipboard(node, nodeProperty, content);
        String plainTextContent = HtmlUtils.htmlToPlain(text);
        return plainTextContent;
    }

    private void writeChildrenText(final NodeModel node, CopiedNodeSet copiedNodeSet, final Writer fileout, final int depth, String indentation)
            throws IOException {
        for (final NodeModel child : node.getChildren()) {
            if (copiedNodeSet == CopiedNodeSet.ALL_NODES || child.hasVisibleContent(FilterController.getFilter(node.getMap()))) {
                writeTXT(child, copiedNodeSet, fileout, depth + 1, indentation);
            }
            else {
                writeChildrenText(child, copiedNodeSet, fileout, depth, indentation);
            }
        }
    }
}