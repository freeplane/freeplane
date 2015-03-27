package org.freeplane.features.mode;

import java.io.IOException;

import org.freeplane.core.extension.IExtension;

public interface IControllerExecuteExtension extends IExtension {
	public void exec(final String string, boolean waitFor) throws IOException;
	public void exec(final String[] command, boolean waitFor) throws IOException;
}
