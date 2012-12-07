package org.freeplane.plugin.latex;

/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2012 dimitry
 *
 *  This file author is Felix Natter (copied from FormulaEditor)
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

import java.awt.event.KeyEvent;

import javax.swing.JEditorPane;
import javax.swing.RootPaneContainer;

import org.freeplane.features.map.NodeModel;
import org.freeplane.features.text.mindmapmode.EditNodeDialog;
import org.freeplane.view.swing.ui.mindmapmode.GlassPaneManager;
import org.freeplane.view.swing.ui.mindmapmode.INodeSelector;

/**
 *  @author Felix Natter (copied from FormulaEditor)
 */
class LatexEditor extends EditNodeDialog implements INodeSelector {
	
	private JEditorPane textEditor;

	LatexEditor(NodeModel nodeModel, String text, KeyEvent firstEvent, IEditControl editControl,
                          boolean enableSplit, JEditorPane textEditor) {
	    super(nodeModel, text, firstEvent, editControl, enableSplit, textEditor);
	    super.setModal(false);
	    this.textEditor = textEditor;
    }

	@Override
    public void show(RootPaneContainer frame) {
	    textEditor.addAncestorListener(new GlassPaneManager(frame.getRootPane(), this));
	    super.show(frame);
    }

	public void nodeSelected(final NodeModel model) {
		final String id = model.getID();
		textEditor.replaceSelection(id);
	    textEditor.requestFocus();
    }
}