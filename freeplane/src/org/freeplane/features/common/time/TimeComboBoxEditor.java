/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry Polivaev
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
package org.freeplane.features.common.time;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ComboBoxEditor;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import org.freeplane.core.frame.ViewController;
import org.freeplane.core.resources.NamedObject;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.ContainerComboBoxEditor;
import org.freeplane.features.common.format.FormattedDate;
import org.freeplane.features.common.time.swing.JCalendar;

/**
 * @author Dimitry Polivaev
 * Mar 5, 2009
 */
public class TimeComboBoxEditor implements ComboBoxEditor {
	private class ShowCalendarAction implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			calendarPopupMenu.show(showEditorBtn, 0, showEditorBtn.getHeight());
		}
	}

	final private List<ActionListener> actionListeners;
	final private JPopupMenu calendarPopupMenu;
	final private JCalendar calenderComponent;
	private FormattedDate date;
	final private JButton showEditorBtn;

	public TimeComboBoxEditor(boolean timeVisible) {
		showEditorBtn = new JButton();
		showEditorBtn.addActionListener(new ShowCalendarAction());
		calenderComponent = new JCalendar(null, null, true, true, timeVisible);
		calenderComponent.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				calendarPopupMenu.setVisible(false);
			}
		});
		calendarPopupMenu = calenderComponent.createPopupMenu();
		calendarPopupMenu.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			}
			
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				updateDate();
			}
			
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
		});
		actionListeners = new LinkedList<ActionListener>();
	}

	public void addActionListener(final ActionListener l) {
		actionListeners.add(l);
	}

	public Component getEditorComponent() {
		return showEditorBtn;
	}

	public Object getItem() {
		return date;
	}

	public void removeActionListener(final ActionListener l) {
		actionListeners.remove(l);
	}

	public void selectAll() {
	}

	public void setItem(final Object date) {
		if(! (date instanceof FormattedDate))
			return;
		this.date = (FormattedDate) date;
		showEditorBtn.setText(date == null ? "" : date.toString());
	}

	private void updateDate() {
		date = new FormattedDate(calenderComponent.getDate(), 
			calenderComponent.isTimeVisible() ? "yyyy-MM-dd HH:mm" : "yyyy-MM-dd");
	    if (actionListeners.size() == 0) {
	    	return;
	    }
	    final ActionEvent actionEvent = new ActionEvent(this, 0, null);
	    for (final ActionListener l : actionListeners) {
	    	l.actionPerformed(actionEvent);
	    }
    }

	public void setItem() {
	    updateDate();
    }
	
	public static ComboBoxEditor getTextDateTimeEditor() {
	    final ContainerComboBoxEditor editor = new ContainerComboBoxEditor();
		final NamedObject keyText = new NamedObject("text", "1Ab");
		final BasicComboBoxEditor textEditor = new BasicComboBoxEditor(){
			private Object oldItem;

			@Override
            public void setItem(Object object) {
				oldItem = object;
				if(object instanceof FormattedDate)
					super.setItem("");
				else
					super.setItem(object);
            }

			@Override
            public Object getItem() {
	            final Object item = super.getItem();
				final Object oldItem = this.oldItem;
				this.oldItem = null;
	            if(item != null && oldItem != null && item.toString().equals(oldItem.toString()))
	            	return oldItem;
	            if(ResourceController.getResourceController().getBooleanProperty("parse_data") 
	            		&& item instanceof String){
	            	final FormattedDate date = FormattedDate.toDateISO(((String)item).trim());
	            	if(date != null)
	            		return date;
	            }
				return item;
            }
			
		};
		editor.put(keyText, textEditor);
		
		final NamedObject keyDate = new NamedObject("date", ""); 
		keyDate.setIcon(ViewController.dateIcon);
		final TimeComboBoxEditor dateComboBoxEditor = new TimeComboBoxEditor(false){
			@Override
            public void setItem(Object object) {
				if(object instanceof FormattedDate && !((FormattedDate)object).containsTime())
					super.setItem(object);
				else
					super.setItem(null);
            }
		};
		
		dateComboBoxEditor.setItem();
		editor.put(keyDate, dateComboBoxEditor);

		final NamedObject keyDateTime = new NamedObject("date_time", ""); 
		keyDateTime.setIcon(ViewController.dateTimeIcon);
		final TimeComboBoxEditor dateTimeComboBoxEditor = new TimeComboBoxEditor(true){
			@Override
            public void setItem(Object object) {
				if(object instanceof FormattedDate && ((FormattedDate)object).containsTime())
					super.setItem(object);
				else
					super.setItem(null);
            }
		};
		dateTimeComboBoxEditor.setItem();
		editor.put(keyDateTime, dateTimeComboBoxEditor);

		return editor;
    }

	
}
