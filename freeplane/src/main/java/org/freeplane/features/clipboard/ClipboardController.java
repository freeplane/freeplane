package org.freeplane.features.clipboard;

public interface ClipboardController{
	boolean canCopy();
	void copy();
	int getPriority();
}
