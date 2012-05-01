<map version="0.9.0">
<!--To view this file, download free mind mapping software Freeplane from http://freeplane.sourceforge.net -->
<node TEXT="New Concept Map">
<hook NAME="MapStyle" max_node_width="600">
    <conditional_styles>
        <conditional_style ACTIVE="true" LOCALIZED_STYLE_REF="styles.connection">
            <node_periodic_level_condition PERIOD="2" REMAINDER="1"/>
        </conditional_style>
        <conditional_style ACTIVE="true" LOCALIZED_STYLE_REF="styles.topic">
            <node_level_condition VALUE="2" IGNORE_CASE="true" COMPARATION_RESULT="0" SUCCEED="true"/>
        </conditional_style>
        <conditional_style ACTIVE="true" LOCALIZED_STYLE_REF="styles.subtopic">
            <node_level_condition VALUE="4" IGNORE_CASE="true" COMPARATION_RESULT="0" SUCCEED="true"/>
        </conditional_style>
        <conditional_style ACTIVE="true" LOCALIZED_STYLE_REF="styles.subsubtopic">
            <node_level_condition VALUE="6" IGNORE_CASE="true" COMPARATION_RESULT="0" SUCCEED="true"/>
        </conditional_style>
    </conditional_styles>
	<map_styles>
		<stylenode LOCALIZED_TEXT="styles.root_node">
		<stylenode LOCALIZED_TEXT="styles.predefined" POSITION="right">
		<stylenode LOCALIZED_TEXT="default" COLOR="#000000" STYLE="fork">
		<font NAME="Arial" SIZE="10" BOLD="false" ITALIC="false"/>
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
		<stylenode LOCALIZED_TEXT="styles.connection" COLOR="#606060" STYLE="fork">
		<font NAME="Arial" SIZE="8" BOLD="false"/>
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
</node>
</map>
