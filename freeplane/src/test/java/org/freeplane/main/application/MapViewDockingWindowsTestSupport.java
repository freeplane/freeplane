package org.freeplane.main.application;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.awt.Component;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.dnd.DropTarget;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import org.apache.commons.codec.binary.Base64;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.features.map.MapModel;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.MapViewController;
import org.freeplane.view.swing.ui.UserInputListenerFactory;
import org.freeplane.view.swing.map.MapViewTitleTestSupport;
import org.junit.After;
import org.junit.Before;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import net.infonode.docking.DockingWindow;
import net.infonode.docking.View;
import net.infonode.docking.theme.LookAndFeelDockingTheme;
import net.infonode.docking.properties.RootWindowProperties;
import net.infonode.docking.properties.DockingWindowProperties;
import net.infonode.docking.properties.ViewProperties;
import net.infonode.docking.properties.WindowTabProperties;
import net.infonode.tabbedpanel.titledtab.TitledTabProperties;
import net.infonode.tabbedpanel.titledtab.TitledTabStateProperties;

public class MapViewDockingWindowsTestSupport extends MapViewTitleTestSupport {
    protected MapViewDockingWindows windows;
    protected MockedConstruction<javax.swing.Timer> timers;
    protected MockedConstruction<DropTarget> dropTargets;
    protected MockedConstruction<LookAndFeelDockingTheme> themes;
    protected MockedConstruction<java.awt.PopupMenu> overviewMenus;
    protected MockedConstruction<java.awt.MenuItem> overviewMenuItems;
    protected MockedStatic<UITools> ui;
    protected MockedStatic<JOptionPane> dialogs;
    protected List<PropertyChangeListener> previousFocusListeners;

    @Before
    public void createDockingWindows() throws Exception {
        previousFocusListeners = Arrays.asList(KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .getPropertyChangeListeners("focusedWindow"));
        timers = mockConstruction(javax.swing.Timer.class);
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
        if (timers != null) timers.close();
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

    protected void rename(DockingWindow owner, String name) {
        dialogs.when(() -> JOptionPane.showInputDialog(any(Object.class), nullable(Object.class))).thenReturn(name);
        JPopupMenu popup = windows.getRootWindow().getPopupMenuFactory().createPopupMenu(owner);
        assertNotNull(popup);
        ((JMenuItem) popup.getComponent(1)).doClick();
    }

    protected org.freeplane.view.swing.map.MapViewController useRealViewManager() throws Exception {
        org.freeplane.view.swing.map.MapViewController manager = newViewController();
        manager.addMapViewChangeListener(windows);
        when(controller.getMap()).thenAnswer(call -> manager.getMap());
        doAnswer(call -> {
            manager.onRemove(call.getArgument(0));
            return null;
        }).when(mapController).closeWithoutSaving(any(MapModel.class));
        return manager;
    }

    protected MapView openView(org.freeplane.view.swing.map.MapViewController manager, MapModel map, String key)
            throws Exception {
        MapView view = newView(map, key);
        addView(manager, view);
        manager.changeToMapView(view);
        return view;
    }

    protected AFreeplaneAction menuAction(MapView view) throws Exception {
        return menuActions(Collections.singletonList(view)).get(0);
    }

    protected List<AFreeplaneAction> menuActions(List<? extends Component> views) throws Exception {
        doReturn(views).when(display).getMapViewVector();
        UserInputListenerFactory factory = new UserInputListenerFactory(mode);
        Entry entry = new Entry();
        Method create = factory.getClass().getDeclaredMethod("createMapActions", Entry.class);
        create.setAccessible(true);
        create.invoke(factory, entry);
        EntryAccessor accessor = new EntryAccessor();
        return entry.children().stream().map(accessor::getAction).collect(Collectors.toList());
    }

    protected JMenuItem menuItem(org.freeplane.core.ui.AFreeplaneAction action) {
        return new org.freeplane.core.ui.components.JAutoCheckBoxMenuItem(
                new org.freeplane.core.ui.menubuilders.action.AcceleratebleActionProvider().acceleratableAction(action));
    }

    protected List<MapView> restoreFreshViews(MapViewController manager) throws Exception {
        List<Component> originals = new ArrayList<>(manager.getMapViews());
        List<View> originalOwners = originals.stream()
                .map(view -> (View) SwingUtilities.getAncestorOfClass(View.class, view))
                .collect(Collectors.toList());
        loadLayout(saveAndCloseViews(manager));
        List<MapView> restored = new ArrayList<>();
        for (Component component : manager.getMapViews()) {
            assertFalse(originals.contains(component));
            assertFalse(originalOwners.contains(SwingUtilities.getAncestorOfClass(View.class, component)));
            restored.add((MapView) component);
        }
        return restored;
    }

    protected byte[] saveAndCloseViews(MapViewController manager) throws Exception {
        List<Component> originals = new ArrayList<>(manager.getMapViews());
        Map<URL, MapModel> maps = new HashMap<>();
        for (Component view : originals) {
            MapModel map = ((MapView) view).getMap();
            maps.put(map.getURL(), map);
            SwingUtilities.getAncestorOfClass(View.class, view).addNotify();
        }
        AtomicReference<String> saved = new AtomicReference<>();
        doAnswer(call -> { saved.set(call.getArgument(1)); return null; })
                .when(resources).setProperty(eq("openedNow_1.3.04"), anyString());
        windows.saveLayout();
        assertNotNull(saved.get());
        for (Component view : originals)
            manager.closeWithoutSaving(view);
        doAnswer(call -> {
            URL url = call.getArgument(0);
            if (!manager.tryToChangeToMapView(url)) {
                MapModel map = maps.get(url);
                openView(manager, map, map.getTitle());
            }
            return null;
        }).when(mapController).openMap(any(URL.class));
        doAnswer(call -> {
            MapModel map = call.getArgument(0);
            openView(manager, map, map.getTitle());
            return null;
        }).when(mapController).createMapView(any(MapModel.class));
        return Base64.decodeBase64(saved.get());
    }

    protected void loadLayout(byte[] bytes) {
        when(resources.getProperty("openedNow_1.3.04", null)).thenReturn(Base64.encodeBase64String(bytes));
        windows.loadLayout();
    }

    protected View dock(MapView view) {
        windows.afterViewChange(null, view);
        return (View) SwingUtilities.getAncestorOfClass(View.class, view);
    }

    protected ViewProperties observeTitle(View view) throws Exception {
        ViewProperties properties = spy(view.getViewProperties());
        field(field(view, "windowItem"), "viewProperties", properties);
        return properties;
    }

    protected TitledTabStateProperties tooltip(View view) {
        return view.getWindowProperties().getTabProperties().getTitledTabProperties().getNormalProperties();
    }

    protected TitledTabStateProperties observeTooltip(View view) throws Exception {
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
