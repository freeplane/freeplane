package org.freeplane.features.map;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ComboBoxEditor;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class NumberComboBoxEditor implements ComboBoxEditor{
	final private  List<ActionListener> actionListeners;
	final private JSpinner editor;
	public NumberComboBoxEditor(){
		actionListeners = new LinkedList<ActionListener>();
		editor = new JSpinner();
		editor.addChangeListener(new  ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if(actionListeners.isEmpty()){
					return;
				}
				final ActionEvent actionEvent = new ActionEvent(e.getSource(), 0, null);
				for(ActionListener l : actionListeners){
					l.actionPerformed(actionEvent);
				}
			}
		});
	}
	public void addActionListener(ActionListener l) {
		actionListeners.add(l);
	}

	public Component getEditorComponent() {
		return editor;
	}

	public Object getItem() {
		return editor.getValue().toString();
	}

	public void removeActionListener(ActionListener l) {
		actionListeners.remove(l);
	}

	public void selectAll() {
	}

	public void setItem(Object anObject) {
		if(anObject != null){
			editor.setValue(Integer.valueOf(anObject.toString()));
			return;
		}
		editor.setValue(0);
	}
}
