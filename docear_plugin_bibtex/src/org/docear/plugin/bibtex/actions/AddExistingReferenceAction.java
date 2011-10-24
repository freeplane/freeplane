package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JOptionPane;

import net.sf.jabref.BibtexDatabase;
import net.sf.jabref.BibtexEntry;
import net.sf.jabref.bst.BibtexTextPrefix;
import net.sf.jabref.journals.JournalAbbreviations;

import org.docear.plugin.bibtex.ReferencesController;
import org.docear.plugin.pdfutilities.util.NodeUtils;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

public class AddExistingReferenceAction extends AFreeplaneAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AddExistingReferenceAction(String key) {
		super(key);
	}

	public void actionPerformed(ActionEvent arg0) {
		BibtexDatabase db = ReferencesController.getReferencesController().getJabrefWrapper().getDatabase();

		TreeSet<String> bibtexKeys = new TreeSet<String>();

		for (String s : db.getKeySet()) {
			BibtexEntry entry = db.getEntryById(s);
			bibtexKeys.add(entry.getCiteKey());
		}
		String key = (String) JOptionPane.showInputDialog(Controller.getCurrentController().getViewController().getContentPane(),
				null, TextUtils.getText("add_reference"), JOptionPane.QUESTION_MESSAGE, null,
				bibtexKeys.toArray(), bibtexKeys.first());

		try {		
			NodeModel currentNode = Controller.getCurrentModeController().getMapController().getSelectedNode();			
			NodeUtils.setAttributeValue(currentNode, "bibtex_key", key);
			NodeUtils.setAttributeValue(currentNode, "jabref_author", db.getEntryByKey(key).getField("author"));
			NodeUtils.setAttributeValue(currentNode, "jabref_title", db.getEntryByKey(key).getField("title"));
			NodeUtils.setAttributeValue(currentNode, "jabref_year", db.getEntryByKey(key).getField("year"));
			NodeUtils.setAttributeValue(currentNode, "jabref_journal", db.getEntryByKey(key).getField("journal"));			
		}
		catch (NullPointerException e) {
			JOptionPane.showMessageDialog(Controller.getCurrentController().getViewController().getContentPane(),
					TextUtils.getText("bibtex_key_not_found_title"), TextUtils.getText("bibtex_key_not_found"),
					JOptionPane.ERROR_MESSAGE);
		}

	}

}
