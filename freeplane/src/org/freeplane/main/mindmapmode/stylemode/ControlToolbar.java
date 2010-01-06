package org.freeplane.main.mindmapmode.stylemode;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.MenuBuilder;

public class ControlToolbar {
	private int status = JOptionPane.DEFAULT_OPTION;
	final private AFreeplaneAction okAction;
	final private AFreeplaneAction cancelAction;
	private Component window;
	public AFreeplaneAction getOkAction() {
		return okAction;
	}

	public AFreeplaneAction getCancelAction() {
		return cancelAction;
	}

	public int getStatus() {
		return status;
	}

	public ControlToolbar(Controller controller, String key, Window window) {
		this.window = window;
		window.addComponentListener(new ComponentListener() {
			
			public void componentShown(ComponentEvent e) {
				status = JOptionPane.DEFAULT_OPTION;
			}
			
			public void componentResized(ComponentEvent e) {
			}
			
			public void componentMoved(ComponentEvent e) {
			}
			
			public void componentHidden(ComponentEvent e) {
			}
		});
		okAction = new AFreeplaneAction(key+".ok", controller) {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				status = JOptionPane.OK_OPTION;
				closeDialog((Component)e.getSource());
				
			}
		};
		cancelAction = new AFreeplaneAction(key+".cancel", controller) {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				status = JOptionPane.CANCEL_OPTION;
				closeDialog((Component)e.getSource());
				
			}
		};
	}

	protected void closeDialog(Component source) {
		window.setVisible(false);
		
	}
}
