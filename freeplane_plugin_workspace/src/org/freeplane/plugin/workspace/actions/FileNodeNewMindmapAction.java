package org.freeplane.plugin.workspace.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import org.apache.commons.io.FilenameUtils;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.n3.nanoxml.XMLParseException;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.io.IFileSystemRepresentation;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.nodes.DefaultFileNode;

public class FileNodeNewMindmapAction extends AWorkspaceAction {
	
	private static final long serialVersionUID = 1L;
	
	private static final Icon icon;
	
	static {
		icon = DefaultFileNode.getApplicationIcon();
	}

	public FileNodeNewMindmapAction() {
		super("workspace.action.file.new.mindmap", TextUtils.getRawText("workspace.action.file.new.mindmap.label"), icon);
	}
	
	public void actionPerformed(final ActionEvent e) {
		Controller.getCurrentController().selectMode(MModeController.MODENAME);
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
				try {
					file = WorkspaceController.getFileSystemMgr().createFile(fileName, ((IFileSystemRepresentation) targetNode).getFile());
					if (createNewMindmap(file)) {
						targetNode.refresh();
					}
				}
				catch(Exception ex) {
					JOptionPane.showMessageDialog(UITools.getFrame(), ex.getMessage(), "Error ... ", JOptionPane.ERROR_MESSAGE);
					ex.printStackTrace();
				}
				
				
			
			}
		}
    }
	
	private boolean createNewMindmap(final File f) throws FileNotFoundException, XMLParseException, MalformedURLException, IOException, URISyntaxException {
//		final MMapIO mapIO = (MMapIO) Controller.getCurrentModeController().getExtension(MapIO.class);
		WorkspaceNewMapAction.createNewMap(f.toURI(), FilenameUtils.getBaseName(f.getName()), false);
		
//		try {
//			mapIO.newMap(f.toURI().toURL());
//		} catch (XMLException e) {
//			LogUtils.severe(e);
//		}
		
		return true;
	}


}
