/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2012 Dimitry
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
package org.freeplane.main.headlessmode;

import java.awt.Component;
import java.awt.Rectangle;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;

import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.ui.FrameController;
import org.freeplane.features.ui.IMapViewManager;

/**
 * @author Dimitry Polivaev
 * 24.12.2012
 */
public class HeadlessUIController extends FrameController {
	final private static AtomicLong workingThreadId = new AtomicLong();
	final private static ExecutorService executorService = Executors.newSingleThreadExecutor(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			final Thread thread = Executors.defaultThreadFactory().newThread(r);
			workingThreadId.set(thread.getId());
			return thread;
		}
	});

	public HeadlessUIController(Controller controller, IMapViewManager mapViewManager, String propertyKeyPrefix) {
		super(controller, mapViewManager, propertyKeyPrefix);
	}

	public Rectangle getFrameSize() {
		throw methodNotImplementedException();
	}

	public void setFrameSize(Rectangle frameSize) {
		throw methodNotImplementedException();
	}

	@Override
	public void changeNoteWindowLocation() {
		throw methodNotImplementedException();
	}

	@Override
	public void err(String msg) {
		throw methodNotImplementedException();
	}

	@Override
	public FreeplaneMenuBar getFreeplaneMenuBar() {
		throw methodNotImplementedException();
	}

	public JFrame getJFrame() {
		throw methodNotImplementedException();
	}

	@Override
	public JComponent getStatusBar() {
		throw methodNotImplementedException();
	}

	@Override
	public void init(Controller controller) {
		throw methodNotImplementedException();
	}

	@Override
	public void insertComponentIntoSplitPane(JComponent noteViewerComponent) {
		throw methodNotImplementedException();
	}

	@Override
	public boolean isApplet() {
		return false;
	}

	@Override
	public boolean isMenubarVisible() {
		throw methodNotImplementedException();
	}

	@Override
	public void openDocument(URI uri) throws IOException {
		throw methodNotImplementedException();
	}

	@Override
	public void openDocument(URL fileToUrl) throws Exception {
		throw methodNotImplementedException();
	}

	@Override
	public void out(String msg) {
	}

	@Override
	public void addStatusInfo(String key, String info) {
	}

	@Override
	public void addStatusInfo(String key, Icon icon) {
	}

	@Override
	public void addStatusInfo(String key, String info, Icon icon) {
	}

	@Override
	public void addStatusInfo(String key, String info, Icon icon, String tooltip) {
	}

	@Override
	public void addStatusComponent(String key, Component component) {
	}

	@Override
	public void removeStatus(String key) {
		throw methodNotImplementedException();
	}

	@Override
	public void removeSplitPane() {
		throw methodNotImplementedException();
	}

	@Override
	public void saveProperties() {
		throw methodNotImplementedException();
	}

	@Override
	public void selectMode(ModeController oldModeController, ModeController newModeController) {
		throw methodNotImplementedException();
	}

	@Override
	public void setMenubarVisible(boolean visible) {
		throw methodNotImplementedException();
	}

	@Override
	public void setTitle(String frameTitle) {
		throw methodNotImplementedException();
	}

	@Override
	public void setWaitingCursor(boolean b) {
		// do nothing
	}

	@Override
	public void viewNumberChanged(int number) {
		throw methodNotImplementedException();
	}

	@Override
	public void addObjectTypeInfo(Object value) {
		throw methodNotImplementedException();
	}

	@Override
	public boolean quit() {
		throw methodNotImplementedException();
	}

	@Override
	public boolean isDispatchThread() {
		return workingThreadId.get() == Thread.currentThread().getId();
	}


	@Override
	public ExecutorService getMainThreadExecutorService() {
		return executorService;
	}
	
	@Override
	public void invokeLater(Runnable runnable) {
		executorService.execute(runnable);
	}

	@Override
	public void invokeAndWait(Runnable runnable) throws InterruptedException, InvocationTargetException {
		try {
			if(isDispatchThread())
				runnable.run();
			else if(! executorService.isShutdown())
				executorService.submit(runnable).get();
		}
		catch (ExecutionException e) {
			throw new InvocationTargetException(e);
		}
	}

	@Override
	public boolean isHeadless() {
		return true;
	}

	@Override
	public boolean areScrollbarsVisible() {
		return false;
	}

	@Override
	public void setScrollbarsVisible(boolean b) {
	}

	public boolean isFullScreenEnabled() {
		return false;
	}

	@Override
	protected void setFreeplaneMenuBar(FreeplaneMenuBar menuBar) {
		throw methodNotImplementedException();
	}

	@Override
	public void previousMapView() {
		throw methodNotImplementedException();
	}

	@Override
	public void nextMapView() {
		throw methodNotImplementedException();
	}

	@Override
	public Component getCurrentRootComponent() {
		throw methodNotImplementedException();
	}

	@Override
	public Component getMenuComponent() {
		throw methodNotImplementedException();
	}

	@Override
	protected boolean isMenuComponentInFullScreenMode() {
		return false;
	}

	private RuntimeException methodNotImplementedException() {
		return new RuntimeException("Method not implemented");
	}

	public void shutdown() {
		executorService.shutdown();
	}
}
