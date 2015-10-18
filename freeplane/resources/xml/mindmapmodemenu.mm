<map version="freeplane 1.3.0">
<!--To view this file, download free mind mapping software Freeplane from http://freeplane.sourceforge.net -->
<attribute_registry SHOW_ATTRIBUTES="hide"/>
<node TEXT="Menu for Mindmap Mode" FOLDED="false" ID="ID_691894585" CREATED="1370194798524" MODIFIED="1370195392596"><hook NAME="MapStyle">
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
<node TEXT="menu_bar" POSITION="right" ID="ID_636805543" CREATED="1370194798524" MODIFIED="1370195023331">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="menu_bar"/>
<node TEXT="File" FOLDED="true" ID="ID_406009783" CREATED="1370194798535" MODIFIED="1370194798535">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="file"/>
<attribute NAME="name_ref" VALUE="file"/>
<node TEXT="New map" ID="ID_1705523326" CREATED="1370194798535" MODIFIED="1370194798535">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="NewMapAction"/>
<attribute NAME="accelerator" VALUE="control N"/>
</node>
<node TEXT="New map from template..." ID="ID_258280788" CREATED="1370194798537" MODIFIED="1370194798537">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="new_map_from_user_templates"/>
</node>
<node TEXT="New protected (encrypted) map ..." ID="ID_1560796784" CREATED="1370194798537" MODIFIED="1370194798537">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="EncryptedMap"/>
</node>
<node TEXT="---" ID="ID_1642777177" CREATED="1370194798545" MODIFIED="1370194798545">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Save map" ID="ID_1536839431" CREATED="1370194798545" MODIFIED="1370194798545">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SaveAction"/>
<attribute NAME="accelerator" VALUE="control S"/>
</node>
<node TEXT="Save map as..." ID="ID_860034402" CREATED="1370194798545" MODIFIED="1370194798545">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SaveAsAction"/>
<attribute NAME="accelerator" VALUE="control shift S"/>
</node>
<node TEXT="Save all opened maps" ID="ID_1453965278" CREATED="1370194798545" MODIFIED="1370194798545">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SaveAll"/>
</node>
<node TEXT="Restore from local history" ID="ID_1303554566" CREATED="1370194798545" MODIFIED="1370194798545">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="RevertAction"/>
</node>
<node TEXT="---" ID="ID_45483510" CREATED="1370194798546" MODIFIED="1370194798546">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Open saved map..." ID="ID_1448431849" CREATED="1370194798546" MODIFIED="1370194798546">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="OpenAction"/>
<attribute NAME="accelerator" VALUE="control O"/>
</node>
<node TEXT="Open map from URL..." ID="ID_1443001755" CREATED="1370194798546" MODIFIED="1370194798546">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="OpenURLMapAction"/>
</node>
<node TEXT="Most recent maps" ID="ID_72482299" CREATED="1370194798546" MODIFIED="1370336921820">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="last"/>
<attribute NAME="name_ref" VALUE="most_recent_files"/>
<attribute NAME="menu_key" VALUE="main_menu_most_recent_files"/>
</node>
<node TEXT="---" ID="ID_1985431728" CREATED="1370194798546" MODIFIED="1370194798546">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Export map..." ID="ID_1722715061" CREATED="1370194798546" MODIFIED="1370194798546">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ExportAction"/>
</node>
<node TEXT="Move branch to new map..." ID="ID_978437039" CREATED="1370194798547" MODIFIED="1370194798547">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ExportBranchAction"/>
<attribute NAME="accelerator" VALUE="alt shift A"/>
</node>
<node TEXT="Import" FOLDED="true" ID="ID_697626933" CREATED="1370194798551" MODIFIED="1370194798551">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="import"/>
<attribute NAME="name_ref" VALUE="menu_file_import"/>
<node TEXT="Branch..." ID="ID_1448767614" CREATED="1370194798551" MODIFIED="1370194798551">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ImportBranchAction"/>
</node>
<node TEXT="Linked branch" ID="ID_1130581947" CREATED="1370194798558" MODIFIED="1370194798558">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ImportLinkedBranchAction"/>
</node>
<node TEXT="Linked branch without root..." ID="ID_1007652649" CREATED="1370194798558" MODIFIED="1370194798558">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ImportLinkedBranchWithoutRootAction"/>
</node>
<node TEXT="---" ID="ID_1324489049" CREATED="1370194798559" MODIFIED="1370194798559">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Explorer favorites..." ID="ID_155149050" CREATED="1370194798559" MODIFIED="1370194798559">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ImportExplorerFavoritesAction"/>
</node>
<node TEXT="Folder structure..." ID="ID_1770054265" CREATED="1370194798559" MODIFIED="1370194798559">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ImportFolderStructureAction"/>
</node>
<node TEXT="MindManager X5 map..." ID="ID_57842411" CREATED="1370194798559" MODIFIED="1370194798559">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ImportMindmanagerFiles"/>
</node>
</node>
<node TEXT="---" ID="ID_1317899899" CREATED="1370194798559" MODIFIED="1370194798559">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Print setup..." ID="ID_115809964" CREATED="1370194798559" MODIFIED="1370194798559">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="PageAction"/>
</node>
<node TEXT="Print preview..." ID="ID_13338748" CREATED="1370194798561" MODIFIED="1370194798561">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="PrintPreviewAction"/>
</node>
<node TEXT="Print map..." ID="ID_374219575" CREATED="1370194798561" MODIFIED="1370194798561">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="PrintAction"/>
<attribute NAME="accelerator" VALUE="control P"/>
</node>
<node TEXT="---" ID="ID_1191657203" CREATED="1370194798561" MODIFIED="1370194798561">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Map statistics..." ID="ID_1855008484" CREATED="1370194798561" MODIFIED="1370194798561">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="FilePropertiesAction"/>
</node>
<node TEXT="---" ID="ID_1934757137" CREATED="1370194798561" MODIFIED="1370194798561">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Close current map" ID="ID_1536850276" CREATED="1370194798566" MODIFIED="1370194798566">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="CloseAction"/>
<attribute NAME="accelerator" VALUE="control W"/>
</node>
<node TEXT="---" ID="ID_944150298" CREATED="1370194798566" MODIFIED="1370194798566">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Quit Freeplane" ID="ID_1881174496" CREATED="1370194798566" MODIFIED="1370194798566">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="QuitAction"/>
<attribute NAME="accelerator" VALUE="control Q"/>
<attribute NAME="menu_key" VALUE="MB_QuitAction"/>
</node>
</node>
<node TEXT="Edit" FOLDED="true" ID="ID_1064514668" CREATED="1370194798567" MODIFIED="1370194798567">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="edit"/>
<attribute NAME="name_ref" VALUE="edit"/>
<node TEXT="New node" FOLDED="true" ID="ID_1392622016" CREATED="1370194798567" MODIFIED="1370194798567">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="menu_newNode"/>
<attribute NAME="name_ref" VALUE="menu_newNode"/>
<node TEXT="New child node" ID="ID_853372580" CREATED="1370194798567" MODIFIED="1370194798567">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="NewChildAction"/>
<attribute NAME="accelerator" VALUE="INSERT"/>
</node>
<node TEXT="New sibling node" ID="ID_1965609973" CREATED="1370194798567" MODIFIED="1370194798567">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="NewSiblingAction"/>
<attribute NAME="accelerator" VALUE="ENTER"/>
</node>
<node TEXT="New previous sibling node" ID="ID_166645385" CREATED="1370194798567" MODIFIED="1370194798567">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="NewPreviousSiblingAction"/>
<attribute NAME="accelerator" VALUE="shift ENTER"/>
</node>
<node TEXT="New parent node" ID="ID_269199803" CREATED="1370194798568" MODIFIED="1370194798568">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="NewParentNode"/>
<attribute NAME="accelerator" VALUE="shift INSERT"/>
</node>
<node TEXT="New free node" ID="ID_1216154833" CREATED="1370194798568" MODIFIED="1370194798568">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="NewFreeNodeAction"/>
</node>
<node TEXT="New summary node (selected nodes)" ID="ID_702337814" CREATED="1370194798568" MODIFIED="1370194798568">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="NewSummaryAction"/>
<attribute NAME="accelerator" VALUE="alt shift INSERT"/>
</node>
</node>
<node TEXT="Node group" FOLDED="true" ID="ID_570938000" CREATED="1370194798568" MODIFIED="1370194798568">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="menu_group"/>
<attribute NAME="name_ref" VALUE="menu_group"/>
<node TEXT="Summary node (begin of group)" ID="ID_1010986763" CREATED="1370194798572" MODIFIED="1370194798572">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="FirstGroupNodeAction"/>
<attribute NAME="accelerator" VALUE="alt shift B"/>
</node>
<node TEXT="Summary node (set/reset)" ID="ID_952297453" CREATED="1370194798573" MODIFIED="1370194798573">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SummaryNodeAction"/>
<attribute NAME="accelerator" VALUE="alt shift S"/>
</node>
<node TEXT="Always unfolded node (set/reset)" ID="ID_1073653247" CREATED="1370194798573" MODIFIED="1370194798573">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="AlwaysUnfoldedNodeAction"/>
</node>
<node TEXT="Add / remove cloud (default)" ID="ID_294484372" CREATED="1370194798573" MODIFIED="1370194798573">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="CloudAction"/>
<attribute NAME="accelerator" VALUE="control shift B"/>
</node>
</node>
<node TEXT="Connect" ID="ID_803645535" CREATED="1370194798573" MODIFIED="1370194798573">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="AddConnectorAction"/>
<attribute NAME="accelerator" VALUE="control L"/>
</node>
<node TEXT="Links" FOLDED="true" ID="ID_1556505118" CREATED="1370194798573" MODIFIED="1370334712365">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="links"/>
<attribute NAME="name_ref" VALUE="menu_links"/>
<node TEXT="Add hyperlink (choose)..." ID="ID_1498143382" CREATED="1370194798574" MODIFIED="1370194798574">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SetLinkByFileChooserAction"/>
<attribute NAME="accelerator" VALUE="control shift K"/>
</node>
<node TEXT="Add or modify hyperlink (type)..." ID="ID_861972359" CREATED="1370194798574" MODIFIED="1370194798574">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SetLinkByTextFieldAction"/>
<attribute NAME="accelerator" VALUE="control K"/>
</node>
<node TEXT="Convert link from within text" ID="ID_1382855387" CREATED="1370194798574" MODIFIED="1370194798574">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ExtractLinkFromTextAction"/>
</node>
<node TEXT="---" ID="ID_1745866315" CREATED="1370194798574" MODIFIED="1370194798574">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Add local hyperlink" ID="ID_133152604" CREATED="1370194798574" MODIFIED="1370194798574">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="AddLocalLinkAction"/>
<attribute NAME="accelerator" VALUE="alt shift L"/>
</node>
<node TEXT="Add hyperlink to menu item..." ID="ID_361535700" CREATED="1370194798574" MODIFIED="1370194798574">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="AddMenuItemLinkAction"/>
<attribute NAME="accelerator" VALUE=""/>
</node>
<node TEXT="---" ID="ID_219304761" CREATED="1370194798575" MODIFIED="1370194798575">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Set link anchor" ID="ID_263691374" CREATED="1370194798575" MODIFIED="1370194798575">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SetLinkAnchorAction"/>
<attribute NAME="accelerator" VALUE=""/>
</node>
<node TEXT="Make link from anchor" ID="ID_1417288354" CREATED="1370194798646" MODIFIED="1370194798646">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="MakeLinkFromAnchorAction"/>
<attribute NAME="accelerator" VALUE=""/>
</node>
<node TEXT="Make link to anchor" ID="ID_1823067975" CREATED="1370194798646" MODIFIED="1370194798646">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="MakeLinkToAnchorAction"/>
<attribute NAME="accelerator" VALUE=""/>
</node>
<node TEXT="Clear link anchor" ID="ID_17511554" CREATED="1370194798646" MODIFIED="1370194798646">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ClearLinkAnchorAction"/>
<attribute NAME="accelerator" VALUE=""/>
</node>
</node>
<node TEXT="---" ID="ID_1120284303" CREATED="1370194798647" MODIFIED="1370194798647">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Remove node" ID="ID_16716029" CREATED="1370194798647" MODIFIED="1370194798647">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="DeleteAction"/>
<attribute NAME="accelerator" VALUE="DELETE"/>
</node>
<node TEXT="---" ID="ID_562002085" CREATED="1370194798647" MODIFIED="1370194798647">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Node core" FOLDED="true" ID="ID_266307790" CREATED="1370194798647" MODIFIED="1370194798647">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="menu_title"/>
<attribute NAME="name_ref" VALUE="menu_title"/>
<node TEXT="Edit node core in-line" ID="ID_607901764" CREATED="1370194798647" MODIFIED="1370194798647">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="EditAction"/>
<attribute NAME="accelerator" VALUE="F2"/>
</node>
<node TEXT="Edit on double click" ID="ID_760123705" CREATED="1370194798664" MODIFIED="1370334775676">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SetBooleanPropertyAction.edit_on_double_click"/>
</node>
<node TEXT="Edit node core in dialog" ID="ID_1835607764" CREATED="1370194798664" MODIFIED="1370194798664">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="EditLongAction"/>
<attribute NAME="accelerator" VALUE="alt ENTER"/>
</node>
<node TEXT="Join nodes" ID="ID_1028352921" CREATED="1370194798664" MODIFIED="1370194798664">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="JoinNodesAction"/>
<attribute NAME="accelerator" VALUE="control J"/>
</node>
<node TEXT="Split node" ID="ID_1163943962" CREATED="1370194798665" MODIFIED="1370194798665">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SplitNode"/>
</node>
<node TEXT="Change revisions background color" ID="ID_1486500406" CREATED="1370194798665" MODIFIED="1370194798665">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="RevisionPluginAction"/>
</node>
<node TEXT="---" ID="ID_99391948" CREATED="1370194798665" MODIFIED="1370194798665">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Image by choice or link..." ID="ID_85819761" CREATED="1370194798665" MODIFIED="1370194798665">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SetImageByFileChooserAction"/>
<attribute NAME="accelerator" VALUE="alt shift K"/>
</node>
</node>
<node TEXT="Icons" FOLDED="true" ID="ID_1377598100" CREATED="1370194798665" MODIFIED="1370334781924">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="icons"/>
<attribute NAME="name_ref" VALUE="menu_iconView"/>
<node TEXT="Icon from table..." ID="ID_1220884861" CREATED="1370194798665" MODIFIED="1370194798665">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="IconSelectionPlugin"/>
<attribute NAME="accelerator" VALUE="control F2"/>
</node>
<node TEXT="Icons" ID="ID_690477551" CREATED="1370194798671" MODIFIED="1370334796068">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="icons"/>
<attribute NAME="name_ref" VALUE="menu_iconByCategory"/>
<node TEXT="---" ID="ID_988568228" CREATED="1370194798672" MODIFIED="1370194798672">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="icons_list" ID="ID_808165381" CREATED="1370194798672" MODIFIED="1370194798672">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="icons_list"/>
<attribute NAME="menu_key" VALUE="main_menu_icons"/>
</node>
</node>
<node TEXT="Progress icon (%)" FOLDED="true" ID="ID_549158740" CREATED="1370194798672" MODIFIED="1370334849539">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="progress"/>
<attribute NAME="name_ref" VALUE="menu_progress"/>
<node TEXT="Progress up" ID="ID_102557355" CREATED="1370194798673" MODIFIED="1370194798673">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="IconProgressIconUpAction"/>
<attribute NAME="accelerator" VALUE="alt LESS"/>
</node>
<node TEXT="Progress down" ID="ID_1904205325" CREATED="1370194798673" MODIFIED="1370194798673">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="IconProgressIconDownAction"/>
<attribute NAME="accelerator" VALUE="alt shift LESS"/>
</node>
<node TEXT="---" ID="ID_341944183" CREATED="1370194798673" MODIFIED="1370194798673">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Extended progress 10%" ID="ID_1112275743" CREATED="1370194798673" MODIFIED="1370194798673">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="IconProgressExtended10Action"/>
</node>
<node TEXT="Extended progress 25%" ID="ID_1606614895" CREATED="1370194798673" MODIFIED="1370194798673">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="IconProgressExtended25Action"/>
</node>
<node TEXT="---" ID="ID_1038107274" CREATED="1370194798673" MODIFIED="1370194798673">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Remove progress" ID="ID_1751494257" CREATED="1370194798681" MODIFIED="1370194798681">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="IconProgressRemoveAction"/>
</node>
</node>
<node TEXT="Show icons hierarchically" ID="ID_1725079351" CREATED="1370194798681" MODIFIED="1370194798681">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="HierarchicalIconsAction"/>
</node>
<node TEXT="Show intersection of child icons" ID="ID_1151473440" CREATED="1370194798681" MODIFIED="1370194798681">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="HierarchicalIcons2Action"/>
</node>
<node TEXT="Remove icons" FOLDED="true" ID="ID_1052253050" CREATED="1370194798681" MODIFIED="1370194798681">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="menu_remove_icons"/>
<node TEXT="Remove first icon" ID="ID_7930582" CREATED="1370194798682" MODIFIED="1370194798682">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="RemoveIcon_0_Action"/>
</node>
<node TEXT="Remove Last Icon" ID="ID_535299053" CREATED="1370194798682" MODIFIED="1370194798682">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="RemoveIconAction"/>
</node>
<node TEXT="Remove all icons" ID="ID_114256700" CREATED="1370194798682" MODIFIED="1370194798682">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="RemoveAllIconsAction"/>
</node>
</node>
</node>
<node TEXT="Node extensions" FOLDED="true" ID="ID_1694240299" CREATED="1370194798682" MODIFIED="1370194798682">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="menu_extensions"/>
<attribute NAME="name_ref" VALUE="menu_extensions"/>
<node TEXT="Edit node details in-line" ID="ID_1646950700" CREATED="1370194798682" MODIFIED="1370194798682">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="EditDetailsAction"/>
</node>
<node TEXT="Edit node details in dialog" ID="ID_1478340195" CREATED="1370194798682" MODIFIED="1370194798682">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="EditDetailsInDialogAction"/>
</node>
<node TEXT="Copy extensions from style node" ID="ID_481032468" CREATED="1370194798682" MODIFIED="1370194798682">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="CopyStyleExtensionsAction"/>
</node>
<node TEXT="Remove node details" ID="ID_904492783" CREATED="1370194798683" MODIFIED="1370194798683">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="DeleteDetailsAction"/>
</node>
<node TEXT="---" ID="ID_1349601166" CREATED="1370194798683" MODIFIED="1370194798683">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Add image..." ID="ID_531292236" CREATED="1370194798683" MODIFIED="1370194798683">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ExternalImageAddAction"/>
</node>
<node TEXT="Change image..." ID="ID_1062097964" CREATED="1370194798683" MODIFIED="1370194798683">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ExternalImageChangeAction"/>
</node>
<node TEXT="Remove image" ID="ID_1797697627" CREATED="1370194798683" MODIFIED="1370194798683">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ExternalImageRemoveAction"/>
</node>
<node TEXT="---" ID="ID_1172032718" CREATED="1370194798683" MODIFIED="1370194798683">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Edit attribute in-line" ID="ID_1208895185" CREATED="1370194798683" MODIFIED="1370194798683">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="EditAttributesAction"/>
<attribute NAME="accelerator" VALUE="alt F9"/>
</node>
<node TEXT="Add attribute in dialog..." ID="ID_812006901" CREATED="1370194798683" MODIFIED="1370194798683">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="attributes_AddAttributeAction"/>
</node>
<node TEXT="Find and replace attributes..." ID="ID_1734577061" CREATED="1370194798684" MODIFIED="1370194798684">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="AssignAttributesAction"/>
</node>
<node TEXT="Copy attributes" ID="ID_388733176" CREATED="1370194798684" MODIFIED="1370194798684">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="CopyAttributes"/>
</node>
<node TEXT="Paste attributes" ID="ID_296247951" CREATED="1370194798684" MODIFIED="1370194798684">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="PasteAttributes"/>
</node>
<node TEXT="Attributes from style" ID="ID_1799464385" CREATED="1370194798684" MODIFIED="1370194798684">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="AddStyleAttributes"/>
</node>
<node TEXT="Remove attribute" FOLDED="true" ID="ID_1170025037" CREATED="1370194798684" MODIFIED="1370194798684">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="menu_removeAttribute"/>
<attribute NAME="name_ref" VALUE="menu_removeAttribute"/>
<node TEXT="Remove first attribute" ID="ID_747672780" CREATED="1370194798684" MODIFIED="1370194798684">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="attributes_RemoveFirstAttributeAction"/>
</node>
<node TEXT="Remove last attribute" ID="ID_1297735599" CREATED="1370194798684" MODIFIED="1370194798684">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="attributes_RemoveLastAttributeAction"/>
</node>
<node TEXT="Remove all attributes" ID="ID_1695452318" CREATED="1370194798684" MODIFIED="1370194798684">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="attributes_RemoveAllAttributesAction"/>
</node>
</node>
<node TEXT="Attribute manager..." ID="ID_781687084" CREATED="1370194798685" MODIFIED="1370194798685">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ShowAttributeDialogAction"/>
</node>
<node TEXT="---" ID="ID_1580492073" CREATED="1370194798685" MODIFIED="1370194798685">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Add LaTeX formula..." ID="ID_192550812" CREATED="1370194798685" MODIFIED="1370194798685">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="LatexInsertLatexAction"/>
<attribute NAME="plugin" VALUE="org.freeplane.plugin.latex"/>
</node>
<node TEXT="Edit LaTeX formula..." ID="ID_204716025" CREATED="1370194798693" MODIFIED="1370194798693">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="LatexEditLatexAction"/>
<attribute NAME="plugin" VALUE="org.freeplane.plugin.latex"/>
</node>
<node TEXT="Remove LaTeX formula" ID="ID_1707193002" CREATED="1370194798693" MODIFIED="1370194798693">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="LatexDeleteLatexAction"/>
<attribute NAME="plugin" VALUE="org.freeplane.plugin.latex"/>
</node>
<node TEXT="---" ID="ID_1286298150" CREATED="1370194798693" MODIFIED="1370194798693">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Add OpenMaps Location..." ID="ID_825193044" CREATED="1370194798693" MODIFIED="1370194798693">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="OpenMapsAddLocation"/>
<attribute NAME="plugin" VALUE="org.freeplane.plugin.openmaps"/>
</node>
<node TEXT="Remove OpenMaps Location" ID="ID_1841735261" CREATED="1370194798693" MODIFIED="1370194798693">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="OpenMapsRemoveLocation"/>
<attribute NAME="plugin" VALUE="org.freeplane.plugin.openmaps"/>
</node>
<node TEXT="View OpenMaps Location..." ID="ID_1681751583" CREATED="1370194798700" MODIFIED="1370194798700">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="OpenMapsViewLocation"/>
<attribute NAME="plugin" VALUE="org.freeplane.plugin.openmaps"/>
</node>
</node>
<node TEXT="Notes" FOLDED="true" ID="ID_847498955" CREATED="1370194798701" MODIFIED="1370334870379">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="notes"/>
<attribute NAME="name_ref" VALUE="menu_notes"/>
<node TEXT="SelectNoteAction">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SelectNoteAction"/>
<attribute NAME="accelerator" VALUE="control LESS"/>
</node>
<node TEXT="Edit note in dialog" ID="ID_1125575353" CREATED="1370194798701" MODIFIED="1370194798701">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="EditNoteInDialogAction"/>
</node>
<node TEXT="Remove note" ID="ID_400007160" CREATED="1370194798701" MODIFIED="1370194798701">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="RemoveNoteAction"/>
</node>
</node>
<node TEXT="---" ID="ID_342700381" CREATED="1370194798701" MODIFIED="1370194798701">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Undo" ID="ID_323454547" CREATED="1370194798701" MODIFIED="1370343489059">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="UndoAction"/>
<attribute NAME="accelerator" VALUE="control Z"/>
</node>
<node TEXT="Redo" ID="ID_1639491869" CREATED="1370194798704" MODIFIED="1370194798704">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="RedoAction"/>
<attribute NAME="accelerator" VALUE="control Y"/>
</node>
<node TEXT="Cut" ID="ID_1390723264" CREATED="1370194798704" MODIFIED="1370194798704">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="CutAction"/>
<attribute NAME="accelerator" VALUE="control X"/>
</node>
<node TEXT="Copy" FOLDED="true" ID="ID_987993701" CREATED="1370194798705" MODIFIED="1370194798705">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="menu_copy"/>
<attribute NAME="name_ref" VALUE="menu_copy"/>
<node TEXT="Copy" ID="ID_1463682071" CREATED="1370194798705" MODIFIED="1370194798705">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="CopyAction"/>
<attribute NAME="accelerator" VALUE="control C"/>
</node>
<node TEXT="Copy node (single)" ID="ID_1987568912" CREATED="1370194798706" MODIFIED="1370194798706">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="CopySingleAction"/>
<attribute NAME="accelerator" VALUE="control shift C"/>
</node>
<node TEXT="Copy node ID" ID="ID_1031247260" CREATED="1370194798706" MODIFIED="1370194798706">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="CopyIDAction"/>
</node>
<node TEXT="Copy node URI" ID="ID_118234000" CREATED="1370194798706" MODIFIED="1370194798706">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="CopyNodeURIAction"/>
</node>
</node>
<node TEXT="Paste" ID="ID_763863977" CREATED="1370194798706" MODIFIED="1370194798706">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="PasteAction"/>
<attribute NAME="accelerator" VALUE="control V"/>
</node>
<node TEXT="Paste as..." ID="ID_953688636" CREATED="1370194798707" MODIFIED="1370194798707">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SelectedPasteAction"/>
</node>
<node TEXT="---" ID="ID_1646346919" CREATED="1370194798707" MODIFIED="1370194798707">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Move and sort" FOLDED="true" ID="ID_926267534" CREATED="1370194798707" MODIFIED="1370194798707">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="menu_moveNode"/>
<attribute NAME="name_ref" VALUE="menu_moveNode"/>
<node TEXT="Move node (Sibling up)" ID="ID_1601300013" CREATED="1370194798707" MODIFIED="1370194798707">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="NodeUpAction"/>
<attribute NAME="accelerator" VALUE="control UP"/>
</node>
<node TEXT="Move node (Sibling down)" ID="ID_1583540115" CREATED="1370194798715" MODIFIED="1370194798715">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="NodeDownAction"/>
<attribute NAME="accelerator" VALUE="control DOWN"/>
</node>
<node TEXT="Sort children" ID="ID_1799547046" CREATED="1370194798715" MODIFIED="1370194798715">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SortNodes"/>
</node>
<node TEXT="Move node (Parents sibling)" ID="ID_1399738932" CREATED="1370194798716" MODIFIED="1370194798716">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ChangeNodeLevelLeftsAction"/>
<attribute NAME="accelerator" VALUE="control LEFT"/>
</node>
<node TEXT="Move node (Siblings child)" ID="ID_973589761" CREATED="1370194798716" MODIFIED="1370194798716">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ChangeNodeLevelRightsAction"/>
<attribute NAME="accelerator" VALUE="control RIGHT"/>
</node>
</node>
<node TEXT="Free positioned node (set/reset)" ID="ID_1170575254" CREATED="1370194798716" MODIFIED="1370194798716">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="FreeNodeAction"/>
</node>
<node TEXT="Reset node position" ID="ID_56951123" CREATED="1370194798716" MODIFIED="1370194798716">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ResetNodeLocationAction"/>
</node>
<node TEXT="---" ID="ID_957163489" CREATED="1370194798716" MODIFIED="1370194798716">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="find" FOLDED="true" ID="ID_1965550790" CREATED="1370194798716" MODIFIED="1370194798716">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="find"/>
<node TEXT="Find..." ID="ID_8920238" CREATED="1370194798716" MODIFIED="1370194798716">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="FindAction"/>
<attribute NAME="accelerator" VALUE="control G"/>
</node>
<node TEXT="Find next" ID="ID_1023154200" CREATED="1370194798716" MODIFIED="1370194798716">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="QuickFindAction.FORWARD"/>
<attribute NAME="accelerator" VALUE="control shift G"/>
</node>
<node TEXT="Find previous" ID="ID_960953165" CREATED="1370194798717" MODIFIED="1370194798717">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="QuickFindAction.BACK"/>
</node>
<node TEXT="Find and replace..." ID="ID_1754045628" CREATED="1370194798717" MODIFIED="1370194798717">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="NodeListAction"/>
<attribute NAME="accelerator" VALUE="control shift F"/>
</node>
<node TEXT="Find and replace in all maps" ID="ID_486648719" CREATED="1370194798717" MODIFIED="1370194798717">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="AllMapsNodeListAction"/>
</node>
</node>
</node>
<node TEXT="View" FOLDED="true" ID="ID_736872434" CREATED="1370194798717" MODIFIED="1370334676453">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="view"/>
<attribute NAME="name_ref" VALUE="menu_view"/>
<node TEXT="New map view" ID="ID_827837477" CREATED="1370194798727" MODIFIED="1370194798727">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="NewMapViewAction"/>
</node>
<node TEXT="Menu_Toolbar_Panel" FOLDED="true" ID="ID_143256340" CREATED="1370194798727" MODIFIED="1370194798727">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="Menu_Toolbar_Panel"/>
<node TEXT="toolbars" FOLDED="true" ID="ID_741387409" CREATED="1370194798727" MODIFIED="1370194798727">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="toolbars"/>
<node TEXT="Toolbars" FOLDED="true" ID="ID_463056530" CREATED="1370194798727" MODIFIED="1370334915930">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="toolbars"/>
<attribute NAME="name_ref" VALUE="menu_toolbars"/>
<node TEXT="Menubar" ID="ID_1214899645" CREATED="1370194798727" MODIFIED="1370194798727">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ToggleMenubarAction"/>
<attribute NAME="menu_key" VALUE="MB_ToggleMenubarAction"/>
</node>
<node TEXT="Toolbar" ID="ID_904536782" CREATED="1370194798727" MODIFIED="1370194798727">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ToggleToolbarAction"/>
</node>
<node TEXT="Filter toolbar" ID="ID_715659174" CREATED="1370194798747" MODIFIED="1370194798747">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ShowFilterToolbarAction"/>
<attribute NAME="accelerator" VALUE="control F"/>
</node>
<node TEXT="F-keys Bar" ID="ID_40253593" CREATED="1370194798747" MODIFIED="1370334926482">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ToggleFBarAction"/>
</node>
<node TEXT="Icons toolbar" ID="ID_382413945" CREATED="1370194798748" MODIFIED="1370194798748">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ToggleLeftToolbarAction"/>
</node>
<node TEXT="Display status line" ID="ID_1911024299" CREATED="1370194798748" MODIFIED="1370194798748">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ToggleStatusAction"/>
</node>
<node TEXT="Scrollbars" ID="ID_878393547" CREATED="1370194798749" MODIFIED="1370194798749">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ToggleScrollbarsAction"/>
</node>
</node>
<node TEXT="Properties panel" ID="ID_759196812" CREATED="1370194798749" MODIFIED="1370194798749">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ShowFormatPanel"/>
</node>
</node>
<node TEXT="---" ID="ID_742463946" CREATED="1370194798749" MODIFIED="1370194798749">
<attribute NAME="type" VALUE="separator"/>
</node>
</node>
<node TEXT="Zoom" FOLDED="true" ID="ID_933007771" CREATED="1370194798749" MODIFIED="1370334933626">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="zoom"/>
<node TEXT="Zoom in" ID="ID_407406083" CREATED="1370194798749" MODIFIED="1370194798749">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ZoomInAction"/>
<attribute NAME="accelerator" VALUE="alt UP"/>
</node>
<node TEXT="Zoom out" ID="ID_857707228" CREATED="1370194798749" MODIFIED="1370194798749">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ZoomOutAction"/>
<attribute NAME="accelerator" VALUE="alt DOWN"/>
</node>
<node TEXT="Zoom to fit to page" ID="ID_265550818" CREATED="1370194798762" MODIFIED="1370194798762">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="FitToPage"/>
</node>
<node TEXT="Center selected node" ID="ID_282591741" CREATED="1370194798762" MODIFIED="1370194798762">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="CenterSelectedNodeAction"/>
<attribute NAME="accelerator" VALUE="control alt C"/>
</node>
<node TEXT="Center selected node" ID="ID_872705376" CREATED="1370194798778" MODIFIED="1370334948650">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SetBooleanPropertyAction.center_selected_node"/>
</node>
</node>
<node TEXT="---" ID="ID_1979798018" CREATED="1370194798778" MODIFIED="1370194798778">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="View settings" FOLDED="true" ID="ID_626679579" CREATED="1370194798784" MODIFIED="1370195527394">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="menu_viewmode"/>
<attribute NAME="name_ref" VALUE="menu_viewmode"/>
<node TEXT="Outline view" ID="ID_866224638" CREATED="1370194798784" MODIFIED="1370194798784">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ViewLayoutTypeAction.OUTLINE"/>
</node>
<node TEXT="Full screen mode" ID="ID_370782802" CREATED="1370194798785" MODIFIED="1370194798785">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ToggleFullScreenAction"/>
</node>
<node TEXT="Presentation mode" ID="ID_723572077" CREATED="1370194798785" MODIFIED="1370334966163">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SetBooleanPropertyAction.presentation_mode"/>
</node>
<node TEXT="Rectangular selection" ID="ID_1131505033" CREATED="1370194798785" MODIFIED="1370194798785">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ShowSelectionAsRectangleAction"/>
</node>
<node TEXT="Highlight formulas" ID="ID_1286936249" CREATED="1370194798785" MODIFIED="1370334973602">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SetBooleanPropertyAction.highlight_formulas"/>
</node>
</node>
<node TEXT="---" ID="ID_1644709030" CREATED="1370194798785" MODIFIED="1370194798785">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Minimize node" ID="ID_483570758" CREATED="1370194798806" MODIFIED="1370194798806">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SetShortenerStateAction"/>
</node>
<node TEXT="Hide details" ID="ID_681110643" CREATED="1370194798807" MODIFIED="1370194798807">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ToggleDetailsAction"/>
<attribute NAME="accelerator" VALUE="alt F2"/>
</node>
<node TEXT="Tool tips" FOLDED="true" ID="ID_485642837" CREATED="1370194798807" MODIFIED="1370194798807">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="menu_hoverView"/>
<attribute NAME="name_ref" VALUE="menu_hoverView"/>
<node TEXT="Display tool tips" ID="ID_1678964910" CREATED="1370194798807" MODIFIED="1370335023905">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SetBooleanPropertyAction.show_node_tooltips"/>
</node>
<node TEXT="Display node styles in tool tips" ID="ID_420690474" CREATED="1370194798807" MODIFIED="1370335062113">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SetBooleanPropertyAction.show_styles_in_tooltip"/>
</node>
<node TEXT="Display modification times" ID="ID_1658544067" CREATED="1370194798807" MODIFIED="1370194798807">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="CreationModificationPluginAction"/>
</node>
</node>
<node TEXT="AttributeView" FOLDED="true" ID="ID_1440176200" CREATED="1370194798807" MODIFIED="1370194798807">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="AttributeView"/>
<node TEXT="Node attributes" FOLDED="true" ID="ID_414868867" CREATED="1370194798807" MODIFIED="1370194798807">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="menu_displayAttributes"/>
<attribute NAME="name_ref" VALUE="menu_displayAttributes"/>
<node TEXT="Show selected attributes" ID="ID_587556905" CREATED="1370194798807" MODIFIED="1370194798807">
<attribute NAME="type" VALUE="radio_action"/>
<attribute NAME="action" VALUE="ShowSelectedAttributesAction"/>
</node>
<node TEXT="Show all attributes" ID="ID_58568686" CREATED="1370194798807" MODIFIED="1370194798807">
<attribute NAME="type" VALUE="radio_action"/>
<attribute NAME="action" VALUE="ShowAllAttributesAction"/>
</node>
<node TEXT="Hide all attributes" ID="ID_190347460" CREATED="1370194798808" MODIFIED="1370194798808">
<attribute NAME="type" VALUE="radio_action"/>
<attribute NAME="action" VALUE="HideAllAttributesAction"/>
</node>
<node TEXT="Show icon for attributes" ID="ID_1236326850" CREATED="1370194798808" MODIFIED="1370344038126">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SetBooleanMapPropertyAction.show_icon_for_attributes"/>
</node>
</node>
</node>
<node TEXT="Notes" FOLDED="true" ID="ID_1436570954" CREATED="1370194798808" MODIFIED="1370194798808">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="menu_noteView"/>
<attribute NAME="name_ref" VALUE="menu_noteView"/>
<node TEXT="Display note panel" ID="ID_1042871377" CREATED="1370194798808" MODIFIED="1370194798808">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ShowHideNoteAction"/>
<attribute NAME="accelerator" VALUE="control GREATER"/>
</node>
<node TEXT="Note panel position" FOLDED="true" ID="ID_1676289590" CREATED="1370194798808" MODIFIED="1370194798808">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="note_window_location"/>
<attribute NAME="name_ref" VALUE="note_window_location"/>
<node TEXT="Top" ID="ID_1402820198" CREATED="1370194798808" MODIFIED="1370194798808">
<attribute NAME="type" VALUE="radio_action"/>
<attribute NAME="action" VALUE="SetNoteWindowPosition.top"/>
</node>
<node TEXT="Left" ID="ID_581625920" CREATED="1370194798816" MODIFIED="1370194798816">
<attribute NAME="type" VALUE="radio_action"/>
<attribute NAME="action" VALUE="SetNoteWindowPosition.left"/>
</node>
<node TEXT="Right" ID="ID_375860983" CREATED="1370194798816" MODIFIED="1370194798816">
<attribute NAME="type" VALUE="radio_action"/>
<attribute NAME="action" VALUE="SetNoteWindowPosition.right"/>
</node>
<node TEXT="Bottom" ID="ID_888523136" CREATED="1370194798817" MODIFIED="1370194798817">
<attribute NAME="type" VALUE="radio_action"/>
<attribute NAME="action" VALUE="SetNoteWindowPosition.bottom"/>
</node>
</node>
<node TEXT="Show note icons" ID="ID_1526587566" CREATED="1370194798817" MODIFIED="1370335082456">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SetBooleanMapPropertyAction.show_note_icons"/>
</node>
<node TEXT="Display notes in map" ID="ID_620871847" CREATED="1370194798817" MODIFIED="1370194798817">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ShowNotesInMapAction"/>
</node>
</node>
</node>
<node TEXT="Format" FOLDED="true" ID="ID_74036811" CREATED="1370194798817" MODIFIED="1370334678637">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="format"/>
<attribute NAME="name_ref" VALUE="menu_format"/>
<node TEXT="Apply style" ID="ID_979545798" CREATED="1370194798817" MODIFIED="1370194798817">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="menu_applyStyle"/>
<attribute NAME="name_ref" VALUE="menu_applyStyle"/>
<attribute NAME="menu_key" VALUE="main_menu_styles"/>
</node>
<node TEXT="Apply level styles" FOLDED="true" ID="ID_1818873885" CREATED="1370194798817" MODIFIED="1370194798817">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="AutomaticLayoutAction"/>
<attribute NAME="name_ref" VALUE="automatic_layout"/>
<node TEXT="for non leave nodes" ID="ID_348142044" CREATED="1370194798817" MODIFIED="1370194798817">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="AutomaticLayoutControllerAction.HEADINGS"/>
</node>
<node TEXT="for all nodes" ID="ID_1879525351" CREATED="1370194798817" MODIFIED="1370194798817">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="AutomaticLayoutControllerAction.ALL"/>
</node>
<node TEXT="disabled" ID="ID_793031894" CREATED="1370194798818" MODIFIED="1370194798818">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="AutomaticLayoutControllerAction.null"/>
</node>
</node>
<node TEXT="Manage Styles" FOLDED="true" ID="ID_306428571" CREATED="1370194798818" MODIFIED="1370194798818">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="menu_manageStyles"/>
<attribute NAME="name_ref" VALUE="menu_manageStyles"/>
<node TEXT="New style from selection" ID="ID_159416697" CREATED="1370194798818" MODIFIED="1370194798818">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="NewUserStyleAction"/>
</node>
<node TEXT="Redefine style" ID="ID_818301233" CREATED="1370194798818" MODIFIED="1370194798818">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="RedefineStyleAction"/>
</node>
<node TEXT="Manage conditional styles for map" ID="ID_805486777" CREATED="1370194798818" MODIFIED="1370194798818">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ManageConditionalStylesAction"/>
</node>
<node TEXT="Manage conditional styles for node" ID="ID_1005345644" CREATED="1370194798818" MODIFIED="1370194798818">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ManageNodeConditionalStylesAction"/>
</node>
<node TEXT="---" ID="ID_1647592745" CREATED="1370194798818" MODIFIED="1370194798818">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Copy map style from..." ID="ID_468835002" CREATED="1370194798821" MODIFIED="1370194798821">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="CopyMapStylesAction"/>
</node>
<node TEXT="Edit styles" ID="ID_246750226" CREATED="1370194798822" MODIFIED="1370194798822">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="EditStylesAction"/>
</node>
</node>
<node TEXT="Copy format" ID="ID_1670892979" CREATED="1370194798822" MODIFIED="1370194798822">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="FormatCopy"/>
<attribute NAME="accelerator" VALUE="alt shift C"/>
</node>
<node TEXT="Paste format" ID="ID_1621680412" CREATED="1370194798822" MODIFIED="1370194798822">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="FormatPaste"/>
<attribute NAME="accelerator" VALUE="alt shift V"/>
</node>
<node TEXT="---" ID="ID_1154977018" CREATED="1370194798822" MODIFIED="1370194798822">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Map background color" ID="ID_727314892" CREATED="1370194798822" MODIFIED="1370194798822">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="MapBackgroundColorAction"/>
</node>
<node TEXT="Node core" FOLDED="true" ID="ID_305696182" CREATED="1370194798822" MODIFIED="1370194798822">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="menu_coreFormat"/>
<attribute NAME="name_ref" VALUE="menu_coreFormat"/>
<node TEXT="Bold" ID="ID_1378848121" CREATED="1370194798822" MODIFIED="1370194798822">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="BoldAction"/>
<attribute NAME="accelerator" VALUE="control B"/>
</node>
<node TEXT="Italic" ID="ID_1414436366" CREATED="1370194798822" MODIFIED="1370194798822">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ItalicAction"/>
<attribute NAME="accelerator" VALUE="control I"/>
</node>
<node TEXT="Larger font" ID="ID_1548651326" CREATED="1370194798823" MODIFIED="1370194798823">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="IncreaseNodeFontAction"/>
<attribute NAME="accelerator" VALUE="control PLUS"/>
</node>
<node TEXT="Smaller font" ID="ID_1969269056" CREATED="1370194798823" MODIFIED="1370194798823">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="DecreaseNodeFontAction"/>
<attribute NAME="accelerator" VALUE="control MINUS"/>
</node>
<node TEXT="---" ID="ID_1673359742" CREATED="1370194798823" MODIFIED="1370194798823">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="TextAlignAction.LEFT">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="TextAlignAction.LEFT"/>
</node>
<node TEXT="TextAlignAction.CENTER">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="TextAlignAction.CENTER"/>
</node>
<node TEXT="TextAlignAction.RIGHT">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="TextAlignAction.RIGHT"/>
</node>
<node TEXT="---">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Node color..." ID="ID_769544494" CREATED="1370194798823" MODIFIED="1370194798823">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="NodeColorAction"/>
<attribute NAME="accelerator" VALUE="alt shift F"/>
</node>
<node TEXT="Blinking node" ID="ID_1600861868" CREATED="1370194798823" MODIFIED="1370194798823">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="BlinkingNodeHookAction"/>
</node>
<node TEXT="Blend color" ID="ID_965772571" CREATED="1370194798823" MODIFIED="1370194798823">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="NodeColorBlendAction"/>
</node>
<node TEXT="---" ID="ID_846863399" CREATED="1370194798823" MODIFIED="1370194798823">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Node background color..." ID="ID_131182554" CREATED="1370194798828" MODIFIED="1370194798828">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="NodeBackgroundColorAction"/>
</node>
<node TEXT="---" ID="ID_1353593389" CREATED="1370194798828" MODIFIED="1370194798828">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Fork" ID="ID_610003784" CREATED="1370194798829" MODIFIED="1370194798829">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="NodeShapeAction.fork"/>
</node>
<node TEXT="Bubble" ID="ID_566669024" CREATED="1370194798829" MODIFIED="1370194798829">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="NodeShapeAction.bubble"/>
</node>
<node TEXT="---" ID="ID_884495267" CREATED="1370194798829" MODIFIED="1370194798829">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Remove format" ID="ID_1704403630" CREATED="1370194798829" MODIFIED="1370194798829">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="RemoveFormatAction"/>
</node>
<node TEXT="Use plain text" ID="ID_1328005461" CREATED="1370194798829" MODIFIED="1370194798829">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="UsePlainTextAction"/>
<attribute NAME="accelerator" VALUE="alt shift P"/>
</node>
</node>
<node TEXT="Cloud properties" FOLDED="true" ID="ID_1111702163" CREATED="1370194798829" MODIFIED="1370335177560">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="CloudProperties"/>
<node TEXT="Shapes" FOLDED="true" ID="ID_834386191" CREATED="1370194798829" MODIFIED="1370335168864">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="shapes"/>
<attribute NAME="name_ref" VALUE="format_menu_cloud_shapes"/>
<node TEXT="Arc" ID="ID_1879026538" CREATED="1370194798829" MODIFIED="1370194798829">
<attribute NAME="type" VALUE="radio_action"/>
<attribute NAME="action" VALUE="CloudShapeAction.ARC"/>
</node>
<node TEXT="Star" ID="ID_1450209381" CREATED="1370194798829" MODIFIED="1370194798829">
<attribute NAME="type" VALUE="radio_action"/>
<attribute NAME="action" VALUE="CloudShapeAction.STAR"/>
</node>
<node TEXT="Rectangle" ID="ID_1377798202" CREATED="1370194798829" MODIFIED="1370194798829">
<attribute NAME="type" VALUE="radio_action"/>
<attribute NAME="action" VALUE="CloudShapeAction.RECT"/>
</node>
<node TEXT="Round rectangle" ID="ID_231098218" CREATED="1370194798830" MODIFIED="1370194798830">
<attribute NAME="type" VALUE="radio_action"/>
<attribute NAME="action" VALUE="CloudShapeAction.ROUND_RECT"/>
</node>
</node>
<node TEXT="Cloud color..." ID="ID_524244545" CREATED="1370194798830" MODIFIED="1370194798830">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="CloudColorAction"/>
</node>
</node>
<node TEXT="---" ID="ID_1656891956" CREATED="1370194798830" MODIFIED="1370194798830">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Edge properties" FOLDED="true" ID="ID_1691391318" CREATED="1370194798830" MODIFIED="1370335185695">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="EdgeProperties"/>
<node TEXT="Styles" FOLDED="true" ID="ID_1025058933" CREATED="1370194798830" MODIFIED="1370194798830">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="styles"/>
<attribute NAME="name_ref" VALUE="format_menu_edge_styles"/>
<node TEXT="As parent" ID="ID_453842689" CREATED="1370194798830" MODIFIED="1370194798830">
<attribute NAME="type" VALUE="radio_action"/>
<attribute NAME="action" VALUE="EdgeStyleAsParentAction"/>
</node>
<node TEXT="Linear" ID="ID_760998239" CREATED="1370194798830" MODIFIED="1370194798830">
<attribute NAME="type" VALUE="radio_action"/>
<attribute NAME="action" VALUE="EdgeStyleAction.linear"/>
</node>
<node TEXT="Smoothly curved (bezier)" ID="ID_1966280734" CREATED="1370194798835" MODIFIED="1370194798835">
<attribute NAME="type" VALUE="radio_action"/>
<attribute NAME="action" VALUE="EdgeStyleAction.bezier"/>
</node>
<node TEXT="Sharp linear" ID="ID_1675492093" CREATED="1370194798835" MODIFIED="1370194798835">
<attribute NAME="type" VALUE="radio_action"/>
<attribute NAME="action" VALUE="EdgeStyleAction.sharp_linear"/>
</node>
<node TEXT="Sharply curved (bezier)" ID="ID_21906951" CREATED="1370194798836" MODIFIED="1370194798836">
<attribute NAME="type" VALUE="radio_action"/>
<attribute NAME="action" VALUE="EdgeStyleAction.sharp_bezier"/>
</node>
<node TEXT="Horizontal" ID="ID_988590411" CREATED="1370194798836" MODIFIED="1370194798836">
<attribute NAME="type" VALUE="radio_action"/>
<attribute NAME="action" VALUE="EdgeStyleAction.horizontal"/>
</node>
<node TEXT="Hide edge" ID="ID_1743099691" CREATED="1370194798836" MODIFIED="1370194798836">
<attribute NAME="type" VALUE="radio_action"/>
<attribute NAME="action" VALUE="EdgeStyleAction.hide_edge"/>
</node>
</node>
<node TEXT="Width" FOLDED="true" ID="ID_762696594" CREATED="1370194798836" MODIFIED="1370335196103">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="widths"/>
<attribute NAME="name_ref" VALUE="format_menu_edge_widths"/>
<node TEXT="Parent" ID="ID_1632874082" CREATED="1370194798837" MODIFIED="1370194798837">
<attribute NAME="type" VALUE="radio_action"/>
<attribute NAME="action" VALUE="EdgeWidthAction_width_parent"/>
</node>
<node TEXT="Thin" ID="ID_105397654" CREATED="1370194798837" MODIFIED="1370194798837">
<attribute NAME="type" VALUE="radio_action"/>
<attribute NAME="action" VALUE="EdgeWidthAction_width_thin"/>
</node>
<node TEXT="1" OBJECT="java.lang.Long|1" ID="ID_686807299" CREATED="1370194798837" MODIFIED="1370335252189">
<attribute NAME="type" VALUE="radio_action"/>
<attribute NAME="action" VALUE="EdgeWidthAction_1"/>
</node>
<node TEXT="2" OBJECT="java.lang.Long|2" ID="ID_1378854466" CREATED="1370194798837" MODIFIED="1370335253514">
<attribute NAME="type" VALUE="radio_action"/>
<attribute NAME="action" VALUE="EdgeWidthAction_2"/>
</node>
<node TEXT="4" OBJECT="java.lang.Long|4" ID="ID_1214308721" CREATED="1370194798845" MODIFIED="1370335254979">
<attribute NAME="type" VALUE="radio_action"/>
<attribute NAME="action" VALUE="EdgeWidthAction_4"/>
</node>
<node TEXT="8" OBJECT="java.lang.Long|8" ID="ID_152696495" CREATED="1370194798845" MODIFIED="1370335257126">
<attribute NAME="type" VALUE="radio_action"/>
<attribute NAME="action" VALUE="EdgeWidthAction_8"/>
</node>
</node>
<node TEXT="Edge color..." ID="ID_977789508" CREATED="1370194798845" MODIFIED="1370194798845">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="EdgeColorAction"/>
<attribute NAME="accelerator" VALUE="alt shift E"/>
</node>
<node TEXT="Automatic edge color" ID="ID_83643005" CREATED="1370194798845" MODIFIED="1370194798845">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="AutomaticEdgeColorHookAction"/>
</node>
</node>
<node TEXT="---" ID="ID_1126623769" CREATED="1370194798845" MODIFIED="1370194798845">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Set node width limits" ID="ID_530668930" CREATED="1370194798846" MODIFIED="1370194798846">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="NodeWidthAction"/>
</node>
</node>
<node TEXT="Navigate" FOLDED="true" ID="ID_400942186" CREATED="1370194798846" MODIFIED="1370334681725">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="navigate"/>
<attribute NAME="name_ref" VALUE="menu_navigate"/>
<attribute NAME="menu_key" VALUE="menu_navigate"/>
<node TEXT="navigate" FOLDED="true" ID="ID_1118542616" CREATED="1370194798846" MODIFIED="1370194798846">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="navigate"/>
<node TEXT="Previous map" ID="ID_1130674595" CREATED="1370194798846" MODIFIED="1370194798846">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="NavigationPreviousMapAction"/>
<attribute NAME="accelerator" VALUE="control shift TAB"/>
</node>
<node TEXT="Next map" ID="ID_1422147659" CREATED="1370194798846" MODIFIED="1370194798846">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="NavigationNextMapAction"/>
<attribute NAME="accelerator" VALUE="control TAB"/>
</node>
<node TEXT="---" ID="ID_1150377789" CREATED="1370194798846" MODIFIED="1370194798846">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="folding" FOLDED="true" ID="ID_842290285" CREATED="1370194798846" MODIFIED="1370194798846">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="folding"/>
<node TEXT="(Un)fold" ID="ID_1936410673" CREATED="1370194798846" MODIFIED="1370194798846">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ToggleFoldedAction"/>
<attribute NAME="accelerator" VALUE="SPACE"/>
</node>
<node TEXT="Show next child" ID="ID_1660299916" CREATED="1370194798847" MODIFIED="1370194798847">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ShowNextChildAction"/>
<attribute NAME="accelerator" VALUE="shift SPACE"/>
</node>
<node TEXT="(Un)fold children" ID="ID_706164442" CREATED="1370194798847" MODIFIED="1370194798847">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ToggleChildrenFoldedAction"/>
<attribute NAME="accelerator" VALUE="control SPACE"/>
</node>
<node TEXT="Unfold one level" ID="ID_817918623" CREATED="1370194798847" MODIFIED="1370194798847">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="UnfoldOneLevelAction"/>
<attribute NAME="accelerator" VALUE="alt PAGE_DOWN"/>
</node>
<node TEXT="Fold one level" ID="ID_350814939" CREATED="1370194798847" MODIFIED="1370194798847">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="FoldOneLevelAction"/>
<attribute NAME="accelerator" VALUE="alt PAGE_UP"/>
</node>
<node TEXT="Unfold all" ID="ID_1639555610" CREATED="1370194798847" MODIFIED="1370194798847">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="UnfoldAllAction"/>
<attribute NAME="accelerator" VALUE="alt END"/>
</node>
<node TEXT="Fold all" ID="ID_382857723" CREATED="1370194798847" MODIFIED="1370194798847">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="FoldAllAction"/>
<attribute NAME="accelerator" VALUE="alt HOME"/>
</node>
</node>
</node>
<node TEXT="---" ID="ID_1287635307" CREATED="1370194798847" MODIFIED="1370194798847">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Select all visible nodes" ID="ID_502719313" CREATED="1370194798847" MODIFIED="1370194798847">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SelectAllAction"/>
<attribute NAME="accelerator" VALUE="control A"/>
</node>
<node TEXT="Select visible branch" ID="ID_697134886" CREATED="1370194798848" MODIFIED="1370194798848">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SelectBranchAction"/>
<attribute NAME="accelerator" VALUE="control shift A"/>
</node>
<node TEXT="---" ID="ID_300938106" CREATED="1370194798848" MODIFIED="1370194798848">
<attribute NAME="type" VALUE="separator"/>
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
<node TEXT="Go backward" ID="ID_6096980" CREATED="1370194798856" MODIFIED="1370194798856">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="BackAction"/>
<attribute NAME="accelerator" VALUE="alt LEFT"/>
</node>
<node TEXT="Go forward" ID="ID_291270163" CREATED="1370194798856" MODIFIED="1370194798856">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ForwardAction"/>
<attribute NAME="accelerator" VALUE="alt RIGHT"/>
</node>
<node TEXT="links" ID="ID_1106555890" CREATED="1370194798856" MODIFIED="1370194798856">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="links"/>
<node TEXT="---" ID="ID_1244155586" CREATED="1370194798857" MODIFIED="1370194798857">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Follow link" ID="ID_490792303" CREATED="1370194798857" MODIFIED="1370194798857">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="FollowLinkAction"/>
<attribute NAME="accelerator" VALUE="control ENTER"/>
</node>
<node TEXT="goto links" ID="ID_790442896" CREATED="1370194798861" MODIFIED="1370335408485">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="menu_key" VALUE="menu_goto_links"/>
</node>
</node>
</node>
<node TEXT="Filter" FOLDED="true" ID="ID_1709313215" CREATED="1370194798862" MODIFIED="1370194798862">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="filter"/>
<attribute NAME="name_ref" VALUE="menu_filter"/>
<node TEXT="Filter" ID="ID_951564241" CREATED="1370194798862" MODIFIED="1370194798862">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="Filter"/>
<node TEXT="User-defined filters" ID="ID_750739663" CREATED="1370194798862" MODIFIED="1370194798862">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="user_defined_filters"/>
<attribute NAME="name_ref" VALUE="user_defined_filters"/>
<attribute NAME="menu_key" VALUE="menu_user_defined_filters"/>
</node>
<node TEXT="DoFilter" FOLDED="true" ID="ID_1233594504" CREATED="1370194798868" MODIFIED="1370194798868">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="DoFilter"/>
<node TEXT="Undo filter" ID="ID_1060614745" CREATED="1370194798869" MODIFIED="1370344398562">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="UndoFilterAction"/>
</node>
<node TEXT="Redo filter" ID="ID_647411361" CREATED="1370194798869" MODIFIED="1370344401929">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="RedoFilterAction"/>
</node>
<node TEXT="Reapply filter" ID="ID_1390558533" CREATED="1370194798870" MODIFIED="1370344404457">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ReapplyFilterAction"/>
</node>
<node TEXT="---" ID="ID_1385398789" CREATED="1370194798870" MODIFIED="1370194798870">
<attribute NAME="type" VALUE="separator"/>
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
<node TEXT="---" ID="ID_242790978" CREATED="1370194798874" MODIFIED="1370194798874">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="FilterCondition" FOLDED="true" ID="ID_947710900" CREATED="1370194798874" MODIFIED="1370194798874">
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
<node TEXT="---" ID="ID_751464878" CREATED="1370194798874" MODIFIED="1370194798874">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Find" ID="ID_784014675" CREATED="1370194798874" MODIFIED="1370194798874">
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
<node TEXT="Extras" FOLDED="true" ID="ID_1843603736" CREATED="1370194798883" MODIFIED="1370334683989">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="extras"/>
<attribute NAME="name_ref" VALUE="menu_extras"/>
<node TEXT="time" FOLDED="true" ID="ID_538370288" CREATED="1370194798894" MODIFIED="1370194798894">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="time"/>
<attribute NAME="name_ref" VALUE="menu_time"/>
<node TEXT="Manage time..." ID="ID_1370300121" CREATED="1370194798894" MODIFIED="1370194798894">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="TimeManagementAction"/>
<attribute NAME="accelerator" VALUE="control T"/>
</node>
<node TEXT="Manage tasks ..." ID="ID_605635543" CREATED="1370194798894" MODIFIED="1370194798894">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="TimeListAction"/>
</node>
<node TEXT="Remove reminder" ID="ID_675422744" CREATED="1370194798894" MODIFIED="1370194798894">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ReminderHookAction"/>
</node>
</node>
<node TEXT="---" ID="ID_1606403616" CREATED="1370194798894" MODIFIED="1370194798894">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="encryption" FOLDED="true" ID="ID_914800542" CREATED="1370194798894" MODIFIED="1370194798894">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="encryption"/>
<attribute NAME="name_ref" VALUE="menu_encryption"/>
<node TEXT="Enter password" ID="ID_1147903482" CREATED="1370194798894" MODIFIED="1370194798894">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="EnterPassword"/>
</node>
<node TEXT="Remove password" ID="ID_542006664" CREATED="1370194798894" MODIFIED="1370194798894">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="RemoveEncryption"/>
</node>
</node>
<node TEXT="---" ID="ID_49728777" CREATED="1370194798894" MODIFIED="1370194798894">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="first" FOLDED="true" ID="ID_87077110" CREATED="1370194798894" MODIFIED="1370194798894">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="first"/>
<node TEXT="options" FOLDED="true" ID="ID_1076760803" CREATED="1370194798895" MODIFIED="1370194798895">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="options"/>
<node TEXT="Add-ons" ID="ID_1083541033" CREATED="1370194798895" MODIFIED="1370194798895">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ManageAddOnsAction"/>
<attribute NAME="plugin" VALUE="org.freeplane.plugin.script"/>
</node>
<node TEXT="Assign hot key..." ID="ID_1428658806" CREATED="1370194798895" MODIFIED="1370194798895">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SetAcceleratorOnNextClickAction"/>
</node>
<node TEXT="Hot key presets" FOLDED="true" ID="ID_464605071" CREATED="1370194798895" MODIFIED="1370194798895">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="acceleratorPresets"/>
<attribute NAME="name_ref" VALUE="acceleratorPresets"/>
<node TEXT="Load" ID="ID_698086099" CREATED="1370194798895" MODIFIED="1370194798895">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="load_accelerator_presets"/>
<attribute NAME="menu_key" VALUE="main_menu_new_load_accelerator_presets"/>
</node>
<node TEXT="Save hot key set..." ID="ID_1931077663" CREATED="1370194798895" MODIFIED="1370194798895">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SaveAcceleratorPresetsAction"/>
</node>
</node>
</node>
<node TEXT="---" ID="ID_1758063531" CREATED="1370194798895" MODIFIED="1370194798895">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="scripting" ID="ID_403848834" CREATED="1370194798903" MODIFIED="1370194798903">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="scripting"/>
<attribute NAME="plugin" VALUE="org.freeplane.plugin.script"/>
<attribute NAME="menu_key" VALUE="main_menu_scripting"/>
</node>
<node TEXT="Execute selected node scripts" ID="ID_1948545653" CREATED="1370194798904" MODIFIED="1370194798904">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ExecuteScriptForSelectionAction"/>
<attribute NAME="plugin" VALUE="org.freeplane.plugin.script"/>
</node>
<node TEXT="Execute all scripts" ID="ID_1798993664" CREATED="1370194798904" MODIFIED="1370194798904">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ExecuteScriptForAllNodes"/>
<attribute NAME="plugin" VALUE="org.freeplane.plugin.script"/>
</node>
<node TEXT="Edit script..." ID="ID_589433052" CREATED="1370194798904" MODIFIED="1370194798904">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ScriptEditor"/>
<attribute NAME="plugin" VALUE="org.freeplane.plugin.script"/>
</node>
<node TEXT="---" ID="ID_204656974" CREATED="1370194798904" MODIFIED="1370194798904">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Formula" FOLDED="true" ID="ID_210823348" CREATED="1370194798904" MODIFIED="1370335693682">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="formula"/>
<attribute NAME="name_ref" VALUE="formula.menuname"/>
<attribute NAME="plugin" VALUE="org.freeplane.plugin.formula"/>
<node TEXT="Evaluate all" ID="ID_725452261" CREATED="1370194798904" MODIFIED="1370194798904">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="formula.EvaluateAllAction"/>
<attribute NAME="plugin" VALUE="org.freeplane.plugin.formula"/>
</node>
<node TEXT="---" ID="ID_1774493535" CREATED="1370194798904" MODIFIED="1370194798904">
<attribute NAME="type" VALUE="separator"/>
</node>
</node>
<node TEXT="---" ID="ID_417864337" CREATED="1370194798904" MODIFIED="1370194798904">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Open user directory" ID="ID_1479109245" CREATED="1370194798904" MODIFIED="1370194798904">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="OpenUserDirAction"/>
</node>
</node>
<node TEXT="---" ID="ID_1580920327" CREATED="1370194798905" MODIFIED="1370194798905">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Preferences ..." ID="ID_986394123" CREATED="1370194798905" MODIFIED="1370194798905">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="PropertyAction"/>
<attribute NAME="accelerator" VALUE="control COMMA"/>
<attribute NAME="menu_key" VALUE="MB_PropertyAction"/>
</node>
</node>
<node TEXT="Maps" FOLDED="true" ID="ID_1802477374" CREATED="1370194798905" MODIFIED="1370194798905">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="mindmaps"/>
<attribute NAME="name_ref" VALUE="mindmaps"/>
<node TEXT="Modes" ID="ID_1426335696" CREATED="1370194798905" MODIFIED="1370194798905">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="modes"/>
<attribute NAME="menu_key" VALUE="main_menu_modes"/>
</node>
<node TEXT="navigate" ID="ID_811289470" CREATED="1370194798905" MODIFIED="1370194798905">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="navigate"/>
<attribute NAME="menu_key" VALUE="main_menu_navigate_maps"/>
</node>
<node TEXT="---" ID="ID_1686426603" CREATED="1370194798905" MODIFIED="1370194798905">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Maps" ID="ID_1593219515" CREATED="1370194798905" MODIFIED="1370194798905">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="mindmaps"/>
<attribute NAME="menu_key" VALUE="main_menu_mindmaps"/>
</node>
<node TEXT="---" ID="ID_314119416" CREATED="1370194798905" MODIFIED="1370194798905">
<attribute NAME="type" VALUE="separator"/>
</node>
</node>
<node TEXT="Help" FOLDED="true" ID="ID_471141721" CREATED="1370194798905" MODIFIED="1370194798905">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="help"/>
<attribute NAME="name_ref" VALUE="help"/>
<node TEXT="update" FOLDED="true" ID="ID_722782407" CREATED="1370194798905" MODIFIED="1370194798905">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="update"/>
<node TEXT="Check for updates" ID="ID_1477489495" CREATED="1370194798905" MODIFIED="1370194798905">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="UpdateCheckAction"/>
</node>
</node>
<node TEXT="---" ID="ID_1292710565" CREATED="1370194798906" MODIFIED="1370194798906">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Web resources" FOLDED="true" ID="ID_652775564" CREATED="1370194798906" MODIFIED="1370194798906">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="Web resources"/>
<node TEXT="Freeplane&apos;s Homepage" ID="ID_369316306" CREATED="1370194798906" MODIFIED="1370194798906">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="OpenFreeplaneSiteAction"/>
</node>
<node TEXT="Ask for help" ID="ID_868967885" CREATED="1370194798906" MODIFIED="1370194798906">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="AskForHelp"/>
</node>
<node TEXT="Report a bug" ID="ID_418449274" CREATED="1370194798906" MODIFIED="1370194798906">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ReportBugAction"/>
</node>
<node TEXT="Request a feature" ID="ID_850613199" CREATED="1370194798906" MODIFIED="1370194798906">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="RequestFeatureAction"/>
</node>
</node>
<node TEXT="---" ID="ID_175779890" CREATED="1370194798906" MODIFIED="1370194798906">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="legacy" FOLDED="true" ID="ID_667870728" CREATED="1370194798906" MODIFIED="1370194798906">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="legacy"/>
<node TEXT="About" ID="ID_1556433421" CREATED="1370194798906" MODIFIED="1370194798906">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="AboutAction"/>
<attribute NAME="menu_key" VALUE="MB_AboutAction"/>
</node>
</node>
<node TEXT="---" ID="ID_1384662215" CREATED="1370194798914" MODIFIED="1370194798914">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Tutorial" ID="ID_1441537677" CREATED="1370194798914" MODIFIED="1370194798914">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="GettingStartedAction"/>
<attribute NAME="accelerator" VALUE="F1"/>
</node>
<node TEXT="Documentation" ID="ID_1046484195" CREATED="1370194798915" MODIFIED="1370194798915">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="DocumentationAction"/>
</node>
<node TEXT="Documentation Maps Online" ID="ID_1808535585" CREATED="1370194798915" MODIFIED="1370194798915">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="OnlineReference"/>
</node>
<node TEXT="Key reference" ID="ID_304156316" CREATED="1370194798915" MODIFIED="1370194798915">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="HotKeyInfoAction"/>
</node>
<node TEXT="---" ID="ID_1669047436" CREATED="1370194798915" MODIFIED="1370194798915">
<attribute NAME="type" VALUE="separator"/>
</node>
</node>
</node>
<node TEXT="map_popup" FOLDED="true" POSITION="right" ID="ID_1654114725" CREATED="1370194798915" MODIFIED="1370194798915">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="map_popup"/>
<node TEXT="Maps" ID="ID_1405453807" CREATED="1370194798915" MODIFIED="1370194798915">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="mindmaps"/>
<attribute NAME="menu_key" VALUE="popup_menu_mindmaps"/>
</node>
<node TEXT="---" ID="ID_1475998756" CREATED="1370194798915" MODIFIED="1370194798915">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Full screen mode" ID="ID_578287914" CREATED="1370194798915" MODIFIED="1370194798915">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ToggleFullScreenAction"/>
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
<node TEXT="F-keys Bar" ID="ID_527118617" CREATED="1370194798918" MODIFIED="1370335740913">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ToggleFBarAction"/>
</node>
<node TEXT="Icons toolbar" ID="ID_144251549" CREATED="1370194798918" MODIFIED="1370194798918">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ToggleLeftToolbarAction"/>
</node>
<node TEXT="Display status line" ID="ID_1199576247" CREATED="1370194798919" MODIFIED="1370194798919">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ToggleStatusAction"/>
</node>
<node TEXT="Scrollbars" ID="ID_601963475" CREATED="1370194798919" MODIFIED="1370194798919">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ToggleScrollbarsAction"/>
</node>
<node TEXT="---" ID="ID_1597107668" CREATED="1370194798919" MODIFIED="1370194798919">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Properties panel" ID="ID_1646536129" CREATED="1370194798919" MODIFIED="1370194798919">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ShowFormatPanel"/>
</node>
<node TEXT="Display note panel" ID="ID_254417168" CREATED="1370194798919" MODIFIED="1370194798919">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ShowHideNoteAction"/>
<attribute NAME="accelerator" VALUE="control GREATER"/>
</node>
<node TEXT="---" ID="ID_899379662" CREATED="1370194798919" MODIFIED="1370194798919">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Map background color" ID="ID_1982484281" CREATED="1370194798919" MODIFIED="1370194798919">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="MapBackgroundColorAction"/>
</node>
<node TEXT="---" ID="ID_1222660568" CREATED="1370194798919" MODIFIED="1370194798919">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Outline view" ID="ID_349914674" CREATED="1370194798919" MODIFIED="1370194798919">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ViewLayoutTypeAction.OUTLINE"/>
</node>
<node TEXT="Zoom to fit to page" ID="ID_1060546943" CREATED="1370194798919" MODIFIED="1370194798919">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="FitToPage"/>
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
<node TEXT="node_popup" FOLDED="true" POSITION="right" ID="ID_1281865345" CREATED="1370194798919" MODIFIED="1370194798919">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="node_popup"/>
<node TEXT="Edit node core in dialog" ID="ID_1485228711" CREATED="1370194798920" MODIFIED="1370194798920">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="EditLongAction"/>
<attribute NAME="accelerator" VALUE="alt ENTER"/>
</node>
<node TEXT="Edit node details in dialog" ID="ID_1746465857" CREATED="1370194798920" MODIFIED="1370194798920">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="EditDetailsInDialogAction"/>
</node>
<node TEXT="Edit node details in-line" ID="ID_1274185383" CREATED="1370194798925" MODIFIED="1370194798925">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="EditDetailsAction"/>
</node>
<node TEXT="Remove node details" ID="ID_193980725" CREATED="1370194798926" MODIFIED="1370194798926">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="DeleteDetailsAction"/>
</node>
<node TEXT="Minimize node" ID="ID_1929653572" CREATED="1370194798926" MODIFIED="1370194798926">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SetShortenerStateAction"/>
</node>
<node TEXT="Edit note in dialog" ID="ID_951346222" CREATED="1370194798926" MODIFIED="1370194798926">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="EditNoteInDialogAction"/>
</node>
<node TEXT="Add image..." ID="ID_1332445025" CREATED="1370194798926" MODIFIED="1370194798926">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ExternalImageAddAction"/>
</node>
<node TEXT="Edit attribute in-line" ID="ID_1465258517" CREATED="1370194798926" MODIFIED="1370194798926">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="EditAttributesAction"/>
<attribute NAME="accelerator" VALUE="alt F9"/>
</node>
<node TEXT="---" ID="ID_1132518583" CREATED="1370194798926" MODIFIED="1370194798926">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="New summary node (selected nodes)" ID="ID_1667132492" CREATED="1370194798926" MODIFIED="1370194798926">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="NewSummaryAction"/>
<attribute NAME="accelerator" VALUE="alt shift INSERT"/>
</node>
<node TEXT="Summary node (set/reset)" ID="ID_766891920" CREATED="1370194798926" MODIFIED="1370194798926">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SummaryNodeAction"/>
<attribute NAME="accelerator" VALUE="alt shift S"/>
</node>
<node TEXT="Always unfolded node (set/reset)" ID="ID_1471764005" CREATED="1370194798926" MODIFIED="1370194798926">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="AlwaysUnfoldedNodeAction"/>
</node>
<node TEXT="Free positioned node (set/reset)" ID="ID_827139464" CREATED="1370194798926" MODIFIED="1370194798926">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="FreeNodeAction"/>
</node>
<node TEXT="Add / remove cloud (default)" ID="ID_1664232726" CREATED="1370194798926" MODIFIED="1370194798926">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="CloudAction"/>
<attribute NAME="accelerator" VALUE="control shift B"/>
</node>
<node TEXT="Apply style" ID="ID_1332345084" CREATED="1370194798926" MODIFIED="1370194798926">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="menu_applyStyle"/>
<attribute NAME="name_ref" VALUE="menu_applyStyle"/>
<attribute NAME="menu_key" VALUE="node_popup_styles"/>
</node>
<node TEXT="Connect" ID="ID_851417224" CREATED="1370194798927" MODIFIED="1370194798927">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="AddConnectorAction"/>
<attribute NAME="accelerator" VALUE="control L"/>
</node>
<node TEXT="Links" FOLDED="true" ID="ID_1827136568" CREATED="1370194798927" MODIFIED="1370335770860">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE="links"/>
<attribute NAME="name_ref" VALUE="menu_links"/>
<attribute NAME="menu_key" VALUE="popup_navigate"/>
<node TEXT="Add hyperlink (choose)..." ID="ID_1328956547" CREATED="1370194798927" MODIFIED="1370194798927">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SetLinkByFileChooserAction"/>
<attribute NAME="accelerator" VALUE="control shift K"/>
</node>
<node TEXT="Add or modify hyperlink (type)..." ID="ID_72330031" CREATED="1370194798927" MODIFIED="1370194798927">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SetLinkByTextFieldAction"/>
<attribute NAME="accelerator" VALUE="control K"/>
</node>
<node TEXT="Convert link from within text" ID="ID_523595497" CREATED="1370194798927" MODIFIED="1370194798927">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ExtractLinkFromTextAction"/>
</node>
<node TEXT="---" ID="ID_133814677" CREATED="1370194798927" MODIFIED="1370194798927">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Add local hyperlink" ID="ID_358598645" CREATED="1370194798927" MODIFIED="1370194798927">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="AddLocalLinkAction"/>
<attribute NAME="accelerator" VALUE="alt shift L"/>
</node>
<node TEXT="Add hyperlink to menu item..." ID="ID_1750636716" CREATED="1370194798927" MODIFIED="1370194798927">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="AddMenuItemLinkAction"/>
<attribute NAME="accelerator" VALUE=""/>
</node>
<node TEXT="---" ID="ID_1932798993" CREATED="1370194798927" MODIFIED="1370194798927">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Set link anchor" ID="ID_409197156" CREATED="1370194798927" MODIFIED="1370194798927">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SetLinkAnchorAction"/>
<attribute NAME="accelerator" VALUE=""/>
</node>
<node TEXT="Make link from anchor" ID="ID_105002896" CREATED="1370194798928" MODIFIED="1370194798928">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="MakeLinkFromAnchorAction"/>
<attribute NAME="accelerator" VALUE=""/>
</node>
<node TEXT="Make link to anchor" ID="ID_166361381" CREATED="1370194798928" MODIFIED="1370194798928">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="MakeLinkToAnchorAction"/>
<attribute NAME="accelerator" VALUE=""/>
</node>
<node TEXT="Clear link anchor" ID="ID_1021914424" CREATED="1370194798928" MODIFIED="1370194798928">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ClearLinkAnchorAction"/>
<attribute NAME="accelerator" VALUE=""/>
</node>
<node TEXT="goto links" ID="ID_182715509" CREATED="1370194798928" MODIFIED="1370335805936">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="menu_key" VALUE="popup_goto_links"/>
</node>
</node>
<node TEXT="---" ID="ID_760988947" CREATED="1370194798928" MODIFIED="1370194798928">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Enter password" ID="ID_1920623716" CREATED="1370194798928" MODIFIED="1370194798928">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="EnterPassword"/>
</node>
<node TEXT="scripting" ID="ID_703212235" CREATED="1370194798928" MODIFIED="1370194798928">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="scripting"/>
<attribute NAME="plugin" VALUE="org.freeplane.plugin.script"/>
<attribute NAME="menu_key" VALUE="node_popup_scripting"/>
</node>
<node TEXT="---" ID="ID_1846395794" CREATED="1370194798928" MODIFIED="1370194798928">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Cut" ID="ID_207687297" CREATED="1370194798933" MODIFIED="1370194798933">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="CutAction"/>
<attribute NAME="accelerator" VALUE="control X"/>
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
<node TEXT="Copy node ID" ID="ID_1058995828" CREATED="1370194798934" MODIFIED="1370194798934">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="CopyIDAction"/>
</node>
<node TEXT="Copy node URI" ID="ID_1833296099" CREATED="1370194798934" MODIFIED="1370194798934">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="CopyNodeURIAction"/>
</node>
<node TEXT="Paste" ID="ID_1289482635" CREATED="1370194798934" MODIFIED="1370194798934">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="PasteAction"/>
<attribute NAME="accelerator" VALUE="control V"/>
</node>
<node TEXT="Sort children" ID="ID_58570021" CREATED="1370194798934" MODIFIED="1370194798934">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SortNodes"/>
</node>
<node TEXT="Undo" ID="ID_355735578" CREATED="1370194798934" MODIFIED="1370194798934">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="UndoAction"/>
</node>
<node TEXT="Redo" ID="ID_827556437" CREATED="1370194798934" MODIFIED="1370194798934">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="RedoAction"/>
</node>
</node>
<node TEXT="main_toolbar" FOLDED="true" POSITION="right" ID="ID_900199235" CREATED="1370194798934" MODIFIED="1370194798934">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="main_toolbar"/>
<node TEXT="---" ID="ID_1334331601" CREATED="1370194798934" MODIFIED="1370194798934">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="history" FOLDED="true" ID="ID_561773939" CREATED="1370194798934" MODIFIED="1370194798934">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="history"/>
<node TEXT="Go backward" ID="ID_74127203" CREATED="1370194798934" MODIFIED="1370194798934">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="BackAction"/>
</node>
<node TEXT="Go forward" ID="ID_1216477531" CREATED="1370194798935" MODIFIED="1370194798935">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ForwardAction"/>
</node>
</node>
<node TEXT="update" ID="ID_1100014383" CREATED="1370194798935" MODIFIED="1370194798935">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="update"/>
<attribute NAME="menu_key" VALUE="main_toolbar_update"/>
</node>
<node TEXT="main" FOLDED="true" ID="ID_1903421620" CREATED="1370194798935" MODIFIED="1370194798935">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="main"/>
<node TEXT="Previous map" ID="ID_1247564365" CREATED="1370194798935" MODIFIED="1370194798935">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="NavigationPreviousMapAction"/>
</node>
<node TEXT="Next map" ID="ID_1021884606" CREATED="1370194798935" MODIFIED="1370194798935">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="NavigationNextMapAction"/>
</node>
</node>
<node TEXT="zoom" ID="ID_1772632875" CREATED="1370194798935" MODIFIED="1370194798935">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="zoom"/>
<attribute NAME="menu_key" VALUE="main_toolbar_zoom"/>
</node>
<node TEXT="open" FOLDED="true" ID="ID_1297580046" CREATED="1370194798935" MODIFIED="1370194798935">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="open"/>
<node TEXT="Open saved map..." ID="ID_1136353202" CREATED="1370194798935" MODIFIED="1370194798935">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="OpenAction"/>
</node>
<node TEXT="New map" ID="ID_1943108283" CREATED="1370194798935" MODIFIED="1370194798935">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="NewMapAction"/>
</node>
<node TEXT="Save map" ID="ID_107612010" CREATED="1370194798935" MODIFIED="1370194798935">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SaveAction"/>
</node>
<node TEXT="Save map as..." ID="ID_722737773" CREATED="1370194798935" MODIFIED="1370194798935">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="SaveAsAction"/>
</node>
<node TEXT="Print map..." ID="ID_58065896" CREATED="1370194798935" MODIFIED="1370194798935">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="PrintAction"/>
</node>
<node TEXT="Close current map" ID="ID_783074872" CREATED="1370194798935" MODIFIED="1370194798935">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="CloseAction"/>
</node>
</node>
<node TEXT="---" ID="ID_1489814162" CREATED="1370194798936" MODIFIED="1370194798936">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="undo" FOLDED="true" ID="ID_1062589150" CREATED="1370194798936" MODIFIED="1370194798936">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="undo"/>
<node TEXT="Undo" ID="ID_343705893" CREATED="1370194798936" MODIFIED="1370194798936">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="UndoAction"/>
</node>
<node TEXT="Redo" ID="ID_1375080567" CREATED="1370194798936" MODIFIED="1370194798936">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="RedoAction"/>
</node>
</node>
<node TEXT="---" ID="ID_994470262" CREATED="1370194798941" MODIFIED="1370194798941">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Font" FOLDED="true" ID="ID_1756351063" CREATED="1370194798941" MODIFIED="1370194798941">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="font"/>
<attribute NAME="menu_key" VALUE="main_toolbar_font"/>
<node TEXT="toolbar_styles" ID="ID_1752346910" CREATED="1370194798942" MODIFIED="1370194798942">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="toolbar_styles"/>
<attribute NAME="menu_key" VALUE="main_toolbar_style"/>
</node>
<node TEXT="toolbar_fonts" ID="ID_1000724636" CREATED="1370194798942" MODIFIED="1370194798942">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="toolbar_fonts"/>
<attribute NAME="menu_key" VALUE="main_toolbar_font_name"/>
</node>
<node TEXT="toolbar_fonts" ID="ID_701977165" CREATED="1370194798942" MODIFIED="1370194798942">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE="toolbar_fonts"/>
<attribute NAME="menu_key" VALUE="main_toolbar_font_size"/>
</node>
<node TEXT="Bold" ID="ID_317014057" CREATED="1370194798942" MODIFIED="1370194798942">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="BoldAction"/>
</node>
<node TEXT="Italic" ID="ID_1605545726" CREATED="1370194798943" MODIFIED="1370194798943">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="ItalicAction"/>
</node>
</node>
<node TEXT="---" ID="ID_1378666411" CREATED="1370194798943" MODIFIED="1370194798943">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Text alignment" FOLDED="true">
<attribute NAME="type" VALUE="category"/>
<node TEXT="TextAlignAction.LEFT">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="TextAlignAction.LEFT"/>
</node>
<node TEXT="TextAlignAction.CENTER">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="TextAlignAction.CENTER"/>
</node>
<node TEXT="TextAlignAction.RIGHT">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="TextAlignAction.RIGHT"/>
</node>
</node>
<node TEXT="---">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="Add / remove cloud (default)" ID="ID_1763924306" CREATED="1370194798943" MODIFIED="1370194798943">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="CloudAction"/>
</node>
<node TEXT="Cloud color..." ID="ID_1395559934" CREATED="1370194798943" MODIFIED="1370194798943">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="CloudColorAction"/>
</node>
<node TEXT="---" ID="ID_324083546" CREATED="1370194798943" MODIFIED="1370194798943">
<attribute NAME="type" VALUE="separator"/>
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
<node TEXT="find" ID="ID_1981407568" CREATED="1370194798943" MODIFIED="1370194798943">
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
<node TEXT="Find and replace..." ID="ID_497245284" CREATED="1370194798944" MODIFIED="1370194798944">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="NodeListAction"/>
</node>
</node>
<node TEXT="Enter password" ID="ID_971496927" CREATED="1370194798944" MODIFIED="1370194798944">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE="EnterPassword"/>
</node>
</node>
</node>
</map>
