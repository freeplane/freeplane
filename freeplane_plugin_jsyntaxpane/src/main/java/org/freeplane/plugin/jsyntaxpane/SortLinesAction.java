/*
 * Created on 3 Apr 2024
 *
 * author dimitry
 */
package org.freeplane.plugin.jsyntaxpane;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import de.sciss.syntaxpane.SyntaxDocument;
import de.sciss.syntaxpane.actions.DefaultSyntaxAction;

public class SortLinesAction extends DefaultSyntaxAction {
    private static final long serialVersionUID = 1L;

    public SortLinesAction() {
        super("SORT_LINES");
    }

    @Override
    public void actionPerformed(JTextComponent target, SyntaxDocument sdoc, int dot, ActionEvent e) {
        try {
            int start = target.getSelectionStart();
            int end = target.getSelectionEnd();
            if (start == end) {
                // Apply to the whole document if no selection
                start = 0;
                end = sdoc.getLength();
            } else {
                // Adjust start and end to cover the full lines
                start = sdoc.getLineStartOffset(start);
                end = sdoc.getLineEndOffset(end - 1);
            }
            String text = sdoc.getText(start, end - start);

            String[] lines = text.split("\n");
            Arrays.sort(lines);

            sdoc.remove(start, end - start);
            sdoc.insertString(start, String.join("\n", lines), null);
        } catch (BadLocationException ex) {
            Logger.getLogger(SortLinesAction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
