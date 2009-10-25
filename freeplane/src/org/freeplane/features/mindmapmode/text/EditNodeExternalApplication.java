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
package org.freeplane.features.mindmapmode.text;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.text.MessageFormat;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.url.UrlManager;
import org.freeplane.core.util.LogTool;

/**
 * @author Daniel Polansky
 */
class EditNodeExternalApplication extends EditNodeBase {
	final private KeyEvent firstEvent;

	public EditNodeExternalApplication(final NodeModel node, final String text, final KeyEvent firstEvent,
	                                   final ModeController controller, final IEditControl editControl) {
		super(node, text, controller, editControl);
		this.firstEvent = firstEvent;
	}

	protected KeyEvent getFirstEvent() {
		return firstEvent;
	}

	public void show() {
		new Thread() {
			@Override
			public void run() {
				FileWriter writer = null;
				try {
					final File temporaryFile = File.createTempFile("tmm", ".html");
					writer = new FileWriter(temporaryFile);
					writer.write(EditNodeExternalApplication.this.text);
					writer.close();
					final String htmlEditingCommand = ResourceController.getResourceController().getProperty(
					    "html_editing_command");
					final String expandedHtmlEditingCommand = new MessageFormat(htmlEditingCommand)
					    .format(new String[] { temporaryFile.toString() });
					final Process htmlEditorProcess = Controller.exec(expandedHtmlEditingCommand);
					htmlEditorProcess.waitFor();
					final String content = UrlManager.readFile(temporaryFile);
					if (content == null) {
						getEditControl().cancel();
					}
					getEditControl().ok(content);
				}
				catch (final Exception e) {
					LogTool.severe(e);
					try {
						if (writer != null) {
							writer.close();
						}
					}
					catch (final Exception e1) {
					}
				}
			}
		}.start();
		return;
	}
}
