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

public class FreeplaneVersion implements Comparable<FreeplaneVersion> {
	private static final FreeplaneVersion VERSION = FreeplaneVersion.loadVersion();
	public static final String VERSION_KEY = "freeplane_version";
	public static final String VERSION_PROPERTIES = "/version.properties";
	public static final String XML_VERSION = "0.9.0";

	public static FreeplaneVersion getVersion() {
		return VERSION;
	}

	public static FreeplaneVersion getVersion(final String pString) throws IllegalArgumentException {
		final StringTokenizer t = new StringTokenizer(pString, ". ", false);
		final String[] info = new String[t.countTokens()];
		int i = 0;
		while (t.hasMoreTokens()) {
			info[i++] = t.nextToken();
		}
		if (info.length < 3 | info.length > 5) {
			throw new IllegalArgumentException("Wrong number of tokens for version information: " + pString);
		}
		try {
			final int maj = Integer.parseInt(info[0]);
			final int mid = Integer.parseInt(info[1]);
			final int min = Integer.parseInt(info[2]);
			final String type;
			final int num;
			if (info.length == 3) {
				type = "";
				num = 0;
			}
			else {
				type = info[3];
				if (info.length == 4) {
					num = 0;
				}
				else {
					num = Integer.parseInt(info[4]);
				}
			}
			return new FreeplaneVersion(maj, mid, min, type, num);
		}
		catch (final NumberFormatException e) {
			throw new IllegalArgumentException("Wrong version token: " + pString, e);
		}
	}

	private static FreeplaneVersion loadVersion() {
		final Properties versionProperties = ResUtil.loadProperties(VERSION_PROPERTIES);
		final String versionString = versionProperties.getProperty(VERSION_KEY);
		final String versionStatus = versionProperties.getProperty("freeplane_version_status");
		final FreeplaneVersion version = FreeplaneVersion.getVersion(versionString);
		version.mType = versionStatus;
		return version;
	}

	private final int mMaj;
	private final int mMid;
	private final int mMin;
	private final int mNum;
	private String mType;

	public int getMaj() {
    	return mMaj;
    }

	public int getMid() {
    	return mMid;
    }

	public int getMin() {
    	return mMin;
    }

	public int getNum() {
    	return mNum;
    }

	public String getType() {
    	return mType;
    }

	public FreeplaneVersion(final int pMaj, final int pMid, final int pMin, final String pType, final int pNum) {
		super();
		mMaj = pMaj;
		mMid = pMid;
		mMin = pMin;
		mType = pType;
		mNum = pNum;
	}

	public int compareTo(final FreeplaneVersion o) {
		if (mMaj < o.mMaj) {
			return -1;
		}
		if (mMaj > o.mMaj) {
			return 1;
		}
		if (mMid < o.mMid) {
			return -1;
		}
		if (mMid > o.mMid) {
			return 1;
		}
		if (mMin < o.mMin) {
			return -1;
		}
		if (mMin > o.mMin) {
			return 1;
		}
		if (mNum < o.mNum) {
			return -1;
		}
		if (mNum > o.mNum) {
			return 1;
		}
		return 0;
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
	public String numberToString() {
		final StringBuilder buf = new StringBuilder();
		buf.append(mMaj);
		buf.append('.');
		buf.append(mMid);
		buf.append('.');
		buf.append(mMin);
		return buf.toString();
	}
}
