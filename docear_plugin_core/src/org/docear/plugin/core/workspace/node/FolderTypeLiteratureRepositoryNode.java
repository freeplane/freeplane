/**
 * author: Marcel Genzmehr
 * 18.08.2011
 */
package org.docear.plugin.core.workspace.node;

import java.io.File;

import javax.swing.tree.DefaultMutableTreeNode;

import org.docear.plugin.core.CoreConfiguration;
import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.config.node.PhysicalFolderNode;

/**
 * 
 */
public class FolderTypeLiteratureRepositoryNode extends PhysicalFolderNode implements IFreeplanePropertyListener /* FolderNode */{

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

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	public void propertyChanged(String propertyName, final String newValue, String oldValue) {
		if (propertyName.equals(CoreConfiguration.DOCUMENT_REPOSITORY_PATH) && newValue != null && newValue.trim().length() > 0) {
			IndexedTree indexTree = WorkspaceController.getController().getIndexTree();
			String key = (String) indexTree.getKeyByUserObject(this);
			if(key == null) {
				//FIXME: DOCEAR> remove this node from "Controller.getCurrentController().getResourceController().removePropertyChangeListener(this);" !!!ConcurrentModificationException
				return;
			}
			final DefaultMutableTreeNode node = indexTree.get(key);
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
				WorkspaceController.getController().getIndexTree().removeChildElements(key);
				WorkspaceController.getController().getFilesystemReader().scanFilesystem(key, file);
				WorkspaceController.getController().getViewModel().reload(node);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
