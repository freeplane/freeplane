package org.freeplane.api;

public class NodeChanged {
	public enum ChangedElement{TEXT, DETAILS, NOTE, ICON, ATTRIBUTE, FORMULA_RESULT, UNKNOWN};
	private final Node node;
	private final ChangedElement changedElement;
	public NodeChanged(Node node, ChangedElement changedElement) {
		super();
		this.node = node;
		this.changedElement = changedElement;
	}
	public Node getNode() {
		return node;
	}
	public ChangedElement getChangedElement() {
		return changedElement;
	}
	@Override
	public String toString() {
		return "NodeChanged [node=" + node + ", changedElement=" + changedElement + "]";
	}
}
