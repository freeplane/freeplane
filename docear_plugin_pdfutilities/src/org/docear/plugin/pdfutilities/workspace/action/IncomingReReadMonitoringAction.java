package org.docear.plugin.pdfutilities.workspace.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;
import java.util.Map;

import org.docear.plugin.core.workspace.node.LinkTypeIncomingNode;
import org.docear.plugin.pdfutilities.actions.UpdateMonitoringFolderAction;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mapio.MapIO;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.actions.AWorkspaceAction;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

public class IncomingReReadMonitoringAction extends AWorkspaceAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public IncomingReReadMonitoringAction() {
		super("workspace.action.docear.incoming.reread_monitored");		
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
    			if (path.equals(map.getFile().getAbsolutePath())) {
    				incoming = map;
    				break;
    			}
    		}
    		
    		if (incoming == null) {
        		MapIO mapIo = Controller.getCurrentController().getModeController(MModeController.MODENAME).getExtension(MapIO.class);
        		try {
    				mapIo.newMap(uri.toURL());
    			}
        		catch (Exception e1) {
    				LogUtils.warn(e1);
    			}
        		
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
