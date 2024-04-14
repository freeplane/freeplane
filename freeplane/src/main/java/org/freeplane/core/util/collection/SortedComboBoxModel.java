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
package org.freeplane.core.util.collection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

/**
 * @author Dimitry Polivaev
 */
public class SortedComboBoxModel<T> extends AbstractListModel<T> implements ComboBoxModel<T>, IListModel<T>, Iterable<T> {

    private static final Comparator<Object> COMPARATOR = Comparator.comparing(Object::toString).thenComparing(x -> x.getClass().getName());

	private static final long serialVersionUID = 1L;
	private Object selectedItem;
	private final List<T> model;

    private final boolean areElementsCompable;
    public SortedComboBoxModel() {
        this(false);
    }

    public SortedComboBoxModel(Class<T> objectClass) {
		this(Comparable.class.isInstance(objectClass));
	}

	private SortedComboBoxModel(boolean areElementsCompable) {
	    this.areElementsCompable = areElementsCompable;
	    model = new ArrayList<T>();
    }

    @Override
    public void add(final T element) {
		addIfNotExists(element);
	}

	public T addIfNotExists(final T element) {
        T addedElement = addImpl(element);
        if(addedElement == element) {
            fireContentsChanged(this, 0, getSize());
        }
        return addedElement;
    }

	private T addImpl(final T element) {
	    int index = binarySearch(element);
		if(index >= 0)
			return model.get(index);
		model.add( - index - 1, element);
		return element;
    }

    private int binarySearch(final T element) {
        if (areElementsCompable)
            return Collections.binarySearch((List)model, (Comparable)element);
        return Collections.binarySearch(model, element, COMPARATOR);
    }

	public boolean addAll(final T[] elements) {
	    boolean contentsChanged = false;
		for(T e : elements)
		    contentsChanged = addImpl(e) == e || contentsChanged;
		if(contentsChanged)
		    fireContentsChanged(this, 0, getSize());
		return contentsChanged;
	}

	@Override
    public void clear() {
		final int oldSize = getSize();
		if (oldSize > 0) {
			model.clear();
			fireIntervalRemoved(this, 0, oldSize - 1);
		}
	}

	@Override
    public boolean contains(final Object element) {
		return binarySearch((T)element) >= 0;
	}

	public T firstElement() {
		return model.get(0);
	}

	@Override
    public T getElementAt(final int index) {
		return model.get(index);
	}

    public Optional<T> getElement(final T comparedElement) {
        int index = getIndexOf(comparedElement);
        return index >= 0 ? Optional.of(model.get(index)) : Optional.empty();
    }
	/**
	*/
	@Override
    public int getIndexOf(final T o) {
	    int index = binarySearch(o);
	    return index < 0 ? -1 : index;
	}

	@Override
    public int getSize() {
		return model.size();
	}

	@Override
    public Iterator<T> iterator() {
		return model.iterator();
	}

	public Object lastElement() {
		return model.get(model.size() - 1);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.controller.filter.util.SortedListModel#delete(java.lang.Object)
	 */
	@Override
    public void remove(final Object element) {
	    int index = binarySearch((T)element);
		if (index >= 0) {
		    model.remove(index);
			fireContentsChanged(this, index, index);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.controller.filter.util.SortedListModel#replace(java.lang.Object,
	 * java.lang.Object)
	 */
	@Override
    public void replace(final T oldO, final T newO) {
		if (oldO.equals(newO)) {
			return;
		}
		remove(oldO);
		add(newO);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.ComboBoxModel#getSelectedItem()
	 */
	@Override
    public Object getSelectedItem() {
		return selectedItem;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.ComboBoxModel#setSelectedItem(java.lang.Object)
	 */
	@Override
    public void setSelectedItem(final Object o) {
		selectedItem = o;
		fireContentsChanged(this, -1, -1);
	}

    public Stream<T> stream() {
       return model.stream();
    }
}
