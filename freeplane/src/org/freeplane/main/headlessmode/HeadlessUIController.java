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
import java.awt.Container;
import java.awt.Frame;
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
import javax.swing.RootPaneContainer;

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
	
	final private AtomicLong workingThreadId = new AtomicLong();
	final private ExecutorService worker = Executors.newSingleThreadExecutor(new ThreadFactory() {
		public Thread newThread(Runnable r) {
			final Thread thread = Executors.defaultThreadFactory().newThread(r);
			workingThreadId.set(thread.getId());
			return thread;
		}
	}) ;
	
	public HeadlessUIController(Controller controller, IMapViewManager mapViewManager, String propertyKeyPrefix) {
		super(controller, mapViewManager, propertyKeyPrefix);		
	}

	public Rectangle getFrameSize() {
		throw new RuntimeException("Method not implemented");
	}

	public void setFrameSize(Rectangle frameSize) {
		throw new RuntimeException("Method not implemented");
	}

	public void changeNoteWindowLocation() {
		throw new RuntimeException("Method not implemented");
	}

	public void err(String msg) {
		throw new RuntimeException("Method not implemented");
	}

	public RootPaneContainer getRootPaneContainer() {
		throw new RuntimeException("Method not implemented");
	}

	public Container getContentPane() {
		throw new RuntimeException("Method not implemented");
	}

	public Frame getFrame() {
		throw new RuntimeException("Method not implemented");
	}

	public FreeplaneMenuBar getFreeplaneMenuBar() {
		throw new RuntimeException("Method not implemented");
	}

	public JFrame getJFrame() {
		throw new RuntimeException("Method not implemented");
	}

	public JComponent getStatusBar() {
		throw new RuntimeException("Method not implemented");
	}

	public void init(Controller controller) {
		throw new RuntimeException("Method not implemented");
	}

	public void insertComponentIntoSplitPane(JComponent noteViewerComponent) {
		throw new RuntimeException("Method not implemented");
	}

	public boolean isApplet() {
		throw new RuntimeException("Method not implemented");
	}

	public boolean isMenubarVisible() {
		throw new RuntimeException("Method not implemented");
	}

	public void openDocument(URI uri) throws IOException {
		throw new RuntimeException("Method not implemented");
	}

	public void openDocument(URL fileToUrl) throws Exception {
		throw new RuntimeException("Method not implemented");
	}

	public void out(String msg) {
		throw new RuntimeException("Method not implemented");
	}

	public void addStatusInfo(String key, String info) {
		throw new RuntimeException("Method not implemented");
	}

	public void addStatusInfo(String key, Icon icon) {
		throw new RuntimeException("Method not implemented");
	}

	public void addStatusInfo(String key, String info, Icon icon) {
		throw new RuntimeException("Method not implemented");
	}

	public void addStatusInfo(String key, String info, Icon icon, String tooltip) {
		throw new RuntimeException("Method not implemented");
	}

	public void addStatusComponent(String key, Component component) {
		throw new RuntimeException("Method not implemented");
	}

	public void removeStatus(String key) {
		throw new RuntimeException("Method not implemented");
	}

	public void removeSplitPane() {
		throw new RuntimeException("Method not implemented");
	}

	public void saveProperties() {
		throw new RuntimeException("Method not implemented");
	}

	public void selectMode(ModeController oldModeController, ModeController newModeController) {
		throw new RuntimeException("Method not implemented");
	}

	public void setMenubarVisible(boolean visible) {
		throw new RuntimeException("Method not implemented");
	}

	public void setTitle(String frameTitle) {
		throw new RuntimeException("Method not implemented");
	}

	public void setWaitingCursor(boolean b) {
		throw new RuntimeException("Method not implemented");
	}

	public void viewNumberChanged(int number) {
		throw new RuntimeException("Method not implemented");
	}

	public String completeVisiblePropertyKey(JComponent toolBar) {
		throw new RuntimeException("Method not implemented");
	}

	public void addObjectTypeInfo(Object value) {
		throw new RuntimeException("Method not implemented");
	}

	public boolean quit() {
		throw new RuntimeException("Method not implemented");
	}

	public boolean isDispatchThread() {
		return workingThreadId.get() == Thread.currentThread().getId();
    }

	public void invokeLater(Runnable runnable) {
		worker.execute(runnable);
    }

	public void invokeAndWait(Runnable runnable) throws InterruptedException, InvocationTargetException {
	    try {
			worker.submit(runnable).get();
		} catch (ExecutionException e) {			
		}	    
    }

	public boolean isHeadless() {
	    return true;
    }

	public boolean areScrollbarsVisible() {
	    return false;
    }

	public void setScrollbarsVisible(boolean b) {
    }

	public boolean isFullScreenEnabled() {
	    return false;
    }

	@Override
	protected void setFreeplaneMenuBar(FreeplaneMenuBar menuBar) {
		throw new RuntimeException("Method not implemented");
	}

	public void previousMapView() {
		throw new RuntimeException("Method not implemented");
		
	}

	public void nextMapView() {
		throw new RuntimeException("Method not implemented");
	}	
	
}
