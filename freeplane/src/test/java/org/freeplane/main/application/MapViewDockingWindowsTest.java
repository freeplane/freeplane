package org.freeplane.main.application;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.awt.Component;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import org.freeplane.api.TextWritingDirection;
import org.freeplane.features.map.MapModel;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.ui.MapDisplayNameResolver;
import org.junit.Test;
import net.infonode.docking.DockingWindow;
import net.infonode.docking.RootWindow;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;
import net.infonode.docking.properties.DockingWindowProperties;
import net.infonode.docking.properties.ViewProperties;
import net.infonode.tabbedpanel.titledtab.TitledTabStateProperties;

public class MapViewDockingWindowsTest extends MapViewDockingWindowsTestSupport {
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
        File file = new File("/tmp/title-path.mm");
        map.setURL(file.toURI().toURL());
        rename(docked, "custom");
        assertEquals("custom", docked.getTitle());
        assertEquals(TextWritingDirection.LEFT_TO_RIGHT.isolatePathSeparators(file.getAbsolutePath()),
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
        rename(first, "changed");
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
        rename(docked, "owned");
        assertEquals("owned", source.getCustomizedTabName());
        ViewProperties title = observeTitle(docked);
        List<DockingWindow> removed = new ArrayList<>();
        windows.getRootWindow().addListener(new net.infonode.docking.DockingWindowAdapter() {
            @Override
            public void windowRemoved(DockingWindow parent, DockingWindow window) {
                if (window == docked) {
                    assertNull(source.getCustomizedTabName());
                    assertFalse(Arrays.asList(source.getPropertyChangeListeners(MapView.TITLE_REFRESH_PROPERTY)).contains(windows));
                    removed.add(window);
                }
            }
        });
        windows.afterViewClose(source);
        assertEquals(Collections.singletonList(docked), removed);
        source.refreshTitle();
        verify(title, never()).setTitle(anyString());
        assertFalse(Arrays.asList(source.getPropertyChangeListeners(MapView.TITLE_REFRESH_PROPERTY)).contains(windows));
    }

    @Test
    public void tabRenamePublishesOneViewTitleRequest() throws Exception {
        MapModel map = newMap();
        MapView source = newView(map, "source");
        View docked = dock(source);
        MapView sameMap = newView(map, "other");
        View other = dock(sameMap);
        rename(other, "other name");
        List<java.beans.PropertyChangeEvent> otherRequests = recordTitleRequests(sameMap);
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
        assertEquals(1, requests.size());
        assertEquals("custom", source.getCustomizedTabName());
        assertEquals("custom", docked.getTitle());
        rename.doClick();
        rename.doClick();
        assertEquals(3, requests.size());
        assertNull(source.getCustomizedTabName());
        assertEquals("source", docked.getTitle());
        for (String literal : new String[]{"   ", " &日本語 (2) * ", "very long title which must not be shortened"}) {
            rename(docked, literal);
            assertEquals(literal, source.getCustomizedTabName());
            assertEquals(literal, docked.getTitle());
        }
        assertEquals(6, requests.size());
        assertEquals("other name", sameMap.getCustomizedTabName());
        assertTrue(otherRequests.isEmpty());
    }

    @Test
    public void dockingFocusSelectsTheViewReference() throws Exception {
        MapView first = newView(newMap(), "one");
        MapView second = newView(newMap(), "two");
        rename(dock(first), "shared");
        View target = dock(second);
        rename(target, "shared");
        when(viewManager.getMapViewComponent()).thenReturn(first);
        clearInvocations(viewManager);
        Method focus = RootWindow.class.getDeclaredMethod("setFocusedView", View.class);
        focus.setAccessible(true);
        focus.invoke(windows.getRootWindow(), target);
        verify(viewManager).changeToMapView(second);
        verify(viewManager, never()).changeToMapView(anyString());
    }

    @Test
    public void renamePopupUsesPersistentNameInstructions() throws Exception {
        View leaf = dock(newView(newMap(), "component"));
        View second = dock(newView(newMap(), "second"));
        TabWindow group = new TabWindow(new DockingWindow[]{leaf, second});
        windows.getRootWindow().setWindow(new TabWindow(new DockingWindow[]{group,
                dock(newView(newMap(), "other"))}));
        String instructions = "Empty input restores the automatic title. Names are saved with the tab's layout. Removing a group discards its name.";
        java.util.Properties english = new java.util.Properties();
        try (java.io.InputStream input = getClass().getResourceAsStream("/translations/Resources_en.properties")) {
            english.load(input);
        }
        assertEquals(instructions, english.getProperty("TabPopUpMenu.rename.persistent.tooltip"));
        assertEquals("Input new tab name:", english.getProperty("TabPopUpMenu.rename.inputDialog"));
        assertNull(english.getProperty("TabPopUpMenu.rename.tooltip"));
        for (DockingWindow owner : new DockingWindow[]{leaf, group}) {
            JPopupMenu popup = windows.getRootWindow().getPopupMenuFactory().createPopupMenu(owner);
            assertEquals(instructions, ((JMenuItem) popup.getComponent(1)).getToolTipText());
        }
        rename(leaf, "named");
        dialogs.verify(() -> JOptionPane.showInputDialog("Input new tab name:", "component"));
        rename(leaf, null);
        dialogs.verify(() -> JOptionPane.showInputDialog("Input new tab name:", "named"));
        rename(group, null);
        dialogs.verify(() -> JOptionPane.showInputDialog("Input new tab name:", null));
        verify((org.freeplane.core.resources.ResourceBundles) resources.getResources(), atLeastOnce())
                .getResourceString("TabPopUpMenu.rename.persistent.tooltip", instructions);
    }

    @Test
    public void groupRenameLeavesIndividualLabelsUnchanged() throws Exception {
        org.freeplane.view.swing.map.MapViewController manager = useRealViewManager();
        MapView first = openView(manager, newMap(), "first");
        MapView second = openView(manager, newMap(), "second");
        TabWindow group = new TabWindow(new DockingWindow[]{owner(first), owner(second)});
        windows.getRootWindow().setWindow(new TabWindow(new DockingWindow[]{group,
                dock(newView(newMap(), "other"))}));
        JMenuItem item = menuItem(menuAction(second));
        List<java.beans.PropertyChangeEvent> firstRequests = recordTitleRequests(first);
        List<java.beans.PropertyChangeEvent> secondRequests = recordTitleRequests(second);
        clearInvocations(display);
        rename(group, "group & 日本語");
        assertEquals("group & 日本語 (2)", group.getTitle());
        rename(group, "group & 日本語");
        rename(group, null);
        assertEquals("group & 日本語", CustomTabTitleProvider.getCustomizedTabName(group));
        assertEquals("second", item.getText());
        assertEquals("first", owner(first).getTitle());
        assertTrue(firstRequests.isEmpty());
        assertTrue(secondRequests.isEmpty());
        verifyNoInteractions(display);
        rename(group, "");
        assertNull(CustomTabTitleProvider.getCustomizedTabName(group));
        assertTrue(group.getTitle().contains("first"));
        assertTrue(group.getTitle().contains("second"));
    }

    @Test
    public void literalLabelsAgreeAcrossTabsMenuAndFrame() throws Exception {
        org.freeplane.view.swing.map.MapViewController manager = useRealViewManager();
        MapModel map = newMap();
        File file = new File("/tmp/literal.mm");
        map.setURL(file.toURI().toURL());
        MapView view = openView(manager, map, "key");
        org.freeplane.core.ui.AFreeplaneAction action = menuAction(view);
        JMenuItem item = menuItem(action);
        map.setSaved(false);
        rename(owner(view), " &日本語 (2) * ");
        String label = " &日本語 (2) *  *";
        assertEquals(label, tabText(owner(view)));
        assertEquals(label, item.getText());
        assertEquals(label, action.getRawText());
        verify(display).setTitle(TextWritingDirection.LEFT_TO_RIGHT.isolatePathSeparators(
                label + " - Mind map " + file.getAbsolutePath()));
        assertEquals(" &日本語 (2) * ", view.getCustomizedTabName());
    }

    @Test
    public void dirtyMarkersComeFromMapStateNotNameText() {
        MapModel map = newMap();
        MapView first = newView(map, "first");
        View a = dock(first);
        View b = dock(newView(map, "second"));
        TabWindow group = new TabWindow(new DockingWindow[]{a, b});
        windows.getRootWindow().setWindow(new TabWindow(new DockingWindow[]{group,
                dock(newView(newMap(), "other"))}));
        rename(a, "literal *");
        rename(group, "group (7) *");
        assertEquals("group (7) * (2)", group.getTitle());
        map.setSaved(false);
        first.refreshTitle();
        assertEquals("literal * *", a.getTitle());
        assertEquals("group (7) * (2) *", group.getTitle());
        map.setReadOnly(true);
        first.refreshTitle();
        assertEquals("literal *", a.getTitle());
        assertEquals("group (7) * (2)", group.getTitle());
        assertEquals("group (7) *", CustomTabTitleProvider.getCustomizedTabName(group));
    }

    @Test
    public void layoutRestoresNamesOnFreshOwners() throws Exception {
        org.freeplane.view.swing.map.MapViewController manager = useRealViewManager();
        MapModel map = savedMap("shared");
        MapView first = openView(manager, map, "shared");
        MapView second = openView(manager, map, "shared");
        rename(owner(first), "first & 日本語 *");
        rename(owner(second), "second (3)");
        TabWindow group = new TabWindow(new DockingWindow[]{owner(first), owner(second)});
        windows.getRootWindow().setWindow(new TabWindow(new DockingWindow[]{group,
                owner(openView(manager, savedMap("other"), "other"))}));
        rename(group, "named group");
        map.setSaved(false);
        first.refreshTitle();
        List<MapView> restored = restoreFreshViews(manager);
        assertEquals(3, restored.size());
        MapView restoredFirst = named(restored, "first & 日本語 *");
        MapView restoredSecond = named(restored, "second (3)");
        assertSame(map, restoredFirst.getMap());
        assertSame(map, restoredSecond.getMap());
        assertEquals("first & 日本語 * *", owner(restoredFirst).getTitle());
        assertEquals("second (3) *", owner(restoredSecond).getTitle());
        DockingWindow restoredGroup = owner(restoredFirst).getWindowParent();
        assertNotSame(group, restoredGroup);
        assertSame(restoredGroup, owner(restoredSecond).getWindowParent());
        assertEquals("named group", CustomTabTitleProvider.getCustomizedTabName(restoredGroup));
        assertEquals("named group (2) *", restoredGroup.getTitle());
        assertNull(named(restored, null).getCustomizedTabName());
    }

    @Test
    public void customNameMatchingAutomaticTextRemainsCustom() throws Exception {
        org.freeplane.view.swing.map.MapViewController manager = useRealViewManager();
        MapView view = openView(manager, savedMap("same"), "same");
        rename(owner(view), "same");
        MapView restored = restoreFreshViews(manager).get(0);
        when(resources.getEnumProperty(MapDisplayNameResolver.MAP_DISPLAY_NAME_SOURCE_PROPERTY,
                org.freeplane.view.swing.ui.MapDisplayNameSource.FILE_NAME))
                .thenReturn(org.freeplane.view.swing.ui.MapDisplayNameSource.VIEW_ROOT_TEXT);
        when(text.getShortPlainText(any(), anyInt(), anyString())).thenReturn("changed root");
        preferenceChanged(MapDisplayNameResolver.MAP_DISPLAY_NAME_SOURCE_PROPERTY);
        assertEquals("same", restored.getCustomizedTabName());
        assertEquals("same", owner(restored).getTitle());
    }

    @Test
    public void clearedNamesStayAutomaticAfterRestoration() throws Exception {
        org.freeplane.view.swing.map.MapViewController manager = useRealViewManager();
        MapView first = openView(manager, savedMap("one"), "one");
        MapView second = openView(manager, savedMap("two"), "two");
        TabWindow group = new TabWindow(new DockingWindow[]{owner(first), owner(second)});
        windows.getRootWindow().setWindow(new TabWindow(new DockingWindow[]{group,
                owner(openView(manager, savedMap("other"), "other"))}));
        rename(owner(first), "named");
        rename(owner(first), "");
        rename(group, "group");
        rename(group, "");
        List<MapView> restored = restoreFreshViews(manager);
        for (MapView view : restored) {
            assertNull(view.getCustomizedTabName());
            assertNull(CustomTabTitleProvider.getCustomizedTabName(owner(view).getWindowParent()));
            assertEquals(view.getName(), owner(view).getTitle());
        }
    }

    @Test
    public void oldLayoutsRestoreAutomaticNamesWithoutGuessing() throws Exception {
        org.freeplane.view.swing.map.MapViewController manager = useRealViewManager();
        MapView first = openView(manager, savedMap("one"), "one");
        MapView second = openView(manager, savedMap("two"), "two");
        owner(first).getViewProperties().setTitle("arbitrary old name *");
        owner(second).getViewProperties().setTitle("another old name");
        TabWindow group = new TabWindow(new DockingWindow[]{owner(first), owner(second)});
        group.putClientProperty("customizedTabName", "old group name");
        windows.getRootWindow().setWindow(group);
        List<MapView> restored = restoreFreshViews(manager);
        assertEquals(2, restored.size());
        for (MapView view : restored) {
            assertNull(view.getCustomizedTabName());
            assertEquals(view.getName(), owner(view).getTitle());
        }
        assertSame(owner(restored.get(0)).getWindowParent(), owner(restored.get(1)).getWindowParent());
        assertNull(CustomTabTitleProvider.getCustomizedTabName(owner(restored.get(0)).getWindowParent()));
    }

    @Test
    public void restoredNamesDoNotOverwriteLaterRenames() throws Exception {
        org.freeplane.view.swing.map.MapViewController manager = useRealViewManager();
        MapView view = openView(manager, savedMap("one"), "one");
        rename(owner(view), "original");
        MapView restored = restoreFreshViews(manager).get(0);
        rename(owner(restored), "later & *");
        preferenceChanged(MapDisplayNameResolver.MAP_DISPLAY_NAME_SOURCE_PROPERTY);
        restored.getMap().setSaved(false);
        restored.refreshTitle();
        restored.getMap().setURL(new File("/tmp/changed.mm").toURI().toURL());
        restored.refreshTitle();
        assertEquals("later & *", restored.getCustomizedTabName());
        assertEquals("later & * *", owner(restored).getTitle());
        assertTrue(tooltip(owner(restored)).getToolTipText().contains("changed.mm"));
    }

    @Test
    public void layoutDoesNotSerializeCustomTitleProvider() throws Exception {
        MapView view = newView(savedMap("one"), "one");
        View leaf = dock(view);
        rename(leaf, "literal & 日本語 (9) *");
        leaf.addNotify();
        java.util.concurrent.atomic.AtomicReference<String> saved = new java.util.concurrent.atomic.AtomicReference<>();
        doAnswer(call -> { saved.set(call.getArgument(1)); return null; })
                .when(resources).setProperty(eq("openedNow_1.3.04"), anyString());
        windows.saveLayout();
        byte[] bytes = org.apache.commons.codec.binary.Base64.decodeBase64(saved.get());
        String stream = new String(bytes, java.nio.charset.StandardCharsets.ISO_8859_1);
        assertFalse(stream.contains("CustomTabTitleProvider"));
        assertFalse(stream.contains("MapViewDockingWindows"));
        assertTrue(stream.contains("customizedTabName"));
        leaf.removeNotify();
        org.freeplane.view.swing.map.MapViewController manager = useRealViewManager();
        addView(manager, view);
        assertEquals("literal & 日本語 (9) *", restoreFreshViews(manager).get(0).getCustomizedTabName());
    }

    @Test
    public void nativeReaderSkipsUnknownNameProperty() throws Exception {
        net.infonode.properties.propertymap.PropertyMap values =
                net.infonode.properties.propertymap.PropertyMapFactory.create(DockingWindowProperties.PROPERTIES);
        CustomTabTitleProvider.CUSTOMIZED_TAB_NAME_PROPERTY.set(values, "literal & 日本語 (9) *");
        java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            values.write(out, true);
            out.writeInt(729);
        }
        net.infonode.properties.propertymap.PropertyMap unknown =
                net.infonode.properties.propertymap.PropertyMapFactory.create(
                        new net.infonode.properties.propertymap.PropertyMapGroup("old", ""));
        try (ObjectInputStream in = new ObjectInputStream(new java.io.ByteArrayInputStream(bytes.toByteArray()))) {
            unknown.read(in);
            assertEquals(729, in.readInt());
        }
    }

    @Test
    public void malformedLayoutUsesExistingEmptyFallback() {
        when(resources.getProperty("openedNow_1.3.04", null)).thenReturn("not a layout");
        windows.loadLayout();
        MapView view = newView(newMap(), "after failure");
        View leaf = dock(view);
        rename(leaf, "works");
        assertEquals("works", leaf.getTitle());
    }

    @Test
    public void ineligibleViewsDoNotRestoreCustomNames() throws Exception {
        org.freeplane.view.swing.map.MapViewController manager = useRealViewManager();
        MapView eligible = openView(manager, savedMap("eligible"), "eligible");
        MapView unsaved = openView(manager, newMap(), "no URL");
        MapModel documentation = savedMap("documentation");
        documentation.addExtension(org.freeplane.features.map.DocuMapAttribute.INSTANCE);
        MapView doc = openView(manager, documentation, "documentation");
        rename(owner(eligible), "eligible name");
        rename(owner(unsaved), "unsaved name");
        rename(owner(doc), "documentation name");
        List<MapView> restored = restoreFreshViews(manager);
        assertEquals(1, restored.size());
        assertEquals("eligible name", restored.get(0).getCustomizedTabName());
        assertEquals(1, windows.getMapViewVector().size());
    }

    @Test
    public void survivingGroupKeepsItsNameWhenChildrenMove() {
        MapView first = newView(newMap(), "first");
        View a = dock(first);
        View b = dock(newView(newMap(), "second"));
        View c = dock(newView(newMap(), "third"));
        View other = dock(newView(newMap(), "other"));
        TabWindow group = new TabWindow(new DockingWindow[]{a, b, c});
        TabWindow outer = new TabWindow(new DockingWindow[]{group, other});
        windows.getRootWindow().setWindow(outer);
        rename(group, "survives");
        outer.addTab(c);
        assertSame(group, a.getWindowParent());
        assertEquals("survives (2)", group.getTitle());
        first.getMap().setSaved(false);
        first.refreshTitle();
        assertEquals("survives (2) *", group.getTitle());
    }

    @Test
    public void movingAGroupPreservesItsOwnerAndName() {
        MapView first = newView(newMap(), "first");
        View a = dock(first);
        View b = dock(newView(newMap(), "second"));
        TabWindow group = new TabWindow(new DockingWindow[]{a, b});
        TabWindow source = new TabWindow(new DockingWindow[]{group, dock(newView(newMap(), "other"))});
        TabWindow destination = new TabWindow(new DockingWindow[]{dock(newView(newMap(), "destination"))});
        windows.getRootWindow().setWindow(new SplitWindow(true, .5f, source, destination));
        rename(group, "moving");
        java.util.concurrent.atomic.AtomicBoolean temporarilyParentless = new java.util.concurrent.atomic.AtomicBoolean();
        windows.getRootWindow().addListener(new net.infonode.docking.DockingWindowAdapter() {
            @Override
            public void windowRemoved(DockingWindow parent, DockingWindow removed) {
                if (removed == group) {
                    assertNull(group.getWindowParent());
                    assertEquals(2, group.getChildWindowCount());
                    assertEquals("moving", CustomTabTitleProvider.getCustomizedTabName(group));
                    temporarilyParentless.set(true);
                }
            }
        });
        destination.addTab(group);
        assertTrue(temporarilyParentless.get());
        assertSame(group, a.getWindowParent());
        assertEquals("moving", CustomTabTitleProvider.getCustomizedTabName(group));
        assertEquals("moving (2)", group.getTitle());
        assertEquals(1, Arrays.stream(first.getPropertyChangeListeners(MapView.TITLE_REFRESH_PROPERTY))
                .filter(listener -> listener == windows).count());
    }

    @Test
    public void eliminatedGroupDoesNotTransferItsName() throws Exception {
        org.freeplane.view.swing.map.MapViewController manager = useRealViewManager();
        MapView first = openView(manager, savedMap("one"), "one");
        MapView second = openView(manager, savedMap("two"), "two");
        TabWindow group = new TabWindow(new DockingWindow[]{owner(first), owner(second)});
        TabWindow outer = new TabWindow(new DockingWindow[]{group,
                owner(openView(manager, savedMap("other"), "other"))});
        windows.getRootWindow().setWindow(outer);
        rename(group, "discard");
        rename(owner(first), "first name");
        outer.addTab(owner(second));
        assertNull(group.getWindowParent());
        assertEquals(0, group.getChildWindowCount());
        assertNull(CustomTabTitleProvider.getCustomizedTabName(group));
        assertEquals("first name", first.getCustomizedTabName());
        assertNull(second.getCustomizedTabName());
        List<MapView> restored = restoreFreshViews(manager);
        for (MapView view : restored)
            assertNotEquals("discard", CustomTabTitleProvider.getCustomizedTabName(owner(view).getWindowParent()));
        assertEquals("first name", named(restored, "first name").getCustomizedTabName());
    }

    @Test
    public void closingAndRecreatingSameUrlDoesNotReuseNames() throws Exception {
        org.freeplane.view.swing.map.MapViewController manager = useRealViewManager();
        MapModel map = savedMap("same");
        MapView old = openView(manager, map, "same");
        rename(owner(old), "old name");
        manager.closeWithoutSaving(old);
        MapView fresh = openView(manager, map, "same");
        assertNull(fresh.getCustomizedTabName());
        assertEquals("same", owner(fresh).getTitle());
    }

    @Test
    public void changedUrlKeepsNamesAndUpdatesPaths() throws Exception {
        org.freeplane.view.swing.map.MapViewController manager = useRealViewManager();
        MapView view = openView(manager, savedMap("old"), "old");
        MapView other = openView(manager, savedMap("other"), "other");
        TabWindow group = new TabWindow(new DockingWindow[]{owner(view), owner(other)});
        windows.getRootWindow().setWindow(new TabWindow(new DockingWindow[]{group,
                owner(openView(manager, savedMap("outside"), "outside"))}));
        manager.changeToMapView(view);
        rename(group, "group name");
        rename(owner(view), "custom");
        File file = new File("/tmp/new.mm");
        view.getMap().setURL(file.toURI().toURL());
        manager.updateMapViewName();
        assertEquals(view.getMap().getTitle(), view.getName());
        assertEquals("custom", view.getCustomizedTabName());
        assertEquals("custom", owner(view).getTitle());
        assertEquals(TextWritingDirection.LEFT_TO_RIGHT.isolatePathSeparators(file.getAbsolutePath()),
                tooltip(owner(view)).getToolTipText());
        assertEquals("group name", CustomTabTitleProvider.getCustomizedTabName(group));
        assertEquals("group name (2)", group.getTitle());
        assertNull(other.getCustomizedTabName());
        verify(input, atLeastOnce()).updateMapList();
    }

    @Test
    public void renamesDoNotChangeMindMapContents() throws Exception {
        MapModel map = savedMap("contents");
        MapView view = newView(map, "contents");
        View a = dock(view);
        View b = dock(newView(map, "contents<2>"));
        TabWindow group = new TabWindow(new DockingWindow[]{a, b});
        windows.getRootWindow().setWindow(new TabWindow(new DockingWindow[]{group,
                dock(newView(newMap(), "other"))}));
        org.freeplane.features.map.MapController writer = mock(org.freeplane.features.map.MapController.class);
        when(writer.getModeController()).thenReturn(mode);
        org.freeplane.core.io.WriteManager writes = new org.freeplane.core.io.WriteManager();
        when(writer.getWriteManager()).thenReturn(writes);
        org.freeplane.features.map.MapWriter mapWriter = new org.freeplane.features.map.MapWriter(writer);
        writes.addElementWriter("map", mapWriter);
        writes.addAttributeWriter("map", mapWriter);
        String before = mapXml(mapWriter, map);
        org.freeplane.features.map.NodeModel root = map.getRootNode();
        java.util.Map<?, ?> extensions = new java.util.HashMap<>(map.getExtensions());
        rename(a, "individual");
        rename(group, "group");
        rename(a, "");
        rename(group, "");
        assertTrue(map.isSaved());
        assertSame(root, map.getRootNode());
        assertEquals("root", root.getText());
        assertEquals(extensions, map.getExtensions());
        assertEquals(before, mapXml(mapWriter, map));
        verify(mapController, never()).mapSaved(any(MapModel.class), anyBoolean());
    }

    private String mapXml(org.freeplane.features.map.MapWriter writer, MapModel map) throws Exception {
        java.io.StringWriter output = new java.io.StringWriter();
        writer.writeMapAsXml(map, output, org.freeplane.features.map.MapWriter.Mode.FILE,
                org.freeplane.features.map.clipboard.MapClipboardController.CopiedNodeSet.ALL_NODES, false);
        return output.toString();
    }

    private MapModel savedMap(String filename) throws Exception {
        MapModel map = newMap();
        map.setURL(new File("/tmp/" + filename + ".mm").toURI().toURL());
        return map;
    }

    private View owner(MapView view) {
        return (View) SwingUtilities.getAncestorOfClass(View.class, view);
    }

    private MapView named(List<MapView> views, String name) {
        return views.stream().filter(view -> java.util.Objects.equals(name, view.getCustomizedTabName()))
                .findFirst().orElseThrow(AssertionError::new);
    }

    private String tabText(View owner) throws Exception {
        Method tab = DockingWindow.class.getDeclaredMethod("getTab");
        tab.setAccessible(true);
        return ((net.infonode.tabbedpanel.titledtab.TitledTab) tab.invoke(owner)).getText();
    }

}
