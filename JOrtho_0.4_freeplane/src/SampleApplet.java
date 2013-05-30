/*
 *  JOrtho
 *
 *  Copyright (C) 2005-2008 by i-net software
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 *  
 *  Created on 13.12.2007
 */
import javax.swing.JApplet;
import javax.swing.JEditorPane;
import javax.swing.JTextPane;

import com.inet.jortho.SpellChecker;

public class SampleApplet extends JApplet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void start() {
		// Build the test frame for the sample
		final JEditorPane text = new JTextPane();
		text.setText("This is a simppler textt with spellingg errors.");
		add(text);
		// Load the configuration from the file dictionaries.cnf and 
		// use the english dictionary as default 
		SpellChecker.registerDictionaries(getCodeBase(), "en");
		// enable the spell checking on the text component with all features
		SpellChecker.register(text);
	}
}
