package org.freeplane.features.clipboard.mindmapmode;

import org.freeplane.features.clipboard.ClipboardController;

public interface MClipboardController extends ClipboardController {
	boolean canCut();
	void cut();

	boolean canPaste();
	void paste();
}
