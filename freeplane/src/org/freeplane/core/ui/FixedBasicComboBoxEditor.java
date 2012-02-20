package org.freeplane.core.ui;

import java.awt.Color;


import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxEditor;

/**
 * This class fixes a problem with BasicComboBoxEditor (too few left padding
 * in the embedded JTextField => first character is half-way outside text field)
 * 
 * @author Felix Natter
 *
 */
public class FixedBasicComboBoxEditor extends BasicComboBoxEditor {
	public FixedBasicComboBoxEditor()
	{
		// don't change the background color to grey, it would be harder to use :-(
		//Color c = UIManager.getLookAndFeelDefaults().getColor("ComboBox.background");
		//editor.setBackground(new Color(c.getRed(), c.getGreen(), c.getBlue()));
		
		// add left padding to the embedded JTextField:
		// this border-hack was proposed by John B. Matthews on comp.lang.java.gui
		editor.setBorder(BorderFactory.createCompoundBorder(
				editor.getBorder(), new EmptyBorder(0, 4, 0, 0))); 
	}
}
