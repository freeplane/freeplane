// SPDX-License-Identifier: GPL-2.0-or-later
// Adapted from shlomiv/freeplane-interactive-html; see dynamic-svg-NOTICE.txt.
package org.freeplane.plugin.svg;

import java.awt.Font;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.freeplane.core.resources.ResourceController;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

final class DynamicSvgDocument {
    private static final Gson JSON = new GsonBuilder().serializeNulls().create();
    private static final Pattern TOKEN = Pattern.compile("__MAP_TITLE__|/\\*__MANIFEST__\\*/|__CSP__|__NOTICE__|__LICENSE__");
    final String title;
    final JsonObject manifest;
    final Map<Font, String> textByFont;

    DynamicSvgDocument(String title, JsonObject manifest, Map<Font, String> textByFont) {
        this.title = title;
        this.manifest = manifest.deepCopy();
        this.textByFont = new LinkedHashMap<>(textByFont);
    }

    void write(Path destination, DynamicSvgFonts.Embedding embedding) throws IOException {
        String template = resource(DynamicSvgDocument.class, "dynamic-svg.html");
        JsonObject payload = manifest.deepCopy();
        payload.add("fontFaces", embedding.fontFaces.deepCopy());
        validate(payload);
        Map<String, String> values = new LinkedHashMap<>();
        values.put("__MAP_TITLE__", html(title));
        values.put("/*__MANIFEST__*/", JSON.toJson(payload));
        values.put("__NOTICE__", html(resource(DynamicSvgDocument.class, "dynamic-svg-NOTICE.txt")));
        values.put("__LICENSE__", html(resource(ResourceController.class, "/license.txt")));
        values.put("__CSP__", scriptPolicy(template));
        Matcher tokens = TOKEN.matcher(template);
        StringBuffer output = new StringBuffer();
        while (tokens.find())
            tokens.appendReplacement(output, Matcher.quoteReplacement(values.get(tokens.group())));
        tokens.appendTail(output);
        Path target = destination.toAbsolutePath();
        Path temporary = Files.createTempFile(target.getParent(), ".dynamic-svg-", ".tmp");
        try {
            Files.write(temporary, output.toString().getBytes(StandardCharsets.UTF_8));
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private void validate(JsonObject payload) {
        fields(payload, "rootId nodes links summaries fontFaces");
        require(string(payload.get("rootId")), "root ID");
        for (String name : Arrays.asList("nodes", "links", "summaries", "fontFaces"))
            require(payload.get(name).isJsonArray(), name);
        Map<String, JsonObject> nodes = new LinkedHashMap<>();
        for (JsonElement element : payload.getAsJsonArray("nodes")) {
            JsonObject node = element.getAsJsonObject();
            fields(node, "id parentId label searchText width height artwork topOrLeft firstGroup summary alwaysUnfolded collapsed contentVisible linkHref");
            for (String name : Arrays.asList("id", "label", "searchText", "artwork"))
                require(string(node.get(name)), "node " + name);
            for (String name : Arrays.asList("parentId", "linkHref"))
                require(node.get(name).isJsonNull() || string(node.get(name)), "node " + name);
            for (String name : Arrays.asList("topOrLeft", "firstGroup", "summary", "alwaysUnfolded", "collapsed", "contentVisible"))
                require(node.get(name).isJsonPrimitive() && node.getAsJsonPrimitive(name).isBoolean(), "node " + name);
            require(number(node.get("width")) >= 0 && number(node.get("height")) >= 0, "node dimensions");
            String id = node.get("id").getAsString();
            require(!id.isEmpty() && nodes.put(id, node) == null, "duplicate or empty node ID");
            if (!node.get("contentVisible").getAsBoolean())
                require(node.get("label").getAsString().isEmpty() && node.get("searchText").getAsString().isEmpty()
                        && node.get("artwork").getAsString().isEmpty() && node.get("linkHref").isJsonNull()
                        && number(node.get("width")) == 0 && number(node.get("height")) == 0, "hidden node content");
        }
        String root = payload.get("rootId").getAsString();
        require(nodes.containsKey(root), "missing root");
        for (Map.Entry<String, JsonObject> entry : nodes.entrySet()) {
            JsonElement parent = entry.getValue().get("parentId");
            require(parent.isJsonNull() == entry.getKey().equals(root)
                    && (parent.isJsonNull() || nodes.containsKey(parent.getAsString())), "node parent");
        }
        Set<String> connected = new HashSet<>(Arrays.asList(root));
        for (String id : nodes.keySet()) {
            Set<String> path = new HashSet<>();
            while (!connected.contains(id)) {
                require(path.add(id), "node cycle");
                id = nodes.get(id).get("parentId").getAsString();
            }
            connected.addAll(path);
        }
        for (JsonElement element : payload.getAsJsonArray("links")) {
            JsonObject link = element.getAsJsonObject();
            fields(link, "sourceId targetId resolved geometry");
            for (String end : Arrays.asList("sourceId", "targetId")) {
                require(string(link.get(end)), "connector endpoint");
                JsonObject node = nodes.get(link.get(end).getAsString());
                require(node != null && node.get("contentVisible").getAsBoolean(), "connector endpoint");
            }
            JsonObject style = link.getAsJsonObject("resolved"), geometry = link.getAsJsonObject("geometry");
            fields(style, "color alpha shape");
            require(string(style.get("color")) && style.get("color").getAsString().matches("#[0-9a-fA-F]{6}")
                    && string(style.get("shape")) && style.get("shape").getAsString().equals("CUBIC_CURVE"), "connector style");
            integer(style.get("alpha"), 0, 255);
            fields(geometry, "zoom sourceAnchor targetAnchor sourceControl targetControl arrowPolygons strokeWidth strokeDash strokeCap strokeJoin");
            require(number(geometry.get("zoom")) == 1 && number(geometry.get("strokeWidth")) > 0, "connector scale/stroke");
            integer(geometry.get("strokeCap"), 0, 2); integer(geometry.get("strokeJoin"), 0, 2);
            for (String name : Arrays.asList("sourceAnchor", "targetAnchor", "sourceControl", "targetControl"))
                point(geometry.getAsJsonObject(name));
            require(geometry.get("strokeDash").isJsonArray() && geometry.get("arrowPolygons").isJsonArray(), "connector arrays");
            for (JsonElement length : geometry.getAsJsonArray("strokeDash")) require(number(length) >= 0, "stroke dash");
            for (JsonElement arrowElement : geometry.getAsJsonArray("arrowPolygons")) {
                JsonObject arrow = arrowElement.getAsJsonObject();
                fields(arrow, "end points");
                require(string(arrow.get("end")) && Arrays.asList("source", "target").contains(arrow.get("end").getAsString())
                        && arrow.get("points").isJsonArray() && arrow.getAsJsonArray("points").size() >= 3, "arrow");
                for (JsonElement point : arrow.getAsJsonArray("points")) point(point.getAsJsonObject());
            }
        }
        for (JsonElement element : payload.getAsJsonArray("summaries")) {
            JsonObject summary = element.getAsJsonObject();
            fields(summary, "markerId startMarkerId parentId memberIds summaryChildIds");
            for (String name : Arrays.asList("markerId", "startMarkerId", "parentId"))
                require(string(summary.get(name)) && nodes.containsKey(summary.get(name).getAsString()), "summary reference");
            JsonObject marker = nodes.get(summary.get("markerId").getAsString());
            JsonObject start = nodes.get(summary.get("startMarkerId").getAsString());
            require(marker.get("summary").getAsBoolean() && start.get("firstGroup").getAsBoolean()
                    && marker.get("parentId").equals(summary.get("parentId"))
                    && start.get("parentId").equals(summary.get("parentId")), "summary markers");
            for (String name : Arrays.asList("memberIds", "summaryChildIds")) {
                require(summary.get(name).isJsonArray() && !summary.getAsJsonArray(name).isEmpty(), "summary contents");
                for (JsonElement id : summary.getAsJsonArray(name))
                    require(string(id) && nodes.containsKey(id.getAsString())
                            && nodes.get(id.getAsString()).get("parentId").equals(summary.get(name.equals("memberIds") ? "parentId" : "markerId")), "summary child reference");
            }
        }
        for (JsonElement element : payload.getAsJsonArray("fontFaces")) {
            JsonObject face = element.getAsJsonObject();
            fields(face, "family style weight format data");
            for (String name : Arrays.asList("family", "style", "format", "data")) require(string(face.get(name)), "font " + name);
            integer(face.get("weight"), 1, 1000);
            require(Arrays.asList("normal", "italic").contains(face.get("style").getAsString())
                    && Arrays.asList("truetype", "opentype").contains(face.get("format").getAsString())
                    && face.get("data").getAsString().matches("[A-Za-z0-9+/]*={0,2}"), "font face");
        }
    }

    private void fields(JsonObject object, String names) {
        require(object.keySet().equals(new HashSet<>(Arrays.asList(names.split(" ")))), "record fields");
    }

    private boolean string(JsonElement value) {
        return value.isJsonPrimitive() && value.getAsJsonPrimitive().isString();
    }

    private double number(JsonElement value) {
        require(value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber(), "number");
        double number = value.getAsDouble();
        require(Double.isFinite(number), "non-finite number");
        return number;
    }

    private void integer(JsonElement value, int minimum, int maximum) {
        double number = number(value);
        require(number >= minimum && number <= maximum && Math.rint(number) == number, "integer");
    }

    private void point(JsonObject point) {
        fields(point, "x y"); number(point.get("x")); number(point.get("y"));
    }

    private void require(boolean valid, String reason) {
        if (!valid) throw new IllegalStateException("Invalid dynamic SVG manifest: " + reason);
    }

    private String scriptPolicy(String template) throws IOException {
        int start = template.indexOf("<script>");
        int end = template.indexOf("</script>", start);
        if (start < 0 || end < 0)
            throw new IOException("Bundled dynamic SVG viewer has no script");
        String script = template.substring(start + "<script>".length(), end);
        try {
            String hash = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256")
                    .digest(script.getBytes(StandardCharsets.UTF_8)));
            return "default-src 'none'; script-src 'sha256-" + hash
                    + "'; style-src 'unsafe-inline'; img-src data:; font-src data:; base-uri 'none'; object-src 'none'";
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }

    private String resource(Class<?> owner, String name) throws IOException {
        try (InputStream input = owner.getResourceAsStream(name)) {
            if (input == null)
                throw new IOException("Missing bundled resource: " + owner.getName() + ": " + name);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            for (int count; (count = input.read(buffer)) >= 0;)
                bytes.write(buffer, 0, count);
            return new String(bytes.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private String html(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
