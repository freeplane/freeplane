package org.freeplane.plugin.workspace.handler;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.components.dialog.FileExistsDialogPanel;
import org.freeplane.plugin.workspace.io.CancelExecutionException;
import org.freeplane.plugin.workspace.io.IConflictHandler;
import org.freeplane.plugin.workspace.io.SkipTaskException;

public class DirectoryMergeConflictDialog implements IConflictHandler {

	public void resolveConflict(File file, Properties properties) throws IOException {
		if(properties == null) {
			properties = new Properties();
		}
		FileExistsDialogPanel dialog = new FileExistsDialogPanel(file, FileExistsDialogPanel.class.getSimpleName().toLowerCase(Locale.ENGLISH)+".dir.text");
		int opt = JOptionPane.showConfirmDialog(UITools.getFrame(), dialog, TextUtils.getText("workspace.directory.merge.title"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if(opt == JOptionPane.CANCEL_OPTION) {
			throw new CancelExecutionException();
		}
		if(opt == JOptionPane.NO_OPTION) {
			throw new SkipTaskException();
		}
		
		properties.setProperty("mergeAll", String.valueOf(dialog.applyToAll()));

	}

}
