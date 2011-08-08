<map version="0.9.0">
<!--To view this file, download free mind mapping software Freeplane from http://freeplane.sourceforge.net -->
<attribute_registry SHOW_ATTRIBUTES="hide">
    <attribute_name VISIBLE="true" NAME="See also"/>
    <attribute_name VISIBLE="true" NAME="a2"/>
</attribute_registry>
<node TEXT="Documentation&#xa;Freeplane 1.2" STYLE_REF="MainMenuAccent" FOLDED="false" ID="ID_1723255651" CREATED="1283093380553" MODIFIED="1312572955682" TEXT_SHORTENED="true">
<icon BUILTIN="bee"/>
<hook NAME="MapStyle" zoom="0.909" max_node_width="600">
    <conditional_styles>
        <conditional_style ACTIVE="false" STYLE_REF="Revision" LAST="false">
            <time_condition_modified_after DATE="1312451668603"/>
        </conditional_style>
        <conditional_style ACTIVE="true" STYLE_REF="Revision" LAST="false">
            <time_condition_modified_after DATE="1312651262739"/>
        </conditional_style>
    </conditional_styles>
    <properties show_notes_in_map="false"/>
<map_styles>
<stylenode LOCALIZED_TEXT="styles.root_node">
<stylenode LOCALIZED_TEXT="styles.predefined" POSITION="right">
<stylenode LOCALIZED_TEXT="default"/>
<stylenode LOCALIZED_TEXT="defaultstyle.details"/>
<stylenode LOCALIZED_TEXT="defaultstyle.note"/>
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
<stylenode LOCALIZED_TEXT="styles.user-defined" POSITION="right">
<stylenode TEXT="Definition">
<font ITALIC="true"/>
</stylenode>
<stylenode TEXT="Method"/>
<stylenode TEXT="OptionalValue" COLOR="#cc3300"/>
<stylenode TEXT="Procedure" COLOR="#006666"/>
<stylenode TEXT="Exception">
<icon BUILTIN="messagebox_warning"/>
</stylenode>
<stylenode TEXT="Refine">
<icon BUILTIN="xmag"/>
</stylenode>
<stylenode TEXT="ToNote">
<icon BUILTIN="yes"/>
</stylenode>
<stylenode TEXT="Example">
<icon BUILTIN="../AttributesView"/>
</stylenode>
<stylenode TEXT="MainMenu" BACKGROUND_COLOR="#99ffcc" STYLE="bubble"/>
<stylenode TEXT="MainMenuAccent" BACKGROUND_COLOR="#33ffcc" STYLE="bubble">
<font BOLD="true"/>
</stylenode>
<stylenode TEXT="SubMenu" STYLE="bubble"/>
<stylenode TEXT="MenuGroupLabel" STYLE="bubble">
<edge COLOR="#66ccff" WIDTH="2"/>
<attribute_layout NAME_WIDTH="78" VALUE_WIDTH="116"/>
</stylenode>
<stylenode TEXT="Title" COLOR="#ffffff" BACKGROUND_COLOR="#009999" STYLE="bubble">
<font BOLD="true"/>
<edge STYLE="hide_edge"/>
</stylenode>
<stylenode TEXT="IsChecked" COLOR="#cc3300">
<icon BUILTIN="button_ok"/>
</stylenode>
<stylenode TEXT="UnChecked" COLOR="#cc3300">
<icon BUILTIN="button_cancel"/>
</stylenode>
<stylenode TEXT="Revision">
<icon BUILTIN="info"/>
</stylenode>
</stylenode>
<stylenode LOCALIZED_TEXT="styles.topic" POSITION="right" COLOR="#18898b" STYLE="fork">
<font NAME="Liberation Sans" SIZE="12" BOLD="true"/>
</stylenode>
<stylenode LOCALIZED_TEXT="styles.subtopic" POSITION="right" COLOR="#cc3300" STYLE="fork">
<font NAME="Liberation Sans" SIZE="12" BOLD="true"/>
</stylenode>
<stylenode LOCALIZED_TEXT="styles.subsubtopic" POSITION="right" COLOR="#669900">
<font NAME="Liberation Sans" SIZE="12" BOLD="true"/>
</stylenode>
<stylenode LOCALIZED_TEXT="styles.important" POSITION="right">
<icon BUILTIN="yes"/>
</stylenode>
</stylenode>
</map_styles>
</hook>
<hook NAME="FirstGroupNode"/>
<hook NAME="accessories/plugins/CreationModificationPlugin.properties"/>
<node TEXT="Preparation" STYLE_REF="Title" POSITION="left" ID="ID_1405760079" CREATED="1286914216123" MODIFIED="1312553660887">
<icon BUILTIN="gohome"/>
</node>
<node TEXT="Freeplane menu" STYLE_REF="Title" POSITION="right" ID="ID_332175012" CREATED="1286914216123" MODIFIED="1312552920002">
<icon BUILTIN="wizard"/>
</node>
<node TEXT="File" STYLE_REF="MainMenu" FOLDED="true" POSITION="right" ID="ID_1242926404" CREATED="1310134532663" MODIFIED="1311402702137" TEXT_SHORTENED="true">
<attribute_layout NAME_WIDTH="37" VALUE_WIDTH="77"/>
<attribute NAME="Chapter" VALUE="1,2,6,8"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MainMenu" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To handle mind maps.
    </p>
  </body>
</html></richcontent>
<node TEXT="New [New standard map]" ID="ID_1310307853" CREATED="1310134532663" MODIFIED="1311402583467" TEXT_SHORTENED="true">
<icon BUILTIN="../filenew"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">Creates a new Freeplane map. This map resides only in memory until it is saved.</span></font>
    </p>
  </body>
</html></richcontent>
<hook NAME="FirstGroupNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="1"/>
</node>
<node TEXT="New map from template ..." ID="ID_1630948273" CREATED="1310134532663" MODIFIED="1311402754475" TEXT_SHORTENED="true">
<icon BUILTIN="../new_map_"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Opens dialog to choose a map style from which a new map is created. <font face="SansSerif, sans-serif" color="#000000">This map resides only in memory until it is saved.</font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
</node>
<node TEXT="Create encrypted map..." FOLDED="true" ID="ID_1015806590" CREATED="1310134532663" MODIFIED="1311405939478" TEXT_SHORTENED="true">
<icon BUILTIN="../lock"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif">Prompts to enter a password required to open the map.</font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="8"/>
<node TEXT="" ID="ID_43524792" CREATED="1310756995899" MODIFIED="1310757316215" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Standard this option is diabled. To enable it, check:
    </p>
    <ul>
      <li>
        <i>Tools &gt; Preferences &gt; Environment &gt; Files &gt; Experimental File Locking</i>
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="New map" ID="ID_177723183" CREATED="1310835372274" MODIFIED="1311402754521">
<hook NAME="SummaryNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
</node>
<node TEXT="Save as..[Save map as...]." ID="ID_449363060" CREATED="1310134532663" MODIFIED="1311402754553" TEXT_SHORTENED="true">
<icon BUILTIN="../filesaveas"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">Opens a dialog to save an existing map, which has previously been saved, using a new file name.</span></font>
    </p>
  </body>
</html></richcontent>
<hook NAME="FirstGroupNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
</node>
<node TEXT="Save [Save current map]" ID="ID_1253878525" CREATED="1310134532663" MODIFIED="1311402615541" TEXT_SHORTENED="true">
<icon BUILTIN="../filesave"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">Save a map that has alrerady been created. If you are saving the map for the first time, you will be presented with the Save dialog box. If the map has already been saved once, it is automatically overwritten with the most recent Save. </span></font>
    </p>
    <p>
      
    </p>
    <p>
      <b><font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">Note</span></font></b>
    </p>
    <ul>
      <li>
        <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">This is a controlled save, not an automatic save. (A controlled save is done manually, using any of: File &gt; Save, File &gt; Save As..., the Save or Save As icon on the toolbar, or Ctrl + S.) </span></font>
      </li>
    </ul>
    <p>
      
    </p>
    <p>
      <b><font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">See also</span></font></b>
    </p>
    <ul>
      <li>
        <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">Automatic save.</span></font>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="1,2"/>
</node>
<node TEXT="Save all [Save all open maps]" ID="ID_1423263542" CREATED="1310534781892" MODIFIED="1311185259435" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">Saves all open maps. Same as Save, but is applied to all open maps.</span></font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Save map" FOLDED="true" ID="ID_1689022735" CREATED="1310835414628" MODIFIED="1311402754599">
<hook NAME="SummaryNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
<node TEXT="Save folding" ID="ID_1155653392" CREATED="1311925121696" MODIFIED="1311925365681" LINK="#ID_619771458" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        In preferences can be set if/how folding must be saved.
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Revert [Revert to last save]" ID="ID_1896010117" CREATED="1310534540786" MODIFIED="1311185259419" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Reverts the map to the last controlled save.
    </p>
    <p>
      
    </p>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        A controlled save is done manually using any of: File &gt; Save, File &gt; Save As..., the Save or Save As icon on the toolbar, or Ctrl + S.
      </li>
      <li>
        Automatic saves are ignored. <u>This is an unforgiving command</u>, and cannot be reversed with Edit &gt; Undo.
      </li>
    </ul>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        &#160;Edit &gt; Undo and &#160;Edit &gt; Redo for discreet changes to individual nodes.
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Open [Open saved map...]" ID="ID_1284635316" CREATED="1310134532663" MODIFIED="1311402615572" TEXT_SHORTENED="true">
<icon BUILTIN="../fileopen"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">Open a dialog to select and open a map that has already been saved.</span></font>
    </p>
  </body>
</html></richcontent>
<hook NAME="FirstGroupNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="1,2"/>
</node>
<node TEXT="Most recent files  [Most recent maps]" ID="ID_655783347" CREATED="1310534990519" MODIFIED="1311402754615" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">Opens a list of maps that you have opened recently.</span></font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
</node>
<node TEXT="Open map" FOLDED="true" ID="ID_565242033" CREATED="1310835477153" MODIFIED="1311925398410" LINK="#ID_950384750">
<hook NAME="SummaryNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
<node TEXT="Load settings" ID="ID_553299918" CREATED="1311925472829" MODIFIED="1311925552562" LINK="#ID_950384750">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Export  [Export map]" FOLDED="true" ID="ID_1294197740" CREATED="1310134532663" MODIFIED="1311405837126" TEXT_SHORTENED="true">
<icon BUILTIN="../export"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Opens a dialog to select a destination and type of export.
    </p>
  </body>
</html></richcontent>
<hook NAME="FirstGroupNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="6"/>
<node TEXT="As HTML" ID="ID_289318260" CREATED="1266417318562" MODIFIED="1311405837157" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <span style="font-family: SansSerif, sans-serif; color: #000000"><font face="SansSerif, sans-serif" color="#000000">This exports the entire map as a HTML document. The map is exported &quot;as is&quot;; the HTML document appears just as the map appears in Freeplane. If a node is expanded, it appears expanded in the HTML document. If a node is collapsed at the time of export, it appears collapsed in the HTML document. The HTML document contains JavaScript that allows the collapsed nodes to be unfolded.</font></span>
    </p>
  </body>
</html></richcontent>
<hook NAME="FirstGroupNode"/>
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="Branch as HTML" ID="ID_1222466772" CREATED="1266417322421" MODIFIED="1311405837204" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <span style="font-family: SansSerif, sans-serif; color: #000000"><font face="SansSerif, sans-serif" color="#000000">Exports only the branch of the map defined by the node selected and all of its children. In all other respects it is identical to the &quot;As HTML&quot; export.</font></span>
    </p>
  </body>
</html></richcontent>
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="Open Office Writer Document (ODT)..." ID="ID_413819995" CREATED="1266417526828" MODIFIED="1311405837266" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <span style="font-family: SansSerif, sans-serif; color: #000000"><font face="SansSerif, sans-serif" color="#000000">Exports entire map as an Open Office Writer document.</font></span>
    </p>
  </body>
</html></richcontent>
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="Portable Network Graphics (PNG)..." ID="ID_1426694137" CREATED="1266417536125" MODIFIED="1311405837297" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <span style="font-family: SansSerif, sans-serif; color: #000000"><font face="SansSerif, sans-serif" color="#000000">Exports entire map in the PNG image format.</font></span>
    </p>
  </body>
</html></richcontent>
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="Compressed image (JPEG)..." ID="ID_298130830" CREATED="1266417521765" MODIFIED="1311405837344" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <span style="font-family: SansSerif, sans-serif; color: #000000"><font face="SansSerif, sans-serif" color="#000000">Exports entire map in the JPEG/JPG image format.</font></span>
    </p>
  </body>
</html></richcontent>
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="As XHTML (Javascript version)..." ID="ID_1590599513" CREATED="1266417345906" MODIFIED="1311405837375" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <span style="font-family: SansSerif, sans-serif; color: #000000"><font face="SansSerif, sans-serif" color="#000000">Exports the entire map with full JavaScript functionality.</font></span>
    </p>
  </body>
</html></richcontent>
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="As XHTML (click-able map image version HTML)..." ID="ID_519681176" CREATED="1266417370281" MODIFIED="1311405837422" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <span style="font-family: SansSerif, sans-serif; color: #000000"><font face="SansSerif, sans-serif" color="#000000">Exports the entire map. The map portion of the page will appear exactly as the map appears in Freeplane. If some nodes are collapsed and others expanded, that's how it will appear in the document. The detail part of the page will contain all of the nodes in outline format.</font></span>
    </p>
  </body>
</html></richcontent>
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="As Java Applet (HTML)..." FOLDED="true" ID="ID_376134497" CREATED="1266417395953" MODIFIED="1311405837453" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <span style="font-family: SansSerif, sans-serif; color: #000000"><font face="SansSerif, sans-serif" color="#000000">Creates a version of the map embedded in an HTML file, suitable for publishing as a Web page. Appears very similar to the actual map: nodes expand and collapse the same as they do in the Freeplane program.</font></span>
    </p>
  </body>
</html></richcontent>
<attribute NAME="Chapter" VALUE="6"/>
<node TEXT="Publish on a website or share" ID="ID_126764093" CREATED="1309808574878" MODIFIED="1311405837469" TEXT_SHORTENED="true" LINK="#ID_1111281504">
<hook NAME="SummaryNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="Refine" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="6"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000">To publish or share a mind map without images, but with standard icons: </font>
    </p>
    <ul>
      <li>
        Fold the mind map as you want it to be when opening
      </li>
      <li>
        Select <i>File &gt; Export</i>&#160;and choose <i>Java Applet</i>. One file and one directory will be created: <i>myFile.html </i>and <i>myFile.html_files</i>&#160; respectively.
      </li>
    </ul>
    <p>
      Now you can move the file <i>myFile.html</i>&#160;and the subdirectory <i>myFile.html_files</i>&#160; to the location (directory) where you want to it be it accessible. You may rename&#160; <i>myFile.html</i>&#160;&#160;to a name you want, e.g. <i>myName.mm</i>. &#160;The subdirectory should not be renamed. You van then open the mind map with<i>&#160;myName.mm. </i>
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        Press on the <i>green arrow</i>&#160;for publishing/sharing images too.
      </li>
      <li>
        <i>Tools &gt; Preferences &gt; Appearances&#160;&gt; Icons </i>to hide icons.
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="As Flash (HTML)..." ID="ID_1234525000" CREATED="1266417407640" MODIFIED="1311405837485" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <span style="font-family: SansSerif, sans-serif; color: #000000"><font face="SansSerif, sans-serif" color="#000000">Exports a Flash map that appears very similar to the actual map. Nodes expand and collapse the same as they do in the Freeplane map.</font></span>
    </p>
  </body>
</html></richcontent>
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="Tasks from TASKS mode to TaskJuggler file (TJI)..." ID="ID_1188629539" CREATED="1266417449093" MODIFIED="1311405837500" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <span style="font-family: SansSerif, sans-serif; color: #000000"><font face="SansSerif, sans-serif" color="#000000">Exports to a file that can be loaded from TaskJuggler, an open source project management tool.</font></span>
    </p>
  </body>
</html></richcontent>
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="Resources from RESOURCES mode to TaskJuggler file (TJI)..." ID="ID_1727885349" CREATED="1266417478265" MODIFIED="1311405837516" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <span style="font-family: SansSerif, sans-serif; color: #000000"><font face="SansSerif, sans-serif" color="#000000">Exports to a file that can be loaded from TaskJuggler, an open source project management tool.</font></span>
    </p>
  </body>
</html></richcontent>
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="As TWiki (TW)..." ID="ID_1482055774" CREATED="1266417500265" MODIFIED="1311405837531" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <span style="font-family: SansSerif, sans-serif; color: #000000"><font face="SansSerif, sans-serif" color="#000000">Exports the entire map to a TWiki formatted file.</font></span>
    </p>
  </body>
</html></richcontent>
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="Freeplane 1.1" ID="ID_449157790" CREATED="1310762788434" MODIFIED="1311405837547" TEXT_SHORTENED="true">
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="HTML document (HTM) (HTML)" ID="ID_198819817" CREATED="1310762816708" MODIFIED="1311405837563" TEXT_SHORTENED="true">
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="LaTeX document (TEX)" ID="ID_1973701217" CREATED="1310762886823" MODIFIED="1311405837594" TEXT_SHORTENED="true">
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="LaTeX book (TXT)" ID="ID_308084692" CREATED="1310762930260" MODIFIED="1311405837594" TEXT_SHORTENED="true">
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="MS Project 2003 (XML)" ID="ID_1015672283" CREATED="1310762997021" MODIFIED="1311405837609" TEXT_SHORTENED="true">
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="Mediawiki (MWIKI)" ID="ID_1345826529" CREATED="1310763063563" MODIFIED="1311405837625" TEXT_SHORTENED="true">
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="Oowriter file (OOWRITER)" ID="ID_123526746" CREATED="1310763100184" MODIFIED="1311405837641" TEXT_SHORTENED="true">
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="Opml file (OPML)" ID="ID_1024088291" CREATED="1310763140050" MODIFIED="1311405837641" TEXT_SHORTENED="true">
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="Plain text (TXT)" ID="ID_581818306" CREATED="1310763169697" MODIFIED="1311405837656" TEXT_SHORTENED="true">
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="Word &gt;= 2003 (DOC, XML)" ID="ID_786673808" CREATED="1310763197052" MODIFIED="1311405837656" TEXT_SHORTENED="true">
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="XBEL" ID="ID_1844163113" CREATED="1310763237495" MODIFIED="1311405837672" TEXT_SHORTENED="true">
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="(XML, XLS)" ID="ID_1650734011" CREATED="1310763267456" MODIFIED="1311405837672" TEXT_SHORTENED="true">
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="Portable Document Format (PDF)..." ID="ID_603087610" CREATED="1266417575078" MODIFIED="1311405837672" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <span style="font-family: SansSerif, sans-serif; color: #000000"><font face="SansSerif, sans-serif" color="#000000">Exports entire map in Adobe Acrobat PDF format.</font></span>
    </p>
  </body>
</html></richcontent>
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="Scalable Vector Graphics (SVG)" ID="ID_1337380062" CREATED="1266417578453" MODIFIED="1311405837687" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <span style="font-family: SansSerif, sans-serif; color: #000000"><font face="SansSerif, sans-serif" color="#000000">Exports entire map in Scalable Vector Graphics format.</font></span>
    </p>
  </body>
</html></richcontent>
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="" FOLDED="true" ID="ID_783046977" CREATED="1310799890904" MODIFIED="1311405837687" TEXT_SHORTENED="true">
<hook NAME="SummaryNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="6"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Images are not part of Freeplane, except system icons. Hence images are not exported automatically.
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>Node features &gt; Images </i>
      </li>
      <li>
        <i>Tools &gt; Preferences &gt; Appearance &gt; Icons</i>
      </li>
      <li>
        <i>Tools &gt; Preferences &gt; HTML</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<node TEXT="To other text editor" ID="ID_22164919" CREATED="1286653812825" MODIFIED="1311946353162" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      If a branch is copied to a regular text editor, its hierarchical structure appears a levels of indentation. Hyperlinks are shown between brackets. Examples of editors which can handle RTF are OpenOffice/Write, MSWord, Wordpad and Outlook.
    </p>
  </body>
</html></richcontent>
<attribute NAME="Chapter" VALUE="6"/>
</node>
</node>
</node>
<node TEXT="Branch as new map [Move branch to new, linked map]" ID="ID_995694574" CREATED="1310567044505" MODIFIED="1311405837734" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Saves selected node and descendants as new map. Selected node becomes root of new map with&#160;&#160;hyperlink to its original location. Selected node gets hyperlink to new map and descendants are deleted.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="Import [Import map]" FOLDED="true" ID="ID_977906606" CREATED="1310134532663" MODIFIED="1311405837734" TEXT_SHORTENED="true">
<icon BUILTIN="../import"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="6"/>
<node TEXT="Branch" ID="ID_148627051" CREATED="1266417591406" MODIFIED="1311405837750">
<hook NAME="FirstGroupNode"/>
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="Linked Branch" ID="ID_282345042" CREATED="1266417599062" MODIFIED="1311405837750">
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="(Linked Branch) Without Root" ID="ID_1230060690" CREATED="1266417606156" MODIFIED="1311405837765">
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="Explorer favourites..." ID="ID_1416306563" CREATED="1266417657421" MODIFIED="1311405837765">
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="Folder Structure..." ID="ID_379387162" CREATED="1266417664671" MODIFIED="1311405837765">
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="MindManager X5 Map..." ID="ID_1880120674" CREATED="1266417677171" MODIFIED="1311405837765">
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="" ID="ID_1138762077" CREATED="1310801095185" MODIFIED="1311405837765" TEXT_SHORTENED="true">
<hook NAME="SummaryNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="6"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>Tools &gt; Preferences &gt; HTML</i>
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Import/Export map" ID="ID_1115722286" CREATED="1310835645851" MODIFIED="1310835678970">
<hook NAME="SummaryNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
</node>
<node TEXT="Page set up [Print set up]" ID="ID_1629904320" CREATED="1310535299236" MODIFIED="1311405837781" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">Presents a dialog box from which you can select formatting for printing a map.</span></font>
    </p>
    <ul>
      <li>
        <span onclick="show_folder('1_1')" class="foldclosed" id="show1_1">+</span>&#160;<span onclick="hide_folder('1_1')" class="foldopened" id="hide1_1">-</span>&#160;<font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">Fit to one page</span></font>

        <ul id="fold1_1">
          <li>
            <p>
              <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">The entire map is condensed to one page. Depending on the size of the map, this may render the typeface too small to be readable.</span></font>
            </p>
          </li>
        </ul>
      </li>
      <li>
        <span onclick="show_folder('1_2')" class="foldclosed" id="show1_2">+</span>&#160;<span onclick="hide_folder('1_2')" class="foldopened" id="hide1_2">-</span>&#160;<font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">Fit width to one page</span></font>

        <ul id="fold1_2">
          <li>
            <p>
              <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">The map is forced to fit the width of the page, but may take up multiple pages depending on the size of the map. Results may vary based on choosing Portrait or Landscape orientation for printing.</span></font>
            </p>
          </li>
        </ul>
      </li>
      <li>
        <span onclick="show_folder('1_3')" class="foldclosed" id="show1_3">+</span>&#160;<span onclick="hide_folder('1_3')" class="foldopened" id="hide1_3">-</span>&#160;<font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">Fit height to one page</span></font>

        <ul id="fold1_3">
          <li>
            <p>
              <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">The map is forced to fit the height of the page, but may take up multiple pages depnding on the size of the map. Results may vary based on choosing Portrait or Landscape orientation for printing.</span></font>
            </p>
          </li>
        </ul>
      </li>
      <li>
        <span onclick="show_folder('1_4')" class="foldclosed" id="show1_4">+</span>&#160;<span onclick="hide_folder('1_4')" class="foldopened" id="hide1_4">-</span>&#160;<font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">User defined scale</span></font>

        <ul id="fold1_4">
          <li>
            <p>
              <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">Allows access to Print Zoom Factor where a magnification factor of 0.0 to 2.0 may be entered. This applies to width and height proportionally.</span></font>
            </p>
          </li>
        </ul>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="FirstGroupNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="Print preview" ID="ID_1079934903" CREATED="1310134532663" MODIFIED="1311402754677" TEXT_SHORTENED="true">
<icon BUILTIN="../print_preview"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">Presents a view of how the printed map will appear based on the Page Setup settings. </span></font>
    </p>
    <p>
      
    </p>
    <p>
      <b><font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">See also</span></font></b>
    </p>
    <ul>
      <li>
        <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">Page Setup...</span></font>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
</node>
<node TEXT="Print. [Print map]" ID="ID_111478060" CREATED="1310134532663" MODIFIED="1311405837781" TEXT_SHORTENED="true">
<icon BUILTIN="../fileprint"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">Opens the dialog box associated with the printer on which the map will print.</span></font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="Print map" ID="ID_682857312" CREATED="1310835584902" MODIFIED="1311405837781">
<hook NAME="SummaryNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="Statistics" ID="ID_688770977" CREATED="1310134532663" MODIFIED="1311185259341" TEXT_SHORTENED="true">
<icon BUILTIN="../BranchStats"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Close [Close current map]" ID="ID_1018458898" CREATED="1310134532663" MODIFIED="1311402754677" TEXT_SHORTENED="true">
<icon BUILTIN="../close"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">Closes the map that has focus. If multiple maps are open, focus refers to the tab that you are currently editing.</span></font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
</node>
<node TEXT="Quit  [Quit Freeplane]" ID="ID_1770693982" CREATED="1310134532663" MODIFIED="1311185259341" TEXT_SHORTENED="true">
<icon BUILTIN="../quit"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">Close all open maps and exit Freeplane.</span></font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Edit" FOLDED="true" POSITION="right" ID="ID_1198850460" CREATED="1310134532663" MODIFIED="1311404647788" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MainMenu" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3" OBJECT="org.freeplane.features.format.FormattedNumber|3.0"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To find frequently used edit functions.
    </p>
  </body>
</html></richcontent>
<node TEXT="Undo" ID="ID_1899055023" CREATED="1310134532663" MODIFIED="1311226359815" TEXT_SHORTENED="true">
<icon BUILTIN="../undo"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif">Reverse previous changes or edits. </font>
    </p>
    <p>
      
    </p>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><b>Note</b> </font>
    </p>
    <ul>
      <li>
        <font color="#000000" face="SansSerif, sans-serif">The number of Undo actions stored by FreePlane is determined in Preferences. </font>
      </li>
    </ul>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><b>See aslo</b> </font>
    </p>
    <ul>
      <li>
        <font color="#000000" face="SansSerif, sans-serif"><i>Tools &gt;Preferences &gt; Behavior &gt; Undo</i>.</font>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Redo" ID="ID_807893197" CREATED="1310134532663" MODIFIED="1311226360064" TEXT_SHORTENED="true">
<icon BUILTIN="../redo"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">Reverses an Undo. It is limited to the most recent Undo.</span></font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Select all visible" ID="ID_166465072" CREATED="1310134532663" MODIFIED="1311226360033" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">Selects all nodes visible on the map.</span></font>
    </p>
  </body>
</html></richcontent>
<hook NAME="FirstGroupNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Select visible branch" ID="ID_1140223878" CREATED="1310134532663" MODIFIED="1311226360018" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">Selects all the currently-visible (unfolded) children of the node which is already selected.</span></font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Select" ID="ID_222729198" CREATED="1310835972305" MODIFIED="1310886250857">
<hook NAME="SummaryNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
</node>
<node TEXT="Cut [Cut branch]" ID="ID_353410114" CREATED="1310134532663" MODIFIED="1311226360002" TEXT_SHORTENED="true">
<icon BUILTIN="../editcut"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">Cuts the node currently selected and all of its children.</span></font>
    </p>
  </body>
</html></richcontent>
<hook NAME="FirstGroupNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Copy [Copy branch]" ID="ID_1274800359" CREATED="1310134532663" MODIFIED="1311226359986" TEXT_SHORTENED="true">
<icon BUILTIN="../editcopy"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">Copies the node selected and all of its children.</span></font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Paste" ID="ID_1705754282" CREATED="1310134532663" MODIFIED="1311226359971" TEXT_SHORTENED="true">
<icon BUILTIN="../editpaste"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">Pastes copied text. If the selected node is in edit mode, the text will be added to the contents of the node. If the selected node is not in edit mode, the text will be added as a child node.</span></font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Paste as..." ID="ID_1160623822" CREATED="1310885419000" MODIFIED="1311226359955">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Copy single [Copy node]" ID="ID_1302326175" CREATED="1310134532663" MODIFIED="1311226359940" TEXT_SHORTENED="true">
<icon BUILTIN="../copy_single"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">Copies only the node selected, even if it has children.</span></font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Copy Node ID" ID="ID_1433358397" CREATED="1310193968260" MODIFIED="1311226359924" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">Gets the internal Node ID of the currently selected node and stores it in the system Clipboard. This advanced feature has, for most purposes, been replaced by the menu command Insert &gt; Add Local Hyperlink.</span></font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Copy Format" ID="ID_165229421" CREATED="1310134532663" MODIFIED="1311226359908" TEXT_SHORTENED="true">
<icon BUILTIN="../colorpicker"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">Copies the formatting of a node, but not the content of the node. SEE ALSO: Paste Format.</span></font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Paste Format" ID="ID_85344174" CREATED="1310134532663" MODIFIED="1311226359893" TEXT_SHORTENED="true">
<icon BUILTIN="../color_fill"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">Pastes the formatting of a node, but not the contents of the node. SEE ALSO: Copy Format.</span></font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Copy attributes" ID="ID_1688307508" CREATED="1310193998671" MODIFIED="1311404610831" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Copies attributes from the selected node.
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>Node features &gt; Attributes</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3" OBJECT="org.freeplane.features.format.FormattedNumber|3.0"/>
</node>
<node TEXT="Paste attributes" ID="ID_1986438286" CREATED="1310194008929" MODIFIED="1311404628787" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Pastes attributes in the selected node that before have been copied form another node.
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>Copy attributes </i>
      </li>
      <li>
        <i>Node features &gt; Attributes</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3" OBJECT="org.freeplane.features.format.FormattedNumber|3.0"/>
</node>
<node TEXT="Cut, copy and paste" ID="ID_758366226" CREATED="1310835917635" MODIFIED="1310835947798">
<hook NAME="SummaryNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
</node>
<node TEXT="Find..." ID="ID_412879235" CREATED="1310134532663" MODIFIED="1311226359862" TEXT_SHORTENED="true">
<icon BUILTIN="../filefind"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">Opens the Find dialog box. </span></font>
    </p>
    <p>
      
    </p>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif"><b>See also</b> </span></font>
    </p>
    <ul>
      <li>
        <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif"><i>Find Next.</i></span></font>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="FirstGroupNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Find Next" ID="ID_409531459" CREATED="1310134532663" MODIFIED="1311226359862" TEXT_SHORTENED="true">
<icon BUILTIN="../find_next"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">Searches for the next instance of the string entered in the Find dialog box. (A string is any combination of letters and numbers.) </span></font>
    </p>
    <p>
      
    </p>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif"><b>See also</b> </span></font>
    </p>
    <ul>
      <li>
        <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">Find...</span></font>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Find and Replace..." FOLDED="true" ID="ID_1230019641" CREATED="1310134532663" MODIFIED="1311226359846" TEXT_SHORTENED="true">
<icon BUILTIN="../NodeListAction"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">Opens a dialog box that lists all of the nodes in a map. When matches are found for the string entered in the Find field, you have the option to apply the value of the Replace field or skip to the next instance of the Find string. </span></font>
    </p>
    <p>
      
    </p>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif"><b>See also</b> </span></font>
    </p>
    <ul>
      <li>
        <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">Find and Replace in all Maps.</span></font>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<node TEXT="Find last modified nodes" ID="ID_1272430146" CREATED="1311098601253" MODIFIED="1311225760042" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="Procedure" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To find the list of nodes last modified, click on top of the table Modified to sort the rows top down or bottom up. Next you can see which nodes have been last modified.
    </p>
    <p>
      
    </p>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        To go the the node, double click on the line the node is in.
      </li>
    </ul>
    <p>
      <b>See also </b>
    </p>
    <ul>
      <li>
        Quick filter for a filtterrule to see al nodes modified after a date.
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Find and replace in all maps" ID="ID_4891503" CREATED="1310194040136" MODIFIED="1311226359846" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">Same as Find and Replace... but applied to all open maps. </span></font>
    </p>
    <p>
      
    </p>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif"><b>See also</b> </span></font>
    </p>
    <ul>
      <li>
        <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">Find and Replace...</span></font>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Find and replace" ID="ID_1229770899" CREATED="1310835859572" MODIFIED="1310835894843">
<hook NAME="SummaryNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="View" STYLE_REF="MainMenu" FOLDED="true" POSITION="right" ID="ID_389115947" CREATED="1266240584812" MODIFIED="1311402648660" TEXT_SHORTENED="true">
<attribute_layout VALUE_WIDTH="216"/>
<attribute NAME="Chapter" VALUE="1,2,3,4,5"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MainMenu" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To change the way the mind map appears to you.
    </p>
  </body>
</html></richcontent>
<node TEXT="Full screen mode" FOLDED="true" ID="ID_1526004925" CREATED="1310672109364" MODIFIED="1310721364783" TEXT_SHORTENED="true">
<hook NAME="FirstGroupNode"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000">Hide or show all menu/tool bars. </font>
    </p>
    <p>
      
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<node TEXT="" ID="ID_1272735653" CREATED="1310716776303" MODIFIED="1310716821153" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000">Once in full screen mode, the only way to change back to normal screen mode is to right click on the screen and UNCHECK Full Screen mode.</font>
    </p>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Toolbars" FOLDED="true" ID="ID_1963983857" CREATED="1303929006438" MODIFIED="1311947052465" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Methods are accessible through the main menu-bar as menu-items, the Toolbar and Filter toolbar as icon-buttons, as short-cut keys (F-keys and key combinations) and as context menu's which are connected to objects such as nodes and connectors. It is your own choice which of these possibilities you want to use.
    </p>
    <p>
      
    </p>
    <p>
      A menu-bar which is not needed, can be hidden to save screen space. If all bars are hidden, right-click on the screen to display the context menu with which cou can make them visible again.
    </p>
    <p>
      
    </p>
    <p>
      
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="SubMenu" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
<node TEXT="menu-bar [Main menu-bar]" FOLDED="true" ID="ID_926021846" CREATED="1266247097703" MODIFIED="1311402754771" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif">Toggles the main toolbar on and off. This is the bar with the menu's for File, Edit, View, Nodes, etc. </font>
    </p>
    <p>
      
    </p>
    <p>
      <b><font color="#000000" face="SansSerif, sans-serif">Note</font></b>
    </p>
    <ul>
      <li>
        Many menu items show short cut keys/combinations which can be used in stead of pressing the menu items.
      </li>
      <li>
        The icons shown with the menu items are generally available as icon-buttons (short cuts) in toolbars.
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
<node TEXT="Main menu-bar" ID="ID_1325292461" CREATED="1311183309593" MODIFIED="1311402615603" TEXT_SHORTENED="true">
<attribute_layout VALUE_WIDTH="175"/>
<attribute NAME="Chapter" VALUE="1,2"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000">The<b>&#160;main menu-bar</b>&#160;is the bar with the main textual menu's of Freeplane at the top of the screen. </font>
    </p>
    <p>
      
    </p>
    <p>
      <font face="SansSerif, sans-serif" color="#000000"><b>Note</b> </font>
    </p>
    <ul>
      <li>
        <font face="SansSerif, sans-serif" color="#000000">Many of the menu's display <b>icons</b>&#160;which at other places represent<b>&#160;icon-buttons</b>&#160;that can be pressed and then have the same effect as selecting the textual menu's. </font>
      </li>
      <li>
        <font face="SansSerif, sans-serif" color="#000000">The menu-items also may display <b>short cuts</b>&#160;(special keys or combinations of keys) that can be pressed to activate the menu-items. </font>
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Toolbar [Main toolbar]" FOLDED="true" ID="ID_1777795790" CREATED="1288423137527" MODIFIED="1311402754787" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Toggles visibility of toolbar on and off.
    </p>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        This map contains the most used items of the Main menu-bar as icon-buttons for easy access.
      </li>
      <li>
        Move the cursor over the icon buttons to see their function. The icons are also shown in the Main menu bar.
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
<node TEXT="Main toolbar" ID="ID_1091232727" CREATED="1311319747381" MODIFIED="1311402583514" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="1"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      The <b>main toolbar</b>&#160;is the bar with icon-buttons for most used map handling, e.g.Previous map, Following map, etc.
    </p>
    <p>
      
    </p>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Filter toolbar" FOLDED="true" ID="ID_850553052" CREATED="1288423176425" MODIFIED="1311402754787" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Toggles the visibility of the toolbar with filter methods.
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>Filter</i>&#160;&#160;in the main menu.
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
<node TEXT="Filter toolbar" ID="ID_793475442" CREATED="1311319982419" MODIFIED="1311402583514" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="1"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      The <b>filter toolbar</b>&#160;is the bar containing entry fields for filter rules and&#160;&#160;icon-buttons for most used filter methods like Undo, Redo, etc.
    </p>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="F-keys" ID="ID_664958295" CREATED="1266247102156" MODIFIED="1311402754802" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif">Toggles the Function Key (F-Key) toolbar on and off. This is the bar with F1, F2, F3, etc. </font>
    </p>
    <p>
      
    </p>
    <p>
      <b><font color="#000000" face="SansSerif, sans-serif">Note</font></b>
    </p>
    <ul>
      <li>
        <font color="#000000" face="SansSerif, sans-serif">F-keys on the keyboard are also active if the F-bar is hidden.</font>
      </li>
      <li>
        F-keys must be defined before they can be used, see below.
      </li>
    </ul>
    <p>
      
    </p>
    <p>
      <b><font color="#000000" face="SansSerif, sans-serif">See also</font></b>
    </p>
    <ul>
      <li>
        <font color="#000000" face="SansSerif, sans-serif">Tools &gt; Short cuts</font>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
</node>
<node TEXT="Secondary Tool bar [Icons toolbar]" ID="ID_1263742214" CREATED="1266247112156" MODIFIED="1311402754802" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif">Toggles the visibility of the Secondary Toolbar [Icon bar] on and off. This is the bar containing all kinds of icons the user can add to a node and located vertically at the left of the screen (most of the time).</font>
    </p>
    <p>
      
    </p>
    <p>
      <b><font color="#000000" face="SansSerif, sans-serif">See also</font></b>
    </p>
    <ul>
      <li>
        <font color="#000000" face="SansSerif, sans-serif">Node features &gt; Icons</font>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
</node>
</node>
<node TEXT="Properties Panel" FOLDED="true" ID="ID_1783882978" CREATED="1291154608643" MODIFIED="1311412594814" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Toggles the visibility of the properties panel.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3,4,5" OBJECT="org.freeplane.features.format.FormattedNumber|345.0|#,#,#"/>
<node TEXT="Properties panel" ID="ID_125269399" CREATED="1310714224145" MODIFIED="1311402583530" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="1"/>
<font BOLD="false"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      The<b>&#160;properties panel </b>is a dialog to change the basic properties of a node. Properties are distinguihed in format-properties and properties of Calendar &amp; attributes.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Format properties" FOLDED="true" ID="ID_786320758" CREATED="1293305969634" MODIFIED="1311405889214" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To change a property of a selected node in the Properties Panel:
    </p>
    <ul>
      <li>
        check change box; and
      </li>
      <li>
        select or edit the changed property
      </li>
    </ul>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        The change takes effect immediately. There is no need to press an OK button or leave the panel.
      </li>
      <li>
        It is possible to select a different node without leaving the Properties Panel
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="SubMenu" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="5"/>
<node TEXT="Node shape" ID="ID_1006574881" CREATED="1310714589873" MODIFIED="1311405449340" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>Node shape</b>&#160;is: the appearance of the node: without surrounding box (Fork), with surrounding box (Bubble), as parent or combined.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Node Font" FOLDED="true" ID="ID_724156158" CREATED="1310714589873" MODIFIED="1311405449387" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>Node Font</b>&#160;is&#160;the Font family, size, bold and italic.
    </p>
  </body>
</html></richcontent>
<node TEXT="Set standard font size" ID="ID_627969850" CREATED="1311926183029" MODIFIED="1311926253479" LINK="#ID_728681381">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Node color" ID="ID_242110669" CREATED="1310714589873" MODIFIED="1311405449450" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Node color is the color of Text or Background.
    </p>
    <p>
      
    </p>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        For hiding an edge, see elsewhere.
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="Edge color" ID="ID_211700099" CREATED="1310714589889" MODIFIED="1311405449496" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>Edge color</b>&#160;is&#160;the color of the edge connecting nodes.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Edge style" ID="ID_1060644831" CREATED="1310714589889" MODIFIED="1311405449543" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>Edge style</b>&#160;is the&#160;way an edge bends.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Edge width" ID="ID_1392877020" CREATED="1310714589889" MODIFIED="1311405449590" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>Edge width </b>is the thickness of the edge.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Cloud color" FOLDED="true" ID="ID_58140909" CREATED="1310714589889" MODIFIED="1311405449637" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>Cloud color</b>&#160;is the<b>&#160;</b>background color of cloud.
    </p>
    <p>
      
    </p>
    <p>
      
    </p>
    <p>
      
    </p>
  </body>
</html></richcontent>
<node TEXT="Set standard cloud color" ID="ID_1130479055" CREATED="1311925883476" MODIFIED="1311925956392" LINK="#ID_749503390">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Cloud Shape" ID="ID_840961117" CREATED="1310714589889" MODIFIED="1311405449684" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>Cloud Shape</b>&#160;is its the form of the cloud, e.g.&#160;Star, Rectangle or Round rectangle form.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Automatic properties" FOLDED="true" ID="ID_799508278" CREATED="1310714589889" MODIFIED="1311405449699" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To set or unset one of the automatically applied methods:
    </p>
    <ul>
      <li>
        check box before the method
      </li>
    </ul>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        The change takes effect immediately. There is no need to press an OK button or leave the panel.
      </li>
      <li>
        It is possible to select a different node without leaving the Properties Panel
      </li>
    </ul>
  </body>
</html></richcontent>
<node TEXT="Automatic Layout" ID="ID_523604667" CREATED="1310714589889" MODIFIED="1311405449730" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>Automatic Layout</b>&#160;is the state when Freeplane automatically gives all nodes on a particualr hierarchical level a unique, predefined appearance. This automated appearance can be changed in Menu Styles.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Automatic edge color" ID="ID_1945706598" CREATED="1310714589889" MODIFIED="1311405449730" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>Automatic edge color</b>&#160;is the state when Freeplane&#160;automatically selects a different edge color each time a new node is made.
    </p>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Node text Template/Format" FOLDED="true" ID="ID_225849752" CREATED="1311415106808" MODIFIED="1311415388841" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To use standard number formats or date formats in node text, check the box and select (and edit) the format.
    </p>
    <p>
      
    </p>
    <p>
      <b>Note </b>
    </p>
    <ul>
      <li>
        
      </li>
    </ul>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        Attribute data formatting
      </li>
    </ul>
  </body>
</html></richcontent>
<node TEXT="Date/time formatting" ID="ID_530315899" CREATED="1311337763292" MODIFIED="1311621855746" TEXT_SHORTENED="true" LINK="#ID_1277533848">
<attribute NAME="Chapter" VALUE="4,5"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To have entered date-times converted to internally standardized structures and displayed in the format of the template, select or type a template.
    </p>
    <p>
      
    </p>
    <p>
      <b>Note&#160;</b>
    </p>
    <ul>
      <li>
        <p>
          In case of an attribute value, the template applies to the selected attribute value.
        </p>
      </li>
      <li>
        With internally standardized formats dates and times can be applied independent of their presentation format (template) in comparisons and find and filter operations.
      </li>
      <li>
        With internally standardized formats it is possible to change the presentation of dates and times Internally standardized date-time&#160;without changing comparison or find operations (&quot;is equal&quot;, etc.).
      </li>
      <li>
        Rule 1. If date is entered from calendar panel or dialog, it is recognised as a date.
      </li>
      <li>
        Rule 2. If entered text has format yyyy-MM-dd or yyyy-MM-dd hh:mm it is converted to a date.
      </li>
      <li>
        Rule 3. If entered text has format currently selected in the calendar panel / dialog it is converted to a date.
      </li>
      <li>
        Rule 4. If neither of the above rules applies, entered text is treated as a text. It means that interpretation of text can depend on selection in the calendar panel.
      </li>
      <li>
        Formats available in the calendar panel can be edited in the formatting panel, but they can not be deleted and they are changed only upon program restart. They are saved as formats.xml in freeplane user configuration dir.
      </li>
      <li>
        Rules 2 and 3 are applied to * node text after is is edited, * attribute value after is is edited and * find / filter condition values with comparison operators &quot;is equal&quot;, &quot;is not equal&quot;, &quot;&lt;&quot;, &quot;&lt;=&quot;, &quot;&gt;&quot;, &quot;&gt;=&quot;
      </li>
      <li>
        The <i>Templates</i>&#160;drop down menu contain the formats defined in file <i>formats.xml</i>. This file can be edited with a text formatter.
      </li>
    </ul>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <p>
          If<i>&#160;Tools &gt; Preferences &gt; Behaviour &gt; Search &gt; Recognize input of number and date-time</i>
        </p>
        <p>
          <i>&#160;</i>is&#160;checked AND node text / attribute value is a string, the search engine tries to convert it to a date/number.
        </p>
      </li>
      <li>
        <i>Tools &gt; Open User Directory</i>, subdirectory<i>&#160;XML</i>&#160;for <i>formats.xml</i>.
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="Number formatting" ID="ID_1358687242" CREATED="1311338038657" MODIFIED="1311621724347" TEXT_SHORTENED="true" LINK="#ID_1277533848">
<attribute NAME="Chapter" VALUE="4"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To format a number according to a preselected template, select the template from the drop down menu.
    </p>
    <p>
      
    </p>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        The <i>Templates</i>&#160;drop down menu contain the formats defined in file <i>formats.xml</i>. This file can be edited with a text formatter.
      </li>
      <li>
        In attribute values: to have the number-template NOT applied, start with a non-number character like &quot;. This character may be removed later.
      </li>
      <li>
        <p>
          If<i>&#160;Tools &gt; Preferences &gt; Behaviour &gt; Search &gt; Recognize input of number and date-time</i>
        </p>
        <p>
          <i>&#160;</i>is&#160;checked AND node text / attribute value is a string, the search engine tries to convert it to a date/number.
        </p>
      </li>
    </ul>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>Tools &gt; Open User Directory</i>, subdirectory<i>&#160;XML</i>&#160;for <i>formats.xml</i>.
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="" FOLDED="true" ID="ID_1625443252" CREATED="1311432799325" MODIFIED="1311622036628" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Refine" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <h4>
      Date and Time Patterns
    </h4>
    <p>
      Date and time formats are specified by <em>date and time pattern</em>&#160; strings. Within date and time pattern strings, unquoted letters from <code>'A'</code>&#160; to <code>'Z'</code>&#160;and from <code>'a'</code>&#160;to <code>'z'</code>&#160;are interpreted as pattern letters representing the components of a date or time string. Text can be quoted using single quotes (<code>'</code>) to avoid interpretation. <code>&quot;''&quot;</code>&#160; represents a single quote. All other characters are not interpreted; they're simply copied into the output string during formatting or matched against the input string during parsing.
    </p>
    <p>
      The following pattern letters are defined (all other characters from <code>'A'</code>&#160; to <code>'Z'</code>&#160;and from <code>'a'</code>&#160;to <code>'z'</code>&#160;are reserved):
    </p>
    <blockquote>
      <table cellspacing="3" summary="Chart shows pattern letters, date/time component, presentation, and examples." border="0" cellpadding="0">
        <tr bgcolor="#ccccff">
          <th align="left">
            Letter
          </th>
          <th align="left">
            Date or Time Component
          </th>
          <th align="left">
            Presentation
          </th>
          <th align="left">
            Examples
          </th>
        </tr>
        <tr>
          <td>
            <code>G</code>
          </td>
          <td>
            Era designator
          </td>
          <td>
            <a href="http://download.oracle.com/javase/1.5.0/docs/api/java/text/SimpleDateFormat.html#text">Text</a>
          </td>
          <td>
            <code>AD</code>
          </td>
        </tr>
        <tr bgcolor="#eeeeff">
          <td>
            <code>y</code>
          </td>
          <td>
            Year
          </td>
          <td>
            <a href="http://download.oracle.com/javase/1.5.0/docs/api/java/text/SimpleDateFormat.html#year">Year</a>
          </td>
          <td>
            <code>1996</code>; <code>96</code>
          </td>
        </tr>
        <tr>
          <td>
            <code>M</code>
          </td>
          <td>
            Month in year
          </td>
          <td>
            <a href="http://download.oracle.com/javase/1.5.0/docs/api/java/text/SimpleDateFormat.html#month">Month</a>
          </td>
          <td>
            <code>July</code>; <code>Jul</code>; <code>07</code>
          </td>
        </tr>
        <tr bgcolor="#eeeeff">
          <td>
            <code>w</code>
          </td>
          <td>
            Week in year
          </td>
          <td>
            <a href="http://download.oracle.com/javase/1.5.0/docs/api/java/text/SimpleDateFormat.html#number">Number</a>
          </td>
          <td>
            <code>27</code>
          </td>
        </tr>
        <tr>
          <td>
            <code>W</code>
          </td>
          <td>
            Week in month
          </td>
          <td>
            <a href="http://download.oracle.com/javase/1.5.0/docs/api/java/text/SimpleDateFormat.html#number">Number</a>
          </td>
          <td>
            <code>2</code>
          </td>
        </tr>
        <tr bgcolor="#eeeeff">
          <td>
            <code>D</code>
          </td>
          <td>
            Day in year
          </td>
          <td>
            <a href="http://download.oracle.com/javase/1.5.0/docs/api/java/text/SimpleDateFormat.html#number">Number</a>
          </td>
          <td>
            <code>189</code>
          </td>
        </tr>
        <tr>
          <td>
            <code>d</code>
          </td>
          <td>
            Day in month
          </td>
          <td>
            <a href="http://download.oracle.com/javase/1.5.0/docs/api/java/text/SimpleDateFormat.html#number">Number</a>
          </td>
          <td>
            <code>10</code>
          </td>
        </tr>
        <tr bgcolor="#eeeeff">
          <td>
            <code>F</code>
          </td>
          <td>
            Day of week in month
          </td>
          <td>
            <a href="http://download.oracle.com/javase/1.5.0/docs/api/java/text/SimpleDateFormat.html#number">Number</a>
          </td>
          <td>
            <code>2</code>
          </td>
        </tr>
        <tr>
          <td>
            <code>E</code>
          </td>
          <td>
            Day in week
          </td>
          <td>
            <a href="http://download.oracle.com/javase/1.5.0/docs/api/java/text/SimpleDateFormat.html#text">Text</a>
          </td>
          <td>
            <code>Tuesday</code>; <code>Tue</code>
          </td>
        </tr>
        <tr bgcolor="#eeeeff">
          <td>
            <code>a</code>
          </td>
          <td>
            Am/pm marker
          </td>
          <td>
            <a href="http://download.oracle.com/javase/1.5.0/docs/api/java/text/SimpleDateFormat.html#text">Text</a>
          </td>
          <td>
            <code>PM</code>
          </td>
        </tr>
        <tr>
          <td>
            <code>H</code>
          </td>
          <td>
            Hour in day (0-23)
          </td>
          <td>
            <a href="http://download.oracle.com/javase/1.5.0/docs/api/java/text/SimpleDateFormat.html#number">Number</a>
          </td>
          <td>
            <code>0</code>
          </td>
        </tr>
        <tr bgcolor="#eeeeff">
          <td>
            <code>k</code>
          </td>
          <td>
            Hour in day (1-24)
          </td>
          <td>
            <a href="http://download.oracle.com/javase/1.5.0/docs/api/java/text/SimpleDateFormat.html#number">Number</a>
          </td>
          <td>
            <code>24</code>
          </td>
        </tr>
        <tr>
          <td>
            <code>K</code>
          </td>
          <td>
            Hour in am/pm (0-11)
          </td>
          <td>
            <a href="http://download.oracle.com/javase/1.5.0/docs/api/java/text/SimpleDateFormat.html#number">Number</a>
          </td>
          <td>
            <code>0</code>
          </td>
        </tr>
        <tr bgcolor="#eeeeff">
          <td>
            <code>h</code>
          </td>
          <td>
            Hour in am/pm (1-12)
          </td>
          <td>
            <a href="http://download.oracle.com/javase/1.5.0/docs/api/java/text/SimpleDateFormat.html#number">Number</a>
          </td>
          <td>
            <code>12</code>
          </td>
        </tr>
        <tr>
          <td>
            <code>m</code>
          </td>
          <td>
            Minute in hour
          </td>
          <td>
            <a href="http://download.oracle.com/javase/1.5.0/docs/api/java/text/SimpleDateFormat.html#number">Number</a>
          </td>
          <td>
            <code>30</code>
          </td>
        </tr>
        <tr bgcolor="#eeeeff">
          <td>
            <code>s</code>
          </td>
          <td>
            Second in minute
          </td>
          <td>
            <a href="http://download.oracle.com/javase/1.5.0/docs/api/java/text/SimpleDateFormat.html#number">Number</a>
          </td>
          <td>
            <code>55</code>
          </td>
        </tr>
        <tr>
          <td>
            <code>S</code>
          </td>
          <td>
            Millisecond
          </td>
          <td>
            <a href="http://download.oracle.com/javase/1.5.0/docs/api/java/text/SimpleDateFormat.html#number">Number</a>
          </td>
          <td>
            <code>978</code>
          </td>
        </tr>
        <tr bgcolor="#eeeeff">
          <td>
            <code>z</code>
          </td>
          <td>
            Time zone
          </td>
          <td>
            <a href="http://download.oracle.com/javase/1.5.0/docs/api/java/text/SimpleDateFormat.html#timezone">General time zone</a>
          </td>
          <td>
            <code>Pacific Standard Time</code>; <code>PST</code>; <code>GMT-08:00</code>
          </td>
        </tr>
        <tr>
          <td>
            <code>Z</code>
          </td>
          <td>
            Time zone
          </td>
          <td>
            <a href="http://download.oracle.com/javase/1.5.0/docs/api/java/text/SimpleDateFormat.html#rfc822timezone">RFC 822 time zone</a>
          </td>
          <td>
            <code>-0800</code>
          </td>
        </tr>
      </table>
    </blockquote>
    Pattern letters are usually repeated, as their number determines the exact presentation:

    <ul>
      <li>
        <strong><a name="text">Text:</a></strong>&#160;For formatting, if the number of pattern letters is 4 or more, the full form is used; otherwise a short or abbreviated form is used if available. For parsing, both forms are accepted, independent of the number of pattern letters.
      </li>
      <li>
        <strong><a name="number">Number:</a></strong>&#160;For formatting, the number of pattern letters is the minimum number of digits, and shorter numbers are zero-padded to this amount. For parsing, the number of pattern letters is ignored unless it's needed to separate two adjacent fields.
      </li>
      <li>
        <strong><a name="year">Year:</a></strong>&#160;For formatting, if the number of pattern letters is 2, the year is truncated to 2 digits; otherwise it is interpreted as a <a href="http://download.oracle.com/javase/1.5.0/docs/api/java/text/SimpleDateFormat.html#number">number</a>.

        <p>
          For parsing, if the number of pattern letters is more than 2, the year is interpreted literally, regardless of the number of digits. So using the pattern &quot;MM/dd/yyyy&quot;, &quot;01/11/12&quot; parses to Jan 11, 12 A.D.
        </p>
        <p>
          For parsing with the abbreviated year pattern (&quot;y&quot; or &quot;yy&quot;),
 <code>SimpleDateFormat</code>&#160; must interpret the abbreviated year relative to some century. It does this by adjusting dates to be within 80 years before and 20 years after the time the <code>SimpleDateFormat</code>&#160;instance is created. For example, using a pattern of &quot;MM/dd/yy&quot; and a <code>SimpleDateFormat</code>&#160; instance created on Jan 1, 1997, the string &quot;01/11/12&quot; would be interpreted as Jan 11, 2012 while the string &quot;05/04/64&quot; would be interpreted as May 4, 1964. During parsing, only strings consisting of exactly two digits, as defined by <code><a href="http://download.oracle.com/javase/1.5.0/docs/api/java/lang/Character.html#isDigit%28char%29">Character.isDigit(char)</a></code>, will be parsed into the default century. Any other numeric string, such as a one digit string, a thee or more digit string, or a two digit string that isn't all digits (for example, &quot;-1&quot;), is interpreted literally. So &quot;01/02/3&quot; or &quot;01/02/003&quot; are parsed, using the same pattern, as Jan 2, 3 AD. Likewise, &quot;01/02/-3&quot; is parsed as Jan 2, 4 BC.
        </p>
      </li>
      <li>
        <strong><a name="month">Month:</a></strong>&#160;If the number of pattern letters is 3 or more, the month is interpreted as <a href="http://download.oracle.com/javase/1.5.0/docs/api/java/text/SimpleDateFormat.html#text">text</a>; otherwise, it is interpreted as a <a href="http://download.oracle.com/javase/1.5.0/docs/api/java/text/SimpleDateFormat.html#number">number</a>.
      </li>
      <li>
        <strong><a name="timezone">General time zone:</a></strong>&#160;Time zones are interpreted as <a href="http://download.oracle.com/javase/1.5.0/docs/api/java/text/SimpleDateFormat.html#text">text</a>&#160; if they have names. For time zones representing a GMT offset value, the following syntax is used:

        <pre>     <i><a name="GMTOffsetTimeZone">GMTOffsetTimeZone:</a></i>
             <code>GMT</code> <i>Sign</i> <i>Hours</i> <code>:</code> <i>Minutes</i>
     <i>Sign:</i> one of
             <code>+ -</code>
     <i>Hours:</i>
             <i>Digit</i>
             <i>Digit</i> <i>Digit</i>
     <i>Minutes:</i>
             <i>Digit</i> <i>Digit</i>
     <i>Digit:</i> one of
             <code>0 1 2 3 4 5 6 7 8 9</code></pre>
        <i>Hours</i>&#160;must be between 0 and 23, and <i>Minutes</i>&#160;must be between 00 and 59. The format is locale independent and digits must be taken from the Basic Latin block of the Unicode standard.

        <p>
          For parsing, <a href="http://download.oracle.com/javase/1.5.0/docs/api/java/text/SimpleDateFormat.html#rfc822timezone">RFC 822 time zones</a>&#160;are also accepted.
        </p>
      </li>
      <li>
        <strong><a name="rfc822timezone">RFC 822 time zone:</a></strong>&#160;For formatting, the RFC 822 4-digit time zone format is used:

        <pre>     <i>RFC822TimeZone:</i>
             <i>Sign</i> <i>TwoDigitHours</i> <i>Minutes</i>
     <i>TwoDigitHours:</i>
             <i>Digit Digit</i></pre>
        <i>TwoDigitHours</i>&#160;must be between 00 and 23. Other definitions are as for <a href="http://download.oracle.com/javase/1.5.0/docs/api/java/text/SimpleDateFormat.html#timezone">general time zones</a>.

        <p>
          For parsing, <a href="http://download.oracle.com/javase/1.5.0/docs/api/java/text/SimpleDateFormat.html#timezone">general time zones</a>&#160;are also accepted.
        </p>
      </li>
    </ul>
    <code>SimpleDateFormat</code>&#160;also supports <em>localized date and time pattern</em>&#160; strings. In these strings, the pattern letters described above may be replaced with other, locale dependent, pattern letters. <code>SimpleDateFormat</code>&#160; does not deal with the localization of text other than the pattern letters; that's up to the client of the class.
  </body>
</html></richcontent>
<hook NAME="SummaryNode"/>
<node TEXT="" FOLDED="true" ID="ID_424923563" CREATED="1311432086263" MODIFIED="1311621906789" LINK="http://download.oracle.com/javase/1.5.0/docs/api/java/text/SimpleDateFormat.html" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <blockquote>
      <table cellspacing="3" summary="Examples of date and time patterns interpreted in the U.S. locale" border="0" cellpadding="0">
        <tr bgcolor="#ccccff">
          <td>
            
          </td>
        </tr>
      </table>
    </blockquote>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Refine" LAST="false"/>
</hook>
<node TEXT="" ID="ID_161379435" CREATED="1311432447147" MODIFIED="1311432935054" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Example" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <h4>
      Examples
    </h4>
    The following examples show how date and time patterns are interpreted in the U.S. locale. The given date and time are 2001-07-04 12:08:56 local time in the U.S. Pacific Time time zone.

    <blockquote>
      <table cellspacing="3" summary="Examples of date and time patterns interpreted in the U.S. locale" border="0" cellpadding="0">
        <tr bgcolor="#ccccff">
          <th align="left">
            Date and Time Pattern
          </th>
          <th align="left">
            Result
          </th>
        </tr>
        <tr>
          <td>
            <code>&quot;yyyy.MM.dd G 'at' HH:mm:ss z&quot;</code>
          </td>
          <td>
            <code>2001.07.04 AD at 12:08:56 PDT</code>
          </td>
        </tr>
        <tr bgcolor="#eeeeff">
          <td>
            <code>&quot;EEE, MMM d, ''yy&quot;</code>
          </td>
          <td>
            <code>Wed, Jul 4, '01</code>
          </td>
        </tr>
        <tr>
          <td>
            <code>&quot;h:mm a&quot;</code>
          </td>
          <td>
            <code>12:08 PM</code>
          </td>
        </tr>
        <tr bgcolor="#eeeeff">
          <td>
            <code>&quot;hh 'o''clock' a, zzzz&quot;</code>
          </td>
          <td>
            <code>12 o'clock PM, Pacific Daylight Time</code>
          </td>
        </tr>
        <tr>
          <td>
            <code>&quot;K:mm a, z&quot;</code>
          </td>
          <td>
            <code>0:08 PM, PDT</code>
          </td>
        </tr>
        <tr bgcolor="#eeeeff">
          <td>
            <code>&quot;yyyyy.MMMMM.dd GGG hh:mm aaa&quot;</code>
          </td>
          <td>
            <code>02001.July.04 AD 12:08 PM</code>
          </td>
        </tr>
        <tr>
          <td>
            <code>&quot;EEE, d MMM yyyy HH:mm:ss Z&quot;</code>
          </td>
          <td>
            <code>Wed, 4 Jul 2001 12:08:56 -0700</code>
          </td>
        </tr>
        <tr bgcolor="#eeeeff">
          <td>
            <code>&quot;yyMMddHHmmssZ&quot;</code>
          </td>
          <td>
            <code>010704120856-0700</code>
          </td>
        </tr>
        <tr>
          <td>
            <code>&quot;yyyy-MM-dd'T'HH:mm:ss.SSSZ&quot;</code>
          </td>
          <td>
            <code>2001-07-04T12:08:56.235-0700</code>
          </td>
        </tr>
      </table>
    </blockquote>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="" ID="ID_317387284" CREATED="1311620492485" MODIFIED="1311620649255" LINK="http://download.oracle.com/javase/1.5.0/docs/api/java/util/Formatter.html">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Refine" LAST="false"/>
</hook>
</node>
</node>
</node>
<node TEXT="Reset style" ID="ID_25667795" CREATED="1310720110705" MODIFIED="1311405449746" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      When a property is checked, its change overrides the value set by Style. To return to the value of the style, uncheck Change.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Not included" FOLDED="true" ID="ID_1971371205" CREATED="1310714589905" MODIFIED="1311405449746" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Not all properties can be set in Properties Panel.
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        Format
      </li>
    </ul>
  </body>
</html></richcontent>
<node TEXT="Blinking node" ID="ID_1863972932" CREATED="1310714589905" MODIFIED="1310716556350" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>Blinking node</b>&#160;is a&#160;node which cycles through a number of colors.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Blend color" ID="ID_661366038" CREATED="1310714589905" MODIFIED="1310716556350">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
</node>
<node TEXT="Map background" ID="ID_657472078" CREATED="1310714589905" MODIFIED="1310716556335" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>Map background</b>&#160;is the&#160;color of background.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Hidden edge" ID="ID_1015777202" CREATED="1310714589905" MODIFIED="1310716556335" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>Hidden edge</b>&#160;is an edge style when the&#160;edge is invisible.
    </p>
    <p>
      &#160;
    </p>
  </body>
</html></richcontent>
</node>
</node>
</node>
<node TEXT="Calendar and Attributes" FOLDED="true" ID="ID_1981388225" CREATED="1310720605287" MODIFIED="1311487283381">
<icon BUILTIN="../AttributesView"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="SubMenu" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="5" OBJECT="java.lang.Long|5"/>
<node TEXT="Calendar" FOLDED="true" ID="ID_22413267" CREATED="1310720744003" MODIFIED="1311412001798" TEXT_SHORTENED="true" FORMAT="#0.####">
<icon BUILTIN="icon_not_found"/>
<icon BUILTIN="../TimeManagementAction"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="SubMenu" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="5"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>See also&#160;</b>
    </p>
    <ul>
      <li>
        <i>Node features &gt; Time management</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<node TEXT="year-month-day" ID="ID_144760412" CREATED="1310720753106" MODIFIED="1311405889261">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="5"/>
</node>
<node TEXT="hours-minutes" ID="ID_143054900" CREATED="1312570564893" MODIFIED="1312570588997"/>
<node TEXT="Reset Calendar" ID="ID_435441461" CREATED="1310720775437" MODIFIED="1311405889261">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="5"/>
</node>
<node TEXT="Insert Date in Selection [date text in node]" ID="ID_63295810" CREATED="1310720786116" MODIFIED="1312570693602" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="5"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To add a date text according to standard date format
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Edit Script... [Execute script]" FOLDED="true" ID="ID_252404367" CREATED="1312535206255" MODIFIED="1312535543833" TEXT_SHORTENED="true">
<attribute NAME="Chapter" VALUE="5"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Opens dialog to enter a script which will be executed at the time of the reminder, if the mind map is open at this time.
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        Edit script
      </li>
    </ul>
  </body>
</html></richcontent>
<node TEXT="Reminder triggers script" ID="ID_121096889" CREATED="1312569727978" MODIFIED="1312570228647" TEXT_SHORTENED="true">
<attribute NAME="Chapter" VALUE="5"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      This example contains a script that will add the ok-icon to the present node.
    </p>
    <p>
      
    </p>
    <p>
      <b>Do</b>
    </p>
    <ul>
      <li>
        Open Edit script, see it contains:

        <pre class="code">node.<span class="me1">getIcons</span><span class="br0">()</span>.<span class="me1">addIcon</span><span class="br0">(</span><span class="st0">&quot;button_ok&quot;</span><span class="br0">)</span></pre>
      </li>
      <li>
        Add a reminder to the node at 2 minutes from now.
      </li>
      <li>
        Keep the mindmap in memory (do nothing to close it).
      </li>
      <li>
        Wait 2 minutes.
      </li>
      <li>
        See the reminder going active: clock is flashing; see ok-button added.
      </li>
      <li>
        remove the ok-button if you want to try again.
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Example" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Remind me at this date" ID="ID_721277173" CREATED="1310720824358" MODIFIED="1312571253753" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="5"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To set a reminder at the date and time set.
    </p>
    <p>
      
    </p>
    <p>
      <b>Note</b>
    </p>
    <p>
      The reminder wil also trigger the script, if defined AND the mindmap is open. It is not necessary that the map is visible.
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        Edit script... [Execute script]
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="Remove reminder" FOLDED="true" ID="ID_1320064607" CREATED="1310720836910" MODIFIED="1311405889292">
<icon BUILTIN="../ReminderHookAction"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="5"/>
<node TEXT="" ID="ID_1442797370" CREATED="1312569522171" MODIFIED="1312569700286" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        Also removes script
      </li>
    </ul>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>Remind me at this date</i>&#160;to change date/time without deleting the script
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
</hook>
</node>
</node>
</node>
<node TEXT="Attributes" FOLDED="true" ID="ID_1738195082" CREATED="1310720848133" MODIFIED="1311412804759" TEXT_SHORTENED="true">
<icon BUILTIN="../showAttributes"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="SubMenu" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3,4" OBJECT="org.freeplane.features.format.FormattedNumber|34.0|#,#"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>Node features &gt; Attributes</i>
      </li>
      <li>
        <i>View &gt; Attributes</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<node TEXT="New attribute" ID="ID_682004984" CREATED="1310720854444" MODIFIED="1311402834908">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3"/>
</node>
<node TEXT="Optimal width" ID="ID_19755775" CREATED="1310720862656" MODIFIED="1311402834940">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3"/>
</node>
<node TEXT="Attribute data formatting" ID="ID_539941097" CREATED="1311410715399" MODIFIED="1311414561884" TEXT_SHORTENED="true">
<attribute NAME="Chapter" VALUE="3,4"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To format the attribute value, select and modify on of the format templates.
    </p>
    <p>
      
    </p>
    <p>
      See also
    </p>
    <ul>
      <li>
        <i>Tools &gt; Preferences</i>
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="Table of attributes" ID="ID_110950554" CREATED="1310720873662" MODIFIED="1311414481419" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3,4"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      An <b>attribute</b>&#160;has a name and value/data, meaning name=value.
    </p>
    <p>
      
    </p>
    <p>
      The list of attributes consists of two columns. The left column can contain the attribute's&#160;&#160;name and the right column its value.
    </p>
    <p>
      
    </p>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        The attribute's value can be automatically formatted, see Preferences.
      </li>
      <li>
        To prevent automatic data formatting incidentally, type as a first character a non-number. The entered text will be treated as text, also if the first character is deleted afterwards. Examples:

        <ul>
          <li>
            When you enter 1,2,,3 this will transform in 123 (except if you defined the template to be #,#,#)&#160;
          </li>
          <li>
            When you enter &quot;1,2,3 and next remove &quot;, you will keep 1,2,3&#160;
          </li>
        </ul>
      </li>
    </ul>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>Attribute data formatting</i>
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="" ID="ID_1128827136" CREATED="1310841040542" MODIFIED="1311402834955" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      It is possible to add or edit the attribute of one node at the time.
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>Node features &gt; Attributes</i>&#160;to add the same attribute at once to a group of selected nodes.
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="SummaryNode"/>
</node>
</node>
</node>
</node>
<node TEXT="(context)Menu&apos;s, Toolbars and Panels" FOLDED="true" ID="ID_832730564" CREATED="1310672464952" MODIFIED="1311402779279" TEXT_SHORTENED="true">
<hook NAME="SummaryNode"/>
<attribute_layout NAME_WIDTH="82" VALUE_WIDTH="228"/>
<attribute NAME="Chapter" VALUE="2,4"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      &#160;Context menus are powerful alternative to using menu-bars.
    </p>
    <p>
      
    </p>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        To open a context menu, right-click on the object you want to display its context menu.
      </li>
    </ul>
  </body>
</html></richcontent>
<node TEXT="Context menu" ID="ID_497182548" CREATED="1310890907946" MODIFIED="1310891033981" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      A <b>context menu</b>&#160;is a menu associated with a particular object which only contains menu items most relevant for this object. A context menu is opened by right-clicking the object.
    </p>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Zoom In" ID="ID_1817335801" CREATED="1310134532663" MODIFIED="1310721457759" TEXT_SHORTENED="true">
<icon BUILTIN="../ZoomIn24"/>
<hook NAME="FirstGroupNode"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">Makes the font size and the distance between nodes larger; fits less of the map on the screen.</span></font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Zoom Out" ID="ID_1176192683" CREATED="1310134532663" MODIFIED="1310721457931" TEXT_SHORTENED="true">
<icon BUILTIN="../ZoomOut24"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">Makes the font size and the distance between nodes smaller; fits more of the map on the screen.</span></font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Zoom to fit page" ID="ID_1360497080" CREATED="1266249692203" MODIFIED="1310721457900" TEXT_SHORTENED="true">
<icon BUILTIN="../FitToPage"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">Adjusts the font size and distance between nodes to fit the entire map on the screen. If many nodes are visible (unfolded), the font may be rendered too small to be readable.</span></font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Outline view" ID="ID_519734292" CREATED="1266247214640" MODIFIED="1310721911393" TEXT_SHORTENED="true">
<icon BUILTIN="../outline_view"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif">Toggles the display of the map between default (radial view) or list (outline view). </font>
    </p>
    <p>
      
    </p>
    <p>
      
    </p>
    <p>
      
    </p>
    <p>
      <b>Note </b>
    </p>
    <ul>
      <li>
        <p>
          <font color="#000000" face="SansSerif, sans-serif">Node spacing may be fine-tuned using Preferences. </font>
        </p>
      </li>
      <li>
        <p>
          <font color="#000000" face="SansSerif, sans-serif">Some aspects of visual appearance are simplified while in Outline view, and more information may be visible on-screen at one time than in the normal Mind Map view. </font>This view may be handy in printing a large maps.
        </p>
      </li>
    </ul>
    <p>
      
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Rectangular selection" ID="ID_1851007191" CREATED="1266247206859" MODIFIED="1310721457822" TEXT_SHORTENED="true">
<icon BUILTIN="../ShowSelectionAsRectangleAction"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">Toggles the appearance of selecting a node. When in Rectangular selection mode, the selected node has a solid line around it, with rounded corners. When not in Rectangular mode, the selected node has a grey background color. </span></font>
    </p>
    <p>
      
    </p>
    <p>
      <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif"><b>See also</b> </span></font>
    </p>
    <ul>
      <li>
        <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif"><i>Tools &gt; Preferences &gt; Appearance &gt; Selection Colors.</i></span></font>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Display Status Line" ID="ID_183960920" CREATED="1303929119304" MODIFIED="1310724563973" TEXT_SHORTENED="true">
<icon BUILTIN="icon_not_found"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Toggles the visibility of a status line at the bottom of the window.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Map view" ID="ID_1126116586" CREATED="1306262990022" MODIFIED="1310711210217">
<hook NAME="SummaryNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
</node>
<node TEXT="Display Tool Tips for Nodes [Display hidden content at cursor]" FOLDED="true" ID="ID_152459666" CREATED="1310673142140" MODIFIED="1312541253569" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Toggles the visibility of Tool Tips.
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>View &gt; Show notes in map </i>
      </li>
      <li>
        View &gt; Attributes
      </li>
    </ul>
  </body>
</html></richcontent>
<node TEXT="Tool Tips" ID="ID_1399586746" CREATED="1310718250232" MODIFIED="1311402583545" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Tool Tip is a window that opens when the mouse cursor is over a node and &#160;View &gt; Display Tootips is set. You are reading the current text in a Tool Tip window.
    </p>
    <p>
      
    </p>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        The window shows otherwise hidden content, see <i>Node &gt; Shortened node content</i>.
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="1"/>
</node>
<node TEXT="Set width tool tip" ID="ID_383683750" CREATED="1311926079888" MODIFIED="1311926122758" LINK="#ID_215853027"/>
</node>
<node TEXT="Display Node styles in Tool Tio" ID="ID_499630441" CREATED="1312541024559" MODIFIED="1312541264567" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Toggles visibility of the list of styles which apply to the selected node.
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        Display Tool tips for Nodes
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="Show modification times" ID="ID_208481180" CREATED="1266247851640" MODIFIED="1310723748068" TEXT_SHORTENED="true">
<icon BUILTIN="../kword"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Toggles visibility of modification times when the cursor hovers over the node.
    </p>
    <p>
      
    </p>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        Date/time of last creation/modification
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="shortened node content [Hide node features]" ID="ID_787093188" CREATED="1291155413971" MODIFIED="1311920638430" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Toggles visibility of the content of all nodes (if the cursor is not over the node). The effect is:
    </p>
    <ul>
      <li>
        Basic node text is limited in length;
      </li>
      <li>
        Node features are hidden (Details, Attributes, Notes)
      </li>
    </ul>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>View &gt; Display Tool Tips for notes</i>&#160;to make the hidden context visible when the cursor is hovering over the node.
      </li>
      <li>
        <i>View &gt; Show notes in map </i>
      </li>
      <li>
        <i>View &gt; Attributes </i>
      </li>
      <li>
        <i>View &gt; Shortened node content</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Change revisions background color [Mark revisions]" FOLDED="true" ID="ID_974319465" CREATED="1266247833890" MODIFIED="1311411314503" BACKGROUND_COLOR="#ffffff" TEXT_SHORTENED="true">
<icon BUILTIN="../RevisionPluginAction"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Toggles between visibility marking of revisions.
    </p>
    <p>
      
    </p>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        Standard the revision is marked by making background color = yellow.
      </li>
      <li>
        To undo the marking: select the marked node and uncheck the Change box in the Properties Panel.
      </li>
    </ul>
    <p>
      <b>See also:</b>
    </p>
    <ul>
      <li>
        <p>
          <i>View &gt; Show modification times</i>
        </p>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<node TEXT="Set default revision color" ID="ID_1766752356" CREATED="1311925728592" MODIFIED="1312529890753" LINK="#ID_665414546" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="Procedure" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To remove a revision, for each node uncheck the checkbox for background color.
    </p>
    <p>
      
    </p>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        This way of showing/hiding revisions is outdated.
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="Alternatives for marking revisions" ID="ID_961541761" CREATED="1312527817946" MODIFIED="1312530148247" TEXT_SHORTENED="true">
<attribute NAME="Chapter" VALUE="1"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To display a revision:
    </p>
    <ul>
      <li>
        find/filter by revision date

        <ul>
          <li>
            Revisions after a date or whin a period of time can be found and filtered
          </li>
          <li>
            Pro's: revisions are kept automatically; filter rule can be defined once
          </li>
          <li>
            Con's: the revision date is set to the latest revision. It is not possible to know the history of revisions..
          </li>
        </ul>
      </li>
      <li>
        find/filter by user defined attribute <i>Revision</i>&#160;= <i>nr/date</i>

        <ul>
          <li>
            Pro's: History of revision dates pro node (not content) is kept.
          </li>
          <li>
            Con's: to be (un)set manually pro node or selected group of nodes.
          </li>
        </ul>
      </li>
      <li>
        use conditional map styles to show a style <i>Revision</i>

        <ul>
          <li>
            To be used with revision dates or revision attributes
          </li>
          <li>
            Pro's: see above. Filter rules are kept an managed at map level, in Conditional map styles list.
          </li>
          <li>
            Con's: see above.
          </li>
        </ul>
      </li>
      <li>
        (Un)set a discriminating background color pro node

        <ul>
          <li>
            Pro's: Simple
          </li>
          <li>
            Con's: Restricted style option. Fully manual.
          </li>
        </ul>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="Procedure" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Don&apos;t mark formula&apos;s with a border [Unmark formula&apos;s]" ID="ID_1847984972" CREATED="1303929296863" MODIFIED="1310723748084" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Toggle between not marking or marking the visibility of formula's by a rectangular, colored surrounding. defaultt is a surrounding.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Set maximum node width" ID="ID_826769128" CREATED="1288558613969" MODIFIED="1310723748053" TEXT_SHORTENED="true">
<icon BUILTIN="../max_node_width"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif">Opens dialog to changes the visible width of all nodes in the map, measured in pixels. &#160;Default is 600.&#160; </font>
    </p>
    <p>
      
    </p>
    <p>
      <font face="SansSerif, sans-serif" color="#000000"><b>Note </b></font>
    </p>
    <ul>
      <li>
        <font face="SansSerif, sans-serif" color="#000000">To control the width of a particular node, make shorter lines, use <i>Enter</i>&#160;or <i>Shift + Enter.</i></font>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Node view" ID="ID_533984642" CREATED="1310673881816" MODIFIED="1310711210202">
<hook NAME="SummaryNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
</node>
<node TEXT="Toggle Details [(Un)Roll Details text]" ID="ID_734978433" CREATED="1291155323140" MODIFIED="1310726671857" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Toggles the visibility of the Details field under the basic node text.
    </p>
    <p>
      
    </p>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        Alternative: Click the triangle button below the node
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<hook NAME="FirstGroupNode"/>
</node>
<node TEXT="Details view" ID="ID_147261423" CREATED="1310724212271" MODIFIED="1310724238167">
<hook NAME="SummaryNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
</node>
<node TEXT="Attributes" ID="ID_353026241" CREATED="1266249735046" MODIFIED="1311921014960" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Toggles the visibility of attributes as set by&#160;<i>Node features &gt; Attribute manger. </i>
    </p>
    <p>
      
    </p>
    <p>
      Options are:
    </p>
    <ul>
      <li>
        Show Selected attributes
      </li>
      <li>
        Show all attributes
      </li>
      <li>
        Hide All attributes
      </li>
    </ul>
    <p>
      See also
    </p>
    <ul>
      <li>
        <p>
          &#160;<i>Node features &gt; Attribute manger </i>
        </p>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<hook NAME="FirstGroupNode"/>
</node>
<node TEXT="Show/hide icon-attributes" ID="ID_94712823" CREATED="1310727590558" MODIFIED="1311923396872" TEXT_SHORTENED="true">
<hook NAME="SummaryNode"/>
<attribute_layout VALUE_WIDTH="228"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>Tools &gt; Preferences &gt; Appearances &gt; Icons</i>&#160;to standardly Show icons for attributes
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="Note panel [Show Note panel]" FOLDED="true" ID="ID_1073686439" CREATED="1266249699921" MODIFIED="1310735225391" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif">Toggles the Note panel visible or hidden. </font>
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        Note position
      </li>
      <li>
        Show notes in map
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<hook NAME="FirstGroupNode"/>
<node TEXT="Note panel" ID="ID_1985668082" CREATED="1310725021296" MODIFIED="1310725159902" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>Note panel</b>&#160;is a window which is independently placed of the selected node, in which the Note content of the selected node is shown and can be edited.
    </p>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Note position [Note panel position]" ID="ID_1996327153" CREATED="1266249705546" MODIFIED="1310726068112" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif">Determines the placement of the Note panel. Options are Top, Left, Right, and Bottom.</font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Show notes in map" ID="ID_389897702" CREATED="1291155591515" MODIFIED="1310726068112" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Toggles the visibility of a node's note text below its basic node text.
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>View &gt; Shortened node content</i>
      </li>
      <li>
        <i>Note panel</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Notes view" ID="ID_1709371267" CREATED="1306012919034" MODIFIED="1310727306434" TEXT_SHORTENED="true">
<hook NAME="SummaryNode"/>
<attribute_layout VALUE_WIDTH="215"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>Tools &gt; Preferences &gt; Appearances &gt; Icons</i>&#160;to standardly NOT show icons for notes
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="Show icons hierarchically" ID="ID_1929367560" CREATED="1310672205671" MODIFIED="1310726026460" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Toggles that the icons present in descendants of a node, are replicated in the node.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Show intersections of icons" ID="ID_664401378" CREATED="1310672261152" MODIFIED="1310726026460">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Icons view" ID="ID_382172833" CREATED="1310672383972" MODIFIED="1310728259721" TEXT_SHORTENED="true">
<hook NAME="SummaryNode"/>
<attribute_layout VALUE_WIDTH="160"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>Tools &gt; Preferences &gt; Appearances &gt; Icons</i>&#160;to standardly:

        <ul>
          <li>
            Show icons for attributes
          </li>
          <li>
            Not show icons for notes
          </li>
          <li>
            Structured icons tool bar
          </li>
        </ul>
        <p>
          
        </p>
      </li>
    </ul>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        With Java Applet the following are always showing

        <ul>
          <li>
            Icons for attributes (bug)
          </li>
        </ul>
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Nodes" STYLE_REF="MainMenuAccent" FOLDED="true" POSITION="right" ID="ID_1435472587" CREATED="1303928254072" MODIFIED="1311402729842" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To provide basic node functions.
    </p>
    <p>
      
    </p>
    <p>
      Each node contains an central area in which a text can be displayed, the&#160;basic<b>&#160;node text</b>. This text can be surrounded by a line or <b>bubble</b>. A node generally has a hierarchical, parent-child relation with one or more other nodes. This relation is represented by a line called <b>edge</b>&#160;drawn between parent and child. The properties of an edge can be set in the child, not the parent. .&#160;Two arbitrary nodes can be connected with a visible line or arrow, a so-called <b>connector</b>. This connector can have <b>labels</b>&#160;attached to it signifying its meaning. Nodes which have a common parent are called <b>siblings</b>. A group of siblings can have a <b>sum node</b>&#160;which connects the siblings through an accolade.
    </p>
    <p>
      
    </p>
    <p>
      A node can have additional features, see<i>&#160;Node features</i>. The basic node text can be edited with an inline editor, see <i>Node &gt; Edit node</i>. The basic node text can also be edited in a separate window, see<i>&#160;Node &gt; Edit node in separate Dialog</i>. The former works faster. The latter has more advanced options like splitting and joining nodes and HTML-editing.
    </p>
    <p>
      
    </p>
    <p>
      <b>See also: </b>
    </p>
    <ul>
      <li>
        <p>
          <i>View &gt; Properties Panel &gt; Format </i>&#160;for formatting text or generating a <b>node number</b>, or <b>date</b>
        </p>
      </li>
      <li>
        <i>Languages </i>&#160;in context menu (right-click) for spell checking
      </li>
      <li>
        <i>Node features</i>&#160;to add additional features
      </li>
    </ul>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        To add a special symbol as &#169;, copy and paste it from your word processor.
      </li>
      <li>
        <p>
          There are different languages for spell checking available on the Freeplane web site.
        </p>
      </li>
    </ul>
  </body>
</html></richcontent>
<attribute_layout VALUE_WIDTH="200"/>
<attribute NAME="Chapter" VALUE="1,2,7"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MainMenu" LAST="false"/>
</hook>
<node TEXT="New child node" ID="ID_942355748" CREATED="1266249828031" MODIFIED="1309638877810" TEXT_SHORTENED="true">
<icon BUILTIN="../idea"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000">Creates a new node as a child of&#160;&#160;the currently-selected node. </font>
    </p>
    <p>
      
    </p>
    <p>
      <font face="SansSerif, sans-serif" color="#000000"><b>Same effect: </b></font>
    </p>
    <ul>
      <li>
        <font face="SansSerif, sans-serif" color="#000000">press key <i>Ins</i></font>
      </li>
      <li>
        press icon <i>Bulb</i>&#160;in <i>Toolbar</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="New sibling node" ID="ID_1544212529" CREATED="1266249835578" MODIFIED="1309634523961" TEXT_SHORTENED="true">
<icon BUILTIN="../NewSiblingAction"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <span style="color: #000000; font-family: SansSerif, sans-serif"><font face="SansSerif, sans-serif" color="#000000">Creates a new node below the currently selected node, at the same hierarchical level. </font></span>
    </p>
    <p>
      
    </p>
    <p>
      <span style="color: #000000; font-family: SansSerif, sans-serif"><font face="SansSerif, sans-serif" color="#000000"><b>Same effect:</b> </font></span>
    </p>
    <ul>
      <li>
        <span style="color: #000000; font-family: SansSerif, sans-serif"><font face="SansSerif, sans-serif" color="#000000">press <i>Enter</i></font></span>
      </li>
      <li>
        if in editing mode: press two times <i>Enter</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="New previous sibling node" ID="ID_852536639" CREATED="1266249852093" MODIFIED="1309723810154" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Creates a new node <b>above</b>&#160;the node that is currently selected, at the same hierarchical level.
    </p>
    <p>
      
    </p>
    <p>
      Same effect:
    </p>
    <ul>
      <li>
        press <i>&lt;Shift&gt;+&lt;Enter&gt;</i>&#160;key combination.
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="New parent node" ID="ID_1509185966" CREATED="1266249870703" MODIFIED="1309634530716" TEXT_SHORTENED="true">
<icon BUILTIN="../stock_text_indent"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000">Creates a new node between the current node and its parent node. The new node is a child of the original parent node.</font>
    </p>
    <p>
      
    </p>
    <p>
      Same effect:
    </p>
    <ul>
      <li>
        press <i>&lt;Shift&gt; + &lt;Insert&gt;</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="New node" STYLE_REF="MenuGroupLabel" ID="ID_302533881" CREATED="1309638877685" MODIFIED="1309980625272" TEXT_SHORTENED="true">
<hook NAME="SummaryNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
</node>
<node TEXT="New Summary node" FOLDED="true" ID="ID_1126176775" CREATED="1304191328224" MODIFIED="1312701414433" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Creates a new <i>Summary node</i>&#160;which connects the range of selected siblings by an accolade.
    </p>
    <p>
      
    </p>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        Technically the summary node is a sibling node, below the lowest node in the range.
      </li>
      <li>
        The upper selected node is marked as the first node of the node group.
      </li>
    </ul>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>Node features &gt; Begin node group</i>&#160;to in/decrease the range of included nodes.
      </li>
      <li>
        <i>Node features &gt; Summary Node</i>&#160;to convert the Summary node to a sibbling node.
      </li>
    </ul>
  </body>
</html>
</richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<hook NAME="FirstGroupNode"/>
<node TEXT="" ID="ID_805111336" CREATED="1309875915466" MODIFIED="1310196224212" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        Technically the sum node is a sibling, below the group of siblings. See example.
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="" ID="ID_1070404224" CREATED="1309876392685" MODIFIED="1310196224212" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>Error</b>
    </p>
    <p>
      Sometimes an <b>error message</b>&#160;appears that it is not possible to create a Summary node for a particular group. It may be that one of the nodes in the group has a check for <i>Node &gt; Begin node group</i>. If so, select <i>Node &gt; Begin node group </i>to uncheck and try again.
    </p>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Begin node group" FOLDED="true" ID="ID_428394610" CREATED="1305384767382" MODIFIED="1312700223387" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Shows if the selected node is marked as first node of a Summary node group. Toggles the selected node as first node of a Summary node group.
    </p>
    <p>
      
    </p>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        The effect depends on three factors:

        <ol>
          <li>
            the Summary node being active (See <i>Node &gt; Summary node</i>)
          </li>
          <li>
            the selected node being in the range of the summary node and
          </li>
          <li>
            the selected node not yet&#160;&#160;being marked as first node
          </li>
        </ol>
        <p>
          If all three conditions are true, the selected node will be marked&#160;as &#160;the (new) first node of the Summary node group and&#160;&#160;the upper&#160;&#160;range of the Summary node will be decreased to the selected node.&#160;
        </p>
      </li>
      <li>
        If the Summary node is not active, the selected node may still be marked as the first node of a previously active Summary node. In that case the mark will be removed. The effect of this becomes apparent at the moment the inactive Summary node is reactivated, see <i>Node &gt; Summary node</i>.
      </li>
    </ul>
  </body>
</html>
</richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<node TEXT="Node group" ID="ID_1499184609" CREATED="1312552947980" MODIFIED="1312697277990" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      A <b>node group</b>&#160;is a group of neighboring siblings (same hierarchical level, same parent)&#160;&#160;which are within the range of a defined <i>Summary node</i>.
    </p>
    <p>
      
    </p>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        The range can enclose all siblings, or a subset of neighbouring siblings.
      </li>
      <li>
        &#160;The upper sibling in the range is called<b>&#160;first node</b>.
      </li>
      <li>
        The range may be decreased by defining another node of the node group as is the<i>&#160;first node</i>. As a result the siblings above it will be excluded from the node group.
      </li>
    </ul>
  </body>
</html>
</richcontent>
</node>
</node>
<node TEXT="Summary Node" ID="ID_1343097813" CREATED="1305384807731" MODIFIED="1312701610869" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Converts the selected node into a (active) summary node or turns a Summary node back into a sibling node (inactive Summary node).
    </p>
    <p>
      
    </p>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        The range of a newly created Summary node group are all siblings above the selected node. There are two exceptions to this:
      </li>
    </ul>
    <ol>
      <li>
        If one of the siblings is marked as First node of a Summary node group, the range starts with this node. This may happen if an existing Summary node has been deactivated.
      </li>
      <li>
        At the same hierarchical level, the ranges of two Summary nodes cannot overlap. Hence the range of the newly created Summary node starts directly below the existing Summary node.
      </li>
    </ol>
    <p>
      See also
    </p>
    <ul>
      <li>
        <i>Node &gt; Summary node &gt; Begin node group</i>
      </li>
    </ul>
  </body>
</html>
</richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Summary node / accollade" FOLDED="true" ID="ID_1919219984" CREATED="1304192858754" MODIFIED="1312697016612" TEXT_SHORTENED="true" STYLE="bubble">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Technically the summary node is a sibling of the node group, below the bottom node of the group.&#160;&#160;This position becomes apparent if you choose <i>Nodes &gt; Sum node</i>&#160;for a particular summary node<i>&#160; </i>Selecting <i>Nodes &gt; Summary node </i>again will restore its original position.
    </p>
    <p>
      
    </p>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        Use the up/down keys to move siblings into or out of the node group.
      </li>
      <li>
        Use drag an drop to move into or out of the node group.
      </li>
    </ul>
  </body>
</html>
</richcontent>
<hook NAME="SummaryNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Refine" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="Example" LAST="false"/>
</hook>
<node TEXT="1" ID="ID_1351236194" CREATED="1304192867654" MODIFIED="1310196224197" TEXT_SHORTENED="true">
<edge WIDTH="thin"/>
</node>
<node TEXT="2" FOLDED="true" ID="ID_1855113133" CREATED="1304192871203" MODIFIED="1310196224197" TEXT_SHORTENED="true">
<hook NAME="FirstGroupNode"/>
<edge WIDTH="thin"/>
<node TEXT="xx" ID="ID_1443415412" CREATED="1305386736429" MODIFIED="1310196224197" TEXT_SHORTENED="true"/>
</node>
<node TEXT="3" ID="ID_642318706" CREATED="1304192874252" MODIFIED="1310196224181" TEXT_SHORTENED="true">
<edge WIDTH="thin"/>
</node>
<node TEXT="4 = sumary node" ID="ID_1072874332" CREATED="1304192876647" MODIFIED="1310196224181" TEXT_SHORTENED="true">
<hook NAME="SummaryNode"/>
<edge WIDTH="thin"/>
</node>
<node TEXT="5" ID="ID_24722596" CREATED="1304192881678" MODIFIED="1310196224165" TEXT_SHORTENED="true">
<edge WIDTH="thin"/>
</node>
</node>
<node TEXT="Edit node [Edit node in-line]" ID="ID_1162372089" CREATED="1266247401765" MODIFIED="1311402583623" TEXT_SHORTENED="true">
<icon BUILTIN="../edit_node"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000"><span style="font-family: SansSerif, sans-serif; color: #000000">Puts a node in Edit Mode. </span></font>Opens an inline editor for fast entering and modifying text. Standard modus when making a new node (<i>Ins</i>) or when double-clicking on the node.
    </p>
    <p>
      
    </p>
    <p>
      <b>Context menu</b>. To open the context menu, right-click on the text which is being edited. This menu contains:
    </p>
    <ul>
      <li>
        <b>Cut</b>
      </li>
      <li>
        <b>Copy</b>
      </li>
      <li>
        <b>Paste</b>
      </li>
      <li>
        <b>Spelling</b>: turn spelling check on/of
      </li>
      <li>
        <b>Languages</b>: choose language for spelling checker
      </li>
      <li>
        <b>Format</b>: bold: italic etc.
      </li>
    </ul>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>&#160;Node&gt; Edit node in separate Dialog</i>
      </li>
      <li>
        ... for installing your own language
      </li>
    </ul>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        <font face="SansSerif, sans-serif" color="#000000"><span style="font-family: SansSerif, sans-serif; color: #000000">&#160;If a node is the last child node, left clicking the node will place it in Edit Mode. Otherwise, a left click will toggle the visibility (folding) of the child nodes</span></font>
      </li>
      <li>
        The text to be shown cannot start with &quot;=&quot;. If it starts with it, it is interpreted as a formula.
      </li>
      <li>
        To insert special symbols such as &#169; , copy and paste it from your favourite text editor.
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="1"/>
</node>
<node TEXT="Edit node in separate Dialog" ID="ID_1542197841" CREATED="1266244681296" MODIFIED="1311402583748" TEXT_SHORTENED="true">
<icon BUILTIN="../edit_long_node"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Open basic node text in a separate dialog for advanced editing&#177;
    </p>
    <ul>
      <li>
        Including Alignment and Bulleted/Numbered lists.&#160;
      </li>
      <li>
        Splitting nodes.
      </li>
      <li>
        Editing HTML-code.
      </li>
    </ul>
    <p>
      <b>Context menu</b>. To open the context menu, click-right in the text area of the dialog.
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>Node &gt; Edit node </i>for the context menu
      </li>
      <li>
        <i>Languages </i>in the context menu
      </li>
    </ul>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        The text to be shown cannot start with &quot;=&quot;. If it starts with it, it is interpreted as a formula.
      </li>
      <li>
        To insert special symbols such as &#169; , copy and paste it from your favourite text editor.
      </li>
    </ul>
    <p>
      
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="1"/>
</node>
<node TEXT="Join nodes" ID="ID_1720469761" CREATED="1266247462656" MODIFIED="1309687496955" TEXT_SHORTENED="true">
<icon BUILTIN="../JoinNodesAction"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000"><span style="font-family: SansSerif, sans-serif; color: #000000">Joins two nodes (the basic text only) into one. Only works on nodes that do not have children.</span></font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Split nodes" ID="ID_1796564424" CREATED="1266247473250" MODIFIED="1309687782261" TEXT_SHORTENED="true">
<icon BUILTIN="../split_node"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000"><span style="font-family: SansSerif, sans-serif; color: #000000">Splits and distributes the basic text of a node over two nodes. Split inserts a new node for the splitted text. The split will take place at line breaks. </span></font>
    </p>
    <p>
      
    </p>
    <p>
      <font face="SansSerif, sans-serif" color="#000000"><span style="font-family: SansSerif, sans-serif; color: #000000">The Split Node command may be used on any plain node which contains one or more line breaks, which can be inserted using Shift Enter. Split Node also works on any Rich Formatted (Long or HTML) node which contains multiple paragraphs. In either case the node gets split at the line and/or paragraph breaks. For HTML nodes you can apply the split repeatedly if there are nested elements. A plain node may be split at any point by first converting it using Format &gt; Use Rich Formatting. Then click on the node to open the Edit Long Node window. In that window, place the cursor where you want to split the node, then click the &quot;Split&quot; button at the bottom of the dialog. The part of the node to the right of the cursor will appear below the node from which it was split.</span></font><br/>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Use richt formatting (RTF)" ID="ID_601879583" CREATED="1266251602796" MODIFIED="1309688323379" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000"><span style="font-family: SansSerif, sans-serif; color: #000000">For the selected node(s), this causes subsequent editing to take place in the Rich Text dialog.</span></font>
    </p>
    <p>
      
    </p>
    <p>
      ?? Converts text in selected nodes to Rich text format (Rich Text Format, HTML).
    </p>
    <p>
      
    </p>
    <p>
      Inverse of <i>Use plain text.</i>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Use plain text" ID="ID_421682408" CREATED="1266251609187" MODIFIED="1309688143323" TEXT_SHORTENED="true">
<font NAME="SansSerif" SIZE="12" BOLD="false" ITALIC="false"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000"><span style="font-family: SansSerif, sans-serif; color: #000000">For the selected nodes, all subsequent editing takes place inline. This reverses the effect of &quot;Use Rich Formatting...&quot;</span></font>
    </p>
    <p>
      
    </p>
    <p>
      <font face="SansSerif, sans-serif" color="#000000"><span style="font-family: SansSerif, sans-serif; color: #000000">?? Converts the basic text of the selected nodes in plain text to be used in systems which cannot handle RTF.</span></font>
    </p>
    <p>
      
    </p>
    <p>
      <font face="SansSerif, sans-serif" color="#000000"><span style="font-family: SansSerif, sans-serif; color: #000000">Inverse of <i>Use richt text formating</i></span></font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Editing basic node text" FOLDED="true" ID="ID_1250935548" CREATED="1305444766488" MODIFIED="1311947283860" TEXT_SHORTENED="true">
<hook NAME="SummaryNode"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        To enter a special symbol like &#169;, copy and paste it from another text editor.
      </li>
      <li>
        There are dictionries for different languages which you can download from the

        <p>
          &#160;Freeplane web site.
        </p>
      </li>
      <li>
        It is possible to add icons, hyperlinks and an (internal) image.
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="7"/>
<node TEXT="Spell check dictionaries" ID="ID_141336344" CREATED="1271096394315" MODIFIED="1310243564880" LINK="http://sourceforge.net/projects/freeplane/files/spell_check_dictionaries" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Only necessary for non-English languages.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Unexpected colored border" ID="ID_1972048143" CREATED="1310123437022" MODIFIED="1311355045110" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      If the node gets an unexpected colored border, check if the text starts with &quot;= &quot;. If so, this indicates a formula.
    </p>
    <p>
      
    </p>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        <i>View &gt; Don't mark formula's with a border. </i>
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="Hidden formula" ID="ID_800369329" CREATED="1311355065541" MODIFIED="1311406117427" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="7"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      If text starts with&#160;&#160;&quot;=&quot; the remainder is treated as a formula and the calculated result is displayed, just like in a spread sheet.
    </p>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Node Down [Move node Sibling down]" ID="ID_694884995" CREATED="1306059969763" MODIFIED="1311402754818" TEXT_SHORTENED="true">
<hook NAME="FirstGroupNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif">Changes the position of the node by moving it down through the sibling hierarchy. If already at the bottom, the node is moved to the top. </font>
    </p>
    <p>
      
    </p>
    <p>
      <b><font color="#000000" face="SansSerif, sans-serif">Hot key</font></b>
    </p>
    <ul>
      <li>
        <i>Ctrl + Down</i>
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="Node Up [Move node Sibling Up]" ID="ID_33657896" CREATED="1306059989034" MODIFIED="1311402754849" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif">Changes the position of the node by moving it up through the sibling hierarchy. If already at the top, the node is moved to the bottom. </font>
    </p>
    <p>
      
    </p>
    <p>
      <b><font color="#000000" face="SansSerif, sans-serif">Hot key</font></b>
    </p>
    <ul>
      <li>
        <i>Ctrl + Up</i>
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="Node Left [Move node Parents sibling]" ID="ID_1330098607" CREATED="1306059994859" MODIFIED="1311402754865" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif">Changes the position of the node by making it a sibling of its parent. It is placed below the parent to which it was originally a child. </font>
    </p>
    <p>
      
    </p>
    <p>
      
    </p>
    <p>
      <b><font color="#000000" face="SansSerif, sans-serif">Hot key</font></b>
    </p>
    <ul>
      <li>
        <i>Ctrl + Left</i>
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="Node right [Move node Siblings child]" ID="ID_1588077274" CREATED="1306060092075" MODIFIED="1311402754896" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif">Changes the position of the node by making it a child of the node directly above it. If there are other child nodes it becomes the sibling of those children, and is placed last among them. </font>
    </p>
    <p>
      
    </p>
    <p>
      
    </p>
    <p>
      <b><font color="#000000" face="SansSerif, sans-serif">Hot key</font></b>
    </p>
    <ul>
      <li>
        <i>Ctrl + Right</i>
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="Moving nodes" STYLE_REF="Aggregatie als Toelichting" ID="ID_999302270" CREATED="1306060121800" MODIFIED="1311402754911" TEXT_SHORTENED="true">
<hook NAME="SummaryNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
</node>
<node TEXT="Add connector" FOLDED="true" ID="ID_1803275985" CREATED="1266249966500" MODIFIED="1310121057247" TEXT_SHORTENED="true">
<icon BUILTIN="../designer"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif">Creates a connector between two selected nodes. </font>
    </p>
    <p>
      
    </p>
    <p>
      The connector can be formatted by opening its context window (right-click on the connector). <font color="#000000" face="SansSerif, sans-serif">&#160;A label can be attached at the binning, middle end the end of the connector. </font>
    </p>
    <p>
      
    </p>
    <p>
      <b><font color="#000000" face="SansSerif, sans-serif">See also</font></b>
    </p>
    <ul>
      <li>
        <i>View &gt; Properties Panel</i>&#160;to edit properties
      </li>
    </ul>
    <p>
      
    </p>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        If more than two nodes are selected, several connectors are drawn at once, with the last selected node as the (common) destination.
      </li>
      <li>
        By making a self-referenced nod it is possible to have a connector appear as a line connected to one node only.
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<node TEXT="Connector" FOLDED="true" ID="ID_725880686" CREATED="1303722690480" MODIFIED="1310196224134" HGAP="50" TEXT_SHORTENED="true">
<hook NAME="SummaryNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Refine" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="Example" LAST="false"/>
</hook>
<arrowlink SHAPE="CUBIC_CURVE" COLOR="#000099" WIDTH="2" TRANSPARENCY="80" FONT_SIZE="12" FONT_FAMILY="SansSerif" DESTINATION="ID_725880686" MIDDLE_LABEL="line + label for self-referenced node" STARTINCLINATION="13;65;" ENDINCLINATION="13;65;" STARTARROW="NONE" ENDARROW="DEFAULT"/>
<node TEXT="node a" FOLDED="true" ID="ID_504660350" CREATED="1288535672221" MODIFIED="1310196224134" TEXT_SHORTENED="true">
<arrowlink SHAPE="CUBIC_CURVE" COLOR="#000000" WIDTH="2" TRANSPARENCY="80" FONT_SIZE="12" FONT_FAMILY="SansSerif" DESTINATION="ID_580762900" SOURCE_LABEL="begin&#xa;label" TARGET_LABEL="end&#xa;label" MIDDLE_LABEL="middle&#xa;label" STARTINCLINATION="10;26;" ENDINCLINATION="-19;52;" STARTARROW="DEFAULT" ENDARROW="DEFAULT"/>
<node TEXT="node b" FOLDED="true" ID="ID_519354024" CREATED="1288535688688" MODIFIED="1310196224119" TEXT_SHORTENED="true">
<edge STYLE="bezier"/>
<node TEXT="node c" ID="ID_580762900" CREATED="1288535699522" MODIFIED="1310196224119" TEXT_SHORTENED="true"/>
</node>
</node>
</node>
</node>
<node TEXT="Reset position" ID="ID_1003048503" CREATED="1306060582988" MODIFIED="1309980662072" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000"><span style="font-family: SansSerif, sans-serif; color: #000000">Returns the node to the position it had when it was first created. If you have moved nodes around and want to reformat your map, this command will do so. <i>Esc</i>&#160;followed by<i>&#160;Ctrl + A</i>&#160;will select all the nodes in a map.</span></font>
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Remove node" ID="ID_1954594298" CREATED="1266247035265" MODIFIED="1309691150548" TEXT_SHORTENED="true">
<icon BUILTIN="../editdelete"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000"><span style="font-family: SansSerif, sans-serif; color: #000000">Deletes the node and all of its children. </span></font>
    </p>
    <p>
      
    </p>
    <p>
      <font face="SansSerif, sans-serif" color="#000000"><span style="font-family: SansSerif, sans-serif; color: #000000"><b>See also</b> </span></font>
    </p>
    <ul>
      <li>
        <font face="SansSerif, sans-serif" color="#000000"><span style="font-family: SansSerif, sans-serif; color: #000000">Undo</span></font>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="FirstGroupNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Node features" STYLE_REF="MainMenuAccent" FOLDED="true" POSITION="right" ID="ID_1929697075" CREATED="1303928282470" MODIFIED="1311402974622" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To provide additional features that can be added to a basic node. &#160;Some features are related to the inner area, within the bubble, and others to the outer area or both.
    </p>
    <ul>
      <li>
        <b>Inner area:</b>&#160;Fixed image, Icon, Hyperlink, Formula (LaTeX), node number, date
      </li>
      <li>
        <b>Outer area:</b>: Details, Attributes, Notes, Scalable image.
      </li>
      <li>
        <b>Both/other</b>: Clouds, Progression indicator, Time manager, Agenda, Groovy script, Encryption
      </li>
    </ul>
    <p>
      These elements can be added through menu <i>Node features</i>. They can be edited and formatted through a <b>context menu</b>&#160;which appears on right-clicking the feature. In menu <i>View</i>&#160;it can be set if particular elements should generally be hidden and only shown if e.g. a node is selected.
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>View &gt; Note panel</i>
      </li>
      <li>
        <i>View &gt; View attributes</i>
      </li>
      <li>
        <i>Node &gt; Shortened node content</i>
      </li>
      <li>
        <i>View &gt; Properties Panel</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<attribute_layout NAME_WIDTH="109" VALUE_WIDTH="96"/>
<attribute NAME="Chapter" VALUE="3,5,7"/>
<node TEXT="Details" FOLDED="true" ID="ID_1543644666" CREATED="1303930434451" MODIFIED="1311947536864" TEXT_SHORTENED="true">
<font NAME="SansSerif" SIZE="12" BOLD="false" ITALIC="false"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>Details</b>&#160;is an optional text field directly under the basic node text, outside the node area (bubble area). It is the text you are currently reading.&#160;&#160;Details is generally used to extend and describe in detail the basic node text.
    </p>
    <p>
      
    </p>
    <p>
      Details can be hidden in two ways:
    </p>
    <ul>
      <li>
        press the triangle to roll up the text; press again to roll down again
      </li>
      <li>
        select <i>View &gt; Shortened node</i>&#160;content
      </li>
    </ul>
    <p>
      Hidden details are shown when the cursor is above the node.
    </p>
    <p>
      
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>View &gt; Toggle details</i>
      </li>
      <li>
        <i>View &gt; Notes in map</i>
      </li>
      <li>
        <i>View &gt; Shortened node content</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="FirstGroupNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="SubMenu" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3"/>
<node TEXT="Edit node details" ID="ID_1807308711" CREATED="1291153533418" MODIFIED="1311402835142" TEXT_SHORTENED="true">
<icon BUILTIN="../edit_details"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Opens in line editor for editing Details. This editor is similar to the inline editor for basic node text. The text can be hidden, unless selected.
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>Node &gt; Edit node</i>
      </li>
      <li>
        <i>View &gt; Toggle Details </i>
      </li>
      <li>
        <i>Node &gt; Shortened node content</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3"/>
</node>
<node TEXT="Edit node details in separate Dialog" ID="ID_742575187" CREATED="1309980895003" MODIFIED="1311402835158" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Opens editor for node details in a separate dialog. Similar to the dialog for basic node text.
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        Node &gt; Edit node in separate dialog
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="Remove node details" ID="ID_949038869" CREATED="1291201354423" MODIFIED="1311402835158" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Removes the details text from the selected node.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3"/>
</node>
</node>
<node TEXT="Attributes" FOLDED="true" ID="ID_147715298" CREATED="1303930553284" MODIFIED="1311404319391" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      An <b>attribute</b>&#160;is an entity with a name and a value, meaning name = value. Attributes are shown in a table with two columns, which appears in the external area of the node,&#160;&#160;under Details.
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>View &gt;Properties Panel &gt;Calendar and Attributes&#160;&#160;(</i>changes one node at a time)
      </li>
      <li>
        <i>View &gt;Shortened node content</i>&#160;to show/hide attributes
      </li>
      <li>
        <i>View &gt; Attribute &gt; Show all attributes/Show selected attributes/Hide attributes</i>
      </li>
      <li>
        <i>Node features &gt; Attribute manager</i>&#160;to check which nodes are selected.
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="SubMenu" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3" OBJECT="org.freeplane.features.format.FormattedNumber|3.0"/>
<node TEXT="Add attribute" FOLDED="true" ID="ID_984784283" CREATED="1266244687437" MODIFIED="1311404438154" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Opens a table structure below the basic node text with two columns in which a name and value can be entered or selected.
    </p>
    <p>
      
    </p>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        Right-click on the columns to open a context menu.
      </li>
      <li>
        The width of the columns can be changed by dragging the bar above the columns.
      </li>
    </ul>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>Edit &gt; Copy attributes </i>
      </li>
      <li>
        <i>Edit &gt; Paste attributes</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3" OBJECT="org.freeplane.features.format.FormattedNumber|3.0"/>
<node TEXT="" ID="ID_110645225" CREATED="1310841527098" MODIFIED="1311404452959" TEXT_SHORTENED="true">
<attribute_layout NAME_WIDTH="90" VALUE_WIDTH="163"/>
<attribute NAME="Chapter" VALUE="3" OBJECT="org.freeplane.features.format.FormattedNumber|3.0"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      It is possible to add the same attribute and value at once to all selected nodes.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Copy and paste attributes" ID="ID_1118988780" CREATED="1311226498788" MODIFIED="1311404930655" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="0" OBJECT="org.freeplane.features.format.FormattedNumber|0.0"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>See&#160;also</b>
    </p>
    <ul>
      <li>
        <i>Edit &gt; Copy attributes </i>
      </li>
      <li>
        <i>Edit &gt; Paste attributes</i>
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="Show/hide attribute-icon" ID="ID_395016262" CREATED="1311926417042" MODIFIED="1311926469162" LINK="#ID_1228898153">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Add style attributes" ID="ID_1359819434" CREATED="1310841915000" MODIFIED="1311405219490" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3" OBJECT="org.freeplane.features.format.FormattedNumber|3.0"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Copies (imports) attributes from the style of a selected node to the node.
    </p>
    <p>
      
    </p>
    <p>
      Note
    </p>
    <ul>
      <li>
        Has effect only if the node is connected to a style.
      </li>
      <li>
        Has effect only if the style has one or more attributes.
      </li>
      <li>
        When a node is connected to a style, attributes are not automatically connected.
      </li>
      <li>
        Example: Has been used for the current node. The node was first given Style Method and next attribute Class=Method was imported.
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="Edit attributes" ID="ID_639830064" CREATED="1266247880359" MODIFIED="1311404523955" TEXT_SHORTENED="true">
<icon BUILTIN="../EditAttributesAction"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Opens dialog to edit names and values of attributes of selected nodes.
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>Edit &gt; Copy attributes </i>
      </li>
      <li>
        <i>Edit &gt; Paste attributes</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3" OBJECT="org.freeplane.features.format.FormattedNumber|3.0"/>
</node>
<node TEXT="Extended attribute editor..." ID="ID_260530468" CREATED="1310842038396" MODIFIED="1311404539492" TEXT_SHORTENED="true">
<icon BUILTIN="../showAttributes"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3" OBJECT="org.freeplane.features.format.FormattedNumber|3.0"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Opens a dialog to manage all attributes of all nodes.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Remove first attribute" ID="ID_869406084" CREATED="1303930754909" MODIFIED="1310842744790" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<hook NAME="FirstGroupNode"/>
</node>
<node TEXT="Remove last attribute" ID="ID_412020595" CREATED="1303930767148" MODIFIED="1310842749408" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Remove all attributes" ID="ID_8268990" CREATED="1303930781149" MODIFIED="1310842754478" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Remove attributes" ID="ID_874836622" CREATED="1310842581512" MODIFIED="1311921132662">
<attribute_layout NAME_WIDTH="90" VALUE_WIDTH="163"/>
<attribute NAME="Chapter" VALUE="3" OBJECT="org.freeplane.features.format.FormattedNumber|3.0"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<hook NAME="SummaryNode"/>
</node>
<node TEXT="Attribute manager [Show/Hide attribute(value)]" FOLDED="true" ID="ID_900795535" CREATED="1311921084341" MODIFIED="1311922888319" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To hide an attribute, check its name in the list.
    </p>
    <p>
      To hide only a restricted set of values of an attribute, check and define the set.
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        Node &gt; Shortened node content
      </li>
    </ul>
  </body>
</html></richcontent>
<node TEXT="" ID="ID_796383862" CREATED="1311921423470" MODIFIED="1311921537007" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        Attributes are visible in the Tool Tip, independent of their setting in the Attribute manager.
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="Hide attribute a1, show attribute a2" ID="ID_475109570" CREATED="1288646162992" MODIFIED="1312615263072">
<font ITALIC="false"/>
<richcontent TYPE="NOTE">
<html>
  <head>
    
  </head>
  <body>
    <p>
      text in field notes
    </p>
  </body>
</html></richcontent>
<attribute NAME="a1" VALUE="value 1"/>
<attribute NAME="a2" VALUE="value 2"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Example" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      This node contains 2 attributes, a1 and a2 (may be more)
    </p>
    <p>
      Open <i>Attribute manager</i>&#160;to see that attribute a2 is checked visible and a1 is not, hence is hidden.
    </p>
    <p>
      
    </p>
    <p>
      <b>Note </b>
    </p>
    <ul>
      <li>
        &#160;a2 keeps visible in the <i>Tool Tip</i>&#160;(if you hover with the cursor above the node).
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node ID="ID_510422017" CREATED="1288646162992" MODIFIED="1312615295145" TEXT_SHORTENED="true">
<richcontent TYPE="NODE">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Show/Hide attribute&#160;&#160;&quot;<i>Shortened node content&quot;&#160; </i>set
    </p>
  </body>
</html></richcontent>
<font ITALIC="false"/>
<richcontent TYPE="NOTE">
<html>
  <head>
    
  </head>
  <body>
    <p>
      text in field notes
    </p>
  </body>
</html></richcontent>
<attribute NAME="a1" VALUE="value 1"/>
<attribute NAME="a2" VALUE="value 2"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Example" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      This node contains 2 attributes, a1 and a2 AND<i>&#160;Node &gt; Shortened node content </i>is ON.
    </p>
    <p>
      
    </p>
    <p>
      Result: text of Details is hidden, and checked attributes stay visible.
    </p>
    <p>
      
    </p>
    <p>
      <b>Note </b>
    </p>
    <ul>
      <li>
        Open <i>Attribute manager </i>to see that attribute a2 is checked visible and a1 is not, hence is hidden.
      </li>
      <li>
        a2 keeps visible in the Tool Tip (if you hover with the cursor above the node).
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
</node>
</node>
<node TEXT="Notes" FOLDED="true" ID="ID_1365598976" CREATED="1303930145801" MODIFIED="1311402835220" TEXT_SHORTENED="true">
<font NAME="SansSerif" SIZE="12" BOLD="false" ITALIC="false"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      A <b>note</b>&#160;is a special text field preceded with a note <b>icon. </b>A note&#160;can appear in different ways:
    </p>
    <ol>
      <li>
        In the external area of the node, below Attributes. Here it can be used as an alternative to Details.
      </li>
      <li>
        In a separate, independent window. Here one can present complete, screen wide documents. See: View
      </li>
      <li>
        In the dialog of the Time managert (Agenda). Here it is used to name an activity or task.
      </li>
    </ol>
    <p>
      <b>See also </b>
    </p>
    <ul>
      <li>
        <i>View &gt; Show notes in map</i>
      </li>
      <li>
        <i>View &gt; Toggle Note panel</i>
      </li>
      <li>
        <i>Tools &gt; Preferences</i>&#160;to set that the note icon should not be shown
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="SubMenu" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3"/>
<node TEXT="Edit note" ID="ID_924676619" CREATED="1266255695750" MODIFIED="1311402835283" TEXT_SHORTENED="true">
<icon BUILTIN="../EditNoteInDialogAction"/>
<font NAME="SansSerif" SIZE="12" BOLD="false" ITALIC="false"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Opens a dialog for editing the note field of the selected note.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3"/>
</node>
<node TEXT="Remove note" ID="ID_142539324" CREATED="1266247870640" MODIFIED="1311402835283" TEXT_SHORTENED="true">
<icon BUILTIN="../RemoveNoteAction"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Opens a dialog to remove the note field of the selected nodes.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3"/>
</node>
<node TEXT="Edit in note panel" FOLDED="true" ID="ID_221910481" CREATED="1298574467254" MODIFIED="1311402835283" COLOR="#663300" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b><font color="#000000" face="SansSerif, sans-serif">See also</font></b>
    </p>
    <ul>
      <li>
        <font color="#000000" face="SansSerif, sans-serif">View &gt; Note Panel </font>
      </li>
    </ul>
  </body>
</html></richcontent>
<attribute_layout NAME_WIDTH="82" VALUE_WIDTH="165"/>
<attribute NAME="Chapter" VALUE="3"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<hook NAME="SummaryNode"/>
<node TEXT="Note" ID="ID_875633710" CREATED="1310843490041" MODIFIED="1311402835283" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      A <b>note</b>&#160;is a special text field, generally preceded with a note <b>icon.</b>
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Hide note-icons" ID="ID_830303915" CREATED="1311926324089" MODIFIED="1311926380000" LINK="#ID_635307234">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
</hook>
</node>
</node>
</node>
<node TEXT="basic node text + View &gt; shortened node content" ID="ID_993043149" CREATED="1288646162992" MODIFIED="1311924009477" TEXT_SHORTENED="true">
<font ITALIC="false"/>
<attribute NAME="a1" VALUE="waarde1"/>
<attribute NAME="a2" VALUE="waarde2"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Example" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      text in field Details
    </p>
  </body>
</html></richcontent>
<richcontent TYPE="NOTE">
<html>
  <head>
    
  </head>
  <body>
    <p>
      text in field notes
    </p>
  </body>
</html></richcontent>
<hook NAME="SummaryNode"/>
</node>
<node TEXT="Icons" FOLDED="true" ID="ID_68469558" CREATED="1266240681421" MODIFIED="1311402835330" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif">An icon is a mini image which can be attached to a node.&#160; </font>
    </p>
    <p>
      
    </p>
    <p>
      <b><font face="SansSerif, sans-serif" color="#000000">Note</font></b>
    </p>
    <ul>
      <li>
        <font face="SansSerif, sans-serif" color="#000000">In the current version of Freeplane icons can only be attached to the inner region of the note, withhin the bubble area.</font>
      </li>
      <li>
        <font face="SansSerif, sans-serif" color="#000000">&#160;It is possible to attach multiple icons.</font>
      </li>
      <li>
        Freeplane has system icons. It is also possible to ad user icons.
      </li>
    </ul>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>View &gt; toolbars &gt; secondary toolbar</i>&#160;for a permanent icon toolbar.
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="SubMenu" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3"/>
<hook NAME="FirstGroupNode"/>
<node TEXT="Select icon..." FOLDED="true" ID="ID_1962266219" CREATED="1309725076165" MODIFIED="1311402835361" TEXT_SHORTENED="true">
<icon BUILTIN="../designer"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Opens a table from which an icon can be chosen by clicking it.
    </p>
    <p>
      
    </p>
    <p>
      Note
    </p>
    <ul>
      <li>
        This table also contains system icons.
      </li>
    </ul>
  </body>
</html></richcontent>
<node TEXT="" ID="ID_1761101081" CREATED="1310844234592" MODIFIED="1310844323451" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Contrary to the other icons lists, this table also contains system icons.
    </p>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Remove first icon" ID="ID_1452556696" CREATED="1305728866037" MODIFIED="1311402835361" TEXT_SHORTENED="true">
<icon BUILTIN="../remove_first_icon"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Remove firstly added icon in the selected node.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Remove last icon" ID="ID_424983852" CREATED="1305728879991" MODIFIED="1311402835376" TEXT_SHORTENED="true">
<icon BUILTIN="../remove_last_icon"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Removes last icon in selected node
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Remove all icons" ID="ID_1438443252" CREATED="1305728891917" MODIFIED="1311402835376" TEXT_SHORTENED="true">
<icon BUILTIN="../icon_trash"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Remove all icons from selected node
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Select Icon submenus..." ID="ID_80370661" CREATED="1305728952563" MODIFIED="1311405988711" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE=""/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Select node through one of the submenus with icons
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="" STYLE_REF="Aggregatie als Toelichting" FOLDED="true" ID="ID_892520547" CREATED="1305729001930" MODIFIED="1311402835376" TEXT_SHORTENED="true">
<hook NAME="SummaryNode"/>
<attribute_layout NAME_WIDTH="82" VALUE_WIDTH="211"/>
<attribute NAME="Chapter" VALUE="3"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Refine" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="Example" LAST="false"/>
</hook>
<node TEXT="Icon" ID="ID_845839827" CREATED="1309802783787" MODIFIED="1311402835392" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      An icon is a mini image.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT=" Examples of icons" ID="ID_835126969" CREATED="1270892460676" MODIFIED="1311402835392" TEXT_SHORTENED="true">
<icon BUILTIN="help"/>
<icon BUILTIN="yes"/>
<icon BUILTIN="messagebox_warning"/>
<icon BUILTIN="button_ok"/>
<attribute NAME="Chapter" VALUE="3"/>
</node>
<node TEXT="Add user defined icon" ID="ID_105231801" CREATED="1305728960278" MODIFIED="1311402835392" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To add ones own icon to the list of selectable icons, put the file myicon.png to in sub directory <i>icons</i>&#160;of the user directory which opens by selecting <i>Tools &gt; Open user directory</i>.
    </p>
  </body>
</html></richcontent>
</node>
</node>
</node>
<node TEXT="Image" FOLDED="true" ID="ID_1620391394" CREATED="1303931708607" MODIFIED="1311402835423" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      An image in Freeplane is a picture with format<b>&#160;png, jpeg, gif, </b>or<b>&#160;svg</b>&#160;. An image can be attached to the internal area of a node (inside the bubble) or to the external area (outside the bubble).
    </p>
    <ul>
      <li>
        The image in the internal area is fixed in size. It is shown in its acual size. To make it smaller, use a paint program.
      </li>
      <li>
        The image in the external area is sizeable. To resize it, move the cursor over one of the corners until the resize handle appears and drag it to its new size.
      </li>
    </ul>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        Images are NOT included in the mind map .mm. As a result, when moving a mind map images are not automatically moved with the mind map and get disconnected.
      </li>
      <li>
        .<b>svg</b>&#160;images can NOT be published on internet, e.g. with Java Applet.
      </li>
    </ul>
    <p>
      <b>Best practice</b>
    </p>
    <ul>
      <li>
        Make a directory in which both images and the .mm file are kept. Add images to the mind map from this directory. When this directory is displaced as a whole to a different position, the images keep appearing in the mind map. When publishing the mind map, an additional action is necessary, see below.
      </li>
    </ul>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        &#160;<i>Pubishing mind maps</i>&#160;with images on Internet.
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="SubMenu" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3"/>
<node TEXT="Add external image...[Add scalable image]" FOLDED="true" ID="ID_334408230" CREATED="1266250647515" MODIFIED="1311883044559" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Add sizeable image to the selected node, below the basic node text, outside the bubble area.
    </p>
    <p>
      
    </p>
    <p>
      
    </p>
    <p>
      
    </p>
    <p>
      <b>Note </b>
    </p>
    <ul>
      <li>
        Alternatively,&#160;add image with <b>drag and drop </b>or add a hyperlink to the image with&#160;<b>copy and&#160;paste. </b>Note the difference:

        <ul>
          <li>
            <b>Copy &amp; Paste</b>

            <ul>
              <li>
                Pasting at the upper part of a node adds a hyperlink to a newly created sibling
              </li>
              <li>
                Pasting at the outer side of a node adds a child node with a hyperlink.
              </li>
            </ul>
          </li>
          <li>
            <b>Drag &amp; drop</b>

            <ul>
              <li>
                Dropping at the upper part of the node adds an external, scalable image in the selected node.
              </li>
              <li>
                Dropping at the outer side of the node adds an external, scalable image in a newly created child node.
              </li>
              <li>
                Dropping only works if the mind map has been saved.
              </li>
              <li>
                For security reasons dragging and dropping is not possible for internet images.
              </li>
            </ul>
          </li>
        </ul>
      </li>
    </ul>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>View &gt; Shortened node content.</i>&#160;It hides/unhides the image.
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3"/>
<hook NAME="FirstGroupNode"/>
<node TEXT="" ID="ID_1592683562" CREATED="1311880649184" MODIFIED="1311880910313" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        Drag and drop images only works if the min map has been saved before.
      </li>
      <li>
        Drag and drop from the Internet is, because of security reasons, not possible. Dropping hyperlinks is. .
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="Drag &amp; drop image, copy &amp; paste link" ID="ID_7896368" CREATED="1311883050086" MODIFIED="1311930535976" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="Procedure" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To&#160;add an image <b>drag and drop</b>&#160;(not from Internet):
    </p>
    <ul>
      <li>
        Dropping at the upper part of the node adds an external, scalable image in the selected node.
      </li>
      <li>
        Dropping at the outer side of the node adds an external, scalable image in a newly created child node.
      </li>
      <li>
        Dropping only works if the mind map has been saved.
      </li>
      <li>
        For security reasons dragging and dropping is not possible for internet images. Hence, first drag and drop on your desktop, hence drag and drop on the mind map.
      </li>
    </ul>
    <p>
      
    </p>
    <p>
      To add a hyperlink, <b>copy and paste </b>(also from Internet);
    </p>
    <ul>
      <li>
        Pasting at the upper part of a node adds a hyperlink to a newly created sibling
      </li>
      <li>
        Pasting at the outer side of a node adds a child node with a hyperlink.
      </li>
    </ul>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        Images are not part of the mind map. If the image source in your computer is removed, the image will no longer display.
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Change external image..." ID="ID_276232981" CREATED="1303931774914" MODIFIED="1311402835470" TEXT_SHORTENED="true">
<font NAME="SansSerif" SIZE="12" BOLD="false" ITALIC="false"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Shows the handle of the external image of the selected node by which the size of the image can be changed.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3"/>
</node>
<node TEXT="Remove external image" ID="ID_1348672374" CREATED="1303931789305" MODIFIED="1311402835470" TEXT_SHORTENED="true">
<font NAME="SansSerif" SIZE="12" BOLD="false" ITALIC="false"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Remove the external images from the selected node.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Image (File chooser or link) [Add fixed image]" ID="ID_1277752024" CREATED="1266249875046" MODIFIED="1311402835486" TEXT_SHORTENED="true">
<font NAME="SansSerif" SIZE="12" BOLD="false" ITALIC="false"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Attach a fixed&#160;&#160;image to the inner area of the selected node, within the bubble area and replacing the basic node text. If the node already contains a hyperlink to an image, the image will replace the hyperlink. Otherwise the file chooser can be used to select an image file.
    </p>
    <p>
      
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>View &gt; Shortened node content</i>&#160;to hide/unhide the image
      </li>
    </ul>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        The image is shown in its original size. To decrease this size, use a paint program.
      </li>
      <li>
        The basic node text is not showing.
      </li>
      <li>
        The extended node editor allows to add an image as HTML code. Example: <i>&lt;html&gt;&lt;img src=&quot;linked/Apple.png&quot;&gt; &#160;&#160;&lt;html&gt;&lt;img src=&quot;file://C:/Users/My Documents/Mind Maps/Linked/Apple.png&quot;&gt;</i>&#160;The relative link points to an image file in a subdirectory of the directory in which the min map is kept..
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3"/>
</node>
<node TEXT="Publish images" ID="ID_1111281504" CREATED="1309808574878" MODIFIED="1311405741529" TEXT_SHORTENED="true">
<hook NAME="SummaryNode"/>
<attribute_layout NAME_WIDTH="82" VALUE_WIDTH="264"/>
<attribute NAME="Chapter" VALUE="3,6"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="Procedure" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000">To publish a mind map with images: </font>
    </p>
    <ul>
      <li>
        Make sure the images are in the same directory as the mind map
      </li>
      <li>
        Fold the mind map as you want it to be when opening
      </li>
      <li>
        Select <i>File &gt; Export</i>&#160;and choose <i>Java Applet</i>. One file and one directory will be created: <i>myFile.html </i>and <i>myFile.html_files</i>&#160; respectively.
      </li>
      <li>
        Copy the images to <i>myFyle.html_files</i>
      </li>
    </ul>
    <p>
      Now you can move the file <i>myFile.html</i>&#160;and the subdirectory <i>myFile.html_files</i>&#160; to the location (directory) where you want to it be it accessible. Call <i>myFile.html</i>&#160; to open the mind map. If publishing on internet, you may rename myFile.html in e.g. index.html. The subdirectory should not be renamed.
    </p>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Progress [% Disc]" FOLDED="true" ID="ID_1053622072" CREATED="1303931822760" MODIFIED="1311402835501" TEXT_SHORTENED="true">
<icon BUILTIN="25%"/>
<font NAME="SansSerif" SIZE="12" BOLD="false" ITALIC="false"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Progress is an indicator in which a percentage can be visualized in steps of 10% or 25%.
    </p>
    <p>
      
    </p>
    <p>
      Note
    </p>
    <ul>
      <li>
        The standard is to present the indicator as an icon. This icon cannot be hidden.
      </li>
      <li>
        Additionally an image of the indicator can be shown. This image can be hidden, see images.
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="SubMenu" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3"/>
<node TEXT="Progress Up" ID="ID_1352152467" CREATED="1304432750435" MODIFIED="1310844546671" TEXT_SHORTENED="true">
<icon BUILTIN="50%"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Adds progress indicator in selected node resp. moves it up in steps of 25% until 100% and OK.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Progress Down" ID="ID_1503763375" CREATED="1304432902357" MODIFIED="1310844546687" TEXT_SHORTENED="true">
<icon BUILTIN="25%"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Decreases progress indicator in selected node in steps of 25% and as a last step removes it.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Extended progress 10%" ID="ID_931502203" CREATED="1304432912176" MODIFIED="1310844546687">
<icon BUILTIN="25%"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Adds a progress indication with extended size that is:
    </p>
    <ul>
      <li>
        increased 10 %<i>&#160;</i>on<i>&#160;double Click</i>
      </li>
      <li>
        decreased 10% o<i>n Ctrl + double Click</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="FirstGroupNode"/>
<hook URI="freeplaneresource:/images/svg/Progress_tenth_02.svg" SIZE="1.0" NAME="ExternalObject"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Extended progress 25%" ID="ID_1330662491" CREATED="1310803008021" MODIFIED="1310844546687" TEXT_SHORTENED="true">
<icon BUILTIN="25%"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Adds a progress indicator with extended size that is:
    </p>
    <ul>
      <li>
        increased 25 %<i>&#160;on double Click</i>
      </li>
      <li>
        decreased 25% o<i>n Ctrl + double Click</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook URI="freeplaneresource:/images/svg/Progress_quarter_01.svg" SIZE="1.0" NAME="ExternalObject"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="" ID="ID_1791031466" CREATED="1310017200205" MODIFIED="1310844646886" TEXT_SHORTENED="true">
<attribute_layout NAME_WIDTH="90" VALUE_WIDTH="192"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      The big indicator is an .svg image. This will not appear in a Java Applet.
    </p>
  </body>
</html></richcontent>
<hook NAME="SummaryNode"/>
</node>
<node TEXT="Delete progress" ID="ID_1336453593" CREATED="1304432977103" MODIFIED="1310844546687" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Images" ID="ID_604986373" CREATED="1310061708286" MODIFIED="1310196223853" TEXT_SHORTENED="true">
<hook NAME="SummaryNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
</node>
<node TEXT="Links" FOLDED="true" ID="ID_1430712985" CREATED="1303930954062" MODIFIED="1311402835517" TEXT_SHORTENED="true">
<font NAME="SansSerif" SIZE="12" BOLD="false" ITALIC="false"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      A link can be:
    </p>
    <ul>
      <li>
        A click-able connection between two nodes.
      </li>
      <li>
        A click-able connection between a node and an external object (website, directory, program,..)
      </li>
      <li>
        A click-able connection between a node and a menu item
      </li>
      <li>
        A click-able email address
      </li>
    </ul>
    <p>
      The easiest way to enter hyperlinks to external objects is by copy and paste resp. drag and drop. In that case the hyperlink will we pasted to a newly created child of the selected node. It is possible to drop more than one address at once.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="SubMenu" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3"/>
<hook NAME="FirstGroupNode"/>
<node TEXT="Hyperlink (File Chooser)... [Add hyperlink...]" ID="ID_178833757" CREATED="1266249910406" MODIFIED="1311402835548" TEXT_SHORTENED="true">
<icon BUILTIN="../SetLinkByFileChooserAction"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Opens a dialog to navigate to a directory or file. A hyperlink to the selected directory or file is put into the selected node. The hyperlink appears as a red arrow and can be clicked to open the file or directory.
    </p>
    <p>
      
    </p>
    <p>
      To remove the hyperlink:
    </p>
    <ul>
      <li>
        select <i>Hyperlink (Text Field)... </i>or press<i>&#160;Ctrl + K; </i>and
      </li>
      <li>
        delete the text in the text field ( the address)
      </li>
    </ul>
    <p>
      &#160;
    </p>
  </body>
</html></richcontent>
<hook NAME="FirstGroupNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3"/>
</node>
<node TEXT="Hyperlink (Text Field)... [Type hyperlink]" ID="ID_873690575" CREATED="1266249928203" MODIFIED="1311402835579" TEXT_SHORTENED="true">
<icon BUILTIN="../SetLinkByTextFieldAction"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Opens a dialog to enter a hyperlink as text, or enter an e-mail address.
    </p>
    <p>
      
    </p>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        It is easier top use copy &amp; paste.
      </li>
      <li>
        Use it for adding hyperlink to the root node.
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3"/>
</node>
<node TEXT="Hyperlink from text [Activate hyperlink in text]" ID="ID_1049705529" CREATED="1266247483750" MODIFIED="1311402835595" TEXT_SHORTENED="true">
<icon BUILTIN="../ExtractLinkFromTextAction"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Converts a text address (URL as http://www.google.com/) in the basic text of the selected node to a click-able hyperlink.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3"/>
</node>
<node TEXT="Add Local hyperlink" ID="ID_271903009" CREATED="1266251024562" MODIFIED="1311402835610" TEXT_SHORTENED="true">
<icon BUILTIN="../LinkLocal"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">Creates a link between two nodes in the current map. The two nodes must be selected before this command is invoked: The first selected node is the target of the link, which is added to the second node, and represented there as a small green arrow. </span></font>
    </p>
    <p>
      
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3"/>
</node>
<node TEXT="Hyperlink (Menu entry)... [Add Hyperlink to menu item]" ID="ID_1477740287" CREATED="1291192827927" MODIFIED="1311402835610" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Opens a dialog to choose a menu option to which a hyperlink will be added in the selected node.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3"/>
</node>
<node TEXT="" STYLE_REF="Aggregatie als Toelichting" FOLDED="true" ID="ID_557988874" CREATED="1305558188109" MODIFIED="1311402835610" TEXT_SHORTENED="true">
<hook NAME="SummaryNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Refine" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="Example" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3"/>
<node TEXT="Hyperlink" ID="Freeplane_Link_203858515" CREATED="1270892460675" MODIFIED="1311402835642" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      A hyperlink is an electronic connection from a source node to a destination object. The destination can be a directory, file, web page, application, e-mail etc. The connection is click-able and appears as a red arrow (external hyperlink) or green arrow (internal hyperlink).
    </p>
    <p>
      
    </p>
    <p>
      A hyperlink can be contained in
    </p>
    <ul>
      <li>
        the basic node text and
      </li>
      <li>
        attribute values.
      </li>
    </ul>
    <p>
      A hyperlink can be added by drag and drop or menu selection.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3"/>
</node>
<node TEXT="Hyperlink(s) by drag and drop" ID="ID_640110372" CREATED="1286651969385" MODIFIED="1311402835673" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      The easiest way to add a hyperlink or group of hyperlinks to external objects is by drag and drop.
    </p>
    <p>
      Dropping a copy of a directory, file, file address or webaddress will generate a child node of the selected node which contains a hyperlink to the address of it.
    </p>
    <p>
      
    </p>
    <p>
      Dropping a group of hyperlinks will genareate a group of child nodes, each containing one of the hyperlinks.
    </p>
    <p>
      
    </p>
    <p>
      The text of the respective nodes will initially be the address of the hyperlink. This text may be removed, edited or replaced. This will not change the hyperlink itself (red arrow). To edit the hyperlink itself, select <i>Node features &gt; Links &gt; Hyperlink (Text Field)...</i>&#160;and edit the address.
    </p>
  </body>
</html></richcontent>
<attribute_layout NAME_WIDTH="82" VALUE_WIDTH="161"/>
<attribute NAME="Chapter" VALUE="3"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Procedure" LAST="false"/>
</hook>
</node>
<node TEXT="Email link" ID="ID_869144882" CREATED="1270892460675" MODIFIED="1311402835688" LINK="mailto:%20jokro@freeplane.nl" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      The simplest way to make an e-mail link is to copy and paste its address into a selected node. This will generate a<b>&#160;child node</b>&#160;of the selected node with the link. The text of the child node will consist of the e-mail address. You may replace this by e.g. only the name.
    </p>
    <p>
      
    </p>
    <p>
      If you want to add the e-mail link to a particular node (and not to its newly generated child), select <i>Node features &gt; Links &gt; Hyperlink (Text Field)...</i>&#160;and enter:
    </p>
    <ul>
      <li>
        <i>mailto:emailaddress</i>, e.g. mailto:jokro@freeplane.nl
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Procedure" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3"/>
</node>
<node TEXT="Remove hyperlink or e-mail" ID="ID_1464441556" CREATED="1310047903960" MODIFIED="1311402835688" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Procedure" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To remove a hyperlink or e-mail address:
    </p>
    <ul>
      <li>
        select <i>Hyperlink (Text Field)... </i>or press<i>&#160;Ctrl + K; </i>and
      </li>
      <li>
        delete the text in the text field ( the address)
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="Hyperlinks relative" ID="ID_1942965129" CREATED="1311925599759" MODIFIED="1311925653580" LINK="#ID_1309284534">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
</hook>
</node>
<node TEXT="Hyperlink in attribute" ID="ID_457929879" CREATED="1312540477537" MODIFIED="1312544796065">
<attribute_layout NAME_WIDTH="48" VALUE_WIDTH="48"/>
<attribute NAME="See also" VALUE="#ID_278329781" OBJECT="java.net.URI|#ID_278329781"/>
<attribute NAME="See also" VALUE="#ID_1279811672" OBJECT="java.net.URI|#ID_1279811672"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Example" LAST="false"/>
</hook>
</node>
</node>
</node>
<node TEXT="Time management" FOLDED="true" ID="ID_1959496513" CREATED="1303932101749" MODIFIED="1311405889292" TEXT_SHORTENED="true">
<font NAME="SansSerif" SIZE="12" BOLD="false" ITALIC="false"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      The Time manager allows you to set dates, times (reminders) and tasks (reminder texts) in selected nodes. The date appears as basic node text. A clock icon signals that a reminder is set.&#160;&#160;This icon appears in the note and its predecessors, except nodes which are within a Sum node group. A flasing clock icon signals that a reminder time has been reached.
    </p>
    <p>
      
    </p>
    <p>
      There are two locations where these functions can be set of edited.
    </p>
    <ul>
      <li>
        <i>Node features &gt; Time management </i>
      </li>
      <li>
        <i>View &gt; Properties panel &gt; Calendar and Attribute</i>s
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="SubMenu" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="5"/>
<node TEXT="Show Calendar..." FOLDED="true" ID="ID_1104707017" CREATED="1267746559867" MODIFIED="1311405889324" TEXT_SHORTENED="true">
<icon BUILTIN="../TimeManagementAction"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Opens a dialog to set ar change a date (text) or a reminder which has been attached to the selected node. The node and its predecessors show a clock icon. This icon flases at the reminder time. Also a red exclamation mark flashes in the root node.
    </p>
    <p>
      
    </p>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        <i>View &gt; Properties Panel &gt; Calendar &amp; Attributes</i>&#160;for an alternative dialog
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="FirstGroupNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="5"/>
<node TEXT="Set Year, Month and Day" ID="ID_629838545" CREATED="1310058100675" MODIFIED="1310196223822" TEXT_SHORTENED="true"/>
<node TEXT="Insert Date in Selection" ID="ID_77223280" CREATED="1288907062597" MODIFIED="1310058248120" TEXT_SHORTENED="true"/>
<node TEXT="Remind Me At This Date" ID="ID_1905175036" CREATED="1310058020709" MODIFIED="1310196223807" TEXT_SHORTENED="true"/>
<node TEXT="Remove Reminder (time)" ID="ID_1600844387" CREATED="1310058040514" MODIFIED="1310196223807" TEXT_SHORTENED="true"/>
</node>
<node TEXT="Show Time Scheduler List... [Task manager]" ID="ID_1106133151" CREATED="1266247905093" MODIFIED="1311405889339" BACKGROUND_COLOR="#ffffff" TEXT_SHORTENED="true">
<icon BUILTIN="../TimeListAction"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif">Opens a window which displays the reminders of all nodes. For each reminder are displayed: </font>
    </p>
    <ul>
      <li>
        date
      </li>
      <li>
        basic node text
      </li>
      <li>
        icons
      </li>
      <li>
        time created
      </li>
      <li>
        time last modified
      </li>
      <li>
        notes
      </li>
    </ul>
    <p>
      
    </p>
    <p>
      <font color="#000000" face="SansSerif, sans-serif">The entries can be searched, and changed with search and replace. </font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="5"/>
<arrowlink SHAPE="CUBIC_CURVE" COLOR="#000000" WIDTH="2" TRANSPARENCY="80" FONT_SIZE="12" FONT_FAMILY="SansSerif" DESTINATION="ID_1106133151" STARTINCLINATION="0;0;" ENDINCLINATION="0;0;" STARTARROW="NONE" ENDARROW="DEFAULT"/>
</node>
<node TEXT="Remove reminder" ID="ID_148977369" CREATED="1303932208426" MODIFIED="1311405889339" TEXT_SHORTENED="true">
<icon BUILTIN="../ReminderHookAction"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="5"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Remove the reminder of the selected node. For alternatives:
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>Show Calendar..</i>.
      </li>
      <li>
        <i>View Properties Panel &gt; Calendar &amp; Attribute</i>s
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="" ID="ID_517678813" CREATED="1288542096079" MODIFIED="1310196223791" TEXT_SHORTENED="true">
<hook NAME="plugins/TimeManagementReminder.xml">
    <Parameters REMINDUSERAT="2077460580480"/>
</hook>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Example" LAST="false"/>
</hook>
<hook NAME="SummaryNode"/>
</node>
</node>
<node TEXT="Encryption [Protection]" FOLDED="true" ID="ID_83654353" CREATED="1303931982176" MODIFIED="1312613663749" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="SubMenu" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>Encryption</b>&#160;is a method to protect (only) the descendants of a node from viewing and editing. unless the right encryption key has been entered.
    </p>
    <p>
      
    </p>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        The information in the source file is unreadable too.
      </li>
      <li>
        Example use: safety lock for private information.<br/>
      </li>
    </ul>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>Maps &gt; Map browser</i>&#160;to protect a mind map from editing, but keep it readable
      </li>
    </ul>
  </body>
</html></richcontent>
<node TEXT="Toggle encrypted / decrypted [Toggle protection]" FOLDED="true" ID="ID_1020968664" CREATED="1266247815921" MODIFIED="1312695265642" TEXT_SHORTENED="true">
<icon BUILTIN="../lock"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000">Opens a dialog&#160;&#160;to add and use&#160;&#160;a password to protect the descendants of the selected node form being displayed and edited. </font>
    </p>
    <p>
      
    </p>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><b>Note</b>&#160;</font>
    </p>
    <ul>
      <li>
        <font color="#000000" face="SansSerif, sans-serif">To protect a map, select an protect the root node.</font>
      </li>
      <li>
        <font color="#000000" face="SansSerif, sans-serif">The selected node itself is not protected, its information keeps being displayed and editable.</font>
      </li>
      <li>
        Multiple layer protection is possible: protect a child of a protected parent with a different password.
      </li>
      <li>
        Closing the map will automatically protect the node (toggle <i>Encrypted</i>).
      </li>
    </ul>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>Node feutures &gt; Link &gt; Hyperlink (Menu Entry)</i>&#160;to make the lock clickable.
      </li>
    </ul>
  </body>
</html>
</richcontent>
<node TEXT="" ENCRYPTED_CONTENT="/xWFGOIZ0dg= gT0iwj5Mr9ZIUJGRpaqO8Htqqrsf2WP03ZNL+++X2kTszPq0YnO2ZeUGmygvV6OwMQ0CXGau4qbnLlt5ODSMosrdy5dZ6ZqDB4hELxsH/uvBkCIhukwfQA7ExofI6u2MbKWGXK/WWJ5cB+65yJVcWqjeLTsDYwNc4stftw3OcVG4ui/40qfkmCCuSemvAOV2sNjrxV5qyDnwzJWVCS0VtwHzUeDKlKRPNz08FmIdhXWpX3YtWYhIn7CLC9rzh9OI" ID="ID_866287848" CREATED="1312574380161" MODIFIED="1312610621270" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>Example</b>
    </p>
    <p>
      This node is protected: my children are visible only after entering a password.
    </p>
    <p>
      
    </p>
    <p>
      <b>Do</b>
    </p>
    <ul>
      <li>
        Select <i>Toggle Encrypted </i>and enter password <i>freeplane </i>
      </li>
      <li>
        See that I have two child nodes.
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Example" LAST="false"/>
</hook>
</node>
<node TEXT="Encryption" ID="ID_1331695196" CREATED="1312694750646" MODIFIED="1312695043802" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Toggle encryption does not function when published (Java Applet)
    </p>
  </body>
</html>
</richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Remove encryption [Remove protection]" ID="ID_136268743" CREATED="1303932060947" MODIFIED="1312610954680" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Opens a dialog to enter a password and remove the protection from the selected node. Next time it is not necessary to enter a password to display the descendants of the node.
    </p>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="LaTeX formula" FOLDED="true" ID="ID_1750517188" CREATED="1303931890027" MODIFIED="1311406091422" TEXT_SHORTENED="true">
<font NAME="SansSerif" SIZE="12" BOLD="false" ITALIC="false"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      LaTeX is a method to define the layout of scientific formulas..
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="SubMenu" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3,7"/>
<node TEXT="Add LaTeX formula..." ID="ID_816429133" CREATED="1266250651984" MODIFIED="1311406091422" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Opent het bewerkingsvenster&#160;&#160;voor de LaTeX opmaak.
    </p>
    <p>
      
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3,7"/>
</node>
<node TEXT="Edit LaTeX formula..." ID="ID_1933244848" CREATED="1305483582106" MODIFIED="1311406091437" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3,7"/>
</node>
<node TEXT="Remove LaTeX formula" ID="ID_1339380039" CREATED="1303931933356" MODIFIED="1311406091437" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3,7"/>
</node>
<node TEXT="" ID="ID_1974921327" CREATED="1310061549760" MODIFIED="1311406091437" LINK="http://freeplane.sourceforge.net/wiki/index.php/File:Freeplane_LaTex.mm" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Refine" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3,7"/>
<hook NAME="SummaryNode"/>
</node>
</node>
<node TEXT="Other" FOLDED="true" ID="ID_1058571618" CREATED="1310122180308" MODIFIED="1310196223744" TEXT_SHORTENED="true">
<hook NAME="SummaryNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<node TEXT="Automatic node numbering" ID="ID_1868795872" CREATED="1310121407444" MODIFIED="1311402835704" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To automatically add a serial node number like 1.2.3,
    </p>
    <ul>
      <li>
        &#160;open <i>View &gt; Properties Panel</i>
      </li>
      <li>
        set checks in check boxes for <i>Change</i>&#160;and <i>Node numbering</i>
      </li>
    </ul>
    <p>
      <b>Note </b>
    </p>
    <ul>
      <li>
        All children are numbered starting by 1 downwards and from right to left
      </li>
      <li>
        Nodes in the second level are numbered 1.1, 1.2, ... 2.1, 2.2, ... and so forth.
      </li>
      <li>
        N.B. The root node gets no number
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="Insert Node date" ID="ID_1844988948" CREATED="1310122028971" MODIFIED="1311622692649" TEXT_SHORTENED="true" LINK="#ID_63295810">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To insert a date int to a node, see <i>View &gt; Properties Panel &gt; Calendar &amp; Attributes</i>
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>View &gt; Properties Panel &gt; Calendar &amp; Attributes</i>&#160;for standard format dates
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="Template / Format" ID="ID_91758847" CREATED="1311336806364" MODIFIED="1311336821153"/>
</node>
</node>
<node TEXT="Styles" STYLE_REF="MainMenuAccent" FOLDED="true" POSITION="right" ID="ID_1778719162" CREATED="1291143603918" MODIFIED="1311955526248" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Styles (classes) are a means to quickly&#160;&#160;apply or change a group of visual properties or content&#160;&#160;for all related nodes at once. Styles (classes) can be used to find, select or filter nodes which are associated with a particular type of content or structure.&#160; You can attach one or more styles to a node by selecting the style name(s) from a list. With filter rules you can automatically decide if a particular style should be applied to a particular node or not.. There is a predefined set of styles for 5 different hierarchical node levels which can be applied right away.
    </p>
    <p>
      
    </p>
    <p>
      <b>Example</b>. The <i>Freeplane Documentation</i>&#160;you are reading, contains visually discriminating styles for classes of information indicating respectively methods, exceptions, examples etc. Each node is attached to all the styles (classes) it belongs to. As a result&#160;&#160;you can see form the appearance of the node which type of information it contains. Also you can display only nodes containing one particular type of information, by filtering for for style (class) <i>Definition</i>. For example you can display only (all) definitions.
    </p>
    <p>
      
    </p>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        If a defined style has Attributes or Details, these are copied to the node being attached to the style, but only if the node has been newly created, or if the method for this is applied.
      </li>
      <li>
        Single styles are available through a drop-down menu in the Toolbar.
      </li>
      <li>
        It is possible to assign styles to the F-keys such that they can be easily applied.
      </li>
    </ul>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>Node features &gt; Attributes &gt; Copy from style</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MainMenu" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
<node TEXT="Styles" FOLDED="true" ID="ID_512501370" CREATED="1291199304490" MODIFIED="1311955598929" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000">Opens a list of node styles. The selected style will be appied to the selected node(s). </font>
    </p>
    <p>
      
    </p>
    <p>
      <b>See also </b>
    </p>
    <ul>
      <li>
        <i>Styles &gt; User defined styles</i>
      </li>
      <li>
        <i>View &gt; Properties panel&#160;</i>
      </li>
      <li>
        <i>Styles &gt; Manage Conditional Styles</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
<node TEXT="Default" ID="ID_1111398038" CREATED="1310066467081" MODIFIED="1310848304117" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Details" ID="ID_1577454061" CREATED="1310066475584" MODIFIED="1310848304133" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Note" ID="ID_985652023" CREATED="1310066511975" MODIFIED="1310848304133" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Hierarchical Level styles" ID="ID_1923695111" CREATED="1311948335133" MODIFIED="1311955580739"/>
<node TEXT="User defined styles" ID="ID_948529232" CREATED="1291550475365" MODIFIED="1311405450074" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000">Opens a list of user defined node styles. The selected style will be appied to the selected node(s). </font>
    </p>
    <p>
      
    </p>
    <p>
      <b>See also </b>
    </p>
    <ul>
      <li>
        <i>Styles &gt; New style from selection</i>
      </li>
      <li>
        <i>Styles &gt; Edit style</i>
      </li>
      <li>
        <i>Styles &gt; predefined styles</i>
      </li>
      <li>
        <i>View &gt; Properties panel&#160;</i>
      </li>
      <li>
        <i>Styles &gt; Manage Conditional Styles</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
</node>
</node>
<node TEXT="New style from selection" FOLDED="true" ID="ID_643671716" CREATED="1291389456052" MODIFIED="1311960465723" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Opens a dialog to enter the name under which the selected node will be saved as a user style.
    </p>
    <p>
      
    </p>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        In the style are copied:

        <ul>
          <li>
            icons
          </li>
          <li>
            Basic node text format
          </li>
        </ul>
      </li>
    </ul>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>&#160;Styles &gt; Edit style</i>&#160;to add/change a style
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
<node TEXT="Style Details and Attributes" ID="ID_1105873514" CREATED="1311959895499" MODIFIED="1311960596686" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        <i>Details</i>&#160;and <i>Attributes</i>&#160;can be added to a Style ONLY in the edit styles panel
      </li>
    </ul>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>Style &gt; Edit styles&gt; Edit&#160;&#160;t</i>o add Details and Attributes
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Redefine style" STYLE_REF="Functie" ID="ID_1559299046" CREATED="1291196112325" MODIFIED="1311405450120" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Change the standard node style to the one of the selected node.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
</node>
<node TEXT="Automatic layout" FOLDED="true" ID="ID_1679194345" CREATED="1291199318553" MODIFIED="1311947787977" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Opens a list of 6 styles which can be automatically applied to respectively the root and 5 hierarchical node levels from the root.
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>Styles &gt; Edit style t</i>o change the styles&#160;to your own wish
      </li>
      <li>
        <i>Styles &gt; Manage Conditional Style </i>to get the same effect with filters
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
<node TEXT="Format of the root node" ID="ID_1512017078" CREATED="1310066376934" MODIFIED="1310849026063" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="1. Level Node Format" ID="ID_912205112" CREATED="1310066388858" MODIFIED="1310849082192" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="2. Level Node Format" ID="ID_1521677657" CREATED="1310066388858" MODIFIED="1310849082208" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="3. Level Node Format" ID="ID_1792037977" CREATED="1310066388858" MODIFIED="1310849082208" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="4. Level Node Format" ID="ID_33761254" CREATED="1310066388858" MODIFIED="1310849082192" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="5. Level Node Format" ID="ID_730037876" CREATED="1310066388858" MODIFIED="1310849082192" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Manage Conditional Styles for Map" FOLDED="true" ID="ID_1169167186" CREATED="1291196254933" MODIFIED="1312558286303" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif">Opens a dialog to define the rules for automatically applying a particular style for each node. </font>A particular node may trigger different rules at once. In that case the different styles will be applied to the same&#160;&#160;node.
    </p>
    <p>
      
    </p>
    <p>
      If the conditions of the rule are true, the style is applied to the node. Multiple rules may fire at once, hence a node can display multiple styles. The list of rules is evaluated from top rule to bottom rule. If a rule triggers and <i>Stop</i>&#160;is set for that rule, the rules below will not be evaluated and not fire. &#160;The style rule itself is similar to a filter rule.
    </p>
    <p>
      
    </p>
    <p>
      <b>Do</b>
    </p>
    <ul>
      <li>
        In the conditional styles window select <i>New</i>&#160;. A rule is added.
      </li>
      <li>
        Double-click on the last, new row to open the <i>Filter composer</i>.
      </li>
      <li>
        Select or define and select a new rule, see <i>Filter Composer</i>. (One rule is highlighted !
      </li>
      <li>
        Click OK).
      </li>
      <li>
        Click on <i>Default</i>&#160;and select the style you want from the drop down menu of syles.
      </li>
      <li>
        Click OK.
      </li>
    </ul>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        Filter composer
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="0" OBJECT="org.freeplane.features.format.FormattedNumber|0.0"/>
<node TEXT="Revision" ID="ID_1791624318" CREATED="1312115683651" MODIFIED="1312144957668" TEXT_SHORTENED="true" LINK="#_Freeplane_Link_784043927">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Example" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      This map contains conditional map styles for displaying revisions of this map.
    </p>
    <p>
      Click the green arrow to read more.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="" ID="ID_362867811" CREATED="1310102673082" MODIFIED="1311405450136" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Only works with newly created mind maps (= Freeplane 1.2, not 1.3) or if the mapstyle is copied from a Freeplane 1.2 map with copy mapstyle.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Templates" ID="ID_1796388799" CREATED="1310103103356" MODIFIED="1311405450136" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="Refine" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <pre wrap="">The same nodes on the style map are used as templates and as styles.
They are used as styles either if style is explicitly assigned by user or if
style condition is met .  Only formatting including colors, shapes clouds and
icons are relevant for styles and conditional styles. All these features have
in common that they can not be edited, they can be only attached or removed,
and style basically means that they are virtually attached to relevant nodes.

Actually style conditions build a chain so that many styles are applied to the
same node if many conditions are satisfied.
For instance if you have conditional styles 

Node text contains &quot;Important&quot; =&gt; styleOne,
Node text contains &quot;error&quot; =&gt; styleTwo,

the both styles are applied to node with the both words in the node text, and
all formatting features not addressed by styleOne but addressed by styleTwo
are applied to the node. For instance if styleOne means bold text and styleTwo
means red background, the node becomes bold on the red background.

Other node features like attributes or details are editable. Therefore they
are interpreted as a part of template. It is possible to include other features
like notes or even child nodes in the future. The templates are applied
as follows:
 
There are many ways to insert new nodes: before, after, as a child, as a parent,
as summary node. After the new node is inserted you can apply a template for
it just assigning the corresponding style. In this case its content is copied
into the new created node.

And if you want to copy the template elements later you can do it using only
one action (which currently copies only the attributes but it could be improved
to copy the details and everything else too). Assign this action to a short
cut and you can do it by hitting only one hot key.  I can change this action
so that it works with the conditional styles too so that the first or even all
elements from the styles activated by conditions are copied.</pre>
  </body>
</html></richcontent>
</node>
<node TEXT="Table of style rules." FOLDED="true" ID="ID_1058697460" CREATED="1312551108410" MODIFIED="1312551121202">
<node TEXT="Empty" ID="ID_805739790" CREATED="1312551143432" MODIFIED="1312557189580" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Initially the table is empty.
    </p>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="New" ID="ID_322783972" CREATED="1312550032317" MODIFIED="1312557158458" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Press to add a new rule.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Edit" ID="ID_338804554" CREATED="1312550065693" MODIFIED="1312557158489" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To edit the selected rule.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Delete" ID="ID_44463838" CREATED="1312550073844" MODIFIED="1312557158489" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To remove the selected rule(s).
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Up" ID="ID_1131478979" CREATED="1312550078001" MODIFIED="1312557158489" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Move the selected rule one place up. Hence it will be evaluated earlier.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Down" ID="ID_198508283" CREATED="1312550083485" MODIFIED="1312557158473" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To move the position of the selected rule one place down. Hence it will be evaluated later.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Active" ID="ID_1882394254" CREATED="1312550251133" MODIFIED="1312557158473" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="IsChecked" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      If is Checked, the rule behind it will be evaluated. Otherwise not.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Condition" FOLDED="true" ID="ID_1073372746" CREATED="1312550256444" MODIFIED="1312557158473" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      The condition, set with a filter rule, when the rule will fire. If the rule fires, the style will be applied. Otherwise the style will not be applied.
    </p>
  </body>
</html></richcontent>
<node TEXT="Always" ID="ID_894528691" CREATED="1312550827305" MODIFIED="1312557178956" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Default filter rule: it will fire any time.
    </p>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Style" FOLDED="true" ID="ID_1230882400" CREATED="1312550264190" MODIFIED="1312550270899">
<node TEXT="Default" ID="ID_1640996661" CREATED="1312550406517" MODIFIED="1312557178956" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      The style <i>Default</i>.&#160;Double-click to open a drop down list of possible styles.
    </p>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Stop" ID="ID_496187218" CREATED="1312550090044" MODIFIED="1312557210952" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="UnChecked" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      If is checked and this rule fires, the rules below this rule are not evaluated.
    </p>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Manage Conditional Styles for Node" FOLDED="true" ID="ID_1036704793" CREATED="1311948482849" MODIFIED="1312558092052" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Opens a dialog to define and connect multiple style rules to the selected node.&#160;If the conditions of the rule are true, the style is applied to the node. Multiple rules may fire at once, hence a node can display multiple styles.
    </p>
    <p>
      The list of rules is evaluated from top rule to bottom rule. If a rule triggers and <i>Stop</i>&#160;is set for that rule, the rules below will not be evaluated and not fire. &#160;The style rule itself is similar to a filter rule.
    </p>
    <p>
      
    </p>
    <p>
      <b>Do</b>
    </p>
    <ul>
      <li>
        In the conditional styles window select <i>New</i>&#160;
      </li>
      <li>
        Double-click on the last, new row to open the <i>Filter composer</i>.
      </li>
      <li>
        Select or define and select a new rule, see <i>Filter Composer</i>. (One rule is highlighted !
      </li>
      <li>
        Click OK).
      </li>
      <li>
        Click on <i>Default</i>&#160;and select the style you want from the drop down menu of syles.
      </li>
      <li>
        Click OK.
      </li>
    </ul>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        Filter composer
      </li>
      <li>
        <i>Styles &gt; Edit styles</i>. In the panel that opens it is possible do define node styles too.
      </li>
    </ul>
  </body>
</html></richcontent>
<node TEXT="Conditional node style" ID="ID_589037277" CREATED="1312530388253" MODIFIED="1312531097782" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      This node you are reading is made using Conditional Node styles. The styles applied are shown at the top of the Tool Tip.
    </p>
    <p>
      
    </p>
    <p>
      Open the window Styles &gt; Manage Conditional Node Styles to see the two rules, one for applying style Example and one for style ToNote. Both rules are of type: always apply this style. This is the most simple use of conditional node styles: to apply more than one style to the same node at once.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Example" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
</hook>
</node>
<node TEXT="Table of style rules." FOLDED="true" ID="ID_278157696" CREATED="1312551108410" MODIFIED="1312551121202">
<node TEXT="Empty" ID="ID_1858267405" CREATED="1312551143432" MODIFIED="1312557189580" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Initially the table is empty.
    </p>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="New" ID="ID_1731631753" CREATED="1312550032317" MODIFIED="1312557158458" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Press to add a new rule.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Edit" ID="ID_1266620968" CREATED="1312550065693" MODIFIED="1312557158489" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To edit the selected rule.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Delete" ID="ID_1025560603" CREATED="1312550073844" MODIFIED="1312557158489" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To remove the selected rule(s).
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Up" ID="ID_1559495569" CREATED="1312550078001" MODIFIED="1312557158489" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Move the selected rule one place up. Hence it will be evaluated earlier.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Down" ID="ID_128011628" CREATED="1312550083485" MODIFIED="1312557158473" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To move the position of the selected rule one place down. Hence it will be evaluated later.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Active" ID="ID_1788623449" CREATED="1312550251133" MODIFIED="1312557158473" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="IsChecked" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      If is Checked, the rule behind it will be evaluated. Otherwise not.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Condition" FOLDED="true" ID="ID_1272337891" CREATED="1312550256444" MODIFIED="1312557158473" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      The condition, set with a filter rule, when the rule will fire. If the rule fires, the style will be applied. Otherwise the style will not be applied.
    </p>
  </body>
</html></richcontent>
<node TEXT="Always" ID="ID_386830428" CREATED="1312550827305" MODIFIED="1312557178956" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Default filter rule: it will fire any time.
    </p>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Style" FOLDED="true" ID="ID_654077801" CREATED="1312550264190" MODIFIED="1312550270899">
<node TEXT="Default" ID="ID_563202841" CREATED="1312550406517" MODIFIED="1312557178956" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      The style <i>Default</i>.&#160;Double-click to open a drop down list of possible styles.
    </p>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Stop" ID="ID_293594044" CREATED="1312550090044" MODIFIED="1312557210952" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="UnChecked" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      If is checked and this rule fires, the rules below this rule are not evaluated.
    </p>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Insert style related content [Style Details &amp; Attributes]" ID="ID_1403178597" CREATED="1311948532074" MODIFIED="1311961496488" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To copy into the selected node the&#160;&#160;<i>Details</i>&#160;and <i>Attributes</i>&#160; of the styles connected to the node.
    </p>
    <p>
      
    </p>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        Applies if <i>Details</i>&#160;or <i>Attributes</i>&#160;in the style have been changed, or if the node has not been connected to the style at a birth.
      </li>
    </ul>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>New style from selectio</i>n
      </li>
      <li>
        <i>Node features &gt; Attributes &gt; Copy from style</i>
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="Automatic Layout [Automatic  Level Style]" ID="ID_1362134553" CREATED="1310063210078" MODIFIED="1311405450152" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Set/unset the style mode to automatic layout. The nodes automatically get the predefined styles related to their node level.
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>Styles &gt; Automatic layout</i>&#160;submenu
      </li>
      <li>
        <i>View &gt; Properties Panel</i>&#160;check box for Automatic Layout
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="Copy map style from..." ID="ID_13843990" CREATED="1291196992351" MODIFIED="1311405450183" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Opens a dialog for choosing a map style. These styles are in fact mind maps.
    </p>
    <p>
      
    </p>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        <i>File &gt; New map</i>&#160;uses the style <i>standard.mm</i>. You can define a user map as standard.mm by naming it that way and putting it in the directory that opens by selecting <i>Tools &gt; Open user directory &gt; templates. </i>
      </li>
      <li>
        <i>In Tools &gt; Preferences</i>&#160;can be defined that the standard mind map should have a different name.
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
</node>
<node TEXT="Edit styles" STYLE_REF="Functie" ID="ID_1277682010" CREATED="1291196100290" MODIFIED="1311405450183" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Opens a dialog to edit all styles, add and remove styles.
    </p>
    <p>
      
    </p>
    <p>
      To define a new user style in this mode:
    </p>
    <ul>
      <li>
        Select the standard node style
      </li>
      <li>
        Click-right and select <i>Save selection as user style</i>
      </li>
      <li>
        Save it under a new name
      </li>
      <li>
        Modify the newly created, saved style with <i>View &gt; Properties Panel</i>
      </li>
      <li>
        Leave the dialog; it has no OK button.
      </li>
      <li>
        When ask to save, press OK.
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
</node>
<node TEXT="More" STYLE_REF="Aggregatie als Toelichting" FOLDED="true" ID="ID_1132402390" CREATED="1305876789039" MODIFIED="1311405450183" TEXT_SHORTENED="true">
<hook NAME="SummaryNode"/>
<attribute NAME="Chapter" VALUE="4"/>
<node TEXT="Style (and Node template)" ID="ID_1305038685" CREATED="1310847063806" MODIFIED="1311405450198" TEXT_SHORTENED="true">
<attribute_layout NAME_WIDTH="82" VALUE_WIDTH="156"/>
<attribute NAME="Chapter" VALUE="4"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      A <b>style</b>&#160;for a node is a set of node properties and a basic content structure that can be applied to the node by calling the name of the style.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Node template" ID="ID_1252291426" CREATED="1310925308234" MODIFIED="1311959880220" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        If a style has Attributes or Details, Details these are copied to the node being attached to the style, but ONLY if the node has been newly created.
      </li>
    </ul>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>Node features &gt; Attributes &gt; Copy attributes from style</i>&#160;to copy the content later
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="Example style" ID="ID_1379615942" CREATED="1291305410190" MODIFIED="1311405450230" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Example" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
</node>
<node TEXT="System location" ID="ID_825090436" CREATED="1310065922176" MODIFIED="1311405450230" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="Refine" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Styles are stored in the configuration file &quot;patterns.xml&quot; (note&#160;: this file name can be customized by the user under <i>Tools &gt; Preferences &gt; Environment</i>).
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Wiki" ID="ID_422433135" CREATED="1310926117459" MODIFIED="1311405450230" LINK="http://freeplane.sourceforge.net/wiki/index.php/Node_styles">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Refine" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
</node>
</node>
</node>
<node TEXT="GettingStarted" FOLDED="true" POSITION="left" ID="ID_1108028010" CREATED="1309552935597" MODIFIED="1312615454174" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      This documentation is unlike a classical linear presentation of information. It is set up as a source of information which can be used in a way wich is most appropriate for a particular user an situation.
    </p>
    <p>
      
    </p>
    <p>
      The right side of this mind map describes the Freeplane menu's and functionality. This is a huge amount of information and therefore difficult to grasp at once.&#160;&#160;Most of it is also not necessary to know for a simple user . Therefore it is hidden from view, except the names of the main menu's.
    </p>
    <p>
      
    </p>
    <p>
      The left side of the map shows a classical view of chapters. These (numbered) chapters are unlike classical linear information. They describe how you can use filtering&#160;&#160;to gather and present a subset of the right hand side's information, in an amount that is useful and can be digested. You can go through the information displayed by each chapter in your own way and order. The chapters are numbered and organized from simple to more advanced.
    </p>
    <p>
      
    </p>
    <p>
      Some information from the right hand side is specially prepared to give you a quick start.&#160;&#160;This information is stored on the left side under Basics. Don't worry Chapter 1 will present you most of this information. Finally the left hand side contains information installing Freeplane and about this documentation.
    </p>
    <p>
      
    </p>
    <p>
      As a novice, read Chapters&#160;&#160;1 - 4. Hover over node <i>Chapter</i>&#160; for viewing instructions.
    </p>
    <p>
      
    </p>
    <p>
      To become a professional, read also the other chapters &#160;and read the nodes displaying after:
    </p>
    <ul>
      <li>
        Filter<i>&#160;(Class, Contains, Definition)</i>
      </li>
      <li>
        Filter <i>(Class, Contains, Exception</i>)
      </li>
      <li>
        Filter <i>(Class, Contains,ToNote)</i>
      </li>
    </ul>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        <i>Chapter</i>&#160;does not contain content except a chapter index and filter instruction.
      </li>
      <li>
        All chapters are generated by filtering. This facilitates maintenance.
      </li>
    </ul>
  </body>
</html></richcontent>
<node TEXT="Click on text to unfold an optional hidden tree." ID="ID_502812150" CREATED="1311400357132" MODIFIED="1311406421760">
<icon BUILTIN="idea"/>
</node>
<node TEXT="Hover with the cursor over the visible items to display optional hidden text." ID="ID_1774697602" CREATED="1311400331385" MODIFIED="1312558728470">
<icon BUILTIN="idea"/>
</node>
<node ID="ID_959050622" CREATED="1311400886769" MODIFIED="1312558718237">
<icon BUILTIN="idea"/>
<richcontent TYPE="NODE">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Hover over <i>GettingStarted</i>&#160;for a reading guide.
    </p>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Basics" LOCALIZED_STYLE_REF="default" FOLDED="true" POSITION="left" ID="ID_789850012" CREATED="1309419541463" MODIFIED="1311402048090" TEXT_SHORTENED="true">
<edge STYLE="bezier"/>
<node TEXT="Finding your way" FOLDED="true" ID="ID_1836270960" CREATED="1310900715782" MODIFIED="1312615541335" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Refine" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="Procedure" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="1,2"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To help you orient in this documentation and find information more easily, it is organized in the following way:
    </p>
    <ul>
      <li>
        The main content is structured along the lines of the main menu (right hand side). Each menu and submenu contains hidden text which displays if the cursor is hovering above it (<i>Tool Tip</i>). The main menu contains a more general description and the submenu and its items more specific information.
      </li>
      <li>
        Different types of context are displayed by different styles. Definition nodes are displayed in italic.
      </li>
      <li>
        It is possible to find or filter for specific text and/or or for these types called classes.
      </li>
    </ul>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        Making This documentation 1.2
      </li>
    </ul>
  </body>
</html></richcontent>
<node TEXT="Definition" STYLE_REF="Definition" ID="ID_228937336" CREATED="1309618770906" MODIFIED="1311402615666" TEXT_SHORTENED="true">
<attribute NAME="Chapter" VALUE="1,2"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      All definitions can be regognised by this style.
    </p>
    <p>
      
    </p>
    <p>
      Note
    </p>
    <ul>
      <li>
        It is possible to view a list of all definitions by filtering, see the example.
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="Method" STYLE_REF="Method" ID="ID_516439573" CREATED="1309618777965" MODIFIED="1311402615681" TEXT_SHORTENED="true">
<attribute NAME="Chapter" VALUE="1,2"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      A method is a Freeplane action as in a menu item.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="OptionalValue" STYLE_REF="OptionalValue" FOLDED="true" ID="ID_1926379292" CREATED="1310845841441" MODIFIED="1311402754927" TEXT_SHORTENED="true">
<attribute NAME="Chapter" VALUE="2"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      An optional value is a value that can be selected in a menu dialog.
    </p>
  </body>
</html></richcontent>
<node TEXT="IsChecked" ID="ID_833160047" CREATED="1312143903870" MODIFIED="1312144371887">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="IsChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
</node>
<node TEXT="UnChecked" ID="ID_1245047726" CREATED="1312143954266" MODIFIED="1312144371902">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="UnChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
</node>
</node>
<node TEXT="Procedure" STYLE_REF="Procedure" ID="ID_414428824" CREATED="1310838858321" MODIFIED="1311402583810" TEXT_SHORTENED="true">
<attribute NAME="Chapter" VALUE="1"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      A procedure is a description of how methods can be used to realize a particular effect.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Exception" STYLE_REF="Exception" ID="ID_609263580" CREATED="1309891278322" MODIFIED="1311402754943" TEXT_SHORTENED="true">
<attribute NAME="Chapter" VALUE="2"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      An exception is behaviour which may be unexpected.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="ToNote" STYLE_REF="ToNote" ID="ID_1172516925" CREATED="1309890992904" MODIFIED="1311402754943" TEXT_SHORTENED="true">
<attribute NAME="Chapter" VALUE="2"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Information to note attracts attention to relevant related behaviour.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Refine" STYLE_REF="Refine" ID="ID_1719115146" CREATED="1309619037550" MODIFIED="1311402754958" TEXT_SHORTENED="true">
<attribute NAME="Chapter" VALUE="2"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Information with this style gives more background or more details.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Example" STYLE_REF="Example" ID="ID_1648027177" CREATED="1309639156705" MODIFIED="1311402754958" TEXT_SHORTENED="true">
<attribute NAME="Chapter" VALUE="2"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Information with this style describes an example.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Revision" STYLE_REF="Revision" ID="ID_901482560" CREATED="1312144029512" MODIFIED="1312144398734" TEXT_SHORTENED="true">
<attribute NAME="Chapter" VALUE="2"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To indicate that a node has been modified.
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        Example Conditional Map Styles.
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="MainMenu" STYLE_REF="MainMenu" ID="ID_1534498435" CREATED="1309691442065" MODIFIED="1311402615697" TEXT_SHORTENED="true">
<attribute NAME="Chapter" VALUE="1,2"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Information with this style represents a menu in the <i>Main toolbar.</i>
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="MenuGroup" STYLE_REF="MenuGroupLabel" ID="ID_1191898047" CREATED="1310838790219" MODIFIED="1311486685783" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Information with this style summarizes a group of related methods.
    </p>
  </body>
</html></richcontent>
<attribute NAME="Chapter" VALUE="2"/>
</node>
</node>
<node TEXT="Basic terms" STYLE_REF="Definition" FOLDED="true" ID="ID_1480936832" CREATED="1309419658445" MODIFIED="1311366593106" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      As a tree editor/mind mapper, Freeplane uses some standard terms from computer science. As Wikipedia explains: &quot;In computer science, a tree is a widely-used data structure that emulates a hierarchical tree structure with a set of linked nodes&quot;.
    </p>
    <p>
      
    </p>
    <p>
      Basically, a mind map consists of <b>nodes</b>&#160;and <b>relations between nodes. </b>
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        Click on this node to unfold and see more terms.
      </li>
      <li>
        Press the red arrow to read more on Internet.
      </li>
    </ul>
  </body>
</html></richcontent>
<node TEXT="Nodes" FOLDED="true" ID="ID_1279930643" CREATED="1309419658459" MODIFIED="1311402583826" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      A&#160;<b>node</b>&#160;is the point in the map which carries the information. I si the basic unit information and usually includes text and images. The text you are currently reading has been entered into this node.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="1"/>
<node TEXT="Root/Root node" ID="ID_1163921092" CREATED="1309419658455" MODIFIED="1311402583826" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      The <b>root node</b>&#160;is the central node in the hierarchy from which all others flow.&#160;&#160;It.is the oval that is present by default at the centre of Freeplane workspace. It cannot be deleted, nor can there be more than one root.
    </p>
  </body>
</html></richcontent>
<hook NAME="FirstGroupNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="1"/>
</node>
<node TEXT="Parent node" STYLE_REF="Definition" ID="ID_283022327" CREATED="1309419658464" MODIFIED="1311402583842" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>Parent node</b>&#160;is the name of a node that precedes another node in the hierarchy.
    </p>
  </body>
</html></richcontent>
<attribute NAME="Chapter" VALUE="1"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
</node>
<node TEXT="Child node" STYLE_REF="Definition" ID="ID_1209680113" CREATED="1309419658467" MODIFIED="1311402583857" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      A <b>child node </b>is a node that proceeds another node (its parent) in the hierarchy.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="1"/>
</node>
<node TEXT="Sibling node" STYLE_REF="Definition" ID="ID_1669709193" CREATED="1309419658469" MODIFIED="1311402583857" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      A <b>sibling</b>&#160;is a node at the same hierarchical level as a particular other node.&#160;&#160;Thus if you insert a node, and then another one underneath, you have created a sibling.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="1"/>
</node>
<node TEXT="Relations are relative" ID="ID_1402619445" CREATED="1309420875668" MODIFIED="1310196223463" TEXT_SHORTENED="true">
<hook NAME="SummaryNode"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      These family terms can be extended to represent the respective relationship, such as grandparent, grandchild, uncle and so on. Note that the designated terms are relative, thus a node is a child to its parent and a parent to its child and so on.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Refine" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Relations" STYLE_REF="Definition" FOLDED="true" ID="ID_1688918449" CREATED="1309419658471" MODIFIED="1311012621775" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Freeplane can represent several kinds of relations. Edges are buid logical relations representing a hierarchy. Connectors are user defined conceptual relations. And sum nodes represent visual aggregation relations.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
<node TEXT="Edge" STYLE_REF="Definition" FOLDED="true" ID="ID_682300475" CREATED="1309419658485" MODIFIED="1311402583873" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      An <b>edge</b>&#160;is the visual representation of the tree structure.&#160;&#160;It is the line you see underneath and between the nodes, This line can be hidden.
    </p>
    <p>
      
    </p>
    <p>
      Characteristics of an egde are:
    </p>
    <ul>
      <li>
        <b>Edge color:</b>&#160;The color of the edge connecting nodes.
      </li>
      <li>
        <b>Edge style:</b>&#160;The way an edge bends. If an edge is visible or hidden.
      </li>
      <li>
        <b>Edge width:</b>&#160;The thickness of the edge.
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="1"/>
<node TEXT="Branch" STYLE_REF="Definition" ID="ID_979063484" CREATED="1309513163069" MODIFIED="1310837396416" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      A <b>branch</b>&#160;isa group of nodes consisting of a node and all its descendents. A main branch is a branch which starts at the child of the root node.
    </p>
    <p>
      
    </p>
    <p>
      <b>To branch</b>&#160;means to tear off the nodes of a branch and export it to another map.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Connector" STYLE_REF="Definition" ID="ID_1063899671" CREATED="1309419658487" MODIFIED="1310837410816" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      A <b>connector</b>&#160;is a graphical link that allows for connections outside of the main tree hierarchy. It is an arrow or line between two arbitrary nodes expressing an implicit, user defined meaning. This meaning may be made explicit with a label
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
</node>
<node TEXT="Cloud" STYLE_REF="Definition" ID="ID_1605059702" CREATED="1309509385523" MODIFIED="1310837422750" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      A <b>cloud</b>&#160;is a visual wrapper for a group of nodes which are part of a branch. The group is an attribute of a node and contains this node and all of its descendants.
    </p>
  </body>
</html></richcontent>
<font BOLD="false"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
</node>
<node TEXT="Summary Node" STYLE_REF="Definition" ID="ID_1358837812" CREATED="1309419658489" MODIFIED="1310837434310" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      A <b>summary node</b>&#160;is&#160;a graphical structure representing a user defined group of siblings. The first and last element of the group are indicated by the end point of an accollade. The central point of the group points to a node, the actual summary node, which contains information about the group.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Node features" ID="ID_1372334594" CREATED="1309631960680" MODIFIED="1311012855822" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>Node features</b>&#160;are additional features that can be added to a basic node.
    </p>
    <p>
      
    </p>
    <p>
      &#160;Examples are notes, attributes, hyperlinks and images.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Node properties" ID="ID_395697976" CREATED="1309632189617" MODIFIED="1310196223417" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>Node properties</b>&#160;are (mostly visible) aspects of nodes, relations and node features. Examples are the color and size of text.
    </p>
    <p>
      
    </p>
    <p>
      <b>See also:</b>
    </p>
    <ul>
      <li>
        <i>Formatting</i>
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Creating a simple map" ID="ID_306961173" CREATED="1309422625880" MODIFIED="1311402583873" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      When Freeplane first opens you will be greeted by a blank map. It contains one, elliptical form, the root node. The root node contains a text such as &quot;New map&quot;. This text is highligted, meaning you can replace it by typing your own text. Simply type your title and press <i>Enter</i>. The <i>Insert</i>&#160;&#160;key will add a child node - again just type and press <i>Enter</i>. A second <i>Enter</i>&#160;&#160;will add a sibling. That's really all there is to it. The basic modus operandi is that simple. You can now easily create a simple map
    </p>
  </body>
</html></richcontent>
<attribute NAME="Chapter" VALUE="1"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Procedure" LAST="false"/>
</hook>
</node>
<node TEXT="Editing basic node text" LOCALIZED_STYLE_REF="default" FOLDED="true" ID="ID_697305939" CREATED="1309422760422" MODIFIED="1311402583888" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To add text to a selected node simply type and press <i>Enter</i>&#160;when done. Move the cursor through the edited text with the arrow keys. Press <i>End</i>&#160; to place the cursor at the end of the text. Or press <i>Home</i>&#160;to place the cursor at the beginning. To add formatting like bold and italic, right-click on the text you are typing and select in the context menu that opens <i>Format</i>.<br/><br/>Text can also be edited in a a separate WYSIWYG dialog for more extensive formatting (Edit long node): select menu <i>Nodes &gt; Edit node in separate Dialog</i>&#160;or press <i>Shift+Enter</i>. Here you can create tables, numbered and bullet-pointed lists and even do HTML-editing.
    </p>
    <p>
      
    </p>
    <p>
      It is also possible to add text to special areas of the node called <b>details</b>, <b>notes</b>&#160;and <b>attributes</b>. This is described in section <i>Node features</i>.
    </p>
    <p>
      
    </p>
    <p>
      <b>Warning</b>: Starting to type on any selected node will overwrite the current text. If you do this by accident then press <i>Enter</i>, then select <i>Edit &gt; Undo</i>&#160;or press <i>Ctrl+Z</i>.
    </p>
  </body>
</html></richcontent>
<attribute NAME="Chapter" VALUE="1"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Procedure" LAST="false"/>
</hook>
<node TEXT="Pasting text as child node" ID="ID_277512717" CREATED="1286646785709" MODIFIED="1311402583904" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To paste a copied text into a new child of a selected node, paste the text by selecting <i>Paste</i>&#160;in menu Edit or the context menu of the node.
    </p>
    <p>
      
    </p>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        The selected node must not be in edit mode (cursor showing in the text).
      </li>
      <li>
        The child node is created automatically when pasting.
      </li>
      <li>
        If the text contains several lines, each line appears in a separate child node.
      </li>
      <li>
        If the text lines have leading spaces, these appear as hierarchical levels.
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Procedure" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="1"/>
</node>
<node TEXT="Pasting hyperlink as child node" ID="ID_513393084" CREATED="1270892460686" MODIFIED="1311402583920" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To paste a copied file, group of files, application, or website location as an hyperlink in a newly created child of the selected node, select <i>Paste</i>&#160;in menu Edit or the context menu of the node.
    </p>
    <p>
      
    </p>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        The child node is created automatically when pasting.
      </li>
      <li>
        The copied files&#160;&#160;or application copied are not pasted themselves, a hyperlink to their locations is.
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Procedure" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="1"/>
</node>
</node>
<node TEXT="Editing an edge" FOLDED="true" ID="ID_630478306" CREATED="1288986009569" MODIFIED="1311402583935" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To change the characteristics of an edge, select the child node (!) and:
    </p>
    <ul>
      <li>
        select <i>View &gt; Properties panel; </i>or<i>&#160;</i>
      </li>
      <li>
        select<i>&#160;Format </i>in<i>&#160;Menu bar</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Procedure" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="1"/>
<node TEXT="" FOLDED="true" ID="ID_303930508" CREATED="1288536745875" MODIFIED="1311313975787" STYLE="bubble" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Example" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Example styles
    </p>
  </body>
</html></richcontent>
<node TEXT="As parent" ID="ID_1693021941" CREATED="1310115852702" MODIFIED="1310196223385" TEXT_SHORTENED="true"/>
<node TEXT="Linear" ID="ID_414270087" CREATED="1288538234095" MODIFIED="1310196223370" HGAP="30" TEXT_SHORTENED="true">
<edge STYLE="linear" WIDTH="4"/>
</node>
<node TEXT="Bezier curve" ID="ID_58422842" CREATED="1288538240451" MODIFIED="1310196223370" HGAP="40" TEXT_SHORTENED="true">
<edge STYLE="bezier" WIDTH="4"/>
</node>
<node TEXT="Sharp Linear" ID="ID_215376208" CREATED="1288538242377" MODIFIED="1310196223354" HGAP="40" TEXT_SHORTENED="true">
<edge STYLE="sharp_linear" WIDTH="4"/>
</node>
<node TEXT="Sharp Bezier" ID="ID_1650229254" CREATED="1288538470332" MODIFIED="1310196223354" HGAP="40" TEXT_SHORTENED="true">
<edge STYLE="sharp_bezier" WIDTH="4"/>
</node>
<node TEXT="Horizontal" ID="ID_615628127" CREATED="1310115825254" MODIFIED="1310196223354" TEXT_SHORTENED="true">
<edge STYLE="horizontal"/>
</node>
<node TEXT="Hide Edge" ID="ID_254658284" CREATED="1288538507390" MODIFIED="1310196223339" BACKGROUND_COLOR="#00ffff" HGAP="50" TEXT_SHORTENED="true">
<edge STYLE="hide_edge" WIDTH="4"/>
</node>
</node>
</node>
<node TEXT="Show all definitions" FOLDED="true" ID="ID_1066951352" CREATED="1310722013900" MODIFIED="1311486841550" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>Example </b>
    </p>
    <p>
      
    </p>
    <p>
      To show all and only all definitions:
    </p>
    <ul>
      <li>
        set the 4 conditions as shown for the icon-buttons, in the Filter toolbar.; and
      </li>
      <li>
        define in the Fiter toolbar filter (<i>Class, Contains, Definition);</i>&#160;and
      </li>
      <li>
        press <i>icon-Quick filter</i>.
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Example" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="Procedure" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
<node TEXT="Check conditions" FOLDED="true" ID="ID_235662859" CREATED="1310913088700" MODIFIED="1310914330151" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Check if the icon-buttons shown to the left are all unpressed, or Press to unpress.
    </p>
  </body>
</html></richcontent>
<node TEXT="Show Ancestors: Off" ID="ID_173119044" CREATED="1310819865458" MODIFIED="1310821392966">
<icon BUILTIN="../show_ancestors"/>
<hook NAME="FirstGroupNode"/>
</node>
<node TEXT="Show Descendants: Off" ID="ID_685714769" CREATED="1310819950485" MODIFIED="1310819980625">
<icon BUILTIN="../show_descendants"/>
</node>
<node TEXT="Unfold all nodes hidden after filtering: Off" ID="ID_1351621161" CREATED="1310820023813" MODIFIED="1310820087805">
<icon BUILTIN="../unfold_filtered_ancestors"/>
</node>
<node TEXT="Applies to filtered nodes: Off" ID="ID_1625012504" CREATED="1310820191451" MODIFIED="1310820292758">
<icon BUILTIN="../applies_to_filtered_nodes"/>
</node>
</node>
<node TEXT="Unfold all nodes" FOLDED="true" ID="ID_1616604166" CREATED="1310913172416" MODIFIED="1310913183306">
<node TEXT="Select root node (press Esc)" ID="ID_385659674" CREATED="1310821238510" MODIFIED="1310821551509">
<hook NAME="FirstGroupNode"/>
</node>
<node TEXT="Press: Unfold all descendants" ID="ID_1954125202" CREATED="1310821255419" MODIFIED="1310821336463">
<icon BUILTIN="../unfold_all"/>
</node>
</node>
<node TEXT="Select: Class" ID="ID_1042564393" CREATED="1310820732631" MODIFIED="1310821479686">
<hook NAME="FirstGroupNode"/>
</node>
<node TEXT="Select: Contains" ID="ID_1553005293" CREATED="1310820745993" MODIFIED="1310820756976"/>
<node TEXT="Select: Definition" ID="ID_695746856" CREATED="1310820789556" MODIFIED="1310820798012"/>
<node TEXT="Define filter rule" ID="ID_341322292" CREATED="1310821479686" MODIFIED="1310821489280">
<hook NAME="SummaryNode"/>
</node>
<node TEXT="Press Quick Filter" ID="ID_1377895866" CREATED="1310820822433" MODIFIED="1310820854679">
<icon BUILTIN="../apply_quick_filter"/>
</node>
<node TEXT="To end" FOLDED="true" ID="ID_1694191165" CREATED="1310821779644" MODIFIED="1310821794745">
<node TEXT="Press: Undo filter" ID="ID_738883159" CREATED="1310822117674" MODIFIED="1310822194279">
<icon BUILTIN="../remove_filtering"/>
</node>
<node TEXT="Select root node (press Esc)" ID="ID_352017968" CREATED="1310821238510" MODIFIED="1310821551509">
<hook NAME="FirstGroupNode"/>
</node>
<node TEXT="Press: Fold all descendants" ID="ID_548666264" CREATED="1310821255419" MODIFIED="1310822238084">
<icon BUILTIN="../fold_all"/>
</node>
<node TEXT="Fold all nodes" ID="ID_214673967" CREATED="1310821426631" MODIFIED="1310821850515">
<hook NAME="SummaryNode"/>
</node>
</node>
</node>
<node TEXT="Show all about filtering" FOLDED="true" ID="ID_1873380309" CREATED="1310134532663" MODIFIED="1311487014492" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>Example</b>
    </p>
    <p>
      
    </p>
    <p>
      To view all nodes concerning filtering, define and apply a filter in the following way:
    </p>
    <p>
      
    </p>
    <p>
      1 Set the filter rule by selecting in the Filter Tool bar:
    </p>
    <ul>
      <li>
        <i>Node Text </i>(default value, or select from left drop down menu)
      </li>
      <li>
        <i>Contains (default value, or </i>select from next drop down menu)
      </li>
      <li>
        <i>filter</i>&#160;(type &quot;filter&quot; in third, edit window)
      </li>
    </ul>
    <p>
      &#160;2. Select <i>Esc</i>&#160;followed by <i>Alt + End</i>&#160;and press <i>Quick filter. </i>
    </p>
    <p>
      
    </p>
    <p>
      Now all nodes are showing which are related to filtering. Also <i>No Filter</i>&#160;is unset.
    </p>
    <p>
      
    </p>
    <p>
      3. When you are finished reading the nodes
    </p>
    <ul>
      <li>
        select No filter to restore the unfilter condition
      </li>
      <li>
        press <i>Esc</i>&#160;followed by <i>Fold all nodes</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="FirstGroupNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Example" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="Procedure" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
<node TEXT="Check conditions" FOLDED="true" ID="ID_25461383" CREATED="1310913088700" MODIFIED="1310914484077" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Check if the icon-buttons shown to the left are all unpressed, or Press to unpress.
    </p>
  </body>
</html></richcontent>
<node TEXT="Show Ancestors: Off" ID="ID_495695197" CREATED="1310819865458" MODIFIED="1310821392966">
<icon BUILTIN="../show_ancestors"/>
<hook NAME="FirstGroupNode"/>
</node>
<node TEXT="Show Descendants: Off" ID="ID_878732630" CREATED="1310819950485" MODIFIED="1310819980625">
<icon BUILTIN="../show_descendants"/>
</node>
<node TEXT="Unfold all nodes hidden after filtering: Off" ID="ID_1394638468" CREATED="1310820023813" MODIFIED="1310820087805">
<icon BUILTIN="../unfold_filtered_ancestors"/>
</node>
<node TEXT="Applies to filtered nodes: Off" ID="ID_1342621920" CREATED="1310820191451" MODIFIED="1310820292758">
<icon BUILTIN="../applies_to_filtered_nodes"/>
</node>
</node>
<node TEXT="Unfold all nodes" FOLDED="true" ID="ID_1859325076" CREATED="1310913172416" MODIFIED="1310913183306">
<node TEXT="Select root node (press Esc)" ID="ID_956693368" CREATED="1310821238510" MODIFIED="1310821551509">
<hook NAME="FirstGroupNode"/>
</node>
<node TEXT="Press: Unfold all descendants" ID="ID_205411813" CREATED="1310821255419" MODIFIED="1310821336463">
<icon BUILTIN="../unfold_all"/>
</node>
</node>
<node TEXT="Select: Node text" ID="ID_1734926057" CREATED="1310820732631" MODIFIED="1310821733866">
<hook NAME="FirstGroupNode"/>
</node>
<node TEXT="Select: Contains" ID="ID_7376434" CREATED="1310820745993" MODIFIED="1310820756976"/>
<node TEXT="Type: filter" ID="ID_1345958489" CREATED="1310820789556" MODIFIED="1310821753693"/>
<node TEXT="Define filter rule" ID="ID_950194912" CREATED="1310821479686" MODIFIED="1310821489280">
<hook NAME="SummaryNode"/>
</node>
<node TEXT="Press Quick Filter" ID="ID_338438421" CREATED="1310820822433" MODIFIED="1310820854679">
<icon BUILTIN="../apply_quick_filter"/>
</node>
<node TEXT="To end" FOLDED="true" ID="ID_1679478175" CREATED="1310821779644" MODIFIED="1310821794745">
<node TEXT="Press: Undo filter" ID="ID_407446498" CREATED="1310822117674" MODIFIED="1310822194279">
<icon BUILTIN="../remove_filtering"/>
</node>
<node TEXT="Select root node (press Esc)" ID="ID_647928992" CREATED="1310821238510" MODIFIED="1310821551509">
<hook NAME="FirstGroupNode"/>
</node>
<node TEXT="Press: Fold all descendants" ID="ID_1201115927" CREATED="1310821255419" MODIFIED="1310822238084">
<icon BUILTIN="../fold_all"/>
</node>
<node TEXT="Fold all nodes" ID="ID_426393770" CREATED="1310821426631" MODIFIED="1310821850515">
<hook NAME="SummaryNode"/>
</node>
</node>
</node>
</node>
<node TEXT="Chapter guide" POSITION="left" ID="ID_919267416" CREATED="1311398738170" MODIFIED="1312695348408" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>Guide for learning the Freeplane menu&#180;s </b>
    </p>
    <p>
      
    </p>
    <p>
      To view the following chapters, apply&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;<b>&#160; &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;filter rule&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160; </b>
    </p>
    <ol>
      <li>
        <i>Chapter &gt; 1. My first mind map &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160; </i>&#160; &#160;&#160;(Chapter,Contains,1)&#160;&#160;&#160;&#160;&#160;&#160;&#160;
      </li>
      <li>
        <i>Chapter &gt; 2. Handling nodes &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</i>&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;(Chapter,Contains,2)
      </li>
      <li>
        <i>Chapter &gt;&#160;3. Main Node features &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160; </i>(Chapter,Contains,3)
      </li>
      <li>
        <i>Chapter &gt; 4. Format &amp; Style&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</i>&#160; &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;(Chapter,Contains,4)
      </li>
      <li>
        <i>Chapter &gt; 5. Date, Time &amp; Reminder&#160;&#160;&#160;&#160;</i>&#160;&#160; &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;(Chapter,Contains,5)
      </li>
      <li>
        <i>Chapter &gt; 6. Moving &amp; Publishing maps&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</i>(Chapter,Contains,6)
      </li>
      <li>
        <i>Chapter &gt; 6. Scripts &amp; Formulas&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</i>(Chapter,Contains,7)
      </li>
      <li>
        <i>Chapter &gt; 6. Security&#160;&amp; Preferences&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</i>(Chapter,Contains,8)
      </li>
    </ol>
    <p>
      &#160;&#160;&#160;&#160;&#160;&#160;&#160;When filtering, check <b>filer conditions</b>:
    </p>
    <ul>
      <li>
        Show ancestors: <b>OFF</b>
      </li>
      <li>
        Show descendants: <b>OFF</b>
      </li>
      <li>
        Applies to filtered node <b>OFF</b>
      </li>
      <li>
        Unfold all descendants hidden after filtering: <b>ON</b>
      </li>
    </ul>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        Explanation of filtering in children of <i>1. My first mind map</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<cloud COLOR="#f0f0f0" SHAPE="ROUND_RECT"/>
<node TEXT="Content" ID="ID_1750213503" CREATED="1311482029110" MODIFIED="1311482095324" HGAP="50" VSHIFT="-10">
<font BOLD="true"/>
<edge STYLE="hide_edge"/>
</node>
<node TEXT="My first mind map" FOLDED="true" ID="ID_829890234" CREATED="1310822521013" MODIFIED="1312558785098" TEXT_SHORTENED="true" NUMBERED="true">
<attribute NAME="Chapter" VALUE="0"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Use filtering to show all nodes for getting started at GS1. For this, define and filter (Chapter,Contains,1):
    </p>
    <p>
      
    </p>
    <p>
      1. &#160;In the first thee fields of the filter menu bar select/enter:
    </p>
    <ul>
      <li>
        <i>Chapter</i>&#160;&#160;&#160;&#160;(&#160;N.B. Type C to scroll to menu item starting with C)
      </li>
      <li>
        <i>Contains</i>
      </li>
      <li>
        <i>1</i>
      </li>
    </ul>
    <p>
      2. Check filter conditions:
    </p>
    <ul>
      <li>
        short cut icon <i>Unfold hidden nodes after filtering </i><b>ON</b>
      </li>
      <li>
        short cut icon <i>Show ancestors </i><b>OFF</b>
      </li>
    </ul>
    <p>
      3. Press <i>Esc</i>&#160;followed by <i>Alt + Enter&#160;</i>to&#160;make visible all nodes
    </p>
    <p>
      
    </p>
    <p>
      4. Select short cut icon <i>Quick filter</i>&#160;to make appear all nodes of Chapter 1
    </p>
    <p>
      
    </p>
    <p>
      5 Move the cursor in turn over every node to make it show its content. Read the content.
    </p>
    <p>
      
    </p>
    <p>
      When finished:
    </p>
    <p>
      
    </p>
    <p>
      6. Select short cut icon <i>No Filtering </i>
    </p>
    <p>
      
    </p>
    <p>
      <i>7. </i>Press&#160;<i>Esc</i>&#160;followed by <i>Fold all nodes&#160;</i>to&#160; return to the initial view.
    </p>
    <p>
      
    </p>
    <p style="text-align: left">
      <b>See the nodes to the left for the complete procedure and the icon-buttons that can be used.</b>
    </p>
  </body>
</html></richcontent>
<node TEXT="Define filter rule" FOLDED="true" ID="ID_1670868929" CREATED="1310821479686" MODIFIED="1311402544217">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Procedure" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="0"/>
<node TEXT="Select: Chapter" ID="ID_1014969877" CREATED="1310820732631" MODIFIED="1311485344306" TEXT_SHORTENED="true">
<attribute NAME="Chapter" VALUE="0"/>
<hook NAME="FirstGroupNode"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        If you type C, the windows scrolls down to items beginning with C.
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="Select: Contains" ID="ID_1048644932" CREATED="1310820745993" MODIFIED="1311402544217">
<attribute NAME="Chapter" VALUE="0"/>
</node>
<node TEXT="Select/Enter: 1" ID="ID_48466550" CREATED="1310820789556" MODIFIED="1311485365460">
<attribute NAME="Chapter" VALUE="0" OBJECT="org.freeplane.features.format.FormattedNumber|0.0"/>
</node>
<node TEXT="Three entry fields in Toolbar" ID="ID_545396005" CREATED="1311317134192" MODIFIED="1311402544233">
<hook NAME="SummaryNode"/>
<attribute NAME="Chapter" VALUE="0"/>
</node>
</node>
<node TEXT="Unfold all nodes" FOLDED="true" ID="ID_1968664250" CREATED="1310821426631" MODIFIED="1311402544202">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Procedure" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="0"/>
<node TEXT="Select root node (press Esc)" ID="ID_179404412" CREATED="1310821238510" MODIFIED="1311402544202">
<hook NAME="FirstGroupNode"/>
<attribute NAME="Chapter" VALUE="0"/>
</node>
<node TEXT="Press: Unfold all descendants" ID="ID_1986038792" CREATED="1310821255419" MODIFIED="1311402544202">
<icon BUILTIN="../unfold_all"/>
<attribute NAME="Chapter" VALUE="0"/>
</node>
</node>
<node TEXT="Press Quick Filter" ID="ID_1699746394" CREATED="1310820822433" MODIFIED="1311402544233">
<icon BUILTIN="../apply_quick_filter"/>
<attribute NAME="Chapter" VALUE="0"/>
</node>
<node TEXT="Show Ancestors: Off" ID="ID_1477283067" CREATED="1310819865458" MODIFIED="1311402544186">
<icon BUILTIN="../show_ancestors"/>
<hook NAME="FirstGroupNode"/>
<attribute NAME="Chapter" VALUE="0"/>
</node>
<node TEXT="Check default filter conditions" FOLDED="true" ID="ID_1618746225" CREATED="1311316685082" MODIFIED="1311485080494">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Procedure" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="0"/>
<node TEXT="Show Descendants: Off" ID="ID_467302191" CREATED="1310819950485" MODIFIED="1311402544186">
<icon BUILTIN="../show_descendants"/>
<attribute NAME="Chapter" VALUE="0"/>
</node>
<node TEXT="Unfold all nodes hidden after filtering: On" ID="ID_178956245" CREATED="1310820023813" MODIFIED="1311402544186">
<icon BUILTIN="../unfold_filtered_ancestors"/>
<attribute NAME="Chapter" VALUE="0"/>
</node>
<node TEXT="Applies to filtered nodes: Off" ID="ID_588285753" CREATED="1310820191451" MODIFIED="1311402544202">
<icon BUILTIN="../applies_to_filtered_nodes"/>
<attribute NAME="Chapter" VALUE="0"/>
</node>
</node>
<node TEXT="See icon-buttons in Toolbar" ID="ID_1962045856" CREATED="1310821392950" MODIFIED="1311402544233">
<attribute NAME="Chapter" VALUE="0"/>
<hook NAME="SummaryNode"/>
</node>
<node TEXT="To end filter 1. My first mind map" FOLDED="true" ID="ID_1678666271" CREATED="1310821779644" MODIFIED="1311486465417">
<attribute NAME="Chapter" VALUE="1"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="Procedure" LAST="false"/>
</hook>
<font BOLD="true"/>
<node TEXT="Press: Undo filter" ID="ID_1343052095" CREATED="1310822117674" MODIFIED="1311402583935">
<icon BUILTIN="../remove_filtering"/>
<attribute NAME="Chapter" VALUE="1"/>
</node>
<node TEXT="Select root node (press Esc)" ID="ID_1663841014" CREATED="1310821238510" MODIFIED="1311402583951">
<hook NAME="FirstGroupNode"/>
<attribute NAME="Chapter" VALUE="1"/>
</node>
<node TEXT="Press: Fold all descendants" ID="ID_1495450969" CREATED="1310821255419" MODIFIED="1311402583951">
<icon BUILTIN="../fold_all"/>
<attribute NAME="Chapter" VALUE="1"/>
</node>
<node TEXT="Fold all nodes" ID="ID_1931349122" CREATED="1310821426631" MODIFIED="1311402583951">
<hook NAME="SummaryNode"/>
<attribute NAME="Chapter" VALUE="1"/>
</node>
</node>
</node>
<node TEXT="Handling nodes" FOLDED="true" ID="ID_1153833253" CREATED="1309552935597" MODIFIED="1311485855519" TEXT_SHORTENED="true" NUMBERED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Use filtering to show all nodes. For this, define and filter (Chapter,Contains,2):
    </p>
    <p>
      
    </p>
    <p>
      1. &#160;In the first thee fields of the filter menu bar select/enter:
    </p>
    <ul>
      <li>
        <i>Chapter</i>
      </li>
      <li>
        <i>Contains</i>
      </li>
      <li>
        <i>2</i>
      </li>
    </ul>
    <p>
      2. See the procedure of Chapter <i>1. My first mind map</i>
    </p>
  </body>
</html></richcontent>
<attribute NAME="Chapter" VALUE="0" OBJECT="org.freeplane.features.format.FormattedNumber|0.0"/>
<node TEXT="To end  filter 2. Handling nodes" FOLDED="true" ID="ID_37226247" CREATED="1310821779644" MODIFIED="1311486560328">
<attribute NAME="Chapter" VALUE="2"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="Procedure" LAST="false"/>
</hook>
<font BOLD="true"/>
<node TEXT="Press: Undo filter" ID="ID_1801742142" CREATED="1310822117674" MODIFIED="1311312926786">
<icon BUILTIN="../remove_filtering"/>
</node>
<node TEXT="Select root node (press Esc)" ID="ID_44167237" CREATED="1310821238510" MODIFIED="1311402755005">
<hook NAME="FirstGroupNode"/>
<attribute NAME="Chapter" VALUE="2"/>
</node>
<node TEXT="Press: Fold all descendants" ID="ID_1953731339" CREATED="1310821255419" MODIFIED="1311402755005">
<icon BUILTIN="../fold_all"/>
<attribute NAME="Chapter" VALUE="2"/>
</node>
<node TEXT="Fold all nodes" ID="ID_329795336" CREATED="1310821426631" MODIFIED="1311402755005">
<hook NAME="SummaryNode"/>
<attribute NAME="Chapter" VALUE="2"/>
</node>
</node>
</node>
<node TEXT="Node features" ID="ID_1790476268" CREATED="1311316250037" MODIFIED="1311486045059" TEXT_SHORTENED="true" NUMBERED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To see the nodes of this chapter,&#160;&#160;filter (Chapter,Contains,3).
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>1. My first mind map</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<attribute NAME="Chapter" VALUE="0" OBJECT="org.freeplane.features.format.FormattedNumber|0.0"/>
</node>
<node TEXT="Format &amp; Style" ID="ID_1115783310" CREATED="1311337025606" MODIFIED="1312558844332" TEXT_SHORTENED="true" NUMBERED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To see the nodes of this chapter, filter (Chapter,Contains,4).
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>1. My first mind map</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<attribute NAME="Chapter" VALUE="1"/>
</node>
<node TEXT="Date, Time &amp; Reminder" ID="ID_13139289" CREATED="1311333932581" MODIFIED="1312558867997" TEXT_SHORTENED="true" NUMBERED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To see the nodes of this chapter, filter (Chapter,Contains,5).
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>1. My first mind map</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<attribute NAME="Chapter" VALUE="5"/>
</node>
<node TEXT="Moving &amp; Publishing maps" ID="ID_70860166" CREATED="1311343174255" MODIFIED="1312558886982" NUMBERED="true" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To see the nodes of this chapter, filter (Chapter,Contains,6).
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>1. My first mind map</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="Scripts &amp; Formulas" ID="ID_1425593168" CREATED="1311343136286" MODIFIED="1312558897980" NUMBERED="true" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To see the nodes of this chapter, filter (Chapter,Contains,7).
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>1. My first mind map</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<attribute NAME="Chapter" VALUE="7"/>
</node>
<node TEXT="Security &amp; Preferences" ID="ID_1563209179" CREATED="1311337201372" MODIFIED="1312558910320" TEXT_SHORTENED="true" NUMBERED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To see the nodes of this chapter, filter (Chapter,Contains,8).
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>1. My first mind map</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<attribute NAME="Chapter" VALUE="8"/>
</node>
</node>
<node TEXT="Format" FOLDED="true" POSITION="right" ID="ID_258919699" CREATED="1266240683718" MODIFIED="1311405450276" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To change the appearance of a node. Select <i>View &gt; Properties Panel</i>&#160;to display most of the properties that can be changed. Check the property you want to change and enter a new value to change it. Select <i>View &gt; Properties Panel</i>&#160;again to close the panel. Alternatively properties can be changed through menu <i>Format</i>. For example, to change the text colour of the node, choose <i>Format &gt; Change node color</i>&#160;or press <i>Alt+Shift+F</i>.
    </p>
    <p>
      
    </p>
    <p>
      Properties that can be changed in the <i>Properties panel</i>&#160;are:
    </p>
    <ul>
      <li>
        <b>Node shape:</b>&#160;The appearance of the node: without surrounding box (Fork), with surrounding box (Bubble), as parent or combined.
      </li>
    </ul>
    <ul>
      <li>
        <b>Node Font:</b>&#160;the Font family, size, bold and italic.
      </li>
      <li>
        <b>Node colors:</b>&#160;The color of Text or Background. For hiding an edge, see below.
      </li>
    </ul>
    <ul>
      <li>
        <b>Edge color:</b>&#160;The color of the edge connecting nodes.
      </li>
      <li>
        <b>Edge style:</b>&#160;The way an edge bends.
      </li>
      <li>
        <b>Edge width:</b>&#160;The thickness of the edge.
      </li>
    </ul>
    <ul>
      <li>
        <b>Cloud color:</b>&#160;background color of cloud.
      </li>
      <li>
        <b>Cloud Shape:</b>&#160;Star, Rectangle or Round rectangle form.
      </li>
    </ul>
    <p>
      You can also have properties automatically set. Check in <i>Properties Panel</i>:
    </p>
    <ul>
      <li>
        <b>Automatic Layout:</b>&#160;automatically gives all nodes on a particualr hierarchical level a unique appearance. This automated appearance can be changed in Menu Styles.
      </li>
      <li>
        <b>Automatic edge color:</b>&#160;automatically selects a different edge color each time a new node is made.
      </li>
    </ul>
    <p>
      In addition you can set also the following properties in <i>Menu Format</i>:
    </p>
    <ul>
      <li>
        <b>Blinking node:</b>&#160;node which cycles through a number of colors.
      </li>
      <li>
        <b>Blend color</b>
      </li>
      <li>
        <b>Map background:</b>&#160;color of background.
      </li>
      <li>
        <b>Edge styles &gt; Hide edge:</b>&#160;Makes edge invisible.
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MainMenu" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
<node TEXT="Bubble" ID="ID_1235732708" CREATED="1266251659609" MODIFIED="1310882979565" STYLE="bubble" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000">Surround the inner area of the node with a round-cornered Bubble. The Bubble format has been applied to this node. The alternative is Fork format.</font>
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>Format &gt;</i>&#160;<i>Fork.</i>
      </li>
      <li>
        <i>View &gt; Properties Panel &gt; Bubble/Fork </i>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="FirstGroupNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Fork" ID="ID_401265529" CREATED="1266251650156" MODIFIED="1310882979862" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">Undo set Bubble and show a horizontal line below the node's content. This is the default format, and it is used for this node and for most of the other nodes in this document. The alternative is Bubble format.</span></font>
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>Format &gt; Bubble</i>
      </li>
      <li>
        <i>View &gt; Properites Panel &gt; Fork / Bubble</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Bubble central node area" ID="ID_5209411" CREATED="1310105269631" MODIFIED="1310196222543" TEXT_SHORTENED="true">
<hook NAME="SummaryNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>View &gt; Properties Panel</i>
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="Bold" ID="ID_25890496" CREATED="1266251692562" MODIFIED="1310882979846" TEXT_SHORTENED="true">
<icon BUILTIN="../Bold16"/>
<font NAME="SansSerif" SIZE="12" BOLD="true" ITALIC="false"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Changes font to Bold in whole basic node and Details.
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        Icon for Bold
      </li>
      <li>
        Exception
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="FirstGroupNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Italic" ID="ID_408787243" CREATED="1266251689531" MODIFIED="1310882979815" TEXT_SHORTENED="true">
<icon BUILTIN="../Italic16"/>
<font ITALIC="true"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Changes font to Italic in whole basic node and Details.
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        Icon for Italic
      </li>
      <li>
        Exception
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Larger font" ID="ID_349364472" CREATED="1266251662328" MODIFIED="1311405450323" TEXT_SHORTENED="true">
<icon BUILTIN="../IncreaseNodeFontAction"/>
<font NAME="SansSerif" SIZE="14" BOLD="false" ITALIC="false"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Increases font&#160;&#160;size in whole basic node and Details.
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        Font size in menu bar
      </li>
      <li>
        Exception
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
</node>
<node TEXT="Smaller font" ID="ID_533544086" CREATED="1266251685593" MODIFIED="1311405450386" TEXT_SHORTENED="true">
<icon BUILTIN="../DecreaseNodeFontAction"/>
<font SIZE="10"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Decreases font&#160;&#160;size in whole basic node and Details.
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        Font size in menu bar
      </li>
      <li>
        Exception
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
</node>
<node ID="ID_863447007" CREATED="1310105197652" MODIFIED="1311405450432" TEXT_SHORTENED="true">
<richcontent TYPE="NODE">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Font <b>whole</b>&#160;basic node text
    </p>
  </body>
</html></richcontent>
<hook NAME="SummaryNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      These actions effect the whole basic node text. and overrule WYSIWYG formatting in the edit windows and in the context menu of the inline editor. The action initially affects Details, but can be refined/undone by formatting in the edit dialog.
    </p>
    <p>
      
    </p>
    <p>
      It was originally meant for easy, fast formatting. However, the formatting with the inline editor context menu makes these functions more or less obsolete.
    </p>
    <p>
      
    </p>
    <p>
      To undo, select <i>Format &gt; Remove Format</i>.
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>Edit &gt; Copy format </i>
      </li>
      <li>
        <i>Edit &gt; Paste forma</i>t
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="Node color" ID="ID_1085935601" CREATED="1266251694000" MODIFIED="1310882979737" COLOR="#ff3366" TEXT_SHORTENED="true">
<icon BUILTIN="../NodeColorAction"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">Sets the foreground color of the selected node(s) using a color chooser dialog. The color is applied to all text in the node.</span></font><font color="#333333">.</font>
    </p>
  </body>
</html></richcontent>
<hook NAME="FirstGroupNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Blinking node" FOLDED="true" ID="ID_854971495" CREATED="1266252279859" MODIFIED="1311405450479" COLOR="#663300" BACKGROUND_COLOR="#ffffff" TEXT_SHORTENED="true">
<icon BUILTIN="../xeyes"/>
<edge COLOR="#000000"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">Cause the node to blink. The node cycles through different colors while blinking.</span></font>
    </p>
  </body>
</html></richcontent>
<hook NAME="accessories/plugins/BlinkingNodeHook.properties"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
<node TEXT="" ID="ID_1509903999" CREATED="1310113382439" MODIFIED="1310196222449" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      This property is not available in <i>View &gt; Properties Panel</i>
    </p>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Blend color" FOLDED="true" ID="ID_1238551182" CREATED="1266251704359" MODIFIED="1310882979690" COLOR="#663300" TEXT_SHORTENED="true">
<icon BUILTIN="../NodeColorBlendAction"/>
<font NAME="SansSerif" SIZE="12" BOLD="false" ITALIC="false"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">Changes the foreground color of the selected node(s) to be closer to its background color. May be applied multiple times. Can be reset by choosing Format &gt; Node Color... &gt; Use default.</span></font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<node TEXT="" ID="ID_1316784379" CREATED="1310113382439" MODIFIED="1310196222449" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      This property is not available in <i>View &gt; Properties Panel</i>
    </p>
  </body>
</html></richcontent>
</node>
</node>
<node ID="ID_1370207590" CREATED="1310105407161" MODIFIED="1311405450526" TEXT_SHORTENED="true">
<richcontent TYPE="NODE">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Color <b>whole</b>&#160;basic node text
    </p>
  </body>
</html></richcontent>
<hook NAME="SummaryNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      These actions effect the whole basic node text. and overrule WYSIWYG formatting in the edit windows and in the context menu of the inline editor.
    </p>
    <p>
      
    </p>
    <p>
      It was meant for easy, fast formatting. However, the formatting with the inline editor context menu makes these functions more or less obsolete.
    </p>
    <p>
      
    </p>
    <p>
      To undo, select <i>Format &gt; Remove Format</i>.
    </p>
    <p>
      
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>Edit &gt; Copy format </i>
      </li>
      <li>
        <i>Edit &gt; Paste forma</i>t
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="Node Background Color..." ID="ID_1529197837" CREATED="1266252194281" MODIFIED="1310882979675" COLOR="#663300" BACKGROUND_COLOR="#ffcc00" TEXT_SHORTENED="true">
<icon BUILTIN="../NodeBackgroundColorAction"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif">Sets the background color of the selected node(s) using a color chooser dialog. Formatting will apply to the entire node. </font>
    </p>
    <p>
      
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>Edit &gt; Copy format </i>
      </li>
      <li>
        <i>Edit &gt; Paste forma</i>t
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="FirstGroupNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Map Background" FOLDED="true" ID="ID_1564352042" CREATED="1266252207296" MODIFIED="1311405450557" TEXT_SHORTENED="true">
<icon BUILTIN="../MapBackgroundColorAction"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">Sets the background color of the entire map using a color chooser dialog.</span></font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
<node TEXT="" ID="ID_307696782" CREATED="1310109164393" MODIFIED="1310196222387" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      This option is not available in <i>View &gt; Properties Panel</i>
    </p>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Background color" ID="ID_608903890" CREATED="1310105995582" MODIFIED="1311405450588" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
<hook NAME="SummaryNode"/>
</node>
<node TEXT="Edge styles" FOLDED="true" ID="ID_1001249442" CREATED="1266252322375" MODIFIED="1310882979659" TEXT_SHORTENED="true">
<hook NAME="FirstGroupNode"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">Opens dialog to select an edge style. Options are:</span></font>
    </p>
    <ul>
      <li>
        <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">As Parent</span></font>
      </li>
      <li>
        <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">Linear</span></font>
      </li>
      <li>
        <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">Bezier</span></font>
      </li>
      <li>
        <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">Sharp Linear</span></font>
      </li>
      <li>
        <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">Sharp Bezier</span></font>
      </li>
      <li>
        <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">Horizontal</span></font>
      </li>
      <li>
        <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">Hide Edge</span></font>
      </li>
    </ul>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>View &gt; Properties Panel</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<node TEXT="" FOLDED="true" ID="ID_831160653" CREATED="1288536745875" MODIFIED="1310196222340" STYLE="bubble" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Example" LAST="false"/>
</hook>
<node TEXT="As parent" ID="ID_196205281" CREATED="1310115852702" MODIFIED="1310196222340" TEXT_SHORTENED="true"/>
<node TEXT="Linear" ID="ID_479844675" CREATED="1288538234095" MODIFIED="1310196222325" HGAP="30" TEXT_SHORTENED="true">
<edge STYLE="linear" WIDTH="4"/>
</node>
<node TEXT="Bezier curve" ID="ID_622749170" CREATED="1288538240451" MODIFIED="1310196222325" HGAP="40" TEXT_SHORTENED="true">
<edge STYLE="bezier" WIDTH="4"/>
</node>
<node TEXT="Sharp Linear" ID="ID_1385668433" CREATED="1288538242377" MODIFIED="1310196222309" HGAP="40" TEXT_SHORTENED="true">
<edge STYLE="sharp_linear" WIDTH="4"/>
</node>
<node TEXT="Sharp Bezier" ID="ID_925061428" CREATED="1288538470332" MODIFIED="1310196222309" HGAP="40" TEXT_SHORTENED="true">
<edge STYLE="sharp_bezier" WIDTH="4"/>
</node>
<node TEXT="Horizontal" ID="ID_1255718418" CREATED="1310115825254" MODIFIED="1310196222309" TEXT_SHORTENED="true">
<edge STYLE="horizontal"/>
</node>
<node TEXT="Hide Edge" ID="ID_846030610" CREATED="1288538507390" MODIFIED="1310196222293" BACKGROUND_COLOR="#00ffff" HGAP="50" TEXT_SHORTENED="true">
<edge STYLE="hide_edge" WIDTH="4"/>
</node>
</node>
</node>
<node TEXT="Edge Width" ID="ID_1718207662" CREATED="1266252326750" MODIFIED="1310882979643" TEXT_SHORTENED="true">
<font NAME="SansSerif" SIZE="12" BOLD="false" ITALIC="false"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">Opens dialog to select the style to be applied to the selected node(s). </span></font>
    </p>
    <p>
      
    </p>
    <p>
      <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">Options are: </span></font>
    </p>
    <ul>
      <li>
        <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">Parent</span></font>
      </li>
      <li>
        <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">Thin</span></font>
      </li>
      <li>
        <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">1</span></font>
      </li>
      <li>
        <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">2</span></font>
      </li>
      <li>
        <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">4</span></font>
      </li>
      <li>
        <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">8</span></font>
      </li>
    </ul>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">The difference between &quot;Thin&quot; and &quot;1&quot; may not be apparent at lower zoom percentages.</span></font>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Edge color" ID="ID_865389195" CREATED="1266252317187" MODIFIED="1310882979628" TEXT_SHORTENED="true">
<edge COLOR="#ff0000"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">Sets the color of the fork (or bubble) of the selected node(s) using a color chooser dialog. Also sets the color of the connectors between parent and child nodes.</span></font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Automatic edge color" ID="ID_1587417548" CREATED="1291194199529" MODIFIED="1310882979628" TEXT_SHORTENED="true">
<icon BUILTIN="../AutomaticEdgeColorHookAction"/>
<font NAME="SansSerif" SIZE="12" BOLD="false" ITALIC="false"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">See <i>Styles &gt; Automatic Layout.</i></span></font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Edge properties" STYLE_REF="Aggregatie als Toelichting" ID="ID_881523974" CREATED="1305835940519" MODIFIED="1311946117727" TEXT_SHORTENED="true">
<hook NAME="SummaryNode"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      An edge is a lineage between parent and child. The flow of the edge can be altered by clicking on it and drawing it to a different location. Technically an edge belongs to the child and edge properties like color can be changed in the child node. See <i>View &gt; Properties panel. </i>An edge can be hidden, see Egde style.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
</node>
<node TEXT="Cloud [New cloud]" ID="ID_649516172" CREATED="1310106596579" MODIFIED="1311405450604" TEXT_SHORTENED="true">
<icon BUILTIN="../Cloud24"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">Places a cloud around all of the selected nodes. </span></font>
    </p>
    <p>
      
    </p>
    <p>
      <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">This is useful for highlighting certain nodes. The cloud covers the node selected and all of its child nodes. Multiple clouds can be created at the same time by holding the Ctrl key (for selecting discreet nodes), or the Shift key for selecting all the nodes between the first node selected and the last. SEE ALSO: Format &gt; Cloud Color...</span></font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
</node>
<node TEXT="Cloud color" ID="ID_1092192366" CREATED="1266252268093" MODIFIED="1310882979628" TEXT_SHORTENED="true">
<icon BUILTIN="../Colors24"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000">Changes the color of the cloud. </font>
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        &#160;<i>Format&#160;&gt; Cloud</i>.
      </li>
      <li>
        <i>Properties Panel &gt; Cloud</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Cloud [Cloud style]" ID="ID_759607649" CREATED="1291193913924" MODIFIED="1310882979612" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Cloud properties" STYLE_REF="Aggregatie als Toelichting" ID="ID_1759348470" CREATED="1305837722596" MODIFIED="1310196222215" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<hook NAME="SummaryNode"/>
</node>
<node TEXT="Remove Format" ID="ID_640761139" CREATED="1291193542440" MODIFIED="1311405450604" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Restores format to standar style
    </p>
    <p>
      
    </p>
    <p>
      <b>See alo</b>
    </p>
    <ul>
      <li>
        Work bar <i>Default</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
</node>
</node>
<node TEXT="Navigate" FOLDED="true" POSITION="right" ID="ID_1224083972" CREATED="1310134532663" MODIFIED="1311402671170" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MainMenu" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="1,2,6"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To move between maps and nodes and to fold nodes.
    </p>
  </body>
</html></richcontent>
<node TEXT="Previous Map" ID="ID_1925064048" CREATED="1310134532663" MODIFIED="1311402807499" TEXT_SHORTENED="true">
<icon BUILTIN="../MoveTo_PrevMM"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">Moves to the previous map listed under the Maps menu. This only works if multiple maps are open.</span></font>
    </p>
  </body>
</html></richcontent>
<hook NAME="FirstGroupNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2.6"/>
</node>
<node TEXT="Next Map" ID="ID_1576684136" CREATED="1310134532663" MODIFIED="1311402807562" TEXT_SHORTENED="true">
<icon BUILTIN="../MoveTo_NextMM"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">Moves to the next map listed under the Maps menu. This only works if multiple maps are open.</span></font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2.6"/>
</node>
<node TEXT="Move to map" ID="ID_913517126" CREATED="1310828960234" MODIFIED="1311402807608">
<hook NAME="SummaryNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2.6"/>
</node>
<node TEXT="Center selected node" FOLDED="true" ID="ID_1435118166" CREATED="1310134532663" MODIFIED="1311402755021" TEXT_SHORTENED="true">
<icon BUILTIN="../CenterSelectedNodeAction"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Brings the selected node to the centre of the screen, moving the surrounding nodes with it.
    </p>
    <p>
      
    </p>
    <p>
      <b>Short cut</b>
    </p>
    <ul>
      <li>
        <i>Ctrl + Alt + C</i>
      </li>
      <li>
        icon-button
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
<node TEXT="Scroll/move map" ID="ID_1548435797" CREATED="1311407287938" MODIFIED="1311410621214" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="Procedure" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="12" OBJECT="org.freeplane.features.format.FormattedNumber|12.0"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To move the whole map, click in an empty part of the screen and drag the map, ore use the scrollbars at the sides of the screen.
    </p>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Toggle Folded" ID="ID_1370299836" CREATED="1310134532663" MODIFIED="1311402755067" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000">Toggles the nodes that are children of the selected node between being visible/hidden. </font>
    </p>
    <p>
      
    </p>
    <p>
      <b><font face="SansSerif, sans-serif" color="#000000">Short cut </font></b>
    </p>
    <ul>
      <li>
        <font face="SansSerif, sans-serif" color="#000000">Space</font>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="FirstGroupNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
</node>
<node TEXT="(Un)fold Children" ID="ID_383325209" CREATED="1310134532663" MODIFIED="1311402755099" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">This command toggles folding and unfolding nodes that are one child level removed from the node that is selected. As an example, below this node (that you are reading) is &quot;(Un)fold Children 2&quot;, and below &quot;Unfold Children 2&quot; are four nodes, &quot;Child 1&quot;, Child 2&quot;, Child 3&quot;, Child 4&quot;. From this node, using the &quot;Toggle Folded&quot; command, the node &quot;(Un)fold Children 2&quot; will toggle visible/hidden. Using the &quot;(Un)fold Children&quot; command, the nodes that are children of &quot;(Un)fold Children 2&quot; will toggle visible/hidden.</span></font>
    </p>
    <ul>
      <li>
        <span onclick="show_folder('1_1')" class="foldclosed" id="show1_1">+</span>&#160;<span onclick="hide_folder('1_1')" class="foldopened" id="hide1_1">-</span>&#160;<font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">(Un)fold Children 2</span></font>

        <ul id="fold1_1">
          <li>
            <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">Child 1</span></font>
          </li>
          <li>
            <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">Child 2</span></font>
          </li>
          <li>
            <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">Child 3</span></font>
          </li>
          <li>
            <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">Child 4</span></font>
          </li>
        </ul>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
</node>
<node TEXT="Unfold One Level" ID="ID_321295556" CREATED="1310134532663" MODIFIED="1311402755130" TEXT_SHORTENED="true">
<icon BUILTIN="../unfold_one_level"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">Progressively unfolds the descendants of the selected node. Clicking once unfolds the children, clicking again unfolds the grandchildren, etc.</span></font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
</node>
<node TEXT="Fold One Level" ID="ID_831251421" CREATED="1310134532679" MODIFIED="1311402755161" TEXT_SHORTENED="true">
<icon BUILTIN="../fold_one_level"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">Progressively folds the descendants of the selected node, starting at the deepest level in the hierarchy. If four levels exist, clicking once folds the great grandchildren, clicking again folds the grandchildren, clicking again folds the children, etc.</span></font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
</node>
<node TEXT="Unfold All" ID="ID_933325044" CREATED="1310134532679" MODIFIED="1311402583982" TEXT_SHORTENED="true">
<icon BUILTIN="../unfold_all"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000">Unfolds (makes visible) all nodes that are descendants of the selected node, including all children, grandchildren, etc. </font>
    </p>
    <p>
      
    </p>
    <p>
      <b>Hot key</b>
    </p>
    <ul>
      <li>
        press <i>Space; or</i>
      </li>
      <li>
        single click on the node; or
      </li>
      <li>
        click icon-button in <i>Toolbar</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="1"/>
</node>
<node TEXT="Fold All" ID="ID_657829947" CREATED="1310134532679" MODIFIED="1311402584013" TEXT_SHORTENED="true">
<icon BUILTIN="../fold_all"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000">Folds (hides) all nodes that are descendants of the selected node, including all children, grandchildren, etc. </font>
    </p>
    <p>
      
    </p>
    <p>
      <b>Hot key</b>
    </p>
    <ul>
      <li>
        press <i>Space; or</i>
      </li>
      <li>
        single click on the node; or
      </li>
      <li>
        click icon-button in Toolbar
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="1"/>
</node>
<node TEXT="Folding node(s)" ID="ID_101814873" CREATED="1310828754782" MODIFIED="1311402615712" TEXT_SHORTENED="true">
<hook NAME="SummaryNode"/>
<attribute_layout VALUE_WIDTH="202"/>
<attribute NAME="Chapter" VALUE="1,2"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>Folding</b>&#160;is hiding the descendants of a node. A small circle appears as an indicator of hidden descendants. Unfolding is making the descendants reappear.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Move to root  [Select root node]" ID="ID_1564476202" CREATED="1310134532679" MODIFIED="1311402584029" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif">Moves the selection focus to the root node (the parent of all other nodes). </font>
    </p>
    <p>
      
    </p>
    <p>
      <b>Short cut </b>
    </p>
    <ul>
      <li>
        press <i>Esc; or</i>
      </li>
      <li>
        single click root
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="FirstGroupNode"/>
<attribute_layout VALUE_WIDTH="156"/>
<attribute NAME="Chapter" VALUE="1"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Go to node with ID.. [Select node with ID...]" ID="ID_343872144" CREATED="1310191716143" MODIFIED="1311402755177" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Opens a dialog to enter the ID of a node and moves the selection focus to this node.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
</node>
<node TEXT="Previous node [Select previous node]" ID="ID_482454762" CREATED="1310134532679" MODIFIED="1311402755192" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000">Moves the selection focus to the previous node in the map's hierarchy. If the previous node is currently hidden, the map will be unfolded to display the node. </font>
    </p>
    <p>
      
    </p>
    <p>
      <b><font face="SansSerif, sans-serif" color="#000000">Short cut</font></b>
    </p>
    <ul>
      <li>
        <i>Ctrl + Alt + Left Arrow</i>.
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
</node>
<node TEXT="Next node [Select next node]" ID="ID_43445328" CREATED="1310134532679" MODIFIED="1311402755208" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000">Moves the selection focus to the next node in the map's hierarchy. If the next node is currently hidden, the map will be unfolded to display the node. </font>
    </p>
    <p>
      
    </p>
    <p>
      <b>Short cut</b>
    </p>
    <ul>
      <li>
        <i>Ctrl + Alt + Right Arrow</i>&#160;
      </li>
    </ul>
    <p>
      
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
</node>
<node TEXT="Previous node (fold) [Select previous node (fold)]" ID="ID_303327981" CREATED="1310134532679" MODIFIED="1311402755223" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000">Moves the selection focus to the previous node in the map's hierarchy and, if possible, hide the current node by folding.</font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
</node>
<node TEXT="Next node (fold) [Select next node (fold)]" ID="ID_230717042" CREATED="1310134532679" MODIFIED="1311402755239" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000">Move the selection focus to the next node in the map's hierarchy and, if possible, hide the current node by folding.</font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
</node>
<node TEXT="Back [Move selection back]" ID="ID_449527632" CREATED="1310134532679" MODIFIED="1311402755255" TEXT_SHORTENED="true">
<icon BUILTIN="../MoveTo_PrevNode"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000">Moves the selection focus to the node that was selected prior to the node that is currently selected. Each time this is used, the selection will continue to move through the history of previously-selected nodes.</font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
</node>
<node TEXT="Forward [Move selection forward]" ID="ID_602013651" CREATED="1310134532679" MODIFIED="1311402755255" TEXT_SHORTENED="true">
<icon BUILTIN="../MoveTo_NextNode"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">Retraces the nodes selected by using the Back command.</span></font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
</node>
<node TEXT="Selecting nodes" FOLDED="true" ID="ID_1414672331" CREATED="1310828830380" MODIFIED="1311402755255">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
<hook NAME="SummaryNode"/>
<node TEXT="Selecting nodes" FOLDED="true" ID="ID_1403801753" CREATED="1309423158240" MODIFIED="1311402615759" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To <b>select a node </b>is to bring it into focus and to <b>un-select a node </b>is to remove the focus. The node in focus is can be acted upon by menu functions.
    </p>
    <p>
      
    </p>
    <p>
      The currently selected node is highlighted by either a selection rectangle or a shaded node. The rectangle is the default. This can be changed in <i>View &gt; Rectangular selection</i>.
    </p>
    <p>
      
    </p>
    <p>
      To <b>un-select</b>&#160;a selected node(s), click outside the node, in an empty place of the background; or
    </p>
    <p>
      click on an other node, which wil be selected next.
    </p>
    <p>
      
    </p>
    <p>
      The node under the cursor is automatically selected. To <b>keep this selection</b>&#160;when moving around the mouse cursor, keep <i>Shift</i>&#160;&#160;pressed. If you dislike this behaviour, you can turn automatic selection off in menu <i>Tools &gt; Preferences</i>.
    </p>
    <p>
      
    </p>
    <p>
      Alternative ways of selecting a node or group of nodes are by pressing a special key or key combination.
    </p>
  </body>
</html></richcontent>
<attribute_layout NAME_WIDTH="82" VALUE_WIDTH="156"/>
<attribute NAME="Chapter" VALUE="1,2"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="Procedure" LAST="false"/>
</hook>
<node TEXT="" ID="ID_17827125" CREATED="1311313093106" MODIFIED="1311402615775" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="1,2"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To keep a selection when moving the cursor around, keep <i>Shift</i>&#160;pressed.
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        Tools &gt; Preferences to disable automatic selection
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Select First/last  sibling" STYLE_REF="Method" ID="ID_1705866199" CREATED="1309546732183" MODIFIED="1311402755270" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To select the first&#160;&#160;or last sibling of the currently selected node &#160;press <i>PgUp </i>respectively<i>&#160;PgDown. </i>
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>Navigate</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Procedure" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
</node>
<node TEXT="Select All Descendants" STYLE_REF="Method" FOLDED="true" ID="ID_228451346" CREATED="1309424361071" MODIFIED="1311402755286" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To select all descendants of a selected node:
    </p>
    <ul>
      <li>
        press <i>Alt + End </i>to&#160;&#160;unfold all hidden descendants
      </li>
      <li>
        press <i>Als + Shift + A </i>to select all visible descendants
      </li>
    </ul>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>Navigate</i>
      </li>
      <li>
        icon-buttons in the <i>Toolbar</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Procedure" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
<node TEXT=" Icon-button to show all descendants" ID="ID_1807802504" CREATED="1310803337034" MODIFIED="1311053879745">
<icon BUILTIN="../unfold_all"/>
</node>
<node TEXT="Icon-button to hide all descendants" ID="ID_70318547" CREATED="1310803411047" MODIFIED="1311053889011">
<icon BUILTIN="../fold_all"/>
</node>
</node>
<node TEXT="Select All nodes" STYLE_REF="Method" FOLDED="true" ID="ID_1432526079" CREATED="1309424361078" MODIFIED="1311402755301" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To select all nodes of a mind map:
    </p>
    <ul>
      <li>
        press <i>Esc</i>&#160;to select the root node
      </li>
      <li>
        press <i>Alt + End </i>to&#160;&#160;unfold all hidden descendants
      </li>
      <li>
        press <i>Als + Shift + A </i>to select all visible descendants
      </li>
    </ul>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>Navigate</i>
      </li>
      <li>
        icon-buttons in the <i>Toolbar</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Procedure" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
<node TEXT=" Icon-button to show all descendants" ID="ID_919487043" CREATED="1310803337034" MODIFIED="1311053879745">
<icon BUILTIN="../unfold_all"/>
</node>
<node TEXT="Icon-button to hide all descendants" ID="ID_389342260" CREATED="1310803411047" MODIFIED="1311053889011">
<icon BUILTIN="../fold_all"/>
</node>
</node>
<node TEXT="Select All filtered nodes" STYLE_REF="Method" ID="ID_1432270723" CREATED="1309424361092" MODIFIED="1311402755301" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To select all filtered nodes, see
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Procedure" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
</node>
<node TEXT="Select Group of siblings" STYLE_REF="Method" ID="ID_1664441735" CREATED="1309424361085" MODIFIED="1311402755317" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To select a group of siblings, click&#160;&#160;the first and last node while keeping <i>Shift</i>&#160;pressed.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Procedure" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
</node>
<node TEXT="Select Arbitrary nodes" STYLE_REF="Method" ID="ID_1374755186" CREATED="1309424361089" MODIFIED="1311402755333" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To select two or more arbitrary nodes, keep <i>Ctrl</i>&#160;pressed while clicking the nodes.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Procedure" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
</node>
<node TEXT="Select Using arrow keys" STYLE_REF="Method" ID="ID_81219304" CREATED="1309546028707" MODIFIED="1311402755333" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Use the arrow keys to move the selection focus from one node to another node. The focus moves in the direction of the arrows.
    </p>
    <p>
      
    </p>
    <p>
      <b>See also:</b>
    </p>
    <ul>
      <li>
        Main menu <i>Nodes</i>&#160;
      </li>
      <li>
        Main menu <i>Navigate</i>.
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Procedure" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
</node>
<node TEXT="With Hidden Text in Tool Tip" ID="ID_390018350" CREATED="1304485524408" MODIFIED="1311402755348" TEXT_SHORTENED="true" COLOR="#407000">
<font NAME="SansSerif" SIZE="12"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Hidden text shown in a Tool Tip may hide a sibling under the current node. To move to the node below and show its tool tip, use the down arrow key.
    </p>
    <p>
      
    </p>
    <p>
      To finish the appearance of the Too Tip, move the cursor up,&#160;&#160;above the node.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
</node>
</node>
<node TEXT="Follow Link" ID="ID_1084969591" CREATED="1310134532679" MODIFIED="1311184745576" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">If the node is hyperlinked, this command will execute the link. </span></font>
    </p>
    <p>
      
    </p>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif"><b>See also</b> </span></font>
    </p>
    <ul>
      <li>
        <i>Node features &gt; Hyperlink(Text Field)..</i>.
      </li>
    </ul>
    <p>
      
    </p>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Filter" STYLE_REF="MainMenu" FOLDED="true" POSITION="right" ID="ID_1254354349" CREATED="1266240693156" MODIFIED="1312143262764" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif">To display only a subset of nodes. Build and use filters with the Filter Toolbar or the Filter Menu. </font>
    </p>
    <p>
      
    </p>
    <p>
      <font color="#000000" face="SansSerif, sans-serif">Method<b>&#160;Quick filter</b>&#160;does the major filtering (other options are available).</font>
    </p>
    <p>
      
    </p>
    <p>
      <b>&#160;Filter rules</b>&#160;are build from thee elements:
    </p>
    <ul>
      <li>
        <i>Node <b>component</b></i>&#160;(Node Text, Details, Icon, etc.)
      </li>
      <li>
        <b><i>Type of rule: </i></b>&#160;component <i>exists, contains, =</i>, &gt; etc.
      </li>
      <li>
        <b><i>Value</i></b>&#160;of component
      </li>
      <li>
        If value is text, relevancy of <b><i>case</i></b>
      </li>
    </ul>
    <p>
      <b>Filter options</b>&#160;to set <b>before</b>&#160;applying <i>Quick filter </i>are:
    </p>
    <ul>
      <li>
        Apply to selected nodes only
      </li>
      <li>
        Apply to already filtered nodes (refine)
      </li>
    </ul>
    <p>
      <b>Filter option</b>s that can be set&#160;&#160;<b>before or after</b>&#160; applying <i>Quick filter</i>&#160;are:
    </p>
    <ul>
      <li>
        Unfold hidden nodes
      </li>
      <li>
        Show Ancestors
      </li>
      <li>
        Show Descendants
      </li>
      <li>
        Select all matching
      </li>
    </ul>
    <p>
      To return to the condition before filtering
    </p>
    <ul>
      <li>
        Unselect <i>No Filter</i>
      </li>
    </ul>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        Nodes hidden before the filter was applied, keep hidden.
      </li>
      <li>
        All nodes left visible move towards each other along the their edges.
      </li>
    </ul>
  </body>
</html></richcontent>
<attribute NAME="Chapter" VALUE="1"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MainMenu" LAST="false"/>
</hook>
<node TEXT="Undo filter" ID="ID_619416419" CREATED="1310134532663" MODIFIED="1310883132742" TEXT_SHORTENED="true">
<icon BUILTIN="../undo_filter"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000">Reverses the effect of the previously-used filtering command.</font>
    </p>
  </body>
</html></richcontent>
<hook NAME="FirstGroupNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Redo filter" ID="ID_1724296233" CREATED="1310134532663" MODIFIED="1310883132820" TEXT_SHORTENED="true">
<icon BUILTIN="../redo_filter"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000">Re-applies the previously-used filtering command, after Filter &gt; Undo has been used.</font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Reapply filter" ID="ID_1748016122" CREATED="1310134532663" MODIFIED="1310883132804" TEXT_SHORTENED="true">
<icon BUILTIN="../reapply_filter"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000">Updates the effect of filtering after changes have been made to the map.</font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Quick filter" FOLDED="true" ID="ID_1552136796" CREATED="1310134532663" MODIFIED="1312615201124" TEXT_SHORTENED="true">
<icon BUILTIN="../apply_quick_filter"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Hides all visible nodes which do not fit the filter rules and options. Icon keeps unset..
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>No Filter</i>&#160;to finish filtering.
      </li>
      <li>
        Tool Tip menu Filter (hover the cursor over menu Filter to see its hidden text !)
      </li>
    </ul>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        Nodes hidden before the filter was applied, keep hidden.
      </li>
      <li>
        All nodes left visible move towards each other along the their edges.
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="1"/>
<node TEXT="Find last modified nodes" ID="ID_1539236846" CREATED="1311098601253" MODIFIED="1311402755348" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="Procedure" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To filter for all nodes modified after a certain date, ser a filterrrule (Date filter<i>, Modified after, date, xxx)</i>&#160;and <i>Quick filter</i>&#160;for all modified nodes.
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>Find and replace all nodes...</i>&#160;for an alternative way.
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Filter selected nodes" ID="ID_402507556" CREATED="1310134532663" MODIFIED="1310883132789" TEXT_SHORTENED="true">
<icon BUILTIN="../filter_selected_nodes"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000">Display only the currently-selected node(s) and, optionally, their ancestors and/or descendants. </font>
    </p>
    <p>
      
    </p>
    <p>
      <b><font color="#000000" face="SansSerif, sans-serif">See also</font></b><font color="#000000" face="SansSerif, sans-serif">&#160;</font>
    </p>
    <ul>
      <li>
        <i><font color="#000000" face="SansSerif, sans-serif">Filter &gt; Show Ancestors</font> </i>
      </li>
      <li>
        <i><font color="#000000" face="SansSerif, sans-serif">Filter &gt; Show Descendants</font></i>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Select all matching" ID="ID_164487469" CREATED="1310134532663" MODIFIED="1310883132789" TEXT_SHORTENED="true">
<icon BUILTIN="../select_all_found_nodes"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Filter and select all matching descendants (visible and invisible) of the node in focus and show and select also the nodes in between.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="No filtering [Remove filtering]" ID="ID_1978139715" CREATED="1310134532663" MODIFIED="1311402584076" TEXT_SHORTENED="true">
<icon BUILTIN="../remove_filtering"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000">Removes the effect of any filters which were applied; this is the default. Un-sets icon.</font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="1"/>
</node>
<node TEXT="Edit filtering [Filter Composer]" FOLDED="true" ID="ID_1142884544" CREATED="1310134532663" MODIFIED="1312142144031" TEXT_SHORTENED="true">
<icon BUILTIN="../edit_filtering_condition"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000">Opens the Filter Composer dialog to create and edit filter rules / manage filters. </font>
    </p>
    <p>
      
    </p>
    <p>
      <font face="SansSerif, sans-serif" color="#000000">The first row contains three entries to define a filter rule, filled with (Node text, Contains, empty filed). and a check box for indicating if the Case of letters is relevant (<i>Match case</i>). The box below these editable fields contains the list of predefined rules. Initially it is empty. Two or more of these rules can be combined to form a more complex rule with AND and OR operators using the buttons to the right. A rule can be named with buttond Set Name. </font>
    </p>
    <p>
      
    </p>
    <p>
      <b><font face="SansSerif, sans-serif" color="#000000">Do</font></b>
    </p>
    <ol>
      <li>
        <font face="SansSerif, sans-serif" color="#000000">select or enter the conditions of the filter rule, e.g. (Node text, Contains, filter); Match Case unchecked.</font>
      </li>
      <li>
        press <i>Add </i>to add it to the list of rules.
      </li>
      <li>
        repeat for all rules you need.
      </li>
      <li>
        press OK
      </li>
    </ol>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        To combine two or more rules with AND or OR operators, select the rules and press AND resp. OR
      </li>
      <li>
        To connect a rule to a label, select the rule and press<i>&#160;Set Name</i>.
      </li>
    </ul>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>Styles &gt; Conditional Map styles</i>&#160;for example use of <i>Filter Composer </i>
      </li>
      <li>
        <i>Styles &gt; Conditional Node styles</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<node TEXT="First filter entry field" FOLDED="true" ID="ID_1625917093" CREATED="1312123492440" MODIFIED="1312142518057">
<node TEXT="Node Text (default)" ID="ID_440014122" CREATED="1312123267776" MODIFIED="1312123719962">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Parent Text" ID="ID_58128801" CREATED="1312123283057" MODIFIED="1312123720196">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Details" ID="ID_1976565775" CREATED="1312123289164" MODIFIED="1312123720181">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Icon" ID="ID_265220662" CREATED="1312123306722" MODIFIED="1312123720150">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Hyperlink" ID="ID_1229040721" CREATED="1312123310770" MODIFIED="1312123720118">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Connector label" ID="ID_725547789" CREATED="1312123356611" MODIFIED="1312123720103">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Connector" ID="ID_1614932096" CREATED="1312123365245" MODIFIED="1312123720087">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Date filter" ID="ID_1276079599" CREATED="1312123370370" MODIFIED="1312123720072">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Priority" ID="ID_30298697" CREATED="1312123376602" MODIFIED="1312123720056">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Node" ID="ID_863184223" CREATED="1312123395954" MODIFIED="1312123720040">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Style" ID="ID_1110522569" CREATED="1312123399644" MODIFIED="1312123720025">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Node level" ID="ID_1102698985" CREATED="1312123420696" MODIFIED="1312123720009">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Reminder" ID="ID_988583509" CREATED="1312123426101" MODIFIED="1312123720009">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Script filter" ID="ID_1339780088" CREATED="1312123431320" MODIFIED="1312123719994">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="and user defined styles..." ID="ID_1524254052" CREATED="1312123439299" MODIFIED="1312123719994">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Second filter entry field" FOLDED="true" ID="ID_387521533" CREATED="1312123543788" MODIFIED="1312142526029">
<node TEXT="Contains (default)" ID="ID_1227202337" CREATED="1312123550505" MODIFIED="1312123823391">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Is equal to" ID="ID_39134629" CREATED="1312123556784" MODIFIED="1312123823453">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Is not equal to" ID="ID_1364103252" CREATED="1312123568866" MODIFIED="1312123823437">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="&gt;" ID="ID_877198724" CREATED="1312123576922" MODIFIED="1312123823437">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="&gt;=" ID="ID_1015544739" CREATED="1312123587882" MODIFIED="1312123823422">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="&lt;=" ID="ID_1164055784" CREATED="1312123591711" MODIFIED="1312123823406">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="&gt;" ID="ID_1681290681" CREATED="1312123597631" MODIFIED="1312123823406">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Matches regexpr" ID="ID_91821609" CREATED="1312123615252" MODIFIED="1312123823406">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Third filter entry field" FOLDED="true" ID="ID_871096693" CREATED="1312123628941" MODIFIED="1312142534281">
<node TEXT="empty text field (default)" ID="ID_1473584730" CREATED="1312123652068" MODIFIED="1312143315102"/>
</node>
<node TEXT="Check box" FOLDED="true" ID="ID_191687274" CREATED="1312123857125" MODIFIED="1312142551707">
<node TEXT="Match case" ID="ID_939695594" CREATED="1312123867351" MODIFIED="1312123893606">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="UnChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Table with List of filters" ID="ID_1260531487" CREATED="1312142902004" MODIFIED="1312143438061"/>
<node TEXT="Buttons of Filter composer" FOLDED="true" ID="ID_1027349840" CREATED="1312142569809" MODIFIED="1312145303474">
<node TEXT="Add" ID="ID_1309133157" CREATED="1312142576136" MODIFIED="1312142620971"/>
<node TEXT="And" ID="ID_1757300074" CREATED="1312142621789" MODIFIED="1312142625752"/>
<node TEXT="Or" ID="ID_1457390872" CREATED="1312142626695" MODIFIED="1312142629988"/>
<node TEXT="Split" ID="ID_473058214" CREATED="1312142630728" MODIFIED="1312143344757" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To split a filter rule which was composed with AND and OR.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Delete" ID="ID_1607587118" CREATED="1312142647630" MODIFIED="1312142654339"/>
<node TEXT="Set Name" ID="ID_1779620539" CREATED="1312142655048" MODIFIED="1312142660259"/>
<node TEXT="Right" ID="ID_208287444" CREATED="1312142731636" MODIFIED="1312142968844">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<hook NAME="SummaryNode"/>
</node>
<node TEXT="OK" ID="ID_1874975203" CREATED="1312142849292" MODIFIED="1312142854644"/>
<node TEXT="Apply" ID="ID_1375530764" CREATED="1312142661031" MODIFIED="1312142698425"/>
<node TEXT="Cancel" ID="ID_23661885" CREATED="1312142699056" MODIFIED="1312142706358"/>
<node TEXT="Save" ID="ID_1878373607" CREATED="1312142707706" MODIFIED="1312142712231"/>
<node TEXT="Load" ID="ID_1746410510" CREATED="1312142713221" MODIFIED="1312142718073"/>
<node TEXT="Bottom" ID="ID_1789678903" CREATED="1312142752033" MODIFIED="1312143004256">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<hook NAME="SummaryNode"/>
</node>
</node>
<node TEXT="Filter Example" ID="ID_662270274" CREATED="1312144688348" MODIFIED="1312145254068" LINK="#_Freeplane_Link_784043927" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Example" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Click green arrow to move to an example.
    </p>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Filter actions" ID="ID_214063901" CREATED="1310496676053" MODIFIED="1310496718968">
<hook NAME="SummaryNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
</node>
<node TEXT="Applies to filtered nodes (refine)" ID="ID_1768599003" CREATED="1310134532663" MODIFIED="1310883132773" TEXT_SHORTENED="true">
<icon BUILTIN="../applies_to_filtered_nodes"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000">Toggle Apply to filtered (visible) nodes, i.e. add an additional filter condition, to further narrow the results.</font>
    </p>
  </body>
</html></richcontent>
<hook NAME="FirstGroupNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Unfold hidden nodes" ID="ID_865947666" CREATED="1310134532663" MODIFIED="1310883132773" TEXT_SHORTENED="true">
<icon BUILTIN="../unfold_filtered_ancestors"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000">Expand all nodes in a map that match the filter. This is not a toggle switch: once the nodes are expanded, they can only be collapsed using other commands (such as Filter &gt; Undo, which may be used immediately after this command). </font>
    </p>
    <p>
      
    </p>
    <p>
      <b><font color="#000000" face="SansSerif, sans-serif">See also</font></b><font color="#000000" face="SansSerif, sans-serif">&#160;</font>
    </p>
    <ul>
      <li>
        <font color="#000000" face="SansSerif, sans-serif">Navigate &gt; Toggle Folded</font>
      </li>
      <li>
        <font color="#000000" face="SansSerif, sans-serif">Navigate &gt; Fold All</font>
      </li>
      <li>
        <font color="#000000" face="SansSerif, sans-serif">Navigate &gt; Fold One Level.</font>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Show ancestors" ID="ID_1384592614" CREATED="1310134532663" MODIFIED="1311402584091" TEXT_SHORTENED="true">
<icon BUILTIN="../show_ancestors"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000">Toggles Display all ancestor nodes of nodes which match the filter when filtering is in effect (Applies to Filtered Nodes is set) </font>
    </p>
    <p>
      
    </p>
    <p>
      <b><font color="#000000" face="SansSerif, sans-serif">Note</font></b><font color="#000000" face="SansSerif, sans-serif">&#160;</font>
    </p>
    <ul>
      <li>
        <font color="#000000" face="SansSerif, sans-serif">Can be set before or after <i>Quick filter</i></font>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="1"/>
</node>
<node TEXT="Show descendants" ID="ID_347749612" CREATED="1310134532663" MODIFIED="1311402584091" TEXT_SHORTENED="true">
<icon BUILTIN="../show_descendants"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000">Togles Display all descendant nodes of nodes which match the filter when filtering is in effect (Applies to Filtered Nodes is set)</font>
    </p>
    <p>
      
    </p>
    <p>
      <b><font color="#000000" face="SansSerif, sans-serif">Note</font></b><font color="#000000" face="SansSerif, sans-serif">&#160;</font>
    </p>
    <ul>
      <li>
        <font color="#000000" face="SansSerif, sans-serif">Can be set before or after <i>Quick filter</i>&#160;has been issued.</font>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="1"/>
</node>
<node TEXT="Filter conditions / states" ID="ID_1103140596" CREATED="1310496750715" MODIFIED="1310496809027">
<hook NAME="SummaryNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
</node>
<node TEXT="Find previous" ID="ID_132872030" CREATED="1310134532663" MODIFIED="1310883132758" TEXT_SHORTENED="true">
<icon BUILTIN="../find_previous"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000">Find text or other criteria in the selected node. The searching direction is opposite to Find Next. </font>
    </p>
    <p>
      
    </p>
    <p>
      <b><font color="#000000" face="SansSerif, sans-serif">Note</font></b>
    </p>
    <ul>
      <li>
        The found node is selected.
      </li>
      <li>
        If the found node is folded, it is unfolded.
      </li>
      <li>
        If the found node is outside the screen, the mind map is shifted to bring it in the visible are of the screen.
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="FirstGroupNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Find next" ID="ID_1528431308" CREATED="1310134532663" MODIFIED="1310883132758" TEXT_SHORTENED="true">
<icon BUILTIN="../find_next"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000">Find text or other criteria in the selected node and all its descendant nodes. And if not found, search further in other branches. This is called depth first search.</font>
    </p>
    <p>
      
    </p>
    <p>
      <b><font color="#000000" face="SansSerif, sans-serif">Note</font></b>
    </p>
    <ul>
      <li>
        The found node is selected.
      </li>
      <li>
        If the found node is folded, it is unfolded.
      </li>
      <li>
        If the found node is outside the screen, the mind map is shirgted to bring it in the visible are of the screen.
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Finding / searching" ID="ID_721920269" CREATED="1310496838340" MODIFIED="1310496875468">
<hook NAME="SummaryNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Tools" STYLE_REF="MainMenu" FOLDED="true" POSITION="right" ID="ID_1833899525" CREATED="1266240695203" MODIFIED="1312614772334" TEXT_SHORTENED="true">
<attribute_layout NAME_WIDTH="37" VALUE_WIDTH="77"/>
<attribute NAME="Chapter" VALUE="6,7,8"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MainMenu" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To fit your Freeplane to your wishes and advanced features.
    </p>
  </body>
</html></richcontent>
<node TEXT="Sort children" ID="ID_1942706169" CREATED="1266247810468" MODIFIED="1311402755364" TEXT_SHORTENED="true">
<icon BUILTIN="../SortNodes"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <span style="color: #000000; font-family: SansSerif, sans-serif"><font color="#000000" face="SansSerif, sans-serif">Sorts all children of the selected node recursively, in descending alphanumeric order. The node hierarchy is preserved</font></span>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="2"/>
</node>
<node TEXT="Assign short cut" ID="ID_764902206" CREATED="1271849865669" MODIFIED="1311405939493" TEXT_SHORTENED="true">
<icon BUILTIN="../SetAcceleratorOnNextClickAction"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif">Allows assigning a hotkey combination to most menu commands, or reassigning the current hotkeys. </font>
    </p>
    <p>
      
    </p>
    <p>
      Opens a dialog to:
    </p>
    <ol>
      <li>
        select by navigating the menu item to connect to
      </li>
      <li>
        enter the key, key combination, or F-key
      </li>
    </ol>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>Tools &gt; Hot Key Presets &gt; Save presets</i>
      </li>
    </ul>
    <p>
      
    </p>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        Different sets can be saved for different uses
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="FirstGroupNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="8"/>
</node>
<node TEXT="Hot Key Presets" FOLDED="true" ID="ID_701336954" CREATED="1266248138750" MODIFIED="1311405939509">
<icon BUILTIN="../acceleratorPresets"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="SubMenu" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="8"/>
<node TEXT="Select hot key set" ID="ID_418449510" CREATED="1289732931429" MODIFIED="1311405939524" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Opens a list of previously saved hot key sets to choose from.
    </p>
    <p>
      
    </p>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        If there is no user defined set, none is shown.
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="8"/>
</node>
<node TEXT="Save hot key set" ID="ID_918316602" CREATED="1266248179781" MODIFIED="1311405939540" COLOR="#000000" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif">Oprens a dialog to save all the currently-defined hot key short-cuts to a dedicated file, which may be used to share the keystroke sets among multiple computers.The dialog prompts for the name of the file, which is automatically given the &quot;.properties&quot; extension. </font>
    </p>
    <p>
      
    </p>
    <p>
      <b><font color="#000000" face="SansSerif, sans-serif">Note</font></b>
    </p>
    <ul>
      <li>
        <font color="#000000" face="SansSerif, sans-serif">The file is created in the &quot;accelerators&quot; subdirectory of the User Directory.</font>
      </li>
    </ul>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        Tools &gt; Open User Directory
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="8"/>
</node>
<node TEXT="Remove hot key set" STYLE_REF="Functiegroep" ID="ID_978833755" CREATED="1293376890432" MODIFIED="1311405939540" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Select <i>Tools &gt; Open User directory</i>&#160;and delete the set name in directory <i>accelerators</i>.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="Procedure" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="8"/>
</node>
</node>
<node TEXT="Short cuts" FOLDED="true" ID="ID_1175856403" CREATED="1310739595312" MODIFIED="1311405939556">
<hook NAME="SummaryNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="8"/>
<node TEXT="Short cut or hot key" ID="ID_812201013" CREATED="1310823944773" MODIFIED="1311405939571" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="8"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      A <b>hot key</b>&#160;is a special key or key combination which can be used to trigger a menu item without having to navigate to the menu item.
    </p>
    <p>
      
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="F-keys" ID="ID_435839537" CREATED="1289732578947" MODIFIED="1311405939571" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      F-keys (F1 - F-12) are hot keys. See above for assigning, selecting and deleting.
    </p>
  </body>
</html></richcontent>
<attribute NAME="Chapter" VALUE="8"/>
</node>
<node TEXT="Reference card for special system keys" ID="ID_1818761339" CREATED="1286915483579" MODIFIED="1311405939587" TEXT_SHORTENED="true" LINK="#ID_82273720">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>See</b>
    </p>
    <ul>
      <li>
        <i>Help &gt; Key Reference</i>
      </li>
    </ul>
    <p>
      
    </p>
  </body>
</html></richcontent>
<attribute NAME="Chapter" VALUE="8"/>
</node>
</node>
<node TEXT="Execute selected node scripts" ID="ID_894921324" CREATED="1266247915812" MODIFIED="1311405964531" TEXT_SHORTENED="true">
<icon BUILTIN="../ExecuteScriptForSelectionAction"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <span style="color: #000000; font-family: SansSerif, sans-serif"><font color="#000000" face="SansSerif, sans-serif">Runs all scripts which are attached to the currently selected node(s).</font></span>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="7"/>
</node>
<node TEXT="Execute all scripts" ID="ID_1362957476" CREATED="1266248076859" MODIFIED="1311405964547" TEXT_SHORTENED="true">
<icon BUILTIN="../ExecuteScriptForAllNodes"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <span style="color: #000000; font-family: SansSerif, sans-serif"><font color="#000000" face="SansSerif, sans-serif">Runs all the scripts which are local to the current map, having been created using the Script Editor.</font></span>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="7"/>
</node>
<node TEXT="Script editor ..." ID="ID_1785517002" CREATED="1310737755450" MODIFIED="1311405964562">
<icon BUILTIN="../ScriptEditor"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <span style="color: #000000; font-family: SansSerif, sans-serif"><font color="#000000" face="SansSerif, sans-serif">Displays a window for creating scripts which become attached to individual nodes, and are saved local to the current map file.</font></span>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="7"/>
</node>
<node TEXT="Scripts" ID="ID_1890181858" CREATED="1267746871633" MODIFIED="1311405964578" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <span style="color: #000000; font-family: SansSerif, sans-serif"><font color="#000000" face="SansSerif, sans-serif">Displays a hierarchical menu of Groovy scripts which have been added to the user's scripts directory.</font></span>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="7"/>
</node>
<node TEXT="Formulas..." STYLE_REF="Functie" FOLDED="true" ID="ID_394104724" CREATED="1291201976731" MODIFIED="1311405964578" BACKGROUND_COLOR="#ffffff">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      In Freeplane there are 3 types of formulas:
    </p>
    <ul>
      <li>
        a formula in a text, written in LaTeX
      </li>
      <li>
        a simple formula as in a Spread Sheet (Excel) which is executed at once. This type of formula is written in the basic node text and starts with &quot;=&quot;, followed by a regular expression like 3 + 5. The node calculates and shows the result (8) in stead of the formula. This type of node can be recognized by a colored border.
      </li>
      <li>
        an advanced formula defined in Groovy script language.
      </li>
    </ul>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>View &gt; Don't mark formulas with a border</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="SubMenu" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="7"/>
<node TEXT="Evaluate all" STYLE_REF="Functie" ID="ID_1812283108" CREATED="1291201995803" MODIFIED="1310883758950">
<icon BUILTIN="../formula"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Scripts and formulas" FOLDED="true" ID="ID_1686912429" CREATED="1310739619133" MODIFIED="1312572823729" TEXT_SHORTENED="true" LINK="#ID_1528894906">
<hook NAME="SummaryNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="7"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To use scripts, preferences must be activated.
    </p>
    <p>
      
    </p>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        Click the green arrow to see the preferences.
      </li>
    </ul>
  </body>
</html></richcontent>
<node TEXT="Wiki Formulas" ID="ID_1130453106" CREATED="1310826300439" MODIFIED="1311405964594" LINK="http://freeplane.sourceforge.net/wiki/index.php/Formulas">
<attribute NAME="Chapter" VALUE="7"/>
</node>
<node TEXT="Wiki Groovy scripts" ID="ID_1846416316" CREATED="1310826176176" MODIFIED="1311405964594" LINK="http://freeplane.sourceforge.net/wiki/index.php/Scripting">
<attribute NAME="Chapter" VALUE="7"/>
</node>
</node>
<node TEXT="Open User directory" FOLDED="true" ID="ID_4259182" CREATED="1303933344973" MODIFIED="1311406257921">
<icon BUILTIN="../OpenUserDirAction"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="6,8"/>
<node TEXT="log files" ID="ID_338415854" CREATED="1310759742065" MODIFIED="1311353618620">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="directory-.backup" FOLDED="true" ID="ID_692458643" CREATED="1310759556908" MODIFIED="1310883473236" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      automatic backups
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
<node TEXT="" ID="ID_1708394985" CREATED="1310825788339" MODIFIED="1310825924293">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      If you lost your map, look here.
    </p>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="directory-templates" ID="ID_366822599" CREATED="1310759536089" MODIFIED="1311405837797" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Contains all styles, .mm format.
    </p>
    <p>
      
    </p>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        default.mm is the system file used with New map.
      </li>
      <li>
        You can exchange styles with other users here.
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="FirstGroupNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="directory-scripts" ID="ID_1775013886" CREATED="1310759542634" MODIFIED="1310883473236" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      user defined scripts
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="directory-icons" ID="ID_7040911" CREATED="1310759549880" MODIFIED="1311405837797" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Contains user defined icons.
    </p>
    <p>
      
    </p>
    <p>
      Note
    </p>
    <ul>
      <li>
        You can put your own icons here, or exchange icons with other users.
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="directory-xml" ID="ID_912917878" CREATED="1310885156896" MODIFIED="1310885260200" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Optional, to be added by the user.
    </p>
    <p>
      Used to store user defined menu structure.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="directory-accelerators" ID="ID_390138300" CREATED="1310825223726" MODIFIED="1311406257921" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Contains hot key sets.
    </p>
    <p>
      These can be exchanged between users.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="6,8"/>
</node>
<node TEXT="" ID="ID_877249660" CREATED="1310825608361" MODIFIED="1311406257921" TEXT_SHORTENED="true">
<hook NAME="SummaryNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="6,8"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      You can exchange icons, styles, short key sets with other users here.
    </p>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Preferences ..." FOLDED="true" ID="ID_827606855" CREATED="1310134532679" MODIFIED="1311487166576" TEXT_SHORTENED="true">
<icon BUILTIN="../PropertyAction"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      See the Menu Preferences for more details.
    </p>
    <p>
      
    </p>
    <p>
      The present node is used in Finding and Filtering only.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="SubMenu" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="1,3,4,6,7,8"/>
<node TEXT="Environment" FOLDED="true" ID="ID_1029291913" CREATED="1310756725581" MODIFIED="1311405939587">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="SubMenu" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="8"/>
<node TEXT="Open files in a running instance" FOLDED="true" ID="ID_637030087" CREATED="1310756756821" MODIFIED="1311924479311">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="UnChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
<node TEXT="" ID="ID_390598009" CREATED="1311706805194" MODIFIED="1311708348283">
<attribute_layout NAME_WIDTH="33" VALUE_WIDTH="137"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Avoid a second instance in any case" ID="ID_646231690" CREATED="1310756773801" MODIFIED="1311924479295">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="UnChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Single Program Instance" ID="ID_1182954985" CREATED="1310756739294" MODIFIED="1311483332938">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<hook NAME="SummaryNode"/>
</node>
<node TEXT="Language" FOLDED="true" ID="ID_922308916" CREATED="1310756795196" MODIFIED="1310756805899">
<node TEXT="Automatic" ID="ID_1563480497" CREATED="1311708441781" MODIFIED="1311708471843">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Last Opened List Length" FOLDED="true" ID="ID_980964669" CREATED="1310756813075" MODIFIED="1310756834338">
<node TEXT="25" ID="ID_707557773" CREATED="1311708478168" MODIFIED="1311708512364">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Automatically open last map" ID="ID_196332959" CREATED="1310756835047" MODIFIED="1311924479295">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="UnChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Load all last maps" ID="ID_1374498842" CREATED="1310756850593" MODIFIED="1311924592723">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="IsChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Load last and new maps" ID="ID_1415496133" CREATED="1310756870115" MODIFIED="1311924479280">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="UnChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Experimental File Locking" ID="ID_323193765" CREATED="1310756884507" MODIFIED="1311924479280">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="UnChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Standard Template File" FOLDED="true" ID="ID_1036795471" CREATED="1310756905403" MODIFIED="1310756919872">
<node TEXT="standard.mm" ID="ID_706234894" CREATED="1310756919873" MODIFIED="1311482969704">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Files" ID="ID_1889550390" CREATED="1310756806514" MODIFIED="1311483332953">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<hook NAME="SummaryNode"/>
</node>
<node TEXT="Time For Automatic Save" FOLDED="true" ID="ID_1187275639" CREATED="1310757398287" MODIFIED="1310757414635">
<node TEXT="60,000" ID="ID_1936163905" CREATED="1311710082273" MODIFIED="1311710111930">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Use single directory for backup files" ID="ID_1591228143" CREATED="1310757416031" MODIFIED="1311924479264">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="UnChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Use single directory for backup files" ID="ID_99034104" CREATED="1311710239748" MODIFIED="1311924479264">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="UnChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Backup directory if above is selected" ID="ID_76108740" CREATED="1310757444914" MODIFIED="1311710505479"/>
<node TEXT="Delete Automatic Saves At Exit" ID="ID_1981866727" CREATED="1310757462254" MODIFIED="1311924567521">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="IsChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="NumberOf Different Files For Automatic SAve" FOLDED="true" ID="ID_1665531613" CREATED="1310757485006" MODIFIED="1310757520045">
<node TEXT="10" ID="ID_205157037" CREATED="1311710082273" MODIFIED="1311710711041">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Number of kept backup files" FOLDED="true" ID="ID_862892627" CREATED="1310757520629" MODIFIED="1310757538788">
<node TEXT="2" ID="ID_1142741407" CREATED="1311710082273" MODIFIED="1311710744909">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Automatic Save" ID="ID_650602318" CREATED="1310757388200" MODIFIED="1311483332953">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<hook NAME="SummaryNode"/>
</node>
<node TEXT="Save folding" FOLDED="true" ID="ID_886253731" CREATED="1310757551768" MODIFIED="1311925279709" TEXT_SHORTENED="true">
<node TEXT="Never" ID="ID_689273752" CREATED="1311710082273" MODIFIED="1311710858729">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Always" ID="ID_702281311" CREATED="1311710082273" MODIFIED="1311710837513">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="If map is changed" ID="ID_1299958092" CREATED="1311710082273" MODIFIED="1311710921269">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Save modification times" ID="ID_1062887553" CREATED="1310757564052" MODIFIED="1310757575768"/>
<node TEXT="Save" ID="ID_619771458" CREATED="1310757542352" MODIFIED="1311925029243">
<hook NAME="SummaryNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
</node>
<node TEXT="On Load" FOLDED="true" ID="ID_6779259" CREATED="1310757608637" MODIFIED="1310757615408">
<node TEXT="Load from map or fold all" ID="ID_1843878856" CREATED="1311710082273" MODIFIED="1311711108181">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Load from map or unfold all" ID="ID_204010386" CREATED="1311710082273" MODIFIED="1311711145403">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Unfold all" ID="ID_98026410" CREATED="1311710082273" MODIFIED="1311711162984">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Maximum number of displayed nodes" FOLDED="true" ID="ID_1905038356" CREATED="1310757621062" MODIFIED="1310757637146">
<node TEXT="20" ID="ID_1759313322" CREATED="1311710082273" MODIFIED="1311711208708">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Charset" FOLDED="true" ID="ID_884107518" CREATED="1310757640710" MODIFIED="1310757655391">
<node TEXT="Default" ID="ID_198865739" CREATED="1311710082273" MODIFIED="1311711259423">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="..." ID="ID_1437522671" CREATED="1311710082273" MODIFIED="1311711289305">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Load" ID="ID_950384750" CREATED="1310757604557" MODIFIED="1311483332953">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<hook NAME="SummaryNode"/>
</node>
<node TEXT="Links" FOLDED="true" ID="ID_82833119" CREATED="1311711318523" MODIFIED="1311711328648">
<node TEXT="Relative" ID="ID_1185657954" CREATED="1311710082273" MODIFIED="1311711344919">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Absolute" ID="ID_252889669" CREATED="1311710082273" MODIFIED="1311711401321">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Hyperlink Types" ID="ID_1309284534" CREATED="1310757657738" MODIFIED="1311711469041">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<hook NAME="SummaryNode"/>
</node>
<node TEXT="Check for updates on program start" ID="ID_348083728" CREATED="1311483101532" MODIFIED="1311924533693">
<hook NAME="FirstGroupNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="IsChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Program Updates" ID="ID_1155437887" CREATED="1310757670366" MODIFIED="1311483332953">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<hook NAME="SummaryNode"/>
</node>
<node TEXT="Policy" FOLDED="true" ID="ID_938224301" CREATED="1311483188239" MODIFIED="1311483193597">
<node TEXT="Show report dialog" ID="ID_1017882911" CREATED="1311483193598" MODIFIED="1311483296605">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Always send" ID="ID_920549965" CREATED="1311483233541" MODIFIED="1311483296621">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Never send" ID="ID_1237652835" CREATED="1311483239570" MODIFIED="1311483296621">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Automatic bug report" ID="ID_849413397" CREATED="1310757685958" MODIFIED="1311483332953">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<hook NAME="SummaryNode"/>
</node>
</node>
<node TEXT="Appearance" FOLDED="true" ID="ID_186670407" CREATED="1266242716593" MODIFIED="1310883516932" COLOR="#000000">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="SubMenu" LAST="false"/>
</hook>
<node TEXT="Look and feel" FOLDED="true" ID="ID_702879709" CREATED="1311711679398" MODIFIED="1311711696357">
<node TEXT="Default" ID="ID_1366474327" CREATED="1311483239570" MODIFIED="1311711744124">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Nimbus" ID="ID_801561072" CREATED="1311483239570" MODIFIED="1311711781509">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Metal" ID="ID_1239129408" CREATED="1311483239570" MODIFIED="1311711797047">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="CDE/Motiv" ID="ID_1852646607" CREATED="1311483239570" MODIFIED="1311711894313">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Windows" ID="ID_1713648658" CREATED="1311483239570" MODIFIED="1311711847950">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Windows classic" ID="ID_850989941" CREATED="1311483239570" MODIFIED="1311711865422">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Use Tabs" ID="ID_678230606" CREATED="1310755710026" MODIFIED="1311924298928">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="IsChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Use default font for notes too" ID="ID_1971832225" CREATED="1310755787737" MODIFIED="1311924298928">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="IsChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Remove top margin for notes" ID="ID_136257105" CREATED="1310755814000" MODIFIED="1311924298912">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="IsChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Look and feel" ID="ID_1413754050" CREATED="1271849999714" MODIFIED="1311483736216">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<hook NAME="SummaryNode"/>
</node>
<node TEXT="Display node ID" ID="ID_1626584260" CREATED="1310755845061" MODIFIED="1311924437893">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="UnChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Status line" ID="ID_781487286" CREATED="1310755835707" MODIFIED="1311484239091">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<hook NAME="SummaryNode"/>
</node>
<node TEXT="Revision Color" FOLDED="true" ID="ID_665414546" CREATED="1310755901626" MODIFIED="1310755910206">
<node TEXT="Yellow background" ID="ID_17029124" CREATED="1311483239570" MODIFIED="1311712048831">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="White background for printing" ID="ID_1915067515" CREATED="1310755911352" MODIFIED="1311924298897">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="IsChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Standard Cloud Color" FOLDED="true" ID="ID_749503390" CREATED="1310755933481" MODIFIED="1310755947974">
<node TEXT="#f0f0f0" ID="ID_327342349" CREATED="1311483239570" MODIFIED="1311712152088">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Default Colors" ID="ID_415228347" CREATED="1310755860823" MODIFIED="1311483736232">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<hook NAME="SummaryNode"/>
</node>
<node TEXT="Display Selected Nodes in Bubbles" ID="ID_1439588791" CREATED="1310755963730" MODIFIED="1311924298897">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="IsChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Selected Node Bubble Color" FOLDED="true" ID="ID_224972929" CREATED="1310755988837" MODIFIED="1311369314642" LINK="#ID_922308916">
<node TEXT="#002080" ID="ID_1345958852" CREATED="1311483239570" MODIFIED="1311712219371">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Standard Selected Node Color" FOLDED="true" ID="ID_465580103" CREATED="1310756005584" MODIFIED="1310756019500">
<node TEXT="#d2d2d2" ID="ID_1819028753" CREATED="1311483239570" MODIFIED="1311712256499">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Selection Colors" ID="ID_1053593738" CREATED="1310755953441" MODIFIED="1311483736232">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<hook NAME="SummaryNode"/>
</node>
<node TEXT="Edges start from one point at root node" ID="ID_1368491675" CREATED="1310756047331" MODIFIED="1310756073117"/>
<node TEXT="Root node appearance" ID="ID_329983622" CREATED="1310756026277" MODIFIED="1311483736247">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<hook NAME="SummaryNode"/>
</node>
<node TEXT="Antialias" FOLDED="true" ID="ID_1542458909" CREATED="1310756074170" MODIFIED="1310756094701">
<node TEXT="Antialias Edges" ID="ID_1654348959" CREATED="1311483457645" MODIFIED="1311483550591">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
<hook NAME="FirstGroupNode"/>
</node>
<node TEXT="Antialias all" ID="ID_627671109" CREATED="1311483468403" MODIFIED="1311483542121">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="No Antialias" ID="ID_1212369653" CREATED="1311483477473" MODIFIED="1311483542121">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Antialias" ID="ID_1408904285" CREATED="1311483550591" MODIFIED="1311483736247">
<hook NAME="SummaryNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Max shortened node length" FOLDED="true" ID="ID_1747043647" CREATED="1310756107165" MODIFIED="1311483613709">
<hook NAME="FirstGroupNode"/>
<node TEXT="100" ID="ID_602426240" CREATED="1311483477473" MODIFIED="1311712335560">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Max Node Width" FOLDED="true" ID="ID_1131898915" CREATED="1310756120854" MODIFIED="1310756133194">
<node TEXT="600" ID="ID_1544215822" CREATED="1311483477473" MODIFIED="1311712370598">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="ToolTip Width" FOLDED="true" ID="ID_215853027" CREATED="1310756134699" MODIFIED="1311484565389">
<attribute NAME="Chapter" VALUE="1"/>
<node TEXT="600" ID="ID_1670353099" CREATED="1311483477473" MODIFIED="1311712383312">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Size Limits" ID="ID_1349141635" CREATED="1310756096393" MODIFIED="1311483736247">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<hook NAME="SummaryNode"/>
</node>
<node TEXT="Standard Link Color" FOLDED="true" ID="ID_1272643299" CREATED="1310756199330" MODIFIED="1310756210204">
<node TEXT="#000000" ID="ID_1523516109" CREATED="1311483477473" MODIFIED="1311712415276">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Opacity" FOLDED="true" ID="ID_1391515369" CREATED="1310756211084" MODIFIED="1310756217278">
<node TEXT="80" ID="ID_1305662372" CREATED="1311483477473" MODIFIED="1311712432186">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Connector shapes" FOLDED="true" ID="ID_266926804" CREATED="1310756227987" MODIFIED="1310756240156">
<node TEXT="Curve" ID="ID_53878188" CREATED="1311483477473" MODIFIED="1311712481904">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Line" ID="ID_210394121" CREATED="1311483477473" MODIFIED="1311712497878">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Linear path" ID="ID_972235482" CREATED="1311483477473" MODIFIED="1311712514570">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Simulate edge" ID="ID_441814953" CREATED="1311483477473" MODIFIED="1311712554015">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Width [Connector width]" FOLDED="true" ID="ID_1481643722" CREATED="1310756243034" MODIFIED="1310756268212">
<node TEXT="2" ID="ID_22643760" CREATED="1311483477473" MODIFIED="1311712592563">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Font Family [Connector Font Family]" FOLDED="true" ID="ID_1221903920" CREATED="1310756269499" MODIFIED="1310756303133">
<node TEXT="SansSerif" ID="ID_1484575894" CREATED="1311483477473" MODIFIED="1311712626867">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Font Size [Connector Font Size]" FOLDED="true" ID="ID_728681381" CREATED="1310756305651" MODIFIED="1310756331502">
<node TEXT="12" ID="ID_589330080" CREATED="1311483477473" MODIFIED="1311712644152">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Connectors" ID="ID_1984458992" CREATED="1310756191506" MODIFIED="1311483736263">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<hook NAME="SummaryNode"/>
</node>
<node TEXT="Buttons at the top" ID="ID_1376964587" CREATED="1310756385375" MODIFIED="1311924369526">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="UnChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Position Window Below Node" ID="ID_1576279776" CREATED="1310756348428" MODIFIED="1311924298881">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="IsChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Min Default Window Height" FOLDED="true" ID="ID_1734781250" CREATED="1310756407465" MODIFIED="1310756423846">
<node TEXT="150" ID="ID_1551290999" CREATED="1311483477473" MODIFIED="1311712739718">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Max Default Window Height" FOLDED="true" ID="ID_720944351" CREATED="1310756426130" MODIFIED="1310756447285">
<node TEXT="600" ID="ID_1973666685" CREATED="1311483477473" MODIFIED="1311712754904">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Min Default Window Width" FOLDED="true" ID="ID_741939779" CREATED="1310756407465" MODIFIED="1310756512384">
<node TEXT="400" ID="ID_164780892" CREATED="1311483477473" MODIFIED="1311712766690">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Max Default Window Width" FOLDED="true" ID="ID_848971421" CREATED="1310756426130" MODIFIED="1310756524630">
<node TEXT="900" ID="ID_1286106720" CREATED="1311483477473" MODIFIED="1311712779037">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Edit Long Node Window" ID="ID_991611253" CREATED="1310756332632" MODIFIED="1311483736263">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<hook NAME="SummaryNode"/>
</node>
<node TEXT="vertical distance" FOLDED="true" ID="ID_1935836042" CREATED="1310756536143" MODIFIED="1310756546486">
<node TEXT="5" ID="ID_1567487763" CREATED="1311483477473" MODIFIED="1311712789029">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="horizontal distance" FOLDED="true" ID="ID_1853135324" CREATED="1310756547226" MODIFIED="1310756556041">
<node TEXT="15" ID="ID_1756020932" CREATED="1311483477473" MODIFIED="1311712804013">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Outline view" ID="ID_1466344690" CREATED="1310756525838" MODIFIED="1311483736263">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<hook NAME="SummaryNode"/>
</node>
<node TEXT="Structured icon toolbar" ID="ID_23497406" CREATED="1310756571579" MODIFIED="1311924402668">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="UnChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Don&apos;t show the note icons" ID="ID_635307234" CREATED="1310756586125" MODIFIED="1311924298881">
<attribute NAME="Chapter" VALUE="3"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="IsChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Show/Hide Icon For Attributes" FOLDED="true" ID="ID_1228898153" CREATED="1310756602810" MODIFIED="1311924185648">
<attribute NAME="Chapter" VALUE="3"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="UnChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
<node TEXT="" ID="ID_1610400762" CREATED="1311923205552" MODIFIED="1311923312554" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      
    </p>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        In <i>Java Applet</i>&#160;attribute icons are displayed, whatever the setting.
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Icons" ID="ID_1409698218" CREATED="1310756566609" MODIFIED="1311484513191">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="3"/>
<hook NAME="SummaryNode"/>
</node>
</node>
<node TEXT="Keystrokes" FOLDED="true" ID="ID_190794373" CREATED="1271849091114" MODIFIED="1310883516932">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="SubMenu" LAST="false"/>
</hook>
<node TEXT="Controls hotkey short-cuts for various predefined icons." ID="ID_699416889" CREATED="1271850151778" MODIFIED="1271850153886"/>
</node>
<node TEXT="Behaviour" FOLDED="true" ID="ID_1251832106" CREATED="1271849242668" MODIFIED="1311483808928" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">Provides control over various aspects of the user interface.</span></font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="SubMenu" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="0"/>
<node TEXT="Place new branches" FOLDED="true" ID="ID_1206588336" CREATED="1310757864314" MODIFIED="1310757871428">
<node TEXT="Last" ID="ID_1088081628" CREATED="1311829566642" MODIFIED="1311829638285">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="First" ID="ID_460059219" CREATED="1311829583482" MODIFIED="1311829638285">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Drag and drop" ID="ID_937187121" CREATED="1310757901793" MODIFIED="1311829691218">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="IsChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Unfold node on paste" ID="ID_688415851" CREATED="1310757872183" MODIFIED="1311829789799">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="UnChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Disable Cursor Move Paper" ID="ID_673199843" CREATED="1310757926214" MODIFIED="1310757942969"/>
<node TEXT="Folding Symbol Width" FOLDED="true" ID="ID_162608451" CREATED="1310757944645" MODIFIED="1310757958359">
<node TEXT="6" ID="ID_917162640" CREATED="1311829819524" MODIFIED="1311829867799">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Grid gap size" FOLDED="true" ID="ID_1904922870" CREATED="1310757958974" MODIFIED="1310757978818">
<node TEXT="10" ID="ID_230800785" CREATED="1311829828003" MODIFIED="1311829867815">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Move note cursor to the end" ID="ID_16484668" CREATED="1310757979449" MODIFIED="1311829925504">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="IsChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="On Key Type" FOLDED="true" ID="ID_870410939" CREATED="1310757895934" MODIFIED="1310758013817">
<node TEXT="Overwrite content" ID="ID_906283996" CREATED="1311829944995" MODIFIED="1311830017793">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Do nothing" ID="ID_238703082" CREATED="1311829953520" MODIFIED="1311830017809">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Add sibling node" ID="ID_1320405878" CREATED="1311829975665" MODIFIED="1311830017793">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Add child node" ID="ID_1310255165" CREATED="1311829984253" MODIFIED="1311830017793">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Behaviour" ID="ID_171435040" CREATED="1310757843354" MODIFIED="1311482753146">
<attribute_layout NAME_WIDTH="33" VALUE_WIDTH="94"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<hook NAME="SummaryNode"/>
</node>
<node TEXT="Recognize input of number and date-time" FOLDED="true" ID="ID_1904729163" CREATED="1310758053597" MODIFIED="1311830112220">
<attribute NAME="Chapter" VALUE="4"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="UnChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
<node TEXT="Check box" ID="ID_1209834896" CREATED="1311430948225" MODIFIED="1311431214666">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
</node>
</node>
<node TEXT="Standard number format" FOLDED="true" ID="ID_1543011178" CREATED="1310758092136" MODIFIED="1311431192311">
<attribute NAME="Chapter" VALUE="4"/>
<node TEXT="#0.####" ID="ID_599272734" CREATED="1311430882736" MODIFIED="1311431228035">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
</node>
</node>
<node TEXT="Standard date format" FOLDED="true" ID="ID_1356962423" CREATED="1310758109725" MODIFIED="1311431192311">
<attribute NAME="Chapter" VALUE="4"/>
<node TEXT="SHORT" ID="ID_1799574764" CREATED="1311430899060" MODIFIED="1311431243666">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
</node>
</node>
<node TEXT="Standard date-time format" FOLDED="true" ID="ID_1291135490" CREATED="1310758124522" MODIFIED="1311431192311">
<attribute NAME="Chapter" VALUE="4"/>
<node TEXT="SHORT,SHORT" ID="ID_1489062870" CREATED="1311430911643" MODIFIED="1311431257269">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
</node>
</node>
<node TEXT="Locale for formats" FOLDED="true" ID="ID_415722182" CREATED="1310758158959" MODIFIED="1311431192311">
<attribute NAME="Chapter" VALUE="4"/>
<node TEXT="Automatic (List with languages)" ID="ID_1141766183" CREATED="1311430933973" MODIFIED="1311431271793">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="4"/>
</node>
</node>
<node TEXT="Data formatting and parsing" ID="ID_1277533848" CREATED="1310758029673" MODIFIED="1311482800101" TEXT_SHORTENED="true">
<attribute_layout NAME_WIDTH="46" VALUE_WIDTH="149"/>
<attribute NAME="Chapter" VALUE="4"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        <i>Properties panel . Calendar &amp; Attributes .&gt; Date/time and number formatting</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="SummaryNode"/>
</node>
<node TEXT="Display inline editor for all new nodes" ID="ID_1855645772" CREATED="1310758187367" MODIFIED="1311830174901">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="IsChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Double click to edit" ID="ID_983797623" CREATED="1310758210931" MODIFIED="1311830174964">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="IsChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Layout map during editing" ID="ID_1344543432" CREATED="1310758229220" MODIFIED="1311926557786">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="IsChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Enter Confirms By Default" ID="ID_410727117" CREATED="1310758243815" MODIFIED="1311830174917">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="IsChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="In-line node editor" ID="ID_411278174" CREATED="1310758175346" MODIFIED="1311482858788">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<hook NAME="SummaryNode"/>
</node>
<node TEXT="Use rich text for pasted nodes" FOLDED="true" ID="ID_95105756" CREATED="1310758275032" MODIFIED="1310758291209">
<node TEXT="Ask" ID="ID_1722707084" CREATED="1311830230522" MODIFIED="1311830274983">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Yes" ID="ID_1038355271" CREATED="1311830236224" MODIFIED="1311830274968">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="No" ID="ID_1904863532" CREATED="1311830240475" MODIFIED="1311830274968">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Delete nodes without confirmation" ID="ID_815571706" CREATED="1310758292152" MODIFIED="1311830340176">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="IsChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Cut nodes without confirmations" ID="ID_32888474" CREATED="1310758330411" MODIFIED="1311830340238">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="IsChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Remove Notes without Question?" ID="ID_420987413" CREATED="1310758343023" MODIFIED="1311830340223">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="IsChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Confirmations" ID="ID_1796859910" CREATED="1310758266552" MODIFIED="1311482858803">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<hook NAME="SummaryNode"/>
</node>
<node TEXT="Compare as numbers" ID="ID_1349176298" CREATED="1310758381166" MODIFIED="1311830340207">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="IsChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Search" ID="ID_692764704" CREATED="1310758369224" MODIFIED="1311830453923">
<hook NAME="SummaryNode"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
</node>
<node TEXT="Selection Method" FOLDED="true" ID="ID_1935087614" CREATED="1310758403896" MODIFIED="1310758410479">
<node TEXT="Delayed" ID="ID_844433841" CREATED="1311830373100" MODIFIED="1311830428293">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Direct" ID="ID_1150077727" CREATED="1311830379081" MODIFIED="1311830428293">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="By click" ID="ID_942709712" CREATED="1311830385486" MODIFIED="1311830428293">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Time For Delayed Selection" FOLDED="true" ID="ID_326872031" CREATED="1310758414699" MODIFIED="1310758431500">
<node TEXT="200" ID="ID_1264808816" CREATED="1311830585298" MODIFIED="1311830634720">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Center selected nodes" ID="ID_266453260" CREATED="1310758432162" MODIFIED="1311830679149">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="IsChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Selection Method" ID="ID_459809249" CREATED="1310758392609" MODIFIED="1311482858803">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<hook NAME="SummaryNode"/>
</node>
<node TEXT="Speed [Speed Mouse wheel]" FOLDED="true" ID="ID_1877269542" CREATED="1310758457856" MODIFIED="1311482535651">
<node TEXT="80" ID="ID_324161648" CREATED="1311830697922" MODIFIED="1311830742157">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Mouse wheel" ID="ID_897961065" CREATED="1310758446257" MODIFIED="1311482858819">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<hook NAME="SummaryNode"/>
</node>
<node TEXT="Speed [Speed Scrollbar]" FOLDED="true" ID="ID_337671733" CREATED="1310758473472" MODIFIED="1311482573777">
<node TEXT="20" ID="ID_1492439142" CREATED="1311830705918" MODIFIED="1311830742157">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Scrollbar" ID="ID_1429616466" CREATED="1310758465133" MODIFIED="1311482858819">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<hook NAME="SummaryNode"/>
</node>
<node TEXT="Case sensitive" ID="ID_1576000342" CREATED="1310758490757" MODIFIED="1311830800642">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="IsChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Ignore all upper case words" ID="ID_60040035" CREATED="1310758500919" MODIFIED="1311830800642">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="IsChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Ignore capital letters at word begin" ID="ID_1838570041" CREATED="1310758524296" MODIFIED="1311830831967">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="UnChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Ignore words with numbers" ID="ID_1573148106" CREATED="1310758546066" MODIFIED="1311830800626">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="IsChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Maximum count of suggestions in the dialog" FOLDED="true" ID="ID_854267681" CREATED="1310758559973" MODIFIED="1310758587773">
<node TEXT="15" ID="ID_1785386989" CREATED="1311830855375" MODIFIED="1311830894648">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Maximum count of suggestions in the menu" FOLDED="true" ID="ID_975657338" CREATED="1310758588794" MODIFIED="1310758611758">
<node TEXT="15" ID="ID_223576174" CREATED="1311830863962" MODIFIED="1311830894663">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Spell checker options" ID="ID_1152047272" CREATED="1310758481013" MODIFIED="1311482858835">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<hook NAME="SummaryNode"/>
</node>
<node TEXT="Display Tool Tips for Nodes" ID="ID_544704953" CREATED="1310758646858" MODIFIED="1311830949170">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="UnChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Initial delay,ms" FOLDED="true" ID="ID_1443483058" CREATED="1310758666943" MODIFIED="1311830976492">
<node TEXT="750" ID="ID_970116987" CREATED="1311830976493" MODIFIED="1311831070109">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Dismiss delay, ms" FOLDED="true" ID="ID_108531934" CREATED="1310758676622" MODIFIED="1311830995017">
<node TEXT="4,000" ID="ID_578810629" CREATED="1311830995018" MODIFIED="1311831070109">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="ReshowDelay, ms" FOLDED="true" ID="ID_865138030" CREATED="1310758685896" MODIFIED="1311831013278">
<node TEXT="500" ID="ID_1312285366" CREATED="1311831002732" MODIFIED="1311831070109">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Tooltip times" ID="ID_1779710506" CREATED="1310758612654" MODIFIED="1311482858835">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<hook NAME="SummaryNode"/>
</node>
<node TEXT="Extra width step" FOLDED="true" ID="ID_1698327273" CREATED="1310758707535" MODIFIED="1310758719250">
<node TEXT="80" ID="ID_980366954" CREATED="1311831029088" MODIFIED="1311831070125">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Editor settings" ID="ID_433167342" CREATED="1310758697339" MODIFIED="1311482879973">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<hook NAME="SummaryNode"/>
</node>
<node TEXT="Undo levels" FOLDED="true" ID="ID_1377721651" CREATED="1310758724476" MODIFIED="1310758752790">
<node TEXT="100" ID="ID_933957391" CREATED="1311831038549" MODIFIED="1311831070125">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Undo" ID="ID_1615979770" CREATED="1310758720396" MODIFIED="1311482858835">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<hook NAME="SummaryNode"/>
</node>
</node>
<node TEXT="HTML" FOLDED="true" ID="ID_857967913" CREATED="1271849323057" MODIFIED="1311405837812" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">Controls interaction with a Web browser, plus options for HTML Import and Export.</span></font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="SubMenu" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="6"/>
<node TEXT="Default Browser Command Windows NT" FOLDED="true" ID="ID_285818766" CREATED="1310758826172" MODIFIED="1310758850961">
<node TEXT="cmd.exe/c start&quot;&quot; &quot;{0}&quot;" ID="ID_1405736836" CREATED="1311831132641" MODIFIED="1311831254064">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Default Browser Command Windows 9x" FOLDED="true" ID="ID_1767258052" CREATED="1310758851561" MODIFIED="1310758873901">
<node TEXT="command.com /c start &quot;{0}&quot;" ID="ID_1915045103" CREATED="1311831164051" MODIFIED="1311831254049">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Default Browser Command Other OS" FOLDED="true" ID="ID_1677430296" CREATED="1310758874485" MODIFIED="1310758897169">
<node TEXT="xdg-open {0}" ID="ID_155698917" CREATED="1311831197693" MODIFIED="1311831254049">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Default Browser Command MAC" FOLDED="true" ID="ID_487716764" CREATED="1310758897815" MODIFIED="1310758922464">
<node TEXT="open {0}" ID="ID_1454396895" CREATED="1311831216218" MODIFIED="1311831254064">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Browser" ID="ID_592493497" CREATED="1310758924498" MODIFIED="1311483926302">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<hook NAME="SummaryNode"/>
</node>
<node TEXT="Html Export Folding" FOLDED="true" ID="ID_326052798" CREATED="1310758955934" MODIFIED="1311405837812">
<attribute NAME="Chapter" VALUE="6"/>
<node TEXT="No Folding" ID="ID_734831498" CREATED="1311831296620" MODIFIED="1311831368592">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Fold Currently Folded" ID="ID_1029122886" CREATED="1311831265428" MODIFIED="1311831368608">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Fold all" ID="ID_1140314813" CREATED="1311831304101" MODIFIED="1311831368608">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Based on Headings" ID="ID_1492749382" CREATED="1311831322876" MODIFIED="1311831385799">
<icon BUILTIN="pencil"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Export Icons In Html" ID="ID_700548117" CREATED="1310758971244" MODIFIED="1311831423052">
<attribute NAME="Chapter" VALUE="6"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="UnChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="HTML Export" ID="ID_842620377" CREATED="1310758942104" MODIFIED="1311487932405">
<attribute NAME="Chapter" VALUE="0"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<hook NAME="SummaryNode"/>
</node>
<node TEXT="Import HTML as node structure" ID="ID_1711300863" CREATED="1310759009575" MODIFIED="1311831462239">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="IsChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="HTML Import" ID="ID_1620307568" CREATED="1310758992624" MODIFIED="1311487937194">
<attribute NAME="Chapter" VALUE="0"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<hook NAME="SummaryNode"/>
</node>
</node>
<node TEXT="Plugins" FOLDED="true" ID="ID_740727461" CREATED="1310759053262" MODIFIED="1311831500350">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="SubMenu" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="7"/>
<node TEXT="Disable formula plugin" ID="ID_713879634" CREATED="1310759073402" MODIFIED="1311831580448">
<attribute NAME="Chapter" VALUE="7"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="UnChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Disable formula evaluation cache" ID="ID_1486266663" CREATED="1310759087090" MODIFIED="1311831580433">
<attribute NAME="Chapter" VALUE="7"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="UnChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Don&apos;t mak formulas with a border" ID="ID_1293139407" CREATED="1310759105693" MODIFIED="1311831613474">
<attribute NAME="Chapter" VALUE="7"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="IsChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Formulas" ID="ID_169365773" CREATED="1310759060166" MODIFIED="1311483987033">
<attribute NAME="Chapter" VALUE="7"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<hook NAME="SummaryNode"/>
</node>
<node TEXT="Script execution enabled" ID="ID_1977472596" CREATED="1310759163843" MODIFIED="1311831580417">
<attribute NAME="Chapter" VALUE="7"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="UnChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Permit File/Read Operations" ID="ID_881249705" CREATED="1310759181789" MODIFIED="1311831580417">
<attribute NAME="Chapter" VALUE="7"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="UnChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Permit File/Write operations" ID="ID_905270948" CREATED="1310759202748" MODIFIED="1311831580402">
<attribute NAME="Chapter" VALUE="7"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="UnChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Permit Network Operations" ID="ID_1355620121" CREATED="1310759224097" MODIFIED="1311831580402">
<attribute NAME="Chapter" VALUE="7"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="UnChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Permit to Execute other Applications" ID="ID_1365136531" CREATED="1310759242325" MODIFIED="1311831580386">
<attribute NAME="Chapter" VALUE="7"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="UnChecked" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Trust signed scripts" ID="ID_1412203400" CREATED="1310759263674" MODIFIED="1311405964687">
<attribute NAME="Chapter" VALUE="7"/>
</node>
<node TEXT="Optional User Key Alias for Signing" ID="ID_1319215468" CREATED="1310759282714" MODIFIED="1311405964687">
<attribute NAME="Chapter" VALUE="7"/>
</node>
<node TEXT="Script search path" FOLDED="true" ID="ID_1885113964" CREATED="1310759307198" MODIFIED="1311405964687">
<attribute NAME="Chapter" VALUE="7"/>
<node TEXT="scripts" ID="ID_1078878417" CREATED="1311831645227" MODIFIED="1311831673354">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="Script classpath" ID="ID_1018193900" CREATED="1310759327486" MODIFIED="1311405964703">
<attribute NAME="Chapter" VALUE="7"/>
</node>
<node TEXT="Scripting" ID="ID_1528894906" CREATED="1310759127432" MODIFIED="1311483987049">
<attribute NAME="Chapter" VALUE="7"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MenuGroupLabel" LAST="false"/>
</hook>
<hook NAME="SummaryNode"/>
</node>
</node>
</node>
</node>
<node TEXT="Maps" FOLDED="true" POSITION="right" ID="ID_1279811672" CREATED="1310134532679" MODIFIED="1311405837812" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MainMenu" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="6"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To select and handle mind maps.
    </p>
  </body>
</html></richcontent>
<node TEXT="Mind map editor" ID="ID_327363055" CREATED="1310134532679" MODIFIED="1311405837828" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">The mode in which mind maps are created and edited.</span></font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="Map browser" ID="ID_524078813" CREATED="1310134532679" MODIFIED="1310883817139" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font color="#000000" face="SansSerif, sans-serif"><span style="color: #000000; font-family: SansSerif, sans-serif">This is a read-only mode for browsing maps rather than editing them. It is has two main uses: First, when using Export &gt; As Java Applet..., the applet opens the map only for reading, because editing maps is not supported via the Web. Second, Freeplane's Help file, when accessed via Help &gt; Documentation or F1, is opened in read-only mode in order to preserve its contents while users learn various Freeplane features. Otherwise this Map Browser mode is generally unused.</span></font>
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="File explorer" ID="ID_1982200889" CREATED="1310134532679" MODIFIED="1311405837859" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="6"/>
</node>
<node TEXT="List with open maps" ID="ID_1685127239" CREATED="1310198658417" MODIFIED="1310883817139">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Publish mind map on web page" FOLDED="true" ID="ID_1615767068" CREATED="1310241684255" MODIFIED="1311405837890" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Procedure" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="6"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000">To publish or share a mind map: </font>
    </p>
    <ul>
      <li>
        Fold the mind map as you want it to be when opening
      </li>
      <li>
        Select <i>File &gt; Export</i>&#160;and choose <i>Java Applet</i>.
      </li>
      <li>
        One file and one directory will be created: <i>myFile.html </i>and <i>myFile.html_files</i>&#160; respectively.
      </li>
    </ul>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        You can move the file <i>myFile.html</i>&#160;and the subdirectory <i>myFile.html_files</i>&#160;&#160;to the location (directory) where you want to it be it accessible. Call <i>myFile.html</i>&#160;&#160;to open the mind map.
      </li>
      <li>
        If publishing on internet, you may rename myFile.html in e.g. index.html. The subdirectory should NOT be renamed.
      </li>
    </ul>
    <p>
      <b>Note also</b>
    </p>
    <ul>
      <li>
        <p>
          <font face="SansSerif, sans-serif" color="#000000">In the Freeplane applet, you can only use the Browse mode; you cannot edit remote maps. Click a node to toggle folding or to follow a link. Drag the background to move the map. To search the map, use the node context menu.</font>
        </p>
      </li>
      <li>
        <font face="SansSerif, sans-serif" color="#000000">Small maps can also use Export as Flash which also offers limited browsing functionality.</font>
      </li>
      <li>
        <font face="SansSerif, sans-serif" color="#000000">LaTeX and SVG images are not displayed in the Freeplane Applet nor in the Flash browser.</font>
      </li>
    </ul>
    <p>
      <b>See also</b>
    </p>
    <ul>
      <li>
        Embed a mind map on a web page or wiki
      </li>
    </ul>
  </body>
</html></richcontent>
<hook NAME="SummaryNode"/>
<node TEXT="Embed mind map on wiki" ID="ID_590309579" CREATED="1310926331342" MODIFIED="1310927934949" LINK="http://freeplane.sourceforge.net/wiki/index.php/Embedding_mind_maps" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Refine" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To embed a mind map in a web site or wiki, see Inernet
    </p>
  </body>
</html></richcontent>
</node>
</node>
</node>
<node TEXT="Help" FOLDED="true" POSITION="right" ID="ID_278329781" CREATED="1310134532679" MODIFIED="1312571971561" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MainMenu" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="1"/>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Methods to help you to solve problems in using Freeplane.
    </p>
  </body>
</html></richcontent>
<node TEXT="Menu reference" ID="ID_1373709444" CREATED="1310134532679" MODIFIED="1310883845609" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Opens overview of menu's in read-only modus.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Key reference" ID="ID_82273720" CREATED="1271855625345" MODIFIED="1310883858042" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Opens Adobe Acrobat-format .PDF file containing a table with key combinations.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Check for updates" ID="ID_1783314150" CREATED="1310198902331" MODIFIED="1310199751621" TEXT_SHORTENED="true">
<icon BUILTIN="../update"/>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Manually check whether a newer version of Freeplane has been released. This check will be done automatically if the corresponding checkbox is enabled at the bottom of the dialog which opens.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Freeplane&apos;s home page" ID="ID_797729331" CREATED="1271855734459" MODIFIED="1310883865062" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Opens a web browser containing the home page of the Freeplane web site.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Ask for help" FOLDED="true" ID="ID_1043826397" CREATED="1271856103992" MODIFIED="1311402584107" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Opens a Web browser window to Freeplane's Open Discussion Forum. A confirmation dialog alerts you to the fact that this function requires that you log in to SourceForge.net where you can easily create a free account. After logging in, you may check for information related to your question by using the Search function, located under the Forums menu heading. If you then want to ask your own question, scroll down toward the bottom of the page to find the Add a Topic heading, type a short title in the line labelled &quot;Enter topic title&quot;, then enter your message in the larger text box below.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="1"/>
<node TEXT="Troubleshooting" ID="ID_389902247" CREATED="1310925961805" MODIFIED="1310925972645" LINK="http://freeplane.sourceforge.net/wiki/index.php/Troubleshooting"/>
</node>
<node TEXT="Report a bug" ID="ID_1404935411" CREATED="1271855833848" MODIFIED="1310883875436" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Opens een web browser containing the page of Mantis Bug tracker. You can report a bug here.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Request a feature" ID="ID_800979042" CREATED="1271855982412" MODIFIED="1310883879804" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Opens een web browser containing Freeplane's Feature Request page in Mantis Tracker. You can leave your request here.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="Licence" ID="ID_1881309718" CREATED="1271857117445" MODIFIED="1310883885233" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Opens a window displaying a short summary of Freeplane's copyright and licensing information, including information about where to find full copies of the GNU General Public License.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="About" ID="ID_1959296182" CREATED="1272489323245" MODIFIED="1311402584122" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Opens a dialog with copyright, version, and technical information.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="1"/>
</node>
<node TEXT="Documentation" ID="ID_604224444" CREATED="1271855545007" MODIFIED="1311402584122" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Opens the main help file in read-only modus.
    </p>
  </body>
</html></richcontent>
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="1"/>
</node>
<node TEXT="Scripting API" ID="ID_1515623567" CREATED="1303933416063" MODIFIED="1310883900178">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
</node>
<node TEXT="This Documentation" FOLDED="true" POSITION="left" ID="ID_1263067716" CREATED="1311401568817" MODIFIED="1311401580814">
<node FOLDED="true" ID="ID_518502200" CREATED="1309618730385" MODIFIED="1311258273867">
<richcontent TYPE="NODE">
<html>
  <head>
    
  </head>
  <body>
    <p style="text-align: centre">
      Making This Documentation 1.2
    </p>
  </body>
</html></richcontent>
<node TEXT="Use Design principles" FOLDED="true" ID="ID_1034693607" CREATED="1309694957628" MODIFIED="1310196223167" TEXT_SHORTENED="true">
<node TEXT="Easy use" FOLDED="true" ID="ID_971999971" CREATED="1309695171606" MODIFIED="1310196223167" TEXT_SHORTENED="true">
<node TEXT="Target group: educated laymen" FOLDED="true" ID="ID_650466721" CREATED="1309695100454" MODIFIED="1310196223151" TEXT_SHORTENED="true">
<node TEXT="The visually limited" ID="ID_1067605334" CREATED="1309695629927" MODIFIED="1310196223151" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <ul>
      <li>
        High contrast
      </li>
      <li>
        Limited use of color (grouping only)
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Scoped visibility" FOLDED="true" ID="ID_1491781564" CREATED="1309695214007" MODIFIED="1310196223136" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Task related
    </p>
  </body>
</html></richcontent>
<node TEXT="Predefined Filters" ID="ID_1063131817" CREATED="1309695826409" MODIFIED="1310196223136" TEXT_SHORTENED="true"/>
</node>
<node TEXT="Standard content types" FOLDED="true" ID="ID_1568417876" CREATED="1309696476579" MODIFIED="1310196223136" TEXT_SHORTENED="true">
<node TEXT="Reading guidance" ID="ID_754327054" CREATED="1309696324854" MODIFIED="1310196223120" TEXT_SHORTENED="true"/>
<node TEXT="Definitions" ID="ID_1594805543" CREATED="1309696223219" MODIFIED="1310196223120" TEXT_SHORTENED="true"/>
<node TEXT="Methods" ID="ID_1690427045" CREATED="1309696230263" MODIFIED="1310196223105" TEXT_SHORTENED="true"/>
<node TEXT="Refine information" ID="ID_1224425563" CREATED="1309696253141" MODIFIED="1310196223105" TEXT_SHORTENED="true"/>
<node TEXT="Exceptions" ID="ID_22860584" CREATED="1310836503572" MODIFIED="1310836515771"/>
<node TEXT="Examples" ID="ID_1077186776" CREATED="1309696266689" MODIFIED="1310196223105" TEXT_SHORTENED="true"/>
</node>
<node TEXT="Standard use of node elements" FOLDED="true" ID="ID_1585389386" CREATED="1309696673647" MODIFIED="1310196223089" TEXT_SHORTENED="true">
<node TEXT="basic text: title/name" ID="ID_1920924045" CREATED="1309696563043" MODIFIED="1310196223089" TEXT_SHORTENED="true"/>
<node TEXT="details: what/how to" ID="ID_715096689" CREATED="1309696579165" MODIFIED="1310196223089" TEXT_SHORTENED="true"/>
</node>
<node TEXT="Standard Form" FOLDED="true" ID="ID_762818872" CREATED="1309696773167" MODIFIED="1310196223073" TEXT_SHORTENED="true">
<node TEXT="Styles" ID="ID_1772043588" CREATED="1309695477062" MODIFIED="1310196223073" TEXT_SHORTENED="true"/>
</node>
<node TEXT="Easy handling" FOLDED="true" ID="ID_41805908" CREATED="1309696039857" MODIFIED="1310196223073" TEXT_SHORTENED="true">
<node TEXT="Scripts" ID="ID_53029889" CREATED="1309696120205" MODIFIED="1310196223058" TEXT_SHORTENED="true"/>
</node>
</node>
<node TEXT="Easy maintenance" FOLDED="true" ID="ID_684636398" CREATED="1309695019936" MODIFIED="1310196223058" TEXT_SHORTENED="true">
<node TEXT="Follow structure of Main menu" ID="ID_96415895" CREATED="1309695540344" MODIFIED="1310196223058" TEXT_SHORTENED="true"/>
<node TEXT="Define content  once, use many" ID="ID_599399448" CREATED="1309694968072" MODIFIED="1310196223042" TEXT_SHORTENED="true"/>
<node TEXT="Use Conditional styles" ID="ID_1144923217" CREATED="1309695364859" MODIFIED="1310196223042" TEXT_SHORTENED="true"/>
</node>
</node>
<node TEXT="Prepare empty mind map" FOLDED="true" ID="ID_548574769" CREATED="1309691330424" MODIFIED="1310196223042" TEXT_SHORTENED="true">
<node TEXT="Automatic edge color off" ID="ID_1831289663" CREATED="1309618738099" MODIFIED="1310196223027" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <ul>
      <li>
        Deselect <i>View &gt; Properties panel &gt; Automatic edge color</i>
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="Attributes invisible" ID="ID_1915815091" CREATED="1309618929144" MODIFIED="1310196223027" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <ul>
      <li>
        Select<i>&#160;Attributes &gt; Hide all attributes</i>
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="Attribute icon off" ID="ID_1498387169" CREATED="1309618959853" MODIFIED="1310196223011" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <ul>
      <li>
        Deselect<i>&#160;Tools &gt; Show icons for attributes</i>
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Define Category" FOLDED="true" ID="ID_507010795" CREATED="1310838169626" MODIFIED="1310838588667">
<node TEXT="Content" FOLDED="true" ID="ID_862676309" CREATED="1310838629343" MODIFIED="1310838635553">
<node TEXT="Definition" ID="ID_611433412" CREATED="1310838201903" MODIFIED="1310838488593" TEXT_SHORTENED="true"/>
<node TEXT="Method" ID="ID_247256524" CREATED="1310838209149" MODIFIED="1310838488624" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Menu item
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Procedure" ID="ID_528236762" CREATED="1310838217269" MODIFIED="1310838488624" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Series of actions
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="OptionalValue" ID="ID_1278994490" CREATED="1310845769830" MODIFIED="1310848523586" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      A possible value of a method
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Exception" ID="ID_1086387726" CREATED="1310838298748" MODIFIED="1310838488624" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Special condition
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Refine" ID="ID_464752434" CREATED="1310838284084" MODIFIED="1310838488609" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      More detailed explanation
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Example" ID="ID_1007698430" CREATED="1310838291767" MODIFIED="1310838488609" TEXT_SHORTENED="true"/>
<node TEXT="ToNote" ID="ID_961458845" CREATED="1310838313685" MODIFIED="1310838488609" TEXT_SHORTENED="true"/>
</node>
<node TEXT="Structure" FOLDED="true" ID="ID_1577905162" CREATED="1310838651581" MODIFIED="1310838659179">
<node TEXT="Title" ID="ID_405338718" CREATED="1310838898171" MODIFIED="1310838904381"/>
<node TEXT="MainMenu" FOLDED="true" ID="ID_65441144" CREATED="1310838664397" MODIFIED="1310838674382">
<node TEXT="MainMenuAccent" ID="ID_959800516" CREATED="1310838930051" MODIFIED="1310838943685"/>
</node>
<node TEXT="SubMenu" ID="ID_1383579659" CREATED="1310838678803" MODIFIED="1310838690769"/>
<node TEXT="MainMenuGroupLabel" ID="ID_1034739022" CREATED="1310838719886" MODIFIED="1310838746641"/>
</node>
<node TEXT="Revision" ID="ID_1107739085" CREATED="1312101477551" MODIFIED="1312102431789"/>
</node>
<node TEXT="Define Chapter" FOLDED="true" ID="ID_362129212" CREATED="1309618782903" MODIFIED="1312101358874" TEXT_SHORTENED="true">
<node TEXT="Goal" ID="ID_403391885" CREATED="1311327772687" MODIFIED="1311403771425" TEXT_SHORTENED="true">
<icon BUILTIN="full-0"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      This filter shows the procedure how to set the filter rule and filter conditions for displaying only the nodes of Scope=GS1.
    </p>
  </body>
</html></richcontent>
<attribute NAME="Chapter" VALUE="0" OBJECT="org.freeplane.features.format.FormattedNumber|0.0"/>
</node>
<node TEXT="Chapter 1. My first mind map" ID="ID_1104918030" CREATED="1311259207123" MODIFIED="1312101375972" TEXT_SHORTENED="true">
<icon BUILTIN="button_ok"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Mastering Chapter 1 means you know:
    </p>
    <ul>
      <li>
        basic definitions of nodes and relations between nodes
      </li>
      <li>
        how to (re)open and save your first map
      </li>
      <li>
        how to create child nodes and sibling nodes
      </li>
      <li>
        when to use two different ways of editing (in-line or dialog)
      </li>
      <li>
        how to fold (hide) and unfold all descendants
      </li>
      <li>
        the basic difference between menu's <i>Nodes</i>&#160;an <i>Node features</i>
      </li>
      <li>
        the basic difference between menu's <i>Format</i>&#160;and and <i>Styles</i>.
      </li>
      <li>
        the benefits are of filtering.
      </li>
    </ul>
  </body>
</html></richcontent>
<attribute NAME="Chapter" VALUE="1"/>
<font BOLD="true"/>
</node>
<node TEXT="Chapter 2.Handling nodes" ID="ID_955352628" CREATED="1311258291175" MODIFIED="1312101390464" TEXT_SHORTENED="true">
<attribute NAME="Chapter" VALUE="2"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      You mastered GS2&#160;when you are able to handle nodes more easily:
    </p>
    <ul>
      <li>
        Know the meaning of all menu/toolbars.
      </li>
      <li>
        Know all about selecting nodes
      </li>
      <li>
        Know how to move nodes
      </li>
      <li>
        Know basic file operations
      </li>
      <li>
        Know additional fold operations
      </li>
      <li>
        Know to find last modified nodes
      </li>
    </ul>
  </body>
</html></richcontent>
<font BOLD="true"/>
</node>
<node TEXT="Chapter 3.Basic Node features" ID="ID_1361306471" CREATED="1311327937765" MODIFIED="1312101402429" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      You have mastered GS3 if you can handle Node Features:
    </p>
    <ul>
      <li>
        add and remove Icons
      </li>
      <li>
        add, edit and hide Images
      </li>
      <li>
        add, edit and hide Details
      </li>
      <li>
        add, edit and hide Attributes
      </li>
      <li>
        add, edit and hide Notes; and use the Note Panel
      </li>
    </ul>
  </body>
</html></richcontent>
<attribute NAME="Chapter" VALUE="3" OBJECT="org.freeplane.features.format.FormattedNumber|3.0"/>
<font BOLD="true"/>
</node>
<node TEXT="Chapter 4.Format &amp; Style" ID="ID_1181215259" CREATED="1311342024084" MODIFIED="1312101416672">
<attribute NAME="Chapter" VALUE="4" OBJECT="org.freeplane.features.format.FormattedNumber|4.0"/>
<font BOLD="true"/>
</node>
<node TEXT="Chapter  5.Date, Time &amp; Reminder" ID="ID_1315985597" CREATED="1311342048684" MODIFIED="1312101426641">
<attribute NAME="Chapter" VALUE="5" OBJECT="org.freeplane.features.format.FormattedNumber|5.0"/>
<font BOLD="true"/>
</node>
<node TEXT="Chapter 6.Moving &amp; Publishing maps" ID="ID_1822943808" CREATED="1311342076904" MODIFIED="1312101437373">
<attribute NAME="Chapter" VALUE="6" OBJECT="org.freeplane.features.format.FormattedNumber|6.0"/>
<font BOLD="true"/>
</node>
<node TEXT="Chapter 7.Scripts &amp; Formulas" ID="ID_1540834370" CREATED="1311404012843" MODIFIED="1312101446843">
<attribute NAME="Chapter" VALUE="7" OBJECT="org.freeplane.features.format.FormattedNumber|7.0"/>
<font BOLD="true"/>
</node>
<node TEXT="Chapter 8.Security &amp; Preferences" ID="ID_390065587" CREATED="1311404075142" MODIFIED="1312101455922">
<attribute NAME="Chapter" VALUE="8" OBJECT="org.freeplane.features.format.FormattedNumber|8.0"/>
<font BOLD="true"/>
</node>
</node>
<node TEXT="Define Styles" FOLDED="true" ID="ID_1310961875" CREATED="1309618758309" MODIFIED="1310196223011" TEXT_SHORTENED="true">
<node TEXT="Default" ID="ID_150604698" CREATED="1309639080400" MODIFIED="1310196222995" TEXT_SHORTENED="true"/>
<node TEXT="Definition" STYLE_REF="Defenition" ID="ID_24518950" CREATED="1309618770906" MODIFIED="1310196222995" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Definition" LAST="false"/>
</hook>
</node>
<node TEXT="Method" ID="ID_255587171" CREATED="1309618777965" MODIFIED="1310196222980" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Method" LAST="false"/>
</hook>
</node>
<node TEXT="OptionalValue" STYLE_REF="OptionalValue" ID="ID_796900128" CREATED="1310845841441" MODIFIED="1310848600401">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="OptionalValue" LAST="false"/>
</hook>
</node>
<node TEXT="Procedure" ID="ID_927469794" CREATED="1310838858321" MODIFIED="1310839528312" COLOR="#006666">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Procedure" LAST="false"/>
</hook>
</node>
<node TEXT="Exception" ID="ID_497865634" CREATED="1309891278322" MODIFIED="1310196222933" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
</hook>
</node>
<node TEXT="ToNote" ID="ID_21237962" CREATED="1309890992904" MODIFIED="1310196222933" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
</hook>
</node>
<node TEXT="Refine" ID="ID_1208298554" CREATED="1309619037550" MODIFIED="1310196222980" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Refine" LAST="false"/>
</hook>
</node>
<node TEXT="Example" STYLE_REF="Example" ID="ID_1230627708" CREATED="1311097198651" MODIFIED="1311097742921">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Example" LAST="false"/>
</hook>
</node>
<node TEXT="MainMenu" ID="ID_349932844" CREATED="1309691442065" MODIFIED="1310196222964" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="MainMenu" LAST="false"/>
</hook>
</node>
<node TEXT="MainMenuAccent" STYLE_REF="MainMenuAccent" ID="ID_654287956" CREATED="1310284844205" MODIFIED="1310284875766"/>
<node TEXT="SubMenu" ID="ID_843480445" CREATED="1309802400475" MODIFIED="1310196222949" STYLE="bubble" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="SubMenu" LAST="false"/>
</hook>
</node>
<node TEXT="MenuGroupLabel" STYLE_REF="MenuGroupLabel" ID="ID_699038370" CREATED="1310838790219" MODIFIED="1310838831607"/>
<node TEXT="Title" STYLE_REF="Title" ID="ID_1964332650" CREATED="1286914216123" MODIFIED="1310242058632"/>
<node TEXT="Revision" STYLE_REF="Revision" ID="ID_1824312381" CREATED="1312102440033" MODIFIED="1312102451750"/>
</node>
<node TEXT="Define Conditional styles" FOLDED="true" ID="ID_475303000" CREATED="1309618798837" MODIFIED="1310196222917" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <ul>
      <li>
        Define a conditional style for each Class type
      </li>
      <li>
        Set an attribute Class for each node
      </li>
      <li>
        Set an attribute Scope = GettingStarted for all nodes of the introduction
      </li>
      <li>
        Define a groovy script for node Getting started
      </li>
    </ul>
  </body>
</html></richcontent>
<node TEXT="Attribute Class defines the style(s) to be applied" ID="ID_813678576" CREATED="1309694697522" MODIFIED="1310196222917" TEXT_SHORTENED="true"/>
<node TEXT="Class value = list of Style names" ID="ID_864906280" CREATED="1309694751130" MODIFIED="1310196222902" TEXT_SHORTENED="true"/>
</node>
</node>
<node TEXT="Translating This Documention" LOCALIZED_STYLE_REF="default" ID="ID_1796932858" CREATED="1311099007251" MODIFIED="1311931543787" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      In translating Freeplane the following points have to be noted:
    </p>
    <ul>
      <li>
        The look and feel depends on styles and conditional styles. The good functioning of the conditional styles presupposes the existence of the hidden attribute <i>Class</i>.
      </li>
      <li>
        The predefined filtering options presuppose the existence of the hidden attributes <i>Chapter</i>&#160;and <i>Class</i>.
      </li>
      <li>
        Formulas and Groovy scripts should stay in tact.
      </li>
      <li>
        The English documentation is saved with a particular folding state.
      </li>
    </ul>
    <p>
      To make sure that in the translation process these components keep present and consistent:
    </p>
    <ul>
      <li>
        Make a copy of this English documentation and translate the text in the main node and in Details.
      </li>
      <li>
        Do not translate the attributes. Or load the mind map in an editor like Note++ and use<i>&#160;find and replace</i>&#160;to translate all attribute values in the same way.
      </li>
      <li>
        Set Save folding in Preferences, then Unfold all, Fold all, Unfold Chapter, Save map.
      </li>
    </ul>
    <p>
      The next problem is to keep your translation synchronized with modification of the English documentation. To know what has been changed:
    </p>
    <ul>
      <li>
        Find and read the procedure &quot;<b>Find last modified</b>&quot;, see<i>&#160;&#160;Filter &gt; Quick filter </i>or <i>Edit &gt; Find and replace.</i>.. This shows the modified nodes. It does not show the deleted nodes.
      </li>
    </ul>
    <p>
      To make the change, the most certain procedure is to:
    </p>
    <ul>
      <li>
        &#160;Copy and paste each modified node to your translated file and translate it (again). In this way you are sure the hidden structure is taken over too.
      </li>
    </ul>
    <p>
      An alternative is to check all components (basic text and optional icons, formula's, hyperlinks, Details, Attributes, Images, formulas <b>AND</b>&#160; Groovy scripts) and make the changes necessary.
    </p>
    <p>
      
    </p>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        You could perform a spell check to the whole map by loading the documentation mm in a text editor like OpenOffice.
      </li>
      <li>
        If you want to change colors, do this by editing the used style. See <i>Styles &gt; Edit styles.</i>
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="Version, revisions and credits" FOLDED="true" ID="_Freeplane_Link_784043927" CREATED="1270892460645" MODIFIED="1312145108660" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="Example" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="Procedure" LAST="false"/>
</hook>
<attribute NAME="Chapter" VALUE="1"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To see which nodes have been revised in a version:
    </p>
    <ul>
      <li>
        select <i>Styles &gt; Conditional Map Styles</i>
      </li>
      <li>
        check the box before the revision you are interested in. (To undo, remove the check.)
      </li>
    </ul>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        The nodes revised are marked with a blue icon with &quot;i&quot;.
      </li>
      <li>
        Find or filter for these nodes with <i>(Style,IsEqual, Revision)</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<node TEXT="Version Freeplane 1.2.7" ID="ID_605038742" CREATED="1289133018961" MODIFIED="1310284238481"/>
<node TEXT="Documentation" FOLDED="true" ID="ID_136822724" CREATED="1286311222217" MODIFIED="1310284652022">
<node TEXT="Jokro" FOLDED="true" ID="ID_1128010246" CREATED="1286311242349" MODIFIED="1293375084474" LINK="mailto:jokro@users.sourceforge.net?subject%20=%20handleiding">
<node TEXT="Basic rewrite of Freeplane 1.3 to Freeplane 1.2" ID="ID_52670674" CREATED="1289731003743" MODIFIED="1310284343860"/>
</node>
<node TEXT="Date of last modification:" ID="ID_21798827" CREATED="1289730911791" MODIFIED="1310284256952"/>
</node>
<node TEXT="Software" FOLDED="true" ID="ID_1342635261" CREATED="1270892460645" MODIFIED="1310284708885">
<node TEXT="Original Authors" FOLDED="true" ID="Freeplane_Link_415458128" CREATED="1270892460646" MODIFIED="1310284712036">
<node TEXT="Joerg Mueller" ID="_Freeplane_Link_1896457660" CREATED="1270892460646" MODIFIED="1310284722098"/>
<node TEXT="Daniel Polansky" ID="_Freeplane_Link_984984595" CREATED="1270892460647" MODIFIED="1310284722113" LINK="http://danpolansky.blogspot.com/"/>
<node TEXT="Petr Novak" ID="_Freeplane_Link_459203293" CREATED="1270892460647" MODIFIED="1310284722098"/>
<node TEXT="Christian Foltin" ID="_Freeplane_Link_875814410" CREATED="1270892460647" MODIFIED="1310284722098"/>
<node TEXT="Dimitry Polivaev" ID="_Freeplane_Link_1415293905" CREATED="1270892460648" MODIFIED="1310284722098"/>
<node TEXT="Graphical Design by Predrag Cuklin" ID="ID_1094825033" CREATED="1270892460648" MODIFIED="1310284722098"/>
</node>
<node TEXT="Current Team" FOLDED="true" ID="ID_1090487344" CREATED="1271097427203" MODIFIED="1310284734578">
<node TEXT="Release 1.1.x" FOLDED="true" ID="ID_1795869028" CREATED="1271097443907" MODIFIED="1310284741099">
<node TEXT="Dimitry Polivaev" ID="ID_809494025" CREATED="1271097633119" MODIFIED="1271097633119"/>
<node TEXT="Volker Boerchers" ID="ID_548414191" CREATED="1271097633119" MODIFIED="1271097633119"/>
<node TEXT="Eric L." ID="ID_306583030" CREATED="1271097633122" MODIFIED="1271097633122"/>
<node TEXT="jayseye" ID="ID_502187025" CREATED="1271097633123" MODIFIED="1271097633123"/>
<node TEXT="Predrag" ID="ID_320430724" CREATED="1271097633123" MODIFIED="1271097633123"/>
<node TEXT="Ryan Wesley" ID="ID_288819242" CREATED="1271097633123" MODIFIED="1271097633123"/>
</node>
</node>
</node>
</node>
</node>
<node TEXT="Download &amp; Install" FOLDED="true" POSITION="left" ID="ID_931855473" CREATED="1311401499171" MODIFIED="1311401511542">
<node TEXT="Download" FOLDED="true" ID="_Freeplane_Link_904501221" CREATED="1270892460638" MODIFIED="1310243962938">
<node TEXT="Freeplane&apos;s homepage" ID="ID_916980386" CREATED="1270892460631" MODIFIED="1271530957733" LINK="http://freeplane.sourceforge.net">
<font NAME="SansSerif" SIZE="12"/>
</node>
<node TEXT="Windows platform" STYLE_REF="Functiegroep" FOLDED="true" ID="_Freeplane_Link_139664576" CREATED="1270892460640" MODIFIED="1310281568073" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <span style="color: #000000; font-family: SansSerif, sans-serif"><font face="SansSerif, sans-serif" color="#000000">To install Freeplane on Microsoft Windows, install Java from Sun and install Freeplane using the Freeplane installer.</font></span>
    </p>
  </body>
</html></richcontent>
<node TEXT="Download Java Runtime Environment (at least J2RE1.5)" ID="ID_1620747952" CREATED="1270892460639" MODIFIED="1310243329632" LINK="http://java.sun.com/javase/downloads/index.jsp" TEXT_SHORTENED="true">
<icon BUILTIN="full-1"/>
<edge WIDTH="thin"/>
<font NAME="SansSerif" SIZE="12"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Most of the times this is done automatically when downloading Freeplane form the Freeplane home page.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Download Freeplane application" ID="_Freeplane_Link_1612101865" CREATED="1270892460640" MODIFIED="1310243335684" LINK="http://freeplane.sourceforge.net" TEXT_SHORTENED="true">
<icon BUILTIN="full-2"/>
<edge WIDTH="thin"/>
<font NAME="SansSerif" SIZE="12"/>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Most of the times this is done automatically when downloading Freeplane form the Freeplane home page.
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Install dictionaries for spell check" ID="ID_375945761" CREATED="1310243241880" MODIFIED="1310243496708" LINK="#ID_141336344">
<icon BUILTIN="full-3"/>
</node>
</node>
<node TEXT="Linux platform" STYLE_REF="Functiegroep" ID="ID_839470339" CREATED="1289029709910" MODIFIED="1310244076935" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">To install Freeplane on Linux, download the Java Runtime Environment and the Freeplane application itself. First install Java, then unpack Freeplane. To run Freeplane, execute freeplane.sh.</span></font>
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT=" MAC platform" STYLE_REF="Functiegroep" ID="ID_1956173686" CREATED="1289029737710" MODIFIED="1310244086061" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">To install Freeplane on Mac OS X first use the built in Software Update feature to ensure that you have all the latest available updates, especially Java. Software Update is located under the Apple logo menu in the top left-hand corner of the screen. Then download a Mac-specific version of Freeplane. The .dmg version is easiest to install, though a .zip version may also be available. When the download is complete, the file may be automatically mounted (or un-zipped) depending on your Web browser settings. Otherwise either double-click on the downloaded .dmg file to &quot;mount&quot; it, or double-click on the downloaded .zip file to un-zip it. Now you should see a Freeplane application icon, which you can drag to your Applications folder. Then you may optionally create an alias (short-cut) on the Desktop, and/or on the Dock. To run Freeplane, either double-click on its application icon (in the Applications folder) or on its Desktop short-cut, or click once on its icon in the Dock. The Freeplane Wiki has Macintosh page with more information.</span></font>
    </p>
  </body>
</html></richcontent>
</node>
</node>
<node TEXT="Install" FOLDED="true" ID="Freeplane_Link_1822195277" CREATED="1270892460697" MODIFIED="1310903701338" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To set preferences
    </p>
    <ul>
      <li>
        &#160;select <i>Tools &gt; Preferences.</i>
      </li>
    </ul>
  </body>
</html></richcontent>
<node TEXT="System language" ID="ID_172050805" CREATED="1271856103992" MODIFIED="1310282001239" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      To change the language used in Freeplane menu's:
    </p>
    <ul>
      <li>
        select <i>Tools &gt; Preferences &gt; Environment &gt; Language;</i>
      </li>
      <li>
        select your language
      </li>
      <li>
        restart Freeplane
      </li>
    </ul>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        &quot;language&quot;Atomatic chooses the language of your operating system
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="Dictionaries for spell check" ID="ID_1124028676" CREATED="1310243241880" MODIFIED="1310282095144" LINK="#ID_141336344" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Standard spell check is in English
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Automatic backup" ID="ID_595250711" CREATED="1271096741387" MODIFIED="1310281994968" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000">Freeplane creates a subfolder named .backup in each folder which contains edited maps. Old map versions are saved there, along with automatically-saved copies of edited maps. These copies are named with the file extension &quot;<i>.autosave</i>&quot;. </font>
    </p>
    <p>
      
    </p>
    <p>
      <font face="SansSerif, sans-serif" color="#000000">By default, an automatic save happens every minute, and the last 10 autosave files are kept as protection against accidental termination of Freeplane, e.g. on computer shutdown. </font>
    </p>
    <p>
      
    </p>
    <p>
      <font face="SansSerif, sans-serif" color="#000000">To restore an automatically-saved file, you must manually rename it to remove the &quot;.autosave&quot; extension and the preceding sequence number. By default, sequence numbers range from 1 to 10. For instance, to restore &quot;freeplane.mm&quot; from the most recent autosave file, rename (or copy) &quot;.backup\freeplane.mm.10.autosave&quot; to &quot;freeplane.mm&quot;. </font>
    </p>
    <p>
      
    </p>
    <p>
      <font face="SansSerif, sans-serif" color="#000000">Automatic save and backup can be adjusted to your needs via Tools &gt; Preferences &gt; Environment &gt; Automatic Save.</font>
    </p>
  </body>
</html></richcontent>
</node>
</node>
</node>
<node TEXT="Macintosh Usage Notes" POSITION="left" ID="ID_480781217" CREATED="1270892460643" MODIFIED="1310284803717" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS" HIDDEN="true">
<html>
  <head>
    
  </head>
  <body>
    <p>
      <font face="SansSerif, sans-serif" color="#000000"><span style="color: #000000; font-family: SansSerif, sans-serif">Freeplane recently added full support for Apple's Mac OS X operating system. Some of the documentation is still oriented toward PC users running Windows or Linux. Mac users will be able to follow along by keeping some differences in mind, listed below.</span></font>
    </p>
    <ul>
      <li>
        <span onclick="show_folder('1_1')" class="foldclosed" id="show1_1">+</span>&#160;<span onclick="hide_folder('1_1')" class="foldopened" id="hide1_1">-</span>&#160;<font face="SansSerif, sans-serif" color="#000000"><span style="font-family: SansSerif, sans-serif; color: #000000">Command key versus the Ctrl (control) key</span></font>

        <ul id="fold1_1">
          <li>
            <p>
              <font face="SansSerif, sans-serif" color="#000000"><span style="font-family: SansSerif, sans-serif; color: #000000">When Freeplane runs under Mac OS X, you generally will use the Apple Command key rather than the &quot;control&quot; key, which is labelled Ctrl on a PC. The Command key is marked with a cloverleaf &#8984; symbol, and may also be captioned as &quot;command&quot; or &quot;cmd&quot;, or be marked with an Apple logo.</span></font>
            </p>
            <p>
              <font face="SansSerif, sans-serif" color="#000000"><span style="font-family: SansSerif, sans-serif; color: #000000">When you see the Ctrl key mentioned in Freeplane's documentation, in most cases you should use the Command key instead.</span></font>
            </p>
            <p>
              <font face="SansSerif, sans-serif" color="#000000"><span style="font-family: SansSerif, sans-serif; color: #000000">In a very few cases, even on your Mac, Freeplane actually requires using the &quot;control&quot; key rather than the Command key. In these cases, this document refers to the key explicitly as Control, for emphasis. In other cases, if the documentation refers to the Ctrl key, yet the Command key fails to work as a substitute on your Mac, try the &quot;control&quot; key instead.</span></font>
            </p>
          </li>
        </ul>
      </li>
      <li>
        <span onclick="show_folder('1_2')" class="foldclosed" id="show1_2">+</span>&#160;<span onclick="hide_folder('1_2')" class="foldopened" id="hide1_2">-</span>&#160;<font face="SansSerif, sans-serif" color="#000000"><span style="font-family: SansSerif, sans-serif; color: #000000">Mice, Right Clicks and Context Menus</span></font>

        <ul id="fold1_2">
          <li>
            <p>
              <font face="SansSerif, sans-serif" color="#000000"><span style="font-family: SansSerif, sans-serif; color: #000000">Many Apple-branded mice and trackpads have only one click-able &quot;button.&quot; To bring up a context-sensitive menu (or &quot;context menu&quot;), Mac users can hold down the Control key while clicking the mouse (abbreviated as &quot;Control + click&quot;). Yes, use the Control key here, rather than the Command key, because this function is built into Mac OS X so Freeplane cannot change it. Apple makes Control + click equivalent to the &quot;right click&quot; which is available on mice with two buttons.</span></font>
            </p>
          </li>
        </ul>
      </li>
      <li>
        <span onclick="show_folder('1_3')" class="foldclosed" id="show1_3">+</span>&#160;<span onclick="hide_folder('1_3')" class="foldopened" id="hide1_3">-</span>&#160;<font face="SansSerif, sans-serif" color="#000000"><span style="font-family: SansSerif, sans-serif; color: #000000">Menu Differences</span></font>

        <ul id="fold1_3">
          <li>
            <font face="SansSerif, sans-serif" color="#000000"><span style="font-family: SansSerif, sans-serif; color: #000000">A few menu items are relocated in accordance with Mac OS X standards. These are listed below.</span></font>
          </li>
          <li>
            <font face="SansSerif, sans-serif" color="#000000"><span style="font-family: SansSerif, sans-serif; color: #000000">Help &gt; About is moved to the Freeplane application menu &gt; About Freeplane.</span></font>
          </li>
          <li>
            <font face="SansSerif, sans-serif" color="#000000"><span style="font-family: SansSerif, sans-serif; color: #000000">Tools &gt; Preferences is moved to the Freeplane application menu &gt; Preferences.</span></font>
          </li>
          <li>
            <font face="SansSerif, sans-serif" color="#000000"><span style="font-family: SansSerif, sans-serif; color: #000000">File &gt; Quit is moved to the Freeplane application menu &gt; Quit Freeplane.</span></font>
          </li>
        </ul>
      </li>
      <li>
        <span onclick="show_folder('1_4')" class="foldclosed" id="show1_4">+</span>&#160;<span onclick="hide_folder('1_4')" class="foldopened" id="hide1_4">-</span>&#160;<font face="SansSerif, sans-serif" color="#000000"><span style="font-family: SansSerif, sans-serif; color: #000000">Zooming</span></font>

        <ul id="fold1_4">
          <li>
            <p>
              <font face="SansSerif, sans-serif" color="#000000"><span style="font-family: SansSerif, sans-serif; color: #000000">Mac OS X provides a means to zoom in on (magnify) part of the screen, by holding down the Control key while turning the mouse wheel forward. This facility is built into the Mac operating system, so the function is unavailable for Freeplane to use for control of its own Zoom function. So Mac users must use one of the alternate means which Freeplane provides to adjust its zoom level. </span></font>
            </p>
            <p>
              <font face="SansSerif, sans-serif" color="#000000"><span style="font-family: SansSerif, sans-serif; color: #000000">If you are unfamiliar with OS X's Control + mouse wheel function, be aware that some mouse functions become awkward to use, or even unusable, while zoomed in. To return the screen to normal, hold down the Conrol key and turn the mouse wheel backward to zoom back out all the way.</span></font>
            </p>
          </li>
        </ul>
      </li>
      <li>
        <span onclick="show_folder('1_5')" class="foldclosed" id="show1_5">+</span>&#160;<span onclick="hide_folder('1_5')" class="foldopened" id="hide1_5">-</span>&#160;<font face="SansSerif, sans-serif" color="#000000"><span style="font-family: SansSerif, sans-serif; color: #000000">Freeplane Wiki</span></font>

        <ul id="fold1_5">
          <li>
            <a target="_blank" href="http://freeplane.sourceforge.net/wiki/index.php/Macintosh"><span class="l">~</span>&#160;<font color="#000000" face="SansSerif, sans-serif"><span style="font-family: SansSerif, sans-serif; color: #000000">Visit the Macintosh page on Freeplane's online Wiki to learn more about running Freeplane on a Mac.</span></font>&#160;</a>
          </li>
        </ul>
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="FAQ" FOLDED="true" POSITION="left" ID="ID_1040911735" CREATED="1312613811349" MODIFIED="1312695011324" TEXT_SHORTENED="true">
<hook NAME="NodeConditionalStyles">
    <conditional_style ACTIVE="true" STYLE_REF="Exception" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="ToNote" LAST="false"/>
    <conditional_style ACTIVE="true" STYLE_REF="Example" LAST="false"/>
</hook>
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Frequently asked questions.
    </p>
    <p>
      
    </p>
    <p>
      <b>Note</b>
    </p>
    <ul>
      <li>
        Also find/filter for Details AND

        <ul>
          <li>
            (Style, Contains, Exception) to look for exceptional cases
          </li>
          <li>
            (Style, Contains, ToNote) to loop for important information.
          </li>
        </ul>
      </li>
    </ul>
  </body>
</html>
</richcontent>
<node TEXT="Map / node not editable" ID="ID_398310650" CREATED="1312614404180" MODIFIED="1312614515004" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      Use Maps &gt; Mind map editor in stead of Maps &gt; Mind map browser
    </p>
  </body>
</html></richcontent>
</node>
<node TEXT="Node does not unfold" ID="ID_582886738" CREATED="1312614555555" MODIFIED="1312614724161" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      If this happens after filtering:
    </p>
    <ul>
      <li>
        undo filtering
      </li>
      <li>
        adapt filter settings to allow for descendants / hidden nodes
      </li>
    </ul>
  </body>
</html></richcontent>
</node>
<node TEXT="Style not applied" ID="ID_834195855" CREATED="1312613817971" MODIFIED="1312613933333" TEXT_SHORTENED="true">
<richcontent TYPE="DETAILS">
<html>
  <head>
    
  </head>
  <body>
    <p>
      In Properties Panel: remove all checks. These overrule style settings.
    </p>
  </body>
</html></richcontent>
</node>
</node>
</node>
</map>
