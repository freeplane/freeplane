package org.freeplane.core.ui.components;

import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

public class JComboBoxFactory{
	public static final int MAXIMUM_ROW_COUNT = 10;

	public static <T> JComboBox<T> create() {
		JComboBox<T> c = new JComboBox<T>();
		c.setMaximumRowCount(MAXIMUM_ROW_COUNT);
		return c;
	}

	public static <T> JComboBox<T> create(ComboBoxModel aModel) {
		JComboBox<T> c = new JComboBox<T>(aModel);
		c.setMaximumRowCount(MAXIMUM_ROW_COUNT);
		return c;
	}

	public static <T> JComboBox<T> create(T[] items) {
		JComboBox<T> c = new JComboBox<T>(items);
		c.setMaximumRowCount(MAXIMUM_ROW_COUNT);
		return c;
	}

	public static <T> JComboBox<T> create(Vector<T> items) {
		JComboBox<T> c = new JComboBox<T>(items);
		c.setMaximumRowCount(MAXIMUM_ROW_COUNT);
		return c;
	}
}
