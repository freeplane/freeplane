/**
 * author: Marcel Genzmehr
 * 18.08.2011
 */
package org.docear.plugin.core.workspace.node;

import java.awt.Component;
import java.io.File;
import java.net.URI;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mapio.MapIO;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenu;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenuBuilder;
import org.freeplane.plugin.workspace.event.IWorkspaceNodeActionListener;
import org.freeplane.plugin.workspace.event.WorkspaceActionEvent;
import org.freeplane.plugin.workspace.io.annotation.ExportAsAttribute;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.IMutableLinkNode;
import org.freeplane.plugin.workspace.nodes.ALinkNode;

/**
 * 
 */
public class LinkTypeLiteratureAnnotationsNode extends ALinkNode implements IWorkspaceNodeActionListener, IMutableLinkNode {
	private static final Icon DEFAULT_ICON = new ImageIcon(ResourceController.class.getResource("/images/docear16.png"));

	private static final long serialVersionUID = 1L;
	
	private URI linkPath;
	private WorkspacePopupMenu popupMenu = null;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public LinkTypeLiteratureAnnotationsNode(String type) {
		super(type);
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	@ExportAsAttribute(name="path")
	public URI getLinkPath() {
		return linkPath;
	}
	
	public void setLinkPath(URI linkPath) {
		this.linkPath = linkPath;
		if(this.linkPath != null) {
			DocearEvent event = new DocearEvent(this, DocearEventType.LIBRARY_NEW_MINDMAP_INDEXING_REQUEST, getLinkPath());
			DocearController.getController().dispatchDocearEvent(event);
		}
	}
	
	public boolean setIcons(DefaultTreeCellRenderer renderer) {
		renderer.setOpenIcon(DEFAULT_ICON);
		renderer.setClosedIcon(DEFAULT_ICON);
		renderer.setLeafIcon(DEFAULT_ICON);
		return true;
	}
	
	public void initializePopup() {
		if (popupMenu == null) {
						
			popupMenu  = new WorkspacePopupMenu();
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
	
	protected AWorkspaceTreeNode clone(LinkTypeLiteratureAnnotationsNode node) {
		node.setLinkPath(getLinkPath());
		return super.clone(node);
	}
	
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public void handleAction(WorkspaceActionEvent event) {
		if (event.getType() == WorkspaceActionEvent.MOUSE_LEFT_DBLCLICK) {
			try {				
				File f = WorkspaceUtils.resolveURI(getLinkPath());
				if(f == null) {
					return;
				}
				if (!f.exists()) {
					if(!WorkspaceUtils.createNewMindmap(f, getName())) {
						LogUtils.warn("could not create " + getLinkPath());
					}
				}
				final MapIO mapIO = (MapIO) Controller.getCurrentModeController().getExtension(MapIO.class);		
				try {
					if(mapIO.newMap(f.toURL())) {
						DocearEvent evnt = new DocearEvent(this, DocearEventType.NEW_LITERATURE_ANNOTATIONS, Controller.getCurrentController().getMap());
						DocearController.getController().dispatchDocearEvent(evnt);
					}
				}
				catch (Exception e) {
					LogUtils.severe(e);
					return;
				}
				
			}
			catch (Exception e) {
				LogUtils.warn("could not open document (" + getLinkPath() + ")", e);
			}
		}
		else if (event.getType() == WorkspaceActionEvent.MOUSE_RIGHT_CLICK) {			
			showPopup((Component) event.getBaggage(), event.getX(), event.getY());
		}
		else {
			// do nth for now
		}
	}

	public AWorkspaceTreeNode clone() {
		LinkTypeLiteratureAnnotationsNode node = new LinkTypeLiteratureAnnotationsNode(getType());
		return clone(node);
	}
	
	public WorkspacePopupMenu getContextMenu() {
		if (popupMenu == null) {
			initializePopup();
		}
		return popupMenu;
	}

	public boolean changeName(String newName, boolean renameLink) {
		// simple set the node name
		this.setName(newName);
		return true;
	}
}
