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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.nio.channels.FileLock;

import javax.swing.Timer;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.LogUtils;

public class LockManager{
	static final String LOCK_EXPIRATION_TIME = "lock_expiration_time_in_minutes";
	private File lockedSemaphoreFile;
	private String lockingUserOfOldLock;
	private final long lockSafetyPeriod;
	private Timer lockTimer;
	private final int lockUpdatePeriod;


	public LockManager() {
	    super();
		lockedSemaphoreFile = null;
		lockingUserOfOldLock = null;
		lockTimer = null;
		lockSafetyPeriod = ResourceController.getResourceController().getIntProperty(LOCK_EXPIRATION_TIME) * 60 * 1000;
		lockUpdatePeriod = (int) Math.min(Integer.MAX_VALUE, Math.round(lockSafetyPeriod * 0.8));
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
			lockTimer.stop();
			lockTimer = null;
		}
	}

	protected synchronized void updateSemaphoreFile() {
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
		try (BufferedReader semaphoreReader = new BufferedReader(new FileReader(semaphoreFile))){
			;
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
		writeSemaphoreFile(semaphoreFile);
		release();
		lockedSemaphoreFile = semaphoreFile;
		if (lockTimer == null && lockUpdatePeriod > 0) {
			lockTimer = new Timer(lockUpdatePeriod, new ActionListener() {
                
                @Override
                public void actionPerformed(ActionEvent e) {
                    updateSemaphoreFile();
                }
            });
			lockTimer.start();
		}
		return null;
	}

	private boolean isLockExpired(final String lockTimeString) {
	    final long lockTime = Long.parseLong(lockTimeString);
        final long timeDifference = System.currentTimeMillis() - lockTime;
		return lockTimeString == null || lockSafetyPeriod > 0 && timeDifference > lockSafetyPeriod;
    }

	private void writeSemaphoreFile(final File inSemaphoreFile) throws Exception {
		try (FileOutputStream semaphoreOutputStream  = new FileOutputStream(inSemaphoreFile)){
	        FileLock lock = null;
	        try {
	            lock = semaphoreOutputStream.getChannel().tryLock();
	            if (lock == null) {
	                LogUtils.severe("Locking failed.");
	                throw new Exception();
	            }
	        }
	        catch (final UnsatisfiedLinkError eUle) {/**/}
	        catch (final NoClassDefFoundError eDcdf) {/**/}
	        semaphoreOutputStream.write(System.getProperty("user.name").getBytes());
	        semaphoreOutputStream.write('\n');
	        semaphoreOutputStream.write(String.valueOf(System.currentTimeMillis()).getBytes());
	        FileUtils.setHidden(inSemaphoreFile, true, /* synchro= */false);
	        if (lock != null) {
	            lock.release();
	        }
		}
		catch (final FileNotFoundException e) {
			if (lockTimer != null) {
				lockTimer.stop();
			}
			return;
		}
	}
}
