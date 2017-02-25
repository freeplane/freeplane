package org.freeplane.features.text;

import javax.swing.Icon;

import org.freeplane.features.map.NodeModel;

public abstract class AbstractContentTransformer implements IContentTransformer {
	private int priority;

	public AbstractContentTransformer(int priority) {
		this.priority = priority;
	}

	public int getPriority() {
		return priority;
	}

	public int compareTo(IContentTransformer that) {
		int thatPriority = that.getPriority();
		return (this.priority < thatPriority ? -1 : (this.priority == thatPriority ? 0 : 1));
	}

	public boolean markTransformation() {
	    return false;
    }

	public Icon getIcon(TextController textController, Object content,
			NodeModel node, Object transformedExtension) {
		return null;
	}

	@Override
	public boolean isFormula(TextController textController, Object content, NodeModel node,
			Object transformedExtension) {
		return false;
	}
	
}
