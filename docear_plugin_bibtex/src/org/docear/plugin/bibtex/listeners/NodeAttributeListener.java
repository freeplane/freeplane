package org.docear.plugin.bibtex.listeners;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import net.sf.jabref.BasePanel;
import net.sf.jabref.BibtexDatabase;
import net.sf.jabref.BibtexEntry;

import org.docear.plugin.bibtex.JabRefAttributes;
import org.docear.plugin.bibtex.ReferencesController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.attribute.Attribute;
import org.freeplane.features.attribute.NodeAttributeTableModel;

public class NodeAttributeListener implements TableModelListener {
	
	public void tableChanged(TableModelEvent e) {
		// do not use locking mechanism --> changes made to jabref should change all associated entries in mindmapo nodes as well

		// if changes only happened one row and one column
		if (e.getFirstRow() == e.getLastRow() && e.getColumn() > 0) {
			JabRefAttributes jabRefAttributes = ReferencesController.getController().getJabRefAttributes();
			NodeAttributeTableModel table = (NodeAttributeTableModel) e.getSource();
			Attribute attribute = table.getAttribute(e.getFirstRow());
			

			String key = null;
			//only act on known attributes
			if (attribute.getName().equals(jabRefAttributes.getKeyAttribute())) {
				System.out.println("debug e: "+e.getSource());
				
			}
			else if (jabRefAttributes.getValueAttributes().containsKey(attribute.getName())) {				
				int pos = table.getAttributePosition(jabRefAttributes.getKeyAttribute());
				if (pos > 0) {
					key = (String) table.getValue(pos);
					
					if (key != null) {
						LogUtils.info("debug changed for [" + key + "]: " + attribute.getName() + " --> " + attribute.getValue());
						updateBibtexEntry(key, attribute);
					}
				}
			}
		}
	}

	private void updateBibtexEntry(String key, Attribute attribute) {
		((BasePanel) ReferencesController.getController().getJabrefWrapper().getJabrefFrame().getTabbedPane().getSelectedComponent()).getDatabase();
		BibtexDatabase database = ReferencesController.getController().getJabrefWrapper().getDatabase(); 
		BibtexEntry entry = database.getEntryByKey(key);
		if (entry != null) {
			//updating the entry updates it in the database object which is used to rendere jabrefs entry table --> no need to update the jabref view
			entry.setField(ReferencesController.getController().getJabRefAttributes().getValueAttributes().get(attribute.getName()), attribute.getValue().toString());			
		}
	}
	
	

}
