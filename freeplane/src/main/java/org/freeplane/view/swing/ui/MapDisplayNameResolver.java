package org.freeplane.view.swing.ui;

import java.util.Objects;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.bookmarks.mindmapmode.MapBookmarks;
import org.freeplane.features.bookmarks.mindmapmode.NodeBookmark;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.text.TextController;
import org.freeplane.view.swing.map.MapView;

public final class MapDisplayNameResolver {
    public static final String MAP_DISPLAY_NAME_SOURCE_PROPERTY = "mapDisplayNameSource";
    public static final String MAP_DISPLAY_NAME_MAX_LENGTH_PROPERTY = "mapDisplayNameMaxLength";

    private static final int DEFAULT_MAX_LENGTH = 100;
    private static final String CONTINUATION_MARK = " ...";

    interface MapNameChoicePreferences {
        MapDisplayNameSource getDisplayNameSource();

        int getMaximumLabelLength();
    }

    interface OpenedMapNameAccess {
        String getFileName(MapView mapView);
    }

    interface CurrentViewRootNameResolver {
        String resolve(MapView mapView, int maximumLength, String continuationMark);
    }

    interface CurrentViewRootBookmarkTitleResolver {
        String resolve(MapView mapView);
    }

    interface LabelShortener {
        String shorten(String value, int maximumLength, String continuationMark);
    }

    private static final class ResourceBasedMapNameChoicePreferences implements MapNameChoicePreferences {
        @Override
        public MapDisplayNameSource getDisplayNameSource() {
            ResourceController resourceController = ResourceController.getResourceController();
            return resourceController.getEnumProperty(MAP_DISPLAY_NAME_SOURCE_PROPERTY, MapDisplayNameSource.FILE_NAME);
        }

        @Override
        public int getMaximumLabelLength() {
            return ResourceController.getResourceController().getIntProperty(MAP_DISPLAY_NAME_MAX_LENGTH_PROPERTY,
                    DEFAULT_MAX_LENGTH);
        }
    }

    private static final class DefaultOpenedMapNameAccess implements OpenedMapNameAccess {
        @Override
        public String getFileName(MapView mapView) {
            return mapView.getName();
        }
    }

    private static final class DefaultCurrentViewRootNameResolver implements CurrentViewRootNameResolver {
        @Override
        public String resolve(MapView mapView, int maximumLength, String continuationMark) {
            NodeModel candidateNode = mapView.getRoot() != null ? mapView.getRoot().getNode() : null;
            TextController textController = mapView.getModeController().getExtension(TextController.class);
            if (textController == null) {
                return null;
            }
            NodeModel displayNode = findNearestNonFormulaNode(candidateNode, textController);
            if (displayNode == null) {
                return null;
            }
            return textController.getShortPlainText(displayNode, maximumLength, continuationMark);
        }

        private NodeModel findNearestNonFormulaNode(NodeModel node, TextController textController) {
            NodeModel current = node;
            while (current != null) {
                if (!textController.isFormula(current.getUserObject())) {
                    return current;
                }
                current = current.getParentNode();
            }
            return null;
        }
    }

    private static final class DefaultCurrentViewRootBookmarkTitleResolver implements CurrentViewRootBookmarkTitleResolver {
        @Override
        public String resolve(MapView mapView) {
            NodeModel candidateNode = mapView.getRoot() != null ? mapView.getRoot().getNode() : null;
            if (candidateNode == null) {
                return null;
            }
            MapBookmarks mapBookmarks = candidateNode.getMap().getExtension(MapBookmarks.class);
            if (mapBookmarks == null) {
                return null;
            }
            NodeModel current = candidateNode;
            while (current != null) {
                NodeBookmark bookmark = mapBookmarks.getBookmark(current.getID());
                if (bookmark != null) {
                    return bookmark.getName();
                }
                current = current.getParentNode();
            }
            return null;
        }
    }

    private static final class DefaultLabelShortener implements LabelShortener {
        @Override
        public String shorten(String value, int maximumLength, String continuationMark) {
            return TextUtils.getShortText(value, maximumLength, continuationMark);
        }
    }

    private static final MapDisplayNameResolver DEFAULT_RESOLVER = new MapDisplayNameResolver(
            new ResourceBasedMapNameChoicePreferences(),
            new DefaultOpenedMapNameAccess(),
            new DefaultCurrentViewRootNameResolver(),
            new DefaultCurrentViewRootBookmarkTitleResolver(),
            new DefaultLabelShortener());

    private final MapNameChoicePreferences preferences;
    private final OpenedMapNameAccess openedMapNameAccess;
    private final CurrentViewRootNameResolver currentViewRootNameResolver;
    private final CurrentViewRootBookmarkTitleResolver currentViewRootBookmarkTitleResolver;
    private final LabelShortener labelShortener;

    MapDisplayNameResolver(MapNameChoicePreferences preferences,
                           OpenedMapNameAccess openedMapNameAccess,
                           CurrentViewRootNameResolver currentViewRootNameResolver,
                           CurrentViewRootBookmarkTitleResolver currentViewRootBookmarkTitleResolver,
                           LabelShortener labelShortener) {
        this.preferences = preferences;
        this.openedMapNameAccess = openedMapNameAccess;
        this.currentViewRootNameResolver = currentViewRootNameResolver;
        this.currentViewRootBookmarkTitleResolver = currentViewRootBookmarkTitleResolver;
        this.labelShortener = labelShortener;
    }

    public static String resolveBaseLabel(MapView mapView) {
        return DEFAULT_RESOLVER.resolveBaseLabelWithDependencies(mapView);
    }

    public static String resolveAutomaticBaseLabel(MapView mapView) {
        return DEFAULT_RESOLVER.resolveAutomaticBaseLabelWithDependencies(mapView,
                DEFAULT_RESOLVER.preferences.getDisplayNameSource());
    }

    static String resolveAutomaticBaseLabel(MapView mapView, MapDisplayNameSource source) {
        return DEFAULT_RESOLVER.resolveAutomaticBaseLabelWithDependencies(mapView, source);
    }

    public static boolean matchesAnyAutomaticLabel(MapView mapView, String titleWithoutDirtyMarker) {
        return DEFAULT_RESOLVER.matchesAnyAutomaticLabelWithDependencies(mapView, titleWithoutDirtyMarker);
    }

    public static String removeDirtyMarker(String title) {
        if (title != null && title.endsWith(" *")) {
            return title.substring(0, title.length() - 2);
        }
        return title;
    }

    String resolveBaseLabelWithDependencies(MapView mapView) {
        return resolveAutomaticBaseLabelWithDependencies(mapView, preferences.getDisplayNameSource());
    }

    String resolveAutomaticBaseLabelWithDependencies(MapView mapView, MapDisplayNameSource source) {
        switch (source) {
            case VIEW_ROOT_TEXT:
                String rootName = currentViewRootNameResolver.resolve(mapView, preferences.getMaximumLabelLength(),
                        CONTINUATION_MARK);
                return rootName != null ? rootName : resolveFileNameLabel(mapView);
            case VIEW_ROOT_BOOKMARK:
                String bookmarkTitle = currentViewRootBookmarkTitleResolver.resolve(mapView);
                return bookmarkTitle != null ? maybeTruncate(bookmarkTitle) : resolveFileNameLabel(mapView);
            case FILE_NAME:
            default:
                return resolveFileNameLabel(mapView);
        }
    }

    boolean matchesAnyAutomaticLabelWithDependencies(MapView mapView, String titleWithoutDirtyMarker) {
        for (MapDisplayNameSource source : MapDisplayNameSource.values()) {
            String automaticTitle = resolveAutomaticBaseLabelWithDependencies(mapView, source);
            if (Objects.equals(titleWithoutDirtyMarker, automaticTitle)) {
                return true;
            }
        }
        return false;
    }

    private String resolveFileNameLabel(MapView mapView) {
        return maybeTruncate(openedMapNameAccess.getFileName(mapView));
    }

    private String maybeTruncate(String label) {
        return labelShortener.shorten(label, preferences.getMaximumLabelLength(), CONTINUATION_MARK);
    }
}
