package org.freeplane.view.swing.ui;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.core.ui.components.JAutoCheckBoxMenuItem;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.menubuilders.action.AcceleratebleActionProvider;
import org.freeplane.core.util.Compat;
import org.freeplane.features.map.MapModel;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.MapViewTitleTestSupport;
import org.junit.Test;
import org.mockito.MockedStatic;

public class MapsMenuActionTest extends MapViewTitleTestSupport {
    @Test
    public void menuClickSelectsItsViewWithEqualLabels() {
        MapView first = newView(newMap(), "one");
        MapView second = newView(newMap(), "two");
        first.putClientProperty(MapDisplayNameResolver.CUSTOMIZED_TAB_NAME_PROPERTY, "shared");
        second.putClientProperty(MapDisplayNameResolver.CUSTOMIZED_TAB_NAME_PROPERTY, "shared");
        doReturn(Arrays.asList(first, second)).when(viewManager).getMapViews();
        when(viewManager.getMapViewComponent()).thenReturn(first);
        JAutoCheckBoxMenuItem item = item(new MapsMenuAction(second));
        List<Runnable> focus = new ArrayList<>();
        try (MockedStatic<UITools> ui = mockStatic(UITools.class)) {
            ui.when(() -> UITools.executeWhenNodeHasFocus(any(Runnable.class))).thenAnswer(call -> {
                focus.add(call.getArgument(0));
                return null;
            });
            item.doClick();
            assertEquals(1, focus.size());
            second.putClientProperty(MapDisplayNameResolver.CUSTOMIZED_TAB_NAME_PROPERTY, "changed before focus");
            focus.get(0).run();
        }
        verify(viewManager).changeToMapView(second);
        verify(viewManager, never()).changeToMapView(anyString());
    }

    @Test
    public void delayedMenuClickDoesNotSelectAClosedView() {
        MapView view = newView(newMap(), "closed");
        doReturn(Collections.singletonList(view)).when(viewManager).getMapViews();
        JAutoCheckBoxMenuItem item = item(new MapsMenuAction(view));
        List<Runnable> focus = new ArrayList<>();
        try (MockedStatic<UITools> ui = mockStatic(UITools.class)) {
            ui.when(() -> UITools.executeWhenNodeHasFocus(any(Runnable.class))).thenAnswer(call -> {
                focus.add(call.getArgument(0));
                return null;
            });
            item.doClick();
            doReturn(Collections.emptyList()).when(viewManager).getMapViews();
            focus.get(0).run();
        }
        verify(viewManager, never()).changeToMapView(any(java.awt.Component.class));
        verify(viewManager, never()).changeToMapView(anyString());
    }

    @Test
    public void titleRequestUpdatesTheExistingMenuItem() {
        MapModel map = newMap();
        MapView view = newView(map, "key");
        MapsMenuAction action = new MapsMenuAction(view);
        JAutoCheckBoxMenuItem item = item(action);
        assertEquals("key", item.getText());
        map.setSaved(false);
        view.putClientProperty(MapDisplayNameResolver.CUSTOMIZED_TAB_NAME_PROPERTY, "changed");
        assertEquals("key : changed *", item.getText());
        assertEquals("MapsMenuAction.key", action.getKey());
        verify(viewManager, never()).getMapViews();
    }

    @Test
    public void rawTextTracksTheCurrentFormattedLabel() {
        MapView view = newView(newMap(), "key");
        MapsMenuAction action = new MapsMenuAction(view);
        JAutoCheckBoxMenuItem item = item(action);
        view.putClientProperty(MapDisplayNameResolver.CUSTOMIZED_TAB_NAME_PROPERTY, "&Current");
        assertEquals("key : &Current", action.getRawText());
        assertEquals("key : Current", item.getText());
    }

    @Test
    public void titleChangesReplaceMnemonicProperties() {
        MapView view = newView(newMap(), "key");
        MapsMenuAction action = new MapsMenuAction(view);
        JAutoCheckBoxMenuItem item = item(action);
        for (boolean mac : new boolean[]{false, true}) {
            try (MockedStatic<Compat> platform = mockStatic(Compat.class)) {
                platform.when(Compat::isMacOsX).thenReturn(mac);
                view.putClientProperty(MapDisplayNameResolver.CUSTOMIZED_TAB_NAME_PROPERTY, "&Alpha");
                assertEquals(mac ? 0 : 'A', item.getMnemonic());
                view.putClientProperty(MapDisplayNameResolver.CUSTOMIZED_TAB_NAME_PROPERTY, "Be&Ta");
                assertEquals(mac ? 0 : 'T', item.getMnemonic());
                assertEquals(mac ? -1 : 8, item.getDisplayedMnemonicIndex());
                view.putClientProperty(MapDisplayNameResolver.CUSTOMIZED_TAB_NAME_PROPERTY, "plain");
                assertNull(action.getValue(Action.MNEMONIC_KEY));
                assertNull(action.getValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY));
                assertEquals(0, item.getMnemonic());
                assertEquals(-1, item.getDisplayedMnemonicIndex());
            }
        }
    }

    @Test
    public void unchangedPresentationDoesNotRewriteActionProperties() {
        MapView view = newView(newMap(), "key");
        MapsMenuAction original = new MapsMenuAction(view);
        MapsMenuAction action = spy(original);
        original.dispose();
        view.addPropertyChangeListener(MapView.TITLE_REFRESH_PROPERTY, action);
        view.addPropertyChangeListener(SelectableAction.SELECTION_PROPERTY, action);
        clearInvocations(action);
        view.refreshTitle();
        verify(action, never()).putValue(anyString(), any());
    }

    @Test
    public void selectionUpdatesTheExistingCheckBoxItem() {
        MapView first = newView(newMap(), "one");
        MapView second = newView(newMap(), "two");
        when(viewManager.getMapViewComponent()).thenReturn(first);
        JAutoCheckBoxMenuItem firstItem = item(new MapsMenuAction(first));
        JAutoCheckBoxMenuItem secondItem = item(new MapsMenuAction(second));
        assertTrue(firstItem.isSelected());
        assertFalse(secondItem.isSelected());
        when(viewManager.getMapViewComponent()).thenReturn(second);
        first.notifySelectionChanged();
        second.notifySelectionChanged();
        assertFalse(firstItem.isSelected());
        assertTrue(secondItem.isSelected());
    }

    @Test
    public void disposingActionDetachesItsViewSubscriptions() {
        MapView view = newView(newMap(), "key");
        MapsMenuAction action = new MapsMenuAction(view);
        JAutoCheckBoxMenuItem item = item(action);
        action.dispose();
        action.dispose();
        when(viewManager.getMapViewComponent()).thenReturn(view);
        view.putClientProperty(MapDisplayNameResolver.CUSTOMIZED_TAB_NAME_PROPERTY, "changed");
        view.notifySelectionChanged();
        assertEquals("key", item.getText());
        assertFalse(item.isSelected());
        assertFalse(Arrays.asList(view.getPropertyChangeListeners(MapView.TITLE_REFRESH_PROPERTY)).contains(action));
        assertFalse(Arrays.asList(view.getPropertyChangeListeners(SelectableAction.SELECTION_PROPERTY)).contains(action));
    }

    private static JAutoCheckBoxMenuItem item(MapsMenuAction action) {
        return new JAutoCheckBoxMenuItem(new AcceleratebleActionProvider().acceleratableAction(action));
    }
}
