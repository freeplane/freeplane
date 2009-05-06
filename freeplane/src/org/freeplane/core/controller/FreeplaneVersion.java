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
package org.freeplane.core.controller;

import java.util.Properties;
import java.util.StringTokenizer;

import org.freeplane.core.util.ResUtil;

// TODO rladstaetter 15.02.2009 use build properties for this information
@Deprecated
public class FreeplaneVersion {
	private static final FreeplaneVersion VERSION = FreeplaneVersion.loadVersion();
	public static final String VERSION_KEY = "freeplane_version";
	public static final String VERSION_PROPERTIES = "/version.properties";
	public static final String XML_VERSION = "0.9.0";

	public static FreeplaneVersion getVersion() {
		return VERSION;
	}

	private static FreeplaneVersion loadVersion() {
		final Properties versionProperties = ResUtil.loadProperties(VERSION_PROPERTIES);
		final String versionString = versionProperties.getProperty(VERSION_KEY);
		final String versionStatus = versionProperties.getProperty("freeplane_version_status");
		return new FreeplaneVersion(versionString, versionStatus);
	}

	final int mMaj;
	final int mMid;
	final int mMin;
	final int mNum;
	String mType;

	public FreeplaneVersion(final int pMaj, final int pMid, final int pMin, final String pType, final int pNum) {
		super();
		mMaj = pMaj;
		mMid = pMid;
		mMin = pMin;
		mType = pType;
		mNum = pNum;
	}

	public FreeplaneVersion(final String pString) {
		final StringTokenizer t = new StringTokenizer(pString, ". ", false);
		final String[] info = new String[t.countTokens()];
		int i = 0;
		while (t.hasMoreTokens()) {
			info[i++] = t.nextToken();
		}
		if (info.length < 3 | info.length > 5) {
			throw new IllegalArgumentException("Wrong number of tokens for version information: " + pString);
		}
		mMaj = Integer.parseInt(info[0]);
		mMid = Integer.parseInt(info[1]);
		mMin = Integer.parseInt(info[2]);
		if (info.length == 3) {
			mType = "";
			mNum = 0;
			return;
		}
		mType = info[3];
		if (info.length == 4) {
			mNum = 0;
			return;
		}
		mNum = Integer.parseInt(info[4]);
	}

	public FreeplaneVersion(final String versionString, final String versionStatus) {
		this(versionString);
		mType = versionStatus;
	}

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
}
