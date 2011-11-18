package org.docear.plugin.pdfutilities.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import org.docear.plugin.core.features.DocearNodeModelExtension.DocearExtensionKey;
import org.docear.plugin.core.features.DocearNodeModelExtensionController;
import org.docear.plugin.core.util.Tools;
import org.docear.plugin.pdfutilities.PdfUtilitiesController;
import org.docear.plugin.pdfutilities.util.NodeUtils;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;

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
			NodeUtils.setAttributeValue(selected, PdfUtilitiesController.MON_FLATTEN_DIRS, 1);			
		}
		else{
			URI pdfDirURI = NodeUtils.getPdfDirFromMonitoringNode(selected);
			pdfDirURI = Tools.getAbsoluteUri(pdfDirURI);
			if(pdfDirURI == null || Tools.getFilefromUri(pdfDirURI) == null || !Tools.getFilefromUri(pdfDirURI).exists() || !Tools.getFilefromUri(pdfDirURI).isDirectory()){
				return;
			}
			File pdfDirFile = Tools.getFilefromUri(pdfDirURI);
			Map<NodeModel, Stack<File>> result = new HashMap<NodeModel, Stack<File>>();
			for(NodeModel node : selected.getChildren()){
				URI uri = Tools.getAbsoluteUri(node);
				if(uri == null || !Tools.getFilefromUri(uri).exists() || !Tools.getFilefromUri(uri).isFile() || !Tools.getFilefromUri(uri).getAbsolutePath().startsWith(pdfDirFile.getAbsolutePath())){
					continue;
				}
				Stack<File> folderStack = new Stack<File>();
				File parent = Tools.getFilefromUri(uri).getParentFile();
				while(parent != null && !parent.equals(pdfDirFile)){
					folderStack.push(parent);
					parent = parent.getParentFile();
				}
				if(!folderStack.isEmpty()){
					result.put(node, folderStack);
				}					
			}
			for(Entry<NodeModel, Stack<File>> entry : result.entrySet()){
				pasteNodeIntoPath(entry.getKey(), selected, entry.getValue());				
			}
			NodeUtils.setAttributeValue(selected, PdfUtilitiesController.MON_FLATTEN_DIRS, 0);			
		}		
	}
	
	private void pasteNodeIntoPath(NodeModel node, NodeModel target, Stack<File> pathStack) {		
		if(pathStack.isEmpty()){			
			((MMapController) Controller.getCurrentModeController().getMapController()).moveNode(node, target, target.getChildCount());
			return;
		}
		File parent = pathStack.pop();
		NodeModel pathNode = null;
		for(NodeModel child : target.getChildren()){
			if(child.getText().equals(parent.getName())){
				pathNode = child;
				break;
			}
		}
		if(pathNode != null){
			pasteNodeIntoPath(node, pathNode, pathStack);
		}
		else{
			pathNode = ((MMapController) Controller.getCurrentModeController().getMapController()).newNode(parent.getName(), target.getMap());
			DocearNodeModelExtensionController.setEntry(pathNode, DocearExtensionKey.MONITOR_PATH.toString(), null);			
			NodeUtils.insertChildNodeFrom(pathNode, target.isLeft(), target);
			pasteNodeIntoPath(node, pathNode, pathStack);
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
