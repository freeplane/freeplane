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
package org.freeplane.controller.resources;

import java.awt.Font;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;

import org.freeplane.controller.Controller;
import org.freeplane.controller.Freeplane;
import org.freeplane.main.Tools;

import deprecated.freemind.common.BooleanProperty;
import deprecated.freemind.common.ITextTranslator;
import deprecated.freemind.preferences.IFreemindPropertyListener;

/**
 * @author Dimitry Polivaev
 */
public abstract class ResourceController {
	public static final String LOCAL_PROPERTIES = "LocalProperties.";
	public static final String RESOURCE_DRAW_RECTANGLE_FOR_SELECTION = "standarddrawrectangleforselection";
	public static final String RESOURCE_LANGUAGE = "language";
	public static final String RESOURCE_PRINT_ON_WHITE_BACKGROUND = "printonwhitebackground";
	public static final String RESOURCES_BACKGROUND_COLOR = "standardbackgroundcolor";
	public static final String RESOURCES_CLOUD_COLOR = "standardcloudcolor";
	public static final String RESOURCES_CONVERT_TO_CURRENT_VERSION = "resources_convert_to_current_version";
	public static final String RESOURCES_CUT_NODES_WITHOUT_QUESTION = "resources_cut_nodes_without_question";
	public static final String RESOURCES_DELETE_NODES_WITHOUT_QUESTION = "delete_nodes_without_question";
	public static final String RESOURCES_DON_T_SHOW_NOTE_ICONS = "resources_don_t_show_note_icons";
	public static final String RESOURCES_EDGE_COLOR = "standardedgecolor";
	public static final String RESOURCES_EDGE_STYLE = "standardedgestyle";
	public static final String RESOURCES_EXECUTE_SCRIPTS_WITHOUT_ASKING = "resources_execute_scripts_without_asking";
	public static final String RESOURCES_EXECUTE_SCRIPTS_WITHOUT_EXEC_RESTRICTION = "resources_execute_scripts_without_exec_restriction";
	public static final String RESOURCES_EXECUTE_SCRIPTS_WITHOUT_FILE_RESTRICTION = "resources_execute_scripts_without_file_restriction";
	public static final String RESOURCES_EXECUTE_SCRIPTS_WITHOUT_NETWORK_RESTRICTION = "resources_execute_scripts_without_network_restriction";
	public static final String RESOURCES_LINK_COLOR = "standardlinkcolor";
	public static final String RESOURCES_NODE_SHAPE = "standardnodeshape";
	public static final String RESOURCES_NODE_TEXT_COLOR = "standardnodetextcolor";
	public static final String RESOURCES_REMIND_USE_RICH_TEXT_IN_NEW_LONG_NODES = "remind_use_rich_text_in_new_long_nodes";
	public static final String RESOURCES_REMOVE_NOTES_WITHOUT_QUESTION = "resources_remove_notes_without_question";
	public static final String RESOURCES_ROOT_NODE_SHAPE = "standardrootnodeshape";
	public static final String RESOURCES_SAVE_FOLDING_STATE = "resources_save_folding_state";
	public static final String RESOURCES_SCRIPT_USER_KEY_NAME_FOR_SIGNING = "resources_script_user_key_name_for_signing";
	public static final String RESOURCES_SELECTED_NODE_COLOR = "standardselectednodecolor";
	public static final String RESOURCES_SELECTED_NODE_RECTANGLE_COLOR = "standardselectednoderectanglecolor";
	public static final String RESOURCES_SELECTION_METHOD = "selection_method";
	public static final String RESOURCES_SIGNED_SCRIPT_ARE_TRUSTED = "resources_signed_script_are_trusted";
	public static final String RESOURCES_USE_SPLIT_PANE = "use_split_pane";
	public static final String RESOURCES_USE_TABBED_PANE = "use_tabbed_pane";
	public static final String RESOURCES_WHEEL_VELOCITY = "wheel_velocity";

	/**
	 * Removes the "TranslateMe" sign from the end of not translated texts.
	 */
	public static String removeTranslateComment(String inputString) {
		if (inputString != null
		        && inputString
		            .endsWith(FreemindResourceBundle.POSTFIX_TRANSLATE_ME)) {
			inputString = inputString.substring(0, inputString.length()
			        - FreemindResourceBundle.POSTFIX_TRANSLATE_ME.length());
		}
		return inputString;
	}

	private String baseDir;
	final private Vector propertyChangeListeners = new Vector();
	private FreemindResourceBundle resources;

	public ResourceController() {
		super();
	}

	public void addPropertyChangeListener(
	                                      final IFreemindPropertyListener listener) {
		propertyChangeListeners.add(listener);
	}

	/**
	 * @param listener
	 *            The new listener. All currently available properties are sent
	 *            to the listener after registration. Here, the oldValue
	 *            parameter is set to null.
	 */
	public void addPropertyChangeListenerAndPropagate(
	                                                  final IFreemindPropertyListener listener) {
		addPropertyChangeListener(listener);
		final Properties properties = getProperties();
		for (final Iterator it = properties.keySet().iterator(); it.hasNext();) {
			final String key = (String) it.next();
			listener.propertyChanged(key, properties.getProperty(key), null);
		}
	}

	public void clearLanguageResources() {
		resources = null;
	}

	public NamedObject createTranslatedString(final String key) {
		final String fs = getText(key);
		return new NamedObject(key, fs);
	}

	protected void firePropertyChanged(final String property,
	                                   final String value, final String oldValue) {
		if (oldValue == null || !oldValue.equals(value)) {
			setProperty(property, value);
			for (final Iterator i = getPropertyChangeListeners().iterator(); i
			    .hasNext();) {
				final IFreemindPropertyListener listener = (IFreemindPropertyListener) i
				    .next();
				listener.propertyChanged(property, value, oldValue);
			}
		}
	}

	public String format(final String resourceKey,
	                     final Object[] messageArguments) {
		final MessageFormat formatter = new MessageFormat(getText(resourceKey));
		final String stringResult = formatter.format(messageArguments);
		return stringResult;
	}

	public String getAdjustableProperty(final String label) {
		String value = getProperty(label);
		if (value == null) {
			return value;
		}
		if (value.startsWith("?") && !value.equals("?")) {
			final String localValue = ((FreemindResourceBundle) getResources())
			    .getResourceString(ResourceController.LOCAL_PROPERTIES + label,
			        null);
			value = localValue == null ? value.substring(1).trim() : localValue;
			setDefaultProperty(label, value);
		}
		return value;
	}

	public boolean getBoolProperty(final String key) {
		final String boolProperty = getProperty(key);
		return Tools.safeEquals("true", boolProperty);
	}

	public Font getDefaultFont() {
		final int fontSize = getDefaultFontSize();
		final int fontStyle = getDefaultFontStyle();
		final String fontFamily = getDefaultFontFamilyName();
		return new Font(fontFamily, fontStyle, fontSize);
	}

	/**
	 */
	public String getDefaultFontFamilyName() {
		final String fontFamily = getProperty("defaultfont");
		return fontFamily;
	}

	/**
	 */
	public int getDefaultFontSize() {
		final int fontSize = Integer.parseInt(getProperty("defaultfontsize"));
		return fontSize;
	}

	/**
	 */
	public int getDefaultFontStyle() {
		final int fontStyle = Integer.parseInt(getProperty("defaultfontstyle"));
		return fontStyle;
	}

	/**
	 * @param resourcesNodeTextColor
	 * @return
	 */
	public String getDefaultProperty(final String key) {
		return null;
	}

	/*
	 * We define the base dir of FreeMind as the directory where accessories,
	 * plugins and other things are to be found. We expect it to be either the
	 * directory where the main jar file is (freemind.jar), or the root of the
	 * class hierarchy (if no jar file is used), after any 'lib' directory is
	 * removed. One can overwrite this definition by setting the
	 * freemind.base.dir property.
	 */
	public String getFreemindBaseDir() {
		if (baseDir == null) {
			try {
				File file;
				final String dir = System.getProperty("freemind.base.dir");
				if (dir == null) {
					final String classname = this.getClass().getName();
					final URL url = this.getClass().getResource(
					    classname.replaceFirst("^"
					            + this.getClass().getPackage().getName() + ".",
					        "")
					            + ".class");
					file = new File(URLDecoder.decode(url.getPath()
					    .replaceFirst("^(file:|jar:)+", "").replaceFirst(
					        "!.*$", "").replaceFirst(
					        classname.replace('.', '/') + ".class$", ""),
					    "UTF-8"));
					if (file.isFile()) {
						file = file.getParentFile();
					}
					/*
					 * Now, we remove the lib directory: Example:
					 * /home/foltin/freemindapp/lib/freemind.jar gives
					 * /home/foltin/freemindapp
					 */
					if (file.getName().equals("lib")) {
						file = file.getParentFile();
					}
				}
				else {
					file = new File(dir);
				}
				if (!file.exists()) {
					throw new IllegalArgumentException("FreeMind base dir '"
					        + file + "' does not exist.");
				}
				if (!file.isDirectory()) {
					throw new IllegalArgumentException(
					    "FreeMind base dir (!) '" + file
					            + "' is not a directory.");
				}
				baseDir = file.getCanonicalPath();
			}
			catch (final Exception e) {
				Tools.logException(e);
				throw new IllegalArgumentException(
				    "FreeMind base dir can't be determined.");
			}
		}
		return baseDir;
	}

	/**
	 * Old version using String manipulation out of the classpath to find the
	 * base dir.
	 */
	public String getFreemindBaseDirOld() {
		if (baseDir == null) {
			final String classPath = System.getProperty("java.class.path");
			final String mainJarFile = "freemind.jar";
			int lastpos = classPath.indexOf(mainJarFile);
			int firstpos = 0;
			if (lastpos == -1) {
				baseDir = System.getProperty("user.dir");
				return baseDir;
			}
			/*
			 * fc: Now, if freemind.jar is the first, firstpos == -1. This
			 * results in bad results in the substring method, or not??
			 */
			firstpos = classPath.lastIndexOf(File.pathSeparator, lastpos) + 1;
			lastpos -= 1;
			if (lastpos > firstpos) {
				baseDir = classPath.substring(firstpos, lastpos);
			}
			else {
				baseDir = "";
			}
			final File basePath = new File(baseDir);
			baseDir = basePath.getAbsolutePath();
			/*
			 * I suppose, that here, the freemind.jar is removed together with
			 * the last path. Example: /home/foltin/freemindapp/lib/freemind.jar
			 * gives /home/foltin/freemindapp
			 */
			lastpos = baseDir.lastIndexOf(File.separator);
			if (lastpos > -1) {
				baseDir = baseDir.substring(0, lastpos);
			}
		}
		return baseDir;
	}

	public ClassLoader getFreeMindClassLoader() {
		final ClassLoader classLoader = this.getClass().getClassLoader();
		try {
			return new URLClassLoader(new URL[] { Tools.fileToUrl(new File(
			    getFreemindBaseDir())) }, classLoader);
		}
		catch (final MalformedURLException e) {
			org.freeplane.main.Tools.logException(e);
			return classLoader;
		}
	}

	/**
	 * @return
	 */
	abstract public String getFreemindUserDirectory();

	public int getIntProperty(final String key, final int defaultValue) {
		try {
			return Integer.parseInt(getProperty(key));
		}
		catch (final NumberFormatException nfe) {
			return defaultValue;
		}
	}

	/**
	 * @return
	 */
	abstract public Properties getProperties();

	abstract public String getProperty(final String key);

	public String getProperty(final String key, final String value) {
		return getProperties().getProperty(key, value);
	}

	public Collection getPropertyChangeListeners() {
		return Collections.unmodifiableCollection(propertyChangeListeners);
	}

	abstract public URL getResource(String string);

	/** Returns the ResourceBundle with the current language */
	public ResourceBundle getResources() {
		if (resources == null) {
			resources = new FreemindResourceBundle(this);
		}
		return resources;
	}

	public String getText(final String key) {
		return ((FreemindResourceBundle) getResources()).getResourceString(key);
	}

	public String getText(final String key, final String resource) {
		return ((FreemindResourceBundle) getResources()).getResourceString(key,
		    resource);
	}

	public ITextTranslator getTextTranslator() {
		return new ITextTranslator() {
			public String getText(final String key) {
				return ResourceController.this.getText(key);
			}
		};
	}

	public void init() {
		final Controller controller = Freeplane.getController();
		controller.addAction("propertyAction", new PropertyAction());
		controller.addAction("optionHTMLExportFoldingAction",
		    new OptionHTMLExportFoldingAction());
		controller.addAction("optionSelectionMechanismAction",
		    new OptionSelectionMechanismAction());
		controller.addAction("showSelectionAsRectangle",
		    new ShowSelectionAsRectangleAction());
	}

	boolean isSelectionAsRectangle() {
		return getProperty(
		    ResourceController.RESOURCE_DRAW_RECTANGLE_FOR_SELECTION)
		    .equalsIgnoreCase(BooleanProperty.TRUE_VALUE);
	}

	public void removePropertyChangeListener(
	                                         final IFreemindPropertyListener listener) {
		propertyChangeListeners.remove(listener);
	}

	abstract public void saveProperties();

	abstract void setDefaultProperty(final String key, final String value);

	abstract public void setProperty(final String property, final String value);

	public void toggleSelectionAsRectangle() {
		if (isSelectionAsRectangle()) {
			setProperty(
			    ResourceController.RESOURCE_DRAW_RECTANGLE_FOR_SELECTION,
			    BooleanProperty.FALSE_VALUE);
		}
		else {
			setProperty(
			    ResourceController.RESOURCE_DRAW_RECTANGLE_FOR_SELECTION,
			    BooleanProperty.TRUE_VALUE);
		}
	}
}
