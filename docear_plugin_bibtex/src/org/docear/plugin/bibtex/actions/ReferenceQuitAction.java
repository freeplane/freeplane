/**
 * author: Marcel Genzmehr
 * 02.12.2011
 */
package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;

import net.sf.jabref.BibtexDatabase;
import net.sf.jabref.BibtexEntry;
import net.sf.jabref.export.SaveDatabaseAction;

import org.docear.plugin.bibtex.ReferencesController;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
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
		try {
			if(ReferencesController.getController().getJabrefWrapper() != null) {
				BibtexDatabase database = ReferencesController.getController().getJabrefWrapper().getDatabase();
				if(database == null) {
					return;
				}
				for (BibtexEntry entry : database.getEntries()) {
					if (entry.getField("docear_add_to_node") != null) {
						entry.setField("docear_add_to_node", null);
					}
				}
				if(ReferencesController.getController().getJabrefWrapper().getBasePanel().isUpdatedExternally()){
					DocearController.getController().addWorkingThreadHandle("ReferenceQuitAction");
					SaveDatabaseAction saveAction = new SaveDatabaseAction(ReferencesController.getController().getJabrefWrapper().getBasePanel());
					saveAction.runCommand();
					if (saveAction.isCancelled() || !saveAction.isSuccess()) {						
						DocearController.getController().dispatchDocearEvent(new DocearEvent(this, DocearEventType.APPLICATION_CLOSING_ABORTED));
					}
					DocearController.getController().removeWorkingThreadHandle("ReferenceQuitAction");
				}
				else{
					ReferencesController.getController().getJabrefWrapper().getBasePanel().runCommand("save");
				}
			}
		} 
		catch (Exception ex) {
			LogUtils.warn(ex);
		} catch (Throwable t) {
			LogUtils.warn(t);
		}
	}
}
