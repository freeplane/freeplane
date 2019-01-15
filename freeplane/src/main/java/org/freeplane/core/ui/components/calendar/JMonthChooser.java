/*
 * JMonthChooser.java - A bean for choosing a month Copyright (C) 2004 Kai
 * Toedter kai@toedter.com www.toedter.com This program is free software; you
 * can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version. This program
 * is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.freeplane.core.ui.components.calendar;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.freeplane.core.ui.components.JComboBoxWithBorder;

/**
 * JMonthChooser is a bean for choosing a month.
 *
 * @author Kai Toedter
 * @version $LastChangedRevision: 100 $
 * @version $LastChangedDate: 2006-06-04 14:36:06 +0200 (So, 04 Jun 2006) $
 */
public class JMonthChooser extends JPanel implements ItemListener, ChangeListener {
	public static final String MONTH_PROPERTY = "month";
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a JFrame with a JMonthChooser inside and can be used for testing.
	 *
	 * @param s
	 *            The command line arguments
	 */
	public static void main(final String[] s) {
		final JFrame frame = new JFrame("MonthChooser");
		frame.getContentPane().add(new JMonthChooser());
		frame.pack();
		frame.setVisible(true);
	}

	final private JComboBox comboBox;
	private JDayChooser dayChooser;
	/** true, if the month chooser has a spinner component */
	protected boolean hasSpinner;
	final private boolean initialized;
	private Locale locale;
	private boolean localInitialize;
	private int month;
	private int oldSpinnerValue = 0;
	private JSpinner spinner;
	private JYearChooser yearChooser;

	/**
	 * Default JMonthChooser constructor.
	 */
	public JMonthChooser() {
		this(true);
	}

	/**
	 * JMonthChooser constructor with month spinner parameter.
	 *
	 * @param hasSpinner
	 *            true, if the month chooser should have a spinner component
	 */
	public JMonthChooser(final boolean hasSpinner) {
		super();
		setName("JMonthChooser");
		this.hasSpinner = hasSpinner;
		setLayout(new BorderLayout());
		comboBox = new JComboBoxWithBorder();
		comboBox.addItemListener(this);
		locale = Locale.getDefault();
		initNames();
		if (hasSpinner) {
			spinner = new JSpinner() {
				/**
				 *
				 */
				private static final long serialVersionUID = 1L;
				final private JTextField textField = new JTextField();

				@Override
				public Dimension getPreferredSize() {
					final Dimension size = super.getPreferredSize();
					return new Dimension(size.width, textField.getPreferredSize().height);
				}
			};
			spinner.addChangeListener(this);
			spinner.setEditor(comboBox);
			comboBox.setBorder(new EmptyBorder(0, 0, 0, 0));
			updateUI();
			add(spinner, BorderLayout.WEST);
		}
		else {
			add(comboBox, BorderLayout.WEST);
		}
		initialized = true;
		setMonth(Calendar.getInstance().get(Calendar.MONTH));
	}

	/**
	 * Returns the month chooser's comboBox text area (which allow the focus to
	 * be set to it).
	 *
	 * @return the combo box
	 */
	public Component getComboBox() {
		return comboBox;
	}

	/**
	 * Returns the locale.
	 *
	 * @return the locale value
	 * @see #setLocale
	 */
	@Override
	public Locale getLocale() {
		return locale;
	}

	/**
	 * Returns the month.
	 *
	 * @return the month value
	 */
	public int getMonth() {
		return month;
	}

	/**
	 * Returns the month chooser's comboBox bar (which allow the focus to be set
	 * to it).
	 *
	 * @return Component the spinner or null, if the month chooser has no
	 *         spinner
	 */
	public Component getSpinner() {
		return spinner;
	}

	/**
	 * Returns the type of spinner the month chooser is using.
	 *
	 * @return true, if the month chooser has a spinner
	 */
	public boolean hasSpinner() {
		return hasSpinner;
	}

	/**
	 * Initializes the locale specific month names.
	 */
	public void initNames() {
		localInitialize = true;
		final DateFormatSymbols dateFormatSymbols = new DateFormatSymbols(locale);
		final String[] monthNames = dateFormatSymbols.getMonths();
		if (comboBox.getItemCount() == 12) {
			comboBox.removeAllItems();
		}
		for (int i = 0; i < 12; i++) {
			comboBox.addItem(monthNames[i]);
		}
		localInitialize = false;
		comboBox.setSelectedIndex(month);
	}

	/**
	 * The ItemListener for the months.
	 *
	 * @param e
	 *            the item event
	 */
	public void itemStateChanged(final ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			final int index = comboBox.getSelectedIndex();
			if ((index >= 0) && (index != month)) {
				setMonth(index, false);
			}
		}
	}

	/**
	 * Convenience method set a day chooser.
	 *
	 * @param dayChooser
	 *            the day chooser
	 */
	public void setDayChooser(final JDayChooser dayChooser) {
		this.dayChooser = dayChooser;
	}

	/**
	 * Enable or disable the JMonthChooser.
	 *
	 * @param enabled
	 *            the new enabled value
	 */
	@Override
	public void setEnabled(final boolean enabled) {
		super.setEnabled(enabled);
		comboBox.setEnabled(enabled);
		if (spinner != null) {
			spinner.setEnabled(enabled);
		}
	}

	/**
	 * Sets the font for this component.
	 *
	 * @param font
	 *            the desired <code>Font</code> for this component
	 */
	@Override
	public void setFont(final Font font) {
		if (comboBox != null) {
			comboBox.setFont(font);
		}
		super.setFont(font);
	}

	/**
	 * Set the locale and initializes the new month names.
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
			locale = l;
			initNames();
		}
	}

	/**
	 * Sets the month. This is a bound property. Valuse are valid between 0
	 * (January) and 11 (December). A value < 0 will be treated as 0, a value >
	 * 11 will be treated as 11.
	 *
	 * @param newMonth
	 *            the new month value
	 * @see #getMonth
	 */
	public void setMonth(final int newMonth) {
		if (newMonth < 0 || newMonth == Integer.MIN_VALUE) {
			setMonth(0, true);
		}
		else if (newMonth > 11) {
			setMonth(11, true);
		}
		else {
			setMonth(newMonth, true);
		}
	}

	/**
	 * Sets the month attribute of the JMonthChooser object. Fires a property
	 * change "month".
	 *
	 * @param newMonth
	 *            the new month value
	 * @param select
	 *            true, if the month should be selected in the combo box.
	 */
	private void setMonth(final int newMonth, final boolean select) {
		if (!initialized || localInitialize) {
			return;
		}
		final int oldMonth = month;
		month = newMonth;
		if (select) {
			comboBox.setSelectedIndex(month);
		}
		if (dayChooser != null) {
			dayChooser.setMonth(month);
		}
		firePropertyChange(JMonthChooser.MONTH_PROPERTY, oldMonth, month);
	}

	/**
	 * Convenience method set a year chooser. If set, the spin for the month
	 * buttons will spin the year as well
	 *
	 * @param yearChooser
	 *            the new yearChooser value
	 */
	public void setYearChooser(final JYearChooser yearChooser) {
		this.yearChooser = yearChooser;
	}

	/**
	 * Is invoked if the state of the spinner changes.
	 *
	 * @param e
	 *            the change event.
	 */
	public void stateChanged(final ChangeEvent e) {
		final SpinnerNumberModel model = (SpinnerNumberModel) ((JSpinner) e.getSource()).getModel();
		final int value = model.getNumber().intValue();
		final boolean increase = (value > oldSpinnerValue) ? true : false;
		oldSpinnerValue = value;
		int month = getMonth();
		if (increase) {
			month += 1;
			if (month == 12) {
				month = 0;
				if (yearChooser != null) {
					int year = yearChooser.getYear();
					year += 1;
					yearChooser.setYear(year);
				}
			}
		}
		else {
			month -= 1;
			if (month == -1) {
				month = 11;
				if (yearChooser != null) {
					int year = yearChooser.getYear();
					year -= 1;
					yearChooser.setYear(year);
				}
			}
		}
		setMonth(month);
	}

	/**
	 * Updates the UI.
	 *
	 * @see javax.swing.JPanel#updateUI()
	 */
	@Override
	public void updateUI() {
		final JSpinner testSpinner = new JSpinner();
		if (spinner != null) {
			if ("Windows".equals(UIManager.getLookAndFeel().getID())) {
				spinner.setBorder(testSpinner.getBorder());
			}
			else {
				spinner.setBorder(new EmptyBorder(0, 0, 0, 0));
			}
		}
	}
}
