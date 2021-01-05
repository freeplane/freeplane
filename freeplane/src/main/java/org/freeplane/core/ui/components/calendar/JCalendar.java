/*
 * JCalendar.java - A bean for choosing a date Copyright (C) 2004 Kai Toedter
 * kai@toedter.com www.toedter.com This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version. This program is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.freeplane.core.ui.components.calendar;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

/**
 * JCalendar is a bean for entering a date by choosing the year, month and day.
 *
 * @author Kai Toedter
 * @version $LastChangedRevision: 95 $
 * @version $LastChangedDate: 2006-05-05 18:43:15 +0200 (Fr, 05 Mai 2006) $
 */
public class JCalendar extends JPanel implements PropertyChangeListener, MouseListener {
	private final class JCalendarPopupMenu extends JPopupMenu {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void menuSelectionChanged(boolean isIncluded) {
			if (!isIncluded) {
				AWTEvent currentEvent = EventQueue.getCurrentEvent();
				if(currentEvent != null) {
				    final Object source = currentEvent.getSource();
				    if (source instanceof Component) {
				        final Component c = (Component) source;
				        isIncluded = SwingUtilities.isDescendingFrom(c, this);
				    }
				}
			}
			super.menuSelectionChanged(isIncluded);
		}
	}

	public static final String DATE_PROPERTY = "date";
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a JFrame with a JCalendar inside and can be used for testing.
	 *
	 * @param s
	 *            The command line arguments
	 */
	public static void main(final String[] s) {
		final JFrame frame = new JFrame("JCalendar");
		final JCalendar jcalendar = new JCalendar();
		frame.getContentPane().add(jcalendar);
		frame.pack();
		frame.setVisible(true);
	}

	private Calendar calendar;
	private JPopupMenu calendarPopupMenu;
	final private JTimeChooser timeChooser;
	/** the day chooser */
	final private JDayChooser dayChooser;
	private boolean initialized = false;
	/** the locale */
	private Locale locale;
	/** the month chooser */
	final private JMonthChooser monthChooser;
	final private JPanel monthYearPanel;
	/** the year chhoser */
	final private JYearChooser yearChooser;

	/**
	 * Default JCalendar constructor.
	 */
	public JCalendar() {
		this(null, null, true, true, true);
	}

	/**
	 * JCalendar constructor specifying the month spinner type.
	 *
	 * @param monthSpinner
	 *            false, if no month spinner should be used
	 */
	public JCalendar(final boolean monthSpinner) {
		this(null, null, monthSpinner, true, false);
	}

	/**
	 * JCalendar constructor which allows the initial calendar to be set.
	 *
	 * @param calendar
	 *            the calendar
	 */
	public JCalendar(final Calendar calendar) {
		this(null, null, true, true, false);
		setCalendar(calendar);
	}

	/**
	 * JCalendar constructor which allows the initial date to be set.
	 *
	 * @param date
	 *            the date
	 */
	public JCalendar(final Date date) {
		this(date, null, true, true, false);
	}

	/**
	 * JCalendar constructor specifying both the initial date and the month
	 * spinner type.
	 *
	 * @param date
	 *            the date
	 * @param monthSpinner
	 *            false, if no month spinner should be used
	 */
	public JCalendar(final Date date, final boolean monthSpinner) {
		this(date, null, monthSpinner, true, false);
	}

	/**
	 * JCalendar constructor specifying both the initial date and locale.
	 *
	 * @param date
	 *            the date
	 * @param locale
	 *            the new locale
	 */
	public JCalendar(final Date date, final Locale locale) {
		this(date, locale, true, true, false);
	}

	/**
	 * JCalendar constructor with month spinner parameter.
	 *
	 * @param date
	 *            the date
	 * @param locale
	 *            the locale
	 * @param monthSpinner
	 *            false, if no month spinner should be used
	 * @param weekOfYearVisible
	 *            true, if weeks of year shall be visible
	 * @param timeVisible
	 *            true, if hours and minutes shall be visible
	 */
	public JCalendar(final Date date, final Locale locale, final boolean monthSpinner, final boolean weekOfYearVisible, final boolean timeVisible) {
		setName("JCalendar");
		this.locale = locale;
		if (locale == null) {
			this.locale = Locale.getDefault();
		}
		calendar = Calendar.getInstance();
		setLayout(new BorderLayout());
		monthYearPanel = new JPanel();
		monthYearPanel.setLayout(new BorderLayout());
		monthChooser = new JMonthChooser(monthSpinner);
		yearChooser = new JYearChooser();
		monthChooser.setYearChooser(yearChooser);
		monthYearPanel.add(monthChooser, BorderLayout.WEST);
		monthYearPanel.add(yearChooser, BorderLayout.CENTER);
		monthYearPanel.setBorder(BorderFactory.createEmptyBorder());
		dayChooser = new JDayChooser(weekOfYearVisible);
		dayChooser.addPropertyChangeListener(this);
		dayChooser.addMouseListener(this);
		monthChooser.setDayChooser(dayChooser);
		monthChooser.addPropertyChangeListener(this);
		yearChooser.setDayChooser(dayChooser);
		yearChooser.addPropertyChangeListener(this);
		dayChooser.setYearChooser(yearChooser);
		dayChooser.setMonthChooser(monthChooser);
		add(monthYearPanel, BorderLayout.NORTH);
		add(dayChooser, BorderLayout.CENTER);
		if(timeVisible){
			timeChooser = new JTimeChooser();
			add(timeChooser, BorderLayout.SOUTH);
		}
		else{
			timeChooser = null;
		}
		if (date != null) {
			calendar.setTime(date);
		}
		initialized = true;
		setCalendar(calendar);
	}

	/**
	 * JCalendar constructor allowing the initial locale to be set.
	 *
	 * @param locale
	 *            the new locale
	 */
	public JCalendar(final Locale locale) {
		this(null, locale, true, true, false);
	}

	/**
	 * JCalendar constructor specifying both the locale and the month spinner.
	 *
	 * @param locale
	 *            the locale
	 * @param monthSpinner
	 *            false, if no month spinner should be used
	 */
	public JCalendar(final Locale locale, final boolean monthSpinner) {
		this(null, locale, monthSpinner, true, false);
	}

	public JPopupMenu createPopupMenu() {
		if (calendarPopupMenu != null) {
			return calendarPopupMenu;
		}
		calendarPopupMenu = new JCalendarPopupMenu();
		calendarPopupMenu.add(this);
		return calendarPopupMenu;
	}

	/**
	 * Returns the calendar property.
	 *
	 * @return the value of the calendar property.
	 */
	public Calendar getCalendar() {
		return calendar;
	}

	/**
	 * Returns a Date object.
	 *
	 * @return a date object constructed from the calendar property.
	 */
	public Date getDate() {
		return new Date(calendar.getTimeInMillis());
	}

	/**
	 * Gets the dayChooser attribute of the JCalendar object
	 *
	 * @return the dayChooser value
	 */
	public JDayChooser getDayChooser() {
		return dayChooser;
	}

	/**
	 * Returns the color of the decoration (day names and weeks).
	 *
	 * @return the color of the decoration (day names and weeks).
	 */
	public Color getDecorationBackgroundColor() {
		return dayChooser.getDecorationBackgroundColor();
	}

	/**
	 * Returns the locale.
	 *
	 * @return the value of the locale property.
	 * @see #setLocale
	 */
	@Override
	public Locale getLocale() {
		return locale;
	}

	/**
	 * Gets the maximum number of characters of a day name or 0. If 0 is
	 * returned, dateFormatSymbols.getShortWeekdays() will be used.
	 *
	 * @return the maximum number of characters of a day name or 0.
	 */
	public int getMaxDayCharacters() {
		return dayChooser.getMaxDayCharacters();
	}

	/**
	 * Gets the minimum selectable date.
	 *
	 * @return the minimum selectable date
	 */
	public Date getMaxSelectableDate() {
		return dayChooser.getMaxSelectableDate();
	}

	/**
	 * Gets the maximum selectable date.
	 *
	 * @return the maximum selectable date
	 */
	public Date getMinSelectableDate() {
		return dayChooser.getMinSelectableDate();
	}

	/**
	 * Gets the monthChooser attribute of the JCalendar object
	 *
	 * @return the monthChooser value
	 */
	public JMonthChooser getMonthChooser() {
		return monthChooser;
	}

	/**
	 * Returns the Sunday foreground.
	 *
	 * @return Color the Sunday foreground.
	 */
	public Color getSundayForeground() {
		return dayChooser.getSundayForeground();
	}

	/**
	 * Returns the weekday foreground.
	 *
	 * @return Color the weekday foreground.
	 */
	public Color getWeekdayForeground() {
		return dayChooser.getWeekdayForeground();
	}

	/**
	 * Gets the yearChooser attribute of the JCalendar object
	 *
	 * @return the yearChooser value
	 */
	public JYearChooser getYearChooser() {
		return yearChooser;
	}

	/**
	 * Gets the visibility of the decoration background.
	 *
	 * @return true, if the decoration background is visible.
	 */
	public boolean isDecorationBackgroundVisible() {
		return dayChooser.isDecorationBackgroundVisible();
	}

	/**
	 * Gets the visibility of the decoration border.
	 *
	 * @return true, if the decoration border is visible.
	 */
	public boolean isDecorationBordersVisible() {
		return dayChooser.isDecorationBordersVisible();
	}

	/**
	 * Indicates if the weeks of year are visible..
	 *
	 * @return boolean true, if weeks of year are visible
	 */
	public boolean isWeekOfYearVisible() {
		return dayChooser.isWeekOfYearVisible();
	}

	public void mouseClicked(final MouseEvent e) {
		processMouseEvent(e);
	}

	public void mouseEntered(final MouseEvent e) {
		processMouseEvent(e);
	}

	public void mouseExited(final MouseEvent e) {
		processMouseEvent(e);
	}

	public void mousePressed(final MouseEvent e) {
		processMouseEvent(e);
	}

	public void mouseReleased(final MouseEvent e) {
		processMouseEvent(e);
	}

	/**
	 * JCalendar is a PropertyChangeListener, for its day, month and year
	 * chooser.
	 *
	 * @param evt
	 *            the property change event
	 */
	public void propertyChange(final PropertyChangeEvent evt) {
		if (calendar != null) {
			if (evt.getPropertyName().equals(JDayChooser.DAY_PROPERTY)) {
				calendar.set(Calendar.DAY_OF_MONTH, ((Integer) evt.getNewValue()).intValue());
			}
			else if (evt.getPropertyName().equals(JMonthChooser.MONTH_PROPERTY)) {
				calendar.set(Calendar.MONTH, ((Integer) evt.getNewValue()).intValue());
			}
			else if (evt.getPropertyName().equals(JYearChooser.YEAR_PROPERTY)) {
				calendar.set(Calendar.YEAR, ((Integer) evt.getNewValue()).intValue());
			}
			else if (evt.getPropertyName().equals(JCalendar.DATE_PROPERTY)) {
				calendar.setTime((Date) evt.getNewValue());
			}
		}
		firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
	}

	/**
	 * Sets the background color.
	 *
	 * @param bg
	 *            the new background
	 */
	@Override
	public void setBackground(final Color bg) {
		super.setBackground(bg);
		if (dayChooser != null) {
			dayChooser.setBackground(bg);
		}
	}

	/**
	 * Sets the calendar property. This is a bound property.
	 *
	 * @param c
	 *            the new calendar
	 * @throws NullPointerException
	 *             - if c is null;
	 * @see #getCalendar
	 */
	public void setCalendar(final Calendar c) {
		if (c == null) {
		setDate(null);
        }
        final Calendar oldCalendar = calendar;
        calendar = c;
        yearChooser.setYear(c.get(Calendar.YEAR));
        monthChooser.setMonth(c.get(Calendar.MONTH));
        dayChooser.setDay(c.get(Calendar.DATE));
        if(timeChooser != null)
		timeChooser.setCalendar(calendar);
        firePropertyChange("calendar", oldCalendar, calendar);
	}

	/**
	 * Sets the date. Fires the property change "date".
	 *
	 * @param date
	 *            the new date.
	 * @throws NullPointerException
	 *             - if tha date is null
	 */
	public void setDate(final Date date) {
		final Date oldDate = calendar.getTime();
		calendar.setTime(date);
		final int year = calendar.get(Calendar.YEAR);
		final int month = calendar.get(Calendar.MONTH);
		final int day = calendar.get(Calendar.DAY_OF_MONTH);
		yearChooser.setYear(year);
		monthChooser.setMonth(month);
		dayChooser.setCalendar(calendar);
		dayChooser.setDay(day);
		firePropertyChange(JCalendar.DATE_PROPERTY, oldDate, date);
	}

	/**
	 * Sets the background of days and weeks of year buttons.
	 *
	 * @param decorationBackgroundColor
	 *            the background color
	 */
	public void setDecorationBackgroundColor(final Color decorationBackgroundColor) {
		dayChooser.setDecorationBackgroundColor(decorationBackgroundColor);
	}

	/**
	 * Sets the decoration background visible.
	 *
	 * @param decorationBackgroundVisible
	 *            true, if the decoration background should be visible.
	 */
	public void setDecorationBackgroundVisible(final boolean decorationBackgroundVisible) {
		dayChooser.setDecorationBackgroundVisible(decorationBackgroundVisible);
		setLocale(locale);
	};

	/**
	 * Sets the decoration borders visible.
	 *
	 * @param decorationBordersVisible
	 *            true, if the decoration borders should be visible.
	 */
	public void setDecorationBordersVisible(final boolean decorationBordersVisible) {
		dayChooser.setDecorationBordersVisible(decorationBordersVisible);
		setLocale(locale);
	}

	/**
	 * Enable or disable the JCalendar.
	 *
	 * @param enabled
	 *            the new enabled value
	 */
	@Override
	public void setEnabled(final boolean enabled) {
		super.setEnabled(enabled);
		if (dayChooser != null) {
			dayChooser.setEnabled(enabled);
			monthChooser.setEnabled(enabled);
			yearChooser.setEnabled(enabled);
		}
	}

	/**
	 * Sets the font property.
	 *
	 * @param font
	 *            the new font
	 */
	@Override
	public void setFont(final Font font) {
		super.setFont(font);
		if (dayChooser != null) {
			dayChooser.setFont(font);
			monthChooser.setFont(font);
			yearChooser.setFont(font);
		}
	}

	/**
	 * Sets the foreground color.
	 *
	 * @param fg
	 *            the new foreground
	 */
	@Override
	public void setForeground(final Color fg) {
		super.setForeground(fg);
		if (dayChooser != null) {
			dayChooser.setForeground(fg);
			monthChooser.setForeground(fg);
			yearChooser.setForeground(fg);
		}
	}

	/**
	 * Sets the locale property. This is a bound property.
	 *
	 * @param l
	 *            the new locale value
	 * @see #getLocale
	 */
	@Override
	public void setLocale(final Locale l) {
		if (!initialized) {
			super.setLocale(l);
		}
		else {
			final Locale oldLocale = locale;
			locale = l;
			dayChooser.setLocale(locale);
			monthChooser.setLocale(locale);
			firePropertyChange("locale", oldLocale, locale);
		}
	}

	/**
	 * Sets the maximum number of characters per day in the day bar. Valid
	 * values are 0-4. If set to 0, dateFormatSymbols.getShortWeekdays() will be
	 * used, otherwise theses strings will be reduced to the maximum number of
	 * characters.
	 *
	 * @param maxDayCharacters
	 *            the maximum number of characters of a day name.
	 */
	public void setMaxDayCharacters(final int maxDayCharacters) {
		dayChooser.setMaxDayCharacters(maxDayCharacters);
	}

	/**
	 * Sets the maximum selectable date.
	 *
	 * @param max
	 *            maximum selectable date
	 */
	public void setMaxSelectableDate(final Date max) {
		dayChooser.setMaxSelectableDate(max);
	}

	/**
	 * Sets the minimum selectable date.
	 *
	 * @param min
	 *            minimum selectable date
	 */
	public void setMinSelectableDate(final Date min) {
		dayChooser.setMinSelectableDate(min);
	}

	/**
	 * Sets a valid date range for selectable dates. If max is before min, the
	 * default range with no limitation is set.
	 *
	 * @param min
	 *            the minimum selectable date or null (then the minimum date is
	 *            set to 01\01\0001)
	 * @param max
	 *            the maximum selectable date or null (then the maximum date is
	 *            set to 01\01\9999)
	 */
	public void setSelectableDateRange(final Date min, final Date max) {
		dayChooser.setSelectableDateRange(min, max);
	}

	/**
	 * Sets the Sunday foreground.
	 *
	 * @param sundayForeground
	 *            the sundayForeground to set
	 */
	public void setSundayForeground(final Color sundayForeground) {
		dayChooser.setSundayForeground(sundayForeground);
	}

	/**
	 * Sets the weekday foreground.
	 *
	 * @param weekdayForeground
	 *            the weekdayForeground to set
	 */
	public void setWeekdayForeground(final Color weekdayForeground) {
		dayChooser.setWeekdayForeground(weekdayForeground);
	}

	/**
	 * Sets the week of year visible.
	 *
	 * @param weekOfYearVisible
	 *            true, if weeks of year shall be visible
	 */
	public void setWeekOfYearVisible(final boolean weekOfYearVisible) {
		dayChooser.setWeekOfYearVisible(weekOfYearVisible);
		setLocale(locale);
	}

	public boolean isTimeVisible(){
		return timeChooser != null;
	}
}
