package org.freeplane.view.swing.ui;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.freeplane.core.ui.menubuilders.action.AcceleratebleActionProvider;
import org.freeplane.core.ui.menubuilders.action.IAcceleratorMap;
import org.freeplane.core.ui.menubuilders.generic.BuilderDestroyerPair;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.ui.menubuilders.generic.PhaseProcessor;
import org.freeplane.core.ui.menubuilders.generic.PhaseProcessor.Phase;
import org.freeplane.core.ui.menubuilders.generic.RecursiveMenuStructureProcessor;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;
import org.freeplane.core.ui.menubuilders.generic.SubtreeProcessor;
import org.freeplane.core.ui.menubuilders.menu.JComponentRemover;
import org.freeplane.core.ui.menubuilders.menu.JMenuItemBuilder;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.ui.IMapViewManager.MapChangeEventProperty;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.features.map.MapModel;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.MapViewTitleTestSupport;
import org.junit.Test;

public class UserInputListenerFactoryMapActionsTest extends MapViewTitleTestSupport {
    @Test
    public void mapsMenuUsesResolvedLabelsWithExistingOrderAndSelection() throws Exception {
        MapModel dirty = newMap();
        dirty.setSaved(false);
        MapView z = newView(newMap(), "z");
        MapView b = newView(dirty, "Beta");
        MapView a = newView(newMap(), "alpha");
        setCustomName(a, "shared label");
        setCustomName(z, "shared label");
        doReturn(Arrays.asList(z, b, a)).when(display).getMapViewVector();
        when(viewManager.getMapViewComponent()).thenReturn(b);
        MenuFixture fixture = new MenuFixture(z, b, a);
        Entry menu = fixture.maps;
        assertEquals("shared label", fixture.menu.getItem(0).getText());
        JMenuItem betaItem = (JMenuItem) new EntryAccessor().getComponent(menu.getChild(1));
        assertEquals("Beta *", betaItem.getText());
        assertTrue(betaItem.isSelected());
        assertEquals("shared label",
                ((JMenuItem) new EntryAccessor().getComponent(menu.getChild(2))).getText());
        assertEquals(3, menu.getChildCount());
        assertAction(menu, 0, "MapsMenuAction.alpha", "shared label", false);
        assertAction(menu, 1, "MapsMenuAction.Beta", "Beta *", true);
        assertAction(menu, 2, "MapsMenuAction.z", "shared label", false);
        dirty.setReadOnly(true);
        Entry readOnlyMenu = new MenuFixture(z, b, a).maps;
        assertAction(readOnlyMenu, 1, "MapsMenuAction.Beta", "Beta", true);
    }

    @Test
    public void titleChangesKeepExistingEntriesAndActions() throws Exception {
        MapModel map = newMap();
        NodeModel child = new NodeModel("child", map);
        map.getRootNode().insert(child);
        MapView view = newView(map, "key");
        installNodeViews(view);
        MapView unrelated = newView(newMap(), "other");
        MenuFixture fixture = new MenuFixture(view, unrelated);
        Entry entry = fixture.maps.getChild(0);
        AFreeplaneAction action = new EntryAccessor().getAction(entry);
        JMenuItem item = fixture.menu.getItem(0);
        JMenuItem otherItem = (JMenuItem) new EntryAccessor().getComponent(fixture.maps.getChild(1));
        assertEquals("other", otherItem.getText());
        clearInvocations(display, viewManager, input);
        when(resources.getEnumProperty(MapDisplayNameResolver.MAP_DISPLAY_NAME_SOURCE_PROPERTY,
                MapDisplayNameSource.FILE_NAME)).thenReturn(MapDisplayNameSource.VIEW_ROOT_TEXT);
        when(text.getShortPlainText(any(NodeModel.class), anyInt(), anyString()))
                .thenAnswer(call -> ((NodeModel) call.getArgument(0)).getText());
        changeViewRoot(view, child);
        assertEquals("child", item.getText());
        setCustomName(view, "custom");
        assertEquals("custom", item.getText());
        map.setSaved(false);
        map.fireMapChangeEvent(new MapChangeEvent(this, map, MapChangeEventProperty.MAP_VIEW_TITLE, null, null, false));
        assertEquals("custom *", item.getText());
        assertEquals("other", otherItem.getText());
        setCustomName(view, null);
        assertEquals("child *", item.getText());
        when(resources.getEnumProperty(MapDisplayNameResolver.MAP_DISPLAY_NAME_SOURCE_PROPERTY,
                MapDisplayNameSource.FILE_NAME)).thenReturn(MapDisplayNameSource.FILE_NAME);
        preferenceChanged(MapDisplayNameResolver.MAP_DISPLAY_NAME_SOURCE_PROPERTY);
        assertEquals("key *", item.getText());
        assertSame(entry, fixture.maps.getChild(0));
        assertSame(action, new EntryAccessor().getAction(entry));
        assertSame(item, fixture.menu.getItem(0));
        assertSame(otherItem, new EntryAccessor().getComponent(fixture.maps.getChild(1)));
        assertEquals(2, fixture.maps.getChildCount());
        assertEquals("other", otherItem.getText());
        assertEquals(2, fixture.menu.getItemCount());
        verify(display, never()).getMapViewVector();
        verify(viewManager, never()).getMapViews();
        verify(viewManager, never()).getViews(any(MapModel.class));
        verify(input, never()).updateMapList();
    }

    @Test
    public void menuRebuildDisposesPreviousActions() throws Exception {
        MapView view = newView(newMap(), "key");
        MenuFixture fixture = new MenuFixture(view);
        AFreeplaneAction previous = new EntryAccessor().getAction(fixture.maps.getChild(0));
        JMenuItem previousItem = fixture.menu.getItem(0);
        doAnswer(call -> {
            assertEquals(0, menuListenerCount(view));
            return null;
        }).when(mode).removeActionIfSet(anyString());
        fixture.processor.childEntriesHidden(fixture.root);
        assertEquals(0, fixture.maps.getChildCount());
        assertEquals(0, fixture.menu.getItemCount());
        assertNull(previousItem.getAction());
        setCustomName(view, "changed while hidden");
        assertEquals("key", previous.getValue(Action.NAME));
        fixture.processor.childEntriesWillBecomeVisible(fixture.root);
        AFreeplaneAction reopened = new EntryAccessor().getAction(fixture.maps.getChild(0));
        assertNotSame(previous, reopened);
        assertEquals("changed while hidden", fixture.menu.getItem(0).getText());
        assertEquals(1, menuListenerCount(view));
        fixture.processor.rebuildChildren(fixture.root);
        assertNotSame(reopened, new EntryAccessor().getAction(fixture.maps.getChild(0)));
        assertEquals(1, menuListenerCount(view));
        verify(mode, times(2)).removeActionIfSet("MapsMenuAction.key");
    }

    private long menuListenerCount(MapView view) {
        return Arrays.stream(view.getPropertyChangeListeners(MapView.TITLE_REFRESH_PROPERTY))
                .filter(listener -> listener instanceof MapsMenuAction).count();
    }

    private class MenuFixture {
        final Entry root = new Entry();
        final Entry maps = new Entry().setBuilders("navigate_maps");
        final JMenu menu = new JMenu();
        final SubtreeProcessor processor = new SubtreeProcessor(entry -> true);

        @SuppressWarnings("unchecked")
        MenuFixture(MapView... views) throws Exception {
            doReturn(Arrays.asList(views)).when(display).getMapViewVector();
            UserInputListenerFactory factory = new UserInputListenerFactory(mode);
            List<Map<String, BuilderDestroyerPair>> builders =
                    (List<Map<String, BuilderDestroyerPair>>) field(factory, "customBuilders");
            RecursiveMenuStructureProcessor actions = new RecursiveMenuStructureProcessor();
            actions.setDefaultBuilder(EntryVisitor.EMTPY);
            actions.addBuilderPair("navigate_maps", builders.get(Phase.ACTIONS.ordinal()).get("navigate_maps"));
            ResourceAccessor resourceAccessor = mock(ResourceAccessor.class);
            when(resourceAccessor.getRawText(anyString())).thenReturn("");
            when(resourceAccessor.getIntProperty(anyString(), anyInt())).thenAnswer(call -> call.getArgument(1));
            JMenuItemBuilder itemBuilder = new JMenuItemBuilder(processor, mock(IAcceleratorMap.class),
                    new AcceleratebleActionProvider(), resourceAccessor);
            RecursiveMenuStructureProcessor controls = new RecursiveMenuStructureProcessor();
            controls.setDefaultBuilderPair(itemBuilder, JComponentRemover.INSTANCE);
            processor.setProcessor(new PhaseProcessor().withPhase(Phase.ACTIONS, actions).withPhase(Phase.UI, controls));
            new EntryAccessor().setComponent(root, menu);
            root.addChild(maps);
            processor.childEntriesWillBecomeVisible(root);
        }
    }

    private static void assertAction(Entry menu, int index, String key, String label, boolean selected) {
        AFreeplaneAction action = new EntryAccessor().getAction(menu.getChild(index));
        assertEquals(key, action.getKey());
        assertEquals(label, action.getRawText());
        assertEquals(selected, action.isSelected());
    }

}
