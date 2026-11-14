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
    and preserve those owners and names during external-file reload and
    Revert.
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
    Verify the selected library serialization path during detailed
    planning before relying on it for implementation.
  - Treat external-file reload and Revert as replacement that preserves
    docking-window ownership, rather than ordinary close followed by
    unrelated open and URL-based title recovery. Reuse existing
    map-replacement capabilities where they satisfy the required lifecycle
    behavior.
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

- **Status:** review
- **Scope:**
  - Build on the first subtask to use one custom-name ownership mechanism
    for individual tabs and group tabs, with each name attached to its
    docking-window owner rather than a contained map view.
  - Use each individual view's resolved label consistently in its tab,
    maps-menu entry, and main-window title while retaining file-path
    context separately.
  - Persist custom tab names with their docking-window owners through the
    existing layout serialization and preserve those owners during
    external-file reload and Revert. Keep automatic labels and derived
    dirty markers separate from the custom name.
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
- **Scenario:**
  - Renaming an individual tab or group tab assigns a custom tab name to
    that tab's docking-window owner. Cancel leaves the name unchanged;
    confirming empty input clears it and restores the automatic label.
  - Restart restores explicit custom tab names with their saved owners.
    An old layout without explicit custom-name metadata restores its maps
    and layout with automatic labels.
  - External-file reload and Revert replace map contents in the existing
    views and docking-window owners. Different views of the same map retain
    their own names, and the selected view remains selected.
  - A surviving group keeps its name when maps move into or out of it.
    Eliminating that group discards its name without transferring it to a
    remaining map or a newly created group.
  - Each individual view uses its resolved label in its tab, maps-menu
    entry, and main-window title, including literal ampersands rather than
    mnemonic markup. File-path context, dirty markers, and group map counts
    remain separate from the stored custom tab name.
- **Glossary:**
  - **Custom tab name:** text assigned to the renamed tab's docking-window
    owner.
    - Individual tabs and group tabs use the same ownership rule.
    - The stored text excludes derived dirty markers and map counts; it
      is not shared through the map or identified by its URL.
  - **`DockingWindow`:** shared term from InfoNode; the layout object that
    owns the individual tab or group tab.
    - Restoring its saved layout state restores its explicit custom tab
      name; removing that owner does not transfer its name.
  - **`DockingWindowTitleProvider`:** shared term from InfoNode; the object
    that formats a docking window's displayed title.
    - The custom tab name belongs to the window's saved properties, not
      to a captured controller or map view inside this object.
  - **`StringProperty`:** shared term from InfoNode; a named text property
    registered in a property group and stored in an individual property map.
    - Register the custom-name property before reading a layout; missing
      text means automatic naming, not recovery from a rendered title.
  - **`PropertyMapValueHandler`:** shared term from InfoNode; the value
    handler that connects a property definition to property-map values.
    - Use the existing window property-map writer and reader for the
      custom-name text rather than add a second persistence mechanism.
  - **`Supplier`:** shared term from the Java functional API; a callable
    that returns a current value.
    - A map view reads its owner's current custom name through this API
      without owning or copying that name.

  ```mermaid
  flowchart LR
    subgraph layout["Docking layout"]
      owner["Docking window"]
      name["Custom tab name"]
      property["Text property"]
      saved["Saved layout state"]
      owner -->|owns| name
      property -->|stores| name
      property -->|is written with its owner to| saved
    end
    subgraph display["Tab titles"]
      title["Displayed tab title"]
      derived["Dirty marker and map count"]
    end
    name -->|supplies the name for| title
    derived -->|are added separately to| title
    subgraph view["Map view"]
      access["Current custom name"]
    end
    access -->|reads without owning| name
  ```

- **Constraints:**
  - Keep custom tab names as layout state, not mind-map contents.
  - Restore maps and docking layout from old layouts, but use automatic
    labels wherever explicit custom-name metadata is absent. Do not infer
    custom names from rendered titles or retain the text-comparison
    heuristic. Names assigned afterward use the new persistence mechanism.
  - Keep the first subtask's direct title notifications, selected-view
    frame subscription, unchanged-property guards, and lifetime cleanup.
    Reload and layout restoration may enumerate views. Title requests must
    not enumerate manager/tracked-view registries or rebuild the maps menu.
    Retain existing InfoNode docking-tree propagation and group aggregation;
    do not add a global view scan.
  - Preserve existing automatic-label sources, shortening, formula and
    bookmark fallback, string-selection keys, scripting names, menu order,
    path context, and read-only rules except for the explicitly agreed
    consistent labels and literal ampersands.
  - Preserve the existing map-replacement reset of the current view root,
    node selection, filter, root history, and zoom. This subtask does not
    add persistence for those states or extend layout restoration to maps
    without a URL or documentation maps. Dirty maps with a saved URL keep
    their existing restoration eligibility.
- **Briefing:**
  - Commit `998d2fca7a` supplies direct view-title notifications and
    lifetime-owned docking, menu, and selected-frame subscriptions.
    `MapViewDockingWindows` and `MapViewSerializer` own application layout
    handling; the bundled InfoNode 1.6.1 library owns its binary format.
    `MapDisplayNameResolver` supplies automatic labels. Both reload callers
    reach `MMapController`; model-cache, undo-handler, resource-release,
    and map-removal ordering affect view-preserving replacement. Tests
    use real lightweight Swing/InfoNode controls with native substitutions.
- **Research:**

  ```plantuml
  @startuml
  set separator none
  package "Custom tab names" {
    package org.freeplane {
      package main.application {
        class MapViewDockingWindows {
          + saveLayout(): void
          + loadLayout(): void
        }
      }
      package view.swing {
        package map {
          class MapView
        }
        package ui {
          class MapDisplayNameResolver {
            {static} + resolveBaseLabel(mapView: MapView): String
          }
        }
      }
    }
    package net.infonode.docking {
      class DockingWindow
      package title {
        interface DockingWindowTitleProvider {
          + getTitle(window: DockingWindow): String
        }
      }
    }
  }
  MapViewDockingWindows --> MapView : individual customizedTabName client property
  MapViewDockingWindows --> DockingWindow : group customizedTabName client property
  MapViewDockingWindows --> DockingWindowTitleProvider : bound method reference
  MapDisplayNameResolver --> MapView : reads individual custom name
  @enduml
  ```

  - Individual names currently live on `MapView`; group names live on
    their docking windows. `getCustomTitle()` ignores the docking-window
    client property for `ConnectedToMenuView`, whose displayed label is
    resolved from its map view instead. The same name key therefore has
    two owners and two reading paths.
  - Rename installs a bound `getCustomTitle()` method reference and then
    changes the relevant client property. Cancel and empty input both
    clear the name. The frame still uses the component name, while menu
    entries retain their component-name prefix.

  ```plantuml
  @startuml
  participant MapViewDockingWindows
  participant RootWindow
  participant WindowItem
  participant PropertyMapImpl
  participant SimplePropertyValue
  participant MapViewSerializer
  MapViewDockingWindows -> RootWindow : write(out)
  RootWindow -> MapViewSerializer : writeView(view, out)
  MapViewSerializer -> MapViewSerializer : write eligibility, mode, and map URL
  RootWindow -> WindowItem : write(out, context, viewWriter)
  WindowItem -> PropertyMapImpl : write window property maps
  PropertyMapImpl -> SimplePropertyValue : isSerializable()
  opt property value implements Serializable
    PropertyMapImpl -> SimplePropertyValue : write(out)
  end
  @enduml
  ```

  - `DockingWindowTitleProvider` does not extend `Serializable`. The
    current bound method reference is therefore skipped. InfoNode's
    `LengthLimitedDockingWindowTitleProvider` explicitly implements both
    interfaces. A temporary Java 21 planning probe exercised the bundled
    library's property-map writer and reader: that serializable provider's
    type was retained, while a non-serializable title-provider lambda was
    skipped. No repository implementation or tests were changed by the
    probe. A fresh-owner application-layout round trip has not been run.
  - The layout writer does not write Swing client properties. Individual
    rendered titles survive as view properties, but no flag records
    whether they were automatic or custom. Group custom-name client
    properties are not stored by this path. The current restoration code
    guesses individual custom names by comparing the restored rendered
    title with all current automatic labels after removing a dirty marker.

  ```plantuml
  @startuml
  set separator none
  package "InfoNode text-property serialization" {
    package net.infonode {
      package docking.properties {
        class DockingWindowProperties {
          {static} + PROPERTIES: PropertyMapGroup
          + getMap(): PropertyMap
        }
      }
      package properties {
        package types {
          class StringProperty {
            + StringProperty(\ngroup: PropertyGroup,\nname: String,\ndescription: String,\nvalueHandler: PropertyValueHandler\n)
            + get(valueContainer: Object): String
            + set(valueContainer: Object, value: String): void
          }
        }
        package propertymap {
          class PropertyMapValueHandler {
            {static} + INSTANCE: PropertyMapValueHandler
          }
          interface PropertyMap {
            + write(out: ObjectOutputStream, recursive: boolean): void
            + read(in: ObjectInputStream): void
          }
        }
      }
    }
  }
  DockingWindowProperties --> PropertyMap : exposes per-window values
  StringProperty --> PropertyMapValueHandler : supplied value handler
  PropertyMapValueHandler --> PropertyMap : reads and writes text values
  @enduml
  ```

  - The native reader consumes an unknown simple-property value without
    assigning it when the property definition is absent.
  - A second temporary Java 21 probe registered `customizedTabName` in
    that property group. The existing writer/reader retained a literal
    name containing ampersands, Japanese text, count-like text, and an
    asterisk in a fresh property map. A reader without the property
    definition skipped the value without error; clearing the value left
    it absent after serialization. This verifies the native text-property
    mechanism without introducing a serialized Freeplane class.
  - A native `RootWindow.write()`/`read()` probe with the bundled library
    restored individual and group text properties on fresh `View` and
    `TabWindow` objects. The probe's view serializer wrote only view titles;
    custom names traveled through the native window property maps.
  - In the same probe, moving one view out of a two-view group caused
    InfoNode to remove that group with zero children and no window parent.
    Its raw name remained in its retained `WindowItem` and in the next
    serialized model. Removing the displayed container alone therefore
    does not discard saved metadata. A further native probe observed two
    children and no parent during whole-group reparenting, then the same
    group under its new parent. Lightweight Swing notification made real
    native views displayable, and closure removed that eligibility.

  ```plantuml
  @startuml
  participant ExternalMapChangeMonitor
  participant MMapController
  participant MapModel
  participant MMapModel
  participant MFileManager
  ExternalMapChangeMonitor -> MMapController : restoreCurrentMapIgnoreAlternatives()
  MMapController -> MapModel : releaseResources()
  MMapController -> MFileManager : loadAndLock(url, newModel)
  MMapController -> MMapController : fireMapCreated(newModel) and addLoadedMap(newModel)
  MMapController -> MMapController : closeWithoutSaving(oldMap)
  MMapController -> MMapModel : enableAutosave()
  MMapController -> MMapController : createMapView(newModel)
  @enduml
  ```

  - The external-change monitor delegates to the shared map-restoration
    method. For ordinary maps, that method closes the old map and creates
    a new view after loading; it does not use the view-preserving
    `IMapViewManager.setMap(view, map)` path. Documentation maps have a
    separate close-and-open branch.
  - `RevertAction` calls `restoreCurrentMap()`; the external-change
    monitor calls `restoreCurrentMapIgnoreAlternatives()`. Both delegate
    to the same restoration method, differing in whether they check for
    alternative saved files. `RevertAction` still has a Javadoc description
    based on close-and-open behavior.
  - Documentation-map loading uses `MapLoader.asDocumentation()` without
    file locking or autosave. `MapLoader.getMap()` can load without
    creating a view, but first consults the controller's loaded-map cache.
    `MMapController.closeWithoutSaving()` removes the old cache entry and
    fires map-removal listeners; the Swing manager closes only views still
    bound to that removed map. `MMapModel.beforeViewCreated()` installs
    the undo handler required by views of a newly loaded model.
  - `MapViewController.setMap()` already preserves the view component and
    rebinds map listeners. `MapView.setMap()` resets the component name to
    the new map title and resets the current root, selection, filter,
    root history, and zoom. Using it for several views of the same map
    without handling their component names can duplicate selection keys.
    Its existing root-selection call requests focus for showing views,
    including showing views that are not the selected view.
  - The headless view manager has no view components: `getViews(map)`
    returns an empty list and `setMap(view, map)` does nothing. Its
    model-only replacement currently goes through `newMapView()`.

  ```plantuml
  @startuml
  participant MapsMenuAction
  participant AFreeplaneAction
  participant LabelAndMnemonicSetter
  MapsMenuAction -> AFreeplaneAction : construct with key, label, and null icon
  opt constructor title is nonempty
    AFreeplaneAction -> LabelAndMnemonicSetter : setLabelAndMnemonic(action, title)
  end
  ... later title request ...
  MapsMenuAction -> LabelAndMnemonicSetter : setLabelAndMnemonic(presentation, rawText)
  @enduml
  ```

  - Mnemonic conversion removes the first ampersand; tabs and frame titles
    do not convert custom names. Passing a null constructor title already
    skips that conversion without changing the shared action API. Menu
    construction uses the action's current presentation; absent dynamic
    translation keys do not add another label conversion.
  - Existing docking tests exercise real InfoNode windows but mock native
    drag-and-drop, themes, and overview AWT menus. Their restoration case
    returns the original view through a mocked `MapViewSerializer` and
    verifies the current heuristic, not fresh-owner restart restoration.
    Existing menu tests exercise real checkbox menu items and explicitly
    verify the mnemonic conversion. Existing replacement tests verify
    listener rebinding and menu rebuilding when the component key changes.
  - `README.md` links the external user documentation but defines no tab
    naming contract or living-document list. No project glossary exists.
    The current release section of `freeplane/doc/history_en.txt` has only
    a generic bug-fix entry. The English rename tooltip warns that layout
    changes may reset the title; localized copies use the same resource
    key. The Java dialog fallback still describes a temporary name.
  - This research combines source inspection and bundled-library planning
    probes. No application restart or external-file reload reproduction
    has been executed.
- **Analysis:**
  - Use automatic labels when an old layout has no explicit custom-name
    metadata because rendered titles do not distinguish custom names from
    automatic labels. Preserve its maps and docking layout; newly assigned
    names use explicit metadata in the existing layout serialization.
  - Treat Cancel as no change and confirmed empty input as clearing the
    custom name because cancellation does not request a rename.
  - Preserve views, docking-window owners, and custom tab names for both
    Revert and external-file reload because both operations replace map
    contents rather than request new tabs. Keep Revert's alternative-file
    checks unchanged.
  - Render ampersands literally in resolved labels so that tabs, maps-menu
    entries, and frame titles display the same name characters rather than
    interpret those characters as menu instructions.
- **Design:**

  ```plantuml
  @startuml
  set separator none
  package "Window-owned custom names" {
    package org.freeplane.main.application {
      class "<plain>**+**</plain> CustomTabTitleProvider" as CustomTabTitleProvider {
        {static} ~ <plain>**+**</plain> CUSTOMIZED_TAB_NAME_PROPERTY: StringProperty
        {static} ~ <plain>**+**</plain> getCustomizedTabName(window: DockingWindow): String
        + <plain>**+**</plain> getTitle(window: DockingWindow): String
      }
    }
    package net.infonode {
      package docking {
        class "<plain>**=**</plain> DockingWindow" as DockingWindow {
          + <plain>**=**</plain> getWindowProperties(): DockingWindowProperties
        }
        package properties as dockingPropertyPackage {
          class "<plain>**=**</plain> DockingWindowProperties" as DockingWindowProperties {
            {static} + <plain>**=**</plain> PROPERTIES: PropertyMapGroup
            + <plain>**=**</plain> getMap(): PropertyMap
            + <plain>**=**</plain> setTitleProvider(\ntitleProvider: DockingWindowTitleProvider\n)\n: DockingWindowProperties
          }
        }
        package title {
          interface "<plain>**=**</plain> DockingWindowTitleProvider" as DockingWindowTitleProvider {
            + <plain>**=**</plain> getTitle(window: DockingWindow): String
          }
        }
      }
      package properties as propertyDefinitionPackage {
        package types {
          class "<plain>**=**</plain> StringProperty" as StringProperty {
            + <plain>**=**</plain> get(valueContainer: Object): String
            + <plain>**=**</plain> set(valueContainer: Object, value: String): void
          }
        }
        package propertymap {
          class "<plain>**=**</plain> PropertyMapValueHandler" as PropertyMapValueHandler {
            {static} + <plain>**=**</plain> INSTANCE: PropertyMapValueHandler
          }
        }
      }
    }
  }
  CustomTabTitleProvider ..|> DockingWindowTitleProvider
  CustomTabTitleProvider --> StringProperty : defines customizedTabName
  CustomTabTitleProvider --> DockingWindowProperties : registers in PROPERTIES
  CustomTabTitleProvider --> PropertyMapValueHandler : uses existing value storage
  CustomTabTitleProvider --> DockingWindow : reads owner and current map state
  DockingWindow *-- DockingWindowProperties : owns saved properties
  DockingWindowProperties --> DockingWindowTitleProvider : title formatting
  @enduml
  ```

  - Move `MapViewDockingWindows.getCustomTitle()` formatting into
    `CustomTabTitleProvider`, replacing its bound method reference and
    client-property ownership. The new class is runtime formatting, not
    a second custom-name owner.
  - The persisted window property is `customizedTabName`, represented as
    an ordinary string in the existing `openedNow_1.3.04` InfoNode layout
    stream. Absent/null means automatic naming; confirmed non-empty input
    is retained literally without trimming or shortening. Literal
    asterisks and count-like text are not metadata.
  - The title provider is stateless and not serialized. Custom-name
    metadata adds no Freeplane class identity or captured runtime objects
    to the stream. Keep the existing map-URL/mode view payload and
    restoration eligibility.
  - Individual titles use the shared resolved label. Named groups append
    `" (count)"` only above one and `" *"` only when a contained map is
    unsaved and writable. Count contained map views, including multiple
    views of one map, rather than distinct models. Unnamed groups retain
    InfoNode's concatenated view titles. New containers have no custom
    property; reparenting does not copy or replace a surviving owner's
    property values.

  ```plantuml
  @startuml
  set separator none
  package "Docking-owner view bindings" {
    package org.freeplane {
      package main.application {
        class "<plain>**±**</plain> MapViewDockingWindows" as MapViewDockingWindows {
          + <plain>**±**</plain> MapViewDockingWindows(\napplicationViewController: ApplicationViewController\n)
          + <plain>**±**</plain> loadLayout(): void
          + <plain>**±**</plain> afterViewChange(\npOldMap: Component,\npNewMap: Component\n)\n: void
          + <plain>**±**</plain> afterViewClose(pOldMapView: Component): void
          {static} ~ <plain>**=**</plain> getContainedMapView(dockedWindow: View): Component
        }
        class "<plain>**±**</plain> MapViewSerializer" as MapViewSerializer {
          # <plain>**±**</plain> newDockedView(pNewMap: Component, title: String): View
          + <plain>**=**</plain> writeView(view: View, out: ObjectOutputStream): void
          + <plain>**=**</plain> readView(in: ObjectInputStream): View
        }
      }
      package view.swing.map {
        class "<plain>**±**</plain> MapView" as MapView {
          + <plain>**+**</plain> getCustomizedTabName(): String
          + <plain>**+**</plain> setCustomizedTabNameSupplier(\nnameSupplier: Supplier<String>\n)\n: void
          + <plain>**±**</plain> setMap(viewedMap: MapModel): void
          + <plain>**=**</plain> selectAsTheOnlyOneSelected(\nnewSelected: NodeView,\nrequestFocus: boolean\n)\n: void
          + <plain>**=**</plain> isSelected(): boolean
          + <plain>**=**</plain> refreshTitle(): void
        }
      }
    }
    package java.util.function {
      interface "<plain>**=**</plain> Supplier<String>" as Supplier {
        + <plain>**=**</plain> get(): String
      }
    }
  }
  MapViewSerializer --> MapView : binds current owner lookup once
  MapView --> Supplier : retains lookup, not name
  MapViewDockingWindows --> MapViewSerializer : creates docking owners
  MapViewDockingWindows --> MapView : individual rename and restore notifications
  @enduml
  ```

  - The supplier captures the specific docking owner created by
    `newDockedView()`, reads its current native text property, and survives
    replacement of its contained map. It is runtime state only. Clear the
    supplier before closing that owner; do not rebind it during temporary
    reparenting or preserve a copied name on `MapView`.

  ```plantuml
  @startuml
  set separator none
  package "Resolved view labels" {
    package org.freeplane {
      package main.application {
        class "<plain>**±**</plain> MapViewDockingWindows" as MapViewDockingWindows {
          + <plain>**=**</plain> propertyChange(event: PropertyChangeEvent): void
        }
      }
      package view.swing {
        package map {
          class "<plain>**±**</plain> MapView" as MapView {
            + <plain>**+**</plain> getCustomizedTabName(): String
          }
          class "<plain>**±**</plain> MapViewController" as MapViewController {
            + <plain>**±**</plain> setMap(view: Component, newMap: MapModel): void
            + <plain>**=**</plain> propertyChange(event: PropertyChangeEvent): void
          }
        }
        package ui {
          class "<plain>**±**</plain> MapDisplayNameResolver" as MapDisplayNameResolver {
            {static} + <plain>**-**</plain> CUSTOMIZED_TAB_NAME_PROPERTY: String
            {static} + <plain>**±**</plain> resolveBaseLabel(mapView: MapView): String
            {static} + <plain>**+**</plain> resolveLabel(mapView: MapView): String
            {static} + <plain>**-**</plain> resolveAutomaticBaseLabel(mapView: MapView): String
            {static} ~ <plain>**-**</plain> resolveAutomaticBaseLabel(\nmapView: MapView,\nsource: MapDisplayNameSource\n)\n: String
            {static} + <plain>**-**</plain> matchesAnyAutomaticLabel(\nmapView: MapView,\ntitleWithoutDirtyMarker: String\n)\n: boolean
            {static} + <plain>**-**</plain> removeDirtyMarker(title: String): String
          }
          class "<plain>**±**</plain> MapsMenuAction" as MapsMenuAction {
            + <plain>**±**</plain> MapsMenuAction(mapView: MapView)
            + <plain>**±**</plain> getRawText(): String
            + <plain>**±**</plain> propertyChange(event: PropertyChangeEvent): void
            + <plain>**=**</plain> actionPerformed(menuEvent: ActionEvent): void
            ~ <plain>**=**</plain> dispose(): void
          }
        }
      }
    }
  }
  MapViewDockingWindows --> MapDisplayNameResolver : docking label
  MapDisplayNameResolver --> MapView : reads current owner name
  MapViewController --> MapDisplayNameResolver : selected frame label
  MapViewController --> MapView : same-URL replacement retains selection key
  MapsMenuAction --> MapDisplayNameResolver : literal menu label
  @enduml
  ```

  - The shared label appends `" *"` only for unsaved, writable maps. The
    menu displays that label without the component-name prefix or
    ampersand conversion; `getRawText()` returns the same literal text.
    Keep action keys as `MapsMenuAction.` plus the construction-time
    component name. Use the existing superclass constructor with a null
    title, then set literal `Action.NAME`; refresh that property directly
    with the unchanged-value guard. Do not change mnemonic conversion for
    other actions. Normal menu keyboard handling remains unchanged; name
    text assigns no explicit mnemonic properties.
  - The frame places the shared label inside its existing workspace,
    mode, editing-lock/read-only, path, and bidi formatting. Do not append
    a second dirty marker. Preserve no-map/no-mode output. The filename
    automatic source retains its existing component-name input rather
    than introduce a new filename or scripting-name contract.

  ```plantuml
  @startuml
  participant JMenuItem
  participant MapViewDockingWindows
  participant JOptionPane
  participant StringProperty
  participant DockingWindowProperties
  participant MapView
  JMenuItem -> MapViewDockingWindows : ActionEvent from Rename tab
  MapViewDockingWindows -> JOptionPane : showInputDialog(prompt,\nstored name or getContainedMapView(window).getName())
  JOptionPane --> MapViewDockingWindows : input
  alt Cancel returns null
    MapViewDockingWindows --> JMenuItem : no metadata change or title request
  else confirmed input
    MapViewDockingWindows -> MapViewDockingWindows : empty becomes null; other text stays literal
    opt stored name differs
      MapViewDockingWindows -> StringProperty : set(owner property map, name)
      MapViewDockingWindows -> DockingWindowProperties : setTitleProvider(new CustomTabTitleProvider())
      DockingWindowProperties -> DockingWindowProperties : existing title and ancestor notifications
      opt individual tab
        MapViewDockingWindows -> MapView : refreshTitle()
      end
    end
  end
  @enduml
  ```

  - Replacing the runtime provider uses InfoNode's existing title
    invalidation rather than add custom-property invalidation to the
    library. Group renames leave individual frame/menu labels unchanged.
    Renaming does not dirty or save the mind map.

  ```plantuml
  @startuml
  participant DockingWindow
  participant DockingWindowAdapter
  participant StringProperty
  DockingWindow -> DockingWindowAdapter : windowRemoved(removedFromWindow, removedWindow)
  alt named container has no parent and zero children
    DockingWindowAdapter -> StringProperty : clear removed owner's customizedTabName
  else surviving container or individual view
    DockingWindowAdapter --> DockingWindow : retain owner metadata
  end
  @enduml
  ```

  - Clear eliminated-group metadata synchronously in the existing removal
    listener so a subsequent layout save cannot retain it in InfoNode's
    model. A group with children retains its metadata during reparenting,
    minimization, or undocking; do not use temporary parent loss alone as
    the clearing condition.

  ```plantuml
  @startuml
  participant MapViewDockingWindows
  participant CustomTabTitleProvider
  participant RootWindow
  participant MapViewSerializer
  participant MapView
  MapViewDockingWindows -> CustomTabTitleProvider : initialize native property during docking configuration
  MapViewDockingWindows -> RootWindow : write(out) using existing layout stream
  RootWindow --> MapViewDockingWindows : owner text values written by native property maps
  ... subsequent layout restoration ...
  MapViewDockingWindows -> RootWindow : read(in) with layout-loading guard enabled
  RootWindow -> MapViewSerializer : readView(in)
  MapViewSerializer -> MapView : setCustomizedTabNameSupplier(owner lookup)
  MapViewSerializer --> RootWindow : fresh ConnectedToMenuView
  RootWindow --> MapViewDockingWindows : restored owners and property values
  MapViewDockingWindows -> MapViewDockingWindows : remove dummy views; install runtime title providers
  MapViewDockingWindows -> MapViewDockingWindows : clear layout-loading guard
  loop restored map views
    MapViewDockingWindows -> MapView : refreshTitle()
  end
  @enduml
  ```

  - Reinstall runtime formatters without overwriting owner text. Until
    property reading and dummy-view removal finish, title requests must
    not write restored presentation properties. Reinstate the common
    default provider as well as restored per-owner formatters. Afterwards
    recompute labels from current model/preferences and explicit metadata. A
    missing custom-name property always selects automatic naming.
  - Remove the old Swing `customizedTabName` client-property constant and
    listener, initial-name loading flag/recovery, automatic-label matching,
    dirty-marker parsing, and unused automatic-label recovery entrypoints.
    There is no compatibility path that infers a custom name from text.

  ```plantuml
  @startuml
  set separator none
  package "Map reload and Revert" {
    package org.freeplane.features {
      package map.mindmapmode {
        class "<plain>**±**</plain> MMapController" as MMapController {
          + <plain>**±**</plain> restoreCurrentMap(): void
          + <plain>**±**</plain> restoreCurrentMapIgnoreAlternatives(): void
        }
        class "<plain>**=**</plain> MMapModel" as MMapModel {
          + <plain>**=**</plain> beforeViewCreated(): void
          + <plain>**=**</plain> enableAutosave(): void
        }
      }
      package ui {
        interface "<plain>**=**</plain> IMapViewManager" as IMapViewManager {
          + <plain>**=**</plain> getViews(map: MapModel): List<Component>
          + <plain>**=**</plain> setMap(view: Component, map: MapModel): void
          + <plain>**=**</plain> newMapView(\nmap: MapModel,\nmodeController: ModeController\n)\n: void
        }
      }
      package url.mindmapmode {
        class "<plain>**±**</plain> RevertAction" as RevertAction {
          + <plain>**=**</plain> actionPerformed(ev: ActionEvent): void
        }
        class "<plain>**=**</plain> MapLoader" as MapLoader {
          + <plain>**=**</plain> load(url: URL): MapLoader
          + <plain>**=**</plain> asDocumentation(): MapLoader
          + <plain>**=**</plain> getMap(): MapModel
        }
      }
    }
  }
  RevertAction --> MMapController : existing action delegates to shared replacement
  MMapController --> IMapViewManager : replaces each existing view binding
  MMapController --> MMapModel : initializes new view model and autosave
  MMapController --> MapLoader : documentation loading without a new view
  @enduml
  ```

  ```plantuml
  @startuml
  participant MMapController
  participant MFileManager
  participant MMapModel
  participant MapViewController
  participant MapView
  MMapController -> MMapController : retain URL guards and ordinary Revert alternative-file checks
  MMapController -> MapViewController : getViews(oldMap); snapshot existing view references
  MMapController -> MMapController : set waiting cursor; release old map resources
  MMapController -> MFileManager : loadAndLock(sourceURL, newModel)
  MMapController -> MMapModel : set original URL and saved state
  MMapController -> MMapController : fireMapCreated(newModel); addLoadedMap(newModel)
  opt existing Swing views
    MMapController -> MMapModel : beforeViewCreated()
    loop every existing view of the old map
      MMapController -> MapViewController : setMap(view, newModel)
      MapViewController -> MapView : setMap(newModel)
      MapView -> MapView : selectAsTheOnlyOneSelected(root, isSelected())
      MapViewController -> MapView : retain prior component name for the same non-null URL
      MapViewController -> MapViewController : existing afterMapChange listeners
      MapViewController -> MapView : refreshTitle()
    end
  end
  MMapController -> MMapController : closeWithoutSaving(oldMap) after rebinding
  MMapController -> MMapModel : enableAutosave()
  opt no Swing views
    MMapController -> MMapController : createMapView(newModel), including existing undo setup
  end
  MMapController -> MMapController : clear waiting cursor in finally
  @enduml
  ```

  - Same-URL component names include unique suffixes and scripting names.
    Other `setMap()` callers retain existing changed-URL/name behavior.
    Suppressing background-view focus requests does not retain old node
    selections; the reset uses the existing two-argument selection API.
    The empty-view path is required by the headless manager's existing
    contract, not custom-name compatibility recovery.

  ```plantuml
  @startuml
  participant MMapController
  participant MapLoader
  participant MMapModel
  participant IMapViewManager
  MMapController -> IMapViewManager : getViews(oldMap); snapshot existing view references
  MMapController -> MMapController : temporarily remove old model registration under existing cache locking
  MMapController -> MapLoader : load(url).asDocumentation().getMap()
  alt load fails
    MMapController -> MMapController : restore old model registration
    MMapController -> MMapController : propagate existing load exception; end replacement
  else load succeeds
    MapLoader --> MMapController : fresh read-only documentation model, created and registered
    alt existing Swing views
      MMapController -> MMapModel : beforeViewCreated()
      loop existing views
        MMapController -> IMapViewManager : setMap(view, newModel) with the same view/key lifecycle
      end
      MMapController -> MMapController : closeWithoutSaving(oldMap) after rebinding
    else no Swing views
      MMapController -> MMapController : closeWithoutSaving(oldMap); createMapView(newModel)
    end
  end
  @enduml
  ```

  - Documentation replacement retains its no-lock/no-autosave behavior,
    bypasses alternative-file selection, and adds no waiting-cursor change.
    Ordinary documentation opening still uses its existing view-creating
    API; only replacement uses the existing no-view loader capability.
  - Before the first view is rebound, a cancelled source selection or a
    thrown load error must leave the existing view/owner bindings and name
    properties untouched. Keep waiting-cursor cleanup and the existing
    ordinary-map resource-release/load-error policy; do not promise rollback
    of released locks or autosave after a load exception. Keep normal model
    creation/removal notifications and initialize the new undo handler.
  - Replace `RevertAction`'s obsolete close-and-open Javadoc with its
    view-preserving behavior; its action callback and exception logging
    remain unchanged.
  - **UI text:**
    - Keep `TabPopUpMenu.rename.text` and
      `TabPopUpMenu.rename.inputDialog`; align the Java dialog fallback
      with the existing English prompt `Input new tab name:` rather than
      describe a temporary name.
    - In
      `freeplane/src/viewer/resources/translations/Resources_en.properties`,
      replace the tooltip resource key:

      ```diff
      -TabPopUpMenu.rename.tooltip
      +TabPopUpMenu.rename.persistent.tooltip
      ```

      Set the English text to
      `Empty input restores the automatic title. Names are saved with the
      tab's layout. Removing a group discards its name.`
      Use the same fallback in the popup. Remove the obsolete key from all
      translation files so localized warnings are not retained as current
      instructions; untranslated new text uses the normal English fallback.
  - **Living project documents:**
    - `freeplane/doc/history_en.txt` — users reading release notes;
      **update** the current release section with owner-based name
      preservation across restart, reload, and Revert; consistent labels;
      Cancel versus empty input; and the requirement to re-enter custom
      names from layouts without explicit metadata. No README, project
      glossary, or ADR change is required.
- **Test specification:**
  - Use the real docking layout writer/reader and `MapViewSerializer` for
    restart regressions. Restore into fresh owners and map views after
    closing the originals; never return original owners from a serializer
    mock or copy their custom-name properties in fixture code. Exercise
    saved-map eligibility with lightweight `addNotify()`/`removeNotify()`,
    including ineligible-view dummy removal.
  - Reuse native docking setup through
    `org.freeplane.main.application.MapViewDockingWindowsTestSupport`,
    extracted from the existing docking tests, for the new reload tests.
    Keep its native drag-and-drop, theme, overview-menu, and dialog
    substitutions. Model loading may substitute file I/O, but replacement,
    title subscriptions, owners, and rendered menu controls remain real.
    Native focus gates and modal-dialog results are explicit inputs;
    exercise their actual event callbacks without an asynchronous timer.
    Native frame painting is impractical headlessly: assert the exact
    frame text delivered by the real view manager, retain the real frame
    setter tests, and supplement them with the desktop checks below.
    Full desktop exit/startup also needs native windows; fresh-owner layout
    callbacks do not claim to perform an application restart.
  - Replace obsolete client-property publication, heuristic recovery,
    mnemonic-markup, and old frame/menu-format cases with the cases below.
    Preserve their relevant targeting, context, ordering, and lifecycle
    assertions. Unchanged first-subtask title-source tests remain
    regression coverage; no production-only testing API is added.
  - **Automated tests:**
    - **±** `org.freeplane.main.application.MapViewDockingWindowsTest`
      - **±** `viewTitleRequestUpdatesOnlyItsDockingWindow`: rename
        through the real popup item; update only that owner and its path
        tooltip without scanning manager or tracked-view registries or
        writing unrelated window properties.
      - **=** `unchangedTitlesAndTooltipsAreNotAssignedAgain`: repeated
        requests retain unchanged-value guards.
      - **±** `changedLeafTitleUpdatesDockingAncestors`: real individual
        renames and map dirty-state changes propagate through nested
        owners, not unrelated docking branches.
      - **±** `dockingSubscriptionsFollowViewLifetime`: keep one title
        subscription through reparenting; clear owner-name access and
        detach before native removal; late requests change no displays.
      - **±** `tabRenamePublishesOneViewTitleRequest`: clicking Rename
        with changed literal text publishes once only for that individual
        view; equal input and Cancel change nothing; confirmed empty input
        clears the property and publishes once. Cover whitespace,
        ampersands, Unicode, asterisks, and count-like text without trimming
        or shortening; another view of the same map retains its own name.
      - **+** `renamePopupUsesPersistentNameInstructions`: real
        individual/group popups use the persistent-name tooltip and
        existing English input prompt, with matching Java fallbacks rather
        than obsolete temporary-name/layout-reset instructions. Initial
        input is the stored name or the existing contained-component name,
        including a null group default.
      - **±** `dockingFocusSelectsTheViewReference`: equal owner names
        do not redirect the native focus callback to another view or the
        string-selection overload.
      - **+** `groupRenameLeavesIndividualLabelsUnchanged`: use the
        real group popup item; update its displayed title and ancestors
        without individual title requests, menu changes, or frame changes;
        group Cancel/equal input/empty input follow the same rules.
      - **+** `literalLabelsAgreeAcrossTabsMenuAndFrame`: rename through
        the popup and assert the same name characters and single derived
        dirty marker in the real InfoNode titled-tab control text,
        checkbox-menu text/raw text, and selected-view frame label, with
        path and mode context separate.
      - **+** `dirtyMarkersComeFromMapStateNotNameText`: literal trailing
        asterisks and count-like text remain part of stored names; saved,
        unsaved, and read-only state determine only the derived markers.
        Group counts include separate views of the same model.
      - **+** `layoutRestoresNamesOnFreshOwners`: actual application
        save/load layout callbacks retain differently named individual
        views of one map and a named group on fresh objects, preserving
        maps and docking structure. Current model state determines dirty
        markers; serialized names contain no derived decoration.
      - **+** `customNameMatchingAutomaticTextRemainsCustom`: save an
        explicit name equal to an automatic label; after fresh-owner load
        and preference/root changes, it remains explicit and unchanged.
      - **+** `clearedNamesStayAutomaticAfterRestoration`: empty-confirm
        individual/group names, save, and restore fresh owners; their raw
        properties are absent and labels follow current automatic sources.
      - **+** `oldLayoutsRestoreAutomaticNamesWithoutGuessing`: restore
        metadata-free native layouts with arbitrary old rendered titles,
        trailing dirty markers, and old group client properties; preserve
        eligible maps/layout but use only current automatic labels.
      - **-** `layoutRestorationKeepsExistingIndividualNameRecovery`:
        remove heuristic-recovery expectations; replacement coverage is
        `oldLayoutsRestoreAutomaticNamesWithoutGuessing` and
        `layoutRestoresNamesOnFreshOwners`.
      - **+** `restoredNamesDoNotOverwriteLaterRenames`: after fresh
        load, a later popup rename survives subsequent title, preference,
        dirty-state, and URL changes without repeating recovery.
      - **+** `layoutDoesNotSerializeCustomTitleProvider`: the actual
        saved stream round-trips text without a serialized
        `CustomTabTitleProvider` class identity or captured runtime graph.
      - **+** `nativeReaderSkipsUnknownNameProperty`: the native
        property-map reader without the custom property definition
        consumes its encoded string without error or stream misalignment.
      - **+** `malformedLayoutUsesExistingEmptyFallback`: corrupt saved
        layout input restores the existing empty configuration; later
        opening/renaming a map works with the loading guard cleared.
      - **+** `ineligibleViewsDoNotRestoreCustomNames`: views of maps
        without a URL and documentation views are not made restorable by
        assigning names;
        dummy removal leaves only eligible owners and their own metadata.
      - **+** `survivingGroupKeepsItsNameWhenChildrenMove`: moving one
        child out of a three-view group retains the same owner and name
        while its count and dirty marker reflect its remaining views.
      - **+** `movingAGroupPreservesItsOwnerAndName`: whole-group
        reparenting retains owner identity and metadata while its parent is
        temporarily absent; title subscriptions are not duplicated.
      - **+** `eliminatedGroupDoesNotTransferItsName`: collapsing a
        two-view group clears its retained native property before another
        layout save; remaining/new owners retain only their own names,
        including after fresh-owner restoration.
      - **+** `closingAndRecreatingSameUrlDoesNotReuseNames`: a new view
        or group for previously closed maps does not recover a removed
        owner's name by URL or equal displayed text.
      - **+** `changedUrlKeepsNamesAndUpdatesPaths`: supported URL/name
        updates retain individual/group metadata, update path context,
        and keep legitimate selection-key/menu-order changes separate.
      - **+** `renamesDoNotChangeMindMapContents`: real individual and
        group rename/clear actions leave map saved state and serialized
        mind-map contents unchanged and request no map save.
    - **+** `org.freeplane.main.application.MapViewReloadTest`
      - **+** `externalReloadPreservesAllViewsAndNames`: accept the real
        external-change callback for several views of one map in named
        owners; all bind to the new model with unchanged components,
        docking parents, names, selected view, unique scripting keys,
        menu-action identities, and unrelated views. Check new content,
        undo-handler setup, new-model registration, old-model removal
        after rebinding, autosave, dirty state, and listener cleanup.
      - **+** `revertPreservesViewsWhenLoadingAnAlternativeFile`: click
        a real menu item using `RevertAction`; retain its alternative-file
        choice and original map URL, mark alternate contents unsaved, and
        retain every view/owner/name/key and the selected view.
      - **+** `cancelledExternalReloadLeavesViewsAndNamesUnchanged`:
        rejecting the external-change dialog performs no load, replacement,
        owner/name mutation, or display update.
      - **+** `cancelledAlternativeSelectionLeavesViewsAndNamesUnchanged`:
        cancelling Revert's source choice releases no resources and keeps
        the original model, owners, names, and selection.
      - **+** `revertWithoutUrlKeepsExistingErrorBehavior`: the real
        action for an unsaved map reports the existing error without
        loading or changing its owners/names.
      - **+** `failedReloadKeepsExistingViewOwners`: a load exception
        after ordinary resource release leaves old view/model bindings and
        names untouched and clears the waiting cursor; do not assert a
        new lock/autosave rollback contract.
      - **+** `revertingDocumentationKeepsOwnersAndReadOnlyState`:
        bypass alternative-file selection, load a fresh documentation model
        rather than return the cached old model, preserve views/names, and
        retain read-only, no-lock/no-autosave behavior.
      - **+** `failedDocumentationReloadRestoresLoadedMapRegistration`:
        a documentation load error restores the old cache entry and leaves
        its existing owners, bindings, names, and selection untouched.
      - **+** `headlessReloadReplacesOnlyTheModel`: the actual headless
        manager replaces the model without Swing views, docking owners, or
        a new title API, retaining URL/saved-state and lifecycle behavior.
    - **±** `org.freeplane.view.swing.map.MapViewControllerTitleTest`
      - **+** `frameTitleUsesResolvedLabelWithExistingContext`: assert
        exact custom/automatic labels and single dirty markers within
        workspace, mode, editing-lock/read-only, absolute-path, and bidi
        formatting, including no-map/no-mode output.
      - **±** `stringSelectionKeepsExistingKeysAndErrors`: owner names
        are not selection keys; retain suffixes, scripting name changes,
        immutable key/map snapshots, and invalid-key behavior.
      - **±** `onlyTheSelectedViewCanUpdateTheFrameTitle`: current
        owner-name changes reach the selected view's frame subscription;
        other views have none.
      - **=** `selectionChangesMoveTheFrameSubscriptionOnce`: same-view
        notifications do not duplicate frame listeners or refresh tabs/menu.
      - **=** `selectionNotifiesOnlyTheOldAndNewViews`: retain source
        targeting without a view-registry scan.
      - **±** `mapReplacementKeepsViewAndRebindsNotifications`: owner
        lookup and display subscriptions survive; only the new model
        notifies the view after replacement listeners complete.
      - **±** `mapReplacementRebuildsMenuOnlyWhenItsKeyChanges`:
        same-URL replacements preserve distinct component keys before
        replacement listeners and final title requests; changed-URL
        replacements retain existing rekey/menu-rebuild behavior.
      - **+** `mapReplacementDoesNotFocusUnselectedViews`: a showing
        background view resets its node selection without requesting native
        focus; the selected view retains its existing focus request.
      - **+** `mapReplacementKeepsExistingViewStateResets`: replacing
        the model resets current root, node selection, filter, root
        history, and zoom as before; name preservation does not retain
        references to nodes from the old model.
      - **±** `closedViewRequestsDoNotChangeDisplays`: detached title
        requests cannot update frame, docking, or disposed menu actions.
      - **=** `urlSelectionUsesTheMatchingViewReference`: URL matching
        continues to select the actual view and retains its string result.
    - **±** `org.freeplane.view.swing.ui.MapsMenuActionTest`
      - **±** `menuClickSelectsItsViewWithEqualLabels`: actual checkbox
        items with equal displayed names select their captured references;
        later name/preference changes cannot redirect the delayed click.
      - **=** `delayedMenuClickDoesNotSelectAClosedView`: retain warning
        and no selection after closure before delayed focus.
      - **±** `titleRequestUpdatesTheExistingMenuItem`: the real wrapped
        checkbox item receives literal labels and derived dirty markers
        without replacement or component-name prefixes.
      - **±** `rawTextTracksTheCurrentFormattedLabel`: raw and rendered
        text contain the same current literal label, not mnemonic markup
        or a stale constructor label.
      - **+** `ampersandsRemainLiteralAcrossTitleChanges`: single,
        doubled, leading, and trailing ampersands remain visible in the
        real item; construction and refresh assign no name-derived
        mnemonic/index on supported platforms.
      - **-** `titleChangesReplaceMnemonicProperties`: remove the
        obsolete name-derived mnemonic expectation; replacement coverage
        is `ampersandsRemainLiteralAcrossTitleChanges`.
      - **=** `unchangedPresentationDoesNotRewriteActionProperties`:
        equal labels cause no redundant property setters.
      - **=** `selectionUpdatesTheExistingCheckBoxItem`: existing
        controls follow old/new selection without menu reconstruction.
      - **±** `disposingActionDetachesItsViewSubscriptions`: late owner
        name/title requests cannot change the disposed action.
    - **±** `org.freeplane.view.swing.ui.UserInputListenerFactoryMapActionsTest`
      - **+** `mapsMenuUsesResolvedLabelsWithExistingOrderAndSelection`:
        real menu construction displays shared labels without prefixes,
        keeps original action keys and component-key ordering, and marks
        the selected view even when labels are equal.
      - **-** `mapsMenuKeepsExistingLabelsOrderAndSelection`: remove
        obsolete component-name prefixes; replacement coverage is
        `mapsMenuUsesResolvedLabelsWithExistingOrderAndSelection`.
      - **±** `titleChangesKeepExistingEntriesAndActions`: real root,
        rename, dirty-state, and preference changes preserve affected
        entry/action/control identity and unrelated controls, without
        title-notification registry scans or menu reconstruction.
      - **±** `menuRebuildDisposesPreviousActions`: real structural
        rebuild/hide/reopen paths detach before unregistration and retain
        only live action subscriptions with the current literal labels.
    - **±** `org.freeplane.view.swing.ui.MapDisplayNameResolverTest`
      - **+** `resolvedLabelAddsOnlyCurrentDirtyMarker`: literal custom
        text or the configured automatic source gains one derived marker
        only for unsaved writable maps; a literal asterisk is not state.
      - **=** `shouldUseCustomizedTabNameWithoutTruncation`: custom
        text has precedence and is not shortened.
      - **±** `shouldResolveFileNameSourceWithTruncation`: use the
        remaining configured-source entrypoint after removing recovery;
        preserve maximum length and continuation-mark assertions.
      - **-** `shouldMatchAnyAutomaticLabelAcrossSources`: remove
        heuristic-matching verification; the docking case
        `oldLayoutsRestoreAutomaticNamesWithoutGuessing` verifies that
        metadata-free layouts use current automatic labels instead.
      - **=** `shouldUseCurrentViewRootNameWhenConfigured`: retain the
        configured text source.
      - **=** `shouldFallbackToFileNameWhenCurrentViewRootNameMissing`:
        missing root text retains filename fallback.
      - **=** `shouldUseCurrentViewRootBookmarkTitleWhenConfigured`:
        preserve bookmark shortening.
      - **=** `usesNearestNonFormulaAncestorForViewRootText`: retain
        actual ancestor traversal and missing-text-service fallback.
      - **=** `usesNearestAncestorBookmarkForViewRootBookmark`: retain
        actual ancestor bookmark lookup and filename fallback.
    - **±** `org.freeplane.view.swing.map.MapViewTitleTest`
      - **-** `customNameChangeNotifiesOnlyItsView`: remove publication
        from the obsolete map-view client property; the docking case
        `tabRenamePublishesOneViewTitleRequest` verifies owner-based
        rename publication and unchanged-value guards.
      - **=** `titleRequestIdentifiesItsViewWithoutCopiedDisplayState`:
        keep source-only notification without copied title/dirty state.
      - **=** `viewRootChangeNotifiesOnlyItsSourceView`: root changes
        notify only that view.
      - **=** `sharedMapChangesNotifyOnlySubscribedViews`: shared
        changes reach the existing per-map subscriptions only.
      - **=** `labelPreferencesNotifyRegisteredViewsDirectly`: label
        preferences retain existing listener fan-out without registry scans.
      - **=** `closingViewsRemovesMapAndPreferenceSubscriptions`:
        closing one or all views removes model/resource registrations.
    - **=** `org.freeplane.main.application.ApplicationViewControllerTitleTest`
      - **=** `settingFrameTitleDoesNotRefreshTabs`: the real frame
        setter remains frame-only.
      - **=** `unchangedFrameTitleIsNotAssignedAgain`: equal frame text
        produces no additional setter call.
  - **Manual tests:**
    - Use disposable saved maps and a disposable user-config directory
      selected with the existing `-U<userdir>` launcher option. Record
      initial title preferences and restore them afterward. Do not replace
      the normal saved layout with the test layout.
    - In the desktop application, name two views of one saved map
      differently and name a group containing them. Include `&`, Unicode,
      literal `*`, and count-like text. Exit normally and restart with the
      same user directory; inspect native tab/frame painting and selected
      menu marks. Automated tests cover the underlying saved values and
      fresh-owner restoration; this checks application exit/startup wiring.
    - Change the file in an external editor, return focus, accept reload,
      and use Revert with an available alternative file. Inspect native
      focus, docking positions, names, and newly loaded contents. The
      selected view stays selected; view root, node selection, filter,
      history, and zoom retain the existing replacement reset behavior.
    - Drag a whole named group, move children without eliminating it, and
      collapse a two-view group. Inspect native drag/drop painting: only a
      surviving owner retains its name. Inspect native tooltip/dialog
      painting and Cancel/empty-input behavior; automated menu tests cover
      their text, callbacks, and metadata.
