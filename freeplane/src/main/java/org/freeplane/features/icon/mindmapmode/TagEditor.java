/*
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
package org.freeplane.features.icon.mindmapmode;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.ComboBoxEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RootPaneContainer;
import javax.swing.TransferHandler;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.JColorButton;
import org.freeplane.core.ui.ColorTracker;
import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.ui.components.AutoResizedTable;
import org.freeplane.core.ui.components.JRestrictedSizeScrollPane;
import org.freeplane.core.ui.components.TagIcon;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.core.util.collection.SortedComboBoxModel;
import org.freeplane.features.icon.Tag;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.text.mindmapmode.EditorHolder;


class TagEditor {

    static class TagEditorHolder extends EditorHolder {

        public TagEditorHolder(NodeModel node, Window window) {
            super(node, window);
        }

    }

    private static class TagsWrapper extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        private final ArrayList<Tag> tags;


        public TagsWrapper(ArrayList<Tag> tags) {
            super();
            this.tags = tags;
        }

        public List<Tag> getTags() {
            return new ArrayList<>(tags);
        }

        public void insertEmptyTags(int first, int last) {
            for(int index = last; index >= first; index--)
                tags.add(first, Tag.EMPTY_TAG);
            fireTableRowsInserted(first, last);
         }

        public void deleteTags(int first, int last) {
            for(int index = last; index >= first; index--)
                tags.remove(index);
            fireTableRowsDeleted(first, last);
         }

        @Override
        public int getRowCount() {
            return tags.size() + 1;
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return rowIndex < tags.size() ? tags.get(rowIndex) : Tag.EMPTY_TAG;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if(columnIndex == 0) {
                Tag tag = (Tag)aValue;
                int tagCount = tags.size();
                if(rowIndex < tagCount) {
                    tags.set(rowIndex, tag);
                    fireTableCellUpdated(0, columnIndex);
                }
                else {
                    tags.add(tag);
                    fireTableRowsInserted(tagCount, tagCount);
                }
            }
        }

        @Override
        public String getColumnName(int column) {
            return "";
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        public void sortTags(int first, int last) {
            if(first == 0 && last + 1 == tags.size())
                Collections.sort(tags);
            else
                Collections.sort(tags.subList(first, last + 1));
            fireTableRowsUpdated(first, last);
        }

        public void insertRow(int index, Tag tag) {
            tags.add(index, tag);
            fireTableRowsInserted(index, index);
        }
    }

    @SuppressWarnings("serial")
    class TableCellTransferHandler extends TransferHandler {
        @Override
        public int getSourceActions(JComponent c) {
            return COPY_OR_MOVE;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            JTable table = (JTable) c;
            List<String> values = new ArrayList<>();
            int[] rows = table.getSelectedRows();
            int col = table.getSelectedColumn();

            for (int row : rows) {
                Object cellValue = table.getValueAt(row, col);
                if (cellValue instanceof Tag) {
                    values.add(((Tag) cellValue).getContent());
                } else if (cellValue != null) {
                    values.add(cellValue.toString());
                } else {
                    values.add("");
                }
            }
            return new StringSelection(String.join("\n", values));
        }

        @Override
        public boolean canImport(TransferHandler.TransferSupport info) {
            return info.isDataFlavorSupported(DataFlavor.stringFlavor);
        }

        @Override
        public boolean importData(TransferHandler.TransferSupport info) {
            if (!canImport(info)) {
                return false;
            }

            String data;
            try {
                data = (String) info.getTransferable().getTransferData(DataFlavor.stringFlavor);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            JTable target = (JTable) info.getComponent();
            if (info.isDrop()) {
                JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
                int index = dl.getRow();
                insertData(target, data, index);
            } else {
                int index = target.getSelectedRow();
                if (index >= 0) {
                    insertData(target, data, index);
                } else {
                    return false;
                }
            }
            return true;
        }

        private void insertData(JTable target, String data, int index) {
            TableModel model = target.getModel();
            String[] rows = data.split("\n");
            for (String row : rows)
                ((TagsWrapper) model).insertRow(index++, createTag(row));
        }

        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {
            super.exportDone(source, data, action);
            if (action == MOVE) {
                deleteTags();
            }
        }
    }

    private static final String WIDTH_PROPERTY = "tagDialog.width";
    private static final String HEIGHT_PROPERTY = "tagDialog.height";

    private final NodeModel node;
    private String title;
    private MIconController iconController;
    private JTable tagTable;
    private JDialog dialog;
    private List<Tag> originalTags;
    private final Map<String, Tag> newTags;
    private JColorButton colorButton;

	TagEditor(MIconController iconController, RootPaneContainer frame, NodeModel node){
        this.iconController = iconController;
        this.node = node;
        this.dialog = frame instanceof Frame ? new JDialog((Frame)frame, title, /*modal=*/true) : new JDialog((JDialog)frame, title, /*modal=*/true);
        final JButton okButton = new JButton();
        final JButton cancelButton = new JButton();
        final JButton sortButton = new JButton();
        colorButton = new JColorButton();
        colorButton.setColor(Tag.EMPTY_TAG.getIconColor());
        final JCheckBox enterConfirms = new JCheckBox("", ResourceController.getResourceController()
            .getBooleanProperty("el__enter_confirms_by_default"));
        LabelAndMnemonicSetter.setLabelAndMnemonic(okButton, TextUtils.getRawText("ok"));
        LabelAndMnemonicSetter.setLabelAndMnemonic(cancelButton, TextUtils.getRawText("cancel"));
        LabelAndMnemonicSetter.setLabelAndMnemonic(sortButton, TextUtils.getRawText("sort"));
        LabelAndMnemonicSetter.setLabelAndMnemonic(enterConfirms, TextUtils.getRawText("enter_confirms"));
        colorButton.setEnabled(false);
        okButton.addActionListener(e -> {
            dialog.setVisible(false);
            submit();
        });
        cancelButton.addActionListener(e -> dialog.setVisible(false));
        sortButton.addActionListener(e -> sortSelectedTags());

        colorButton.addActionListener(e -> modifyTagColor());
        final JPanel buttonPane = new JPanel();
        buttonPane.add(enterConfirms);
        buttonPane.add(okButton);
        buttonPane.add(cancelButton);
        buttonPane.add(colorButton);
        buttonPane.add(sortButton);
        buttonPane.setMaximumSize(new Dimension(1000, 20));
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        final Container contentPane = dialog.getContentPane();
        JRestrictedSizeScrollPane editorScrollPane = createScrollPane();
        newTags = new HashMap<>();
        originalTags = iconController.getTags(node).stream()
                .map(tag -> newTags.computeIfAbsent(tag.getContent(), x -> tag.copy()))
                .collect(Collectors.toList());
        tagTable = createTagTable(originalTags);
        editorScrollPane.setViewportView(tagTable);
        editorScrollPane.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                tagTable.revalidate();
                tagTable.repaint();
            }

        });
        enterConfirms.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                tagTable.requestFocus();
                ResourceController.getResourceController().setProperty("el__enter_confirms_by_default",
                    Boolean.toString(enterConfirms.isSelected()));
            }
        });
        tagTable.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(final KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ESCAPE:
                        e.consume();
                        dialog.setVisible(false);
                        break;
                    case KeyEvent.VK_ENTER:
                        e.consume();
                        if ((e.getModifiers() & InputEvent.SHIFT_MASK) != 0
                                || enterConfirms.isSelected() == ((e.getModifiers() & InputEvent.ALT_MASK) != 0)) {
                            insertTags();
                            break;
                        }
                        dialog.setVisible(false);
                        submit();
                        break;
                    case KeyEvent.VK_DELETE:
                    case KeyEvent.VK_BACK_SPACE:
                        e.consume();
                        deleteTags();
                        break;
                }
            }

            private void insertTags() {
                ListSelectionModel selectionModel = tagTable.getSelectionModel();
                int minSelectedRow = selectionModel.getMinSelectionIndex();
                if(minSelectedRow >= 0) {
                    getTableModel().insertEmptyTags(minSelectedRow, selectionModel.getMaxSelectionIndex());
                }
            }

            @Override
            public void keyReleased(final KeyEvent e) {
            }

            @Override
            public void keyTyped(final KeyEvent e) {
            }
        });

        tagTable.getSelectionModel().addListSelectionListener(this::updateColorButton);
        tagTable.getModel().addTableModelListener(this::selectInsertedRows);

        contentPane.add(editorScrollPane, BorderLayout.CENTER);
        final boolean areButtonsAtTheTop = ResourceController.getResourceController().getBooleanProperty("el__buttons_above");
        contentPane.add(buttonPane, areButtonsAtTheTop ? BorderLayout.NORTH : BorderLayout.SOUTH);
        if (title == null) {
            title = TextUtils.getText("edit_long_node");
        }
        node.addExtension(new TagEditorHolder(node, dialog));
        configureDialog(dialog);
        restoreDialogSize(dialog);
        dialog.pack();
        dialog.addComponentListener(new ComponentListener() {
            @Override
            public void componentShown(final ComponentEvent e) {
            }

            @Override
            public void componentResized(final ComponentEvent e) {
                saveDialogSize(dialog);
            }

            @Override
            public void componentMoved(final ComponentEvent e) {
            }

            @Override
            public void componentHidden(final ComponentEvent e) {
                node.removeExtension(TagEditorHolder.class);
                dialog.dispose();
            }
        });

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                if (dialog.isVisible()) {
                    confirmedSubmit();
                }
            }
        });
    }
    private void modifyTagColor() {
        int selectedRow = tagTable.getSelectedRow();
        if(selectedRow < 0)
            return;
        Tag tag = (Tag) tagTable.getValueAt(selectedRow, 0);
        if(tag.isEmpty())
            return;

        Color defaultColor = new Color(tag.getDefaultColor().getRGB(), true);
        Color initialColor = tag.getIconColor();
        final Color result = ColorTracker.showCommonJColorChooserDialog(tagTable, tag.getContent(),
                initialColor, defaultColor);
        if(result != null && ! initialColor.equals(result) || result == defaultColor){
            Optional<Color> newColor = result == defaultColor ? Optional.empty() : Optional.of(result);
            tag.setColor(newColor);
            TagsWrapper tableModel = getTableModel();
            IntStream.range(0, tagTable.getRowCount() - 1)
            .filter(i -> tagTable.getValueAt(i, 0) ==tag)
            .forEach(i -> tableModel.fireTableCellUpdated(i, 0));
            updateColorButton();
        }

    }

    protected void sortSelectedTags() {
        ListSelectionModel selectionModel = tagTable.getSelectionModel();
        if(selectionModel.getMinSelectionIndex() < selectionModel.getMaxSelectionIndex())
            getTableModel().sortTags(selectionModel.getMinSelectionIndex(), selectionModel.getMaxSelectionIndex());
        else
            getTableModel().sortTags(0, tagTable.getRowCount() - 2);

    }
    void show() {
        Controller.getCurrentModeController().getController().getMapViewManager().scrollNodeToVisible(node);
        if (ResourceController.getResourceController().getBooleanProperty("el__position_window_below_node")) {
            UITools.setDialogLocationUnder(dialog, node);
        }
        else {
            UITools.setDialogLocationRelativeTo(dialog, node);
        }
        dialog.setVisible(true);
    }
    protected void submit() {
        List<Tag> tags = getCurrentTags();
        iconController.setTags(node, tags, true);

    }
    private List<Tag> getCurrentTags() {
        return getTableModel().getTags();
    }

    private JRestrictedSizeScrollPane createScrollPane() {
        final JRestrictedSizeScrollPane scrollPane = new JRestrictedSizeScrollPane();
        UITools.setScrollbarIncrement(scrollPane);
        scrollPane.setMinimumSize(new Dimension(0, 60));
        return scrollPane;
    }
    private Tag createTag(String string) {
        return node.getMap().getIconRegistry().createTag(string);
    }

    private JTable createTagTable(List<Tag> tags) {
        JTable table = new AutoResizedTable(new TagsWrapper(new ArrayList<>(tags))) {

            @Override
            public boolean editCellAt(int row, int column, EventObject e) {
                if(super.editCellAt(row, column, e)){
                    final Component editorComponent = getEditorComponent();
                    if (editorComponent instanceof JComboBox) {
                        final ComboBoxEditor editor = ((JComboBox)editorComponent).getEditor();
                        Component textField = editor.getEditorComponent();
                        if(textField instanceof JTextField)
                            ((JTextField)textField).selectAll();
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void editingStopped(ChangeEvent e) {
                 super.editingStopped(e);

            }

            @Override
            public void editingCanceled(ChangeEvent e) {
                 super.editingCanceled(e);

            }



        };
        table.setTransferHandler(new TableCellTransferHandler());
        table.setDragEnabled(true);
        table.setDropMode(DropMode.ON);
        table.setFont(iconController.getTagFont(node));
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void setValue(Object value) {
                if(value == null)
                    setIcon(null);
                else {
                    Tag tag = value instanceof Tag ? (Tag)value : createTag(value.toString());
                    setIcon(new TagIcon(tag, table.getFont()));
                }
            }

        });

        SortedComboBoxModel<Tag> knownTags = node.getMap().getIconRegistry().getTagsAsListModel();
        JComboBox<Tag> cellEditorComponent = new JComboBox<>(knownTags);
        cellEditorComponent.setEditable(true);
        DefaultCellEditor cellEditor = new DefaultCellEditor(cellEditorComponent) {
            private static final long serialVersionUID = 1L;

            @Override
            public Object getCellEditorValue() {
                Object value = super.getCellEditorValue();
                if(value instanceof Tag)
                    return value;
                else
                    return newTags.computeIfAbsent(value.toString(),
                            content -> node.getMap().getIconRegistry().createTag(content).copy());
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value,
                    boolean isSelected, int row, int column) {
                 return super.getTableCellEditorComponent(table, value, isSelected, row, column);
            }

            @Override
            public boolean isCellEditable(EventObject anEvent) {
               if(anEvent instanceof MouseEvent)
                   return super.isCellEditable(anEvent);
               else if(anEvent instanceof KeyEvent) {
                KeyEvent keyEvent = (KeyEvent)anEvent;
                return ! keyEvent.isControlDown() && ! keyEvent.isMetaDown()
                        && (keyEvent.getKeyChar() != KeyEvent.CHAR_UNDEFINED || keyEvent.getKeyCode() == KeyEvent.VK_F2);
            } else
                   return false;
            }
        };
        cellEditor.setClickCountToStart(2);
        table.getColumnModel().getColumn(0).setCellEditor(cellEditor);

        return table;
    }

	private void configureDialog(JDialog dialog) {
		dialog.setModal(false);
	}

	private void saveDialogSize(final JDialog dialog) {
        ResourceController resourceController = ResourceController.getResourceController();
        resourceController.setProperty(WIDTH_PROPERTY, dialog.getWidth());
        resourceController.setProperty(HEIGHT_PROPERTY, dialog.getHeight());
    }

	private void restoreDialogSize(final JDialog dialog) {
        Dimension preferredSize = dialog.getPreferredSize();
        ResourceController resourceController = ResourceController.getResourceController();
        preferredSize.width = Math.max(preferredSize.width, resourceController.getIntProperty(WIDTH_PROPERTY, 0));
        preferredSize.height = Math.max(preferredSize.height, resourceController.getIntProperty(HEIGHT_PROPERTY, 0));
        dialog.setPreferredSize(preferredSize);
    }

    private void confirmedSubmit() {
        if (dialog.isVisible()) {
            if (!originalTags.equals(getCurrentTags())) {
                final int action = JOptionPane.showConfirmDialog(dialog, TextUtils.getText("long_node_changed_submit"), "",
                    JOptionPane.YES_NO_CANCEL_OPTION);

                if (action == JOptionPane.YES_OPTION) {
                    submit();
                }
                else if (action == JOptionPane.CANCEL_OPTION) {
                    return;
                }
            }
            dialog.setVisible(false);
        }
    }

    private TagsWrapper getTableModel() {
        return (TagsWrapper)tagTable.getModel();
    }

    private void updateColorButton(ListSelectionEvent e) {
        if(!e.getValueIsAdjusting())
            updateColorButton();
    }

    private void selectInsertedRows(TableModelEvent e) {
        if(e.getType() == TableModelEvent.INSERT)
            EventQueue.invokeLater(() ->
                tagTable.getSelectionModel().setSelectionInterval(e.getFirstRow(), e.getLastRow()));
    }

    private void deleteTags() {
        ListSelectionModel selectionModel = tagTable.getSelectionModel();
        int minSelectedRow = selectionModel.getMinSelectionIndex();
        if(minSelectedRow >= 0) {
            getTableModel().deleteTags(minSelectedRow, selectionModel.getMaxSelectionIndex());
        }
    }

    private void updateColorButton() {
        int firstIndex = tagTable.getSelectedRow();
        if(firstIndex == -1) {
            return;
        }
        Tag tag = (Tag) tagTable.getValueAt(firstIndex, 0);
        if(tag.isEmpty()) {
            colorButton.setEnabled(false);
            colorButton.setColor(Tag.EMPTY_TAG.getIconColor());
            return;
        }
        colorButton.setEnabled(true);
        colorButton.setColor(tag.getIconColor());
    }
}