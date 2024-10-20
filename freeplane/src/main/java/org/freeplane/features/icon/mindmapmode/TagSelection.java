/*
 * Created on 11 May 2024
 *
 * author dimitry
 */
package org.freeplane.features.icon.mindmapmode;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.UUID;
import java.util.stream.Stream;

public class TagSelection implements Transferable {
    public static final DataFlavor tagFlavor = new DataFlavor("application/x-freeplane-tag; class=java.lang.String", "Freeplane Tags");

    private static final int UUID_LENGTH = 36;

    private static final int TRANSFERABLE_ID_LENGTH = TagSelection.UUID_LENGTH + System.lineSeparator().length();

    public static String getTransferContent(String transferData) {
        return transferData.substring(TRANSFERABLE_ID_LENGTH);
    }

    public static String getTransferId(String transferData) {
        return transferData.substring(0, TagSelection.TRANSFERABLE_ID_LENGTH);
    }

    private static final DataFlavor[] flavors = {
            tagFlavor,
            DataFlavor.stringFlavor
        };

    private final String id;
    private final String tagSelection;

    public TagSelection(UUID uuid, String tagData) {
        this.id = uuid.toString();
        tagSelection = tagData;
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
            return id + System.lineSeparator() + tagSelection;
        else
            return tagSelection;
    }
}
