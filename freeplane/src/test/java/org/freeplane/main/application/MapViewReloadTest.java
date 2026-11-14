package org.freeplane.main.application;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.OptionalDontShowMeAgainDialog;
import org.freeplane.core.ui.components.OptionalDontShowMeAgainDialog.MessageType;
import org.freeplane.core.undo.IUndoHandler;
import org.freeplane.features.map.DocuMapAttribute;
import org.freeplane.features.map.IMapLifeCycleListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.map.mindmapmode.MMapModel;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.ui.IMapViewManager.MapChangeEventProperty;
import org.freeplane.features.url.UrlManager;
import org.freeplane.features.url.mindmapmode.ExternalMapChangeMonitor;
import org.freeplane.features.url.mindmapmode.MFileManager;
import org.freeplane.features.url.mindmapmode.MFileManager.AlternativeFileMode;
import org.freeplane.main.headlessmode.HeadlessMapViewController;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.MapViewController;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.MockedStatic;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;

public class MapViewReloadTest extends MapViewDockingWindowsTestSupport {
    private MMapController maps;
    private MFileManager files;
    private MapViewController manager;
    private MMapModel original;
    private File sourceFile;
    private MapView first;
    private MapView second;
    private MapView unrelated;
    private TabWindow group;
    private AutoCloseable rendering;

    @Before
    public void prepareReload() throws Exception {
        when(resources.getProperty("number_of_different_files_for_automatic_save")).thenReturn("0");
        ModeController previousMode = mode;
        mode = mock(MModeController.class, AdditionalAnswers.delegatesTo(previousMode));
        when(controller.getModeController()).thenReturn(mode);
        maps = mock(MMapController.class, CALLS_REAL_METHODS);
        field(maps, "modeController", mode);
        field(maps, "loadedMaps", new WeakHashMap<MMapModel, Void>());
        field(maps, "mapLifeCycleListeners", new LinkedList<IMapLifeCycleListener>());
        field(maps, "mapChangeListeners", new LinkedList<>());
        files = mock(MFileManager.class);
        when(mode.getExtension(UrlManager.class)).thenReturn(files);
        doReturn(maps).when(mode).getMapController();
        mapController = maps;
        manager = useRealViewManager();
        // Use actual removal notifications rather than the basic fixture's removal substitute.
        doCallRealMethod().when(maps).closeWithoutSaving(any(MapModel.class));
        maps.addMapLifeCycleListener(manager);
        doAnswer(call -> { ((Runnable) call.getArgument(0)).run(); return null; })
                .when(display).invokeLater(any(Runnable.class));
        sourceFile = File.createTempFile("freeplane-reload", ".mm");
        original = map(sourceFile.toURI().toURL(), "old contents");
        maps.addLoadedMap(original);
        first = openView(manager, original, "reload");
        second = openView(manager, original, "reload");
        second.setName("script key");
        unrelated = openView(manager, map(new File("/tmp/other.mm").toURI().toURL(), "other"), "other");
        group = new TabWindow(new DockingWindow[]{owner(first), owner(second)});
        windows.getRootWindow().setWindow(new TabWindow(new DockingWindow[]{group, owner(unrelated)}));
        rename(owner(first), "first & *");
        rename(owner(second), "second (2)");
        rename(group, "group");
        manager.changeToMapView(second);
        when(files.getAlternativeURL(any(URL.class), eq(AlternativeFileMode.ALL)))
                .thenReturn(original.getURL());
        doAnswer(call -> {
            MMapModel replacement = call.getArgument(1);
            replacement.setRoot(new NodeModel("new contents", replacement));
            return null;
        }).when(files).loadAndLock(any(URL.class), any(MMapModel.class));
        doAnswer(call -> {
            MMapModel replacement = call.getArgument(1);
            replacement.setRoot(new NodeModel("new documentation", replacement));
            return null;
        }).when(files).load(any(URL.class), any(MapModel.class));
        rendering = prepareReplacementRendering(manager);
    }

    @After
    public void releaseReloadRendering() throws Exception {
        if (rendering != null) rendering.close();
        if (sourceFile != null) sourceFile.delete();
    }

    @Test
    public void externalReloadPreservesAllViewsAndNames() throws Exception {
        View firstOwner = owner(first);
        View secondOwner = owner(second);
        AFreeplaneAction firstAction = menuAction(first);
        AFreeplaneAction secondAction = menuAction(second);
        JMenuItem firstItem = menuItem(firstAction);
        JMenuItem secondItem = menuItem(secondAction);
        javax.swing.Action firstControlAction = firstItem.getAction();
        javax.swing.Action secondControlAction = secondItem.getAction();
        List<String> keys = manager.getMapKeys();
        List<String> lifecycle = new ArrayList<>();
        maps.addMapLifeCycleListener(new IMapLifeCycleListener() {
            @Override
            public void onCreate(MapModel map) { lifecycle.add("created"); }
            @Override
            public void onRemove(MapModel map) {
                assertNotSame(original, first.getMap());
                assertSame(first.getMap(), second.getMap());
                assertNotNull(first.getMap().getExtension(IUndoHandler.class));
                lifecycle.add("removed");
            }
        });
        clearInvocations(input);

        externalChange(JOptionPane.OK_OPTION);

        MapModel replacement = first.getMap();
        assertNotSame(original, replacement);
        assertEquals("new contents", replacement.getRootNode().getText());
        assertEquals(Arrays.asList("created", "removed"), lifecycle);
        assertSame(replacement, maps.getMap(original.getURL()));
        assertTrue(replacement.isSaved());
        assertEquals(Boolean.TRUE, field(replacement, "autosaveEnabled"));
        assertSame(firstOwner, owner(first));
        assertSame(secondOwner, owner(second));
        assertSame(group, firstOwner.getWindowParent());
        assertSame(group, secondOwner.getWindowParent());
        assertSame(second, manager.getMapViewComponent());
        assertEquals(keys, manager.getMapKeys());
        assertEquals("script key", second.getName());
        assertEquals("first & *", firstItem.getText());
        assertEquals("second (2)", secondItem.getText());
        assertSame(firstControlAction, firstItem.getAction());
        assertSame(secondControlAction, secondItem.getAction());
        assertEquals("other", unrelated.getMap().getRootNode().getText());
        assertEquals("group (2)", group.getTitle());
        verify(input, never()).updateMapList();
        List<java.beans.PropertyChangeEvent> requests = recordTitleRequests(first);
        original.fireMapChangeEvent(new MapChangeEvent(this, original, MapChangeEventProperty.MAP_VIEW_TITLE, null, null, false));
        assertTrue(requests.isEmpty());
        replacement.fireMapChangeEvent(new MapChangeEvent(this, replacement, MapChangeEventProperty.MAP_VIEW_TITLE, null, null, false));
        assertEquals(1, requests.size());
        assertEquals(1, Arrays.stream(second.getPropertyChangeListeners(MapView.TITLE_REFRESH_PROPERTY))
                .filter(listener -> listener == manager).count());
        verify(files, never()).getAlternativeURL(any(), any());
    }

    @Test
    public void revertPreservesViewsWhenLoadingAnAlternativeFile() throws Exception {
        URL alternative = new File("/tmp/reload.backup.mm").toURI().toURL();
        when(files.getAlternativeURL(original.getURL(), AlternativeFileMode.ALL)).thenReturn(alternative);
        List<String> keys = manager.getMapKeys();
        View firstOwner = owner(first);
        View secondOwner = owner(second);

        revertItem().doClick();

        verify(files).loadAndLock(eq(alternative), any(MMapModel.class));
        assertEquals(original.getURL(), first.getMap().getURL());
        assertFalse(first.getMap().isSaved());
        assertSame(first.getMap(), second.getMap());
        assertSame(firstOwner, owner(first));
        assertSame(secondOwner, owner(second));
        assertSame(second, manager.getMapViewComponent());
        assertEquals(keys, manager.getMapKeys());
        assertEquals("first & *", first.getCustomizedTabName());
        assertEquals("second (2)", second.getCustomizedTabName());
        assertEquals("group (2) *", group.getTitle());
    }

    @Test
    public void cancelledExternalReloadLeavesViewsAndNamesUnchanged() throws Exception {
        clearInvocations(files, display, manager);
        externalChange(JOptionPane.CANCEL_OPTION);
        assertUnchanged();
        verify(files, never()).loadAndLock(any(), any());
        verify(manager, never()).setMap(any(), any());
        verify(display, never()).setTitle(anyString());
        verify(display, never()).setWaitingCursor(anyBoolean());
    }

    @Test
    public void cancelledAlternativeSelectionLeavesViewsAndNamesUnchanged() throws Exception {
        when(files.getAlternativeURL(original.getURL(), AlternativeFileMode.ALL)).thenReturn(null);
        field(original, "autosaveEnabled", true);
        org.freeplane.features.url.mindmapmode.LockManager lock = spy(original.getLockManager());
        field(original, "lockManager", lock);
        clearInvocations(files, display, manager);
        revertItem().doClick();
        assertUnchanged();
        verify(files, never()).loadAndLock(any(), any());
        verify(manager, never()).setMap(any(), any());
        verify(display, never()).setWaitingCursor(anyBoolean());
        assertEquals(Boolean.TRUE, field(original, "autosaveEnabled"));
        verify(lock, never()).release();
    }

    @Test
    public void revertWithoutUrlKeepsExistingErrorBehavior() throws Exception {
        original.setURL(null);
        revertItem().doClick();
        assertUnchanged();
        ui.verify(() -> org.freeplane.core.ui.components.UITools.errorMessage("map_not_saved"));
        verify(files, never()).loadAndLock(any(), any());
    }

    @Test
    public void failedReloadKeepsExistingViewOwners() throws Exception {
        View owner = owner(first);
        org.freeplane.features.url.mindmapmode.LockManager lock = spy(original.getLockManager());
        field(original, "lockManager", lock);
        field(original, "autosaveEnabled", true);
        doThrow(new IOException("load failed")).when(files).loadAndLock(any(), any());
        clearInvocations(display);
        assertThrows(IOException.class, () -> maps.restoreCurrentMapIgnoreAlternatives());
        assertUnchanged();
        assertSame(owner, owner(first));
        verify(lock).release();
        assertEquals(Boolean.FALSE, field(original, "autosaveEnabled"));
        verify(display).setWaitingCursor(true);
        verify(display).setWaitingCursor(false);
    }

    @Test
    public void revertingDocumentationKeepsOwnersAndReadOnlyState() throws Exception {
        makeDocumentation();
        View owner = owner(first);
        revertItem().doClick();
        MapModel replacement = first.getMap();
        assertNotSame(original, replacement);
        assertEquals("new documentation", replacement.getRootNode().getText());
        assertTrue(replacement.isReadOnly());
        assertTrue(replacement.containsExtension(DocuMapAttribute.class));
        assertTrue(replacement.isSaved());
        assertEquals(Boolean.FALSE, field(replacement, "autosaveEnabled"));
        assertSame(replacement, maps.getMap(original.getURL()));
        assertSame(replacement, second.getMap());
        assertSame(owner, owner(first));
        assertSame(second, manager.getMapViewComponent());
        assertEquals("first & *", first.getCustomizedTabName());
        assertEquals("second (2)", second.getCustomizedTabName());
        verify(files, never()).getAlternativeURL(any(), any());
        verify(files, never()).loadAndLock(any(), any());
        verify(files, never()).lock(any(), any());
    }

    @Test
    public void failedDocumentationReloadRestoresLoadedMapRegistration() throws Exception {
        makeDocumentation();
        doThrow(new IOException("documentation failed")).when(files).load(any(URL.class), any(MapModel.class));
        assertThrows(RuntimeException.class, () -> maps.restoreCurrentMap());
        assertUnchanged();
        assertSame(original, maps.getMap(original.getURL()));
    }

    @Test
    public void headlessReloadReplacesOnlyTheModel() throws Exception {
        for (Component view : new ArrayList<Component>(manager.getMapViews()))
            manager.closeWithoutSaving(view);
        assertTrue(windows.getMapViewVector().isEmpty());
        maps.addLoadedMap(original);
        HeadlessMapViewController headless = spy(new HeadlessMapViewController());
        viewManager = headless;
        when(controller.getMap()).thenAnswer(call -> headless.getMap());
        headless.newMapView(original, mode);
        doAnswer(call -> {
            try (MockedStatic<GraphicsEnvironment> graphics = mockStatic(GraphicsEnvironment.class, CALLS_REAL_METHODS)) {
                graphics.when(GraphicsEnvironment::isHeadless).thenReturn(false);
                ((Runnable) call.getArgument(0)).run();
            }
            return null;
        }).when(display).invokeLater(any(Runnable.class));
        maps.restoreCurrentMapIgnoreAlternatives();
        MapModel replacement = headless.getMap();
        assertNotSame(original, replacement);
        assertTrue(headless.getMapViews().isEmpty());
        assertTrue(replacement.isSaved());
        assertEquals(original.getURL(), replacement.getURL());
        assertSame(replacement, maps.getMap(original.getURL()));
        assertNotNull(replacement.getExtension(IUndoHandler.class));
        assertEquals(Boolean.TRUE, field(replacement, "autosaveEnabled"));
        verify(headless, never()).setMap(any(), any());
        verify(headless, times(2)).newMapView(any(), eq(mode));
    }

    private void externalChange(int response) throws Exception {
        when(resources.getBooleanProperty("monitor_external_mind_map_file_changes", true)).thenReturn(true);
        // The callback's native focus gate is an explicit input; file-change detection remains real.
        field(original, "lastKnownModificationTime", Long.MIN_VALUE);
        try (MockedStatic<KeyboardFocusManager> focus = mockStatic(KeyboardFocusManager.class);
                MockedStatic<SwingUtilities> swing = mockStatic(SwingUtilities.class, CALLS_REAL_METHODS);
                MockedStatic<OptionalDontShowMeAgainDialog> confirm = mockStatic(OptionalDontShowMeAgainDialog.class)) {
            KeyboardFocusManager keyboard = mock(KeyboardFocusManager.class);
            Window frame = mock(Window.class);
            focus.when(KeyboardFocusManager::getCurrentKeyboardFocusManager).thenReturn(keyboard);
            when(keyboard.getFocusedWindow()).thenReturn(frame);
            swing.when(() -> SwingUtilities.isDescendingFrom(second, frame)).thenReturn(true);
            confirm.when(() -> OptionalDontShowMeAgainDialog.show("reload_mindmap_after_external_change",
                    "monitor_external_mind_map_file_changes", MessageType.ONLY_CANCEL_SELECTION_IS_STORED))
                    .thenReturn(response);
            java.lang.reflect.Field instance = ExternalMapChangeMonitor.class.getDeclaredField("INSTANCE");
            instance.setAccessible(true);
            ExternalMapChangeMonitor monitor = (ExternalMapChangeMonitor) instance.get(null);
            monitor.checkForExternalChange(original);
        }
    }

    private void assertUnchanged() {
        assertSame(original, first.getMap());
        assertSame(original, second.getMap());
        assertSame(second, manager.getMapViewComponent());
        assertEquals("first & *", first.getCustomizedTabName());
        assertEquals("second (2)", second.getCustomizedTabName());
        assertSame(group, owner(first).getWindowParent());
        assertSame(group, owner(second).getWindowParent());
        assertEquals("group", CustomTabTitleProvider.getCustomizedTabName(group));
    }

    private void makeDocumentation() throws Exception {
        original.setURL(new URL("https://example.org/documentation.mm"));
        original.addExtension(DocuMapAttribute.INSTANCE);
        original.setReadOnly(true);
    }

    private MMapModel map(URL url, String rootText) {
        MMapModel map = new MMapModel(null);
        map.setRoot(new NodeModel(rootText, map));
        map.setURL(url);
        map.setSaved(true);
        return map;
    }

    private View owner(MapView view) {
        return (View) SwingUtilities.getAncestorOfClass(View.class, view);
    }

    private JMenuItem revertItem() throws Exception {
        Constructor<?> constructor = Class.forName("org.freeplane.features.url.mindmapmode.RevertAction")
                .getDeclaredConstructor();
        constructor.setAccessible(true);
        return new JMenuItem((AFreeplaneAction) constructor.newInstance());
    }
}
