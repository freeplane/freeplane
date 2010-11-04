package org.freeplane.plugin.script;

import org.freeplane.features.common.map.NodeModel;

public class ScriptContext {
	private static final class NodeWrapper {
		private final NodeModel nodeModel;
		private final String script;

		public NodeWrapper(NodeModel nodeModel, String script) {
			this.nodeModel = nodeModel;
			// NOTE: to ignore the script for cycle detection comment out next line
			this.script = script;
		}

		public NodeModel getNodeModel() {
			return nodeModel;
		}

		//		public String getScript() {
		//			return script;
		//		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((nodeModel == null) ? 0 : nodeModel.hashCode());
			result = prime * result + ((script == null) ? 0 : script.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			NodeWrapper other = (NodeWrapper) obj;
			if (nodeModel != other.nodeModel)
				return false;
			if (script == null) {
				if (other.script != null)
					return false;
			}
			return script.equals(other.script);
		}

		@Override
		public String toString() {
			return nodeModel + "[" + script + "]";
		}
	}

	private final UniqueStack<NodeWrapper> stack = new UniqueStack<NodeWrapper>();

	public ScriptContext() {
	}

	public void accessNode(final NodeModel accessedNode) {
		FormulaUtils.accessNode(stack.last().getNodeModel(), accessedNode);
	}

	public void accessBranch(final NodeModel accessedNode) {
		FormulaUtils.accessBranch(stack.last().getNodeModel(), accessedNode);
	}

	public void accessAll() {
		FormulaUtils.accessAll(stack.last().getNodeModel());
	}

	public boolean push(NodeModel nodeModel, String script) {
		return stack.push(new NodeWrapper(nodeModel, script));
	}

	public void pop() {
		stack.pop();
	}

	public NodeModel getStackFront() {
		return stack.first().getNodeModel();
	}

	@Override
	public String toString() {
		return stack.toString();
	}
}
