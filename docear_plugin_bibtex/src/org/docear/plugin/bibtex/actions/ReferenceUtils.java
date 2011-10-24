package org.docear.plugin.bibtex.actions;

import net.sf.jabref.BibtexEntry;

import org.docear.plugin.pdfutilities.util.NodeUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

public class ReferenceUtils {
	public static void addReferenceToNode(BibtexEntry entry) {
		NodeModel currentNode = Controller.getCurrentModeController().getMapController().getSelectedNode();
		
		NodeUtils.removeAttribute(currentNode, "jabref_id");
		NodeUtils.removeAttribute(currentNode, "bibtex_key");
		NodeUtils.removeAttribute(currentNode, "jabref_author");
		NodeUtils.removeAttribute(currentNode, "jabref_title");
		NodeUtils.removeAttribute(currentNode, "jabref_year");
		NodeUtils.removeAttribute(currentNode, "jabref_journal");
		
		NodeUtils.setAttributeValue(currentNode, "jabref_id", entry.getId());
		NodeUtils.setAttributeValue(currentNode, "bibtex_key", entry.getField("bibtexkey"));
		NodeUtils.setAttributeValue(currentNode, "jabref_author", entry.getField("author"));
		NodeUtils.setAttributeValue(currentNode, "jabref_title", entry.getField("title"));
		NodeUtils.setAttributeValue(currentNode, "jabref_year", entry.getField("year"));
		NodeUtils.setAttributeValue(currentNode, "jabref_journal", entry.getField("journal"));
	}
}
