package org.docear.plugin.core.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.ui.components.DocearLicensePanel;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;

public class DocearShowDataProcessingTermsAction extends AFreeplaneAction {

	private static final long serialVersionUID = 1L;
	public static final String key = "DocearShowDataProcessingTerms";
	final DocearLicensePanel licenseText = new DocearLicensePanel();

	public DocearShowDataProcessingTermsAction() {
		super(key);
		licenseText.setLicenseText(DocearController.getController().getDataProcessingTerms());
	}

	public void actionPerformed(ActionEvent e) {		
		JOptionPane.showConfirmDialog(UITools.getFrame(), licenseText, TextUtils.getText("docear.license.data_processing.title"), JOptionPane.PLAIN_MESSAGE, JOptionPane.PLAIN_MESSAGE, null);
	}

}
