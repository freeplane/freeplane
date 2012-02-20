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
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mapio.mindmapmode.MMapIO;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.url.mindmapmode.MFileManager;
import org.freeplane.n3.nanoxml.XMLParseException;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.io.IFileSystemRepresentation;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.nodes.DefaultFileNode;

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
				try {
					file = WorkspaceController.getController().getFilesystemMgr().createFile(fileName, ((IFileSystemRepresentation) targetNode).getFile());
					if (createNewMindmap(file)) {
						targetNode.refresh();
					}
				}
				catch(Exception ex) {
					JOptionPane.showMessageDialog(UITools.getFrame(), ex.getMessage(), "Error ... ", JOptionPane.ERROR_MESSAGE);
				}
				
				
			
			}
		}
    }
	
	@SuppressWarnings("deprecation")
	private boolean createNewMindmap(final File f) throws FileNotFoundException, XMLParseException, MalformedURLException, IOException, URISyntaxException {
		final MMapIO mapIO = (MMapIO) Controller.getCurrentModeController().getExtension(MMapIO.class);
		final ModeController modeController = Controller.getCurrentController().getModeController(MModeController.MODENAME);
		MFileManager.getController(modeController).newMapFromDefaultTemplate();
		Controller.getCurrentController().getMap().getRootNode().setText(FilenameUtils.getBaseName(f.getName()));
		mapIO.save(Controller.getCurrentController().getMap(), f);
		Controller.getCurrentController().close(true);
		mapIO.newMap(f.toURL());
		
		return true;
	}


}
