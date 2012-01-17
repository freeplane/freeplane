package org.freeplane.plugin.workspace.io.action;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mapio.mindmapmode.MMapIO;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.url.mindmapmode.MFileManager;
import org.freeplane.plugin.workspace.io.IFileSystemRepresentation;
import org.freeplane.plugin.workspace.io.node.DefaultFileNode;
import org.freeplane.plugin.workspace.model.action.AWorkspaceAction;
import org.freeplane.plugin.workspace.model.node.AWorkspaceTreeNode;

public class FileNodeNewMindmapAction extends AWorkspaceAction {
	
	private static final long serialVersionUID = 1L;
	
	private static final Icon icon;
	
	static {
		icon = (ResourceController.getResourceController().getProperty("ApplicationName", "Docear").equals("Docear") ? DefaultFileNode.DOCEAR_ICON : DefaultFileNode.FREEPLANE_ICON);
	}

	public FileNodeNewMindmapAction() {
		super("workspace.action.file.new.mindmap", TextUtils.getRawText("workspace.action.file.new.mindmap.label"), icon);
	}
	
	public void actionPerformed(final ActionEvent e) {	
		AWorkspaceTreeNode targetNode = this.getNodeFromActionEvent(e);
		if(targetNode instanceof IFileSystemRepresentation ) {
			String fileName = JOptionPane.showInputDialog(Controller.getCurrentController().getViewController().getContentPane(),
				TextUtils.getText("add_new_mindmap"), TextUtils.getText("add_new_mindmap_title"),
				JOptionPane.OK_CANCEL_OPTION);
		
			if (fileName != null && fileName.length()>0) {
				if (!fileName.endsWith(".mm")) {
					fileName += ".mm";
				}
				File file = new File(((IFileSystemRepresentation) targetNode).getFile(), fileName);
				if (file.exists()) {
					JOptionPane.showMessageDialog(Controller.getCurrentController().getViewController().getContentPane(),
                            TextUtils.getText("error_file_exists"), TextUtils.getText("error_file_exists_title"),
                            JOptionPane.ERROR_MESSAGE);
				} 
				else if (createNewMindmap(file)) {
					targetNode.refresh();
				}
			
			}
		}
    }
	
	@SuppressWarnings("deprecation")
	private boolean createNewMindmap(final File f) {
		final MMapIO mapIO = (MMapIO) Controller.getCurrentModeController().getExtension(MMapIO.class);		
//		try {
//			mapIO.newMap(f.toURL());
//		}
//		catch (Exception e) {
//			LogUtils.severe(e);
//			return false;
//		}
		final ModeController modeController = Controller.getCurrentController().getModeController(MModeController.MODENAME);
		MFileManager.getController(modeController).newMapFromDefaultTemplate();
		
		mapIO.save(Controller.getCurrentController().getMap(), f);
		Controller.getCurrentController().getMap().getRootNode().setText(f.getName());
		return true;
	}


}
