package org.freeplane.features.url.mindmapmode;

import static org.mockito.Mockito.*;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.url.UrlManager;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.MapViewTitleTestSupport;
import org.junit.Test;
import org.mockito.InOrder;

public class SaveAllTest extends MapViewTitleTestSupport {
    @Test
    public void saveAllKeepsSaveDecisionsAndRestoresTheSelectedView() throws Exception {
        MapModel current = newMap();
        MapModel saved = newMap();
        MapModel writable = newMap();
        writable.setSaved(false);
        writable.setURL(new File("/tmp/map.mm").toURI().toURL());
        MapModel readOnly = newMap();
        readOnly.setSaved(false);
        readOnly.setReadOnly(true);
        MapModel unnamed = newMap();
        unnamed.setSaved(false);
        MapView initial = newView(current, "current");
        MapView readOnlyView = newView(readOnly, "readonly");
        MapView first = newView(unnamed, "unnamed");
        MapView second = newView(unnamed, "unnamed<2>");
        Map<String, MapModel> maps = new LinkedHashMap<>();
        maps.put("current", current);
        maps.put("saved", saved);
        maps.put("writable", writable);
        maps.put("readonly", readOnly);
        maps.put("unnamed", unnamed);
        maps.put("unnamed<2>", unnamed);
        when(viewManager.getMapViewComponent()).thenReturn(initial);
        when(viewManager.getMap()).thenReturn(current);
        when(viewManager.getMaps()).thenReturn(maps);
        doReturn(Arrays.asList(initial, newView(saved, "saved"), newView(writable, "writable"),
                readOnlyView, first, second)).when(viewManager).getMapViews();
        MFileManager files = mock(MFileManager.class);
        when(mode.getExtension(UrlManager.class)).thenReturn(files);
        when(files.save(unnamed)).thenAnswer(call -> {
            first.setName("saved name");
            second.setName("saved name<2>");
            initial.setName("renamed current");
            return true;
        });

        new SaveAll().actionPerformed(null);

        InOrder order = inOrder(viewManager, files);
        order.verify(files).save(writable);
        order.verify(viewManager).changeToMapView(readOnlyView);
        order.verify(files).save(readOnly);
        order.verify(viewManager).changeToMapView(first);
        order.verify(files).save(unnamed);
        order.verify(viewManager).changeToMapView(second);
        order.verify(files).save(unnamed);
        order.verify(viewManager).changeToMapView(initial);
        order.verify(files).save(current);
        verify(files, never()).save(saved);
        verify(viewManager, never()).changeToMapView(anyString());
    }
}
