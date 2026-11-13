package org.freeplane.features.map.mindmapmode;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.List;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.menubuilders.generic.UserRole;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.ui.IMapViewManager.MapChangeEventProperty;
import org.freeplane.view.swing.map.MapViewTitleTestSupport;
import org.junit.Test;

public class MMapControllerTitleTest extends MapViewTitleTestSupport {
    @Test
    public void savedStateChangeNotifiesOnlyTheChangedMap() {
        MMapController publisher = mock(MMapController.class, CALLS_REAL_METHODS);
        AFreeplaneAction save = mock(AFreeplaneAction.class);
        when(mode.getAction("SaveAction")).thenReturn(save);
        MapModel map = newMap();
        List<MapChangeEvent> events = recordMapEvents(map);
        List<MapChangeEvent> unrelated = recordMapEvents(newMap());
        publisher.mapSaved(map, false);
        assertFalse(map.isSaved());
        publisher.mapSaved(map, true);
        publisher.mapSaved(map, true);
        assertEquals(2, events.size());
        map.setReadOnly(true);
        publisher.mapSaved(map, true);
        assertEquals(3, events.size());
        for (MapChangeEvent event : events) {
            assertSame(publisher, event.getSource());
            assertSame(map, event.getMap());
            assertSame(MapChangeEventProperty.MAP_VIEW_TITLE, event.getProperty());
            assertNull(event.getOldValue());
            assertNull(event.getNewValue());
            assertFalse(event.setsDirtyFlag());
        }
        assertTrue(unrelated.isEmpty());
        verify(save, times(3)).setEnabled(UserRole.EDITOR);
        verifyNoInteractions(viewManager);
    }

    @Test
    public void savedStateWithoutViewsDoesNotAccessSwingDisplays() {
        MMapController publisher = mock(MMapController.class, CALLS_REAL_METHODS);
        MapModel map = newMap();
        publisher.mapSaved(map, false);
        assertFalse(map.isSaved());
        verifyNoInteractions(viewManager, display);
    }
}
