package org.freeplane.plugin.svg;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import java.awt.Font;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.fop.fonts.EmbedFontInfo;
import org.apache.fop.fonts.FontDetector;
import org.apache.fop.fonts.FontDetectorFactory;
import org.apache.fop.fonts.FontUris;
import org.freeplane.view.swing.map.DynamicSvgTestMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.MockedStatic;

public class DynamicSvgFontsTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();
    private byte[] gentium;
    private Font font;

    @Before
    public void readLicensedFont() throws Exception {
        gentium = DynamicSvgTestMap.gentiumBytes();
        font = Font.createFont(Font.TRUETYPE_FONT, new java.io.ByteArrayInputStream(gentium)).deriveFont(12f);
    }

    @Test
    public void embedsMatchingStandaloneFonts() throws Exception {
        Path file = temporary.newFile("gentium.ttf").toPath();
        Files.write(file, gentium);
        Map<Font, String> usage = new LinkedHashMap<>();
        usage.put(font, "alpha");
        usage.put(font.deriveFont(18f), "beta");

        DynamicSvgFonts.Embedding embedding = embed(usage, file.toUri());

        assertThat(embedding.warnings).isEmpty();
        assertThat(embedding.fontFaces).hasSize(1);
        com.google.gson.JsonObject face = embedding.fontFaces.get(0).getAsJsonObject();
        assertThat(face.get("family").getAsString()).isEqualTo(font.getFamily());
        assertThat(face.get("style").getAsString()).isEqualTo("normal");
        assertThat(face.get("weight").getAsInt()).isEqualTo(400);
        assertThat(Base64.getDecoder().decode(face.get("data").getAsString())).isEqualTo(gentium);
    }

    @Test
    public void reportsFontsThatCannotBeEmbedded() throws Exception {
        Map<Integer, String> restricted = new LinkedHashMap<>();
        restricted.put(2, "restricted");
        restricted.put(512, "bitmap");
        for (Map.Entry<Integer, String> entry : restricted.entrySet()) {
            Path file = temporary.newFile("restricted-" + entry.getKey() + ".ttf").toPath();
            Files.write(file, withEmbeddingFlags(entry.getKey()));
            DynamicSvgFonts.Embedding embedding = embed(Collections.singletonMap(font, "Hello"), file.toUri());
            assertThat(embedding.fontFaces).isEmpty();
            assertThat(embedding.warnings).singleElement().asString().contains(font.getFamily(), entry.getValue());
        }
        Path unsupported = temporary.newFile("collection.ttc").toPath();
        Files.write(unsupported, "ttcfunsupported".getBytes(StandardCharsets.US_ASCII));
        assertThat(embed(Collections.singletonMap(font, "Hello"), unsupported.toUri()).warnings)
                .singleElement().asString().contains("unsupported font format");
        assertThat(embed(Collections.singletonMap(font, "Hello"), temporary.getRoot().toPath().resolve("missing.ttf").toUri()).warnings)
                .singleElement().asString().contains("inaccessible");
        Path file = temporary.newFile("gentium.ttf").toPath();
        Files.write(file, gentium);
        assertThat(embed(Collections.singletonMap(font, "\uD83E\uDD84"), file.toUri()).warnings)
                .singleElement().asString().contains("glyph substitution");
        assertThat(embed(Collections.singletonMap(new Font(Font.SANS_SERIF, Font.PLAIN, 12), "Hello"), file.toUri()).warnings)
                .singleElement().asString().contains("logical/composite");
        assertThat(embed(Collections.singletonMap(font, "Hello"), null).warnings)
                .singleElement().asString().contains("not found");
    }

    @Test
    public void embedsWholeFontsWhenSubsettingIsForbidden() throws Exception {
        byte[] complete = withEmbeddingFlags(256);
        Path file = temporary.newFile("whole-font.ttf").toPath();
        Files.write(file, complete);

        DynamicSvgFonts.Embedding embedding = embed(Collections.singletonMap(font, "alpha"), file.toUri());

        assertThat(embedding.warnings).isEmpty();
        assertThat(Base64.getDecoder().decode(embedding.fontFaces.get(0).getAsJsonObject().get("data").getAsString()))
                .isEqualTo(complete);
    }

    @Test
    public void doesNotDiscoverFontsWhenEmbeddingIsDisabled() {
        try (MockedStatic<FontDetectorFactory> detector = mockStatic(FontDetectorFactory.class)) {
            DynamicSvgFonts.Embedding embedding = new DynamicSvgFonts().embed(Collections.singletonMap(font, "Hello"), false);

            assertThat(embedding.fontFaces).isEmpty();
            assertThat(embedding.warnings).isEmpty();
            detector.verifyNoInteractions();
        }
    }

    private DynamicSvgFonts.Embedding embed(Map<Font, String> usage, URI file) throws Exception {
        FontDetector detector = mock(FontDetector.class);
        doAnswer(call -> {
            List<EmbedFontInfo> files = call.getArgument(4);
            if (file != null) {
                EmbedFontInfo info = new EmbedFontInfo(new FontUris(file, null), false, false, Collections.emptyList(), null);
                info.setPostScriptName(font.getPSName());
                files.add(info);
            }
            return null;
        }).when(detector).detect(any(), any(), anyBoolean(), any(), any());
        try (MockedStatic<FontDetectorFactory> factory = mockStatic(FontDetectorFactory.class)) {
            factory.when(FontDetectorFactory::createDefault).thenReturn(detector);
            return new DynamicSvgFonts().embed(usage, true);
        }
    }

    private byte[] withEmbeddingFlags(int flags) {
        byte[] bytes = Arrays.copyOf(gentium, gentium.length);
        ByteBuffer data = ByteBuffer.wrap(bytes);
        int count = data.getShort(4) & 0xffff;
        for (int index = 0; index < count; index++) {
            int record = 12 + index * 16;
            if (data.getInt(record) == 0x4f532f32) {
                int offset = data.getInt(record + 8);
                data.putShort(offset + 8, (short) flags);
                return bytes;
            }
        }
        throw new AssertionError("Gentium has no OS/2 table");
    }
}
