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
import javax.swing.border.TitledBorder;

import org.freeplane.core.ui.components.JAutoScrollBarPane;

class CollectionBoxController <T extends NamedElement<T>> {
	private NamedElementCollection<T> collection;
	private JComboBox<Stringifyed<T>> comboBoxCollectionNames;
	private final JComponent[] components;
	private final JButton btnMoveUp;
	private final JButton btnMoveDown;
	private final JButton btnMove;
	private final JButton btnNewElement;
	private final JButton btnDeleteElement;
	private final CollectionChangeListener<T> collectionChangeListener;
	private JComponent collectionComponent;
	private JLabel lblElementCount;
	
	public JComponent createCollectionBox(String title) {
		collectionComponent = Box.createVerticalBox();
		collectionComponent.setBorder(new TitledBorder(null, title, TitledBorder.LEADING, TitledBorder.TOP, null, null));
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
		
		btnMove = createMoveButton(btnMoveDown);
		components = new JComponent[]{comboBoxCollectionNames, lblElementCount, btnNewElement, btnDeleteElement, btnMoveUp, btnMoveDown, btnMove};
		disableUiElements();
		collectionChangeListener = new CollectionChangeListener<T>() {
			@Override
			public void onCollectionChange(CollectionChangedEvent<T> event) {
				if(event.eventType != CollectionChangedEvent.EventType.SELECTION_CHANGED)
				updateUiElements();
			}
		};
	}
	
	public void setCollection(NamedElementCollection<T> newCollection) {
		if(collection == newCollection)
			return;
		if(collection != null)
			collection.removeCollectionChangeListener(collectionChangeListener);;
		this.collection = newCollection;
		if(newCollection == null){
			disableUiElements();
		}
		else{
			final ComboBoxModel<Stringifyed<T>> elements = newCollection.getElements();
			comboBoxCollectionNames.setModel(elements);
			updateUiElements();
			collection.addCollectionChangeListener(collectionChangeListener);;
		}
	}

	private void updateUiElements() {
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
		lblElementCount.setText( currentElementIndex >= 0 ? Integer.toString(currentElementIndex + 1) + "/" + Integer.toString(collectionSize) + ": " : "-/-: ");
	}

	private void disableUiElements() {
		comboBoxCollectionNames.setModel(new DefaultComboBoxModel<Stringifyed<T>>());
		lblElementCount.setText("-/-: ");
		for(JComponent c : components)
			c.setEnabled(false);
	}

	private JButton createMoveButton(JButton btnMoveDown) {
		JButton btnMove = new JButton("Move");
		btnMove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JList<Stringifyed<T>> targets = new JList<>(collection.getElements());
				targets.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				if (JOptionPane.showConfirmDialog(collectionComponent, new JAutoScrollBarPane(targets), "Move before", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) 
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
				collection.add(newElementName + " " + (collection.getSize() + 1));
				comboBoxCollectionNames.setEditable(true);
			}
		});
		return btnNewElement;
	}
	
}