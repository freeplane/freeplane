package org.freeplane.features.presentations;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import static org.freeplane.features.presentations.CollectionChangedEvent.EventType.*;

public class CollectionModel<T extends NamedElement<T>> {
	final private DefaultComboBoxModel<Stringifyed<T>> elements;
	private int currentIndex;
	private Constructor<T> constructor;
	private ArrayList<CollectionChangeListener<T>> selectionChangeListeners;
	private boolean moveInProgress;

	public CollectionModel(Class<T> elementClass) {
		super();
		try {
			constructor = elementClass.getConstructor(String.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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
					selectCurrentElement(elements.getIndexOf(elements.getSelectedItem()));
				}
			}
		});
		currentIndex = -1;
		selectionChangeListeners = new ArrayList<>();
	}

	public ComboBoxModel<Stringifyed<T>> getElements() {
		return elements;
	}

	public void add(String name) {
		try {
			final T currentElement = getCurrentElement();
			final T newInstance = currentElement != null ? currentElement.saveAs(name) : constructor.newInstance(name);
			add(newInstance);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void add(T element) {
		final int newElementIndex = currentIndex + 1;
		elements.insertElementAt(new Stringifyed<>(element), newElementIndex);
		elements.setSelectedItem(element);
		fireCollectionChangeEvent(COLLECTION_SIZE_CHANGED);
		selectCurrentElement(newElementIndex);
	}

	public void removeCurrentElement() {
		if(currentIndex >= 0) {
			elements.removeElementAt(currentIndex);
			fireCollectionChangeEvent(COLLECTION_SIZE_CHANGED);
			selectCurrentElement(Math.min(currentIndex, elements.getSize() - 1));
		}
	}

	public void selectCurrentElement(int index) {
		if(currentIndex != index) {
			currentIndex = index;
			final Stringifyed<T> newSelecteditem = elements.getElementAt(index);
			if(newSelecteditem != elements.getSelectedItem()) {
				elements.setSelectedItem(newSelecteditem);
				fireCollectionChangeEvent(SELECTION_INDEX_CHANGED);
				fireCollectionChangeEvent(SELECTION_CHANGED);
			}
			else
				fireCollectionChangeEvent(SELECTION_INDEX_CHANGED);
		}
	}

	private void fireCollectionChangeEvent(CollectionChangedEvent.EventType eventType) {
		for (CollectionChangeListener<T> selectionChangeListener : selectionChangeListeners)
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
		if(newElementIndex >= 0 && newElementIndex < getSize() && newElementIndex != currentIndex) {
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

	public void addSelectionChangeListener(CollectionChangeListener<T> selectionChangeListener) {
		this.selectionChangeListeners.add(selectionChangeListener);
	}

	public void removeSelectionChangeListener(CollectionChangeListener<T> selectionChangeListener) {
		this.selectionChangeListeners.remove(selectionChangeListener);
	}

	public int getCurrentElementIndex() {
		return currentIndex;
	}

}
