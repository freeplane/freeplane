/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2010 dimitry
 *
 *  This file author is dimitry
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

import java.awt.event.ActionEvent;

import javax.swing.JEditorPane;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import com.lightdev.app.shtm.SHTMLEditorKit;

/**
 * @author Dimitry Polivaev
 * Nov 28, 2010
 */
@SuppressWarnings("serial")
class ExtendedEditorKit extends StyledEditorKit{

	static public class RemoveStyleAttributeAction extends StyledTextAction {
	    final private Object attribute;

		public RemoveStyleAttributeAction(Object attribute, String name) {
	        super(name);
	        this.attribute = attribute;
        }

		public void actionPerformed(ActionEvent e) {
		    final JEditorPane editor = getEditor(e);
		    if(editor == null){
		    	return;
		    }
		    final int selectionStart = editor.getSelectionStart();
		    final int selectionEnd = editor.getSelectionEnd();
		    if(selectionStart == selectionEnd){
		    	return;
		    }
		    SHTMLEditorKit.removeCharacterAttributes((StyledDocument) editor.getDocument(), attribute, selectionStart, selectionEnd - selectionStart);
	    }
    }
	
}
