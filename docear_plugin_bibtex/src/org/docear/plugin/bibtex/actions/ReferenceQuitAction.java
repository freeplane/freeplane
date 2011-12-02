/**
 * author: Marcel Genzmehr
 * 02.12.2011
 */
package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;

import net.sf.jabref.BibtexDatabase;
import net.sf.jabref.BibtexEntry;

import org.docear.plugin.bibtex.ReferencesController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.QuitAction;

/**
 * 
 */
public class ReferenceQuitAction extends QuitAction {
	private static final long serialVersionUID = 1L;
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	public void actionPerformed(ActionEvent e) {
		LogUtils.info("saving all references ...");
		BibtexDatabase database = ReferencesController.getController().getJabrefWrapper().getDatabase();
		
		for (BibtexEntry entry : database.getEntries()) {
			if (entry.getField("docear_add_to_node") != null) {
				entry.setField("docear_add_to_node", null);
			}
		}
		
		ReferencesController.getController().getJabrefWrapper().getBasePanel().runCommand("save");
	}
}
