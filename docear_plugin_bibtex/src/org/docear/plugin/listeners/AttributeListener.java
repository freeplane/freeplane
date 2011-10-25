package org.docear.plugin.listeners;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import net.sf.jabref.BasePanel;
import net.sf.jabref.BibtexDatabase;
import net.sf.jabref.BibtexEntry;
import net.sf.jabref.JabRefFrame;

import org.docear.plugin.bibtex.JabRefAttributes;
import org.docear.plugin.bibtex.ReferencesController;
import org.freeplane.features.attribute.Attribute;
import org.freeplane.features.attribute.NodeAttributeTableModel;

public class AttributeListener implements TableModelListener {

	@Override
	public void tableChanged(TableModelEvent e) {
		System.out.println("debug tableChanged: column: " + e.getColumn() + " firstRow:" + e.getFirstRow() + " lastRow:"
				+ e.getLastRow() + " type: " + e.getType());

		// if changes only happened one row and one column
		if (e.getFirstRow() == e.getLastRow() && e.getColumn() > 0) {
			JabRefAttributes jabRefAttributes = ReferencesController.getController().getJabRefAttributes();
			NodeAttributeTableModel table = (NodeAttributeTableModel) e.getSource();
			Attribute attribute = table.getAttribute(e.getFirstRow());

			String key = null;
			if (jabRefAttributes.getValueAttributes().containsKey(attribute.getName())) {
				key = (String) table.getAttribute(
						table.getAttributePosition(jabRefAttributes.getKeyAttribute()))
						.getValue();
				System.out.println("debug changed for [" + key + "]: " + attribute.getName() + " <--> " + attribute.getValue());
				
				updateBibtexEntry(key, attribute);
			}

		}
	}

	private void updateBibtexEntry(String key, Attribute attribute) {
		BibtexDatabase database = ReferencesController.getController().getJabrefWrapper().getDatabase(); 
		BibtexEntry entry = database.getEntryByKey(key);
		
		entry.setField(ReferencesController.getController().getJabRefAttributes().getValueAttributes().get(attribute.getName()), attribute.getValue().toString());
		System.out.println("database: "+database.getEntryByKey("Langer2011").getField("year"));
		ReferencesController.getController().getJabrefWrapper().updateDatabase(database);
	}
	
	

}
