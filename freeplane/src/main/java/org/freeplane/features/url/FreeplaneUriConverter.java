/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2014 Dimitry
 *
 *  This file author is Dimitry
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
package org.freeplane.features.url;

import static org.freeplane.features.url.UrlManager.FREEPLANE_SCHEME;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;


/**
 * @author Dimitry Polivaev
 * 12.01.2014
 */
public class FreeplaneUriConverter{
	private static final String ENCODED_SPACE = "/%20";
	private static final String ENCODED_FREEPLANE_URI_PREFIX = FREEPLANE_SCHEME + ":" + ENCODED_SPACE;
	private static final String INTERNET_EXPLORER_FREEPLANE_URI_PREFIX = FREEPLANE_SCHEME + ":" + "/ ";
	private static final String MS_OFFICE_FREEPLANE_URI_PREFIX = FREEPLANE_SCHEME + ":" + "// ";
	private static final String[] MICROSOFT_URI_PREFIXES = new String[] {INTERNET_EXPLORER_FREEPLANE_URI_PREFIX, MS_OFFICE_FREEPLANE_URI_PREFIX};

	public String freeplaneUriForFile(final String fileBasedUri) {
		return ENCODED_FREEPLANE_URI_PREFIX + fileBasedUri.substring(UrlManager.FILE_SCHEME.length() + 1);
    }

	public URL freeplaneUrl(URI uri) throws MalformedURLException {
	    final String scheme = uri.getScheme();
	    if(FREEPLANE_SCHEME.equals(scheme)){
	    	return new URL(UrlManager.FILE_SCHEME, uri.getHost(), uri.getPath().substring(2));
	    }
	    else
	    	return new URL(scheme, uri.getHost(), uri.getPort(), uri.getPath());
    }

	public String fixPartiallyDecodedFreeplaneUriComingFromInternetExplorer(String uriCandidate) {
		for (String microsoftPrefix : MICROSOFT_URI_PREFIXES)
			if(uriCandidate.startsWith(microsoftPrefix)){
				int referenceStart = uriCandidate.indexOf('#');
				if(referenceStart == -1)
					referenceStart = uriCandidate.length();
				URI uri;
				try {
					String path = uriCandidate.substring(microsoftPrefix.length(), referenceStart);
					uri = new URI(FREEPLANE_SCHEME, path, null);
					String encodedPath = uri.getRawPath();
					return ENCODED_FREEPLANE_URI_PREFIX + encodedPath + uriCandidate.substring(referenceStart);
				}
				catch (URISyntaxException e) {
					return uriCandidate;
				}
			}
		return uriCandidate;

    }

}
