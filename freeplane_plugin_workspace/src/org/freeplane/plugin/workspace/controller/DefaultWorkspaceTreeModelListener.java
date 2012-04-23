package org.freeplane.plugin.workspace.controller;

import java.io.File;
import java.net.MalformedURLException;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.UrlManager;
import org.freeplane.plugin.workspace.model.WorkspaceTreeModelEvent;
import org.freeplane.plugin.workspace.model.WorkspaceTreeModelEvent.WorkspaceTreeModelEventType;
import org.freeplane.plugin.workspace.nodes.DefaultFileNode;
import org.freeplane.plugin.workspace.nodes.LinkTypeFileNode;

public class DefaultWorkspaceTreeModelListener implements TreeModelListener {

	public void treeNodesChanged(TreeModelEvent e) {
		if(e instanceof WorkspaceTreeModelEvent && ((WorkspaceTreeModelEvent) e).getType() == WorkspaceTreeModelEventType.rename){
			WorkspaceTreeModelEvent event =  (WorkspaceTreeModelEvent) e;
			if(event.getTreePath().getLastPathComponent() instanceof DefaultFileNode){
				updateOpenedMaps(event);
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
				updateOpenedMaps(event);
			}
		}

	}
	
	private void updateOpenedMaps(WorkspaceTreeModelEvent event) {
		if(event.getFrom().toString().toLowerCase().endsWith(".mm")){				
			File oldFile = (File) event.getFrom();
			File newFile = (File) event.getTo();
			String mapExtensionKey = null;
			try {
				mapExtensionKey = Controller.getCurrentController().getMapViewManager().checkIfFileIsAlreadyOpened(oldFile.toURL());
			} catch (MalformedURLException ex) {
				LogUtils.warn(ex);
			}
			if(mapExtensionKey != null){
				MapModel map = Controller.getCurrentController().getMapViewManager().getMaps().get(mapExtensionKey);
				if(map != null){
					try {
						boolean isSaved = map.isSaved();
						map.setURL(newFile.toURL());						
						MapChangeEvent mapChangeEvent = new MapChangeEvent(this, map, UrlManager.MAP_URL, oldFile.toURL(), newFile.toURL());								
						Controller.getCurrentModeController().getMapController().fireMapChanged(mapChangeEvent);
						if(event.getType() == WorkspaceTreeModelEventType.rename){
							Controller.getCurrentController().getMapViewManager().updateMapViewName();
						}
						map.setSaved(isSaved);
						Controller.getCurrentController().getViewController().setTitle();								
					} catch (MalformedURLException ex) {
						LogUtils.warn(ex);
					}							
				}
			}
		}
	}

}
