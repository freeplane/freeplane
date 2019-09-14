/*
 * JSpinField.java - A spin field using a JSpinner (JDK 1.4) Copyright (C) 2004
 * Kai Toedter kai@toedter.com www.toedter.com This program is free software;
 * you can redistribute it and/or modify it under the terms of the GNU Lesser
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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * JSpinField is a numeric field with 2 spin buttons to increase or decrease the
 * value. It has the same interface as the "old" JSpinField but uses a JSpinner
 * internally (since J2SE SDK 1.4) rather than a scrollbar for emulating the
 * spin buttons.
 *
 * @author Kai Toedter
 * @version $LastChangedRevision: 85 $
 * @version $LastChangedDate: 2006-04-28 13:50:52 +0200 (Fr, 28 Apr 2006) $
 */
class JSpinField extends JPanel implements ChangeListener, CaretListener, ActionListener, FocusListener {
	private static final Color DEFAULT_TEXT_COLOR = UIManager.getColor("TextField.foreground");
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a JFrame with a JSpinField inside and can be used for testing.
	 *
	 * @param s
	 *            The command line arguments
	 */
	public static void main(final String[] s) {
		final JFrame frame = new JFrame("JSpinField");
		frame.getContentPane().add(new JSpinField());
		frame.pack();
		frame.setVisible(true);
	}

	private Color darkGreen;
	private int max;
	private int min;
	private int minWidth;
	int getMinWidth() {
    	return minWidth;
    }

	void setMinWidth(int minWidth) {
    	this.minWidth = minWidth;
    }

	protected JSpinner spinner;
	/** the text (number) field */
	private JTextField textField;
	protected int value;

	/**
	 * Default JSpinField constructor. The valid value range is between
	 * Integer.MIN_VALUE and Integer.MAX_VALUE. The initial value is 0.
	 */
	public JSpinField() {
		this(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * JSpinField constructor with given minimum and maximum vaues and initial
	 * value 0.
	 */
	public JSpinField(final int min, int max) {
		super();
		setName("JSpinField");
		this.min = min;
		if (max < min) {
			max = min;
		}
		this.max = max;
		value = 0;
		if (value < min) {
			value = min;
		}
		if (value > max) {
			value = max;
		}
		darkGreen = new Color(0, 150, 0);
		setLayout(new BorderLayout());
		textField = new JTextField(){
            private static final long serialVersionUID = 1L;

			@Override
            public void setText(String t) {
				if(minWidth <= t.length()){
					super.setText(t);
				}
				else{
					StringBuilder sb = new  StringBuilder(minWidth);
					for(int i = minWidth; i > t.length(); i--){
						sb.append('0');
					}
					sb.append(t);
					super.setText(sb.toString());
				}
            }

		};
		textField.addCaretListener(this);
		textField.addActionListener(this);
		textField.setHorizontalAlignment(SwingConstants.RIGHT);
		textField.setBorder(BorderFactory.createEmptyBorder());
		textField.setText(Integer.toString(value));
		textField.addFocusListener(this);
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
		spinner.setEditor(textField);
		spinner.addChangeListener(this);
		add(spinner, BorderLayout.CENTER);
	}

	/**
	 * After any user input, the value of the textfield is proofed. Depending on
	 * being an integer, the value is colored green or red. If the textfield is
	 * green, the enter key is accepted and the new value is set.
	 *
	 * @param e
	 *            Description of the Parameter
	 */
	public void actionPerformed(final ActionEvent e) {
		if (textField.getForeground().equals(darkGreen)) {
			setValue(Integer.valueOf(textField.getText()).intValue());
		}
	}

	public void adjustWidthToMaximumValue() {
		final JTextField testTextField = new JTextField(Integer.toString(max));
		final int width = testTextField.getPreferredSize().width;
		final int height = testTextField.getPreferredSize().height;
		textField.setPreferredSize(new Dimension(width, height));
		textField.revalidate();
	}

	/**
	 * After any user input, the value of the textfield is proofed. Depending on
	 * being an integer, the value is colored green or red.
	 *
	 * @param e
	 *            the caret event
	 */
	public void caretUpdate(final CaretEvent e) {
		try {
			final int testValue = Integer.valueOf(textField.getText()).intValue();
			if ((testValue >= min) && (testValue <= max)) {
				textField.setForeground(darkGreen);
				setValue(testValue, false, true);
			}
			else {
				textField.setForeground(Color.red);
			}
		}
		catch (final Exception ex) {
			if (ex instanceof NumberFormatException) {
				textField.setForeground(Color.red);
			}
		}
		textField.repaint();
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
	 */
	public void focusGained(final FocusEvent e) {
	}

	/**
	 * The value of the text field is checked against a valid (green) value. If
	 * valid, the value is set and a property change is fired.
	 */
	public void focusLost(final FocusEvent e) {
		actionPerformed(null);
	}

	/**
	 * Returns the maximum value.
	 *
	 * @return the maximum value
	 */
	public int getMaximum() {
		return max;
	}

	/**
	 * Returns the minimum value.
	 *
	 * @return the minimum value
	 */
	public int getMinimum() {
		return min;
	}

	/**
	 * Returns the year chooser's spinner (which allow the focus to be set to
	 * it).
	 *
	 * @return Component the spinner or null, if the month chooser has no
	 *         spinner
	 */
	public Component getSpinner() {
		return spinner;
	}

	/**
	 * Returns the value.
	 *
	 * @return the value value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Enable or disable the JSpinField.
	 *
	 * @param enabled
	 *            The new enabled value
	 */
	@Override
	public void setEnabled(final boolean enabled) {
		super.setEnabled(enabled);
		spinner.setEnabled(enabled);
		textField.setEnabled(enabled);
		/*
		 * Fixes the background bug 4991597 and sets the background explicitely
		 * to a TextField.inactiveBackground.
		 */
		if (!enabled) {
			textField.setBackground(UIManager.getColor("TextField.inactiveBackground"));
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
		if (textField != null) {
			textField.setFont(font);
		}
	}

	/**
	 * Sets the foreground
	 *
	 * @param fg
	 *            the foreground
	 */
	@Override
	public void setForeground(final Color fg) {
		if (textField != null) {
			textField.setForeground(fg);
		}
	}

	/**
	 * Sets the horizontal alignment of the displayed value.
	 *
	 * @param alignment
	 *            the horizontal alignment
	 */
	public void setHorizontalAlignment(final int alignment) {
		textField.setHorizontalAlignment(alignment);
	}

	/**
	 * Sets the maximum value and adjusts the preferred width.
	 *
	 * @param newMaximum
	 *            the new maximum value
	 * @see #getMaximum
	 */
	public void setMaximum(final int newMaximum) {
		max = newMaximum;
	}

	/**
	 * Sets the minimum value.
	 *
	 * @param newMinimum
	 *            the new minimum value
	 * @see #getMinimum
	 */
	public void setMinimum(final int newMinimum) {
		min = newMinimum;
	}

	/**
	 * Sets the value. This is a bound property.
	 *
	 * @param newValue
	 *            the new value
	 * @see #getValue
	 */
	public void setValue(final int newValue) {
		setValue(newValue, true, true);
		spinner.setValue(new Integer(value));
	}

	/**
	 * Sets the value attribute of the JSpinField object.
	 *
	 * @param newValue
	 *            The new value
	 * @param updateTextField
	 *            true if text field should be updated
	 */
	protected void setValue(final int newValue, final boolean updateTextField, final boolean firePropertyChange) {
		final int oldValue = value;
		if (newValue < min) {
			value = min;
		}
		else if (newValue > max) {
			value = max;
		}
		else {
			value = newValue;
		}
		if (updateTextField) {
			textField.setText(Integer.toString(value));
			textField.setForeground(DEFAULT_TEXT_COLOR);
		}
		if (firePropertyChange) {
			firePropertyChange("value", oldValue, value);
		}
	}

	/**
	 * Is invoked when the spinner model changes
	 *
	 * @param e
	 *            the ChangeEvent
	 */
	public void stateChanged(final ChangeEvent e) {
		final SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
		final int value = model.getNumber().intValue();
		setValue(value);
	}
}
