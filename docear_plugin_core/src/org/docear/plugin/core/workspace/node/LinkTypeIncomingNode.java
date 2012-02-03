/**
 * author: Marcel Genzmehr
 * 19.08.2011
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
import org.docear.plugin.core.features.DocearMapModelController;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mapio.mindmapmode.MMapIO;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.url.mindmapmode.MFileManager;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeActionListener;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeAction;
import org.freeplane.plugin.workspace.io.annotation.ExportAsAttribute;
import org.freeplane.plugin.workspace.model.WorkspacePopupMenu;
import org.freeplane.plugin.workspace.model.WorkspacePopupMenuBuilder;
import org.freeplane.plugin.workspace.model.node.ALinkNode;
import org.freeplane.plugin.workspace.model.node.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.node.IMutableLinkNode;

/**
 * 
 */
public class LinkTypeIncomingNode extends ALinkNode implements IWorkspaceNodeActionListener, IMutableLinkNode {
	private static final Icon DEFAULT_ICON = new ImageIcon(ResourceController.class.getResource("/images/docear16.png"));

	private static final long serialVersionUID = 1L;
	
	private URI linkPath;
	private WorkspacePopupMenu popupMenu = null;
	
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public LinkTypeIncomingNode(String type) {
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
	
	@SuppressWarnings("deprecation")
	private boolean createNewMindmap(final File f) {
		if (!createFolderStructure(f)) {
			return false;
		}
		
		final MMapIO mapIO = (MMapIO) Controller.getCurrentModeController().getExtension(MMapIO.class);		
//		try {
//			if(!f.exists() && !f.createNewFile()) {
//				return false;
//			}
//			if(!mapIO.newMap(f.toURL())) {
//				return false;
//			}
//		}
//		catch (Exception e) {
//			LogUtils.severe(e);
//			return false;
//		}
		
		final ModeController modeController = Controller.getCurrentController().getModeController(MModeController.MODENAME);
		MFileManager.getController(modeController).newMapFromDefaultTemplate();
		
		MapModel map = Controller.getCurrentController().getMap();
		DocearMapModelController.setModelWithCurrentVersion(map);
		map.getRootNode().setText(getName());
		DocearEvent evnt = new DocearEvent(this, DocearEventType.NEW_INCOMING, map);
		DocearController.getController().dispatchDocearEvent(evnt);
		
		mapIO.save(Controller.getCurrentController().getMap(), f);		
		Controller.getCurrentController().close(false);

		LogUtils.info("New Mindmap Created: " + f.getAbsolutePath());
		return true;
	}

	private boolean createFolderStructure(final File f) {
		final File folder = f.getParentFile();
		if (folder.exists()) {
			return true;
		}
		return folder.mkdirs();
	}
	
	protected AWorkspaceTreeNode clone(LinkTypeIncomingNode node) {
		node.setLinkPath(getLinkPath());
		return super.clone(node);
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public void handleAction(WorkspaceNodeAction event) {
		System.out.println("event: "+event.getType());
		if (event.getType() == WorkspaceNodeAction.MOUSE_LEFT_DBLCLICK) {
			System.out.println("doublecklicked MindmapNode");
			try {				
				File f = WorkspaceUtils.resolveURI(getLinkPath());
				if(f == null) {
					return;
				}
				if (!f.exists()) {
					createNewMindmap(f);
				}
				
				final MMapIO mapIO = (MMapIO) Controller.getCurrentModeController().getExtension(MMapIO.class);		
				try {
					mapIO.newMap(f.toURL());
				}
				catch (Exception e) {
					LogUtils.severe(e);
				}
			}
			catch (Exception e) {
				LogUtils.warn("could not open document (" + getLinkPath() + ")", e);
			}
		}
		else if (event.getType() == WorkspaceNodeAction.MOUSE_RIGHT_CLICK) {			
				showPopup((Component) event.getBaggage(), event.getX(), event.getY());
		}
		else {
			// do nth for now
		}
	}

	public AWorkspaceTreeNode clone() {
		LinkTypeIncomingNode node = new LinkTypeIncomingNode(getType());
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
