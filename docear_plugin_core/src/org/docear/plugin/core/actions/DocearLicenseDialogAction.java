package org.docear.plugin.core.actions;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
import org.docear.plugin.core.ui.DocearLicenseDialogPanel;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;

public class DocearLicenseDialogAction extends AFreeplaneAction {	

	private static final long serialVersionUID = 1L;
	
	public static final String KEY = "DocearLicenseDialogAction";
	
	public DocearLicenseDialogAction() {
		super(KEY);
	}
	
	public void actionPerformed(ActionEvent e) {
		showDialog();
	}

	public static void showDialog() {
		final DocearLicenseDialogPanel dialog = new DocearLicenseDialogPanel();
		
		ArrayList<JButton> buttonsList = new ArrayList<JButton>();
		buttonsList.add(new JButton(TextUtils.getText("docear.license.button.ok")));
		buttonsList.add(new JButton(TextUtils.getText("docear.license.button.cancel")));
		
		
		
		JButton[] dialogButtons = buttonsList.toArray(new JButton[] {});
		
		dialogButtons[0].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Container cont = dialog.getParent();
				while(!(cont instanceof JOptionPane)) {
					cont = cont.getParent();
				}
				((JOptionPane)cont).setValue(e.getSource());				
			}
		});
		
		dialog.integrateButtons(dialogButtons);
		
		int result = JOptionPane.showOptionDialog(UITools.getFrame(), dialog, TextUtils.getText("docear.license.dialog.title"), JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, dialogButtons, 1);
		if (result == 0) {
			DocearController.getController().dispatchDocearEvent(new DocearEvent(dialog, DocearEventType.LICENSES_ACCEPTED));
		}	
		else {
			//DocearQuitAction.quit(dialog);
			System.exit(0);
		}
		
	}
	
	

}
