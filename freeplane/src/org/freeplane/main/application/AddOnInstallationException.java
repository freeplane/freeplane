package org.freeplane.main.application;

import java.util.ArrayList;
import java.util.Arrays;

public class AddOnInstallationException extends Exception {
	private static final long serialVersionUID = 0L;
	private final ArrayList<String> messages;

	public AddOnInstallationException(String... messages) {
		this.messages = new ArrayList<String>(Arrays.asList(messages));
    }
	
	public ArrayList<String> getMessages() {
		return messages;
	}
}
