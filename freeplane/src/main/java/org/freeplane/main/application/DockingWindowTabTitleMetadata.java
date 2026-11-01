/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2026 Freeplane contributors
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 */
package org.freeplane.main.application;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

final class DockingWindowTabTitleMetadata {
    private static final int MAGIC = 0x46505454;
    private static final int VERSION = 1;
    private static final int MAX_ENTRY_COUNT = 10000;

    static final class Entry {
        final String path;
        final String windowSignature;
        final String title;

        Entry(String path, String windowSignature, String title) {
            this.path = path;
            this.windowSignature = windowSignature;
            this.title = title;
        }
    }

    private DockingWindowTabTitleMetadata() {
    }

    static String encode(Collection<Entry> entries) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            output.writeInt(MAGIC);
            output.writeInt(VERSION);
            output.writeInt(entries.size());
            for (Entry entry : entries) {
                output.writeUTF(entry.path);
                output.writeUTF(entry.windowSignature);
                output.writeUTF(entry.title);
            }
        }
        return Base64.getEncoder().encodeToString(bytes.toByteArray());
    }

    static List<Entry> decode(String encoded) throws IOException {
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(encoded);
        }
        catch (IllegalArgumentException e) {
            throw new IOException("Invalid docking tab-title metadata", e);
        }
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes))) {
            if (input.readInt() != MAGIC) {
                throw new IOException("Unknown docking tab-title metadata format");
            }
            if (input.readInt() != VERSION) {
                throw new IOException("Unsupported docking tab-title metadata version");
            }
            int entryCount = input.readInt();
            if (entryCount < 0 || entryCount > MAX_ENTRY_COUNT) {
                throw new IOException("Invalid docking tab-title metadata entry count");
            }
            List<Entry> entries = new ArrayList<Entry>(entryCount);
            for (int i = 0; i < entryCount; i++) {
                entries.add(new Entry(input.readUTF(), input.readUTF(), input.readUTF()));
            }
            if (input.read() != -1) {
                throw new IOException("Unexpected data after docking tab-title metadata");
            }
            return entries;
        }
    }
}
