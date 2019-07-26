package org.freeplane.main.osgi;

import java.net.URL;

import com.github.robtimus.net.protocol.data.DataURLConnection;
import com.github.robtimus.net.protocol.data.Handler;

class DataHandler extends Handler implements ConnectionHandler{

	@Override
	public DataURLConnection openConnection(URL u) {
		return super.openConnection(u);
	}

}
