<map version="freeplane 1.11.1">
<!--To view this file, download free mind mapping software Freeplane from https://www.freeplane.org -->
<node TEXT="Latest Freeplane features" STYLE_REF="Introduction" FOLDED="false" ID="ID_1286342769" CREATED="1541847389542" MODIFIED="1668190156172" BACKGROUND_COLOR="#99ccff" TEXT_ALIGN="RIGHT" MIN_WIDTH="0 cm" CHILD_NODES_LAYOUT="TOPTOBOTTOM_RIGHT_CENTERED">
<edge DASH="SOLID"/>
<hook NAME="MapStyle" background="#ffcccc">
    <conditional_styles>
        <conditional_style ACTIVE="false" STYLE_REF="Revision" LAST="false">
            <conjunct_condition user_name="Example">
                <time_condition_modified_after DATE="1335514983501"/>
                <time_condition_modified_before DATE="1335515403501"/>
            </conjunct_condition>
        </conditional_style>
    </conditional_styles>
    <properties show_icon_for_attributes="false" show_notes_in_map="false" show_note_icons="true" fit_to_viewport="false;"/>

<map_styles>
<stylenode LOCALIZED_TEXT="styles.root_node" STYLE="oval" UNIFORM_SHAPE="true" VGAP_QUANTITY="24 pt">
<font SIZE="24"/>
<stylenode LOCALIZED_TEXT="styles.predefined" POSITION="bottom_or_right" STYLE="bubble">
<stylenode LOCALIZED_TEXT="default" ID="ID_1913414962" ICON_SIZE="12 pt" FORMAT_AS_HYPERLINK="true" COLOR="#000000" STYLE="as_parent" NUMBERED="false" FORMAT="STANDARD_FORMAT" TEXT_ALIGN="DEFAULT" CHILD_NODES_LAYOUT="AUTO" VGAP_QUANTITY="2 pt" BORDER_WIDTH_LIKE_EDGE="false" BORDER_WIDTH="1 px" BORDER_COLOR_LIKE_EDGE="true" BORDER_COLOR="#808080" BORDER_DASH_LIKE_EDGE="false" BORDER_DASH="SOLID" MAX_WIDTH="10 cm" MIN_WIDTH="0 cm">
<arrowlink SHAPE="CUBIC_CURVE" COLOR="#000000" WIDTH="2" TRANSPARENCY="200" DASH="" FONT_SIZE="9" FONT_FAMILY="SansSerif" DESTINATION="ID_1913414962" STARTARROW="NONE" ENDARROW="DEFAULT"/>
<font NAME="SansSerif" SIZE="8" BOLD="false" STRIKETHROUGH="false" ITALIC="false"/>
<edge COLOR="#808080" WIDTH="1"/>
<richcontent TYPE="NOTE" CONTENT-TYPE="plain/html"/>
<richcontent CONTENT-TYPE="plain/html" TYPE="DETAILS"/>
</stylenode>
<stylenode LOCALIZED_TEXT="defaultstyle.attributes">
<font SIZE="9"/>
</stylenode>
<stylenode LOCALIZED_TEXT="defaultstyle.floating">
<edge STYLE="hide_edge"/>
<cloud COLOR="#f0f0f0" SHAPE="ROUND_RECT"/>
</stylenode>
<stylenode LOCALIZED_TEXT="defaultstyle.details" BACKGROUND_COLOR="#ffcccc">
<font SIZE="8"/>
</stylenode>
<stylenode LOCALIZED_TEXT="defaultstyle.note" BACKGROUND_COLOR="#ffffff"/>
<stylenode LOCALIZED_TEXT="defaultstyle.selection" BACKGROUND_COLOR="#afd3f7" BORDER_COLOR_LIKE_EDGE="false" BORDER_COLOR="#002080"/>
</stylenode>
<stylenode LOCALIZED_TEXT="styles.AutomaticLayout" POSITION="bottom_or_right" STYLE="bubble">
<stylenode LOCALIZED_TEXT="AutomaticLayout.level.root" COLOR="#000000">
<font SIZE="12"/>
</stylenode>
<stylenode LOCALIZED_TEXT="AutomaticLayout.level,1" COLOR="#0033ff">
<font SIZE="10"/>
</stylenode>
<stylenode LOCALIZED_TEXT="AutomaticLayout.level,2" COLOR="#00b439">
<font SIZE="10"/>
</stylenode>
<stylenode LOCALIZED_TEXT="AutomaticLayout.level,3" COLOR="#990000">
<font SIZE="10"/>
</stylenode>
<stylenode LOCALIZED_TEXT="AutomaticLayout.level,4" COLOR="#111111">
<font SIZE="12"/>
</stylenode>
</stylenode>
<stylenode LOCALIZED_TEXT="styles.user-defined" POSITION="bottom_or_right" STYLE="bubble">
<stylenode TEXT="Introduction" BACKGROUND_COLOR="#00cc33" MIN_WIDTH="5 cm">
<font BOLD="true"/>
<edge STYLE="bezier" COLOR="#33ffcc"/>
</stylenode>
<stylenode TEXT="Beginner" BACKGROUND_COLOR="#33ffcc" STYLE="bubble" MIN_WIDTH="5 cm">
<font BOLD="true"/>
<edge STYLE="bezier" COLOR="#33ffcc"/>
</stylenode>
<stylenode TEXT="Advanced" COLOR="#000000" BACKGROUND_COLOR="#ffff00" STYLE="bubble" MIN_WIDTH="5 cm">
<font BOLD="true"/>
<edge STYLE="bezier" COLOR="#33ffcc"/>
</stylenode>
<stylenode TEXT="Professional" BACKGROUND_COLOR="#ff9900" STYLE="bubble" MIN_WIDTH="5 cm">
<font BOLD="true"/>
<edge STYLE="bezier" COLOR="#33ffcc"/>
</stylenode>
<stylenode TEXT="TitlesContent" STYLE="fork">
<edge STYLE="hide_edge"/>
</stylenode>
<stylenode TEXT="Example">
<icon BUILTIN="../AttributesView"/>
<edge STYLE="bezier" COLOR="#808080"/>
</stylenode>
<stylenode TEXT="Revision">
<icon BUILTIN="revision"/>
</stylenode>
<stylenode TEXT="Purpose" BACKGROUND_COLOR="#fff899" TEXT_ALIGN="CENTER" MAX_WIDTH="110 pt" MIN_WIDTH="110 pt">
<font BOLD="true"/>
<edge COLOR="#ff3333"/>
</stylenode>
<stylenode TEXT="Actions" BACKGROUND_COLOR="#99ff99" TEXT_ALIGN="CENTER" MAX_WIDTH="110 pt" MIN_WIDTH="110 pt">
<font BOLD="true"/>
<edge COLOR="#009900"/>
</stylenode>
<stylenode TEXT="Notes and explanations" BACKGROUND_COLOR="#afd3f7" TEXT_ALIGN="CENTER" MAX_WIDTH="110 pt" MIN_WIDTH="110 pt">
<font BOLD="true"/>
<edge COLOR="#0066ff"/>
</stylenode>
<stylenode TEXT="Tips and tricks" BACKGROUND_COLOR="#ffcc99" TEXT_ALIGN="CENTER" MAX_WIDTH="110 pt" MIN_WIDTH="110 pt">
<font BOLD="true"/>
<edge COLOR="#ff9900"/>
</stylenode>
<stylenode TEXT="Old documentation" BACKGROUND_COLOR="#eaeaea" TEXT_ALIGN="CENTER" MAX_WIDTH="110 pt" MIN_WIDTH="110 pt">
<font BOLD="true"/>
<edge COLOR="#666666"/>
</stylenode>
<stylenode TEXT="MyTemplate">
<icon BUILTIN="females"/>
<richcontent CONTENT-TYPE="xml/" TYPE="DETAILS">
<html>
  <head>

  </head>
  <body>
    <p>
      <b><font color="#3333ff" size="4">Template information</font></b>
    </p>
    <ul>
      <li>
        <i>Name</i>:
      </li>
      <li>
        Address:
      </li>
    </ul>
  </body>
</html></richcontent>
<attribute NAME="birth date" VALUE=""/>
<attribute NAME="e-mail" VALUE=""/>
</stylenode>
<stylenode TEXT="MainWidth" MIN_WIDTH="600 px"/>
<stylenode TEXT="SubWidth" MAX_WIDTH="550 px" MIN_WIDTH="550 px"/>
<stylenode TEXT="Method" STYLE="as_parent">
<edge STYLE="horizontal"/>
</stylenode>
</stylenode>
<stylenode LOCALIZED_TEXT="styles.topic" POSITION="bottom_or_right" COLOR="#18898b" STYLE="fork">
<font NAME="Liberation Sans" SIZE="12" BOLD="true"/>
</stylenode>
<stylenode LOCALIZED_TEXT="styles.subtopic" POSITION="bottom_or_right" COLOR="#cc3300" STYLE="fork">
<font NAME="Liberation Sans" SIZE="12" BOLD="true"/>
</stylenode>
<stylenode LOCALIZED_TEXT="styles.subsubtopic" POSITION="bottom_or_right" COLOR="#669900">
<font NAME="Liberation Sans" SIZE="12" BOLD="true"/>
</stylenode>
<stylenode LOCALIZED_TEXT="styles.important" POSITION="bottom_or_right">
<icon BUILTIN="yes"/>
</stylenode>
</stylenode>
</map_styles>
</hook>
<node TEXT="Version 1.11.x" STYLE_REF="Actions" POSITION="bottom_or_right" ID="ID_414352132" CREATED="1668190203324" MODIFIED="1668190235820">
<node TEXT="Editor features" STYLE_REF="Purpose" ID="ID_1802701213" CREATED="1541848060291" MODIFIED="1541848968607">
<node TEXT="Top-to-bottom, left-to-right and combined layouts" ID="ID_120328641" CREATED="1675608098149" MODIFIED="1675608116259">
<hook URI="latestFreeplaneFeatures_files/layouts.png" SIZE="0.6124021" NAME="ExternalObject"/>
</node>
<node TEXT="Base distance from parent node to child node configurable for parent nodes and styles" ID="ID_455500811" CREATED="1675606733862" MODIFIED="1675606733862"/>
<node TEXT="Node movement restricted to single directions" ID="ID_1260662085" CREATED="1675606733862" MODIFIED="1675606733862"/>
<node TEXT="Simultaneous movement of all selected nodes" ID="ID_1265802507" CREATED="1675606733868" MODIFIED="1675606733868"/>
<node TEXT="Fold/unfold only visible nodes and their ancestors" ID="ID_1883753058" CREATED="1675606733869" MODIFIED="1675606733869"/>
<node TEXT="New flat look and feels for MacOS" ID="ID_467226780" CREATED="1675606743640" MODIFIED="1675606781008"/>
<node TEXT="TAB  inserts new nodes when editing node content" ID="ID_1345875067" CREATED="1675606821161" MODIFIED="1675607669478"/>
</node>
<node TEXT="Updated software components" STYLE_REF="Purpose" ID="ID_127255283" CREATED="1541847932161" MODIFIED="1541848968607"/>
</node>
<node TEXT="Version 1.10.5" STYLE_REF="Actions" POSITION="bottom_or_right" ID="ID_1651354600" CREATED="1668190189347" MODIFIED="1668190578373">
<node TEXT="Editor features" STYLE_REF="Purpose" ID="ID_343021245" CREATED="1541848060291" MODIFIED="1541848968607">
<node TEXT="New UI icons (created by Rexel and Predrag)" ID="ID_1135148263" CREATED="1668190473688" MODIFIED="1668190473688"/>
<node TEXT="Command line option -R to run groovy scripts on start-up" ID="ID_1329668559" CREATED="1668190473688" MODIFIED="1668190473688"/>
<node TEXT="Support command line options -R and -S in non interactive mode" ID="ID_1972168280" CREATED="1668190473695" MODIFIED="1668190473695"/>
<node TEXT="Compact map layout" ID="ID_1482025158" CREATED="1668190489243" MODIFIED="1668190489243"/>
<node TEXT="Jump in selected subtrees" ID="ID_519305347" CREATED="1668190533641" MODIFIED="1668190533641"/>
<node TEXT="Vertical aligning child nodes at the top, at the bottom or at the center of the parent node" ID="ID_161848692" CREATED="1668190566667" MODIFIED="1668190566667"/>
<node TEXT="Consider hidden free node positions in map layout" ID="ID_430638175" CREATED="1668190566667" MODIFIED="1668190566667"/>
<node TEXT="Creating new user style from selection assigns it to all selected nodes and clears their formatting" ID="ID_1478638916" CREATED="1668190566691" MODIFIED="1668190566691"/>
<node TEXT="Make dragging area width configurable and increase its default size" ID="ID_1449443293" CREATED="1668190566691" MODIFIED="1668190566691"/>
<node TEXT="Change key based navigation for summary nodes" ID="ID_1444166533" CREATED="1668190566693" MODIFIED="1668190566693"/>
<node TEXT="Word wrap in editors for formulas, scripts, markdown and latex" ID="ID_171708339" CREATED="1668190566693" MODIFIED="1668190566693"/>
</node>
<node TEXT="Updated software components" STYLE_REF="Purpose" FOLDED="true" ID="ID_1651141080" CREATED="1541847932161" MODIFIED="1675607272082" VGAP_QUANTITY="2 pt">
<node TEXT="Java 17 support" ID="ID_602837388" CREATED="1668190416034" MODIFIED="1668190416034"/>
<node TEXT="Updated Groovy to version 4" ID="ID_1354215770" CREATED="1668190416034" MODIFIED="1675607272081"/>
</node>
</node>
<node TEXT="Version 1.9.x" STYLE_REF="Actions" POSITION="bottom_or_right" ID="ID_1638165251" CREATED="1541847687331" MODIFIED="1620389180349">
<node TEXT="Editor features" STYLE_REF="Purpose" FOLDED="true" ID="ID_1741600255" CREATED="1541848060291" MODIFIED="1620561170250">
<node TEXT="Connector styles configurable at user style nodes in style editor" ID="ID_309599694" CREATED="1620389785732" MODIFIED="1620389785732"/>
<node TEXT="Selected node colors configurable at appropriate styles in use style editor" ID="ID_521611641" CREATED="1620389785732" MODIFIED="1620389785732"/>
<node TEXT="Formulas in node core, node details and notes" ID="ID_1417671379" CREATED="1620389785752" MODIFIED="1620560915450"/>
<node TEXT="Markdown in node core, node details and notes" ID="ID_1004436581" CREATED="1620389785746" MODIFIED="1620560894192"/>
<node TEXT="Latex in node core, node details and notes" ID="ID_806550986" CREATED="1620389785750" MODIFIED="1620560905322"/>
<node TEXT="New Freeplane Application icons and splash screen" ID="ID_1937358243" CREATED="1620389785754" MODIFIED="1620389785754"/>
<node TEXT="Configurable icons for links depending on link URL and file extension" ID="ID_962835223" CREATED="1620389785757" MODIFIED="1620389785757"/>
<node TEXT="Following styles defined in external maps (synchronized when the map is loaded)" ID="ID_1108233463" CREATED="1620389785759" MODIFIED="1620389785759"/>
<node TEXT="New action for copying styles from other maps but keeping own user styles if they are different" ID="ID_393838871" CREATED="1620389785762" MODIFIED="1620389785762"/>
<node TEXT="Fix default edge and node widths" ID="ID_212042030" CREATED="1620389785769" MODIFIED="1620389785769"/>
<node TEXT="Option to open node links only if Control key is pressed" ID="ID_420124712" CREATED="1620389785773" MODIFIED="1620389785773"/>
<node TEXT="Add background colors to generated HTML" ID="ID_1992594483" CREATED="1620389785775" MODIFIED="1620389785775"/>
<node TEXT="Option to disable all colors in generated HTML" ID="ID_216094501" CREATED="1620389785776" MODIFIED="1620389785776"/>
<node TEXT="Speed up file dialogs" ID="ID_1169212059" CREATED="1620389785777" MODIFIED="1620389785777"/>
<node TEXT="VAqua Look and Feel for MacOS" ID="ID_1622519681" CREATED="1620389785778" MODIFIED="1620561043463"/>
<node TEXT="Scripting API extended to support the new features" ID="ID_1116916304" CREATED="1620389785779" MODIFIED="1620389785779"/>
<node TEXT="Copy images pasted into rich text editors" ID="ID_454781677" CREATED="1620389785780" MODIFIED="1620389785780"/>
<node TEXT="Mind map template previews" ID="ID_704419783" CREATED="1620389785781" MODIFIED="1620389785781"/>
<node TEXT="Replace SplitToWords by SplitInRows" ID="ID_1145247548" CREATED="1620389785782" MODIFIED="1620389785782"/>
</node>
<node TEXT="Updated software components" STYLE_REF="Purpose" FOLDED="true" ID="ID_234329131" CREATED="1541847932161" MODIFIED="1541848968607">
<node TEXT="Java 15 support" ID="ID_1930135299" CREATED="1620389739143" MODIFIED="1620389742376"/>
<node TEXT="Updated Groovy to 3.0.8" ID="ID_702829975" CREATED="1620389743797" MODIFIED="1620389751288"/>
</node>
</node>
<node TEXT="Version 1.8.x" STYLE_REF="Actions" FOLDED="true" POSITION="bottom_or_right" ID="ID_25848260" CREATED="1541847687331" MODIFIED="1620389153347">
<node TEXT="Editor features" STYLE_REF="Purpose" ID="ID_109116165" CREATED="1541848060291" MODIFIED="1541848968607">
<node TEXT="Use emoticons from https://twemoji.twitter.com/ as icons" ID="ID_391516983" CREATED="1620389509864" MODIFIED="1620389509864" LINK="https://twemoji.twitter.com/"/>
<node TEXT="Optionally ignore accents and diacritics in filter conditions" ID="ID_684736245" CREATED="1620389550378" MODIFIED="1620389550378"/>
<node TEXT="Usability improvements suggested by University of Oulu UX research group" ID="ID_1022343423" CREATED="1620389563253" MODIFIED="1620389563253"/>
<node TEXT="Quick &quot;And filter&quot; and &quot;Or filter&quot;" ID="ID_1228038812" CREATED="1620389573027" MODIFIED="1620389573027"/>
<node TEXT="Command/Preferences/icons search dialog (Control/⌘ + F1)" ID="ID_1203365755" CREATED="1620389595525" MODIFIED="1620560872498" LINK="https://youtu.be/GNoCulrt-xY"/>
<node TEXT="Draw clouds around hidden or filtered our nodes" ID="ID_556585090" CREATED="1620389771860" MODIFIED="1620389771860"/>
</node>
<node TEXT="Updated software components" STYLE_REF="Purpose" FOLDED="true" ID="ID_1480328703" CREATED="1541847932161" MODIFIED="1541848968607">
<node TEXT="Java 13 support" ID="ID_1423743100" CREATED="1620389729576" MODIFIED="1620389733768"/>
</node>
<node TEXT="Formulas and scripts" STYLE_REF="Purpose" ID="ID_377149294" CREATED="1541847985637" MODIFIED="1541848968607"/>
</node>
<node TEXT="Version 1.7.x" STYLE_REF="Actions" FOLDED="true" POSITION="bottom_or_right" ID="ID_1757711154" CREATED="1541847687331" MODIFIED="1541847877907">
<node TEXT="Editor features" STYLE_REF="Purpose" FOLDED="true" ID="ID_1765567826" CREATED="1541848060291" MODIFIED="1541848968607">
<node TEXT="Nodes URLs with relative and absolute paths like #at(:~someNodeAlias)" ID="ID_1539755264" CREATED="1541847881092" MODIFIED="1541847881092"/>
<node TEXT="Node aliases" ID="ID_1352789056" CREATED="1541847881092" MODIFIED="1541847881092"/>
<node TEXT="Global nodes" ID="ID_24137642" CREATED="1541847881092" MODIFIED="1541847881092"/>
<node TEXT="Filtering nodes on aliases and on global accessibility" ID="ID_1864695826" CREATED="1541847881092" MODIFIED="1541847881092"/>
<node TEXT="Option to skip dialog for unnassigned F-keys" ID="ID_939273256" CREATED="1541847881092" MODIFIED="1541847881092"/>
<node TEXT="Denied (inverted) conditions in filter toolbar" ID="ID_1393999486" CREATED="1541847881092" MODIFIED="1541847881092"/>
<node TEXT="Export for selected branches only" ID="ID_817997036" CREATED="1541847881092" MODIFIED="1541847881092"/>
<node TEXT="Go to nodes by their &quot;reference paths&quot;" ID="ID_1785822754" CREATED="1541847881092" MODIFIED="1541847881092"/>
<node TEXT="Dark UI mode support (Look and feel and map template &quot;Darcula&quot;)" ID="ID_1513276613" CREATED="1620389389899" MODIFIED="1620389393403"/>
</node>
<node TEXT="Formulas and scripts" STYLE_REF="Purpose" FOLDED="true" ID="ID_635077048" CREATED="1541847985637" MODIFIED="1541848968607">
<node TEXT="Formula dependency tracing" ID="ID_181704820" CREATED="1541847881092" MODIFIED="1541847881092"/>
<node TEXT="API for referencing nodes from formulas and scripts" ID="ID_170531925" CREATED="1541847881092" MODIFIED="1541847881092"/>
<node TEXT="API for loading mind maps" ID="ID_1674596796" CREATED="1541847881092" MODIFIED="1541847881092"/>
<node TEXT="API for script execution" ID="ID_1341352019" CREATED="1541847881092" MODIFIED="1541847881092"/>
<node TEXT="Add new properties  to connector API" ID="ID_235525051" CREATED="1620389487416" MODIFIED="1620389488747"/>
<node TEXT="Persistent cache for compiled library scripts" ID="ID_45063614" CREATED="1541848275479" MODIFIED="1541848294730"/>
<node TEXT="Runtime cache for compiled formulas and scripts" ID="ID_1167426315" CREATED="1541848295504" MODIFIED="1541848315487"/>
</node>
<node TEXT="Updated software components" STYLE_REF="Purpose" FOLDED="true" ID="ID_1632208670" CREATED="1541847932161" MODIFIED="1541848968607">
<node TEXT="Java 7 support dropped, Java 8 is required" ID="ID_231313072" CREATED="1541847881092" MODIFIED="1541847881092"/>
<node TEXT="Java 11 support" ID="ID_1997699757" CREATED="1541847881092" MODIFIED="1541847881092"/>
<node TEXT="Update Groovy to version 2.5.3" ID="ID_1119389141" CREATED="1541847881092" MODIFIED="1541847918742"/>
<node TEXT="Update JLatexMath to versin 1.0.7" ID="ID_14411050" CREATED="1541847881092" MODIFIED="1541847881092"/>
<node TEXT="Update batik to version 2.10" ID="ID_1048875697" CREATED="1541847881092" MODIFIED="1541847881092"/>
</node>
<node TEXT="Freeplane API can be embedded in arbitrary JVM applications and scripts" ID="ID_343365936" CREATED="1541848092374" MODIFIED="1620389208685"/>
</node>
<node TEXT="Version 1.6.x" STYLE_REF="Actions" FOLDED="true" POSITION="bottom_or_right" ID="ID_132081568" CREATED="1541847698879" MODIFIED="1541850524278">
<node TEXT="connectors" STYLE_REF="Purpose" FOLDED="true" ID="ID_1672618426" CREATED="1541848613842" MODIFIED="1541848953471">
<node TEXT="Option to hide connectors" ID="ID_389731514" CREATED="1541848487215" MODIFIED="1541848487215"/>
<node TEXT="Option to show connectors only for selected nodes" ID="ID_353659522" CREATED="1541848487215" MODIFIED="1541848487215"/>
<node TEXT="Option to hide connectors when filtering hides other connector end" ID="ID_244698873" CREATED="1541848487246" MODIFIED="1541848487246"/>
<node TEXT="Options to hide icons and connectors" ID="ID_258015212" CREATED="1541848487230" MODIFIED="1541848487230"/>
</node>
<node TEXT="formatting" STYLE_REF="Purpose" FOLDED="true" ID="ID_1839085284" CREATED="1541848655304" MODIFIED="1541848923823">
<node TEXT="Configurable map icon sizes" ID="ID_775318770" CREATED="1541848487230" MODIFIED="1541850458119"/>
<node TEXT="Strike through formatting for nodes added" ID="ID_518969117" CREATED="1541848487230" MODIFIED="1541848487230"/>
<node TEXT="Image in node without black border (no border)" ID="ID_1952572862" CREATED="1541848487246" MODIFIED="1541848487246"/>
<node TEXT="Configurable clone marks" ID="ID_1297692234" CREATED="1541848487246" MODIFIED="1541848487246"/>
<node TEXT="Configurable standard cloud shape" ID="ID_102027493" CREATED="1541848487246" MODIFIED="1541848487246"/>
<node TEXT="Configurable node border widths" ID="ID_1709640038" CREATED="1541848487246" MODIFIED="1541848487246"/>
<node TEXT="Configurable node border colors" ID="ID_799827115" CREATED="1541848487246" MODIFIED="1541848487246"/>
<node TEXT="Configurable edge line type" ID="ID_775607802" CREATED="1541848487246" MODIFIED="1541848487246"/>
<node TEXT="Configurable node border line type" ID="ID_592874223" CREATED="1541848487246" MODIFIED="1541848487246"/>
<node TEXT="Configurable standard for connector arrows" ID="ID_1095586855" CREATED="1541848487246" MODIFIED="1541848487246"/>
<node TEXT="Configurable standard for connector line types" ID="ID_1171811720" CREATED="1541848487246" MODIFIED="1541848487246"/>
</node>
<node TEXT="user interface" STYLE_REF="Purpose" FOLDED="true" ID="ID_1705450882" CREATED="1541848669161" MODIFIED="1541850493867">
<node TEXT="Add icon and attribute conditions to style editor" ID="ID_557879353" CREATED="1541848487230" MODIFIED="1541848487230"/>
<node TEXT="Skip node numbers on node sort and in some other cases" ID="ID_1101307232" CREATED="1541848487230" MODIFIED="1541848487230"/>
<node TEXT="Show creation modification in the status line" ID="ID_1674482897" CREATED="1541848487230" MODIFIED="1541848487230"/>
<node TEXT="Outline view fits window width (option)" ID="ID_855766222" CREATED="1541848487230" MODIFIED="1541848487230"/>
<node TEXT="Save note when note editor looses focus" ID="ID_1387794471" CREATED="1541848487230" MODIFIED="1541848487230"/>
<node TEXT="Detail icon color matches detail color instead of node border color" ID="ID_146106752" CREATED="1541848487230" MODIFIED="1541848487230"/>
<node TEXT="Drag and drop of image file holding Ctrl+Shift creates a link without inserting the image into the map" ID="ID_223599963" CREATED="1541848487230" MODIFIED="1541848487230"/>
<node TEXT="&quot;Close all maps&quot; and &quot;close all other maps&quot; actions added to file menu" ID="ID_1478173293" CREATED="1541848487230" MODIFIED="1541848487230"/>
<node TEXT="SVG icons for map and UI" ID="ID_1287774899" CREATED="1541848487230" MODIFIED="1541848487230"/>
<node TEXT="Paint clone node markers only on selected nodes" ID="ID_1512259000" CREATED="1541848487230" MODIFIED="1541848487230"/>
<node TEXT="Action for showing next presentation item without folding other nodes" ID="ID_1257128438" CREATED="1541848487246" MODIFIED="1541848487246"/>
<node TEXT="Optional presentation automatics &quot;processesUpDownKeys&quot;, &quot;switchToFullScreen&quot;, &quot;switchToPresentationMode&quot;" ID="ID_439917968" CREATED="1541848487246" MODIFIED="1541848487246"/>
<node TEXT="Increased sensitive area for image scaling" ID="ID_1399423959" CREATED="1541848487246" MODIFIED="1541848487246"/>
<node TEXT="No special treatment for white space nodes when content is copied to text" ID="ID_1062579525" CREATED="1541848487246" MODIFIED="1541848487246"/>
<node TEXT="Option to paste images from files" ID="ID_452998276" CREATED="1541848487246" MODIFIED="1541848487246"/>
<node TEXT="Dragged and dropped files are copied into directory mindmap_files/ if CONTROL key is pressed" ID="ID_1928449518" CREATED="1541848487246" MODIFIED="1541848487246"/>
<node TEXT="Option note icon in notes tool-tip" ID="ID_147304126" CREATED="1541848487230" MODIFIED="1541848487230"/>
<node TEXT="Option to move aligned node smoothly" ID="ID_1209459718" CREATED="1541848487230" MODIFIED="1541848487230"/>
<node TEXT="Option to define spotlight background color" ID="ID_1714364653" CREATED="1541848487230" MODIFIED="1541848487230"/>
<node TEXT="Configurable UI icon sizes" ID="ID_1108785112" CREATED="1541848487230" MODIFIED="1541850460364"/>
<node TEXT="Aligning current node on the left side, on the right side or on the center, hot keys alt+W (west), alt+E (east), alt+C (center)" ID="ID_150208659" CREATED="1541848487230" MODIFIED="1541848487230"/>
<node TEXT="Editor dialog for automatic edge colors" ID="ID_1960800845" CREATED="1541848487246" MODIFIED="1541848487246"/>
</node>
<node TEXT="export" STYLE_REF="Purpose" FOLDED="true" ID="ID_1018225688" CREATED="1541848658297" MODIFIED="1541848942192">
<node TEXT="Options for handling texts and fonts in exported PDF and SVG" ID="ID_1250175151" CREATED="1541848487230" MODIFIED="1541848487230"/>
<node TEXT="Removed broken export to flash" ID="ID_168053298" CREATED="1541848487230" MODIFIED="1541848487230"/>
<node TEXT="Export for java web start" ID="ID_923366841" CREATED="1541848487246" MODIFIED="1541848487246"/>
</node>
<node TEXT="Presentations" ID="ID_1736223790" CREATED="1541848487246" MODIFIED="1541848568631" LINK="ID_1747402695"/>
</node>
</node>
</map>
