package org.freeplane.main.mindmapmode.stylemode;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.ui.MenuBuilder;

public class ControlToolbar extends Box {
	private int status = 0;
	public int getStatus() {
		return status;
	}

	public ControlToolbar() {
		super(BoxLayout.X_AXIS);
		add(Box.createHorizontalGlue());
		JButton okBtn = new JButton();
		MenuBuilder.setLabelAndMnemonic(okBtn, ResourceBundles.getText("ok"));
		okBtn.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				status = JOptionPane.OK_OPTION;
				closeDialog((Component)e.getSource());
				
			}
		});
		add(okBtn);
		add(Box.createHorizontalStrut(20));
		JButton cancelBtn = new JButton();
		MenuBuilder.setLabelAndMnemonic(cancelBtn, ResourceBundles.getText("cancel"));
		add(cancelBtn);
		cancelBtn.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				status = JOptionPane.CANCEL_OPTION;
				closeDialog((Component)e.getSource());
				
			}
		});
		add(Box.createHorizontalGlue());
	}

	protected void closeDialog(Component source) {
		SwingUtilities.getWindowAncestor(source).setVisible(false);
		
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
