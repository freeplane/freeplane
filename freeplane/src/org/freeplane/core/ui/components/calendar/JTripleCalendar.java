/*
 * Freeplane - A Program for creating and viewing MindmapsCopyright (C) 2007
 * Christian Foltin <christianfoltin@users.sourceforge.net>See COPYING for
 * DetailsThis program is free software; you can redistribute it and/ormodify it
 * under the terms of the GNU General Public Licenseas published by the Free
 * Software Foundation; either version 2of the License, or (at your option) any
 * later version.This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty ofMERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See theGNU General Public License for
 * more details.You should have received a copy of the GNU General Public
 * Licensealong with this program; if not, write to the Free SoftwareFoundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
/* $Id: JTripleCalendar.java,v 1.1.2.2 2007/02/25 21:12:50 christianfoltin Exp $ */
package org.freeplane.core.ui.components.calendar;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;


/** */
public class JTripleCalendar extends JPanel implements PropertyChangeListener {
	private static class JInfoPanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		final private JDayChooser dayChooser;
		final private JMonthChooser monthChooser;
		final private JYearChooser yearChooser;

		public JInfoPanel() {
			this.setLayout(new BorderLayout());
			final JPanel monthYearPanel = new JPanel();
			monthYearPanel.setLayout(new BorderLayout());
			monthChooser = new JMonthChooser();
			monthChooser.setEnabled(false);
			yearChooser = new JYearChooser();
			yearChooser.setEnabled(false);
			monthYearPanel.add(monthChooser, BorderLayout.WEST);
			monthYearPanel.add(yearChooser, BorderLayout.CENTER);
			dayChooser = new JDayChooser(true) {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				protected void init() {
					super.init();
					selectedColor = oldDayBackgroundColor;
				}
			};
			dayChooser.setEnabled(false);
			this.add(monthYearPanel, BorderLayout.NORTH);
			this.add(dayChooser, BorderLayout.CENTER);
		}

		public void setDate(final Calendar calendar) {
			final int year = calendar.get(Calendar.YEAR);
			final int month = calendar.get(Calendar.MONTH);
			monthChooser.setMonth(month);
			yearChooser.setYear(year);
			dayChooser.setYear(year);
			dayChooser.setMonth(month);
			dayChooser.setEnabled(false);
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static void main(final String[] args) {
		final JFrame frame = new JFrame("JTripleCalendar");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		final JTripleCalendar jcalendar = new JTripleCalendar();
		frame.getContentPane().add(jcalendar);
		frame.pack();
		frame.setVisible(true);
	}

	final private JCalendar calendarWidget;
	final private JInfoPanel leftPanel;
	final private JInfoPanel rightPanel;

	public JTripleCalendar() {
		this.setName("JTripleCalendar");
		final GridLayout gridLayout = new GridLayout(1, 3);
		gridLayout.setVgap(10);
		setLayout(gridLayout);
		leftPanel = createInfoPanel();
		rightPanel = createInfoPanel();
		add(leftPanel);
		calendarWidget = new JCalendar();
		calendarWidget.addPropertyChangeListener(this);
		add(calendarWidget);
		add(rightPanel);
	}

	private JInfoPanel createInfoPanel() {
		final JInfoPanel panel = new JInfoPanel();
		return panel;
	}

	public JCalendar getCalendar() {
		return calendarWidget;
	}

	public void propertyChange(final PropertyChangeEvent evt) {
		final Calendar gregorianCalendar = (Calendar) calendarWidget.getCalendar().clone();
		gregorianCalendar.add(Calendar.MONTH, -1);
		leftPanel.setDate(gregorianCalendar);
		gregorianCalendar.add(Calendar.MONTH, 2);
		rightPanel.setDate(gregorianCalendar);
	}

	public void setCalendar(final Calendar c) {
		calendarWidget.setCalendar(c);
	}

	public void setDate(final Date date) {
		calendarWidget.setDate(date);
	}
}
