package org.freeplane.plugin.svg;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.Component;
import java.awt.Graphics;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.filechooser.FileFilter;

import org.apache.fop.fonts.FontDetector;
import org.apache.fop.fonts.FontDetectorFactory;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.export.mindmapmode.IExportEngine;
import org.freeplane.features.filter.Filter;
import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.link.ConnectorShape;
import org.freeplane.features.map.FirstGroupNodeFlag;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.SummaryNodeFlag;
import org.freeplane.view.swing.map.DynamicSvgTestMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.MockedStatic;

import com.google.gson.JsonObject;

public class ExportDynamicSvgTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();
    private DynamicSvgTestMap map;
    private final List<String> errors = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();

    @Before public void setUp() throws Exception { map = new DynamicSvgTestMap(); }
    @After public void tearDown() throws Exception { map.close(); }

    @Test
    public void registersHtmlExportWithoutReplacingExistingExports() throws Exception {
        Map<FileFilter, IExportEngine> engines = installExports();

        assertThat(engines.values()).anyMatch(ExportDynamicSvg.class::isInstance)
                .anyMatch(ExportSvg.class::isInstance).anyMatch(ExportPdf.class::isInstance);
        assertThat(engines.keySet()).anySatisfy(filter -> {
            assertThat(filter.getDescription()).contains("Dynamic SVG (HTML)");
            assertThat(filter.accept(new java.io.File("map.html"))).isTrue();
            assertThat(filter.accept(new java.io.File("map.svg"))).isFalse();
        });
        assertThat(engines.keySet()).anyMatch(filter -> filter.getDescription().startsWith("existing HTML"));
        verify(map.exports, org.mockito.Mockito.never()).addBranchExportEngine(any(), any());
    }

    @Test
    public void exportsUnsavedContentAndCurrentFolding() throws Exception {
        NodeModel branch = map.add(map.model.getRootNode(), "unsaved branch", true);
        NodeModel leaf = map.add(branch, "unsaved leaf", true);
        map.add(leaf, "folded descendant", false);
        NodeModel anonymous = new NodeModel("anonymous node", map.model);
        branch.insert(anonymous);
        assertThat(anonymous.getID()).isNull();
        map.add(map.model.getRootNode(), "outside current root", false);
        map.attach();
        map.narrowRoot(branch);
        map.model.setSaved(false);
        int changes = map.model.getNumberOfChangesSinceLastSave();
        Path output = temporary.getRoot().toPath().resolve("unsaved.html");
        IExportEngine engine = registeredEngine();

        export(engine, output);

        assertThat(errors).isEmpty();
        JsonObject manifest = DynamicSvgDocumentTest.manifest(read(output));
        assertThat(manifest.get("rootId").getAsString()).isEqualTo(branch.getID());
        assertThat(manifest.toString()).contains("unsaved branch", "unsaved leaf", "folded descendant", "anonymous node", "<svg")
                .doesNotContain("outside current root");
        assertThat(node(manifest, branch).get("parentId").isJsonNull()).isTrue();
        assertThat(node(manifest, leaf).get("collapsed").getAsBoolean()).isTrue();
        assertThat(map.node(leaf).isFolded()).isTrue();
        assertThat(anonymous.getID()).isNull();
        assertThat(map.model.getNumberOfChangesSinceLastSave()).isEqualTo(changes);
        assertThat(map.view.getRoot().getNode()).isSameAs(branch);
    }

    @Test
    public void exportsVisibleDetailsFromHeadlessView() throws Exception {
        NodeModel leaf = map.add(map.model.getRootNode(), "node with details", false);
        map.details(leaf, "visible details");
        map.attach();
        Path output = temporary.getRoot().toPath().resolve("details.html");
        assertThat(map.view.isDisplayable()).isFalse();

        export(registeredEngine(), output);

        assertThat(errors).isEmpty();
        JsonObject record = node(DynamicSvgDocumentTest.manifest(read(output)), leaf);
        assertThat(record.get("artwork").getAsString()).contains("node with details", "visible details");
        assertThat(record.get("searchText").getAsString()).contains("visible details");
    }

    @Test
    public void doesNotSerializeFilteredOutContent() throws Exception {
        NodeModel ancestor = map.add(map.model.getRootNode(), "EXCLUDED_ANCESTOR_TEXT", true);
        NodeModel matching = map.add(ancestor, "included leaf", false);
        NodeModel excluded = map.add(ancestor, "EXCLUDED_LEAF_TEXT", false);
        map.hyperlink(ancestor, "private:EXCLUDED_ANCESTOR_LINK");
        map.details(ancestor, "EXCLUDED_ANCESTOR_DETAIL");
        map.details(excluded, "EXCLUDED_LEAF_DETAIL");
        map.attach();
        map.filter(new Filter(node -> node == matching, false, false, false, false, Filter.FilteredElement.NODE, null));
        Path output = temporary.getRoot().toPath().resolve("filtered.html");

        export(registeredEngine(), output);

        assertThat(errors).isEmpty();
        assertThat(read(output)).doesNotContain("EXCLUDED_");
        JsonObject payload = DynamicSvgDocumentTest.manifest(read(output));
        JsonObject path = node(payload, ancestor);
        assertThat(path.get("contentVisible").getAsBoolean()).isFalse();
        assertThat(path.get("artwork").getAsString()).isEmpty();
        assertThat(path.get("searchText").getAsString()).isEmpty();
        assertThat(path.get("linkHref").isJsonNull()).isTrue();
        assertThat(payload.getAsJsonArray("nodes")).hasSize(3);
    }

    @Test
    public void reportsUnsupportedInScopeStructures() throws Exception {
        NodeModel source = map.add(map.model.getRootNode(), "source", false);
        NodeModel target = map.add(map.model.getRootNode(), "target", false);
        map.connect(source, target);
        map.attach();
        IExportEngine engine = registeredEngine();
        Path output = temporary.newFile("preserved.html").toPath();
        for (ConnectorShape shape : new ConnectorShape[]{ConnectorShape.LINE, ConnectorShape.LINEAR_PATH, ConnectorShape.EDGE_LIKE}) {
            Files.write(output, new byte[]{1, 2, 3});
            when(map.links.getShape(any(ConnectorModel.class))).thenReturn(shape);
            errors.clear();

            export(engine, output);

            assertThat(errors).singleElement().asString().contains(source.getID(), target.getID(), shape.name());
            assertThat(Files.readAllBytes(output)).containsExactly(1, 2, 3);
        }
        when(map.links.getShape(any(ConnectorModel.class))).thenReturn(ConnectorShape.CUBIC_CURVE);
        NodeModel summary = map.add(map.model.getRootNode(), "", false);
        summary.addExtension(SummaryNodeFlag.SUMMARY);
        map.add(summary, "malformed summary content", false);
        map.attach();
        errors.clear();

        export(engine, output);

        assertThat(errors).singleElement().asString().contains(summary.getID(), "summary structure");
        assertThat(Files.readAllBytes(output)).containsExactly(1, 2, 3);
        NodeModel outer = new NodeModel("", map.model);
        outer.createID(); outer.addExtension(FirstGroupNodeFlag.FIRST_GROUP);
        map.model.getRootNode().insert(outer, 0);
        NodeModel inner = new NodeModel("", map.model);
        inner.createID(); inner.addExtension(FirstGroupNodeFlag.FIRST_GROUP);
        map.model.getRootNode().insert(inner, 1);
        map.attach(); errors.clear();

        export(engine, output);

        assertThat(errors).singleElement().asString().contains(summary.getID(), "summary structure");
        assertThat(Files.readAllBytes(output)).containsExactly(1, 2, 3);
    }

    @Test
    public void retainsFilteredSummaryContentAndInScopeConnectors() throws Exception {
        NodeModel root = map.add(map.model.getRootNode(), "current root", false);
        NodeModel outsideSummary = map.add(map.model.getRootNode(), "", false);
        outsideSummary.addExtension(SummaryNodeFlag.SUMMARY);
        map.add(outsideSummary, "EXCLUDED unsupported summary", false);
        NodeModel start = map.add(root, "", false);
        start.addExtension(FirstGroupNodeFlag.FIRST_GROUP);
        NodeModel member = map.add(root, "included member", false);
        NodeModel omitted = map.add(root, "omitted member", false);
        NodeModel summary = map.add(root, "", false);
        summary.addExtension(SummaryNodeFlag.SUMMARY);
        NodeModel label = map.add(summary, "included summary", false);
        map.connect(member, label);
        ConnectorModel excludedConnector = map.connect(omitted, member);
        ConnectorModel outsideConnector = map.connect(member, outsideSummary);
        when(map.links.getShape(excludedConnector)).thenReturn(ConnectorShape.LINE);
        when(map.links.getShape(outsideConnector)).thenReturn(ConnectorShape.EDGE_LIKE);
        map.attach();
        map.narrowRoot(root);
        IExportEngine engine = registeredEngine();
        for (Filter.FilteredElement element : Filter.FilteredElement.values()) {
            map.filter(new Filter(node -> node == member || node == label, false, false, false, false, element, null));
            Path output = temporary.getRoot().toPath().resolve("summary-" + element + ".html");
            errors.clear();

            export(engine, output);

            assertThat(errors).isEmpty();
            JsonObject payload = DynamicSvgDocumentTest.manifest(read(output));
            assertThat(payload.getAsJsonArray("summaries")).hasSize(1);
            assertThat(payload.getAsJsonArray("links")).hasSize(1);
            assertThat(read(output)).doesNotContain("EXCLUDED unsupported summary", outsideSummary.getID());
            if (element != Filter.FilteredElement.CONNECTOR) assertThat(read(output)).doesNotContain("omitted member");
        }
        map.filter(new Filter(node -> node == label, false, false, false, false, Filter.FilteredElement.NODE, null));
        Path output = temporary.getRoot().toPath().resolve("summary-without-members.html");
        errors.clear();

        export(engine, output);

        assertThat(errors).isEmpty();
        JsonObject payload = DynamicSvgDocumentTest.manifest(read(output));
        assertThat(payload.getAsJsonArray("summaries")).isEmpty();
        assertThat(payload.getAsJsonArray("links")).isEmpty();
        assertThat(read(output)).contains("included summary").doesNotContain("included member", "omitted member", start.getID());
    }

    @Test
    public void leavesDestinationUnchangedWhenCaptureFails() throws Exception {
        NodeModel parent = map.add(map.model.getRootNode(), "parent", true);
        map.add(parent, "leaf", false);
        map.attach();
        map.icon(parent, new Icon() {
            public int getIconWidth() { return 16; }
            public int getIconHeight() { return 16; }
            public void paintIcon(Component component, Graphics graphics, int x, int y) { throw new IllegalStateException("native painter failed"); }
        });
        Path output = temporary.newFile("preserved.html").toPath();
        Files.write(output, new byte[]{4, 5, 6});

        export(registeredEngine(), output);
        map.flush();

        assertThat(errors).singleElement().asString().contains("native painter failed");
        assertThat(Files.readAllBytes(output)).containsExactly(4, 5, 6);
        assertThat(map.node(parent).isFolded()).isTrue();
        assertThat(temporary.getRoot().list()).containsExactly("preserved.html");
        verify(map.display).setWaitingCursor(false);
    }

    @Test
    public void reportsWriteFailureWithoutReportingSuccess() throws Exception {
        map.attach();
        Path output = temporary.newFile("preserved.html").toPath();
        Files.write(output, new byte[]{7, 8, 9});
        IExportEngine engine = registeredEngine();
        for (String failedOperation : new String[]{"write", "move"}) {
            errors.clear();
            map.onEdt(() -> {
                try (MockedStatic<UITools> dialogs = dialogs(); MockedStatic<Files> files = mockStatic(Files.class, call -> {
                    if (call.getMethod().getName().equals(failedOperation)) {
                        if (failedOperation.equals("move"))
                            throw new AtomicMoveNotSupportedException("temporary", "destination", "test filesystem");
                        throw new java.io.IOException("test filesystem write failed");
                    }
                    return call.callRealMethod();
                })) {
                    engine.export(Collections.singletonList(map.model.getRootNode()), output.toFile());
                }
                return null;
            });

            assertThat(errors).singleElement().asString().contains("test filesystem");
            assertThat(warnings).isEmpty();
            assertThat(Files.readAllBytes(output)).containsExactly(7, 8, 9);
            assertThat(temporary.getRoot().list()).containsExactly("preserved.html");
        }
        verify(map.display, org.mockito.Mockito.times(2)).setWaitingCursor(false);
    }

    @Test
    public void warnsAboutIncompleteEmbeddingAfterSuccessfulWrite() throws Exception {
        map.attach();
        IExportEngine engine = registeredEngine();
        Path output = temporary.getRoot().toPath().resolve("fonts.html");
        when(map.resources.getBooleanProperty(ExportVectorGraphic.SVG_EMBED_FONTS)).thenReturn(true);
        map.onEdt(() -> {
            try (MockedStatic<UITools> dialogs = dialogs(); MockedStatic<FontDetectorFactory> fonts = mockStatic(FontDetectorFactory.class)) {
                FontDetector detector = mock(FontDetector.class);
                fonts.when(FontDetectorFactory::createDefault).thenReturn(detector);
                engine.export(Collections.singletonList(map.model.getRootNode()), output.toFile());
            }
            return null;
        });
        assertThat(errors).isEmpty();
        assertThat(Files.exists(output)).isTrue();
        assertThat(warnings).singleElement().asString().contains("Font embedding is incomplete", "SansSerif", "logical/composite");
        warnings.clear();
        when(map.resources.getBooleanProperty(ExportVectorGraphic.SVG_EMBED_FONTS)).thenReturn(false);

        export(engine, output);

        assertThat(errors).isEmpty();
        assertThat(warnings).isEmpty();
    }

    private void export(IExportEngine engine, Path output) throws Exception {
        map.onEdt(() -> {
            try (MockedStatic<UITools> dialogs = dialogs()) {
                engine.export(Collections.singletonList(map.model.getRootNode()), output.toFile());
            }
            return null;
        });
    }

    private MockedStatic<UITools> dialogs() {
        return mockStatic(UITools.class, call -> {
            if (call.getMethod().getName().equals("errorMessage")) { errors.add(String.valueOf(call.getArguments()[0])); return null; }
            if (call.getMethod().getName().equals("informationMessage")) { warnings.add(String.valueOf(call.getArguments()[1])); return null; }
            return call.callRealMethod();
        });
    }

    private IExportEngine registeredEngine() throws Exception {
        return installExports().values().stream().filter(ExportDynamicSvg.class::isInstance).findFirst().get();
    }

    private Map<FileFilter, IExportEngine> installExports() throws Exception {
        return map.installExports(new Activator());
    }

    private JsonObject node(JsonObject payload, NodeModel model) {
        return payload.getAsJsonArray("nodes").asList().stream().map(element -> element.getAsJsonObject())
                .filter(node -> node.get("id").getAsString().equals(model.getID())).findFirst().get();
    }

    private String read(Path file) throws Exception { return new String(Files.readAllBytes(file), StandardCharsets.UTF_8); }
}
