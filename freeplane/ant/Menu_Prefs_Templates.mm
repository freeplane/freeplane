<map version="freeplane 1.3.0">
<!--To view this file, download free mind mapping software Freeplane from http://freeplane.sourceforge.net -->
<attribute_registry SHOW_ATTRIBUTES="hide"/>
<node TEXT="Templates" LOCALIZED_STYLE_REF="AutomaticLayout.level.root" ID="ID_1723255651"><hook NAME="MapStyle">
    <conditional_styles>
        <conditional_style ACTIVE="true" STYLE_REF="combo" LAST="false">
            <attribute_compare_condition VALUE="combo" MATCH_CASE="true" MATCH_APPROXIMATELY="false" ATTRIBUTE="type" COMPARATION_RESULT="0" SUCCEED="true"/>
        </conditional_style>
        <conditional_style ACTIVE="true" STYLE_REF="choice" LAST="false">
            <attribute_compare_condition VALUE="choice" MATCH_CASE="true" MATCH_APPROXIMATELY="false" ATTRIBUTE="type" COMPARATION_RESULT="0" SUCCEED="true"/>
        </conditional_style>
        <conditional_style ACTIVE="true" STYLE_REF="font" LAST="false">
            <attribute_compare_condition VALUE="font" MATCH_CASE="true" MATCH_APPROXIMATELY="false" ATTRIBUTE="type" COMPARATION_RESULT="0" SUCCEED="true"/>
        </conditional_style>
        <conditional_style ACTIVE="true" STYLE_REF="number" LAST="false">
            <attribute_compare_condition VALUE="number" MATCH_CASE="true" MATCH_APPROXIMATELY="false" ATTRIBUTE="type" COMPARATION_RESULT="0" SUCCEED="true"/>
        </conditional_style>
        <conditional_style ACTIVE="true" STYLE_REF="path" LAST="false">
            <attribute_compare_condition VALUE="path" MATCH_CASE="true" MATCH_APPROXIMATELY="false" ATTRIBUTE="type" COMPARATION_RESULT="0" SUCCEED="true"/>
        </conditional_style>
        <conditional_style ACTIVE="true" STYLE_REF="remind_value" LAST="false">
            <attribute_compare_condition VALUE="remind_value" MATCH_CASE="true" MATCH_APPROXIMATELY="false" ATTRIBUTE="type" COMPARATION_RESULT="0" SUCCEED="true"/>
        </conditional_style>
        <conditional_style ACTIVE="true" STYLE_REF="separator" LAST="false">
            <attribute_compare_condition VALUE="separator" MATCH_CASE="true" MATCH_APPROXIMATELY="false" ATTRIBUTE="type" COMPARATION_RESULT="0" SUCCEED="true"/>
        </conditional_style>
        <conditional_style ACTIVE="true" STYLE_REF="group" LAST="false">
            <attribute_compare_condition VALUE="group" MATCH_CASE="true" MATCH_APPROXIMATELY="false" ATTRIBUTE="type" COMPARATION_RESULT="0" SUCCEED="true"/>
        </conditional_style>
        <conditional_style ACTIVE="true" STYLE_REF="color" LAST="false">
            <attribute_compare_condition VALUE="color" MATCH_CASE="true" MATCH_APPROXIMATELY="false" ATTRIBUTE="type" COMPARATION_RESULT="0" SUCCEED="true"/>
        </conditional_style>
        <conditional_style ACTIVE="true" STYLE_REF="string" LAST="false">
            <attribute_compare_condition VALUE="string" MATCH_CASE="true" MATCH_APPROXIMATELY="false" ATTRIBUTE="type" COMPARATION_RESULT="0" SUCCEED="true"/>
        </conditional_style>
        <conditional_style ACTIVE="true" STYLE_REF="tab" LAST="false">
            <attribute_compare_condition VALUE="tab" MATCH_CASE="true" MATCH_APPROXIMATELY="false" ATTRIBUTE="type" COMPARATION_RESULT="0" SUCCEED="true"/>
        </conditional_style>
        <conditional_style ACTIVE="true" STYLE_REF="text" LAST="false">
            <attribute_compare_condition VALUE="text" MATCH_CASE="true" MATCH_APPROXIMATELY="false" ATTRIBUTE="type" COMPARATION_RESULT="0" SUCCEED="true"/>
        </conditional_style>
        <conditional_style ACTIVE="true" STYLE_REF="category" LAST="true">
            <attribute_compare_condition VALUE="category" MATCH_CASE="true" MATCH_APPROXIMATELY="false" ATTRIBUTE="type" COMPARATION_RESULT="0" SUCCEED="true"/>
        </conditional_style>
        <conditional_style ACTIVE="true" STYLE_REF="action" LAST="true">
            <attribute_compare_condition VALUE="action" MATCH_CASE="true" MATCH_APPROXIMATELY="false" ATTRIBUTE="type" COMPARATION_RESULT="0" SUCCEED="true"/>
        </conditional_style>
        <conditional_style ACTIVE="true" STYLE_REF="radio_action" LAST="true">
            <attribute_compare_condition VALUE="radio_action" MATCH_CASE="true" MATCH_APPROXIMATELY="false" ATTRIBUTE="type" COMPARATION_RESULT="0" SUCCEED="true"/>
        </conditional_style>
        <conditional_style ACTIVE="true" STYLE_REF="submenu" LAST="true">
            <attribute_compare_condition VALUE="submenu" MATCH_CASE="true" MATCH_APPROXIMATELY="false" ATTRIBUTE="type" COMPARATION_RESULT="0" SUCCEED="true"/>
        </conditional_style>
    </conditional_styles>
    <properties show_icon_for_attributes="false" show_note_icons="true"/>

<map_styles>
<stylenode LOCALIZED_TEXT="styles.root_node">
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
<node TEXT="Instructions" POSITION="right" ID="ID_16954228" LINK="http://freeplane.sourceforge.net/wiki/index.php/How_to_Edit_Preferences_and_Menus" VSHIFT="-60">
<attribute NAME="type" VALUE="--ignore--"/>
<hook NAME="FreeNode"/>
</node>
<node TEXT="for Preferences" LOCALIZED_STYLE_REF="AutomaticLayout.level,1" POSITION="right" ID="ID_457483691">
<attribute NAME="type" VALUE="--ignore--"/>
<node TEXT="tab" ID="ID_571535954">
<attribute NAME="type" VALUE="tab"/>
<attribute NAME="name" VALUE=""/>
</node>
<node TEXT="separator" ID="ID_335899498">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE=""/>
</node>
<node TEXT="boolean" ID="ID_409443701">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE=""/>
</node>
<node TEXT="number" ID="ID_238686102">
<attribute NAME="type" VALUE="number"/>
<attribute NAME="name" VALUE=""/>
<attribute NAME="min" VALUE="1"/>
<attribute NAME="max" VALUE="50"/>
</node>
<node TEXT="combo" ID="ID_1992869935">
<attribute NAME="type" VALUE="combo"/>
<attribute NAME="name" VALUE=""/>
<node TEXT="1" OBJECT="java.lang.Long|1" ID="ID_572209665">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="1" OBJECT="org.freeplane.features.format.FormattedNumber|1"/>
</node>
<node TEXT="2" OBJECT="java.lang.Long|2" ID="ID_1221314968">
<attribute NAME="type" VALUE="choice"/>
<attribute NAME="value" VALUE="2" OBJECT="org.freeplane.features.format.FormattedNumber|2|#0.####"/>
</node>
</node>
<node TEXT="string" ID="ID_1859949883">
<attribute NAME="type" VALUE="string"/>
<attribute NAME="name" VALUE=""/>
</node>
<node TEXT="path" ID="ID_1448860584">
<attribute NAME="type" VALUE="path"/>
<attribute NAME="name" VALUE=""/>
</node>
<node TEXT="language" ID="ID_1723400994">
<attribute NAME="type" VALUE="languages"/>
<attribute NAME="name" VALUE=""/>
</node>
<node TEXT="group" ID="ID_955542498">
<attribute NAME="type" VALUE="group"/>
<attribute NAME="name" VALUE=""/>
</node>
<node TEXT="color" ID="ID_772320075">
<attribute NAME="type" VALUE="color"/>
<attribute NAME="name" VALUE=""/>
</node>
<node TEXT="font" ID="ID_1965612781">
<attribute NAME="type" VALUE="font"/>
<attribute NAME="name" VALUE=""/>
</node>
<node TEXT="text" ID="ID_489893342">
<attribute NAME="type" VALUE="text"/>
<attribute NAME="name" VALUE=""/>
</node>
</node>
<node TEXT="for Menus" LOCALIZED_STYLE_REF="AutomaticLayout.level,1" POSITION="left" ID="ID_1718414539">
<node TEXT="category" ID="ID_636805543">
<attribute NAME="type" VALUE="category"/>
<attribute NAME="name" VALUE=""/>
</node>
<node TEXT="submenu" ID="ID_406009783">
<attribute NAME="type" VALUE="submenu"/>
<attribute NAME="name" VALUE=""/>
<attribute NAME="name_ref" VALUE=""/>
</node>
<node TEXT="separator" ID="ID_1642777177">
<attribute NAME="type" VALUE="separator"/>
</node>
<node TEXT="action" ID="ID_1705523326">
<attribute NAME="type" VALUE="action"/>
<attribute NAME="action" VALUE=""/>
<attribute NAME="accelerator" VALUE=""/>
<attribute NAME="menu_key" VALUE=""/>
</node>
<node TEXT="radio_action" ID="ID_587556905">
<attribute NAME="type" VALUE="radio_action"/>
<attribute NAME="action" VALUE=""/>
</node>
</node>
</node>
</map>
