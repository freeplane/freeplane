<map version="0.9.0">
<!-- To view this file, download free mind mapping software Freeplane from https://www.freeplane.org -->
<node CREATED="1216974513042" ID="ID_833600903" MODIFIED="1216991733257" TEXT="Example of map exportable to Word">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      The root node is exported as document title (with format &quot;Title&quot;).
    </p>
    <p>
      Attributes of the root node are exported as document properties if they have the prefix &quot;doc-&quot; in their name. Acceptable names are Subject, Author, Manager, Keywords, Category, Company and Description.
    </p>
    <p>
      The attribute &quot;header-maxlevel&quot; is used to define the maximum of nodes until which &quot;Heading N&quot; styles are used. If the attribute is not defined, the default value is 4. The maximum possible is 9.
    </p>
  </body>
</html></richcontent>
<attribute_layout NAME_WIDTH="91" VALUE_WIDTH="91"/>
<attribute NAME="doc-Subject" VALUE="TheSubject"/>
<attribute NAME="doc-Author" VALUE="TheAuthor"/>
<attribute NAME="doc-Manager" VALUE="TheManager"/>
<attribute NAME="doc-Keywords" VALUE="TheKeywords"/>
<attribute NAME="doc-Category" VALUE="TheCategory"/>
<attribute NAME="doc-Company" VALUE="TheCompany"/>
<attribute NAME="doc-Description" VALUE="TheDescription"/>
<attribute NAME="header-maxlevel" VALUE="4"/>
<node CREATED="1216974528086" ID="ID_1996762094" MODIFIED="1216974692827" POSITION="left" TEXT="Chapter 1">
<node CREATED="1216974536680" ID="ID_418841879" MODIFIED="1216974708501" TEXT="Chapter 1.1">
<node CREATED="1216974544352" ID="ID_1231871458" MODIFIED="1216991404490" TEXT="Chapter 1.1.1">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      This is a note belonging to Chapter 1.1.1, such notes are exported with style &quot;Body Text&quot; but any formatting,
    </p>
    <p>
      or even new lines are lost. That's sad but that's reality.
    </p>
  </body>
</html></richcontent>
<node CREATED="1216974561800" ID="ID_35441158" MODIFIED="1216974730363" TEXT="Chapter 1.1.1.1">
<node CREATED="1216974620653" ID="ID_1657992058" MODIFIED="1216991329486" TEXT="Text wich is"/>
<node CREATED="1216974660607" ID="ID_1076025767" MODIFIED="1216991352258" TEXT="deeper than the"/>
<node CREATED="1216974664012" ID="ID_1612257345" MODIFIED="1216991345298" TEXT="header-maxlevel attribute"/>
<node CREATED="1216974667197" ID="ID_1877504467" MODIFIED="1216991366458" TEXT="is exported with &quot;Normal&quot; style."/>
</node>
<node CREATED="1216974674739" ID="ID_843043724" MODIFIED="1217604631678" TEXT="This nodes will be exported as a normal paragraph">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      By marking a node with the attribute 'NoHeading' (the value is not important), you make sure that this chapter will be exported as normal paragraph, together with all nodes below.
    </p>
  </body>
</html>
</richcontent>
<attribute_layout NAME_WIDTH="62" VALUE_WIDTH="91"/>
<attribute NAME="NoHeading" VALUE=""/>
<node CREATED="1217604758817" ID="ID_863632446" MODIFIED="1217604766680" TEXT="Like also this one"/>
</node>
</node>
</node>
<node CREATED="1216974696283" ID="ID_1342553402" MODIFIED="1217604572992" TEXT="Chapter 1.2 - mark a header as last heading">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      By marking a node with the attribute 'LastHeading' (the value is not important), you make sure that this chapter will be exported as the last heading in the hierarchy, i.e. all nodes below the chapter will be exported as normal paragraphs.
    </p>
  </body>
</html>
</richcontent>
<attribute_layout NAME_WIDTH="69" VALUE_WIDTH="91"/>
<attribute NAME="LastHeading" VALUE=""/>
<node CREATED="1217603132140" ID="ID_1323406791" MODIFIED="1217603515832" TEXT="this node becomes a normal paragraph&#xa;even though it&apos;s above the defaultlevel">
<node CREATED="1217603804767" ID="ID_630190221" MODIFIED="1217603812619" TEXT="And this one as well"/>
</node>
<node CREATED="1217603814001" ID="ID_1067471434" MODIFIED="1217603819328" TEXT="And also this one"/>
</node>
</node>
<node CREATED="1216991067197" ID="ID_334419387" MODIFIED="1216991070354" POSITION="left" TEXT="Chapter 2"/>
<node CREATED="1216809914482" ID="ID_1308741003" MODIFIED="1216991809773" POSITION="right" TEXT="Chapter 3 - how to export a mindmap to MS Word ?">
<node CREATED="1216809917636" ID="ID_199484608" MODIFIED="1216991907919" TEXT="Chapter 3.1 - create a map following the notes and hints expressed in this example map"/>
<node CREATED="1216809921221" ID="ID_1681718272" MODIFIED="1216991918173" TEXT="Chapter 3.2 - export the map using the File -&gt; Export -&gt; Using XSLT... menu">
<node CREATED="1216826868748" ID="ID_1660904657" MODIFIED="1216991964598" TEXT="Chapter 3.2.1 - select the mm2wordml_utf8.xsl XSL file from the accessories directory in the Freeplane base directory."/>
<node CREATED="1216826924521" ID="ID_1561412985" MODIFIED="1216991975934" TEXT="Chapter 3.2.2 - export to a file with a name ending in .doc (or .xml)"/>
</node>
<node CREATED="1216826940554" ID="ID_769680777" MODIFIED="1216991935017" TEXT="Chapter 3.3 - just double click in the Explorer on the newly created file and Microsoft Office Word should open the file properly.">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      You need a version of MS Project supporting XML, I think MS Project 2003 and later.
    </p>
  </body>
</html></richcontent>
</node>
<node CREATED="1216827072099" ID="ID_785390572" MODIFIED="1216991949417" TEXT="Chapter 3.4 - you&apos;re done, enjoy!"/>
</node>
<node CREATED="1216991668227" ID="ID_1657343694" MODIFIED="1216991670530" POSITION="right" TEXT="Chapter 4"/>
</node>
</map>
