package org.docear.plugin.pdfutilities.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import javax.swing.SwingUtilities;

import org.docear.plugin.core.features.DocearNodeModelExtension.DocearExtensionKey;
import org.docear.plugin.core.features.DocearNodeModelExtensionController;
import org.docear.plugin.core.ui.SwingWorkerDialog;
import org.docear.plugin.core.ui.SwingWorkerDialogLite;
import org.docear.plugin.core.util.Tools;
import org.docear.plugin.pdfutilities.PdfUtilitiesController;
import org.docear.plugin.pdfutilities.util.NodeUtils;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;
import org.jdesktop.swingworker.SwingWorker;

@EnabledAction( checkOnNodeChange = true )
public class MonitoringFlattenSubfoldersAction extends DocearAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public MonitoringFlattenSubfoldersAction(String key) {
		super(key);		
	}

	public void actionPerformed(ActionEvent arg0) {
		NodeModel selected = Controller.getCurrentController().getSelection().getSelected();
		Object value = NodeUtils.getAttributeValue(selected, PdfUtilitiesController.MON_FLATTEN_DIRS);
		if(value == null || (Integer)value == 0){			
			boolean isFolded = selected.isFolded();
			selected.setFolded(true);
			SwingWorker<Void, Void> thread = getFlattenThread(selected);
			SwingWorkerDialogLite dialog = new SwingWorkerDialogLite(Controller.getCurrentController().getViewController().getFrame());
			dialog.setHeadlineText("Flatten subfolders...");
			dialog.showDialog(thread);
			NodeUtils.setAttributeValue(selected, PdfUtilitiesController.MON_FLATTEN_DIRS, 1);				
			selected.setFolded(isFolded);
		}
		else{			
			boolean isFolded = selected.isFolded();
			selected.setFolded(true);
			SwingWorker<Void, Void> thread = getUnFlattenThread(selected);
			SwingWorkerDialogLite dialog = new SwingWorkerDialogLite(Controller.getCurrentController().getViewController().getFrame());
			dialog.setHeadlineText("Creating subfolder nodes...");
			dialog.showDialog(thread);
			NodeUtils.setAttributeValue(selected, PdfUtilitiesController.MON_FLATTEN_DIRS, 0);
			selected.setFolded(isFolded);			
		}		
	}
	
		

	private SwingWorker<Void, Void> getFlattenThread(final NodeModel selected) {
		return new SwingWorker<Void, Void>(){

			@Override
			protected Void doInBackground() throws Exception {
				fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_INDETERMINATE, null, null);				
				flattenMonitorNodes(selected, selected.getChildren());				
				removePathNodes(selected);				
				return null;
			}
			
			@Override
		    protected void done() {			
				if(this.isCancelled() || Thread.currentThread().isInterrupted()){					
					this.firePropertyChange(SwingWorkerDialog.IS_DONE, null, "Flatten subfolders canceled.");
				}
				else{
					this.firePropertyChange(SwingWorkerDialog.IS_DONE, null, "Flatten subfolders complete.");
				}
				
			}			
		};		
	}
	
	private SwingWorker<Void, Void> getUnFlattenThread(final NodeModel selected) {
		return new SwingWorker<Void, Void>(){

			@Override
			protected Void doInBackground() throws Exception {
				fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_INDETERMINATE, null, null);				
				Map<NodeModel, Stack<File>> result = new HashMap<NodeModel, Stack<File>>();
				for(NodeModel node : selected.getChildren()){					
					URI uri = Tools.getAbsoluteUri(node);
					if(uri == null || Tools.getFilefromUri(uri) == null || !Tools.getFilefromUri(uri).exists() || !Tools.getFilefromUri(uri).isFile()){
						continue;
					}
					Stack<File> folderStack = NodeUtils.getFolderStructureStack(selected, uri);				
					if(!folderStack.isEmpty()){
						result.put(node, folderStack);
					}					
				}
				for(final Entry<NodeModel, Stack<File>> entry : result.entrySet()){	
					SwingUtilities.invokeAndWait(
					        new Runnable() {
					            public void run(){					            	
					            	NodeModel target = NodeUtils.createFolderStructurePath(selected, entry.getValue());
									((MMapController) Controller.getCurrentModeController().getMapController()).moveNode(entry.getKey(), target, target.getChildCount());															
					            }
					        }
					   );
					
				}
				return null;
			}
			
			@Override
		    protected void done() {			
				if(this.isCancelled() || Thread.currentThread().isInterrupted()){					
					this.firePropertyChange(SwingWorkerDialog.IS_DONE, null, "Creating subfolder nodes canceled.");
				}
				else{
					this.firePropertyChange(SwingWorkerDialog.IS_DONE, null, "Creating subfolder nodes complete.");
				}
				
			}			
		};		
	}
	
	private void fireStatusUpdate(final String propertyName, final Object oldValue, final Object newValue) throws InterruptedException, InvocationTargetException{				
		SwingUtilities.invokeAndWait(
		        new Runnable() {
		            public void run(){
		            	firePropertyChange(propertyName, oldValue, newValue);										
		            }
		        }
		   );	
	}

	private void removePathNodes(NodeModel selected) throws InterruptedException, InvocationTargetException {
		List<NodeModel> pathNodes = new ArrayList<NodeModel>();
		for(NodeModel node : selected.getChildren()){
			if(DocearNodeModelExtensionController.containsKey(node, DocearExtensionKey.MONITOR_PATH)){
				pathNodes.add(node);
			}
		}
		for(final NodeModel node : pathNodes){
			SwingUtilities.invokeAndWait(
				new Runnable() {
		            public void run(){							            	
						node.removeFromParent();						            											
		            }
		        }        
			);	
		}
	}
	
	private void flattenMonitorNodes(final NodeModel rootTarget, List<NodeModel> children) throws InterruptedException, InvocationTargetException {
		List<NodeModel> pathNodes = new ArrayList<NodeModel>();
		List<NodeModel> monitorNodes = new ArrayList<NodeModel>();
		for(NodeModel node : children){
			if(DocearNodeModelExtensionController.containsKey(node, DocearExtensionKey.MONITOR_PATH)){
				pathNodes.add(node);
			}
			else{
				monitorNodes.add(node);
			}
		}
		for(final NodeModel node : monitorNodes){
			if(node.getParentNode() != rootTarget){
				SwingUtilities.invokeAndWait(
						new Runnable() {
				            public void run(){							            	
				            	((MMapController) Controller.getCurrentModeController().getMapController()).moveNode(node, rootTarget, rootTarget.getChildCount());	            											
				            }
				        }        
					);
				
			}
		}
		for(NodeModel node : pathNodes){
			flattenMonitorNodes(rootTarget, node.getChildren());
		}
	}	

	@Override
	public void setEnabled(){
		NodeModel selected = Controller.getCurrentController().getSelection().getSelected();
		if(selected == null){
			this.setEnabled(false);
		}
		else{
			this.setEnabled(NodeUtils.isMonitoringNode(selected));
		}
		Object value = NodeUtils.getAttributeValue(selected, PdfUtilitiesController.MON_FLATTEN_DIRS);
		if(value == null || (Integer)value == 0){			
			this.setSelected(false);
		}
		else{			
			this.setSelected(true);
		}
	}

}
