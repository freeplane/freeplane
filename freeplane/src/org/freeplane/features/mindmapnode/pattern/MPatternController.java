/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Dimitry Polivaev in 2008.
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
package org.freeplane.features.mindmapnode.pattern;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JOptionPane;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.icon.IconStore;
import org.freeplane.core.icon.factory.IconStoreFactory;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.KeyAlreadyUsedException;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.url.UrlManager;
import org.freeplane.core.util.LogTool;

/**
 * @author Dimitry Polivaev
 */
public class MPatternController implements IExtension {
	public static MPatternController getController(final ModeController modeController) {
		return (MPatternController) modeController.getExtension(MPatternController.class);
	}

	public static void install(final ModeController modeController, final MPatternController patternController) {
		modeController.addExtension(MPatternController.class, patternController);
	}

	private static final IconStore STORE = IconStoreFactory.create();
	private final ModeController modeController;
	private final File patternsFile;
	private List<Pattern> mPatternsList = new ArrayList<Pattern>();
	public ApplyPatternAction patterns[] = new ApplyPatternAction[0];

	public MPatternController(final ModeController modeController) {
		super();
		this.modeController = modeController;
		patternsFile = new File(ResourceController.getResourceController().getFreeplaneUserDirectory(),
		    ResourceController.getResourceController().getProperty("patternsfile"));
		createActions();
	}

	public void applyPattern(final NodeModel node, final Pattern pattern) {
		if (patterns.length > 0) {
			patterns[0].applyPattern(node, pattern);
		}
		else {
			throw new IllegalArgumentException("No pattern defined.");
		}
	}

	public void applyPattern(final NodeModel node, final String patternName) {
		for (int i = 0; i < patterns.length; i++) {
			final ApplyPatternAction patternAction = patterns[i];
			if (patternAction.getPattern().getName().equals(patternName)) {
				patternAction.applyPattern(node, patternAction.getPattern());
				break;
			}
		}
	}

	/**
	 * Tries to load the user patterns and proposes an update to the new format,
	 * if they are old fashioned (this is determined by having an exception
	 * while reading the pattern file).
	 */
	private void createActions() {
		try {
			loadPatterns(getPatternReader());
		}
		catch (final Exception ex) {
			LogTool.severe("Patterns not loaded:" + ex);
			// repair old patterns:
			final String repairTitle = "Repair patterns";
			final File patternsFile = getPatternsFile();
			final int result = JOptionPane
			    .showConfirmDialog(null, "<html>The pattern file format has changed, <br>"
			            + "and it seems, that your pattern file<br>" + "'" + patternsFile.getAbsolutePath()
			            + "'<br> is formatted in the old way. <br>" + "Should I try to repair the pattern file <br>"
			            + "(otherwise, you should update it by hand or delete it)?", repairTitle,
			        JOptionPane.YES_NO_OPTION);
			if (result == JOptionPane.YES_OPTION) {
				// try xslt script:
				boolean success = false;
				try {
					loadPatterns(UrlManager.getUpdateReader(patternsFile, "/xslt/patterns_updater.xslt"));
					// save patterns directly:
					StylePatternFactory.savePatterns(new FileWriter(patternsFile), mPatternsList);
					success = true;
				}
				catch (final Exception e) {
					LogTool.severe(e);
				}
				if (success) {
					JOptionPane.showMessageDialog(UITools.getFrame(), "Successfully repaired the pattern file.",
					    repairTitle, JOptionPane.PLAIN_MESSAGE);
				}
				else {
					JOptionPane.showMessageDialog(UITools.getFrame(), "An error occured repairing the pattern file.",
					    repairTitle, JOptionPane.WARNING_MESSAGE);
				}
			}
		}
	}

	private void createPatterns(final List<Pattern> patternsList) throws Exception {
		mPatternsList = patternsList;
		patterns = new ApplyPatternAction[patternsList.size()];
		for (int i = 0; i < patterns.length; i++) {
			final Pattern actualPattern = patternsList.get(i);
			patterns[i] = new ApplyPatternAction(modeController, actualPattern);
			final PatternProperty patternIcon = actualPattern.getPatternIcon();
			if (patternIcon != null && patternIcon.getValue() != null) {
				patterns[i].putValue(Action.SMALL_ICON, STORE.getMindIcon(patternIcon.getValue()).getIcon());
			}
		}
	}

	/**
	 */
	public void createPatternSubMenu(final MenuBuilder builder, final String formatMenuString) {
		final String group = formatMenuString + "/patterns/patterns";
		builder.removeChildElements(group);
		StringBuilder sb = null;
		for (int i = 0; i < patterns.length; ++i) {
			try {
				builder.addAction(group, patterns[i], MenuBuilder.AS_CHILD);
			}
			catch (final KeyAlreadyUsedException e) {
				if (!formatMenuString.startsWith("/menu_bar/")) {
					continue;
				}
				if (sb == null) {
					sb = new StringBuilder(ResourceBundles.getText("doubled_patterns_ignored"));
				}
				sb.append('\n');
				sb.append(patterns[i].getKey());
			}
		}
		if (sb != null) {
			UITools.errorMessage(sb.toString());
		}
	}

	/**
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public Reader getPatternReader() throws FileNotFoundException, IOException {
		Reader reader = null;
		final File patternsFile = getPatternsFile();
		if (patternsFile != null && patternsFile.exists()) {
			reader = new FileReader(patternsFile);
		}
		else {
			System.out.println("User patterns file " + patternsFile + " not found.");
			reader = new BufferedReader(new InputStreamReader(ResourceController.getResourceController().getResource(
			    "/xml/patterns.xml").openStream()));
		}
		return reader;
	}

	public File getPatternsFile() {
		return patternsFile;
	}

	/**
	 * Creates the patterns actions (saved in array patterns), and the pure
	 * patterns list (saved in mPatternsList).
	 *
	 * @throws Exception
	 */
	public void loadPatterns(final Reader reader) throws Exception {
		createPatterns(StylePatternFactory.loadPatterns(reader));
	}
}
