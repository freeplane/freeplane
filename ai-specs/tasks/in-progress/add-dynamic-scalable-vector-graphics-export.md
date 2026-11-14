# Task: Add dynamic SVG export

- **Task Identifier:** 2026-11-13-dynamic-svg
- **Scope:** Integrate the functionality of
  [Freeplane Interactive HTML Companion](https://github.com/shlomiv/freeplane-interactive-html)
  into `freeplane_plugin_svg` as an additional export option implemented
  in Java, producing one `.html` file containing SVG, CSS, and JavaScript.
  Export current content, including unsaved changes, under the current
  view root and within the current filter scope. Include folded
  descendants within that scope and initialize the viewer with the
  current folding state. Include captured node artwork,
  branch folding, expand/collapse all, search, pan, zoom, fit, single-level
  summaries, and cubic connectors. Fail export with a clear explanation
  for unsupported summary structures or connector shapes. Preserve
  custom-scheme links and delegate their handling to
  the browser instead of retaining the source viewer's HTTP, HTTPS, and
  mail restriction. Preserve relative links as written so that the HTML
  and referenced files can be distributed together with the same relative
  directory structure. Links to nodes within the exported map unfold the
  target's ancestors and center the target in the viewer. The option
  label is "Dynamic SVG (HTML)". Shared file-copy and ZIP options
  are outside this task and tracked in
  [Add shared HTML export file-copy and ZIP options](../backlog/006-add-shared-export-file-copy-and-archive-options.md).
- **Motivation:** Make the interactive export available from Freeplane
  without a separate Python launcher, temporary application profile, or
  scripting setup.
- **Scenario:** Current content, including unsaved changes, is exported
  under the current view root and within the current filter scope.
  Folded descendants within that scope remain available for unfolding.
  The exported HTML file opens in a browser without Freeplane and starts
  with the current folding state. Branches can be folded and unfolded.
  Node text and captured details can be searched, and the map can be
  panned, zoomed, and fitted to the available area. Activating a link to
  a node within the exported map unfolds the target's ancestors and
  centers the target in the viewer. Custom-scheme links remain present.
  Activating them delegates to the browser and any installed handler.
  Unsupported summary structures or connector shapes produce a clear
  export error rather than silently omitted elements. If requested font
  embedding cannot be completed, export continues with affected text
  dependent on fonts available on the recipient's machine and warns
  about incomplete embedding.
- **Constraints:**
  - Keep the existing SVG, PDF, and HTML export options available.
  - Reuse "Embed fonts into SVG"
    (`org.freeplane.plugin.svg.export.svg.embed_fonts`). When enabled,
    embed browser-compatible fonts where possible and warn if embedding
    is incomplete. Fonts that cannot be embedded remain dependent on
    fonts available on the recipient's machine. When disabled, use fonts
    available on the recipient's machine without an incomplete-embedding
    warning. Do not introduce independent font-export settings or apply
    the PDF-specific text-to-shapes setting to this export.
  - Support Chrome, Firefox, Safari, and Edge, including opening the
    exported artifact locally. Link behavior remains subject to browser
    restrictions and installed handlers.
  - Do not rebase relative links to the source map or HTML output
    location. Distribution must preserve the relative directory structure
    needed by those links.
  - Preserve the source map and its view state during export, including
    failed export.
  - Do not begin implementation without separate explicit approval.
    Do not change unrelated work in the repository.
- **Briefing:** `freeplane_plugin_svg` contains `Activator`, `ExportSvg`,
  and `ExportVectorGraphic`. `ExportController` and `IExportEngine` provide
  the existing export registration and invocation mechanism. The source
  exporter uses Freeplane's Swing renderer and Batik; browser interaction
  is implemented in its HTML/CSS/JavaScript viewer.
- **Research:**

  ```plantuml
  @startuml
  component "Python companion" as Companion
  component "Freeplane" as Freeplane
  component "Groovy exporter" as Exporter
  component "SVG plugin / Batik" as Svg
  artifact "HTML with SVG and JSON manifest" as Html
  component "Browser viewer" as Viewer
  Companion --> Freeplane : launch with saved map
  Freeplane --> Exporter : invoke trusted script
  Exporter --> Freeplane : read map and capture renderer state
  Exporter --> Svg : generate node artwork
  Exporter --> Html : write
  Viewer --> Html : read
  @enduml
  ```

  ```plantuml
  @startuml
  participant "Groovy exporter" as Exporter
  participant "Freeplane map view" as View
  participant "Browser viewer" as Viewer
  Exporter -> View : unfold and lay out nodes for capture
  Exporter -> View : capture node artwork and connector geometry
  Exporter -> View : restore map view state
  Exporter -> Viewer : provide HTML with artwork and manifest
  Viewer -> Viewer : calculate layout and handle interactions
  @enduml
  ```

  - Updated source baseline:
    `918c34f2c1d593b4505fbc016c554b5210e94d24`, superseding
    `7bb8d6c09e15b0a066158b511d00ee7c19056f2b`. This includes merged
    [PR #4](https://github.com/shlomiv/freeplane-interactive-html/pull/4).
    Its exporter and viewer template are byte-identical to the
    previously inspected PR head
    `b8b0dc1cd6aaa714cdec3e5c23eb992efceca420`. Node artwork uses SVG
    text through Freeplane's existing graphics class; font embedding
    remains absent.
  - Upstream commit `fa1b005006394447de4863d169ed9367f8d9ed45`
    records GPL-2.0-or-later for authored content in `LICENSE`, `NOTICE`,
    `README.md`, and the Python packaging/companion SPDX headers. This
    verifies the licensing agreement previously reported for this task.
  - `freeplane/src/viewer/resources/freeplane.properties` defaults
    `org.freeplane.plugin.svg.export.svg.embed_fonts` to `true` and
    `org.freeplane.plugin.svg.export.pdf.convert_text_to_shapes` to
    `false`. The latter is currently a PDF-specific option.
  - Batik's SVG font embedding generates SVG font elements, not WOFF or
    WOFF2 web fonts. Following the existing setting therefore does not
    establish a browser-compatible implementation by itself.
  - Source viewer limitations include custom layout, single-level
    summaries, cubic connectors, and links restricted to HTTP, HTTPS,
    and mail. Connector labels are captured but not displayed. Initial
    map folding is not included in the viewer manifest.
  - `README.md` contains general application and contribution links,
    not export-specific instructions or a project document list. No
    project `glossary.adoc` or `glossary.md`, or existing SVG-plugin
    export guide, was found.

  ```plantuml
  @startuml
  set separator none
  package "Freeplane" {
    package "org.freeplane" {
      package "view.swing.map" {
        class MapView {
          - mapScroller: MapScroller
          - nodeViewFolder: NodeViewFolder
          - filterNodeViewFolder: NodeViewFolder
          - paintingMode: PaintingMode
          - zoom: float
          - scrollsViewAfterLayout: boolean
          + getRoot(): NodeView
          + getFilter(): Filter
          + preparePrinting(): void
          + endPrinting(): void
          + setZoom(float zoom): void
        }
        class MapScroller {
          - anchor: NodeView
          - anchorContentLocation: Point
          - scrolledNode: NodeView
          - scrollingDirective: ScrollingDirective
        }
        class NodeViewFolder {
          - unfoldedNodeViews: Set<NodeView>
          + foldingWasSet(NodeView view): void
        }
        package "link" {
          class ConnectorView {
            - arrowLinkCurve: Shape
            - arrows: List<Polygon>
            - stroke: BasicStroke
            + paint(Graphics graphics): void
          }
        }
      }
      package "plugin.svg" {
        abstract class ExportVectorGraphic {
          # createSVGGraphics2D(MapView view): SVGGraphics2D
        }
        class "ExportVectorGraphic.SVGExportGraphics2D" as ExportGraphics {
          - SVGExportGraphics2D(\nSVGGeneratorContext generatorCtx,\nboolean textAsShapes\n)
          + create(): Graphics
          + drawString(String s, float x, float y): void
          + drawString(\nAttributedCharacterIterator ati,\nfloat x, float y\n)\n: void
        }
      }
    }
    MapView *-- MapScroller : scrolling state
    MapView *-- NodeViewFolder : automatic folding
    ConnectorView --> MapView : resolves endpoint views
    ExportVectorGraphic +-- ExportGraphics : private nested class
  }
  @enduml
  ```

  - `MapView.setZoom` changes pending scrolling state. View folding also
    updates automatic folding state. Restoring only zoom, node folds,
    and viewport coordinates does not establish complete restoration.
  - Constructing a `MapView` calls `setMap`, registers a map-change
    listener, and can fire map-change events. A detached export view is
    not established as free of effects on the running application.
  - Native connector geometry and the SVG graphics implementation above
    are not exposed through an existing public capture operation. The
    source exporter's reflective access is not an integration API.
  - `freeplane_plugin_svg/build.gradle` already supplies FOP 2.11 and
    FontBox 3.0.3. Upstream sources for these exact versions expose font
    file discovery, font embedding URIs, PostScript names, TrueType
    subsetting, and embedding-restriction metadata. No WOFF2 encoder was
    identified in the inspected dependencies.
  - FontBox's `OS2WindowsMetricsTable` exposes restricted-embedding,
    no-subsetting, and bitmap-only flags. These flags alone do not
    establish permission to distribute an installed font as a web font.
  - Freeplane defaults to the Java logical font `SansSerif`. Mapping
    every font used during AWT painting, including logical fonts and
    glyph substitution, to an embeddable font file has not been
    established by this research.
  - `MapViewRootLifecycleTest` and
    `MapViewJumpOutContextRestorationTest` construct Swing views with
    mocked controllers and assert view registration, root history, and
    selection. They do not exercise an export-capture operation.
  - The root Gradle build supplies JUnit 4, Mockito, and AssertJ to
    subprojects. No SVG plugin tests or browser automation dependencies
    were found in the inspected source tree and Gradle files.

  ```plantuml
  @startuml
  participant ExportAction
  participant ExportDialog
  participant ExportSvg
  participant MapView
  ExportAction -> ExportDialog : export(..., singletonList(map.getRootNode()))
  ExportDialog -> ExportSvg : export(branches, destination)
  ExportSvg -> ExportSvg : createSVGGraphics2D(currentMapView)
  ExportSvg -> MapView : preparePrinting()
  ExportSvg -> MapView : getInnerBounds()
  ExportSvg -> MapView : print(graphics)
  ExportSvg -> MapView : endPrinting()
  @enduml
  ```

  - The existing export action passes the model root, but SVG output is
    painted from the current `MapView`. The action's input alone does
    not establish that a narrowed view root or filter is ignored.
  - The companion traverses every model child from the map root, then
    requires a native view for each node. It does not define a separate
    export policy for filtered maps or a view rooted inside the map.
  - `UITools.informationMessage` has an overload accepting a message
    type, allowing a standard warning dialog without a new browser
    control.
  - `NodeViewFactory` initializes content when a view is displayable.
    Its initialization registers the node view and fires creation hooks.
    Constructing an ordinary detached view does not by itself establish
    initialized export content without lifecycle effects.
  - `NodeModel.hasVisibleContent(filter)` excludes hidden summary
    markers and content rejected by the filter.
    `subtreeHasVisibleContent(filter)` includes ancestors needed for
    matching descendants. A node can therefore be needed for the tree
    without its text belonging in the output. `Filter.FilteredElement`
    distinguishes `NODE`, `CONNECTOR`, and `NODE_AND_CONNECTOR`;
    `Filter.isVisible(ConnectorModel)` applies connector filtering.
  - `SummaryNode.getSummaryLevel(root, node)` identifies native summary
    levels relative to a view root; level-1 groups can exist under
    different parents.
  - `NodeView.addChildViews` materializes children when unfolded and
    respects the view's hidden-children state. Native view folding can
    be changed without changing `NodeModel` folding.
  - `ConnectorShape` contains `LINE`, `LINEAR_PATH`, `CUBIC_CURVE`, and
    `EDGE_LIKE`. `ConnectorView` already separates connector-line
    visibility from its geometry calculation.
  - No JSON serializer or browser-test dependency was found in the SVG
    plugin or core dependencies. Maven Central contains Gson 2.13.2
    with Java 8 base classes and Selenium Java 4.38.0. Selenium can drive
    actual Chrome, Firefox, Safari, and Edge rather than substituting
    browser engines for those browsers.
  - `freeplane` tests add viewer, editor, and external resource
    directories to their runtime classpath. The editor resources
    contain `gentium-subset.ttf` and its SIL Open Font License, providing
    a repository-owned font fixture without installing a system font.
- **Analysis:**
  - Use Java in the SVG plugin so that export does not require the
    companion launcher or renderer access through a user script.
  - License the integrated exporter and viewer under GPL-2.0-or-later
    because the source author agreed to that license for the project.
  - Reuse Freeplane's existing GPL license text because a separate copy
    is unnecessary under the same license; retain upstream attribution
    and embed the license text in independently distributed HTML.
  - Reuse the SVG font-embedding setting and keep the PDF-specific
    text-to-shapes setting separate because their existing meanings are
    specific to those export formats.
  - Allow deviations from Freeplane's layout and link behavior because
    the exported map runs in a browser rather than in Freeplane.
  - Produce one HTML file containing SVG, CSS, and JavaScript so that the
    existing browser controls can be retained.
  - Preserve custom-scheme links rather than removing them because the
    exporter does not recognize their schemes; browser restrictions and
    installed handlers determine whether activation succeeds.
  - Unfold the target's ancestors and center the target when an internal
    node link is activated so that targets inside collapsed branches can
    be reached.
  - Preserve relative links as written so that the HTML and referenced
    files remain usable when distributed with the same relative
    directory structure.
  - Track shared file-copy and ZIP options separately so that they can be
    implemented and released for existing HTML exports independently of
    this exporter.
  - Export current content, including unsaved changes, and preserve the
    current folding state in the viewer so that the output represents
    the current map state.
  - Initially support single-level summaries and cubic connectors to
    keep the feature bounded. Fail clearly for unsupported summary
    structures or connector shapes rather than silently omitting those
    elements or producing incorrect output.
  - Continue export with recipient-machine fonts for text whose font
    cannot be embedded, and warn about incomplete requested embedding so
    that an unavailable or restricted font does not prevent export.
  - Respect the current view root and filter scope, while retaining
    folded descendants within that scope, so that filtered-out content
    and content outside the current view root are not included
    unintentionally.
  - Add PDFBox IO 3.0.3 explicitly because FontBox 3.0.3 requires its
    font-file I/O and the SVG plugin disables transitive dependencies
    for FontBox.
  - Keep export workflow and its capture context in
    `MapViewExportCapture`, while preserving private view state inside
    `MapView`, so that export workflow does not enlarge `MapView` or
    require exposing its private fields.
- **Design:**

  ```plantuml
  @startuml
  set separator none
  package "org.freeplane.plugin.svg" {
    class "<plain>**±**</plain> Activator" as Activator {
      + <plain>**=**</plain> start(BundleContext context): void
    }
    abstract class "<plain>**±**</plain> ExportVectorGraphic" as Vector {
      {static} # <plain>**+**</plain> SVG_EMBED_FONTS: String
      # <plain>**±**</plain> createSVGGraphics2D(MapView view): SVGGraphics2D
      # <plain>**+**</plain> createSVGGraphics2D(\nDimension size,\nBiConsumer<Font, String> textObserver\n)\n: SVGGraphics2D
    }
    class "<plain>**±**</plain> ExportVectorGraphic.SVGExportGraphics2D" as Graphics {
      + <plain>**±**</plain> create(): Graphics
      + <plain>**±**</plain> drawString(String s, float x, float y): void
      + <plain>**±**</plain> drawString(\nAttributedCharacterIterator ati,\nfloat x, float y\n)\n: void
    }
    class "<plain>**±**</plain> ExportSvg" as Svg {
      # <plain>**=**</plain> createGeneratorContext(\nDocument domFactory\n)\n: SVGGeneratorContext
    }
    class "<plain>**+**</plain> ExportDynamicSvg" as Exporter {
      + <plain>**+**</plain> export(List<NodeModel> branches, File chosenFile): void
      ~ <plain>**+**</plain> capture(\nMapViewExportCapture.ExportContext context\n)\n: DynamicSvgDocument
    }
    class "<plain>**+**</plain> DynamicSvgDocument" as Document {
      ~ <plain>**+**</plain> title: String
      ~ <plain>**+**</plain> manifest: JsonObject
      ~ <plain>**+**</plain> textByFont: Map<Font, String>
      ~ <plain>**+**</plain> write(\nPath destination,\nDynamicSvgFonts.Embedding embedding\n)\n: void
    }
    class "<plain>**+**</plain> DynamicSvgFonts" as Fonts {
      ~ <plain>**+**</plain> embed(\nMap<Font, String> textByFont,\nboolean enabled\n)\n: Embedding
    }
    class "<plain>**+**</plain> DynamicSvgFonts.Embedding" as Embedding {
      ~ <plain>**+**</plain> fontFaces: JsonArray
      ~ <plain>**+**</plain> warnings: List<String>
    }
    Activator --> Exporter : registers html export
    Exporter --|> Vector
    Svg --|> Vector
    Vector +-- Graphics : creates native SVG graphics
    Exporter --> Document : captures and writes
    Exporter --> Fonts : embeds captured text fonts
    Fonts +-- Embedding : returns fonts and warnings
    Document --> Embedding : serializes fonts
  }
  @enduml
  ```

  - Register only with `addMapExportEngine`, in the same modes as the
    existing SVG export. Use extension `html` and translation key
    `export_dynamic_svg_text` with value `Dynamic SVG (HTML)`; keep the
    existing SVG, PDF, and HTML registrations unchanged. The existing
    export dialog retains destination selection and overwrite handling.
  - Add `lib 'com.google.code.gson:gson:2.13.2'` to the SVG plugin for
    structured JSON serialization. Enable `serializeNulls()` and retain
    Gson's HTML-safe escaping; serialize map data as data, not as
    JavaScript source.
  - Add `lib ('org.apache.pdfbox:pdfbox-io:3.0.3') {transitive = false}`
    to the SVG plugin alongside FontBox 3.0.3. Keep the existing FontBox
    dependency and its disabled transitive dependencies unchanged.
  - The graphics-factory overload initializes Batik without printing a
    map. Preserve the existing generator-context dispatch, precision,
    gradient handling, text rendering, and rendering hints. A missing
    observer leaves the existing SVG and PDF behavior unchanged. Move
    the existing private `SVG_EMBED_FONTS` constant from `ExportSvg` to
    its superclass so both SVG exports read the same existing key
    without duplicated declarations; its value and behavior do not
    change. Observe actual text runs, including attributed runs and graphics
    returned by `create()`. Do not record fonts for text forced to
    shapes through `GraphicsHints.FORCE_TEXT_TO_SHAPE`.

  ```plantuml
  @startuml
  set separator none
  package "org.freeplane.view.swing.map" {
    class "<plain>**±**</plain> MapView" as View {
      ~ <plain>**+**</plain> withPreservedExportState(Supplier<T> operation): T
      ~ <plain>**+**</plain> prepareForExport(): void
      ~ <plain>**+**</plain> paintNodeForExport(NodeView node, Graphics2D graphics): void
    }
    class "<plain>**+**</plain> MapViewExportCapture" as Capture {
      + <plain>**+**</plain> MapViewExportCapture(MapView view)
      + <plain>**+**</plain> capture(Function<ExportContext, T> capture): T
    }
    class "<plain>**+**</plain> MapViewExportCapture.ExportContext" as Context {
      + <plain>**+**</plain> root: NodeView
      + <plain>**+**</plain> nodes: List<NodeView>
      + <plain>**+**</plain> initialFolding: Map<NodeModel, Boolean>
      + <plain>**+**</plain> paintNode(NodeView node, Graphics2D graphics): void
    }
    class "<plain>**±**</plain> MapScroller" as Scroller {
      ~ <plain>**+**</plain> withPreservedState(Supplier<T> operation): T
    }
    class "<plain>**±**</plain> NodeViewFolder" as Folder {
      ~ <plain>**+**</plain> withPreservedState(Supplier<T> operation): T
    }
    Capture +-- Context : provides scoped capture access
    Capture --> View : preserves state and uses native painting
    View --> Scroller : preserves scrolling
    View --> Folder : preserves folding histories
  }
  @enduml
  ```

  - These operations are internal renderer integration, not additions
    to the public scripting API. Use typed access within the classes
    that own state; do not use reflection or expose mutable state
    getters for export. The SVG plugin calls `MapViewExportCapture`
    directly; `MapView` has no public capture entry point or nested
    capture context. Scope traversal, temporary unfolding and folding
    restoration, callback orchestration, and context lifetime/scope
    checks belong to `MapViewExportCapture`. Package-private operations
    in `MapView` preserve its private state, prepare native painting and
    layout, and paint node content with its private painting mode.
  - Capture runs synchronously on the Swing event dispatch thread.
    Preserve the current view root and filter; do not switch views,
    change the model root, or construct an ordinary detached map view.
    Temporarily unfold native views under the current view root and
    lay them out at zoom 1. Do not change model folding or unlock
    encrypted content. Do not clear the view's hidden-children state.
  - Retain native views needed for content visible within the filter,
    their ancestor paths, and required summary markers. Content-hidden
    ancestors carry only the tree information needed to reach retained
    descendants: no artwork, label, search text, hyperlink, or fonts.
    Subtrees without retained content are absent from the output.
    The current view root follows native root-visibility behavior.
  - Read initial folding before temporary unfolding, using an existing
    native view where present and the controller's folding state for
    descendants not yet materialized. The callback receives an immutable
    node list and folding map, valid only during capture. Painting selects
    the native node-painting mode appropriate to the node's selection
    without changing selection; capture includes content, icons, and
    details painted by the native content component, with three pixels
    of padding added by the SVG plugin.
  - Restore folds, zoom, layout, printing state, painting mode, fit and
    printing bounds, both automatic folding histories, selection-driven
    folding state, and viewport coordinates in `finally`. Restore
    pending scrolling and the post-layout scrolling flag after restoring
    layout. Copy mutable point and rectangle values when saving state.
    Preserve selection, focus, current view root, root history, filter,
    map content, undo history, and save/dirty state. Never reset the
    model's changes-since-save counter to hide a mutation.

  ```plantuml
  @startuml
  set separator none
  package "org.freeplane.view.swing.map.link" {
    class "<plain>**±**</plain> ConnectorView" as Connector {
      + <plain>**±**</plain> ConnectorView(\nConnectorModel connectorModel,\nNodeView source,\nNodeView target,\nColor bgColor\n)
      + <plain>**+**</plain> ConnectorView(\nConnectorModel connectorModel,\nNodeView source,\nNodeView target,\nColor bgColor,\nboolean paintsConnectorLine\n)
      + <plain>**=**</plain> paint(Graphics graphics): void
      + <plain>**+**</plain> getExportGeometry(): ExportGeometry
    }
    class "<plain>**+**</plain> ConnectorView.ExportGeometry" as Geometry {
      + <plain>**+**</plain> curve: CubicCurve2D
      + <plain>**+**</plain> arrows: List<Polygon>
      + <plain>**+**</plain> stroke: BasicStroke
    }
    Connector +-- Geometry : returns detached geometry
  }
  @enduml
  ```

  - The connector constructor overload parameterizes complete-line
    capture without changing the map's connector-visibility setting.
    The existing constructor delegates with its existing visibility
    calculation. Capture only connectors accepted by the current filter
    whose endpoints have retained visible content. Native painting
    supplies the cubic curve, arrow polygons, and stroke; export geometry
    is detached from mutable renderer geometry. No alternate
    connector-painting implementation.

  ```plantuml
  @startuml
  participant ExportDialog
  participant ExportDynamicSvg
  participant MapViewExportCapture
  participant MapView
  participant DynamicSvgFonts
  participant DynamicSvgDocument
  participant "HTML viewer" as Viewer
  ExportDialog -> ExportDynamicSvg : export(branches, destination)
  ExportDynamicSvg -> MapViewExportCapture : capture(callback)
  MapViewExportCapture -> MapView : withPreservedExportState(operation)
  MapView -> MapView : save private state
  MapView -> MapViewExportCapture : operation
  MapViewExportCapture -> MapViewExportCapture : record scope and folds; unfold scoped views
  MapViewExportCapture -> MapView : prepareForExport(): zoom 1 and native layout
  MapViewExportCapture -> ExportDynamicSvg : capture(context)
  ExportDynamicSvg --> MapViewExportCapture : DynamicSvgDocument
  MapViewExportCapture -> MapViewExportCapture : invalidate context; restore folds in finally
  MapViewExportCapture --> MapView : captured document or error
  MapView -> MapView : restore private state and layout in finally
  MapView --> MapViewExportCapture : captured document or error
  MapViewExportCapture --> ExportDynamicSvg : captured document or error
  alt capture and restoration succeed
    ExportDynamicSvg -> DynamicSvgFonts : embed(captured text, existing setting)
    DynamicSvgFonts --> ExportDynamicSvg : font faces and warnings
    ExportDynamicSvg -> DynamicSvgDocument : write(destination, embedding)
    DynamicSvgDocument -> DynamicSvgDocument : render HTML\nwrite temporary sibling\natomic replace
    DynamicSvgDocument --> ExportDynamicSvg : success or error
    opt successful write with incomplete embedding
      ExportDynamicSvg -> ExportDynamicSvg : show standard Freeplane warning
    end
  else capture or restoration fails
    ExportDynamicSvg -> ExportDynamicSvg : show error; leave destination untouched
  end
  Viewer -> Viewer : load inline fonts\nhonor initial folds\nlayout and fit
  @enduml
  ```

  - Accept the existing map-export input for the current map; the
    actual exported root is `MapView.getRoot()`, not the model root
    passed by the export action. Check the current map through the
    existing export controller. Do not add branch-export registration
    or read the saved `.mm` file.
  - Use the current view root's transformed plain text as the HTML
    title, or `Mind map` when empty; do not derive the title from an
    excluded model root.
  - No destination is opened until capture and restoration succeed.
    Render a UTF-8 HTML document, write a temporary sibling file, and
    replace the destination using an atomic move. Delete the temporary
    file on failure. If atomic replacement is unavailable, report an
    export error rather than using a non-atomic replacement. Restore
    the waiting cursor on every outcome. Font warnings are shown only
    after a successful write; write errors remain export errors.
  - Resolve fonts through the SVG plugin's existing FOP font-detection
    policy and inspect files with FontBox. Match physical faces by
    PostScript identity and preserve intrinsic style and weight. Use
    family names that match the captured SVG text; matching a font file
    alone does not establish that the browser uses it. Embed complete
    standalone TrueType or OpenType font files as base64 data
    in the HTML, not Batik SVG font elements. Do not subset, rename, or
    convert fonts to WOFF/WOFF2. Preserve font metadata and enforce
    restricted-embedding and bitmap-only restrictions; using whole
    files respects no-subsetting restrictions. Font metadata is not a
    claim that every font license permits redistribution.
  - Unresolved logical/composite fonts, font collections, inaccessible
    or restricted files, unsupported font formats, and text requiring
    unresolved glyph substitution use recipient-machine fonts and
    contribute family/style/reason entries to the warning when embedding
    is enabled. Never guess that an unrelated physical file is the font
    AWT used. Disabling embedding performs no font discovery and
    produces no incomplete-embedding warning.
  - The viewer loads only inline font data before initial layout and
    fit. A browser font-load failure retains usable text and adds an
    incomplete-font message to the existing status area; it does not
    prevent viewer initialization or create another control.

  The JSON manifest is an internal contract between the Java writer
  and the bundled viewer. All fields below are required; nullable
  values are explicit. Arrays preserve native sibling order where
  relevant. Coordinates are finite numbers in zoom-1 artwork units.

  ```text
  Manifest:
    rootId: string
    nodes: Node[]
    links: Link[]
    summaries: Summary[]
    fontFaces: FontFace[]
  Node:
    id: string                  # unique
    parentId: string or null    # null only for exported root
    label: string
    searchText: string
    width: number
    height: number
    artwork: string
    topOrLeft: boolean
    firstGroup: boolean
    summary: boolean
    alwaysUnfolded: boolean
    collapsed: boolean
    contentVisible: boolean
    linkHref: string or null
  Link:
    sourceId: string
    targetId: string
    resolved: LinkStyle
    geometry: LinkGeometry
  LinkStyle:
    color: string               # #rrggbb
    alpha: integer              # 0-255
    shape: string               # CUBIC_CURVE
  LinkGeometry:
    zoom: number                # 1
    sourceAnchor: Point
    targetAnchor: Point
    sourceControl: Point
    targetControl: Point
    arrowPolygons: Arrow[]
    strokeWidth: number
    strokeDash: number[]        # empty means solid
    strokeCap: integer          # 0-2, BasicStroke meanings
    strokeJoin: integer         # 0-2, BasicStroke meanings
  Point:
    x: number
    y: number
  Arrow:
    end: string                 # source or target
    points: Point[]
  Summary:
    markerId: string
    startMarkerId: string
    parentId: string
    memberIds: string[]
    summaryChildIds: string[]
  FontFace:
    family: string
    style: string               # normal or italic
    weight: integer             # 1-1000
    format: string              # truetype or opentype
    data: string                # base64 complete font bytes
  ```

  Connector endpoints refer to retained content nodes. Anchors are
  artwork-relative; controls and polygons are endpoint-relative.
  Summary IDs refer only to retained tree entries; only groups with
  retained members and summary content produce a bracket. Both writer
  and viewer require this schema; do not add compatibility paths for
  companion manifests or guess replacements for missing fields.

  - A content-hidden ancestor or marker has empty `label`, `searchText`,
    and `artwork`, zero artwork dimensions, and null `linkHref`. Preserve
    IDs, sibling order, folding, side, and summary flags only when needed
    for retained descendants or summaries. Derive searchable text using
    `TextController.getPlainTransformedTextWithoutNodeNumber` and plain
    text from `DetailModel.getDetailText` for included content nodes.
    Do not serialize excluded content in diagnostics, titles, or unused
    fields. The exported root's parent is null even when its model has
    a parent.
  - Validate the retained tree, IDs, summary references, connector
    shapes, and geometry before writing. Support level-1 summaries as
    identified by Freeplane's summary-level rules, including separate
    groups under different parents. Reject higher summary levels,
    nested/overlapping marker groups, and malformed required markers
    with an explanation identifying the affected node. Filtering out a
    member is not an unsupported structure: retain only included
    members; if no bracket remains, render any remaining summary content
    through its retained tree path. Preserve summary-marker folding.
  - Reject in-scope connectors with `LINE`, `LINEAR_PATH`, or
    `EDGE_LIKE` shapes before capture. Geometry capture must produce a
    cubic curve and the expected arrow polygons. Connectors rejected by
    the current filter or crossing outside the exported scope are absent;
    unsupported content wholly outside scope does not prevent export.
    Connector labels are not added: the source viewer does not render
    them.
  - Bundle the adapted viewer at
    `freeplane_plugin_svg/src/main/resources/org/freeplane/plugin/svg/dynamic-svg.html`.
    Retain its existing controls and keyboard/pointer interactions;
    initialize `collapsed` from the captured state, honor
    `alwaysUnfolded`, and make expand/collapse-all act on retained
    branches, including content-hidden ancestor paths. Search and
    in-scope internal node links unfold every target ancestor, update
    layout, and center the target. Ordinary folding updates node,
    summary, and connector visibility together.
  - Retained ancestor paths participate in layout without exposing
    their excluded content. Search, status counts, node artwork, and
    node links use only `contentVisible` entries. Summary layout must
    traverse retained marker paths instead of assuming every parent
    has artwork. No additional toolbar control or production test API.
  - Preserve `NodeLinks.getLinkAsString` in `linkHref` and in anchor
    attributes without rebasing or a scheme allowlist. Intercept only
    fragment links to content inside this export. Other links, including
    targets outside scope, use ordinary browser navigation. Keep the
    source viewer's separate link hit area so node-link activation
    does not toggle folding or start panning.
  - Escape the HTML title and HTML-safe JSON, parse artwork as SVG,
    remove active artwork content and event attributes, and give each
    artwork's SVG IDs a unique prefix while rewriting local references.
    Keep images inline. Permit only the bundled viewer script to
    execute; map-supplied scripts remain inactive without filtering
    custom hyperlink schemes. Do not load scripts, styles, fonts, or display
    resources from the network or rely on browser storage or a server.
  - License the adapted exporter and viewer, including Java ports of
    source exporter code, under GPL-2.0-or-later. Preserve source
    attribution in `dynamic-svg-NOTICE.txt`; state GPL version 2 or
    later under the source-author agreement. Load `/license.txt` from
    Freeplane's core resources through `ResourceController`'s class
    loader; its source is
    `freeplane/src/editor/resources/license.txt`. Do not add a separate
    GPL license file. Record source URL and inspected commit identities,
    identify modifications, and include the notice and existing license
    text in the generated HTML without adding visible controls. Do not
    label the map content or fonts with that license.
    Do not copy the Python launcher, Groovy exporter, diagnostic
    `window.probe`, or companion registration path into production.
  - Add English translation entries in
    `freeplane/src/viewer/resources/translations/Resources_en.properties`:
    `export_dynamic_svg_text`, `dynamic_svg_unsupported_summary`,
    `dynamic_svg_unsupported_connector`,
    `dynamic_svg_font_embedding_incomplete`, and
    `dynamic_svg_export_failed`. Messages identify the affected node,
    connector shape, font family/style/reason, or write error as
    applicable. Use existing translation fallback for other languages;
    do not add a second font setting. Apply the project's translation
    formatting requirements during implementation.
  - Add Selenium Java 4.38.0 as a test-only dependency of the SVG plugin.
    Use one parameterized browser suite for Chrome, Firefox, Safari,
    and Edge; these browser names select drivers, not separate behavior
    implementations. Exercise locally opened generated HTML with
    standard browser settings. Browser/driver installation and Safari
    remote-automation permission are prerequisites; never enable
    permissions, change security settings, or bypass browser policies
    automatically. Missing prerequisites block verification rather than
    count as passed tests.
  - Include the core viewer/editor/external resources on the SVG
    plugin's test runtime classpath using the existing core test
    convention. Build test fixtures with native content components and
    the existing Swing/controller fixture pattern; do not substitute
    fixed SVG for renderer-capture acceptance tests. Use the existing
    Gentium resource for deterministic font-file tests, without
    installing a system font. Add state-preservation regression tests
    before changing renderer access or the graphics factory.
  - **Living project documents:** None affected
- **Test specification:**
  - **Automated tests:**
    - **+** `org.freeplane.view.swing.map.MapViewExportTest`
      - **+** `capturesCurrentViewRootAndFilterScope`: capture through
        `MapViewExportCapture` with a narrowed root and active filter
        retains matching folded descendants and needed
        ancestor paths, without including unrelated subtrees.
      - **+** `recordsFoldingBeforeTemporaryUnfolding`: existing view
        folds and not-yet-materialized descendant folds are recorded
        before capture; all captured artwork uses zoom 1.
      - **+** `restoresViewStateAfterCapture`: capture leaves folds,
        zoom, printing state, automatic folding histories,
        selection-driven folding, selection/focus, viewport, pending
        scrolling, root/history, and filter unchanged, including after
        queued Swing work is processed.
      - **+** `restoresViewStateWhenPaintingFails`: a failing native
        painter receives the same restoration guarantees.
      - **+** `preservesMapContentAndSaveState`: saved and unsaved maps
        keep content, model folds, undo history, and the exact value
        from `getNumberOfChangesSinceLastSave`; capture does not mark an
        unsaved map as saved.
      - **+** `rejectsCaptureOutsideEventDispatchThread`:
        `MapViewExportCapture.capture` rejects non-EDT capture before
        changing view state.
    - **+** `org.freeplane.view.swing.map.link.ConnectorViewExportTest`
      - **+** `capturesCompleteCubicGeometryWithoutChangingVisibility`:
        selection-only and hidden native connector settings remain
        unchanged while capture obtains complete curves and arrows.
      - **+** `returnsDetachedConnectorGeometry`: translating returned
        curves or polygons cannot mutate native geometry or connector
        model inclinations; include self-connectors and both arrow ends.
    - **+** `org.freeplane.plugin.svg.ExportVectorGraphicTest`
      - **+** `retainsExistingVectorTextAndShapeBehavior`: SVG/PDF
        generator-context options, gradients, normal text, forced text
        shapes, attributed text, and child graphics retain their
        existing behavior after the factory change.
      - **+** `observesTextRunsAcrossChildGraphics`: captured fonts/text
        include attributed runs and child graphics, but not glyphs
        painted as shapes.
    - **+** `org.freeplane.plugin.svg.ExportDynamicSvgTest`
      - **+** `registersHtmlExportWithoutReplacingExistingExports`:
        installed SVG-plugin extensions expose the new label and HTML
        extension alongside the existing SVG, PDF, and HTML options.
      - **+** `exportsUnsavedContentAndCurrentFolding`: invoke the
        registered engine with a native renderer fixture; HTML contains
        unsaved text/artwork, current view scope, and initial folds.
      - **+** `exportsVisibleDetailsFromHeadlessView`: the headless
        native fixture lays out included node details; engine-generated
        HTML contains their artwork and searchable text.
      - **+** `doesNotSerializeFilteredOutContent`: unique excluded text,
        details, hyperlinks, and unrelated subtrees are absent even
        when an excluded ancestor is needed for a matching descendant.
      - **+** `reportsUnsupportedInScopeStructures`: parameterized
        nested/malformed summary structures and non-cubic connector
        shapes produce identifying errors, no silent omission, and no
        replacement of an existing destination.
      - **+** `retainsFilteredSummaryContentAndInScopeConnectors`:
        partial groups retain included content and valid references;
        node, connector, and combined filter modes exclude rejected
        content. Excluded connectors, endpoints, and unsupported content
        do not appear or cause rejection.
      - **+** `leavesDestinationUnchangedWhenCaptureFails`: a native
        painting failure restores application state and preserves an
        existing destination byte-for-byte; no temporary output remains.
      - **+** `reportsWriteFailureWithoutReportingSuccess`: failed
        temporary writes or atomic replacement preserve an existing
        destination, clean up temporary files, and restore the cursor.
      - **+** `warnsAboutIncompleteEmbeddingAfterSuccessfulWrite`:
        requested embedding continues with available fonts, writes the
        file, and identifies incomplete embedding in a standard warning;
        disabling embedding produces no such warning.
    - **+** `org.freeplane.plugin.svg.DynamicSvgFontsTest`
      - **+** `embedsMatchingStandaloneFonts`: licensed Gentium bytes
        produce reusable font faces with intrinsic style/weight and
        retained metadata; repeated usage does not duplicate font data.
      - **+** `reportsFontsThatCannotBeEmbedded`: parameterized missing,
        restricted, bitmap-only, unsupported/collection, unresolved
        logical/composite, and glyph-substitution cases retain usable
        text and identify the font and reason.
      - **+** `embedsWholeFontsWhenSubsettingIsForbidden`: complete
        allowed font files are retained without creating subsets.
      - **+** `doesNotDiscoverFontsWhenEmbeddingIsDisabled`: no font
        discovery, embedded bytes, or incomplete-embedding warning.
    - **+** `org.freeplane.plugin.svg.DynamicSvgDocumentTest`
      - **+** `writesOneSelfContainedUtf8HtmlFile`: the generated file
        contains artwork, manifest, viewer, inline fonts, retained
        source attribution, a GPL-2.0-or-later notice for the adapted
        exporter/viewer, and the complete text from Freeplane's existing
        license resource, with no companion resources or copied documents.
      - **+** `escapesMapDataWithoutChangingLinks`: title, Unicode,
        script-closing text, SVG IDs, and custom/relative link strings
        remain data; links retain their exact serialized values.
    - **+** `org.freeplane.plugin.svg.DynamicSvgBrowserTest`
      - **+** `opensLocallyWithInitialFolding`: actual Chrome, Firefox,
        Safari, and Edge open engine-generated `file:` HTML without
        page errors or network resource requests; initial node,
        connector, and summary visibility matches the captured folds.
      - **+** `foldControlsUpdateTheRenderedMap`: click and keyboard
        node controls plus expand/collapse-all update rendered nodes,
        summaries, connectors, and arrowheads, including summary folds
        and content-hidden ancestor paths.
      - **+** `searchFindsTextAndDetailsInsideFoldedBranches`: typing
        and next-match activation reveal, highlight, and center matches;
        excluded content never becomes a match.
      - **+** `internalLinksRevealAndCenterTargets`: activate an actual
        rendered internal anchor to reveal its target through folded
        ancestors without changing excluded scope or starting panning.
      - **+** `preservesBrowserNavigationForOtherLinks`: rendered custom
        schemes and out-of-scope links retain hrefs and unprevented
        navigation; the test intercepts navigation before any external
        application can be launched.
      - **+** `relativeLinksWorkAfterMovingTheDirectoryTree`: move the
        generated HTML and a benign linked HTML fixture together;
        clicking the unchanged relative anchor opens that file.
      - **+** `panZoomAndFitUpdateTheRenderedViewport`: actual pointer,
        wheel, zoom-button, and fit-button events change the viewport
        transform and fit the rendered content.
      - **+** `loadsEmbeddedFontsAndKeepsFallbackTextUsable`: valid
        embedded font data reaches loaded browser font faces used by
        the captured SVG family names; disabled, incomplete, and
        browser-rejected embedding retain visible text
        and working controls, with the appropriate warning behavior.
      - **+** `keepsArtworkReferencesLocalAndMapScriptsInactive`:
        repeated native gradients/clip IDs remain distinct, inline
        images render, map data cannot execute script, and filtered
        ancestor content is absent from rendered text and links.
- **Implementation notes:**
  - **Tradeoffs:**
    - The capture context keeps live native views and model nodes so
      artwork uses the current renderer rather than a detached view.
      The collections are unmodifiable; contained `NodeView` and
      `NodeModel` references remain live. `paintNode` enforces callback
      lifetime and EDT checks; ordinary reads of retained references
      are restricted by the callback-only contract, not mechanically
      prevented.
