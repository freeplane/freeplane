/*
 * JDayChooser.java - A bean for choosing a day Copyright (C) 2004 Kai Toedter
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicButtonUI;

import org.freeplane.core.ui.components.UITools;

/**
 * JDayChooser is a bean for choosing a day.
 *
 * @author Kai Toedter
 * @version $LastChangedRevision: 104 $
 * @version $LastChangedDate: 2006-06-04 15:20:45 +0200 (So, 04 Jun 2006) $
 */
public class JDayChooser extends JPanel implements ActionListener, KeyListener, FocusListener, MouseListener {
	static class JDayButton extends JButton{
		private static final long serialVersionUID = 1L;

		@Override
        public void updateUI() {
			setMargin(new Insets(0, 0, 0, 0));
	        super.updateUI();
	        final Insets insets = getInsets();
	        if(insets.left + insets.right > 8){
			setUI(BasicButtonUI.createUI(this));
			setBorder(BorderFactory.createLineBorder(Color.GRAY));
	        }
        }

	}
	class DecoratorButton extends JDayButton {
		private static final long serialVersionUID = 1L;

		public DecoratorButton() {
			setBackground(decorationBackgroundColor);
			setContentAreaFilled(decorationBackgroundVisible);
			setBorderPainted(decorationBordersVisible);
			setFocusable(false);
		}

		@Override
		public void paint(final Graphics g) {
			if ("Windows".equals(UIManager.getLookAndFeel().getID())) {
				if (decorationBackgroundVisible) {
					g.setColor(decorationBackgroundColor);
				}
				else {
					g.setColor(days[7].getBackground());
				}
				g.fillRect(0, 0, getWidth(), getHeight());
				if (isBorderPainted()) {
					setContentAreaFilled(true);
				}
				else {
					setContentAreaFilled(false);
				}
			}
			super.paint(g);
		}

	}

	public static final String DAY_PROPERTY = "day";
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a JFrame with a JDayChooser inside and can be used for testing.
	 *
	 * @param s
	 *            The command line arguments
	 */
	public static void main(final String[] s) {
		final JFrame frame = new JFrame("JDayChooser");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(new JDayChooser());
		frame.pack();
		frame.setVisible(true);
	}

	private boolean alwaysFireDayProperty;
	protected Calendar calendar;
	protected int day;
	protected boolean dayBordersVisible;
	protected String[] dayNames;
	protected JPanel dayPanel;
	protected JButton[] days;
	protected Color decorationBackgroundColor;
	protected boolean decorationBackgroundVisible = true;
	protected boolean decorationBordersVisible;
	protected Date defaultMaxSelectableDate;
	protected Date defaultMinSelectableDate;
	protected boolean initialized;
	protected Locale locale;
	protected int maxDayCharacters;
	protected Date maxSelectableDate;
	protected Date minSelectableDate;
	protected JMonthChooser monthChooser = null;
	protected Color oldDayBackgroundColor;
	protected Color selectedColor;
	protected JButton selectedDay;
	protected Color sundayForeground;
	protected Calendar today;
	protected Color weekdayForeground;
	protected boolean weekOfYearVisible;
	protected JPanel weekPanel;
	protected JButton[] weeks;
	protected JYearChooser yearChooser = null;

	/**
	 * Default JDayChooser constructor.
	 */
	public JDayChooser() {
		this(false);
	}

	/**
	 * JDayChooser constructor.
	 *
	 * @param weekOfYearVisible
	 *            true, if the weeks of a year shall be shown
	 */
	public JDayChooser(final boolean weekOfYearVisible) {
		setName("JDayChooser");
		setBackground(Color.blue);
		this.weekOfYearVisible = weekOfYearVisible;
		locale = Locale.getDefault();
		days = new JButton[49];
		selectedDay = null;
		calendar = Calendar.getInstance(locale);
		today = (Calendar) calendar.clone();
		setLayout(new BorderLayout());
		dayPanel = new JPanel();
		dayPanel.setLayout(new GridLayout(7, 7));
		sundayForeground = new Color(164, 0, 0);
		weekdayForeground = new Color(0, 90, 164);
		decorationBackgroundColor = new Color(210, 228, 238);
		for (int y = 0; y < 7; y++) {
			for (int x = 0; x < 7; x++) {
				final int index = x + (7 * y);
				if (y == 0) {
					days[index] = new DecoratorButton();
				}
				else {
					days[index] = new JDayButton() {
						/**
						 *
						 */
						private static final long serialVersionUID = 1L;

						@Override
						public void paint(final Graphics g) {
							if ("Windows".equals(UIManager.getLookAndFeel().getID())) {
								if (selectedDay == this) {
									g.setColor(selectedColor);
									g.fillRect(0, 0, getWidth(), getHeight());
								}
							}
							super.paint(g);
						}
					};
					days[index].addActionListener(this);
					days[index].addKeyListener(this);
					days[index].addFocusListener(this);
				}
				days[index].setMargin(new Insets(0, 0, 0, 0));
				days[index].setFocusPainted(false);
				dayPanel.add(days[index]);
			}
		}
		weekPanel = new JPanel();
		weekPanel.setLayout(new GridLayout(7, 1));
		weeks = new JButton[7];
		for (int i = 0; i < 7; i++) {
			weeks[i] = new DecoratorButton();
			weeks[i].setMargin(new Insets(0, 0, 0, 0));
			weeks[i].setFocusPainted(false);
			weeks[i].setForeground(new Color(100, 100, 100));
			if (i != 0) {
				weeks[i].setText("0" + (i + 1));
			}
			weekPanel.add(weeks[i]);
		}
		final Calendar tmpCalendar = Calendar.getInstance();
		tmpCalendar.set(1, 0, 1, 1, 1);
		defaultMinSelectableDate = tmpCalendar.getTime();
		minSelectableDate = defaultMinSelectableDate;
		tmpCalendar.set(9999, 0, 1, 1, 1);
		defaultMaxSelectableDate = tmpCalendar.getTime();
		maxSelectableDate = defaultMaxSelectableDate;
		init();
		setDay(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
		add(dayPanel, BorderLayout.CENTER);
		if (weekOfYearVisible) {
			add(weekPanel, BorderLayout.WEST);
		}
		initialized = true;
		updateUI();
	}

	/**
	 * JDayChooser is the ActionListener for all day buttons.
	 *
	 * @param e
	 *            the ActionEvent
	 */
	public void actionPerformed(final ActionEvent e) {
		final JButton button = (JButton) e.getSource();
		final String buttonText = button.getText();
		final int day = new Integer(buttonText).intValue();
		setDay(day);
	}

	/**
	 * Draws the day names of the day columnes.
	 */
	private void drawDayNames() {
		final int firstDayOfWeek = calendar.getFirstDayOfWeek();
		final DateFormatSymbols dateFormatSymbols = new DateFormatSymbols(locale);
		dayNames = dateFormatSymbols.getShortWeekdays();
		int day = firstDayOfWeek;
		for (int i = 0; i < 7; i++) {
			if (maxDayCharacters > 0 && maxDayCharacters < 5) {
				if (dayNames[day].length() >= maxDayCharacters) {
					dayNames[day] = dayNames[day].substring(0, maxDayCharacters);
				}
			}
			days[i].setText(dayNames[day]);
			if (day == 1) {
				days[i].setForeground(sundayForeground);
			}
			else {
				days[i].setForeground(weekdayForeground);
			}
			if (day < 7) {
				day++;
			}
			else {
				day -= 6;
			}
		}
	}

	/**
	 * Hides and shows the day buttons.
	 */
	protected void drawDays() {
		final Calendar tmpCalendar = (Calendar) calendar.clone();
		tmpCalendar.set(Calendar.HOUR_OF_DAY, 0);
		tmpCalendar.set(Calendar.MINUTE, 0);
		tmpCalendar.set(Calendar.SECOND, 0);
		tmpCalendar.set(Calendar.MILLISECOND, 0);
		final Calendar minCal = Calendar.getInstance();
		minCal.setTime(minSelectableDate);
		minCal.set(Calendar.HOUR_OF_DAY, 0);
		minCal.set(Calendar.MINUTE, 0);
		minCal.set(Calendar.SECOND, 0);
		minCal.set(Calendar.MILLISECOND, 0);
		final Calendar maxCal = Calendar.getInstance();
		maxCal.setTime(maxSelectableDate);
		maxCal.set(Calendar.HOUR_OF_DAY, 0);
		maxCal.set(Calendar.MINUTE, 0);
		maxCal.set(Calendar.SECOND, 0);
		maxCal.set(Calendar.MILLISECOND, 0);
		final int firstDayOfWeek = tmpCalendar.getFirstDayOfWeek();
		tmpCalendar.set(Calendar.DAY_OF_MONTH, 1);
		int firstDay = tmpCalendar.get(Calendar.DAY_OF_WEEK) - firstDayOfWeek;
		if (firstDay < 0) {
			firstDay += 7;
		}
		int i;
		for (i = 0; i < firstDay; i++) {
			days[i + 7].setVisible(false);
			days[i + 7].setText("");
		}
		tmpCalendar.add(Calendar.MONTH, 1);
		final Date firstDayInNextMonth = tmpCalendar.getTime();
		tmpCalendar.add(Calendar.MONTH, -1);
		Date day = tmpCalendar.getTime();
		int n = 0;
		final Color foregroundColor = getForeground();
		while (day.before(firstDayInNextMonth)) {
			days[i + n + 7].setText(Integer.toString(n + 1));
			days[i + n + 7].setVisible(true);
			if ((tmpCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR))
			        && (tmpCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR))) {
				days[i + n + 7].setForeground(sundayForeground);
			}
			else {
				days[i + n + 7].setForeground(foregroundColor);
			}
			if ((n + 1) == this.day) {
				days[i + n + 7].setBackground(selectedColor);
				selectedDay = days[i + n + 7];
			}
			else {
				days[i + n + 7].setBackground(oldDayBackgroundColor);
			}
			if (tmpCalendar.before(minCal) || tmpCalendar.after(maxCal)) {
				days[i + n + 7].setEnabled(false);
			}
			else {
				days[i + n + 7].setEnabled(true);
			}
			n++;
			tmpCalendar.add(Calendar.DATE, 1);
			day = tmpCalendar.getTime();
		}
		for (int k = n + i + 7; k < 49; k++) {
			days[k].setVisible(false);
			days[k].setText("");
		}
		drawWeeks();
	}

	/**
	 * Hides and shows the week buttons.
	 */
	protected void drawWeeks() {
		final Calendar tmpCalendar = (Calendar) calendar.clone();
		for (int i = 1; i < 7; i++) {
			tmpCalendar.set(Calendar.DAY_OF_MONTH, (i * 7) - 6);
			final int week = tmpCalendar.get(Calendar.WEEK_OF_YEAR);
			String buttonText = Integer.toString(week);
			if (week < 10) {
				buttonText = "0" + buttonText;
			}
			weeks[i].setText(buttonText);
			if ((i == 5) || (i == 6)) {
				weeks[i].setVisible(days[i * 7].isVisible());
			}
		}
	}

	/**
	 * JDayChooser is the FocusListener for all day buttons. (Added by Thomas
	 * Schaefer)
	 *
	 * @param e
	 *            the FocusEvent
	 */
	/*
	 * Code below commented out by Mark Brown on 24 Aug 2004. This code breaks
	 * the JDateChooser code by triggering the actionPerformed method on the
	 * next day button. This causes the date chosen to always be incremented by
	 * one day.
	 */
	public void focusGained(final FocusEvent e) {
	}

	/**
	 * Does nothing.
	 *
	 * @param e
	 *            the FocusEvent
	 */
	public void focusLost(final FocusEvent e) {
	}

	/**
	 * Returns the selected day.
	 *
	 * @return the day value
	 * @see #setDay
	 */
	public int getDay() {
		return day;
	}

	/**
	 * Returns the day panel.
	 *
	 * @return the day panel
	 */
	public JPanel getDayPanel() {
		return dayPanel;
	}

	/**
	 * Returns the color of the decoration (day names and weeks).
	 *
	 * @return the color of the decoration (day names and weeks).
	 */
	public Color getDecorationBackgroundColor() {
		return decorationBackgroundColor;
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
	 * Gets the maximum number of characters of a day name or 0. If 0 is
	 * returned, dateFormatSymbols.getShortWeekdays() will be used.
	 *
	 * @return the maximum number of characters of a day name or 0.
	 */
	public int getMaxDayCharacters() {
		return maxDayCharacters;
	}

	/**
	 * Gets the maximum selectable date.
	 *
	 * @return the maximum selectable date
	 */
	public Date getMaxSelectableDate() {
		return maxSelectableDate;
	}

	/**
	 * Gets the minimum selectable date.
	 *
	 * @return the minimum selectable date
	 */
	public Date getMinSelectableDate() {
		return minSelectableDate;
	}

	/**
	 * Returns the Sunday foreground.
	 *
	 * @return Color the Sunday foreground.
	 */
	public Color getSundayForeground() {
		return sundayForeground;
	}

	/**
	 * Returns the weekday foreground.
	 *
	 * @return Color the weekday foreground.
	 */
	public Color getWeekdayForeground() {
		return weekdayForeground;
	}

	/**
	 * Initilizes the locale specific names for the days of the week.
	 */
	protected void init() {
		final JButton testButton = new JButton();
		oldDayBackgroundColor = testButton.getBackground();
		selectedColor = new Color(160, 160, 160);
		final Date date = calendar.getTime();
		calendar = Calendar.getInstance(locale);
		calendar.setTime(date);
		drawDayNames();
		drawDays();
	}

	/**
	 * Initializes both day names and weeks of the year.
	 */
	protected void initDecorations() {
		for (int x = 0; x < 7; x++) {
			days[x].setContentAreaFilled(decorationBackgroundVisible);
			days[x].setBorderPainted(decorationBordersVisible);
			days[x].invalidate();
			days[x].repaint();
			weeks[x].setContentAreaFilled(decorationBackgroundVisible);
			weeks[x].setBorderPainted(decorationBordersVisible);
			weeks[x].invalidate();
			weeks[x].repaint();
		}
	}

	public boolean isDayBordersVisible() {
		return dayBordersVisible;
	}

	/**
	 * The decoration background is the background color of the day titles and
	 * the weeks of the year.
	 *
	 * @return Returns true, if the decoration background is painted.
	 */
	public boolean isDecorationBackgroundVisible() {
		return decorationBackgroundVisible;
	}

	/**
	 * The decoration border is the button border of the day titles and the
	 * weeks of the year.
	 *
	 * @return Returns true, if the decoration border is painted.
	 */
	public boolean isDecorationBordersVisible() {
		return decorationBordersVisible;
	}

	/**
	 * In some Countries it is often usefull to know in which week of the year a
	 * date is.
	 *
	 * @return boolean true, if the weeks of the year is shown
	 */
	public boolean isWeekOfYearVisible() {
		return weekOfYearVisible;
	}

	/**
	 * JDayChooser is the KeyListener for all day buttons. (Added by Thomas
	 * Schaefer and modified by Austin Moore)
	 *
	 * @param e
	 *            the KeyEvent
	 */
	public void keyPressed(final KeyEvent e) {
		final int offset = (e.getKeyCode() == KeyEvent.VK_UP) ? (-7) : ((e.getKeyCode() == KeyEvent.VK_DOWN) ? (+7)
		        : ((e.getKeyCode() == KeyEvent.VK_LEFT) ? (-1) : ((e.getKeyCode() == KeyEvent.VK_RIGHT) ? (+1) : 0)));
		final int newDay = getDay() + offset;
		if ((newDay >= 1) && (newDay <= calendar.getActualMaximum(Calendar.DAY_OF_MONTH))) {
			setDay(newDay);
		}
		else if (monthChooser != null && yearChooser != null) {
			final GregorianCalendar tempCalendar = new GregorianCalendar(yearChooser.getYear(),
			    monthChooser.getMonth(), getDay());
			tempCalendar.add(Calendar.DAY_OF_MONTH, offset);
			final int month = tempCalendar.get(Calendar.MONTH);
			final int year = tempCalendar.get(Calendar.YEAR);
			final int day = tempCalendar.get(Calendar.DAY_OF_MONTH);
			yearChooser.setYear(year);
			monthChooser.setMonth(month);
			this.setDay(day);
		}
	}

	/**
	 * Does nothing.
	 *
	 * @param e
	 *            the KeyEvent
	 */
	public void keyReleased(final KeyEvent e) {
	}

	/**
	 * Does nothing.
	 *
	 * @param e
	 *            the KeyEvent
	 */
	public void keyTyped(final KeyEvent e) {
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
	 * this is needed for JDateChooser.
	 *
	 * @param alwaysFire
	 *            true, if day property shall be fired every time a day is
	 *            chosen.
	 */
	public void setAlwaysFireDayProperty(final boolean alwaysFire) {
		alwaysFireDayProperty = alwaysFire;
	}

	/**
	 * Sets a specific calendar. This is needed for correct graphical
	 * representation of the days.
	 *
	 * @param calendar
	 *            the new calendar
	 */
	public void setCalendar(final Calendar calendar) {
		this.calendar = calendar;
		drawDays();
	}

	/**
	 * Sets the day. This is a bound property.
	 *
	 * @param d
	 *            the day
	 * @see #getDay
	 */
	public void setDay(int d) {
		if (d < 1) {
			d = 1;
		}
		final Calendar tmpCalendar = (Calendar) calendar.clone();
		tmpCalendar.set(Calendar.DAY_OF_MONTH, 1);
		tmpCalendar.add(Calendar.MONTH, 1);
		tmpCalendar.add(Calendar.DATE, -1);
		final int maxDaysInMonth = tmpCalendar.get(Calendar.DATE);
		if (d > maxDaysInMonth) {
			d = maxDaysInMonth;
		}
		final int oldDay = day;
		day = d;
		if (selectedDay != null) {
			selectedDay.setBackground(oldDayBackgroundColor);
			selectedDay.removeMouseListener(this);
			selectedDay.setMultiClickThreshhold(0);
			selectedDay.repaint();
		}
		for (int i = 7; i < 49; i++) {
			if (days[i].getText().equals(Integer.toString(day))) {
				selectedDay = days[i];
				selectedDay.setMultiClickThreshhold(10000000);
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						selectedDay.addMouseListener(JDayChooser.this);
					}
				});
				selectedDay.setBackground(selectedColor);
				break;
			}
		}
		if (alwaysFireDayProperty) {
			firePropertyChange(JDayChooser.DAY_PROPERTY, 0, day);
		}
		else {
			firePropertyChange(JDayChooser.DAY_PROPERTY, oldDay, day);
		}
	}

	public void setDayBordersVisible(final boolean dayBordersVisible) {
		this.dayBordersVisible = dayBordersVisible;
		if (initialized) {
			for (int x = 7; x < 49; x++) {
				if ("Windows".equals(UIManager.getLookAndFeel().getID())) {
					days[x].setContentAreaFilled(dayBordersVisible);
				}
				else {
					days[x].setContentAreaFilled(true);
				}
				days[x].setBorderPainted(dayBordersVisible);
			}
		}
	}

	/**
	 * Sets the background of days and weeks of year buttons.
	 *
	 * @param decorationBackgroundColor
	 *            The background to set
	 */
	public void setDecorationBackgroundColor(final Color decorationBackgroundColor) {
		this.decorationBackgroundColor = decorationBackgroundColor;
		if (days != null) {
			for (int i = 0; i < 7; i++) {
				days[i].setBackground(decorationBackgroundColor);
			}
		}
		if (weeks != null) {
			for (int i = 0; i < 7; i++) {
				weeks[i].setBackground(decorationBackgroundColor);
			}
		}
	}

	/**
	 * The decoration background is the background color of the day titles and
	 * the weeks of the year.
	 *
	 * @param decorationBackgroundVisible
	 *            true, if the decoration background shall be painted.
	 */
	public void setDecorationBackgroundVisible(final boolean decorationBackgroundVisible) {
		this.decorationBackgroundVisible = decorationBackgroundVisible;
		initDecorations();
	}

	/**
	 * The decoration border is the button border of the day titles and the
	 * weeks of the year.
	 *
	 * @param decorationBordersVisible
	 *            true, if the decoration border shall be painted.
	 */
	public void setDecorationBordersVisible(final boolean decorationBordersVisible) {
		this.decorationBordersVisible = decorationBordersVisible;
		initDecorations();
	}

	/**
	 * Enable or disable the JDayChooser.
	 *
	 * @param enabled
	 *            The new enabled value
	 */
	@Override
	public void setEnabled(final boolean enabled) {
		super.setEnabled(enabled);
		for (short i = 0; i < days.length; i++) {
			if (days[i] != null) {
				days[i].setEnabled(enabled);
			}
		}
		for (short i = 0; i < weeks.length; i++) {
			if (weeks[i] != null) {
				weeks[i].setEnabled(enabled);
			}
		}
	}

	/**
	 * Requests that the selected day also have the focus.
	 */
	public void setFocus() {
		if (selectedDay != null) {
			selectedDay.requestFocusInWindow();
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
		if (days != null) {
			for (int i = 0; i < 49; i++) {
				days[i].setFont(font);
			}
		}
		if (weeks != null) {
			for (int i = 0; i < 7; i++) {
				weeks[i].setFont(font);
			}
		}
	}

	/**
	 * Sets the foregroundColor color.
	 *
	 * @param foreground
	 *            the new foregroundColor
	 */
	@Override
	public void setForeground(final Color foreground) {
		super.setForeground(foreground);
		if (days != null) {
			for (int i = 7; i < 49; i++) {
				days[i].setForeground(foreground);
			}
			drawDays();
		}
	}

	/**
	 * Sets the locale.
	 *
	 * @param locale
	 *            the new locale value
	 * @see #getLocale
	 */
	@Override
	public void setLocale(final Locale locale) {
		if (!initialized) {
			super.setLocale(locale);
		}
		else {
			this.locale = locale;
			super.setLocale(locale);
			init();
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
		if (maxDayCharacters == this.maxDayCharacters) {
			return;
		}
		if (maxDayCharacters < 0 || maxDayCharacters > 4) {
			this.maxDayCharacters = 0;
		}
		else {
			this.maxDayCharacters = maxDayCharacters;
		}
		drawDayNames();
		drawDays();
		invalidate();
	}

	/**
	 * Sets the maximum selectable date. If null, the date 01\01\9999 will be
	 * set instead.
	 *
	 * @param max
	 *            the maximum selectable date
	 * @return the maximum selectable date
	 */
	public Date setMaxSelectableDate(final Date max) {
		if (max == null) {
			maxSelectableDate = defaultMaxSelectableDate;
		}
		else {
			maxSelectableDate = max;
		}
		drawDays();
		return maxSelectableDate;
	}

	/**
	 * Sets the minimum selectable date. If null, the date 01\01\0001 will be
	 * set instead.
	 *
	 * @param min
	 *            the minimum selectable date
	 * @return the minimum selectable date
	 */
	public Date setMinSelectableDate(final Date min) {
		if (min == null) {
			minSelectableDate = defaultMinSelectableDate;
		}
		else {
			minSelectableDate = min;
		}
		drawDays();
		return minSelectableDate;
	}

	/**
	 * Sets a specific month. This is needed for correct graphical
	 * representation of the days.
	 *
	 * @param month
	 *            the new month
	 */
	public void setMonth(final int month) {
		final int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		calendar.set(Calendar.MONTH, month);
		if (maxDays == day) {
			day = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		}
		final boolean storedMode = alwaysFireDayProperty;
		alwaysFireDayProperty = false;
		setDay(day);
		alwaysFireDayProperty = storedMode;
		drawDays();
	}

	public void setMonthChooser(final JMonthChooser monthChooser) {
		this.monthChooser = monthChooser;
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
		if (min == null) {
			minSelectableDate = defaultMinSelectableDate;
		}
		else {
			minSelectableDate = min;
		}
		if (max == null) {
			maxSelectableDate = defaultMaxSelectableDate;
		}
		else {
			maxSelectableDate = max;
		}
		if (maxSelectableDate.before(minSelectableDate)) {
			minSelectableDate = defaultMinSelectableDate;
			maxSelectableDate = defaultMaxSelectableDate;
		}
		drawDays();
	}

	/**
	 * Sets the Sunday foreground.
	 *
	 * @param sundayForeground
	 *            The sundayForeground to set
	 */
	public void setSundayForeground(final Color sundayForeground) {
		this.sundayForeground = sundayForeground;
		drawDayNames();
		drawDays();
	}

	/**
	 * Sets the weekday foreground.
	 *
	 * @param weekdayForeground
	 *            The weekdayForeground to set
	 */
	public void setWeekdayForeground(final Color weekdayForeground) {
		this.weekdayForeground = weekdayForeground;
		drawDayNames();
		drawDays();
	}

	/**
	 * In some Countries it is often usefull to know in which week of the year a
	 * date is.
	 *
	 * @param weekOfYearVisible
	 *            true, if the weeks of the year shall be shown
	 */
	public void setWeekOfYearVisible(final boolean weekOfYearVisible) {
		if (weekOfYearVisible == this.weekOfYearVisible) {
			return;
		}
		else if (weekOfYearVisible) {
			add(weekPanel, BorderLayout.WEST);
		}
		else {
			remove(weekPanel);
		}
		this.weekOfYearVisible = weekOfYearVisible;
		validate();
		dayPanel.validate();
	}

	/**
	 * Sets a specific year. This is needed for correct graphical representation
	 * of the days.
	 *
	 * @param year
	 *            the new year
	 */
	public void setYear(final int year) {
		calendar.set(Calendar.YEAR, year);
		drawDays();
	}

	public void setYearChooser(final JYearChooser yearChooser) {
		this.yearChooser = yearChooser;
	}

	/**
	 * Updates the UI and sets the day button preferences.
	 */
	@Override
	public void updateUI() {
		super.updateUI();
		setFont(UITools.scale(Font.decode("Dialog Plain 11")));
		if (weekPanel != null) {
			weekPanel.updateUI();
		}
		if (initialized) {
			if ("Windows".equals(UIManager.getLookAndFeel().getID())) {
				setDayBordersVisible(false);
				setDecorationBackgroundVisible(true);
				setDecorationBordersVisible(false);
			}
			else {
				setDayBordersVisible(true);
				setDecorationBackgroundVisible(decorationBackgroundVisible);
				setDecorationBordersVisible(decorationBordersVisible);
			}
		}
	}
}
