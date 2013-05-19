package org.freeplane.plugin.workspace.model;

import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.core.util.LogUtils;
import org.freeplane.n3.nanoxml.XMLElement;

public abstract class AWorkspaceNodeCreator implements IElementDOMHandler {
	
	private IResultProcessor resultProcessor;

	abstract public AWorkspaceTreeNode getNode(final XMLElement data);
	
			
	public AWorkspaceNodeCreator() {
	}
	
	public Object createElement(final Object parent, final String tag, final XMLElement attributes) {
		if (attributes == null) {
			return null;
		}		
		
		AWorkspaceTreeNode node = getNode(attributes);		
		if (node == null) { 
			return null;
		}
		node.setParent((AWorkspaceTreeNode) parent);
		node.setMandatoryAttributes(attributes);
		//node.initializePopup();
		processResult((AWorkspaceTreeNode) parent, node);
		return node;
	}

	public void endElement(final Object parent, final String tag, final Object userObject, final XMLElement lastBuiltElement) {
	}
	
	public void setResultProcessor(IResultProcessor processor) {
		this.resultProcessor = processor;
	}
	
	public IResultProcessor getResultProcessor() {
		return this.resultProcessor;
	}
	
	private void processResult(AWorkspaceTreeNode parent, AWorkspaceTreeNode node) {
		if(getResultProcessor() == null) {
			LogUtils.warn("Missing ResultProcessor for node "+ node.getClass().getName());
			return;
		}
		getResultProcessor().process(parent, node);
	}
}