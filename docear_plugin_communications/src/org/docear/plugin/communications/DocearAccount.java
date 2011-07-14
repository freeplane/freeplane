package org.docear.plugin.communications;

import org.freeplane.plugin.accountmanager.*;

public class DocearAccount extends Account {
	
	public DocearAccount() {
		this.enableButton("docear_validate_credentials", "docear_validate_credentials");
	}

	@Override
	public String getAccountName() {
		return "Docear";
	}
	
	
	
}
