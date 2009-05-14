package org.freeplane.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.freeplane.core.controller.FreeplaneVersion;
import org.freeplane.core.util.LogTool;

/**
 * Handles communication with update webservice.
 * @author robert ladstaetter
 */
public class HttpVersionClient{
	private FreeplaneVersion remoteVersion;
	private String history;

	public HttpVersionClient(final String versionUrl, final FreeplaneVersion currentVersion) {
		remoteVersion = null;
		history = "";
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(new URL(versionUrl).openConnection().getInputStream()));
			String line;
			for(line = in.readLine(); 
			line != null && line.startsWith("====="); 
			line=in.readLine());
            if(line == null){
            	return;
            }
			remoteVersion = FreeplaneVersion.getVersion(line);
            if(remoteVersion.compareTo(currentVersion) <= 0){
            	return;
            }
			StringBuffer historyBuffer = new StringBuffer();
			historyBuffer.append(line);
			historyBuffer.append('\n');
			for(line = in.readLine(); line != null; line=in.readLine()){
				try {
	                final FreeplaneVersion version = FreeplaneVersion.getVersion(line);
	                if(version.compareTo(currentVersion) <= 0){
	                	break;
	                }
                }
                catch (IllegalArgumentException e) {
                }
				historyBuffer.append(line);
				historyBuffer.append('\n');
			}
			history = historyBuffer.toString();
		}
		catch (final MalformedURLException e) {
			LogTool.warn(e, "Url not well formed: " + versionUrl);
			return;
		}
		catch (final IOException e) {
			LogTool.warn(e, "Could not read update url - check your internet connection.");
			return;
		}
		catch (final IllegalArgumentException e){
			LogTool.warn(e, "Could not read version.");
			return;
		}
		finally {
			if (in != null) {
				try {
					in.close();
				}
				catch (final IOException e) {
					LogTool.warn("Couldn't close buffered reader.");
					return;
				}
			}
		}
	}

	public FreeplaneVersion getRemoteVersion() {
		return remoteVersion;
	}
	public String getHistory() {
    	return history;
    }
}
