<?xml version='1.0' encoding='ISO-8859-1'?>
<xsl:stylesheet version='1.0'
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>
	<xsl:output media-type='text/xml'  method="xml" indent="yes" encoding="utf-8"/>
	<xsl:template match='/'>
		<opml version='1.0'>
			<head>
				<expansionState>/opml[1]/body[1]/outline[1]</expansionState>
				<vertScrollState>1</vertScrollState>
			</head>
			<xsl:apply-templates select='map' />
		</opml>
	</xsl:template>
	<xsl:template match='map'>
		<body>
			<xsl:apply-templates select='node' />
		</body>
	</xsl:template>


	<xsl:template match='node[hook[@NAME="FirstGroupNode" or @NAME="SummaryNode"]]'>
		<xsl:apply-templates select='node' />
	</xsl:template>
	
	<xsl:template match='node'>
		<outline>
			<xsl:attribute name='text'>
				<xsl:value-of select="normalize-space(translate(@TEXT, '&#160;', ' '))" />
				<xsl:apply-templates select="richcontent[@TYPE='NODE']"/>
			</xsl:attribute>
			<xsl:if test='@LINK and not(starts-with(@LINK,"#"))'>
				<xsl:attribute name='type'>link</xsl:attribute>
				<xsl:attribute name='url'><xsl:value-of select='@LINK' /></xsl:attribute>
			</xsl:if>
			<xsl:apply-templates select='node' />
		</outline>
	</xsl:template>
	
	<xsl:template match="richcontent">
		<xsl:value-of select="normalize-space(translate(., '&#160;', ' '))" />
	</xsl:template>

</xsl:stylesheet>