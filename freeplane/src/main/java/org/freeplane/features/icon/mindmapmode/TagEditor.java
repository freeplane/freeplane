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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.RootPaneContainer;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.ui.components.JRestrictedSizeScrollPane;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.core.util.collection.SortedComboBoxModel;
import org.freeplane.features.icon.Tag;
import org.freeplane.features.icon.Tags;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.text.mindmapmode.EditorHolder;
import org.freeplane.features.text.mindmapmode.SourceTextEditorUIConfigurator;


class TagEditor {

    static class TagEditorHolder extends EditorHolder {

        public TagEditorHolder(NodeModel node, Window window) {
            super(node, window);
        }

    }

	private static final String WIDTH_PROPERTY = "tagDialog.width";
    private static final String HEIGHT_PROPERTY = "tagDialog.height";

    private final NodeModel node;
    private String title;
    private MIconController iconController;
    private JEditorPane textEditorPane;
    private JDialog dialog;

	TagEditor(MIconController iconController, RootPaneContainer frame, NodeModel node){
        this.iconController = iconController;
        this.node = node;
        this.dialog = frame instanceof Frame ? new JDialog((Frame)frame, title, /*modal=*/true) : new JDialog((JDialog)frame, title, /*modal=*/true);
                final JButton okButton = new JButton();
        final JButton cancelButton = new JButton();
        final JCheckBox enterConfirms = new JCheckBox("", ResourceController.getResourceController()
            .getBooleanProperty("el__enter_confirms_by_default"));
        LabelAndMnemonicSetter.setLabelAndMnemonic(okButton, TextUtils.getRawText("ok"));
        LabelAndMnemonicSetter.setLabelAndMnemonic(cancelButton, TextUtils.getRawText("cancel"));
        LabelAndMnemonicSetter.setLabelAndMnemonic(enterConfirms, TextUtils.getRawText("enter_confirms"));
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                dialog.setVisible(false);
                submit();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                dialog.setVisible(false);
            }
        });
        final JPanel buttonPane = new JPanel();
        buttonPane.add(enterConfirms);
        buttonPane.add(okButton);
        buttonPane.add(cancelButton);
        buttonPane.setMaximumSize(new Dimension(1000, 20));
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        final Container contentPane = dialog.getContentPane();
        JRestrictedSizeScrollPane editorScrollPane = createScrollPane();
        textEditorPane = createTextEditorPane();
        editorScrollPane.setViewportView(textEditorPane);
        String tags = Tags.getTags(node).stream().map(Tag::getContent).collect(Collectors.joining("\n"));
        textEditorPane.setText(tags);
        enterConfirms.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                textEditorPane.requestFocus();
                ResourceController.getResourceController().setProperty("el__enter_confirms_by_default",
                    Boolean.toString(enterConfirms.isSelected()));
            }
        });
        textEditorPane.addKeyListener(new KeyListener() {
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
                            insertString("\n");
                            break;
                        }
                        dialog.setVisible(false);
                        submit();
                        break;
                    case KeyEvent.VK_TAB:
                        e.consume();
                        insertString("    ");
                        break;
                }
            }

            public void insertString(final String text) {
                try {
                    textEditorPane.getDocument().insertString(textEditorPane.getCaretPosition(), text, null);
                }
                catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void keyReleased(final KeyEvent e) {
            }

            @Override
            public void keyTyped(final KeyEvent e) {
            }
        });

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
                dialog.dispose();
            }
        });
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
        List<Tag> tags = Stream.of(textEditorPane.getText().split("\n"))
        .map(String::trim)
        .filter(s -> ! s.isEmpty())
        .map(Tag::new)
        .collect(Collectors.toList());
        iconController.setTags(node, tags);

    }

    private JRestrictedSizeScrollPane createScrollPane() {
        final JRestrictedSizeScrollPane scrollPane = new JRestrictedSizeScrollPane();
        UITools.setScrollbarIncrement(scrollPane);
        scrollPane.setMinimumSize(new Dimension(0, 60));
        return scrollPane;
    }

    private JEditorPane createTextEditorPane() {
        JEditorPane textEditor = new JEditorPane();
        textEditor.setContentType("text/tags");
        SortedComboBoxModel<Tag> knownTags = node.getMap().getIconRegistry().getTagsAsListModel();
        List<String> completions = StreamSupport.stream(knownTags.spliterator(), false)
        .map(Tag::getContent)
        .collect(Collectors.toList());
        Action action = textEditor.getActionMap().get("combo-completion");
        try {
            action.getClass().getMethod("setItems", List.class).invoke(action, completions);
        } catch (Exception e) {
            LogUtils.severe(e);
        }

        SourceTextEditorUIConfigurator.configureColors(textEditor);
        final String fontName = ResourceController.getResourceController().getProperty("groovy_editor_font");
        final int fontSize = ResourceController.getResourceController().getIntProperty("groovy_editor_font_size");
        final Font font = UITools.scaleUI(new Font(fontName, Font.PLAIN, fontSize));
        textEditor.setFont(font);
        return textEditor;
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

}