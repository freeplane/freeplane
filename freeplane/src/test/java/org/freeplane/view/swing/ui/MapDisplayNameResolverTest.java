package org.freeplane.view.swing.ui;

import static org.assertj.core.api.Assertions.assertThat;

import org.freeplane.view.swing.map.MapView;
import org.junit.Test;

public class MapDisplayNameResolverTest {

    @Test
    public void shouldResolveTabNameIndependentlyFromDockingTitle() {
        MapDisplayNameResolver resolver = resolverWith(MapDisplayNameSource.FILE_NAME, 5,
                new TestOpenedMapNameAccess("MapName"),
                (mapView, maximumLength, continuationMark) -> "ignored",
                mapView -> "ignored",
                (value, maximumLength, continuationMark) -> "truncated");

        String label = resolver.resolveBaseLabelWithDependencies(null);

        assertThat(label).isEqualTo("truncated");
    }

    @Test
    public void shouldResolveFileNameSourceWithTruncation() {
        MapDisplayNameResolver resolver = resolverWith(MapDisplayNameSource.FILE_NAME, 12,
                new TestOpenedMapNameAccess("MapName"),
                (mapView, maximumLength, continuationMark) -> "ignored",
                mapView -> "ignored",
                (value, maximumLength, continuationMark) -> value + "|" + maximumLength + "|" + continuationMark);

        String label = resolver.resolveAutomaticBaseLabelWithDependencies(null, MapDisplayNameSource.FILE_NAME);

        assertThat(label).isEqualTo("MapName|12| ...");
    }

    @Test
    public void shouldUseCurrentViewRootNameWhenConfigured() {
        MapDisplayNameResolver resolver = resolverWith(MapDisplayNameSource.VIEW_ROOT_TEXT, 100,
                new TestOpenedMapNameAccess("MapName"),
                (mapView, maximumLength, continuationMark) -> "Current Root",
                mapView -> "ignored",
                (value, maximumLength, continuationMark) -> "shortened:" + value);

        String label = resolver.resolveBaseLabelWithDependencies(null);

        assertThat(label).isEqualTo("Current Root");
    }

    @Test
    public void shouldFallbackToFileNameWhenCurrentViewRootNameMissing() {
        MapDisplayNameResolver resolver = resolverWith(MapDisplayNameSource.VIEW_ROOT_TEXT, 7,
                new TestOpenedMapNameAccess("FallbackName"),
                (mapView, maximumLength, continuationMark) -> null,
                mapView -> "ignored",
                (value, maximumLength, continuationMark) -> "file:" + value + ":" + maximumLength);

        String label = resolver.resolveBaseLabelWithDependencies(null);

        assertThat(label).isEqualTo("file:FallbackName:7");
    }

    @Test
    public void shouldUseCurrentViewRootBookmarkTitleWhenConfigured() {
        MapDisplayNameResolver resolver = resolverWith(MapDisplayNameSource.VIEW_ROOT_BOOKMARK, 9,
                new TestOpenedMapNameAccess("MapName"),
                (mapView, maximumLength, continuationMark) -> "ignored",
                mapView -> "BookmarkTitle",
                (value, maximumLength, continuationMark) -> "bookmark:" + value + ":" + maximumLength);

        String label = resolver.resolveBaseLabelWithDependencies(null);

        assertThat(label).isEqualTo("bookmark:BookmarkTitle:9");
    }

    @Test
    public void shouldMatchAnyAutomaticLabelAcrossSources() {
        MapDisplayNameResolver resolver = resolverWith(MapDisplayNameSource.FILE_NAME, 100,
                new TestOpenedMapNameAccess("File Name"),
                (mapView, maximumLength, continuationMark) -> "Root Name",
                mapView -> "Bookmark Name",
                (value, maximumLength, continuationMark) -> value);

        assertThat(resolver.matchesAnyAutomaticLabelWithDependencies(null, "File Name")).isTrue();
        assertThat(resolver.matchesAnyAutomaticLabelWithDependencies(null, "Root Name")).isTrue();
        assertThat(resolver.matchesAnyAutomaticLabelWithDependencies(null, "Bookmark Name")).isTrue();
        assertThat(resolver.matchesAnyAutomaticLabelWithDependencies(null, "Other")).isFalse();
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
        private final String fileName;

        private TestOpenedMapNameAccess(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public String getFileName(MapView mapView) {
            return fileName;
        }
    }
}
