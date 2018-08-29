<?xml version="1.0" encoding="UTF-8" ?>

	<!--
	
			MINDMAPEXPORTFILTER txt %xslt_export.text
	
		: This code released under the GPL. :
		(http://www.gnu.org/copyleft/gpl.html) Document : mm2text.xsl Created
		on : 01 February 2004, 17:17 Author : joerg feuerhake
		joerg.feuerhake@free-penguin.org Description: transforms freeplane mm
		format to html, handles crossrefs and adds numbering. feel free to
		customize it while leaving the ancient authors mentioned. thank you
		ChangeLog: See: http://freeplane.sourceforge.net/
	-->

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="text" indent="no"/>
	<xsl:strip-space elements="map node" />
	<xsl:key name="refid" match="node" use="@ID" />

	<xsl:template match="/">
		<xsl:text>#MindMapExport FreeplaneVersion:</xsl:text>
		<xsl:value-of select="map/@version" />
		<xsl:text>&#xA;</xsl:text>
		<xsl:text>&#xA;</xsl:text>
		<xsl:apply-templates />
	</xsl:template>

	<xsl:template match="/map">
		<xsl:apply-templates select="node"/>
	</xsl:template>
	
	<xsl:template match="richcontent[normalize-space(.) != '']">
		<xsl:if test="@TYPE='DETAILS'">
			<xsl:text>DETAILS:&#xA;</xsl:text>
		</xsl:if>
		<xsl:if test="@TYPE='NOTE'">
			<xsl:text>NOTE:&#xA;</xsl:text>
		</xsl:if>
		<xsl:apply-templates/>
		<xsl:text>&#xA;</xsl:text>
	</xsl:template>

	<xsl:template match="child::text()">
		<xsl:value-of select="normalize-space(.)" />
	</xsl:template>

	<xsl:template match="p|br|tr|div|li|pre">
		<xsl:if test="preceding-sibling::*">
			<xsl:text>&#xA;</xsl:text>
		</xsl:if>
		<xsl:apply-templates/>
	</xsl:template>

	<xsl:template match='node[hook[@NAME="FirstGroupNode" or @NAME="SummaryNode"]]'>
		<xsl:apply-templates select='node' />
	</xsl:template>
	
	<xsl:template match='node'>
		<xsl:variable name="thisid" select="@ID" />
		<xsl:variable name="target" select="arrowlink/@DESTINATION" />
		<xsl:number level="multiple" count="node" format="1" />
		<xsl:text> </xsl:text>
		<xsl:if test="@TEXT">
			<xsl:value-of select="normalize-space(@TEXT)" />
			<xsl:text>&#xA;</xsl:text>
    	</xsl:if>
		<xsl:apply-templates select="richcontent[@TYPE='NODE']"/>
		<xsl:apply-templates select="richcontent[@TYPE='DETAILS']"/>
		<xsl:apply-templates select="richcontent[@TYPE='NOTE']"/>
		<xsl:if test="arrowlink/@DESTINATION != ''">
			<xsl:text> (see:</xsl:text>
			<xsl:for-each select="key('refid', $target)">
				<xsl:value-of select="@TEXT" />
			</xsl:for-each>
			<xsl:text>)</xsl:text>
		</xsl:if>
		<xsl:apply-templates select="node"/>
	</xsl:template>

</xsl:stylesheet> 
