/**
 * author: Marcel Genzmehr
 * 25.07.2011
 */
package org.freeplane.plugin.workspace.io.xml;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.core.io.IAttributeWriter;
import org.freeplane.core.io.IElementWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.workspace.config.node.AWorkspaceNode;
import org.freeplane.plugin.workspace.io.annotation.ExportAsAttribute;

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
	public void writeAttributes(ITreeWriter writer, Object userObject, String tag) {		
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) userObject;
		AWorkspaceNode wsNode = (AWorkspaceNode) node.getUserObject();
		if(wsNode.getType() != null) writer.addAttribute("type", wsNode.getType());
		if(wsNode.getName() != null) writer.addAttribute("name", wsNode.getName());
		
		for(Method m : wsNode.getClass().getDeclaredMethods()) {
			if(m.getAnnotation(ExportAsAttribute.class) != null && m.getParameterTypes().length == 0 && m.getReturnType() != void.class) {				
				writeAdditionalAttribute(writer, wsNode, m);
			}
		}
	}

	/**
	 * @param userObject
	 * @param m
	 * @param value
	 */
	private void writeAdditionalAttribute(ITreeWriter writer, Object object, Method m) {
		Object[] args = null;
		Object value;
		try {
			value = m.invoke(object, args);
			if(value != null) {
				String attrName = m.getAnnotation(ExportAsAttribute.class).value();
				if(attrName.trim().length()==0) throw new IllegalArgumentException("value for annotation 'ExportAsAttribute' must not be empty!");
				writer.addAttribute(attrName.trim(), value.toString());
			}
		}
		catch (IllegalArgumentException e) {
			throw e;
		}
		catch (Exception e) {
			LogUtils.severe("This should not have happend: ", e);
		}		
	}

	public void writeContent(ITreeWriter writer, Object element, String tag) throws IOException {
		final DefaultMutableTreeNode node = (DefaultMutableTreeNode) element;
		for (int i=0; i < node.getChildCount(); i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
			if(child.getUserObject() instanceof AWorkspaceNode) {
				AWorkspaceNode wsNode = (AWorkspaceNode) child.getUserObject();
				if(wsNode.getTagName() == null) continue;
				writer.addElement(child, wsNode.getTagName());
			}
		}
	}
	
}
