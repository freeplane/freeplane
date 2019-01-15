package org.freeplane.main.application;

import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Compat;
import org.freeplane.features.mode.Controller;

public class Browser {
	public void openDocument(final URI uri) {
		String uriString = uri.toString();
		final String UNC_PREFIX = "file:////";
		if (uriString.startsWith(UNC_PREFIX)) {
			uriString = "file://" + uriString.substring(UNC_PREFIX.length());
		}
		final String osName = System.getProperty("os.name");
		if (osName.substring(0, 3).equals("Win")) {
			String propertyString = "default_browser_command_windows";
			if (osName.indexOf("9") != -1 || osName.indexOf("Me") != -1) {
				propertyString += "_9x";
			}
			else {
				propertyString += "_nt";
			}
			String[] command = null;
			try {
				final Object[] messageArguments = { uriString };
				final MessageFormat formatter = new MessageFormat(ResourceController.getResourceController()
				    .getProperty(propertyString));
				final String browserCommand = formatter.format(messageArguments);
				final String scheme = uri.getScheme();
                if (scheme.equals("file") || scheme.equals("smb")) {
                    if(scheme.equals("smb")){
                        uriString = Compat.smbUri2unc(uri);
                    }
					if (System.getProperty("os.name").startsWith("Windows 2000"))
						command = new String[] { "rundll32", "shell32.dll,ShellExec_RunDLL", uriString };
					else
	                    command = new String[] { "rundll32", "url.dll,FileProtocolHandler", uriString };
				}
				else if (uriString.startsWith("mailto:")) {
					command = new String[] { "rundll32", "url.dll,FileProtocolHandler", uriString };
				}
				else {
					Controller.exec(browserCommand);
					return;
				}
				Controller.exec(command);
			}
			catch (final IOException x) {
				UITools
				    .errorMessage("Could not invoke browser.\n\nFreeplane executed the following statement on a command line:\n\""
				            + command
				            + "\".\n\nYou may look at the user or default property called '"
				            + propertyString
				            + "'.");
				System.err.println("Caught: " + x);
			}
		}
		else if (osName.startsWith("Mac OS")) {
			String browserCommand = null;
			try {
				final Object[] messageArguments = { uriString, uriString };
				final MessageFormat formatter = new MessageFormat(ResourceController.getResourceController()
				    .getProperty("default_browser_command_mac"));
				browserCommand = formatter.format(messageArguments);
				Controller.exec(browserCommand);
			}
			catch (final IOException ex2) {
				UITools
				    .errorMessage("Could not invoke browser.\n\nFreeplane executed the following statement on a command line:\n\""
				            + browserCommand
				            + "\".\n\nYou may look at the user or default property called 'default_browser_command_mac'.");
				System.err.println("Caught: " + ex2);
			}
		}
		else {
			String browserCommand = null;
			try {
				final Object[] messageArguments = { uriString, uriString };
				final MessageFormat formatter = new MessageFormat(ResourceController.getResourceController()
				    .getProperty("default_browser_command_other_os"));
				browserCommand = formatter.format(messageArguments);
				Controller.exec(browserCommand);
			}
			catch (final IOException ex2) {
				UITools
				    .errorMessage("Could not invoke browser.\n\nFreeplane executed the following statement on a command line:\n\""
				            + browserCommand
				            + "\".\n\nYou may look at the user or default property called 'default_browser_command_other_os'.");
				System.err.println("Caught: " + ex2);
			}
		}
	}
}
