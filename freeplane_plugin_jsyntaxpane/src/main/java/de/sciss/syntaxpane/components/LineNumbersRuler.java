/*
« * Copyright 2008 Ayman Al-Sairafi ayman.alsairafi@gmail.com
 * Copyright 2011-2022 Hanns Holger Rutz.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License
 *       at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.sciss.syntaxpane.components;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;

import de.sciss.syntaxpane.DefaultSyntaxKit;
import de.sciss.syntaxpane.SyntaxDocument;
import de.sciss.syntaxpane.SyntaxView;
import de.sciss.syntaxpane.actions.ActionUtils;
import de.sciss.syntaxpane.actions.gui.GotoLineDialog;
import de.sciss.syntaxpane.util.Configuration;

/**
 * This class will display line numbers for a related text component. The text
 * component must use the same line height for each line. 
 *
 * This class was designed to be used as a component added to the row header
 * of a JScrollPane.
 *
 * Original code from http://tips4java.wordpress.com/2009/05/23/text-component-line-number/
 *
 * @author Rob Camick
 *
 * Revised for de.sciss.syntaxpane
 * 
 * @author Ayman Al-Sairafi, Hanns Holger Rutz
 */
public class LineNumbersRuler extends JPanel
    implements CaretListener, DocumentListener, PropertyChangeListener, SyntaxComponent {

    public static final String PROPERTY_BACKGROUND = "LineNumbers.Background";
    public static final String PROPERTY_FOREGROUND = "LineNumbers.Foreground";
    public static final String PROPERTY_CURRENT_BACK = "LineNumbers.CurrentBack";
    public static final String PROPERTY_LEFT_MARGIN = "LineNumbers.LeftMargin";
    public static final String PROPERTY_RIGHT_MARGIN = "LineNumbers.RightMargin";
    public static final String PROPERTY_Y_OFFSET = "LineNumbers.YOFFset";
    public static final int DEFAULT_R_MARGIN = 5;
    public static final int DEFAULT_L_MARGIN = 5;
    private Status status;
    private final static int MAX_HEIGHT = 0x100000; // issue #36 - avoid overflow on HiDPI monitors
    //  Text component this TextTextLineNumber component is in sync with
    private JEditorPane editor;
    private int minimumDisplayDigits = 2;
    //  Keep history information to reduce the number of times the component
    //  needs to be repainted
    private int lastDigits;
    private int lastHeight;
    private int lastLine;
    private MouseListener mouseListener = null;
    // The formatting to use for displaying numbers.  Use in String.format(numbersFormat, line)
    private String numbersFormat = "%3d";

    private Color currentLineColor;

    private boolean isWordWrapEnabled;

    /**
     * Returns the JScrollPane that contains this EditorPane, or null if no
     * JScrollPane is the parent of this editor
     */
    public JScrollPane getScrollPane(JTextComponent editorPane) {
        Container p = editorPane.getParent();
        while (p != null) {
            if (p instanceof JScrollPane) {
                return (JScrollPane) p;
            }
            p = p.getParent();
        }
        return null;
    }

    @Override
    public void config(Configuration config) {
        int right = config.getInteger(PROPERTY_RIGHT_MARGIN, DEFAULT_R_MARGIN);
        int left  = config.getInteger(PROPERTY_LEFT_MARGIN , DEFAULT_L_MARGIN);
        Color foreground = config.getColor(PROPERTY_FOREGROUND, Color.BLACK);
        setForeground(foreground);
        Color back = config.getColor(PROPERTY_BACKGROUND, Color.WHITE);
        setBackground(back);
        setBorder(BorderFactory.createEmptyBorder(0, left, 0, right));
        currentLineColor = config.getColor(PROPERTY_CURRENT_BACK, back);
        isWordWrapEnabled = config.getBoolean(DefaultSyntaxKit.CONFIG_ENABLE_WORD_WRAP, false);
    }

    @Override
    public void install(final JEditorPane editor) {
        this.editor = editor;

        setFont(editor.getFont());

        // setMinimumDisplayDigits(3);
        Insets ein = editor.getInsets();
        if (ein.top != 0 || ein.bottom != 0) {
            Insets curr = getInsets();
            setBorder(BorderFactory.createEmptyBorder(ein.top, curr.left, ein.bottom, curr.right));
        }

        editor.getDocument().addDocumentListener(this);
        editor.addCaretListener(this);
        editor.addPropertyChangeListener(this);
        JScrollPane sp = getScrollPane(editor);
        if (sp != null) sp.setRowHeaderView(this);
        mouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                GotoLineDialog.showForEditor(editor);
            }
        };
        addMouseListener(mouseListener);
        setPreferredWidth(false);    // required for toggle-lines to correctly repaint
        status = Status.INSTALLING;
    }

    @Override
    public void deinstall(JEditorPane editor) {
        removeMouseListener(mouseListener);
        status = Status.DEINSTALLING;
        editor.getDocument().removeDocumentListener(this);
        editor.removeCaretListener(this);
        editor.removePropertyChangeListener(this);
        JScrollPane sp = getScrollPane(editor);
        if (sp != null) {
            sp.setRowHeaderView(null);
        }
    }

    /**
     *  Gets the minimum display digits
     *
     *  @return the minimum display digits
     */
    public int getMinimumDisplayDigits() {
        return minimumDisplayDigits;
    }

    /**
     *  Specify the minimum number of digits used to calculate the preferred
     *  width of the component. Default is 3.
     *
     *  @param minimumDisplayDigits  the number digits used in the preferred
     *                               width calculation
     */
    public void setMinimumDisplayDigits(int minimumDisplayDigits) {
        this.minimumDisplayDigits = minimumDisplayDigits;
        setPreferredWidth(false);
    }

    /**
     *  Calculate the width needed to display the maximum line number
     */
    private void setPreferredWidth(boolean force) {
        int lines  = ActionUtils.getLineCount(editor);
        int digits = Math.max(String.valueOf(lines).length(), minimumDisplayDigits);

        //  Update sizes when number of digits in the line number changes

        if (force || lastDigits != digits) {
            lastDigits = digits;
            numbersFormat = "%" + digits + "d";
            FontMetrics fontMetrics = getFontMetrics(getFont());
            int width = fontMetrics.charWidth('0') * digits;
            Insets insets = getInsets();
            int preferredWidth = insets.left + insets.right + width;

            Dimension d = getPreferredSize();
            d.setSize(preferredWidth, MAX_HEIGHT);
            setPreferredSize(d);
            setSize(d);
        }
    }

    /**
     *  Draw the line numbers
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        FontMetrics fontMetrics = getFontMetrics(getFont());
        Insets insets = getInsets();
        int currentLine = -1;
        try {
            currentLine = ActionUtils.getLineNumber(editor, editor.getCaretPosition());
        } catch (BadLocationException ex) {
            // this won't happen, even if it does, we can ignore it and we will not have
            // a current line to worry about...
        }

        SyntaxView.setRenderingHits((Graphics2D) g);

        Rectangle clip = g.getClip().getBounds();
        int lh = fontMetrics.getHeight();
        int topY = Math.max(clip.y / lh * lh, 0);
        int bottomY;
        if(isWordWrapEnabled) {
            bottomY = Math.min((clip.y + clip.height) / lh * lh, editor.getHeight()) + lh;
        }
        else {
            int topLine    = (int) (clip.getY() / lh);
            int maxLines = ActionUtils.getLineCount(editor);
            int bottomLine = Math.min(maxLines, (int) (clip.getHeight() + lh - 1) / lh + topLine + 1);
            bottomY = bottomLine * lh;
        }

        Point p = new Point();
        int pos = editor.viewToModel(p);
        int previousLine = getLineNumber(pos) - 1;
        for (int y = topY; y < bottomY; y+=lh) {
            int line;
            if(isWordWrapEnabled) {
                p.y = y;
                pos = editor.viewToModel(p);
                if(getY(pos) < y)
                    break;
                line = getLineNumber(pos);
            }
            else {
                line = previousLine + 1;
            }
            if (line == currentLine) {
                g.setColor(currentLineColor);
                g.fillRect(0, y /* - lh + fontMetrics.getDescent() - 1 */, getWidth(), lh);
                g.setColor(getForeground());
            }
            if(! isWordWrapEnabled || line > previousLine && (y > topY || pos == 0 || line > getLineNumber(pos - 1)) ) {
                previousLine = line;
                String lineNumber = String.format(numbersFormat, line + 1);
                int yt = y + fontMetrics.getAscent();
                g.drawString(lineNumber, insets.left, yt);
            }
        }
    }

    private int getY(int pos) {
        try {
            return editor.modelToView(pos).y;
        } catch (BadLocationException e) {
            return -1;
        }
    }


    private int getLineNumber(int pos){
        try {
            return ActionUtils.getLineNumber(editor, pos);
        } catch (BadLocationException e) {
            return -1;
        }
    }

//
//  Implement CaretListener interface
//
    @Override
    public void caretUpdate(CaretEvent e) {
        //  Get the line the caret is positioned on

        int caretPosition = editor.getCaretPosition();
        Element root = editor.getDocument().getDefaultRootElement();
        int currentLine = root.getElementIndex(caretPosition);

        //  Need to repaint so the correct line number can be highlighted

        if (lastLine != currentLine) {
            repaint();
            lastLine = currentLine;
        }
    }

//
//  Implement DocumentListener interface
//
    @Override
    public void changedUpdate(DocumentEvent e) {
        documentChanged();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        documentChanged();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        documentChanged();
    }

    /*
     *  A document change may affect the number of displayed lines of text.
     *  Therefore the lines numbers will also change.
     */
    private void documentChanged() {
        //  Preferred size of the component has not been updated at the time
        //  the DocumentEvent is fired

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                int preferredHeight = editor.getPreferredSize().height;

                //  Document change has caused a change in the number of lines.
                //  Repaint to reflect the new line numbers

                if (lastHeight != preferredHeight) {
                    setPreferredWidth(false);
                    repaint();
                    lastHeight = preferredHeight;
                }
            }
        });
    }

    /**
     * Implement PropertyChangeListener interface
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String prop = evt.getPropertyName();
        if (prop.equals("document")) {
            if (evt.getOldValue() instanceof SyntaxDocument) {
                SyntaxDocument syntaxDocument = (SyntaxDocument) evt.getOldValue();
                syntaxDocument.removeDocumentListener(this);
            }
            if (evt.getNewValue() instanceof SyntaxDocument && status.equals(Status.INSTALLING)) {
                SyntaxDocument syntaxDocument = (SyntaxDocument) evt.getNewValue();
                syntaxDocument.addDocumentListener(this);
                setPreferredWidth(false);
                repaint();
            }
        } else if (prop.equals("font") && evt.getNewValue() instanceof Font) {
            setFont((Font) evt.getNewValue());
            setPreferredWidth(true);
        }
        // TODO - theoretically also track "insets"
    }
}
