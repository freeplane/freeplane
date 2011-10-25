package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import net.sf.jabref.BasePanel;
import net.sf.jabref.BibtexDatabase;
import net.sf.jabref.BibtexEntry;
import net.sf.jabref.BibtexEntryType;
import net.sf.jabref.EntryTypeDialog;
import net.sf.jabref.export.DocearSaveDatabaseAction;

import org.docear.plugin.bibtex.ReferencesController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.mode.Controller;

import ca.odell.glazedlists.EventList;

public class AddNewReferenceAction extends AFreeplaneAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AddNewReferenceAction(String key) {
		super(key);
	}

	public void actionPerformed(ActionEvent e) {
		BasePanel basePanel = ((BasePanel) ReferencesController.getController().getJabrefWrapper().getJabrefFrame().getTabbedPane()
				.getSelectedComponent());
		
		if(e.getActionCommand().equals(DocearSaveDatabaseAction.JABREF_DATABASE_SAVE_SUCCESS)) {
			BibtexDatabase db = ReferencesController.getController().getJabrefWrapper().getDatabase();
			
			EventList<BibtexEntry> entryList = basePanel.getMainTable().getSelected();			
			if (entryList.size()>0) {
				BibtexEntry entry = entryList.get(0);
			
				if (entry.getField("docear_add_to_node") == null) {					 
					return;
				}
				entry.setField("docear_add_to_node", null);				
				ReferenceUtils.addReferenceToNode(entry);
			}
			return;
		}
		if(e.getActionCommand().equals(DocearSaveDatabaseAction.JABREF_DATABASE_SAVE_FAILED)) {
			return;
		}
		EntryTypeDialog dialog = new EntryTypeDialog(ReferencesController.getController().getJabrefWrapper().getJabrefFrame());
		dialog.setVisible(true);

		BibtexEntryType bet = dialog.getChoice();
		if (bet == null) {
			return;
		}
		String thisType = bet.getName();

		showJabRefTab();
		
		BibtexEntry entry = basePanel.newEntry(BibtexEntryType.getType(thisType));
		entry.setField("docear_add_to_node", "true");

	}

	private void showJabRefTab() {
		final JComponent toolBar = Controller.getCurrentModeController().getUserInputListenerFactory().getToolBar("/format");
		toolBar.setVisible(true);
		((JComponent) toolBar.getParent()).revalidate();

		final String propertyName = Controller.getCurrentController().getViewController().completeVisiblePropertyKey(toolBar);
		Controller.getCurrentController().getResourceController().setProperty(propertyName, true);

		JTabbedPane tabbedPane = (JTabbedPane) toolBar.getComponent(1);
		tabbedPane.setSelectedComponent(ReferencesController.getController().getJabrefWrapper().getJabrefFrame());
	}

}
