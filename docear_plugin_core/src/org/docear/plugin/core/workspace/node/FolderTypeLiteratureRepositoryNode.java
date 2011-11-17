/**
 * author: Marcel Genzmehr
 * 18.08.2011
 */
package org.docear.plugin.core.workspace.node;

import java.io.File;

import org.docear.plugin.core.CoreConfiguration;
import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.config.node.PhysicalFolderNode;
import org.freeplane.plugin.workspace.model.node.AWorkspaceTreeNode;

/**
 * 
 */
public class FolderTypeLiteratureRepositoryNode extends PhysicalFolderNode implements IFreeplanePropertyListener /* FolderNode */{

	private static final long serialVersionUID = 1L;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public FolderTypeLiteratureRepositoryNode(String type) {
		super(type);
		// TODO Auto-generated constructor stub
		Controller.getCurrentController().getResourceController().addPropertyChangeListener(this);
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public AWorkspaceTreeNode clone() {
		FolderTypeLiteratureRepositoryNode node = new FolderTypeLiteratureRepositoryNode(getType());
		return clone(node);
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	public void propertyChanged(String propertyName, final String newValue, String oldValue) {
		if (propertyName.equals(CoreConfiguration.DOCUMENT_REPOSITORY_PATH) && newValue != null && newValue.trim().length() > 0) {
			try {
				File file = new File(newValue);
				if (file != null) {
					if (!file.exists()) {
						if (file.mkdirs()) {
							LogUtils.info("New Filesystem Folder Created: " + file.getAbsolutePath());
						}
					}
					setName(file.getName());
				}
				WorkspaceUtils.getModel().removeAllElements(this);
				WorkspaceController.getController().getFilesystemReader().scanFileSystem(this, file);
				WorkspaceUtils.getModel().reload(this);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
