package org.freeplane.plugin.workspace.dnd;

import java.awt.datatransfer.ClipboardOwner;

public interface IWorspaceClipboardOwner extends ClipboardOwner {
	public WorkspaceTransferable getTransferable();
}
