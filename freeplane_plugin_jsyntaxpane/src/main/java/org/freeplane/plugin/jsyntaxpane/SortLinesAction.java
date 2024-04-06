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
import java.util.stream.Collectors;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import org.freeplane.core.util.LineComparator;

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
            boolean isEntireDocument = false;

            if (start == end) {
                // Apply to the whole document if no selection
                isEntireDocument = true;
                start = 0;
                end = sdoc.getLength();
            } else {
                // Adjust start to the beginning of the first selected line
                start = sdoc.getLineStartOffset(start);
                // Adjust end to include the newline character of the last selected line, if present
                end = sdoc.getLineEndOffset(end - 1);
            }

            String text = sdoc.getText(start, end - start);

            // Use the system's line separator value
            String lineSeparator = System.lineSeparator();

            // Split lines in a way that considers the system's line ending
            String[] lines = text.split("\\r?\\n");

            boolean containsEmptyOrWhitespaceOnlyLines = Arrays.stream(lines)
                    .anyMatch(line -> line.trim().isEmpty());

            String joinedText = Arrays.stream(lines)
                    .filter(line -> !line.trim().isEmpty()) // Remove empty or whitespace-only lines
                    .sorted(LineComparator::compareLinesParsingNumbers)
                    .collect(Collectors.joining(lineSeparator)); // Use the system's line separator for joining

            if (containsEmptyOrWhitespaceOnlyLines) {
                joinedText = lineSeparator + joinedText; // Add an empty line at the beginning if needed
            }


            // Correctly handling insertion to not add an extra line break at the end of the document
            if (isEntireDocument && !joinedText.endsWith(lineSeparator)) {
                joinedText += lineSeparator;
            }

            sdoc.replace(start, end - start, joinedText, null);
        } catch (BadLocationException ex) {
            Logger.getLogger(SortLinesAction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
