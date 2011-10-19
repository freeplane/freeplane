package org.docear.plugin.bibtex;

import javax.swing.JFrame;

import net.sf.jabref.JabRef;
import net.sf.jabref.JabRefFrame;

public class JabrefWrapper extends JabRef  {

	protected JabrefWrapper(String[] arg0) {
		super(arg0);		
	}
	
	public JabrefWrapper(JFrame frame) {
		super(frame);
	}
	
	public JabRefFrame getJabrefFrame(){
		
		return this.jrf;
	}
}
