/**
 * author: Marcel Genzmehr
 * 27.07.2011
 */
package org.freeplane.plugin.workspace.io.node;

import java.io.File;
import java.net.URL;

import javax.swing.JOptionPane;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mapio.mindmapmode.MMapIO;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeAction;
import org.freeplane.plugin.workspace.model.node.AWorkspaceTreeNode;

/**
 * 
 */
public class MindMapFileNode extends DefaultFileNode {
	
	private static final long serialVersionUID = 1L;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	/**
	 * @param name
	 * @param file
	 */
	public MindMapFileNode(String name, File file) {
		super(name, file);
	}
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public AWorkspaceTreeNode clone() {
		MindMapFileNode node = new MindMapFileNode(getName(), getFile());
		return clone(node);
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	public void handleAction(WorkspaceNodeAction event) {
		if(event.getType() == WorkspaceNodeAction.WSNODE_OPEN_DOCUMENT) {
			try {
				if(!getFile().exists()) {
					JOptionPane.showMessageDialog(UITools.getFrame(), TextUtils.format("workspace.node.link.notfound", 
							new Object[]{
							getFile().isDirectory()? TextUtils.getText("workspace.node.link.notfound.directory"):TextUtils.getText("workspace.node.link.notfound.file")
										,getFile().getName()
										,getFile().getParent()
							}));
					return;
				}	
				final URL mapUrl = Compat.fileToUrl(getFile());
				final MMapIO mapIO = (MMapIO) Controller.getCurrentModeController().getExtension(MMapIO.class);
				mapIO.newMap(mapUrl);
//				Controller.getCurrentModeController().getMapController().newMap(mapUrl);
			}
			catch (final Exception e) {
				LogUtils.severe(e);
			}
		} 
		else 
			super.handleAction(event);
	}
}
