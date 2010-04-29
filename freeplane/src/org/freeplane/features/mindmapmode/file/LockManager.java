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
package org.freeplane.features.mindmapmode.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.nio.channels.FileLock;
import java.util.Timer;
import java.util.TimerTask;

import org.freeplane.core.url.UrlManager;
import org.freeplane.core.util.LogTool;
import org.freeplane.core.util.SysUtil;

public class LockManager extends TimerTask {
	private File lockedSemaphoreFile = null;
	private String lockingUserOfOldLock = null;
	private final long lockSafetyPeriod = 5 * 60 * 1000;
	private Timer lockTimer = null;
	private final long lockUpdatePeriod = 4 * 60 * 1000;

	private File getSemaphoreFile(final File mapFile) {
		return new File(mapFile.getParent() + System.getProperty("file.separator") + "$~" + mapFile.getName() + "~");
	}

	public synchronized String popLockingUserOfOldLock() {
		final String toReturn = lockingUserOfOldLock;
		lockingUserOfOldLock = null;
		return toReturn;
	}

	public synchronized void releaseLock() {
		if (lockedSemaphoreFile != null) {
			lockedSemaphoreFile.delete();
			lockedSemaphoreFile = null;
		}
	}

	public synchronized void releaseTimer() {
		if (lockTimer != null) {
			lockTimer.cancel();
			lockTimer = null;
		}
	}

	@Override
	public synchronized void run() {
		if (lockedSemaphoreFile == null) {
			LogTool.severe("unexpected: lockedSemaphoreFile is null upon lock update");
			return;
		}
		try {
			UrlManager.setHidden(lockedSemaphoreFile, false, /* synchro= */true);
			writeSemaphoreFile(lockedSemaphoreFile);
		}
		catch (final Exception e) {
			LogTool.severe(e);
		}
	}

	public synchronized String tryToLock(final File file) throws Exception {
		final File semaphoreFile = getSemaphoreFile(file);
		if (semaphoreFile.equals(lockedSemaphoreFile)) {
			return null;
		}
		BufferedReader semaphoreReader = null;
		try {
			semaphoreReader = new BufferedReader(new FileReader(semaphoreFile));
			final String lockingUser = semaphoreReader.readLine();
			final long lockTime = new Long(semaphoreReader.readLine()).longValue();
			final long timeDifference = System.currentTimeMillis() - lockTime;
			if (timeDifference > lockSafetyPeriod) {
				lockingUserOfOldLock = lockingUser;
				semaphoreFile.delete();
			}
			else {
				return lockingUser;
			}
		}
		catch (final FileNotFoundException e) {
		}
		finally {
			if (semaphoreReader != null) {
				semaphoreReader.close();
			}
		}
		writeSemaphoreFile(semaphoreFile);
		if (lockTimer == null) {
			lockTimer = SysUtil.createTimer(getClass().getSimpleName());
			lockTimer.schedule(this, lockUpdatePeriod, lockUpdatePeriod);
		}
		releaseLock();
		lockedSemaphoreFile = semaphoreFile;
		return null;
	}

	private void writeSemaphoreFile(final File inSemaphoreFile) throws Exception {
		FileOutputStream semaphoreOutputStream;
		try {
			semaphoreOutputStream = new FileOutputStream(inSemaphoreFile);
		}
		catch (final FileNotFoundException e) {
			if (lockTimer != null) {
				lockTimer.cancel();
			}
			return;
		}
		FileLock lock = null;
		try {
			lock = semaphoreOutputStream.getChannel().tryLock();
			if (lock == null) {
				semaphoreOutputStream.close();
				LogTool.severe("Locking failed.");
				throw new Exception();
			}
		}
		catch (final UnsatisfiedLinkError eUle) {
		}
		catch (final NoClassDefFoundError eDcdf) {
		}
		semaphoreOutputStream.write(System.getProperty("user.name").getBytes());
		semaphoreOutputStream.write('\n');
		semaphoreOutputStream.write(String.valueOf(System.currentTimeMillis()).getBytes());
		UrlManager.setHidden(inSemaphoreFile, true, /* synchro= */false);
		if (lock != null) {
			lock.release();
		}
		semaphoreOutputStream.close();
		semaphoreOutputStream = null;
	}
}
