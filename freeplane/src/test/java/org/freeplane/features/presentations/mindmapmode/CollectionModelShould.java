package org.freeplane.features.presentations.mindmapmode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.freeplane.features.presentations.mindmapmode.CollectionChangedEvent.EventType.COLLECTION_SIZE_CHANGED;
import static org.freeplane.features.presentations.mindmapmode.CollectionChangedEvent.EventType.SELECTION_CHANGED;
import static org.freeplane.features.presentations.mindmapmode.CollectionChangedEvent.EventType.SELECTION_INDEX_CHANGED;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import javax.swing.ComboBoxModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CollectionModelShould {
	private CollectionModel<PresentationModel> elementCollectionModel;
	@Mock
	private CollectionChangeListener<PresentationModel> listener;

	@Before 
	public void setup() {
		elementCollectionModel = new CollectionModel<PresentationModel>(PresentationModel.class);
	}
	
	@Test
	public void containNoElementsInitially() throws Exception {
		assertThat(elementCollectionModel.getSize()).isEqualTo(0);
	}
	
	@Test
	public void addNewElement() throws Exception {
		elementCollectionModel.add("element");
		ComboBoxModel<Stringifyed<PresentationModel>> elements = elementCollectionModel.getElements();
		assertThat(elements.getElementAt(0).toString()).isEqualTo("element");
	}
	
	@Test
	public void notifyListenerAddingNewElement() throws Exception {
		elementCollectionModel.addSelectionChangeListener(listener);
		elementCollectionModel.add("element");
		verify(listener).onCollectionChange(refEq(COLLECTION_SIZE_CHANGED.of(elementCollectionModel)));
		verify(listener).onCollectionChange(refEq(SELECTION_INDEX_CHANGED.of(elementCollectionModel)));
	}
	
	@Test
	public void setCurrentItemToNewElement() throws Exception {
		elementCollectionModel.add("element");
		ComboBoxModel<Stringifyed<PresentationModel>> elements = elementCollectionModel.getElements();
		assertThat(elements.getElementAt(0)).isEqualTo(elements.getSelectedItem());
	}


	@Test
	public void deleteElement() throws Exception {
		elementCollectionModel.add("element");
		elementCollectionModel.removeCurrentElement();
		ComboBoxModel<Stringifyed<PresentationModel>> elements = elementCollectionModel.getElements();
		assertThat(elements.getSize()).isEqualTo(0);
	}

	@Test
	public void notifyListenerDeletingElement() throws Exception {
		elementCollectionModel.add("element");
		elementCollectionModel.addSelectionChangeListener(listener);
		elementCollectionModel.removeCurrentElement();
		verify(listener).onCollectionChange(refEq(COLLECTION_SIZE_CHANGED.of(elementCollectionModel)));
	}

	@Test
	public void deleteLastAddedElement() throws Exception {
		elementCollectionModel.add("element 1");
		elementCollectionModel.add("element 2");
		elementCollectionModel.removeCurrentElement();
		ComboBoxModel<Stringifyed<PresentationModel>> elements = elementCollectionModel.getElements();
		assertThat(elements.getElementAt(0).toString()).isEqualTo("element 1");
	}


	@Test
	public void deleteSelectedElement() throws Exception {
		elementCollectionModel.add("element 1");
		elementCollectionModel.add("element 2");
		elementCollectionModel.selectCurrentElement(0);
		elementCollectionModel.removeCurrentElement();
		ComboBoxModel<Stringifyed<PresentationModel>> elements = elementCollectionModel.getElements();
		assertThat(elements.getElementAt(0).toString()).isEqualTo("element 2");
	}


	@Test
	public void addNewElementAfterSelectedElement() throws Exception {
		elementCollectionModel.add("element 1");
		elementCollectionModel.add("element 3");
		elementCollectionModel.selectCurrentElement(0);
		elementCollectionModel.add("element 2");
		ComboBoxModel<Stringifyed<PresentationModel>> elements = elementCollectionModel.getElements();
		Stringifyed<PresentationModel> element = elements.getElementAt(1);
		assertThat(element.toString()).isEqualTo("element 2");
	}

	@Test
	public void addAndDeleteElements() throws Exception {
		elementCollectionModel.add("element");
		elementCollectionModel.removeCurrentElement();
		elementCollectionModel.add("element");
		elementCollectionModel.removeCurrentElement();
		ComboBoxModel<Stringifyed<PresentationModel>> elements = elementCollectionModel.getElements();
		assertThat(elements.getSize()).isEqualTo(0);
	}


	@Test
	public void moveLastElementUp() throws Exception {
		elementCollectionModel.add("element 0");
		elementCollectionModel.add("element 1");
		elementCollectionModel.add("element 2");
		elementCollectionModel.moveCurrentElementUp();
		ComboBoxModel<Stringifyed<PresentationModel>> elements = elementCollectionModel.getElements();
		assertThat(elements.getElementAt(0).toString()).isEqualTo("element 0");
		assertThat(elements.getElementAt(1).toString()).isEqualTo("element 2");
		assertThat(elements.getElementAt(2).toString()).isEqualTo("element 1");
		assertThat(elements.getSelectedItem().toString()).isEqualTo("element 2");
	}
	

	@Test
	public void notifyListenerMovingLastElementUp() throws Exception {
		elementCollectionModel.add("element 0");
		elementCollectionModel.add("element 1");
		elementCollectionModel.add("element 2");
		elementCollectionModel.addSelectionChangeListener(listener);
		elementCollectionModel.moveCurrentElementUp();
		verify(listener).onCollectionChange(refEq(SELECTION_INDEX_CHANGED.of(elementCollectionModel)));
		verifyNoMoreInteractions(listener);
	}


	@Test
	public void moveSelectedElementUp() throws Exception {
		elementCollectionModel.add("element 0");
		elementCollectionModel.add("element 1");
		elementCollectionModel.add("element 2");
		elementCollectionModel.selectCurrentElement(1);
		elementCollectionModel.moveCurrentElementUp();
		ComboBoxModel<Stringifyed<PresentationModel>> elements = elementCollectionModel.getElements();
		assertThat(elements.getElementAt(0).toString()).isEqualTo("element 1");
		assertThat(elements.getElementAt(1).toString()).isEqualTo("element 0");
		assertThat(elements.getElementAt(2).toString()).isEqualTo("element 2");
		assertThat(elements.getSelectedItem().toString()).isEqualTo("element 1");
	}


	@Test
	public void moveFirstElementUp() throws Exception {
		elementCollectionModel.add("element 0");
		elementCollectionModel.add("element 1");
		elementCollectionModel.add("element 2");
		elementCollectionModel.selectCurrentElement(0);
		elementCollectionModel.moveCurrentElementUp();
		ComboBoxModel<Stringifyed<PresentationModel>> elements = elementCollectionModel.getElements();
		assertThat(elements.getElementAt(0).toString()).isEqualTo("element 0");
		assertThat(elements.getElementAt(1).toString()).isEqualTo("element 1");
		assertThat(elements.getElementAt(2).toString()).isEqualTo("element 2");
		assertThat(elements.getSelectedItem().toString()).isEqualTo("element 0");
	}


	@Test
	public void moveLastElementDown() throws Exception {
		elementCollectionModel.add("element 0");
		elementCollectionModel.add("element 1");
		elementCollectionModel.add("element 2");
		elementCollectionModel.moveCurrentElementDown();
		ComboBoxModel<Stringifyed<PresentationModel>> elements = elementCollectionModel.getElements();
		assertThat(elements.getElementAt(0).toString()).isEqualTo("element 0");
		assertThat(elements.getElementAt(1).toString()).isEqualTo("element 1");
		assertThat(elements.getElementAt(2).toString()).isEqualTo("element 2");
	}

	@Test
	public void moveSelectedElementDown() throws Exception {
		elementCollectionModel.add("element 0");
		elementCollectionModel.add("element 1");
		elementCollectionModel.add("element 2");
		elementCollectionModel.selectCurrentElement(1);
		elementCollectionModel.moveCurrentElementDown();
		ComboBoxModel<Stringifyed<PresentationModel>> elements = elementCollectionModel.getElements();
		assertThat(elements.getElementAt(0).toString()).isEqualTo("element 0");
		assertThat(elements.getElementAt(1).toString()).isEqualTo("element 2");
		assertThat(elements.getElementAt(2).toString()).isEqualTo("element 1");
	}


	@Test
	public void moveFirstElementDown() throws Exception {
		elementCollectionModel.add("element 0");
		elementCollectionModel.add("element 1");
		elementCollectionModel.add("element 2");
		elementCollectionModel.selectCurrentElement(0);
		elementCollectionModel.moveCurrentElementDown();
		ComboBoxModel<Stringifyed<PresentationModel>> elements = elementCollectionModel.getElements();
		assertThat(elements.getElementAt(0).toString()).isEqualTo("element 1");
		assertThat(elements.getElementAt(1).toString()).isEqualTo("element 0");
		assertThat(elements.getElementAt(2).toString()).isEqualTo("element 2");
	}

	@Test
	public void ignoresDeletingElementsFromEmptyCollection() throws Exception {
		elementCollectionModel.removeCurrentElement();
	}

	@Test
	public void changeCurrentElementIndexAfterSelectionChange() throws Exception {
		elementCollectionModel.add("element 1");
		elementCollectionModel.add("element 2");
		ComboBoxModel<Stringifyed<PresentationModel>> elements = elementCollectionModel.getElements();
		elements.setSelectedItem(elements.getElementAt(0));
		assertThat(elementCollectionModel.getCurrentElement().getName()).isEqualTo("element 1");
	}

	@Test
	public void notifyListenerOnSelectionChange() throws Exception {
		elementCollectionModel.add("element 1");
		elementCollectionModel.add("element 2");
		elementCollectionModel.addSelectionChangeListener(listener);
		elementCollectionModel.selectCurrentElement(0);
		
		verify(listener).onCollectionChange(refEq(SELECTION_INDEX_CHANGED.of(elementCollectionModel)));
		verify(listener).onCollectionChange(refEq(SELECTION_CHANGED.of(elementCollectionModel)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void doNotNotifyRemovedListeners() throws Exception {
		elementCollectionModel.add("element 1");
		elementCollectionModel.add("element 2");
		elementCollectionModel.addSelectionChangeListener(listener);
		elementCollectionModel.removeSelectionChangeListener(listener);
		elementCollectionModel.selectCurrentElement(0);
		verify(listener, never()).onCollectionChange(any(CollectionChangedEvent.class));
	}
}
