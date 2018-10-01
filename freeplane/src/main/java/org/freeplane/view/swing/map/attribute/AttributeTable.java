/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
package org.freeplane.view.swing.map.attribute;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.util.EventObject;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.ui.components.JComboBoxWithBorder;
import org.freeplane.core.ui.components.TypedListCellRenderer;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.attribute.AttributeRegistry;
import org.freeplane.features.attribute.AttributeTableLayoutModel;
import org.freeplane.features.attribute.ColumnWidthChangeEvent;
import org.freeplane.features.attribute.IColumnWidthChangeListener;
import org.freeplane.features.edge.EdgeModel;
import org.freeplane.features.format.FormattedObject;
import org.freeplane.features.format.IFormattedObject;
import org.freeplane.features.format.PatternFormat;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.NodeStyleModel;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.mindmapmode.EditNodeBase;
import org.freeplane.features.text.mindmapmode.EditNodeBase.EditedComponent;
import org.freeplane.features.text.mindmapmode.EditNodeBase.IEditControl;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.features.ui.ViewController;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;


/**
 * @author Dimitry Polivaev
 */
class AttributeTable extends JTable implements IColumnWidthChangeListener {
	private static final String EDITING_STOPPED = AttributeTable.class.getName() + ".editingStopped";
	private static int CLICK_COUNT_TO_START = 2;

	private static final class TableHeaderRendererImpl implements TableCellRenderer {
		final private TableCellRenderer delegate;
		TableHeaderRendererImpl(TableCellRenderer renderer){
			this.delegate = renderer;
		}
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			final Component c = delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			final Container mapView = SwingUtilities.getAncestorOfClass(MapView.class, table);
			if(mapView != null)
				c.setBackground(mapView.getBackground());
			final int height = (int) (((AttributeTable)table).getZoom() * 6);
			final Dimension preferredSize = new Dimension(1, height);
			c.setPreferredSize(preferredSize);
			return c;
		}
	}
	@SuppressWarnings("serial")
	private static final class TableHeader extends JTableHeader {
		private TableHeader(TableColumnModel cm) {
			super(cm);
		}
		@Override
		protected TableCellRenderer createDefaultRenderer() {
			return new TableHeaderRendererImpl(super.createDefaultRenderer());
		}
	}

	static private class HeaderMouseListener extends MouseAdapter {
		@Override
		public void mouseReleased(final MouseEvent e) {
			final JTableHeader header = (JTableHeader) e.getSource();
			final AttributeTable table = (AttributeTable) header.getTable();
			final float zoom = table.attributeView.getMapView().getZoom();
			final AttributeTableModel model = (AttributeTableModel) table
			.getModel();
			for (int col = 0; col < table.getColumnCount(); col++) {
				final int modelColumnWidth = model.getColumnWidth(col).toBaseUnitsRounded();
				final int currentColumnWidth = Math.round(table.getColumnModel().getColumn(col).getWidth() / zoom);
				if (modelColumnWidth != currentColumnWidth) {
					model.setColumnWidth(col, LengthUnits.pixelsInPt(currentColumnWidth));
				}
			}
		}
	}

	private static GlobalFocusChangeListener globalFocusChangeListener;

	static {
		globalFocusChangeListener = new GlobalFocusChangeListener();
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("permanentFocusOwner", globalFocusChangeListener);
	}
	static private class GlobalFocusChangeListener implements PropertyChangeListener {
		AttributeTable selectedTable;


		private void focusGained(final Component source) {
			final AttributeTable newTable;
			if (source instanceof AttributeTable) {
				newTable = (AttributeTable) source;
			}
			else {
				newTable = (AttributeTable) SwingUtilities.getAncestorOfClass(AttributeTable.class, source);
			}
			if(newTable != null) {
				selectedTable = newTable;
				selectedTable.setSelectedCellTypeInfo();
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (selectedTable != null) {
							final Component newNodeViewInFocus = SwingUtilities.getAncestorOfClass(NodeView.class,
									selectedTable);
							if (newNodeViewInFocus != null) {
								final NodeView viewer = (NodeView) newNodeViewInFocus;
								if (viewer != viewer.getMap().getSelected()) {
									viewer.getMap().selectAsTheOnlyOneSelected(viewer, false);
								}
							}
						}
					}
				});
			}
		}

		private void focusLost(final Component oppositeComponent) {
			if (selectedTable == null || null == SwingUtilities.getAncestorOfClass(MapView.class, oppositeComponent)
					&& ! (oppositeComponent instanceof AttributeTable)) {
				return;
			}
			final Component newTable;
			if (oppositeComponent instanceof AttributeTable) {
				newTable = oppositeComponent;
			}
			else {
				newTable = SwingUtilities.getAncestorOfClass(AttributeTable.class, oppositeComponent);
			}
			if (selectedTable != newTable) {
				selectedTable.clearSelection();
				if (selectedTable.isEditing()) {
					selectedTable.getCellEditor().stopCellEditing();
				}
				if (!selectedTable.attributeView.isPopupShown()) {
					final AttributeView attributeView = selectedTable.getAttributeView();
					final String currentAttributeViewType = AttributeRegistry.getRegistry(
					    attributeView.getNode().getMap()).getAttributeViewType();
					if (attributeView.getViewType() != currentAttributeViewType) {
						attributeView.stateChanged(null);
					}
				}
				selectedTable = null;
				return;
			}
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			Component newFocusOwner = (Component) evt.getNewValue();
			if(selectedTable != null) {
				focusLost(newFocusOwner);
			}
			focusGained(newFocusOwner);
		}
	}

	static private MouseListener componentListener = new HeaderMouseListener();
	static private ComboBoxModel defaultComboBoxModel = null;
	static private AttributeTableCellRenderer dtcr = new AttributeTableCellRenderer();
	private static final int EXTRA_HEIGHT = 4;
	static private CursorUpdater cursorUpdater = new CursorUpdater();
	private static final int MAX_HEIGTH = 300;
	private static final int MAX_WIDTH = 300;
	private static final long serialVersionUID = 1L;
	private static final float TABLE_ROW_HEIGHT = 4;

	public static AttributeTable getSelectedTable(){
		return globalFocusChangeListener.selectedTable;
	}

	static ComboBoxModel getDefaultComboBoxModel() {
		if (AttributeTable.defaultComboBoxModel == null) {
			AttributeTable.defaultComboBoxModel = new DefaultComboBoxModel();
		}
		return AttributeTable.defaultComboBoxModel;
	}

	final private AttributeView attributeView;
	private int highRowIndex = 0;
	private static DefaultCellEditor dce;

	AttributeTable(final AttributeView attributeView) {
		super();
		this.attributeView = attributeView;
		addMouseListener(AttributeTable.cursorUpdater);
		addMouseMotionListener(AttributeTable.cursorUpdater);
		if (attributeView.getMapView().getModeController().canEdit()) {
			tableHeader.addMouseListener(AttributeTable.componentListener);
		}
		else {
			tableHeader.setResizingAllowed(false);
		}
		setModel(attributeView.getCurrentAttributeTableModel());
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		getTableHeader().setReorderingAllowed(false);
		setCellSelectionEnabled(true);
		getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);
		setShowGrid(true);
	}

	@Override
	protected JTableHeader createDefaultTableHeader() {
		return new TableHeader(columnModel);
	}

	private void changeSelectedRowHeight(final int rowIndex) {
		if (highRowIndex != rowIndex) {
			if (highRowIndex < getRowCount()) {
				final int h = getRowHeight(highRowIndex);
				setRowHeight(highRowIndex, h - AttributeTable.EXTRA_HEIGHT);
			}
			final int h = getRowHeight(rowIndex);
			setRowHeight(rowIndex, h + AttributeTable.EXTRA_HEIGHT);
			highRowIndex = rowIndex;
			assert highRowIndex >= 0;
		}
	}

	@Override
	public void changeSelection(int rowIndex, int columnIndex, final boolean toggle, final boolean extend) {
		final int rowCount = getRowCount();
		if (rowCount == 0) {
			return;
		}
		if (rowIndex >= rowCount) {
			rowIndex = 0;
			columnIndex = 0;
		}
		changeSelectedRowHeight(rowIndex);
		super.changeSelection(rowIndex, columnIndex, toggle, extend);
	}

	@Override
	public void columnWidthChanged(final ColumnWidthChangeEvent event) {
		final float zoom = getZoom();
		final int col = event.getColumnNumber();
		final AttributeTableLayoutModel layoutModel = (AttributeTableLayoutModel) event.getSource();
		final int width = layoutModel.getColumnWidth(col).toBaseUnitsRounded();
		getColumnModel().getColumn(col).setPreferredWidth((int) (width * zoom));
		final MapView map = attributeView.getMapView();
		final NodeModel node = attributeView.getNode();
		map.getModeController().getMapController().nodeChanged(node);
	}

	/**
	 * @return Returns the currentModel.
	 */
	public AttributeTableModel getAttributeTableModel() {
		return (AttributeTableModel) getModel();
	}

	public AttributeView getAttributeView() {
		return attributeView;
	}



	@Override
    public boolean editCellAt(int row, int column, EventObject e) {
		if(isEditing() && getCellEditor() instanceof DialogTableCellEditor){
			return false;
		}
		if(column == 1 && e instanceof MouseEvent){
			final MouseEvent me = (MouseEvent) e;
			final Object value = getValueAt(row, column);
			if(value instanceof URI){
				final URI uri = (URI) value;
				final Icon linkIcon = getLinkIcon(uri);
				final int xmax = linkIcon != null ? linkIcon.getIconWidth() : 0;
				final int x = me.getX() - getColumnModel().getColumn(0).getWidth();
				if(x < xmax){
					LinkController.getController().loadURL(attributeView.getNode(), new ActionEvent(me.getSource(), me.getID(), null), uri);
					return false;
				}
             }
		}
		putClientProperty("AttributeTable.EditEvent", e);
		try{
			if(super.editCellAt(row, column, e)){
				final TableCellEditor cellEditor = getCellEditor();
				if(isEditing() && cellEditor instanceof DialogTableCellEditor){
					((JComponent)editorComp).paintImmediately(0, 0, editorComp.getWidth(), editorComp.getHeight());
					((DialogTableCellEditor)cellEditor).startEditing();
					return false;
				}
				return true;
			}
			return false;
		}
		finally{
			putClientProperty("AttributeTable.EditEvent", null);
		}
    }

	Icon getLinkIcon(final URI uri) {
		NodeModel nodeModel = ((AttributeTableModel)getModel()).getNode();
	    final Icon linkIcon =  Controller.getCurrentModeController().getExtension(LinkController.class).getLinkIcon(uri, nodeModel);
	    return linkIcon;
    }

	@SuppressWarnings("serial")
    private class DialogTableCellEditor extends AbstractCellEditor implements TableCellEditor{

		final private IEditControl editControl;
		private Object value;
		private EditNodeBase editBase;
		public DialogTableCellEditor() {
			super();
			editControl = new IEditControl() {
				@Override
				public void split(String newText, int position) {
				}

				@Override
				public void ok(String newText) {
					value = newText;
					stopCellEditing();
				}

				@Override
				public void cancel() {
					stopCellEditing();
				}

				@Override
				public boolean canSplit() {
	                return false;
                }

				@Override
				public EditedComponent getEditType() {
	                return EditedComponent.TEXT;
                }
			};
        }

		public IEditControl getEditControl() {
        	return editControl;
        }

		public void setEditBase(EditNodeBase editBase) {
        	this.editBase = editBase;
        }

		@Override
		public Object getCellEditorValue() {
	        return value;
        }

		public void startEditing(){
			if(editBase == null){
				return;
			}
			final JFrame frame = (JFrame) JOptionPane.getFrameForComponent(AttributeTable.this);
			editBase.show(frame);
		}

		@Override
		public boolean isCellEditable(EventObject anEvent) {
			if (anEvent instanceof MouseEvent) {
				return ((MouseEvent)anEvent).getClickCount() >= CLICK_COUNT_TO_START;
			}
			return true;
		}
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
	        return new AttributeTableCellRenderer().getTableCellRendererComponent(table, value, true, true, row, column);
        }
	};

	@Override
	public TableCellEditor getCellEditor(final int row, final int col) {
		return getCellEditor(row, col, (EventObject) getClientProperty("AttributeTable.EditEvent"));
	}

	@SuppressWarnings("serial")
    public TableCellEditor getCellEditor(final int row, final int col, EventObject e) {
		if (dce != null) {
			dce.stopCellEditing();
		}
		if(col == 1){
			final MTextController textController = (MTextController) TextController.getController();
			if(e instanceof KeyEvent){
				final KeyEvent kev = (KeyEvent) e;
				textController.getEventQueue().setFirstEvent(kev);
			}
			final AttributeTableModel model = (AttributeTableModel) getModel();
			final String text = getValueForEdit(row, col);
			final DialogTableCellEditor dialogTableCellEditor = new DialogTableCellEditor();
			EditNodeBase base = textController.getEditNodeBase(model.getNode(), text, dialogTableCellEditor.getEditControl(), false);
			if(base != null){
				dialogTableCellEditor.setEditBase(base);
				return dialogTableCellEditor;
			}
		}
		final JComboBox comboBox;
		if (dce == null) {
			comboBox = new JComboBoxWithBorder(){

				// Workaround for bug introduced in Java 8: they use wrong component in DefaultCellEditor.EditorDelegate
				@Override
				public void actionPerformed(ActionEvent e) {
					if(e != null && e.getSource() == dce){
						super.actionPerformed(new ActionEvent(getEditor(), e.getID(), e.getActionCommand(),e.getWhen(),e.getModifiers()));
					}
					else
						super.actionPerformed(e);
				}

			};
			comboBox.setRenderer(new TypedListCellRenderer());
			dce = new DefaultCellEditor(comboBox) {
		        @Override
				public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
		            return super.getTableCellEditorComponent(table, ((AttributeTable)table).getValueForEdit(row, col), isSelected, row, col);
		        }
			};
			dce.setClickCountToStart(CLICK_COUNT_TO_START);
		}
		return dce;
	}

    private String getValueForEdit(final int row, final int col) {
        final Object value = getValueAt(row, col);
        return (value instanceof IFormattedObject ? ((IFormattedObject) value).getObject() : value).toString();
    }


	@Override
	public TableCellRenderer getCellRenderer(final int row, final int column) {
		return AttributeTable.dtcr;
	}

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		if (!isValid()) {
			validate();
		}
		final Dimension dimension = super.getPreferredSize();
		NodeView nodeView = (NodeView) SwingUtilities.getAncestorOfClass(NodeView.class, this);
		if(nodeView != null){
			final MapView map = nodeView.getMap();
			final ModeController modeController = map.getModeController();
			final NodeStyleController nsc = NodeStyleController.getController(modeController);
			dimension.width = Math.min(map.getZoomed(nsc.getMaxWidth(nodeView.getModel()).toBaseUnits()), dimension.width);
			dimension.height = Math.min(map.getZoomed(AttributeTable.MAX_HEIGTH) - getTableHeaderHeight(), dimension.height);
		}
		else{
			dimension.width = Math.min(MAX_WIDTH, dimension.width);
			dimension.height = Math.min(MAX_HEIGTH, dimension.height);
		}
		return dimension;
	}

	int getTableHeaderHeight() {
		final JTableHeader tableHeader = getTableHeader();
		return tableHeader != null ? tableHeader.getPreferredSize().height : 0;
	}

	float getZoom() {
        final MapView mapView = attributeView.getMapView();
	    if(SwingUtilities.isDescendingFrom(this, mapView)) {
            return mapView.getZoom();
        }
	    return 1f;
	}

	/**
	 */
	public void insertRow(final int row) {
		if (getModel() instanceof ExtendedAttributeTableModelDecorator) {
			final ExtendedAttributeTableModelDecorator model = (ExtendedAttributeTableModelDecorator) getModel();
			if (isEditing() && getCellEditor() != null && !getCellEditor().stopCellEditing()) {
				return;
			}
			model.insertRow(row);
			changeSelection(row, 0, false, false);
			if (editCellAt(row, 0)) {
				getEditorComponent().requestFocusInWindow();
			}
		}
	}

	@Override
	public boolean isVisible() {
		return super.isVisible() && attributeView.areAttributesVisible();
	}

	/**
	 */
	public void moveRowDown(final int row) {
		if (getModel() instanceof ExtendedAttributeTableModelDecorator && row < getRowCount() - 1) {
			final ExtendedAttributeTableModelDecorator model = (ExtendedAttributeTableModelDecorator) getModel();
			model.moveRowDown(row);
			changeSelection(row + 1, getSelectedColumn(), false, false);
		}
	}

	/**
	 */
	public void moveRowUp(final int row) {
		if (getModel() instanceof ExtendedAttributeTableModelDecorator && row > 0) {
			final ExtendedAttributeTableModelDecorator model = (ExtendedAttributeTableModelDecorator) getModel();
			model.moveRowUp(row);
			changeSelection(row - 1, getSelectedColumn(), false, false);
		}
	}

	@Override
	public Component prepareEditor(final TableCellEditor tce, final int row, final int col) {
		if(tce instanceof DialogTableCellEditor){
			return super.prepareEditor(tce, row, col);
		}
		final JComboBox comboBox = (JComboBox) ((DefaultCellEditor) tce).getComponent();
		final NodeModel node = getAttributeTableModel().getNode();
		final AttributeRegistry attributes = AttributeRegistry.getRegistry(node.getMap());
		final ComboBoxModel model;
		switch (col) {
			case 0:
				model = attributes.getComboBoxModel();
				comboBox.setEditable(!attributes.isRestricted());
				break;
			case 1:
				final String attrName = getAttributeTableModel().getValueAt(row, 0).toString();
				model = attributes.getDefaultComboBoxModel(attrName);
				comboBox.setEditable(!attributes.isRestricted(attrName));
				break;
			default:
				model = AttributeTable.getDefaultComboBoxModel();
		}
		final Object[] items = new Object[model.getSize()];
		for (int i = 0; i < items.length; i++) {
			items[i] = model.getElementAt(i);
		}
		final DefaultComboBoxModel currentModel = new DefaultComboBoxModel(items);
		comboBox.setModel(currentModel);
		updateComponentFontAndColors(comboBox);
		final JComponent editorComponent = (JComponent) comboBox.getEditor().getEditorComponent();
		updateComponentFontAndColors(editorComponent);
		editorComponent.setOpaque(true);
		final Font font = editorComponent.getFont();
		editorComponent.setFont(font.deriveFont(font.getSize2D() * getZoom()));
		return super.prepareEditor(tce, row, col);
	}

	@Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
	    Object value = getValueAt(row, column);

            boolean isSelected = false;
            boolean hasFocus = false;

            // Only indicate the selection and focused cell if not printing
            MapView map = (MapView) SwingUtilities.getAncestorOfClass(MapView.class, this);
            if (map == null || ! map.isPrinting()) {
                isSelected = isCellSelected(row, column);

                boolean rowIsLead =
                    (selectionModel.getLeadSelectionIndex() == row);
                boolean colIsLead =
                    (columnModel.getSelectionModel().getLeadSelectionIndex() == column);

                final Window windowAncestor = SwingUtilities.getWindowAncestor(this);
				hasFocus = (rowIsLead && colIsLead) && windowAncestor != null && equals(windowAncestor.getMostRecentFocusOwner());
            }

        return renderer.getTableCellRendererComponent(this, value,
                                                      isSelected, hasFocus,
                                                      row, column);
    }

	@Override
	protected boolean processKeyBinding(final KeyStroke ks, final KeyEvent e, final int condition, final boolean pressed) {
		if (ks.getKeyCode() == KeyEvent.VK_TAB && e.getModifiers() == 0 && pressed && getSelectedColumn() == 1
		        && getSelectedRow() == getRowCount() - 1 && getModel() instanceof ExtendedAttributeTableModelDecorator) {
			insertRow(getRowCount());
			return true;
		}
		if (ks.getKeyCode() == KeyEvent.VK_ESCAPE && e.getModifiers() == 0 && pressed) {
			if (! isEditing()){
				attributeView.getNodeView().requestFocusInWindow();
				return true;
			}
			else
				return super.processKeyBinding(ks, e, condition, pressed);

		}
		boolean retValue = super.processKeyBinding(ks, e, condition, pressed);
		if (!retValue && condition == JComponent.WHEN_FOCUSED && isFocusOwner() && ks.getKeyCode() != KeyEvent.VK_TAB
		        && e != null && e.getID() == KeyEvent.KEY_PRESSED && !e.isActionKey()
		        && e.getKeyChar() != KeyEvent.CHAR_UNDEFINED
		        && 0 == (e.getModifiers() & (InputEvent.CTRL_MASK | InputEvent.ALT_MASK))) {
			final int leadRow = getSelectionModel().getLeadSelectionIndex();
			final int leadColumn = getColumnModel().getSelectionModel().getLeadSelectionIndex();
			if (leadRow != -1 && leadColumn != -1 && !isEditing()) {
				if (!editCellAt(leadRow, leadColumn, e)) {
					return false;
				}
			}
			final Component editorComponent = getEditorComponent();
			if (editorComponent instanceof JComboBox) {
				final JComboBox comboBox = (JComboBox) editorComponent;
				if (comboBox.isEditable()) {
					final ComboBoxEditor editor = comboBox.getEditor();
					editor.selectAll();
					KeyEvent keyEv;
					keyEv = new KeyEvent(editor.getEditorComponent(), KeyEvent.KEY_TYPED, e.getWhen(),
					    e.getModifiers(), KeyEvent.VK_UNDEFINED, e.getKeyChar(), KeyEvent.KEY_LOCATION_UNKNOWN);
					retValue = SwingUtilities.processKeyBindings(keyEv);
				}
				else {
					editorComponent.requestFocusInWindow();
					retValue = true;
				}
			}
		}
		if (ks.getKeyCode() == KeyEvent.VK_SPACE) {
			return true;
		}
		return retValue;
	}

	@Override
	public void removeEditor() {
		final Component editorComponent = getEditorComponent();
		final Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		boolean requestFocus = editorComponent != null && focusOwner != null &&
		(focusOwner == editorComponent || SwingUtilities.isDescendingFrom(focusOwner, editorComponent));
		getAttributeTableModel().editingCanceled();
		final boolean focusCycleRoot = isFocusCycleRoot();
		setFocusCycleRoot(true);
		super.removeEditor();
		setFocusCycleRoot(focusCycleRoot);
		if(requestFocus)
			requestFocusInWindow();
	}

	/**
	 */
	public void removeRow(final int row) {
		if (getModel() instanceof ExtendedAttributeTableModelDecorator) {
			final ExtendedAttributeTableModelDecorator model = (ExtendedAttributeTableModelDecorator) getModel();
			model.removeRow(row);
			final int rowCount = getRowCount();
			if (row <= rowCount - 1) {
				changeSelection(row, getSelectedColumn(), false, false);
			}
			else if (rowCount >= 1) {
				changeSelection(row - 1, getSelectedColumn(), false, false);
			}
		}
	}

	@Override
	public void setModel(final TableModel dataModel) {
		super.setModel(dataModel);
	}

	/**
	 *
	 */
	public void setOptimalColumnWidths() {
		Component comp = null;
		int cellWidth = 0;
		int maxCellWidth = 2 * (int) (Math.ceil(getFont().getSize2D() / UITools.FONT_SCALE_FACTOR +  AttributeTable.TABLE_ROW_HEIGHT));
		int rowCount = getRowCount();
		if(rowCount > 0) {
			for (int col = 0; col < 2; col++) {
				for (int row = 0; row < rowCount; row++) {
					comp = AttributeTable.dtcr.getTableCellRendererComponent(this, getValueAt(row, col), false, false, row,
							col);
					cellWidth = comp.getPreferredSize().width;
					maxCellWidth = Math.max(cellWidth, maxCellWidth);
				}
				getAttributeTableModel().setColumnWidth(col, LengthUnits.pixelsInPt(maxCellWidth + 1));
			}
		}
	}

	@Override
	public void tableChanged(final TableModelEvent e) {
		if(isEditing() && null == getClientProperty(EDITING_STOPPED) ){
			removeEditor();
		}
		int selectedRow = getSelectedRow();
		super.tableChanged(e);
		if (getParent() == null) {
			return;
		}
			switch(e.getType())
			{
				case TableModelEvent.DELETE:
					if(selectedRow != -1 ){
						if(e.getFirstRow() <= selectedRow){
							if( e.getLastRow() >= selectedRow && e.getFirstRow() != 0) {
								changeSelection(e.getFirstRow() - 1, 0, false, false);
							}
							else if(e.getLastRow() < selectedRow){
								int rowIndex = selectedRow - (e.getLastRow() - e.getFirstRow() + 1);
								if(rowIndex < 0){
									rowIndex = 0;
								}
								if(rowIndex < getRowCount()){
									changeSelection(rowIndex , getSelectedColumn(), false, false);
								}
							}
						}
					}
					break;
				case TableModelEvent.INSERT:
					changeSelection(e.getFirstRow() , getSelectedColumn(), false, false);
					break;
				default:
					if(selectedRow > getRowCount() && getRowCount() > 0){
						changeSelection(getRowCount() - 1 , getSelectedColumn(), false, false);
					}
			}
		getParent().getParent().invalidate();
	}

	void updateAttributeTable() {
		updateComponentFontAndColors(this);
		updateGridColor();
		updateRowHeights();
		updateColumnWidths();
	}

	private void updateGridColor() {
		final NodeView nodeView = attributeView.getNodeView();
		if(! SwingUtilities.isDescendingFrom(this, nodeView))
			return;
		final MapView mapView = nodeView.getMap();
        final MapStyleModel model = MapStyleModel.getExtension(mapView.getModel());
        final NodeModel attributeStyleNode = model.getStyleNodeSafe(MapStyleModel.ATTRIBUTE_STYLE);
        final EdgeModel edge = EdgeModel.getModel(attributeStyleNode);
        if(edge != null){
        	final Color edgeColor = edge.getColor();
        	setGridAndBorderColor(edgeColor);
        }
        else
        	this.gridColor = null;
	}

	private Color gridColor = null;
	public void setGridAndBorderColor(Color gridColor) {
		this.gridColor = gridColor;
		if(gridColor != null) {
			if(! gridColor.equals(getGridColor())) {
				AttributeViewScrollPane scrollPane = (AttributeViewScrollPane) SwingUtilities.getAncestorOfClass(AttributeViewScrollPane.class, this);
				final Border border = BorderFactory.createLineBorder(gridColor);
				scrollPane.setBorder(border);
				scrollPane.setViewportBorder(border);
				super.setGridColor(gridColor);
			}
		}
	}

	private void updateColumnWidths() {
		final float zoom = getZoom();
		for (int i = 0; i < 2; i++) {
			final int width = (int) (getAttributeTableModel().getColumnWidth(i).toBaseUnitsRounded() * zoom);
			getColumnModel().getColumn(i).setPreferredWidth(width);
		}
	}

	private void updateComponentFontAndColors(final JComponent c) {
		final NodeView nodeView = attributeView.getNodeView();
		final MapView mapView = nodeView.getMap();
		final ModeController modeController = mapView.getModeController();
		final NodeStyleController style = modeController.getExtension(NodeStyleController.class);
        final MapStyleModel model = MapStyleModel.getExtension(mapView.getModel());
        final NodeModel attributeStyleNode = model.getStyleNodeSafe(MapStyleModel.ATTRIBUTE_STYLE);
        final Font font = style.getFont(attributeStyleNode);
        c.setFont(font.deriveFont(UITools.FONT_SCALE_FACTOR * font.getSize2D()));
        if(! SwingUtilities.isDescendingFrom(this, nodeView)) {
        	return;
        }
        final Color backgroundColor = NodeStyleModel.getBackgroundColor(attributeStyleNode);
        if(backgroundColor!= null) {
        	c.setOpaque(true);
			c.setBackground(backgroundColor);
		} else {
			c.setBackground(nodeView.getBackgroundColor());
			c.setOpaque(false);
		}
        c.setForeground(style.getColor(attributeStyleNode));
    }

	private void updateRowHeights() {
		if(! isDisplayable()){
			addHierarchyListener(new HierarchyListener() {
				@Override
				public void hierarchyChanged(HierarchyEvent e) {
					if(isDisplayable()){
						updateRowHeights();
						removeHierarchyListener(this);
					}
				}
			});
			return;
		}
		final int rowCount = getRowCount();
		if (rowCount == 0) {
			return;
		}
		final int constHeight = getTableHeaderHeight() + AttributeTable.EXTRA_HEIGHT;
		final float zoom = getZoom();
		final float fontSize = (float) getFont().getMaxCharBounds(((Graphics2D)getGraphics()).getFontRenderContext()).getHeight();
		final float tableRowHeight = fontSize + zoom * AttributeTable.TABLE_ROW_HEIGHT;
		int newHeight = (int) ((tableRowHeight * rowCount + (zoom - 1) * constHeight) / rowCount);
		if (newHeight < 1) {
			newHeight = 1;
		}
		final int highRowsNumber = (int) ((tableRowHeight - newHeight) * rowCount);
		for (int i = 0; i < highRowsNumber; i++) {
			setRowHeight(i, 1 + newHeight + (i == highRowIndex ? AttributeTable.EXTRA_HEIGHT : 0));
		}
		for (int i = highRowsNumber; i < rowCount; i++) {
			setRowHeight(i, newHeight + (i == highRowIndex ? AttributeTable.EXTRA_HEIGHT : 0));
		}
	}

	public void viewRemoved(NodeView nodeView) {
		getModel().removeTableModelListener(this);
	}

	@Override
	public void editingStopped(ChangeEvent e) {
		if(isEditing() && null == getClientProperty(EDITING_STOPPED) ) {
			try{
				putClientProperty(EDITING_STOPPED, Boolean.TRUE);
				// Take in the new value
				TableCellEditor editor = getCellEditor();
				if (editor != null) {
					final Object value = editor.getCellEditorValue();
					if (value != null) {
						final String pattern = extractPatternIfAvailable(getValueAt(editingRow, editingColumn));
						final Object newValue = enforceFormattedObjectForIdentityPattern(value, pattern);
						setValueAt(newValue, editingRow, editingColumn);
					}
					removeEditor();
				}
			}
			finally{
				putClientProperty(EDITING_STOPPED, null);
			}
		}
	}

    private String extractPatternIfAvailable(final Object oldValue) {
        return oldValue instanceof IFormattedObject ? ((IFormattedObject) oldValue).getPattern() : null;
    }

    // unfortunately we have to handle IDENTITY_PATTERN explicitely since (only) attributes
    // have no place for the format except for the value itself - so we need a FormattedObject here
    private Object enforceFormattedObjectForIdentityPattern(Object value, final String pattern) {
        final MTextController textController = (MTextController) TextController.getController();
        return PatternFormat.IDENTITY_PATTERN.equals(pattern) ? new FormattedObject(value, pattern) : textController
            .guessObjectOrURI(value, pattern);
    }

	@Override
    public void setValueAt(Object aValue, int row, int column) {
	    super.setValueAt(column == 0 ? aValue.toString() : aValue, row, column);
	    setSelectedCellTypeInfo();
    }

	@Override
    public void valueChanged(ListSelectionEvent e) {
	    super.valueChanged(e);
	    setSelectedCellTypeInfo();
    }



	@Override
    public void columnSelectionChanged(ListSelectionEvent e) {
	    super.columnSelectionChanged(e);
	    setSelectedCellTypeInfo();
    }

	private void setSelectedCellTypeInfo() {
		final int r = getSelectedRow();
		final int c = getSelectedColumn();
		if(r >= 0 && c >= 0){
			final Object value = getValueAt(r, c);
			final ViewController viewController = Controller.getCurrentController().getViewController();
			viewController.addObjectTypeInfo(value);
		}
    }

	@Override
	protected void paintComponent(Graphics g) {
		if(gridColor == null){
			final NodeView nodeView = attributeView.getNodeView();
			if(SwingUtilities.isDescendingFrom(this, nodeView))
				setGridAndBorderColor(nodeView.getMainView().getBorderColor());
		}
		super.paintComponent(g);
	}



}
