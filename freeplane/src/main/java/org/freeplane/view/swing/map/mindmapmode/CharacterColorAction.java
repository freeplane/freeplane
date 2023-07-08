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
import javax.swing.text.StyledEditorKit.StyledTextAction;
import javax.swing.text.html.CSS;

import org.freeplane.core.util.ColorUtils;

import com.lightdev.app.shtm.Util;

/**
 * @author Dimitry Polivaev
 * Jan 3, 2020
 */
class CharacterColorAction extends StyledTextAction {

    /**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private final Color darkColor;
	private final Color lightColor;
	private final CSS.Attribute attributeName;


	/**
     * Creates a new ForegroundAction.
     *
     * @param nm the action name
     * @param fg the foreground color
     */
    public CharacterColorAction(String nm, CSS.Attribute attributeName, Color darkColor, Color lightColor) {
        super(nm);
		this.darkColor = darkColor;
		this.lightColor = lightColor;
        this.attributeName = attributeName;
    }

    /**
     * Sets the foreground color.
     *
     * @param e the action event
     */
    public void actionPerformed(ActionEvent e) {
        JEditorPane editor = getEditor(e);
        if (editor != null) {
            Color color = getColorCloserTo(editor.getCaretColor());
            if ((e != null) && (e.getSource() == editor)) {
                String s = e.getActionCommand();
                try {
                    color = Color.decode(s);
                } catch (NumberFormatException nfe) {
                }
            }
            if (color != null) {
                MutableAttributeSet attr = new SimpleAttributeSet();
                final String colorRGB = "#" + Integer.toHexString(color.getRGB()).substring(2);
                Util.styleSheet().addCSSAttribute(attr, attributeName, colorRGB);
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