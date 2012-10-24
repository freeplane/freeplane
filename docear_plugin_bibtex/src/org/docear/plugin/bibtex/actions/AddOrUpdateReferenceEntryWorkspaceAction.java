package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import org.docear.plugin.bibtex.ReferencesController;
import org.docear.plugin.bibtex.jabref.JabRefCommons;
import org.docear.plugin.bibtex.jabref.JabrefWrapper;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.actions.AWorkspaceAction;
import org.freeplane.plugin.workspace.io.IFileSystemRepresentation;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.nodes.LinkTypeFileNode;

@EnabledAction(checkOnPopup = true)
public class AddOrUpdateReferenceEntryWorkspaceAction extends AWorkspaceAction {
	public static final String KEY = "workspace.action.addOrUpdateReferenceEntry";

	public AddOrUpdateReferenceEntryWorkspaceAction() {
		super(KEY);
	}
	private static final long serialVersionUID = 1L;

	public void setEnabledFor(AWorkspaceTreeNode node) {
		File file = null;
		if(node instanceof IFileSystemRepresentation) {
			file = ((IFileSystemRepresentation) node).getFile();
		}
		else {
			if(node instanceof LinkTypeFileNode) {				
				file = WorkspaceUtils.resolveURI(((LinkTypeFileNode) node).getLinkPath());
			}
		}
		
		if(file == null || !file.getName().toLowerCase().endsWith(".pdf") /*|| (AnnotationController.getDocumentHash(file.toURI()) == null)*/) {
			setEnabled(false);
			return;
		}
		
		super.setEnabledFor(node);
	}
	
	public void actionPerformed(ActionEvent e) {
		JabrefWrapper jabrefWrapper = ReferencesController.getController().getJabrefWrapper();
		AWorkspaceTreeNode node = getNodeFromActionEvent(e);
		File file = null;
		if(node instanceof IFileSystemRepresentation) {
			file = ((IFileSystemRepresentation) node).getFile();
		}
		else {
			if(node instanceof LinkTypeFileNode) {				
				file = WorkspaceUtils.resolveURI(((LinkTypeFileNode) node).getLinkPath());
			}
		}
		
		if(jabrefWrapper != null && file != null) {
			JabRefCommons.addNewRefenceEntry(new String[] { file.getPath() }, jabrefWrapper.getJabrefFrame(), jabrefWrapper.getJabrefFrame().basePanel());
		}

	}

}
