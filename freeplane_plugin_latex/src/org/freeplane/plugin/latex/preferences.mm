<map version="freeplane 1.3.0">
<!--To view this file, download free mind mapping software Freeplane from http://freeplane.sourceforge.net -->
<node TEXT="LaTeX Preferences" FOLDED="false" ID="ID_647512264"><hook NAME="MapStyle">
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
<node TEXT="Plugins" POSITION="right" ID="ID_591627084">
<attribute NAME="type" VALUE="tab"/>
<attribute NAME="name" VALUE="plugins"/>
<node TEXT="Latex" ID="ID_1783515461">
<attribute NAME="type" VALUE="separator"/>
<attribute NAME="name" VALUE="latex"/>
<node TEXT="Common Macros" ID="ID_11961549">
<attribute_layout NAME_WIDTH="38" VALUE_WIDTH="85"/>
<attribute NAME="type" VALUE="textbox"/>
<attribute NAME="name" VALUE="latex_macros"/>
<attribute NAME="lines" VALUE="5" OBJECT="org.freeplane.features.format.FormattedNumber|5"/>
</node>
<node TEXT="LaTeX Editor Font" ID="ID_121453512">
<attribute_layout NAME_WIDTH="38" VALUE_WIDTH="107"/>
<attribute NAME="type" VALUE="font"/>
<attribute NAME="name" VALUE="latex_editor_font"/>
</node>
<node TEXT="LaTeX Editor Font Size" ID="ID_302068721">
<attribute_layout NAME_WIDTH="38" VALUE_WIDTH="136"/>
<attribute NAME="type" VALUE="number"/>
<attribute NAME="name" VALUE="latex_editor_font_size"/>
<attribute NAME="min" VALUE="4" OBJECT="org.freeplane.features.format.FormattedNumber|4"/>
<attribute NAME="max" VALUE="216" OBJECT="org.freeplane.features.format.FormattedNumber|216"/>
</node>
<node TEXT="Disable LaTeX Editor" ID="ID_326771379">
<attribute_layout NAME_WIDTH="38" VALUE_WIDTH="107"/>
<attribute NAME="type" VALUE="boolean"/>
<attribute NAME="name" VALUE="latex_disable_editor"/>
</node>
</node>
</node>
</node>
</map>
