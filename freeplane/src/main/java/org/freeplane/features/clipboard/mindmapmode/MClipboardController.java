package org.freeplane.features.clipboard.mindmapmode;

import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;

import org.freeplane.features.clipboard.ClipboardController;

public interface MClipboardController extends ClipboardController {
	boolean canCut();
	void cut();

	boolean canPaste(Transferable t);
	void paste(ActionEvent e, Transferable t);
}
