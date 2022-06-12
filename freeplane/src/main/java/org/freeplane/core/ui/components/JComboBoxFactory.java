package org.freeplane.core.ui.components;

import java.awt.Dimension;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.event.ListDataEvent;

public class JComboBoxFactory{
	private static class JComboBoxWithAdaptingSize<T> extends JComboBox<T> {
		private static final long serialVersionUID = 1L;
		private boolean preferredSizeCalculationInProgress;

		private JComboBoxWithAdaptingSize(ComboBoxModel<T> aModel) {
			super(aModel);
		}

		@Override
		public void intervalAdded(ListDataEvent e) {
			super.intervalAdded(e);
			adaptSize();
		}

		@Override
		public void contentsChanged(ListDataEvent e) {
			super.contentsChanged(e);
			adaptSize();
		}

		private void adaptSize() {
			if(getPrototypeDisplayValue() == null)
				revalidate();
		}

		@Override
		public Dimension getPreferredSize() {
			preferredSizeCalculationInProgress = true;
			try {
				return super.getPreferredSize();
			} finally {
				preferredSizeCalculationInProgress = false;
			}
		}

		@Override
		public boolean isEditable() {
			return ! preferredSizeCalculationInProgress && super.isEditable();
		}
	}

	public static final int MAXIMUM_ROW_COUNT = 10;

	public static <T> JComboBox<T> create() {
		JComboBox<T> c = new JComboBox<T>();
		c.setMaximumRowCount(MAXIMUM_ROW_COUNT);
		return c;
	}

	public static <T> JComboBox<T> create(ComboBoxModel<T> aModel) {
		JComboBox<T> c = new JComboBoxWithAdaptingSize<T>(aModel);
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
