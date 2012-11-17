<?xml version="1.0" encoding="UTF-8"?>
	<!--
		/*Freeplane - A Program for creating and viewing Mindmaps *Copyright
		(C) 2000-2008 Christian Foltin and others. * *See COPYING for Details
		* *This program is free software; you can redistribute it and/or
		*modify it under the terms of the GNU General Public License *as
		published by the Free Software Foundation; either version 2 *of the
		License, or (at your option) any later version. * *This program is
		distributed in the hope that it will be useful, *but WITHOUT ANY
		WARRANTY; without even the implied warranty of *MERCHANTABILITY or
		FITNESS FOR A PARTICULAR PURPOSE. See the *GNU General Public License
		for more details. * *You should have received a copy of the GNU
		General Public License *along with this program; if not, write to the
		Free Software *Foundation, Inc., 59 Temple Place - Suite 330, Boston,
		MA 02111-1307, USA. * */
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
				<style:style style:name="P1" style:family="paragraph"
					style:parent-style-name="Text_20_body" style:list-style-name="L1" />
				<style:style style:name="P3" style:family="paragraph"
					style:parent-style-name="Standard">
					<style:paragraph-properties
						fo:text-align="center" style:justify-single-word="false" />
				</style:style>
				<style:style style:name="P4" style:family="paragraph"
					style:parent-style-name="Standard">
					<style:paragraph-properties
						fo:text-align="end" style:justify-single-word="false" />
				</style:style>
				<style:style style:name="P5" style:family="paragraph"
					style:parent-style-name="Standard">
					<style:paragraph-properties
						fo:text-align="justify" style:justify-single-word="false" />
				</style:style>
				<style:style style:name="T1" style:family="text">
					<style:text-properties fo:font-weight="bold"
						style:font-weight-asian="bold" style:font-weight-complex="bold" />
				</style:style>
				<style:style style:name="T2" style:family="text">
					<style:text-properties fo:font-style="italic"
						style:font-style-asian="italic" style:font-style-complex="italic" />
				</style:style>
				<style:style style:name="T3" style:family="text">
					<style:text-properties
						style:text-underline-style="solid" style:text-underline-width="auto"
						style:text-underline-color="font-color" />
				</style:style>
				<text:list-style style:name="L1">
					<text:list-level-style-bullet
						text:level="1" text:style-name="Bullet_20_Symbols"
						style:num-suffix="." text:bullet-char="●">
						<style:list-level-properties
							text:space-before="0.635cm" text:min-label-width="0.635cm" />
						<style:text-properties style:font-name="StarSymbol" />
					</text:list-level-style-bullet>
					<text:list-level-style-bullet
						text:level="2" text:style-name="Bullet_20_Symbols"
						style:num-suffix="." text:bullet-char="○">
						<style:list-level-properties
							text:space-before="1.27cm" text:min-label-width="0.635cm" />
						<style:text-properties style:font-name="StarSymbol" />
					</text:list-level-style-bullet>
					<text:list-level-style-bullet
						text:level="3" text:style-name="Bullet_20_Symbols"
						style:num-suffix="." text:bullet-char="■">
						<style:list-level-properties
							text:space-before="1.905cm" text:min-label-width="0.635cm" />
						<style:text-properties style:font-name="StarSymbol" />
					</text:list-level-style-bullet>
					<text:list-level-style-bullet
						text:level="4" text:style-name="Bullet_20_Symbols"
						style:num-suffix="." text:bullet-char="●">
						<style:list-level-properties
							text:space-before="2.54cm" text:min-label-width="0.635cm" />
						<style:text-properties style:font-name="StarSymbol" />
					</text:list-level-style-bullet>
					<text:list-level-style-bullet
						text:level="5" text:style-name="Bullet_20_Symbols"
						style:num-suffix="." text:bullet-char="○">
						<style:list-level-properties
							text:space-before="3.175cm" text:min-label-width="0.635cm" />
						<style:text-properties style:font-name="StarSymbol" />
					</text:list-level-style-bullet>
					<text:list-level-style-bullet
						text:level="6" text:style-name="Bullet_20_Symbols"
						style:num-suffix="." text:bullet-char="■">
						<style:list-level-properties
							text:space-before="3.81cm" text:min-label-width="0.635cm" />
						<style:text-properties style:font-name="StarSymbol" />
					</text:list-level-style-bullet>
					<text:list-level-style-bullet
						text:level="7" text:style-name="Bullet_20_Symbols"
						style:num-suffix="." text:bullet-char="●">
						<style:list-level-properties
							text:space-before="4.445cm" text:min-label-width="0.635cm" />
						<style:text-properties style:font-name="StarSymbol" />
					</text:list-level-style-bullet>
					<text:list-level-style-bullet
						text:level="8" text:style-name="Bullet_20_Symbols"
						style:num-suffix="." text:bullet-char="○">
						<style:list-level-properties
							text:space-before="5.08cm" text:min-label-width="0.635cm" />
						<style:text-properties style:font-name="StarSymbol" />
					</text:list-level-style-bullet>
					<text:list-level-style-bullet
						text:level="9" text:style-name="Bullet_20_Symbols"
						style:num-suffix="." text:bullet-char="■">
						<style:list-level-properties
							text:space-before="5.715cm" text:min-label-width="0.635cm" />
						<style:text-properties style:font-name="StarSymbol" />
					</text:list-level-style-bullet>
					<text:list-level-style-bullet
						text:level="10" text:style-name="Bullet_20_Symbols"
						style:num-suffix="." text:bullet-char="●">
						<style:list-level-properties
							text:space-before="6.35cm" text:min-label-width="0.635cm" />
						<style:text-properties style:font-name="StarSymbol" />
					</text:list-level-style-bullet>
				</text:list-style>
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

	<xsl:template match="node">
		<xsl:variable name="depth">
			<xsl:apply-templates select=".." mode="depthMesurement" />
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="$depth=0"><!-- Title -->
				<xsl:call-template name="output-nodecontent">
					<xsl:with-param name="style">
						Title
					</xsl:with-param>
				</xsl:call-template>
				<xsl:apply-templates select="hook|@LINK" />
				<xsl:call-template name="output-notecontent"> <xsl:with-param name="contentType" select="'DETAILS'"/> </xsl:call-template>
				<xsl:call-template name="output-notecontent"> <xsl:with-param name="contentType" select="'NOTE'"/> </xsl:call-template>
				<xsl:apply-templates select="node" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when test="ancestor::node[@FOLDED='true']">
						<text:list text:style-name="L1">
							<text:list-item>
								<xsl:call-template name="output-nodecontent">
									<xsl:with-param name="style">
										Standard
									</xsl:with-param>
								</xsl:call-template>
								<xsl:apply-templates select="hook|@LINK" />
								<xsl:call-template name="output-notecontent">
									<xsl:with-param name="contentType" select="'DETAILS'" />
								</xsl:call-template>
								<xsl:call-template name="output-notecontent">
									<xsl:with-param name="contentType" select="'NOTE'" />
								</xsl:call-template>
								<xsl:apply-templates select="node" />
							</text:list-item>
						</text:list>
					</xsl:when>
					<xsl:otherwise>
						<xsl:variable name="heading_level">
							<xsl:text>Heading_20_</xsl:text>
							<xsl:value-of select="$depth" />
						</xsl:variable>
						<xsl:element name="text:h">
							<xsl:attribute name="text:style-name"><!--
								--><xsl:value-of
								select="$heading_level" /><!--
							--></xsl:attribute>
							<xsl:attribute name="text:outline-level"><xsl:value-of
								select="$depth" /></xsl:attribute>
							<xsl:call-template name="output-nodecontent">
								<!--No Style for Headings.-->
								<xsl:with-param name="style"></xsl:with-param>
							</xsl:call-template>
						</xsl:element>
						<xsl:apply-templates select="hook|@LINK" />
						<xsl:call-template name="output-notecontent"> <xsl:with-param name="contentType" select="'DETAILS'"/> </xsl:call-template>
						<xsl:call-template name="output-notecontent"> <xsl:with-param name="contentType" select="'NOTE'"/> </xsl:call-template>
						<xsl:apply-templates select="node" />
					</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>

	</xsl:template>

	<xsl:template match="hook" />

	<!--
		<xsl:template
		match="hook[@NAME='accessories/plugins/NodeNote.properties']">
		<xsl:choose> <xsl:when test="./text"> <text:p
		text:style-name="Standard"> <xsl:value-of select="./text"/> </text:p>
		</xsl:when> </xsl:choose> </xsl:template> <xsl:template match="node"
		mode="childoutputOrdered"> <xsl:param name="nodeText"></xsl:param>
		<text:ordered-list text:style-name="L1"
		text:continue-numbering="true"> <text:list-item> <xsl:apply-templates
		select=".." mode="childoutputOrdered"> <xsl:with-param
		name="nodeText"><xsl:copy-of select="$nodeText"/></xsl:with-param>
		</xsl:apply-templates> </text:list-item> </text:ordered-list>
		</xsl:template> <xsl:template match="map" mode="childoutputOrdered">
		<xsl:param name="nodeText"></xsl:param> <xsl:copy-of
		select="$nodeText"/> </xsl:template>
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
		<text:p text:style-name="Standard">
			<xsl:element name="text:a" namespace="text">
				<xsl:attribute namespace="xlink" name="xlink:type">simple</xsl:attribute>
				<xsl:attribute namespace="xlink" name="xlink:href"><xsl:value-of
					select="." />
				</xsl:attribute>
				<xsl:value-of select="." />
			</xsl:element>
		</text:p>
	</xsl:template>

	<xsl:template name="output-nodecontent">
		<xsl:param name="style">
			Standard
		</xsl:param>
		<xsl:choose>
			<xsl:when test="richcontent[@TYPE='NODE']">
				<xsl:apply-templates select="richcontent[@TYPE='NODE']/html/body"
					mode="richcontent">
					<xsl:with-param name="style" select="$style" />
				</xsl:apply-templates>
			</xsl:when>
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when test="$style = ''">
						<!--no style for headings. -->
						<xsl:call-template name="textnode" />
					</xsl:when>
					<xsl:otherwise>
						<xsl:element name="text:p">
							<xsl:attribute name="text:style-name"><xsl:value-of
								select="$style" /></xsl:attribute>
							<xsl:call-template name="textnode" />
						</xsl:element>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template> <!-- xsl:template name="output-nodecontent" -->

	<xsl:template name="output-notecontent">
	<xsl:param name="contentType"/>
		<xsl:if test="richcontent[@TYPE=$contentType]">
			<xsl:apply-templates select="richcontent[@TYPE=$contentType]/html/body"
				mode="richcontent">
				<xsl:with-param name="style">
					Standard
				</xsl:with-param>
			</xsl:apply-templates>
		</xsl:if>
	</xsl:template> <!-- xsl:template name="output-note" -->


	<xsl:template name="textnode">
		<xsl:call-template name="format_text">
			<xsl:with-param name="nodetext">
				<xsl:choose>
					<xsl:when test="@TEXT = ''">
						<xsl:text> </xsl:text>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="@TEXT" />
					</xsl:otherwise>
				</xsl:choose>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template> <!-- xsl:template name="textnode" -->


	<!-- replace ASCII line breaks through ODF line breaks (br) -->
	<xsl:template name="format_text">
		<xsl:param name="nodetext"></xsl:param>
		<xsl:if test="string-length(substring-after($nodetext,'&#xa;')) = 0">
			<xsl:value-of select="$nodetext" />
		</xsl:if>
		<xsl:if test="string-length(substring-after($nodetext,'&#xa;')) > 0">
			<xsl:value-of select="substring-before($nodetext,'&#xa;')" />
			<text:line-break />
			<xsl:call-template name="format_text">
				<xsl:with-param name="nodetext">
					<xsl:value-of select="substring-after($nodetext,'&#xa;')" />
				</xsl:with-param>
			</xsl:call-template>
		</xsl:if>
	</xsl:template> <!-- xsl:template name="format_text" -->

	<xsl:template match="body" mode="richcontent">
		<xsl:param name="style">
			Standard
		</xsl:param>
		<!--       <xsl:copy-of select="string(.)"/> -->
		<xsl:apply-templates select="text()|*" mode="richcontent">
			<xsl:with-param name="style" select="$style"></xsl:with-param>
		</xsl:apply-templates>
	</xsl:template>
	<xsl:template match="text()" mode="richcontent">
		<xsl:copy-of select="string(.)" />
	</xsl:template>
	<xsl:template match="br" mode="richcontent">
		<text:line-break />
	</xsl:template>
	<xsl:template match="b" mode="richcontent">
		<xsl:param name="style">
			Standard
		</xsl:param>
		<text:span text:style-name="T1">
			<xsl:apply-templates select="text()|*" mode="richcontent">
				<xsl:with-param name="style" select="$style"></xsl:with-param>
			</xsl:apply-templates>
		</text:span>
	</xsl:template>
	<xsl:template match="p" mode="richcontent">
		<xsl:param name="style">
			Standard
		</xsl:param>
		<xsl:choose>
			<xsl:when test="$style = ''">
				<xsl:apply-templates select="text()|*" mode="richcontent">
					<xsl:with-param name="style" select="$style"></xsl:with-param>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:when test="@style='text-align: center'">
				<text:p text:style-name="P3">
					<xsl:apply-templates select="text()|*" mode="richcontent">
						<xsl:with-param name="style" select="$style"></xsl:with-param>
					</xsl:apply-templates>
				</text:p>
			</xsl:when>
			<xsl:when test="@style='text-align: right'">
				<text:p text:style-name="P4">
					<xsl:apply-templates select="text()|*" mode="richcontent">
						<xsl:with-param name="style" select="$style"></xsl:with-param>
					</xsl:apply-templates>
				</text:p>
			</xsl:when>
			<xsl:when test="@style='text-align: justify'">
				<text:p text:style-name="P5">
					<xsl:apply-templates select="text()|*" mode="richcontent">
						<xsl:with-param name="style" select="$style"></xsl:with-param>
					</xsl:apply-templates>
				</text:p>
			</xsl:when>
			<xsl:otherwise>
				<xsl:element name="text:p">
					<xsl:attribute name="text:style-name"><xsl:value-of
						select="$style" /></xsl:attribute>
					<xsl:apply-templates select="text()|*" mode="richcontent">
						<xsl:with-param name="style" select="$style"></xsl:with-param>
					</xsl:apply-templates>
				</xsl:element>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="i" mode="richcontent">
		<xsl:param name="style">
			Standard
		</xsl:param>
		<text:span text:style-name="T2">
			<xsl:apply-templates select="text()|*" mode="richcontent">
				<xsl:with-param name="style" select="$style"></xsl:with-param>
			</xsl:apply-templates>
		</text:span>
	</xsl:template>
	<xsl:template match="u" mode="richcontent">
		<xsl:param name="style">
			Standard
		</xsl:param>
		<text:span text:style-name="T3">
			<xsl:apply-templates select="text()|*" mode="richcontent">
				<xsl:with-param name="style" select="$style"></xsl:with-param>
			</xsl:apply-templates>
		</text:span>
	</xsl:template>
	<xsl:template match="ul" mode="richcontent">
		<xsl:param name="style">
			Standard
		</xsl:param>
		<text:list text:style-name="L1">
			<xsl:apply-templates select="text()|*" mode="richcontentul">
				<xsl:with-param name="style" select="$style"></xsl:with-param>
			</xsl:apply-templates>
		</text:list>
		<text:p text:style-name="P3" />
	</xsl:template>
	<xsl:template match="ol" mode="richcontent">
		<xsl:param name="style">
			Standard
		</xsl:param>
		<text:list text:style-name="L2">
			<xsl:apply-templates select="text()|*" mode="richcontentol">
				<xsl:with-param name="style" select="$style"></xsl:with-param>
			</xsl:apply-templates>
		</text:list>
		<text:p text:style-name="P3" />
	</xsl:template>
	<xsl:template match="li" mode="richcontentul">
		<xsl:param name="style">
			Standard
		</xsl:param>
		<text:list-item>
			<text:p text:style-name="P1"><!--
			-->
				<xsl:apply-templates select="text()|*" mode="richcontent">
					<xsl:with-param name="style" select="$style"></xsl:with-param>
				</xsl:apply-templates><!--			
		-->
			</text:p>
		</text:list-item>
	</xsl:template>
	<xsl:template match="li" mode="richcontentol">
		<xsl:param name="style">
			Standard
		</xsl:param>
		<text:list-item>
			<text:p text:style-name="P2"><!--
			-->
				<xsl:apply-templates select="text()|*" mode="richcontent">
					<xsl:with-param name="style" select="$style"></xsl:with-param>
				</xsl:apply-templates><!--			
		-->
			</text:p>
		</text:list-item>
	</xsl:template>
	
	<xsl:template match="a" mode="richcontent">
		<xsl:element name="text:a" namespace="text">
			<xsl:attribute namespace="xlink" name="xlink:type">simple</xsl:attribute>
			<xsl:attribute namespace="xlink" name="xlink:href"><xsl:value-of
				select="@href" />
			</xsl:attribute>
			<xsl:apply-templates select="text()" />
		</xsl:element>
	</xsl:template>
	

	<!--
		<text:list-item> <text:p text:style-name="P1">b </text:list-item>
		<text:list-item> <text:p text:style-name="P1">c</text:p>
		</text:list-item> <text:p text:style-name="P2"/>
	-->
	<!--
		<text:ordered-list text:style-name="L2"> <text:list-item> <text:p
		text:style-name="P3">1</text:p> </text:list-item> <text:list-item>
		<text:p text:style-name="P3">2</text:p> </text:list-item>
		<text:list-item> <text:p text:style-name="P3">3</text:p>
		</text:list-item> </text:ordered-list> <text:p text:style-name="P2"/>
	-->
	<!--
		Table: <table:table table:name="Table1" table:style-name="Table1">
		<table:table-column table:style-name="Table1.A"
		table:number-columns-repeated="3"/> <table:table-row>
		<table:table-cell table:style-name="Table1.A1"
		table:value-type="string"> <text:p text:style-name="Table
		Contents">T11</text:p> </table:table-cell> <table:table-cell
		table:style-name="Table1.A1" table:value-type="string"> <text:p
		text:style-name="Table Contents">T21</text:p> </table:table-cell>
		<table:table-cell table:style-name="Table1.C1"
		table:value-type="string"> <text:p text:style-name="Table
		Contents">T31</text:p> </table:table-cell> </table:table-row>
		<table:table-row> <table:table-cell table:style-name="Table1.A2"
		table:value-type="string"> <text:p text:style-name="Table
		Contents">T12</text:p> </table:table-cell> <table:table-cell
		table:style-name="Table1.A2" table:value-type="string"> <text:p
		text:style-name="Table Contents">T22</text:p> </table:table-cell>
		<table:table-cell table:style-name="Table1.C2"
		table:value-type="string"> <text:p text:style-name="Table
		Contents">T32</text:p> </table:table-cell> </table:table-row>
		<table:table-row> <table:table-cell table:style-name="Table1.A2"
		table:value-type="string"> <text:p text:style-name="Table
		Contents">T13</text:p> </table:table-cell> <table:table-cell
		table:style-name="Table1.A2" table:value-type="string"> <text:p
		text:style-name="Table Contents">T23</text:p> </table:table-cell>
		<table:table-cell table:style-name="Table1.C2"
		table:value-type="string"> <text:p text:style-name="Table
		Contents">T32</text:p> </table:table-cell> </table:table-row>
		</table:table>
	-->


</xsl:stylesheet>
