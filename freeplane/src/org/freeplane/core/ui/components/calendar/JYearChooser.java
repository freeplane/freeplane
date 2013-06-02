/*
 * JYearChooser.java - A bean for choosing a year Copyright (C) 2004 Kai Toedter
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

import java.util.Calendar;

import javax.swing.JFrame;

/**
 * JYearChooser is a bean for choosing a year.
 *
 * @version $LastChangedRevision: 85 $
 * @version $LastChangedDate: 2006-04-28 13:50:52 +0200 (Fr, 28 Apr 2006) $
 */
public class JYearChooser extends JSpinField {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	public static final String YEAR_PROPERTY = "year";

	/**
	 * Creates a JFrame with a JYearChooser inside and can be used for testing.
	 *
	 * @param s
	 *            command line arguments
	 */
	static public void main(final String[] s) {
		final JFrame frame = new JFrame("JYearChooser");
		frame.getContentPane().add(new JYearChooser());
		frame.pack();
		frame.setVisible(true);
	}

	protected JDayChooser dayChooser;
	protected int endYear;
	protected int oldYear;
	protected int startYear;

	/**
	 * Default JCalendar constructor.
	 */
	public JYearChooser() {
		setName("JYearChooser");
		final Calendar calendar = Calendar.getInstance();
		dayChooser = null;
		setMinimum(calendar.getMinimum(Calendar.YEAR));
		setMaximum(calendar.getMaximum(Calendar.YEAR));
		setValue(calendar.get(Calendar.YEAR));
	}

	/**
	 * Returns the endy ear.
	 *
	 * @return the end year
	 */
	public int getEndYear() {
		return getMaximum();
	}

	/**
	 * Returns the start year.
	 *
	 * @return the start year.
	 */
	public int getStartYear() {
		return getMinimum();
	}

	/**
	 * Returns the year.
	 *
	 * @return the year
	 */
	public int getYear() {
		return super.getValue();
	}

	/**
	 * Convenience method set a day chooser that might be updated directly.
	 *
	 * @param dayChooser
	 *            the day chooser
	 */
	public void setDayChooser(final JDayChooser dayChooser) {
		this.dayChooser = dayChooser;
	}

	/**
	 * Sets the end ear.
	 *
	 * @param endYear
	 *            the end year
	 */
	public void setEndYear(final int endYear) {
		setMaximum(endYear);
	}

	/**
	 * Sets the start year.
	 *
	 * @param startYear
	 *            the start year
	 */
	public void setStartYear(final int startYear) {
		setMinimum(startYear);
	}

	/**
	 * Sets the year value.
	 *
	 * @param value
	 *            the year value
	 */
	@Override
	public void setValue(final int value) {
		setYear(value);
	}

	/**
	 * Sets the year. This is a bound property.
	 *
	 * @param y
	 *            the new year
	 * @see #getYear
	 */
	public void setYear(final int y) {
		super.setValue(y, true, false);
		if (dayChooser != null) {
			dayChooser.setYear(value);
		}
		spinner.setValue(new Integer(value));
		firePropertyChange(JYearChooser.YEAR_PROPERTY, oldYear, value);
		oldYear = value;
	}
}
