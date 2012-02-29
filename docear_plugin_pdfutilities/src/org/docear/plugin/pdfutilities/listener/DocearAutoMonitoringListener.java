package org.docear.plugin.pdfutilities.listener;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.docear.plugin.core.mindmap.MapConverter;
import org.docear.plugin.pdfutilities.actions.UpdateMonitoringFolderAction;
import org.docear.plugin.pdfutilities.util.NodeUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.IMapLifeCycleListener;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

public class DocearAutoMonitoringListener implements IMapLifeCycleListener,  WindowFocusListener{
	
	private List<NodeModel> autoMonitorNodes = new ArrayList<NodeModel>();
	private boolean startup = true;
	
	
	public void onCreate(final MapModel map) {
		if(map == null || map.getFile() == null) return;
		autoMonitorNodes.addAll(getAutoMonitorNodes(map.getRootNode()));
		if(!startup){
			SwingUtilities.invokeLater(new Thread() {
				public void run() {
					//FIXME: DOCEAR - Needs to be tested for thread timing problems, see Null-mapView
//					final Thread me = this;
//					if(Controller.getCurrentController().getSelection() == null) {
//						IMapViewChangeListener changeListener = new IMapViewChangeListener() {						
//							public void beforeViewChange(Component oldView, Component newView) {
//							}
//							
//							public void afterViewCreated(Component mapView) {
//								me.interrupt();
//							}
//							
//							public void afterViewClose(Component oldView) {							
//							}
//							
//							public void afterViewChange(Component oldView, Component newView) {
//							}
//						};
//						Controller.getCurrentController().getMapViewManager().addMapViewChangeListener(changeListener);
//						
//						try {
//							LogUtils.info("Monitoring waiting...");
//							this.wait();
//						}
//						catch (InterruptedException e1) {
//						}
//						
//						Controller.getCurrentController().getMapViewManager().removeMapViewChangeListener(changeListener);
//					}
					LogUtils.info("Monitoring started"); //$NON-NLS-1$
					startMonitoring();
			
				} //run()
			}); // Thread
		}
	}

	public void onRemove(MapModel map) {
		
	}

	public void windowGainedFocus(WindowEvent e) {
		if(startup && !MapConverter.currentlyConverting){
			startup = false;
			startMonitoring();			
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

	public void onSaved(MapModel map) {
		
	}

	private synchronized void startMonitoring() {
		if(autoMonitorNodes.size() > 0){
			UpdateMonitoringFolderAction.updateNodesAgainstMonitoringDir(autoMonitorNodes, !startup);
			autoMonitorNodes.clear();
		}		
	}

}
