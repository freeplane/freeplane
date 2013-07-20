/**
 * author: Marcel Genzmehr
 * 04.11.2011
 */
package org.freeplane.plugin.workspace.components.menu;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;

/**
 * 
 */
public class WorkspaceToolBar extends JToolBar {
	protected static Insets nullInsets = new Insets(0, 0, 0, 0);
	
	public WorkspaceToolBar() {
		this.setMargin(WorkspaceToolBar.nullInsets);
		setFloatable(false);
		setRollover(true);
		
		JButton button = add(new AbstractAction("New", new ImageIcon(WorkspaceToolBar.class.getResource("/images/16x16/document-new-6.png"))) {
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent e) {
				//WorkspaceController.getController().actionPerformed(e);				
			}
		});
		configureComponent(button);
		this.addSeparator((Dimension) null);
		button = add(new AbstractAction("Delete", new ImageIcon(WorkspaceToolBar.class.getResource("/images/16x16/document-delete.png"))) {			
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent e) {
				//WorkspaceController.getController().actionPerformed(e);
			}
		});
		configureComponent(button);
	}

	private static final long serialVersionUID = 1L;
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
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
		abstractButton.setMargin(WorkspaceToolBar.nullInsets);
	}
}
