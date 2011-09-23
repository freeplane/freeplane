package org.docear.plugin.pdfutilities.listener;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;
import java.util.List;

import org.docear.plugin.pdfutilities.actions.UpdateMonitoringFolderAction;
import org.docear.plugin.pdfutilities.util.NodeUtils;
import org.freeplane.features.map.IMapLifeCycleListener;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

public class DocearAutoMonitoringListener implements IMapLifeCycleListener,  WindowFocusListener{
	
	private List<NodeModel> autoMonitorNodes = new ArrayList<NodeModel>();
	private boolean startup = true;
	
	
	public void onCreate(MapModel map) {
		if(map == null || map.getFile() == null) return;
		autoMonitorNodes.addAll(getAutoMonitorNodes(map.getRootNode()));	
		if(!this.startup){
			if(this.autoMonitorNodes.size() > 0){
				UpdateMonitoringFolderAction.updateNodesAgainstMonitoringDir(autoMonitorNodes, !this.startup);
				autoMonitorNodes.clear();
			}
		}
	}

	public void onRemove(MapModel map) {
		
	}

	public void windowGainedFocus(WindowEvent e) {
		if(startup && !DocearMapConverterListener.currentlyConverting){
			startup = false;
			if(this.autoMonitorNodes.size() > 0){				
				UpdateMonitoringFolderAction.updateNodesAgainstMonitoringDir(autoMonitorNodes, !this.startup);
				autoMonitorNodes.clear();
			}
			
		}
	}

	public void windowLostFocus(WindowEvent e) {
	
	}
	
	private List<? extends NodeModel> getAutoMonitorNodes(NodeModel node) {
		List<NodeModel> result = new ArrayList<NodeModel>();
		if(NodeUtils.isAutoMonitorNode(node)){
			result.add(node);
		}
		for(NodeModel child : node.getChildren()){
			result.addAll(getAutoMonitorNodes(child));
		}
		return result;
	}

	public void onSavedAs(MapModel map) {		
		
	}

}
