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
				if(uri.getScheme().equals("file"))
					uriString = decodedString(uri);
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
	
    private String decodedString(URI uri) {

        StringBuffer sb = new StringBuffer();
        String scheme = uri.getScheme();
		if (scheme != null) {
            sb.append(scheme);
            sb.append(':');
        }
        if (uri.isOpaque()) {
            sb.append(uri.getSchemeSpecificPart());
        } else {
        	String host = uri.getHost();
            if (host != null) {
                sb.append("//");
                if (uri.getUserInfo() != null) {
                    sb.append(uri.getUserInfo());
                    sb.append('@');
                }
                boolean needBrackets = ((host.indexOf(':') >= 0)
                                    && !host.startsWith("[")
                                    && !host.endsWith("]"));
                if (needBrackets) sb.append('[');
                sb.append(host);
                if (needBrackets) sb.append(']');
                if (uri.getPort() != -1) {
                    sb.append(':');
                    sb.append(uri.getPort());
                }
            } else if (uri.getAuthority() != null) {
                sb.append("//");
                sb.append(uri.getAuthority());
            }
            if (uri.getPath() != null)
                sb.append(uri.getPath());
            if (uri.getQuery() != null) {
                sb.append('?');
                sb.append(uri.getQuery());
            }
        }
        if (uri.getFragment() != null) {
            sb.append('#');
            sb.append(uri.getFragment());
        }
        return sb.toString();
    }

}
