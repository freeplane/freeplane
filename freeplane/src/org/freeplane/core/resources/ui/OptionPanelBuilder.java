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
package org.freeplane.core.resources.ui;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.core.resources.FpStringUtils;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.n3.nanoxml.XMLException;

/**
 * @author Dimitry Polivaev
 * 26.12.2008
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
			final Vector<String> choices = new Vector(childrenCount);
			final Vector<String> translations = new Vector(childrenCount);
			for (int i = 0; i < childrenCount; i++) {
				final String choice = data.getChildAtIndex(i).getAttribute("value", null);
				choices.add(choice);
				translations.add(FpStringUtils.getOptionalText("OptionPanel." + choice));
			}
			return createComboProperty(name, choices, translations);
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

		@Override
		public IPropertyControlCreator getCreator(final String name, final XMLElement data) {
			final int min = Integer.parseInt(data.getAttribute("min", "1"));
			final int step = Integer.parseInt(data.getAttribute("step", "1"));
			final int max = Integer.parseInt(data.getAttribute("max", MAX_INT));
			return createNumberPropertyCreator(name, min, step, max);
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
				treeNode.setUserObject(creator);
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
		readManager.addElementHandler("font", new FontOptionCreator());
		readManager.addElementHandler("boolean", new BooleanOptionCreator());
		readManager.addElementHandler("number", new NumberOptionCreator());
		readManager.addElementHandler("color", new ColorOptionCreator());
		readManager.addElementHandler("combo", new ComboOptionCreator());
		readManager.addElementHandler("key", new KeyOptionCreator());
		readManager.addElementHandler("remind_value", new RemindValueCreator());
	}

	public void load(final URL menu) {
		final TreeXmlReader reader = new TreeXmlReader(readManager);
		try {
			reader.load(new InputStreamReader(new BufferedInputStream(menu.openStream())));
		}
		catch (final IOException e) {
			throw new RuntimeException(e);
		}
		catch (final XMLException e) {
			throw new RuntimeException(e);
		}
	}
}
