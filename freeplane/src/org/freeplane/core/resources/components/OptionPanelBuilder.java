/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
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
package org.freeplane.core.resources.components;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.text.Collator;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.n3.nanoxml.XMLException;

/**
 * @author Dimitry Polivaev
 * 26.12.2008
 * <p>
 * Note that the OptionPanelBuilder allows to set a custom validator for options,
 * see {@link #addValidator(IValidator)}.
 */
public class OptionPanelBuilder {
	private class BooleanOptionCreator extends PropertyCreator {
		@Override
		public IPropertyControlCreator getCreator(final String name, final XMLElement data) {
			return createBooleanOptionCreator(name);
		}
	}

	private class ColorOptionCreator extends PropertyCreator {
		@Override
		public IPropertyControlCreator getCreator(final String name, final XMLElement data) {
			return createColorOptionCreator(name);
		}
	}

	private class ComboOptionCreator extends PropertyCreator {
		@Override
		public IPropertyControlCreator getCreator(final String name, final XMLElement data) {
			final int childrenCount = data.getChildrenCount();
			final Vector<String> choices = new Vector<String>(childrenCount);
			final Vector<String> translations = new Vector<String>(childrenCount);
			for (int i = 0; i < childrenCount; i++) {
				final XMLElement element = data.getChildAtIndex(i);
				final String choice = element.getAttribute("value", null);
				choices.add(choice);
				final String translationKey = element.getAttribute("text", "OptionPanel." + choice);
				final String translation = TextUtils.getOptionalText(translationKey);
				translations.add(translation);
			}
			return createComboProperty(name, choices, translations);
		}
	}
	
	private class LanguagesComboCreator extends PropertyCreator {
		@Override
		public IPropertyControlCreator getCreator(final String name, final XMLElement data) {
			final Set<String> locales = findAvailableLocales();
			locales.add(ResourceBundles.LANGUAGE_AUTOMATIC);
			final Vector<String> choices = new Vector<String>(locales.size());
			final Vector<String> translations = new Vector<String>(locales.size());
			// sort according to current locale
			final TreeMap<String, String> inverseMap = new TreeMap<String, String>(Collator.getInstance());
			for (String locale : locales) {
				final String translation = TextUtils.getOptionalText("OptionPanel." + locale);
				choices.add(locale);
				translations.add(translation);
				if (inverseMap.containsKey(translation)) {
					LogUtils.severe("translation " + translation + " is used for more that one locale, for "
					        + inverseMap.get(translation) + " and for " + locale + ".");
				}
				inverseMap.put(translation, locale);
			}
			if (inverseMap.size() == choices.size()) {
				// fix #630: Language not sorted alphabetically
				choices.clear();
				translations.clear();
				for (Entry<String, String> entry : inverseMap.entrySet()) {
					choices.add(entry.getValue());
					translations.add(entry.getKey());
				}
			}
			return createComboProperty(name, choices, translations);
		}

		private Set<String> findAvailableLocales() {
			final TreeSet<String> locales = new TreeSet<String>();
			final String name = "/translations/locales.txt";
			final InputStream stream = ResourceController.class.getResourceAsStream(name);
			if (stream == null) {
				LogUtils.info("available locales not found");
                // as this happens when Freeplane is started from Eclipse add some locales for developer's sake
                locales.addAll(Arrays.asList(("ar,ca,cs,da,de,el,es,et,fr,gl,hr,hu,id,it,ja,ko,lt,nb,nl,nn,pl,pt_BR,"
                        + "pt_PT,ru,sk,sl,sr,sv,tr,uk_UA,zh_CN,zh_TW,en").split(",")));
				return locales;
			}
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
                                FileUtils.copyStream(stream, out);
                                locales.addAll(Arrays.asList(out.toString().split("\\s+")));
			}
			catch (IOException e) {
				// OK - return locales
			}
			finally {
				FileUtils.silentlyClose(stream);
			}
			return locales;
		}
	}

	private class EmptyCreator extends PropertyCreator {
		@Override
		public IPropertyControlCreator getCreator(final String name, final XMLElement data) {
			return null;
		}
	}

	private class FontOptionCreator extends PropertyCreator {
		@Override
		public IPropertyControlCreator getCreator(final String name, final XMLElement data) {
			return createFontOptionCreator(name);
		}
	}

	private class KeyOptionCreator extends PropertyCreator {
		@Override
		public IPropertyControlCreator getCreator(final String name, final XMLElement data) {
			return createKeyOptionCreator(name);
		}
	}

	private class NumberOptionCreator extends PropertyCreator {
		private IPropertyControlCreator createNumberPropertyCreator(final String name, final int min, final int step,
		                                                            final int max) {
			return new IPropertyControlCreator() {
				public IPropertyControl createControl() {
					return new NumberProperty(name, min, max, step);
				}
			};
		}

		private IPropertyControlCreator createNumberPropertyCreator(
				final String name, final double min, final double step, final double max) {
			return new IPropertyControlCreator() {
				public IPropertyControl createControl() {
					return new NumberProperty(name, min, max, step);
				}
			};
		}
		
		@Override
		public IPropertyControlCreator getCreator(final String name, final XMLElement data) {
			final String minString = data.getAttribute("min", "1");
			final String maxString = data.getAttribute("max", MAX_INT);
			final String stepString = data.getAttribute("step", "1");
			if (minString.contains(".") || maxString.contains(".") || stepString.contains("."))
			{
				return createNumberPropertyCreator(name,
						Double.parseDouble(minString),
						Double.parseDouble(stepString),
						Double.parseDouble(maxString));
			}
			else
			{
				return createNumberPropertyCreator(name,
						Integer.parseInt(minString),
						Integer.parseInt(stepString),
						Integer.parseInt(maxString));
			}
		}
	}
	
	private class PathOptionCreator extends PropertyCreator {
		private IPropertyControlCreator createPathPropertyCreator(final String name, final boolean isDir,
		                                                          final String[] suffixes) {
			return new IPropertyControlCreator() {
				public IPropertyControl createControl() {
					return new PathProperty(name, isDir, suffixes);
				}
			};
		}
		
		@Override
		public IPropertyControlCreator getCreator(final String name, final XMLElement data) {
			final boolean isDir = Boolean.parseBoolean(data.getAttribute("dir", "false"));
			final String[] suffixes = parseCSV(data.getAttribute("suffixes", ""));
			return createPathPropertyCreator(name, isDir, suffixes);
		}

		// Parses CSV, strips whitespace, returns null if empty
		private String[] parseCSV(String csv) {
			if (csv == null)
				return null;
			final String[] result = csv.trim().split("\\s*,\\s*");
	        return result.length > 0 ? result : null;
        }
	}

	private static class Path {
		static Path emptyPath() {
			final Path Path = new Path(null);
			Path.path = null;
			return Path;
		}

		String parentPath;
		String path;

		Path(final String path) {
			parentPath = path;
		}

		void setName(final String name) {
			path = parentPath == null ? name : parentPath + '/' + name;
		}

		@Override
		public String toString() {
			return path;
		}
	};

	protected abstract class PropertyCreator implements IElementDOMHandler {
		public Object createElement(final Object parent, final String tag, final XMLElement attributes) {
			if (attributes == null) {
				return null;
			}
			final String name = attributes.getAttribute("name", null);
			if (name == null) {
				return parent == null ? Path.emptyPath() : parent;
			}
			final Path path = new Path(parent == null ? null : parent.toString());
			path.setName(name);
			if (!tree.contains(path.path)) {
				tree
				    .addElement(path.parentPath == null ? tree : path.parentPath, this, path.path, IndexedTree.AS_CHILD);
			}
			return path;
		}

		public void endElement(final Object parent, final String tag, final Object userObject,
		                       final XMLElement lastBuiltElement) {
			final String name = lastBuiltElement.getAttribute("name", null);
			final Path path = (Path) userObject;
			if (path.path == null) {
				return;
			}
			final DefaultMutableTreeNode treeNode = tree.get(path.path);
			if (treeNode.getUserObject() == this) {
				final IPropertyControlCreator creator = getCreator(name, lastBuiltElement);
				final String text = lastBuiltElement.getAttribute("text", null);
				if(text == null){
					treeNode.setUserObject(creator);
				}
				else{
					treeNode.setUserObject(new IPropertyControlCreator(){
						public IPropertyControl createControl() {
							final IPropertyControl control = creator.createControl();
							if( control instanceof PropertyAdapter){
								final PropertyAdapter control2 = (PropertyAdapter) control;
								control2.setLabel(text);
							}
							return control;
                        }});
				}
				
			}
		}

		abstract public IPropertyControlCreator getCreator(String name, XMLElement data);
	}

	private class RemindValueCreator extends PropertyCreator {
		@Override
		public IPropertyControlCreator getCreator(final String name, final XMLElement data) {
			return createRemindValueProperty(name);
		}
	}

	private class SeparatorCreator extends PropertyCreator {
		@Override
		public void endElement(final Object parent, final String tag, final Object userObject,
		                       final XMLElement lastBuiltElement) {
			final Path path = (Path) userObject;
			final DefaultMutableTreeNode treeNode = tree.get(path.path);
			if (treeNode.getUserObject() != this) {
				return;
			}
			super.endElement(parent, tag, userObject, lastBuiltElement);
			tree.addElement(path.parentPath == null ? tree : path.parentPath, nextLineCreator, IndexedTree.AS_CHILD);
			return;
		}

		@Override
		public IPropertyControlCreator getCreator(final String name, final XMLElement data) {
			final String label = "OptionPanel.separator." + name;
			return createSeparatorCreator(label);
		}
	}

	private class StringOptionCreator extends PropertyCreator {
		@Override
		public IPropertyControlCreator getCreator(final String name, final XMLElement data) {
			return createStringOptionCreator(name);
		}
	}

	private class TextBoxOptionCreator extends PropertyCreator {
		@Override
		public IPropertyControlCreator getCreator(final String name, final XMLElement data) {
			final int lines = data.getAttribute("lines", 2);
			return createTextBoxOptionCreator(name, lines);
		}
	}

	private class TabCreator extends PropertyCreator {
		@Override
		public IPropertyControlCreator getCreator(final String name, final XMLElement data) {
			final String label = "OptionPanel." + name;
			final String layout = data.getAttribute("layout", null);
			return createTabCreator(label, layout);
		}
	}

	private class TextCreator extends PropertyCreator {
		@Override
		public IPropertyControlCreator getCreator(final String name, final XMLElement data) {
			final String label = "OptionPanel.text." + name;
			return createTextCreator(label);
		}
	}

	private static final String MAX_INT = Integer.toString(Integer.MAX_VALUE);
	final private IPropertyControlCreator nextLineCreator;
	final private ReadManager readManager;
	final private IndexedTree tree;

	public OptionPanelBuilder() {
		readManager = new ReadManager();
		tree = new IndexedTree(null);
		nextLineCreator = new IPropertyControlCreator() {
			public IPropertyControl createControl() {
				return new NextLineProperty();
			}
		};
		initReadManager();
	}

	public void addBooleanProperty(final String path, final String name, final int position) {
		tree.addElement(path, createBooleanOptionCreator(name), path + "/" + name, position);
	}

	public void addColorProperty(final String path, final String name, final int position) {
		tree.addElement(path, createColorOptionCreator(name), path + "/" + name, position);
	}

	public void addComboProperty(final String path, final String name, final Vector<String> choices,
	                             final Vector<String> translations, final int position) {
		tree.addElement(path, createComboProperty(name, choices, translations), path + "/" + name, position);
	}

	public void addCreator(final String path, final IPropertyControlCreator creator, final int position) {
		tree.addElement(path, creator, position);
	}

	public void addCreator(final String path, final IPropertyControlCreator creator, final String name,
	                       final int position) {
		tree.addElement(path, creator, path + "/" + name, position);
	}

	public void addFontProperty(final String path, final String name, final int position) {
		tree.addElement(path, createFontOptionCreator(name), path + "/" + name, position);
	}

	public void addKeyProperty(final String path, final String name, final int position) {
		tree.addElement(path, createKeyOptionCreator(name), path + "/" + name, position);
	}

	public void addNumberProperty(final String path, final String name, final int min, final int max, final int step,
	                              final int position) {
		tree.addElement(path, createNumberOptionCreator(name, min, max, step), path + "/" + name, position);
	}

	public void addRemindValueProperty(final String path, final String name, final int position) {
		tree.addElement(path, createRemindValueProperty(name), path + "/" + name, position);
	}

	public void addSeparator(final String path, final String name, final int position) {
		tree.addElement(path, createSeparatorCreator(name), path + "/" + name, position);
	}

	public void addSpace(final String path, final int position) {
		tree.addElement(path, nextLineCreator, position);
	}

	public void addStringProperty(final String path, final String name, final int position) {
		tree.addElement(path, createStringOptionCreator(name), path + "/" + name, position);
	}

	public void addTab(final String name) {
		addTab(name, null, IndexedTree.AS_CHILD);
	}

	public void addTab(final String name, final String layout, final int position) {
		tree.addElement(tree, createTabCreator(name, layout), name, position);
	}

	public void addText(final String path, final String name, final int position) {
		tree.addElement(path, createTextCreator(name), path + "/" + name, position);
	}

	private IPropertyControlCreator createBooleanOptionCreator(final String name) {
		return new IPropertyControlCreator() {
			public IPropertyControl createControl() {
				return new BooleanProperty(name);
			}
		};
	}

	private IPropertyControlCreator createColorOptionCreator(final String name) {
		return new IPropertyControlCreator() {
			public IPropertyControl createControl() {
				return new ColorProperty(name, ResourceController.getResourceController().getDefaultProperty(name));
			}
		};
	}

	private IPropertyControlCreator createComboProperty(final String name, final Vector<String> choices,
	                                                    final Vector<String> translations) {
		return new IPropertyControlCreator() {
			public IPropertyControl createControl() {
				return new ComboProperty(name, choices, translations);
			}
		};
	}

	private IPropertyControlCreator createFontOptionCreator(final String name) {
		return new IPropertyControlCreator() {
			public IPropertyControl createControl() {
				return new FontProperty(name);
			}
		};
	}

	private IPropertyControlCreator createKeyOptionCreator(final String name) {
		return new IPropertyControlCreator() {
			public IPropertyControl createControl() {
				return new KeyProperty(name);
			}
		};
	}

	private Object createNumberOptionCreator(final String name, final int min, final int max, final int step) {
		return new IPropertyControlCreator() {
			public IPropertyControl createControl() {
				return new NumberProperty(name, min, max, step);
			}
		};
	}

	private IPropertyControlCreator createRemindValueProperty(final String name) {
		return new IPropertyControlCreator() {
			public IPropertyControl createControl() {
				return new RemindValueProperty(name);
			}
		};
	}

	private IPropertyControlCreator createSeparatorCreator(final String label) {
		return new IPropertyControlCreator() {
			public IPropertyControl createControl() {
				return new SeparatorProperty(label);
			}
		};
	}

	private IPropertyControlCreator createStringOptionCreator(final String name) {
		return new IPropertyControlCreator() {
			public IPropertyControl createControl() {
				return new StringProperty(name);
			}
		};
	}

	private IPropertyControlCreator createTextBoxOptionCreator(final String name, final int lines) {
		return new IPropertyControlCreator() {
			public IPropertyControl createControl() {
				return new TextBoxProperty(name, lines);
			}
		};
	}

	private IPropertyControlCreator createTabCreator(final String label, final String layout) {
		return new IPropertyControlCreator() {
			public IPropertyControl createControl() {
				if (layout != null) {
					return new TabProperty(label, layout);
				}
				return new TabProperty(label);
			}
		};
	}

	private IPropertyControlCreator createTextCreator(final String label) {
		return new IPropertyControlCreator() {
			public IPropertyControl createControl() {
				return new Text(label);
			}
		};
	}

	ReadManager getReadManager() {
		return readManager;
	}

	public DefaultMutableTreeNode getRoot() {
		return getTree().getRoot();
	}

	IndexedTree getTree() {
		return tree;
	}

	private void initReadManager() {
		readManager.addElementHandler("preferences_structure", new EmptyCreator());
		readManager.addElementHandler("tabbed_pane", new EmptyCreator());
		readManager.addElementHandler("group", new EmptyCreator());
		readManager.addElementHandler("tab", new TabCreator());
		readManager.addElementHandler("separator", new SeparatorCreator());
		readManager.addElementHandler("text", new TextCreator());
		readManager.addElementHandler("string", new StringOptionCreator());
		readManager.addElementHandler("textbox", new TextBoxOptionCreator());
		readManager.addElementHandler("font", new FontOptionCreator());
		readManager.addElementHandler("boolean", new BooleanOptionCreator());
		readManager.addElementHandler("number", new NumberOptionCreator());
		readManager.addElementHandler("path", new PathOptionCreator());
		readManager.addElementHandler("color", new ColorOptionCreator());
		readManager.addElementHandler("combo", new ComboOptionCreator());
		readManager.addElementHandler("languages", new LanguagesComboCreator());
		readManager.addElementHandler("key", new KeyOptionCreator());
		readManager.addElementHandler("remind_value", new RemindValueCreator());
	}

	public void load(final URL menu) {
		InputStreamReader reader = null;
		try {
			reader = new InputStreamReader(new BufferedInputStream(menu.openStream()));
			load(reader);
		}
		catch (final IOException e) {
			throw new RuntimeException(e);
		}
		finally {
			FileUtils.silentlyClose(reader);
		}
	}
	
	public void load(final Reader inputStreamReader) {
		final TreeXmlReader treeXmlReader = new TreeXmlReader(readManager);
		try {
			treeXmlReader.load(inputStreamReader);
		}
		catch (final XMLException e) {
			throw new RuntimeException(e);
		}
	}
}
