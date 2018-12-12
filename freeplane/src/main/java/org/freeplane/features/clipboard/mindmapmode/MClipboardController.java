package org.freeplane.features.clipboard.mindmapmode;

import java.awt.datatransfer.Transferable;

import org.freeplane.features.clipboard.ClipboardController;

public interface MClipboardController extends ClipboardController {
	boolean canCut();
	void cut();

	boolean canPaste(Transferable t);
	void paste(Transferable t);
}
