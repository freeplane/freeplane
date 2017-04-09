/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.MindIcon;
import org.freeplane.features.icon.factory.IconStoreFactory;
import org.freeplane.features.icon.mindmapmode.MIconController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.script.ScriptContext;
import org.freeplane.plugin.script.proxy.Proxy.IconsRO;

class IconsProxy extends AbstractProxy<NodeModel> implements Proxy.Icons {
	IconsProxy(final NodeModel delegate, final ScriptContext scriptContext) {
		super(delegate, scriptContext);
	}

	public void add(final String name) {
		getIconController().addIcon(getDelegate(), IconStoreFactory.ICON_STORE.getMindIcon(name));
	}

	@Override
    public void addAll(Collection<String> names) {
		for (String name : names) {
	        add(name);
        }
    }

	@Override
    public void addAll(IconsRO icons) {
		for (String name : icons.getIcons()) {
	        add(name);
        }
    }

	@Deprecated
	public void addIcon(final String name) {
		add(name);
	}

	private int findIcon(final String iconID) {
		final List<MindIcon> icons = getDelegate().getIcons();
		for (int i = 0; i < icons.size(); i++) {
			if (icons.get(i).getName().equals(iconID)) {
				return i;
			}
		}
		return -1;
	}

	private MIconController getIconController() {
		return (MIconController) IconController.getController();
	}

	public String getAt(int index) {
		final List<MindIcon> icons = getDelegate().getIcons();
		return icons.size() <= index ? null : icons.get(index).getName();
	}

	public String getFirst() {
		final List<MindIcon> icons = getDelegate().getIcons();
		return icons.isEmpty() ? null : icons.get(0).getName();
	}

	public boolean contains(String name) {
		final List<MindIcon> icons = getDelegate().getIcons();
		for (final MindIcon icon : icons) {
			if (icon.getName().equals(name))
				return true;
		}
		return false;
	}

	public int size() {
		final List<MindIcon> icons = getDelegate().getIcons();
		return icons.size();
	}

	public List<String> getIcons() {
		final List<MindIcon> icons = getDelegate().getIcons();
		final int size = icons.size();
		if (size == 0) {
			return Collections.emptyList();
		}
		final ArrayList<String> list = new ArrayList<String>(size);
		for (final MindIcon icon : icons) {
			list.add(icon.getName());
		}
		return Collections.unmodifiableList(list);
	}

	public List<URL> getUrls() {
	    final List<MindIcon> icons = getDelegate().getIcons();
	    final int size = icons.size();
	    if (size == 0) {
	        return Collections.emptyList();
	    }
	    final ArrayList<URL> list = new ArrayList<URL>(size);
	    for (final MindIcon icon : icons) {
	        list.add(icon.getUrl());
	    }
	    return Collections.unmodifiableList(list);
	}

    public Iterator<String> iterator() {
        return new Iterator<String>() {
            final Iterator<String> iterator = getIcons().iterator();

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public String next() {
                return iterator.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("icons iterator is read-only");
            }
        };
    }

	public boolean remove(final int index) {
		if (index >= size()) {
			return false;
		}
		getIconController().removeIcon(getDelegate(), index);
		return true;
	}
	
	public boolean remove(final String iconID) {
		final int index = findIcon(iconID);
		if (index == -1) {
			return false;
		}
		getIconController().removeIcon(getDelegate(), index);
		return true;
	}

	@Deprecated
	public boolean removeIcon(final String iconID) {
		return remove(iconID);
	}
	
	public void clear() {
	    getIconController().removeAllIcons(getDelegate());
	}

    /** make <code>if (node.icons) println "has some icon"</code> work. */
    public boolean asBoolean() {
        return size() > 0;
    }
}
