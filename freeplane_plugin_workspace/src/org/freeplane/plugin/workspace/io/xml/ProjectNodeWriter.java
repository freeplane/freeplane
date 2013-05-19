/**
 * author: Marcel Genzmehr
 * 25.07.2011
 */
package org.freeplane.plugin.workspace.io.xml;

import java.io.IOException;
import java.lang.reflect.Method;

import org.freeplane.core.io.IAttributeWriter;
import org.freeplane.core.io.IElementWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.workspace.io.annotation.ExportAsAttribute;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

/**
 * 
 */
public class ProjectNodeWriter implements IElementWriter, IAttributeWriter {
	

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public ProjectNodeWriter() {
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
		AWorkspaceTreeNode wsNode = (AWorkspaceTreeNode) userObject;
		if(wsNode.getType() != null) writer.addAttribute("type", wsNode.getType());
		if(wsNode.getName() != null) writer.addAttribute("name", wsNode.getName());
		
		for(Method m : wsNode.getClass().getMethods()) {
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
				ExportAsAttribute annotation = m.getAnnotation(ExportAsAttribute.class);
				if(value instanceof Boolean && ((Boolean) value).booleanValue() == annotation.defaultBool()) {
					return;
				}
				String attrName = annotation.name();
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
		final AWorkspaceTreeNode node = (AWorkspaceTreeNode) element;
		for (int i=0; i < node.getChildCount(); i++) {
			AWorkspaceTreeNode child = node.getChildAt(i);			
			if(child == null || child.getTagName() == null) {
				continue;
			}			
			writer.addElement(child, child.getTagName());			
		}
	}
	
}
