package org.freeplane.main.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.freeplane.core.util.FreeplaneVersion;
import org.freeplane.core.util.LogUtils;

/**
 * Handles communication with update webservice.
 * @author robert ladstaetter
 */
class HttpVersionClient {
	private static URL getUrl(final String versionUrl) {
		try {
			return new URL(versionUrl);
		}
		catch (final MalformedURLException e) {
			return null;
		}
	}

	private String history;
	private FreeplaneVersion remoteVersion;
	private boolean successful;
	private URL remoteVersionDownloadUrl;
	private URL remoteVersionChangelogUrl;

	public HttpVersionClient(final String versionUrl, final FreeplaneVersion currentVersion) {
		this(HttpVersionClient.getUrl(versionUrl), currentVersion);
	}

	public HttpVersionClient(final URL url, final FreeplaneVersion currentVersion) {
		remoteVersion = null;
		history = "";
		successful = false;
		BufferedReader in = null;
		
		// get file format to use the good parser later
		String fileFormat = "default";
		if ( (url.getPath() != null) && (url.getPath().length() > 11)) {
			// valid file formats :
			// - '.properties'
			fileFormat = url.getPath().substring(url.getPath().length() - 11, url.getPath().length());
		}
		
		try {
			if (fileFormat.equals(".properties")) {
				// properties format
				Properties versionProperties = new Properties();
				versionProperties.load(new InputStreamReader(url.openConnection().getInputStream()));

				// if the 'version' property doesn't exist, an IllegalArgumentException will be raised since it's mandatory
				if (versionProperties.getProperty("version") != null) {
					remoteVersion = FreeplaneVersion.getVersion(versionProperties.getProperty("version"));
					successful = true;
					if (remoteVersion.compareTo(currentVersion) <= 0) {
						return;
					}
					
					// parsing optionnal properties
					try {
						remoteVersionDownloadUrl = new URL(versionProperties.getProperty("downloadUrl"));
					} catch (final MalformedURLException e) {
						remoteVersionDownloadUrl = null;
					}
					
					try {
						remoteVersionChangelogUrl = new URL(versionProperties.getProperty("changelogUrl"));
					} catch (final MalformedURLException e) {
						remoteVersionChangelogUrl = null;
					}
					
					
				} else {
					throw new IllegalArgumentException();
				}
			} else {
				// "version.txt" format
				in = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()));
				
				String line = in.readLine();
				while (line != null && !line.startsWith("=====")) {
					line = in.readLine();
				}
				while (line != null && line.startsWith("=====")) {
					line = in.readLine();
				}
				if (line == null) {
					return;
				}
				remoteVersion = FreeplaneVersion.getVersion(line);
				successful = true;
				if (remoteVersion.compareTo(currentVersion) <= 0) {
					return;
				}
				final StringBuilder historyBuffer = new StringBuilder();
				historyBuffer.append(line);
				historyBuffer.append('\n');
				for (line = in.readLine(); line != null; line = in.readLine()) {
					try {
						final FreeplaneVersion version = FreeplaneVersion.getVersion(line);
						if (version.compareTo(currentVersion) <= 0) {
							break;
						}
					}
					catch (final IllegalArgumentException e) {
					}
					historyBuffer.append(line);
					historyBuffer.append('\n');
				}
				history = historyBuffer.toString();
			}
		}
		catch (final NullPointerException e) {
			return;
		}
		catch (final IOException e) {
			LogUtils.warn("Could not read update url (" + url + ") - check your internet connection.");
			return;
		}
		catch (final IllegalArgumentException e) {
			LogUtils.warn("Could not read version.");
			return;
		}
		finally {
			if (in != null) {
				try {
					in.close();
				}
				catch (final IOException e) {
					LogUtils.warn("Couldn't close buffered reader.");
					return;
				}
			}
		}
	}

	public String getHistory() {
		return history;
	}

	public FreeplaneVersion getRemoteVersion() {
		return remoteVersion;
	}

	public URL getRemoteVersionDownloadUrl() {
		return remoteVersionDownloadUrl;
	}

	public URL getRemoteVersionChangelogUrl() {
		return remoteVersionChangelogUrl;
	}
	
	
	public boolean isSuccessful() {
		return successful;
	}
}
