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

import java.util.StringTokenizer;

public class FreeplaneVersionInformation {
	public int mMaj = 0;
	public int mMid = 0;
	public int mMin = 0;
	public int mNum = 0;
	public String mType = "";

	public FreeplaneVersionInformation(final int pMaj, final int pMid, final int pMin, final String pType,
	                                   final int pNum) {
		super();
		mMaj = pMaj;
		mMid = pMid;
		mMin = pMin;
		mType = pType;
		mNum = pNum;
	}

	public FreeplaneVersionInformation(final String pString) {
		final StringTokenizer t = new StringTokenizer(pString, ". ", false);
		final String[] info = new String[t.countTokens()];
		int i = 0;
		while (t.hasMoreTokens()) {
			info[i++] = t.nextToken();
		}
		if (info.length != 3 && info.length != 5) {
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
		mNum = Integer.parseInt(info[4]);
	}

	@Override
	public String toString() {
		final StringBuffer buf = new StringBuffer();
		buf.append(mMaj);
		buf.append('.');
		buf.append(mMid);
		buf.append('.');
		buf.append(mMin);
		if (!mType.equals("")) {
			buf.append(' ');
			buf.append(mType);
			buf.append(' ');
			buf.append(mNum);
		}
		return buf.toString();
	}

	public String toVersionNumberString() {
		final StringBuffer buf = new StringBuffer();
		buf.append(mMaj);
		buf.append('.');
		buf.append(mMid);
		buf.append('.');
		buf.append(mMin);
		buf.append(':');
		buf.append(mNum);
		return buf.toString();
	}
}
