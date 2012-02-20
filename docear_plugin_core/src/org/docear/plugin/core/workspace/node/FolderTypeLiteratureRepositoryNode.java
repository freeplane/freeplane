/**
 * author: Marcel Genzmehr
 * 18.08.2011
 */
package org.docear.plugin.core.workspace.node;

import java.io.File;
import java.net.URI;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.docear.plugin.core.CoreConfiguration;
import org.docear.plugin.core.workspace.node.config.NodeAttributeObserver;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenu;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenuBuilder;
import org.freeplane.plugin.workspace.io.IFileSystemRepresentation;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.nodes.FolderLinkNode;

/**
 * 
 */
public class FolderTypeLiteratureRepositoryNode extends FolderLinkNode implements ChangeListener, IFileSystemRepresentation {

	private static final long serialVersionUID = 1L;
	private boolean locked;
	private WorkspacePopupMenu popupMenu = null;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public FolderTypeLiteratureRepositoryNode(String type) {
		super(type);		
		CoreConfiguration.repositoryPathObserver.addChangeListener(this);
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public AWorkspaceTreeNode clone() {
		FolderTypeLiteratureRepositoryNode node = new FolderTypeLiteratureRepositoryNode(getType());
		return clone(node);
	}
	
	public void disassociateReferences()  {
		CoreConfiguration.repositoryPathObserver.removeChangeListener(this);
	}
	
	public void setName(String name) {
		super.setName("Literature Repository");
	}
	
	public void setPath(URI uri) {
		super.setPath(uri);
		locked = true;		
		CoreConfiguration.repositoryPathObserver.setUri(uri);
		if (uri != null) {
			createPathIfNeeded(uri);
		}
		locked = false;	
	}
	
	private void createPathIfNeeded(URI uri) {
		File file = WorkspaceUtils.resolveURI(uri);

		if (file != null) {
			if (!file.exists()) {
				if (file.mkdirs()) {
					LogUtils.info("New Literature Folder Created: " + file.getAbsolutePath());
				}
			}
			this.setName(file.getName());
		}
		else {
			this.setName("no folder selected!");
		}

		
	}
	
	public void initializePopup() {
		if (popupMenu == null) {
			
			popupMenu = new WorkspacePopupMenu();
			WorkspacePopupMenuBuilder.addActions(popupMenu, new String[] {
					WorkspacePopupMenuBuilder.createSubMenu(TextUtils.getRawText("workspace.action.new.label")),
					"workspace.action.node.new.folder",
					"workspace.action.file.new.mindmap",
					//WorkspacePopupMenuBuilder.SEPARATOR,
					//"workspace.action.file.new.file",
					WorkspacePopupMenuBuilder.endSubMenu(),
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.docear.uri.change",
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.node.cut",
					"workspace.action.node.copy",
					"workspace.action.node.paste",
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.node.rename",
					"workspace.action.node.remove",
					"workspace.action.file.delete",					
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.node.physical.sort",
					WorkspacePopupMenuBuilder.SEPARATOR,					
					"workspace.action.docear.enable.monitoring",
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

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	//TODO: replace by new method
	public void propertyChanged(String propertyName, final String newValue, String oldValue) {
		
	}

	public void stateChanged(ChangeEvent e) {		
		if(!locked && e.getSource() instanceof NodeAttributeObserver) {			
			URI uri = ((NodeAttributeObserver) e.getSource()).getUri();
			try{		
				createPathIfNeeded(uri);				
			}
			catch (Exception ex) {
				return;
			}
			this.setPath(uri);
			this.refresh();
		}
	}
	
	public File getFile() {
		return WorkspaceUtils.resolveURI(this.getPath());
	}
}
