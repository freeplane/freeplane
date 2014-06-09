/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2013 Dimitry
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
package org.freeplane.plugin.script;

import java.io.File;

/**
 * @author Dimitry Polivaev
 * 15.12.2013
 */
public class CompileTimeStrategy {
	private static final long FILE_CHECK_PERIOD = 100;
	private static final long NEVER = 0;
	private final File scriptFile;
	private long compileTime;
	private long fileModificationTime;
	private long lastFileStampCheckTime;

	public CompileTimeStrategy(File scriptFile) {
		this.scriptFile = scriptFile;
		compileTime = NEVER;
		fileModificationTime = NEVER;
		lastFileStampCheckTime = NEVER;
	}

	/** mark the beginning of a script compile run */
	public void scriptCompileStart() {
		compileTime = NEVER;
	}

	/** mark the end of a successful script compile run */
	public void scriptCompiled() {
		assert (compileTime == NEVER);
		compileTime = now();
	}

	private long now() {
		return System.currentTimeMillis();
	}

	public boolean canUseOldCompiledScript() {
		if (compileTime == NEVER)
			return false;
		if (scriptFile == null)
			return true;
		long now = now();
		if (now - lastFileStampCheckTime < FILE_CHECK_PERIOD)
			return true;
		lastFileStampCheckTime = now;
		if (!scriptFile.canRead())
			return false;
		fileModificationTime = scriptFile.lastModified();
		boolean canUseOldCompiledScript = compileTime >= fileModificationTime;
		return canUseOldCompiledScript;
	}
}
