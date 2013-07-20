package org.freeplane.plugin.workspace.nodes;

import java.awt.Component;
import java.io.File;
import java.io.FileFilter;
import java.net.URI;
import java.util.Locale;

import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenu;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenuBuilder;
import org.freeplane.plugin.workspace.event.IWorkspaceNodeActionListener;
import org.freeplane.plugin.workspace.event.WorkspaceActionEvent;
import org.freeplane.plugin.workspace.io.IFileSystemRepresentation;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

public class FolderTypeMyFilesNode extends AFolderNode implements IWorkspaceNodeActionListener
																	, IFileSystemRepresentation {

	public static final String TYPE = "myFiles"; 
	private static final long serialVersionUID = 1L;
	private final AWorkspaceProject project;
	private static WorkspacePopupMenu popupMenu = null;
	private boolean orderDescending = false;

	public FolderTypeMyFilesNode(AWorkspaceProject project) {
		super(TYPE);
		this.project = project;
		
	}

	public String getName() {
		return TextUtils.getText(FolderTypeMyFilesNode.class.getName().toLowerCase(Locale.ENGLISH)+".name");
	}
	
	@Override
	public URI getPath() {
		return project.getProjectHome();
	}
	
	public AWorkspaceTreeNode clone() {
		return super.clone(new FolderTypeMyFilesNode(project));
	}
	
	public boolean isSystem() {
		return true;
	}
	
	public boolean isTransferable() {
		return false;
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public void handleAction(WorkspaceActionEvent event) {
		if (event.getType() == WorkspaceActionEvent.MOUSE_RIGHT_CLICK) {
			showPopup((Component) event.getBaggage(), event.getX(), event.getY());
		}
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
					"workspace.action.node.physical.sort",
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
	
	public void refresh() {
		try {
			File file = URIUtils.getAbsoluteFile(getPath());
			if (file != null) {
				getModel().removeAllElements(this);
				WorkspaceController.getFileSystemMgr().scanFileSystem(this, file, new FileFilter() {
					
					public boolean accept(File pathname) {
						if("_data".equals(pathname.getName())) {
							return false;
						}
						return true;
					}
				});
				getModel().reload(this);
			}			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public final String getTagName() {
		return null;
	}
	
	public File getFile() {
		return URIUtils.getAbsoluteFile(getPath());
	}

	public void orderDescending(boolean enable) {
		this.orderDescending = enable;
	}

	public boolean orderDescending() {
		return orderDescending;
	}

}
