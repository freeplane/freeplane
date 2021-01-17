package org.freeplane.plugin.markdown;

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
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.text.mindmapmode.EditNodeDialog;

/**
 *  @author Felix Natter (copied from FormulaEditor)
 */
class MarkdownEditor extends EditNodeDialog {
	
	MarkdownEditor(NodeModel nodeModel, String text, KeyEvent firstEvent, IEditControl editControl,
                          boolean enableSplit, JEditorPane textEditor) {
	    super(nodeModel, text, firstEvent, editControl, enableSplit, textEditor);
    }
	
    @Override
    protected boolean editorBlocks() {
        return false;
    }

}