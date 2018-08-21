<?xml version="1.0" encoding="UTF-8"?>

<!--
	File:        freeplane2html.xsl
	Version:     0.8.1
	Description: A XSLT stylesheet to transform mindmap files created with
	Freeplane (http://freeplane.sf.net) into HTML files. The
	transformation will keep the structure of the files, clouds
	(with it's colors), icons, internal and external links and the ability
	to collapse whole subtrees of the document (with JavaScript enabled).
	The results of the transformation were tested and found to be working
	in the following browsers:
		- Internet Explorer 6
		- Mozilla Firefox 0.9 (should be working with nearly any
		  browser using the Geko engine)
		- Konqueror
		- Opera 7
	Other browsers were not tested, but you should have a good chance of
	gettting things to work with them.
	Usage:     Use any XSLT-Processor (development was done using xsltproc
	under Linux) to apply this stylesheet to the Freeplane-file. Copy the
	result and all the PNG-Files inside the script directory
	(including the icons-subdir) into a directory of it's own
	(e.g. on a webserver).
	Open the HTML-file with a webbrowser.
	Author:   Markus Brueckner <freeplane-xsl@slash-me.net>
	License:  BSD license without advertising clause. (see
	http://www.opensource.org/licenses/bsd-license.php for further details)
	Bug fix (FC/ 25.04.2006):
	- Export of local hyperlinks corrected.
	Update (EWL / 2006-06-02):
	 - add export of notes & attributes
	 - re-format/re-arrange/modularize the file to align with tohtml.xsl
-->
<xsl:stylesheet version="1.0"
                xmlns="http://www.w3.org/1999/xhtml"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		>
<!-- mozilla doesn't parse method xhtml (in xslt 2.0) -->
<xsl:output method="xml"
            version="1.0"
            encoding="UTF-8"
            doctype-public="-//W3C//DTD XHTML 1.1//EN"
            doctype-system="http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd"
	    omit-xml-declaration="no"
	    />

<!-- fc, 20.10.2004: The following parameter is set by freeplane. -->
<xsl:param name="destination_dir">./</xsl:param>
<xsl:param name="title">Mind Map</xsl:param><xsl:param name="area_code"></xsl:param>
<xsl:param name="folding_type">html_export_no_folding</xsl:param>
	<!-- possible values:
		html_export_fold_all,
		html_export_no_folding,
		html_export_fold_currently_folded,
		html_export_based_on_headings: this means, that approx. five levels are given, more deeper nodes are folded.
		As of the time being, this parameter is not used.
		-->
<!-- if false, does not show standard freeplane icons
(assumed to be in ./icons directory), default is true -->
<xsl:param name="show_icons">true</xsl:param>
<!-- if true, external links urls are shown, default is false. -->
<xsl:param name="show_link_url">false</xsl:param>

<!-- ### THE ROOT TEMPLATE ### -->

<xsl:template match="/">
<html>
<!-- Thanks to gulpman: -->
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>

<xsl:comment>This file has been created with freeplane2html.xsl</xsl:comment>
<head>
	<title><xsl:call-template name="output-title" /></title>
	<!-- Stylesheet, generator and some JavaScript for the collapsing of
		the trees -->
	<xsl:element name="link">
		<xsl:attribute name="rel">stylesheet</xsl:attribute>
		<xsl:attribute name="href">
			<xsl:value-of select="$destination_dir"/>freeplane2html.css</xsl:attribute>
		<xsl:attribute name="type">text/css</xsl:attribute>
	</xsl:element>
        <meta name="generator" content="Freeplane-XSL Stylesheet (see: http://freeplane-xsl.dev.slash-me.net/ for details)" />
	<xsl:element name="script">
		<xsl:attribute name="type">text/javascript</xsl:attribute>
		<xsl:attribute name="src">
			<xsl:value-of select="$destination_dir"/>freeplane2html.js</xsl:attribute>&#160;
	</xsl:element>
	<script type="text/javascript">
		<xsl:comment>
          <![CDATA[
               function toggle(id)
               {
                   div_el = document.getElementById(id);
                   img_el = document.getElementById('img'+id);
                   if (div_el.style.display != 'none')
                   {
          ]]>

                      div_el.style.display='none';
                      img_el.src = '<xsl:value-of select="$destination_dir"/>show.png';
          <![CDATA[
                   }
                   else
                   {
          ]]>
                      div_el.style.display='block';
                      img_el.src = '<xsl:value-of select="$destination_dir"/>hide.png';
          <![CDATA[
                   };
               };
          ]]>
          </xsl:comment>
	</script>
</head>

<body>
	<h1><xsl:call-template name="output-title" /></h1>
	<!-- place image -->
	<div style="width:96%; 	padding:2%; 	margin-bottom:10px; 	border: 0px; 	text-align:center; 	vertical-align:center;">
		<xsl:element name="img">
			<xsl:attribute name="src">
				<xsl:value-of select="$destination_dir"/>image.png</xsl:attribute>
			<xsl:attribute name="style">margin-bottom:10px; 	border: 0px; 	text-align:center; 	vertical-align:center;</xsl:attribute>
			<xsl:attribute name="alt">Imagemap</xsl:attribute>
			<xsl:attribute name="usemap">#fm_imagemap</xsl:attribute>
		</xsl:element>
	</div>
	<map name="fm_imagemap" id="fm_imagemap">
		<xsl:value-of select="$area_code" disable-output-escaping="yes"/>
	</map>
	<xsl:apply-templates />
</body>

</html>
</xsl:template> <!-- xsl:template match="/" -->

<!-- the template to output for each node -->
<xsl:template match="node">
<div>
	<!-- generate a unique ID that can be used to reference this node
		e.g. from the JavaScript -->
	<xsl:variable name="contentID">
		<xsl:value-of select="generate-id()"/>
	</xsl:variable>
	<!-- check whether this node is a cloud... -->
	<xsl:choose>
	<xsl:when test="cloud">
		<!-- ...if yes, check whether it has a special color... -->
		<xsl:choose>
		<xsl:when test="cloud/@COLOR">
			<xsl:attribute name="class">cloud</xsl:attribute>
			<xsl:attribute name="style">background-color:<xsl:value-of select="cloud/@COLOR" /></xsl:attribute>
		</xsl:when>
		<!-- no? Then choose some default color -->
		<xsl:otherwise>
			<xsl:attribute name="class">cloud</xsl:attribute>
		</xsl:otherwise>
		</xsl:choose>
	</xsl:when>
	<xsl:otherwise>
		<xsl:attribute name="class">node</xsl:attribute>
	</xsl:otherwise>
	</xsl:choose>
	<!-- check whether this node has any child nodes... -->
	<xsl:choose>
	<xsl:when test="node">
		<!-- ...yes? Then put the "hide" button in front of the text...
			-->
		<!--<img src="hide.png" class="hideshow" alt="hide">-->
		<xsl:element name="img">
			<xsl:attribute name="src">
				<xsl:value-of select="$destination_dir"/>hide.png</xsl:attribute>
			<xsl:attribute name="class">hideshow</xsl:attribute>
			<xsl:attribute name="alt">hide</xsl:attribute>
			<xsl:attribute name="onClick"><![CDATA[toggle("]]><xsl:value-of select="$contentID" /><![CDATA[")]]></xsl:attribute>
			<xsl:attribute name="id">img<xsl:value-of select="$contentID" /></xsl:attribute>
		</xsl:element>
		<!--</img>-->
	</xsl:when>
	<xsl:otherwise>
		<!-- ...no? Then output the empty leaf icon -->
		<!--<img src="leaf.png" class="hideshow" alt="leaf" />-->
		<xsl:element name="img">
			<xsl:attribute name="src">
				<xsl:value-of select="$destination_dir"/>leaf.png</xsl:attribute>
			<xsl:attribute name="class">hideshow</xsl:attribute>
			<xsl:attribute name="alt">leaf</xsl:attribute>
		</xsl:element>
	</xsl:otherwise>
	</xsl:choose>
	<xsl:call-template name="output-icons" />
	<!-- check if this node has an ID (for the document internal links) -->
	<xsl:if test="@ID">
		<!-- note: as Freeplane sometimes prepends the IDs with an
			underscore which is not valid as the first character
			in an HTML id, we surround the ID with FM<ID>FM -->
		<a>
			<xsl:attribute name="id">FM<xsl:value-of select="@ID"/>FM</xsl:attribute>
		</a>
	</xsl:if>
	<xsl:call-template name="output-node" />
	<!-- if there are arrowlinks inside this node (i.e. this node is
		connected to another node in Freeplane using an arrow), then
		create a document internal link -->
	<xsl:if test="child::arrowlink">
		<xsl:call-template name="output-arrowlinks" />
	</xsl:if>
	<!-- Output the note and attributes -->
	<xsl:if test="richcontent[@TYPE='NOTE'] or attribute">
		<div class="note-and-attributes">
			<xsl:call-template name="output-note" />
			<xsl:call-template name="output-attributes" />
		</div>
	</xsl:if>
	<!-- the content div. This div contains all subnodes of this node.
		It carries the unique ID created in the beginning (which is
		used to hide this div when necessary). The content node
		is only created if there are any subnodes -->
	<xsl:if test="node">
		<div class="content">
			<xsl:attribute name="id"><xsl:value-of select="$contentID" /></xsl:attribute>
			<xsl:apply-templates select="node"/>
		</div>
	</xsl:if>
</div>
</xsl:template> <!-- xsl:template match="node" -->

<!-- ### XHTML LIBRARY ### -->
<!-- (this part could be extracted and 'import'ed from toxhtml.xsl and
     freeplanetohtml.xsl if there wouldn't be issues with the path -->

<xsl:template match="font">
	<xsl:if test="string-length(@SIZE) > 0">font-size:<xsl:value-of select="round((number(@SIZE) div 12)*100)" />%;</xsl:if><xsl:if test="@BOLD='true'">font-weight:bold;</xsl:if><xsl:if test="@ITALIC='true'">font-style:italic;</xsl:if>
</xsl:template>

<xsl:template name="output-node">
	<xsl:element name="div">
		<xsl:attribute name="class">nodecontent</xsl:attribute>
		<xsl:if test="@COLOR or @BACKGROUND_COLOR or font">
			<xsl:attribute name="style">
				<xsl:if test="@COLOR">color:<xsl:value-of select="@COLOR" />;</xsl:if>
				<xsl:if test="@BACKGROUND_COLOR">background-color:<xsl:value-of select="@BACKGROUND_COLOR" />;</xsl:if>
				<xsl:apply-templates select="font" />
			</xsl:attribute>
		</xsl:if>
		<xsl:choose>
		<xsl:when test="@LINK">
			<xsl:call-template name="output-node-with-link" />
		</xsl:when>
		<xsl:otherwise>
			<xsl:call-template name="output-nodecontent" />
		</xsl:otherwise>
		</xsl:choose>
	</xsl:element>
</xsl:template> <!-- xsl:template name="output-node" -->

<xsl:template name="output-node-with-link">
	<xsl:choose>
	<xsl:when test="not($show_link_url='true')">
		<xsl:variable name="link">
			<xsl:choose>
			<!-- test for local hyperlinks. -->
			<xsl:when test="starts-with(@LINK, '#')">#FM<xsl:value-of select="substring(@LINK,2)" />FM</xsl:when>
			<xsl:otherwise><xsl:value-of select="@LINK" /></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:element name="a">
			<xsl:attribute name="href">
				<xsl:value-of select="$link" />
			</xsl:attribute>
			<xsl:call-template name="output-nodecontent" />
		</xsl:element>
		<xsl:if test="not($show_icons='false')">
			<xsl:text> </xsl:text>
			<xsl:element name="a">
				<xsl:attribute name="href">
					<xsl:value-of select="$link"/>
				</xsl:attribute>
				<xsl:element name="img">
					<xsl:attribute name="src"><xsl:value-of select="$destination_dir"/>ilink.png</xsl:attribute>
					<xsl:attribute name="alt">User Link</xsl:attribute>
					<xsl:attribute name="style">border-width:0</xsl:attribute>
				</xsl:element>
			</xsl:element>
		</xsl:if>
	</xsl:when>
	<xsl:otherwise>
		<xsl:call-template name="output-nodecontent" />
	</xsl:otherwise>
	</xsl:choose>
	<xsl:if test="$show_link_url='true'">
		- [ <a><xsl:attribute name="href"><xsl:value-of select="@LINK" />
		</xsl:attribute><xsl:value-of select="@LINK"/></a> ]
	</xsl:if>
</xsl:template> <!-- xsl:template name="output-node-with-link" -->

<xsl:template name="output-nodecontent">
		<xsl:choose>
		<xsl:when test="richcontent[@TYPE='NODE']">
			<xsl:apply-templates select="richcontent[@TYPE='NODE']/html/body" mode="richcontent" />
		</xsl:when>
		<xsl:otherwise>
			<xsl:call-template name="textnode" />
		</xsl:otherwise>
		</xsl:choose>		<xsl:if test="richcontent[@TYPE='DETAILS']">			<xsl:apply-templates select="richcontent[@TYPE='DETAILS']/html/body" mode="richcontent" />		</xsl:if>
</xsl:template> <!-- xsl:template name="output-nodecontent" -->

<xsl:template match="body" mode="richcontent">
	<xsl:copy-of select="*|text()"/>
</xsl:template> <!-- xsl:template name="htmlnode" -->

<xsl:template name="textnode">
	<xsl:call-template name="format_text">
		<xsl:with-param name="nodetext">
			<xsl:value-of select="@TEXT" />
		</xsl:with-param>
	</xsl:call-template>
</xsl:template> <!-- xsl:template name="textnode" -->

	<xsl:template name="output-title">		<!-- look if there is any node inside the map (there should never be			none, but who knows?) and take its text as the title -->		<xsl:choose>		<xsl:when test="/map/node/@TEXT">			<xsl:value-of select="normalize-space(/map/node/@TEXT)" />		</xsl:when>		<xsl:when test="/map/node/richcontent[@TYPE='NODE']">			<xsl:variable name="t">				<xsl:apply-templates select="/map/node/richcontent[@TYPE='NODE']/html/body" mode="strip-tags" />						</xsl:variable>			<xsl:value-of select="normalize-space($t)" />		</xsl:when>		<xsl:otherwise>			<xsl:text>Mind Map</xsl:text>		</xsl:otherwise>		</xsl:choose>	</xsl:template>		<xsl:template match="text()|@*"  mode="strip-tags">		  <xsl:value-of select="string(.)"/>	</xsl:template>
<!-- replace ASCII line breaks through HTML line breaks (br) -->
<xsl:template name="format_text">
	<xsl:param name="nodetext" />
	<xsl:if test="string-length(substring-after($nodetext,'&#xa;')) = 0">
		<xsl:value-of select="$nodetext" />
	</xsl:if>
	<xsl:if test="string-length(substring-after($nodetext,'&#xa;')) > 0">
		<xsl:value-of select="substring-before($nodetext,'&#xa;')" />
		<br />
		<xsl:call-template name="format_text">
			<xsl:with-param name="nodetext">
				<xsl:value-of select="substring-after($nodetext,'&#xa;')" />
			</xsl:with-param>
		</xsl:call-template>
	</xsl:if>
</xsl:template> <!-- xsl:template name="format_text" -->

<xsl:template name="output-note">
	<xsl:if test="richcontent[@TYPE='NOTE']">
		<span class="note">
			<xsl:apply-templates select="richcontent[@TYPE='NOTE']/html/body" mode="richcontent" />
		</span>
	</xsl:if>
</xsl:template> <!-- xsl:template name="output-note" -->

<xsl:template name="output-attributes">
	<xsl:if test="attribute">
		<table class="attributes" summary="Attributes Names and Values">
			<caption>Attributes</caption>
			<tr><th>Name</th><th>Value</th></tr>
			<xsl:for-each select="attribute">
				<tr>
				<td><xsl:value-of select="@NAME" /></td>
				<td><xsl:value-of select="@VALUE" /></td>
				</tr>
			</xsl:for-each>
		</table>
	</xsl:if>
</xsl:template> <!-- xsl:template name="output-attributes" -->

<!-- function is defined in such a way that an undefined show_icons variable
	leads to output of the icons (important for reuse) -->
<xsl:template name="output-arrowlinks">
	<xsl:if test="$show_icons='false'"> - [ </xsl:if>
	<xsl:for-each select="arrowlink">
		<xsl:text> </xsl:text>
		<a>
			<xsl:attribute name="onclick">getVisibleParents('FM<xsl:value-of select="@DESTINATION" />FM')</xsl:attribute>
			<xsl:attribute name="href">#FM<xsl:value-of select="@DESTINATION" />FM</xsl:attribute>
			<xsl:choose>
			<xsl:when test="$show_icons='false'">
				<xsl:value-of
					select="concat('&amp;','rArr',';')"
					disable-output-escaping="yes" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:element name="img">
					<xsl:attribute name="src"><xsl:value-of select="$destination_dir"/>ilink.png</xsl:attribute>
					<xsl:attribute name="class">ilink</xsl:attribute>
					<xsl:attribute name="alt">Connector</xsl:attribute>
				</xsl:element>
			</xsl:otherwise>
			</xsl:choose>
		</a>
	</xsl:for-each>
	<xsl:if test="$show_icons='false'"> ] </xsl:if>
</xsl:template> <!-- xsl:template name="output-arrowlinks" -->

<xsl:template name="output-icons">
	<xsl:if test="not($show_icons='false')">
		<xsl:for-each select="icon">
			<xsl:element name="img">
				<xsl:attribute name="src">
					<xsl:value-of select="$destination_dir"/>icons/<xsl:value-of select="@BUILTIN" />.png</xsl:attribute>
				<xsl:attribute name="alt">
					<xsl:value-of select="@BUILTIN" />
				</xsl:attribute>
			</xsl:element>
			<xsl:text> </xsl:text>
		</xsl:for-each>
	</xsl:if>
</xsl:template> <!-- xsl:template name="output-icons" -->

</xsl:stylesheet>
