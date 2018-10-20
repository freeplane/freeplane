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
package org.freeplane.features.url.mindmapmode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.nio.channels.FileLock;
import java.util.Timer;
import java.util.TimerTask;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.SysUtils;

public class LockManager extends TimerTask {
	static final String LOCK_EXPIRATION_TIME = "lock_expiration_time_in_minutes";
	private File lockedSemaphoreFile;
	private String lockingUserOfOldLock;
	private final long lockSafetyPeriod;
	private Timer lockTimer;
	private final long lockUpdatePeriod;


	public LockManager() {
	    super();
		lockedSemaphoreFile = null;
		lockingUserOfOldLock = null;
		lockTimer = null;
		lockSafetyPeriod = ResourceController.getResourceController().getIntProperty(LOCK_EXPIRATION_TIME) * 60 * 1000;
		lockUpdatePeriod = Math.round(lockSafetyPeriod * 0.8);
    }

	private File getSemaphoreFile(final File mapFile) {
		return new File(mapFile.getParent() + System.getProperty("file.separator") + "$~" + mapFile.getName() + "~");
	}

	public synchronized String popLockingUserOfOldLock() {
		final String toReturn = lockingUserOfOldLock;
		lockingUserOfOldLock = null;
		return toReturn;
	}

	public synchronized void release() {
		if (lockedSemaphoreFile != null) {
			lockedSemaphoreFile.delete();
			lockedSemaphoreFile = null;
		}
		if (lockTimer != null) {
			lockTimer.cancel();
			lockTimer = null;
		}
	}

	@Override
	public synchronized void run() {
		if (lockedSemaphoreFile == null) {
			LogUtils.severe("unexpected: lockedSemaphoreFile is null upon lock update");
			return;
		}
		try {
			FileUtils.setHidden(lockedSemaphoreFile, false, /* synchro= */true);
			writeSemaphoreFile(lockedSemaphoreFile);
		}
		catch (final Exception e) {
			LogUtils.severe(e);
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
			final String lockTime = semaphoreReader.readLine();
			if (isLockExpired(lockTime)) {
				lockingUserOfOldLock = lockingUser;
				semaphoreFile.delete();
			}
			else {
				return lockingUser;
			}
		}
		catch (final FileNotFoundException e) {
		}
		catch (final NumberFormatException e) {
		}
		finally {
			if (semaphoreReader != null) {
				semaphoreReader.close();
			}
		}
		writeSemaphoreFile(semaphoreFile);
		if (lockTimer == null && lockUpdatePeriod > 0) {
			lockTimer = SysUtils.createTimer(getClass().getSimpleName());
			lockTimer.schedule(this, lockUpdatePeriod, lockUpdatePeriod);
		}
		release();
		lockedSemaphoreFile = semaphoreFile;
		return null;
	}

	private boolean isLockExpired(final String lockTimeString) {
	    final long lockTime = new Long(lockTimeString).longValue();
        final long timeDifference = System.currentTimeMillis() - lockTime;
		return lockTimeString == null || lockSafetyPeriod > 0 && timeDifference > lockSafetyPeriod;
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
				LogUtils.severe("Locking failed.");
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
		FileUtils.setHidden(inSemaphoreFile, true, /* synchro= */false);
		if (lock != null) {
			lock.release();
		}
		semaphoreOutputStream.close();
		semaphoreOutputStream = null;
	}
}
