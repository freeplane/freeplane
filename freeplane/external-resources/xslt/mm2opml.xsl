<?xml version='1.0' encoding='ISO-8859-1'?>
<xsl:stylesheet version='1.0'
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>
	<xsl:output media-type='text/xml' />
	<xsl:template match='/'>
		<opml version='1.0'>
			<head>
				<title>elbowarthros.opml</title>
				<dateCreated>Tue, 17 Apr 2001 18:35:55 GMT</dateCreated>
				<dateModified>Tue, 16 Mar 2004 20:29:12 </dateModified>
				<ownerName>none</ownerName>
				<ownerEmail />
				<expansionState>/opml[1]/body[1]/outline[1]</expansionState>
				<vertScrollState>1</vertScrollState>
				<windowTop>23</windowTop>
				<windowLeft>-13</windowLeft>
				<windowBottom>648</windowBottom>
				<windowRight>558</windowRight>
			</head>
			<xsl:apply-templates select='map' />
		</opml>
	</xsl:template>
	<xsl:template match='map'>
		<body>
			<xsl:apply-templates select='node' />
		</body>
	</xsl:template>
	<xsl:template match='node'>
		<xsl:choose>
			<xsl:when test='@LINK'>
				<outline>
					<xsl:attribute name='text'><xsl:value-of
						select='@TEXT' /></xsl:attribute>
					<xsl:attribute name='type'>link</xsl:attribute>
					<xsl:apply-templates select='node' />
				</outline>
			</xsl:when>
			<xsl:otherwise>
				<outline>
					<xsl:attribute name='text'><xsl:value-of
						select='@TEXT' /></xsl:attribute>
					<xsl:apply-templates select='node' />
				</outline>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>