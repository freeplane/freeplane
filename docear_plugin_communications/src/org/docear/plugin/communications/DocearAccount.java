package org.docear.plugin.communications;

import org.freeplane.plugin.accountmanager.*;

public class DocearAccount extends Account {
	final public static String VALIDATE = "docear_validate_credentials";
	
	public DocearAccount() {
		this.enableButton(DocearAccount.VALIDATE, DocearAccount.VALIDATE);
	}

	@Override
	public String getAccountName() {
		return "Docear";
	}
	
	
	
}
