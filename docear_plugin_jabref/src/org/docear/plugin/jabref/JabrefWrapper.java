package org.docear.plugin.jabref;

import net.sf.jabref.JabRef;
import net.sf.jabref.JabRefFrame;

public class JabrefWrapper extends JabRef {

	protected JabrefWrapper(String[] arg0) {
		super(arg0);		
	}
	
	public JabRefFrame getJabrefFrame(){
		return this.jrf;
	}

}
