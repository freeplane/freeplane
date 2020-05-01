/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2020 dimitry
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

import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.JEditorPane;
import javax.swing.UIManager;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit.StyledTextAction;

import org.freeplane.core.util.ColorUtils;

/**
 * @author Dimitry Polivaev
 * Jan 3, 2020
 */
class ForegroundAction extends StyledTextAction {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Color darkColor;
	private final Color lightColor;

	/**
     * Creates a new ForegroundAction.
     *
     * @param nm the action name
     * @param fg the foreground color
     */
    public ForegroundAction(String nm, Color darkColor, Color lightColor) {
        super(nm);
		this.darkColor = darkColor;
		this.lightColor = lightColor;
    }

    /**
     * Sets the foreground color.
     *
     * @param e the action event
     */
    public void actionPerformed(ActionEvent e) {
        JEditorPane editor = getEditor(e);
        if (editor != null) {
            Color fg = getColorCloserTo(editor.getCaretColor());
            if ((e != null) && (e.getSource() == editor)) {
                String s = e.getActionCommand();
                try {
                    fg = Color.decode(s);
                } catch (NumberFormatException nfe) {
                }
            }
            if (fg != null) {
                MutableAttributeSet attr = new SimpleAttributeSet();
                StyleConstants.setForeground(attr, fg);
                setCharacterAttributes(editor, attr, false);
            } else {
                UIManager.getLookAndFeel().provideErrorFeedback(editor);
            }
        }
    }

	private Color getColorCloserTo(Color color) {
		if(ColorUtils.isDark(color))
			return darkColor;
		else
			return lightColor;
	}
}