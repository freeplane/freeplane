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

import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * @author foltin
 */
class FlatNodeTableFilterModel extends AbstractTableModel {
	private class TableModelHandler implements TableModelListener {
		@Override
		public void tableChanged(final TableModelEvent arg0) {
			fireTableDataChanged();
		}
	}

	private static final long serialVersionUID = 1L;
	private String mFilterRegexp;
	private Pattern mPattern;
	/**
	 * Contains indices or rows matching the filter criteria.
	 */
	private ArrayList<Integer> mIndexArray;
	/**
	 * The column that contains the NodeHolder items
	 */
	final private int[] mNodeTextColumns;
	final private TableModel mTableModel;
	private boolean matchCase;

	/**
	 * @param node_text_column
	 */
	public FlatNodeTableFilterModel(final TableModel tableModel, final int[] node_text_column) {
		super();
		mTableModel = tableModel;
		mNodeTextColumns = node_text_column;
		tableModel.addTableModelListener(new TableModelHandler());
		resetFilter();
	}

	@Override
	public Class<?> getColumnClass(final int arg0) {
		return mTableModel.getColumnClass(arg0);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return mTableModel.getColumnCount();
	}

	@Override
	public String getColumnName(final int pColumnIndex) {
		return mTableModel.getColumnName(pColumnIndex);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		return mIndexArray.size();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(final int row, final int column) {
		ensureValidRow(row);
		final int origRow = mIndexArray.get(row).intValue();
		return mTableModel.getValueAt(origRow, column);
	}

	private void ensureValidRow(final int row) {
		if (row < 0 || row >= getRowCount()) {
			throw new IllegalArgumentException("Illegal Row specified: " + row);
		}
	}

	@Override
	public void setValueAt(Object value, final int row, final int column) {
		ensureValidRow(row);
		final int origRow = mIndexArray.get(row).intValue();
		mTableModel.setValueAt(value, origRow, column);
		fireTableCellUpdated(row, column);
	}

	public void resetFilter() {
		setFilter(null, false, false);
	}

	public void setFilter(final String filterRegexp, boolean matchCase, boolean useRegex) {
		if(filterRegexp == null || "".equals(filterRegexp)){
			mFilterRegexp = null;
		}
		else{
			mFilterRegexp = matchCase ? filterRegexp : filterRegexp.toLowerCase();
		}
		this.matchCase = matchCase;
		//		System.out.println("Setting filter to '" + mFilterRegexp + "'");
		try {
			if(! useRegex || mFilterRegexp == null){
				mPattern = null;
			}
			else{
				mPattern = Pattern.compile(mFilterRegexp, matchCase ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
			}
			updateIndexArray();
			fireTableDataChanged();
		}
		catch (final PatternSyntaxException e) {
		}
	}

	private void updateIndexArray() {
		final ArrayList<Integer> newIndexArray = new ArrayList<Integer>();
		for (int i = 0; i < mTableModel.getRowCount(); i++) {
			if(mFilterRegexp == null){
				newIndexArray.add(new Integer(i));
				continue;
			}
			for(int nodeTextColumn : mNodeTextColumns){
				final TextHolder nodeContent = (TextHolder) mTableModel.getValueAt(i, nodeTextColumn);
				if(mPattern == null && (
						matchCase && nodeContent.toString().contains(mFilterRegexp)
						|| ! matchCase && nodeContent.toString().toLowerCase().contains(mFilterRegexp))
					|| mPattern != null && mPattern.matcher(nodeContent.toString()).find()
				) {
	                newIndexArray.add(new Integer(i));
	                break;
                }
			}
		}
		mIndexArray = newIndexArray;
	}
}
