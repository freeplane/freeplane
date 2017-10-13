package org.freeplane.features.presentations.mindmapmode;

import static org.freeplane.features.presentations.mindmapmode.CollectionChangedEvent.EventType.COLLECTION_SIZE_CHANGED;
import static org.freeplane.features.presentations.mindmapmode.CollectionChangedEvent.EventType.SELECTION_CHANGED;
import static org.freeplane.features.presentations.mindmapmode.CollectionChangedEvent.EventType.SELECTION_INDEX_CHANGED;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.freeplane.core.util.TextUtils;

public class NamedElementCollection<T extends NamedElement<T>> implements Iterable<T>{
	final private DefaultComboBoxModel<Stringifyed<T>> elements;
	private int currentIndex;
	private ArrayList<CollectionChangeListener<T>> collectionChangeListeners;
	private boolean moveInProgress;
	private NamedElementFactory<T> factory;

	public NamedElementCollection(NamedElementFactory<T> factory) {
		super();
		this.factory = factory;
		this.elements =  new DefaultComboBoxModel<>();
		elements.addListDataListener(new ListDataListener() {

			@Override
			public void intervalRemoved(ListDataEvent e) {
			}
			
			@Override
			public void intervalAdded(ListDataEvent e) {
			}
			
			@Override
			public void contentsChanged(ListDataEvent e) {
				final int index = e.getIndex0();
				if (index == -1 && e.getIndex1() == index && ! moveInProgress){
					final int newIndex = elements.getIndexOf(elements.getSelectedItem());
					if(newIndex != -1) {
						currentIndex = newIndex;
						fireCollectionChangeEvent(SELECTION_INDEX_CHANGED);
						fireCollectionChangeEvent(SELECTION_CHANGED);
					}
				}
			}
		});
		currentIndex = -1;
		collectionChangeListeners = new ArrayList<>();
	}

	public ComboBoxModel<Stringifyed<T>> getElements() {
		return elements;
	}

	public void add(String name) {
			final T currentElement = getCurrentElement();
			final T newInstance = currentElement != null ? factory.create(currentElement, name) : factory.create(name);
			add(newInstance);
	}
	
	public void add(T element) {
		final int newElementIndex = currentIndex + 1;
		final Stringifyed<T> anObject = new Stringifyed<>(element);
		elements.insertElementAt(anObject, newElementIndex);
		elements.setSelectedItem(anObject);
		selectCurrentElement(newElementIndex);
		fireCollectionChangeEvent(COLLECTION_SIZE_CHANGED);
	}

	public void removeCurrentElement() {
		if(currentIndex >= 0) {
			elements.removeElementAt(currentIndex);
			selectCurrentElement(Math.min(currentIndex, elements.getSize() - 1));
			fireCollectionChangeEvent(COLLECTION_SIZE_CHANGED);
		}
	}

	public void selectCurrentElement(int index) {
		if(currentIndex != index) {
			currentIndex = index;
			final Stringifyed<T> newSelecteditem = index == -1  ? null : elements.getElementAt(index);
			if(newSelecteditem != elements.getSelectedItem()) {
				elements.setSelectedItem(newSelecteditem);
			}
			else { 
				fireCollectionChangeEvent(SELECTION_INDEX_CHANGED);
				fireCollectionChangeEvent(SELECTION_CHANGED);
			}
		}
	}

	private void fireCollectionChangeEvent(CollectionChangedEvent.EventType eventType) {
		for (CollectionChangeListener<T> selectionChangeListener : collectionChangeListeners)
			selectionChangeListener.onCollectionChange(eventType.of(this));
	}

	public int getSize() {
		return elements.getSize();
	}

	public T getCurrentElement() {
		return currentIndex >= 0 ? elements.getElementAt(currentIndex).element : null;
	}

	public void moveCurrentElementUp() {
		moveCurrentElementTo(currentIndex - 1);
	}

	public void moveCurrentElementDown() {
		moveCurrentElementTo(currentIndex + 1);
	}
	
	public void moveCurrentElementTo(int newElementIndex) {
		if(canMoveCurrentElementTo(newElementIndex)) {
			final Stringifyed<T> currentElement = elements.getElementAt(currentIndex);
			moveInProgress = true;
			try {
				elements.removeElementAt(currentIndex);
				elements.insertElementAt(currentElement, newElementIndex);
				currentIndex = newElementIndex;
				elements.setSelectedItem(currentElement);
			} finally {
				moveInProgress = false;
			}
			fireCollectionChangeEvent(SELECTION_INDEX_CHANGED);
		}
	}

	public boolean canMoveCurrentElementTo(int newElementIndex) {
		return newElementIndex >= 0 && newElementIndex < getSize() && newElementIndex != currentIndex;
	}

	public void addCollectionChangeListener(CollectionChangeListener<T> selectionChangeListener) {
		this.collectionChangeListeners.add(selectionChangeListener);
	}

	public void removeCollectionChangeListener(CollectionChangeListener<T> selectionChangeListener) {
		this.collectionChangeListeners.remove(selectionChangeListener);
	}

	public int getCurrentElementIndex() {
		return currentIndex;
	}

	public T getElement(int i) {
		return elements.getElementAt(i).element;
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			private int index = 0;
			@Override
			public boolean hasNext() {
				return index < getSize();
			}

			@Override
			public T next() {
				if(hasNext())
					return NamedElementCollection.this.getElement(index++);
				else
					throw new NoSuchElementException();
			}
			
			public void remove() {
			        throw new UnsupportedOperationException("remove");
			}
		};
	}

	public void copyCurrentElement() {
		final T currentElement = getCurrentElement();
		String newName = TextUtils.format("copy_name", currentElement.getName());
		final T newInstance = currentElement.saveAs(newName);
		add(newInstance);
	}
}
