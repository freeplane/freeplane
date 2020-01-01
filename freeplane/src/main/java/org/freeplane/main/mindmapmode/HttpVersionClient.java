package org.freeplane.main.mindmapmode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import javax.net.ssl.SSLHandshakeException;

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
		initialize(url, currentVersion);
	}

	private void initialize(final URL url, final FreeplaneVersion currentVersion) {
		try {
			if (isPropertyUrl(url))
			    parseProperties(url, currentVersion);
			else
				parseHistory(url, currentVersion);
		}
		catch (SSLHandshakeException ex) {
			if (url.getProtocol().equalsIgnoreCase("https")) {
				try {
					URL httpUrl = new URL(url.toString().replaceFirst(url.getProtocol(), "http"));
					initialize(httpUrl, currentVersion);
				} catch (MalformedURLException e) {
					LogUtils.severe(e);
				}
			}
		}
		catch (final NullPointerException e) {
		    LogUtils.warn("problem with update check for url (" + url + ")", e);
		}
		catch (final IOException e) {
			LogUtils.warn("Could not read update url (" + url + ") - check your internet connection.");
		}
		catch (final IllegalArgumentException e) {
            LogUtils.warn("Could not read version from " + url + ":" + e.getMessage());
		}
	}

    private boolean isPropertyUrl(final URL url) {
        return url.getPath() != null && url.getPath().endsWith(".properties");
    }

    private boolean parseProperties(final URL url, final FreeplaneVersion currentVersion) throws IOException {
        Properties versionProperties = new Properties();
        versionProperties.load(new InputStreamReader(url.openConnection().getInputStream(), StandardCharsets.UTF_8));

        // if the 'version' property doesn't exist, an IllegalArgumentException will be raised since it's mandatory
        if (versionProperties.getProperty("version") != null) {
            remoteVersion = FreeplaneVersion.getVersion(versionProperties.getProperty("version"));
            successful = true;
            // optional properties
            remoteVersionDownloadUrl = parseUrl(versionProperties.getProperty("downloadUrl"));
            remoteVersionChangelogUrl = parseUrl(versionProperties.getProperty("changelogUrl"));
            return remoteVersion.compareTo(currentVersion) > 0;
        } else {
            LogUtils.warn("add-on update: no version found in " + url);
            return false;
        }
    }

    private void parseHistory(final URL url, final FreeplaneVersion currentVersion) throws IOException {
        BufferedReader in = null;
        try {
            // "version.txt" format
            in = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream(), StandardCharsets.UTF_8));
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
            if (remoteVersion.compareTo(currentVersion) > 0) {
                parseHistory(currentVersion, in, line);
            }
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (final IOException e) {
                    LogUtils.warn("Couldn't close buffered reader.");
                }
            }
        }
    }

    private void parseHistory(FreeplaneVersion currentVersion, BufferedReader in, String firstLine) throws IOException {
        final StringBuilder historyBuffer = new StringBuilder();
        historyBuffer.append(firstLine);
        historyBuffer.append('\n');
        for (String line = in.readLine(); line != null; line = in.readLine()) {
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

    private URL parseUrl(final String urlString) {
        try {
            return new URL(urlString);
        } catch (final MalformedURLException e) {
        	return null;
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
