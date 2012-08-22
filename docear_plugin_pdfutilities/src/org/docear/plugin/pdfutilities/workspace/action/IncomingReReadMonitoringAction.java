package org.docear.plugin.pdfutilities.workspace.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.docear.plugin.core.workspace.node.LinkTypeIncomingNode;
import org.docear.plugin.pdfutilities.PdfUtilitiesController;
import org.docear.plugin.pdfutilities.actions.UpdateMonitoringFolderAction;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mapio.MapIO;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.actions.AWorkspaceAction;
import org.freeplane.plugin.workspace.event.WorkspaceActionEvent;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

public class IncomingReReadMonitoringAction extends AWorkspaceAction {

	private static final Icon icon;
	
	static {
		icon = new ImageIcon(PdfUtilitiesController.class.getResource("/icons/view-refresh-3.png"));
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public IncomingReReadMonitoringAction() {
		super("workspace.action.docear.incoming.reread_monitored", TextUtils.getRawText("workspace.action.docear.incoming.reread_monitored.label"), icon);		
	}

	public void actionPerformed(ActionEvent e) {
		ModeController oldController = Controller.getCurrentModeController();
		Controller.getCurrentController().selectMode(MModeController.MODENAME);
		
		try {
    		AWorkspaceTreeNode node = getNodeFromActionEvent(e);
    		if (!(node instanceof LinkTypeIncomingNode)) {
    			return;
    		}
    		
    		URI uri = ((LinkTypeIncomingNode) node).getLinkPath();
    		File f = WorkspaceUtils.resolveURI(uri);
    		if (f == null) {
    			return;
    		}
    		String path = f.getAbsolutePath();
    		
    		MapModel incoming = null;
    		
    		Map<String, MapModel> openMaps = Controller.getCurrentController().getMapViewManager().getMaps();
    		for (MapModel map : openMaps.values()) {    			
    			if (map.getFile() != null && path.equals(map.getFile().getAbsolutePath())) {
    				incoming = map;
    				break;
    			}
    		}
    		
    		if (incoming == null) {
    			((LinkTypeIncomingNode) node).handleAction(new WorkspaceActionEvent(node, WorkspaceActionEvent.WSNODE_OPEN_DOCUMENT, null));
    			incoming = Controller.getCurrentController().getMap();
    		}
    		
    		updateMonitoringFolder(incoming);
    		
		}
		finally {
			Controller.getCurrentController().selectMode(oldController);
		}
	}
	
	private void updateMonitoringFolder(MapModel map) {
		if (map != null) {
			UpdateMonitoringFolderAction.updateNodesAgainstMonitoringDir(map.getRootNode(), true);
//			Controller.getCurrentController().getAction(key)
		}
	}

}
