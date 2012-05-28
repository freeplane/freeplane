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
package org.freeplane.features.time;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.swing.ComboBoxEditor;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.freeplane.core.ui.components.calendar.JCalendar;
import org.freeplane.features.format.FormattedDate;
import org.freeplane.features.format.IFormattedObject;

/**
 * @author Dimitry Polivaev
 * Mar 5, 2009
 */
public class TimeComboBoxEditor implements ComboBoxEditor {
	private class ShowCalendarAction implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			if(showEditorBtn.isShowing())
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
		calenderComponent = new JCalendar(new Date(), Locale.getDefault(), true, true, timeVisible);
		calenderComponent.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				calendarPopupMenu.setVisible(false);
			}
		});
		calendarPopupMenu = calenderComponent.createPopupMenu();
		calendarPopupMenu.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			    calenderComponent.setDate(date);
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
		final FormattedDate newDate = new FormattedDate(calenderComponent.getDate(), 
			calenderComponent.isTimeVisible() ? "yyyy-MM-dd HH:mm" : "yyyy-MM-dd");
        final String type = newDate.containsTime() ? IFormattedObject.TYPE_DATETIME : IFormattedObject.TYPE_DATE;
        date = FormattedDate.createDefaultFormattedDate(newDate.getTime(), type);
		
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

	
}
