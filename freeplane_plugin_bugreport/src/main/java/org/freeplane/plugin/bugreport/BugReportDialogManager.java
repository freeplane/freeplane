package org.freeplane.plugin.bugreport;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.freeplane.core.ui.components.UITools;

class BugReportDialogManager {
	static final String ALLOWED = "org.freeplane.plugin.bugreport.allowed";
	static final String ASK = "org.freeplane.plugin.bugreport.ask";
	static final String DENIED = "org.freeplane.plugin.bugreport.denied";

	static int showBugReportDialog(final String title, final String question, final int messageType,
	                               final Object[] options, final Object firstChoice, final String reportName,
	                               final String log) {
		final Box messagePane = Box.createVerticalBox();
		final JLabel messageLabel = new JLabel(question);
		messageLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		messagePane.add(messageLabel);
		messagePane.add(Box.createVerticalStrut(10));
		final JLabel messageLabel2 = new JLabel(reportName);
		messageLabel2.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		messagePane.add(messageLabel2);
		final JTextArea historyArea = new JTextArea(log);
		historyArea.setEditable(false);
		final JScrollPane historyPane = new JScrollPane(historyArea);
		historyPane.setPreferredSize(new Dimension(500, 300));
		historyPane.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		messagePane.add(historyPane);
		final int choice = JOptionPane.showOptionDialog(UITools.getCurrentRootComponent(), messagePane, title,
		    JOptionPane.DEFAULT_OPTION, messageType, null, options, firstChoice);
		return choice;
	}
}
