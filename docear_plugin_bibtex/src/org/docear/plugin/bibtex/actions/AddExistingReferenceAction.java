package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.Collection;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.docear.plugin.bibtex.dialogs.ExistingReferencesDialog;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspaceUtils;

public class AddExistingReferenceAction extends AFreeplaneAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AddExistingReferenceAction(String key) {
		super(key);
	}

	public void actionPerformed(ActionEvent arg0) {
		Collection<NodeModel> nodes = Controller.getCurrentModeController().getMapController().getSelectedNodes();
		URI link = null;
		String name = null;
		// check for conflicting file links (two nodes linking to at least two distinct files)
		for (NodeModel node : nodes) {
			try {
				URI tempLink = NodeLinks.getLink(node);
				String tempName = WorkspaceUtils.resolveURI(tempLink, node.getMap()).getName();

				if (link == null) {
					link = tempLink;
					name = tempName;
				}
				
				if (!tempName.equals(name)) {
					int yesOrNo = JOptionPane.showConfirmDialog(UITools.getFrame(), TextUtils.getText("docear.add_existing_reference.error.conflicting_pdf_files"), TextUtils.getText("docear.add_existing_reference.error.title"),
							JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					
					
					if (yesOrNo == JOptionPane.YES_OPTION) {
						link = null;
						break;
					}					
					else {
						return;
					}
				}
			}
			catch (NullPointerException ex) {
			}
		}

		ExistingReferencesDialog dialog = new ExistingReferencesDialog(Controller.getCurrentController().getViewController().getFrame(), link);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);

	}

}
