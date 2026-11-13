package org.freeplane.main.application;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.awt.Component;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.dnd.DropTarget;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import org.freeplane.api.TextWritingDirection;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.map.MapModel;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.MapViewTitleTestSupport;
import org.freeplane.view.swing.ui.MapDisplayNameResolver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import net.infonode.docking.DockingWindow;
import net.infonode.docking.RootWindow;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;
import net.infonode.docking.theme.LookAndFeelDockingTheme;
import net.infonode.docking.properties.RootWindowProperties;
import net.infonode.docking.properties.DockingWindowProperties;
import net.infonode.docking.properties.ViewProperties;
import net.infonode.docking.properties.WindowTabProperties;
import net.infonode.tabbedpanel.titledtab.TitledTabProperties;
import net.infonode.tabbedpanel.titledtab.TitledTabStateProperties;

public class MapViewDockingWindowsTest extends MapViewTitleTestSupport {
    private MapViewDockingWindows windows;
    private MockedConstruction<DropTarget> dropTargets;
    private MockedConstruction<LookAndFeelDockingTheme> themes;
    private MockedConstruction<java.awt.PopupMenu> overviewMenus;
    private MockedConstruction<java.awt.MenuItem> overviewMenuItems;
    private MockedStatic<UITools> ui;
    private MockedStatic<JOptionPane> dialogs;
    private List<PropertyChangeListener> previousFocusListeners;

    @Before
    public void createDockingWindows() throws Exception {
        previousFocusListeners = Arrays.asList(KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .getPropertyChangeListeners("focusedWindow"));
        dropTargets = mockConstruction(DropTarget.class);
        overviewMenus = mockConstruction(java.awt.PopupMenu.class);
        overviewMenuItems = mockConstruction(java.awt.MenuItem.class);
        themes = mockConstruction(LookAndFeelDockingTheme.class, (theme, context) ->
                when(theme.getRootWindowProperties()).thenReturn(new RootWindowProperties()));
        ui = mockStatic(UITools.class);
        dialogs = mockStatic(JOptionPane.class);
        JFrame frame = mock(JFrame.class);
        when(frame.isResizable()).thenReturn(true);
        ui.when(UITools::getCurrentFrame).thenReturn(frame);
        ui.when(UITools::getUIFont).thenReturn(new Font(Font.DIALOG, Font.PLAIN, 12));
        ui.when(() -> UITools.getUIFontSize(anyDouble())).thenReturn(12);
        dialogs.when(() -> JOptionPane.getFrameForComponent(any(Component.class))).thenReturn(frame);
        ApplicationViewController application = mock(ApplicationViewController.class);
        windows = new MapViewDockingWindows(application);
        when(application.getMenuComponent()).thenReturn(frame);
        when(resources.getBooleanProperty("selected_map_follows_active_window")).thenReturn(true);
    }

    @After
    public void disposeDockingServices() {
        if (dialogs != null) dialogs.close();
        if (ui != null) ui.close();
        if (dropTargets != null) dropTargets.close();
        if (themes != null) themes.close();
        if (overviewMenuItems != null) overviewMenuItems.close();
        if (overviewMenus != null) overviewMenus.close();
        KeyboardFocusManager focus = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        for (PropertyChangeListener listener : focus.getPropertyChangeListeners("focusedWindow"))
            if (!previousFocusListeners.contains(listener))
                focus.removePropertyChangeListener("focusedWindow", listener);
    }

    @Test
    public void viewTitleRequestUpdatesOnlyItsDockingWindow() throws Exception {
        MapModel map = newMap();
        MapView source = newView(map, "source");
        MapView unrelated = newView(newMap(), "other");
        View docked = dock(source);
        View other = dock(unrelated);
        ViewProperties otherTitle = observeTitle(other);
        TitledTabStateProperties otherTooltip = observeTooltip(other);
        @SuppressWarnings("unchecked")
        Vector<Component> tracked = spy((Vector<Component>) field(windows, "mapViews"));
        field(windows, "mapViews", tracked);
        clearInvocations(viewManager, tracked);
        map.setURL(new File("/tmp/title-path.mm").toURI().toURL());
        source.putClientProperty(MapDisplayNameResolver.CUSTOMIZED_TAB_NAME_PROPERTY, "custom");
        assertEquals("custom", docked.getTitle());
        assertEquals(TextWritingDirection.LEFT_TO_RIGHT.isolatePathSeparators("/tmp/title-path.mm"),
                tooltip(docked).getToolTipText());
        assertEquals("other", other.getTitle());
        verifyNoInteractions(tracked);
        verify(otherTitle, never()).setTitle(anyString());
        verify(otherTooltip, never()).setToolTipText(nullable(String.class));
        verify(viewManager, never()).getMapViews();
        verify(viewManager, never()).getViews(any(MapModel.class));
    }

    @Test
    public void unchangedTitlesAndTooltipsAreNotAssignedAgain() throws Exception {
        MapView source = newView(newMap(), "source");
        View docked = dock(source);
        ViewProperties title = observeTitle(docked);
        TitledTabStateProperties tooltip = observeTooltip(docked);
        source.refreshTitle();
        source.refreshTitle();
        verify(title, never()).setTitle(anyString());
        verify(tooltip, never()).setToolTipText(nullable(String.class));
    }

    @Test
    public void changedLeafTitleUpdatesDockingAncestors() throws Exception {
        MapModel map = newMap();
        MapView source = newView(map, "source");
        View first = dock(source);
        View second = dock(newView(newMap(), "second"));
        View unrelated = dock(newView(newMap(), "other"));
        TabWindow inner = new TabWindow(new DockingWindow[]{first, second});
        TabWindow outer = new TabWindow(new DockingWindow[]{inner});
        windows.getRootWindow().setWindow(new SplitWindow(true, 0.5f, outer, unrelated));
        source.putClientProperty(MapDisplayNameResolver.CUSTOMIZED_TAB_NAME_PROPERTY, "changed");
        assertTrue(inner.getTitle().contains("changed"));
        assertTrue(outer.getTitle().contains("changed"));
        map.setSaved(false);
        source.refreshTitle();
        assertTrue(first.getTitle().endsWith(" *"));
        assertTrue(inner.getTitle().contains(" *"));
        assertTrue(outer.getTitle().contains(" *"));
        assertEquals("other", unrelated.getTitle());
    }

    @Test
    public void dockingSubscriptionsFollowViewLifetime() throws Exception {
        MapView source = newView(newMap(), "source");
        View docked = dock(source);
        windows.afterViewChange(source, source);
        windows.afterViewChange(source, source);
        windows.getRootWindow().setWindow(new TabWindow(new DockingWindow[]{docked}));
        assertEquals(1, Arrays.stream(source.getPropertyChangeListeners(MapView.TITLE_REFRESH_PROPERTY))
                .filter(listener -> listener == windows).count());
        ViewProperties title = observeTitle(docked);
        List<DockingWindow> removed = new ArrayList<>();
        windows.getRootWindow().addListener(new net.infonode.docking.DockingWindowAdapter() {
            @Override
            public void windowRemoved(DockingWindow parent, DockingWindow window) {
                if (window == docked) {
                    assertFalse(Arrays.asList(source.getPropertyChangeListeners(MapView.TITLE_REFRESH_PROPERTY)).contains(windows));
                    removed.add(window);
                }
            }
        });
        windows.afterViewClose(source);
        assertEquals(Collections.singletonList(docked), removed);
        source.putClientProperty(MapDisplayNameResolver.CUSTOMIZED_TAB_NAME_PROPERTY, "closed");
        verify(title, never()).setTitle(anyString());
        assertFalse(Arrays.asList(source.getPropertyChangeListeners(MapView.TITLE_REFRESH_PROPERTY)).contains(windows));
    }

    @Test
    public void layoutRestorationKeepsExistingIndividualNameRecovery() throws Exception {
        MapView source = newView(newMap(), "source");
        View docked = dock(source);
        docked.getViewProperties().setTitle("restored custom *");
        MapViewSerializer serializer = mock(MapViewSerializer.class);
        doAnswer(call -> { ((ObjectOutputStream) call.getArgument(1)).writeInt(1); return null; })
                .when(serializer).writeView(any(View.class), any(ObjectOutputStream.class));
        when(serializer.readView(any(ObjectInputStream.class))).thenAnswer(call -> {
            ((ObjectInputStream) call.getArgument(0)).readInt();
            source.refreshTitle();
            assertEquals("restored custom *", docked.getViewProperties().getTitle());
            windows.afterViewChange(null, source);
            return docked;
        });
        field(windows.getRootWindow(), "viewSerializer", serializer);
        doAnswer(call -> {
            when(resources.getProperty("openedNow_1.3.04", null)).thenReturn(call.getArgument(1));
            return null;
        }).when(resources).setProperty(eq("openedNow_1.3.04"), anyString());
        windows.saveLayout();
        windows.loadLayout();
        assertEquals("restored custom", source.getClientProperty(MapDisplayNameResolver.CUSTOMIZED_TAB_NAME_PROPERTY));
        assertEquals("restored custom", docked.getTitle());
        source.putClientProperty(MapDisplayNameResolver.CUSTOMIZED_TAB_NAME_PROPERTY, "renamed later");
        source.refreshTitle();
        assertEquals("renamed later", docked.getTitle());
        assertEquals(1, Arrays.stream(source.getPropertyChangeListeners(MapView.TITLE_REFRESH_PROPERTY))
                .filter(listener -> listener == windows).count());
    }

    @Test
    public void tabRenamePublishesOneViewTitleRequest() throws Exception {
        MapView source = newView(newMap(), "source");
        View docked = dock(source);
        dock(newView(newMap(), "other"));
        JPopupMenu menu = windows.getRootWindow().getPopupMenuFactory().createPopupMenu(docked);
        JMenuItem rename = (JMenuItem) menu.getComponent(1);
        List<java.beans.PropertyChangeEvent> requests = recordTitleRequests(source);
        dialogs.when(() -> JOptionPane.showInputDialog(any(Object.class), any(Object.class)))
                .thenReturn("custom", "custom", null, "another", "");
        rename.doClick();
        assertEquals(1, requests.size());
        assertEquals("custom", docked.getTitle());
        rename.doClick();
        assertEquals(1, requests.size());
        rename.doClick();
        assertEquals(2, requests.size());
        assertNull(source.getClientProperty(MapDisplayNameResolver.CUSTOMIZED_TAB_NAME_PROPERTY));
        assertEquals("source", docked.getTitle());
        rename.doClick();
        rename.doClick();
        assertEquals(4, requests.size());
        assertNull(source.getClientProperty(MapDisplayNameResolver.CUSTOMIZED_TAB_NAME_PROPERTY));
        assertEquals("source", docked.getTitle());
    }

    @Test
    public void dockingFocusSelectsTheViewReference() throws Exception {
        MapView first = newView(newMap(), "one");
        MapView second = newView(newMap(), "two");
        first.putClientProperty(MapDisplayNameResolver.CUSTOMIZED_TAB_NAME_PROPERTY, "shared");
        second.putClientProperty(MapDisplayNameResolver.CUSTOMIZED_TAB_NAME_PROPERTY, "shared");
        dock(first);
        View target = dock(second);
        when(viewManager.getMapViewComponent()).thenReturn(first);
        clearInvocations(viewManager);
        Method focus = RootWindow.class.getDeclaredMethod("setFocusedView", View.class);
        focus.setAccessible(true);
        focus.invoke(windows.getRootWindow(), target);
        verify(viewManager).changeToMapView(second);
        verify(viewManager, never()).changeToMapView(anyString());
    }

    private View dock(MapView view) {
        windows.afterViewChange(null, view);
        return (View) SwingUtilities.getAncestorOfClass(View.class, view);
    }

    private ViewProperties observeTitle(View view) throws Exception {
        ViewProperties properties = spy(view.getViewProperties());
        field(field(view, "windowItem"), "viewProperties", properties);
        return properties;
    }

    private TitledTabStateProperties tooltip(View view) {
        return view.getWindowProperties().getTabProperties().getTitledTabProperties().getNormalProperties();
    }

    private TitledTabStateProperties observeTooltip(View view) throws Exception {
        DockingWindowProperties window = spy(view.getWindowProperties());
        WindowTabProperties tab = spy(window.getTabProperties());
        TitledTabProperties titled = spy(tab.getTitledTabProperties());
        TitledTabStateProperties normal = spy(titled.getNormalProperties());
        doReturn(normal).when(titled).getNormalProperties();
        doReturn(titled).when(tab).getTitledTabProperties();
        doReturn(tab).when(window).getTabProperties();
        field(field(view, "windowItem"), "dockingWindowProperties", window);
        return normal;
    }
}
