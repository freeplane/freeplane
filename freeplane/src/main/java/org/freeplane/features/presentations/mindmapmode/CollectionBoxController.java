package org.freeplane.features.presentations.mindmapmode;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.freeplane.core.ui.components.JAutoScrollBarPane;
import org.freeplane.features.presentations.CollectionChangeListener;
import org.freeplane.features.presentations.CollectionChangedEvent;
import org.freeplane.features.presentations.CollectionModel;
import org.freeplane.features.presentations.NamedElement;
import org.freeplane.features.presentations.Stringifyed;

class CollectionBoxController <T extends NamedElement> {
	private CollectionModel<T> collection;
	private JComboBox<Stringifyed<T>> comboBoxCollectionNames;
	private final ArrayList<JButton> buttons;
	private final Box collectionBox;
	private final JButton btnMoveUp;
	private final JButton btnMoveDown;
	private final JButton btnMove;
	private final JButton btnNewElement;
	private final JButton btnDeleteElement;
	private final CollectionChangeListener<T> collectionChangeListener;
	
	public JComponent getCollectionBox() {
		return collectionBox;
	}

	public CollectionBoxController(String title, final String newElementName) {
		buttons = new ArrayList<>();
		collectionBox = Box.createVerticalBox();
		collectionBox.setBorder(new TitledBorder(null, title, TitledBorder.LEADING, TitledBorder.TOP, null, null));
		comboBoxCollectionNames = new JComboBox<Stringifyed<T>>();
		collectionBox.add(comboBoxCollectionNames);
		comboBoxCollectionNames.setEditable(false);
		
		Box collectionButtons = Box.createHorizontalBox();
		collectionBox.add(collectionButtons);
		
		btnNewElement = createNewElementButton(newElementName);
		collectionButtons.add(btnNewElement);
		buttons.add(btnNewElement);
		
		btnDeleteElement = createDeleteElementButton();
		collectionButtons.add(btnDeleteElement);
		buttons.add(btnDeleteElement);
		
		Box orderButtons = Box.createHorizontalBox();
		collectionBox.add(orderButtons);
		
		btnMoveUp = createMoveUpButton();
		orderButtons.add(btnMoveUp);
		buttons.add(btnMoveUp);

		btnMoveDown = createMoveDownButton();
		orderButtons.add(btnMoveDown);                       
		buttons.add(btnMoveDown);
		
		btnMove = createMoveButton(collectionBox, btnMoveDown);
		orderButtons.add(btnMove);
		buttons.add(btnMove);
		disableUiElements();
		collectionChangeListener = new CollectionChangeListener<T>() {
			@Override
			public void onCollectionChange(CollectionChangedEvent<T> event) {
				if(event.eventType != CollectionChangedEvent.EventType.SELECTION_CHANGED)
				updateUiElements();
			}
		};
	}
	
	public void setCollection(CollectionModel<T> newCollection) {
		if(collection == newCollection)
			return;
		if(collection != null)
			collection.removeSelectionChangeListener(collectionChangeListener);;
		this.collection = newCollection;
		if(newCollection == null){
			disableUiElements();
		}
		else{
			final ComboBoxModel<Stringifyed<T>> elements = newCollection.getElements();
			comboBoxCollectionNames.setModel(elements);
			updateUiElements();
			collection.addSelectionChangeListener(collectionChangeListener);;
		}
	}

	private void updateUiElements() {
		final int collectionSize = collection.getSize();
		final int currentElementIndex = collection.getCurrentElementIndex();
		comboBoxCollectionNames.setEnabled(true);
		comboBoxCollectionNames.setEditable(collectionSize > 0);
		btnNewElement.setEnabled(true);
		btnDeleteElement.setEnabled(collectionSize > 0);
		btnMoveUp.setEnabled(currentElementIndex > 0);
		btnMoveDown.setEnabled(currentElementIndex >= 0 && currentElementIndex < collectionSize-1);
		btnMove.setEnabled(collectionSize > 1);
	}

	private void disableUiElements() {
		comboBoxCollectionNames.setModel(new DefaultComboBoxModel<Stringifyed<T>>());
		comboBoxCollectionNames.setEditable(false);
		comboBoxCollectionNames.setEnabled(false);
		for(JButton button : buttons)
			button.setEnabled(false);
	}

	private JButton createMoveButton(final Box collectionBox, JButton btnMoveDown) {
		JButton btnMove = new JButton("Move");
		btnMove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JList<Stringifyed<T>> targets = new JList<>(collection.getElements());
				targets.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				if (JOptionPane.showConfirmDialog(collectionBox, new JAutoScrollBarPane(targets), "Move before", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) 
						== JOptionPane.OK_OPTION)
					collection.moveCurrentElementTo(targets.getSelectedIndex());
			}
		});
		return btnMove;
	}
	private JButton createMoveDownButton() {
		JButton btnMoveDown = new JButton("Down");
		btnMoveDown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				collection.moveCurrentElementDown();
			}
		});
		return btnMoveDown;
	}
	private JButton createMoveUpButton() {
		JButton btnMoveUp = new JButton("Up");
		btnMoveUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				collection.moveCurrentElementUp();
			}
		});
		return btnMoveUp;
	}
	private JButton createDeleteElementButton() {
		JButton btnDeleteElement = new JButton("Delete");
		btnDeleteElement.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				collection.removeCurrentElement();
			}
		});
		return btnDeleteElement;
	}
	private JButton createNewElementButton(final String newElementName) {
		JButton btnNewElement = new JButton("Append");
		btnNewElement.addActionListener(new ActionListener() {
			

			@Override
			public void actionPerformed(ActionEvent e) {
				collection.add(newElementName);
				comboBoxCollectionNames.setEditable(true);
			}
		});
		return btnNewElement;
	}
	
}