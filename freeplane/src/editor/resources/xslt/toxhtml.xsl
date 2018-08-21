<?xml version="1.0" encoding="UTF-8"?>
<!--

 The contents of this file are subject to the Mozilla Public License Version
 1.1 (the "License"); you may not use this file except in compliance with
 the License. You may obtain a copy of the License at
 http://www.mozilla.org/MPL/
 
 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 for the specific language governing rights and limitations under the
 License.

mmTree - alternate XHTML+Javascript export style sheet for Freeplane.

 Miika Nurminen (minurmin@cc.jyu.fi) 12.12.2003.

Transforms Freeplane (0.6.7 - 0.8.0) mm file to XHTML 1.1 with JavaScript-based keyboard navigation (MarkTree).
Output is valid (possibly apart HTML entered by user in Freeplane).

Update (MN / 14.12.2004): 
 - Support for mm 0.7.1 - 0.8.0 constructs (clouds, internal links. opens internal link also if collapsed).
 - Support for icons. Some code adapted from Markus Brueckner's freeplane2html.xsl style sheet.
 - newlines &#xa;&#xa; behaviour (find and convert to <br/>)
	
Bug fix (FC/ 25.04.2006): 
 - Export of local hyperlinks corrected.

Update (EWL / 2006-06-02):
 - add export of notes & attributes
 - re-format/re-arrange/modularize the file to align with freeplane2html.xsl

Todo:
 - Can CSS fonts be used with Freeplane fonts?
 - Change licence to MIT -style.
 - integrate JS file to stylesheet
 - parameters: use_icons (true/false)
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

<!-- fc, 17.10.2004: The following parameter is set by freeplane. -->
<xsl:param name="destination_dir">./</xsl:param>

<!-- if true, external links urls are shown, default is false. -->
<xsl:param name="show_link_url">false</xsl:param>

<!-- if false, does not show standard freeplane icons
(assumed to be in ./icons directory), default is true -->
<xsl:param name="show_icons">true</xsl:param>

<xsl:strip-space elements="*" />
<!-- note! nonempty links are required for opera! (tested with opera 7).
     #160 is non-breaking space.  / mn, 11.12.2003 -->

<!-- ### THE ROOT TEMPLATE ### -->

<xsl:template match="/">

<xsl:processing-instruction name="xml-stylesheet">href="treestyles.css" type="text/css"</xsl:processing-instruction>
<html xmlns="http://www.w3.org/1999/xhtml">
  <xsl:comment>This file has been created with toxhtml.xsl</xsl:comment>
<!-- Thanks to gulpman, wolfgangradke: -->
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>


	<title><xsl:call-template name="output-title" /></title>
	<xsl:element name="link">
		<xsl:attribute name="rel">stylesheet</xsl:attribute>
		<xsl:attribute name="href">
			<xsl:value-of select="$destination_dir"/>treestyles.css</xsl:attribute>
		<xsl:attribute name="type">text/css</xsl:attribute>
	</xsl:element>
	<xsl:element name="script">
		<xsl:attribute name="type">text/javascript</xsl:attribute>
		<xsl:attribute name="src">
			<xsl:value-of select="$destination_dir"/>marktree.js</xsl:attribute>&#160;
	</xsl:element>
</head>

<body>

<div class="basetop">
<a href="#" onclick="expandAll(document.getElementById('base'))">Expand</a> -
<a href="#" onclick="collapseAll(document.getElementById('base'))">Collapse</a>
</div>

<div id="base" class="basetext">
<ul>

<xsl:apply-templates />

</ul>
</div>

</body>
</html>
</xsl:template>

<!-- ### THE MATCHED TEMPLATES ### -->

<xsl:template match="node">
	<xsl:if test="count(child::node)=0"> 
		<xsl:call-template name="listnode">
			<xsl:with-param name="lifold">basic</xsl:with-param>
		</xsl:call-template>
	</xsl:if>
	<xsl:if test="count(child::node)>0" > 
		<xsl:choose>
		<xsl:when test="@FOLDED='true'">
			<xsl:call-template name="listnode">
			<xsl:with-param name="lifold">exp</xsl:with-param>
			<xsl:with-param name="ulfold">sub</xsl:with-param>
			</xsl:call-template>
		</xsl:when>
		<xsl:otherwise>
			<xsl:call-template name="listnode">
			<xsl:with-param name="lifold">col</xsl:with-param>
			<xsl:with-param name="ulfold">subexp</xsl:with-param>
			</xsl:call-template>
		</xsl:otherwise>
		</xsl:choose>
	</xsl:if>
</xsl:template> <!-- xsl:template match="node" -->

<xsl:template name="listnode">
<xsl:param name="lifold" /> <xsl:param name="ulfold" />
	<xsl:text>
	</xsl:text> <!-- adds a line-break in the html code -->
	<li class="{$lifold}">
	<xsl:if test="cloud/@COLOR">
		<xsl:attribute name="style">background-color:<xsl:value-of select="cloud/@COLOR" />;</xsl:attribute>
	</xsl:if>
	<!-- check if this node has an ID (for the document internal links) -->
	<xsl:if test="@ID">
	<!-- note: as Freeplane sometimes prepends the IDs with an underscore
	which is not valid as the first character in an HTML id,
	we surround the ID with FM<ID>FM -->
		<xsl:attribute name="id">FM<xsl:value-of select="@ID"/>FM</xsl:attribute>
	</xsl:if>
	<xsl:call-template name="output-icons" />
	<xsl:choose>
	<xsl:when test="richcontent[@TYPE='NOTE'] or attribute">
		<div class="boxed">
			<xsl:call-template name="output-node" />
			<div class="note-and-attributes">
				<xsl:call-template name="output-note" />
				<xsl:call-template name="output-attributes" />
			</div>
		</div>
	</xsl:when>
	<xsl:otherwise>
		<xsl:call-template name="output-node" />
	</xsl:otherwise>
	</xsl:choose>
	<xsl:if test="child::node"> 
		<xsl:text>
		</xsl:text> <!-- adds a line-break in the html code -->
		<ul class="{$ulfold}"><xsl:apply-templates select="node"/></ul>
	</xsl:if>
	<!-- if there are arrowlinks inside this node (i.e. this node is
	connected to another node in Freeplane using an arrow), then create a
	document internal link -->
	<xsl:if test="child::arrowlink">
		<xsl:call-template name="output-arrowlinks" />
	</xsl:if>
	</li>
</xsl:template> <!-- xsl:template name="listnode" -->

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
		</xsl:choose>
		<xsl:if test="richcontent[@TYPE='DETAILS']">
			<xsl:apply-templates select="richcontent[@TYPE='DETAILS']/html/body" mode="richcontent" />
		</xsl:if>
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

<xsl:template name="output-title">
	<!-- look if there is any node inside the map (there should never be
		none, but who knows?) and take its text as the title -->
	<xsl:choose>
	<xsl:when test="/map/node/@TEXT">
		<xsl:value-of select="normalize-space(/map/node/@TEXT)" />
	</xsl:when>
	<xsl:when test="/map/node/richcontent[@TYPE='NODE']">
		<xsl:variable name="t">
			<xsl:apply-templates select="/map/node/richcontent[@TYPE='NODE']/html/body" mode="strip-tags" />			
		</xsl:variable>
		<xsl:value-of select="normalize-space($t)" />
	</xsl:when>
	<xsl:otherwise>
		<xsl:text>Mind Map</xsl:text>
	</xsl:otherwise>
	</xsl:choose>
</xsl:template>


<xsl:template match="text()|@*"  mode="strip-tags">
	  <xsl:value-of select="string(.)"/>
</xsl:template>


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
					<xsl:value-of select="$destination_dir"/>icons/<xsl:value-of select="@src" /></xsl:attribute>
				<xsl:attribute name="alt">
					<xsl:value-of select="@BUILTIN" />
				</xsl:attribute>
				<xsl:attribute name="height">
					<xsl:value-of select="@height" />
				</xsl:attribute>
			</xsl:element>
			<xsl:text> </xsl:text>
		</xsl:for-each>
	</xsl:if>
</xsl:template> <!-- xsl:template name="output-icons" -->

</xsl:stylesheet>
