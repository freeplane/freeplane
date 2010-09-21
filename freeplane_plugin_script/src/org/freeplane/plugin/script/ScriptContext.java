package org.freeplane.plugin.script;

import org.freeplane.features.common.map.NodeModel;

public class ScriptContext {
	private final FastStack<NodeModel> stack = new FastStack<NodeModel>();

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
	
	public void push(NodeModel nodeModel) {
		stack.push(nodeModel);
	}
	
	public void pop() {
		stack.pop();
	}

	@Override
    public String toString() {
	    return stack.toString();
    }
}
