package org.freeplane.plugin.svg;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.view.swing.map.DynamicSvgTestMap;
import org.freeplane.view.swing.map.MapViewExportCapture;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DynamicSvgDocumentTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    @Test
    public void writesOneSelfContainedUtf8HtmlFile() throws Exception {
        try (DynamicSvgTestMap map = new DynamicSvgTestMap()) {
            NodeModel child = map.add(map.model.getRootNode(), "alpha \u03b1", false);
            map.attach();
            DynamicSvgDocument document = map.onEdt(() -> new MapViewExportCapture(map.view).capture(new ExportDynamicSvg()::capture));
            Path output = temporary.getRoot().toPath().resolve("map.html");
            byte[] font = DynamicSvgTestMap.gentiumBytes();
            JsonObject face = new JsonObject();
            face.addProperty("family", "Gentium"); face.addProperty("style", "normal"); face.addProperty("weight", 400);
            face.addProperty("format", "truetype"); face.addProperty("data", Base64.getEncoder().encodeToString(font));
            com.google.gson.JsonArray faces = new com.google.gson.JsonArray(); faces.add(face);

            document.write(output, new DynamicSvgFonts.Embedding(faces, Collections.emptyList()));

            String html = new String(Files.readAllBytes(output), StandardCharsets.UTF_8);
            assertThat(html).contains("alpha \u03b1", "Freeplane Interactive HTML Companion", "GPL-2.0-or-later",
                    "GNU GENERAL PUBLIC LICENSE", "918c34f2c1d593b4505fbc016c554b5210e94d24",
                    "not the exported map", "<style>", "<script>", "id=\"dynamic-svg-notice\"")
                    .doesNotContain("window.probe", "/*__MANIFEST__*/", "__MAP_TITLE__", "__CSP__", "<script src=");
            Matcher license = Pattern.compile("<template id=\"dynamic-svg-license\">([\\s\\S]*?)</template>").matcher(html);
            assertThat(license.find()).isTrue();
            try (Scanner existingLicense = new Scanner(ResourceController.class.getResourceAsStream("/license.txt"), "UTF-8")) {
                assertThat(StringEscapeUtils.unescapeHtml(license.group(1)))
                        .isEqualTo(existingLicense.useDelimiter("\\A").next());
            }
            assertThat(manifest(html).getAsJsonArray("nodes").toString()).contains(child.getID(), "alpha");
            assertThat(Base64.getDecoder().decode(manifest(html).getAsJsonArray("fontFaces").get(0).getAsJsonObject().get("data").getAsString()))
                    .containsExactly(font);
            assertThat(temporary.getRoot().list()).containsExactly("map.html");
            Matcher script = Pattern.compile("<script>([\\s\\S]*?)</script>").matcher(html);
            assertThat(script.find()).isTrue();
            String hash = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256")
                    .digest(script.group(1).getBytes(StandardCharsets.UTF_8)));
            assertThat(html).contains("script-src 'sha256-" + hash + "'", "default-src 'none'", "font-src data:");
        }
    }

    @Test
    public void escapesMapDataWithoutChangingLinks() throws Exception {
        try (DynamicSvgTestMap map = new DynamicSvgTestMap()) {
            map.model.getRootNode().setUserObject("<title> & \"__MAP_TITLE__\" /*__MANIFEST__*/");
            NodeModel custom = map.add(map.model.getRootNode(), "</script><script>window.mapScriptRan=true</script> \u03b1", false);
            NodeModel relative = map.add(map.model.getRootNode(), "relative", false);
            map.hyperlink(custom, "custom-map:open?value=1&next=2");
            map.hyperlink(relative, "documents/linked.html#anchor");
            map.attach();
            DynamicSvgDocument document = map.onEdt(() -> new MapViewExportCapture(map.view).capture(new ExportDynamicSvg()::capture));
            Path output = temporary.newFile("escaped.html").toPath();

            document.write(output, new DynamicSvgFonts.Embedding(new com.google.gson.JsonArray(), Collections.emptyList()));

            String html = new String(Files.readAllBytes(output), StandardCharsets.UTF_8);
            assertThat(html).contains("<title>&lt;title&gt; &amp; &quot;__MAP_TITLE__&quot; /*__MANIFEST__*/</title>")
                    .doesNotContain("</script><script>window.mapScriptRan");
            JsonObject payload = manifest(html);
            assertThat(payload.getAsJsonArray("nodes").asList()).anySatisfy(node ->
                    assertThat(node.getAsJsonObject().get("linkHref").isJsonNull() ? null
                            : node.getAsJsonObject().get("linkHref").getAsString()).isEqualTo("custom-map:open?value=1&next=2"));
            assertThat(payload.getAsJsonArray("nodes").asList()).anySatisfy(node ->
                    assertThat(node.getAsJsonObject().get("linkHref").isJsonNull() ? null
                            : node.getAsJsonObject().get("linkHref").getAsString()).isEqualTo("documents/linked.html#anchor"));
            assertThat(payload.getAsJsonArray("nodes").asList()).anySatisfy(node ->
                    assertThat(node.getAsJsonObject().get("label").getAsString()).contains("</script><script>"));
        }
    }

    @Test
    public void rejectsInvalidManifestBeforeReplacingDestination() throws Exception {
        try (DynamicSvgTestMap map = new DynamicSvgTestMap()) {
            NodeModel first = map.add(map.model.getRootNode(), "first", false);
            NodeModel second = map.add(map.model.getRootNode(), "second", false);
            map.connect(first, second);
            map.attach();
            DynamicSvgDocument captured = map.onEdt(() -> new MapViewExportCapture(map.view).capture(new ExportDynamicSvg()::capture));
            Path output = temporary.newFile("preserved.html").toPath();
            Map<String, Consumer<JsonObject>> invalid = new LinkedHashMap<>();
            invalid.put("missing field", payload -> payload.getAsJsonArray("nodes").get(0).getAsJsonObject().remove("artwork"));
            invalid.put("duplicate ID", payload -> payload.getAsJsonArray("nodes").add(payload.getAsJsonArray("nodes").get(0).deepCopy()));
            invalid.put("missing root", payload -> payload.addProperty("rootId", "missing"));
            invalid.put("unknown parent", payload -> payload.getAsJsonArray("nodes").get(1).getAsJsonObject().addProperty("parentId", "missing"));
            invalid.put("parent cycle", payload -> {
                payload.getAsJsonArray("nodes").get(1).getAsJsonObject().addProperty("parentId", second.getID());
                payload.getAsJsonArray("nodes").get(2).getAsJsonObject().addProperty("parentId", first.getID());
            });
            invalid.put("hidden content", payload -> payload.getAsJsonArray("nodes").get(1).getAsJsonObject().addProperty("contentVisible", false));
            invalid.put("non-finite dimensions", payload -> payload.getAsJsonArray("nodes").get(1).getAsJsonObject().addProperty("width", Double.NaN));
            invalid.put("wrong dimension type", payload -> payload.getAsJsonArray("nodes").get(1).getAsJsonObject().addProperty("width", "100"));
            invalid.put("connector endpoint", payload -> payload.getAsJsonArray("links").get(0).getAsJsonObject().addProperty("targetId", "missing"));
            invalid.put("connector shape", payload -> payload.getAsJsonArray("links").get(0).getAsJsonObject().getAsJsonObject("resolved").addProperty("shape", "LINE"));
            invalid.put("connector coordinates", payload -> payload.getAsJsonArray("links").get(0).getAsJsonObject().getAsJsonObject("geometry").getAsJsonObject("sourceAnchor").addProperty("x", Double.POSITIVE_INFINITY));
            for (Map.Entry<String, Consumer<JsonObject>> entry : invalid.entrySet()) {
                JsonObject payload = captured.manifest.deepCopy();
                entry.getValue().accept(payload);
                DynamicSvgDocument document = new DynamicSvgDocument(captured.title, payload, captured.textByFont);
                Files.write(output, new byte[]{1, 2, 3});

                assertThatThrownBy(() -> document.write(output,
                        new DynamicSvgFonts.Embedding(new com.google.gson.JsonArray(), Collections.emptyList())))
                        .as(entry.getKey()).isInstanceOf(IllegalStateException.class).hasMessageContaining("Invalid dynamic SVG manifest");

                assertThat(Files.readAllBytes(output)).as(entry.getKey()).containsExactly(1, 2, 3);
                assertThat(temporary.getRoot().list()).containsExactly("preserved.html");
            }
        }
    }

    static JsonObject manifest(String html) {
        Matcher manifest = Pattern.compile("<script id=\"manifest\" type=\"application/json\">([\\s\\S]*?)</script>").matcher(html);
        assertThat(manifest.find()).isTrue();
        return JsonParser.parseString(manifest.group(1)).getAsJsonObject();
    }
}
