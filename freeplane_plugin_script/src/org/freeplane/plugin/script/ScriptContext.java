package org.freeplane.plugin.script;

import org.freeplane.features.common.map.NodeModel;

public class ScriptContext {
	private final UniqueStack<NodeModel> stack = new UniqueStack<NodeModel>();

	public ScriptContext() {
    }

	public void accessNode(final NodeModel accessedNode) {
		FormulaUtils.accessNode(stack.last(), accessedNode);
	}
	
	public void accessBranch(final NodeModel accessedNode) {
		FormulaUtils.accessBranch(stack.last(), accessedNode);
	}
	
	public void accessAll() {
		FormulaUtils.accessAll(stack.last());
	}
	
	public boolean push(NodeModel nodeModel) {
		return stack.push(nodeModel);
	}
	
	public void pop() {
		stack.pop();
	}
	
	public NodeModel getStackFront() {
		return stack.first();
	}

	@Override
    public String toString() {
	    return stack.toString();
    }
}
