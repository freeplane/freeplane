<map version="freeplane 1.3.0">
<!--To view this file, download free mind mapping software Freeplane from http://freeplane.sourceforge.net -->
<attribute_registry SHOW_ATTRIBUTES="hide"/>
<node TEXT="Menu for Filemode" FOLDED="false" ID="ID_201436647" CREATED="1370196341000" MODIFIED="1370196363296"><hook NAME="MapStyle">
    <conditional_styles>
        <conditional_style ACTIVE="true" STYLE_REF="category" LAST="true">
            <attribute_contains_condition ATTRIBUTE="type" VALUE="category" MATCH_CASE="true" MATCH_APPROXIMATELY="false"/>
        </conditional_style>
        <conditional_style ACTIVE="true" STYLE_REF="action" LAST="true">
            <attribute_contains_condition ATTRIBUTE="type" VALUE="action" MATCH_CASE="true" MATCH_APPROXIMATELY="false"/>
        </conditional_style>
        <conditional_style ACTIVE="true" STYLE_REF="radio_action" LAST="true">
            <attribute_contains_condition ATTRIBUTE="type" VALUE="radio_action" MATCH_CASE="true" MATCH_APPROXIMATELY="false"/>
        </conditional_style>
        <conditional_style ACTIVE="true" STYLE_REF="separator" LAST="true">
            <attribute_contains_condition ATTRIBUTE="type" VALUE="separator" MATCH_CASE="true" MATCH_APPROXIMATELY="false"/>
        </conditional_style>
        <conditional_style ACTIVE="true" STYLE_REF="submenu" LAST="true">
            <attribute_contains_condition ATTRIBUTE="type" VALUE="submenu" MATCH_CASE="true" MATCH_APPROXIMATELY="false"/>
        </conditional_style>
    </conditional_styles>
    <properties show_icon_for_attributes="false" show_note_icons="true"/>

<map_styles>
<stylenode LOCALIZED_TEXT="styles.root_node">
<stylenode LOCALIZED_TEXT="styles.predefined" POSITION="right">
<stylenode LOCALIZED_TEXT="default" MAX_WIDTH="600" COLOR="#000000" STYLE="as_parent">
<font NAME="SansSerif" SIZE="10" BOLD="false" ITALIC="false"/>
</stylenode>
<stylenode LOCALIZED_TEXT="defaultstyle.details"/>
<stylenode LOCALIZED_TEXT="defaultstyle.note"/>
<stylenode LOCALIZED_TEXT="defaultstyle.floating">
<edge STYLE="hide_edge"/>
<cloud COLOR="#f0f0f0" SHAPE="ROUND_RECT"/>
</stylenode>
</stylenode>
<stylenode LOCALIZED_TEXT="styles.user-defined" POSITION="right">
<stylenode LOCALIZED_TEXT="styles.topic" COLOR="#18898b" STYLE="fork">
<font NAME="Liberation Sans" SIZE="10" BOLD="true"/>
</stylenode>
<stylenode LOCALIZED_TEXT="styles.subtopic" COLOR="#cc3300" STYLE="fork">
<font NAME="Liberation Sans" SIZE="10" BOLD="true"/>
</stylenode>
<stylenode LOCALIZED_TEXT="styles.subsubtopic" COLOR="#669900">
<font NAME="Liberation Sans" SIZE="10" BOLD="true"/>
</stylenode>
<stylenode LOCALIZED_TEXT="styles.important">
<icon BUILTIN="yes"/>
</stylenode>
<stylenode TEXT="separator" COLOR="#999999"/>
<stylenode TEXT="action"/>
<stylenode TEXT="radio_action">
<icon BUILTIN="unchecked"/>
</stylenode>
<stylenode TEXT="category" COLOR="#000000" BACKGROUND_COLOR="#ccffcc">
<font ITALIC="true"/>
<cloud COLOR="#ccffcc" SHAPE="ARC"/>
</stylenode>
<stylenode TEXT="submenu">
<font ITALIC="true"/>
</stylenode>
</stylenode>
<stylenode LOCALIZED_TEXT="styles.AutomaticLayout" POSITION="right">
<stylenode LOCALIZED_TEXT="AutomaticLayout.level.root" COLOR="#000000">
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
</stylenode>
</stylenode>
</map_styles>
</hook>
<node TEXT="menu_bar" POSITION="right" ID="ID_32635647" CREATED="1370196341001" MODIFIED="1370196341001">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="menu_bar"/>
<node TEXT="File" FOLDED="true" ID="ID_559147292" CREATED="1370196341010" MODIFIED="1370196341010">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="file"/>
<attribute NAME="name_ref" VALUE="file"/>
<node TEXT="Most recent maps" ID="ID_235051047" CREATED="1370196341010" MODIFIED="1370336913324">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="menu_key" VALUE="main_menu_most_recent_files"/>
<attribute NAME="name" VALUE="last"/>
<attribute NAME="name_ref" VALUE="most_recent_files"/>
</node>
<node TEXT="Print setup..." ID="ID_1716886830" CREATED="1370196341021" MODIFIED="1370196341021">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="PageAction"/>
</node>
<node TEXT="Print preview..." ID="ID_1838270397" CREATED="1370196341022" MODIFIED="1370196341022">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="PrintPreviewAction"/>
</node>
<node TEXT="Print map..." ID="ID_127592253" CREATED="1370196341025" MODIFIED="1370196341025">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="control P"/>
<attribute NAME="action" VALUE="PrintAction"/>
</node>
<node TEXT="Close current map" ID="ID_834282498" CREATED="1370196341026" MODIFIED="1370196341026">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="control W"/>
<attribute NAME="action" VALUE="CloseAction"/>
</node>
<node TEXT="Quit Freeplane" ID="ID_620197928" CREATED="1370196341026" MODIFIED="1370196341026">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="control Q"/>
<attribute NAME="action" VALUE="QuitAction"/>
<attribute NAME="menu_key" VALUE="MB_QuitAction"/>
</node>
</node>
<node TEXT="Edit" FOLDED="true" ID="ID_1176046308" CREATED="1370196341026" MODIFIED="1370196341026">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="edit"/>
<attribute NAME="name_ref" VALUE="edit"/>
<node TEXT="find" FOLDED="true" ID="ID_532385965" CREATED="1370196341026" MODIFIED="1370196341026">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="find"/>
<node TEXT="Find..." ID="ID_1002317457" CREATED="1370196341026" MODIFIED="1370196341026">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="control G"/>
<attribute NAME="action" VALUE="FindAction"/>
</node>
<node TEXT="Find next" ID="ID_707793412" CREATED="1370196341027" MODIFIED="1370196341027">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="control shift G"/>
<attribute NAME="action" VALUE="QuickFindAction.FORWARD"/>
</node>
<node TEXT="Find previous" ID="ID_236170818" CREATED="1370196341027" MODIFIED="1370196420437">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="QuickFindAction.BACK"/>
</node>
<node TEXT="Open File" ID="ID_669079876" CREATED="1370196341027" MODIFIED="1370196341027">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="OpenPathAction"/>
</node>
</node>
</node>
<node TEXT="view" FOLDED="true" ID="ID_145741322" CREATED="1370196341027" MODIFIED="1370196341027">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="view"/>
<attribute NAME="name_ref" VALUE="menu_view"/>
<node TEXT="Menu_Toolbar_Panel" FOLDED="true" ID="ID_1381163767" CREATED="1370196341027" MODIFIED="1370196341027">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="Menu_Toolbar_Panel"/>
<node TEXT="toolbars" FOLDED="true" ID="ID_7330669" CREATED="1370196341028" MODIFIED="1370196341028">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="toolbars"/>
<node TEXT="Toolbars" FOLDED="true" ID="ID_623864969" CREATED="1370196341034" MODIFIED="1370336842333">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="toolbars"/>
<attribute NAME="name_ref" VALUE="menu_toolbars"/>
<node TEXT="Menubar" ID="ID_550762170" CREATED="1370196341035" MODIFIED="1370196341035">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ToggleMenubarAction"/>
<attribute NAME="menu_key" VALUE="MB_ToggleMenubarAction"/>
</node>
<node TEXT="Toolbar" ID="ID_1101190455" CREATED="1370196341035" MODIFIED="1370196341035">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ToggleToolbarAction"/>
</node>
<node TEXT="Filter toolbar" ID="ID_1834541773" CREATED="1370196341035" MODIFIED="1370196341035">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ShowFilterToolbarAction"/>
<attribute NAME="accelerator" VALUE="control F"/>
</node>
<node TEXT="Scrollbars" ID="ID_1325316435" CREATED="1370196341035" MODIFIED="1370196341035">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ToggleScrollbarsAction"/>
</node>
<node TEXT="Presentation mode" ID="ID_1201946503" CREATED="1370196341035" MODIFIED="1370336996637">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SetBooleanPropertyAction.presentation_mode"/>
</node>
</node>
</node>
</node>
<node TEXT="zoom" FOLDED="true" ID="ID_1165816028" CREATED="1370196341035" MODIFIED="1370196341035">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="zoom"/>
<node TEXT="Zoom in" ID="ID_1613199056" CREATED="1370196341036" MODIFIED="1370196341036">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="alt UP"/>
<attribute NAME="action" VALUE="ZoomInAction"/>
</node>
<node TEXT="Zoom out" ID="ID_1893025140" CREATED="1370196341036" MODIFIED="1370196341036">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="alt DOWN"/>
<attribute NAME="action" VALUE="ZoomOutAction"/>
</node>
<node TEXT="Center selected node" ID="ID_1801971893" CREATED="1370196341036" MODIFIED="1370196341036">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="control alt C"/>
<attribute NAME="action" VALUE="CenterSelectedNodeAction"/>
</node>
</node>
<node TEXT="View settings" FOLDED="true" ID="ID_919512036" CREATED="1370196341036" MODIFIED="1370196341036">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="menu_viewmode"/>
<attribute NAME="name_ref" VALUE="menu_viewmode"/>
<node TEXT="Outline view" ID="ID_1850220739" CREATED="1370196341036" MODIFIED="1370196341036">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ViewLayoutTypeAction.OUTLINE"/>
</node>
<node TEXT="Rectangular selection" ID="ID_1417267859" CREATED="1370196341036" MODIFIED="1370196341036">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ShowSelectionAsRectangleAction"/>
</node>
</node>
</node>
<node TEXT="navigate" FOLDED="true" ID="ID_470424918" CREATED="1370196341037" MODIFIED="1370196341037">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="navigate"/>
<attribute NAME="name_ref" VALUE="menu_navigate"/>
<node TEXT="navigate" FOLDED="true" ID="ID_922830929" CREATED="1370196341037" MODIFIED="1370196341037">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="navigate"/>
<node TEXT="Previous map" ID="ID_1700653396" CREATED="1370196341037" MODIFIED="1370196341037">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="control shift TAB"/>
<attribute NAME="action" VALUE="NavigationPreviousMapAction"/>
</node>
<node TEXT="Next map" ID="ID_646647320" CREATED="1370196341037" MODIFIED="1370196341037">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="control TAB"/>
<attribute NAME="action" VALUE="NavigationNextMapAction"/>
</node>
<node TEXT="folding" FOLDED="true" ID="ID_1202051404" CREATED="1370196341041" MODIFIED="1370196341041">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="folding"/>
<node TEXT="(Un)fold" ID="ID_1661783713" CREATED="1370196341041" MODIFIED="1370196341041">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="SPACE"/>
<attribute NAME="action" VALUE="ToggleFoldedAction"/>
</node>
<node TEXT="Show next child" ID="ID_1774683712" CREATED="1370196341042" MODIFIED="1370196341042">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ShowNextChildAction"/>
<attribute NAME="accelerator" VALUE="shift SPACE"/>
</node>
<node TEXT="(Un)fold children" ID="ID_1995267580" CREATED="1370196341042" MODIFIED="1370196341042">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="control SPACE"/>
<attribute NAME="action" VALUE="ToggleChildrenFoldedAction"/>
</node>
</node>
</node>
<node TEXT="Goto root" ID="ID_1840726524" CREATED="1370196341042" MODIFIED="1370196341042">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="ESCAPE"/>
<attribute NAME="action" VALUE="MoveToRootAction"/>
</node>
<node TEXT="Goto previous node" ID="ID_1915289168" CREATED="1370196341042" MODIFIED="1370196341042">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="control alt LEFT"/>
<attribute NAME="action" VALUE="NextNodeAction.BACK"/>
</node>
<node TEXT="Goto next node" ID="ID_1200359313" CREATED="1370196341044" MODIFIED="1370196341044">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="control alt RIGHT"/>
<attribute NAME="action" VALUE="NextNodeAction.FORWARD"/>
</node>
</node>
<node TEXT="Filter" FOLDED="true" ID="ID_865102516" CREATED="1370196341049" MODIFIED="1370196341049">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="filter"/>
<attribute NAME="name_ref" VALUE="menu_filter"/>
<node TEXT="Filter" FOLDED="true" ID="ID_1245776869" CREATED="1370196341049" MODIFIED="1370196341049">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="Filter"/>
<node TEXT="DoFilter" FOLDED="true" ID="ID_1770253546" CREATED="1370196341049" MODIFIED="1370196341049">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="DoFilter"/>
<node TEXT="Undo filter action" ID="ID_1952308974" CREATED="1370196341050" MODIFIED="1370196341050">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="UndoFilterAction"/>
</node>
<node TEXT="Redo filter action" ID="ID_196177860" CREATED="1370196341050" MODIFIED="1370196341050">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="RedoFilterAction"/>
</node>
<node TEXT="Reapply filter action" ID="ID_100550506" CREATED="1370196341050" MODIFIED="1370196341050">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ReapplyFilterAction"/>
</node>
<node TEXT="Quick filter" ID="ID_1928680482" CREATED="1370196341050" MODIFIED="1370196341050">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="QuickFilterAction"/>
</node>
<node TEXT="Filter selected nodes" ID="ID_1907894877" CREATED="1370196341111" MODIFIED="1370196341111">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ApplySelectedViewConditionAction"/>
</node>
<node TEXT="Select all matching nodes" ID="ID_592402852" CREATED="1370196341111" MODIFIED="1370196341111">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="QuickFindAllAction"/>
</node>
<node TEXT="No filtering" ID="ID_575972793" CREATED="1370196341111" MODIFIED="1370196341111">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ApplyNoFilteringAction"/>
</node>
<node TEXT="Compose filter" ID="ID_1946747633" CREATED="1370196341111" MODIFIED="1370196341111">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="EditFilterAction"/>
</node>
</node>
<node TEXT="FilterCondition" FOLDED="true" ID="ID_421431012" CREATED="1370196341116" MODIFIED="1370196341116">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="FilterCondition"/>
<node TEXT="Applies to filtered nodes" ID="ID_706220136" CREATED="1370196341117" MODIFIED="1370196341117">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ApplyToVisibleAction"/>
</node>
<node TEXT="Show ancestors" ID="ID_443820131" CREATED="1370196341117" MODIFIED="1370196341117">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ShowAncestorsAction"/>
</node>
<node TEXT="Show descendants" ID="ID_1961158927" CREATED="1370196341117" MODIFIED="1370196341117">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ShowDescendantsAction"/>
</node>
</node>
<node TEXT="Find" FOLDED="true" ID="ID_180210796" CREATED="1370196341117" MODIFIED="1370196341117">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="Find"/>
<node TEXT="Find previous" ID="ID_680619885" CREATED="1370196341117" MODIFIED="1370196341117">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="QuickFindAction.BACK"/>
</node>
<node TEXT="Find next" ID="ID_967730868" CREATED="1370196341117" MODIFIED="1370196341117">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="QuickFindAction.FORWARD"/>
</node>
</node>
</node>
</node>
<node TEXT="Maps" FOLDED="true" ID="ID_1277849098" CREATED="1370196341117" MODIFIED="1370196341117">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="mindmaps"/>
<attribute NAME="name_ref" VALUE="mindmaps"/>
<node TEXT="Modes" ID="ID_5487162" CREATED="1370196341117" MODIFIED="1370196341117">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="menu_key" VALUE="main_menu_modes"/>
<attribute NAME="name" VALUE="modes"/>
</node>
<node TEXT="navigate" ID="ID_661325256" CREATED="1370196341118" MODIFIED="1370196341118">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="menu_key" VALUE="main_menu_navigate_maps"/>
<attribute NAME="name" VALUE="navigate"/>
</node>
<node TEXT="Maps" ID="ID_1421008563" CREATED="1370196341118" MODIFIED="1370196341118">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="menu_key" VALUE="main_menu_mindmaps"/>
<attribute NAME="name" VALUE="mindmaps"/>
</node>
</node>
<node TEXT="Help" FOLDED="true" ID="ID_1686155645" CREATED="1370196341118" MODIFIED="1370196341118">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="help"/>
<attribute NAME="name_ref" VALUE="help"/>
<node TEXT="update" FOLDED="true" ID="ID_1178888256" CREATED="1370196341118" MODIFIED="1370196341118">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="update"/>
<node TEXT="Check for updates" ID="ID_702644148" CREATED="1370196341124" MODIFIED="1370196341124">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="UpdateCheckAction"/>
</node>
</node>
<node TEXT="Web resources" FOLDED="true" ID="ID_911592986" CREATED="1370196341125" MODIFIED="1370196341125">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="Web resources"/>
<node TEXT="Freeplane&apos;s Homepage" ID="ID_1039201186" CREATED="1370196341125" MODIFIED="1370196341125">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="OpenFreeplaneSiteAction"/>
</node>
<node TEXT="Ask for help" ID="ID_1079864325" CREATED="1370196341125" MODIFIED="1370196341125">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="AskForHelp"/>
</node>
<node TEXT="Report a bug" ID="ID_611302666" CREATED="1370196341125" MODIFIED="1370196341125">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ReportBugAction"/>
</node>
<node TEXT="Request a feature" ID="ID_688619955" CREATED="1370196341125" MODIFIED="1370196341125">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="RequestFeatureAction"/>
</node>
</node>
<node TEXT="legacy" FOLDED="true" ID="ID_183712839" CREATED="1370196341125" MODIFIED="1370196341125">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="legacy"/>
<node TEXT="About" ID="ID_410708086" CREATED="1370196341134" MODIFIED="1370196341134">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="AboutAction"/>
<attribute NAME="menu_key" VALUE="MB_AboutAction"/>
</node>
</node>
<node TEXT="Tutorial" ID="ID_427345446" CREATED="1370196341134" MODIFIED="1370196341134">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="GettingStartedAction"/>
</node>
<node TEXT="Documentation" ID="ID_917660070" CREATED="1370196341134" MODIFIED="1370196341134">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="F1"/>
<attribute NAME="action" VALUE="DocumentationAction"/>
</node>
</node>
</node>
<node TEXT="map_popup" FOLDED="true" POSITION="right" ID="ID_1296648975" CREATED="1370196341135" MODIFIED="1370196341135">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="map_popup"/>
<node TEXT="Maps" ID="ID_299624286" CREATED="1370196341135" MODIFIED="1370196341135">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="mindmaps"/>
<attribute NAME="menu_key" VALUE="popup_menu_mindmaps"/>
</node>
<node TEXT="Menubar" ID="ID_765873519" CREATED="1370196341135" MODIFIED="1370196341135">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ToggleMenubarAction"/>
<attribute NAME="menu_key" VALUE="MP_ToggleMenubarAction"/>
</node>
<node TEXT="Toolbar" ID="ID_1416238396" CREATED="1370196341135" MODIFIED="1370196341135">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ToggleToolbarAction"/>
</node>
<node TEXT="Filter toolbar" ID="ID_1015388149" CREATED="1370196341135" MODIFIED="1370196341135">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ShowFilterToolbarAction"/>
</node>
<node TEXT="Scrollbars" ID="ID_1970921662" CREATED="1370196341135" MODIFIED="1370196341135">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ToggleScrollbarsAction"/>
</node>
<node TEXT="Outline view" ID="ID_403436016" CREATED="1370196341135" MODIFIED="1370196341135">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ViewLayoutTypeAction.OUTLINE"/>
</node>
<node TEXT="Center selected node" ID="ID_1378526351" CREATED="1370196341135" MODIFIED="1370196341135">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="CenterSelectedNodeAction"/>
</node>
<node TEXT="Goto root" ID="ID_1633709060" CREATED="1370196341136" MODIFIED="1370196341136">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="ESCAPE"/>
<attribute NAME="action" VALUE="MoveToRootAction"/>
</node>
</node>
<node TEXT="node_popup" FOLDED="true" POSITION="right" ID="ID_750660346" CREATED="1370196341136" MODIFIED="1370196341136">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="node_popup"/>
<node TEXT="Center" ID="ID_854389109" CREATED="1370196341136" MODIFIED="1370196341136">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="CenterAction"/>
</node>
</node>
<node TEXT="main_toolbar" FOLDED="true" POSITION="right" ID="ID_507310330" CREATED="1370196341136" MODIFIED="1370196341136">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="main_toolbar"/>
<node TEXT="update" ID="ID_605543401" CREATED="1370196341136" MODIFIED="1370196341136">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="menu_key" VALUE="main_toolbar_update"/>
<attribute NAME="name" VALUE="update"/>
</node>
<node TEXT="main" FOLDED="true" ID="ID_1756888414" CREATED="1370196341136" MODIFIED="1370196341136">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="main"/>
<node TEXT="Previous map" ID="ID_1173271268" CREATED="1370196341136" MODIFIED="1370196341136">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="NavigationPreviousMapAction"/>
</node>
<node TEXT="Next map" ID="ID_1488201699" CREATED="1370196341136" MODIFIED="1370196341136">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="NavigationNextMapAction"/>
</node>
</node>
<node TEXT="zoom" FOLDED="true" ID="ID_287085157" CREATED="1370196341137" MODIFIED="1370196341137">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="menu_key" VALUE="main_toolbar_zoom"/>
<attribute NAME="name" VALUE="zoom"/>
<node TEXT="Center" ID="ID_1136675678" CREATED="1370196341137" MODIFIED="1370196341137">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="CenterAction"/>
</node>
</node>
<node TEXT="open" FOLDED="true" ID="ID_104199953" CREATED="1370196341137" MODIFIED="1370196341137">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="open"/>
<node TEXT="Print map..." ID="ID_1845284650" CREATED="1370196341137" MODIFIED="1370196341137">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="PrintAction"/>
</node>
<node TEXT="Close current map" ID="ID_664205456" CREATED="1370196341137" MODIFIED="1370196341137">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="CloseAction"/>
</node>
</node>
</node>
</node>
</map>
