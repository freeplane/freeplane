<map version="freeplane 1.3.0">
<!--To view this file, download free mind mapping software Freeplane from http://freeplane.sourceforge.net -->
<attribute_registry SHOW_ATTRIBUTES="hide"/>
<node TEXT="Script Preferences" FOLDED="false" ID="ID_776863743"><hook NAME="MapStyle">
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
    <properties show_icon_for_attributes="false"/>

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
<node TEXT="Plugins" POSITION="right" ID="ID_1156983468">
<attribute NAME="type" VALUE="tab"/>
<attribute NAME="name" VALUE="plugins"/>
<node TEXT="Scripting" ID="ID_301669529">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="scripting"/>
<node TEXT="Script Execution enabled" ID="ID_1919787107">
<attribute NAME="type" VALUE="remind_value"/>
<attribute NAME="name" VALUE="execute_scripts_without_asking"/>
</node>
<node TEXT="Permit file/read operations" ID="ID_670476941">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="execute_scripts_without_file_restriction"/>
</node>
<node TEXT="Permit file/write operations" ID="ID_357965285">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="execute_scripts_without_write_restriction"/>
</node>
<node TEXT="Permit network operations" ID="ID_195127580">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="execute_scripts_without_network_restriction"/>
</node>
<node TEXT="Permit to execute other applications" ID="ID_1113896850">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="execute_scripts_without_exec_restriction"/>
</node>
<node TEXT="Trust signed scripts" ID="ID_1220206880">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="signed_script_are_trusted"/>
</node>
<node TEXT="Optional user key alias for signing" ID="ID_613870876">
<attribute NAME="type" VALUE="string"/>
<attribute NAME="name" VALUE="script_user_key_name_for_signing"/>
</node>
<node TEXT="File extensions not to be compiled" ID="ID_181914556">
<attribute NAME="type" VALUE="string"/>
<attribute NAME="name" VALUE="script_compilation_disabled_extensions"/>
</node>
<node TEXT="Script search path" ID="ID_425351509">
<attribute NAME="type" VALUE="string"/>
<attribute NAME="name" VALUE="script_directories"/>
</node>
<node TEXT="Script Classpath" ID="ID_1675835784">
<attribute NAME="type" VALUE="string"/>
<attribute NAME="name" VALUE="script_classpath"/>
</node>
<node TEXT="Groovy Editor Font" ID="ID_890021469">
<icon BUILTIN="edit"/>
<attribute NAME="type" VALUE="font"/>
<attribute NAME="name" VALUE="groovy_editor_font"/>
</node>
<node TEXT="Groovy Editor Font Size" ID="ID_1178357305">
<icon BUILTIN="edit"/>
<attribute NAME="type" VALUE="number"/>
<attribute NAME="name" VALUE="groovy_editor_font_size"/>
<attribute NAME="min" VALUE="4" OBJECT="org.freeplane.features.format.FormattedNumber|4"/>
<attribute NAME="max" VALUE="216" OBJECT="org.freeplane.features.format.FormattedNumber|216"/>
</node>
</node>
</node>
</node>
</map>
