package org.docear.plugin.core.listeners;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import org.apache.commons.io.FileUtils;
import org.docear.plugin.core.mindmap.MindmapFileLinkUpdater;
import org.docear.plugin.core.mindmap.MindmapUpdateController;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.model.WorkspaceTreeModelEvent;
import org.freeplane.plugin.workspace.model.WorkspaceTreeModelEvent.WorkspaceTreeModelEventType;
import org.freeplane.plugin.workspace.nodes.DefaultFileNode;
import org.freeplane.plugin.workspace.nodes.LinkTypeFileNode;

public class DocearWorkspaceTreeModelListener implements TreeModelListener {

	public void treeNodesChanged(TreeModelEvent e) {
		if(e instanceof WorkspaceTreeModelEvent && ((WorkspaceTreeModelEvent) e).getType() == WorkspaceTreeModelEventType.rename){
			WorkspaceTreeModelEvent event =  (WorkspaceTreeModelEvent) e;
			if(event.getTreePath().getLastPathComponent() instanceof DefaultFileNode){
				updateMaps(event);
			}
		}
	}

	public void treeNodesInserted(TreeModelEvent e) {
		// TODO Auto-generated method stub

	}

	public void treeNodesRemoved(TreeModelEvent e) {
		// TODO Auto-generated method stub

	}

	public void treeStructureChanged(TreeModelEvent e) {
		if(e instanceof WorkspaceTreeModelEvent && ((WorkspaceTreeModelEvent) e).getType() == WorkspaceTreeModelEventType.move){
			WorkspaceTreeModelEvent event =  (WorkspaceTreeModelEvent) e;
			if(event.getTreePath().getLastPathComponent() instanceof DefaultFileNode || event.getTreePath().getLastPathComponent() instanceof LinkTypeFileNode){
				updateMaps(event);
			}
		}
	}
	
	private void updateMaps(WorkspaceTreeModelEvent event) {
		Map<File, File> fileMap = new HashMap<File, File>();
		if(!((File)event.getTo()).isDirectory()){				
			File oldFile = (File) event.getFrom();
			File newFile = (File) event.getTo();			
			fileMap.put(oldFile, newFile);			
		}
		else{
			File oldFile = (File) event.getFrom();
			File newFile = (File) event.getTo();
			Collection<File> files = FileUtils.listFiles(newFile, null, true);			
			for(File file : files){
				String oldPath = file.getPath().replace(newFile.getPath(), oldFile.getPath());
				fileMap.put(new File(oldPath), file);				
			}			
		}
		updateMaps(event, fileMap);
	}

	private void updateMaps(WorkspaceTreeModelEvent event,	Map<File, File> fileMap) {
		MindmapUpdateController mindmapUpdateController = new MindmapUpdateController(false);
		mindmapUpdateController.addMindmapUpdater(new MindmapFileLinkUpdater(TextUtils.getText("updating_links"), event, fileMap));
		mindmapUpdateController.updateAllMindmapsInWorkspace();
	}
}
