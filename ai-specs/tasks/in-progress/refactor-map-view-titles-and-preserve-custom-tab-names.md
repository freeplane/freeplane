# Task: Refactor map view titles and preserve custom tab names

- **Ticket:** github:freeplane/freeplane#3014
- **Scope:**
  - Refactor the current implementation, not the changes proposed in
    [PR #3015](https://github.com/freeplane/freeplane/pull/3015).
    Separate map URLs, view-selection keys, automatic labels, custom tab
    names, dirty markers, and title refresh responsibilities.
  - Use the same resolved individual-view label in its docking tab,
    maps-menu entry, and main-window title. Retain file-path context
    separately from that label.
  - Refresh only displays affected by a change: one view when its current
    view root changes, views of the same map when shared map data changes,
    and all affected views when label preferences change. Update docking
    ancestors when a contained view's displayed title or dirty state
    changes. Updating the frame title must not implicitly refresh all tabs.
  - Fix [issue #3014](https://github.com/freeplane/freeplane/issues/3014):
    preserve custom names of individual tabs and group tabs when their
    saved docking-window owners are restored after application restart,
    and preserve those owners and names during external-file reload.
  - Keep this as one task with a standalone behavior-preserving refactoring
    subtask followed by the title behavior changes and defect fixes. Each
    subtask includes its own automated tests and affected documentation or
    UI-text changes, and must leave a coherent, releasable result.
- **Motivation:**
  - The current code uses `MapView.getName()` both as a unique string for
    selecting a view and as input for displayed filename labels. Custom
    tab names follow different paths for individual tabs and tabs
    containing multiple maps.
  - `setMapTitles()` refreshes the frame title, all docking-tab titles, and
    the maps menu even when a change affects only one view or one map.
    Some operations request that broad refresh more than once.
  - Persistence reconstructs custom names from displayed text, while
    reload destroys the docking-window owner. A separate metadata format
    would add title-matching state without resolving these responsibilities.
- **Constraints:**
  - Retain custom names for both individual tabs and group tabs. A custom
    tab name belongs to its docking-window owner, not to a map URL or a
    shared `MapModel`. A surviving group container keeps its custom name.
    If restructuring eliminates that container, discard its custom name
    rather than transfer it to a remaining map or another container. For
    example, dragging B out of named group A+B leaves A with its own tab
    name if the group container is eliminated. A newly created container
    does not inherit an eliminated container's name merely because it
    contains the same maps.
  - Use `MapView` references for internal view selection. Preserve supported
    string-selection and scripting contracts; do not use displayed labels
    as replacement selection keys.
  - Derive dirty markers and map counts from current state. Do not recover
    that state, or whether a name is custom, by parsing displayed text.
  - Persist custom tab names through the existing InfoNode layout
    serialization, not a separate property containing paths and signatures.
    Select and verify the exact serialization mechanism during detailed
    planning; a serializable title provider is not yet verified.
  - Treat reload as replacement that preserves docking-window ownership,
    rather than ordinary close followed by unrelated open and URL-based
    title recovery. Reuse existing map-replacement capabilities where they
    satisfy the required lifecycle behavior.
  - Complete detailed planning and obtain approval for the current subtask
    before implementation. The second subtask remains in backlog until
    current. Task preparation does not authorize implementation or GitHub
    actions.
- **Briefing:**
  - The Swing application uses InfoNode docking windows around map views.
    Map-view selection and display-name resolution are separate concerns;
    external-file loading and layout serialization also affect tab state.

## Subtask: Separate view selection and title refresh responsibilities

- **Status:** review
- **Scope:**
  - Separate internal view selection from displayed text, preserving
    existing supported string-selection contracts and currently displayed
    names, automatic-label preferences, and title formatting.
  - Separate frame-title updates, docking-tab updates, and maps-menu
    updates. Use the changed view or map to avoid refreshing unrelated
    titles; global label-preference changes may refresh all views.
  - Remove duplicate refresh requests and avoid rewriting unchanged
    displayed values. Include automated coverage for affected-view
    selection, unchanged visible results, view selection, and existing
    automatic-label behavior.
  - Deliver a standalone refactoring that remains acceptable without the
    second subtask. Do not introduce its custom-name persistence or
    main-window label behavior changes in this subtask.
- **Motivation:**
  - Display-text changes should not determine which view an action selects.
    Explicit refresh responsibilities remove the hidden connection between
    setting a frame title and refreshing every docking tab.
- **Constraints:**
  - No intended externally observable behavior change in this subtask.
    Include any supporting documentation changes with the refactoring.
  - Before production edits, verify existing resolver/coordinator tests
    and tests for existing title formatting, automatic labels, selection
    keys, and maps-menu output against the starting code.
  - Leave layout serialization, custom-name ownership, initial custom-name
    recovery rules, rename cancellation/clearing, and external-file reload
    unchanged. Their replacement belongs to the second subtask.
- **Briefing:**
  - Application code is in `freeplane`; `freeplane_plugin_codeexplorer`
    also requests title updates after replacing a view's map. Swing work
    and its event ordering must remain on the existing dispatch path.
  - Existing resolver tests inject dependencies. Map-view lifecycle tests
    restore the global controller after using real Swing map/node views
    with mocked application services. InfoNode sources are available in
    `freeplane/lib/idw-gpl-1.6.1-sources.jar`.
- **Research:**

  ```plantuml
  @startuml
  set separator none
  package org.freeplane {
    package features.ui {
      interface IMapViewManager {
        + changeToMapView(view: Component): boolean
        + changeToMapView(mapViewDisplayName: String): boolean
        + getMapKeys(): List<String>
        + getMaps(): Map<String, MapModel>
        + getViews(map: MapModel): List<Component>
      }
    }
    package view.swing {
      package map {
        class MapViewController
        class MapView
      }
      package ui {
        class MapsMenuAction {
          + MapsMenuAction(command: String, title: String)
          + actionPerformed(menuEvent: ActionEvent): void
        }
      }
    }
  }
  MapViewController ..|> IMapViewManager
  MapViewController --> MapView : stores ordered views and selection
  MapsMenuAction --> IMapViewManager : selects by action-key suffix
  @enduml
  ```

  ```plantuml
  @startuml
  set separator none
  package org.freeplane {
    package features.ui {
      interface ViewController {
        + setTitle(frameTitle: String): void
      }
    }
    package view.swing {
      package map {
        class MapViewController {
          + setMapTitles(): void
        }
      }
      package ui {
        class MapDisplayNameResolver {
          {static} + resolveBaseLabel(mapView: MapView): String
        }
      }
    }
    package main.application {
      class ApplicationViewController {
        + setTitle(frameTitle: String): void
      }
      class MapViewDockingWindows {
        + setTitle(): void
      }
    }
  }
  ApplicationViewController ..|> ViewController
  MapViewController --> ViewController : sets frame title
  ApplicationViewController --> MapViewDockingWindows : refreshes all tabs in setTitle
  MapViewDockingWindows --> MapDisplayNameResolver : resolves tab labels
  @enduml
  ```

  ```plantuml
  @startuml
  participant MapDisplayNameRefreshCoordinator
  participant MapViewController
  participant ApplicationViewController
  participant MapViewDockingWindows
  participant UserInputListenerFactory
  participant SubtreeProcessor
  MapDisplayNameRefreshCoordinator -> MapViewController : setMapTitles()
  MapViewController -> ApplicationViewController : setTitle(frameTitle)
  ApplicationViewController -> MapViewDockingWindows : setTitle()
  loop every map view
    MapViewDockingWindows -> MapViewDockingWindows : update title and path tooltip
  end
  MapViewController -> UserInputListenerFactory : updateMapList()
  UserInputListenerFactory -> SubtreeProcessor : rebuildChildren(entry)
  SubtreeProcessor -> SubtreeProcessor : destroyChildren(entry)
  SubtreeProcessor -> SubtreeProcessor : buildChildren(entry)
  @enduml
  ```

  ```plantuml
  @startuml
  set separator none
  package org.freeplane {
    package core.resources {
      abstract class ResourceController {
        + addPropertyChangeListener(\nlistener: IFreeplanePropertyListener\n)\n: void
      }
    }
    package features.map {
      class MapModel {
        + addMapChangeListener(listener: IMapChangeListener): void
        + removeMapChangeListener(listener: IMapChangeListener): void
        + fireMapChangeEvent(event: MapChangeEvent): void
        + setSaved(saved: boolean): void
      }
    }
    package view.swing.map {
      class MapView {
        + setMap(viewedMap: MapModel): void
        + mapChanged(event: MapChangeEvent): void
        + propertyChanged(\npropertyName: String,\nnewValue: String,\noldValue: String\n)\n: void
      }
      class MapViewController {
        + newMapView(\nmap: MapModel,\nmodeController: ModeController\n)\n: void
      }
    }
  }
  MapModel --> MapView : map-specific callbacks; setMap rebinds
  MapViewController --> ResourceController : registers each created view
  ResourceController --> MapView : property callbacks
  @enduml
  ```

  - `MapViewController` assigns unique component names using `<2>`,
    `<3>`, and subsequent suffixes. String selection, `getMapKeys()`,
    `getMaps()`, and opened-file checks use these names. Its name-assignment
    method contains the same `setName()` call twice.
  - `MapDisplayNameResolver` reads the component name for `FILE_NAME`,
    uses current-root text or the nearest ancestor bookmark for the other
    sources, and gives an individual custom name precedence. Filename and
    bookmark labels are shortened; individual custom names are not.
  - The frame uses the component name, workspace, dirty marker, mode,
    editing/read-only suffix, and absolute path. The maps menu sorts by
    component name and displays that name plus ` : ` and the resolved
    dirty-marked label when different. These are not yet one shared label.
  - `MAP_VIEW_ROOT` events originate from the changed `MapView` and carry
    its map. Bookmark/URL events carry a map; node events expose nodes or
    parents from which the map is available. `moveNodeAndItsClones()`
    rejects moves across maps. Current coordinator tests assert only global
    refresh counts, sometimes with no event map.
  - Rename changes the individual view's client property, which already
    triggers the coordinator, and separately requests a global refresh.
    `SaveAsAction` requests another global refresh after `saveAs()` has
    already emitted URL/saved-state changes and updated the view name.
  - `MapViewDockingWindows` selects by component name on focus changes.
    `SaveAll` selects named map entries when a save needs interaction.
    Scripting `MapProxy.setName()` directly changes the selected component
    name.
  - InfoNode `ViewProperties.TITLE` changes call
    `DockingWindow.fireTitleChanged()`, which propagates to every docking
    ancestor. This is sufficient when a leaf title actually changes;
    rewriting an equal title is not needed for that propagation.
  - `setMap(view, map)` already preserves a view while replacing its map.
    Code explorer adds global refreshes at both replacement points.
  - With a selected view, the shared static `MapView` property listener
    also obtains the open view collection for unhandled properties before
    leaving its fallback loop. This includes the two title preferences.
  - Node-text/bookmark notifications reach the coordinator through the
    mode controller, not through map-specific node-change callbacks.
    `MapModel.setSaved()` and `setReadOnly()` emit no change notification.
    `MMapController.mapSaved()` owns the existing saved/read-only refresh
    condition and Save action enablement.
  - Maps-menu rebuilding destroys and recreates entries synchronously.
    The existing `navigate_maps` destroyer uses `ChildActionEntryRemover`
    to unregister actions. `AFreeplaneAction` already supports action-label
    and selection property changes; menu controls observe these changes.
    `getRawText()` is also read by menu and command-to-node code, so changing
    only `Action.NAME` would leave a second label representation stale.
  - `MapDisplayNameResolverTest` covers injected source selection,
    shortening, custom-name precedence, and automatic-label matching.
    It does not exercise actual ancestor text/bookmark resolution.
    `MapDisplayNameRefreshCoordinatorTest` covers event relevance and
    listener registration but not affected-view selection. No title,
    docking, maps-menu, or `SaveAll` test container currently exists.
  - Living project documents checked: root `README.md` describes the
    application, documentation site, and contribution process; it has no
    title/selection contract or project-document list. No project
    `glossary.md` or `glossary.adoc` was found. Existing ADR subjects are
    unrelated to map titles. No living project document or UI text needs
    to change for this behavior-preserving subtask.
- **Analysis:**
  - Use direct per-view title notifications and display subscriptions
    because existing map and property listeners can deliver changes without
    searching open views.
- **Design:**

  ```plantuml
  @startuml
  set separator none
  package org.freeplane {
    package core.resources {
      abstract class "<plain>**=**</plain> ResourceController" as ResourceController {
        + <plain>**=**</plain> addPropertyChangeListener(\nlistener: IFreeplanePropertyListener\n)\n: void
        + <plain>**=**</plain> removePropertyChangeListener(\nlistener: IFreeplanePropertyListener\n)\n: void
      }
    }
    package features {
      package map {
        class "<plain>**=**</plain> MapModel" as MapModel {
          + <plain>**=**</plain> addMapChangeListener(listener: IMapChangeListener): void
          + <plain>**=**</plain> removeMapChangeListener(listener: IMapChangeListener): void
          + <plain>**=**</plain> fireMapChangeEvent(event: MapChangeEvent): void
        }
      }
      package ui {
        enum "<plain>**±**</plain> IMapViewManager.MapChangeEventProperty" as MapChangeEventProperty {
          <plain>**=**</plain> MAP_VIEW_ROOT
          <plain>**+**</plain> MAP_VIEW_TITLE
        }
      }
    }
    package view.swing.map {
      class "<plain>**±**</plain> MapView" as MapView {
        {static} + <plain>**+**</plain> TITLE_REFRESH_PROPERTY: String = "mapViewTitleRefresh"
        + <plain>**±**</plain> MapView(viewedMap: MapModel, modeController: ModeController)
        + <plain>**+**</plain> refreshTitle(): void
        + <plain>**+**</plain> notifySelectionChanged(): void
        + <plain>**=**</plain> isSelected(): boolean
        + <plain>**=**</plain> setMap(viewedMap: MapModel): void
        + <plain>**±**</plain> mapChanged(event: MapChangeEvent): void
        + <plain>**+**</plain> onNodeDeleted(event: NodeDeletionEvent): void
        + <plain>**+**</plain> onNodeInserted(\nparent: NodeModel,\nchild: NodeModel,\nnewIndex: int\n)\n: void
        + <plain>**+**</plain> onNodeMoved(event: NodeMoveEvent): void
        + <plain>**±**</plain> propertyChanged(\npropertyName: String,\nnewValue: String,\noldValue: String\n)\n: void
      }
    }
  }
  MapModel "1" --> "0..*" MapView : existing map-specific subscriptions
  ResourceController --> MapView : existing per-view property subscriptions
  @enduml
  ```

  - The view notification is a synchronous `PropertyChangeEvent` with
    source equal to that view and both values null. It requests recomputing
    the existing presentation, including path context; it stores no label,
    dirty flag, map count, or revision counter. Each display compares its
    computed values before assigning changes.
  - `notifySelectionChanged()` uses the existing
    `SelectableAction.SELECTION_PROPERTY` (`selected`), null old value,
    and the current `isSelected()` boolean as new value. Selection remains
    derived from the controller's selected view, not a second stored flag.
  - The existing map-event subscription follows `setMap()`; resource
    registration follows view creation. The common view-removal path
    unregisters both subscriptions for every close, including the last
    view of a map. Temporary docking reparenting must not unregister them.
    The shared static property listener returns for the two title
    preferences before its unrelated view-enumeration code; the per-view
    property handlers own these notifications.

  ```plantuml
  @startuml
  set separator none
  package org.freeplane {
    package features.map {
      class "<plain>**=**</plain> MapModel" as MapModel {
        + <plain>**=**</plain> fireMapChangeEvent(event: MapChangeEvent): void
      }
      package mindmapmode {
        class "<plain>**±**</plain> MMapController" as MMapController {
          + <plain>**±**</plain> mapSaved(mapModel: MapModel, saved: boolean): void
        }
      }
    }
    package view.swing.ui {
      class "<plain>**±**</plain> MapDisplayNameRefreshCoordinator" as MapDisplayNameRefreshCoordinator {
        + <plain>**±**</plain> MapDisplayNameRefreshCoordinator()
        ~ <plain>**+**</plain> MapDisplayNameRefreshCoordinator(\npreferences: MapNameChoicePreferences\n)
        + <plain>**±**</plain> nodeChanged(event: NodeChangeEvent): void
      }
    }
    package main.mindmapmode {
      class "<plain>**±**</plain> MModeControllerFactory" as MModeControllerFactory {
        {static} + <plain>**=**</plain> createModeController(): MModeController
      }
    }
  }
  MapDisplayNameRefreshCoordinator --> MapModel : MAP_VIEW_TITLE\nnon-dirty request
  MMapController --> MapModel : MAP_VIEW_TITLE\nexisting saved-state guard
  MModeControllerFactory --> MapDisplayNameRefreshCoordinator : installs node listener only
  @enduml
  ```

  - Node forwarding and saved-state updates call the changed map's
    `fireMapChangeEvent()` directly. The event carries that map, source
    equal to the publisher, property `MAP_VIEW_TITLE`, both values null,
    and `setsDirtyFlag=false`. They do not call the controller's
    `fireMapChanged()`, modify saved state again, or search for views.
    Keep `mapSaved()`'s current refresh guard and Save action enablement;
    do not change the behavior of raw model setters.
  - The coordinator retains only its node listener and existing source
    preference check. Remove its map/view/resource registrations and the
    obsolete `MapTitlesRefresher`, `PropertyListenerRegistrar`, and
    `CustomizedTitleListenerBinder` interfaces and their implementations.

  ```plantuml
  @startuml
  set separator none
  left to right direction
  package org.freeplane {
    package features {
      package ui {
        interface "<plain>**±**</plain> IMapViewManager" as IMapViewManager
      }
      package url.mindmapmode {
        class "<plain>**±**</plain> SaveAsAction" as SaveAsAction {
          + <plain>**±**</plain> actionPerformed(e: ActionEvent): void
        }
        class "<plain>**±**</plain> SaveAll" as SaveAll {
          + <plain>**±**</plain> actionPerformed(e: ActionEvent): void
        }
      }
    }
    package main.headlessmode {
      class "<plain>**±**</plain> HeadlessMapViewController" as HeadlessMapViewController
    }
    package plugin.codeexplorer.map {
      class "<plain>**±**</plain> CodeMapController" as CodeMapController {
        + <plain>**±**</plain> explore(\ncodeExplorerConfiguration: CodeExplorerConfiguration,\nreloadCodebase: boolean\n)\n: void
      }
    }
  }
  HeadlessMapViewController ..|> IMapViewManager
  @enduml
  ```

  - Remove the redundant `SaveAsAction` and code-explorer global refresh
    requests. `SaveAll` captures matching view references before saving,
    preserves existing map-entry order and save decisions, and restores
    the original view reference.

  ```plantuml
  @startuml
  set separator none
  package "Map view titles" {
    package java.beans {
      interface "<plain>**=**</plain> PropertyChangeListener" as PropertyChangeListener {
        + <plain>**=**</plain> propertyChange(event: PropertyChangeEvent): void
      }
    }
    package org.freeplane {
      package features.ui {
        interface "<plain>**=**</plain> ViewController" as ViewController {
          + <plain>**=**</plain> setTitle(frameTitle: String): void
        }
      }
      package view.swing.map {
        class "<plain>**±**</plain> MapView" as MapView
        class "<plain>**±**</plain> MapViewController" as MapViewController {
          + <plain>**+**</plain> propertyChange(event: PropertyChangeEvent): void
          + <plain>**±**</plain> afterViewChange(oldMap: Component, pNewMap: Component): void
          + <plain>**±**</plain> updateMapViewName(): void
          + <plain>**±**</plain> setMap(view: Component, newMap: MapModel): void
          + <plain>**±**</plain> tryToChangeToMapView(url: URL): boolean
          + <plain>**±**</plain> checkIfFileIsAlreadyOpened(urlToCheck: URL): String
        }
      }
      package main.application {
        class "<plain>**±**</plain> ApplicationViewController" as ApplicationViewController {
          + <plain>**±**</plain> setTitle(frameTitle: String): void
        }
      }
    }
  }
  MapView --> MapViewController : title subscription\nselected view only
  MapViewController --> ViewController : frame title only
  ApplicationViewController ..|> ViewController
  MapViewController ..|> PropertyChangeListener
  @enduml
  ```

  - Selection changes detach the frame-title listener from the old view,
    attach it once to the new view, and set the frame title directly.
    Actual transitions notify only the old and new views' selection
    properties. A same-view notification does not duplicate subscriptions.
    Workspace/mode changes and no-map/no-mode frames keep their current
    direct frame-title path.
  - `setMap(view, map)` requests that view's title refresh after replacement
    listeners run. `updateMapViewName()` requests the selected view's
    refresh after assigning its key. A changed key also requires the
    existing maps-menu rebuild because ordering and action keys change.
    Map replacement rebuilds those entries only when the key changes.
  - Keep `getMapKeys()`, `getMaps()`, unique suffix allocation, supported
    string-selection behavior/errors, and scripting naming unchanged.
    Remove the duplicate name assignment. URL selection resolves and
    selects the matching view reference; opened-file checks still return
    the same supported string key.
  - Remove the obsolete no-argument `setMapTitles()` from the manager
    interface and both implementations after replacing all repository
    callers. Do not add map/view overloads, collection-based refresh APIs,
    a map-to-views index, or no-op methods on `FrameController`.

  ```plantuml
  @startuml
  set separator none
  package "Map view titles" {
    package java.beans {
      interface "<plain>**=**</plain> PropertyChangeListener" as PropertyChangeListener {
        + <plain>**=**</plain> propertyChange(event: PropertyChangeEvent): void
      }
    }
    package org.freeplane {
      package view.swing {
        package map {
          class "<plain>**±**</plain> MapView" as MapView
        }
        package ui {
          class "<plain>**±**</plain> MapsMenuAction" as MapsMenuAction {
            + <plain>**+**</plain> MapsMenuAction(mapView: MapView)
            + <plain>**+**</plain> propertyChange(event: PropertyChangeEvent): void
            + <plain>**+**</plain> getRawText(): String
            ~ <plain>**+**</plain> dispose(): void
            + <plain>**±**</plain> actionPerformed(menuEvent: ActionEvent): void
          }
          class "<plain>**±**</plain> UserInputListenerFactory" as UserInputListenerFactory {
            + <plain>**±**</plain> UserInputListenerFactory(modeController: ModeController)
            + <plain>**=**</plain> updateMapList(): void
          }
        }
      }
      package main.application {
        class "<plain>**±**</plain> MapViewDockingWindows" as MapViewDockingWindows {
          + <plain>**+**</plain> propertyChange(event: PropertyChangeEvent): void
          + <plain>**±**</plain> afterViewChange(\npOldMap: Component,\npNewMap: Component\n)\n: void
          + <plain>**±**</plain> afterViewClose(pOldMapView: Component): void
          + <plain>**±**</plain> loadLayout(): void
        }
      }
    }
  }
  MapView --> MapViewDockingWindows : title subscription\none per docked view
  MapView --> MapsMenuAction : title and selection\nfor this menu action
  UserInputListenerFactory --> MapsMenuAction : constructs and disposes\nwith menu entries
  MapViewDockingWindows ..|> PropertyChangeListener
  MapsMenuAction ..|> PropertyChangeListener
  @enduml
  ```

  - Docking initializes the title and path tooltip after existing name
    recovery, registers once when the view enters its tracked view set,
    and unregisters before closing its docking window. Later requests
    update only the event source's title and path tooltip. Keep the loading
    guard and existing initial custom-name recovery rules. Layout restoration
    may enumerate restored views for one-time reconstruction and initial
    titles; remove the old general docking `setTitle()` operation.
  - Each menu action captures its view, initializes its existing label
    format and selected state, and subscribes to that view's two properties.
    Its label formatting moves from the menu builder into the action.
    `getRawText()` returns the current formatted label, not the
    superclass's constructor-time label. Use the existing mnemonic
    conversion, clear obsolete mnemonic properties, and avoid assignments
    when the resulting action properties are unchanged.
  - The `navigate_maps` destroyer calls `dispose()` on its actions before
    the existing `ChildActionEntryRemover` unregisters/removes them.
    Disposal detaches both view subscriptions. Rebuilding or reopening a
    menu must not accumulate subscriptions or retain destroyed controls.
  - Keep action-key spelling `MapsMenuAction.` plus the component name
    at construction. Menu clicks use the captured live view through the
    existing focus callback. A closed view retains warning/no selection.
    Docking focus selection also passes its component directly.

  ```plantuml
  @startuml
  participant MapDisplayNameRefreshCoordinator
  participant MMapController
  participant MapModel
  participant ResourceController
  participant MapView
  alt relevant node text or bookmark change
    MapDisplayNameRefreshCoordinator -> MapModel : fireMapChangeEvent(event)
    MapModel -> MapView : mapChanged(event) through existing map listeners
  else saved/read-only refresh condition
    MMapController -> MapModel : fireMapChangeEvent(event)
    MapModel -> MapView : mapChanged(event) through existing map listeners
  else URL, bookmark list, or node structure changes
    MapModel -> MapView : mapChanged(event) or\nonNodeInserted/onNodeDeleted/onNodeMoved
  else current view root changes
    MapModel -> MapView : mapChanged(event)\nproperty = MAP_VIEW_ROOT
    opt event source is this view
      MapView -> MapView : refreshTitle()
    end
  else individual custom name changes
    MapView -> MapView : custom-name property listener calls refreshTitle()
  else label source or maximum length changes
    ResourceController -> MapView : propertyChanged(...)
  end
  opt relevant shared-map or preference callback
    MapView -> MapView : refreshTitle()
  end
  @enduml
  ```

  - Root events from another view of the same map are ignored for title
    updates. Node text/bookmark forwarding and structural callbacks keep
    their existing source-preference filters; deletion uses the retained
    parent and moves remain within one map. URL handling must request the
    title update even though its existing rendering branch returns early.
  - Rename installs its existing provider before changing the custom-name
    property and relies on that property's notification. Equal custom
    names request no refresh; cancellation/clearing retain current rules.
    Saved-state and content notifications remain synchronous. Separate
    meaningful events may arrive, but equal presentation values are not
    assigned again.

  ```plantuml
  @startuml
  participant MapView
  participant MapViewDockingWindows
  participant "net.infonode.docking.View" as DockedView
  participant MapsMenuAction
  participant JAutoCheckBoxMenuItem
  participant MapViewController
  participant ApplicationViewController
  MapView -> MapViewDockingWindows : TITLE_REFRESH_PROPERTY from this view
  MapViewDockingWindows -> DockedView : assign changed title and path tooltip
  DockedView -> DockedView : existing ancestor-title propagation
  MapView -> MapsMenuAction : TITLE_REFRESH_PROPERTY from this view
  MapsMenuAction -> JAutoCheckBoxMenuItem : changed action-label/mnemonic properties
  opt this view has the frame-title subscription
    MapView -> MapViewController : TITLE_REFRESH_PROPERTY
    MapViewController -> ApplicationViewController : setTitle(existing frame format)
  end
  @enduml
  ```

  - A title request or map/resource title callback does not call
    `getViews()`, enumerate `getMapViews()`, build an affected-view
    collection, or rebuild maps-menu entries.
    Existing model/property dispatch still notifies multiple subscribed
    views when appropriate. Actual menu construction, membership/order
    changes, layout restoration, and unrelated selection/close operations
    may retain their required enumeration.
  - Keep the resolver, preference keys, custom-name ownership, title
    formatting, dirty/read-only rules, bidi handling, menu order, and path
    context unchanged. No new top-level production type is needed.
  - **Living project documents:** None affected
- **Test specification:**
  - **Automated tests:**
    - **±** `org.freeplane.view.swing.ui.MapDisplayNameRefreshCoordinatorTest`
      - **±** `shouldRefreshNodeTextChangesOnlyInViewRootTextMode`: a
        relevant text event publishes one non-dirty `MAP_VIEW_TITLE` event
        through its node's map; no view registry is read.
      - **±** `shouldRefreshBookmarkChangesOnlyInViewRootBookmarkMode`:
        bookmark events use the changed node's map only in bookmark mode.
      - **+** `irrelevantNodeChangesDoNotPublishTitleRequests`: unrelated
        properties and filename mode publish no map-title request.
    - **+** `org.freeplane.view.swing.map.MapViewTitleTest`
      - **+** `titleRequestIdentifiesItsViewWithoutCopiedDisplayState`:
        the property event has this view as source, the exact property
        key, null values, and synchronous delivery without copied label,
        dirty-state, count, or revision values.
      - **+** `viewRootChangeNotifiesOnlyItsSourceView`: a real root
        change with two views of one map and a view of another map notifies
        only the source view, including when that view is not selected.
      - **+** `sharedMapChangesNotifyOnlySubscribedViews`: URL, bookmark,
        and map-title events use the actual map listener list; every
        attached view of that map is notified, unrelated views are not,
        and neither `getViews()` nor `getMapViews()` is called.
      - **+** `labelPreferencesNotifyRegisteredViewsDirectly`: both
        relevant preference changes travel through existing per-view
        resource subscriptions without a manager refresh or registry scan;
        unrelated preferences request no title update.
      - **+** `customNameChangeNotifiesOnlyItsView`: the actual client
        property path requests one view update for a changed value and none
        for an equal value; another view of the same map is untouched.
      - **+** `structuralChangesNotifyOnlyViewsUsingAncestorSources`:
        real map insert/delete/move callbacks retain source-preference
        filters, use the retained parent for deletion, and request one
        update per subscribed view without a per-parent duplicate.
      - **+** `closingViewsRemovesMapAndPreferenceSubscriptions`:
        closing one of several views or the last view removes both
        registrations; later model/resource events do not reach it.
    - **+** `org.freeplane.view.swing.map.MapViewControllerTitleTest`
      - **+** `onlyTheSelectedViewCanUpdateTheFrameTitle`: title
        requests from the selected view reach the frame formatter; other
        views of either map have no frame-title subscription.
      - **+** `selectionChangesMoveTheFrameSubscriptionOnce`: actual
        selection changes detach/attach listeners and update the frame;
        repeated same-view notifications do not duplicate subscriptions,
        request docking title updates, or rebuild menu entries.
      - **+** `selectionNotifiesOnlyTheOldAndNewViews`: existing
        selection properties carry false/true for the old/new views;
        another view receives nothing and no view registry is scanned.
      - **+** `mapReplacementKeepsViewAndRebindsNotifications`:
        `setMap(view, map)` keeps the view and display bindings, detaches
        the old map, attaches the new map, and requests its title after
        replacement listeners have run; unrelated views are untouched.
      - **+** `mapReplacementRebuildsMenuOnlyWhenItsKeyChanges`: a
        changed component name rebuilds the ordering/action keys; a
        replacement retaining the same key updates existing action labels.
      - **+** `viewNameUpdateDoesNotRefreshOtherDockingTitles`: key
        assignment requests the selected view's title and the required
        key/order rebuild, without requesting other views' titles.
      - **+** `closedViewRequestsDoNotChangeDisplays`: after closing,
        direct requests from the old view cannot update the frame, docking
        title, or destroyed menu actions.
      - **+** `frameTitleKeepsExistingFormatting`: exact strings for
        workspace, named/unnamed maps, saved/dirty/read-only state, editing
        lock, mode, absolute path, bidi handling, and no map/no mode.
      - **+** `stringSelectionKeepsExistingKeysAndErrors`: unique suffixes,
        immutable key/map results, explicit component-name changes as used
        by scripting, unknown-name exceptions, and unsuccessful try-select
        results remain unchanged; labels do not become accepted keys.
      - **+** `urlSelectionUsesTheMatchingViewReference`: current-map
        and matching file/non-file URL cases preserve selection outcomes
        and the opened-file string result without string-based selection.
    - **+** `org.freeplane.main.application.MapViewDockingWindowsTest`
      - **+** `viewTitleRequestUpdatesOnlyItsDockingWindow`: a request
        updates that real InfoNode view's existing resolved label and path
        tooltip, with no registry scan or unrelated property assignment.
      - **+** `unchangedTitlesAndTooltipsAreNotAssignedAgain`: repeated
        requests produce no redundant property writes.
      - **+** `changedLeafTitleUpdatesDockingAncestors`: real nested
        InfoNode windows propagate label and dirty-marker changes while
        unrelated branches remain unchanged.
      - **+** `dockingSubscriptionsFollowViewLifetime`: repeated view
        notifications/reparenting do not duplicate the title listener;
        closing detaches it before removing the docking window.
      - **+** `layoutRestorationKeepsExistingIndividualNameRecovery`:
        loading does not overwrite serialized titles before the existing
        recovery rules run; normal requests never repeat recovery or scan
        unrelated views. No new persistence behavior is asserted.
      - **+** `tabRenamePublishesOneViewTitleRequest`: the actual rename
        menu action and client-property path publish one request for a
        changed value; equal values publish none; cancel/empty input retain
        current clearing.
      - **+** `dockingFocusSelectsTheViewReference`: the docking focus
        callback selects its contained component despite equal displayed
        labels, without calling the string-selection overload.
    - **+** `org.freeplane.main.application.ApplicationViewControllerTitleTest`
      - **+** `settingFrameTitleDoesNotRefreshTabs`: the real title
        method updates the frame only.
      - **+** `unchangedFrameTitleIsNotAssignedAgain`: an equal computed
        title produces no additional frame setter call.
    - **+** `org.freeplane.view.swing.ui.MapsMenuActionTest`
      - **+** `menuClickSelectsItsViewWithEqualLabels`:
        `JMenuItem.doClick()` and the focus callback select the captured
        view when labels coincide or label preferences change.
      - **+** `delayedMenuClickDoesNotSelectAClosedView`: closing the
        captured view before focus arrives retains warning/no selection.
      - **+** `titleRequestUpdatesTheExistingMenuItem`: a real
        `JAutoCheckBoxMenuItem` using the existing action wrapper receives
        changed label/dirty-state properties without replacement.
      - **+** `rawTextTracksTheCurrentFormattedLabel`: after a title
        change, raw text retains the exact current menu-label format,
        including mnemonic markup; rendered text has the expected mnemonic
        conversion rather than the constructor-time label.
      - **+** `titleChangesReplaceMnemonicProperties`: changing or
        removing a mnemonic updates/clears the existing action and rendered
        control properties, preserving platform-specific conversion.
      - **+** `unchangedPresentationDoesNotRewriteActionProperties`:
        equal computed label/mnemonic values cause no setter calls.
      - **+** `selectionUpdatesTheExistingCheckBoxItem`: old/new view
        notifications update the existing controls' selected state without
        rebuilding menu entries.
      - **+** `disposingActionDetachesItsViewSubscriptions`: disposal
        removes title and selection listeners; later view requests cannot
        update the destroyed action.
    - **+** `org.freeplane.view.swing.ui.UserInputListenerFactoryMapActionsTest`
      - **+** `mapsMenuKeepsExistingLabelsOrderAndSelection`: actual
        menu construction retains action keys, component-name ordering,
        exact `name` / `name : resolved label` formatting, dirty/read-only
        rules, and selected-view marking, including equal resolved labels.
      - **+** `titleChangesKeepExistingEntriesAndActions`: root/custom
        label, dirty-state, and label-preference changes update affected
        existing controls while preserving entry/action/control identities;
        unrelated entries remain unchanged and no menu rebuild or view
        enumeration occurs during the notification.
      - **+** `menuRebuildDisposesPreviousActions`: legitimate menu
        rebuild/hide/reopen paths detach previous action subscriptions
        before unregistering/removing entries, and create only the
        subscriptions needed by the live actions.
    - **±** `org.freeplane.view.swing.ui.MapDisplayNameResolverTest`
      - **=** `shouldUseCustomizedTabNameWithoutTruncation`: custom-name
        precedence and lack of shortening remain unchanged.
      - **=** `shouldResolveFileNameSourceWithTruncation`: filename source
        retains the existing maximum length and continuation mark.
      - **=** `shouldUseCurrentViewRootNameWhenConfigured`: text source
        preserves its existing output.
      - **=** `shouldFallbackToFileNameWhenCurrentViewRootNameMissing`:
        missing text preserves the existing filename fallback.
      - **=** `shouldUseCurrentViewRootBookmarkTitleWhenConfigured`:
        bookmark source preserves its existing shortening behavior.
      - **=** `shouldMatchAnyAutomaticLabelAcrossSources`: existing
        initial recovery still recognizes all automatic sources.
      - **+** `usesNearestNonFormulaAncestorForViewRootText`: exercise
        actual root/parent traversal with formula ancestors and missing
        text service, not an injected traversal substitute.
      - **+** `usesNearestAncestorBookmarkForViewRootBookmark`: exercise
        actual bookmark lookup and filename fallback when none exists.
    - **+** `org.freeplane.features.map.mindmapmode.MMapControllerTitleTest`
      - **+** `savedStateChangeNotifiesOnlyTheChangedMap`: actual
        saved/read-only branches publish the exact non-dirty map event and
        preserve Save action enablement; unchanged saved state on a
        writable map emits nothing; no view registry is read.
      - **+** `savedStateWithoutViewsDoesNotAccessSwingDisplays`: a map
        without attached views can change saved state without a Swing title
        update, view search, or new headless refresh API.
    - **+** `org.freeplane.features.url.mindmapmode.SaveAsActionTest`
      - **+** `saveAsLeavesTitleNotificationsToSaveAndNameChanges`:
        success, cancellation, and failure retain save delegation without
        a separate title request or view-manager access from the action.
    - **+** `org.freeplane.features.url.mindmapmode.SaveAllTest`
      - **+** `saveAllKeepsSaveDecisionsAndRestoresTheSelectedView`:
        preserves map-entry order, dirty/read-only/URL decisions, repeated
        views of a map, and the final current-map save; interactive
        selection uses captured references rather than string selection.
  - **Manual tests:**
    - These optional desktop checks cover native painting, focus, menus,
      dialogs, and drag/drop that the headless tests do not exercise.
      Event targeting and subscription counts remain covered by automated
      tests; unchanged visible text cannot prove that no refresh occurred.
    - **Setup:** Use disposable copies of two saved maps, A.mm and B.mm,
      with visibly different root text. In A.mm, select a child and use
      `Open selection as root in new view` to create a second view with
      different visible content. Record the current `Tab titles` and
      `Maximum tab title length` preferences so they can be restored.
    - **Native title painting:** Change `Tab titles` between `File name`,
      `Current view root text`, and `Current view root bookmark title`;
      add a bookmark to the child used as the second view's root. Change
      that child's text and bookmark title, then use a short maximum
      title length with long text. Inspect both A.mm tabs, their maps-menu
      entries, and the frame while switching views. Automatic labels must
      follow the selected source, filename fallback, and shortening rules.
      Check for stale text or repaint artifacts. The frame keeps its
      existing component-name, dirty-marker, mode, and path formatting;
      it does not adopt the resolved tab label.
    - **Selection and native focus:** Use `Rename tab` to give both A.mm
      views the same custom name. Alternate between them by clicking tabs
      and selecting their distinct maps-menu entries. The visible root
      must identify the intended view each time; the menu selection mark
      and frame must follow it without an unexpected focus jump. Repeat
      after changing `Tab titles`; custom names retain precedence.
    - **Native rename dialog:** Rename one individual tab while the other
      views remain open. Its title must repaint without renaming another
      view; the maps-menu entry must retain its component-name prefix.
      Enter the same name again, then test Cancel and empty input after
      setting a custom name. Cancel and empty input currently clear the
      custom name and restore the automatic label; preserving a name on
      Cancel is not part of this subtask.
    - **Docking and group painting:** Drag views into a tab group and a
      split layout, then move a view between them. Rename an individual
      tab and edit its map to make it dirty. Inspect the leaf and ancestor
      tab text before and after saving. Dirty markers must repaint for
      the views of that map and their groups, with no stale ancestor
      title. Selecting the moved tab must still show its intended view.
      B.mm must not acquire A.mm's title or dirty marker. Existing group
      custom-name ownership and formatting remain unchanged.
    - **Native save dialogs and focus restoration:** Create an unnamed
      map, edit it and A.mm, then select B.mm and use `Save all maps`.
      Complete the unnamed map's save dialog. The saved maps' names and
      dirty markers must repaint, and B.mm must be selected again when
      saving finishes. Use `Save map as…` on a disposable map and cancel;
      its existing name and path must remain. Repeat and save under a new
      name; verify the tab, menu entry, frame, and path tooltip reflect
      the saved map without selecting another view.
    - **Native path display:** Save a disposable map under a long path
      containing spaces and non-Latin text. Hover its tab and switch to
      it. Inspect the tooltip and frame for the correct path and readable
      path separators; compare with another map's tooltip to detect stale
      path text. Custom tab names must not replace the path context.
    - **Menu reopening and closing views:** Repeatedly open and dismiss
      the maps menu, close one A.mm view, and create another using
      `New map view`. Each live view must have one distinct menu entry,
      with the existing component-name ordering and the correct selection
      mark. Closed views must disappear and remaining entries must still
      select their intended views. Close the last view of a map, then
      change the title preferences and reopen the menu; no closed entry
      may return. Restore the recorded preferences after these checks.
    - Restart and external-file reload custom-name fixes, and identical
      labels across the frame, tabs, and menu, belong to the second
      subtask and are not expected results of these checks.

- **Implementation notes:**
  - **Tradeoffs:**
    - Docking tests retain real InfoNode windows, title properties, menu
      actions, and layout restoration. They mock native drag/drop, the
      heavyweight theme, and overview AWT menus so verification can run
      headless without adding production APIs for test setup. Native
      painting and those overview menus are outside this refactoring.

## Subtask: Preserve custom tab names across restart and map reload

- **Status:** backlog
- **Scope:**
  - Build on the first subtask to use one custom-name ownership mechanism
    for individual tabs and group tabs, with each name attached to its
    docking-window owner rather than a contained map view.
  - Use each individual view's resolved label consistently in its tab,
    maps-menu entry, and main-window title while retaining file-path
    context separately.
  - Persist custom tab names with their docking-window owners through the
    existing layout serialization and preserve those owners during
    external-file reload. Keep automatic labels and derived dirty markers
    separate from the custom name.
  - Include automated regression coverage for individual-tab and group-tab
    name preservation across restart and reload, multiple views of the same
    map, URL changes, dirty-state changes, rename cancellation and clearing,
    and the agreed restructuring behavior: a surviving group keeps its
    name, while an eliminated group's name is discarded without transfer.
    Include affected UI-text and documentation updates in the same subtask.
- **Motivation:**
  - A custom tab name must remain attached to the tab the user renamed,
    rather than disappear during restart or reload, move to another tab,
    or overwrite a later rename.
- **Constraints:**
  - Keep custom tab names as layout state, not mind-map contents.
  - Define old-layout handling during this subtask's detailed planning;
    do not silently retain the current text-comparison heuristic as a
    compatibility fallback.
