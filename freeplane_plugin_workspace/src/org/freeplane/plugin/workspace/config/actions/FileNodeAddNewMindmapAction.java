package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.mindmapmode.MFileManager;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.io.node.DefaultFileNode;

public class FileNodeAddNewMindmapAction extends AWorkspaceAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FileNodeAddNewMindmapAction() {
		super("FileNodeAddNewMindmapAction");
	}
	
	public void actionPerformed(final ActionEvent e) {		
		String fileName = JOptionPane.showInputDialog(Controller.getCurrentController().getViewController().getContentPane(),
				TextUtils.getText("add_new_mindmap"), TextUtils.getText("add_new_mindmap_title"),
				JOptionPane.YES_NO_OPTION);
		
		if (fileName != null && fileName.length()>0) {
			DefaultMutableTreeNode node = this.getNodeFromActionEvent(e);
			if (node.getUserObject() instanceof DefaultFileNode) {
				File file = ((DefaultFileNode) node.getUserObject()).getFile();
				if (file.exists()) {
					JOptionPane.showMessageDialog(Controller.getCurrentController().getViewController().getContentPane(),
                            TextUtils.getText("error_file_exists"), TextUtils.getText("error_file_exists_title"),
                            JOptionPane.ERROR_MESSAGE);
				}
				if (!file.isDirectory()) {
					file = file.getParentFile();
				}
				if (createNewMindmap(new File(file.getPath()+File.separator+fileName))) {
					WorkspaceController.getCurrentWorkspaceController().refreshWorkspace();
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
