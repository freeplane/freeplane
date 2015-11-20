<map version="freeplane 1.3.0">
<!--To view this file, download free mind mapping software Freeplane from http://freeplane.sourceforge.net -->
<node TEXT="Preferences" FOLDED="false" ID="ID_1942740593"><hook NAME="MapStyle">
    <conditional_styles>
        <conditional_style ACTIVE="true" STYLE_REF="boolean" LAST="false">
            <attribute_contains_condition ATTRIBUTE="type" VALUE="boolean" MATCH_CASE="true" MATCH_APPROXIMATELY="false"/>
        </conditional_style>
        <conditional_style ACTIVE="true" STYLE_REF="combo" LAST="false">
            <attribute_contains_condition ATTRIBUTE="type" VALUE="combo" MATCH_CASE="true" MATCH_APPROXIMATELY="false"/>
        </conditional_style>
        <conditional_style ACTIVE="true" STYLE_REF="choice" LAST="false">
            <attribute_contains_condition ATTRIBUTE="type" VALUE="choice" MATCH_CASE="true" MATCH_APPROXIMATELY="false"/>
        </conditional_style>
        <conditional_style ACTIVE="true" STYLE_REF="font" LAST="false">
            <attribute_contains_condition ATTRIBUTE="type" VALUE="font" MATCH_CASE="true" MATCH_APPROXIMATELY="false"/>
        </conditional_style>
        <conditional_style ACTIVE="true" STYLE_REF="number" LAST="false">
            <attribute_contains_condition ATTRIBUTE="type" VALUE="number" MATCH_CASE="true" MATCH_APPROXIMATELY="false"/>
        </conditional_style>
        <conditional_style ACTIVE="true" STYLE_REF="path" LAST="false">
            <attribute_contains_condition ATTRIBUTE="type" VALUE="path" MATCH_CASE="true" MATCH_APPROXIMATELY="false"/>
        </conditional_style>
        <conditional_style ACTIVE="true" STYLE_REF="remind_value" LAST="false">
            <attribute_contains_condition ATTRIBUTE="type" VALUE="remind_value" MATCH_CASE="true" MATCH_APPROXIMATELY="false"/>
        </conditional_style>
        <conditional_style ACTIVE="true" STYLE_REF="separator" LAST="false">
            <attribute_contains_condition ATTRIBUTE="type" VALUE="separator" MATCH_CASE="true" MATCH_APPROXIMATELY="false"/>
        </conditional_style>
        <conditional_style ACTIVE="true" STYLE_REF="group" LAST="false">
            <attribute_contains_condition ATTRIBUTE="type" VALUE="group" MATCH_CASE="true" MATCH_APPROXIMATELY="false"/>
        </conditional_style>
        <conditional_style ACTIVE="true" STYLE_REF="color" LAST="false">
            <attribute_contains_condition ATTRIBUTE="type" VALUE="color" MATCH_CASE="true" MATCH_APPROXIMATELY="false"/>
        </conditional_style>
        <conditional_style ACTIVE="true" STYLE_REF="string" LAST="false">
            <attribute_contains_condition ATTRIBUTE="type" VALUE="string" MATCH_CASE="true" MATCH_APPROXIMATELY="false"/>
        </conditional_style>
        <conditional_style ACTIVE="true" STYLE_REF="tab" LAST="false">
            <attribute_contains_condition ATTRIBUTE="type" VALUE="tab" MATCH_CASE="true" MATCH_APPROXIMATELY="false"/>
        </conditional_style>
        <conditional_style ACTIVE="true" STYLE_REF="text" LAST="false">
            <attribute_contains_condition ATTRIBUTE="type" VALUE="text" MATCH_CASE="true" MATCH_APPROXIMATELY="false"/>
        </conditional_style>
    </conditional_styles>
    <properties show_icon_for_attributes="false" show_note_icons="false"/>

<map_styles>
<stylenode LOCALIZED_TEXT="styles.root_node">
<stylenode LOCALIZED_TEXT="styles.user-defined" POSITION="right">
<stylenode TEXT="boolean">
<icon BUILTIN="checked"/>
</stylenode>
<stylenode TEXT="combo">
<icon BUILTIN="list"/>
<cloud COLOR="#f0f0f0" SHAPE="ROUND_RECT"/>
</stylenode>
<stylenode TEXT="choice"/>
<stylenode TEXT="font"/>
<stylenode TEXT="number"/>
<stylenode TEXT="path"/>
<stylenode TEXT="remind_value"/>
<stylenode TEXT="separator">
<font BOLD="true"/>
</stylenode>
<stylenode TEXT="group">
<icon BUILTIN="folder"/>
</stylenode>
<stylenode TEXT="color">
<icon BUILTIN="licq"/>
</stylenode>
<stylenode TEXT="string">
<icon BUILTIN="edit"/>
</stylenode>
<stylenode TEXT="tab">
<cloud COLOR="#ccffcc" SHAPE="ARC"/>
</stylenode>
<stylenode TEXT="text">
<icon BUILTIN="info"/>
</stylenode>
</stylenode>
<stylenode LOCALIZED_TEXT="styles.predefined" POSITION="right">
<stylenode LOCALIZED_TEXT="default"/>
<stylenode LOCALIZED_TEXT="defaultstyle.details"/>
<stylenode LOCALIZED_TEXT="defaultstyle.note"/>
<stylenode LOCALIZED_TEXT="defaultstyle.floating">
<edge STYLE="hide_edge"/>
<cloud COLOR="#f0f0f0" SHAPE="ROUND_RECT"/>
</stylenode>
</stylenode>
</stylenode>
</map_styles>
</hook>
<node TEXT="Environment" POSITION="right" ID="ID_723831545">
<attribute NAME="type" VALUE="tab"/>
<attribute NAME="name" VALUE="Environment"/>
<node TEXT="Single program instance" FOLDED="true" ID="ID_1840688670">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="single_instance_mode"/>
<node TEXT="Open files in a running instance" ID="ID_885109739">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="single_instance"/>
</node>
<node TEXT="Avoid a second instance in any case" ID="ID_1895070379">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="single_instance_force"/>
</node>
</node>
<node TEXT="Language" FOLDED="true" ID="ID_768814664">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="language"/>
<node TEXT="Language" ID="ID_212812785">
<attribute NAME="type" VALUE="languages"/>
<attribute NAME="name" VALUE="language"/>
</node>
</node>
<node TEXT="Files" FOLDED="true" ID="ID_1398818983">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="files"/>
<node TEXT="Last opened list length" ID="ID_1451739024">
<attribute NAME="type" VALUE="number"/>
<attribute NAME="name" VALUE="last_opened_list_length"/>
<attribute NAME="min" VALUE="1"/>
<attribute NAME="max" VALUE="50"/>
</node>
<node TEXT="Automatically open last map" ID="ID_187279290">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="load_last_map"/>
</node>
<node TEXT="Load all last maps" ID="ID_1898517177">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="load_last_maps"/>
</node>
<node TEXT="Load last and new maps" ID="ID_1488586971">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="always_load_last_maps"/>
</node>
<node TEXT="Experimental file locking" ID="ID_298137736">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="experimental_file_locking_on"/>
</node>
</node>
<node TEXT="Load" FOLDED="true" ID="ID_1577638913">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="load"/>
<node TEXT="On load" FOLDED="true" ID="ID_410599">
<attribute NAME="type" VALUE="combo"/>
<attribute NAME="name" VALUE="load_folding"/>
<node TEXT="Fold all" ID="ID_477016346">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="always_fold_all_after_load"/>
</node>
<node TEXT="Load from map or fold all" ID="ID_185717318">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="load_folding_from_map_default_fold_all"/>
</node>
<node TEXT="Load from map or unfold all" ID="ID_1059944310">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="load_folding_from_map_default_unfold_all"/>
</node>
<node TEXT="Unfold all" ID="ID_997389407">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="always_unfold_all_after_load"/>
</node>
</node>
<node TEXT="Maximum number of displayed nodes" ID="ID_81186168">
<attribute NAME="type" VALUE="number"/>
<attribute NAME="name" VALUE="max_displayed_node_count"/>
<attribute NAME="min" VALUE="1"/>
</node>
</node>
<node TEXT="Save" ID="ID_124150095">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="save"/>
<node TEXT="Save folding" FOLDED="true" ID="ID_416087882">
<attribute NAME="type" VALUE="combo"/>
<attribute NAME="name" VALUE="save_folding"/>
<node TEXT="Never" ID="ID_256325781">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="never_save_folding"/>
</node>
<node TEXT="If map is changed" ID="ID_1959130057">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="save_folding_if_map_is_changed"/>
</node>
<node TEXT="Always" ID="ID_1006028189">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="always_save_folding"/>
</node>
</node>
<node TEXT="Save modification times" ID="ID_634390567">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="save_modification_times"/>
</node>
<node TEXT="Default Save Directory" ID="ID_1450786152">
<attribute NAME="type" VALUE="path"/>
<attribute NAME="name" VALUE="default_save_dir"/>
<attribute NAME="dir" VALUE="true"/>
</node>
</node>
<node TEXT="Automatic save" FOLDED="true" ID="ID_852360897">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="automatic_save"/>
<node TEXT="Time for automatic save" ID="ID_1498208772">
<attribute NAME="type" VALUE="number"/>
<attribute NAME="name" VALUE="time_for_automatic_save"/>
<attribute NAME="min" VALUE="0"/>
</node>
<node TEXT="Use single directory for backup files" ID="ID_899436679">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="single_backup_directory"/>
</node>
<node TEXT="Backup directory (if above option is selected)" ID="ID_1706930676">
<attribute NAME="type" VALUE="path"/>
<attribute NAME="name" VALUE="single_backup_directory_path"/>
<attribute NAME="dir" VALUE="true"/>
</node>
<node TEXT="Delete automatic saves at exit" ID="ID_703236630">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="delete_automatic_saves_at_exit"/>
</node>
<node TEXT="Number of different files for automatic save" ID="ID_634516064">
<attribute NAME="type" VALUE="number"/>
<attribute NAME="name" VALUE="number_of_different_files_for_automatic_save"/>
<attribute NAME="min" VALUE="0"/>
<attribute NAME="max" VALUE="25"/>
</node>
<node TEXT="Number of kept backup files" ID="ID_1192701799">
<attribute NAME="type" VALUE="number"/>
<attribute NAME="name" VALUE="backup_file_number"/>
<attribute NAME="min" VALUE="0"/>
<attribute NAME="max" VALUE="25"/>
</node>
</node>
<node TEXT="Export" FOLDED="true" ID="ID_954161030">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="export"/>
<node TEXT="Exported image resolution (in DPI)" ID="ID_697646517">
<attribute NAME="type" VALUE="number"/>
<attribute NAME="name" VALUE="exported_image_resolution_dpi"/>
<attribute NAME="min" VALUE="72"/>
<attribute NAME="max" VALUE="2400"/>
</node>
</node>
<node TEXT="Hyperlink types" FOLDED="true" ID="ID_1473490330">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="hyperlink_types"/>
<node TEXT="Links" FOLDED="true" ID="ID_311928787">
<attribute NAME="type" VALUE="combo"/>
<attribute NAME="name" VALUE="links"/>
<node TEXT="Relative" ID="ID_509696639">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="relative"/>
</node>
<node TEXT="Absolute" ID="ID_812869471">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="absolute"/>
</node>
</node>
</node>
<node TEXT="Cache" FOLDED="true" ID="ID_1505774869">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="cache"/>
<node TEXT="for images" FOLDED="true" ID="ID_964050006">
<attribute NAME="type" VALUE="combo"/>
<attribute NAME="name" VALUE="image_cache"/>
<node TEXT="Disable" ID="ID_1260513076">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="ic_disable"/>
</node>
<node TEXT="Use disk" ID="ID_40737444">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="ic_file"/>
</node>
<node TEXT="In RAM" ID="ID_1338158409">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="ic_ram"/>
</node>
</node>
</node>
<node TEXT="Program updates" FOLDED="true" ID="ID_378448205">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="updates"/>
<node TEXT="Check for updates on program start" ID="ID_456905076">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="check_updates_automatically"/>
</node>
</node>
<node TEXT="Policy" FOLDED="true" ID="ID_363428684">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="org.freeplane.plugin.bugreport"/>
<node TEXT="Policy" FOLDED="true" ID="ID_915122560">
<attribute NAME="type" VALUE="combo"/>
<attribute NAME="name" VALUE="org.freeplane.plugin.bugreport"/>
<node TEXT="Always send" ID="ID_471506559">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="org.freeplane.plugin.bugreport.allowed"/>
</node>
<node TEXT="Never send" ID="ID_90500310">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="org.freeplane.plugin.bugreport.denied"/>
</node>
<node TEXT="Show report dialog" ID="ID_1395562400">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="org.freeplane.plugin.bugreport.ask"/>
</node>
</node>
<node TEXT="Optional identifier to be sent" ID="ID_441206896">
<attribute NAME="type" VALUE="string"/>
<attribute NAME="name" VALUE="org.freeplane.plugin.bugreport.userid"/>
</node>
</node>
</node>
<node TEXT="Appearance" FOLDED="true" POSITION="right" ID="ID_37070726">
<attribute NAME="type" VALUE="tab"/>
<attribute NAME="name" VALUE="Appearance"/>
<node TEXT="Look and feel" FOLDED="true" ID="ID_844594313">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="look_and_feel"/>
<node TEXT="Look and Feel" ID="ID_1265760042">
<attribute NAME="type" VALUE="group"/>
<attribute NAME="name" VALUE="lookandfeel"/>
</node>
<node TEXT="Apply system screen resolution default" ID="ID_746558491">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="apply_system_screen_resolution"/>
</node>
<node TEXT="User defined screen resolution (dpi)" ID="ID_365827132">
<attribute NAME="type" VALUE="number"/>
<attribute NAME="name" VALUE="user_defined_screen_resolution"/>
<attribute NAME="min" VALUE="72"/>
<attribute NAME="max" VALUE="1200"/>
</node>
<node TEXT="Use default font for notes too" ID="ID_733638596">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="resources_use_default_font_for_notes_too"/>
</node>
<node TEXT="Remove top margin for notes" ID="ID_59875589">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="resources_use_margin_top_zero_for_notes"/>
</node>
<node TEXT="Maximum number of menu items" ID="ID_940027994">
<attribute NAME="type" VALUE="number"/>
<attribute NAME="name" VALUE="max_menu_item_count"/>
<attribute NAME="min" VALUE="10"/>
</node>
</node>
<node TEXT="Status line" FOLDED="true" ID="ID_646613151">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="status"/>
<node TEXT="Display node ID" ID="ID_108835084">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="display_node_id"/>
</node>
</node>
<node TEXT="Default colors" FOLDED="true" ID="ID_1557885832">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="default_colors"/>
<node TEXT="Revision color" ID="ID_259628453">
<attribute NAME="type" VALUE="color"/>
<attribute NAME="name" VALUE="revision_color"/>
</node>
<node TEXT="White background for printing" ID="ID_1279617230">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="printonwhitebackground"/>
</node>
<node TEXT="Standard Cloud color" ID="ID_692317954">
<attribute NAME="type" VALUE="color"/>
<attribute NAME="name" VALUE="standardcloudcolor"/>
</node>
<node TEXT="Presentation dimmer transparenty" ID="ID_1331987741">
<attribute NAME="type" VALUE="number"/>
<attribute NAME="name" VALUE="presentation_dimmer_transparency"/>
<attribute NAME="min" VALUE="0"/>
<attribute NAME="max" VALUE="255"/>
</node>
</node>
<node TEXT="Selection colors" FOLDED="true" ID="ID_1579556482">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="selection_colors"/>
<node TEXT="Display selected nodes in bubbles" ID="ID_1336656688">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="standarddrawrectangleforselection"/>
</node>
<node TEXT="Selected node bubble color" ID="ID_1908988134">
<attribute NAME="type" VALUE="color"/>
<attribute NAME="name" VALUE="standardselectednoderectanglecolor"/>
</node>
<node TEXT="Standard selected node color" ID="ID_1025277999">
<attribute NAME="type" VALUE="color"/>
<attribute NAME="name" VALUE="standardselectednodecolor"/>
</node>
</node>
<node TEXT="Root node appearance" FOLDED="true" ID="ID_185289096">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="root_node_appearance"/>
<node TEXT="Edges start from one point at root node" ID="ID_562395828">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="use_common_out_point_for_root_node"/>
</node>
</node>
<node TEXT="Antialias" FOLDED="true" ID="ID_90204332">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="anti_alias"/>
<node TEXT="Antialias" FOLDED="true" ID="ID_1892518977">
<attribute NAME="type" VALUE="combo"/>
<attribute NAME="name" VALUE="antialias"/>
<node TEXT="Antialias edges" ID="ID_801698319">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="antialias_edges"/>
</node>
<node TEXT="Antialias all" ID="ID_1863414969">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="antialias_all"/>
</node>
<node TEXT="No antialias" ID="ID_1907389980">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="antialias_none"/>
</node>
</node>
</node>
<node TEXT="Size limits" FOLDED="true" ID="ID_887796087">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="size_limits"/>
<node TEXT="Maximum shortened text width" ID="ID_1080402416">
<attribute NAME="type" VALUE="number"/>
<attribute NAME="name" VALUE="max_shortened_text_length"/>
</node>
<node TEXT="Max initial image width" ID="ID_571433786">
<attribute NAME="type" VALUE="number"/>
<attribute NAME="name" VALUE="max_image_width"/>
</node>
<node TEXT="Node tool tip width" ID="ID_835053857">
<attribute NAME="type" VALUE="number"/>
<attribute NAME="name" VALUE="toolTipManager.max_tooltip_width"/>
</node>
<node TEXT="Standard attribute key width" ID="ID_721222417">
<attribute NAME="type" VALUE="number"/>
<attribute NAME="name" VALUE="default_attribute_key_column_width"/>
<attribute NAME="min" VALUE="10"/>
</node>
<node TEXT="Standard attribute value width" ID="ID_1441167649">
<attribute NAME="type" VALUE="number"/>
<attribute NAME="name" VALUE="default_attribute_value_column_width"/>
<attribute NAME="min" VALUE="10"/>
</node>
</node>
<node TEXT="Connectors" FOLDED="true" ID="ID_908110780">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="connectors"/>
<node TEXT="Standard link color" ID="ID_965690541">
<attribute NAME="type" VALUE="color"/>
<attribute NAME="name" VALUE="standardlinkcolor"/>
</node>
<node TEXT="connector_alpha" ID="ID_1042325065">
<attribute NAME="type" VALUE="number"/>
<attribute NAME="name" VALUE="connector_alpha"/>
<attribute NAME="text" VALUE="edit_transparency_label"/>
<attribute NAME="min" VALUE="20"/>
<attribute NAME="max" VALUE="255"/>
</node>
<node TEXT="connector_shape" FOLDED="true" ID="ID_1605623486">
<attribute NAME="type" VALUE="combo"/>
<attribute NAME="name" VALUE="connector_shape"/>
<attribute NAME="text" VALUE="connector_shapes"/>
<node TEXT="LINE" ID="ID_836366585">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="LINE"/>
<attribute NAME="text" VALUE="ChangeConnectorShapeAction.LINE.text"/>
</node>
<node TEXT="LINEAR_PATH" ID="ID_1763438017">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="LINEAR_PATH"/>
<attribute NAME="text" VALUE="ChangeConnectorShapeAction.LINEAR_PATH.text"/>
</node>
<node TEXT="CUBIC_CURVE" ID="ID_1225462774">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="CUBIC_CURVE"/>
<attribute NAME="text" VALUE="ChangeConnectorShapeAction.CUBIC_CURVE.text"/>
</node>
<node TEXT="EDGE_LIKE" ID="ID_1571287345">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="EDGE_LIKE"/>
<attribute NAME="text" VALUE="ChangeConnectorShapeAction.EDGE_LIKE.text"/>
</node>
</node>
<node TEXT="connector_width" ID="ID_1534013234">
<attribute NAME="type" VALUE="number"/>
<attribute NAME="name" VALUE="connector_width"/>
<attribute NAME="text" VALUE="edit_width_label"/>
<attribute NAME="min" VALUE="1"/>
<attribute NAME="max" VALUE="32"/>
</node>
<node TEXT="Font family" ID="ID_557649283">
<attribute NAME="type" VALUE="font"/>
<attribute NAME="name" VALUE="label_font_family"/>
</node>
<node TEXT="Font size" ID="ID_1800154658">
<attribute NAME="type" VALUE="number"/>
<attribute NAME="name" VALUE="label_font_size"/>
<attribute NAME="min" VALUE="4"/>
<attribute NAME="max" VALUE="216"/>
</node>
<node TEXT="Paint connectors behind nodes" ID="ID_673192924">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="paint_connectors_behind"/>
</node>
</node>
<node TEXT="Edit in dialog" FOLDED="true" ID="ID_1513160130">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="edit_long_node_window"/>
<node TEXT="Buttons at the top" ID="ID_952752518">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="el__buttons_above"/>
</node>
<node TEXT="Position window below node" ID="ID_682710154">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="el__position_window_below_node"/>
</node>
<node TEXT="Min default window height" ID="ID_692742423">
<attribute NAME="type" VALUE="number"/>
<attribute NAME="name" VALUE="el__min_default_window_height"/>
</node>
<node TEXT="Max default window height" ID="ID_701741164">
<attribute NAME="type" VALUE="number"/>
<attribute NAME="name" VALUE="el__max_default_window_height"/>
</node>
<node TEXT="Min default window width" ID="ID_376949997">
<attribute NAME="type" VALUE="number"/>
<attribute NAME="name" VALUE="el__min_default_window_width"/>
</node>
<node TEXT="Max default window width" ID="ID_429760868">
<attribute NAME="type" VALUE="number"/>
<attribute NAME="name" VALUE="el__max_default_window_width"/>
</node>
</node>
<node TEXT="Outline view" FOLDED="true" ID="ID_1390048143">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="outline_view"/>
<node TEXT="Vertical distance" ID="ID_1110006236">
<attribute NAME="type" VALUE="length"/>
<attribute NAME="defaultUnit" VALUE="px"/>
<attribute NAME="name" VALUE="outline_vgap"/>
<attribute NAME="min" VALUE="1"/>
<attribute NAME="max" VALUE="1000"/>
<attribute NAME="step" VALUE="0.01"/>
</node>
<node TEXT="Horizontal distance" ID="ID_592653838">
<attribute NAME="type" VALUE="length"/>
<attribute NAME="defaultUnit" VALUE="px"/>
<attribute NAME="name" VALUE="outline_hgap"/>
<attribute NAME="min" VALUE="1"/>
<attribute NAME="max" VALUE="1000"/>
<attribute NAME="step" VALUE="0.01"/>
</node>
</node>
<node TEXT="Icons" FOLDED="true" ID="ID_277900726">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="icon_properties"/>
<node TEXT="Structured icon toolbar" ID="ID_510627650">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="structured_icon_toolbar"/>
</node>
</node>
</node>
<node TEXT="Keystrokes" FOLDED="true" POSITION="right" ID="ID_618364125">
<attribute NAME="type" VALUE="tab"/>
<attribute NAME="name" VALUE="Keystrokes"/>
<attribute NAME="layout" VALUE="right:max(40dlu;p), 4dlu, 80dlu, 7dlu,right:max(40dlu;p), 4dlu, 80dlu, 7dlu"/>
<node TEXT="Commands for the program" ID="ID_298335309">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="commands_for_the_program"/>
</node>
<node TEXT="use_ctrl_key" ID="ID_1873157630">
<attribute NAME="type" VALUE="text"/>
<attribute NAME="name" VALUE="use_ctrl_key"/>
</node>
<node TEXT="Icons in &quot;Select icon...&quot;" ID="ID_1158116466">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="icons"/>
</node>
</node>
<node TEXT="Behaviour" FOLDED="true" POSITION="right" ID="ID_630037237">
<attribute NAME="type" VALUE="tab"/>
<attribute NAME="name" VALUE="Behaviour"/>
<node TEXT="Behaviour" FOLDED="true" ID="ID_1310182301">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="behaviour"/>
<node TEXT="Place new branches" FOLDED="true" ID="ID_877092297">
<attribute NAME="type" VALUE="combo"/>
<attribute NAME="name" VALUE="placenewbranches"/>
<node TEXT="First" ID="ID_199036669">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="first"/>
</node>
<node TEXT="Last" ID="ID_467170704">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="last"/>
</node>
</node>
<node TEXT="Unfold node on paste" ID="ID_1679270241">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="unfold_on_paste"/>
</node>
<node TEXT="Fold on click inside" ID="ID_1874267432">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="fold_on_click_inside"/>
</node>
<node TEXT="Disable cursor move paper" ID="ID_1895979293">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="disable_cursor_move_paper"/>
</node>
<node TEXT="Folding symbol width" ID="ID_598554753">
<attribute NAME="type" VALUE="number"/>
<attribute NAME="name" VALUE="foldingsymbolwidth"/>
</node>
<node TEXT="Edit on double click" ID="ID_1121593000">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="edit_on_double_click"/>
</node>
<node TEXT="Grid gap size" ID="ID_1158278472">
<attribute NAME="type" VALUE="length"/>
<attribute NAME="defaultUnit" VALUE="px"/>
<attribute NAME="name" VALUE="grid_size"/>
<attribute NAME="min" VALUE="0"/>
<attribute NAME="max" VALUE="1000"/>
<attribute NAME="step" VALUE="0.01"/>
</node>
<node TEXT="Automatic map scrolling speed" ID="ID_904143728">
<attribute NAME="type" VALUE="number"/>
<attribute NAME="name" VALUE="scrolling_speed"/>
<attribute NAME="min" VALUE="1"/>
<attribute NAME="max" VALUE="30"/>
</node>
<node TEXT="Move note cursor to the end" ID="ID_956381715">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="goto_note_end_on_edit"/>
</node>
<node TEXT="On key type" FOLDED="true" ID="ID_564669547">
<attribute NAME="type" VALUE="combo"/>
<attribute NAME="name" VALUE="key_type_action"/>
<node TEXT="Overwrite content" ID="ID_1153246997">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="EDIT_CURRENT"/>
</node>
<node TEXT="Do nothing" ID="ID_886636310">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="IGNORE"/>
</node>
<node TEXT="Add sibling node" ID="ID_413294727">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="ADD_SIBLING"/>
</node>
<node TEXT="Add child node" ID="ID_642981674">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="ADD_CHILD"/>
</node>
</node>
</node>
<node TEXT="Data formatting and parsing" ID="ID_1442063739">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="data_formats"/>
<node TEXT="Recognize input of numbers and date-time" ID="ID_910768774">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="parse_data"/>
</node>
<node TEXT="Standard number format" ID="ID_1270880306">
<attribute NAME="type" VALUE="string"/>
<attribute NAME="name" VALUE="number_format"/>
</node>
<node TEXT="Standard date format" ID="ID_515913650">
<attribute NAME="type" VALUE="string"/>
<attribute NAME="name" VALUE="date_format"/>
</node>
<node TEXT="Standard date-time format" ID="ID_355710167">
<attribute NAME="type" VALUE="string"/>
<attribute NAME="name" VALUE="datetime_format"/>
</node>
<node TEXT="Locale for formats" ID="ID_876601772">
<attribute NAME="type" VALUE="languages"/>
<attribute NAME="name" VALUE="format_locale"/>
</node>
<node TEXT="Parse formulas" ID="ID_1432609744">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="parse_formulas"/>
</node>
<node TEXT="Parse LaTeX" ID="ID_1056456513">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="parse_latex"/>
</node>
</node>
<node TEXT="Search" FOLDED="true" ID="ID_1984074531">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="search"/>
<node TEXT="Compare as numbers" ID="ID_1362174029">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="compare_as_number"/>
</node>
<node TEXT="Threshold for approximate matching" ID="ID_1267144945">
<attribute NAME="type" VALUE="number"/>
<attribute NAME="name" VALUE="approximate_search_threshold"/>
<attribute NAME="min" VALUE="0.1"/>
<attribute NAME="max" VALUE="1.0"/>
<attribute NAME="step" VALUE="0.05"/>
</node>
</node>
<node TEXT="In-line node editor" FOLDED="true" ID="ID_557632654">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="inline_editor"/>
<node TEXT="Display inline editor for all new nodes" ID="ID_1014770406">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="display_inline_editor_for_all_new_nodes"/>
</node>
<node TEXT="Layout map during editing" ID="ID_171758367">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="layout_map_on_text_change"/>
</node>
<node TEXT="Enter confirms by default" ID="ID_1902257146">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="el__enter_confirms_by_default"/>
</node>
<node TEXT="Extra width step" ID="ID_1871144597">
<attribute NAME="type" VALUE="number"/>
<attribute NAME="name" VALUE="editor_extra_width"/>
<attribute NAME="min" VALUE="0"/>
<attribute NAME="max" VALUE="1000"/>
<attribute NAME="step" VALUE="40"/>
</node>
</node>
<node TEXT="Spell checker options" FOLDED="true" ID="ID_1278191355">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="spelling"/>
<node TEXT="Case sensitive" ID="ID_1306174035">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="spelling_opt_case_sensitive"/>
</node>
<node TEXT="Ignore all upper case words." ID="ID_203534582">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="spelling_opt_ignore_all_caps_words"/>
</node>
<node TEXT="Ignore capital letters at word begin" ID="ID_1714670354">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="spelling_opt_ignore_capitalization"/>
</node>
<node TEXT="Ignore words with numbers" ID="ID_495998983">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="spelling_opt_ignore_words_with_numbers"/>
</node>
<node TEXT="Maximum count of suggestions in the dialog" ID="ID_1873561798">
<attribute NAME="type" VALUE="number"/>
<attribute NAME="name" VALUE="spelling_opt_suggestions_limit_dialog"/>
<attribute NAME="min" VALUE="0"/>
</node>
<node TEXT="Maximum count of suggestions in the menu" ID="ID_748379690">
<attribute NAME="type" VALUE="number"/>
<attribute NAME="name" VALUE="spelling_opt_suggestions_limit_menu"/>
<attribute NAME="min" VALUE="0"/>
</node>
</node>
<node TEXT="Confirmations" FOLDED="true" ID="ID_1302130978">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="notifications"/>
<node TEXT="Use rich text for pasted nodes" ID="ID_815038150">
<attribute NAME="type" VALUE="remind_value"/>
<attribute NAME="name" VALUE="remind_use_rich_text_in_new_nodes"/>
</node>
<node TEXT="Delete nodes without confirmation?" ID="ID_1610007906">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="delete_nodes_without_question"/>
</node>
<node TEXT="Cut nodes without confirmation?" ID="ID_511499285">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="cut_nodes_without_question"/>
</node>
<node TEXT="Remove notes without question?" ID="ID_931265655">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="remove_notes_without_question"/>
</node>
</node>
<node TEXT="Selection method" FOLDED="true" ID="ID_503444195">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="selection_method"/>
<node TEXT="Selection method" FOLDED="true" ID="ID_1686471954">
<attribute NAME="type" VALUE="combo"/>
<attribute NAME="name" VALUE="selection_method"/>
<node TEXT="Direct" ID="ID_168679294">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="selection_method_direct"/>
</node>
<node TEXT="Delayed" ID="ID_1705316999">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="selection_method_delayed"/>
</node>
<node TEXT="By click" ID="ID_1394653756">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="selection_method_by_click"/>
</node>
</node>
<node TEXT="Time for delayed selection" ID="ID_178397801">
<attribute NAME="type" VALUE="number"/>
<attribute NAME="name" VALUE="time_for_delayed_selection"/>
</node>
<node TEXT="Center selected node automatically" ID="ID_1909142239">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="center_selected_node"/>
</node>
<node TEXT="Slow scrolling to selected node" ID="ID_1188931652">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="slow_scroll_selected_node"/>
</node>
</node>
<node TEXT="Mouse wheel" FOLDED="true" ID="ID_503866131">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="mouse_wheel"/>
<node TEXT="Speed" ID="ID_1770927331">
<attribute NAME="type" VALUE="number"/>
<attribute NAME="name" VALUE="wheel_velocity"/>
</node>
</node>
<node TEXT="Scrollbar" FOLDED="true" ID="ID_1893063596">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="scrollbar"/>
<node TEXT="Speed" ID="ID_107891399">
<attribute NAME="type" VALUE="number"/>
<attribute NAME="name" VALUE="scrollbar_increment"/>
</node>
</node>
<node TEXT="Tooltip times" FOLDED="true" ID="ID_460104421">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="tooltip"/>
<node TEXT="Display tool tips" ID="ID_816419280">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="show_node_tooltips"/>
</node>
<node TEXT="Initial delay, ms" ID="ID_111812950">
<attribute NAME="type" VALUE="number"/>
<attribute NAME="name" VALUE="toolTipManager.initialDelay"/>
<attribute NAME="min" VALUE="0"/>
</node>
<node TEXT="Dismiss delay, ms" ID="ID_1805226613">
<attribute NAME="type" VALUE="number"/>
<attribute NAME="name" VALUE="toolTipManager.dismissDelay"/>
<attribute NAME="min" VALUE="0"/>
</node>
<node TEXT="Reshow delay, ms" ID="ID_1829907889">
<attribute NAME="type" VALUE="number"/>
<attribute NAME="name" VALUE="toolTipManager.reshowDelay"/>
<attribute NAME="min" VALUE="0"/>
</node>
<node TEXT="Display node styles in tool tips" ID="ID_593396324">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="show_styles_in_tooltip"/>
</node>
</node>
<node TEXT="Undo" FOLDED="true" ID="ID_1519744325">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="undo"/>
<node TEXT="Undo levels" ID="ID_165120822">
<attribute NAME="type" VALUE="number"/>
<attribute NAME="name" VALUE="undo_levels"/>
</node>
</node>
<node TEXT="Rich-Text Editor" FOLDED="true" ID="ID_104180183">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="RichTextEditor"/>
<node TEXT="Default paste mode" FOLDED="true" ID="ID_802213301">
<attribute NAME="type" VALUE="combo"/>
<attribute NAME="name" VALUE="simplyhtml.default_paste_mode"/>
<node TEXT="Paste as HTML" ID="ID_579434040">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="PASTE_HTML"/>
</node>
<node TEXT="Paste as plain-text" ID="ID_1807400987">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="PASTE_PLAIN_TEXT"/>
</node>
</node>
</node>
</node>
<node TEXT="HTML" FOLDED="true" POSITION="right" ID="ID_1401105906">
<attribute NAME="type" VALUE="tab"/>
<attribute NAME="name" VALUE="HTML"/>
<node TEXT="Browser" FOLDED="true" ID="ID_479521225">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="browser"/>
<node TEXT="Default browser command Windows Nt" ID="ID_154419270">
<attribute NAME="type" VALUE="string"/>
<attribute NAME="name" VALUE="default_browser_command_windows_nt"/>
</node>
<node TEXT="Default browser command Windows 9x" ID="ID_723429836">
<attribute NAME="type" VALUE="string"/>
<attribute NAME="name" VALUE="default_browser_command_windows_9x"/>
</node>
<node TEXT="Default browser command other OS" ID="ID_1313532976">
<attribute NAME="type" VALUE="string"/>
<attribute NAME="name" VALUE="default_browser_command_other_os"/>
</node>
<node TEXT="Default browser command Mac" ID="ID_554942195">
<attribute NAME="type" VALUE="string"/>
<attribute NAME="name" VALUE="default_browser_command_mac"/>
</node>
</node>
<node TEXT="Html Export" FOLDED="true" ID="ID_572384290">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="html_export"/>
<node TEXT="Html export folding" FOLDED="true" ID="ID_122608747">
<attribute NAME="type" VALUE="combo"/>
<attribute NAME="name" VALUE="html_export_folding"/>
<node TEXT="No folding" ID="ID_1569400510">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="html_export_no_folding"/>
</node>
<node TEXT="Fold currently folded" ID="ID_22456022">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="html_export_fold_currently_folded"/>
</node>
<node TEXT="Fold all" ID="ID_1433182149">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="html_export_fold_all"/>
</node>
<node TEXT="Based On Headings" ID="ID_1665090138">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="html_export_based_on_headings"/>
</node>
</node>
<node TEXT="Export icons in Html" ID="ID_1975340209">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="export_icons_in_html"/>
</node>
</node>
<node TEXT="Html Import" FOLDED="true" ID="ID_1883761755">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="html_import"/>
<node TEXT="Import HTML as node structure" ID="ID_1958637376">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="structured_html_import"/>
</node>
</node>
</node>
<node TEXT="Plugins" POSITION="right" ID="ID_460770893">
<attribute NAME="type" VALUE="tab"/>
<attribute NAME="name" VALUE="plugins"/>
</node>
</node>
</map>
