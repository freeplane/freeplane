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
package org.freeplane.features.mindmapmode.ortho;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;

import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogTool;
import org.freeplane.features.mindmapmode.MModeController;

import com.inet.jortho.FileUserDictionary;
import com.inet.jortho.SpellChecker;

/**
 * @author Dimitry Polivaev
 * Feb 8, 2009
 */
public class SpellCheckerController implements IExtension {
	public static SpellCheckerController getController(final ModeController modeController) {
		return (SpellCheckerController) modeController.getExtension(SpellCheckerController.class);
	}

	public static void install(final MModeController modeController) {
		modeController.addExtension(SpellCheckerController.class, new SpellCheckerController());
	}

	private boolean spellCheckerEnabled = false;
	private boolean spellCheckerInitialized = false;

	private SpellCheckerController() {
		init();
	}

	public void addSpellCheckerMenu(final JPopupMenu popupMenu) {
		if (!isSpellCheckerActive()) {
			return;
		}
		popupMenu.add(SpellChecker.createCheckerMenu());
		popupMenu.add(SpellChecker.createLanguagesMenu());
	}

	public void enableAutoSpell(final JTextComponent editorPane) {
		if (!isSpellCheckerActive()) {
			return;
		}
		SpellChecker.enableAutoSpell(editorPane, true);
	}

	public void enableShortKey(final JTextComponent editorPane) {
		if (!isSpellCheckerActive()) {
			return;
		}
		SpellChecker.enableShortKey(editorPane, true);
	}

	private void init() {
		if (spellCheckerInitialized == false) {
			spellCheckerInitialized = true;
			final ResourceController resourceController = ResourceController.getResourceController();
			final File orthoDir = new File(resourceController.getResourceBaseDir() + File.separatorChar + "ortho");
			if (!orthoDir.exists()) {
				return;
			}
			final String[] dictionaryList = orthoDir.list(new FilenameFilter() {
				public boolean accept(final File dir, final String name) {
					return name.length() == "dictionary_XX.ortho".length() && name.startsWith("dictionary_")
					        && name.endsWith(".ortho");
				}
			});
			if (dictionaryList.length == 0) {
				return;
			}
			SpellChecker.setUserDictionaryProvider(new FileUserDictionary(resourceController
			    .getFreeplaneUserDirectory()));
			final StringBuilder availableLocales = new StringBuilder();
			for (int i = 0; i < dictionaryList.length; i++) {
				final String language = dictionaryList[i].substring("dictionary_".length(), "dictionary_".length() + 2);
				availableLocales.append(language);
				availableLocales.append(",");
			}
			try {
				SpellChecker.registerDictionaries(orthoDir.toURL(), availableLocales.toString(), null, ".ortho");
				spellCheckerEnabled = true;
			}
			catch (final MalformedURLException e) {
				LogTool.severe(e);
			}
		}
	}

	public boolean isSpellCheckerActive() {
		return spellCheckerEnabled;
	}
}
