package org.docear.plugin.pdfutilities.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import org.docear.plugin.core.mindmap.MindmapFileRemovedUpdater;
import org.docear.plugin.core.mindmap.MindmapUpdateController;
import org.docear.plugin.core.util.Tools;
import org.docear.plugin.pdfutilities.util.MonitoringUtils;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;

@EnabledAction( checkOnPopup = true, checkOnNodeChange = true )
public class DeleteFileAction extends DocearAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DeleteFileAction() {
		super("menu_delete_file");
	}

	public void actionPerformed(ActionEvent event) {
		Set<NodeModel> selection = Controller.getCurrentController().getSelection().getSelection();
		if(selection == null){
			return;
		}
		
		Set<File> deletedFiles = new HashSet<File>();
		for (NodeModel node : selection) {
			URI uri = Tools.getAbsoluteUri(node);
			if (uri == null) {
				continue;
			}
			
			File file = WorkspaceUtils.resolveURI(uri, node.getMap());
			if(!file.delete()){
				JOptionPane.showMessageDialog(Controller.getCurrentController().getViewController().getJFrame(), TextUtils.getText("DeleteFileAction.DeleteFailed.Message"), TextUtils.getText("DeleteFileAction.DeleteFailed.Title"), JOptionPane.WARNING_MESSAGE);
				return;
			}			
			deletedFiles.add(file);			
		}
				
		MindmapUpdateController ctrl = new MindmapUpdateController();
		ctrl.addMindmapUpdater(new MindmapFileRemovedUpdater(TextUtils.getText("docear.mm_updater.remove_filelinks"), deletedFiles));
		ctrl.updateRegisteredMindmapsInWorkspace(true);
		//TODO: update mindmap
		
		WorkspaceController.getController().refreshWorkspace();
		
		
		//TODO: only show action in menu, if the node links to a pdf file
		

	}
	
	@Override
	public void setEnabled(){
		if(Controller.getCurrentController().getSelection() == null) {
			this.setEnabled(false);
			return;
		}
		Set<NodeModel> selection = Controller.getCurrentController().getSelection().getSelection();
		if(selection == null){
			this.setEnabled(false);			
		}
		else{
			for(NodeModel selected : selection){
				if(MonitoringUtils.isPdfLinkedNode(selected)){
					this.setEnabled(true);
					return;
				}
			}
			this.setEnabled(false);
		}
	}

}
