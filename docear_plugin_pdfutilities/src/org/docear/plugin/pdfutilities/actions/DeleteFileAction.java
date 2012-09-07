package org.docear.plugin.pdfutilities.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.docear.plugin.core.mindmap.MindmapFileRemovedUpdater;
import org.docear.plugin.core.mindmap.MindmapUpdateController;
import org.docear.plugin.core.util.Tools;
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
		
		Set<String> deletedFiles = new HashSet<String>();
		for (NodeModel node : selection) {
			URI uri = Tools.getAbsoluteUri(node);
			if (uri == null) {
				continue;
			}
			
			File file = WorkspaceUtils.resolveURI(uri, node.getMap());
			file.delete();
			
			deletedFiles.add(file.getAbsolutePath());			
		}
				
		MindmapUpdateController ctrl = new MindmapUpdateController();
		ctrl.addMindmapUpdater(new MindmapFileRemovedUpdater(TextUtils.getText("docear.mm_updater.remove_filelinks"), deletedFiles));
		ctrl.updateRegisteredMindmapsInWorkspace(true);
		//TODO: update mindmap
		
		WorkspaceController.getController().refreshWorkspace();
		
		
		//TODO: only show action in menu, if the node links to a pdf file
		

	}

}
