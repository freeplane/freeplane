/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2011 dimitry
 *
 *  This file author is dimitry
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.main.application.protocols.freeplaneresource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.security.Permission;
import java.util.List;
import java.util.Map;

import org.freeplane.core.resources.ResourceController;

/** A {@link URLStreamHandler} that handles resources on the classpath. */
public class Handler extends URLStreamHandler {

    public Handler() {
    }
    public Handler(ClassLoader classLoader) {
    }

    @Override
    public URLConnection openConnection(URL url) throws IOException {
        final URL resourceUrl = ResourceController.getResourceController().getResource(url.getPath());
        if(resourceUrl == null)
        	throw new IOException("Unable to connect to: " + url.toExternalForm());
        final URLConnection connection = resourceUrl.openConnection();
		return new WrappedConnection(url, connection);
    }
}

class WrappedConnection extends URLConnection{
	private URLConnection connection;

	public WrappedConnection(URL url, URLConnection connection) {
		super(url);
		this.connection = connection;
	}

	public int hashCode() {
		return connection.hashCode();
	}

	public boolean equals(Object obj) {
		return connection.equals(obj);
	}

	public void connect() throws IOException {
		connection.connect();
	}

	public void setConnectTimeout(int timeout) {
		connection.setConnectTimeout(timeout);
	}

	public int getConnectTimeout() {
		return connection.getConnectTimeout();
	}

	public void setReadTimeout(int timeout) {
		connection.setReadTimeout(timeout);
	}

	public int getReadTimeout() {
		return connection.getReadTimeout();
	}

	public int getContentLength() {
		return connection.getContentLength();
	}

	public long getContentLengthLong() {
		return connection.getContentLengthLong();
	}

	public String getContentType() {
		return connection.getContentType();
	}

	public String getContentEncoding() {
		return connection.getContentEncoding();
	}

	public long getExpiration() {
		return connection.getExpiration();
	}

	public long getDate() {
		return connection.getDate();
	}

	public long getLastModified() {
		return connection.getLastModified();
	}

	public String getHeaderField(String name) {
		return connection.getHeaderField(name);
	}

	public Map<String, List<String>> getHeaderFields() {
		return connection.getHeaderFields();
	}

	public int getHeaderFieldInt(String name, int Default) {
		return connection.getHeaderFieldInt(name, Default);
	}

	public long getHeaderFieldLong(String name, long Default) {
		return connection.getHeaderFieldLong(name, Default);
	}

	public long getHeaderFieldDate(String name, long Default) {
		return connection.getHeaderFieldDate(name, Default);
	}

	public String getHeaderFieldKey(int n) {
		return connection.getHeaderFieldKey(n);
	}

	public String getHeaderField(int n) {
		return connection.getHeaderField(n);
	}

	public Object getContent() throws IOException {
		return connection.getContent();
	}

	public Object getContent(Class[] classes) throws IOException {
		return connection.getContent(classes);
	}

	public Permission getPermission() throws IOException {
		return connection.getPermission();
	}

	public InputStream getInputStream() throws IOException {
		return connection.getInputStream();
	}

	public OutputStream getOutputStream() throws IOException {
		return connection.getOutputStream();
	}

	public void setDoInput(boolean doinput) {
		connection.setDoInput(doinput);
	}

	public boolean getDoInput() {
		return connection.getDoInput();
	}

	public void setDoOutput(boolean dooutput) {
		connection.setDoOutput(dooutput);
	}

	public boolean getDoOutput() {
		return connection.getDoOutput();
	}

	public void setAllowUserInteraction(boolean allowuserinteraction) {
		connection.setAllowUserInteraction(allowuserinteraction);
	}

	public boolean getAllowUserInteraction() {
		return connection.getAllowUserInteraction();
	}

	public void setUseCaches(boolean usecaches) {
		connection.setUseCaches(usecaches);
	}

	public boolean getUseCaches() {
		return connection.getUseCaches();
	}

	public void setIfModifiedSince(long ifmodifiedsince) {
		connection.setIfModifiedSince(ifmodifiedsince);
	}

	public long getIfModifiedSince() {
		return connection.getIfModifiedSince();
	}

	public boolean getDefaultUseCaches() {
		return connection.getDefaultUseCaches();
	}

	public void setDefaultUseCaches(boolean defaultusecaches) {
		connection.setDefaultUseCaches(defaultusecaches);
	}

	public void setRequestProperty(String key, String value) {
		connection.setRequestProperty(key, value);
	}

	public void addRequestProperty(String key, String value) {
		connection.addRequestProperty(key, value);
	}

	public String getRequestProperty(String key) {
		return connection.getRequestProperty(key);
	}

	public Map<String, List<String>> getRequestProperties() {
		return connection.getRequestProperties();
	}
	
}
