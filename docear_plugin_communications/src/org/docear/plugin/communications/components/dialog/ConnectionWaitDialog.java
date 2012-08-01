package org.docear.plugin.communications.components.dialog;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.jdesktop.swingworker.SwingWorker;

public class ConnectionWaitDialog {
	private final DocearServiceConnectionWaitPanel waitPanel = new DocearServiceConnectionWaitPanel();
	private final JButton[] dialogButtons;
	private SwingWorker< ?, ?> worker;
	private Boolean started = false;	
	
	public ConnectionWaitDialog() {		
		dialogButtons = new JButton[] { new JButton(TextUtils.getOptionalText("docear.service.connect.dialog.button.cancel")) };
		dialogButtons[0].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(worker != null){
					worker.cancel(true);
					worker = null;
				}
				Container cont = waitPanel.getParent();
				closeDialogManually(cont);
			}
		});
	}
	
	public void start() {
		synchronized (started ) {
			if(!started) {
				started = true;
				JOptionPane.showOptionDialog(UITools.getFrame(), waitPanel, TextUtils.getOptionalText("docear.service.connect.title"), JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, dialogButtons, dialogButtons[0]);
			}
		}
		
	}
	
	public void stop() {
		synchronized (started) {
			if(started) {
				Container cont = waitPanel.getParent();
				closeDialogManually(cont);
				started = false;
			}
		}
	}
	
	public void setWorker(SwingWorker<?, ?> worker) {
		this.worker = worker;
	}
	
	private void closeDialogManually(Container container) {
		if(container == null) return;
		
		while (!(container instanceof JDialog)) {
			container = container.getParent();
		}
		((JDialog) container).dispose();		
	}

}
