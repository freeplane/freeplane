package org.freeplane.features.treetable.mindmapmode;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;

class TreeTableDeletionConfirmation {

	static boolean isConfirmed(final String message) {
		final JButton deleteButton = new JButton(TextUtils.getText("delete"));
		final JButton cancelButton = new JButton(TextUtils.getText("cancel"));
		final JOptionPane optionPane = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE,
			JOptionPane.DEFAULT_OPTION, null, new Object[] { deleteButton, cancelButton }, cancelButton);
		final JDialog dialog = optionPane.createDialog(UITools.getCurrentRootComponent(),
			TextUtils.getText("treetable.confirm_title"));
		deleteButton.addActionListener(event -> {
			optionPane.setValue(JOptionPane.YES_OPTION);
			dialog.dispose();
		});
		cancelButton.addActionListener(event -> dialog.dispose());
		dialog.getRootPane().setDefaultButton(cancelButton);
		UITools.addEscapeActionToDialog(dialog);
		dialog.setVisible(true);
		dialog.dispose();
		return Integer.valueOf(JOptionPane.YES_OPTION).equals(optionPane.getValue());
	}

	private TreeTableDeletionConfirmation() {
	}
}
