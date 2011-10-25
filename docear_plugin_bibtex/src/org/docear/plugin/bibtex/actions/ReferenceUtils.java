package org.docear.plugin.bibtex.actions;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import net.sf.jabref.BibtexEntry;

import org.docear.plugin.pdfutilities.util.NodeUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

public class ReferenceUtils {
	public static void addReferenceToNode(BibtexEntry entry) {
		NodeModel currentNode = Controller.getCurrentModeController().getMapController().getSelectedNode();

		renewAttribute(currentNode, "jabref_id", entry.getId());
		renewAttribute(currentNode, "bibtex_key", entry.getField("bibtexkey"));
		renewAttribute(currentNode, "jabref_author", entry.getField("author"));
		renewAttribute(currentNode, "jabref_title", entry.getField("title"));
		renewAttribute(currentNode, "jabref_year", entry.getField("year"));
		renewAttribute(currentNode, "jabref_journal", entry.getField("journal"));

		String path = entry.getField("file");
		if (path != null) {
			NodeUtils.setLinkFrom(new File(path).toURI(), currentNode);
		}
		else {
			path = entry.getField("url");

			System.out.println("debug url: " + path);
			URI link;
			try {
				link = LinkController.createURI(path.trim());
				final MLinkController linkController = (MLinkController) MLinkController.getController();
				linkController.setLink(currentNode, link, LinkController.LINK_ABSOLUTE);
			}
			catch (URISyntaxException e) {				
				e.printStackTrace();
			}
		}

	}

	private static void renewAttribute(NodeModel node, String key, String value) {
		NodeUtils.removeAttribute(node, key);
		NodeUtils.setAttributeValue(node, key, value);
	}
}
