package org.freeplane.plugin.workspace.io;

import java.io.IOException;

public class CancelExecutionException extends IOException {

	private static final long serialVersionUID = 1L;

	@Override
	public String getMessage() {
		return "execution canceled";
	}
	
}
