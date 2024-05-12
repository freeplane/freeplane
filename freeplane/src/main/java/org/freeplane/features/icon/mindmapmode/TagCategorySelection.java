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

public class TagCategorySelection implements Transferable, ClipboardOwner {

    public static DataFlavor flavor(Transferable t) {
        if(t.isDataFlavorSupported(tagCategoryFlavor))
            return tagCategoryFlavor;
        if(t.isDataFlavorSupported(tagFlavor))
            return tagFlavor;
        if(t.isDataFlavorSupported(stringFlavor))
            return stringFlavor;
        throw new IllegalArgumentException("No supported flavor found");
    }

    public static final DataFlavor tagCategoryFlavor = new DataFlavor("application/x-freeplane-tag-category; class=java.lang.String", "Freeplane Tag Categories");
    public static final DataFlavor tagFlavor = TagSelection.tagFlavor;
    public static final DataFlavor stringFlavor = DataFlavor.stringFlavor;

    private static final DataFlavor[] flavors = {
            tagCategoryFlavor,
            tagFlavor,
            stringFlavor
        };
    private final StringSelection tagCategorySelectionDelegate;
    private final StringSelection tagSelectionDelegate;
    public TagCategorySelection(String tagCategoryData, String tagData) {
        tagCategorySelectionDelegate = new StringSelection(tagCategoryData);
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
        if(flavor.equals(tagFlavor))
            return tagSelectionDelegate.getTransferData(stringFlavor);
        else
            return tagCategorySelectionDelegate.getTransferData(stringFlavor);
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        tagSelectionDelegate.lostOwnership(clipboard, contents);
        tagCategorySelectionDelegate.lostOwnership(clipboard, contents);
    }

}
