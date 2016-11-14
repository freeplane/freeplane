package org.freeplane.features.presentations.mindmapmode;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import org.freeplane.core.ui.components.JAutoScrollBarPane;
import org.freeplane.core.ui.textchanger.TranslatedElementFactory;
import org.freeplane.core.util.TextUtils;

class CollectionBoxController <T extends NamedElement<T>> {
	private NamedElementCollection<T> collection;
	private JComboBox<Stringifyed<T>> comboBoxCollectionNames;
	private final JComponent[] components;
	private final JComponent[] editingComponents;
	private final JButton btnMoveUp;
	private final JButton btnMoveDown;
	private final JButton btnMove;
	private final JButton btnNewElement;
	private final JButton btnDeleteElement;
	private final CollectionChangeListener<T> collectionChangeListener;
	private JComponent collectionComponent;
	private JLabel lblElementCount;
	
	public JComponent createCollectionBox() {
		collectionComponent = Box.createVerticalBox();
		Box namesBox = Box.createHorizontalBox();
		namesBox.add(lblElementCount);
		namesBox.add(comboBoxCollectionNames);
		collectionComponent.add(namesBox);
		Box collectionButtons = Box.createHorizontalBox();
		collectionButtons.add(btnNewElement);
		collectionButtons.add(btnDeleteElement);
		collectionComponent.add(collectionButtons);
		Box orderButtons = Box.createHorizontalBox();
		collectionComponent.add(orderButtons);
		orderButtons.add(btnMoveUp);
		orderButtons.add(btnMoveDown);                       
		orderButtons.add(btnMove);
		return collectionComponent;
	}

	public CollectionBoxController(final String newElementName) {
		comboBoxCollectionNames = new JComboBox<Stringifyed<T>>();
		comboBoxCollectionNames.setEditable(false);
		Dimension comboBoxPreferredSize = comboBoxCollectionNames.getPreferredSize();
		comboBoxCollectionNames.setMaximumSize(new Dimension(Integer.MAX_VALUE, comboBoxPreferredSize.height));
		lblElementCount = new JLabel("XXX/XXX: ");
		lblElementCount.setHorizontalAlignment(SwingConstants.RIGHT);
		lblElementCount.setPreferredSize(lblElementCount.getPreferredSize());
		btnNewElement = createNewElementButton(newElementName);
		
		btnDeleteElement = createDeleteElementButton();
		
		btnMoveUp = createMoveUpButton();

		btnMoveDown = createMoveDownButton();
		
		btnMove = createMoveButton();
		components = new JComponent[]{comboBoxCollectionNames, lblElementCount, btnNewElement, btnDeleteElement, btnMoveUp, btnMoveDown, btnMove};
		editingComponents = new JComponent[] { btnNewElement, btnDeleteElement, btnMoveUp, btnMoveDown, btnMove };
		disableUiElements();
		collectionChangeListener = new CollectionChangeListener<T>() {
			@Override
			public void onCollectionChange(CollectionChangedEvent<T> event) {
				if(event.eventType != CollectionChangedEvent.EventType.SELECTION_CHANGED
						&& btnNewElement.isEnabled())
				updateUiElements();
			}
		};
	}
	
	public void setCollection(NamedElementCollection<T> newCollection) {
		if(collection == newCollection)
			return;
		if(collection != null)
			collection.removeCollectionChangeListener(collectionChangeListener);
		this.collection = newCollection;
		if(newCollection == null){
			disableUiElements();
		}
		else{
			final ComboBoxModel<Stringifyed<T>> elements = newCollection.getElements();
			comboBoxCollectionNames.setModel(elements);
			updateUiElements();
			collection.addCollectionChangeListener(collectionChangeListener);
		}
	}

	private void updateUiElements() {
		if(btnNewElement.isEnabled())
			enableUiElements();
		final int collectionSize = collection.getSize();
		final int currentElementIndex = collection.getCurrentElementIndex();
		lblElementCount.setText( currentElementIndex >= 0 ? Integer.toString(currentElementIndex + 1) + "/" + Integer.toString(collectionSize) + ": " : "-/-: ");
	}

	private void enableUiElements() {
		final int collectionSize = collection.getSize();
		final int currentElementIndex = collection.getCurrentElementIndex();
		comboBoxCollectionNames.setEnabled(true);
		lblElementCount.setEnabled(true);
		comboBoxCollectionNames.setEditable(collectionSize > 0);
		btnNewElement.setEnabled(true);
		btnDeleteElement.setEnabled(collectionSize > 0);
		btnMoveUp.setEnabled(currentElementIndex > 0);
		btnMoveDown.setEnabled(currentElementIndex >= 0 && currentElementIndex < collectionSize-1);
		btnMove.setEnabled(collectionSize > 1);
	}

	private void disableUiElements() {
		comboBoxCollectionNames.setModel(new DefaultComboBoxModel<Stringifyed<T>>());
		lblElementCount.setText("-/-: ");
		for(JComponent c : components)
			c.setEnabled(false);
	}

	private JButton createMoveButton() {
		JButton btnMove = TranslatedElementFactory.createButton("collection.move");
		btnMove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JList<Stringifyed<T>> targets = new JList<>(collection.getElements());
				targets.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				final String title = TextUtils.getText("collection.movebefore");
				if (JOptionPane.showConfirmDialog(collectionComponent, new JAutoScrollBarPane(targets), title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) 
						== JOptionPane.OK_OPTION)
					UndoableNamedElementCollection.of(collection).moveCurrentElementTo(targets.getSelectedIndex());
			}
		});
		return btnMove;
	}
	private JButton createMoveDownButton() {
		JButton btnMoveDown = TranslatedElementFactory.createButton("collection.down");
		btnMoveDown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UndoableNamedElementCollection.of(collection).moveCurrentElementDown();
			}
		});
		return btnMoveDown;
	}
	private JButton createMoveUpButton() {
		JButton btnMoveUp = TranslatedElementFactory.createButton("collection.up");
		btnMoveUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UndoableNamedElementCollection.of(collection).moveCurrentElementUp();
			}
		});
		return btnMoveUp;
	}
	private JButton createDeleteElementButton() {
		JButton btnDeleteElement = TranslatedElementFactory.createButton("collection.delete");
		btnDeleteElement.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UndoableNamedElementCollection.of(collection).removeCurrentElement();
			}
		});
		return btnDeleteElement;
	}
	private JButton createNewElementButton(final String newElementName) {
		JButton btnNewElement = TranslatedElementFactory.createButton("collection.append");
		btnNewElement.addActionListener(new ActionListener() {
			

			@Override
			public void actionPerformed(ActionEvent e) {
				UndoableNamedElementCollection.of(collection).add(newElementName + " " + (collection.getSize() + 1));
			}
		});
		return btnNewElement;
	}

	void disableEditing() {
		for (JComponent c : editingComponents)
			c.setEnabled(false);
	}

	void enableEditing() {
		if (collection != null)
			enableUiElements();
	}
	
}