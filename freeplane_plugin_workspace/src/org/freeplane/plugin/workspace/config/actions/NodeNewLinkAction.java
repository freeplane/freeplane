/**
 * author: Marcel Genzmehr
 * 11.11.2011
 */
package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.List;

import org.freeplane.plugin.workspace.WorkspaceUtils;

/**
 * 
 */
public class NodeNewLinkAction extends AWorkspaceAction {

	private static final long serialVersionUID = -2738773226743524919L;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public NodeNewLinkAction() {
		super("workspace.action.node.new.link");
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	public void actionPerformed(ActionEvent e) {
		List<URI> mindmaps = WorkspaceUtils.getModel().getAllNodesFiltered(".mm");
		for(URI uri : mindmaps) {
			System.out.println(uri);
		}
//		JFileChooser chooser = new JFileChooser(WorkspaceUtils.getWorkspaceBaseFile());
//		chooser.setMultiSelectionEnabled(false);
//		chooser.setVisible(true);
//		URI path = chooser.getSelectedFile().toURI().relativize(WorkspaceUtils.getWorkspaceBaseURI());
//		if (path == null) {
//			return;
//		}	
//		node.setLinkPath(path); 		
//		String name = WorkspaceUtils.resolveURI(node.getLinkPath()).getName();
//		node.setName(name);
//		
//		WorkspaceUtils.getModel().addNodeTo(node, getNodeFromActionEvent(e));
//		WorkspaceUtils.getModel().reload(getNodeFromActionEvent(e));
//		WorkspaceUtils.saveCurrentConfiguration();

	}
}
