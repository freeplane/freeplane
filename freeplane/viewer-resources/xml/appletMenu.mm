<map version="freeplane 1.3.0">
<!--To view this file, download free mind mapping software Freeplane from http://freeplane.sourceforge.net -->
<attribute_registry SHOW_ATTRIBUTES="hide"/>
<node TEXT="Applet Menu Structure" FOLDED="false" ID="ID_1106295222" CREATED="1370289583583" MODIFIED="1370291073656"><hook NAME="MapStyle">
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
<node TEXT="menu_bar" POSITION="right" ID="ID_759195298" CREATED="1370289583583" MODIFIED="1370289583583">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="menu_bar"/>
<node TEXT="File" ID="ID_1648832089" CREATED="1370289583583" MODIFIED="1370333888232">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="file"/>
<attribute NAME="name_ref" VALUE="file"/>
<node TEXT="Most recent maps" ID="ID_626198496" CREATED="1370289583584" MODIFIED="1370336935508">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="menu_key" VALUE="main_menu_most_recent_files"/>
<attribute NAME="name" VALUE="last"/>
<attribute NAME="name_ref" VALUE="most_recent_files"/>
</node>
<node TEXT="Print setup ..." ID="ID_1968870430" CREATED="1370289583585" MODIFIED="1370333934054">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="PageAction"/>
</node>
<node TEXT="Print Preview ..." ID="ID_56086467" CREATED="1370289583585" MODIFIED="1370291281038">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="PrintPreviewAction"/>
</node>
<node TEXT="Print map ..." ID="ID_1129839241" CREATED="1370289583585" MODIFIED="1370291290343">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="control P"/>
<attribute NAME="action" VALUE="PrintAction"/>
</node>
<node TEXT="Map statistics ..." ID="ID_494684784" CREATED="1370289583586" MODIFIED="1370333528006">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="FilePropertiesAction"/>
</node>
</node>
<node TEXT="Edit" FOLDED="true" ID="ID_1462659131" CREATED="1370289583586" MODIFIED="1370333890040">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="edit"/>
<attribute NAME="name_ref" VALUE="edit"/>
<node TEXT="Node extensions" FOLDED="true" ID="ID_887328759" CREATED="1370289583586" MODIFIED="1370333973733">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="menu_extensions"/>
<attribute NAME="name_ref" VALUE="menu_extensions"/>
<node TEXT="Minimize node" ID="ID_1168314005" CREATED="1370289583587" MODIFIED="1370334139419">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SetShortenerStateAction"/>
</node>
</node>
<node TEXT="Copy" FOLDED="true" ID="ID_902816050" CREATED="1370289583587" MODIFIED="1370333985493">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="menu_copy"/>
<attribute NAME="name_ref" VALUE="menu_copy"/>
<node TEXT="Copy" ID="ID_1198325064" CREATED="1370289583587" MODIFIED="1370334075604">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="control C"/>
<attribute NAME="action" VALUE="CopyAction"/>
</node>
<node TEXT="Copy node (single)" ID="ID_478872572" CREATED="1370289583588" MODIFIED="1370334071436">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="control shift C"/>
<attribute NAME="action" VALUE="CopySingleAction"/>
</node>
</node>
<node TEXT="Find" FOLDED="true" ID="ID_1276327828" CREATED="1370289583588" MODIFIED="1370333987149">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="find"/>
<node TEXT="Find ..." ID="ID_193336124" CREATED="1370289583588" MODIFIED="1370334035117">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="control F"/>
<attribute NAME="action" VALUE="FindAction"/>
</node>
<node TEXT="Find next" ID="ID_1275209872" CREATED="1370289583589" MODIFIED="1370334038108">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="control G"/>
<attribute NAME="action" VALUE="QuickFindAction.FORWARD"/>
</node>
<node TEXT="Find Previous" ID="ID_1310362676" CREATED="1370289583589" MODIFIED="1370334042364">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="QuickFindAction.BACK"/>
</node>
</node>
</node>
<node TEXT="View" FOLDED="true" ID="ID_1415081604" CREATED="1370289583590" MODIFIED="1370333892623">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="view"/>
<attribute NAME="name_ref" VALUE="menu_view"/>
<node TEXT="Menu_Toolbar_Panel" FOLDED="true" ID="ID_1602418376" CREATED="1370289583590" MODIFIED="1370289583590">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="Menu_Toolbar_Panel"/>
<node TEXT="toolbars" FOLDED="true" ID="ID_441424651" CREATED="1370289583590" MODIFIED="1370289583590">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="toolbars"/>
<node TEXT="Toolbars" FOLDED="true" ID="ID_1247347160" CREATED="1370289583590" MODIFIED="1370334647998">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="toolbars"/>
<attribute NAME="name_ref" VALUE="menu_toolbars"/>
<node TEXT="Menubar" ID="ID_1874551934" CREATED="1370289583591" MODIFIED="1370334222427">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ToggleMenubarAction"/>
<attribute NAME="menu_key" VALUE="MB_ToggleMenubarAction"/>
</node>
<node TEXT="Toolbar" ID="ID_1201269776" CREATED="1370289583591" MODIFIED="1370334225090">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ToggleToolbarAction"/>
</node>
<node TEXT="Filter toolbar" ID="ID_723905548" CREATED="1370289583592" MODIFIED="1370334228562">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ShowFilterToolbarAction"/>
</node>
<node TEXT="Scrollbars" ID="ID_741413210" CREATED="1370289583592" MODIFIED="1370334251426">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ToggleScrollbarsAction"/>
</node>
</node>
</node>
</node>
<node TEXT="Zoom" FOLDED="true" ID="ID_116766092" CREATED="1370289583592" MODIFIED="1370334442184">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="zoom"/>
<node TEXT="Zoom in" ID="ID_724096960" CREATED="1370289583592" MODIFIED="1370334260954">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="alt UP"/>
<attribute NAME="action" VALUE="ZoomInAction"/>
</node>
<node TEXT="Zoom out" ID="ID_837409072" CREATED="1370289583593" MODIFIED="1370334265242">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="alt DOWN"/>
<attribute NAME="action" VALUE="ZoomOutAction"/>
</node>
<node TEXT="Center selected node" ID="ID_858490021" CREATED="1370289583593" MODIFIED="1370334297242">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="control alt C"/>
<attribute NAME="action" VALUE="CenterSelectedNodeAction"/>
</node>
</node>
<node TEXT="View settings" ID="ID_1616554070" CREATED="1370289583594" MODIFIED="1370334389161">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="menu_viewmode"/>
<attribute NAME="name_ref" VALUE="menu_viewmode"/>
<node TEXT="Outline view" ID="ID_350704036" CREATED="1370289583594" MODIFIED="1370334433776">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ViewLayoutTypeAction.OUTLINE"/>
</node>
<node TEXT="Rectangular selection" ID="ID_1595795715" CREATED="1370289583594" MODIFIED="1370334418883">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ShowSelectionAsRectangleAction"/>
</node>
</node>
<node TEXT="Tool tips" FOLDED="true" ID="ID_1285587072" CREATED="1370289583595" MODIFIED="1370334461440">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="menu_hoverView"/>
<attribute NAME="name_ref" VALUE="menu_hoverView"/>
<node TEXT="Hide Details" ID="ID_703894102" CREATED="1370289583595" MODIFIED="1370334508935">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="alt F2"/>
<attribute NAME="action" VALUE="ToggleDetailsAction"/>
</node>
<node TEXT="Display tool tips" ID="ID_1675277738" CREATED="1370289583595" MODIFIED="1370334540039">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SetBooleanPropertyAction.show_node_tooltips"/>
</node>
</node>
<node TEXT="AttributeView" FOLDED="true" ID="ID_780572515" CREATED="1370289583596" MODIFIED="1370289583596">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="AttributeView"/>
<node TEXT="Node attributes" FOLDED="true" ID="ID_24466809" CREATED="1370289583596" MODIFIED="1370334574174">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="menu_displayAttributes"/>
<attribute NAME="name_ref" VALUE="menu_displayAttributes"/>
<node TEXT="ShowSelectedAttributesAction" ID="ID_1788644027" CREATED="1370289583596" MODIFIED="1370289583596">
<attribute NAME="type" VALUE="radio_action"/>
<attribute NAME="action" VALUE="ShowSelectedAttributesAction"/>
</node>
<node TEXT="ShowAllAttributesAction" ID="ID_544285560" CREATED="1370289583597" MODIFIED="1370289583597">
<attribute NAME="type" VALUE="radio_action"/>
<attribute NAME="action" VALUE="ShowAllAttributesAction"/>
</node>
<node TEXT="HideAllAttributesAction" ID="ID_275579352" CREATED="1370289583597" MODIFIED="1370289583597">
<attribute NAME="type" VALUE="radio_action"/>
<attribute NAME="action" VALUE="HideAllAttributesAction"/>
</node>
</node>
</node>
</node>
<node TEXT="Navigate" ID="ID_1728425430" CREATED="1370289583597" MODIFIED="1370333896167">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="navigate"/>
<attribute NAME="name_ref" VALUE="menu_navigate"/>
<node TEXT="navigate" FOLDED="true" ID="ID_1995616024" CREATED="1370289583598" MODIFIED="1370289583598">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="navigate"/>
<node TEXT="folding" FOLDED="true" ID="ID_337305814" CREATED="1370289583598" MODIFIED="1370289583598">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="folding"/>
<node TEXT="(Un) Fold" ID="ID_1726399366" CREATED="1370289583598" MODIFIED="1370335973102">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="SPACE"/>
<attribute NAME="action" VALUE="ToggleFoldedAction"/>
</node>
<node TEXT="Show next child" ID="ID_1016509477" CREATED="1370289583598" MODIFIED="1370335905967">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ShowNextChildAction"/>
<attribute NAME="accelerator" VALUE="shift SPACE"/>
</node>
<node TEXT="(Un)fold children" ID="ID_1901778256" CREATED="1370289583599" MODIFIED="1370335997526">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="control SPACE"/>
<attribute NAME="action" VALUE="ToggleChildrenFoldedAction"/>
</node>
<node TEXT="Unfold one level" ID="ID_308066099" CREATED="1370289583599" MODIFIED="1370335912431">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="alt PAGE_DOWN"/>
<attribute NAME="action" VALUE="UnfoldOneLevelAction"/>
</node>
<node TEXT="Fold one level" ID="ID_954150012" CREATED="1370289583600" MODIFIED="1370335917671">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="alt PAGE_UP"/>
<attribute NAME="action" VALUE="FoldOneLevelAction"/>
</node>
<node TEXT="Unfold all" ID="ID_304617920" CREATED="1370289583600" MODIFIED="1370335945495">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="alt END"/>
<attribute NAME="action" VALUE="UnfoldAllAction"/>
</node>
<node TEXT="Fold all" ID="ID_1487740142" CREATED="1370289583600" MODIFIED="1370335942998">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="alt HOME"/>
<attribute NAME="action" VALUE="FoldAllAction"/>
</node>
</node>
</node>
<node TEXT="Goto root" ID="ID_1147280816" CREATED="1370194798848" MODIFIED="1370194798848">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="MoveToRootAction"/>
<attribute NAME="accelerator" VALUE="ESCAPE"/>
</node>
<node TEXT="Goto node with ID..." ID="ID_1043132007" CREATED="1370194798848" MODIFIED="1370194798848">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="GotoNodeAction"/>
</node>
<node TEXT="Goto previous node" ID="ID_543681743" CREATED="1370194798848" MODIFIED="1370194798848">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="NextNodeAction.BACK"/>
<attribute NAME="accelerator" VALUE="control alt LEFT"/>
</node>
<node TEXT="Goto next node" ID="ID_235562851" CREATED="1370194798848" MODIFIED="1370194798848">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="NextNodeAction.FORWARD"/>
<attribute NAME="accelerator" VALUE="control alt RIGHT"/>
</node>
<node TEXT="Goto previous node (fold)" ID="ID_1575528067" CREATED="1370194798856" MODIFIED="1370194798856">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="NextNodeAction.BACK_N_FOLD"/>
<attribute NAME="accelerator" VALUE="control shift LEFT"/>
</node>
<node TEXT="Goto next node (fold)" ID="ID_1170003934" CREATED="1370194798856" MODIFIED="1370194798856">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="NextNodeAction.FORWARD_N_FOLD"/>
<attribute NAME="accelerator" VALUE="control shift RIGHT"/>
</node>
<node TEXT="Unfold next presentation item" ID="ID_1396867085" CREATED="1370194798856" MODIFIED="1370194798856">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="NextPresentationItemAction"/>
<attribute NAME="accelerator" VALUE="control shift SPACE"/>
</node>
<node TEXT="links" ID="ID_910745337" CREATED="1370289583602" MODIFIED="1370289583602">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="links"/>
<node TEXT="Follow" ID="ID_66928144" CREATED="1370289583602" MODIFIED="1370333644546">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="accelerator" VALUE="control ENTER"/>
<attribute NAME="action" VALUE="FollowLinkAction"/>
</node>
</node>
</node>
<node TEXT="Filter" FOLDED="true" ID="ID_1362111096" CREATED="1370289583602" MODIFIED="1370333900246">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="filter"/>
<attribute NAME="name_ref" VALUE="menu_filter"/>
<node TEXT="Filter" FOLDED="true" ID="ID_977479815" CREATED="1370289583603" MODIFIED="1370289583603">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="Filter"/>
<node TEXT="DoFilter" FOLDED="true" ID="ID_486634156" CREATED="1370289583603" MODIFIED="1370289583603">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="DoFilter"/>
<node TEXT="Undo filter action" ID="ID_1060614745" CREATED="1370194798869" MODIFIED="1370194798869">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="UndoFilterAction"/>
</node>
<node TEXT="Redo filter action" ID="ID_647411361" CREATED="1370194798869" MODIFIED="1370194798869">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="RedoFilterAction"/>
</node>
<node TEXT="Reapply filter action" ID="ID_1390558533" CREATED="1370194798870" MODIFIED="1370194798870">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ReapplyFilterAction"/>
</node>
<node TEXT="Quick filter" ID="ID_1967496761" CREATED="1370194798870" MODIFIED="1370194798870">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="QuickFilterAction"/>
</node>
<node TEXT="Filter selected nodes" ID="ID_630413645" CREATED="1370194798870" MODIFIED="1370194798870">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ApplySelectedViewConditionAction"/>
</node>
<node TEXT="Select all matching nodes" ID="ID_1321944413" CREATED="1370194798870" MODIFIED="1370194798870">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="QuickFindAllAction"/>
</node>
<node TEXT="No filtering" ID="ID_851198975" CREATED="1370194798874" MODIFIED="1370194798874">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ApplyNoFilteringAction"/>
</node>
<node TEXT="Compose filter" ID="ID_1388523208" CREATED="1370194798874" MODIFIED="1370194798874">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="EditFilterAction"/>
</node>
</node>
<node TEXT="FilterCondition" FOLDED="true" ID="ID_1499552466" CREATED="1370289583605" MODIFIED="1370289583605">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="FilterCondition"/>
<node TEXT="Applies to filtered nodes" ID="ID_969015686" CREATED="1370194798874" MODIFIED="1370194798874">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ApplyToVisibleAction"/>
</node>
<node TEXT="Show ancestors" ID="ID_1702252453" CREATED="1370194798874" MODIFIED="1370194798874">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ShowAncestorsAction"/>
</node>
<node TEXT="Show descendants" ID="ID_1364556615" CREATED="1370194798874" MODIFIED="1370194798874">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ShowDescendantsAction"/>
</node>
</node>
<node TEXT="Find" ID="ID_1686011100" CREATED="1370289583605" MODIFIED="1370289583605">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="Find"/>
<node TEXT="Find previous" ID="ID_1368935799" CREATED="1370194798874" MODIFIED="1370194798874">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="QuickFindAction.BACK"/>
</node>
<node TEXT="Find next" ID="ID_1001632153" CREATED="1370194798882" MODIFIED="1370194798882">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="QuickFindAction.FORWARD"/>
</node>
</node>
</node>
</node>
<node TEXT="Extras" FOLDED="true" ID="ID_1413123056" CREATED="1370289583606" MODIFIED="1370333902382">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="extras"/>
<attribute NAME="name_ref" VALUE="menu_extras"/>
<node TEXT="Encryption" FOLDED="true" ID="ID_87412073" CREATED="1370289583606" MODIFIED="1370336311883">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="encryption"/>
<attribute NAME="name_ref" VALUE="menu_encryption"/>
<node TEXT="EnterPassword" ID="ID_868800887" CREATED="1370289583606" MODIFIED="1370289583606">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="EnterPassword"/>
</node>
</node>
</node>
<node TEXT="Mindmaps" FOLDED="true" ID="ID_1538052925" CREATED="1370289583607" MODIFIED="1370333904990">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="mindmaps"/>
<attribute NAME="name_ref" VALUE="mindmaps"/>
<node TEXT="modes" ID="ID_248381967" CREATED="1370289583607" MODIFIED="1370289583607">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="menu_key" VALUE="main_menu_modes"/>
<attribute NAME="name" VALUE="modes"/>
</node>
<node TEXT="navigate" ID="ID_539139568" CREATED="1370289583607" MODIFIED="1370289583607">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="menu_key" VALUE="main_menu_navigate_maps"/>
<attribute NAME="name" VALUE="navigate"/>
</node>
<node TEXT="mindmaps" ID="ID_402320404" CREATED="1370289583607" MODIFIED="1370289583607">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="menu_key" VALUE="main_menu_mindmaps"/>
<attribute NAME="name" VALUE="mindmaps"/>
</node>
</node>
<node TEXT="Help" FOLDED="true" ID="ID_1648885962" CREATED="1370289583608" MODIFIED="1370333907014">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="help"/>
<attribute NAME="name_ref" VALUE="help"/>
<node TEXT="Web resources" ID="ID_791086589" CREATED="1370289583608" MODIFIED="1370289583608">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="Web resources"/>
<node TEXT="Freeplane&apos;s Homepage" ID="ID_369316306" CREATED="1370194798906" MODIFIED="1370194798906">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="OpenFreeplaneSiteAction"/>
</node>
</node>
<node TEXT="legacy" ID="ID_914632314" CREATED="1370289583608" MODIFIED="1370289583608">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="legacy"/>
<node TEXT="About" ID="ID_1821331741" CREATED="1370289583608" MODIFIED="1370336344850">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="AboutAction"/>
<attribute NAME="menu_key" VALUE="MB_AboutAction"/>
</node>
</node>
</node>
</node>
<node TEXT="map_popup" FOLDED="true" POSITION="right" ID="ID_483779417" CREATED="1370289583609" MODIFIED="1370289583609">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="map_popup"/>
<node TEXT="mindmaps" ID="ID_1996950727" CREATED="1370289583609" MODIFIED="1370289583609">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="mindmaps"/>
<attribute NAME="menu_key" VALUE="popup_menu_mindmaps"/>
</node>
<node TEXT="---" ID="ID_230028625" CREATED="1370289583609" MODIFIED="1370289583609">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Menubar" ID="ID_1384686201" CREATED="1370194798915" MODIFIED="1370194798915">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ToggleMenubarAction"/>
<attribute NAME="menu_key" VALUE="MP_ToggleMenubarAction"/>
</node>
<node TEXT="Toolbar" ID="ID_1888013842" CREATED="1370194798915" MODIFIED="1370194798915">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ToggleToolbarAction"/>
</node>
<node TEXT="Filter toolbar" ID="ID_1317991825" CREATED="1370194798915" MODIFIED="1370194798915">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ShowFilterToolbarAction"/>
</node>
<node TEXT="Scrollbars" ID="ID_601963475" CREATED="1370194798919" MODIFIED="1370194798919">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ToggleScrollbarsAction"/>
</node>
<node TEXT="Outline view" ID="ID_349914674" CREATED="1370194798919" MODIFIED="1370194798919">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ViewLayoutTypeAction.OUTLINE"/>
</node>
<node TEXT="Center selected node" ID="ID_223012438" CREATED="1370194798919" MODIFIED="1370194798919">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="CenterSelectedNodeAction"/>
</node>
<node TEXT="Goto root" ID="ID_698685529" CREATED="1370194798919" MODIFIED="1370194798919">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="MoveToRootAction"/>
<attribute NAME="accelerator" VALUE="ESCAPE"/>
</node>
<node TEXT="Goto node with ID..." ID="ID_250613370" CREATED="1370194798919" MODIFIED="1370194798919">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="GotoNodeAction"/>
</node>
</node>
<node TEXT="node_popup" FOLDED="true" POSITION="right" ID="ID_1432012321" CREATED="1370289583611" MODIFIED="1370289583611">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="node_popup"/>
<node TEXT="Minimize node" ID="ID_1916528420" CREATED="1370289583611" MODIFIED="1370336573232">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SetShortenerStateAction"/>
</node>
<node TEXT="Enter Password" ID="ID_129053134" CREATED="1370289583611" MODIFIED="1370336510664">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="EnterPassword"/>
</node>
<node TEXT="Copy" ID="ID_1488306063" CREATED="1370194798933" MODIFIED="1370194798933">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="CopyAction"/>
<attribute NAME="accelerator" VALUE="control C"/>
</node>
<node TEXT="Copy node (single)" ID="ID_1131152129" CREATED="1370194798934" MODIFIED="1370194798934">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="CopySingleAction"/>
<attribute NAME="accelerator" VALUE="control shift C"/>
</node>
</node>
<node TEXT="main_toolbar" FOLDED="true" POSITION="right" ID="ID_1205690152" CREATED="1370289583612" MODIFIED="1370289583612">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="main_toolbar"/>
<node TEXT="zoom" ID="ID_1599643298" CREATED="1370289583612" MODIFIED="1370289583612">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="menu_key" VALUE="main_toolbar_zoom"/>
<attribute NAME="name" VALUE="zoom"/>
</node>
<node TEXT="open" FOLDED="true" ID="ID_1362981179" CREATED="1370289583612" MODIFIED="1370289583612">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="open"/>
<node TEXT="Print map..." ID="ID_58065896" CREATED="1370194798935" MODIFIED="1370194798935">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="PrintAction"/>
</node>
<node TEXT="Print" ID="ID_387336595" CREATED="1370289583613" MODIFIED="1370336640535">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="PrintDirectAction"/>
</node>
</node>
<node TEXT="paste" FOLDED="true" ID="ID_637995384" CREATED="1370289583613" MODIFIED="1370289583613">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="paste"/>
<node TEXT="Copy" ID="ID_891714808" CREATED="1370289583613" MODIFIED="1370336644183">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="CopyAction"/>
</node>
</node>
<node TEXT="update" ID="ID_1479842637" CREATED="1370289583613" MODIFIED="1370289583613">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="update"/>
<attribute NAME="menu_key" VALUE="main_toolbar_update"/>
</node>
<node TEXT="zoom" ID="ID_361514422" CREATED="1370289583614" MODIFIED="1370289583614">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="zoom"/>
<attribute NAME="menu_key" VALUE="main_toolbar_zoom"/>
</node>
<node TEXT="folding" FOLDED="true" ID="ID_67493551" CREATED="1370194798943" MODIFIED="1370194798943">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="folding"/>
<node TEXT="Unfold one level" ID="ID_1227777430" CREATED="1370194798943" MODIFIED="1370194798943">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="UnfoldOneLevelAction"/>
</node>
<node TEXT="Fold one level" ID="ID_1181360534" CREATED="1370194798943" MODIFIED="1370194798943">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="FoldOneLevelAction"/>
</node>
<node TEXT="Unfold all" ID="ID_1631347354" CREATED="1370194798943" MODIFIED="1370194798943">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="UnfoldAllAction"/>
</node>
<node TEXT="Fold all" ID="ID_1172630625" CREATED="1370194798943" MODIFIED="1370194798943">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="FoldAllAction"/>
</node>
</node>
<node TEXT="find" FOLDED="true" ID="ID_1981407568" CREATED="1370194798943" MODIFIED="1370194798943">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="find"/>
<node TEXT="Find..." ID="ID_496291044" CREATED="1370194798943" MODIFIED="1370194798943">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="FindAction"/>
</node>
<node TEXT="Find next" ID="ID_244700007" CREATED="1370194798943" MODIFIED="1370194798943">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="QuickFindAction.FORWARD"/>
</node>
<node TEXT="Find previous" ID="ID_1464814215" CREATED="1370194798943" MODIFIED="1370194798943">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="QuickFindAction.BACK"/>
</node>
</node>
<node TEXT="EnterPassword" ID="ID_400764611" CREATED="1370289583615" MODIFIED="1370289583615">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="EnterPassword"/>
</node>
</node>
</node>
</map>
