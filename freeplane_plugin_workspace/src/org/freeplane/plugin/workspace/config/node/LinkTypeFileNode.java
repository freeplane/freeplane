package org.freeplane.plugin.workspace.config.node;


import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mapio.mindmapmode.MMapIO;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeActionListener;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeAction;
import org.freeplane.plugin.workspace.dnd.IWorkspaceTransferableCreator;
import org.freeplane.plugin.workspace.dnd.WorkspaceTransferable;
import org.freeplane.plugin.workspace.io.annotation.ExportAsAttribute;
import org.freeplane.plugin.workspace.model.WorkspacePopupMenu;
import org.freeplane.plugin.workspace.model.WorkspacePopupMenuBuilder;
import org.freeplane.plugin.workspace.model.node.ALinkNode;
import org.freeplane.plugin.workspace.model.node.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.node.IMutableLinkNode;

public class LinkTypeFileNode extends ALinkNode implements IWorkspaceNodeActionListener, IWorkspaceTransferableCreator, IMutableLinkNode {
	
	private static final long serialVersionUID = 1L;		

	private URI linkPath;	
	private static WorkspacePopupMenu popupMenu = null;
	private Icon fileIcon = null;
	
	public LinkTypeFileNode() {
		super(ALinkNode.LINK_TYPE_FILE);	
	}
	
	public LinkTypeFileNode(String type) {
		super(type);	
	}
	
	public void initializePopup() {
		if (popupMenu == null) {			
			popupMenu = new WorkspacePopupMenu();
			WorkspacePopupMenuBuilder.addActions(popupMenu, new String[] {
					"workspace.action.node.cut",
					"workspace.action.node.copy", 
					"workspace.action.node.paste",
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.node.rename",
					"workspace.action.node.remove",
					"workspace.action.file.delete",
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.node.refresh"
			});
		}
	}
	
	@ExportAsAttribute(name="path")
	public URI getLinkPath() {
		return linkPath;
	}
	
	public void setLinkPath(URI linkPath) {
		this.linkPath = linkPath;
		if(linkPath != null) {			
			fileIcon = WorkspaceController.getController().getNodeTypeIconManager().getIconForNode(this);
		} else {
			fileIcon = null;
		}
	}	

	public void handleAction(WorkspaceNodeAction event) {
		if(event.getType() == WorkspaceNodeAction.WSNODE_OPEN_DOCUMENT) {
			File file = WorkspaceUtils.resolveURI(getLinkPath());
			if(file != null) {
				if(!file.exists()) {
					WorkspaceUtils.showFileNotFoundMessage(file);
					return;
				}
				if(file.getName().toLowerCase().endsWith(".mm") || file.getName().toLowerCase().endsWith(".dcr")) {
					try {
						final URL mapUrl = Compat.fileToUrl(file);
						final MMapIO mapIO = (MMapIO) Controller.getCurrentModeController().getExtension(MMapIO.class);
						mapIO.newMap(mapUrl);
//						Controller.getCurrentModeController().getMapController().newMap(mapUrl);
					}
					catch (final Exception e) {
						LogUtils.severe(e);
					}
				}
				else {
					try {
						Controller.getCurrentController().getViewController().openDocument(Compat.fileToUrl(file));
					}
					catch (Exception e) {
						LogUtils.warn("could not open document ("+getLinkPath()+")", e);
					}
				}
			}
		}
		else if (event.getType() == WorkspaceNodeAction.MOUSE_RIGHT_CLICK) {
			showPopup((Component) event.getBaggage(), event.getX(), event.getY());
		}
	}

	public Transferable getTransferable() {
		WorkspaceTransferable transferable = new WorkspaceTransferable();
		try {
			URI uri = getLinkPath().toURL().openConnection().getURL().toURI().normalize();
			transferable.addData(WorkspaceTransferable.WORKSPACE_URI_LIST_FLAVOR, uri.toString());
			List<File> fileList = new Vector<File>();
			fileList.add(new File(uri));
			transferable.addData(WorkspaceTransferable.WORKSPACE_FILE_LIST_FLAVOR, fileList);
			if(!this.isSystem()) {
				List<AWorkspaceTreeNode> objectList = new ArrayList<AWorkspaceTreeNode>();
				objectList.add(this);
				transferable.addData(WorkspaceTransferable.WORKSPACE_NODE_FLAVOR, objectList);
			}
			return transferable;
		}
		catch (Exception e) {
			LogUtils.warn(e);
		}		
		return null;
	}
	
	protected AWorkspaceTreeNode clone(LinkTypeFileNode node) {		
		node.setLinkPath(getLinkPath());		
		return super.clone(node);
	}

	
	public AWorkspaceTreeNode clone() {
		LinkTypeFileNode node = new LinkTypeFileNode(getType());
		return clone(node);
	}
	
	public WorkspacePopupMenu getContextMenu() {
		if (popupMenu == null) {
			initializePopup();
		}
		return popupMenu;
	}
	
	public boolean acceptDrop(DataFlavor[] flavors) {
		return false;
	}

	public boolean processDrop(DropTargetDropEvent event) {
		event.rejectDrop();
		return true;
	}
	
	public boolean setIcons(DefaultTreeCellRenderer renderer) {
		if(fileIcon == null) {
			return false;
		}
		renderer.setLeafIcon(fileIcon);	
		return true;
	}

	public boolean changeName(String newName, boolean renameLink) {
		assert(newName != null);
		assert(newName.trim().length() > 0);
		
		if(renameLink) {
			File oldFile = WorkspaceUtils.resolveURI(getLinkPath());
			try{
				if(oldFile == null) {
					throw new Exception("failed to resolve the file for"+getName());
				}
				File destFile = new File(oldFile.getParentFile(), newName);
				if(oldFile.exists() && oldFile.renameTo(destFile)) {					
					this.setName(newName);
					return true;
				}
				else {
					LogUtils.warn("cannot rename "+oldFile.getName());
				}
			}
			catch (Exception e) {
				LogUtils.warn("cannot rename "+oldFile.getName(), e);
			}
		}
		else {
			this.setName(newName);
			return true;
		}
		return false;
	}
}
