package org.freeplane.plugin.script;

import java.net.URL;

import org.freeplane.features.map.NodeModel;

public class NodeScript {
	final NodeModel nodeModel;
	final String script;

	public NodeScript(NodeModel nodeModel, String script) {
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
		NodeScript other = (NodeScript) obj;
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

	public URL getBaseUrl() {
		return nodeModel.getMap().getURL();
	}
}