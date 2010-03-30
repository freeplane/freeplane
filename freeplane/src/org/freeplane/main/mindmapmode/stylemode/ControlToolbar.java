package org.freeplane.main.mindmapmode.stylemode;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JOptionPane;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.AFreeplaneAction;

public class ControlToolbar {
	private int status = JOptionPane.DEFAULT_OPTION;
	final private AFreeplaneAction okAction;
	final private AFreeplaneAction cancelAction;
	private final Component window;

	public AFreeplaneAction getOkAction() {
		return okAction;
	}

	public AFreeplaneAction getCancelAction() {
		return cancelAction;
	}

	public int getStatus() {
		return status;
	}

	public ControlToolbar(final Controller controller, final String key, final Window window) {
		this.window = window;
		window.addComponentListener(new ComponentListener() {
			public void componentShown(final ComponentEvent e) {
				status = JOptionPane.DEFAULT_OPTION;
			}

			public void componentResized(final ComponentEvent e) {
			}

			public void componentMoved(final ComponentEvent e) {
			}

			public void componentHidden(final ComponentEvent e) {
			}
		});
		okAction = new AFreeplaneAction(key + ".ok", controller) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(final ActionEvent e) {
				status = JOptionPane.OK_OPTION;
				closeDialog((Component) e.getSource());
			}
		};
		cancelAction = new AFreeplaneAction(key + ".cancel", controller) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(final ActionEvent e) {
				status = JOptionPane.CANCEL_OPTION;
				closeDialog((Component) e.getSource());
			}
		};
	}

	protected void closeDialog(final Component source) {
		window.setVisible(false);
	}
}
