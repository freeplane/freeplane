<map version="freeplane 1.11.5">
<!--To view this file, download free mind mapping software Freeplane from https://www.freeplane.org -->
<node TEXT="Latest Freeplane features" STYLE_REF="Introduction" FOLDED="false" ID="ID_1286342769" BACKGROUND_COLOR="#99ccff" MIN_WIDTH="0 cm" VGAP_QUANTITY="2 pt">
<edge DASH="SOLID"/>
<hook NAME="MapStyle" background="#ffcccc">
    <properties show_icon_for_attributes="false" show_notes_in_map="false" show_note_icons="true" fit_to_viewport="false;"/>

<map_styles>
<stylenode LOCALIZED_TEXT="styles.root_node" STYLE="oval" UNIFORM_SHAPE="true" VGAP_QUANTITY="24 pt">
<font SIZE="24"/>
<stylenode LOCALIZED_TEXT="styles.predefined" POSITION="bottom_or_right" STYLE="bubble">
<stylenode LOCALIZED_TEXT="default" ID="ID_271890427" ICON_SIZE="12 pt" FORMAT_AS_HYPERLINK="true" COLOR="#000000" STYLE="fork">
<arrowlink SHAPE="CUBIC_CURVE" COLOR="#000000" WIDTH="2" TRANSPARENCY="200" DASH="" FONT_SIZE="9" FONT_FAMILY="SansSerif" DESTINATION="ID_271890427" STARTARROW="NONE" ENDARROW="DEFAULT"/>
<font NAME="SansSerif" SIZE="10" BOLD="false" ITALIC="false"/>
<richcontent TYPE="DETAILS" CONTENT-TYPE="plain/auto"/>
<richcontent TYPE="NOTE" CONTENT-TYPE="plain/auto"/>
</stylenode>
<stylenode LOCALIZED_TEXT="defaultstyle.details"/>
<stylenode LOCALIZED_TEXT="defaultstyle.attributes">
<font SIZE="9"/>
</stylenode>
<stylenode LOCALIZED_TEXT="defaultstyle.note" COLOR="#000000" BACKGROUND_COLOR="#ffffff" TEXT_ALIGN="LEFT"/>
<stylenode LOCALIZED_TEXT="defaultstyle.floating">
<edge STYLE="hide_edge"/>
<cloud COLOR="#f0f0f0" SHAPE="ROUND_RECT"/>
</stylenode>
<stylenode LOCALIZED_TEXT="defaultstyle.selection" BACKGROUND_COLOR="#afd3f7" BORDER_COLOR_LIKE_EDGE="false" BORDER_COLOR="#afd3f7"/>
</stylenode>
<stylenode LOCALIZED_TEXT="styles.user-defined" POSITION="bottom_or_right" STYLE="bubble">
<stylenode LOCALIZED_TEXT="styles.topic" COLOR="#18898b" STYLE="fork">
<font NAME="Liberation Sans" SIZE="10" BOLD="true"/>
</stylenode>
<stylenode LOCALIZED_TEXT="styles.subtopic" COLOR="#cc3300" STYLE="fork">
<font NAME="Liberation Sans" SIZE="10" BOLD="true"/>
</stylenode>
<stylenode LOCALIZED_TEXT="styles.subsubtopic" COLOR="#669900">
<font NAME="Liberation Sans" SIZE="10" BOLD="true"/>
</stylenode>
<stylenode LOCALIZED_TEXT="styles.important" ID="ID_67550811">
<icon BUILTIN="yes"/>
<arrowlink COLOR="#003399" TRANSPARENCY="255" DESTINATION="ID_67550811"/>
</stylenode>
<stylenode LOCALIZED_TEXT="styles.flower" COLOR="#ffffff" BACKGROUND_COLOR="#255aba" STYLE="oval" TEXT_ALIGN="CENTER" BORDER_WIDTH_LIKE_EDGE="false" BORDER_WIDTH="22 pt" BORDER_COLOR_LIKE_EDGE="false" BORDER_COLOR="#f9d71c" BORDER_DASH_LIKE_EDGE="false" BORDER_DASH="CLOSE_DOTS" MAX_WIDTH="6 cm" MIN_WIDTH="3 cm"/>
</stylenode>
<stylenode LOCALIZED_TEXT="styles.AutomaticLayout" POSITION="bottom_or_right" STYLE="bubble">
<stylenode LOCALIZED_TEXT="AutomaticLayout.level.root" COLOR="#000000" STYLE="oval" SHAPE_HORIZONTAL_MARGIN="10 pt" SHAPE_VERTICAL_MARGIN="10 pt">
<font SIZE="18"/>
</stylenode>
<stylenode LOCALIZED_TEXT="AutomaticLayout.level,1" COLOR="#0033ff">
<font SIZE="16"/>
</stylenode>
<stylenode LOCALIZED_TEXT="AutomaticLayout.level,2" COLOR="#00b439">
<font SIZE="14"/>
</stylenode>
<stylenode LOCALIZED_TEXT="AutomaticLayout.level,3" COLOR="#990000">
<font SIZE="12"/>
</stylenode>
<stylenode LOCALIZED_TEXT="AutomaticLayout.level,4" COLOR="#111111">
<font SIZE="10"/>
</stylenode>
<stylenode LOCALIZED_TEXT="AutomaticLayout.level,5"/>
<stylenode LOCALIZED_TEXT="AutomaticLayout.level,6"/>
<stylenode LOCALIZED_TEXT="AutomaticLayout.level,7"/>
<stylenode LOCALIZED_TEXT="AutomaticLayout.level,8"/>
<stylenode LOCALIZED_TEXT="AutomaticLayout.level,9"/>
<stylenode LOCALIZED_TEXT="AutomaticLayout.level,10"/>
<stylenode LOCALIZED_TEXT="AutomaticLayout.level,11"/>
</stylenode>
</stylenode>
</map_styles>
</hook>
<node TEXT="Version 1.11.11" POSITION="bottom_or_right" ID="ID_1996460640">
<node TEXT="Editor festures" ID="ID_960043093">
<node TEXT="Record and analyze violations found by ArchUnit tests" ID="ID_237251897" LINK="https://github.com/freeplane/freeplane-archunit-extension"/>
<node TEXT="Save user defined attributes and details on code explorer nodes" ID="ID_472050454"/>
<node TEXT="Use Operating System Regional Settings by default" ID="ID_109457892"/>
<node TEXT="User option &quot;Use Operating System Regional Settings&quot; (Preferences…-&gt;Environment-&gt;Language)" ID="ID_1290735249"/>
</node>
</node>
<node TEXT="Version 1.11.9" STYLE_REF="Actions" POSITION="bottom_or_right" ID="ID_289590210">
<node TEXT="Editor features" STYLE_REF="Purpose" ID="ID_1031432368">
<node TEXT="Navigate-&gt;Auto-expand selected nodes" ID="ID_1906031168"/>
<node TEXT="JVM Code Explorer mode (based on ArchUnit)" ID="ID_604199270" LINK="https://www.freeplane.org/codeexplorer-video"/>
</node>
</node>
<node TEXT="Version 1.11.8" STYLE_REF="Actions" FOLDED="true" POSITION="bottom_or_right" ID="ID_428268446">
<node TEXT="Editor features" STYLE_REF="Purpose" ID="ID_94012734">
<node TEXT="Flower-like nodes (new node style &quot;Flower&quot; combines non-solid node borders with big border widths)" LOCALIZED_STYLE_REF="default" ID="ID_272657390">
<cloud COLOR="#c6ffff" SHAPE="ROUND_RECT"/>
<hook NAME="AlwaysUnfoldedNode"/>
<node TEXT="node" LOCALIZED_STYLE_REF="styles.flower" ID="ID_676341167" STYLE="fork" BORDER_DASH="SOLID" LOCALIZED_LOCALIZED_STYLE_REF="styles.flower">
<hook NAME="AlwaysUnfoldedNode"/>
<node TEXT="node" LOCALIZED_STYLE_REF="styles.flower" ID="ID_497451484" STYLE="bubble" BORDER_DASH="SOLID">
<hook NAME="AlwaysUnfoldedNode"/>
<node TEXT="node" LOCALIZED_STYLE_REF="styles.flower" ID="ID_1859192615" STYLE="oval" BORDER_DASH="SOLID">
<hook NAME="AlwaysUnfoldedNode"/>
<node TEXT="node" LOCALIZED_STYLE_REF="styles.flower" ID="ID_638900740" STYLE="rectangle" BORDER_DASH="SOLID">
<hook NAME="AlwaysUnfoldedNode"/>
<node TEXT="node" LOCALIZED_STYLE_REF="styles.flower" ID="ID_117688138" STYLE="wide_hexagon" BORDER_DASH="SOLID">
<hook NAME="AlwaysUnfoldedNode"/>
<node TEXT="node" LOCALIZED_STYLE_REF="styles.flower" ID="ID_1027041431" STYLE="narrow_hexagon" BORDER_DASH="SOLID">
<hook NAME="AlwaysUnfoldedNode"/>
</node>
</node>
</node>
</node>
</node>
</node>
<node TEXT="node" LOCALIZED_STYLE_REF="styles.flower" ID="ID_986183555" STYLE="bubble" BORDER_DASH="CLOSE_DOTS">
<hook NAME="AlwaysUnfoldedNode"/>
<node TEXT="node" LOCALIZED_STYLE_REF="styles.flower" ID="ID_1810875334" STYLE="oval" BORDER_DASH="CLOSE_DOTS">
<hook NAME="AlwaysUnfoldedNode"/>
<node TEXT="node" LOCALIZED_STYLE_REF="styles.flower" ID="ID_705790814" STYLE="rectangle" BORDER_DASH="CLOSE_DOTS">
<hook NAME="AlwaysUnfoldedNode"/>
<node TEXT="node" LOCALIZED_STYLE_REF="styles.flower" ID="ID_1633966335" STYLE="wide_hexagon" BORDER_DASH="CLOSE_DOTS">
<hook NAME="AlwaysUnfoldedNode"/>
<node TEXT="node" LOCALIZED_STYLE_REF="styles.flower" ID="ID_1893147490" STYLE="narrow_hexagon" BORDER_DASH="CLOSE_DOTS">
<hook NAME="AlwaysUnfoldedNode"/>
<node TEXT="node" LOCALIZED_STYLE_REF="styles.flower" POSITION="bottom_or_right" ID="ID_523846139" BORDER_DASH="CLOSE_DOTS">
<hook NAME="AlwaysUnfoldedNode"/>
</node>
</node>
</node>
</node>
</node>
</node>
<node TEXT="node" LOCALIZED_STYLE_REF="styles.flower" ID="ID_253639085" STYLE="oval" BORDER_DASH="DASHES">
<hook NAME="AlwaysUnfoldedNode"/>
<node TEXT="node" LOCALIZED_STYLE_REF="styles.flower" ID="ID_826442147" STYLE="rectangle" BORDER_DASH="DASHES">
<hook NAME="AlwaysUnfoldedNode"/>
<node TEXT="node" LOCALIZED_STYLE_REF="styles.flower" ID="ID_360112751" STYLE="wide_hexagon" BORDER_DASH="DASHES">
<hook NAME="AlwaysUnfoldedNode"/>
<node TEXT="node" LOCALIZED_STYLE_REF="styles.flower" ID="ID_1959258091" STYLE="narrow_hexagon" BORDER_DASH="DASHES">
<hook NAME="AlwaysUnfoldedNode"/>
<node TEXT="node" LOCALIZED_STYLE_REF="styles.flower" POSITION="bottom_or_right" ID="ID_801930841" BORDER_DASH="DASHES">
<hook NAME="AlwaysUnfoldedNode"/>
<node TEXT="node" LOCALIZED_STYLE_REF="styles.flower" ID="ID_1003696394" STYLE="bubble" BORDER_DASH="DASHES">
<hook NAME="AlwaysUnfoldedNode"/>
</node>
</node>
</node>
</node>
</node>
</node>
<node TEXT="node" LOCALIZED_STYLE_REF="styles.flower" ID="ID_1511539604" STYLE="rectangle" BORDER_DASH="DISTANT_DOTS">
<hook NAME="AlwaysUnfoldedNode"/>
<node TEXT="node" LOCALIZED_STYLE_REF="styles.flower" ID="ID_1437102862" STYLE="wide_hexagon" BORDER_DASH="DISTANT_DOTS">
<hook NAME="AlwaysUnfoldedNode"/>
<node TEXT="node" LOCALIZED_STYLE_REF="styles.flower" ID="ID_811356527" STYLE="narrow_hexagon" BORDER_DASH="DISTANT_DOTS">
<hook NAME="AlwaysUnfoldedNode"/>
<node TEXT="node" LOCALIZED_STYLE_REF="styles.flower" POSITION="bottom_or_right" ID="ID_586139609" BORDER_DASH="DISTANT_DOTS">
<hook NAME="AlwaysUnfoldedNode"/>
<node TEXT="node" LOCALIZED_STYLE_REF="styles.flower" ID="ID_1194963926" STYLE="bubble" BORDER_DASH="DISTANT_DOTS">
<hook NAME="AlwaysUnfoldedNode"/>
<node TEXT="node" LOCALIZED_STYLE_REF="styles.flower" ID="ID_965594088" STYLE="oval" BORDER_DASH="DISTANT_DOTS">
<hook NAME="AlwaysUnfoldedNode"/>
</node>
</node>
</node>
</node>
</node>
</node>
<node TEXT="node" LOCALIZED_STYLE_REF="styles.flower" ID="ID_1147164298" STYLE="narrow_hexagon" BORDER_DASH="DOTS_AND_DASHES">
<hook NAME="AlwaysUnfoldedNode"/>
<node TEXT="node" LOCALIZED_STYLE_REF="styles.flower" POSITION="bottom_or_right" ID="ID_867273635" BORDER_DASH="DOTS_AND_DASHES">
<hook NAME="AlwaysUnfoldedNode"/>
<node TEXT="node" LOCALIZED_STYLE_REF="styles.flower" ID="ID_657461703" STYLE="bubble" BORDER_DASH="DOTS_AND_DASHES">
<hook NAME="AlwaysUnfoldedNode"/>
<node TEXT="node" LOCALIZED_STYLE_REF="styles.flower" ID="ID_1775104521" STYLE="oval" BORDER_DASH="DOTS_AND_DASHES">
<hook NAME="AlwaysUnfoldedNode"/>
<node TEXT="node" LOCALIZED_STYLE_REF="styles.flower" ID="ID_711675033" STYLE="rectangle" BORDER_DASH="DOTS_AND_DASHES">
<hook NAME="AlwaysUnfoldedNode"/>
<node TEXT="node" LOCALIZED_STYLE_REF="styles.flower" ID="ID_1969797533" STYLE="wide_hexagon" BORDER_DASH="DOTS_AND_DASHES">
<hook NAME="AlwaysUnfoldedNode"/>
</node>
</node>
</node>
</node>
</node>
</node>
</node>
</node>
</node>
<node TEXT="Version 1.11.5" STYLE_REF="Actions" FOLDED="true" POSITION="bottom_or_right" ID="ID_1707937288">
<node TEXT="Editor features" STYLE_REF="Purpose" ID="ID_1363201449">
<node ID="ID_1198724288"><richcontent TYPE="NODE">

<html>
  <head>
    
  </head>
  <body>
    <p>
      <span style="background-color: #ffff00;">Highlight any&#xa0;words</span>&#xa0;in <span style="background-color: #ff6554;">text</span>, <span style="background-color: #00ffff;">details</span>&#xa0;and <span style="background-color: #5bff5b;">notes</span>
    </p>
  </body>
</html>
</richcontent>
</node>
<node TEXT="Indicate selected menu items and options in command search dialog" ID="ID_1835008982"/>
<node TEXT="Improve handling of unknown html tags in pasted html content" ID="ID_1342587624"/>
<node TEXT="New automatic &quot;stacked&quot; layouts (at the bottom of the layout selector)" ID="ID_23956346"/>
</node>
</node>
<node TEXT="Version 1.11.x" STYLE_REF="Actions" FOLDED="true" POSITION="bottom_or_right" ID="ID_414352132">
<node TEXT="Editor features" STYLE_REF="Purpose" ID="ID_1802701213">
<node TEXT="Top-to-bottom, left-to-right and combined layouts" ID="ID_120328641">
<hook URI="latestFreeplaneFeatures_files/layouts.png" SIZE="0.5" NAME="ExternalObject"/>
</node>
<node TEXT="Base distance from parent node to child node configurable for parent nodes and styles" ID="ID_455500811"/>
<node TEXT="Node movement restricted to single directions" ID="ID_1260662085"/>
<node TEXT="Simultaneous movement of all selected nodes" ID="ID_1265802507"/>
<node TEXT="Fold/unfold only visible nodes and their ancestors" ID="ID_1883753058"/>
<node TEXT="New flat look and feels for MacOS" ID="ID_467226780"/>
<node TEXT="TAB  inserts new nodes when editing node content" ID="ID_1345875067"/>
</node>
<node TEXT="Updated software components" STYLE_REF="Purpose" ID="ID_127255283"/>
</node>
<node TEXT="Version 1.10.5" STYLE_REF="Actions" FOLDED="true" POSITION="bottom_or_right" ID="ID_1651354600">
<node TEXT="Editor features" STYLE_REF="Purpose" ID="ID_343021245">
<node TEXT="New UI icons (created by Rexel and Predrag)" ID="ID_1135148263"/>
<node TEXT="Command line option -R to run groovy scripts on start-up" ID="ID_1329668559"/>
<node TEXT="Support command line options -R and -S in non interactive mode" ID="ID_1972168280"/>
<node TEXT="Compact map layout" ID="ID_1482025158"/>
<node TEXT="Jump in selected subtrees" ID="ID_519305347"/>
<node TEXT="Vertical aligning child nodes at the top, at the bottom or at the center of the parent node" ID="ID_161848692"/>
<node TEXT="Consider hidden free node positions in map layout" ID="ID_430638175"/>
<node TEXT="Creating new user style from selection assigns it to all selected nodes and clears their formatting" ID="ID_1478638916"/>
<node TEXT="Make dragging area width configurable and increase its default size" ID="ID_1449443293"/>
<node TEXT="Change key based navigation for summary nodes" ID="ID_1444166533"/>
<node TEXT="Word wrap in editors for formulas, scripts, markdown and latex" ID="ID_171708339"/>
</node>
<node TEXT="Updated software components" STYLE_REF="Purpose" FOLDED="true" ID="ID_1651141080" VGAP_QUANTITY="2 pt">
<node TEXT="Java 17 support" ID="ID_602837388"/>
<node TEXT="Updated Groovy to version 4" ID="ID_1354215770"/>
</node>
</node>
<node TEXT="Version 1.9.x" STYLE_REF="Actions" FOLDED="true" POSITION="bottom_or_right" ID="ID_1638165251">
<node TEXT="Editor features" STYLE_REF="Purpose" FOLDED="true" ID="ID_1741600255">
<node TEXT="Connector styles configurable at user style nodes in style editor" ID="ID_309599694"/>
<node TEXT="Selected node colors configurable at appropriate styles in use style editor" ID="ID_521611641"/>
<node TEXT="Formulas in node core, node details and notes" ID="ID_1417671379"/>
<node TEXT="Markdown in node core, node details and notes" ID="ID_1004436581"/>
<node TEXT="Latex in node core, node details and notes" ID="ID_806550986"/>
<node TEXT="New Freeplane Application icons and splash screen" ID="ID_1937358243"/>
<node TEXT="Configurable icons for links depending on link URL and file extension" ID="ID_962835223"/>
<node TEXT="Following styles defined in external maps (synchronized when the map is loaded)" ID="ID_1108233463"/>
<node TEXT="New action for copying styles from other maps but keeping own user styles if they are different" ID="ID_393838871"/>
<node TEXT="Fix default edge and node widths" ID="ID_212042030"/>
<node TEXT="Option to open node links only if Control key is pressed" ID="ID_420124712"/>
<node TEXT="Add background colors to generated HTML" ID="ID_1992594483"/>
<node TEXT="Option to disable all colors in generated HTML" ID="ID_216094501"/>
<node TEXT="Speed up file dialogs" ID="ID_1169212059"/>
<node TEXT="VAqua Look and Feel for MacOS" ID="ID_1622519681"/>
<node TEXT="Scripting API extended to support the new features" ID="ID_1116916304"/>
<node TEXT="Copy images pasted into rich text editors" ID="ID_454781677"/>
<node TEXT="Mind map template previews" ID="ID_704419783"/>
<node TEXT="Replace SplitToWords by SplitInRows" ID="ID_1145247548"/>
</node>
<node TEXT="Updated software components" STYLE_REF="Purpose" FOLDED="true" ID="ID_234329131">
<node TEXT="Java 15 support" ID="ID_1930135299"/>
<node TEXT="Updated Groovy to 3.0.8" ID="ID_702829975"/>
</node>
</node>
<node TEXT="Version 1.8.x" STYLE_REF="Actions" FOLDED="true" POSITION="bottom_or_right" ID="ID_25848260">
<node TEXT="Editor features" STYLE_REF="Purpose" ID="ID_109116165">
<node TEXT="Use emoticons from https://twemoji.twitter.com/ as icons" ID="ID_391516983" LINK="https://twemoji.twitter.com/"/>
<node TEXT="Optionally ignore accents and diacritics in filter conditions" ID="ID_684736245"/>
<node TEXT="Usability improvements suggested by University of Oulu UX research group" ID="ID_1022343423"/>
<node TEXT="Quick &quot;And filter&quot; and &quot;Or filter&quot;" ID="ID_1228038812"/>
<node TEXT="Command/Preferences/icons search dialog (Control/⌘ + F1)" ID="ID_1203365755" LINK="https://youtu.be/GNoCulrt-xY"/>
<node TEXT="Draw clouds around hidden or filtered our nodes" ID="ID_556585090"/>
</node>
<node TEXT="Updated software components" STYLE_REF="Purpose" FOLDED="true" ID="ID_1480328703">
<node TEXT="Java 13 support" ID="ID_1423743100"/>
</node>
<node TEXT="Formulas and scripts" STYLE_REF="Purpose" ID="ID_377149294"/>
</node>
<node TEXT="Version 1.7.x" STYLE_REF="Actions" FOLDED="true" POSITION="bottom_or_right" ID="ID_1757711154">
<node TEXT="Editor features" STYLE_REF="Purpose" FOLDED="true" ID="ID_1765567826">
<node TEXT="Nodes URLs with relative and absolute paths like #at(:~someNodeAlias)" ID="ID_1539755264"/>
<node TEXT="Node aliases" ID="ID_1352789056"/>
<node TEXT="Global nodes" ID="ID_24137642"/>
<node TEXT="Filtering nodes on aliases and on global accessibility" ID="ID_1864695826"/>
<node TEXT="Option to skip dialog for unnassigned F-keys" ID="ID_939273256"/>
<node TEXT="Denied (inverted) conditions in filter toolbar" ID="ID_1393999486"/>
<node TEXT="Export for selected branches only" ID="ID_817997036"/>
<node TEXT="Go to nodes by their &quot;reference paths&quot;" ID="ID_1785822754"/>
<node TEXT="Dark UI mode support (Look and feel and map template &quot;Darcula&quot;)" ID="ID_1513276613"/>
</node>
<node TEXT="Formulas and scripts" STYLE_REF="Purpose" FOLDED="true" ID="ID_635077048">
<node TEXT="Formula dependency tracing" ID="ID_181704820"/>
<node TEXT="API for referencing nodes from formulas and scripts" ID="ID_170531925"/>
<node TEXT="API for loading mind maps" ID="ID_1674596796"/>
<node TEXT="API for script execution" ID="ID_1341352019"/>
<node TEXT="Add new properties  to connector API" ID="ID_235525051"/>
<node TEXT="Persistent cache for compiled library scripts" ID="ID_45063614"/>
<node TEXT="Runtime cache for compiled formulas and scripts" ID="ID_1167426315"/>
</node>
<node TEXT="Updated software components" STYLE_REF="Purpose" FOLDED="true" ID="ID_1632208670">
<node TEXT="Java 7 support dropped, Java 8 is required" ID="ID_231313072"/>
<node TEXT="Java 11 support" ID="ID_1997699757"/>
<node TEXT="Update Groovy to version 2.5.3" ID="ID_1119389141"/>
<node TEXT="Update JLatexMath to versin 1.0.7" ID="ID_14411050"/>
<node TEXT="Update batik to version 2.10" ID="ID_1048875697"/>
</node>
<node TEXT="Freeplane API can be embedded in arbitrary JVM applications and scripts" ID="ID_343365936"/>
</node>
<node TEXT="Version 1.6.x" STYLE_REF="Actions" FOLDED="true" POSITION="bottom_or_right" ID="ID_132081568">
<node TEXT="connectors" STYLE_REF="Purpose" FOLDED="true" ID="ID_1672618426">
<node TEXT="Option to hide connectors" ID="ID_389731514"/>
<node TEXT="Option to show connectors only for selected nodes" ID="ID_353659522"/>
<node TEXT="Option to hide connectors when filtering hides other connector end" ID="ID_244698873"/>
<node TEXT="Options to hide icons and connectors" ID="ID_258015212"/>
</node>
<node TEXT="formatting" STYLE_REF="Purpose" FOLDED="true" ID="ID_1839085284">
<node TEXT="Configurable map icon sizes" ID="ID_775318770"/>
<node TEXT="Strike through formatting for nodes added" ID="ID_518969117"/>
<node TEXT="Image in node without black border (no border)" ID="ID_1952572862"/>
<node TEXT="Configurable clone marks" ID="ID_1297692234"/>
<node TEXT="Configurable standard cloud shape" ID="ID_102027493"/>
<node TEXT="Configurable node border widths" ID="ID_1709640038"/>
<node TEXT="Configurable node border colors" ID="ID_799827115"/>
<node TEXT="Configurable edge line type" ID="ID_775607802"/>
<node TEXT="Configurable node border line type" ID="ID_592874223"/>
<node TEXT="Configurable standard for connector arrows" ID="ID_1095586855"/>
<node TEXT="Configurable standard for connector line types" ID="ID_1171811720"/>
</node>
<node TEXT="user interface" STYLE_REF="Purpose" FOLDED="true" ID="ID_1705450882">
<node TEXT="Add icon and attribute conditions to style editor" ID="ID_557879353"/>
<node TEXT="Skip node numbers on node sort and in some other cases" ID="ID_1101307232"/>
<node TEXT="Show creation modification in the status line" ID="ID_1674482897"/>
<node TEXT="Outline view fits window width (option)" ID="ID_855766222"/>
<node TEXT="Save note when note editor looses focus" ID="ID_1387794471"/>
<node TEXT="Detail icon color matches detail color instead of node border color" ID="ID_146106752"/>
<node TEXT="Drag and drop of image file holding Ctrl+Shift creates a link without inserting the image into the map" ID="ID_223599963"/>
<node TEXT="&quot;Close all maps&quot; and &quot;close all other maps&quot; actions added to file menu" ID="ID_1478173293"/>
<node TEXT="SVG icons for map and UI" ID="ID_1287774899"/>
<node TEXT="Paint clone node markers only on selected nodes" ID="ID_1512259000"/>
<node TEXT="Action for showing next presentation item without folding other nodes" ID="ID_1257128438"/>
<node TEXT="Optional presentation automatics &quot;processesUpDownKeys&quot;, &quot;switchToFullScreen&quot;, &quot;switchToPresentationMode&quot;" ID="ID_439917968"/>
<node TEXT="Increased sensitive area for image scaling" ID="ID_1399423959"/>
<node TEXT="No special treatment for white space nodes when content is copied to text" ID="ID_1062579525"/>
<node TEXT="Option to paste images from files" ID="ID_452998276"/>
<node TEXT="Dragged and dropped files are copied into directory mindmap_files/ if CONTROL key is pressed" ID="ID_1928449518"/>
<node TEXT="Option note icon in notes tool-tip" ID="ID_147304126"/>
<node TEXT="Option to move aligned node smoothly" ID="ID_1209459718"/>
<node TEXT="Option to define spotlight background color" ID="ID_1714364653"/>
<node TEXT="Configurable UI icon sizes" ID="ID_1108785112"/>
<node TEXT="Aligning current node on the left side, on the right side or on the center, hot keys alt+W (west), alt+E (east), alt+C (center)" ID="ID_150208659"/>
<node TEXT="Editor dialog for automatic edge colors" ID="ID_1960800845"/>
</node>
<node TEXT="export" STYLE_REF="Purpose" FOLDED="true" ID="ID_1018225688">
<node TEXT="Options for handling texts and fonts in exported PDF and SVG" ID="ID_1250175151"/>
<node TEXT="Removed broken export to flash" ID="ID_168053298"/>
<node TEXT="Export for java web start" ID="ID_923366841"/>
</node>
<node TEXT="Presentations" ID="ID_1736223790" LINK="ID_1747402695"/>
</node>
<node TEXT="Detailed changelog" POSITION="top_or_left" ID="ID_958679023" LINK="https://www.freeplane.org/info/history/history_en.txt" HGAP_QUANTITY="-104.125 pt" VSHIFT_QUANTITY="37.125 pt"/>
</node>
</map>
