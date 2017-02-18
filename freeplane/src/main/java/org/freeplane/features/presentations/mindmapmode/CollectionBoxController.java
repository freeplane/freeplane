package org.freeplane.features.presentations.mindmapmode;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.ComboBoxEditor;
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
	private JLabel lblElementCounter;
	
	public JComponent createCollectionBox() {
		collectionComponent = Box.createHorizontalBox();
		final Box controls = Box.createVerticalBox();
		controls.add(comboBoxCollectionNames);
		Box collectionButtons = Box.createHorizontalBox();
		collectionButtons.add(btnNewElement);
		collectionButtons.add(btnDeleteElement);
		collectionButtons.add(btnMoveUp);
		collectionButtons.add(btnMoveDown);                       
		collectionButtons.add(btnMove);
		collectionButtons.add(Box.createHorizontalGlue());
		collectionButtons.add(lblElementCounter);
		controls.add(collectionButtons);
		collectionComponent.add(controls);
		return collectionComponent;
	}

	public CollectionBoxController(final String elementName) {
		comboBoxCollectionNames = new JComboBox<Stringifyed<T>>();
		comboBoxCollectionNames.setEditable(false);
		Dimension comboBoxPreferredSize = comboBoxCollectionNames.getPreferredSize();
		comboBoxCollectionNames.setMaximumSize(new Dimension(Integer.MAX_VALUE, comboBoxPreferredSize.height));
		lblElementCounter = new JLabel("XXX/XXX");
		lblElementCounter.setHorizontalAlignment(SwingConstants.RIGHT);
		lblElementCounter.setPreferredSize(lblElementCounter.getPreferredSize());
		btnNewElement = createNewElementButton(elementName);
		
		btnDeleteElement = createDeleteElementButton(elementName);
		
		btnMoveUp = createMoveUpButton(elementName);

		btnMoveDown = createMoveDownButton(elementName);
		
		btnMove = createMoveButton(elementName);
		components = new JComponent[]{comboBoxCollectionNames, lblElementCounter, btnNewElement, btnDeleteElement, btnMoveUp, btnMoveDown, btnMove};
		editingComponents = new JComponent[] { btnNewElement, btnDeleteElement, btnMoveUp, btnMoveDown, btnMove };
		disableUiElements();
		collectionChangeListener = new CollectionChangeListener<T>() {
			@Override
			public void onCollectionChange(CollectionChangedEvent<T> event) {
				if(event.eventType != CollectionChangedEvent.EventType.SELECTION_CHANGED) {
					if(btnNewElement.isEnabled())
						enableUiElements();
					updateElementCounterLabel();
				}
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
			enableUiElements();
			updateElementCounterLabel();
			collection.addCollectionChangeListener(collectionChangeListener);
		}
	}

	private void updateElementCounterLabel() {
		final int collectionSize = collection.getSize();
		final int currentElementIndex = collection.getCurrentElementIndex();
		lblElementCounter.setText( currentElementIndex >= 0 ? Integer.toString(currentElementIndex + 1) + "/" + Integer.toString(collectionSize): "-/-");
	}

	private void enableUiElements() {
		final int collectionSize = collection.getSize();
		final int currentElementIndex = collection.getCurrentElementIndex();
		comboBoxCollectionNames.setEnabled(true);
		lblElementCounter.setEnabled(true);
		comboBoxCollectionNames.setEditable(collectionSize > 0);
		btnNewElement.setEnabled(true);
		btnDeleteElement.setEnabled(collectionSize > 0);
		btnMoveUp.setEnabled(currentElementIndex > 0);
		btnMoveDown.setEnabled(currentElementIndex >= 0 && currentElementIndex < collectionSize-1);
		btnMove.setEnabled(collectionSize > 1);
	}

	private void disableUiElements() {
		comboBoxCollectionNames.setModel(new DefaultComboBoxModel<Stringifyed<T>>());
		lblElementCounter.setText("-/-: ");
		for(JComponent c : components)
			c.setEnabled(false);
	}

	private JButton createMoveButton(final String elementName) {
		JButton btnMove = TranslatedElementFactory.createButtonWithIcon(elementName + ".move.icon", "collection.move");
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
	private JButton createMoveDownButton(final String elementName) {
		JButton btnMoveDown = TranslatedElementFactory.createButtonWithIcon(elementName + ".down.icon", "collection.down");
		btnMoveDown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UndoableNamedElementCollection.of(collection).moveCurrentElementDown();
			}
		});
		return btnMoveDown;
	}
	private JButton createMoveUpButton(final String elementName) {
		JButton btnMoveUp = TranslatedElementFactory.createButtonWithIcon(elementName + ".up.icon", "collection.up");
		btnMoveUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UndoableNamedElementCollection.of(collection).moveCurrentElementUp();
			}
		});
		return btnMoveUp;
	}
	private JButton createDeleteElementButton(final String elementName) {
		JButton btnDeleteElement = TranslatedElementFactory.createButtonWithIcon(elementName + ".delete.icon", "collection.delete");
		btnDeleteElement.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UndoableNamedElementCollection.of(collection).removeCurrentElement();
			}
		});
		return btnDeleteElement;
	}
	private JButton createNewElementButton(final String elementName) {
		final JButton btnNewElement = TranslatedElementFactory.createButtonWithIcon(elementName + ".new.icon", "collection.new." + elementName);
		btnNewElement.addActionListener(new ActionListener() {
			

			@Override
			public void actionPerformed(ActionEvent e) {
				final String text = TextUtils.getText("collection.new." + elementName);
				UndoableNamedElementCollection.of(collection).add(text + " " + (collection.getSize() + 1));
				final ComboBoxEditor editor = comboBoxCollectionNames.getEditor();
				editor.selectAll();
				editor.getEditorComponent().requestFocusInWindow();
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