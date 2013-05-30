<?xml version="1.0" standalone="no" ?>
	<!--
		: mm2xbel.xsl : XSL stylesheet to convert from Mindmap to XBEL : :

		MINDMAPEXPORTFILTER xbel XBEL

		This code released under the GPL. :
		(http://www.gnu.org/copyleft/gpl.html) : : William McVey
		<wam@cisco.com> : September 11, 2003 : : $Id: mm2xbel.xsl,v 1.1.34.1
		2007/04/20 20:31:31 christianfoltin Exp $ :
	-->

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:strip-space elements="*" />
	<xsl:output method="xml" indent="yes" encoding="UTF-8" />

	<xsl:template match="/map/node">
		<xbel version="1.0" folded="no">
			<title>
				<xsl:value-of select="@TEXT" />
			</title>
			<xsl:for-each select="node">
				<xsl:call-template name="node" />
			</xsl:for-each>
		</xbel>
	</xsl:template>

	<xsl:template name="node">
		<xsl:if test="string-length(@LINK) &gt; 0">
			<bookmark>
				<xsl:attribute name="href">
					<xsl:value-of select="@LINK" />
				</xsl:attribute>
				<title>
					<xsl:value-of select="@TEXT" />
				</title>
			</bookmark>
		</xsl:if>
		<xsl:if test="string-length(@LINK) = 0">
			<folder>
				<title>
					<xsl:value-of select="@TEXT" />
				</title>
				<xsl:for-each select="node">
					<xsl:call-template name="node" />
				</xsl:for-each>
			</folder>
		</xsl:if>
	</xsl:template>

</xsl:stylesheet>
