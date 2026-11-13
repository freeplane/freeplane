package org.freeplane.view.swing.ui;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.freeplane.features.bookmarks.mindmapmode.BookmarksController;
import org.freeplane.features.bookmarks.mindmapmode.NodeBookmarkDescriptor;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.text.TextController;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.MapViewTitleTestSupport;
import org.junit.Test;

public class MapDisplayNameResolverTest extends MapViewTitleTestSupport {

    @Test
    public void shouldUseCustomizedTabNameWithoutTruncation() {
        MapDisplayNameResolver resolver = resolverWith(MapDisplayNameSource.FILE_NAME, 5,
                new TestOpenedMapNameAccess("this custom title stays intact", "MapName"),
                (mapView, maximumLength, continuationMark) -> "ignored",
                mapView -> "ignored",
                (value, maximumLength, continuationMark) -> "truncated");

        String label = resolver.resolveBaseLabelWithDependencies(null);

        assertThat(label).isEqualTo("this custom title stays intact");
    }

    @Test
    public void shouldResolveFileNameSourceWithTruncation() {
        MapDisplayNameResolver resolver = resolverWith(MapDisplayNameSource.FILE_NAME, 12,
                new TestOpenedMapNameAccess(null, "MapName"),
                (mapView, maximumLength, continuationMark) -> "ignored",
                mapView -> "ignored",
                (value, maximumLength, continuationMark) -> value + "|" + maximumLength + "|" + continuationMark);

        String label = resolver.resolveAutomaticBaseLabelWithDependencies(null, MapDisplayNameSource.FILE_NAME);

        assertThat(label).isEqualTo("MapName|12| ...");
    }

    @Test
    public void shouldUseCurrentViewRootNameWhenConfigured() {
        MapDisplayNameResolver resolver = resolverWith(MapDisplayNameSource.VIEW_ROOT_TEXT, 100,
                new TestOpenedMapNameAccess(null, "MapName"),
                (mapView, maximumLength, continuationMark) -> "Current Root",
                mapView -> "ignored",
                (value, maximumLength, continuationMark) -> "shortened:" + value);

        String label = resolver.resolveBaseLabelWithDependencies(null);

        assertThat(label).isEqualTo("Current Root");
    }

    @Test
    public void shouldFallbackToFileNameWhenCurrentViewRootNameMissing() {
        MapDisplayNameResolver resolver = resolverWith(MapDisplayNameSource.VIEW_ROOT_TEXT, 7,
                new TestOpenedMapNameAccess(null, "FallbackName"),
                (mapView, maximumLength, continuationMark) -> null,
                mapView -> "ignored",
                (value, maximumLength, continuationMark) -> "file:" + value + ":" + maximumLength);

        String label = resolver.resolveBaseLabelWithDependencies(null);

        assertThat(label).isEqualTo("file:FallbackName:7");
    }

    @Test
    public void shouldUseCurrentViewRootBookmarkTitleWhenConfigured() {
        MapDisplayNameResolver resolver = resolverWith(MapDisplayNameSource.VIEW_ROOT_BOOKMARK, 9,
                new TestOpenedMapNameAccess(null, "MapName"),
                (mapView, maximumLength, continuationMark) -> "ignored",
                mapView -> "BookmarkTitle",
                (value, maximumLength, continuationMark) -> "bookmark:" + value + ":" + maximumLength);

        String label = resolver.resolveBaseLabelWithDependencies(null);

        assertThat(label).isEqualTo("bookmark:BookmarkTitle:9");
    }

    @Test
    public void shouldMatchAnyAutomaticLabelAcrossSources() {
        MapDisplayNameResolver resolver = resolverWith(MapDisplayNameSource.FILE_NAME, 100,
                new TestOpenedMapNameAccess(null, "File Name"),
                (mapView, maximumLength, continuationMark) -> "Root Name",
                mapView -> "Bookmark Name",
                (value, maximumLength, continuationMark) -> value);

        assertThat(resolver.matchesAnyAutomaticLabelWithDependencies(null, "File Name")).isTrue();
        assertThat(resolver.matchesAnyAutomaticLabelWithDependencies(null, "Root Name")).isTrue();
        assertThat(resolver.matchesAnyAutomaticLabelWithDependencies(null, "Bookmark Name")).isTrue();
        assertThat(resolver.matchesAnyAutomaticLabelWithDependencies(null, "Other")).isFalse();
    }

    @Test
    public void usesNearestNonFormulaAncestorForViewRootText() {
        MapModel map = newMap();
        NodeModel parent = new NodeModel("parent", map);
        NodeModel root = new NodeModel("leaf", map);
        map.getRootNode().insert(parent);
        parent.insert(root);
        MapView view = newView(map, "filename");
        when(view.getRoot().getNode()).thenReturn(root);
        when(resources.getEnumProperty(MapDisplayNameResolver.MAP_DISPLAY_NAME_SOURCE_PROPERTY,
                MapDisplayNameSource.FILE_NAME)).thenReturn(MapDisplayNameSource.VIEW_ROOT_TEXT);
        when(text.isFormula(any())).thenAnswer(call -> call.getArgument(0).toString().startsWith("="));
        when(text.getShortPlainText(any(NodeModel.class), anyInt(), anyString()))
                .thenAnswer(call -> ((NodeModel) call.getArgument(0)).getText());
        assertThat(MapDisplayNameResolver.resolveBaseLabel(view)).isEqualTo("leaf");
        root.setUserObject("=leaf");
        assertThat(MapDisplayNameResolver.resolveBaseLabel(view)).isEqualTo("parent");
        parent.setUserObject("=parent");
        assertThat(MapDisplayNameResolver.resolveBaseLabel(view)).isEqualTo("root");
        map.getRootNode().setUserObject("=root");
        assertThat(MapDisplayNameResolver.resolveBaseLabel(view)).isEqualTo("filename");
        when(mode.getExtension(TextController.class)).thenReturn(null);
        assertThat(MapDisplayNameResolver.resolveBaseLabel(view)).isEqualTo("filename");
    }

    @Test
    public void usesNearestAncestorBookmarkForViewRootBookmark() throws Exception {
        MapModel map = newMap();
        NodeModel parent = new NodeModel("parent", map);
        NodeModel root = new NodeModel("leaf", map);
        map.getRootNode().insert(parent);
        parent.insert(root);
        MapView view = newView(map, "filename");
        when(view.getRoot().getNode()).thenReturn(root);
        when(resources.getEnumProperty(MapDisplayNameResolver.MAP_DISPLAY_NAME_SOURCE_PROPERTY,
                MapDisplayNameSource.FILE_NAME)).thenReturn(MapDisplayNameSource.VIEW_ROOT_BOOKMARK);
        assertThat(MapDisplayNameResolver.resolveBaseLabel(view)).isEqualTo("filename");
        BookmarksController bookmarks = mock(BookmarksController.class, CALLS_REAL_METHODS);
        field(bookmarks, "modeController", mode);
        bookmarks.addBookmark(map.getRootNode(), new NodeBookmarkDescriptor("map bookmark", false));
        bookmarks.addBookmark(parent, new NodeBookmarkDescriptor("parent bookmark", false));
        assertThat(MapDisplayNameResolver.resolveBaseLabel(view)).isEqualTo("parent bookmark");
        bookmarks.removeBookmark(parent);
        assertThat(MapDisplayNameResolver.resolveBaseLabel(view)).isEqualTo("map bookmark");
        bookmarks.removeBookmark(map.getRootNode());
        assertThat(MapDisplayNameResolver.resolveBaseLabel(view)).isEqualTo("filename");
    }

    private MapDisplayNameResolver resolverWith(MapDisplayNameSource source,
                                                int maximumLength,
                                                MapDisplayNameResolver.OpenedMapNameAccess openedMapNameAccess,
                                                MapDisplayNameResolver.CurrentViewRootNameResolver currentViewRootNameResolver,
                                                MapDisplayNameResolver.CurrentViewRootBookmarkTitleResolver currentViewRootBookmarkTitleResolver,
                                                MapDisplayNameResolver.LabelShortener labelShortener) {
        return new MapDisplayNameResolver(new TestMapNameChoicePreferences(source, maximumLength),
                openedMapNameAccess,
                currentViewRootNameResolver,
                currentViewRootBookmarkTitleResolver,
                labelShortener);
    }

    private static class TestMapNameChoicePreferences implements MapDisplayNameResolver.MapNameChoicePreferences {
        private final MapDisplayNameSource source;
        private final int maximumLength;

        private TestMapNameChoicePreferences(MapDisplayNameSource source, int maximumLength) {
            this.source = source;
            this.maximumLength = maximumLength;
        }

        @Override
        public MapDisplayNameSource getDisplayNameSource() {
            return source;
        }

        @Override
        public int getMaximumLabelLength() {
            return maximumLength;
        }
    }

    private static class TestOpenedMapNameAccess implements MapDisplayNameResolver.OpenedMapNameAccess {
        private final String customizedTabName;
        private final String fileName;

        private TestOpenedMapNameAccess(String customizedTabName, String fileName) {
            this.customizedTabName = customizedTabName;
            this.fileName = fileName;
        }

        @Override
        public String getCustomizedTabName(MapView mapView) {
            return customizedTabName;
        }

        @Override
        public String getFileName(MapView mapView) {
            return fileName;
        }
    }
}
