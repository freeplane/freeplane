package org.freeplane.view.swing.map.link;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.image.BufferedImage;

import org.freeplane.features.link.ConnectorArrows;
import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.link.ConnectorShape;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.MapViewTitleTestSupport;
import org.freeplane.view.swing.map.NodeView;
import org.junit.Before;
import org.junit.Test;

public class ConnectorViewExportTest extends MapViewTitleTestSupport {
    private MapView mapView;
    private NodeView source;
    private NodeView target;
    private ConnectorModel connector;

    @Before
    public void prepareConnector() {
        MapModel map = newMap();
        NodeModel targetNode = new NodeModel("target", map);
        map.getRootNode().insert(targetNode);
        connector = new ConnectorModel(map.getRootNode(), "target");
        connector.setStartInclination(new Point(60, 20));
        connector.setEndInclination(new Point(70, -30));
        mapView = mock(MapView.class);
        when(mapView.getModeController()).thenReturn(mode);
        when(mapView.getZoom()).thenReturn(1f);
        when(mapView.getZoomed(anyInt())).thenAnswer(call -> call.getArgument(0));
        source = endpoint(map.getRootNode(), new Point(10, 20));
        target = endpoint(targetNode, new Point(250, 130));
        LinkController links = mock(LinkController.class);
        when(mode.getExtension(LinkController.class)).thenReturn(links);
        when(links.getColor(connector)).thenReturn(Color.BLUE);
        when(links.getOpacity(connector)).thenReturn(128);
        when(links.getWidth(connector)).thenReturn(3);
        when(links.getDashArray(connector)).thenReturn(new int[]{4, 7});
        when(links.getShape(connector)).thenReturn(ConnectorShape.CUBIC_CURVE);
        ConnectorArrows arrows = ConnectorArrows.BOTH;
        when(links.getArrows(connector)).thenReturn(arrows);
    }

    @Test
    public void capturesCompleteCubicGeometryWithoutChangingVisibility() {
        for (boolean showsLines : new boolean[]{false, true}) {
            when(mapView.showsConnectorLines(false)).thenReturn(showsLines);
            ConnectorView normal = new ConnectorView(connector, source, target, Color.WHITE);
            paint(normal);
            if (showsLines)
                assertThat(normal.getArrowLinkCurve()).isNotNull();
            else
                assertThat(normal.getArrowLinkCurve()).isNull();

            ConnectorView capture = new ConnectorView(connector, source, target, Color.WHITE, true);
            paint(capture);
            ConnectorView.ExportGeometry geometry = capture.getExportGeometry();

            assertThat(geometry.curve.getX1()).isEqualTo(10);
            assertThat(geometry.curve.getY2()).isEqualTo(130);
            assertThat(geometry.arrows).hasSize(2);
            assertThat(geometry.stroke.getLineWidth()).isEqualTo(3);
            assertThat(geometry.stroke.getDashArray()).containsExactly(4f, 7f);
            assertThat(mapView.showsConnectorLines(false)).isEqualTo(showsLines);
        }
        verify(mapView, never()).putClientProperty(any(), any());
        assertThat(connector.getStartInclination()).isEqualTo(new Point(60, 20));
        assertThat(connector.getEndInclination()).isEqualTo(new Point(70, -30));
    }

    @Test
    public void returnsDetachedConnectorGeometry() {
        for (NodeView end : new NodeView[]{target, source}) {
            ConnectorView capture = new ConnectorView(connector, source, end, Color.WHITE, true);
            paint(capture);
            ConnectorView.ExportGeometry original = capture.getExportGeometry();
            double originalX = original.curve.getX1();
            Polygon originalArrow = original.arrows.get(0);
            int originalArrowX = originalArrow.xpoints[0];

            original.curve.setCurve(0, 0, 0, 0, 0, 0, 0, 0);
            originalArrow.translate(123, 456);

            ConnectorView.ExportGeometry next = capture.getExportGeometry();
            assertThat(next.curve.getX1()).isEqualTo(originalX);
            assertThat(next.arrows.get(0).xpoints[0]).isEqualTo(originalArrowX);
            assertThat(next.arrows).hasSize(2);
            assertThat(connector.getStartInclination()).isEqualTo(new Point(60, 20));
            assertThat(connector.getEndInclination()).isEqualTo(new Point(70, -30));
        }
    }

    private NodeView endpoint(NodeModel node, Point point) {
        NodeView view = mock(NodeView.class);
        when(view.getNode()).thenReturn(node);
        when(view.getMap()).thenReturn(mapView);
        when(view.isContentVisible()).thenReturn(true);
        when(view.getLinkPoint(any(Point.class))).thenAnswer(call -> new Point(point));
        return view;
    }

    private void paint(ConnectorView view) {
        Graphics2D graphics = new BufferedImage(400, 200, BufferedImage.TYPE_INT_ARGB).createGraphics();
        try {
            view.paint(graphics);
        } finally {
            graphics.dispose();
        }
    }
}
