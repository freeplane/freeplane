package org.freeplane.main.osgi;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public interface ConnectionHandler {
	URLConnection openConnection(URL url)  throws IOException;
}
