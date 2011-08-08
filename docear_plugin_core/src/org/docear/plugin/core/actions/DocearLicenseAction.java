package org.docear.plugin.core.actions;

import java.awt.event.ActionEvent;
import java.util.Scanner;

import org.docear.plugin.core.ui.DocearLicenseDialog;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.mode.Controller;

public class DocearLicenseAction extends AFreeplaneAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String licenseText;

	public DocearLicenseAction(String key) {
		super(key);		
		this.licenseText = this.readLicense();
	}

	public void actionPerformed(ActionEvent e) {
		new DocearLicenseDialog(this.licenseText).showDialog(Controller.getCurrentController().getViewController().getFrame());
	}
	
	private String readLicense() {
	    
		StringBuilder text = new StringBuilder();
	    String NL = System.getProperty("line.separator");
	    Scanner scanner = new Scanner(this.getClass().getClassLoader().getResourceAsStream("/license.txt"));
	    try {
	      while (scanner.hasNextLine()){
	        text.append(scanner.nextLine() + NL);
	      }
	    }
	    finally{
	      scanner.close();
	    }
	    
	    return text.toString();
	}

}
