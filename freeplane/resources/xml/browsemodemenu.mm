<map version="freeplane 1.3.0">
<!--To view this file, download free mind mapping software Freeplane from http://freeplane.sourceforge.net -->
<attribute_registry SHOW_ATTRIBUTES="hide"/>
<node TEXT="Menu for Browser Mode" FOLDED="false" ID="ID_1676259642" CREATED="1370343428335" MODIFIED="1370343443279"><hook NAME="MapStyle">
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
<node TEXT="menu_bar" POSITION="right" ID="ID_951169932" CREATED="1370343428336" MODIFIED="1370343428336">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="menu_bar"/>
<node TEXT="File" FOLDED="true" ID="ID_1673593376" CREATED="1370343428351" MODIFIED="1370343566796">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="file"/>
<attribute NAME="name_ref" VALUE="file"/>
<node TEXT="Most recent maps" ID="ID_1625697829" CREATED="1370343428351" MODIFIED="1370343562651">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="menu_key" VALUE="main_menu_most_recent_files"/>
<attribute NAME="name" VALUE="last"/>
<attribute NAME="name_ref" VALUE="most_recent_files"/>
</node>
<node TEXT="PageAction" ID="ID_432644535" CREATED="1370343428354" MODIFIED="1370343428354">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="PageAction"/>
</node>
<node TEXT="Print preview..." ID="ID_1392946864" CREATED="1370343428355" MODIFIED="1370343512188">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="PrintPreviewAction"/>
</node>
<node TEXT="Print map ..." ID="ID_938124558" CREATED="1370343428355" MODIFIED="1370343515795">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="control P"/>
<attribute NAME="action" VALUE="PrintAction"/>
</node>
<node TEXT="Map statistics..." ID="ID_78989943" CREATED="1370343428355" MODIFIED="1370343530852">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="FilePropertiesAction"/>
</node>
<node TEXT="Close current map" ID="ID_1468657400" CREATED="1370343428355" MODIFIED="1370343544723">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="control W"/>
<attribute NAME="action" VALUE="CloseAction"/>
</node>
<node TEXT="Quit Freeplane" ID="ID_306381165" CREATED="1370343428355" MODIFIED="1370343549339">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="control Q"/>
<attribute NAME="action" VALUE="QuitAction"/>
<attribute NAME="menu_key" VALUE="MB_QuitAction"/>
</node>
</node>
<node TEXT="Edit" FOLDED="true" ID="ID_874402336" CREATED="1370343428356" MODIFIED="1370343568635">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="edit"/>
<attribute NAME="name_ref" VALUE="edit"/>
<node TEXT="menu_extensions" FOLDED="true" ID="ID_1987963521" CREATED="1370343428356" MODIFIED="1370343428356">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="menu_extensions"/>
<attribute NAME="name_ref" VALUE="menu_extensions"/>
<node TEXT="Minimize node" ID="ID_218334281" CREATED="1370343428356" MODIFIED="1370343723881">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SetShortenerStateAction"/>
</node>
</node>
<node TEXT="menu_copy" FOLDED="true" ID="ID_1068748806" CREATED="1370343428356" MODIFIED="1370343428356">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="menu_copy"/>
<attribute NAME="name_ref" VALUE="menu_copy"/>
<node TEXT="Copy" ID="ID_693624274" CREATED="1370343428356" MODIFIED="1370343608172">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="control C"/>
<attribute NAME="action" VALUE="CopyAction"/>
</node>
<node TEXT="Copy node (single)" ID="ID_1861667072" CREATED="1370343428357" MODIFIED="1370343627458">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="control shift C"/>
<attribute NAME="action" VALUE="CopySingleAction"/>
</node>
</node>
<node TEXT="find" FOLDED="true" ID="ID_1401379804" CREATED="1370343428358" MODIFIED="1370343428358">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="find"/>
<node TEXT="Find ..." ID="ID_577962583" CREATED="1370343428360" MODIFIED="1370343594883">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="control G"/>
<attribute NAME="action" VALUE="FindAction"/>
</node>
<node TEXT="Find next" ID="ID_1130543161" CREATED="1370343428360" MODIFIED="1370343598026">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="control shift G"/>
<attribute NAME="action" VALUE="QuickFindAction.FORWARD"/>
</node>
<node TEXT="Find previous" ID="ID_262166590" CREATED="1370343428360" MODIFIED="1370343603690">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="QuickFindAction.BACK"/>
</node>
</node>
</node>
<node TEXT="View" FOLDED="true" ID="ID_1135995959" CREATED="1370343428360" MODIFIED="1370343735457">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="view"/>
<attribute NAME="name_ref" VALUE="menu_view"/>
<node TEXT="Menu_Toolbar_Panel" FOLDED="true" ID="ID_405676964" CREATED="1370343428361" MODIFIED="1370343428361">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="Menu_Toolbar_Panel"/>
<node TEXT="toolbars" FOLDED="true" ID="ID_268271173" CREATED="1370343428361" MODIFIED="1370343428361">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="toolbars"/>
<node TEXT="toolbars" FOLDED="true" ID="ID_608640070" CREATED="1370343428361" MODIFIED="1370343428361">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="toolbars"/>
<attribute NAME="name_ref" VALUE="menu_toolbars"/>
<node TEXT="ToggleMenubarAction" ID="ID_1490668198" CREATED="1370343428362" MODIFIED="1370343428362">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ToggleMenubarAction"/>
<attribute NAME="menu_key" VALUE="MB_ToggleMenubarAction"/>
</node>
<node TEXT="ToggleToolbarAction" ID="ID_1764986225" CREATED="1370343428362" MODIFIED="1370343428362">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ToggleToolbarAction"/>
</node>
<node TEXT="ShowFilterToolbarAction" ID="ID_703403162" CREATED="1370343428362" MODIFIED="1370343428362">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ShowFilterToolbarAction"/>
<attribute NAME="accelerator" VALUE="control F"/>
</node>
<node TEXT="ToggleScrollbarsAction" ID="ID_294248280" CREATED="1370343428362" MODIFIED="1370343428362">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ToggleScrollbarsAction"/>
</node>
</node>
</node>
</node>
<node TEXT="Zoom" FOLDED="true" ID="ID_1940989441" CREATED="1370343428362" MODIFIED="1370343746953">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="zoom"/>
<node TEXT="Zoom in" ID="ID_1716346239" CREATED="1370343428362" MODIFIED="1370343750465">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="alt UP"/>
<attribute NAME="action" VALUE="ZoomInAction"/>
</node>
<node TEXT="Zoom out" ID="ID_1145504960" CREATED="1370343428362" MODIFIED="1370343753785">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="alt DOWN"/>
<attribute NAME="action" VALUE="ZoomOutAction"/>
</node>
<node TEXT="Center selected node" ID="ID_262109607" CREATED="1370343428363" MODIFIED="1370343790449">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="control alt C"/>
<attribute NAME="action" VALUE="CenterSelectedNodeAction"/>
</node>
</node>
<node TEXT="View settings" FOLDED="true" ID="ID_1183206505" CREATED="1370343428363" MODIFIED="1370343822224">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="menu_viewmode"/>
<attribute NAME="name_ref" VALUE="menu_viewmode"/>
<node TEXT="Outline view" ID="ID_235543535" CREATED="1370343428363" MODIFIED="1370343844864">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ViewLayoutTypeAction.OUTLINE"/>
</node>
<node TEXT="Rectangular selection" ID="ID_598640103" CREATED="1370343428363" MODIFIED="1370343833768">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ShowSelectionAsRectangleAction"/>
</node>
</node>
<node TEXT="Tool tips" FOLDED="true" ID="ID_845817461" CREATED="1370343428363" MODIFIED="1370343868904">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="menu_hoverView"/>
<attribute NAME="name_ref" VALUE="menu_hoverView"/>
<node TEXT="Hide details" ID="ID_1883354573" CREATED="1370343428363" MODIFIED="1370343947679">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="alt F2"/>
<attribute NAME="action" VALUE="ToggleDetailsAction"/>
</node>
<node TEXT="Display tool tips" ID="ID_350251826" CREATED="1370343428364" MODIFIED="1370343900271">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SetBooleanPropertyAction.show_node_tooltips"/>
</node>
</node>
<node TEXT="AttributeView" FOLDED="true" ID="ID_1304515396" CREATED="1370343428364" MODIFIED="1370343428364">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="AttributeView"/>
<node TEXT="menu_displayAttributes" FOLDED="true" ID="ID_1602004789" CREATED="1370343428364" MODIFIED="1370343428364">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="menu_displayAttributes"/>
<attribute NAME="name_ref" VALUE="menu_displayAttributes"/>
<node TEXT="Show selected attributes" ID="ID_1742840825" CREATED="1370343428365" MODIFIED="1370343980758">
<attribute NAME="type" VALUE="radio_action"/>
<attribute NAME="action" VALUE="ShowSelectedAttributesAction"/>
</node>
<node TEXT="Show all attributes" ID="ID_764477884" CREATED="1370343428365" MODIFIED="1370343992262">
<attribute NAME="type" VALUE="radio_action"/>
<attribute NAME="action" VALUE="ShowAllAttributesAction"/>
</node>
<node TEXT="Hide all attributes" ID="ID_104831214" CREATED="1370343428365" MODIFIED="1370343997870">
<attribute NAME="type" VALUE="radio_action"/>
<attribute NAME="action" VALUE="HideAllAttributesAction"/>
</node>
</node>
</node>
</node>
<node TEXT="Navigate" FOLDED="true" ID="ID_69427177" CREATED="1370343428365" MODIFIED="1370344058757">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="navigate"/>
<attribute NAME="name_ref" VALUE="menu_navigate"/>
<node TEXT="navigate" FOLDED="true" ID="ID_255652186" CREATED="1370343428366" MODIFIED="1370343428366">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="navigate"/>
<node TEXT="Previous map" ID="ID_1663848870" CREATED="1370343428366" MODIFIED="1370344222196">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="control shift TAB"/>
<attribute NAME="action" VALUE="NavigationPreviousMapAction"/>
</node>
<node TEXT="Next map" ID="ID_346762235" CREATED="1370343428366" MODIFIED="1370344229196">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="control TAB"/>
<attribute NAME="action" VALUE="NavigationNextMapAction"/>
</node>
<node TEXT="folding" FOLDED="true" ID="ID_1132523613" CREATED="1370343428366" MODIFIED="1370343428366">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="folding"/>
<node TEXT="(Un)fold" ID="ID_1869882931" CREATED="1370343428366" MODIFIED="1370344256924">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="SPACE"/>
<attribute NAME="action" VALUE="ToggleFoldedAction"/>
</node>
<node TEXT="Show next child" ID="ID_1818160300" CREATED="1370343428366" MODIFIED="1370344261531">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ShowNextChildAction"/>
<attribute NAME="accelerator" VALUE="shift SPACE"/>
</node>
<node TEXT="(Un)fold children" ID="ID_1874570321" CREATED="1370343428367" MODIFIED="1370344269155">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="control SPACE"/>
<attribute NAME="action" VALUE="ToggleChildrenFoldedAction"/>
</node>
<node TEXT="Unfold one level" ID="ID_1284227579" CREATED="1370343428368" MODIFIED="1370344275139">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="alt PAGE_DOWN"/>
<attribute NAME="action" VALUE="UnfoldOneLevelAction"/>
</node>
<node TEXT="Fold one level" ID="ID_156543287" CREATED="1370343428368" MODIFIED="1370344278851">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="alt PAGE_UP"/>
<attribute NAME="action" VALUE="FoldOneLevelAction"/>
</node>
<node TEXT="Unfold all" ID="ID_219851785" CREATED="1370343428368" MODIFIED="1370344283731">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="alt END"/>
<attribute NAME="action" VALUE="UnfoldAllAction"/>
</node>
<node TEXT="Fold all" ID="ID_1819016354" CREATED="1370343428368" MODIFIED="1370344285907">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="alt HOME"/>
<attribute NAME="action" VALUE="FoldAllAction"/>
</node>
</node>
</node>
<node TEXT="Goto root" ID="ID_756806968" CREATED="1370343428368" MODIFIED="1370344293595">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="ESCAPE"/>
<attribute NAME="action" VALUE="MoveToRootAction"/>
</node>
<node TEXT="Goto node with ID..." ID="ID_300368272" CREATED="1370343428369" MODIFIED="1370344105877">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="GotoNodeAction"/>
</node>
<node TEXT="Goto previous node" ID="ID_468261889" CREATED="1370343428369" MODIFIED="1370344113709">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="control alt LEFT"/>
<attribute NAME="action" VALUE="NextNodeAction.BACK"/>
</node>
<node TEXT="Goto next node" ID="ID_1487023447" CREATED="1370343428369" MODIFIED="1370344125774">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="control alt RIGHT"/>
<attribute NAME="action" VALUE="NextNodeAction.FORWARD"/>
</node>
<node TEXT="Goto previous node (fold)" ID="ID_1360825994" CREATED="1370343428369" MODIFIED="1370344142397">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="control alt shift LEFT"/>
<attribute NAME="action" VALUE="NextNodeAction.BACK_N_FOLD"/>
</node>
<node TEXT="Goto next node (fold)" ID="ID_471823393" CREATED="1370343428372" MODIFIED="1370344154925">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="control alt shift RIGHT"/>
<attribute NAME="action" VALUE="NextNodeAction.FORWARD_N_FOLD"/>
</node>
<node TEXT="Unfold next presentation item" ID="ID_70745346" CREATED="1370343428373" MODIFIED="1370344167636">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="NextPresentationItemAction"/>
</node>
<node TEXT="links" FOLDED="true" ID="ID_1317097629" CREATED="1370343428373" MODIFIED="1370343428373">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="links"/>
<node TEXT="Follow link" ID="ID_495119895" CREATED="1370343428373" MODIFIED="1370344199572">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="control ENTER"/>
<attribute NAME="action" VALUE="FollowLinkAction"/>
</node>
</node>
</node>
<node TEXT="Filer" FOLDED="true" ID="ID_645149412" CREATED="1370343428373" MODIFIED="1370344297243">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="filter"/>
<attribute NAME="name_ref" VALUE="menu_filter"/>
<node TEXT="Filter" FOLDED="true" ID="ID_1749253205" CREATED="1370343428373" MODIFIED="1370343428373">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="Filter"/>
<node TEXT="DoFilter" FOLDED="true" ID="ID_1805860247" CREATED="1370343428373" MODIFIED="1370343428373">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="DoFilter"/>
<node TEXT="undo filter" ID="ID_660461567" CREATED="1370343428373" MODIFIED="1370344336642">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="UndoFilterAction"/>
</node>
<node TEXT="Redo filter" ID="ID_1576067187" CREATED="1370343428374" MODIFIED="1370344340493">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="RedoFilterAction"/>
</node>
<node TEXT="Reapply filter" ID="ID_1884847745" CREATED="1370343428374" MODIFIED="1370344352702">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ReapplyFilterAction"/>
</node>
<node TEXT="Quick filter" ID="ID_905319601" CREATED="1370343428374" MODIFIED="1370344361058">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="QuickFilterAction"/>
</node>
<node TEXT="Filter selected nodes" ID="ID_1117812547" CREATED="1370343428374" MODIFIED="1370344371978">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ApplySelectedViewConditionAction"/>
</node>
<node TEXT="Select all matching nodes" ID="ID_1794886050" CREATED="1370343428374" MODIFIED="1370344381538">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="QuickFindAllAction"/>
</node>
<node TEXT="No filtering" ID="ID_1777399100" CREATED="1370343428374" MODIFIED="1370344386098">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ApplyNoFilteringAction"/>
</node>
<node TEXT="Compose filter" ID="ID_1587836692" CREATED="1370343428374" MODIFIED="1370344389354">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="EditFilterAction"/>
</node>
</node>
<node TEXT="FilterCondition" FOLDED="true" ID="ID_434791760" CREATED="1370343428374" MODIFIED="1370343428374">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="FilterCondition"/>
<node TEXT="Applies to filtered nodes" ID="ID_1736398664" CREATED="1370343428374" MODIFIED="1370344453833">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ApplyToVisibleAction"/>
</node>
<node TEXT="Show ancestors" ID="ID_885840655" CREATED="1370343428375" MODIFIED="1370344432177">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ShowAncestorsAction"/>
</node>
<node TEXT="Show descendants" ID="ID_1705804597" CREATED="1370343428375" MODIFIED="1370344445497">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ShowDescendantsAction"/>
</node>
</node>
<node TEXT="Find" FOLDED="true" ID="ID_1867800620" CREATED="1370343428375" MODIFIED="1370343428375">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="Find"/>
<node TEXT="Find previous" ID="ID_1014323743" CREATED="1370343428375" MODIFIED="1370344468713">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="QuickFindAction.BACK"/>
</node>
<node TEXT="Find next" ID="ID_767954565" CREATED="1370343428375" MODIFIED="1370344472385">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="QuickFindAction.FORWARD"/>
</node>
</node>
</node>
</node>
<node TEXT="Extras" FOLDED="true" ID="ID_1169737865" CREATED="1370343428375" MODIFIED="1370344478481">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="extras"/>
<attribute NAME="name_ref" VALUE="menu_extras"/>
<node TEXT="Encryption" FOLDED="true" ID="ID_920254780" CREATED="1370343428375" MODIFIED="1370344481577">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="encryption"/>
<attribute NAME="name_ref" VALUE="menu_encryption"/>
<node TEXT="Enter password" ID="ID_573043393" CREATED="1370343428376" MODIFIED="1370344486696">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="EnterPassword"/>
</node>
</node>
</node>
<node TEXT="Mindmaps" FOLDED="true" ID="ID_951904015" CREATED="1370343428376" MODIFIED="1370344491177">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="mindmaps"/>
<attribute NAME="name_ref" VALUE="mindmaps"/>
<node TEXT="modes" ID="ID_9405523" CREATED="1370343428376" MODIFIED="1370343428376">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="menu_key" VALUE="main_menu_modes"/>
<attribute NAME="name" VALUE="modes"/>
</node>
<node TEXT="navigate" ID="ID_150058962" CREATED="1370343428376" MODIFIED="1370343428376">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="menu_key" VALUE="main_menu_navigate_maps"/>
<attribute NAME="name" VALUE="navigate"/>
</node>
<node TEXT="mindmaps" ID="ID_537066871" CREATED="1370343428376" MODIFIED="1370343428376">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="menu_key" VALUE="main_menu_mindmaps"/>
<attribute NAME="name" VALUE="mindmaps"/>
</node>
</node>
<node TEXT="Help" FOLDED="true" ID="ID_1277139357" CREATED="1370343428376" MODIFIED="1370344504552">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="help"/>
<attribute NAME="name_ref" VALUE="help"/>
<node TEXT="update" FOLDED="true" ID="ID_972374411" CREATED="1370343428377" MODIFIED="1370343428377">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="update"/>
<node TEXT="Check for updates" ID="ID_939449405" CREATED="1370343428377" MODIFIED="1370344538000">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="UpdateCheckAction"/>
</node>
</node>
<node TEXT="Web resources" FOLDED="true" ID="ID_902808063" CREATED="1370343428377" MODIFIED="1370343428377">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="Web resources"/>
<node TEXT="Freeplane&apos;s Homepage" ID="ID_209820242" CREATED="1370343428377" MODIFIED="1370344554240">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="OpenFreeplaneSiteAction"/>
</node>
<node TEXT="Ask for help" ID="ID_1185626007" CREATED="1370343428377" MODIFIED="1370344561072">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="AskForHelp"/>
</node>
<node TEXT="Report a bug" ID="ID_858397206" CREATED="1370343428377" MODIFIED="1370344595975">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ReportBugAction"/>
</node>
<node TEXT="Request a feature" ID="ID_32257838" CREATED="1370343428377" MODIFIED="1370344604559">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="RequestFeatureAction"/>
</node>
</node>
<node TEXT="legacy" FOLDED="true" ID="ID_1396731472" CREATED="1370343428377" MODIFIED="1370343428377">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="legacy"/>
<node TEXT="About" ID="ID_263533215" CREATED="1370343428377" MODIFIED="1370344613175">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="AboutAction"/>
<attribute NAME="menu_key" VALUE="MB_AboutAction"/>
</node>
</node>
<node TEXT="Tutorial" ID="ID_1223417782" CREATED="1370343428378" MODIFIED="1370344623087">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="GettingStartedAction"/>
</node>
<node TEXT="Documentation" ID="ID_1140194712" CREATED="1370343428378" MODIFIED="1370344628423">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="F1"/>
<attribute NAME="action" VALUE="DocumentationAction"/>
</node>
</node>
</node>
<node TEXT="map_popup" FOLDED="true" POSITION="right" ID="ID_1830699756" CREATED="1370343428378" MODIFIED="1370343428378">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="map_popup"/>
<node TEXT="Maps" ID="ID_1158260612" CREATED="1370343428378" MODIFIED="1370344735869">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="mindmaps"/>
<attribute NAME="menu_key" VALUE="popup_menu_mindmaps"/>
</node>
<node TEXT="---" ID="ID_1371257319" CREATED="1370343428378" MODIFIED="1370343428378">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Menubar" ID="ID_1186446429" CREATED="1370343428378" MODIFIED="1370344660934">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ToggleMenubarAction"/>
<attribute NAME="menu_key" VALUE="MP_ToggleMenubarAction"/>
</node>
<node TEXT="Toolbar" ID="ID_1075218314" CREATED="1370343428378" MODIFIED="1370344665674">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ToggleToolbarAction"/>
</node>
<node TEXT="Filter toolbar" ID="ID_1862889769" CREATED="1370343428379" MODIFIED="1370344672982">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ShowFilterToolbarAction"/>
</node>
<node TEXT="F-keys toolbar" ID="ID_377321612" CREATED="1370343428379" MODIFIED="1370344679806">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ToggleScrollbarsAction"/>
</node>
<node TEXT="Outline view" ID="ID_1183283621" CREATED="1370343428379" MODIFIED="1370344702254">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ViewLayoutTypeAction.OUTLINE"/>
</node>
<node TEXT="Presentation mode" ID="ID_1313996140" CREATED="1370343428380" MODIFIED="1370344698574">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SetBooleanPropertyAction.presentation_mode"/>
</node>
<node TEXT="Center selected node" ID="ID_587549407" CREATED="1370343428380" MODIFIED="1370344711510">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="CenterSelectedNodeAction"/>
</node>
<node TEXT="Goto root" ID="ID_1278328096" CREATED="1370343428380" MODIFIED="1370344715510">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="ESCAPE"/>
<attribute NAME="action" VALUE="MoveToRootAction"/>
</node>
<node TEXT="Goto node with ID..." ID="ID_1367376258" CREATED="1370343428380" MODIFIED="1370344727462">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="GotoNodeAction"/>
</node>
</node>
<node TEXT="node_popup" FOLDED="true" POSITION="right" ID="ID_1003882161" CREATED="1370343428380" MODIFIED="1370343428380">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="node_popup"/>
<node TEXT="Minimize node" ID="ID_1028056187" CREATED="1370343428381" MODIFIED="1370344795333">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SetShortenerStateAction"/>
</node>
<node TEXT="Enter password" ID="ID_1446480553" CREATED="1370343428381" MODIFIED="1370344779084">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="EnterPassword"/>
</node>
<node TEXT="Copy" ID="ID_765323288" CREATED="1370343428382" MODIFIED="1370344765589">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="control C"/>
<attribute NAME="action" VALUE="CopyAction"/>
</node>
<node TEXT="Copy node (single)" ID="ID_1110562737" CREATED="1370343428382" MODIFIED="1370344775485">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="control shift C"/>
<attribute NAME="action" VALUE="CopySingleAction"/>
</node>
</node>
<node TEXT="main_toolbar" FOLDED="true" POSITION="right" ID="ID_494709864" CREATED="1370343428382" MODIFIED="1370343428382">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="main_toolbar"/>
<node TEXT="main" FOLDED="true" ID="ID_656079159" CREATED="1370343428382" MODIFIED="1370343428382">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="main"/>
<node TEXT="Previous map" ID="ID_1028131476" CREATED="1370343428382" MODIFIED="1370346044199">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="NavigationPreviousMapAction"/>
</node>
<node TEXT="Next map" ID="ID_1119132344" CREATED="1370343428382" MODIFIED="1370346049919">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="NavigationNextMapAction"/>
</node>
</node>
<node TEXT="zoom" ID="ID_1984765408" CREATED="1370343428382" MODIFIED="1370343428382">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="menu_key" VALUE="main_toolbar_zoom"/>
<attribute NAME="name" VALUE="zoom"/>
</node>
<node TEXT="open" FOLDED="true" ID="ID_33481588" CREATED="1370343428382" MODIFIED="1370343428382">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="open"/>
<node TEXT="Print map ..." ID="ID_1199035754" CREATED="1370343428383" MODIFIED="1370346082383">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="PrintAction"/>
</node>
<node TEXT="Close current map" ID="ID_1142186699" CREATED="1370343428383" MODIFIED="1370346077911">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="CloseAction"/>
</node>
</node>
<node TEXT="paste" FOLDED="true" ID="ID_391051952" CREATED="1370343428383" MODIFIED="1370343428383">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="paste"/>
<node TEXT="Copy" ID="ID_1465498341" CREATED="1370343428383" MODIFIED="1370346091191">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="CopyAction"/>
</node>
</node>
<node TEXT="folding" FOLDED="true" ID="ID_209866584" CREATED="1370343428383" MODIFIED="1370343428383">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="folding"/>
<node TEXT="Unfold one level" ID="ID_1052431770" CREATED="1370343428384" MODIFIED="1370346110055">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="UnfoldOneLevelAction"/>
</node>
<node TEXT="Fold one level" ID="ID_1237829019" CREATED="1370343428384" MODIFIED="1370346114543">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="FoldOneLevelAction"/>
</node>
<node TEXT="Unfold all" ID="ID_1481420152" CREATED="1370343428385" MODIFIED="1370346121630">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="UnfoldAllAction"/>
</node>
<node TEXT="Fold all" ID="ID_34182894" CREATED="1370343428385" MODIFIED="1370346127086">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="FoldAllAction"/>
</node>
</node>
<node TEXT="url" ID="ID_266919585" CREATED="1370343428386" MODIFIED="1370343428386">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="url"/>
<attribute NAME="menu_key" VALUE="main_toolbar_url"/>
</node>
</node>
</node>
</map>
