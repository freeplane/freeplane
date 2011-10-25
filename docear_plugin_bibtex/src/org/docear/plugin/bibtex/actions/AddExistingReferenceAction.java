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
	
	private class SelectItem implements Comparable {
		private final String name;
		private final String value;
		private final String orderString;
		
		public SelectItem(String name, String value, String orderString) {
			this.name = name;
			this.value = value;
			this.orderString = orderString;
		}

		public String getName() {
			return name;
		}

		public String getValue() {
			return value;
		}
		
		protected String getOrderString() {
			return orderString;
		}
		
		public String toString() {
			return this.name;
		}

		@Override
		public int compareTo(Object o) {
			if (o instanceof SelectItem) {				
				return this.orderString.toLowerCase().compareTo(((SelectItem) o).getOrderString().toLowerCase());						
			}
			return 1;
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AddExistingReferenceAction(String key) {
		super(key);
	}

	public void actionPerformed(ActionEvent arg0) {
		BibtexDatabase db = ReferencesController.getController().getJabrefWrapper().getDatabase();

		TreeSet<SelectItem> bibtexKeys = new TreeSet<SelectItem>();

		for (String s : db.getKeySet()) {			
			BibtexEntry entry = db.getEntryById(s);
			System.out.println(s+": "+entry.getCiteKey());
			bibtexKeys.add(new SelectItem("["+entry.getCiteKey()+"]   "+entry.getAuthorTitleYear(50), entry.getId(), entry.getCiteKey()));
		}		
		SelectItem item = (SelectItem) JOptionPane.showInputDialog(Controller.getCurrentController().getViewController().getContentPane(),
				null, TextUtils.getText("add_reference"), JOptionPane.QUESTION_MESSAGE, null,
				bibtexKeys.toArray(), bibtexKeys.first());
		if (item != null) {
			try {	
				BibtexEntry entry = db.getEntryById(item.getValue());
				ReferenceUtils.addReferenceToNode(entry);			
			}
			catch (NullPointerException e) {
				JOptionPane.showMessageDialog(Controller.getCurrentController().getViewController().getContentPane(),
						TextUtils.getText("bibtex_key_not_found_title"), TextUtils.getText("bibtex_key_not_found"),
						JOptionPane.ERROR_MESSAGE);
			}
		}

	}

}
