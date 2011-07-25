/**
 * author: Marcel Genzmehr
 * 25.07.2011
 */
package org.freeplane.plugin.workspace.io.xml;

import java.io.IOException;

import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.core.io.IAttributeWriter;
import org.freeplane.core.io.IElementWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.plugin.workspace.config.node.WorkspaceNode;

/**
 * 
 */
public class WorkspaceNodeWriter implements IElementWriter, IAttributeWriter {
	

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public WorkspaceNodeWriter() {
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.freeplane.core.io.IAttributeWriter#writeAttributes(org.freeplane.
	 * core.io.ITreeWriter, java.lang.Object, java.lang.String)
	 */
	@Override
	public void writeAttributes(ITreeWriter writer, Object userObject, String tag) {		
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) userObject;
		WorkspaceNode wsNode = (WorkspaceNode) node.getUserObject();
		if(wsNode.getId() != null) writer.addAttribute("id", wsNode.getId());
		if(wsNode.getName() != null) writer.addAttribute("name", wsNode.getName());		
	}

	@Override
	public void writeContent(ITreeWriter writer, Object element, String tag) throws IOException {
		final DefaultMutableTreeNode node = (DefaultMutableTreeNode) element;
		for (int i=0; i < node.getChildCount(); i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
			if(child.getUserObject() instanceof WorkspaceNode) {
				WorkspaceNode wsNode = (WorkspaceNode) child.getUserObject();
				if(wsNode.getTagName() == null) continue;
				writer.addElement(child, wsNode.getTagName());
			}
		}
	}
	
}
