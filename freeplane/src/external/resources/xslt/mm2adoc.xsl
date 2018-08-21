<?xml version="1.0" encoding="UTF-8" ?>

<!--
	MINDMAPEXPORTFILTER adoc Asciidoc

	: This code released under the GPL. : (http://www.gnu.org/copyleft/gpl.html) 
	Document : mm2adoc.xsl 
	Created on : 24 Nov 2013 
	Author : Jean-Marc Meessen (jean-marc@meessen-web.org)
	Description: 
		transforms freeplane mm format to Asciidoc form. 
		based on the mm2html converter by Joerg Feuerhake
		(joerg.feuerhake@free-penguin.org). 
		Feel free to customize it while leaving the ancient authors mentioned. 
		Thank you
	ChangeLog: See: http://freeplane.sourceforge.net/
-->

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="text" indent="no"/>
	<xsl:strip-space elements="map node" />
	<xsl:key name="refid" match="node" use="@ID" />

	<xsl:template match="/">
		<xsl:apply-templates />
	</xsl:template>

	<xsl:template match="/map">
		<xsl:apply-templates select="node"/>
	</xsl:template>

	<xsl:template match="richcontent">
		<xsl:if test="@TYPE='NOTE'">
			<xsl:text>NOTE: </xsl:text>
		</xsl:if>
		<xsl:apply-templates/>
		<xsl:text>&#xA;</xsl:text>
	</xsl:template>

	<xsl:template match="child::text()">
		<xsl:value-of select="translate(., '&#160;&#xA;&#xD;', '  ')" />
	</xsl:template>

	<xsl:template match="p|br|tr|div|li|pre">
		<xsl:if test="preceding-sibling::*">
			<xsl:text>&#xA;</xsl:text>
		</xsl:if>
		<xsl:apply-templates/>
	</xsl:template>

	<xsl:template match="li">
		<xsl:text>&#xA;* </xsl:text>
		<xsl:apply-templates/>
	</xsl:template>


	<xsl:template match='node[hook[@NAME="FirstGroupNode" or @NAME="SummaryNode"]]'>
		<xsl:apply-templates select='node' />
	</xsl:template>
	
	<xsl:template match='node'>
		<xsl:variable name="thisid" select="@ID" />

		<xsl:variable name="depth">
			<xsl:apply-templates select=".." mode="depthMesurement" />
		</xsl:variable>

		<xsl:choose>
			<xsl:when test="$depth = 0">
				<xsl:text>= </xsl:text> 
			</xsl:when>
			<xsl:when test="$depth = 1">
				<xsl:text>&#xA;== </xsl:text>
			</xsl:when>
			<xsl:when test="$depth = 2">
				<xsl:text>&#xA;=== </xsl:text>
			</xsl:when>
			<xsl:when test="$depth = 3">
				<xsl:text>&#xA;==== </xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>? </xsl:text>
			</xsl:otherwise>
		</xsl:choose>


		<xsl:if test="@TEXT">
			<xsl:value-of select="normalize-space(@TEXT)" />
			<xsl:text>&#xA;&#xA;</xsl:text>
    	</xsl:if>
		<xsl:apply-templates select="richcontent[@TYPE='NODE']"/>
		<xsl:apply-templates select="richcontent[@TYPE='DETAILS']"/>
		<xsl:apply-templates select="richcontent[@TYPE='NOTE']"/>
		<xsl:apply-templates select="node"/>
	</xsl:template>

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

</xsl:stylesheet>
