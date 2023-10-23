package org.freeplane.main.application;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.Hyperlink;
import org.freeplane.features.mode.Controller;

public class Browser {
	public void openDocument(final Hyperlink link) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop == null || !desktop.isSupported(Desktop.Action.BROWSE)) {
			openDocumentNotSupportedByDesktop(link);
			return;
		}
		String uriString = link.toString();
		final String UNC_PREFIX = "file:////";
		try {
			URI uri;
			if (uriString.startsWith(UNC_PREFIX)) {
				uriString = "file://" + uriString.substring(UNC_PREFIX.length());
				uri = new URI(uriString);
			}
			else
				uri = link.getUri();
			desktop.browse(uri);
		} catch (Exception e) {
			openDocumentNotSupportedByDesktop(link);
		}
	}

	private void openDocumentNotSupportedByDesktop(final Hyperlink link) {
		String uriString = link.toString();
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
				final String scheme = link.getScheme();
                if (scheme.equals("file") || scheme.equals("smb")) {
                    if(scheme.equals("smb")){
                        uriString = Compat.smbUri2unc(link.getUri());
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
				if(link.getScheme().equals("file"))
					uriString = link.getUri().getPath();
				browserCommand = ResourceController.getResourceController().getProperty("default_browser_command_mac");
				Controller.exec(new String[]{browserCommand, uriString});
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
				browserCommand = ResourceController.getResourceController().getProperty("default_browser_command_other_os");
				Controller.exec(new String[]{browserCommand, uriString});
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
