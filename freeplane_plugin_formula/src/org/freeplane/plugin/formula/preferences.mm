<map version="freeplane 1.3.0">
<!--To view this file, download free mind mapping software Freeplane from http://freeplane.sourceforge.net -->
<attribute_registry SHOW_ATTRIBUTES="hide"/>
<node TEXT="Formula Preferences" ID="ID_647512264" CREATED="1370289419953" MODIFIED="1370289482022"><hook NAME="MapStyle">
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
<node TEXT="Plugins" POSITION="right" ID="ID_591627084" CREATED="1370289419953" MODIFIED="1370290599743">
<attribute NAME="type" VALUE="tab"/>
<attribute NAME="name" VALUE="plugins"/>
<node TEXT="Formula" ID="ID_1783515461" CREATED="1370289419958" MODIFIED="1370290595454">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="formula"/>
<node TEXT="Disable Formula evaluation" ID="ID_1117632012" CREATED="1370289419959" MODIFIED="1370290718637">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="formula_disable_plugin"/>
</node>
<node TEXT="Disable Formula evaluation caching" ID="ID_662998133" CREATED="1370289419959" MODIFIED="1370290733060">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="formula_disable_caching"/>
</node>
<node TEXT="Highlight Formulas" ID="ID_1584146024" CREATED="1370289419960" MODIFIED="1370290754775">
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="highlight_formulas"/>
</node>
</node>
</node>
</node>
</map>
