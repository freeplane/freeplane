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
		<font NAME="Arial" SIZE="12" BOLD="false" ITALIC="false"/>
		</stylenode>
		<stylenode LOCALIZED_TEXT="styles.ok">
		<icon BUILTIN="button_ok"/>
		</stylenode>
		<stylenode LOCALIZED_TEXT="styles.needs_action">
		<icon BUILTIN="messagebox_warning"/>
		</stylenode>
		<stylenode LOCALIZED_TEXT="styles.floating_node">
		<cloud COLOR="#ffffff"/>
		<edge STYLE="hide_edge"/>
		</stylenode>
		<stylenode LOCALIZED_TEXT="styles.topic" COLOR="#18898b" STYLE="fork">
		<font NAME="Liberation Sans" SIZE="12" BOLD="true"/>
		</stylenode>
		<stylenode LOCALIZED_TEXT="styles.subtopic" COLOR="#cc3300" STYLE="fork">
		<font NAME="Liberation Sans" SIZE="12" BOLD="true"/>
		</stylenode>
		<stylenode LOCALIZED_TEXT="styles.subsubtopic" COLOR="#669900">
		<font NAME="Liberation Sans" SIZE="12" BOLD="true"/>
		</stylenode>
		<stylenode LOCALIZED_TEXT="styles.connection" COLOR="#606060" STYLE="fork">
		<font NAME="Arial" SIZE="10" BOLD="false"/>
		</stylenode>
		<stylenode LOCALIZED_TEXT="styles.important" COLOR="#ff0000">
		<icon BUILTIN="yes"/>
		<font NAME="Liberation Sans" SIZE="12"/>
		</stylenode>
		<stylenode LOCALIZED_TEXT="styles.question">
		<icon BUILTIN="help"/>
		<font NAME="Aharoni" SIZE="12"/>
		</stylenode>
		<stylenode LOCALIZED_TEXT="styles.key" COLOR="#996600">
		<icon BUILTIN="password"/>
		<font NAME="Liberation Sans" SIZE="12" BOLD="false"/>
		</stylenode>
		<stylenode LOCALIZED_TEXT="styles.idea">
		<icon BUILTIN="idea"/>
		</stylenode>
		<stylenode LOCALIZED_TEXT="styles.note" COLOR="#990000">
		<font NAME="Liberation Sans" SIZE="12"/>
		</stylenode>
		<stylenode LOCALIZED_TEXT="styles.date" COLOR="#0033ff">
		<icon BUILTIN="calendar"/>
		<font NAME="Liberation Sans" SIZE="12"/>
		</stylenode>
		<stylenode LOCALIZED_TEXT="styles.website" COLOR="#006633">
		<font NAME="Liberation Sans" SIZE="12"/>
		</stylenode>
		<stylenode LOCALIZED_TEXT="styles.list" COLOR="#cc6600">
		<icon BUILTIN="list"/>
		<font NAME="Liberation Sans" SIZE="12" BOLD="true"/>
		</stylenode>
		<stylenode LOCALIZED_TEXT="styles.quotation" COLOR="#338800" STYLE="fork">
		<font NAME="Liberation Sans" SIZE="12" BOLD="false" ITALIC="false"/>
		</stylenode>
		<stylenode LOCALIZED_TEXT="styles.definition" COLOR="#666600">
		<font NAME="Liberation Sans" SIZE="12" BOLD="false"/>
		</stylenode>
		<stylenode LOCALIZED_TEXT="styles.description" COLOR="#996600">
		<font NAME="Liberation Sans" SIZE="12" BOLD="false"/>
		</stylenode>
		<stylenode LOCALIZED_TEXT="styles.pending" COLOR="#b3b95c">
		<font NAME="Liberation Sans" SIZE="12"/>
		</stylenode>
		</stylenode>
		<stylenode LOCALIZED_TEXT="styles.AutomaticLayout" POSITION="right">
		<stylenode LOCALIZED_TEXT="AutomaticLayout.level.root" COLOR="#000000">
		<font SIZE="20"/>
		</stylenode>
		<stylenode LOCALIZED_TEXT="AutomaticLayout.level,1" COLOR="#0033ff">
		<font SIZE="18"/>
		</stylenode>
		<stylenode LOCALIZED_TEXT="AutomaticLayout.level,2" COLOR="#00b439">
		<font SIZE="16"/>
		</stylenode>
		<stylenode LOCALIZED_TEXT="AutomaticLayout.level,3" COLOR="#990000">
		<font SIZE="14"/>
		</stylenode>
		<stylenode LOCALIZED_TEXT="AutomaticLayout.level,4" COLOR="#111111">
		<font SIZE="12"/>
		</stylenode>
		</stylenode>
		<stylenode LOCALIZED_TEXT="styles.user-defined" POSITION="right"/>
		</stylenode>
	</map_styles>
</hook>
<hook NAME="FlexibleLayout" VALUE="CHILDREN"/>
</node>
</map>
