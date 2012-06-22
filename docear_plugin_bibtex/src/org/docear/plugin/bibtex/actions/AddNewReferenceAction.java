package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import net.sf.jabref.BasePanel;
import net.sf.jabref.BibtexEntry;
import net.sf.jabref.BibtexEntryType;
import net.sf.jabref.EntryTypeDialog;
import net.sf.jabref.export.DocearSaveDatabaseAction;

import org.docear.plugin.bibtex.ReferencesController;
import org.docear.plugin.bibtex.jabref.JabrefWrapper;
import org.docear.plugin.core.util.CoreUtils;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspaceUtils;

import spl.PdfImporter;

public class AddNewReferenceAction extends AFreeplaneAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AddNewReferenceAction(String key) {
		super(key);
	}

	public void actionPerformed(ActionEvent e) {		
		Collection<NodeModel> nodes = Controller.getCurrentModeController().getMapController().getSelectedNodes();
		if (e.getActionCommand().equals(DocearSaveDatabaseAction.JABREF_DATABASE_SAVE_SUCCESS)) {
			addCreatedReference(e);
			return;
		}
		else if (e.getActionCommand().equals(DocearSaveDatabaseAction.JABREF_DATABASE_SAVE_FAILED)) {
			return;
		}
		else {		
			createNewReference(nodes);
		}

	}

	private void createNewReference(Collection<NodeModel> nodes) {
		BibtexEntry entry;
		URI link = null;
		String name = null;
		for (NodeModel node : nodes) {
			try {
				URI tempLink = NodeLinks.getLink(node);
				String tempName = CoreUtils.resolveURI(tempLink).getName();

				if (link == null) {
					link = tempLink;
					name = tempName;
				}
				
				if (!tempName.equals(name)) {
					JOptionPane.showMessageDialog(UITools.getFrame(), TextUtils.getText("docear.add_new_reference.error.conflicting_pdf_files"), TextUtils.getText("docear.add_new_reference.error.title"),
							JOptionPane.WARNING_MESSAGE);
					return;
				}
			}
			catch (NullPointerException ex) {
			}
		}

		JabrefWrapper jabrefWrapper = ReferencesController.getController().getJabrefWrapper();
		
		MapModel map = Controller.getCurrentController().getMap();
		if (link != null && link.getPath().toLowerCase().endsWith(".pdf")) {				
			String path = WorkspaceUtils.resolveURI(link, map).getAbsolutePath();
			PdfImporter pdfImporter = new PdfImporter(jabrefWrapper.getJabrefFrame(), jabrefWrapper.getJabrefFrame().basePanel(), null, 0);
			pdfImporter.importPdfFiles(new String[] { path }, Controller.getCurrentController().getViewController().getFrame(), true);
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
			String nodeIds = "";
			for (NodeModel node : nodes) {
				nodeIds += node.getID()+",";
			}
			entry.setField("docear_add_to_node", nodeIds);
			ReferencesController.getController().setInAdd(map);
		}
	}

	private void addCreatedReference(ActionEvent e) {
		BibtexEntry entry;
		try {
			entry = (BibtexEntry) e.getSource();
			String s = entry.getField("docear_add_to_node");
			if (s != null) {
				String[] nodeIDs = s.split(",");
				for (String nodeID : nodeIDs) {
					NodeModel node = Controller.getCurrentModeController().getMapController().getNodeFromID(nodeID);
					ReferencesController.getController().getJabRefAttributes().setReferenceToNode(entry, node);
				}
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return;
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
