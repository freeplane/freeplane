<?xml version="1.0" encoding="UTF-8" ?>
	<!--

		MINDMAPEXPORTFILTER doc;xml %xslt_export.ms_word 
		
		(c) by Naoki Nose, 2006, and Eric Lavarde, 2008 This code is licensed under
		the GPLv2 or later. (http://www.gnu.org/copyleft/gpl.html) Check
		'mm2wordml_utf8_TEMPLATE.mm' for detailed instructions on how to use
		this sheet.
	-->
<xsl:stylesheet version="1.0"
	xmlns:w="http://schemas.microsoft.com/office/word/2003/wordml" xmlns:v="urn:schemas-microsoft-com:vml"
	xmlns:w10="urn:schemas-microsoft-com:office:word" xmlns:sl="http://schemas.microsoft.com/schemaLibrary/2003/core"
	xmlns:aml="http://schemas.microsoft.com/aml/2001/core" xmlns:wx="http://schemas.microsoft.com/office/word/2003/auxHint"
	xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:dt="uuid:C2F41010-65B3-11d1-A29F-00AA00C14882"
	w:macrosPresent="no" w:embeddedObjPresent="no" w:ocxPresent="no"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"
		standalone="yes" />

	<!--
		the variable to be used to determine the maximum level of headings, it
		is defined by the attribute 'head-maxlevel' of the root node if it
		exists, else it's the default 4 (maximum possible is 9)
	-->
	<xsl:variable name="maxlevel">
		<xsl:choose>
			<xsl:when test="//map/node/attribute[@NAME='head-maxlevel']">
				<xsl:value-of select="//map/node/attribute[@NAME='head-maxlevel']/@VALUE" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="'4'" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>

	<xsl:template match="/">
		<xsl:processing-instruction name="mso-application">
			progid="Word.Document"
		</xsl:processing-instruction>
		<w:wordDocument>
			<xsl:apply-templates mode="DocumentProperties" />
			<xsl:call-template name="output-styles" />
			<w:body>
				<wx:sect>
					<xsl:apply-templates mode="heading" />
				</wx:sect>
			</w:body>
		</w:wordDocument>
	</xsl:template>

	<!--
		the 2 following templates transform the doc-* attributes from the root
		node into document properties
	-->
	<xsl:template match="//map" mode="DocumentProperties">
		<o:DocumentProperties>
			<o:Title>
				<xsl:value-of select="node/@TEXT" />
			</o:Title>
			<xsl:apply-templates select="node/attribute">
				<xsl:with-param name="prefix" select="'doc'" />
			</xsl:apply-templates>
		</o:DocumentProperties>
	</xsl:template>

	<xsl:template match="attribute">
		<xsl:param name="prefix" />
		<xsl:if test="starts-with(@NAME,concat($prefix,'-'))">
			<xsl:element
				name="{concat('o:',substring-after(@NAME,concat($prefix,'-')))}">
				<xsl:value-of select="@VALUE" />
			</xsl:element>
		</xsl:if>
	</xsl:template>

	<!-- output each node as heading -->
	<xsl:template match="node" mode="heading">
		<xsl:param name="level" select="0" />
		<xsl:choose> <!-- we change our mind if the NoHeading attribute is present -->
			<xsl:when test="attribute/@NAME = 'NoHeading'">
				<xsl:apply-templates select="." />
			</xsl:when>
			<xsl:otherwise>
				<wx:sub-section>
					<w:p>
						<w:pPr>
							<xsl:choose>
								<xsl:when test="$level = 0">
									<w:pStyle w:val="Title" />
								</xsl:when>
								<xsl:otherwise>
									<w:pStyle w:val="Heading{$level}" />
								</xsl:otherwise>
							</xsl:choose>
						</w:pPr>
						<w:r>
							<w:t>
								<xsl:call-template name="output-node-core" />
							</w:t>
						</w:r>
					</w:p>
					<xsl:call-template name="output-added-richcontent"><xsl:with-param name="contentType" select="'DETAILS'"/></xsl:call-template>
					<xsl:call-template name="output-added-richcontent"><xsl:with-param name="contentType" select="'NOTE'"/></xsl:call-template>
					<!--
						if the level is higher than maxlevel, or if the current node is
						marked with LastHeading, we start outputting normal paragraphs,
						else we loop back into the heading mode
					-->
					<xsl:choose>
						<xsl:when test="attribute/@NAME = 'LastHeading'">
							<xsl:apply-templates select="node" />
						</xsl:when>
						<xsl:when test="$level &lt; $maxlevel">
							<xsl:apply-templates select="node" mode="heading">
								<xsl:with-param name="level" select="$level + 1" />
							</xsl:apply-templates>
						</xsl:when>
						<xsl:otherwise>
							<xsl:apply-templates select="node" />
						</xsl:otherwise>
					</xsl:choose>
				</wx:sub-section>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- output each node as normal paragraph -->
	<xsl:template match="node">
		<w:p>
			<w:pPr>
				<w:pStyle w:val="Normal" />
			</w:pPr>
			<w:r>
				<w:t>
					<xsl:call-template name="output-node-core" />
				</w:t>
			</w:r>
		</w:p>
		<xsl:call-template name="output-added-richcontent"><xsl:with-param name="contentType" select="'DETAILS'"/></xsl:call-template>
		<xsl:call-template name="output-added-richcontent"><xsl:with-param name="contentType" select="'NOTE'"/></xsl:call-template>
		<xsl:apply-templates select="node" />
	</xsl:template>

	<xsl:template name="output-node-core">
		<xsl:choose>
			<xsl:when test="@TEXT">
				<xsl:value-of select="normalize-space(@TEXT)" />
			</xsl:when>
			<xsl:when test="richcontent[@TYPE='NODE']">
				<xsl:value-of select="normalize-space(richcontent[@TYPE='NODE']/html/body)" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:text></xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="output-added-richcontent">
		<xsl:param name="contentType"></xsl:param>
		<xsl:if test="richcontent[@TYPE=$contentType]">
			<w:p>
				<w:pPr>
					<w:pStyle w:val="BodyText" />
				</w:pPr>
				<w:r>
					<w:t>
						<xsl:value-of select="string(richcontent[@TYPE=$contentType]/html/body)" />
					</w:t>
				</w:r>
			</w:p>
		</xsl:if>
	</xsl:template>

	<!--
		The following is a very long template just to output the necessary
		styles, this is the part you should edit if you'd like different
		default styles.
	-->

	<xsl:template name="output-styles">
		<w:styles>
			<w:versionOfBuiltInStylenames w:val="4" />
			<w:latentStyles w:defLockedState="off"
				w:latentStyleCount="156" />

			<w:style w:type="paragraph" w:default="on" w:styleId="Normal">
				<w:name w:val="Normal" />
				<w:rsid w:val="00831C9D" />
				<w:rPr>
					<wx:font wx:val="Times New Roman" />
					<w:sz w:val="24" />
					<w:sz-cs w:val="24" />
					<w:lang w:val="EN-US" w:fareast="EN-US" w:bidi="AR-SA" />
				</w:rPr>
			</w:style>

			<w:style w:type="paragraph" w:styleId="Heading1">
				<w:name w:val="heading 1" />
				<wx:uiName wx:val="Heading 1" />
				<w:basedOn w:val="Normal" />
				<w:next w:val="Normal" />
				<w:rsid w:val="00BA7540" />
				<w:pPr>
					<w:pStyle w:val="Heading1" />
					<w:keepNext />
					<w:spacing w:before="240" w:after="60" />
					<w:outlineLvl w:val="0" />
				</w:pPr>
				<w:rPr>
					<w:rFonts w:ascii="Arial" w:h-ansi="Arial" w:cs="Arial" />
					<wx:font wx:val="Arial" />
					<w:b />
					<w:b-cs />
					<w:kern w:val="32" />
					<w:sz w:val="32" />
					<w:sz-cs w:val="32" />
				</w:rPr>
			</w:style>

			<w:style w:type="paragraph" w:styleId="Heading2">
				<w:name w:val="heading 2" />
				<wx:uiName wx:val="Heading 2" />
				<w:basedOn w:val="Normal" />
				<w:next w:val="Normal" />
				<w:rsid w:val="00BA7540" />
				<w:pPr>
					<w:pStyle w:val="Heading2" />
					<w:keepNext />
					<w:spacing w:before="240" w:after="60" />
					<w:outlineLvl w:val="1" />
				</w:pPr>
				<w:rPr>
					<w:rFonts w:ascii="Arial" w:h-ansi="Arial" w:cs="Arial" />
					<wx:font wx:val="Arial" />
					<w:b />
					<w:b-cs />
					<w:i />
					<w:i-cs />
					<w:sz w:val="28" />
					<w:sz-cs w:val="28" />
				</w:rPr>
			</w:style>

			<w:style w:type="paragraph" w:styleId="Heading3">
				<w:name w:val="heading 3" />
				<wx:uiName wx:val="Heading 3" />
				<w:basedOn w:val="Normal" />
				<w:next w:val="Normal" />
				<w:rsid w:val="00BA7540" />
				<w:pPr>
					<w:pStyle w:val="Heading3" />
					<w:keepNext />
					<w:spacing w:before="240" w:after="60" />
					<w:outlineLvl w:val="2" />
				</w:pPr>
				<w:rPr>
					<w:rFonts w:ascii="Arial" w:h-ansi="Arial" w:cs="Arial" />
					<wx:font wx:val="Arial" />
					<w:b />
					<w:b-cs />
					<w:sz w:val="26" />
					<w:sz-cs w:val="26" />
				</w:rPr>
			</w:style>

			<w:style w:type="paragraph" w:styleId="Heading4">
				<w:name w:val="heading 4" />
				<wx:uiName wx:val="Heading 4" />
				<w:basedOn w:val="Normal" />
				<w:next w:val="Normal" />
				<w:rsid w:val="00BA7540" />
				<w:pPr>
					<w:pStyle w:val="Heading4" />
					<w:keepNext />
					<w:spacing w:before="240" w:after="60" />
					<w:outlineLvl w:val="3" />
				</w:pPr>
				<w:rPr>
					<wx:font wx:val="Times New Roman" />
					<w:b />
					<w:b-cs />
					<w:sz w:val="28" />
					<w:sz-cs w:val="28" />
				</w:rPr>
			</w:style>

			<w:style w:type="paragraph" w:styleId="Heading5">
				<w:name w:val="heading 5" />
				<wx:uiName wx:val="Heading 5" />
				<w:basedOn w:val="Normal" />
				<w:next w:val="Normal" />
				<w:rsid w:val="00BA7540" />
				<w:pPr>
					<w:pStyle w:val="Heading5" />
					<w:spacing w:before="240" w:after="60" />
					<w:outlineLvl w:val="4" />
				</w:pPr>
				<w:rPr>
					<wx:font wx:val="Times New Roman" />
					<w:b />
					<w:b-cs />
					<w:i />
					<w:i-cs />
					<w:sz w:val="26" />
					<w:sz-cs w:val="26" />
				</w:rPr>
			</w:style>

			<w:style w:type="paragraph" w:styleId="Heading6">
				<w:name w:val="heading 6" />
				<wx:uiName wx:val="Heading 6" />
				<w:basedOn w:val="Normal" />
				<w:next w:val="Normal" />
				<w:rsid w:val="00BA7540" />
				<w:pPr>
					<w:pStyle w:val="Heading6" />
					<w:spacing w:before="240" w:after="60" />
					<w:outlineLvl w:val="5" />
				</w:pPr>
				<w:rPr>
					<wx:font wx:val="Times New Roman" />
					<w:b />
					<w:b-cs />
					<w:sz w:val="22" />
					<w:sz-cs w:val="22" />
				</w:rPr>
			</w:style>

			<w:style w:type="paragraph" w:styleId="Heading7">
				<w:name w:val="heading 7" />
				<wx:uiName wx:val="Heading 7" />
				<w:basedOn w:val="Normal" />
				<w:next w:val="Normal" />
				<w:rsid w:val="00BA7540" />
				<w:pPr>
					<w:pStyle w:val="Heading7" />
					<w:spacing w:before="240" w:after="60" />
					<w:outlineLvl w:val="6" />
				</w:pPr>
				<w:rPr>
					<wx:font wx:val="Times New Roman" />
				</w:rPr>
			</w:style>

			<w:style w:type="paragraph" w:styleId="Heading8">
				<w:name w:val="heading 8" />
				<wx:uiName wx:val="Heading 8" />
				<w:basedOn w:val="Normal" />
				<w:next w:val="Normal" />
				<w:rsid w:val="00BA7540" />
				<w:pPr>
					<w:pStyle w:val="Heading8" />
					<w:spacing w:before="240" w:after="60" />
					<w:outlineLvl w:val="7" />
				</w:pPr>
				<w:rPr>
					<wx:font wx:val="Times New Roman" />
					<w:i />
					<w:i-cs />
				</w:rPr>
			</w:style>

			<w:style w:type="paragraph" w:styleId="Heading9">
				<w:name w:val="heading 9" />
				<wx:uiName wx:val="Heading 9" />
				<w:basedOn w:val="Normal" />
				<w:next w:val="Normal" />
				<w:rsid w:val="00BA7540" />
				<w:pPr>
					<w:pStyle w:val="Heading9" />
					<w:spacing w:before="240" w:after="60" />
					<w:outlineLvl w:val="8" />
				</w:pPr>
				<w:rPr>
					<w:rFonts w:ascii="Arial" w:h-ansi="Arial" w:cs="Arial" />
					<wx:font wx:val="Arial" />
					<w:sz w:val="22" />
					<w:sz-cs w:val="22" />
				</w:rPr>
			</w:style>

			<w:style w:type="character" w:default="on" w:styleId="DefaultParagraphFont">
				<w:name w:val="Default Paragraph Font" />
				<w:semiHidden />
			</w:style>

			<w:style w:type="table" w:default="on" w:styleId="TableNormal">
				<w:name w:val="Normal Table" />
				<wx:uiName wx:val="Table Normal" />
				<w:semiHidden />
				<w:rPr>
					<wx:font wx:val="Times New Roman" />
				</w:rPr>
				<w:tblPr>
					<w:tblInd w:w="0" w:type="dxa" />
					<w:tblCellMar>
						<w:top w:w="0" w:type="dxa" />
						<w:left w:w="108" w:type="dxa" />
						<w:bottom w:w="0" w:type="dxa" />
						<w:right w:w="108" w:type="dxa" />
					</w:tblCellMar>
				</w:tblPr>
			</w:style>

			<w:style w:type="list" w:default="on" w:styleId="NoList">
				<w:name w:val="No List" />
				<w:semiHidden />
			</w:style>

			<w:style w:type="paragraph" w:styleId="Title">
				<w:name w:val="Title" />
				<w:basedOn w:val="Normal" />
				<w:rsid w:val="00BA7540" />
				<w:pPr>
					<w:pStyle w:val="Title" />
					<w:spacing w:before="240" w:after="60" />
					<w:jc w:val="center" />
					<w:outlineLvl w:val="0" />
				</w:pPr>
				<w:rPr>
					<w:rFonts w:ascii="Arial" w:h-ansi="Arial" w:cs="Arial" />
					<wx:font wx:val="Arial" />
					<w:b />
					<w:b-cs />
					<w:kern w:val="28" />
					<w:sz w:val="32" />
					<w:sz-cs w:val="32" />
				</w:rPr>
			</w:style>

			<w:style w:type="paragraph" w:styleId="BodyText">
				<w:name w:val="Body Text" />
				<w:basedOn w:val="Normal" />
				<w:rsid w:val="00BA7540" />
				<w:pPr>
					<w:pStyle w:val="BodyText" />
					<w:spacing w:after="120" />
				</w:pPr>
				<w:rPr>
					<wx:font wx:val="Times New Roman" />
				</w:rPr>
			</w:style>

		</w:styles>
	</xsl:template>

</xsl:stylesheet> 
