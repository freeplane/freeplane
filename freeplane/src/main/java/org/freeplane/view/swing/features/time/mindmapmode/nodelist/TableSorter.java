/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.view.swing.features.time.mindmapmode.nodelist;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * TableSorter is a decorator for TableModels; adding sorting functionality to a
 * supplied TableModel. TableSorter does not store or copy the data in its
 * TableModel; instead it maintains a map from the row indexes of the view to
 * the row indexes of the model. As requests are made of the sorter (like
 * getValueAt(row, col)) they are passed to the underlying model after the row
 * numbers have been translated via the internal mapping array. This way, the
 * TableSorter appears to hold another copy of the table with the rows in a
 * different order. <p/> TableSorter registers itself as a listener to the
 * underlying model, just as the JTable itself would. Events recieved from the
 * model are examined, sometimes manipulated (typically widened), and then
 * passed on to the TableSorter's listeners (typically the JTable). If a change
 * to the model has invalidated the order of TableSorter's rows, a note of this
 * is made and the sorter will resort the rows the next time a value is
 * requested. <p/> When the tableHeader property is set, either by using the
 * setTableHeader() method or the two argument constructor, the table header may
 * be used as a complete UI for TableSorter. The default renderer of the
 * tableHeader is decorated with a renderer that indicates the sorting status of
 * each column. In addition, a mouse listener is installed with the following
 * behavior:
 * <ul>
 * <li>Mouse-click: Clears the sorting status of all other columns and advances
 * the sorting status of that column through three values: {NOT_SORTED,
 * ASCENDING, DESCENDING} (then back to NOT_SORTED again).
 * <li>SHIFT-mouse-click: Clears the sorting status of all other columns and
 * cycles the sorting status of the column through the same three values, in the
 * opposite order: {NOT_SORTED, DESCENDING, ASCENDING}.
 * <li>CONTROL-mouse-click and CONTROL-SHIFT-mouse-click: as above except that
 * the changes to the column do not cancel the statuses of columns that are
 * already sorting - giving a way to initiate a compound sort.
 * </ul>
 * <p/> This is a long overdue rewrite of a class of the same name that first
 * appeared in the swing table demos in 1997.
 *
 * @author Philip Milne
 * @author Brendon McLean
 * @author Dan van Enckevort
 * @author Parwinder Sekhon
 * @version 2.0 02/27/04
 */
class TableSorter extends AbstractTableModel {
	private static class Arrow implements Icon {
		final private boolean descending;
		final private int priority;
		final private int size;

		public Arrow(final boolean descending, final int size, final int priority) {
			this.descending = descending;
			this.size = size;
			this.priority = priority;
		}

		public int getIconHeight() {
			return size;
		}

		public int getIconWidth() {
			return size;
		}

		public void paintIcon(final Component c, final Graphics g, final int x, int y) {
			final Color color = c == null ? Color.GRAY : c.getBackground();
			final int dx = (int) (size / 2 * Math.pow(0.8, priority));
			final int dy = descending ? dx : -dx;
			y = y + 5 * size / 6 + (descending ? -dy : 0);
			final int shift = descending ? 1 : -1;
			g.translate(x, y);
			g.setColor(color.darker());
			g.drawLine(dx / 2, dy, 0, 0);
			g.drawLine(dx / 2, dy + shift, 0, shift);
			g.setColor(color.brighter());
			g.drawLine(dx / 2, dy, dx, 0);
			g.drawLine(dx / 2, dy + shift, dx, shift);
			if (descending) {
				g.setColor(color.darker().darker());
			}
			else {
				g.setColor(color.brighter().brighter());
			}
			g.drawLine(dx, 0, 0, 0);
			g.setColor(color);
			g.translate(-x, -y);
		}
	}

	private static class Directive {
		final private int column;
		final private int direction;

		public Directive(final int column, final int direction) {
			this.column = column;
			this.direction = direction;
		}
	}

	private class MouseHandler extends MouseAdapter {
		@Override
		public void mouseClicked(final MouseEvent e) {
			final JTableHeader h = (JTableHeader) e.getSource();
			final TableColumnModel columnModel = h.getColumnModel();
			final int viewColumn = columnModel.getColumnIndexAtX(e.getX());
			if (viewColumn != -1) {
				final int column = columnModel.getColumn(viewColumn).getModelIndex();
				int status = getSortingStatus(column);
				if (!e.isControlDown()) {
					cancelSorting();
				}
				status = status + (e.isShiftDown() ? -1 : 1);
				status = (status + 4) % 3 - 1;
				setSortingStatus(column, status);
			}
		}
	}

	private class Row implements Comparable<Object> {
		final private int modelIndex;

		public Row(final int index) {
			modelIndex = index;
		}

		public int compareTo(final Object o) {
			final int row1 = modelIndex;
			final int row2 = ((Row) o).modelIndex;
			for (final Directive directive : sortingColumns) {
				final int column = directive.column;
				final Object o1 = tableModel.getValueAt(row1, column);
				final Object o2 = tableModel.getValueAt(row2, column);
				int comparison = 0;
				if (o1 == null && o2 == null) {
					comparison = 0;
				}
				else if (o1 == null) {
					comparison = -1;
				}
				else if (o2 == null) {
					comparison = 1;
				}
				else {
					comparison = getComparator(column).compare(o1, o2);
				}
				if (comparison != 0) {
					return directive.direction == TableSorter.DESCENDING ? -comparison : comparison;
				}
			}
			return 0;
		}
	}

	private class SortableHeaderRenderer implements TableCellRenderer {
		final private TableCellRenderer tableCellRenderer;

		public SortableHeaderRenderer(final TableCellRenderer tableCellRenderer) {
			this.tableCellRenderer = tableCellRenderer;
		}

		public Component getTableCellRendererComponent(final JTable table, final Object value,
		                                               final boolean isSelected, final boolean hasFocus, final int row,
		                                               final int column) {
			final Component c = tableCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus,
			    row, column);
			if (c instanceof JLabel) {
				final JLabel l = (JLabel) c;
				l.setHorizontalTextPosition(SwingConstants.LEFT);
				final int modelColumn = table.convertColumnIndexToModel(column);
				l.setIcon(getHeaderRendererIcon(modelColumn, l.getFont().getSize()));
			}
			return c;
		}
	}

	private class TableModelHandler implements TableModelListener {
		public void tableChanged(final TableModelEvent e) {
			if (!isSorting()) {
				clearSortingState();
				fireTableChanged(e);
				return;
			}
			if (e.getFirstRow() == TableModelEvent.HEADER_ROW) {
				cancelSorting();
				fireTableChanged(e);
				return;
			}
			final int column = e.getColumn();
			if (e.getFirstRow() == e.getLastRow() && column != TableModelEvent.ALL_COLUMNS
			        && getSortingStatus(column) == TableSorter.NOT_SORTED && modelToView != null) {
				final int viewIndex = getModelToView()[e.getFirstRow()];
				fireTableChanged(new TableModelEvent(TableSorter.this, viewIndex, viewIndex, column, e.getType()));
				return;
			}
			clearSortingState();
			fireTableDataChanged();
			return;
		}
	}

	public static final int ASCENDING = 1;
	public static final Comparator<Object> COMPARABLE_COMPARATOR = new Comparator<Object>() {
		@SuppressWarnings("unchecked")
        public int compare(final Object o1, final Object o2) {
			return ((Comparable<Object>) o1).compareTo(o2);
		}
	};
	private static final int DESCENDING = -1;
	private static Directive EMPTY_DIRECTIVE = new Directive(-1, TableSorter.NOT_SORTED);
	public static final Comparator<Object> LEXICAL_COMPARATOR = new Comparator<Object>() {
		public int compare(final Object o1, final Object o2) {
			return o1.toString().compareTo(o2.toString());
		}
	};
	private static final int NOT_SORTED = 0;
	private static final long serialVersionUID = 1L;
	final private Map<Class<?>, Comparator<Object>> columnComparators = new HashMap<Class<?>, Comparator<Object>>();
	private int[] modelToView;
	final private MouseListener mouseListener;
	final private List<Directive> sortingColumns = new ArrayList<Directive>();
	private JTableHeader tableHeader;
	private TableModel tableModel;
	final private TableModelListener tableModelListener;
	private Row[] viewToModel;

	public TableSorter() {
		mouseListener = new MouseHandler();
		tableModelListener = new TableModelHandler();
	}

	public TableSorter(final TableModel tableModel) {
		this();
		setTableModel(tableModel);
	}

	private void cancelSorting() {
		sortingColumns.clear();
		sortingStatusChanged();
	}

	private void clearSortingState() {
		viewToModel = null;
		modelToView = null;
	}

	@Override
	public Class<?> getColumnClass(final int column) {
		return tableModel.getColumnClass(column);
	}

	public int getColumnCount() {
		return (tableModel == null) ? 0 : tableModel.getColumnCount();
	}

	@Override
	public String getColumnName(final int column) {
		return tableModel.getColumnName(column);
	}

	private Comparator<Object> getComparator(final int column) {
		final Class<?> columnType = tableModel.getColumnClass(column);
		final Comparator<Object> comparator = columnComparators.get(columnType);
		if (comparator != null) {
			return comparator;
		}
		if (Comparable.class.isAssignableFrom(columnType)) {
			return TableSorter.COMPARABLE_COMPARATOR;
		}
		return TableSorter.LEXICAL_COMPARATOR;
	}

	private Directive getDirective(final int column) {
		for (final Directive directive : sortingColumns) {
			if (directive.column == column) {
				return directive;
			}
		}
		return TableSorter.EMPTY_DIRECTIVE;
	}

	private Icon getHeaderRendererIcon(final int column, final int size) {
		final Directive directive = getDirective(column);
		if (directive == TableSorter.EMPTY_DIRECTIVE) {
			return null;
		}
		return new Arrow(directive.direction == TableSorter.DESCENDING, size, sortingColumns.indexOf(directive));
	}

	private int[] getModelToView() {
		if (modelToView == null) {
			final int n = getViewToModel().length;
			modelToView = new int[n];
			for (int i = 0; i < n; i++) {
				modelToView[modelIndex(i)] = i;
			}
		}
		return modelToView;
	}

	public int getRowCount() {
		return (tableModel == null) ? 0 : tableModel.getRowCount();
	}

	public int getSortingStatus(final int column) {
		return getDirective(column).direction;
	}

	public JTableHeader getTableHeader() {
		return tableHeader;
	}

	public TableModel getTableModel() {
		return tableModel;
	}

	public Object getValueAt(final int row, final int column) {
		return tableModel.getValueAt(modelIndex(row), column);
	}

	private Row[] getViewToModel() {
		if (viewToModel == null) {
			final int tableModelRowCount = tableModel.getRowCount();
			viewToModel = new Row[tableModelRowCount];
			for (int row = 0; row < tableModelRowCount; row++) {
				viewToModel[row] = new Row(row);
			}
			if (isSorting()) {
				Arrays.sort(viewToModel);
			}
		}
		return viewToModel;
	}

	@Override
	public boolean isCellEditable(final int row, final int column) {
		return tableModel.isCellEditable(modelIndex(row), column);
	}

	public boolean isSorting() {
		return sortingColumns.size() != 0;
	}

	private int modelIndex(final int viewIndex) {
		return getViewToModel()[viewIndex].modelIndex;
	}

	public void setColumnComparator(final Class<?> type, final Comparator<Object> comparator) {
		if (comparator == null) {
			columnComparators.remove(type);
		}
		else {
			columnComparators.put(type, comparator);
		}
	}

	public void setSortingStatus(final int column, final int status) {
		final Directive directive = getDirective(column);
		if (directive != TableSorter.EMPTY_DIRECTIVE) {
			sortingColumns.remove(directive);
		}
		if (status != TableSorter.NOT_SORTED) {
			sortingColumns.add(new Directive(column, status));
		}
		sortingStatusChanged();
	}

	public void setTableHeader(final JTableHeader tableHeader) {
		if (this.tableHeader != null) {
			this.tableHeader.removeMouseListener(mouseListener);
			final TableCellRenderer defaultRenderer = this.tableHeader.getDefaultRenderer();
			if (defaultRenderer instanceof SortableHeaderRenderer) {
				this.tableHeader.setDefaultRenderer(((SortableHeaderRenderer) defaultRenderer).tableCellRenderer);
			}
		}
		this.tableHeader = tableHeader;
		if (this.tableHeader != null) {
			this.tableHeader.addMouseListener(mouseListener);
			this.tableHeader.setDefaultRenderer(new SortableHeaderRenderer(this.tableHeader.getDefaultRenderer()));
		}
	}

	public void setTableModel(final TableModel tableModel) {
		if (this.tableModel != null) {
			this.tableModel.removeTableModelListener(tableModelListener);
		}
		this.tableModel = tableModel;
		if (this.tableModel != null) {
			this.tableModel.addTableModelListener(tableModelListener);
		}
		clearSortingState();
		fireTableStructureChanged();
	}

	@Override
	public void setValueAt(final Object aValue, final int row, final int column) {
		tableModel.setValueAt(aValue, modelIndex(row), column);
	}

	private void sortingStatusChanged() {
		clearSortingState();
		fireTableDataChanged();
		if (tableHeader != null) {
			tableHeader.repaint();
		}
	}
}
