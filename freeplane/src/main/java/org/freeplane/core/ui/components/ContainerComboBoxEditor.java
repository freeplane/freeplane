/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2011 dimitry
 *
 *  This file author is dimitry
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
package org.freeplane.core.ui.components;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.freeplane.core.resources.TranslatedObject;

/**
 * @author Dimitry Polivaev
 * Mar 12, 2011
 */
public class ContainerComboBoxEditor implements ComboBoxEditor {
	final private Map<TranslatedObject, ComboBoxEditor> editors;
	private ComboBoxEditor editor;
	final private JComboBox editorSelector;
	final private JPanel editorPanel;
	private Box editorComponent;

	final private List<ActionListener> actionListeners;

	public ContainerComboBoxEditor() {
		editors = new HashMap<TranslatedObject, ComboBoxEditor>();
		editorComponent = Box.createHorizontalBox();
		editorSelector = new JComboBoxWithBorder();
		editorSelector.setEditable(false);
		editorSelector.setRenderer(TranslatedObject.getIconRenderer());
		editorSelector.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final TranslatedObject key = (TranslatedObject) editorSelector.getSelectedItem();
				editor = editors.get(key);
				final CardLayout layout = (CardLayout) editorPanel.getLayout();				
				layout.show(editorPanel,  key.getObject().toString());
				editor.getEditorComponent().requestFocusInWindow();
			    final ActionEvent actionEvent = new ActionEvent(editor, 0, null);
			    for (final ActionListener l : actionListeners) {
			    	l.actionPerformed(actionEvent);
			    }
			}
		});
		editorComponent.add(editorSelector);
		editorPanel = new JPanel(new CardLayout(0, 0));
		editorComponent.add(editorPanel);
		actionListeners = new LinkedList<ActionListener>();
    }
	
	public boolean put(TranslatedObject key, ComboBoxEditor editor){
		final ComboBoxEditor oldEditor = editors.put(key, editor);
		if(oldEditor != null){
			editors.put(key, oldEditor);
			return false;
		}
		final DefaultComboBoxModel model = (DefaultComboBoxModel) editorSelector.getModel();
		model.addElement(key);
		if(this.editor == null){
			this.editor = editor;
		}
		editorPanel.add(editor.getEditorComponent(), key.getObject().toString());
		return true;
	}

	public Component getEditorComponent() {
		return editorComponent;
	}

	public void setItem(Object anObject) {
		if(anObject == null){
			setItem("");
			return;
		}
		for(Entry<TranslatedObject, ComboBoxEditor> editorEntry: editors.entrySet()){
			final ComboBoxEditor editor = editorEntry.getValue();
			editor.setItem(anObject);
			final Object item = editor.getItem();
			TranslatedObject key = editorEntry.getKey();
			if(anObject.equals(item) && ! key.equals(editorSelector.getSelectedItem())){
				editorSelector.setSelectedItem(key);
				return;
			}
		}
	}

	public Object getItem() {
		return editor.getItem();
	}

	public void selectAll() {
		editor.selectAll();
	}

	public void addActionListener(ActionListener l) {
		actionListeners.add(l);
		for(ComboBoxEditor e : editors.values())
			e.addActionListener(l);
	}

	public void removeActionListener(ActionListener l) {
		actionListeners.remove(l);
		for(ComboBoxEditor e : editors.values())
			e.removeActionListener(l);
	}
}
