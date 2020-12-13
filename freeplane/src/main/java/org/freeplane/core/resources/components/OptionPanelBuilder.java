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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.ui.TimePeriodUnits;
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
	static final class ComboPropertyCreator implements IPropertyControlCreator {
		private final Vector<String> choices;
		private final Vector<?> displayedItems;
		private final String name;
		private int verticalMargin;
		@SuppressWarnings("rawtypes")
		private Class enumClass;

		ComboPropertyCreator(Vector<String> choices, Vector<?> displayedItems, String name) {
			this.choices = choices;
			this.displayedItems = displayedItems;
			this.name = name;
			verticalMargin = 0;
		}

		@Override
		public IPropertyControl createControl() {
			@SuppressWarnings("unchecked")
			final ComboProperty comboProperty = enumClass != null ? ComboProperty.of(name, enumClass) : new ComboProperty(name, choices, displayedItems);
			if(verticalMargin > 0)
				comboProperty.setVerticalMargin(verticalMargin);
			return comboProperty;
		}

		public ComboPropertyCreator withVerticalMargin(int verticalMargin) {
			this.verticalMargin = verticalMargin;
			return this;
		}

		public ComboPropertyCreator withEnum(Class<?> enumClass) {
			this.enumClass = enumClass;
			return this;
		}
	}

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
			String enumClassName = data.getAttribute("enum", null);
			ComboPropertyCreator comboProperty;
			if(enumClassName != null) {
				try {
					Class<?> enumClass = OptionPanelBuilder.class.getClassLoader().loadClass(enumClassName);
					comboProperty = createComboProperty(name, enumClass);
				}
				catch (Exception e) {
					LogUtils.severe(e);
					return null;
				}
			}
			else {
				final int childrenCount = data.getChildrenCount();
				final Vector<String> choices = new Vector<String>(childrenCount);
				final Vector<Object> displayedItems = new Vector<Object>(childrenCount);
				addChoicesAndDisplayedItems(data, choices, displayedItems);
				comboProperty = createComboProperty(name, choices, displayedItems);
			}
			final int verticalMargin = Quantity.fromString(data.getAttribute("vertical_margin", "0"), LengthUnit.pt).toBaseUnitsRounded();
			return comboProperty.withVerticalMargin(verticalMargin);
		}

		private void addChoicesAndDisplayedItems(final XMLElement data, final Vector<String> choices,
				final Vector<Object> displayedItems) {
			for (int i = 0; i < data.getChildrenCount(); i++) {
				final XMLElement element = data.getChildAtIndex(i);
				final String choice = element.getAttribute("value", null);
				choices.add(choice);
				final String iconName = element.getAttribute("icon", null);
				final Object displayedItem;
				if(iconName != null) {
					displayedItem = ResourceController.getResourceController().getIcon("/images/" + iconName);
				}
				else {
					final String translationKey = element.getAttribute("text", "OptionPanel." + choice);
					displayedItem = TextUtils.getOptionalText(translationKey);
				}
				displayedItems.add(displayedItem);
			}
		}
	}
	private class LanguagesComboCreator extends PropertyCreator {
		@Override
		public IPropertyControlCreator getCreator(final String name, final XMLElement data) {
			final Set<String> locales = findAvailableLocales();
			locales.add(ResourceBundles.LANGUAGE_AUTOMATIC);
			final Vector<String> choices = new Vector<String>(locales.size());
			final Vector<String> displayedItems = new Vector<String>(locales.size());
			// sort according to current locale
			final TreeMap<String, String> inverseMap = new TreeMap<String, String>(Collator.getInstance());
			for (String locale : locales) {
				final String translation = TextUtils.getOptionalText("OptionPanel." + locale);
				choices.add(locale);
				displayedItems.add(translation);
				if (inverseMap.containsKey(translation)) {
					LogUtils.severe("translation " + translation + " is used for more that one locale, for "
					        + inverseMap.get(translation) + " and for " + locale + ".");
				}
				inverseMap.put(translation, locale);
			}
			if (inverseMap.size() == choices.size()) {
				// fix #630: Language not sorted alphabetically
				choices.clear();
				displayedItems.clear();
				for (Entry<String, String> entry : inverseMap.entrySet()) {
					choices.add(entry.getValue());
					displayedItems.add(entry.getKey());
				}
			}
			return createComboProperty(name, choices, displayedItems);
		}

		private Set<String> findAvailableLocales() {
			final TreeSet<String> locales = new TreeSet<String>();
			LogUtils.info("available locales not found");
			// as this happens when Freeplane is started from Eclipse add some locales for developer's sake
			locales.addAll(Arrays.asList(ResourceController.getResourceController().getProperty("locales") .split(",")));
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
				@Override
				public IPropertyControl createControl() {
					return new NumberProperty(name, min, max, step);
				}
			};
		}

		private IPropertyControlCreator createNumberPropertyCreator(
				final String name, final double min, final double step, final double max) {
			return new IPropertyControlCreator() {
				@Override
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

	private class LengthOptionCreator extends PropertyCreator {
		private IPropertyControlCreator createNumberPropertyCreator(
				final String name, final String defaultUnit, final double min, final double step, final double max) {
			return new IPropertyControlCreator() {
				@Override
				public IPropertyControl createControl() {
					return new QuantityProperty<LengthUnit>(name, min, max, step, LengthUnit.valueOf(defaultUnit));
				}
			};
		}

		@Override
		public IPropertyControlCreator getCreator(final String name, final XMLElement data) {
			final String minString = data.getAttribute("min", "0");
			final String maxString = data.getAttribute("max", "100000");
			final String stepString = data.getAttribute("step", "0.1");
			final String defaultUnit = data.getAttribute("defaultUnit", "px");
			return createNumberPropertyCreator(name,
					defaultUnit,
					Double.parseDouble(minString),
					Double.parseDouble(stepString),
					Double.parseDouble(maxString));
		}
	}

	private class TimePeriodOptionCreator extends PropertyCreator {
		private IPropertyControlCreator createNumberPropertyCreator(
				final String name, final String defaultUnit, final double min, final double step, final double max) {
			return new IPropertyControlCreator() {
				@Override
				public IPropertyControl createControl() {
					return new QuantityProperty<TimePeriodUnits>(name, min, max, step, TimePeriodUnits.valueOf(defaultUnit));
				}
			};
		}

		@Override
		public IPropertyControlCreator getCreator(final String name, final XMLElement data) {
			final String minString = data.getAttribute("min", "0");
			final String maxString = data.getAttribute("max", "100000");
			final String stepString = data.getAttribute("step", "1");
			final String defaultUnit = data.getAttribute("defaultUnit", "ms");
			return createNumberPropertyCreator(name,
					defaultUnit,
					Double.parseDouble(minString),
					Double.parseDouble(stepString),
					Double.parseDouble(maxString));
		}
	}
	private class PathOptionCreator extends PropertyCreator {
		private IPropertyControlCreator createPathPropertyCreator(final String name, final boolean isDir,
		                                                          final String[] suffixes) {
			return new IPropertyControlCreator() {
				@Override
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
		@Override
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

		@Override
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
						@Override
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

	private class MaybeBooleanCreator extends PropertyCreator {
		@Override
		public IPropertyControlCreator getCreator(final String name, final XMLElement data) {
			return createMaybeBooleanProperty(name);
		}
	}

	private class SeparatorCreator extends PropertyCreator {
		@Override
		public void endElement(final Object parent, final String tag, final Object userObject,
		                       final XMLElement lastBuiltElement) {
			final Path path = (Path) userObject;
			final DefaultMutableTreeNode treeNode = tree.get(path.path);
			if (treeNode.getUserObject() == this) {
				super.endElement(parent, tag, userObject, lastBuiltElement);
				tree.addElement(path.parentPath == null ? tree : path.parentPath, nextLineCreator, IndexedTree.AS_CHILD);
			}
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
			return createTextLineCreator(label);
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
			@Override
			public IPropertyControl createControl() {
				return new NextLineProperty();
			}
		};
		initReadManager();
	}

	public void addBooleanProperty(final String path, final String name, final int position) {
		addCreator(path, createBooleanOptionCreator(name), name, position);
	}

	public void addColorProperty(final String path, final String name, final int position) {
		addCreator(path, createColorOptionCreator(name), name, position);
	}

	public void addComboProperty(final String path, final String name, final Vector<String> choices,
	                             final Vector<?> displayedItems, final int position) {
		final IPropertyControlCreator creator = createComboProperty(name, choices, displayedItems);
		addCreator(path, creator, name, position);
	}

	public void addEditableComboProperty(final String path, final String name, final Vector<String> choices,
			final Vector<String> displayedItems, final int position) {
		final IPropertyControlCreator creator = createEditableComboProperty(name, choices, displayedItems);
		addCreator(path, creator, name, position);
	}
	public void addCreator(final String path, final IPropertyControlCreator creator, final int position) {
		tree.addElement(path, creator, position);
	}

	public void addCreator(final String path, final IPropertyControlCreator creator, final String name,
	                       final int position) {
		tree.addElement(path, creator, path + "/" + name, position);
	}

	public void addFontProperty(final String path, final String name, final int position) {
		addCreator(path, createFontOptionCreator(name), name, position);
	}

	public void addKeyProperty(final String path, final String name, final int position) {
		addCreator(path, createKeyOptionCreator(name), name, position);
	}

	public void addNumberProperty(final String path, final String name, final int min, final int max, final int step,
	                              final int position) {
		addCreator(path, createNumberOptionCreator(name, min, max, step), name, position);
	}

	public void addMaybeBooleanProperty(final String path, final String name, final int position) {
		addCreator(path, createMaybeBooleanProperty(name), name, position);
	}

	public void addSeparator(final String path, final String name, final int position) {
		addCreator(path, createSeparatorCreator(name), name, position);
	}

	public void addStringProperty(final String path, final String name, final int position) {
		addCreator(path, createStringOptionCreator(name), name, position);
	}

	public void addTab(final String name) {
		addTab(name, null, IndexedTree.AS_CHILD);
	}

	public void addTab(final String name, final String layout, final int position) {
		tree.addElement(tree, createTabCreator(name, layout), name, position);
	}

	public void addText(final String path, final String name, final int position) {
		addCreator(path, createTextLineCreator(name), name, position);
	}

	private IPropertyControlCreator createBooleanOptionCreator(final String name) {
		return new IPropertyControlCreator() {
			@Override
			public IPropertyControl createControl() {
				return new BooleanProperty(name);
			}
		};
	}

	private IPropertyControlCreator createColorOptionCreator(final String name) {
		return new IPropertyControlCreator() {
			@Override
			public IPropertyControl createControl() {
				return new ColorProperty(name, ResourceController.getResourceController().getDefaultProperty(name));
			}
		};
	}

	private ComboPropertyCreator createComboProperty(final String name, final Vector<String> choices,
			final Vector<?> displayedItems) {
		return new ComboPropertyCreator(choices, displayedItems, name);
	}

	public ComboPropertyCreator createComboProperty(String name, Class<?> enumClass) {
		return new ComboPropertyCreator(null, null, name).withEnum(enumClass);
	}


	private IPropertyControlCreator createEditableComboProperty(final String name, final Vector<String> choices,
			final Vector<String> displayedItems) {
		return new IPropertyControlCreator() {
			@Override
			public IPropertyControl createControl() {
				final ComboProperty comboProperty = new ComboProperty(name, choices, displayedItems);
				comboProperty.setEditable(true);
				return comboProperty;
			}
		};
	}

	private IPropertyControlCreator createFontOptionCreator(final String name) {
		return new IPropertyControlCreator() {
			@Override
			public IPropertyControl createControl() {
				return new FontProperty(name);
			}
		};
	}

	private IPropertyControlCreator createKeyOptionCreator(final String name) {
		return new IPropertyControlCreator() {
			@Override
			public IPropertyControl createControl() {
				return new KeyProperty(name);
			}
		};
	}

	private IPropertyControlCreator createNumberOptionCreator(final String name, final int min, final int max, final int step) {
		return new IPropertyControlCreator() {
			@Override
			public IPropertyControl createControl() {
				return new NumberProperty(name, min, max, step);
			}
		};
	}

	private IPropertyControlCreator createMaybeBooleanProperty(final String name) {
		return new IPropertyControlCreator() {
			@Override
			public IPropertyControl createControl() {
				return new MaybeBooleanProperty(name);
			}
		};
	}

	private IPropertyControlCreator createSeparatorCreator(final String label) {
		return new IPropertyControlCreator() {
			@Override
			public IPropertyControl createControl() {
				return new SeparatorProperty(label);
			}
		};
	}

	private IPropertyControlCreator createStringOptionCreator(final String name) {
		return new IPropertyControlCreator() {
			@Override
			public IPropertyControl createControl() {
				return new StringProperty(name);
			}
		};
	}

	private IPropertyControlCreator createTextBoxOptionCreator(final String name, final int lines) {
		return new IPropertyControlCreator() {
			@Override
			public IPropertyControl createControl() {
				return new TextBoxProperty(name, lines);
			}
		};
	}

	private IPropertyControlCreator createTabCreator(final String label, final String layout) {
		return new IPropertyControlCreator() {
			@Override
			public IPropertyControl createControl() {
				if (layout != null) {
					return new TabProperty(label, layout);
				}
				return new TabProperty(label);
			}
		};
	}

	private IPropertyControlCreator createTextLineCreator(final String label) {
		return new IPropertyControlCreator() {
			@Override
			public IPropertyControl createControl() {
				return new TextLine(label);
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
		readManager.addElementHandler("length", new LengthOptionCreator());
		readManager.addElementHandler("time_period", new TimePeriodOptionCreator());
		readManager.addElementHandler("path", new PathOptionCreator());
		readManager.addElementHandler("color", new ColorOptionCreator());
		readManager.addElementHandler("combo", new ComboOptionCreator());
		readManager.addElementHandler("languages", new LanguagesComboCreator());
		readManager.addElementHandler("key", new KeyOptionCreator());
		readManager.addElementHandler("maybe_boolean", new MaybeBooleanCreator());
	}

	public void load(final URL menu) {
		try (InputStreamReader reader = new InputStreamReader(new BufferedInputStream(menu.openStream()), StandardCharsets.UTF_8)){
			load(reader);
		}
		catch (final IOException e) {
			throw new RuntimeException(e);
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
