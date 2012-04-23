package org.docear.plugin.communications.components.dialog;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;

public class ConnectionWaitDialog {
	private final DocearServiceConnectionWaitPanel waitPanel = new DocearServiceConnectionWaitPanel();
	private final JButton[] dialogButtons;
	
	public ConnectionWaitDialog() {
		dialogButtons = new JButton[] { new JButton(TextUtils.getOptionalText("docear.service.connect.dialog.button.cancel")) };
		dialogButtons[0].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Container cont = waitPanel.getParent();
				closeDialogManually(cont);
			}
		});
	}
	public void start() {			
		Thread waitRunner = new Thread(new Runnable() {
			public void run() {
				int choice = JOptionPane.showOptionDialog(UITools.getFrame(), waitPanel, TextUtils.getOptionalText("docear.service.connect.title"), JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, dialogButtons, dialogButtons[0]);
				if (choice == 0) {
					// try to interrupt the connection process
				}
			}
		});
		waitRunner.start();
	}
	
	public void stop() {
		dialogButtons[0].doClick();
	}
	
	
	private void closeDialogManually(Container container) {
		while (!(container instanceof JDialog)) {
			container = container.getParent();
		}
		((JDialog) container).dispose();
	}

}
