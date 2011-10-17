package org.docear.plugin.jabref;

import org.docear.plugin.core.ALanguageController;
import org.freeplane.features.mode.ModeController;

public class JabrefController extends ALanguageController{
	
	private ModeController modeController;
	
	public JabrefController(ModeController modeController) {
		this.modeController = modeController;
		this.initJabref();
	}
	
	private void initJabref() {
		JabrefWrapper wrapper = new JabrefWrapper(new String[]{ "-s" });    
	}

}
