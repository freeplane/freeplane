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
package org.freeplane.view.swing.map.mindmapmode;

import java.io.File;
import java.io.FileWriter;
import java.text.MessageFormat;

import javax.swing.RootPaneContainer;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.text.mindmapmode.EditNodeBase;

/**
 * @author Daniel Polansky
 */
class EditNodeExternalApplication extends EditNodeBase {

	public EditNodeExternalApplication(final NodeModel node, final String text, final IEditControl editControl) {
		super(node, text, editControl);
	}

	@Override
	public void show(RootPaneContainer frame) {
		new Thread() {
			@Override
			public void run() {
				try {
					final File temporaryFile = File.createTempFile("tmm", ".html");
					try(FileWriter writer = new FileWriter(temporaryFile)) {
					    writer.write(getText());
					}
					final String htmlEditingCommand = ResourceController.getResourceController().getProperty(
					    "html_editing_command");
					final String expandedHtmlEditingCommand = new MessageFormat(htmlEditingCommand)
					    .format(new String[] { temporaryFile.toString() });
					Controller.exec(expandedHtmlEditingCommand, true);
					final String content = FileUtils.readFile(temporaryFile);
					if (content == null) {
						getEditControl().cancel();
					}
					getEditControl().ok(content);
				}
				catch (final Exception e) {
					LogUtils.severe(e);
				}
			}
		}.start();
		return;
	}

    @Override
    protected boolean editorBlocks() {
        return true;
    }
}
