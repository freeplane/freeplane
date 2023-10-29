package org.freeplane.features.clipboard;

public interface ClipboardController{
	enum CopiedNodeSet {ALL_NODES, FILTERED_NODES}
	boolean canCopy();
	void copy();
	int getPriority();
}
