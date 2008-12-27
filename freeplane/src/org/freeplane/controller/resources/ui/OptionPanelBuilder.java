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
package org.freeplane.controller.resources.ui;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.controller.Controller;
import org.freeplane.io.IXMLElementHandler;
import org.freeplane.io.NodeCreatorAdapter;
import org.freeplane.io.ReadManager;
import org.freeplane.io.xml.TreeXmlReader;
import org.freeplane.io.xml.n3.nanoxml.IXMLElement;
import org.freeplane.ui.IndexedTree;

/**
 * @author Dimitry Polivaev
 * 26.12.2008
 */
public class OptionPanelBuilder {
	private class BooleanOptionCreator extends PropertyCreator {
		@Override
		public IPropertyControlCreator getCreator(final String name, final IXMLElement data) {
			return createBooleanOptionCreator(name);
		}
	}

	private class ColorOptionCreator extends PropertyCreator {
		@Override
		public IPropertyControlCreator getCreator(final String name, final IXMLElement data) {
			return createColorOptionCreator(name);
		}
	}

	private class ComboOptionCreator extends PropertyCreator {
		@Override
		public IPropertyControlCreator getCreator(final String name, final IXMLElement data) {
			final int childrenCount = data.getChildrenCount();
			final Vector<String> choices = new Vector(childrenCount);
			final Vector<String> translations = new Vector(childrenCount);
			for (int i = 0; i < childrenCount; i++) {
				final String choice = data.getChildAtIndex(i).getAttribute("value", null);
				choices.add(choice);
				translations.add(OptionString.getText("OptionPanel." + choice));
			}
			return createComboProperty(name, choices, translations);
		}
	}

	private class DontShowNotificationPropertyCreator extends PropertyCreator {
		@Override
		public IPropertyControlCreator getCreator(final String name, final IXMLElement data) {
			return createDontShowNotificationProperty(name);
		}
	}

	private class EmptyCreator extends PropertyCreator {
		@Override
		public IPropertyControlCreator getCreator(final String name, final IXMLElement data) {
			return null;
		}
	}

	private class KeyOptionCreator extends PropertyCreator {
		@Override
		public IPropertyControlCreator getCreator(final String name, final IXMLElement data) {
			return createKeyOptionCreator(name);
		}
	}

	private class NumberOptionCreator extends PropertyCreator {
		private IPropertyControlCreator createNumberPropertyCreator(final String name,
		                                                            final int min, final int step,
		                                                            final int max) {
			return new IPropertyControlCreator() {
				public IPropertyControl createControl() {
					return new NumberProperty(name, min, max, step);
				}
			};
		}

		@Override
		public IPropertyControlCreator getCreator(final String name, final IXMLElement data) {
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
	}

	protected abstract class PropertyCreator extends NodeCreatorAdapter implements
	        IXMLElementHandler {
		public Object createNode(final Object parent, final String tag) {
			return new Path(parent == null ? null : parent.toString());
		}

		abstract public IPropertyControlCreator getCreator(String name, IXMLElement data);

		public boolean parse(final Object userObject, final String tag,
		                     final IXMLElement lastBuiltElement) {
			final String name = lastBuiltElement.getAttribute("name", null);
			final Path path = (Path) userObject;
			if (path.path == null) {
				return true;
			}
			final DefaultMutableTreeNode treeNode = tree.get(path.path);
			if (treeNode.getUserObject() == this) {
				final IPropertyControlCreator creator = getCreator(name, lastBuiltElement);
				treeNode.setUserObject(creator);
			}
			return true;
		}

		void registerFor(final String name) {
			readManager.addNodeCreator(name, this);
			readManager.addXMLElementHandler(name, this);
		}

		@Override
		public void setAttributes(final String tag, final Object userObject,
		                          final IXMLElement attributes) {
			final String name = attributes.getAttribute("name", null);
			if (name == null) {
				return;
			}
			final Path path = (Path) userObject;
			path.setName(name);
			if (!tree.contains(path.path)) {
				tree.addElement(path.parentPath == null ? tree : path.parentPath, this, path.path,
				    IndexedTree.AS_CHILD);
			}
		}
	};

	private class RemindValueCreator extends PropertyCreator {
		@Override
		public IPropertyControlCreator getCreator(final String name, final IXMLElement data) {
			return createRemindValueProperty(name);
		}
	}

	private class SeparatorCreator extends PropertyCreator {
		@Override
		public IPropertyControlCreator getCreator(final String name, final IXMLElement data) {
			final String label = "OptionPanel.separator." + name;
			return createSeparatorCreator(label);
		}

		@Override
		public boolean parse(final Object userObject, final String tag,
		                     final IXMLElement lastBuiltElement) {
			final Path path = (Path) userObject;
			final DefaultMutableTreeNode treeNode = tree.get(path.path);
			if (treeNode.getUserObject() != this) {
				return true;
			}
			super.parse(userObject, tag, lastBuiltElement);
			tree.addElement(path.parentPath, nextLineCreator, IndexedTree.AS_CHILD);
			return true;
		}
	}

	private class StringOptionCreator extends PropertyCreator {
		private IPropertyControlCreator createStringOptionCreator(final String name) {
			return new IPropertyControlCreator() {
				public IPropertyControl createControl() {
					return new StringProperty(name);
				}
			};
		}

		@Override
		public IPropertyControlCreator getCreator(final String name, final IXMLElement data) {
			return createStringOptionCreator(name);
		}
	}

	private class TabCreator extends PropertyCreator {
		@Override
		public IPropertyControlCreator getCreator(final String name, final IXMLElement data) {
			final String label = "OptionPanel." + name;
			final String layout = data.getAttribute("layout", null);
			return createTabCreator(label, layout);
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

	public void addComboProperty(final String path, final String name,
	                             final Vector<String> choices, final Vector<String> translations,
	                             final int position) {
		tree.addElement(path, createComboProperty(name, choices, translations), path + "/" + name,
		    position);
	}

	public void addCreator(final String path, final IPropertyControlCreator creator,
	                       final int position) {
		tree.addElement(path, creator, position);
	}

	public void addCreator(final String path, final IPropertyControlCreator creator,
	                       final String name, final int position) {
		tree.addElement(path, creator, path + "/" + name, position);
	}

	public void addDontShowNotificationProperty(final String path, final String name,
	                                            final int position) {
		tree
		    .addElement(path, createDontShowNotificationProperty(name), path + "/" + name, position);
	}

	public void addKeyProperty(final String path, final String name, final int position) {
		tree.addElement(path, createKeyOptionCreator(name), path + "/" + name, position);
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

	public void addTab(final String path, final String name, final int position) {
		addTab(path, name, null, position);
	}

	public void addTab(final String path, final String name, final String layout, final int position) {
		tree.addElement(path, createTabCreator(name, layout), path + "/" + name, position);
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
				return new ColorProperty(name, Controller.getResourceController()
				    .getDefaultProperty(name));
			}
		};
	}

	private IPropertyControlCreator createComboProperty(final String name,
	                                                    final Vector<String> choices,
	                                                    final Vector<String> translations) {
		return new IPropertyControlCreator() {
			public IPropertyControl createControl() {
				return new ComboProperty(name, choices, translations);
			}
		};
	}

	private IPropertyControlCreator createDontShowNotificationProperty(final String name) {
		return new IPropertyControlCreator() {
			public IPropertyControl createControl() {
				return new DontShowNotificationProperty(name);
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

	ReadManager getReadManager() {
		return readManager;
	}

	IndexedTree getTree() {
		return tree;
	}

	private void initReadManager() {
		new EmptyCreator().registerFor("preferences_structure");
		new EmptyCreator().registerFor("tabbed_pane");
		new EmptyCreator().registerFor("group");
		new TabCreator().registerFor("tab");
		new SeparatorCreator().registerFor("separator");
		new StringOptionCreator().registerFor("string");
		new BooleanOptionCreator().registerFor("boolean");
		new NumberOptionCreator().registerFor("number");
		new ColorOptionCreator().registerFor("color");
		new ComboOptionCreator().registerFor("combo");
		new KeyOptionCreator().registerFor("key");
		new RemindValueCreator().registerFor("remind_value");
		new DontShowNotificationPropertyCreator().registerFor("dont_show_notification_property");
	}

	public void load(final URL menu) {
		final TreeXmlReader reader = new TreeXmlReader(readManager);
		try {
			reader.load(new InputStreamReader(menu.openStream()));
		}
		catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
}
