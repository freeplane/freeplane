package org.freeplane.plugin.workspace.nodes;


import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mapio.MapIO;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.actions.WorkspaceNewMapAction;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenu;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenuBuilder;
import org.freeplane.plugin.workspace.dnd.IWorkspaceTransferableCreator;
import org.freeplane.plugin.workspace.dnd.WorkspaceTransferable;
import org.freeplane.plugin.workspace.event.IWorkspaceNodeActionListener;
import org.freeplane.plugin.workspace.event.WorkspaceActionEvent;
import org.freeplane.plugin.workspace.io.annotation.ExportAsAttribute;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.IMutableLinkNode;

public class LinkTypeFileNode extends ALinkNode implements IWorkspaceNodeActionListener, IWorkspaceTransferableCreator, IMutableLinkNode {
	
	private static final long serialVersionUID = 1L;		

	private static final Icon NOT_EXISTING = new ImageIcon(AWorkspaceTreeNode.class.getResource("/images/16x16/cross.png"));
	
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
	public URI getLinkURI() {
		return linkPath;
	}
	
	public void setLinkURI(URI linkPath) {
		URI oldUri = this.linkPath;
		this.linkPath = linkPath;
		fileIcon = null;
		
		if(getModel() == null) {
			return;
		}
		getModel().nodeChanged(this, linkPath, oldUri);
	}	

	public void handleAction(WorkspaceActionEvent event) {
		if(event.getType() == WorkspaceActionEvent.WSNODE_OPEN_DOCUMENT) {
			
			File file = URIUtils.getAbsoluteFile(getLinkURI());
			if(file != null) {

				if(file.getName().toLowerCase().endsWith(".mm") || file.getName().toLowerCase().endsWith(".dcr")) {
				
    				if(!file.exists() && this.isSystem()){
    					if(WorkspaceNewMapAction.createNewMap(getLinkURI(), getName(), true) == null) {
    						LogUtils.warn("could not create " + getLinkURI());
    					}
    					return;
    				}
    				if(!file.exists() && !this.isSystem()) {
    					//WORKSPACE - todo: replace with some kind of view extension call
    					//WorkspaceUtils.showFileNotFoundMessage(file);
    					return;
    				}
    				
					try {
						final URL mapUrl = Compat.fileToUrl(file);
						
						final MapIO mapIO = (MapIO) MModeController.getMModeController().getExtension(MapIO.class);		
						
						mapIO.newMap(mapUrl);
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
						LogUtils.warn("could not open document ("+getLinkURI()+")", e);
					}
				}
			}
		}
		else if (event.getType() == WorkspaceActionEvent.MOUSE_RIGHT_CLICK) {
			showPopup((Component) event.getBaggage(), event.getX(), event.getY());
		}
	}

	public WorkspaceTransferable getTransferable() {
		WorkspaceTransferable transferable = new WorkspaceTransferable();
		try {
			URI uri = URIUtils.getAbsoluteURI(getLinkURI());
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
	
	public boolean getAllowsChildren() {
		return false;
	}
	
	protected AWorkspaceTreeNode clone(LinkTypeFileNode node) {		
		node.setLinkURI(getLinkURI());		
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
		File file = URIUtils.getAbsoluteFile(getLinkURI());
		if((file == null || !file.exists()) && !isSystem()) {
			renderer.setLeafIcon(NOT_EXISTING);
			renderer.setOpenIcon(NOT_EXISTING);
			renderer.setClosedIcon(NOT_EXISTING);
			return true;
		}
		if(fileIcon == null) {
			if(linkPath != null) {			
				fileIcon = WorkspaceController.getCurrentModeExtension().getView().getNodeTypeIconManager().getIconForNode(this);
			}
		}
				
		if(fileIcon == null) {	
			fileIcon = FileSystemView.getFileSystemView().getSystemIcon(file);
			if(fileIcon != null) {
				renderer.setLeafIcon(fileIcon);
				return true;
			}
			return false;
		}
		renderer.setLeafIcon(fileIcon);	
		return true;
	}

	public boolean changeName(String newName, boolean renameLink) {
		assert(newName != null);
		assert(newName.trim().length() > 0);
		
		if(renameLink) {
			File oldFile = URIUtils.getAbsoluteFile(getLinkURI());
			try{
				if(oldFile == null) {
					throw new Exception("failed to resolve the file for"+getName());
				}
				File destFile = new File(oldFile.getParentFile(), newName);
				if(oldFile.exists() && oldFile.renameTo(destFile)) {
					try {
						getModel().changeNodeName(this, newName);
						return true;
					}
					catch(Exception ex) {
						destFile.renameTo(oldFile);
						return false;
					}
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
			try {
				getModel().changeNodeName(this, newName);
			}
			catch(Exception ex) {
				// do nth.
			}
			return true;
		}
		return false;
	}
}
