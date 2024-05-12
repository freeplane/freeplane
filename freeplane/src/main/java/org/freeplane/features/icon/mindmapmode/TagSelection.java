/*
 * Created on 11 May 2024
 *
 * author dimitry
 */
package org.freeplane.features.icon.mindmapmode;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.stream.Stream;

public class TagSelection implements Transferable, ClipboardOwner {
    public static final DataFlavor tagFlavor = new DataFlavor("application/x-freeplane-tag; class=java.lang.String", "Freeplane Tags");

    private static final DataFlavor[] flavors = {
            tagFlavor,
            DataFlavor.stringFlavor
        };
    private final StringSelection tagSelectionDelegate;
    public TagSelection(String tagData) {
        tagSelectionDelegate = new StringSelection(tagData);
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return flavors.clone();
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return Stream.of(flavors).anyMatch(flavor::equals);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException,
            IOException {
            return tagSelectionDelegate.getTransferData(DataFlavor.stringFlavor);
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        tagSelectionDelegate.lostOwnership(clipboard, contents);
    }

}
