<?xml version="1.0" encoding="UTF-8"?>
<!--
/*Freeplane - A Program for creating and viewing Mindmaps
 *Copyright (C) 2000-2008  Christian Foltin and others.
 *
 *See COPYING for Details
 *
 *This program is free software; you can redistribute it and/or
 *modify it under the terms of the GNU General Public License
 *as published by the Free Software Foundation; either version 2
 *of the License, or (at your option) any later version.
 *
 *This program is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU General Public License for more details.
 *
 *You should have received a copy of the GNU General Public License
 *along with this program; if not, write to the Free Software
 *Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This stylesheet is for generating `content.xml` for ODT-Files (Open Document Text),
 * used for exporting to OpenOffice/LibeOffice Writer documents.
 */

Formatting rules used in this style-sheet
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
* The root node will get style "Title".

* Nodes with "automatic node-numbering" set will become numbered
  list-items.

  Please note: If some but not all sibling nodes have "automatic
               node-numbering" set, the result is unpredicted.

* Children (and grand-children) of folded nodes will become nested
  bullet lists, except if a node has "always unfold node" set. In these
  case it's children will not become list-items, but normal
  paragraphs, but the grand-children will be list-items again.

* Automatic layout modes are honored, both non-leaf-mode and
  all-nodes-mode. Nodes will become headings of the corresponding
  level. As in the map, four levels are used.

  Please note: Automatic layout mode overrules nodes styles, as it
               does in the map. But node folding and auto-numbering
               overrules automatic layout mode.

* Automatic layout level styles (per-defined styles with names "Level
  ...") are honored. These will become headings of the corresponding
  level.

* If the node has a style set (either a pre-defined or a custom), the
  node will become a paragraph with this style set. The document will
  contain a style with the same name which tries to mimic the style in
  the map.

* If the node does not have a style set, it will become "Text body".

* The note and details of a node will get a style named after the
  node's style with " Node" resp. " Details" appended. This style
  will the the node's style as parent-style.

Please note: Formats applied on a node-level (using the "Format"
panel), will not be transferred to the Open Document Format.

Rich-text nodes are converted, too. If you have some hand-crafted HTML
in the rech-text, the result may not be what you expect, as not all
cases of wrong-formatted HTML is handled. If using text-nodes and the
formatting features described above, you'll be on the safe side.

Not implemented
~~~~~~~~~~~~~~~~~~~
- Tables (may occur in rich-text nodes)
- Pictures
- Icons for nodes (will most probably never be implemented).

-->
<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/1999/xhtml"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
	xmlns:style="urn:oasis:names:tc:opendocument:xmlns:style:1.0"
	xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
	xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0"
	xmlns:draw="urn:oasis:names:tc:opendocument:xmlns:drawing:1.0"
	xmlns:fo="urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0"
	xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:dc="http://purl.org/dc/elements/1.1/"
	xmlns:meta="urn:oasis:names:tc:opendocument:xmlns:meta:1.0"
	xmlns:number="urn:oasis:names:tc:opendocument:xmlns:datastyle:1.0"
	xmlns:svg="urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0"
	xmlns:chart="urn:oasis:names:tc:opendocument:xmlns:chart:1.0"
	xmlns:dr3d="urn:oasis:names:tc:opendocument:xmlns:dr3d:1.0" xmlns:math="http://www.w3.org/1998/Math/MathML"
	xmlns:form="urn:oasis:names:tc:opendocument:xmlns:form:1.0"
	xmlns:script="urn:oasis:names:tc:opendocument:xmlns:script:1.0"
	xmlns:ooo="http://openoffice.org/2004/office" xmlns:ooow="http://openoffice.org/2004/writer"
	xmlns:oooc="http://openoffice.org/2004/calc" xmlns:dom="http://www.w3.org/2001/xml-events"
	xmlns:xforms="http://www.w3.org/2002/xforms" xmlns:xsd="http://www.w3.org/2001/XMLSchema"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
	<xsl:output method="xml" version="1.0" indent="yes"
		encoding="UTF-8" omit-xml-declaration="no" />
	<xsl:strip-space elements="*" />

	<xsl:template match="map">
		<office:document-content
			xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
			xmlns:style="urn:oasis:names:tc:opendocument:xmlns:style:1.0"
			xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
			xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0"
			xmlns:draw="urn:oasis:names:tc:opendocument:xmlns:drawing:1.0"
			xmlns:fo="urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0"
			xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:dc="http://purl.org/dc/elements/1.1/"
			xmlns:meta="urn:oasis:names:tc:opendocument:xmlns:meta:1.0"
			xmlns:number="urn:oasis:names:tc:opendocument:xmlns:datastyle:1.0"
			xmlns:svg="urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0"
			xmlns:chart="urn:oasis:names:tc:opendocument:xmlns:chart:1.0"
			xmlns:dr3d="urn:oasis:names:tc:opendocument:xmlns:dr3d:1.0"
			xmlns:math="http://www.w3.org/1998/Math/MathML" xmlns:form="urn:oasis:names:tc:opendocument:xmlns:form:1.0"
			xmlns:script="urn:oasis:names:tc:opendocument:xmlns:script:1.0"
			xmlns:ooo="http://openoffice.org/2004/office" xmlns:ooow="http://openoffice.org/2004/writer"
			xmlns:oooc="http://openoffice.org/2004/calc" xmlns:dom="http://www.w3.org/2001/xml-events"
			xmlns:xforms="http://www.w3.org/2002/xforms" xmlns:xsd="http://www.w3.org/2001/XMLSchema"
			xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" office:version="1.0">
			<office:scripts />
			<office:font-face-decls>
				<style:font-face style:name="StarSymbol"
					svg:font-family="StarSymbol" />
				<style:font-face style:name="DejaVu Sans"
					svg:font-family="&apos;DejaVu Sans&apos;"
					style:font-family-generic="roman" style:font-pitch="variable" />
				<style:font-face style:name="DejaVu Sans1"
					svg:font-family="&apos;DejaVu Sans&apos;"
					style:font-family-generic="swiss" style:font-pitch="variable" />
				<style:font-face style:name="DejaVu Sans2"
					svg:font-family="&apos;DejaVu Sans&apos;"
					style:font-family-generic="system" style:font-pitch="variable" />
			</office:font-face-decls>
			<office:automatic-styles>
				<!-- P1 = unnumbered list item -->
				<style:style style:name="P1" style:family="paragraph"
					     style:parent-style-name="List_20_1"
					     style:list-style-name="List_20_1"/>
				<!-- P2 = numbered list item -->
				<style:style style:name="P2" style:family="paragraph"
					     style:parent-style-name="Numbering_20_1"
					     style:list-style-name="Numbering_20_1" />
				<!-- P3 = center -->
				<style:style style:name="P3" style:family="paragraph"
					style:parent-style-name="Text_20_body">
					<style:paragraph-properties
						fo:text-align="center" style:justify-single-word="false" />
				</style:style>
				<!-- P4 = align right -->
				<style:style style:name="P4" style:family="paragraph"
					style:parent-style-name="Text_20_body">
					<style:paragraph-properties
						fo:text-align="end" style:justify-single-word="false" />
				</style:style>
				<!-- P5 = justify -->
				<style:style style:name="P5" style:family="paragraph"
					style:parent-style-name="Text_20_body">
					<style:paragraph-properties
						fo:text-align="justify" style:justify-single-word="false" />
				</style:style>
				<!-- automatic list and numbering styles for all style s in the map -->
				<xsl:apply-templates select="//stylenode" mode="automatic-list-style" />
				<xsl:call-template name="gen-automatic-list-style">
				  <xsl:with-param name="style">Text_20_body</xsl:with-param>
				</xsl:call-template>
				<!-- T1 = bold text -->
				<style:style style:name="T1" style:family="text">
					<style:text-properties fo:font-weight="bold"
						style:font-weight-asian="bold" style:font-weight-complex="bold" />
				</style:style>
				<!-- T2 = italic text -->
				<style:style style:name="T2" style:family="text">
					<style:text-properties fo:font-style="italic"
						style:font-style-asian="italic" style:font-style-complex="italic" />
				</style:style>
				<!-- T3 = underlined text -->
				<style:style style:name="T3" style:family="text">
					<style:text-properties
						style:text-underline-style="solid" style:text-underline-width="auto"
						style:text-underline-color="font-color" />
				</style:style>

			</office:automatic-styles>
			<office:body>
				<office:text>
					<office:forms form:automatic-focus="false"
						form:apply-design-mode="false" />
					<text:sequence-decls>
						<text:sequence-decl text:display-outline-level="0"
							text:name="Illustration" />
						<text:sequence-decl text:display-outline-level="0"
							text:name="Table" />
						<text:sequence-decl text:display-outline-level="0"
							text:name="Text" />
						<text:sequence-decl text:display-outline-level="0"
							text:name="Drawing" />
					</text:sequence-decls>
					<xsl:apply-templates select="node" />
				</office:text>
			</office:body>

		</office:document-content>
	</xsl:template>

	<!--== generate automatic list and numbering styles for all styles in the map ==-->
	<xsl:template match="stylenode[@TEXT]"
		  mode="automatic-list-style">
	  <!-- a custom style -->
	  <xsl:call-template name="gen-automatic-list-style">
	    <xsl:with-param name="style" select="translate(@TEXT, ' ', '_')" />
	  </xsl:call-template>
	  <xsl:call-template name="gen-automatic-list-style">
	    <xsl:with-param name="style" select="translate(concat(@TEXT, ' Details'), ' ', '_')" />
	  </xsl:call-template>
	  <xsl:call-template name="gen-automatic-list-style">
	    <xsl:with-param name="style" select="translate(concat(@TEXT, ' Note'), ' ', '_')" />
	  </xsl:call-template>
	</xsl:template>

	<xsl:template match="stylenode[starts-with(@LOCALIZED_TEXT,'defaultstyle.')]"
		  mode="automatic-list-style">
	  <!-- one of the Freeplane pre-defined styles -->
	  <xsl:call-template name="gen-automatic-list-style">
	    <xsl:with-param name="style" select="translate(substring-after(@LOCALIZED_TEXT,'defaultstyle.'), ' ', '_')" />
	  </xsl:call-template>
	</xsl:template>

	<xsl:template name="gen-automatic-list-style">
	  <!-- for each style in the map generate then automatic list styles -->
	  <xsl:param name="style" />

	  <!-- P1 = unnumbered list item -->
	  <style:style style:family="paragraph" style:list-style-name="List_20_1">
	    <xsl:attribute name="style:name"><xsl:value-of select="concat($style, '_P1')" /></xsl:attribute>
	    <xsl:attribute name="style:parent-style-name"><xsl:value-of select="$style" /></xsl:attribute>
	  </style:style>
	  <!-- P2 = numbered list item -->
	  <style:style style:family="paragraph" style:list-style-name="Numbering_20_1">
	    <xsl:attribute name="style:name"><xsl:value-of select="concat($style, '_P2')" /></xsl:attribute>
	    <xsl:attribute name="style:parent-style-name"><xsl:value-of select="$style" /></xsl:attribute>
	  </style:style>
	  <!-- P3 = center -->
	  <style:style style:family="paragraph">
	    <xsl:attribute name="style:name"><xsl:value-of select="concat($style, '_P3')" /></xsl:attribute>
	    <xsl:attribute name="style:parent-style-name"><xsl:value-of select="$style" /></xsl:attribute>
	    <style:paragraph-properties
		fo:text-align="center" style:justify-single-word="false" />
	  </style:style>
	  <!-- P4 = align right -->
	  <style:style style:family="paragraph">
	    <xsl:attribute name="style:name"><xsl:value-of select="concat($style, '_P4')" /></xsl:attribute>
	    <xsl:attribute name="style:parent-style-name"><xsl:value-of select="$style" /></xsl:attribute>
	    <style:paragraph-properties
		fo:text-align="end" style:justify-single-word="false" />
	  </style:style>
	  <!-- P5 = justify -->
	  <style:style style:family="paragraph">
	    <xsl:attribute name="style:name"><xsl:value-of select="concat($style, '_P5')" /></xsl:attribute>
	    <xsl:attribute name="style:parent-style-name"><xsl:value-of select="$style" /></xsl:attribute>
	    <style:paragraph-properties
		fo:text-align="justify" style:justify-single-word="false" />
	  </style:style>
	</xsl:template>

	<xsl:template name="canonical-list-style-name">
	    <xsl:param name="style" />
	    <xsl:param name="type" />
	    <xsl:choose>
	      <xsl:when test="(substring($style, string-length($style)-2, 2) = '_P') and (substring($style, string-length($style)) &lt; 6)">
		<xsl:value-of select="concat(substring($style, 1, string-length($style)-2), $type, substring($style, string-length($style)-2))"/>
	      </xsl:when>
	      <xsl:otherwise>
		<xsl:value-of select="concat($style, '_', $type)"/>
	      </xsl:otherwise>
	    </xsl:choose>
	</xsl:template>

	<xsl:template name="output-all-nodecontent">
	  <xsl:param name="style"/>
	  <xsl:param name="heading_level" select="-1"/>
	  <!-- dump the node core -->
	  <xsl:choose>
	    <xsl:when test="$heading_level &gt; 0">
	      <text:h>
		<xsl:attribute name="text:style-name"><xsl:value-of select="$style" /></xsl:attribute>
		<xsl:attribute name="text:outline-level"><xsl:value-of select="$heading_level" /></xsl:attribute>
		<xsl:call-template name="output-nodecontent">
		  <xsl:with-param name="style" /><!-- for headings the style is set by the text:h tag  -->
		</xsl:call-template>
	      </text:h>
	    </xsl:when>
	    <xsl:otherwise>
	      <xsl:call-template name="output-nodecontent">
		<xsl:with-param name="style"><xsl:value-of select="$style" /></xsl:with-param>
	      </xsl:call-template>
	    </xsl:otherwise>
	  </xsl:choose>
	  <!-- dump the details and note -->
	  <xsl:apply-templates select="hook|@LINK" />
	  <xsl:call-template name="output-notecontent">
	    <xsl:with-param name="contentType" select="'DETAILS'"/>
	    <xsl:with-param name="style">
	      <xsl:call-template name="canonical-list-style-name">
		<xsl:with-param name="style" select="$style" />
		<xsl:with-param name="type" select="'Details'" />
	      </xsl:call-template>
	    </xsl:with-param>
	  </xsl:call-template>
	  <xsl:call-template name="output-notecontent">
	    <xsl:with-param name="contentType" select="'NOTE'"/>
	    <xsl:with-param name="style">
	      <xsl:call-template name="canonical-list-style-name">
		<xsl:with-param name="style" select="$style" />
		<xsl:with-param name="type" select="'Note'" />
	      </xsl:call-template>
	    </xsl:with-param>
	  </xsl:call-template>
	  <!-- walk the sub-nodes -->
	  <xsl:choose>
	    <xsl:when test="./node[@NUMBERED='true']">
	      <text:list text:style-name="Numbering_20_1">
		<xsl:apply-templates select="node" />
	      </text:list>
	    </xsl:when>
	    <xsl:when test="(@FOLDED='true' or ancestor::node[@FOLDED='true']) and not(hook[@NAME='AlwaysUnfoldedNode']) and ./node">
	      <text:list text:style-name="List_20_1">
		<xsl:apply-templates select="node" />
	      </text:list>
	    </xsl:when>
	    <xsl:otherwise>
	      <xsl:apply-templates select="node" />
	    </xsl:otherwise>
	  </xsl:choose>
	</xsl:template>

	<xsl:template name="output-node-as-heading">
	  <xsl:param name="heading_level" select="-1"/>
	  <xsl:call-template name="output-all-nodecontent">
	    <xsl:with-param name="style">
	      <xsl:text>Heading_20_</xsl:text><xsl:value-of select="$heading_level" />
	    </xsl:with-param>
	    <xsl:with-param name="heading_level" select="$heading_level" />
	  </xsl:call-template>
	</xsl:template>


	<xsl:template match="node">
		<xsl:variable name="depth">
			<xsl:apply-templates select=".." mode="depthMesurement" />
		</xsl:variable>
		<xsl:variable name="style">
			<xsl:apply-templates select="." mode="get-node-style" />
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="$depth=0"><!-- Root Node becomes 'Title' -->
				<xsl:call-template name="output-all-nodecontent">
				  <xsl:with-param name="style">Title</xsl:with-param>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="@NUMBERED='true'">
			  <text:list-item>
			    <xsl:call-template name="output-all-nodecontent">
			      <!-- :todo: need to remove numbers added by Freeplane -->
			      <xsl:with-param name="style" select="concat($style, '_P2')" />
			    </xsl:call-template>
			  </text:list-item>
			</xsl:when>
			<xsl:when test="ancestor::node[@FOLDED='true'] and ../hook[@NAME='AlwaysUnfoldedNode']">
			  <xsl:apply-templates select="." mode="normal-node-with-style"/>
			</xsl:when>
			<xsl:when test="ancestor::node[@FOLDED='true']">
			    <text:list-item>
			      <xsl:call-template name="output-all-nodecontent">
				<xsl:with-param name="style" select="concat($style, '_P1')" />
			      </xsl:call-template>
			    </text:list-item>
			</xsl:when>
			<xsl:when test="/map/node/hook[@NAME='accessories/plugins/AutomaticLayout.properties' and @VALUE='ALL'] and $depth &lt;= 4">
			  <!-- automatic layout for all nodes up to level 4 -->
			  <xsl:call-template name="output-node-as-heading">
			    <xsl:with-param name="heading_level" select="$depth" />
			  </xsl:call-template>
			</xsl:when>
			<xsl:when test="/map/node/hook[@NAME='accessories/plugins/AutomaticLayout.properties' and @VALUE='HEADINGS'] and $depth &lt;= 4 and ./node">
			  <!-- automatic layout for non-leaf nodes up to level 4 -->
			  <xsl:call-template name="output-node-as-heading">
			    <xsl:with-param name="heading_level" select="$depth" />
			  </xsl:call-template>
			</xsl:when>
			<xsl:when test="starts-with(./@LOCALIZED_STYLE_REF,'AutomaticLayout.level,')">
			  <!-- heading style for one of the known heading levels -->
			  <xsl:call-template name="output-node-as-heading">
			    <xsl:with-param name="heading_level" select="substring-after(./@LOCALIZED_STYLE_REF,'AutomaticLayout.level,')" />
			  </xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
			  <xsl:apply-templates select="." mode="normal-node-with-style"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>


	<xsl:template mode="get-node-style"
		  match="node[@STYLE_REF]">
	  <!-- a custom style -->
	  <xsl:value-of select="translate(@STYLE_REF, ' ', '_')" />
	</xsl:template>

	<xsl:template mode="get-node-style"
		  match="node[starts-with(@LOCALIZED_STYLE_REF,'defaultstyle.')]">
	  <!-- one of the Freeplane pre-defined styles -->
	  <xsl:value-of select="substring-after(./@LOCALIZED_STYLE_REF,'defaultstyle.')" />
	</xsl:template>

	<xsl:template mode="get-node-style"
		  match="node"> <!-- no style defined -->
	  <xsl:text>Text_20_body</xsl:text>
	</xsl:template>


	<xsl:template mode="normal-node-with-style"
		  match="node[@STYLE_REF]">
	  <!-- a custom style -->
	  <xsl:call-template name="output-all-nodecontent">
	    <xsl:with-param name="style" select="translate(@STYLE_REF, ' ', '_')" />
	  </xsl:call-template>
	</xsl:template>

	<xsl:template mode="normal-node-with-style"
		  match="node[starts-with(@LOCALIZED_STYLE_REF,'defaultstyle.')]">
	  <!-- one of the Freeplane pre-defined styles -->
	  <xsl:call-template name="output-all-nodecontent">
	    <xsl:with-param name="style" select="substring-after(./@LOCALIZED_STYLE_REF,'defaultstyle.')" />
	  </xsl:call-template>
	</xsl:template>

	<xsl:template mode="normal-node-with-style"
		  match="node"> <!-- no style defined -->
	  <xsl:call-template name="output-all-nodecontent">
	    <xsl:with-param name="style">Text_20_body</xsl:with-param>
	  </xsl:call-template>
	</xsl:template>


	<xsl:template match="hook" />

	<!--
	<xsl:template match="hook[@NAME='accessories/plugins/NodeNote.properties']">
	  <xsl:choose>
	    <xsl:when test="./text">
	      <text:p text:style-name="Text_20_body"> <xsl:value-of select="./text"/> </text:p>
	    </xsl:when>
	  </xsl:choose>
	</xsl:template>
	<xsl:template match="node" mode="childoutputOrdered">
	  <xsl:param name="nodeText"></xsl:param>
	  <text:ordered-list text:style-name="L1" text:continue-numbering="true">
	    <text:list-item>
	      <xsl:apply-templates select=".." mode="childoutputOrdered">
		<xsl:with-param name="nodeText"><xsl:copy-of select="$nodeText"/></xsl:with-param>
	      </xsl:apply-templates>
	    </text:list-item>
	  </text:ordered-list>
	  </xsl:template>
	  <xsl:template match="map" mode="childoutputOrdered">
	    <xsl:param name="nodeText"></xsl:param>
	    <xsl:copy-of select="$nodeText"/>
	  </xsl:template>
	-->
	<xsl:template match="node" mode="depthMesurement">
		<xsl:param name="depth" select=" '0' " />
		<xsl:apply-templates select=".." mode="depthMesurement">
			<xsl:with-param name="depth" select="$depth + 1" />
		</xsl:apply-templates>
	</xsl:template>
	<xsl:template match="map" mode="depthMesurement">
		<xsl:param name="depth" select=" '0' " />
		<xsl:value-of select="$depth" />
	</xsl:template>


	<!-- Give links out. -->
	<xsl:template match="@LINK">
		<text:p text:style-name="Text_20_body">
			<text:a>
				<xsl:attribute name="xlink:type">simple</xsl:attribute>
				<xsl:attribute name="xlink:href">
					<xsl:value-of select="." />
				</xsl:attribute>
				<xsl:value-of select="." />
			</text:a>
		</text:p>
	</xsl:template>

	<xsl:template name="output-nodecontent">
		<xsl:param name="style">Text_20_body</xsl:param>
		<xsl:choose>
			<xsl:when test="richcontent[@TYPE='NODE']">
				<xsl:apply-templates select="richcontent[@TYPE='NODE']/html/body"
					mode="richcontent">
					<xsl:with-param name="style" select="$style" />
				</xsl:apply-templates>
			</xsl:when>
			<xsl:when test="$style = ''">
				<!--no style for headings. -->
				<xsl:call-template name="textnode" />
			</xsl:when>
			<xsl:otherwise>
				<text:p>
					<xsl:attribute name="text:style-name"><xsl:value-of
						select="$style" /></xsl:attribute>
					<xsl:call-template name="textnode" />
				</text:p>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template> <!-- xsl:template name="output-nodecontent" -->

	<xsl:template name="output-notecontent">
	  <xsl:param name="style">Text_20_body</xsl:param>
	  <xsl:param name="contentType"/>
		<xsl:if test="richcontent[@TYPE=$contentType]">
			<xsl:apply-templates select="richcontent[@TYPE=$contentType]/html/body"
				mode="richcontent">
				<xsl:with-param name="style" select="$style" />
			</xsl:apply-templates>
		</xsl:if>
	</xsl:template> <!-- xsl:template name="output-note" -->


	<!-- replace ASCII line breaks through ODF line breaks (br) -->
	<xsl:template name="textnode">
		<xsl:param name="nodetext" select="@TEXT" />
		<xsl:choose>
		  <xsl:when test="not(contains($nodetext,'&#xa;'))">
			<xsl:value-of select="$nodetext" />
		  </xsl:when>
		  <xsl:otherwise>
			<xsl:value-of select="substring-before($nodetext,'&#xa;')" />
			<text:line-break />
			<xsl:call-template name="textnode">
				<xsl:with-param name="nodetext">
					<xsl:value-of select="substring-after($nodetext,'&#xa;')" />
				</xsl:with-param>
			</xsl:call-template>
		  </xsl:otherwise>
		</xsl:choose>
	</xsl:template> <!-- xsl:template name="textnode" -->

	<xsl:template match="body" mode="richcontent">
		<xsl:param name="style">Text_20_body</xsl:param>
		<xsl:apply-templates select="text()|*" mode="richcontent">
			<xsl:with-param name="style" select="$style"></xsl:with-param>
		</xsl:apply-templates>
	</xsl:template>
	<xsl:template match="text()" mode="richcontent">
		<xsl:copy-of select="string(.)" />
	</xsl:template>
	<xsl:template match="body[text()]" mode="richcontent">
	  <text:p text:style-name="Error">
	    <text:span text:style-name="ErrorIntro">
	      <xsl:text>This rich-text node does not conform to the HTML standard. Please check your HTML code in this node.</xsl:text>
	    </text:span>
	    <xsl:copy-of select="normalize-space(.)" />
	  </text:p>
	</xsl:template>
	<xsl:template match="br" mode="richcontent">
		<text:line-break />
	</xsl:template>
	<xsl:template match="b" mode="richcontent">
		<xsl:param name="style">Text_20_body</xsl:param>
		<text:span text:style-name="T1">
			<xsl:apply-templates select="text()|*" mode="richcontent">
				<xsl:with-param name="style" select="$style"></xsl:with-param>
			</xsl:apply-templates>
		</text:span>
	</xsl:template>
	<xsl:template match="p" mode="richcontent">
		<xsl:param name="style">Text_20_body</xsl:param>
		<xsl:choose>
			<xsl:when test="$style = ''">
				<xsl:apply-templates select="text()|*" mode="richcontent">
					<xsl:with-param name="style" select="$style"></xsl:with-param>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:when test="@style='text-align: center'">
				<text:p>
					<xsl:attribute name="text:style-name">
						<xsl:value-of select="concat($style, '_P4')" />
					</xsl:attribute>
					<xsl:apply-templates select="text()|*" mode="richcontent">
						<xsl:with-param name="style" select="$style"></xsl:with-param>
					</xsl:apply-templates>
				</text:p>
			</xsl:when>
			<xsl:when test="@style='text-align: right'">
				<text:p>
					<xsl:attribute name="text:style-name">
						<xsl:value-of select="concat($style, '_P4')" />
					</xsl:attribute>
					<xsl:apply-templates select="text()|*" mode="richcontent">
						<xsl:with-param name="style" select="$style"></xsl:with-param>
					</xsl:apply-templates>
				</text:p>
			</xsl:when>
			<xsl:when test="@style='text-align: justify'">
				<text:p>
					<xsl:attribute name="text:style-name">
						<xsl:value-of select="concat($style, '_P5')" />
					</xsl:attribute>
					<xsl:apply-templates select="text()|*" mode="richcontent">
						<xsl:with-param name="style" select="$style"></xsl:with-param>
					</xsl:apply-templates>
				</text:p>
			</xsl:when>
			<xsl:otherwise>
				<text:p>
					<xsl:attribute name="text:style-name"><xsl:value-of
						select="$style" /></xsl:attribute>
					<xsl:apply-templates select="text()|*" mode="richcontent">
						<xsl:with-param name="style" select="$style"></xsl:with-param>
					</xsl:apply-templates>
				</text:p>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="i" mode="richcontent">
		<xsl:param name="style">Text_20_body</xsl:param>
		<text:span text:style-name="T2">
			<xsl:apply-templates select="text()|*" mode="richcontent">
				<xsl:with-param name="style" select="$style"></xsl:with-param>
			</xsl:apply-templates>
		</text:span>
	</xsl:template>
	<xsl:template match="u" mode="richcontent">
		<xsl:param name="style">Text_20_body</xsl:param>
		<text:span text:style-name="T3">
			<xsl:apply-templates select="text()|*" mode="richcontent">
				<xsl:with-param name="style" select="$style"></xsl:with-param>
			</xsl:apply-templates>
		</text:span>
	</xsl:template>
	<xsl:template match="ul" mode="richcontent">
		<xsl:param name="style">Text_20_body</xsl:param>
		<text:list text:style-name="List_20_1">
			<xsl:apply-templates select="text()|*" mode="richcontent">
				<xsl:with-param name="style" select="$style"></xsl:with-param>
				<xsl:with-param name="itemstyle">P1</xsl:with-param>
			</xsl:apply-templates>
		</text:list>
	</xsl:template>
	<xsl:template match="ol" mode="richcontent">
		<xsl:param name="style">Text_20_body</xsl:param>
		<text:list text:style-name="Numbering_20_1">
			<xsl:apply-templates select="text()|*" mode="richcontent">
				<xsl:with-param name="style" select="$style"></xsl:with-param>
				<xsl:with-param name="itemstyle">P2</xsl:with-param>
			</xsl:apply-templates>
		</text:list>
	</xsl:template>
	<xsl:template match="li" mode="richcontent">
		<xsl:param name="style">Text_20_body</xsl:param>
		<xsl:param name="itemstyle"/>
		<text:list-item>
			<text:p>
				<xsl:attribute name="text:style-name">
					<xsl:value-of select="concat($style, '_', $itemstyle)" />
				</xsl:attribute>
				<xsl:apply-templates select="text()|*" mode="richcontent">
					<xsl:with-param name="style" select="$style"></xsl:with-param>
				</xsl:apply-templates>
			</text:p>
		</text:list-item>
	</xsl:template>

	<!--== some work-arounds for nested lists ==-->
	<!-- list-item with contained list, nested according to XHTML: do not emit <text:p> -->
	<xsl:template match="li[ol] | li[ul]" mode="richcontent">
		<xsl:param name="style">Text_20_body</xsl:param>
		<text:list-item>
		  <xsl:apply-templates select="text()|*" mode="richcontent">
		    <xsl:with-param name="style" select="$style"></xsl:with-param>
		  </xsl:apply-templates>
		</text:list-item>
	</xsl:template>
	<!-- list contined in a list-item, nested according to XHTML: emit <text:-list-item> -->
	<xsl:template match="ul[../li]" mode="richcontent">
		<xsl:param name="style">Text_20_body</xsl:param>
		<text:list-item>
		  <text:list text:style-name="List_20_1">
			<xsl:apply-templates select="text()|*" mode="richcontent">
				<xsl:with-param name="style" select="$style"></xsl:with-param>
				<xsl:with-param name="itemstyle">P1</xsl:with-param>
			</xsl:apply-templates>
		  </text:list>
		</text:list-item>
	</xsl:template>
	<!-- list contined in a list-item, nested according to XHTML: emit <text:-list-item> -->
	<xsl:template match="ol[../li]" mode="richcontent">
		<xsl:param name="style">Text_20_body</xsl:param>
		<text:list-item>
		  <text:list text:style-name="Numbering_20_1">
			<xsl:apply-templates select="text()|*" mode="richcontent">
				<xsl:with-param name="style" select="$style"></xsl:with-param>
				<xsl:with-param name="itemstyle">P2</xsl:with-param>
			</xsl:apply-templates>
		  </text:list>
		</text:list-item>
	</xsl:template>


	<xsl:template match="a" mode="richcontent">
		<text:a>
			<xsl:attribute name="xlink:type">simple</xsl:attribute>
			<xsl:attribute name="xlink:href">
				<xsl:value-of select="@href" />
			</xsl:attribute>
			<xsl:apply-templates select="text()" />
		</text:a>
	</xsl:template>


	<!--
		<text:list-item> <text:p text:style-name="P1">b</text:p></text:list-item>
		<text:list-item> <text:p text:style-name="P1">c</text:p></text:list-item>
		<text:p text:style-name="P2"/>
	-->
	<!--
		<text:ordered-list text:style-name="L2">
		  <text:list-item><text:p text:style-name="P3">1</text:p></text:list-item>
		  <text:list-item><text:p text:style-name="P3">2</text:p></text:list-item>
		  <text:list-item><text:p text:style-name="P3">3</text:p></text:list-item>
		</text:ordered-list>
		<text:p text:style-name="P2"/>
	-->
	<!--
		Table:
		<table:table table:name="Table1" table:style-name="Table1">
		  <table:table-column table:style-name="Table1.A"
				      table:number-columns-repeated="3"/>
		  <table:table-row>
		    <table:table-cell table:style-name="Table1.A1" table:value-type="string">
		      <text:p text:style-name="Table Contents">T11</text:p>
		    </table:table-cell>
		    <table:table-cell table:style-name="Table1.A1" table:value-type="string">
		      <text:p text:style-name="Table Contents">T21</text:p>
		    </table:table-cell>
		    <table:table-cell table:style-name="Table1.C1" table:value-type="string">
		      <text:p text:style-name="Table Contents">T31</text:p>
		    </table:table-cell>
		  </table:table-row>
		  <table:table-row>
		    <table:table-cell table:style-name="Table1.A2" table:value-type="string">
		      <text:p text:style-name="Table Contents">T12</text:p>
		    </table:table-cell>
		    <table:table-cell table:style-name="Table1.A2" table:value-type="string">
		      <text:p text:style-name="Table Contents">T22</text:p>
		      </table:table-cell>
		      <table:table-cell table:style-name="Table1.C2" table:value-type="string">
			<text:p text:style-name="Table Contents">T32</text:p>
		      </table:table-cell>
		  </table:table-row>
		  <table:table-row>
		    <table:table-cell table:style-name="Table1.A2" table:value-type="string">
		      <text:p text:style-name="Table Contents">T13</text:p>
		    </table:table-cell>
		    <table:table-cell table:style-name="Table1.A2" table:value-type="string">
		      <text:p text:style-name="Table Contents">T23</text:p>
		    </table:table-cell>
		    <table:table-cell table:style-name="Table1.C2" table:value-type="string">
		      <text:p text:style-name="Table Contents">T32</text:p>
		    </table:table-cell>
		  </table:table-row>
		</table:table>
	-->


</xsl:stylesheet>
