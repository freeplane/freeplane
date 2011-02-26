package org.freeplane.features.common.text;

import java.awt.event.KeyEvent;

import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.mindmapmode.text.EditNodeBase;
import org.freeplane.features.mindmapmode.text.EditNodeBase.IEditControl;

public abstract class AbstractTextTransformer implements ITextTransformer {
	private int priority;

	public AbstractTextTransformer(int priority) {
		this.priority = priority;
	}

	public EditNodeBase createEditNodeBase(NodeModel nodeModel, String text, IEditControl editControl,
	                                       KeyEvent firstEvent, boolean isNewNode, boolean editLong) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getPriority() {
		return priority;
	}

	public int compareTo(ITextTransformer that) {
		int thatPriority = that.getPriority();
		return (this.priority < thatPriority ? -1 : (this.priority == thatPriority ? 0 : 1));
	}
}
