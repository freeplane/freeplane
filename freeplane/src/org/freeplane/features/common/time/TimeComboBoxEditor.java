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
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ComboBoxEditor;
import javax.swing.JButton;
import javax.swing.JPopupMenu;

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

	List<ActionListener> actionListeners;
	final private JPopupMenu calendarPopupMenu;
	final private JCalendar calenderComponent;
	private Date date;
	final private JButton showEditorBtn;

	TimeComboBoxEditor() {
		showEditorBtn = new JButton();
		showEditorBtn.addActionListener(new ShowCalendarAction());
		calenderComponent = new JCalendar();
		calenderComponent.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				calendarPopupMenu.setVisible(false);
				if (actionListeners.size() == 0) {
					return;
				}
				date = calenderComponent.getDate();
				final ActionEvent actionEvent = new ActionEvent(e.getSource(), 0, null);
				for (final ActionListener l : actionListeners) {
					l.actionPerformed(actionEvent);
				}
			}
		});
		calendarPopupMenu = calenderComponent.createPopupMenu();
		actionListeners = new LinkedList<ActionListener>();
	}

	public void addActionListener(final ActionListener l) {
		actionListeners.add(l);
	}

	public Component getEditorComponent() {
		return showEditorBtn;
	}

	public Object getItem() {
		return new DayDate(date);
	}

	public void removeActionListener(final ActionListener l) {
		actionListeners.remove(l);
	}

	public void selectAll() {
	}

	public void setItem(final Object date) {
		this.date = (Date) date;
		showEditorBtn.setText(date == null ? "" : TimeCondition.format(this.date));
	}
}
