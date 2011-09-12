package org.docear.plugin.pdfutilities.actions;

import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;

import org.docear.plugin.pdfutilities.PdfUtilitiesController;
import org.docear.plugin.pdfutilities.util.NodeUtils;
import org.docear.plugin.pdfutilities.util.Tools;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

@EnabledAction( checkOnNodeChange = true )
public class EditMonitoringFolderAction extends AbstractMonitoringAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public EditMonitoringFolderAction(String key) {
		super(key);		
	}

	public void actionPerformed(ActionEvent e) {
		JFileChooser fileChooser;
		Object value = NodeUtils.getAttributeValue(Controller.getCurrentController().getSelection().getSelected(), PdfUtilitiesController.MON_INCOMING_FOLDER);
		if(value != null){
			fileChooser = new JFileChooser(Tools.getFilefromUri(Tools.getAbsoluteUri((URI)value)));
		}
		else{
			fileChooser = new JFileChooser();
		}		
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setDialogTitle(TextUtils.getText("AddMonitoringFolderAction_dialog_title")); //$NON-NLS-1$
		int result = fileChooser.showOpenDialog(Controller.getCurrentController().getViewController().getJFrame());
        if(result == JFileChooser.APPROVE_OPTION){
        	URI pdfDir = MLinkController.toLinkTypeDependantURI(Controller.getCurrentController().getMap().getFile(), fileChooser.getSelectedFile());
        	fileChooser.setDialogTitle(TextUtils.getText("AddMonitoringFolderAction_dialog_title_mindmaps")); //$NON-NLS-1$
        	result = fileChooser.showOpenDialog(Controller.getCurrentController().getViewController().getJFrame());
        	if(result == JFileChooser.APPROVE_OPTION){
        		URI mindmapDir = MLinkController.toLinkTypeDependantURI(Controller.getCurrentController().getMap().getFile(), fileChooser.getSelectedFile());
        		NodeModel selected = Controller.getCurrentController().getSelection().getSelected();
        		NodeUtils.setAttributeValue(selected, PdfUtilitiesController.MON_INCOMING_FOLDER, pdfDir);
        		NodeUtils.setAttributeValue(selected, PdfUtilitiesController.MON_MINDMAP_FOLDER, mindmapDir);
        		List<NodeModel> list = new ArrayList<NodeModel>();
        		list.add(Controller.getCurrentController().getSelection().getSelected());	
        		AddMonitoringFolderAction.updateNodesAgainstMonitoringDir(list, true);
        	}   		
        }
	}

	@Override
	public void setEnabled() {
		NodeModel selected = Controller.getCurrentController().getSelection().getSelected();
		if(selected == null){
			this.setEnabled(false);
		}
		else{
			this.setEnabled(NodeUtils.isMonitoringNode(selected));
		}
	}

}
