package org.docear.plugin.pdfutilities.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import org.docear.plugin.pdfutilities.util.NodeUtils;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

@EnabledAction( checkOnPopup = true )
public class UpdateMonitoringFolderAction extends AbstractMonitoringAction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UpdateMonitoringFolderAction(String key) {
		super(key);		
	}

	public void actionPerformed(ActionEvent e) {
		List<NodeModel> list = new ArrayList<NodeModel>();
		list.add(Controller.getCurrentController().getSelection().getSelected());		
		UpdateMonitoringFolderAction.updateNodesAgainstMonitoringDir(list, true);
	}

	@Override
	public void setEnabled() {
		if(Controller.getCurrentController().getSelection() == null) {
			this.setEnabled(false);
			return;
		}
		NodeModel selected = Controller.getCurrentController().getSelection().getSelected();
		if(selected == null){
			this.setEnabled(false);
		}
		else{
			this.setEnabled(NodeUtils.isMonitoringNode(selected));
		}
	}

}
