package org.freeplane.main.application;

import static org.mockito.Mockito.*;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JFrame;
import org.freeplane.view.swing.map.MapViewTitleTestSupport;
import org.junit.Test;

public class ApplicationViewControllerTitleTest extends MapViewTitleTestSupport {
    @Test
    public void settingFrameTitleDoesNotRefreshTabs() throws Exception {
        ApplicationViewController application = mock(ApplicationViewController.class, CALLS_REAL_METHODS);
        JFrame frame = mock(JFrame.class);
        MapViewDockingWindows docking = mock(MapViewDockingWindows.class);
        field(application, "mainFrame", frame);
        field(application, "mapViewWindows", docking);
        application.setTitle("title");
        verify(frame).setTitle("title");
        verifyNoInteractions(docking);
    }

    @Test
    public void unchangedFrameTitleIsNotAssignedAgain() throws Exception {
        ApplicationViewController application = mock(ApplicationViewController.class, CALLS_REAL_METHODS);
        JFrame frame = mock(JFrame.class);
        AtomicReference<String> title = new AtomicReference<>("");
        when(frame.getTitle()).thenAnswer(call -> title.get());
        doAnswer(call -> { title.set(call.getArgument(0)); return null; }).when(frame).setTitle(anyString());
        field(application, "mainFrame", frame);
        application.setTitle("title");
        application.setTitle("title");
        verify(frame, times(1)).setTitle("title");
    }
}
