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
package org.freeplane.features.spellchecker.mindmapmode;

import java.awt.EventQueue;
import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.util.Locale;

import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

import com.inet.jortho.FileUserDictionary;
import com.inet.jortho.LanguageChangeEvent;
import com.inet.jortho.LanguageChangeListener;
import com.inet.jortho.SpellChecker;
import com.inet.jortho.SpellCheckerOptions;

/**
 * @author Dimitry Polivaev
 * Feb 8, 2009
 */
public class SpellCheckerController implements IExtension {
	private static final String SPELLING_LANGUAGE = "spelling_language";

	public static SpellCheckerController getController() {
		final ModeController modeController = Controller.getCurrentModeController();
		return (SpellCheckerController) modeController.getExtension(SpellCheckerController.class);
	}

	public static void install(final ModeController modeController) {
		modeController.addExtension(SpellCheckerController.class, new SpellCheckerController());
	}

	private boolean spellCheckerEnabled = false;
	private boolean spellCheckerInitialized = false;
	private LanguageChangeListener languageChangeListener;

	private SpellCheckerController() {
	}

	public void addSpellCheckerMenu(final JPopupMenu popupMenu) {
		if (!isSpellCheckerActive()) {
			return;
		}
		popupMenu.add(SpellChecker.createCheckerMenu());
		popupMenu.add(SpellChecker.createLanguagesMenu());
	}

	public void enableAutoSpell(final JTextComponent editorPane, final boolean enable) {
		if (!isSpellCheckerActive()) {
			return;
		}
		SpellChecker.enableAutoSpell(editorPane, enable);
	}

	public void enableShortKey(final JTextComponent editorPane, final boolean enable) {
		if (!isSpellCheckerActive()) {
			return;
		}
		SpellChecker.enableShortKey(editorPane, enable);
	}

	private void init() {
		if (spellCheckerInitialized == true) {
			return;
		}
		spellCheckerInitialized = true;
		final ResourceController resourceController = ResourceController.getResourceController();
		final File orthoDir = new File(resourceController.getResourceBaseDir(), "ortho");
		registerDictionaries(orthoDir);
		final File userOrthoDir = new File(resourceController.getFreeplaneUserDirectory(), "ortho");
		registerDictionaries(userOrthoDir);
		if (!spellCheckerEnabled) {
			return;
		}
		setSpellCheckOptions(resourceController);
		resourceController.addPropertyChangeListener(new IFreeplanePropertyListener() {
			public void propertyChanged(final String propertyName, final String newValue, final String oldValue) {
				if (propertyName.startsWith("spelling_opt")) {
					setSpellCheckOptions(resourceController);
				}
			}
		});
		String spellingLanguage = resourceController.getProperty(SPELLING_LANGUAGE, null);
		if (spellingLanguage == null) {
			spellingLanguage = resourceController.getLanguageCode();
		}
		if (!spellingLanguage.equals("disabled")) {
			SpellChecker.setLanguage(spellingLanguage);
		}
		languageChangeListener = new LanguageChangeListener() {
			public void languageChanged(final LanguageChangeEvent ev) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						final Locale currentLocale = ev.getCurrentLocale();
						if (currentLocale == null) {
							resourceController.setProperty(SPELLING_LANGUAGE, "disabled");
							return;
						}
						resourceController.setProperty(SPELLING_LANGUAGE, currentLocale.getLanguage());
					}
				});
			}
		};
		SpellChecker.addLanguageChangeLister(languageChangeListener);
	}

	private void registerDictionaries(final File orthoDir) {
		if(! orthoDir.isDirectory())
			return;
		final String[] dictionaryList = orthoDir.list(new FilenameFilter() {
			public boolean accept(final File dir, final String name) {
				return name.length() == "dictionary_XX.ortho".length() && name.startsWith("dictionary_")
				        && name.endsWith(".ortho");
			}
		});
		if (dictionaryList.length == 0) {
			return;
		}
		final ResourceController resourceController = ResourceController.getResourceController();
		SpellChecker.setUserDictionaryProvider(new FileUserDictionary(resourceController.getFreeplaneUserDirectory()));
		final StringBuilder availableLocales = new StringBuilder();
		for (int i = 0; i < dictionaryList.length; i++) {
			final String language = dictionaryList[i].substring("dictionary_".length(), "dictionary_".length() + 2);
			availableLocales.append(language);
			availableLocales.append(",");
		}
		try {
			SpellChecker.registerDictionaries(orthoDir.toURI().toURL(), availableLocales.toString(), null, ".ortho");
			spellCheckerEnabled = true;
		}
		catch (final MalformedURLException e) {
			LogUtils.severe(e);
		}
	}

	private void setSpellCheckOptions(final ResourceController resourceController) {
		final SpellCheckerOptions options = SpellChecker.getOptions();
		options.setCaseSensitive(resourceController.getBooleanProperty("spelling_opt_case_sensitive"));
		options.setIgnoreAllCapsWords(resourceController.getBooleanProperty("spelling_opt_ignore_all_caps_words"));
		options.setIgnoreCapitalization(resourceController.getBooleanProperty("spelling_opt_ignore_capitalization"));
		options.setIgnoreWordsWithNumbers(resourceController
		    .getBooleanProperty("spelling_opt_ignore_words_with_numbers"));
		options.setSuggestionsLimitDialog(resourceController
		    .getIntProperty("spelling_opt_suggestions_limit_dialog", 15));
		options.setSuggestionsLimitMenu(resourceController.getIntProperty("spelling_opt_suggestions_limit_menu", 15));
	}

	public boolean isSpellCheckerActive() {
		init();
		return spellCheckerEnabled;
	}
}
