package org.docear.plugin.bibtex.actions;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import net.sf.jabref.BibtexEntry;
import net.sf.jabref.BibtexFields;
import net.sf.jabref.Globals;
import net.sf.jabref.labelPattern.LabelPatternUtil;

import org.docear.plugin.bibtex.ReferencesController;
import org.docear.plugin.pdfutilities.util.NodeUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

public class ReferenceUtils {
	public static void addReferenceToNode(BibtexEntry entry) {
		NodeModel currentNode = Controller.getCurrentModeController().getMapController().getSelectedNode();
		
		
		if (entry.getField("bibtexkey")==null) {
			LabelPatternUtil.makeLabel(Globals.prefs.getKeyPattern(), ReferencesController.getController().getJabrefWrapper().getDatabase(), entry);						
		}
		
		//renewAttribute(currentNode, "jabref_id", entry.getId());
		renewAttribute(currentNode, "bibtex_key", entry.getField("bibtexkey"));
		renewAttribute(currentNode, "jabref_author", entry.getField("author"));
		renewAttribute(currentNode, "jabref_title", entry.getField("title"));
		renewAttribute(currentNode, "jabref_year", entry.getField("year"));
		renewAttribute(currentNode, "jabref_journal", entry.getField("journal"));
		
		
		
		String path = entry.getField("file");
		System.out.println("debug path: "+path);
		
		
		if (path != null) {
			NodeUtils.setLinkFrom(new File(path).toURI(), currentNode);
		}
		else {
			path = entry.getField("url");			
			if (path != null) {
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

	}

	private static void renewAttribute(NodeModel node, String key, String value) {
		NodeUtils.removeAttribute(node, key);
		NodeUtils.setAttributeValue(node, key, value);
	}
}
