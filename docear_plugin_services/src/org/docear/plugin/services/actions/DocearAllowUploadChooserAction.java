package org.docear.plugin.services.actions;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.components.dialog.DocearIRChoiceDialogPanel;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;

public class DocearAllowUploadChooserAction extends AFreeplaneAction {

	public static final String KEY = "DocearAllowUploadChooserAction";
	
	public DocearAllowUploadChooserAction() {
		super(KEY);
	}


	private static final long serialVersionUID = 1L;

	
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
	}

	public static void showDialog(boolean showCancelButton) {
		final DocearIRChoiceDialogPanel chooser = new DocearIRChoiceDialogPanel(showCancelButton);
		
		ArrayList<JButton> buttonsList = new ArrayList<JButton>();
		buttonsList.add(new JButton(TextUtils.getText("docear.uploadchooser.button.ok")));
		if (showCancelButton) {
			buttonsList.add(new JButton(TextUtils.getText("docear.uploadchooser.button.cancel")));
		}
		
		
		JButton[] dialogButtons = buttonsList.toArray(new JButton[] {});
		
//		dialogButtons[0].addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				Container cont = chooser.getParent();
//				while(!(cont instanceof JOptionPane)) {
//					cont = cont.getParent();
//				}
//				((JOptionPane)cont).setValue(e.getSource());				
//			}
//		});
		
		chooser.integrateButtons(dialogButtons);
		
		int result = JOptionPane.showOptionDialog(UITools.getFrame(), chooser, TextUtils.getText("docear.uploadchooser.title"), JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, dialogButtons, 1);
		if (result == 0) {			
			ServiceController.getController().setBackupEnabled(chooser.allowBackup());
			ServiceController.getController().setInformationRetrievalCode(chooser.getIrCode());
		}
		
	}


}
