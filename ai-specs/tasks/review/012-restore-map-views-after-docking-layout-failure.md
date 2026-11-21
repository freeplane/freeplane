# Task: Restore map views after a docking layout failure

- **Ticket:** github:freeplane/freeplane#3040
- **Scope:**
  - When startup cannot restore the saved docking layout, keep the map
    views that loaded and show them in one tab bar in the main window.
  - Leave successful restoration of the saved docking layout unchanged.
    Do not diagnose or fix the reported focus-index mismatch in this task.
- **Motivation:**
  - The reported startup error replaces a partially restored docking layout
    with an empty layout. Losing the saved docking layout should not also
    hide maps that Freeplane has already opened.
- **Scenario:**
  - Freeplane restarts with saved map views in several tab groups. The map
    views load, but restoration of the docking layout fails. One tab bar
    shows all the loaded map views, including separately named views of the
    same map. Tab groups and saved focus are not restored.
  - If a saved map cannot be opened, it has no tab. The log distinguishes
    that map-open failure from the docking layout failure, and the other
    loaded map views remain available.
- **Constraints:**
  - Do not change InfoNode, the `openedNow_1.3.04` format, the rules for
    which views are saved, or the existing startup preference checks.
  - Do not reopen map files to recover the docking layout. Do not change
    map files or rewrite `openedNow_1.3.04` during recovery. Normal later
    saves still write the then-current layout.
- **Briefing:**
  - Start with `MapViewDockingWindows.loadLayout()` and
    `MapViewSerializer.readView()` in `org.freeplane.main.application`.
    Read the InfoNode sources in `freeplane/lib/idw-gpl-1.6.1-sources.jar`
    for read order and docking reparenting. Task
    [009](../review/009-refactor-map-view-titles-and-preserve-custom-tab-names.md)
    established custom-name ownership; it is not changed by this task.
- **Research:**

  ```plantuml
  @startuml
  set separator none
  package org.freeplane.main.application {
    class MapViewDockingWindows {
      + loadLayout(): void
    }
    class MapViewSerializer {
      + readView(in: ObjectInputStream): View
      + removeDummyViews(): void
    }
  }
  MapViewDockingWindows --> MapViewSerializer : removes dummy views after reading
  @enduml
  ```

  ```plantuml
  @startuml
  participant MapViewDockingWindows
  participant RootWindow
  participant MapViewSerializer
  participant MapViewController
  MapViewDockingWindows -> RootWindow : read(saved layout)
  loop saved view entries
    RootWindow -> MapViewSerializer : readView(in)
    MapViewSerializer -> MapViewController : getMapViewComponent()\nafter opening the saved URL
    MapViewSerializer --> RootWindow : View or dummy View
  end
  RootWindow -> RootWindow : install docking windows
  RootWindow -> RootWindow : readLocations(in, rootWindow, version)
  RootWindow --> MapViewDockingWindows : NullPointerException in #3040
  MapViewDockingWindows -> RootWindow : read(emptyConfigurations)
  MapViewDockingWindows -> MapViewSerializer : removeDummyViews()
  @enduml
  ```

  - The stack trace for build `f49b56b030` fails at
    `DockingWindow.readLocations():1402`. InfoNode is looking up a saved
    last-focused child in a tab container, not reading a custom tab name.
    InfoNode has already called the saved-view reader and installed the
    docking windows. The trace does not identify why the index is invalid
    or establish that every map file opened. The failing saved layout is
    not available for this task.
  - While layout loading is active, Freeplane registers each new `MapView`
    without docking it through the normal new-view path. Clearing the
    docking layout does not make those views cease to be registered. The
    maps menu is therefore not evidence that a view has a reachable tab.
  - `MapController.openMap(URL)` does not return a view. In mind map mode,
    `MapLoader` can return without selecting the requested map, for example
    after cancellation of the alternative-file selection. The serializer
    currently assumes the current selection belongs to the requested URL.
  - InfoNode's `TabWindow(DockingWindow[])` accepts existing docking windows
    in the supplied order. Adding an existing `View` detaches it from its
    former docking parent; it does not close its `MapView`. The empty
    layout read resets the main, minimized, and floating docking layout.
  - `MapViewDockingWindowsTest` exercises the real layout reader and writer
    with mocked map opening. Its invalid-stream test fails before any view
    has been read; its name-persistence tests exercise successful reads.
  - Documents checked: root `README.md` has no layout-restore contract;
    `freeplane/doc/freeplaneUserGuide.mm` describes tab switching and split
    views, not failed startup restoration. No project glossary or document
    list was found. The layout recovery belongs in the release notes in
    `freeplane/doc/history_en.txt`.
- **Analysis:**
  - Prefer one tab bar over preserving a partial docking layout because the
    layout can fail after map views load without proving that every loaded
    view is attached to it.
  - Reuse each restored docking `View` because a custom tab name belongs to
    that view's docking-window owner, not to the map URL.
- **Design:**

  ```plantuml
  @startuml
  set separator none
  package "Docking layout" {
    package org.freeplane.main.application {
      class "<plain>**±**</plain> MapViewDockingWindows" as MapViewDockingWindows {
        + <plain>**±**</plain> loadLayout(): void
      }
      class "<plain>**±**</plain> MapViewSerializer" as MapViewSerializer {
        + <plain>**±**</plain> readView(in: ObjectInputStream): View
        ~ <plain>**+**</plain> takeRestoredViews(): List<View>
        + <plain>**=**</plain> removeDummyViews(): void
      }
    }
    package net.infonode.docking {
      class "<plain>**=**</plain> View" as View
    }
  }
  MapViewDockingWindows -down-> MapViewSerializer : consumes restored views
  MapViewSerializer -down-> View : records existing owners until consumed
  @enduml
  ```

  ```plantuml
  @startuml
  participant MapViewDockingWindows
  participant RootWindow
  participant MapViewSerializer
  participant IMapViewManager
  participant TabWindow
  MapViewDockingWindows -> RootWindow : read(saved layout)
  loop saved view entries
    RootWindow -> MapViewSerializer : readView(in)
    opt openMap returns for a saved URL
      MapViewSerializer -> IMapViewManager : tryToChangeToMapView(saved URL)
      IMapViewManager --> MapViewSerializer : requested view selected or unavailable
    end
    MapViewSerializer --> RootWindow : existing read result with real views recorded
  end
  RootWindow --> MapViewDockingWindows : success or exception
  MapViewDockingWindows -> MapViewSerializer : takeRestoredViews()
  opt saved layout read threw an exception
    MapViewDockingWindows -> RootWindow : read(emptyConfigurations)
    opt reset succeeded and loaded views exist
      create TabWindow
      MapViewDockingWindows -> TabWindow : TabWindow(loaded docking views)
      MapViewDockingWindows -> RootWindow : setWindow(tabWindow)
      MapViewDockingWindows -> TabWindow : getSelectedWindow()
      MapViewDockingWindows -> IMapViewManager : changeToMapView(selected tab's MapView)
    end
  end
  MapViewDockingWindows -> MapViewSerializer : removeDummyViews()
  @enduml
  ```

  - Record only a non-null real `View` created by `readView()` for a saved
    entry. Read and keep its identity in saved view-entry order. Do not
    deduplicate by URL. Before wrapping a selected map view, verify the
    requested URL with the existing view-manager lookup; an unrelated
    current selection must not become a restored view. Log an attempted
    map-open failure separately using the saved URL and the exception, if
    present. Ineligible entries and null URLs remain dummy views without
    a map-open failure warning.
  - Consume and clear the list on exit from each read attempt, including
    success. The list does not change the saved format or become part of
    the live view registry. The catch applies to exceptions from reading
    the saved layout only, not to exceptions from running the fallback or
    from refreshing titles. Do not inspect exception messages or stack
    frames to decide whether to recover.
  - Keep the layout-loading guard set until reset, reparenting, selection,
    and the existing dummy-view removal and provider installation finish.
    Do not call Freeplane's map-close path on recovered views. If no view
    was loaded, keep the empty layout; if the empty-layout reset throws an
    `IOException`, log it separately and do not attach tabs to that layout.
    Preserve normal startup selection after `loadLayout()` returns.
  - The one tab bar contains each loaded view once. If only one view
    loaded, show it using the normal one-map tab visibility policy.
    Preserve custom names whose properties were read on the individual
    docking views. Do not restore tab groups, splits, floating or minimized
    positions, saved maximization, or saved focus in the fallback. Do not
    add a dialog.
  - **Living project documents:**
    - `freeplane/doc/history_en.txt` — release readers; `update`: state
      that loaded map views remain available as tabs when the saved
      docking layout cannot be restored.
- **Test specification:**
  - **Automated tests:**
    - **±** `org.freeplane.main.application.MapViewDockingWindowsTest`
      - **+** `invalidSavedFocusRestoresLoadedMapViewsInOneTabBar`:
        serialize a native layout with two individually named views of one
        saved map in a named group, and another saved map outside it. In a
        fixture with no minimized, floating, or maximized windows, replace
        the group's saved focus index with its child count. Assert that
        the native read reaches the logged invalid-index failure and that
        the three original restored docking views become direct tabs in
        saved view-entry order. Assert their tab text, their selection by
        tab, and that recovery makes no additional map-open requests and
        creates no duplicate maps-menu entries.
      - **+** `layoutFailureBeforeDockingStillRestoresLoadedMapViews`:
        use a native stream that ends after a complete saved view entry
        but before InfoNode installs the docking windows. Assert that
        the view from that entry is the selected view in the main window,
        despite never having been attached to the failed layout.
      - **+** `mapThatDoesNotOpenIsNotReplacedWithThePreviouslySelectedMap`:
        leave the preceding saved map selected when opening a later saved
        URL returns without selecting a view. On a layout failure after
        those entries are read, only the real views become tabs. Assert
        no second tab of the preceding map and a separate warning for the
        unopened URL.
      - **+** `failedLayoutDoesNotReuseViewsFromAnEarlierRead`:
        complete one read, close its maps normally, then load an invalid
        layout containing no readable views on the same window controller.
        Assert that no prior docking view reappears.
      - **=** `layoutRestoresNamesOnFreshOwners`: a successful read keeps
        the nested group, individual names, and group name.
      - **=** `malformedLayoutUsesExistingEmptyFallback`: an unreadable
        layout with no loaded views still permits opening a map normally.
- **Implementation notes:**
  - **Tradeoffs:**
    - The regression tests use the native InfoNode writer and reader with
      generated layouts. Changing one saved focus index reaches the same
      InfoNode failure as the report without relying on the reporter's
      configuration. Map opening is mocked, so the tests exercise docking
      recovery rather than file opening or desktop startup.
