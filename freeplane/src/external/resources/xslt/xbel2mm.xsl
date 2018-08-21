<?xml version="1.0" standalone="no" ?>
	<!--
		: xbel2mm.xsl : XSL stylesheet to convert from XBEL to Mindmap : :
		This code released under the GPL. :
		(http://www.gnu.org/copyleft/gpl.html) : : William McVey
		<wam@wamber.net> : September 11, 2003 : : $Id: xbel2mm.xsl,v 1.1
		2003/11/03 11:02:42 sviles Exp $ :
	-->

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:strip-space elements="*" />
	<xsl:output method="xml" indent="yes" encoding="us-ascii" />

	<xsl:template match="/xbel">
		<map>
			<node>
				<xsl:attribute name="TEXT">
				<xsl:value-of select="title" />
			</xsl:attribute>
				<xsl:apply-templates />
			</node>
		</map>
	</xsl:template>

	<xsl:template match="folder">
		<node>
			<xsl:attribute name="TEXT">
				<xsl:value-of select="title" />
			</xsl:attribute>
			<xsl:attribute name="FOLDED">
				<xsl:value-of select="@folded" />
			</xsl:attribute>
			<xsl:apply-templates />
		</node>
	</xsl:template>

	<xsl:template match="bookmark">
		<node>
			<xsl:attribute name="TEXT">
				<xsl:value-of select="title" />
			</xsl:attribute>
			<xsl:attribute name="LINK">
				<xsl:value-of select="@href" />
			</xsl:attribute>
		</node>
	</xsl:template>

	<xsl:template match="node()|@*" />


</xsl:stylesheet>
