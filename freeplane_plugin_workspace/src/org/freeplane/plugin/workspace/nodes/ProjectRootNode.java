/**
 * author: Marcel Genzmehr
 * 23.08.2011
 */
package org.freeplane.plugin.workspace.nodes;

import java.awt.Component;
import java.net.URI;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.actions.WorkspaceRemoveProjectAction;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenu;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenuBuilder;
import org.freeplane.plugin.workspace.event.IWorkspaceNodeActionListener;
import org.freeplane.plugin.workspace.event.WorkspaceActionEvent;
import org.freeplane.plugin.workspace.io.annotation.ExportAsAttribute;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.IMutableLinkNode;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;


public class ProjectRootNode extends AFolderNode implements IMutableLinkNode, IWorkspaceNodeActionListener {

	private static final long serialVersionUID = 1L;
	private static final Icon DEFAULT_ICON = new ImageIcon(AFolderNode.class.getResource("/images/project-open-2.png"));
	private static WorkspacePopupMenu popupMenu = null;
	private String projectID;
	private URI projectRoot = null;
	private String versionID;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	public ProjectRootNode() {
		this(null);
	}
	
	public ProjectRootNode(String type) {
		super(null);
		setParent(WorkspaceController.getCurrentModel().getRoot());
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	public URI getPath() {
		return this.projectRoot;		
	}
	
	@ExportAsAttribute(name="ID")
	public String getProjectID() {
		return this.projectID;
	}
	
	public void setProjectID(String id) {
		this.projectID = id;
	}
	
	public String getId() {
		return getProjectID() == null ? Integer.toHexString("".hashCode()).toUpperCase() : getProjectID();
	}
	
	public boolean setIcons(DefaultTreeCellRenderer renderer) {
		renderer.setOpenIcon(DEFAULT_ICON);
		renderer.setClosedIcon(DEFAULT_ICON);
		renderer.setLeafIcon(DEFAULT_ICON);
		return true;
	}

	protected AWorkspaceTreeNode clone(ProjectRootNode node) {
		return super.clone(node);
	}
		
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public void handleAction(WorkspaceActionEvent event) {
		if (event.getType() == WorkspaceActionEvent.MOUSE_RIGHT_CLICK) {
			showPopup((Component) event.getBaggage(), event.getX(), event.getY());
		}
	}
	
	public void refresh() {
		try {
			getModel().reload(this);			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public AWorkspaceTreeNode clone() {
		ProjectRootNode node = new ProjectRootNode(getType());
		return clone(node);
	}

	public void initializePopup() {
		if (popupMenu == null) {			
			popupMenu = new WorkspacePopupMenu();
			WorkspacePopupMenuBuilder.addActions(popupMenu, new String[] {
					WorkspacePopupMenuBuilder.createSubMenu(TextUtils.getRawText("workspace.action.new.label")),
					"workspace.action.node.new.folder",
					"workspace.action.file.new.mindmap",
					WorkspacePopupMenuBuilder.endSubMenu(),
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.node.open.location",
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.node.cut",
					"workspace.action.node.copy",						
					"workspace.action.node.paste",
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.node.rename",
					WorkspaceRemoveProjectAction.KEY,
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.node.refresh"	
			});
		}
		
	}	
	
	public WorkspacePopupMenu getContextMenu() {
		if (popupMenu == null) {
			initializePopup();
		}
		return popupMenu;
	}
	
	public String getTagName() {
		return "project";
	}
	
	public boolean getAllowsChildren() {
		return true;
	}

	public void initiateMyFile(AWorkspaceProject project) {
		FolderTypeMyFilesNode myFilesNode = new FolderTypeMyFilesNode(project); 
		getModel().addNodeTo(myFilesNode, this);
		myFilesNode.refresh();
	}

	public boolean changeName(String newName, boolean renameLink) {
		try {
			if(renameLink) {
				this.getModel().changeNodeName(this, newName);
			}
			else {
				this.setName(newName);
			}
		}
		catch(Exception ex) {
			JOptionPane.showMessageDialog(UITools.getFrame(), TextUtils.getText("error_rename_file") + " ("+ex.getMessage()+")", 
					TextUtils.getText("error_rename_file_title"), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	public void setVersion(String version) {
		this.versionID = version;		
	}
	
	@ExportAsAttribute(name="version")
	public String getVersion() {
		return this.versionID;
	}
}
