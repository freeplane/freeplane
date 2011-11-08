package org.docear.plugin.pdfutilities.actions;

import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.docear.plugin.core.actions.SaveAsAction;
import org.docear.plugin.pdfutilities.PdfUtilitiesController;
import org.docear.plugin.pdfutilities.util.NodeUtils;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

@EnabledAction( checkOnNodeChange = true )
public class AddMonitoringFolderAction extends AbstractMonitoringAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;



	public AddMonitoringFolderAction(String key) {
		super(key);		
	}

	public void actionPerformed(ActionEvent e) {
		NodeModel selected = Controller.getCurrentController().getSelection().getSelected();
		if(Controller.getCurrentController().getMap().getFile() == null){
			int result = UITools.showConfirmDialog(selected, "You have to save the map first.", "Save Map", JOptionPane.OK_CANCEL_OPTION);
			if(result == JOptionPane.OK_OPTION){
				SaveAsAction saveAction = new SaveAsAction();
				saveAction.actionPerformed(null);
				/*final boolean savingNotCancelled = ((MFileManager) UrlManager.getController()).save(Controller.getCurrentController().getMap());
					if (!savingNotCancelled) {
						return;
					}*/
			}
			else{
				return;
			}
		}
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setDialogTitle(TextUtils.getText("AddMonitoringFolderAction_dialog_title")); //$NON-NLS-1$
		int result = fileChooser.showOpenDialog(Controller.getCurrentController().getViewController().getJFrame());
        if(result == JFileChooser.APPROVE_OPTION){
        	URI pdfDir = MLinkController.toLinkTypeDependantURI(Controller.getCurrentController().getMap().getFile(), fileChooser.getSelectedFile());
        	fileChooser.setDialogTitle(TextUtils.getText("AddMonitoringFolderAction_dialog_title_mindmaps")); //$NON-NLS-1$
        	result = fileChooser.showOpenDialog(Controller.getCurrentController().getViewController().getJFrame());
        	if(result == JFileChooser.APPROVE_OPTION){
        		URI mindmapDir = MLinkController.toLinkTypeDependantURI(Controller.getCurrentController().getMap().getFile(), fileChooser.getSelectedFile());
        		
        		NodeUtils.addMonitoringDir(selected, pdfDir);
        		NodeUtils.addMindmapDir(selected, mindmapDir);
        		NodeUtils.setAttributeValue(selected, PdfUtilitiesController.MON_AUTO, 2);
        		NodeUtils.setAttributeValue(selected, PdfUtilitiesController.MON_SUBDIRS, 2);
        		List<NodeModel> list = new ArrayList<NodeModel>();
        		list.add(Controller.getCurrentController().getSelection().getSelected());	
        		AddMonitoringFolderAction.updateNodesAgainstMonitoringDir(list, true);
        	}   		
        }	
		
	}	

	@Override
	public void setEnabled(){
		NodeModel selected = Controller.getCurrentController().getSelection().getSelected();
		if(selected == null){
			this.setEnabled(false);
		}
		else{
			this.setEnabled(!NodeUtils.isMonitoringNode(selected));
		}
	}

}
