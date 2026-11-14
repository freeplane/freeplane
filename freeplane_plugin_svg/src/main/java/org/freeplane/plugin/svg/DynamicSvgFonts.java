// SPDX-License-Identifier: GPL-2.0-or-later
package org.freeplane.plugin.svg;

import java.awt.Font;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.fontbox.ttf.CmapLookup;
import org.apache.fontbox.ttf.OTFParser;
import org.apache.fontbox.ttf.OS2WindowsMetricsTable;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.fonts.EmbedFontInfo;
import org.apache.fop.fonts.FontAdder;
import org.apache.fop.fonts.FontCacheManagerFactory;
import org.apache.fop.fonts.FontDetectorFactory;
import org.apache.fop.fonts.FontManager;
import org.apache.pdfbox.io.RandomAccessReadBuffer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

final class DynamicSvgFonts {
    static final class Embedding {
        final JsonArray fontFaces;
        final List<String> warnings;

        Embedding(JsonArray fontFaces, List<String> warnings) {
            this.fontFaces = fontFaces.deepCopy();
            this.warnings = Collections.unmodifiableList(new ArrayList<>(new LinkedHashSet<>(warnings)));
        }
    }

    Embedding embed(Map<Font, String> textByFont, boolean enabled) {
        JsonArray faces = new JsonArray();
        List<String> warnings = new ArrayList<>();
        if (!enabled || textByFont.isEmpty())
            return new Embedding(faces, warnings);
        List<EmbedFontInfo> files;
        try {
            InternalResourceResolver resolver = ResourceResolverFactory.createDefaultInternalResourceResolver(URI.create("file:/"));
            FontManager manager = new FontManager(resolver, FontDetectorFactory.createDefault(),
                    FontCacheManagerFactory.createDisabled());
            files = new ArrayList<>();
            manager.autoDetectFonts(true, new FontAdder(manager, resolver, null), false, null, files);
        } catch (Exception failure) {
            for (Font font : textByFont.keySet())
                warnings.add(description(font) + ": font discovery failed");
            return new Embedding(faces, warnings);
        }
        Set<String> embedded = new HashSet<>();
        for (Map.Entry<Font, String> usage : textByFont.entrySet()) {
            Font font = usage.getKey();
            String reason = embedFace(font, usage.getValue(), files, faces, embedded);
            if (reason != null)
                warnings.add(description(font) + ": " + reason);
        }
        return new Embedding(faces, warnings);
    }

    private String embedFace(Font font, String text, List<EmbedFontInfo> files, JsonArray faces, Set<String> embedded) {
        String family = font.getFamily(Locale.ROOT);
        if (family.equalsIgnoreCase(Font.DIALOG) || family.equalsIgnoreCase(Font.DIALOG_INPUT)
                || family.equalsIgnoreCase(Font.SANS_SERIF) || family.equalsIgnoreCase(Font.SERIF)
                || family.equalsIgnoreCase(Font.MONOSPACED))
            return "logical/composite font has no resolved physical face";
        String reason = "matching physical font file was not found";
        Set<URI> inspected = new HashSet<>();
        for (EmbedFontInfo info : files) {
            if (info.getPostScriptName() != null && !font.getPSName().equals(info.getPostScriptName()))
                continue;
            URI uri = info.getEmbedURI();
            if (uri == null || !inspected.add(uri))
                continue;
            if (info.getSubFontName() != null || !"file".equalsIgnoreCase(uri.getScheme()) || uri.getFragment() != null) {
                reason = "font collection or unsupported font location";
                continue;
            }
            try {
                Path path = Paths.get(uri);
                byte[] bytes = Files.readAllBytes(path);
                if (bytes.length < 4 || !isStandalone(bytes)) {
                    reason = "unsupported font format or font collection";
                    continue;
                }
                boolean openType = bytes[0] == 'O';
                try (RandomAccessReadBuffer input = new RandomAccessReadBuffer(bytes);
                        TrueTypeFont physical = (openType ? new OTFParser() : new TTFParser()).parse(input)) {
                    if (physical.getNaming() == null || !font.getPSName().equals(physical.getNaming().getPostScriptName()))
                        continue;
                    OS2WindowsMetricsTable metrics = physical.getOS2Windows();
                    if (metrics == null)
                        return "physical font has no embedding/style metadata";
                    if ((metrics.getFsType() & OS2WindowsMetricsTable.FSTYPE_RESTRICTED) != 0)
                        return "font embedding is restricted";
                    if ((metrics.getFsType() & OS2WindowsMetricsTable.FSTYPE_BITMAP_ONLY) != 0)
                        return "font permits bitmap embedding only";
                    boolean italic = (metrics.getFsSelection() & 1) != 0;
                    int weight = metrics.getWeightClass();
                    if (weight < 1 || weight > 1000 || italic != font.isItalic() || (weight >= 600) != font.isBold())
                        return "requested style does not match the physical face";
                    CmapLookup cmap = physical.getUnicodeCmapLookup();
                    if (font.canDisplayUpTo(text) >= 0 || cmap == null
                            || text.codePoints().anyMatch(cp -> !Character.isISOControl(cp) && cmap.getGlyphId(cp) == 0))
                        return "text requires unresolved glyph substitution";
                    String key = family + "/" + italic + "/" + weight;
                    if (embedded.add(key)) {
                        JsonObject face = new JsonObject();
                        face.addProperty("family", font.getFamily());
                        face.addProperty("style", italic ? "italic" : "normal");
                        face.addProperty("weight", weight);
                        face.addProperty("format", openType ? "opentype" : "truetype");
                        face.addProperty("data", Base64.getEncoder().encodeToString(bytes));
                        faces.add(face);
                    }
                    return null;
                }
            } catch (IOException | IllegalArgumentException | SecurityException failure) {
                reason = "font file is inaccessible or cannot be read";
            }
        }
        return reason;
    }

    private boolean isStandalone(byte[] bytes) {
        return (bytes[0] == 0 && bytes[1] == 1 && bytes[2] == 0 && bytes[3] == 0)
                || (bytes[0] == 'O' && bytes[1] == 'T' && bytes[2] == 'T' && bytes[3] == 'O');
    }

    private String description(Font font) {
        return font.getFamily(Locale.ROOT) + " (" + (font.isBold() ? "bold" : "normal")
                + (font.isItalic() ? ", italic" : "") + ")";
    }
}
