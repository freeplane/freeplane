package org.freeplane.plugin.workspace.nodes;

import java.awt.Component;
import java.net.URI;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;

import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.actions.NodeRefreshAction;
import org.freeplane.plugin.workspace.actions.WorkspaceImportProjectAction;
import org.freeplane.plugin.workspace.actions.WorkspaceNewProjectAction;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenu;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenuBuilder;
import org.freeplane.plugin.workspace.event.IWorkspaceNodeActionListener;
import org.freeplane.plugin.workspace.event.WorkspaceActionEvent;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

public class WorkspaceRootNode extends AFolderNode implements IWorkspaceNodeActionListener {

	private static final long serialVersionUID = 1L;
	private static Icon DEFAULT_ICON = new ImageIcon(
			FolderLinkNode.class.getResource("/images/16x16/preferences-desktop-filetype-association.png"));
	private static WorkspacePopupMenu popupMenu;

	public WorkspaceRootNode() {
		super(null);
	}

	public final String getTagName() {
		return null;
	}

	public void handleAction(WorkspaceActionEvent event) {
		if (event.getType() == WorkspaceActionEvent.MOUSE_RIGHT_CLICK) {
			showPopup((Component) event.getBaggage(), event.getX(), event.getY());
		}
	}
	
	public boolean isSystem() {
		return true;
	}

	public boolean setIcons(DefaultTreeCellRenderer renderer) {
		renderer.setOpenIcon(DEFAULT_ICON);
		renderer.setClosedIcon(DEFAULT_ICON);
		renderer.setLeafIcon(DEFAULT_ICON);
		return true;
	}

	public void initializePopup() {
		if (popupMenu == null) {			
			popupMenu = new WorkspacePopupMenu();
			WorkspacePopupMenuBuilder.addActions(popupMenu, new String[] {
					WorkspaceNewProjectAction.KEY,
					WorkspaceImportProjectAction.KEY,
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.all.expand",
					"workspace.action.all.collapse",					 
					WorkspacePopupMenuBuilder.SEPARATOR,
					NodeRefreshAction.KEY					
			});
		}
	}

	protected AWorkspaceTreeNode clone(WorkspaceRootNode node) {
		return super.clone(node);
	}

	public AWorkspaceTreeNode clone() {
		WorkspaceRootNode node = new WorkspaceRootNode();
		return clone(node);
	}

	public WorkspacePopupMenu getContextMenu() {
		if (popupMenu == null) {
			initializePopup();
		}
		return popupMenu;
	}
	
	public String getName() {
		//WORKSPACE - todo: get name from somewhere or set it somehow
		return TextUtils.getText("workspace.node.root.name"); 
	}
	
	public void refresh() {
		getModel().reload(this);
	}
	
	public URI getPath() {
		// not used for workspace root
		return null;
	}
	
	public boolean isLeaf() {
		return false;
	}

	public AWorkspaceTreeNode getChildAt(int childIndex) {
		AWorkspaceTreeNode node = null;
		int offset = super.getChildCount();
		if(offset > 0) {
			node = super.getChildAt(childIndex);
		}
		if(node == null) {
			node = (AWorkspaceTreeNode) WorkspaceController.getCurrentModel().getChild(this, childIndex);
		}
		return node;
	}

	public int getChildCount() {
		int offset = super.getChildCount(); 
		return WorkspaceController.getCurrentModel().getProjects().size()+offset;
	}

	public int getIndex(TreeNode node) {
		return WorkspaceController.getCurrentModel().getIndexOfChild(this, node);
	}

	public boolean getAllowsChildren() {
		return true;
	}

	public Enumeration<AWorkspaceTreeNode> children() {
		return new Enumeration<AWorkspaceTreeNode>() {
		    int count = 0;
		    
		    public boolean hasMoreElements() {
		    	return count < getChildCount();
		    }

		    public AWorkspaceTreeNode nextElement() {
				if (count < getChildCount()) {
				  	return getChildAt(count++);
				}				
				throw new NoSuchElementException("WorkspaceRoot Enumeration");
		    }
		};
	}	
}
