/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
package org.freeplane.ortho;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;

import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.mindmapmode.text.EditNodeBase.EditPopupMenu;
import org.freeplane.main.application.ApplicationResourceController;

import com.inet.jortho.FileUserDictionary;
import com.inet.jortho.SpellChecker;
import com.lightdev.app.shtm.SHTMLEditorPane;

/**
 * @author Dimitry Polivaev
 * Feb 8, 2009
 */
public class SpellCheckerController {
	static private SpellCheckerController spellCheckerController = null;
	
	private SpellCheckerController() {
	    super();
    }
	public static SpellCheckerController getController() {
		if(spellCheckerController == null){
			spellCheckerController = new SpellCheckerController();
		}
    	return spellCheckerController;
    }
	private boolean spellCheckerInitialized = false;
	public  boolean isSpellCheckerActive() {
    	return spellCheckerInitialized;
    }
	public void init() {
		if(spellCheckerInitialized == false){
			spellCheckerInitialized = true;
			final ApplicationResourceController resourceController = (ApplicationResourceController)ResourceController.getResourceController();
			File orthoDir = new File(resourceController.getResourceBaseDir() + File.separatorChar + "ortho");
			if(! orthoDir.exists()){
				return;
			}
			final String[] dictionaryList = orthoDir.list(new FilenameFilter(){
				public boolean accept(File dir, String name) {
	                return name.length() == "dictionary_XX.ortho".length() && name.startsWith("dictionary_") && name.endsWith(".ortho");
                }
			});
			if(dictionaryList.length == 0){
				return;
			}
			SpellChecker.setUserDictionaryProvider(new FileUserDictionary(resourceController.getFreeplaneUserDirectory()));
			StringBuffer availableLocales = new StringBuffer();
			for(int i = 0; i < dictionaryList.length; i++){
				final String language = dictionaryList[i].substring("dictionary_".length(), "dictionary_".length() + 2);
				availableLocales.append(language);
				availableLocales.append(",");
			}
			try {
	            SpellChecker.registerDictionaries(orthoDir.toURL(), availableLocales.toString(), null, ".ortho");
            }
            catch (MalformedURLException e) {
	            e.printStackTrace();
            }
			
		}
    }
	public void addSpellCheckerMenu(JPopupMenu popupMenu) {
		if(! isSpellCheckerActive()){
			return;
		}
		popupMenu.add(SpellChecker.createCheckerMenu());
    }
	public void enableAutoSpell(JTextComponent editorPane) {
		if(! isSpellCheckerActive()){
			return;
		}
		SpellChecker.enableAutoSpell(editorPane, true);
	    
    }
	public void enableShortKey(JTextComponent editorPane) {
		if(! isSpellCheckerActive()){
			return;
		}
		SpellChecker.enableShortKey(editorPane, true);
    }
}
