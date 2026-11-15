package org.freeplane.features.url.mindmapmode;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.url.UrlManager;
import org.freeplane.view.swing.map.MapViewTitleTestSupport;
import org.junit.Test;

public class SaveAsActionTest extends MapViewTitleTestSupport {
    @Test
    public void saveAsLeavesTitleNotificationsToSaveAndNameChanges() {
        MapModel map = newMap();
        when(controller.getMap()).thenReturn(map);
        MFileManager files = mock(MFileManager.class);
        when(mode.getExtension(UrlManager.class)).thenReturn(files);
        when(files.saveAs(map)).thenReturn(true, false).thenThrow(new IllegalStateException("save failed"));
        SaveAsAction action = new SaveAsAction();
        action.actionPerformed(null);
        action.actionPerformed(null);
        assertThrows(IllegalStateException.class, () -> action.actionPerformed(null));
        verify(files, times(3)).saveAs(map);
        verifyNoInteractions(viewManager, display);
    }
}
