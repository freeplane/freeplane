/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.core.util;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.StringTokenizer;

import org.freeplane.core.resources.ResourceController;

/** provides access to the current Freeplane version. In scripts use <code>c.freeplaneVersion</code>.
 * For usage instructions see {@link #compareTo(org.freeplane.api.FreeplaneVersion)}. */
public class FreeplaneVersion implements org.freeplane.api.FreeplaneVersion {
	private static final FreeplaneVersion VERSION = FreeplaneVersion.loadVersion();
	public static final String VERSION_KEY = "freeplane_version";
	public static final String VERSION_PROPERTIES = "/version.properties";
	public static final String XML_VERSION = "freeplane 1.9.0";
	/** major version, the 1 in "1.0.38 rc" */
	private final int mMaj;
	/** mid version, the 0 in "1.0.38 rc" */
	private final int mMid;
	/** minor version, the 38 in "1.0.38 rc" */
	private final int mMin;
	/** optional patch level (testversion only). */
	private final int mNum;
	/** release type e.g. "", "rc", "beta", "alpha" or "nightly_build". */
	private String mType;

	private final String revision;

	public static FreeplaneVersion getVersion() {
		return VERSION;
	}

    /**
     * Parses a version string as FreeplaneVersion.
     * Ignores leading 'v' (e.g. "v1.2.6") and accept '.' and ' ' as separator.
     * @throws IllegalArgumentException on parse errors
     */
	public static FreeplaneVersion getVersion(final String pString) throws IllegalArgumentException {
		if (pString == null)
			return null;
		final StringTokenizer t = new StringTokenizer(pString, "v. ", false);
		final String[] info = new String[t.countTokens()];
		int i = 0;
		while (t.hasMoreTokens()) {
			info[i++] = t.nextToken();
		}
		if (info.length < 2 | info.length > 5) {
			throw new IllegalArgumentException("Wrong number of tokens for version information: " + pString);
		}
		try {
			final int maj = Integer.parseInt(info[0]);
			final int mid = Integer.parseInt(info[1]);
			final int min = info.length < 3 ? 0 : Integer.parseInt(info[2]);
			final String type = info.length < 4 ? "" : info[3];
			final int num = info.length < 5 ? 0 : Integer.parseInt(info[4]);
			return new FreeplaneVersion(maj, mid, min, type, num, loadRevision());
		}
		catch (final NumberFormatException e) {
			throw new IllegalArgumentException("Wrong version token: " + pString, e);
		}
	}

	private static FreeplaneVersion loadVersion() {
		final Properties versionProperties = FileUtils.loadProperties(VERSION_PROPERTIES);
		final String versionString = versionProperties.getProperty(VERSION_KEY);
		final String versionStatus = versionProperties.getProperty("freeplane_version_status");
		final FreeplaneVersion version = FreeplaneVersion.getVersion(versionString);
		version.mType = versionStatus;
		return version;
	}

	@Override
	public int getMaj() {
		return mMaj;
	}

	@Override
	public int getMid() {
		return mMid;
	}

	@Override
	public int getMin() {
		return mMin;
	}

	@Override
	public int getNum() {
		return mNum;
	}

	@Override
	public String getType() {
		return mType;
	}

	@Override
	public String getRevision(){
		return revision;
	}

	public FreeplaneVersion(final int pMaj, final int pMid, final int pMin, final String pType, final int pNum, final String revision) {
		super();
		mMaj = pMaj;
		mMid = pMid;
		mMin = pMin;
		mType = pType;
		mNum = pNum;
		this.revision = revision;
	}

	public FreeplaneVersion(final int pMaj, final int pMid, final int pMin) {
		this(pMaj, pMid, pMin, "", 0, "");
	}

	/** Use it like this:
	 * <pre>
     *   import org.freeplane.core.util.FreeplaneVersion
     *   def required = FreeplaneVersion.getVersion("1.2.20")
     *   if (c.freeplaneVersion &lt; required)
	 *       ui.errorMessage("Freeplane version ${c.freeplaneVersion}"
	 *           + " not supported - update to at least ${required}")
	 * </pre>
	 */
	@Override
	public int compareTo(final org.freeplane.api.FreeplaneVersion o) {
		if (mMaj < o.getMaj()) {
			return -1;
		}
		if (mMaj > o.getMaj()) {
			return 1;
		}
		if (mMid < o.getMid()) {
			return -1;
		}
		if (mMid > o.getMid()) {
			return 1;
		}
		if (mMin < o.getMin()) {
			return -1;
		}
		if (mMin > o.getMin()) {
			return 1;
		}
		if (mNum < o.getNum()) {
			return -1;
		}
		if (mNum > o.getNum()) {
			return 1;
		}
		return 0;
	}

	/** returns the full version number, e.g. "1.0.38 rc". */
	@Override
	public String toString() {
		final StringBuilder buf = new StringBuilder();
		buf.append(mMaj);
		buf.append('.');
		buf.append(mMid);
		buf.append('.');
		buf.append(mMin);
		if (!mType.equals("")) {
			buf.append(' ');
			buf.append(mType);
		}
		if (mNum != 0) {
			buf.append(' ');
			buf.append(mNum);
		}
		return buf.toString();
	}

	/** returns the version number only, e.g. "1.0.38". */
	@Override
	public String numberToString() {
		final StringBuilder buf = new StringBuilder();
		buf.append(mMaj);
		buf.append('.');
		buf.append(mMid);
		buf.append('.');
		buf.append(mMin);
		return buf.toString();
	}

	@Override
	public boolean isOlderThan(org.freeplane.api.FreeplaneVersion freeplaneVersion) {
	    return compareTo(freeplaneVersion) < 0;
    }

	@Override
	public boolean isNewerThan(org.freeplane.api.FreeplaneVersion freeplaneVersion) {
		return compareTo(freeplaneVersion) > 0;
	}

	@Override
	public boolean isFinal(){
		return "".equals(mType);
	}

	private static String loadRevision() {
		final URL gitInfo = ResourceController.getResourceController().getResource("/gitinfo.properties");
		final String revision;
		if(gitInfo != null){
			Properties gitProps = new Properties();
			try {
		        gitProps.load(gitInfo.openStream());
		    }
		    catch (IOException e) {
		    }
			revision = gitProps.getProperty("git-revision", "");
		}
		else{
			revision = "";
		}
		return revision;
	}
}
