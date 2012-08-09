package org.docear.plugin.pdfutilities.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.logger.DocearLogEvent;
import org.docear.plugin.pdfutilities.util.MonitoringUtils;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspaceUtils;

@EnabledAction( checkOnPopup = true )
public class DeleteMonitoringFolderAction extends AbstractMonitoringAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DeleteMonitoringFolderAction(String key) {
		super(key);		
	}

	public void actionPerformed(ActionEvent e) {
	    for (NodeModel selected : Controller.getCurrentModeController().getMapController().getSelectedNodes()) {
		URI uri = MonitoringUtils.getPdfDirFromMonitoringNode(selected);
		File folder = null;
		if (uri != null) {
			folder = WorkspaceUtils.resolveURI(uri);
		}
		
		MonitoringUtils.removeMonitoringEntries(selected);
		DocearController.getController().getDocearEventLogger().appendToLog(this, DocearLogEvent.MONITORING_FOLDER_REMOVE, folder);
	    }		
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
			this.setEnabled(MonitoringUtils.isMonitoringNode(selected));
		}
	}

}
