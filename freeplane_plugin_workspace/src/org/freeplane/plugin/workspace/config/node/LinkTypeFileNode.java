package org.freeplane.plugin.workspace.config.node;


import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;
import org.freeplane.plugin.workspace.dnd.IWorkspaceTransferableCreator;
import org.freeplane.plugin.workspace.dnd.WorkspaceTransferable;
import org.freeplane.plugin.workspace.io.annotation.ExportAsAttribute;
import org.freeplane.plugin.workspace.model.WorkspacePopupMenu;
import org.freeplane.plugin.workspace.model.WorkspacePopupMenuBuilder;
import org.freeplane.plugin.workspace.model.node.ALinkNode;
import org.freeplane.plugin.workspace.model.node.AWorkspaceTreeNode;

public class LinkTypeFileNode extends ALinkNode implements IWorkspaceNodeEventListener, IWorkspaceTransferableCreator {
	
	private static final long serialVersionUID = 1L;

	private URI linkPath;
	
	private static WorkspacePopupMenu popupMenu = null;
	
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
					WorkspacePopupMenuBuilder.SEPARATOR, 
					"workspace.action.node.paste",
					"workspace.action.node.copy",
					"workspace.action.node.cut",
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.node.rename",
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.node.delete"
			});
		}
	}
	
	@ExportAsAttribute("path")
	public URI getLinkPath() {
		return linkPath;
	}
	
	public void setLinkPath(URI linkPath) {
		this.linkPath = linkPath;
	}	

	public void handleEvent(WorkspaceNodeEvent event) {
		if(event.getType() == WorkspaceNodeEvent.WSNODE_OPEN_DOCUMENT) {
			File file = WorkspaceUtils.resolveURI(getLinkPath());
			if(file != null) {
				int dot = file.getPath().lastIndexOf('.');
				String fileExt = "";
				if(-1 != dot) {
					fileExt = file.getPath().substring(dot);
				}				
				if(fileExt.equalsIgnoreCase(".mm") || fileExt.equalsIgnoreCase(".dcr")) {
					try {
						final URL mapUrl = Compat.fileToUrl(file);
						Controller.getCurrentModeController().getMapController().newMap(mapUrl, false);
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
		else if (event.getType() == WorkspaceNodeEvent.MOUSE_RIGHT_CLICK) {
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
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return transferable;
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
}
