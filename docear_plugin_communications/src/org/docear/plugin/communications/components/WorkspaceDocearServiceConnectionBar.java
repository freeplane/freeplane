package org.docear.plugin.communications.components;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.event.DocearEvent;
import org.freeplane.core.util.TextUtils;

public class WorkspaceDocearServiceConnectionBar extends JToolBar {	

	private static final long serialVersionUID = 1L;
	public static final String ACTION_COMMAND_TOGGLE_CONNECTION_STATE = "toggle_connection_state";
	
	protected static Insets nullInsets = new Insets(0, 0, 0, 0);
	protected static Insets marginInsets = new Insets(2, 2, 2, 2);
	
	private static Icon onIcon = new ImageIcon(WorkspaceDocearServiceConnectionBar.class.getResource("/images/arrow-refresh-on.png"));
	private static Icon offIcon = new ImageIcon(WorkspaceDocearServiceConnectionBar.class.getResource("/images/arrow-refresh-off.gif"));
	
	
	
	private final JButton button;
	private final JLabel lblUsername;
	private final JLabel lblConnectedAs;
	
	public WorkspaceDocearServiceConnectionBar() {
		setMargin(nullInsets);
		setFloatable(false);
		setRollover(true);
		
		lblConnectedAs = new JLabel(TextUtils.getText("docear.service.connect.bar.label")+":");
		lblConnectedAs.setBorder(new EmptyBorder(marginInsets));
		add(lblConnectedAs);
		
		lblUsername = new JLabel("username");
		lblUsername.setBorder(new EmptyBorder(marginInsets));
		add(lblUsername);
		
		button = add(new AbstractAction("Connection", onIcon) {
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent e) {
				DocearController.getController().dispatchDocearEvent(new DocearEvent(WorkspaceDocearServiceConnectionBar.this, ACTION_COMMAND_TOGGLE_CONNECTION_STATE));				
			}
		});
		configureComponent(button);
		button.setDisabledIcon(new ImageIcon(this.getClass().getResource("/images/arrow-refresh-disabled.png")));
	}
	
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	public void setUsername(String name) {
		this.lblUsername.setText(name);
	}
	
	public void setEnabled(boolean enabled) {
		this.button.setEnabled(enabled);
		lblUsername.setEnabled(enabled);
		lblConnectedAs.setEnabled(enabled);
		super.setEnabled(enabled);
	}
	
	public void setConnectionState(boolean enabled) {
		if(enabled) {
			button.setIcon(onIcon);
		}
		else {
			button.setIcon(offIcon);
		}
		button.repaint();
	}
	
 	
	protected void configureComponent(final Component comp) {
		if (!(comp instanceof AbstractButton)) {
			return;
		}
		final AbstractButton abstractButton = (AbstractButton) comp;
		final String actionName = (String) abstractButton.getAction().getValue(Action.NAME);
		abstractButton.setName(actionName);
		if (null != abstractButton.getIcon()) {
			final String text = abstractButton.getText();
			final String toolTipText = abstractButton.getToolTipText();
			if (text != null) {
				if (toolTipText == null) {
					abstractButton.setToolTipText(text);
				}
				abstractButton.setText(null);
			}
		}
		if (System.getProperty("os.name").equals("Mac OS X")) {
			abstractButton.putClientProperty("JButton.buttonType", "segmented");
			abstractButton.putClientProperty("JButton.segmentPosition", "middle");
			final Dimension buttonSize = new Dimension(22, 22);
			abstractButton.setPreferredSize(buttonSize);
			abstractButton.setFocusPainted(false);
		}
		abstractButton.setFocusable(false);
		abstractButton.setMargin(nullInsets);
	}
}
