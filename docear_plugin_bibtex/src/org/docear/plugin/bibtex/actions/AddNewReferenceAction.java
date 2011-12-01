package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import net.sf.jabref.BasePanel;
import net.sf.jabref.BibtexEntry;
import net.sf.jabref.BibtexEntryType;
import net.sf.jabref.EntryTypeDialog;
import net.sf.jabref.export.DocearSaveDatabaseAction;

import org.docear.plugin.bibtex.JabrefWrapper;
import org.docear.plugin.bibtex.ReferencesController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspaceUtils;

import spl.PdfImporter;

public class AddNewReferenceAction extends AFreeplaneAction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AddNewReferenceAction(String key) {
		super(key);
	}

	public void actionPerformed(ActionEvent e) {
		BibtexEntry entry = null;
		if (e.getActionCommand().equals(DocearSaveDatabaseAction.JABREF_DATABASE_SAVE_SUCCESS)) {
			try {
				entry = (BibtexEntry) e.getSource();
				String nodeID = entry.getField("docear_add_to_node");
				if (nodeID != null) {
					NodeModel node = Controller.getCurrentModeController().getMapController().getNodeFromID(nodeID);
					ReferencesController.getController().getJabRefAttributes().setReferenceToNode(entry, node);
				}
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
			return;
		}
		else if (e.getActionCommand().equals(DocearSaveDatabaseAction.JABREF_DATABASE_SAVE_FAILED)) {
			return;
		}
		else {			
			NodeModel node = Controller.getCurrentModeController().getMapController().getSelectedNode();
			URI link = NodeLinks.getLink(node);
			
			JabrefWrapper jabrefWrapper = ReferencesController.getController().getJabrefWrapper();

			if (link != null && link.getPath().toLowerCase().endsWith(".pdf")) {
				String path = WorkspaceUtils.resolveURI(link).getAbsolutePath();
				PdfImporter pdfImporter = new PdfImporter(jabrefWrapper.getJabrefFrame(), jabrefWrapper.getJabrefFrame()
						.basePanel(), null, 0);
				pdfImporter.importPdfFiles(new String[] { path }, Controller.getCurrentController()
						.getViewController().getFrame(), true);
				entry = pdfImporter.getNewEntry();
			}
			else {
				BasePanel basePanel = jabrefWrapper.getBasePanel();

				EntryTypeDialog dialog = new EntryTypeDialog(jabrefWrapper.getJabrefFrame());
				dialog.setVisible(true);

				BibtexEntryType bet = dialog.getChoice();
				if (bet == null) {
					return;
				}
				String thisType = bet.getName();

				entry = basePanel.newEntry(BibtexEntryType.getType(thisType));
			}
			showJabRefTab();
			if (entry != null) {
				entry.setField("docear_add_to_node", node.getID());
			}			
		}

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
