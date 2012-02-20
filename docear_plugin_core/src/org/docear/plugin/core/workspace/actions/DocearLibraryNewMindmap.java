/**
 * author: Marcel Genzmehr
 * 30.01.2012
 */
package org.docear.plugin.core.workspace.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import org.apache.commons.io.FilenameUtils;
import org.docear.plugin.core.IDocearLibrary;
import org.docear.plugin.core.features.DocearMapModelController;
import org.docear.plugin.core.workspace.node.FolderTypeLibraryNode;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mapio.mindmapmode.MMapIO;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.url.mindmapmode.MFileManager;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.actions.AWorkspaceAction;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.nodes.DefaultFileNode;
import org.freeplane.plugin.workspace.nodes.LinkTypeFileNode;

public class DocearLibraryNewMindmap extends AWorkspaceAction {

private static final long serialVersionUID = 1L;
	
	private static final Icon icon;
	
	static {
		icon = (ResourceController.getResourceController().getProperty("ApplicationName", "Docear").equals("Docear") ? DefaultFileNode.DOCEAR_ICON : DefaultFileNode.FREEPLANE_ICON);
	}

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	public DocearLibraryNewMindmap() {
		super("workspace.action.library.new.mindmap", TextUtils.getRawText("workspace.action.library.new.mindmap.label"), icon);
	}
	

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	public void actionPerformed(final ActionEvent e) {	
		AWorkspaceTreeNode targetNode = this.getNodeFromActionEvent(e);
		if(targetNode instanceof FolderTypeLibraryNode) {
			String fileName = JOptionPane.showInputDialog(Controller.getCurrentController().getViewController().getContentPane(),
				TextUtils.getText("add_new_mindmap"), TextUtils.getText("add_new_mindmap_title"),
				JOptionPane.OK_CANCEL_OPTION);
		
			if (fileName != null && fileName.length()>0) {
				if (!fileName.endsWith(".mm")) {
					fileName += ".mm";
				}
				try{
					File parentFolder = WorkspaceUtils.resolveURI(((IDocearLibrary)targetNode).getLibraryPath());
					File file = new File(parentFolder, fileName);
					try {
						file = WorkspaceController.getController().getFilesystemMgr().createFile(fileName, parentFolder);
						
//					if (file.exists()) {
//						JOptionPane.showMessageDialog(Controller.getCurrentController().getViewController().getContentPane(),
//	                            TextUtils.getText("error_file_exists"), TextUtils.getText("error_file_exists_title"),
//	                            JOptionPane.ERROR_MESSAGE);
//					} 
//					else 
						if (createNewMindmap(file)) {
							LinkTypeFileNode newNode = new LinkTypeFileNode();
							newNode.setLinkPath(WorkspaceUtils.getWorkspaceRelativeURI(file));
							newNode.setName(FilenameUtils.getBaseName(file.getName()));
							WorkspaceUtils.getModel().addNodeTo(newNode, targetNode);
							targetNode.refresh();
						}
					}
					catch(Exception ex) {
						JOptionPane.showMessageDialog(UITools.getFrame(), ex.getMessage(), "Error ... ", JOptionPane.ERROR_MESSAGE);
					}
				} 
				catch (Exception ex) {
					LogUtils.severe("could not find library paht", ex);
				}
			
			}
		}
    }
	
	@SuppressWarnings("deprecation")
	private boolean createNewMindmap(final File f) {
		final MMapIO mapIO = (MMapIO) Controller.getCurrentModeController().getExtension(MMapIO.class);		
		
		final ModeController modeController = Controller.getCurrentController().getModeController(MModeController.MODENAME);
		MFileManager.getController(modeController).newMapFromDefaultTemplate();
		
				
		MapModel map = Controller.getCurrentController().getMap();
		DocearMapModelController.setModelWithCurrentVersion(map);
		map.getRootNode().setText(FilenameUtils.getBaseName(f.getName()));
		mapIO.save(Controller.getCurrentController().getMap(), f);
		Controller.getCurrentController().close(false);
		try {
			mapIO.newMap(f.toURL());
		}
		catch (Exception e) {
			LogUtils.severe(e);
			return false;
		}
		return true;
	}
	

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
