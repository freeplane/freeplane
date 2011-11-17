package org.freeplane.plugin.workspace.io.action;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JOptionPane;

import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.mindmapmode.MFileManager;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.io.node.DefaultFileNode;
import org.freeplane.plugin.workspace.model.action.AWorkspaceAction;
import org.freeplane.plugin.workspace.model.node.AWorkspaceTreeNode;

public class FileNodeNewMindmapAction extends AWorkspaceAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FileNodeNewMindmapAction() {
		super("workspace.action.file.new.mindmap");
	}
	
	public void actionPerformed(final ActionEvent e) {		
		String fileName = JOptionPane.showInputDialog(Controller.getCurrentController().getViewController().getContentPane(),
				TextUtils.getText("add_new_mindmap"), TextUtils.getText("add_new_mindmap_title"),
				JOptionPane.YES_NO_OPTION);
		
		if (fileName != null && fileName.length()>0) {
			AWorkspaceTreeNode node = this.getNodeFromActionEvent(e);
			if (node instanceof DefaultFileNode) {
				File file = ((DefaultFileNode) node).getFile();
				if (file.exists()) {
					JOptionPane.showMessageDialog(Controller.getCurrentController().getViewController().getContentPane(),
                            TextUtils.getText("error_file_exists"), TextUtils.getText("error_file_exists_title"),
                            JOptionPane.ERROR_MESSAGE);
				}
				if (!file.isDirectory()) {
					file = file.getParentFile();
				}
				if (createNewMindmap(new File(file.getPath()+File.separator+fileName))) {
					WorkspaceController.getController().reloadWorkspace();
				}
			}
		}
    }
	
	private boolean createNewMindmap(final File f) {
		MFileManager mFileManager = MFileManager.getController(Controller.getCurrentModeController());
		mFileManager.newMap();
		
		mFileManager.save(Controller.getCurrentController().getMap(), f);
		Controller.getCurrentController().close(false);

		LogUtils.info("New Mindmap Created: " + f.getAbsolutePath());
		return true;
	}


}
