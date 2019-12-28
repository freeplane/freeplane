/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2019 dimitry
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
package org.freeplane.features.icon;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;

/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
import java.awt.GridLayout;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Quantity;
 
/*
 * LabelDemo.java needs one other file:
 *   images/middle.gif
 */
public class TextIconDemo extends Box {
    public TextIconDemo() {
        super(BoxLayout.Y_AXIS);
        
        UITools.Defaults.DEFAULT_FONT_SCALING_FACTOR = 3;
        
        JLabel label1, label2, label3;
 
        Font font = new Font("dialog", 0, 48);
        StringBuilder emojiAsString = new StringBuilder();
        for (int codePoint = 0x1f300; codePoint <= 0x1F64F; codePoint++)
        	emojiAsString.append(Character.toChars(codePoint));
		String text = "Ag" + emojiAsString;
		System.out.println(Integer.toHexString(emojiAsString.codePointAt(0)));
		Icon icon = new TextIcon(text, font , Color.BLUE).getIcon(new Quantity<LengthUnits>(0.5, LengthUnits.cm));
 
        //Create the first label.
        label1 = new JLabel("Image and Text",
                            icon,
                            JLabel.CENTER);
        //Set the position of its text, relative to its icon:
        label1.setVerticalTextPosition(JLabel.BOTTOM);
        label1.setHorizontalTextPosition(JLabel.CENTER);
 
        //Create the other labels.
        label2 = new JLabel("Text-Only Label");
        label3 = new JLabel(icon);
 
        //Create tool tips, for the heck of it.
        label1.setToolTipText("A label containing both image and text");
        label2.setToolTipText("A label containing only text");
        label3.setToolTipText("A label containing only an image");
 
        label1.setBackground(Color.CYAN);
        label2.setBackground(Color.CYAN);
        label3.setBackground(Color.CYAN);
        
        label1.setOpaque(true);
        label2.setOpaque(true);
        label3.setOpaque(true);
        
        label1.setBorder(BorderFactory.createEtchedBorder());
        label2.setBorder(BorderFactory.createEtchedBorder());
        label3.setBorder(BorderFactory.createEtchedBorder());
        
        //Add the labels.
        add(label1);
        add(label2);
        add(label3);
        JTextArea textInput = new JTextArea();
        textInput.setLineWrap(true);
        textInput.setColumns(80);
        textInput.setFont(font);
        textInput.setText(text);
		add(new JScrollPane(textInput));
    }
 
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("LabelDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        //Add content to the window.
        frame.add(new TextIconDemo());
 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
 
    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
        //Turn off metal's use of bold fonts
            UIManager.put("swing.boldMetal", Boolean.FALSE);
                 
        createAndShowGUI();
            }
        });
    }
}